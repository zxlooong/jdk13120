/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt;

import java.awt.peer.PopupMenuPeer;
import javax.accessibility.*;


/**
 * A class that implements a menu which can be dynamically popped up
 * at a specified position within a component.<p>
 * As the inheritance hierarchy implies, a PopupMenu can be used anywhere
 * a Menu can be used. However, if you use a PopupMenu like a Menu (e.g.,
 * you add it to a MenuBar), then you <b>cannot</b> call <code>show</code>
 * on that PopupMenu.
 *
 * @version	1.25 02/06/02
 * @author 	Amy Fowler
 */
public class PopupMenu extends Menu {

    private static final String base = "popup";
    static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -4620452533522760060L;

    /**
     * Creates a new popup menu.
     */
    public PopupMenu() {
	this("");
    }

    /**
     * Creates a new popup menu with the specified name.
     *
     * @param label a non-null string specifying the popup menu's label 
     */
    public PopupMenu(String label) {
	super(label);
    }

    /**
     * Construct a name for this MenuComponent.  Called by getName() when
     * the name is null.
     */
    String constructComponentName() {
        synchronized (getClass()) {
	    return base + nameCounter++;
	}
    }

    /**
     * Creates the popup menu's peer.
     * The peer allows us to change the appearance of the popup menu without
     * changing any of the popup menu's functionality.
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
	    // If our parent is not a Component, then this PopupMenu is
	    // really just a plain, old Menu.
	    if (parent != null && !(parent instanceof Component)) {
	        super.addNotify();
	    }
	    else {
	        if (peer == null)
		    peer = Toolkit.getDefaultToolkit().createPopupMenu(this);
		int nitems = getItemCount();
		for (int i = 0 ; i < nitems ; i++) {
		    MenuItem mi = getItem(i);
		    mi.parent = this;
		    mi.addNotify();
		}
	    }
	}
    }

   /**
     * Shows the popup menu at the x, y position relative to an origin component.
     * The origin component must be contained within the component
     * hierarchy of the popup menu's parent.  Both the origin and the parent 
     * must be showing on the screen for this method to be valid.<p>
     * If this PopupMenu is being used as a Menu (i.e., it has a non-Component
     * parent), then you cannot call this method on the PopupMenu.
     * 
     * @param origin the component which defines the coordinate space
     * @param x the x coordinate position to popup the menu
     * @param y the y coordinate position to popup the menu
     * @exception IllegalArgumentException  if this PopupMenu has a non-
     *            Component parent
     */
    public void show(Component origin, int x, int y) {
        // Use localParent for thread safety.
        MenuContainer localParent = parent;
	if (localParent == null) {
	    throw new NullPointerException("parent is null");
	}
        if (!(localParent instanceof Component)) {
	    throw new IllegalArgumentException(
	        "PopupMenus with non-Component parents cannot be shown");
	}
        Component compParent = (Component)localParent;
	if (compParent != origin &&
	    compParent instanceof Container &&
	    !((Container)compParent).isAncestorOf(origin)) {
	        throw new IllegalArgumentException(
		    "origin not in parent's hierarchy");
	}
	if (compParent.getPeer() == null || !compParent.isShowing()) {
	    throw new RuntimeException("parent not showing on screen");
	}
	if (peer == null) {
	    addNotify();
	}
	synchronized (getTreeLock()) {
	    if (peer != null) {
	        ((PopupMenuPeer)peer).show(
		    new Event(origin, 0, Event.MOUSE_DOWN, x, y, 0, 0));
	    }
	}
    }


/////////////////
// Accessibility support
////////////////

    /**
     * Gets the AccessibleContext associated with this PopupMenu. 
     * For popup menus, the AccessibleContext takes the form of an 
     * AccessibleAWTPopupMenu. 
     * A new AccessibleAWTPopupMenu instance is created if necessary.
     *
     * @return an AccessibleAWTPopupMenu that serves as the 
     *         AccessibleContext of this PopupMenu
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTPopupMenu();
        }
        return accessibleContext;
    }

    /**
     * Inner class of PopupMenu used to provide default support for
     * accessibility.  This class is not meant to be used directly by
     * application developers, but is instead meant only to be
     * subclassed by menu component developers.
     * <p>
     * This class implements accessibility support for the 
     * <code>PopupMenu</code> class.  It provides an implementation of the 
     * Java Accessibility API appropriate to popup menu user-interface elements.
     */
    protected class AccessibleAWTPopupMenu extends AccessibleAWTMenu {

        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the 
         * object
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.POPUP_MENU;
        }

    } // class AccessibleAWTPopupMenu

}
