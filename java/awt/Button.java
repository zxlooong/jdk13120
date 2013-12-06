/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt;

import java.awt.peer.ButtonPeer;
import java.util.EventListener;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;

/**
 * This class creates a labeled button. The application can cause
 * some action to happen when the button is pushed. This image
 * depicts three views of a "<code>Quit</code>" button as it appears
 * under the Solaris operating system:
 * <p>
 * <img src="doc-files/Button-1.gif"
 * ALIGN=center HSPACE=10 VSPACE=7>
 * <p>
 * The first view shows the button as it appears normally.
 * The second view shows the button
 * when it has input focus. Its outline is darkened to let the
 * user know that it is an active object. The third view shows the
 * button when the user clicks the mouse over the button, and thus
 * requests that an action be performed.
 * <p>
 * The gesture of clicking on a button with the mouse
 * is associated with one instance of <code>ActionEvent</code>,
 * which is sent out when the mouse is both pressed and released
 * over the button. If an application is interested in knowing
 * when the button has been pressed but not released, as a separate
 * gesture, it can specialize <code>processMouseEvent</code>,
 * or it can register itself as a listener for mouse events by
 * calling <code>addMouseListener</code>. Both of these methods are
 * defined by <code>Component</code>, the abstract superclass of
 * all components.
 * <p>
 * When a button is pressed and released, AWT sends an instance
 * of <code>ActionEvent</code> to the button, by calling
 * <code>processEvent</code> on the button. The button's
 * <code>processEvent</code> method receives all events
 * for the button; it passes an action event along by
 * calling its own <code>processActionEvent</code> method.
 * The latter method passes the action event on to any action
 * listeners that have registered an interest in action
 * events generated by this button.
 * <p>
 * If an application wants to perform some action based on
 * a button being pressed and released, it should implement
 * <code>ActionListener</code> and register the new listener
 * to receive events from this button, by calling the button's
 * <code>addActionListener</code> method. The application can
 * make use of the button's action command as a messaging protocol.
 *
 * @version 	1.59 02/06/02
 * @author 	Sami Shaio
 * @see         java.awt.event.ActionEvent
 * @see         java.awt.event.ActionListener
 * @see         java.awt.Component#processMouseEvent
 * @see         java.awt.Component#addMouseListener
 * @since       JDK1.0
 */
public class Button extends Component implements Accessible {

    /*
    * The button's Label.
    * If the Label is not specified it will default to "".
	* @serial
    * @see getLabel()
    * @see setLabel()
    */
	String label;
    /*
    * The action to be performed once a button has been
    * pressed.
    * actionCommand can be null. 
	* @serial
    * @see getActionCommand()
    * @see setActionCommand()
    */
	String actionCommand;

    transient ActionListener actionListener;

    private static final String base = "button";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -8774683716313001058L;


    static {
        /* ensure that the necessary native libraries are loaded */
	Toolkit.loadLibraries();
	initIDs();
    }

    /**
     * Initialize JNI field and method IDs for fields that may be
       accessed from C.
     */
    private static native void initIDs();

    /**
     * Constructs a Button with no label.
     */
    public Button() {
	this("");
    }

    /**
     * Constructs a Button with the specified label.
     * @param label A string label for the button.
     */
    public Button(String label) {
	this.label = label;
    }

    /**
     * Construct a name for this component.  Called by getName() when the
     * name is null.
     */
    String constructComponentName() {
        synchronized (getClass()) {
	    return base + nameCounter++;
	}
    }

    /**
     * Creates the peer of the button.  The button's peer allows the
     * application to change the look of the button without changing
     * its functionality.
     * @see     java.awt.Toolkit#createButton(java.awt.Button)
     * @see     java.awt.Component#getToolkit()
     */
    public void addNotify() {
        synchronized(getTreeLock()) {
	    if (peer == null) 
	        peer = getToolkit().createButton(this);
	    super.addNotify();
	}
    }

    /**
     * Gets the label of this button.
     * @return    the button's label, or <code>null</code>
     *                if the button has no label.
     * @see       java.awt.Button#setLabel
     */
    public String getLabel() {
	return label;
    }

    /**
     * Sets the button's label to be the specified string.
     * @param     label   the new label, or <code>null</code>
     *                if the button has no label.
     * @see       java.awt.Button#getLabel
     */
    public void setLabel(String label) {
        boolean testvalid = false;

	synchronized (this) {
	    if (label != this.label && (this.label == null ||
					!this.label.equals(label))) {
	        this.label = label;
		ButtonPeer peer = (ButtonPeer)this.peer;
		if (peer != null) {
		    peer.setLabel(label);
		}
		testvalid = true;
	    }
	}

	// This could change the preferred size of the Component.
	if (testvalid && valid) {
	    invalidate();
	}
    }

    /**
     * Sets the command name for the action event fired
     * by this button. By default this action command is
     * set to match the label of the button.
     * @param     command  A string used to set the button's
     *                  action command.
     *            If the string is <code>null</code> then the action command
     *            is set to match the label of the button.
     * @see       java.awt.event.ActionEvent
     * @since     JDK1.1
     */
    public void setActionCommand(String command) {
        actionCommand = command;
    }

    /**
     * Returns the command name of the action event fired by this button.
     * If the command name is <code>null</code> (default) then this method
     * returns the label of the button.
     */
    public String getActionCommand() {
        return (actionCommand == null? label : actionCommand);
    }

    /**
     * Adds the specified action listener to receive action events from
     * this button. Action events occur when a user presses or releases
     * the mouse over this button.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param         l the action listener
     * @see           java.awt.event.ActionListener
     * @see           java.awt.Button#removeActionListener
     * @since         JDK1.1
     */
    public synchronized void addActionListener(ActionListener l) {
	if (l == null) {
	    return;
	}
	actionListener = AWTEventMulticaster.add(actionListener, l);
        newEventsOnly = true;
    }

    /**
     * Removes the specified action listener so that it no longer
     * receives action events from this button. Action events occur
     * when a user presses or releases the mouse over this button.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param         	l     the action listener
     * @see           	java.awt.event.ActionListener
     * @see           	java.awt.Button#addActionListener
     * @since         	JDK1.1
     */
    public synchronized void removeActionListener(ActionListener l) {
	if (l == null) {
	    return;
	}
	actionListener = AWTEventMulticaster.remove(actionListener, l);
    }

    /**
     * Return an array of all the listeners that were added to the Button
     * with addXXXListener(), where XXX is the name of the <code>listenerType</code>
     * argument.  For example, to get all of the ActionListener(s) for the
     * given Button <code>b</code>, one would write:
     * <pre>
     * ActionListener[] als = (ActionListener[])(b.getListeners(ActionListener.class))
     * </pre>
     * If no such listener list exists, then an empty array is returned.
     * 
     * @param    listenerType   Type of listeners requested
     * @return	 all of the listeners of the specified type supported by this button
     * @since 1.3
     */
    public EventListener[] getListeners(Class listenerType) { 
	EventListener l = null; 
	if  (listenerType == ActionListener.class) { 
	    l = actionListener;
	} else {
	    return super.getListeners(listenerType);
	}
	return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        if (e.id == ActionEvent.ACTION_PERFORMED) {
            if ((eventMask & AWTEvent.ACTION_EVENT_MASK) != 0 ||
                actionListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    /**
     * Processes events on this button. If an event is
     * an instance of <code>ActionEvent</code>, this method invokes
     * the <code>processActionEvent</code> method. Otherwise,
     * it invokes <code>processEvent</code> on the superclass.
     * @param        e the event.
     * @see          java.awt.event.ActionEvent
     * @see          java.awt.Button#processActionEvent
     * @since        JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
            return;
        }
	super.processEvent(e);
    }

    /**
     * Processes action events occurring on this button
     * by dispatching them to any registered
     * <code>ActionListener</code> objects.
     * <p>
     * This method is not called unless action events are
     * enabled for this button. Action events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>An <code>ActionListener</code> object is registered
     * via <code>addActionListener</code>.
     * <li>Action events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the action event.
     * @see         java.awt.event.ActionListener
     * @see         java.awt.Button#addActionListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processActionEvent(ActionEvent e) {
        if (actionListener != null) {
            actionListener.actionPerformed(e);
        }
    }

    /**
     * Returns the parameter string representing the state of this
     * button. This string is useful for debugging.
     * @return     the parameter string of this button.
     */
    protected String paramString() {
	return super.paramString() + ",label=" + label;
    }


    /* Serialization support.
     */
	/*
    * Button Serial Data Version.
    * @serial
    */
    private int buttonSerializedDataVersion = 1;

	/**
	* Writes default serializable fields to stream.  Writes
	* a list of serializable ItemListener(s) as optional data.
	* The non-serializable ItemListener(s) are detected and 
	* no attempt is made to serialize them.
	*
	* @serialData Null terminated sequence of 0 or more pairs.
	*			  The pair consists of a String and Object.
	*			  The String indicates the type of object and
	* 			  is one of the following :
	*			  itemListenerK indicating and ItemListener object.
	* 			
	* @see AWTEventMulticaster.save(ObjectOutputStream, String, EventListener)
	* @see java.awt.Component.itemListenerK
	*/
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
      s.defaultWriteObject();

      AWTEventMulticaster.save(s, actionListenerK, actionListener);
      s.writeObject(null);
    }

	/*
    * Read the ObjectInputStream and if it isnt null
    * add a listener to receive item events fired
    * by the button.
	* Unrecognised keys or values will be Ignored.
    * @serial
	* @see removeActionListener()
    * @see addActionListener()
    */

    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
      s.defaultReadObject();

      Object keyOrNull;
      while(null != (keyOrNull = s.readObject())) {
	String key = ((String)keyOrNull).intern();

	if (actionListenerK == key)
	  addActionListener((ActionListener)(s.readObject()));

	else // skip value for unrecognized key
	  s.readObject();
      }
    }


/////////////////
// Accessibility support
////////////////

    /**
     * Gets the AccessibleContext associated with this Button. 
     * For buttons, the AccessibleContext takes the form of an 
     * AccessibleAWTButton. 
     * A new AccessibleAWTButton instance is created if necessary.
     *
     * @return an AccessibleAWTButton that serves as the 
     *         AccessibleContext of this Button
     * @beaninfo
     *       expert: true
     *  description: The AccessibleContext associated with this Button.
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTButton();
        }
        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the 
     * <code>Button</code> class.  It provides an implementation of the 
     * Java Accessibility API appropriate to button user-interface elements.
     */
    protected class AccessibleAWTButton extends AccessibleAWTComponent
        implements AccessibleAction, AccessibleValue {

        /**
         * Get the accessible name of this object.  
         *
         * @return the localized name of the object -- can be null if this 
         * object does not have a name
         */
        public String getAccessibleName() {
            if (accessibleName != null) {
                return accessibleName;
            } else {
                if (getLabel() == null) {
                    return super.getAccessibleName();
                } else {
                    return getLabel();
                }
            }
        }

        /**
         * Get the AccessibleAction associated with this object.  In the
         * implementation of the Java Accessibility API for this class, 
	 * return this object, which is responsible for implementing the
         * AccessibleAction interface on behalf of itself.
	 * 
	 * @return this object
         */
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        /**
         * Get the AccessibleValue associated with this object.  In the
         * implementation of the Java Accessibility API for this class, 
	 * return this object, which is responsible for implementing the
         * AccessibleValue interface on behalf of itself.
	 * 
	 * @return this object
         */
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        /**
         * Returns the number of Actions available in this object.  The 
         * default behavior of a button is to have one action - toggle 
         * the button.
         *
         * @return 1, the number of Actions in this object
         */
        public int getAccessibleActionCount() {
            return 1;
        }
    
        /**
         * Return a description of the specified action of the object.
         *
         * @param i zero-based index of the actions
         */
        public String getAccessibleActionDescription(int i) {
            if (i == 0) {
                // [[[PENDING:  WDW -- need to provide a localized string]]]
                return new String("click");
            } else {
                return null;
            }
        }
    
        /**
         * Perform the specified Action on the object
         *
         * @param i zero-based index of actions
         * @return true if the the action was performed; else false.
         */
        public boolean doAccessibleAction(int i) {
            if (i == 0) {
                // Simulate a button click
                Toolkit.getEventQueue().postEvent(
                        new ActionEvent(Button.this,
                                        ActionEvent.ACTION_PERFORMED,
                                        Button.this.getActionCommand()));
                return true;
            } else {
                return false;
            }
        }

        /**
         * Get the value of this object as a Number.
         *
         * @return An Integer of 0 if this isn't selected or an Integer of 1 if
         * this is selected.
         * @see AbstractButton#isSelected
         */
        public Number getCurrentAccessibleValue() {
            return new Integer(0);
        }

        /**
         * Set the value of this object as a Number.
         *
         * @return True if the value was set.
         */
        public boolean setCurrentAccessibleValue(Number n) {
            return false;
        }

        /**
         * Get the minimum value of this object as a Number.
         *
         * @return An Integer of 0.
         */
        public Number getMinimumAccessibleValue() {
            return new Integer(0);
        }

        /**
         * Get the maximum value of this object as a Number.
         *
         * @return An Integer of 0.
         */
        public Number getMaximumAccessibleValue() {
            return new Integer(0);
        }

        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the 
         * object
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PUSH_BUTTON;
        }
    } // inner class AccessibleAWTButton

}