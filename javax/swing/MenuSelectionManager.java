/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * A MenuSelectionManager owns the selection in menu hierarchy.
 * 
 * @version 1.24 02/06/02
 * @author Arnaud Weber
 */
public class MenuSelectionManager {
    private static final MenuSelectionManager instance = 
        new MenuSelectionManager();

    private Vector selection = new Vector();

    /* diagnostic aids -- should be false for production builds. */
    private static final boolean TRACE =   false; // trace creates and disposes
    private static final boolean VERBOSE = false; // show reuse hits/misses
    private static final boolean DEBUG =   false;  // show bad params, misc.

    /**
     * Returns the default menu selection manager.
     *
     * @return a MenuSelectionManager object
     */
    public static MenuSelectionManager defaultManager() {	
        return instance;
    }
    
    /**
     * Only one ChangeEvent is needed per button model instance since the
     * event's only state is the source property.  The source of events
     * generated is always "this".
     */
    protected transient ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Change the selection in the menu hierarchy.
     *
     * @param path  an array of MenuElement objects specifying the selected path
     */
    public void setSelectedPath(MenuElement[] path) {
        int i,c;
        int currentSelectionCount = selection.size();
        int firstDifference = 0;

        if(path == null) {
            path = new MenuElement[0];
        }

	if (DEBUG) {
	    System.out.print("Previous:  "); printMenuElementArray(getSelectedPath());
	    System.out.print("New:  "); printMenuElementArray(path);
	}

        for(i=0,c=path.length;i<c;i++) {
            if(i < currentSelectionCount && (MenuElement)selection.elementAt(i) == path[i]) 
                firstDifference++;
            else
                break;
        }

        for(i=currentSelectionCount - 1 ; i >= firstDifference ; i--) {
            ((MenuElement)selection.elementAt(i)).menuSelectionChanged(false);
            selection.removeElementAt(i);
        }

        for(i = firstDifference, c = path.length ; i < c ; i++) {
	    if (path[i] != null) {
		path[i].menuSelectionChanged(true);
		selection.addElement(path[i]);
	    }        
	}

	fireStateChanged();
    }

    /**
     * Returns the path to the currently selected menu item
     *
     * @return an array of MenuElement objects representing the selected path
     */
    public MenuElement[] getSelectedPath() {
        MenuElement res[] = new MenuElement[selection.size()];
        int i,c;
        for(i=0,c=selection.size();i<c;i++) 
            res[i] = (MenuElement) selection.elementAt(i);
        return res;
    }

    /**
     * Tell the menu selection to close and unselect all the menu components. Call this method
     * when a choice has been made
     */
    public void clearSelectedPath() {
        setSelectedPath(null);
    }

    /**
     * Adds a ChangeListener to the button.
     *
     * @param l the listener to add
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    
    /**
     * Removes a ChangeListener from the button.
     *
     * @param l the listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @see EventListenerList
     */
    protected void fireStateChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }          
        }
    }   

    /**
     * When a MenuElement receives an event from a MouseListener, it should never process the event
     * directly. Instead all MenuElements should call this method with the event.
     *
     * @param event  a MouseEvent object
     */
    public void processMouseEvent(MouseEvent event) {
        int screenX,screenY;
        Point p;
        int i,c,j,d;
        Component mc;
        Rectangle r2;
        int cWidth,cHeight;
        MenuElement menuElement;
        MenuElement subElements[];
        MenuElement path[];
        Vector tmp;
        int selectionSize;
        p = event.getPoint();
	
	Component source = (Component)event.getSource();

	if (!source.isShowing()) {
	    // This can happen if a mouseReleased removes the
	    // containing component -- bug 4146684
	    return;
	}

	int type = event.getID();
	int modifiers = event.getModifiers();
	// 4188027: drag enter/exit added in JDK 1.1.7A, JDK1.2
	if ((type==MouseEvent.MOUSE_ENTERED||
	     type==MouseEvent.MOUSE_EXITED)
	    && ((modifiers & (InputEvent.BUTTON1_MASK |
			      InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) !=0 )) {
	    return;
	}

        SwingUtilities.convertPointToScreen(p,source);

        screenX = p.x;
        screenY = p.y;

        tmp = (Vector)selection.clone();
        selectionSize = tmp.size();
	boolean success = false;
	for (i=selectionSize - 1;i >= 0 && success == false; i--) {
            menuElement = (MenuElement) tmp.elementAt(i);
            subElements = menuElement.getSubElements();
            
            path = null;
	    for (j = 0, d = subElements.length;j < d && success == false; j++) {
		if (subElements[j] == null)
		    continue;
                mc = subElements[j].getComponent();
                if(!mc.isShowing())
                    continue;
                if(mc instanceof JComponent) {
                    cWidth  = ((JComponent)mc).getWidth();
                    cHeight = ((JComponent)mc).getHeight();
                } else {
                    r2 = mc.getBounds();
                    cWidth  = r2.width;
                    cHeight = r2.height;
                }
                p.x = screenX;
                p.y = screenY;
                SwingUtilities.convertPointFromScreen(p,mc);

                /** Send the event to visible menu element if menu element currently in
                 *  the selected path or contains the event location
                 */
                if(
                   (p.x >= 0 && p.x < cWidth && p.y >= 0 && p.y < cHeight)) {
                    int k;
                    if(path == null) {
                        path = new MenuElement[i+2];
                        for(k=0;k<=i;k++)
                            path[k] = (MenuElement)tmp.elementAt(k);
                    }
                    path[i+1] = subElements[j];
		    MenuElement currentSelection[] = getSelectedPath();

		    // Enter/exit detection -- needs tuning...
		    if (currentSelection[currentSelection.length-1] !=
			path[i+1] &&
			currentSelection[currentSelection.length-2] !=
			path[i+1]) {
			Component oldMC = currentSelection[currentSelection.length-1].getComponent();

			MouseEvent exitEvent = new MouseEvent(oldMC, MouseEvent.MOUSE_EXITED,
							      event.getWhen(),
							      event.getModifiers(), p.x, p.y,
							      event.getClickCount(),
							      event.isPopupTrigger());
			currentSelection[currentSelection.length-1].
			    processMouseEvent(exitEvent, path, this);

			MouseEvent enterEvent = new MouseEvent(mc, 
							       MouseEvent.MOUSE_ENTERED,
							       event.getWhen(),
							       event.getModifiers(), p.x, p.y,
							       event.getClickCount(),
							       event.isPopupTrigger());
			subElements[j].processMouseEvent(enterEvent, path, this);
		    } 
		    MouseEvent mouseEvent = new MouseEvent(mc, event.getID(),event. getWhen(),
							   event.getModifiers(), p.x, p.y,
							   event.getClickCount(),
							   event.isPopupTrigger());
		    subElements[j].processMouseEvent(mouseEvent, path, this);
		    success = true;
		    event.consume();
		}
            }
        }
    }

    private void printMenuElementArray(MenuElement path[]) {
	printMenuElementArray(path, false);
    }

    private void printMenuElementArray(MenuElement path[], boolean dumpStack) {
	System.out.println("Path is(");
	int i, j;
	for(i=0,j=path.length; i<j ;i++){
	    for (int k=0; k<=i; k++)
		System.out.print("  ");
	    MenuElement me = (MenuElement) path[i];
	    if(me instanceof JMenuItem) {
		System.out.println(((JMenuItem)me).getText() + ", ");
	    } else if (me instanceof JMenuBar) {
		System.out.println("JMenuBar, ");
	    } else if(me instanceof JPopupMenu) {
		System.out.println("JPopupMenu, ");
	    } else if (me == null) {
		System.out.println("NULL , ");
	    } else {
		System.out.println("" + me + ", ");
	    }
	}
	System.out.println(")");

	if (dumpStack == true)
	    Thread.dumpStack();
    }

    /**
     * Returns the component in the currently selected path 
     * which contains sourcePoint.
     *
     * @param source The component in whose coordinate space sourcePoint
     *        is given
     * @param sourcePoint The point which is being tested
     * @return The component in the currently selected path which
     *         contains sourcePoint (relative to the source component's 
     *         coordinate space.  If sourcePoint is not inside a component
     *         on the currently selected path, null is returned.
     */
    public Component componentForPoint(Component source, Point sourcePoint) {
        int screenX,screenY;
        Point p = sourcePoint;
        int i,c,j,d;
        Component mc;
        Rectangle r2;
        int cWidth,cHeight;
        MenuElement menuElement;
        MenuElement subElements[];
        Vector tmp;
        int selectionSize;

        SwingUtilities.convertPointToScreen(p,source);

        screenX = p.x;
        screenY = p.y;

        tmp = (Vector)selection.clone();
        selectionSize = tmp.size();
        for(i=selectionSize - 1 ; i >= 0 ; i--) {
            menuElement = (MenuElement) tmp.elementAt(i);
            subElements = menuElement.getSubElements();
            
            for(j = 0, d = subElements.length ; j < d ; j++) {
		if (subElements[j] == null)
		    continue;
                mc = subElements[j].getComponent();
                if(!mc.isShowing())
                    continue;
                if(mc instanceof JComponent) {
                    cWidth  = ((JComponent)mc).getWidth();
                    cHeight = ((JComponent)mc).getHeight();
                } else {
                    r2 = mc.getBounds();
                    cWidth  = r2.width;
                    cHeight = r2.height;
                }
                p.x = screenX;
                p.y = screenY;
                SwingUtilities.convertPointFromScreen(p,mc);
		
                /** Return the deepest component on the selection
		 *  path in whose bounds the event's point occurs
                 */
                if (p.x >= 0 && p.x < cWidth && p.y >= 0 && p.y < cHeight) {
                    return mc;
                }
            }
        }
	return null;
    }

    /**
     * When a MenuElement receives an event from a KeyListener, it should never process the event
     * directly. Instead all MenuElements should call this method with the event.
     *
     * @param e  a KeyEvent object
     */
    public void processKeyEvent(KeyEvent e) {
        Vector tmp;
        int selectionSize;
        int i,j,d;
        MenuElement menuElement;
        MenuElement subElements[];
        MenuElement path[];
        Component mc;

	if (DEBUG) {
	    System.out.println("in MenuSelectionManager.processKeyEvent");
	}

        tmp = (Vector)selection.clone();
        selectionSize = tmp.size();
        for(i=selectionSize - 1 ; i >= 0 ; i--) {
            menuElement = (MenuElement) tmp.elementAt(i);
            subElements = menuElement.getSubElements();
            
            path = null;
            for(j = 0, d = subElements.length ; j < d ; j++) {
		if (subElements[j] == null)
		    continue;
                mc = subElements[j].getComponent();
                if(!mc.isShowing())
                    continue;
                if(path == null) {
                    int k;
                    path = new MenuElement[i+2];
                    for(k=0;k<=i;k++)
                        path[k] = (MenuElement)tmp.elementAt(k);
                    }
                path[i+1] = subElements[j];
                subElements[j].processKeyEvent(e,path,this);
                if(e.isConsumed())
                    return;
            }
        }
    }
    
    /** 
     * Return true if c is part of the currently used menu
     */
    public boolean isComponentPartOfCurrentMenu(Component c) {
        if(selection.size() > 0) {
            MenuElement me = (MenuElement)selection.elementAt(0);
            return isComponentPartOfCurrentMenu(me,c);
        } else
            return false;
    }

    private boolean isComponentPartOfCurrentMenu(MenuElement root,Component c) {
        MenuElement children[];
        int i,d;
	
	if (root == null)
	    return false;

        if(root.getComponent() == c)
            return true;
        else {
            children = root.getSubElements();
            for(i=0,d=children.length;i<d;i++) {
                if(isComponentPartOfCurrentMenu(children[i],c))
                    return true;
            }
        }
        return false;
    }

}


