/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import java.awt.Component;
import javax.swing.JTree;

/**
 * Defines the requirements for an object that displays a tree node.
 * See <a
 href="http://java.sun.com/docs/books/tutorial/uiswing/components/tree.html">How to Use Trees</a>
 * in <em>The Java Tutorial</em>
 * for an example of implementing a tree cell renderer
 * that displays custom icons.
 *
 * @version 1.17 02/06/02
 * @author Rob Davis
 * @author Ray Ryan
 * @author Scott Violet
 */
public interface TreeCellRenderer {

    /**
     * Sets the value of the current tree cell to <code>value</code>.
     * If <code>selected</code> is true, the cell will be drawn as if
     * selected. If <code>expanded</code> is true the node is currently
     * expanded and if <code>leaf</code> is true the node represets a
     * leaf anf if <code>hasFocus</code> is true the node currently has
     * focus. <code>tree</code> is the JTree the receiver is being
     * configured for.
     * Returns the Component that the renderer uses to draw the value.
     *
     * @return	Component that the renderer uses to draw the value.
     */
    Component getTreeCellRendererComponent(JTree tree, Object value,
				   boolean selected, boolean expanded,
				   boolean leaf, int row, boolean hasFocus);

}
