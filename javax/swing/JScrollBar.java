/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing;

import java.io.Serializable;
import java.awt.Component;
import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.Graphics;

import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.accessibility.*;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;



/**
 * An implementation of a scrollbar. The user positions the knob in the
 * scrollbar to determine the contents of the viewing area. The
 * program typically adjusts the display so that the end of the
 * scrollbar represents the end of the displayable contents, or 100%
 * of the contents. The start of the scrollbar is the beginning of the 
 * displayable contents, or 0%. The postion of the knob within
 * those bounds then translates to the corresponding percentage of
 * the displayable contents.
 * <p>
 * Typically, as the position of the knob in the scrollbar changes
 * a corresponding change is made to the position of the JViewport on
 * the underlying view, changing the contents of the JViewport.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @see JScrollPane
 * @beaninfo
 *      attribute: isContainer false
 *    description: A component that helps determine the visible content range of an area.
 *
 * @version 1.66 02/06/02
 * @author David Kloba
 */
public class JScrollBar extends JComponent implements Adjustable, Accessible
{
    /**
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "ScrollBarUI";

    /**
     * All changes from the model are treated as though the user moved
     * the scrollbar knob.  
     */
    private ChangeListener fwdAdjustmentEvents = new ModelListener();


    /**
     * The model that represents the scrollbar's minimum, maximum, extent
     * (aka "visibleAmount") and current value.
     * @see #setModel
     */
    protected BoundedRangeModel model;


    /**
     * @see #setOrientation
     */
    protected int orientation;


    /**
     * @see #setUnitIncrement
     */
    protected int unitIncrement;


    /**
     * @see #setBlockIncrement
     */
    protected int blockIncrement;

    
    private void checkOrientation(int orientation) {
        switch (orientation) {
        case VERTICAL:
        case HORIZONTAL:
            break;
        default:
            throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
        }
    }


    /**
     * Creates a scrollbar with the specified orientation,
     * value, extent, mimimum, and maximum.
     * The "extent" is the size of the viewable area. It is also known
     * as the "visible amount". 
     * <p>
     * Note: Use <code>setBlockIncrement</code> to set the block 
     * increment to a size slightly smaller than the view's extent.
     * That way, when the user jumps the knob to an adjacent position,
     * one or two lines of the original contents remain in view.
     * 
     * @exception IllegalArgumentException if orientation is not one of VERTICAL, HORIZONTAL
     * 
     * @see #setOrientation
     * @see #setValue
     * @see #setVisibleAmount
     * @see #setMinimum
     * @see #setMaximum
     */
    public JScrollBar(int orientation, int value, int extent, int min, int max)   
    {
        checkOrientation(orientation);
        this.unitIncrement = 1;
        this.blockIncrement = (extent == 0) ? 1 : extent;
        this.orientation = orientation;
        this.model = new DefaultBoundedRangeModel(value, extent, min, max);
        this.model.addChangeListener(fwdAdjustmentEvents);
	setRequestFocusEnabled(false);
        updateUI();
    }


    /**
     * Creates a scrollbar with the specified orientation
     * and the following initial values:
     * <pre>
     * minimum = 0 
     * maximum = 100 
     * value = 0
     * extent = 10
     * </pre>
     */
    public JScrollBar(int orientation) {
        this(orientation, 0, 10, 0, 100);
    }


    /**
     * Creates a vertical scrollbar with the following initial values:
     * <pre>
     * minimum = 0 
     * maximum = 100 
     * value = 0
     * extent = 10
     * </pre>
     */
    public JScrollBar() {
        this(VERTICAL);
    }


    /**
     * Returns the delegate that implements the look and feel for 
     * this component.
     *
     * @see JComponent#setUI
     */
    public ScrollBarUI getUI() { 
        return (ScrollBarUI)ui;
    }


    /**
     * Overrides <code>JComponent.updateUI</code>.
     * @see JComponent#updateUI
     */
    public void updateUI() {
        setUI((ScrollBarUI)UIManager.getUI(this));
    }


    /**
     * Returns the name of the LookAndFeel class for this component.
     *
     * @return "ScrollBarUI"
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     */
    public String getUIClassID() {
        return uiClassID;
    }



    /**
     * Returns the component's orientation (horizontal or vertical). 
     *                     
     * @return VERTICAL or HORIZONTAL
     * @see #setOrientation
     * @see java.awt.Adjustable#getOrientation
     */
    public int getOrientation() { 
        return orientation; 
    }


    /**
     * Set the scrollbar's orientation to either VERTICAL or
     * HORIZONTAL.
     * 
     * @exception IllegalArgumentException if orientation is not one of VERTICAL, HORIZONTAL
     * @see #getOrientation
     * @beaninfo
     *    preferred: true
     *        bound: true
     *    attribute: visualUpdate true
     *  description: The scrollbar's orientation.
     *         enum: VERTICAL JScrollBar.VERTICAL 
     *               HORIZONTAL JScrollBar.HORIZONTAL
     */
    public void setOrientation(int orientation) 
    { 
        checkOrientation(orientation);
        int oldValue = this.orientation;
        this.orientation = orientation;
        firePropertyChange("orientation", oldValue, orientation);

        if ((oldValue != orientation) && (accessibleContext != null)) {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    ((oldValue == VERTICAL) 
                     ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL),
                    ((orientation == VERTICAL) 
                     ? AccessibleState.VERTICAL : AccessibleState.HORIZONTAL));
        }
        if (orientation != oldValue) {
            revalidate();
        }
    }


    /**
     * Returns data model that handles the scrollbar's four
     * fundamental properties: minimum, maximum, value, extent.
     * 
     * @see #setModel
     */
    public BoundedRangeModel getModel() { 
        return model; 
    }


    /**
     * Sets the model that handles the scrollbar's four
     * fundamental properties: minimum, maximum, value, extent.
     * 
     * @see #getModel
     * @beaninfo
     *       bound: true
     *       expert: true
     * description: The scrollbar's BoundedRangeModel.
     */
    public void setModel(BoundedRangeModel newModel) {
        Integer oldValue = null;
        BoundedRangeModel oldModel = model;
        if (model != null) {
            model.removeChangeListener(fwdAdjustmentEvents);
            oldValue = new Integer(model.getValue());
        }
        model = newModel;
        if (model != null) {
            model.addChangeListener(fwdAdjustmentEvents);
        }

        firePropertyChange("model", oldModel, model);

        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    oldValue, new Integer(model.getValue()));        
        }
    }


    /**
     * Returns the amount to change the scrollbar's value by,
     * given a unit up/down request.  A ScrollBarUI implementation
     * typically calls this method when the user clicks on a scrollbar 
     * up/down arrow and uses the result to update the scrollbar's
     * value.   Subclasses my override this method to compute
     * a value, e.g. the change required to scroll up or down one
     * (variable height) line text or one row in a table.
     * <p>
     * The JScrollPane component creates scrollbars (by default) 
     * that override this method and delegate to the viewports
     * Scrollable view, if it has one.  The Scrollable interface
     * provides a more specialized version of this method.
     * 
     * @param direction is -1 or 1 for up/down respectively
     * @return the value of the unitIncrement property
     * @see #setUnitIncrement
     * @see #setValue
     * @see Scrollable#getScrollableUnitIncrement
     */
    public int getUnitIncrement(int direction) { 
        return unitIncrement; 
    }


    /**
     * Sets the unitIncrement property.
     * @see #getUnitIncrement
     * @beaninfo
     *   preferred: true
     *       bound: true
     * description: The scrollbar's unit increment.
     */
    public void setUnitIncrement(int unitIncrement) { 
        int oldValue = this.unitIncrement;
        this.unitIncrement = unitIncrement;
        firePropertyChange("unitIncrement", oldValue, unitIncrement);
    }


    /**
     * Returns the amount to change the scrollbar's value by,
     * given a block (usually "page") up/down request.  A ScrollBarUI 
     * implementation typically calls this method when the user clicks 
     * above or below the scrollbar "knob" to change the value
     * up or down by large amount.  Subclasses my override this 
     * method to compute a value, e.g. the change required to scroll 
     * up or down one paragraph in a text document.
     * <p>
     * The JScrollPane component creates scrollbars (by default) 
     * that override this method and delegate to the viewports
     * Scrollable view, if it has one.  The Scrollable interface
     * provides a more specialized version of this method.
     * 
     * @param direction is -1 or 1 for up/down respectively
     * @return the value of the blockIncrement property
     * @see #setBlockIncrement
     * @see #setValue
     * @see Scrollable#getScrollableBlockIncrement
     */
    public int getBlockIncrement(int direction) { 
        return blockIncrement; 
    }


    /**
     * Sets the blockIncrement property.
     * @see #getBlockIncrement()
     * @beaninfo
     *   preferred: true
     *       bound: true
     * description: The scrollbar's block increment.
     */
    public void setBlockIncrement(int blockIncrement) { 
        int oldValue = this.blockIncrement;
        this.blockIncrement = blockIncrement;
        firePropertyChange("blockIncrement", oldValue, blockIncrement);
    }


    /**
     * For backwards compatibility with java.awt.Scrollbar.
     * @see Adjustable#getUnitIncrement
     * @see #getUnitIncrement(int)
     */
    public int getUnitIncrement() {
        return unitIncrement;
    }


    /**
     * For backwards compatibility with java.awt.Scrollbar.
     * @see Adjustable#getBlockIncrement
     * @see #getBlockIncrement(int)
     */
    public int getBlockIncrement() {
        return blockIncrement;
    }


    /**
     * Returns the scrollbar's value.
     * @return the model's value property
     * @see #setValue
     */
    public int getValue() { 
        return getModel().getValue(); 
    }


    /**
     * Sets the scrollbar's value.  This method just forwards the value
     * to the model.
     *
     * @see #getValue
     * @see BoundedRangeModel#setValue
     * @beaninfo
     *   preferred: true
     * description: The scrollbar's current value.
     */
    public void setValue(int value) {
        BoundedRangeModel m = getModel();
        int oldValue = m.getValue();
        m.setValue(value);

        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    new Integer(oldValue),
                    new Integer(m.getValue()));
        }
    }


    /**
     * Returns the scrollbar's extent, aka its "visibleAmount".  In many 
     * scrollbar look and feel implementations the size of the 
     * scrollbar "knob" or "thumb" is proportional to the extent.
     * 
     * @return the value of the model's extent property
     * @see #setVisibleAmount
     */
    public int getVisibleAmount() { 
        return getModel().getExtent(); 
    }


    /**
     * Set the model's extent property.
     * 
     * @see #getVisibleAmount
     * @see BoundedRangeModel#setExtent
     * @beaninfo
     *   preferred: true
     * description: The amount of the view that is currently visible.
     */
    public void setVisibleAmount(int extent) { 
        getModel().setExtent(extent); 
    }


    /**
     * Returns the minimum value supported by the scrollbar 
     * (usually zero).
     *
     * @return the value of the model's minimum property
     * @see #setMinimum
     */
    public int getMinimum() { 
        return getModel().getMinimum(); 
    }


    /**
     * Sets the model's minimum property.
     *
     * @see #getMinimum
     * @see BoundedRangeModel#setMinimum
     * @beaninfo
     *   preferred: true
     * description: The scrollbar's minimum value.
     */
    public void setMinimum(int minimum) { 
        getModel().setMinimum(minimum); 
    }


    /**
     * The maximum value of the scrollbar is maximum - extent.
     *
     * @return the value of the model's maximum property
     * @see #setMaximum
     */
    public int getMaximum() { 
        return getModel().getMaximum(); 
    }


    /**
     * Sets the model's maximum property.  Note that the scrollbar's value
     * can only be set to maximum - extent.
     * 
     * @see #getMaximum
     * @see BoundedRangeModel#setMaximum
     * @beaninfo
     *   preferred: true
     * description: The scrollbar's maximum value. 
     */
    public void setMaximum(int maximum) { 
        getModel().setMaximum(maximum); 
    }


    /**
     * True if the scrollbar knob is being dragged.
     * 
     * @return the value of the model's valueIsAdjusting property
     * @see #setValueIsAdjusting
     */
    public boolean getValueIsAdjusting() { 
        return getModel().getValueIsAdjusting(); 
    }


    /**
     * Sets the model's valueIsAdjusting property.  Scrollbar look and
     * feel implementations should set this property to true when 
     * a knob drag begins, and to false when the drag ends.  The
     * scrollbar model will not generate ChangeEvents while
     * valueIsAdjusting is true.
     * 
     * @see #getValueIsAdjusting
     * @see BoundedRangeModel#setValueIsAdjusting
     * @beaninfo
     *      expert: true
     * description: True if the scrollbar thumb is being dragged.
     */
    public void setValueIsAdjusting(boolean b) { 
        BoundedRangeModel m = getModel();   
        boolean oldValue = m.getValueIsAdjusting();
        m.setValueIsAdjusting(b);
   
        if ((oldValue != b) && (accessibleContext != null)) {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    ((oldValue) ? AccessibleState.BUSY : null),
                    ((b) ? AccessibleState.BUSY : null));
        }
    }


    /**
     * Sets the four BoundedRangeModel properties after forcing
     * the arguments to obey the usual constraints:
     * <pre>
     * minimum <= value <= value+extent <= maximum
     * </pre>
     * <p>
     * 
     * @see BoundedRangeModel#setRangeProperties
     * @see #setValue
     * @see #setVisibleAmount
     * @see #setMinimum
     * @see #setMaximum
     */
    public void setValues(int newValue, int newExtent, int newMin, int newMax)    
    {
        BoundedRangeModel m = getModel();
        int oldValue = m.getValue();
        m.setRangeProperties(newValue, newExtent, newMin, newMax, m.getValueIsAdjusting());

        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    new Integer(oldValue),
                    new Integer(m.getValue()));
        }
    }


    /**
     * Adds an AdjustmentListener.  Adjustment listeners are notified
     * each time the scrollbar's model changes.  Adjustment events are 
     * provided for backwards compatability with java.awt.Scrollbar.
     * <p>
     * Note that the AdjustmentEvents type property will always have a
     * placeholder value of AdjustmentEvent.TRACK because all changes
     * to a BoundedRangeModels value are considered equivalent.  To change
     * the value of a BoundedRangeModel one just sets its value property,
     * i.e. model.setValue(123).  No information about the origin of the
     * change, e.g. it's a block decrement, is provided.  We don't try
     * fabricate the origin of the change here.
     *
     * @param l the AdjustmentLister to add
     * @see #removeAdjustmentListener
     * @see BoundedRangeModel#addChangeListener
     */
    public void addAdjustmentListener(AdjustmentListener l)   {
        listenerList.add(AdjustmentListener.class, l);
    }


    /**
     * Removes an AdjustmentEvent listener.
     *
     * @param l the AdjustmentLister to remove
     * @see #addAdjustmentListener
     */
    public void removeAdjustmentListener(AdjustmentListener l)  {
        listenerList.remove(AdjustmentListener.class, l);
    }


    /*
     * Notify listeners that the scrollbar's model has changed.
     * 
     * @see #addAdjustmentListener
     * @see EventListenerList
     */
    protected void fireAdjustmentValueChanged(int id, int type, int value) {
        Object[] listeners = listenerList.getListenerList();
        AdjustmentEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==AdjustmentListener.class) {
                if (e == null) {
                    e = new AdjustmentEvent(this, id, type, value);
                }
                ((AdjustmentListener)listeners[i+1]).adjustmentValueChanged(e);
            }          
        }
    }   


    /** 
     * This class listens to ChangeEvents on the model and forwards 
     * AdjustmentEvents for the sake of backwards compatibility. 
     * Unfortunately there's no way to determine the proper
     * type of the AdjustmentEvent as all updates to the model's
     * value are considered equivalent. 
     */
    private class ModelListener implements ChangeListener, Serializable {
        public void stateChanged(ChangeEvent e)   {
            int id = AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED;
            int type = AdjustmentEvent.TRACK;
            fireAdjustmentValueChanged(id, type, getValue());
        }
    }

    // PENDING(hmuller) - the next three methods should be removed

    /**
     * The scrollbar is flexible along it's scrolling axis and 
     * rigid along the other axis.
     */
    public Dimension getMinimumSize() {
        Dimension pref = getPreferredSize();
        if (orientation == VERTICAL) {
            return new Dimension(pref.width, 5);
        } else {
            return new Dimension(5, pref.height);
        }
    }

    /**
     * The scrollbar is flexible along it's scrolling axis and
     * rigid along the other axis.
     */
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        if (getOrientation() == VERTICAL) {
            return new Dimension(pref.width, Short.MAX_VALUE);
        } else {
            return new Dimension(Short.MAX_VALUE, pref.height);
        }
    }

    /**
     * Enables the component so that the knob position can be changed.
     * When the disabled, the knob position cannot be changed.
     *
     * @param b a boolean value, where true enables the component and
     *          false disables it
     */
    public void setEnabled(boolean x)  {
        super.setEnabled(x);
        Component[] children = getComponents();
        for(int i = 0; i < children.length; i++) {
            children[i].setEnabled(x);
        }
    }

    /**
     * Identifies whether or not this component can receive the focus. This
     * returns false as JScrollBar's do not want to participate in focus
     * traversal.
     *
     * @return true if this component can receive the focus
     */
    public boolean isFocusTraversable() {
	return false;
    }

    /** 
     * See readObject() and writeObject() in JComponent for more 
     * information about serialization in Swing.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
	if ((ui != null) && (getUIClassID().equals(uiClassID))) {
	    ui.installUI(this);
	}
    }


    /**
     * Returns a string representation of this JScrollBar. This method 
     * is intended to be used only for debugging purposes, and the 
     * content and format of the returned string may vary between      
     * implementations. The returned string may be empty but may not 
     * be <code>null</code>.
     * 
     * @return  a string representation of this JScrollBar.
     */
    protected String paramString() {
	String orientationString = (orientation == HORIZONTAL ?
				    "HORIZONTAL" : "VERTICAL");

	return super.paramString() +
	",blockIncrement=" + blockIncrement +
	",orientation=" + orientationString +
	",unitIncrement=" + unitIncrement;
    }

/////////////////
// Accessibility support
////////////////

    /**
     * Gets the AccessibleContext associated with this JScrollBar. 
     * For JScrollBar, the AccessibleContext takes the form of an 
     * AccessibleJScrollBar. 
     * A new AccessibleJScrollBar instance is created if necessary.
     *
     * @return an AccessibleJScrollBar that serves as the 
     *         AccessibleContext of this JScrollBar
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJScrollBar();
        }
        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the 
     * <code>JScrollBar</code> class.  It provides an implementation of the 
     * Java Accessibility API appropriate to scroll bar user-interface 
     * elements.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    protected class AccessibleJScrollBar extends AccessibleJComponent
        implements AccessibleValue {

        /**
         * Get the state set of this object.
         *
         * @return an instance of AccessibleState containing the current state 
         * of the object
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (getValueIsAdjusting()) {
                states.add(AccessibleState.BUSY);
            }
            if (getOrientation() == VERTICAL) {
                states.add(AccessibleState.VERTICAL);
            } else {
                states.add(AccessibleState.HORIZONTAL);
            }
            return states;
        }

        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the 
         * object
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SCROLL_BAR;
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
         * Get the accessible value of this object.
         *
         * @return The current value of this object.
         */
        public Number getCurrentAccessibleValue() {
            return new Integer(getValue());
        }

        /**
         * Set the value of this object as a Number.
         *
         * @return True if the value was set.
         */
        public boolean setCurrentAccessibleValue(Number n) {
            if (n instanceof Integer) {
                setValue(n.intValue());
                return true;
            } else {
                return false;
            }
        }

        /**
         * Get the minimum accessible value of this object.
         *
         * @return The minimum value of this object.
         */
        public Number getMinimumAccessibleValue() {
            return new Integer(getMinimum());
        }

        /**
         * Get the maximum accessible value of this object.
         *
         * @return The maximum value of this object.
         */
        public Number getMaximumAccessibleValue() {
            return new Integer(getMaximum());
        }

    } // AccessibleJScrollBar
}
