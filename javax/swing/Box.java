/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package javax.swing;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.io.Serializable;
import javax.accessibility.*;

/**
 * A lightweight container 
 * that uses a BoxLayout object as its layout manager.
 * Box provides several class methods
 * that are useful for containers using BoxLayout --
 * even non-Box containers.
 *
 * <p>
 *
 * The Box class can create several kinds
 * of invisible components 
 * that affect layout:
 * glue, struts, and rigid areas.
 * If all the components your Box contains 
 * have a fixed size,
 * you might want to use a glue component
 * (returned by <code>createGlue</code>)
 * to control the components' positions.
 * If you need a fixed amount of space between two components,
 * try using a strut
 * (<code>createHorizontalStrut</code> or <code>createVerticalStrut</code>).
 * If you need an invisible component
 * that always takes up the same amount of space,
 * get it by invoking <code>createRigidArea</code>.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @see BoxLayout
 *
 * @author  Timothy Prinzing
 * @version 1.37 02/06/02
 */
public class Box extends Container implements Accessible {

    /**
     * Creates a <code>Box</code> that displays its components
     * along the the specified axis.
     *
     * @param axis  can be either <code>BoxLayout.X_AXIS</code>
     *              (to display components from left to right) or
     *              <code>BoxLayout.Y_AXIS</code>
     *              (to display them from top to bottom)
     * @see #createHorizontalBox
     * @see #createVerticalBox
     */
    public Box(int axis) {
	super();
	super.setLayout(new BoxLayout(this, axis));
    }

    /**
     * Creates a <code>Box</code> that displays its components
     * from left to right.
     *
     * @return the box
     */
    public static Box createHorizontalBox() {
	return new Box(BoxLayout.X_AXIS);
    }

    /**
     * Creates a <code>Box</code> that displays its components
     * from top to bottom.
     *
     * @return the box
     */
    public static Box createVerticalBox() {
	return new Box(BoxLayout.Y_AXIS);
    }

    /**
     * Creates an invisible component that's always the specified size.
     * <!-- WHEN WOULD YOU USE THIS AS OPPOSED TO A STRUT? -->
     *
     * @param d the dimensions of the invisible component
     * @return the component
     * @see #createGlue
     * @see #createHorizontalStrut
     * @see #createVerticalStrut
     */
    public static Component createRigidArea(Dimension d) {
	return new Filler(d, d, d);
    }

    /**
     * Creates an invisible, fixed-width component.
     * In a horizontal box, 
     * you typically use this method 
     * to force a certain amount of space between two components.
     * In a vertical box,
     * you might use this method 
     * to force the box to be at least the specified width.
     * The invisible component has no height
     * unless excess space is available,
     * in which case it takes its share of available space,
     * just like any other component that has no maximum height.
     *
     * @param width the width of the invisible component, in pixels >= 0
     * @return the component
     * @see #createVerticalStrut
     * @see #createGlue
     * @see #createRigidArea
     */
    public static Component createHorizontalStrut(int width) {
	// PENDING(jeff) change to Integer.MAX_VALUE. This hasn't been done
	// to date because BoxLayout alignment breaks.
	return new Filler(new Dimension(width,0), new Dimension(width,0), 
			  new Dimension(width, Short.MAX_VALUE));
    }

    /**
     * Creates an invisible, fixed-height component.
     * In a vertical box, 
     * you typically use this method
     * to force a certain amount of space between two components.
     * In a horizontal box,
     * you might use this method 
     * to force the box to be at least the specified height.
     * The invisible component has no width
     * unless excess space is available,
     * in which case it takes its share of available space,
     * just like any other component that has no maximum width.
     *
     * @param height the height of the invisible component, in pixels >= 0
     * @return the component
     * @see #createHorizontalStrut
     * @see #createGlue
     * @see #createRigidArea
     */
    public static Component createVerticalStrut(int height) {
	// PENDING(jeff) change to Integer.MAX_VALUE. This hasn't been done
	// to date because BoxLayout alignment breaks.
	return new Filler(new Dimension(0,height), new Dimension(0,height), 
			  new Dimension(Short.MAX_VALUE, height));
    }

    /**
     * Creates an invisible "glue" component 
     * that can be useful in a Box
     * whose visible components have a maximum width
     * (for a horizontal box)
     * or height (for a vertical box).
     * You can think of the glue component
     * as being a gooey substance
     * that expands as much as necessary
     * to fill the space between its neighboring components.
     *
     * <p>
     *
     * For example, suppose you have
     * a horizontal box that contains two fixed-size components.
     * If the box gets extra space,
     * the fixed-size components won't become larger,
     * so where does the extra space go?
     * Without glue,
     * the extra space goes to the right of the second component.
     * If you put glue between the fixed-size components,
     * then the extra space goes there.
     * If you put glue before the first fixed-size component,
     * the extra space goes there,
     * and the fixed-size components are shoved against the right
     * edge of the box.
     * If you put glue before the first fixed-size component
     * and after the second fixed-size component,
     * the fixed-size components are centered in the box.
     *
     * <p>
     *
     * To use glue,
     * call <code>Box.createGlue</code>
     * and add the returned component to a container.
     * The glue component has no minimum or preferred size,
     * so it takes no space unless excess space is available.
     * If excess space is available, 
     * then the glue component takes its share of available
     * horizontal or vertical space,
     * just like any other component that has no maximum width or height.
     *
     * @return the component
     */
    public static Component createGlue() {
	// PENDING(jeff) change to Integer.MAX_VALUE. This hasn't been done
	// to date because BoxLayout alignment breaks.
	return new Filler(new Dimension(0,0), new Dimension(0,0), 
			  new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
    }

    /**
     * Creates a horizontal glue component.
     *
     * @return the component
     */
    public static Component createHorizontalGlue() {
	// PENDING(jeff) change to Integer.MAX_VALUE. This hasn't been done
	// to date because BoxLayout alignment breaks.
	return new Filler(new Dimension(0,0), new Dimension(0,0), 
			  new Dimension(Short.MAX_VALUE, 0));
    }

    /**
     * Creates a vertical glue component.
     *
     * @return the component
     */
    public static Component createVerticalGlue() {
	// PENDING(jeff) change to Integer.MAX_VALUE. This hasn't been done
	// to date because BoxLayout alignment breaks.
	return new Filler(new Dimension(0,0), new Dimension(0,0), 
			  new Dimension(0, Short.MAX_VALUE));
    }

    /**
     * Throws an AWTError, since a Box can use only a BoxLayout.
     *
     * @param l the layout manager to use
     */
    public void setLayout(LayoutManager l) {
	throw new AWTError("Illegal request");
    }


    /**
     * An implementation of a lightweight component that participates in
     * layout but has no view.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class Filler extends Component implements Accessible {

	/**
	 * Constructor to create shape with the given size ranges.
	 *
	 * @param min   Minimum size
	 * @param pref  Preferred size
	 * @param max   Maximum size
	 */
        public Filler(Dimension min, Dimension pref, Dimension max) {
	    reqMin = min;
	    reqPref = pref;
	    reqMax = max;
	}

	/**
	 * Change the size requests for this shape.  An invalidate() is
	 * propagated upward as a result so that layout will eventually
	 * happen with using the new sizes.
	 *
	 * @param min   Value to return for getMinimumSize
	 * @param pref  Value to return for getPreferredSize
	 * @param max   Value to return for getMaximumSize
	 */
        public void changeShape(Dimension min, Dimension pref, Dimension max) {
	    reqMin = min;
	    reqPref = pref;
	    reqMax = max;
	    invalidate();
	}

	// ---- Component methods ------------------------------------------

        /**
         * Returns the minimum size of the component.
         *
         * @return the size
         */
        public Dimension getMinimumSize() {
	    return reqMin;
	}

        /**
         * Returns the preferred size of the component.
         *
         * @return the size
         */
        public Dimension getPreferredSize() {
	    return reqPref;
	}

        /**
         * Returns the maximum size of the component.
         *
         * @return the size
         */
        public Dimension getMaximumSize() {
	    return reqMax;
	}

	// ---- member variables ---------------------------------------

        private Dimension reqMin;
        private Dimension reqPref;
        private Dimension reqMax;

/////////////////
// Accessibility support for Box$Filler
////////////////

        /**
         * The currently set AccessibleContext object.
         */
        protected AccessibleContext accessibleContext = null;

        /**
         * Gets the AccessibleContext associated with this Box.Filler. 
         * For box fillers, the AccessibleContext takes the form of an 
         * AccessibleBoxFiller. 
         * A new AccessibleAWTBoxFiller instance is created if necessary.
         *
         * @return an AccessibleBoxFiller that serves as the 
         *         AccessibleContext of this Box.Filler.
         */
        public AccessibleContext getAccessibleContext() {
	    if (accessibleContext == null) {
		accessibleContext = new AccessibleBoxFiller();
	    }
	    return accessibleContext;
        }

        /**
         * This class implements accessibility support for the 
         * <code>Box.Filler</code> class.
         */
	protected class AccessibleBoxFiller extends AccessibleAWTComponent {

            // AccessibleContext methods
            //
            /**
             * Gets the role of this object.
             *
             * @return an instance of AccessibleRole describing the role of
             *   the object (AccessibleRole.FILLER)
             * @see AccessibleRole
             */
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.FILLER;
            }

        }
    }

/////////////////
// Accessibility support for Box
////////////////

    /**
     * The currently set AccessibleContext object.
     */
    protected AccessibleContext accessibleContext = null;

    /**
     * Gets the AccessibleContext associated with this Box. 
     * For boxes, the AccessibleContext takes the form of an 
     * AccessibleBox. 
     * A new AccessibleAWTBox instance is created if necessary.
     *
     * @return an AccessibleBox that serves as the 
     *         AccessibleContext of this Box
     */
    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleBox();
	}
	return accessibleContext;
    }

    /**
     * This class implements accessibility support for the 
     * <code>Box</code> class.
     */
    protected class AccessibleBox extends AccessibleAWTContainer {

        // AccessibleContext methods
        //
        /**
         * Gets the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the 
	 *   object (AccessibleRole.FILLER)
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.FILLER;
        }

    } // inner class AccessibleBox
}
