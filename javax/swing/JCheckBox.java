/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.plaf.*;
import javax.accessibility.*;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


/**
 * An implementation of a check box -- an item that can be selected or
 * deselected, and which displays its state to the user. 
 * By convention, any number of check boxes in a group can be selected.
 * See <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/button.html">How to Use Buttonc, Check Boxes, and Radio Buttons</a>
 * in <em>The Java Tutorial</em>
 * for examples and information on using check boxes.
 * <p>
 * For the keyboard keys used by this component in the standard Look and
 * Feel (L&F) renditions, see the
 * <a href="doc-files/Key-Index.html#JCheckBox">JCheckBox</a> key assignments.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @see JRadioButton
 *
 * @beaninfo
 *   attribute: isContainer false
 * description: A component which can be selected or deselected.
 *
 * @version 1.60 02/06/02
 * @author Jeff Dinkins
 */
public class JCheckBox extends JToggleButton implements Accessible {

    /** Identifies a change to the flat property. */
    public static final String BORDER_PAINTED_FLAT_CHANGED_PROPERTY = "borderPaintedFlat";

    private boolean flat = false;

    /**
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "CheckBoxUI";


    /**
     * Creates an initially unselected check box button with no text, no icon.
     */
    public JCheckBox () {
        this(null, null, false);
    }

    /**
     * Creates an initially unselected check box with an icon.
     *
     * @param icon  the Icon image to display
     */
    public JCheckBox(Icon icon) {
        this(null, icon, false);
    }
    
    /**
     * Creates a check box with an icon and specifies whether
     * or not it is initially selected.
     *
     * @param icon  the Icon image to display
     * @param selected a boolean value indicating the initial selection
     *        state. If <code>true</code> the check box is selected
     */
    public JCheckBox(Icon icon, boolean selected) {
        this(null, icon, selected);
    }
    
    /**
     * Creates an initially unselected check box with text.
     *
     * @param text the text of the check box.
     */
    public JCheckBox (String text) {
        this(text, null, false);
    }

    /**
     * Creates a check box where properties are taken from the 
     * Action supplied.
     *
     * @since 1.3
     */
    public JCheckBox(Action a) {
        this();
	setAction(a);
    }


    /**
     * Creates a check box with text and specifies whether 
     * or not it is initially selected.
     *
     * @param text the text of the check box.
     * @param selected a boolean value indicating the initial selection
     *        state. If <code>true</code> the check box is selected
     */
    public JCheckBox (String text, boolean selected) {
        this(text, null, selected);
    }

    /**
     * Creates an initially unselected check box with 
     * the specified text and icon.
     *
     * @param text the text of the check box.
     * @param icon  the Icon image to display
     */
    public JCheckBox(String text, Icon icon) {
        this(text, icon, false);
    }

    /**
     * Creates a check box with text and icon,
     * and specifies whether or not it is initially selected.
     *
     * @param text the text of the check box.
     * @param icon  the Icon image to display
     * @param selected a boolean value indicating the initial selection
     *        state. If <code>true</code> the check box is selected
     */
    public JCheckBox (String text, Icon icon, boolean selected) {
        super(text, icon, selected);
        setBorderPainted(false);
        setHorizontalAlignment(LEFT);
    }

    /**
     * Sets whether the border should be painted flat. This is usually
     * set to true when a JCheckBox instance is used as a renderer in a
     * component such as a JTable or JTree.
     *
     * @param b if true, the border is painted flat.
     * @see #isBorderPaintedFlat
     * @beaninfo
     *        bound: true
     *    attribute: visualUpdate true
     *  description: Whether the border is painted flat.
     */
    public void setBorderPaintedFlat(boolean b) {
	flat = b;
        boolean oldValue = flat;
        flat = b;
        firePropertyChange(BORDER_PAINTED_FLAT_CHANGED_PROPERTY, oldValue, flat);
        if (b != oldValue) {
            revalidate();
            repaint();
	}
    }

    /**
     * Returns whether the border should be painted flat.
     * @see #setBorderPaintedFlat
     */
    public boolean isBorderPaintedFlat() {
	return flat;
    }

    /**
     * Notification from the UIFactory that the L&F
     * has changed. 
     *
     * @see JComponent#updateUI
     */
    public void updateUI() {
        setUI((ButtonUI)UIManager.getUI(this));
    }


    /**
     * Returns a string that specifies the name of the L&F class
     * that renders this component.
     *
     * @return "CheckBoxUI"
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     * @beaninfo
     *        expert: true
     *   description: A string that specifies the name of the L&F class
     */
    public String getUIClassID() {
        return uiClassID;
    }


    /**
     * Factory method which sets the ActionEvent source's properties
     * according to values from the Action instance.  The properties 
     * which are set may differ for subclasses.
     * By default, the properties which get set are Text, Icon
     * Enabled, and ToolTipText.
     *
     * @param a the Action from which to get the properties, or null
     * @since 1.3
     * @see Action
     * @see #setAction
     */
    protected void configurePropertiesFromAction(Action a) {
	setText((a!=null?(String)a.getValue(Action.NAME):null));
	setEnabled((a!=null?a.isEnabled():true));
 	setToolTipText((a!=null?(String)a.getValue(Action.SHORT_DESCRIPTION):null));	
    }

    /**
     * Factory method which creates the PropertyChangeListener
     * used to update the ActionEvent source as properties change on
     * its Action instance.  Subclasses may override this in order 
     * to provide their own PropertyChangeListener if the set of
     * properties which should be kept up to date differs from the
     * default properties (Text, Icon, Enabled, ToolTipText).
     *
     * Note that PropertyChangeListeners should avoid holding
     * strong references to the ActionEvent source, as this may hinder
     * garbage collection of the ActionEvent source and all components
     * in its containment hierarchy.  
     *
     * @since 1.3
     * @see Action
     * @see #setAction
     */
    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new AbstractActionPropertyChangeListener(this, a) {
	    public void propertyChange(PropertyChangeEvent e) {	    
		String propertyName = e.getPropertyName();
		AbstractButton button = (AbstractButton)getTarget();
		if (button == null) {   //WeakRef GC'ed in 1.2
		    Action action = (Action)e.getSource();
		    action.removePropertyChangeListener(this);
		} else {
		    if (e.getPropertyName().equals(Action.NAME)) {
			String text = (String) e.getNewValue();
			button.setText(text);
			button.repaint();
		    } else if (e.getPropertyName().equals(Action.SHORT_DESCRIPTION)) {
			String text = (String) e.getNewValue();
			button.setToolTipText(text);
		    } else if (propertyName.equals("enabled")) {
			Boolean enabledState = (Boolean) e.getNewValue();
			button.setEnabled(enabledState.booleanValue());
			button.repaint();
		    } 
		}
	    }
	};
    }

    /**
     * See JComponent.readObject() for information about serialization
     * in Swing.
     */
    private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException 
    {
        s.defaultReadObject();
	if (getUIClassID().equals(uiClassID)) {
	    updateUI();
	}
    }


    /**
     * Returns a string representation of this JCheckBox. This method 
     * is intended to be used only for debugging purposes, and the 
     * content and format of the returned string may vary between      
     * implementations. The returned string may be empty but may not 
     * be <code>null</code>.
     * specific new aspects of the JFC components.
     * 
     * @return  a string representation of this JCheckBox.
     */
    protected String paramString() {
	return super.paramString();
    }

/////////////////
// Accessibility support
////////////////

    /**
     * Gets the AccessibleContext associated with this JCheckBox. 
     * For JCheckBoxes, the AccessibleContext takes the form of an 
     * AccessibleJCheckBox. 
     * A new AccessibleJCheckBox instance is created if necessary.
     *
     * @return an AccessibleJCheckBox that serves as the 
     *         AccessibleContext of this JCheckBox
     * @beaninfo
     *       expert: true
     *  description: The AccessibleContext associated with this CheckBox.
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJCheckBox();
        }
        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the 
     * <code>JCheckBox</code> class.  It provides an implementation of the 
     * Java Accessibility API appropriate to check box user-interface 
     * elements.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    protected class AccessibleJCheckBox extends AccessibleJToggleButton {

        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the object
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CHECK_BOX;
        }

    } // inner class AccessibleJCheckBox
}
  
