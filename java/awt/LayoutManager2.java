/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

/** 
 * Defines an interface for classes that know how to layout Containers
 * based on a layout constraints object.
 *
 * This interface extends the LayoutManager interface to deal with layouts
 * explicitly in terms of constraint objects that specify how and where
 * components should be added to the layout.
 * <p>
 * This minimal extension to LayoutManager is intended for tool
 * providers who wish to the creation of constraint-based layouts.
 * It does not yet provide full, general support for custom
 * constraint-based layout managers.
 *
 * @see LayoutManager
 * @see Container
 *
 * @version	1.11, 02/06/02
 * @author 	Jonni Kanerva
 */
public interface LayoutManager2 extends LayoutManager {

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     * @param comp the component to be added
     * @param constraints  where/how the component is added to the layout.
     */
    void addLayoutComponent(Component comp, Object constraints);

    /** 
     * Returns the maximum size of this component.
     * @see java.awt.Component#getMinimumSize()
     * @see java.awt.Component#getPreferredSize()
     * @see LayoutManager
     */
    public Dimension maximumLayoutSize(Container target);

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other 
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getLayoutAlignmentX(Container target);

    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other 
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getLayoutAlignmentY(Container target);

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     */
    public void invalidateLayout(Container target);

}
