/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.event.*;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.*;
import javax.accessibility.*;


/**
 * <a name="jtree_description">
 * A control that displays a set of hierarchical data as an outline.
 * You can find task-oriented documentation and examples of using trees in
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/tree.html">How to Use Trees</a>,
 * a section in <em>The Java Tutorial.</em>
 * <p>
 * A specific node in a tree can be identified either by a
 * <code>TreePath</code> (an object
 * that encapsulates a node and all of its ancestors), or by its
 * display row, where each row in the display area displays one node.
 * An <i>expanded</i> node is one displays its children. A <i>collapsed</i>
 * node is one which hides them. A <i>hidden</i> node is one which is
 * under a collapsed ancestor. All of a <i>viewable</i> nodes parents
 * are expanded, but may or may not be displayed. A <i>displayed</i> node
 * is both viewable and in the display area, where it can be seen.
 * <p>
 * The following <code>JTree</code> methods use "visible" to mean "displayed":
 * <ul>
 * <li><code>isRootVisible()</code>
 * <li><code>setRootVisible()</code>
 * <li><code>scrollPathToVisible()</code>
 * <li><code>scrollRowToVisible()</code>
 * <li><code>getVisibleRowCount()</code>
 * <li><code>setVisibleRowCount()</code>
 * </ul>
 * <p>
 * The next group of <code>JTree</code> methods use "visible" to mean
 * "viewable" (under an expanded parent):
 * <ul>
 * <li><code>isVisible()</code>
 * <li><code>makeVisible()</code>
 * </ul>
 * <p>
 * If you are interested in knowing when the selection changes implement
 * the <code>TreeSelectionListener</code> interface and add the instance
 * using the method <code>addTreeSelectionListener</code>.
 * <code>valueChanged</code> will be invoked when the
 * selection changes, that is if the user clicks twice on the same
 * node <code>valueChanged</code> will only be invoked once.
 * <p>
 * If you are interested in detecting either double-click events or when
 * a user clicks on a node, regardless of whether or not it was selected,
 * we recommend you do the following:
 * <pre>
 * final JTree tree = ...;
 *
 * MouseListener ml = new MouseAdapter() {
 *     public void <b>mousePressed</b>(MouseEvent e) {
 *         int selRow = tree.getRowForLocation(e.getX(), e.getY());
 *         TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
 *         if(selRow != -1) {
 *             if(e.getClickCount() == 1) {
 *                 mySingleClick(selRow, selPath);
 *             }
 *             else if(e.getClickCount() == 2) {
 *                 myDoubleClick(selRow, selPath);
 *             }
 *         }
 *     }
 * };
 * tree.addMouseListener(ml);
 * </pre>
 * NOTE: This example obtains both the path and row, but you only need to
 * get the one you're interested in.
 * <p>
 * To use <code>JTree</code> to display compound nodes
 * (for example, nodes containing both
 * a graphic icon and text), subclass {@link TreeCellRenderer} and use 
 * {@link #setCellRenderer} to tell the tree to use it. To edit such nodes,
 * subclass {@link TreeCellEditor} and use {@link #setCellEditor}.
 * <p>
 * Like all <code>JComponent</code> classes, you can use {@link InputMap} and
 * {@link ActionMap}
 * to associate an {@link Action} object with a {@link KeyStroke}
 * and execute the action under specified conditions.
 * <p>
 * For the keyboard keys used by this component in the standard Look and
 * Feel (L&F) renditions, see the
 * <a href="doc-files/Key-Index.html#JTree">JTree</a> key assignments.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @beaninfo
 *   attribute: isContainer false
 * description: A component that displays a set of hierarchical data as an outline.
 *
 * @version $I% 03/18/02
 * @author Rob Davis
 * @author Ray Ryan
 * @author Scott Violet
 */
public class JTree extends JComponent implements Scrollable, Accessible
{
    /**
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "TreeUI";

    /**
     * The model that defines the tree displayed by this object.
     */
    transient protected TreeModel        treeModel;

    /**
     * Models the set of selected nodes in this tree.
     */
    transient protected TreeSelectionModel selectionModel;

    /**
     * True if the root node is displayed, false if its children are
     * the highest visible nodes.
     */
    protected boolean                    rootVisible;

    /**
     * The cell used to draw nodes. If <code>null</code>, the UI uses a default
     * <code>cellRenderer</code>.
     */
    transient protected TreeCellRenderer  cellRenderer;

    /**
     * Height to use for each display row. If this is <= 0 the renderer 
     * determines the height for each row.
     */
    protected int                         rowHeight;

    /**
     * Maps from <code>TreePath</code> to <code>Boolean</code>
     * indicating whether or not the
     * particular path is expanded. This ONLY indicates whether a 
     * given path is expanded, and NOT if it is visible or not. That
     * information must be determined by visiting all the parent
     * paths and seeing if they are visible.
     */
    transient private Hashtable           expandedState;


    /**
     * True if handles are displayed at the topmost level of the tree.
     * <p>
     * A handle is a small icon that displays adjacent to the node which 
     * allows the user to click once to expand or collapse the node. A
     * common interface shows a plus sign (+) for a node which can be
     * expanded and a minus sign (-) for a node which can be collapsed.
     * Handles are always shown for nodes below the topmost level.
     * <p>
     * If the <code>rootVisible</code> setting specifies that the root 
     * node is to be displayed, then that is the only node at the topmost
     * level. If the root node is not displayed, then all of its 
     * children are at the topmost level of the tree. Handles are 
     * always displayed for nodes other than the topmost.
     * <p> 
     * If the root node isn't visible, it is generally a good to make 
     * this value true. Otherwise, the tree looks exactly like a list,
     * and users may not know that the "list entries" are actually
     * tree nodes.
     *
     * @see #rootVisible
     */
    protected boolean           showsRootHandles;

    /**
     * Creates a new event and passed it off the
     * <code>selectionListeners</code>.
     */
    protected transient TreeSelectionRedirector selectionRedirector;

    /**
     * Editor for the entries.  Default is <code>null</code>
     * (tree is not editable).
     */
    transient protected TreeCellEditor          cellEditor;

    /**
     * Is the tree editable? Default is false.
     */
    protected boolean                 editable;

    /**
     * Is this tree a large model? This is a code-optimization setting.
     * A large model can be used when the cell height is the same for all
     * nodes. The UI will then cache very little information and instead
     * continually message the model. Without a large model the UI caches 
     * most of the information, resulting in fewer method calls to the model.
     * <p>
     * This value is only a suggestion to the UI. Not all UIs will
     * take advantage of it. Default value is false.
     */
    protected boolean                 largeModel;

    /**
     * Number of rows to make visible at one time. This value is used for
     * the <code>Scrollable</code> interface. It determines the preferred
     * size of the display area.
     */
    protected int                     visibleRowCount;

    /**
     * If true, when editing is to be stopped by way of selection changing,
     * data in tree changing or other means <code>stopCellEditing</code>
     * is invoked, and changes are saved. If false,
     * <code>cancelCellEditing</code> is invoked, and changes
     * are discarded. Default is false.
     */
    protected boolean                 invokesStopCellEditing;

    /**
     * If true, when a node is expanded, as many of the descendants are 
     * scrolled to be visible.
     */
    protected boolean                 scrollsOnExpand;

    /**
     * Number of mouse clicks before a node is expanded.
     */
    protected int                     toggleClickCount;

    /**
     * Updates the <code>expandedState</code>.
     */
    transient protected TreeModelListener       treeModelListener;

    /**
     * Used when <code>setExpandedState</code> is invoked,
     * will be a <code>Stack</code> of <code>Stack</code>s.
     */
    transient private Stack           expandedStack;

    /**
     * Lead selection path, may not be <code>null</code>.
     */
    private TreePath                  leadPath;

    /**
     * Anchor path.
     */
    private TreePath                  anchorPath;

    /**
     * True if paths in the selection should be expanded.
     */
    private boolean                   expandsSelectedPaths;

    /**
     * This is set to true for the life of the setUI call.
     */
    private boolean                   settingUI;

    /**
     * When <code>addTreeExpansionListener</code> is invoked,
     * and settingUI is true, this ivar gets set to the passed in
     * <code>Listener</code>. This listener is then notified first in
     * <code>fireTreeCollapsed</code> and <code>fireTreeExpanded</code>.
     * <p>This is an ugly workaround for a way to have the UI listener
     * get notified before other listeners.
     */
    private transient TreeExpansionListener     uiTreeExpansionListener;

    /**
     * Max number of stacks to keep around.
     */
    private static int                TEMP_STACK_SIZE = 11;

    //
    // Bound propery names
    //
    /** Bound property name for <code>cellRenderer</code>. */
    public final static String        CELL_RENDERER_PROPERTY = "cellRenderer";
    /** Bound property name for <code>treeModel</code>. */
    public final static String        TREE_MODEL_PROPERTY = "model";
    /** Bound property name for <code>rootVisible</code>. */
    public final static String        ROOT_VISIBLE_PROPERTY = "rootVisible";
    /** Bound property name for <code>showsRootHandles</code>. */
    public final static String        SHOWS_ROOT_HANDLES_PROPERTY = "showsRootHandles";
    /** Bound property name for <code>rowHeight</code>. */
    public final static String        ROW_HEIGHT_PROPERTY = "rowHeight";
    /** Bound property name for <code>cellEditor</code>. */
    public final static String        CELL_EDITOR_PROPERTY = "cellEditor";
    /** Bound property name for <code>editable</code>. */
    public final static String        EDITABLE_PROPERTY = "editable";
    /** Bound property name for <code>largeModel</code>. */
    public final static String        LARGE_MODEL_PROPERTY = "largeModel";
    /** Bound property name for selectionModel. */
    public final static String        SELECTION_MODEL_PROPERTY = "selectionModel";
    /** Bound property name for <code>visibleRowCount</code>. */
    public final static String        VISIBLE_ROW_COUNT_PROPERTY = "visibleRowCount";
    /** Bound property name for <code>messagesStopCellEditing</code>. */
    public final static String        INVOKES_STOP_CELL_EDITING_PROPERTY = "invokesStopCellEditing";
    /** Bound property name for <code>scrollsOnExpand</code>. */
    public final static String        SCROLLS_ON_EXPAND_PROPERTY = "scrollsOnExpand";
    /** Bound property name for <code>toggleClickCount</code>. */
    public final static String        TOGGLE_CLICK_COUNT_PROPERTY = "toggleClickCount";
    /** Bound property name for <code>leadSelectionPath</code>.
     * @since 1.3 */
    public final static String        LEAD_SELECTION_PATH_PROPERTY = "leadSelectionPath";
    /** Bound property name for anchor selection path.
     * @since 1.3 */
    public final static String        ANCHOR_SELECTION_PATH_PROPERTY = "anchorSelectionPath";
    /** Bound property name for expands selected paths property
     * @since 1.3 */
    public final static String        EXPANDS_SELECTED_PATHS_PROPERTY = "expandsSelectedPaths";


    /**
     * Creates and returns a sample <code>TreeModel</code>.
     * Used primarily for beanbuilders to show something interesting.
     *
     * @return the default <code>TreeModel</code>
     */
    protected static TreeModel getDefaultTreeModel() {
        DefaultMutableTreeNode      root = new DefaultMutableTreeNode("JTree");
	DefaultMutableTreeNode      parent;

	parent = new DefaultMutableTreeNode("colors");
	root.add(parent);
	parent.add(new DefaultMutableTreeNode("blue"));
	parent.add(new DefaultMutableTreeNode("violet"));
	parent.add(new DefaultMutableTreeNode("red"));
	parent.add(new DefaultMutableTreeNode("yellow"));

	parent = new DefaultMutableTreeNode("sports");
	root.add(parent);
	parent.add(new DefaultMutableTreeNode("basketball"));
	parent.add(new DefaultMutableTreeNode("soccer"));
	parent.add(new DefaultMutableTreeNode("football"));
	parent.add(new DefaultMutableTreeNode("hockey"));

	parent = new DefaultMutableTreeNode("food");
	root.add(parent);
	parent.add(new DefaultMutableTreeNode("hot dogs"));
	parent.add(new DefaultMutableTreeNode("pizza"));
	parent.add(new DefaultMutableTreeNode("ravioli"));
	parent.add(new DefaultMutableTreeNode("bananas"));
        return new DefaultTreeModel(root);
    }

    /**
     * Returns a <code>TreeModel</code> wrapping the specified object.
     * If the object
     * is:<ul>
     * <li>an array of <code>Object</code>s,
     * <li>a <code>Hashtable</code>, or
     * <li>a <code>Vector</code>
     * </ul>then a new root node is created with each of the incoming 
     * objects as children. Otherwise, a new root is created with the 
     * specified object as its value.
     *
     * @param value  the <code>Object</code> used as the foundation for
     *		the <code>TreeModel</code>
     * @return a <code>TreeModel</code> wrapping the specified object
     */
    protected static TreeModel createTreeModel(Object value) {
        DefaultMutableTreeNode           root;

        if((value instanceof Object[]) || (value instanceof Hashtable) ||
           (value instanceof Vector)) {
            root = new DefaultMutableTreeNode("root");
            DynamicUtilTreeNode.createChildren(root, value);
        }
        else {
            root = new DynamicUtilTreeNode("root", value);
        }
        return new DefaultTreeModel(root, false);
    }

    /**
     * Returns a <code>JTree</code> with a sample model.
     * The default model used by the tree defines a leaf node as any node
     * without children.
     *
     * @return a <code>JTree</code> with the default model,
     *		which defines a leaf node as any node without children.
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public JTree() {
        this(getDefaultTreeModel());
    }

    /**
     * Returns a <code>JTree</code> with each element of the
     * specified array as the
     * child of a new root node which is not displayed.
     * By default, the tree defines a leaf node as any node without
     * children.
     *
     * @param value  an array of <code>Object</code>s
     * @return a <code>JTree</code> with the contents of the array as
     *		children of the root node
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public JTree(Object[] value) {
        this(createTreeModel(value));
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
    }

    /**
     * Returns a <code>JTree</code> with each element of the specified
     * <code>Vector</code> as the
     * child of a new root node which is not displayed. By default, the
     * tree defines a leaf node as any node without children.
     *
     * @param value  a <code>Vector</code>
     * @return a <code>JTree</code> with the contents of the
     *		<code>Vector</code> as children of the root node
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public JTree(Vector value) {
        this(createTreeModel(value));
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
    }

    /**
     * Returns a <code>JTree</code> created from a <code>Hashtable</code>
     * which does not display with root.
     * Each value-half of the key/value pairs in the <code>HashTable</code>
     * becomes a child of the new root node. By default, the tree defines
     * a leaf node as any node without children.
     *
     * @param value  a <code>Hashtable</code>
     * @return a <code>JTree</code> with the contents of the
     * 		<code>Hashtable</code> as children of the root node
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public JTree(Hashtable value) {
        this(createTreeModel(value));
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
    }

    /**
     * Returns a <code>JTree</code> with the specified TreeNode as its root,
     * which displays the root node.
     * By default, the tree defines a leaf node as any node without children.
     *
     * @param root  a <code>TreeNode</code> object
     * @return a <code>JTree</code> with the specified root node
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public JTree(TreeNode root) {
        this(root, false);
    }

    /**
     * Returns a <code>JTree</code> with the specified <code>TreeNode</code>
     * as its root, which 
     * displays the root node and which decides whether a node is a 
     * leaf node in the specified manner.
     *
     * @param root  a <code>TreeNode</code> object
     * @param asksAllowsChildren  if false, any node without children is a 
     *              leaf node; if true, only nodes that do not allow 
     *              children are leaf nodes
     * @return a <code>JTree</code> with the specified root node
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public JTree(TreeNode root, boolean asksAllowsChildren) {
        this(new DefaultTreeModel(root, asksAllowsChildren));
    }

    /**
     * Returns an instance of <code>JTree</code> which displays the root node 
     * -- the tree is created using the specified data model.
     *
     * @param newModel  the <code>TreeModel</code> to use as the data model
     * @return a <code>JTree</code> based on the <code>TreeModel</code>
     */
    public JTree(TreeModel newModel) {
        super();
	expandedStack = new Stack();
	toggleClickCount = 2;
	expandedState = new Hashtable();
        setLayout(null);
        rowHeight = 16;
        visibleRowCount = 20;
        rootVisible = true;
        selectionModel = new DefaultTreeSelectionModel();
        cellRenderer = null;
	scrollsOnExpand = true;
        setOpaque(true);
	expandsSelectedPaths = true;
        updateUI();
        setModel(newModel);
    }

    /**
     * Returns the L&F object that renders this component.
     *
     * @return the TreeUI object that renders this component
     */
    public TreeUI getUI() {
        return (TreeUI)ui;
    }

    /**
     * Sets the L&F object that renders this component.
     *
     * @param ui  the TreeUI L&F object
     * @see UIDefaults#getUI
     */
    public void setUI(TreeUI ui) {
        if ((TreeUI)this.ui != ui) {
	    settingUI = true;
	    uiTreeExpansionListener = null;
	    try {
		super.setUI(ui);
	    }
	    finally {
		settingUI = false;
	    }
        }
    }

    /**
     * Notification from the <code>UIManager</code> that the L&F has changed. 
     * Replaces the current UI object with the latest version from the 
     * <code>UIManager</code>.
     *
     * @see JComponent#updateUI
     */
    public void updateUI() {
        setUI((TreeUI)UIManager.getUI(this));
        invalidate();
    }


    /**
     * Returns the name of the L&F class that renders this component.
     *
     * @return the string "TreeUI"
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     */
    public String getUIClassID() {
        return uiClassID;
    }


    /**
     * Returns the current <code>TreeCellRenderer</code>
     *  that is rendering each cell.
     *
     * @return the <code>TreeCellRenderer</code> that is rendering each cell
     */
    public TreeCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    /**
     * Sets the <code>TreeCellRenderer</code> that will be used to
     * draw each cell.
     *
     * @param x  the <code>TreeCellRenderer</code> that is to render each cell
     * @beaninfo
     *        bound: true
     *  description: The TreeCellRenderer that will be used to draw
     *               each cell.
     */
    public void setCellRenderer(TreeCellRenderer x) {
        TreeCellRenderer oldValue = cellRenderer;

        cellRenderer = x;
        firePropertyChange(CELL_RENDERER_PROPERTY, oldValue, cellRenderer);
        invalidate();
    }

    /**
      * Determines whether the tree is editable. Fires a property
      * change event if the new setting is different from the existing
      * setting.
      *
      * @param flag  a boolean value, true if the tree is editable
      * @beaninfo
      *        bound: true
      *  description: Whether the tree is editable.
      */
    public void setEditable(boolean flag) {
        boolean                 oldValue = this.editable;

        this.editable = flag;
        firePropertyChange(EDITABLE_PROPERTY, oldValue, flag);
        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                AccessibleContext.ACCESSIBLE_STATE_PROPERTY, 
                (oldValue ? AccessibleState.EDITABLE : null),
                (flag ? AccessibleState.EDITABLE : null));
        }
    }

    /**
     * Returns true if the tree is editable.
     *
     * @return true if the tree is editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the cell editor.  A <code>null</code> value implies that the
     * tree cannot be edited.  If this represents a change in the
     * <code>cellEditor</code>, the <code>propertyChange</code>
     * method is invoked on all listeners.
     *
     * @param cellEditor the <code>TreeCellEditor</code> to use
     * @beaninfo
     *        bound: true
     *  description: The cell editor. A null value implies the tree
     *               cannot be edited.
     */
    public void setCellEditor(TreeCellEditor cellEditor) {
        TreeCellEditor        oldEditor = this.cellEditor;

        this.cellEditor = cellEditor;
        firePropertyChange(CELL_EDITOR_PROPERTY, oldEditor, cellEditor);
        invalidate();
    }

    /**
     * Returns the editor used to edit entries in the tree.
     *
     * @return the <code>TreeCellEditor</code> in use,
     *		or <code>null</code> if the tree cannot be edited
     */
    public TreeCellEditor getCellEditor() {
        return cellEditor;
    }

    /**
     * Returns the <code>TreeModel</code> that is providing the data.
     *
     * @return the <code>TreeModel</code> that is providing the data
     */
    public TreeModel getModel() {
        return treeModel;
    }

    /**
     * Sets the <code>TreeModel</code> that will provide the data.
     *
     * @param newModel the <code>TreeModel</code> that is to provide the data
     * @beaninfo
     *        bound: true
     *  description: The TreeModel that will provide the data.
     */
    public void setModel(TreeModel newModel) {
        TreeModel oldModel = treeModel;

	if(treeModel != null && treeModelListener != null)
	    treeModel.removeTreeModelListener(treeModelListener);

        if (accessibleContext != null) {
	    if (treeModel != null) {
                treeModel.removeTreeModelListener((TreeModelListener)accessibleContext);
	    }
            if (newModel != null) {
	        newModel.addTreeModelListener((TreeModelListener)accessibleContext);
	    }
        }

        treeModel = newModel;
	clearToggledPaths();
	if(treeModel != null) {
	    if(treeModelListener == null)
		treeModelListener = createTreeModelListener();
	    if(treeModelListener != null)
		treeModel.addTreeModelListener(treeModelListener);
	    // Mark the root as expanded, if it isn't a leaf.
	    if(!treeModel.isLeaf(treeModel.getRoot()))
		expandedState.put(new TreePath(treeModel.getRoot()),
				  Boolean.TRUE);
	}
        firePropertyChange(TREE_MODEL_PROPERTY, oldModel, treeModel);
        invalidate();
    }

    /**
     * Returns true if the root node of the tree is displayed.
     *
     * @return true if the root node of the tree is displayed
     * @see #rootVisible
     */
    public boolean isRootVisible() {
        return rootVisible;
    }

    /**
     * Determines whether or not the root node from
     * the <code>TreeModel</code> is visible.
     *
     * @param rootVisible true if the root node of the tree is to be displayed
     * @see #rootVisible
     * @beaninfo
     *        bound: true
     *  description: Whether or not the root node 
     *               from the TreeModel is visible.
     */
    public void setRootVisible(boolean rootVisible) {
        boolean                oldValue = this.rootVisible;

        this.rootVisible = rootVisible;
        firePropertyChange(ROOT_VISIBLE_PROPERTY, oldValue, this.rootVisible);
        if (accessibleContext != null) {
            ((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
        }
    }

    /**
     * Determines whether the node handles are to be displayed.
     * 
     * @param newValue true if root handles are to be displayed
     * @see #showsRootHandles
     * @beaninfo
     *        bound: true
     *  description: Whether the node handles are to be
     *               displayed.
     */
    public void setShowsRootHandles(boolean newValue) {
        boolean                oldValue = showsRootHandles;
	TreeModel              model = getModel();

        showsRootHandles = newValue;
        firePropertyChange(SHOWS_ROOT_HANDLES_PROPERTY, oldValue,
                           showsRootHandles);
        if (accessibleContext != null) {
            ((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
        }
	// Make SURE the root is expanded
	if(model != null) {
	    expandPath(new TreePath(model.getRoot()));
	}
        invalidate();
    }

    /**
     * Returns true if handles for the root nodes are displayed.
     * 
     * @return true if root handles are displayed
     * @see #showsRootHandles
     */
    public boolean getShowsRootHandles()
    {
        return showsRootHandles;
    }

    /**
     * Sets the height of each cell, in pixels.  If the specified value
     * is less than or equal to zero the current cell renderer is
     * queried for each row's height.
     *
     * @param rowHeight the height of each cell, in pixels
     * @beaninfo
     *        bound: true
     *  description: The height of each cell.
     */
    public void setRowHeight(int rowHeight)
    {
        int                oldValue = this.rowHeight;

        this.rowHeight = rowHeight;
        firePropertyChange(ROW_HEIGHT_PROPERTY, oldValue, this.rowHeight);
        invalidate();
    }

    /**
     * Returns the height of each row.  If the returned value is less than
     * or equal to 0 the height for each row is determined by the
     * renderer.
     *
     * @param the height of each cell, in pixels; zero or negative if the
     *        height of each row is determined by the tree cell renderer
     */
    public int getRowHeight()
    {
        return rowHeight;
    }

    /**
     * Returns true if the height of each display row is a fixed size.
     *
     * @return true if the height of each row is a fixed size
     */
    public boolean isFixedRowHeight()
    {
        return (rowHeight > 0);
    }

    /**
     * Specifies whether the UI should use a large model.
     * (Not all UIs will implement this.) Fires a property change
     * for the LARGE_MODEL_PROPERTY.
     * 
     * @param newValue true to suggest a large model to the UI
     * @see #largeModel
     * @beaninfo
     *        bound: true
     *  description: Whether the UI should use a 
     *               large model.
     */
    public void setLargeModel(boolean newValue) {
        boolean                oldValue = largeModel;

        largeModel = newValue;
        firePropertyChange(LARGE_MODEL_PROPERTY, oldValue, newValue);
    }

    /**
     * Returns true if the tree is configured for a large model.
     * 
     * @return true if a large model is suggested
     * @see #largeModel
     */
    public boolean isLargeModel() {
        return largeModel;
    }

    /**
     * Determines what happens when editing is interrupted by selecting
     * another node in the tree, a change in the tree's data, or by some
     * other means. Setting this property to <code>true</code> causes the
     * changes to be automatically saved when editing is interrupted.
     * <p>
     * Fires a property change for the INVOKES_STOP_CELL_EDITING_PROPERTY.
     *
     * @param newValue true means that <code>stopCellEditing</code> is invoked 
     *        when editing is interruped, and data is saved; false means that
     *        <code>cancelCellEditing</code> is invoked, and changes are lost
     * @beaninfo
     *        bound: true
     *  description: Determines what happens when editing is interrupted,
     *               selecting another node in the tree, a change in the
     *               tree's data, or some other means.
     */
    public void setInvokesStopCellEditing(boolean newValue) {
        boolean                  oldValue = invokesStopCellEditing;

        invokesStopCellEditing = newValue;
        firePropertyChange(INVOKES_STOP_CELL_EDITING_PROPERTY, oldValue,
                           newValue);
    }

    /**
     * Returns the indicator that tells what happens when editing is 
     * interrupted.
     *
     * @return the indicator that tells what happens when editing is 
     *         interrupted
     * @see #setInvokesStopCellEditing
     */
    public boolean getInvokesStopCellEditing() {
        return invokesStopCellEditing;
    }

    /**
     * Determines whether or not when a node is expanded, as many of
     * the descendants are scrolled to be inside the viewport as
     * possible. The default is true.
     * @beaninfo
     *        bound: true
     *  description: Indicates if a node descendent should be scrolled when expanded.
     */
    public void setScrollsOnExpand(boolean newValue) {
	boolean           oldValue = scrollsOnExpand;

	scrollsOnExpand = newValue;
        firePropertyChange(SCROLLS_ON_EXPAND_PROPERTY, oldValue,
                           newValue);
    }

    /**
     * Returns true if the tree scrolls to show previously hidden children.
     *
     * @return true if when a node is expanded as many of the descendants
     * as possible are scrolled to be visible
     */
    public boolean getScrollsOnExpand() {
	return scrollsOnExpand;
    }

    /**
     * Sets the number of mouse clicks before a node will expand or close.
     * The default is two. 
     *
     * @since 1.3
     * @beaninfo
     *        bound: true
     *  description: Number of clicks before a node will expand/collapse.
     */
    public void setToggleClickCount(int clickCount) {
	int         oldCount = toggleClickCount;

	toggleClickCount = clickCount;
	firePropertyChange(TOGGLE_CLICK_COUNT_PROPERTY, oldCount,
			   clickCount);
    }

    /**
     * Returns the number of mouse clicks needed to expand or close a node.
     *
     * @return number of mouse clicks before node is expanded
     * @since 1.3
     */
    public int getToggleClickCount() {
	return toggleClickCount;
    }

    /**
     * Configures the <code>expandsSelectedPaths</code> property. If
     * true, any time the selection is changed, either via the
     * <code>TreeSelectionModel</code>, or the cover methods provided by 
     * <code>JTree</code>, the <code>TreePath</code>s parents will be
     * expanded to make them visible (visible meaning the parent path is
     * expanded, not necessarily in the visible rectangle of the
     * <code>JTree</code>). If false, when the selection
     * changes the nodes parent is not made visible (all its parents expanded).
     * This is useful if you wish to have your selection model maintain paths
     * that are not always visible (all parents expanded).
     *
     * @param newValue the new value for <code>expandsSelectedPaths</code>
     *
     * @since 1.3
     * @beaninfo
     *        bound: true
     *  description: Indicates whether changes to the selection should make
     *               the parent of the path visible.
     */
    public void setExpandsSelectedPaths(boolean newValue) {
	boolean         oldValue = expandsSelectedPaths;

	expandsSelectedPaths = newValue;
	firePropertyChange(EXPANDS_SELECTED_PATHS_PROPERTY, oldValue,
			   newValue);
    }

    /**
     * Returns the <code>expandsSelectedPaths</code> property.
     * @return true if selection changes result in the parent path being
     *         expanded
     * @since 1.3
     * @see #setExpandsSelectedPaths
     */
    public boolean getExpandsSelectedPaths() {
	return expandsSelectedPaths;
    }

    /**
     * Returns <code>isEditable</code>. This is invoked from the UI before
     * editing begins to insure that the given path can be edited. This
     * is provided as an entry point for subclassers to add filtered
     * editing without having to resort to creating a new editor.
     *
     * @return true if every parent node and the node itself is editabled
     * @see #isEditable
     */
    public boolean isPathEditable(TreePath path) {
        return isEditable();
    }

    /**
     * Overrides <code>JComponent</code>'s <code>getToolTipText</code>
     * method in order to allow 
     * renderer's tips to be used if it has text set.
     * <p>
     * NOTE: For <code>JTree</code> to properly display tooltips of its
     * renderers, <code>JTree</code> must be a registered component with the
     * <code>ToolTipManager</code>.  This can be done by invoking
     * <code>ToolTipManager.sharedInstance().registerComponent(tree)</code>.
     * This is not done automatically!
     *
     * @param event the <code>MouseEvent</code> that initiated the 
     *		<code>ToolTip</code> display
     * @return a string containing the  tooltip or <code>null</code>
     *		if <code>event</code> is null
     */
    public String getToolTipText(MouseEvent event) {
        if(event != null) {
            Point p = event.getPoint();
            int selRow = getRowForLocation(p.x, p.y);
            TreeCellRenderer       r = getCellRenderer();

            if(selRow != -1 && r != null) {
                TreePath     path = getPathForRow(selRow);
                Object       lastPath = path.getLastPathComponent();
                Component    rComponent = r.getTreeCellRendererComponent
                    (this, lastPath, isRowSelected(selRow),
                     isExpanded(selRow), getModel().isLeaf(lastPath), selRow,
                     true);

                if(rComponent instanceof JComponent) {
                    MouseEvent      newEvent;
                    Rectangle       pathBounds = getPathBounds(path);

                    p.translate(-pathBounds.x, -pathBounds.y);
                    newEvent = new MouseEvent(rComponent, event.getID(),
                                          event.getWhen(),
                                              event.getModifiers(),
                                              p.x, p.y, event.getClickCount(),
                                              event.isPopupTrigger());
                    
                    return ((JComponent)rComponent).getToolTipText(newEvent);
                }
            }
        }
        return null;
    }
    
    /**
     * Called by the renderers to convert the specified value to
     * text. This implementation returns <code>value.toString</code>, ignoring
     * all other arguments. To control the conversion, subclass this 
     * method and use any of the arguments you need.
     * 
     * @param value the <code>Object</code> to convert to text
     * @param selected true if the node is selected
     * @param expanded true if the node is expanded
     * @param leaf  true if the node is a leaf node
     * @param row  an integer specifying the node's display row, where 0 is 
     *             the first row in the display
     * @param hasFocus true if the node has the focus
     * @return the <code>String</code> representation of the node's value
     */
    public String convertValueToText(Object value, boolean selected,
                                     boolean expanded, boolean leaf, int row,
                                     boolean hasFocus) {
        if(value != null)
            return value.toString();
        return "";
    }

    //
    // The following are convenience methods that get forwarded to the
    // current TreeUI.
    //

    /**
     * Returns the number of rows that are currently being displayed.
     *
     * @return the number of rows that are being displayed
     */
    public int getRowCount() {
        TreeUI            tree = getUI();

        if(tree != null)
            return tree.getRowCount(this);
        return 0;
    }

    /** 
     * Selects the node identified by the specified path. If any
     * component of the path is hidden (under a collapsed node), and
     * <code>getExpandsSelectedPaths</code> is true it is 
     * exposed (made viewable).
     *
     * @param path the <code>TreePath</code> specifying the node to select
     */
    public void setSelectionPath(TreePath path) {
        getSelectionModel().setSelectionPath(path);
    }

    /** 
     * Selects the nodes identified by the specified array of paths.
     * If any component in any of the paths is hidden (under a collapsed
     * node), and <code>getExpandsSelectedPaths</code> is true
     * it is exposed (made viewable).
     *
     * @param paths an array of <code>TreePath</code> objects that specifies
     *		the nodes to select
     */
    public void setSelectionPaths(TreePath[] paths) {
        getSelectionModel().setSelectionPaths(paths);
    }

    /**
     * Sets the path identifies as the lead. The lead may not be selected.
     * The lead is not maintained by <code>JTree</code>,
     * rather the UI will update it.
     *
     * @param newPath  the new lead path
     * @since 1.3
     * @beaninfo
     *        bound: true
     *  description: Lead selection path
     */
    public void setLeadSelectionPath(TreePath newPath) {
	TreePath          oldValue = leadPath;

	leadPath = newPath;
	firePropertyChange(LEAD_SELECTION_PATH_PROPERTY, oldValue, newPath);
    }

    /**
     * Sets the path identified as the anchor.
     * The anchor is not maintained by <code>JTree</code>, rather the UI will 
     * update it.
     *
     * @param newPath  the new anchor path
     * @since 1.3
     * @beaninfo
     *        bound: true
     *  description: Anchor selection path
     */
    public void setAnchorSelectionPath(TreePath newPath) {
	TreePath          oldValue = anchorPath;

	anchorPath = newPath;
	firePropertyChange(ANCHOR_SELECTION_PATH_PROPERTY, oldValue, newPath);
    }

    /**
     * Selects the node at the specified row in the display.
     *
     * @param row  the row to select, where 0 is the first row in
     *             the display
     */
    public void setSelectionRow(int row) {
        int[]             rows = { row };

        setSelectionRows(rows);
    }

    /**
     * Selects the nodes corresponding to each of the specified rows
     * in the display. If a particular element of <code>rows</code> is
     * < 0 or >= <code>getRowCount</code>, it will be ignored.
     * If none of the elements
     * in <code>rows</code> are valid rows, the selection will
     * be cleared. That is it will be as if <code>clearSelection</code>
     * was invoked.
     * 
     * @param rows  an array of ints specifying the rows to select,
     *              where 0 indicates the first row in the display
     */
    public void setSelectionRows(int[] rows) {
        TreeUI               ui = getUI();

        if(ui != null && rows != null) {
            int                  numRows = rows.length;
            TreePath[]           paths = new TreePath[numRows];

            for(int counter = 0; counter < numRows; counter++) {
                paths[counter] = ui.getPathForRow(this, rows[counter]);
	    }
            setSelectionPaths(paths);
        }
    }

    /**
     * Adds the node identified by the specified <code>TreePath</code>
     * to the current
     * selection. If any component of the path isn't viewable, and
     * <code>getExpandsSelectedPaths</code>is true it is 
     * made viewable.
     *
     * @param path the <code>TreePath</code> to add
     */
    public void addSelectionPath(TreePath path) {
        getSelectionModel().addSelectionPath(path);
    }

    /**
     * Adds each path in the array of paths to the current selection. If
     * any component of any of the paths isn't viewable and
     * <code>getExpandsSelectedPaths</code> is true, it is
     * made viewable.
     *
     * @param paths an array of <code>TreePath</code> objects that specifies
     *		the nodes to add
     */
    public void addSelectionPaths(TreePath[] paths) {
	getSelectionModel().addSelectionPaths(paths);
    }

    /**
     * Adds the path at the specified row to the current selection.
     *
     * @param row  an integer specifying the row of the node to add,
     *             where 0 is the first row in the display
     */
    public void addSelectionRow(int row) {
        int[]      rows = { row };

        addSelectionRows(rows);
    }

    /**
     * Adds the paths at each of the specified rows to the current selection.
     * 
     * @param rows  an array of ints specifying the rows to add,
     *              where 0 indicates the first row in the display
     */
    public void addSelectionRows(int[] rows) {
        TreeUI             ui = getUI();

        if(ui != null && rows != null) {
            int                  numRows = rows.length;
            TreePath[]           paths = new TreePath[numRows];

            for(int counter = 0; counter < numRows; counter++)
                paths[counter] = ui.getPathForRow(this, rows[counter]);
            addSelectionPaths(paths);
        }
    }

    /**
     * Returns the last path component in the first node of the current 
     * selection.
     *
     * @return the last <code>Object</code> in the first selected node's
     *		<code>TreePath</code>,
     *		or <code>null</code> if nothing is selected
     * @see TreePath#getLastPathComponent
     */
    public Object getLastSelectedPathComponent() {
        TreePath     selPath = getSelectionModel().getSelectionPath();

        if(selPath != null)
            return selPath.getLastPathComponent();
        return null;
    }

    /**
     * Returns the path identified as the lead.
     * @return path identified as the lead
     */
    public TreePath getLeadSelectionPath() {
	return leadPath;
    }

    /**
     * Returns the path identified as the anchor.
     * @return path identified as the anchor
     * @since 1.3
     */
    public TreePath getAnchorSelectionPath() {
	return anchorPath;
    }

    /**
     * Returns the path to the first selected node.
     *
     * @return the <code>TreePath</code> for the first selected node,
     *		or <code>null</code> if nothing is currently selected
     */
    public TreePath getSelectionPath() {
        return getSelectionModel().getSelectionPath();
    }

    /**
     * Returns the paths of all selected values.
     *
     * @return an array of <code>TreePath</code> objects indicating the selected
     *         nodes, or <code>null</code> if nothing is currently selected
     */
    public TreePath[] getSelectionPaths() {
        return getSelectionModel().getSelectionPaths();
    }

    /**
     * Returns all of the currently selected rows. This method is simply
     * forwarded to the <code>TreeSelectionModel</code>.
     * If nothing is selected <code>null</code> or an empty array will
     * be returned, based on the <code>TreeSelectionModel</code>
     * implementation.
     *
     * @return an array of integers that identifies all currently selected rows
     *         where 0 is the first row in the display
     */
    public int[] getSelectionRows() {
        return getSelectionModel().getSelectionRows();
    }

    /**
     * Returns the number of nodes selected.
     *
     * @return the number of nodes selected
     */
    public int getSelectionCount() {
        return selectionModel.getSelectionCount();
    }

    /**
     * Gets the first selected row.
     *
     * @return an integer designating the first selected row, where 0 is the 
     *         first row in the display
     */
    public int getMinSelectionRow() {
        return getSelectionModel().getMinSelectionRow();
    }

    /**
     * Returns the last selected row.
     *
     * @return an integer designating the last selected row, where 0 is the 
     *         first row in the display
     */
    public int getMaxSelectionRow() {
        return getSelectionModel().getMaxSelectionRow();
    }

    /**
     * Returns the row index corresponding to the lead path.
     *
     * @return an integer giving the row index of the lead path,
     *		where 0 is the first row in the display; or -1
     *		if <code>leadPath</code> is <code>null</code>
     */
    public int getLeadSelectionRow() {
	TreePath leadPath = getLeadSelectionPath();

	if (leadPath != null) {
	    return getRowForPath(leadPath);
	}
        return -1;
    }

    /**
     * Returns true if the item identified by the path is currently selected.
     *
     * @param path a <code>TreePath</code> identifying a node
     * @return true if the node is selected
     */
    public boolean isPathSelected(TreePath path) {
        return getSelectionModel().isPathSelected(path);
    }

    /**
     * Returns true if the node identitifed by row is selected.
     *
     * @param row  an integer specifying a display row, where 0 is the first
     *             row in the display
     * @return true if the node is selected
     */
    public boolean isRowSelected(int row) {
        return getSelectionModel().isRowSelected(row);
    }

    /**
     * Returns an <code>Enumeration</code> of the descendants of the
     * path <code>parent</code> that
     * are currently expanded. If <code>parent</code> is not currently
     * expanded, this will return <code>null</code>.
     * If you expand/collapse nodes while
     * iterating over the returned <code>Enumeration</code>
     * this may not return all
     * the expanded paths, or may return paths that are no longer expanded.
     *
     * @param parent  the path which is to be examined
     * @return an <code>Enumeration</code> of the descendents of 
     *		<code>parent</code>, or <code>null</code> if
     *		<code>parent</code> is not currently expanded
     */
    public Enumeration getExpandedDescendants(TreePath parent) {
	if(!isExpanded(parent))
	    return null;

	Enumeration       toggledPaths = expandedState.keys();
	Vector            elements = null;
	TreePath          path;
	Object            value;

	if(toggledPaths != null) {
	    while(toggledPaths.hasMoreElements()) {
		path = (TreePath)toggledPaths.nextElement();
		value = expandedState.get(path);
		// Add the path if it is expanded, a descendant of parent,
		// and it is visible (all parents expanded). This is rather
		// expensive!
		if(path != parent && value != null &&
		   ((Boolean)value).booleanValue() &&
		   parent.isDescendant(path) && isVisible(path)) {
		    if (elements == null) {
			elements = new Vector();
		    }
		    elements.addElement(path);
		}
	    }
	}
	if (elements == null) {
	    return DefaultMutableTreeNode.EMPTY_ENUMERATION;
	}
	return elements.elements();
    }

    /**
     * Returns true if the node identified by the path has ever been
     * expanded.
     * @return true if the <code>path</code> has ever been expanded
     */
    public boolean hasBeenExpanded(TreePath path) {
	return (path != null && expandedState.get(path) != null);
    }

    /**
     * Returns true if the node identified by the path is currently expanded,
     * 
     * @param path  the <code>TreePath</code> specifying the node to check
     * @return false if any of the nodes in the node's path are collapsed, 
     *               true if all nodes in the path are expanded
     */
    public boolean isExpanded(TreePath path) {
	if(path == null)
	    return false;

	// Is this node expanded?
	Object          value = expandedState.get(path);

	if(value == null || !((Boolean)value).booleanValue())
	    return false;

	// It is, make sure its parent is also expanded.
	TreePath        parentPath = path.getParentPath();

	if(parentPath != null)
	    return isExpanded(parentPath);
        return true;
    }

    /**
     * Returns true if the node at the specified display row is currently
     * expanded.
     * 
     * @param row  the row to check, where 0 is the first row in the 
     *             display
     * @return true if the node is currently expanded, otherwise false
     */
    public boolean isExpanded(int row) {
        TreeUI                  tree = getUI();

        if(tree != null) {
	    TreePath         path = tree.getPathForRow(this, row);

	    if(path != null)
		return isExpanded(path);
	}
        return false;
    }

    /**
     * Returns true if the value identified by path is currently collapsed,
     * this will return false if any of the values in path are currently
     * not being displayed.
     * 
     * @param path  the <code>TreePath</code> to check
     * @return true if any of the nodes in the node's path are collapsed, 
     *               false if all nodes in the path are expanded
     */
    public boolean isCollapsed(TreePath path) {
	return !isExpanded(path);
    }

    /**
     * Returns true if the node at the specified display row is collapsed.
     * 
     * @param row  the row to check, where 0 is the first row in the 
     *             display
     * @return true if the node is currently collapsed, otherwise false
     */
    public boolean isCollapsed(int row) {
	return !isExpanded(row);
    }

    /**
     * Ensures that the node identified by path is currently viewable.
     *
     * @param path  the <code>TreePath</code> to make visible
     */
    public void makeVisible(TreePath path) {
        if(path != null) {
	    TreePath        parentPath = path.getParentPath();

	    if(parentPath != null) {
		expandPath(parentPath);
	    }
        }
    }

    /**
     * Returns true if the value identified by path is currently viewable,
     * which means it is either the root or all of its parents are expanded.
     * Otherwise, this method returns false. 
     *
     * @return true if the node is viewable, otherwise false
     */
    public boolean isVisible(TreePath path) {
        if(path != null) {
	    TreePath        parentPath = path.getParentPath();

	    if(parentPath != null)
		return isExpanded(parentPath);
	    // Root.
	    return true;
	}
        return false;
    }

    /**
     * Returns the <code>Rectangle</code> that the specified node will be drawn
     * into. Returns <code>null</code> if any component in the path is hidden
     * (under a collapsed parent).
     * <p>
     * Note:<br>
     * This method returns a valid rectangle, even if the specified
     * node is not currently displayed.
     *
     * @param path the <code>TreePath</code> identifying the node
     * @return the <code>Rectangle</code> the node is drawn in,
     *		or <code>null</code> 
     */
    public Rectangle getPathBounds(TreePath path) {
        TreeUI                   tree = getUI();

        if(tree != null)
            return tree.getPathBounds(this, path);
        return null;
    }

    /**
     * Returns the <code>Rectangle</code> that the node at the specified row is
     * drawn in.
     *
     * @param row  the row to be drawn, where 0 is the first row in the 
     *             display
     * @return the <code>Rectangle</code> the node is drawn in 
     */
    public Rectangle getRowBounds(int row) {
	TreePath          path = getPathForRow(row);

	return getPathBounds(getPathForRow(row));
    }

    /**
     * Makes sure all the path components in path are expanded (except
     * for the last path component) and scrolls so that the 
     * node identified by the path is displayed. Only works when this
     * <code>JTree</code> is contained in a <code>JScrollPane</code>.
     * 
     * @param path  the <code>TreePath</code> identifying the node to
     * 		bring into view
     */
    public void scrollPathToVisible(TreePath path) {
	if(path != null) {
	    makeVisible(path);

	    Rectangle          bounds = getPathBounds(path);

	    if(bounds != null) {
		scrollRectToVisible(bounds);
		if (accessibleContext != null) {
		    ((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
		}
	    }
	}
    }

    /**
     * Scrolls the item identified by row until it is displayed. The minimum
     * of amount of scrolling necessary to bring the row into view
     * is performed. Only works when this <code>JTree</code> is contained in a
     * <code>JScrollPane</code>.
     *
     * @param row  an integer specifying the row to scroll, where 0 is the
     *             first row in the display
     */
    public void scrollRowToVisible(int row) {
	scrollPathToVisible(getPathForRow(row));
    }

    /**
     * Returns the path for the specified row.  If <code>row</code> is
     * not visible, <code>null</code> is returned.
     *
     * @param row  an integer specifying a row
     * @return the <code>TreePath</code> to the specified node,
     *		<code>null</code> if <code>row < 0</code>
     *		or <code>row > getRowCount()</code>
     */
    public TreePath getPathForRow(int row) {
        TreeUI                  tree = getUI();

        if(tree != null)
            return tree.getPathForRow(this, row);
        return null;
    }

    /**
     * Returns the row that displays the node identified by the specified
     * path. 
     * 
     * @param path  the <code>TreePath</code> identifying a node
     * @return an integer specifying the display row, where 0 is the first
     *         row in the display, or -1 if any of the elements in path
     *         are hidden under a collapsed parent.
     */
    public int getRowForPath(TreePath path) {
        TreeUI                  tree = getUI();

        if(tree != null)
            return tree.getRowForPath(this, path);
        return -1;
    }

    /**
     * Ensures that the node identified by the specified path is 
     * expanded and viewable.
     * 
     * @param path  the <code>TreePath</code> identifying a node
     */
    public void expandPath(TreePath path) {
	// Only expand if not leaf!
	TreeModel          model = getModel();

	if(path != null && model != null && 
	   !model.isLeaf(path.getLastPathComponent())) {
	    setExpandedState(path, true);
	}
    }

    /**
     * Ensures that the node in the specified row is expanded and
     * viewable.
     * <p>
     * If <code>row</code> is < 0 or >= <code>getRowCount</code> this
     * will have no effect.
     *
     * @param row  an integer specifying a display row, where 0 is the
     *             first row in the display
     */
    public void expandRow(int row) {
	expandPath(getPathForRow(row));
    }

    /**
     * Ensures that the node identified by the specified path is 
     * collapsed and viewable.
     * 
     * @param path  the <code>TreePath</code> identifying a node
      */
    public void collapsePath(TreePath path) {
	setExpandedState(path, false);
    }

    /**
     * Ensures that the node in the specified row is collapsed.
     * <p>
     * If <code>row</code> is < 0 or >= <code>getRowCount</code> this
     * will have no effect.
     *
     * @param row  an integer specifying a display row, where 0 is the
     *             first row in the display
      */
    public void collapseRow(int row) {
	collapsePath(getPathForRow(row));
    }

    /**
     * Returns the path for the node at the specified location.
     *
     * @param x an integer giving the number of pixels horizontally from
     *          the left edge of the display area, minus any left margin
     * @param y an integer giving the number of pixels vertically from
     *          the top of the display area, minus any top margin
     * @return  the <code>TreePath</code> for the node at that location
     */
    public TreePath getPathForLocation(int x, int y) {
        TreePath          closestPath = getClosestPathForLocation(x, y);

        if(closestPath != null) {
            Rectangle       pathBounds = getPathBounds(closestPath);

            if(x >= pathBounds.x && x < (pathBounds.x + pathBounds.width) &&
               y >= pathBounds.y && y < (pathBounds.y + pathBounds.height))
                return closestPath;
        }
        return null;
    }

    /**
     * Returns the row for the specified location. 
     *
     * @param x an integer giving the number of pixels horizontally from
     *          the left edge of the display area, minus any left margin
     * @param y an integer giving the number of pixels vertically from
     *          the top of the display area, minus any top margin
     * @return the row corresponding to the location, or -1 if the
     *         location is not within the bounds of a displayed cell
     * @see #getClosestRowForLocation
     */
    public int getRowForLocation(int x, int y) {
	return getRowForPath(getPathForLocation(x, y));
    }

    /**
     * Returns the path to the node that is closest to x,y.  If
     * no nodes are currently viewable, or there is no model, returns
     * <code>null</code>, otherwise it always returns a valid path.  To test if
     * the node is exactly at x, y, get the node's bounds and
     * test x, y against that.
     *
     * @param x an integer giving the number of pixels horizontally from
     *          the left edge of the display area, minus any left margin
     * @param y an integer giving the number of pixels vertically from
     *          the top of the display area, minus any top margin
     * @return  the <code>TreePath</code> for the node closest to that location,
     *          <code>null</code> if nothing is viewable or there is no model
     *
     * @see #getPathForLocation
     * @see #getPathBounds
     */
    public TreePath getClosestPathForLocation(int x, int y) {
        TreeUI                  tree = getUI();

        if(tree != null)
            return tree.getClosestPathForLocation(this, x, y);
        return null;
    }

    /**
     * Returns the row to the node that is closest to x,y.  If no nodes
     * are viewable or there is no model, returns -1. Otherwise,
     * it always returns a valid row.  To test if the returned object is 
     * exactly at x, y, get the bounds for the node at the returned
     * row and test x, y against that.
     *
     * @param x an integer giving the number of pixels horizontally from
     *          the left edge of the display area, minus any left margin
     * @param y an integer giving the number of pixels vertically from
     *          the top of the display area, minus any top margin
     * @return the row closest to the location, -1 if nothing is
     *         viewable or there is no model
     *
     * @see #getRowForLocation
     * @see #getRowBounds
     */
    public int getClosestRowForLocation(int x, int y) {
	return getRowForPath(getClosestPathForLocation(x, y));
    }

    /**
     * Returns true if the tree is being edited. The item that is being
     * edited can be obtained using <code>getSelectionPath</code>.
     *
     * @return true if the user is currently editing a node
     * @see #getSelectionPath
     */
    public boolean isEditing() {
        TreeUI                  tree = getUI();

        if(tree != null)
            return tree.isEditing(this);
        return false;
    }

    /**
     * Ends the current editing session.
     * (The <code>DefaultTreeCellEditor</code> 
     * object saves any edits that are currently in progress on a cell.
     * Other implementations may operate differently.) 
     * Has no effect if the tree isn't being edited.
     * <blockquote>
     * <b>Note:</b><br>
     * To make edit-saves automatic whenever the user changes
     * their position in the tree, use {@link #setInvokesStopCellEditing}.
     * </blockquote>
     *
     * @return true if editing was in progress and is now stopped,
     *              false if editing was not in progress
     */
    public boolean stopEditing() {
        TreeUI                  tree = getUI();

        if(tree != null)
            return tree.stopEditing(this);
        return false;
    }

    /**
     * Cancels the current editing session. Has no effect if the
     * tree isn't being edited.
     */
    public void  cancelEditing() {
        TreeUI                  tree = getUI();

        if(tree != null)
	    tree.cancelEditing(this);
    }

    /**
     * Selects the node identified by the specified path and initiates
     * editing.  The edit-attempt fails if the <code>CellEditor</code>
     * does not allow
     * editing for the specified item.
     * 
     * @param path  the <code>TreePath</code> identifying a node
     */
    public void startEditingAtPath(TreePath path) {
        TreeUI                  tree = getUI();

        if(tree != null)
            tree.startEditingAtPath(this, path);
    }

    /**
     * Returns the path to the element that is currently being edited.
     *
     * @return  the <code>TreePath</code> for the node being edited
     */
    public TreePath getEditingPath() {
        TreeUI                  tree = getUI();

        if(tree != null)
            return tree.getEditingPath(this);
        return null;
    }

    //
    // Following are primarily convenience methods for mapping from
    // row based selections to path selections.  Sometimes it is
    // easier to deal with these than paths (mouse downs, key downs
    // usually just deal with index based selections).
    // Since row based selections require a UI many of these won't work
    // without one.
    //

    /**
     * Sets the tree's selection model. When a <code>null</code> value is
     * specified an emtpy
     * <code>selectionModel</code> is used, which does not allow selections.
     *
     * @param selectionModel the <code>TreeSelectionModel</code> to use,
     *		or <code>null</code> to disable selections
     * @see TreeSelectionModel
     * @beaninfo
     *        bound: true
     *  description: The tree's selection model.
     */
    public void setSelectionModel(TreeSelectionModel selectionModel) {
        if(selectionModel == null)
            selectionModel = EmptySelectionModel.sharedInstance();

        TreeSelectionModel         oldValue = this.selectionModel;

	if (this.selectionModel != null && selectionRedirector != null) {
            this.selectionModel.removeTreeSelectionListener
		                (selectionRedirector);
	}
        if (accessibleContext != null) {
           this.selectionModel.removeTreeSelectionListener((TreeSelectionListener)accessibleContext);
           selectionModel.addTreeSelectionListener((TreeSelectionListener)accessibleContext);
        }

        this.selectionModel = selectionModel;
	if (selectionRedirector != null) {
            this.selectionModel.addTreeSelectionListener(selectionRedirector);
	}
        firePropertyChange(SELECTION_MODEL_PROPERTY, oldValue,
                           this.selectionModel);

        if (accessibleContext != null) {
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                    new Boolean(false), new Boolean(true));
        }
    }

    /**
     * Returns the model for selections. This should always return a 
     * non-<code>null</code> value. If you don't want to allow anything
     * to be selected
     * set the selection model to <code>null</code>, which forces an empty
     * selection model to be used.
     *
     * @param the <code>TreeSelectionModel</code> in use
     * @see #setSelectionModel
     */
    public TreeSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Returns <code>JTreePath</code> instances representing the path
     * between index0 and index1 (including index1).
     * Returns <code>null</code> if there is no tree.
     *
     * @param index0  an integer specifying a display row, where 0 is the
     *                first row in the display
     * @param index1  an integer specifying a second display row
     * @return an array of <code>TreePath</code> objects, one for each
     *		node between index0 and index1, inclusive; or <code>null</code>
     *		if there is no tree
     */
    protected TreePath[] getPathBetweenRows(int index0, int index1) {
        int              newMinIndex, newMaxIndex;
        TreeUI           tree = getUI();

        newMinIndex = Math.min(index0, index1);
        newMaxIndex = Math.max(index0, index1);

        if(tree != null) {
            TreePath[]            selection = new TreePath[newMaxIndex -
                                                            newMinIndex + 1];

            for(int counter = newMinIndex; counter <= newMaxIndex; counter++)
                selection[counter - newMinIndex] = tree.getPathForRow(this,
								      counter);
            return selection;
        }
        return null;
    }

    /**
     * Selects the nodes between index0 and index1, inclusive.
     *
     * @param index0  an integer specifying a display row, where 0 is the
     *                first row in the display
     * @param index1  an integer specifying a second display row
    */
    public void setSelectionInterval(int index0, int index1) {
        TreePath[]         paths = getPathBetweenRows(index0, index1);

        this.getSelectionModel().setSelectionPaths(paths);
    }

    /**
     * Adds the paths between index0 and index1, inclusive, to the 
     * selection.
     *
     * @param index0  an integer specifying a display row, where 0 is the
     *                first row in the display
     * @param index1  an integer specifying a second display row
     */
    public void addSelectionInterval(int index0, int index1) {
        TreePath[]         paths = getPathBetweenRows(index0, index1);

        this.getSelectionModel().addSelectionPaths(paths);
    }

    /**
     * Removes the nodes between index0 and index1, inclusive, from the 
     * selection.
     *
     * @param index0  an integer specifying a display row, where 0 is the
     *                first row in the display
     * @param index1  an integer specifying a second display row
     */
    public void removeSelectionInterval(int index0, int index1) {
        TreePath[]         paths = getPathBetweenRows(index0, index1);

        this.getSelectionModel().removeSelectionPaths(paths);
    }

    /**
     * Removes the node identified by the specified path from the current
     * selection.
     * 
     * @param path  the <code>TreePath</code> identifying a node
     */
    public void removeSelectionPath(TreePath path) {
        this.getSelectionModel().removeSelectionPath(path);
    }

    /**
     * Removes the nodes identified by the specified paths from the 
     * current selection.
     *
     * @param paths an array of <code>TreePath</code> objects that
     *              specifies the nodes to remove
     */
    public void removeSelectionPaths(TreePath[] paths) {
        this.getSelectionModel().removeSelectionPaths(paths);
    }

    /**
     * Removes the path at the index <code>row</code> from the current
     * selection.
     * 
     * @param path  the TreePath identifying the node to remove
     */
    public void removeSelectionRow(int row) {
        int[]             rows = { row };

        removeSelectionRows(rows);
    }

    /**
     * Removes the paths that are selected at each of the specified
     * rows.
     *
     * @param row  an array of ints specifying display rows, where 0 is 
     *             the first row in the display
     */
    public void removeSelectionRows(int[] rows) {
        TreeUI             ui = getUI();

        if(ui != null && rows != null) {
            int                  numRows = rows.length;
            TreePath[]           paths = new TreePath[numRows];

            for(int counter = 0; counter < numRows; counter++)
                paths[counter] = ui.getPathForRow(this, rows[counter]);
            removeSelectionPaths(paths);
        }
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        getSelectionModel().clearSelection();
    }

    /**
     * Returns true if the selection is currently empty.
     *
     * @return true if the selection is currently empty
     */
    public boolean isSelectionEmpty() {
        return getSelectionModel().isSelectionEmpty();
    }

    /**
     * Adds a listener for <code>TreeExpansion</code> events.
     *
     * @param tel a TreeExpansionListener that will be notified when
     *            a tree node is expanded or collapsed (a "negative
     *            expansion")
     */
    public void addTreeExpansionListener(TreeExpansionListener tel) {
	if (settingUI) {
	    uiTreeExpansionListener = tel;
	}
        listenerList.add(TreeExpansionListener.class, tel);
    }

    /**
     * Removes a listener for <code>TreeExpansion</code> events.
     *
     * @param tel the <code>TreeExpansionListener</code> to remove
     */
    public void removeTreeExpansionListener(TreeExpansionListener tel) {
        listenerList.remove(TreeExpansionListener.class, tel);
	if (uiTreeExpansionListener == tel) {
	    uiTreeExpansionListener = null;
	}
    }

    /**
     * Adds a listener for <code>TreeWillExpand</code> events.
     *
     * @param tel a <code>TreeWillExpandListener</code> that will be notified 
     *            when a tree node will be expanded or collapsed (a "negative
     *            expansion")
     */
    public void addTreeWillExpandListener(TreeWillExpandListener tel) {
        listenerList.add(TreeWillExpandListener.class, tel);
    }

    /**
     * Removes a listener for <code>TreeWillExpand</code> events.
     *
     * @param tel the <code>TreeWillExpandListener</code> to remove
     */
    public void removeTreeWillExpandListener(TreeWillExpandListener tel) {
        listenerList.remove(TreeWillExpandListener.class, tel);
    }

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param path the <code>TreePath</code> indicating the node that was
     *		expanded
     * @see EventListenerList
     */
     public void fireTreeExpanded(TreePath path) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeExpansionEvent e = null;
	if (uiTreeExpansionListener != null) {
	    e = new TreeExpansionEvent(this, path);
	    uiTreeExpansionListener.treeExpanded(e);
	}
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeExpansionListener.class &&
		listeners[i + 1] != uiTreeExpansionListener) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeExpansionEvent(this, path);
                ((TreeExpansionListener)listeners[i+1]).
                    treeExpanded(e);
            }          
        }
    }   

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param path the <code>TreePath</code> indicating the node that was
     *		collapsed
     * @see EventListenerList
     */
    public void fireTreeCollapsed(TreePath path) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeExpansionEvent e = null;
	if (uiTreeExpansionListener != null) {
	    e = new TreeExpansionEvent(this, path);
	    uiTreeExpansionListener.treeCollapsed(e);
	}
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeExpansionListener.class &&
		listeners[i + 1] != uiTreeExpansionListener) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeExpansionEvent(this, path);
                ((TreeExpansionListener)listeners[i+1]).
                    treeCollapsed(e);
            }          
        }
    }   

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param path the <code>TreePath</code> indicating the node that was
     *		expanded
     * @see EventListenerList
     */
     public void fireTreeWillExpand(TreePath path) throws ExpandVetoException {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeExpansionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeWillExpandListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeExpansionEvent(this, path);
                ((TreeWillExpandListener)listeners[i+1]).
                    treeWillExpand(e);
            }          
        }
    }   

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param path the <code>TreePath</code> indicating the node that was
     *		expanded
     * @see EventListenerList
     */
     public void fireTreeWillCollapse(TreePath path) throws ExpandVetoException {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeExpansionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeWillExpandListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeExpansionEvent(this, path);
                ((TreeWillExpandListener)listeners[i+1]).
                    treeWillCollapse(e);
            }          
        }
    }   

    /**
     * Adds a listener for <code>TreeSelection</code> events.
     *
     * @param tsl the <code>TreeSelectionListener</code> that will be notified 
     *            when a node is selected or deselected (a "negative
     *            selection")
     */
    public void addTreeSelectionListener(TreeSelectionListener tsl) {
        listenerList.add(TreeSelectionListener.class,tsl);
        if(listenerList.getListenerCount(TreeSelectionListener.class) != 0
           && selectionRedirector == null) {
            selectionRedirector = new TreeSelectionRedirector();
            selectionModel.addTreeSelectionListener(selectionRedirector);
        }
    }

    /**
     * Removes a <code>TreeSelection</code> listener.
     *
     * @param tsl the <code>TreeSelectionListener</code> to remove
     */
    public void removeTreeSelectionListener(TreeSelectionListener tsl) {
        listenerList.remove(TreeSelectionListener.class,tsl);
        if(listenerList.getListenerCount(TreeSelectionListener.class) == 0
           && selectionRedirector != null) {
            selectionModel.removeTreeSelectionListener
                (selectionRedirector);
            selectionRedirector = null;
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param e the <code>TreeSelectionEvent</code> generated by the
     *		<code>TreeSelectionModel</code>
     *          when a node is selected or deselected
     * @see EventListenerList
     */
    protected void fireValueChanged(TreeSelectionEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            // TreeSelectionEvent e = null;
            if (listeners[i]==TreeSelectionListener.class) {
                // Lazily create the event:
                // if (e == null)
                // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                ((TreeSelectionListener)listeners[i+1]).valueChanged(e);
            }          
        }
    }

    /**
     * Sent when the tree has changed enough that we need to resize
     * the bounds, but not enough that we need to remove the
     * expanded node set (e.g nodes were expanded or collapsed, or
     * nodes were inserted into the tree). You should never have to
     * invoke this, the UI will invoke this as it needs to.
     */
    public void treeDidChange() {
        revalidate();
        repaint();
    }

    /**
     * Sets the number of rows that are to be displayed.
     * This will only work if the reciever is contained in a 
     * <code>JScrollPane</code>,
     * and will adjust the preferred size and size of that scrollpane.
     *
     * @param newCount the number of rows to display
     * @beaninfo
     *        bound: true
     *  description: The number of rows that are to be displayed.
     */
    public void setVisibleRowCount(int newCount) {
        int                 oldCount = visibleRowCount;

        visibleRowCount = newCount;
        firePropertyChange(VISIBLE_ROW_COUNT_PROPERTY, oldCount,
                           visibleRowCount);
        invalidate();
        if (accessibleContext != null) {
            ((AccessibleJTree)accessibleContext).fireVisibleDataPropertyChange();
        }
    }

    /**
     * Returns the number of rows that are displayed in the display area.
     *
     * @return the number of rows displayed
     */
    public int getVisibleRowCount() {
        return visibleRowCount;
    }

    // Serialization support.  
    private void writeObject(ObjectOutputStream s) throws IOException {
        Vector      values = new Vector();

        s.defaultWriteObject();
        // Save the cellRenderer, if its Serializable.
        if(cellRenderer != null && cellRenderer instanceof Serializable) {
            values.addElement("cellRenderer");
            values.addElement(cellRenderer);
        }
        // Save the cellEditor, if its Serializable.
        if(cellEditor != null && cellEditor instanceof Serializable) {
            values.addElement("cellEditor");
            values.addElement(cellEditor);
        }
        // Save the treeModel, if its Serializable.
        if(treeModel != null && treeModel instanceof Serializable) {
            values.addElement("treeModel");
            values.addElement(treeModel);
        }
        // Save the selectionModel, if its Serializable.
        if(selectionModel != null && selectionModel instanceof Serializable) {
            values.addElement("selectionModel");
            values.addElement(selectionModel);
        }

	Object      expandedData = getArchivableExpandedState();

	if(expandedData != null) {
            values.addElement("expandedState");
            values.addElement(expandedData);
	}

        s.writeObject(values);
	if ((ui != null) && (getUIClassID().equals(uiClassID))) {
	    ui.installUI(this);
	}
    }

    private void readObject(ObjectInputStream s) 
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();

	// Create an instance of expanded state.

	expandedState = new Hashtable();

	expandedStack = new Stack();

        Vector          values = (Vector)s.readObject();
        int             indexCounter = 0;
        int             maxCounter = values.size();

        if(indexCounter < maxCounter && values.elementAt(indexCounter).
           equals("cellRenderer")) {
            cellRenderer = (TreeCellRenderer)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter < maxCounter && values.elementAt(indexCounter).
           equals("cellEditor")) {
            cellEditor = (TreeCellEditor)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter < maxCounter && values.elementAt(indexCounter).
           equals("treeModel")) {
            treeModel = (TreeModel)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter < maxCounter && values.elementAt(indexCounter).
           equals("selectionModel")) {
            selectionModel = (TreeSelectionModel)values.elementAt(++indexCounter);
            indexCounter++;
        }
        if(indexCounter < maxCounter && values.elementAt(indexCounter).
           equals("expandedState")) {
	    unarchiveExpandedState(values.elementAt(++indexCounter));
            indexCounter++;
        }
	// Reinstall the redirector.
        if(listenerList.getListenerCount(TreeSelectionListener.class) != 0) {
            selectionRedirector = new TreeSelectionRedirector();
            selectionModel.addTreeSelectionListener(selectionRedirector);
        }
	// Listener to TreeModel.
	if(treeModel != null) {
	    treeModelListener = createTreeModelListener();
	    if(treeModelListener != null)
		treeModel.addTreeModelListener(treeModelListener);
	}
    }

    /**
     * Returns an object that can be archived indicating what nodes are
     * expanded and what aren't. The objects from the model are NOT
     * written out.
     */
    private Object getArchivableExpandedState() {
	TreeModel       model = getModel();

	if(model != null) {
	    Enumeration        paths = expandedState.keys();

	    if(paths != null) {
		Vector         state = new Vector();

		while(paths.hasMoreElements()) {
		    TreePath   path = (TreePath)paths.nextElement();
		    Object     archivePath;

		    try {
			archivePath = getModelIndexsForPath(path);
		    } catch (Error error) {
			archivePath = null;
		    }
		    if(archivePath != null) {
			state.addElement(archivePath);
			state.addElement(expandedState.get(path));
		    }
		}
		return state;
	    }
	}
	return null;
    }

    /**
     * Updates the expanded state of nodes in the tree based on the 
     * previously archived state <code>state</code>.
     */
    private void unarchiveExpandedState(Object state) {
	if(state instanceof Vector) {
	    Vector          paths = (Vector)state;

	    for(int counter = paths.size() - 1; counter >= 0; counter--) {
		Boolean        eState = (Boolean)paths.elementAt(counter--);
		TreePath       path;

		try {
		    path = getPathForIndexs((int[])paths.elementAt(counter));
		    if(path != null)
			expandedState.put(path, eState);
		} catch (Error error) {}
	    }
	}
    }

    /**
     * Returns an array of integers specifying the indexs of the
     * components in the <code>path</code>. If <code>path</code> is
     * the root, this will return an empty array.  If <code>path</code>
     * is <code>null</code>, <code>null</code> will be returned.
     */
    private int[] getModelIndexsForPath(TreePath path) {
	if(path != null) {
	    TreeModel   model = getModel();
	    int         count = path.getPathCount();
	    int[]       indexs = new int[count - 1];
	    Object      parent = model.getRoot();

	    for(int counter = 1; counter < count; counter++) {
		indexs[counter - 1] = model.getIndexOfChild
			           (parent, path.getPathComponent(counter));
		parent = path.getPathComponent(counter);
		if(indexs[counter - 1] < 0)
		    return null;
	    }
	    return indexs;
	}
	return null;
    }

    /**
     * Returns a <code>TreePath</code> created by obtaining the children
     * for each of the indices in <code>indexs</code>. If <code>indexs</code>
     * or the <code>TreeModel</code> is <code>null</code>, it will return
     * <code>null</code>.
     */
    private TreePath getPathForIndexs(int[] indexs) {
	if(indexs == null)
	    return null;

	TreeModel    model = getModel();

	if(model == null)
	    return null;

	int          count = indexs.length;
	Object       parent = model.getRoot();
	TreePath     parentPath = new TreePath(parent);

	for(int counter = 0; counter < count; counter++) {
	    parent = model.getChild(parent, indexs[counter]);
	    if(parent == null)
		return null;
	    parentPath = parentPath.pathByAddingChild(parent);
	}
	return parentPath;
    }

    /**
     * <code>EmptySelectionModel</code> is a <code>TreeSelectionModel</code>
     * that does not allow anything to be selected.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    protected static class EmptySelectionModel extends
              DefaultTreeSelectionModel
    {
        /** Unique shared instance. */
        protected static final EmptySelectionModel sharedInstance =
            new EmptySelectionModel();

        /** Returns a shared instance of an empty selection model. */
        static public EmptySelectionModel sharedInstance() {
            return sharedInstance;
        }

        /** A <code>null</code> implementation that selects nothing. */
        public void setSelectionPaths(TreePath[] pPaths) {}
        /** A <code>null</code> implementation that adds nothing. */
        public void addSelectionPaths(TreePath[] paths) {}
        /** A <code>null</code> implementation that removes nothing. */
        public void removeSelectionPaths(TreePath[] paths) {}
    }


    /**
     * Handles creating a new <code>TreeSelectionEvent</code> with the 
     * <code>JTree</code> as the
     * source and passing it off to all the listeners.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    protected class TreeSelectionRedirector implements Serializable,
                    TreeSelectionListener
    {
        /**
         * Invoked by the <code>TreeSelectionModel</code> when the
         * selection changes.
         * 
         * @param e the <code>TreeSelectionEvent</code> generated by the
         *		<code>TreeSelectionModel</code>
         */
        public void valueChanged(TreeSelectionEvent e) {
            TreeSelectionEvent       newE;

            newE = (TreeSelectionEvent)e.cloneWithSource(JTree.this);
            fireValueChanged(newE);
        }
    } // End of class JTree.TreeSelectionRedirector

    //
    // Scrollable interface
    //

    /**
     * Returns the preferred display size of a <code>JTree</code>. The height is
     * determined from <code>getVisibleRowCount</code> and the width
     * is the current preferred width.
     *
     * @return a <code>Dimension</code> object containing the preferred size
     */
    public Dimension getPreferredScrollableViewportSize() {
        int                 width = getPreferredSize().width;
        int                 visRows = getVisibleRowCount();
        int                 height;

        if(isFixedRowHeight())
            height = visRows * getRowHeight();
        else {
            TreeUI          ui = getUI();

            if(ui != null && ui.getRowCount(this) > 0)
                height = getRowBounds(0).height * visRows;
            else
                height = 16 * visRows;
        }
        return new Dimension(width, height);
    }

    /**
     * Returns the amount to increment when scrolling. The amount is
     * the height of the first displayed row that isn't completely in view
     * or, if it is totally displayed, the height of the next row in the
     * scrolling direction.
     * 
     * @param visibleRect the view area visible within the viewport
     * @param orientation either <code>SwingConstants.VERTICAL</code>
     *		or <code>SwingConstants.HORIZONTAL</code>
     * @param direction less than zero to scroll up/left,
     *		greater than zero for down/right
     * @return the "unit" increment for scrolling in the specified direction
     * @see JScrollBar#setUnitIncrement(int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation, int direction) {
        if(orientation == SwingConstants.VERTICAL) {
            Rectangle       rowBounds;
            int             firstIndex = getClosestRowForLocation
                                         (0, visibleRect.y);

            if(firstIndex != -1) {
                rowBounds = getRowBounds(firstIndex);
                if(rowBounds.y != visibleRect.y) {
                    if(direction < 0) // UP
                        return (visibleRect.y - rowBounds.y);
                    return (rowBounds.y + rowBounds.height - visibleRect.y);
                }
                if(direction < 0) { // UP
                    if(firstIndex != 0) {
                        rowBounds = getRowBounds(firstIndex - 1);
                        return rowBounds.height;
                    }
                }
                else {
                    return rowBounds.height;
                }
            }
            return 0;
        }
        return 4;
    }


    /**
     * Returns the amount for a block increment, which is the height or
     * width of <code>visibleRect</code>, based on <code>orientation</code>.
     * 
     * @param visibleRect the view area visible within the viewport
     * @param orientation either <code>SwingConstants.VERTICAL</code>
     *		or <code>SwingConstants.HORIZONTAL</code>
     * @param direction less than zero to scroll up/left,
     *		greater than zero for down/right.
     * @return the "block" increment for scrolling in the specified direction
     * @see JScrollBar#setBlockIncrement(int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation, int direction) {
        return (orientation == SwingConstants.VERTICAL) ? visibleRect.height :
            visibleRect.width;
    }
    
    /**
     * Returns false to indicate that the width of the viewport does not 
     * determine the width of the table, unless the preferred width of 
     * the tree is smaller than the viewports width.  In other words: 
     * ensure that the tree is never smaller than its viewport.
     * 
     * @return false
     * @see Scrollable#getScrollableTracksViewportWidth
     */
    public boolean getScrollableTracksViewportWidth() {
	if (getParent() instanceof JViewport) {
	    return (((JViewport)getParent()).getWidth() > getPreferredSize().width);
	}
	return false;
    }

    /**
     * Returns false to indicate that the height of the viewport does not 
     * determine the height of the table, unless the preferred height
     * of the tree is smaller than the viewports height.  In other words: 
     * ensure that the tree is never smaller than its viewport.
     * 
     * @return false
     * @see Scrollable#getScrollableTracksViewportHeight
     */
    public boolean getScrollableTracksViewportHeight() {
	if (getParent() instanceof JViewport) {
	    return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
	}
	return false;
    }

    /**
     * Sets the expanded state of this <code>JTree</code>.
     * If <code>state</code> is
     * true, all parents of <code>path</code> and path are marked as
     * expanded. If <code>state</code> is false, all parents of 
     * <code>path</code> are marked EXPANDED, but <code>path</code> itself
     * is marked collapsed.<p>
     * This will fail if a <code>TreeWillExpandListener</code> vetos it.
     */
    protected void setExpandedState(TreePath path, boolean state) {
	if(path != null) {
	    // Make sure all parents of path are expanded.
	    Stack         stack;
	    TreePath      parentPath = path.getParentPath();

	    if (expandedStack.size() == 0) {
		stack = new Stack();
	    }
	    else {
		stack = (Stack)expandedStack.pop();
	    }

	    try {
		while(parentPath != null) {
		    if(isExpanded(parentPath)) {
			parentPath = null;
		    }
		    else {
			stack.push(parentPath);
			parentPath = parentPath.getParentPath();
		    }
		}
		for(int counter = stack.size() - 1; counter >= 0; counter--) {
		    parentPath = (TreePath)stack.pop();
		    if(!isExpanded(parentPath)) {
			try {
			    fireTreeWillExpand(parentPath);
			} catch (ExpandVetoException eve) {
			    // Expand vetoed!
			    return;
			}
			expandedState.put(parentPath, Boolean.TRUE);
			fireTreeExpanded(parentPath);
			if (accessibleContext != null) {
			    ((AccessibleJTree)accessibleContext).
			                      fireVisibleDataPropertyChange();
			}
		    }
		}
	    }
	    finally {
		if (expandedStack.size() < TEMP_STACK_SIZE) {
		    stack.removeAllElements();
		    expandedStack.push(stack);
		}
	    }
	    if(!state) {
		// collapse last path.
		Object          cValue = expandedState.get(path);

		if(cValue != null && ((Boolean)cValue).booleanValue()) {
		    try {
			fireTreeWillCollapse(path);
		    }
		    catch (ExpandVetoException eve) {
			return;
		    }
		    expandedState.put(path, Boolean.FALSE);
		    fireTreeCollapsed(path);
		    if (removeDescendantSelectedPaths(path, false) &&
			!isPathSelected(path)) {
			// A descendant was selected, select the parent.
			addSelectionPath(path);
		    }
		    if (accessibleContext != null) {
			((AccessibleJTree)accessibleContext).
			            fireVisibleDataPropertyChange();
		    }
		}
	    }
	    else {
		// Expand last path.
		Object          cValue = expandedState.get(path);

		if(cValue == null || !((Boolean)cValue).booleanValue()) {
		    try {
			fireTreeWillExpand(path);
		    }
		    catch (ExpandVetoException eve) {
			return;
		    }
		    expandedState.put(path, Boolean.TRUE);
		    fireTreeExpanded(path);
		    if (accessibleContext != null) {
			((AccessibleJTree)accessibleContext).
			                  fireVisibleDataPropertyChange();
		    }
		}
	    }
	}
    }

    /**
     * Returns an <code>Enumeration</code> of <code>TreePaths</code>
     * that have been expanded that
     * are descendants of <code>parent</code>.
     */
    protected Enumeration getDescendantToggledPaths(TreePath parent) {
	if(parent == null)
	    return null;

	Vector            descendants = new Vector();
	Enumeration       nodes = expandedState.keys();
	TreePath          path;

	while(nodes.hasMoreElements()) {
	    path = (TreePath)nodes.nextElement();
	    if(parent.isDescendant(path))
		descendants.addElement(path);
	}
	return descendants.elements();
    }
    
    /**
     * Removes any descendants of the <code>TreePaths</code> in
     * <code>toRemove</code>
     * that have been expanded.
     */
     protected void removeDescendantToggledPaths(Enumeration toRemove) {
	 if(toRemove != null) {
	     while(toRemove.hasMoreElements()) {
		 Enumeration         descendants = getDescendantToggledPaths
		                         ((TreePath)toRemove.nextElement());

		 if(descendants != null) {
		     while(descendants.hasMoreElements()) {
			 expandedState.remove(descendants.nextElement());
		     }
		 }
	     }
	 }
     }

     /**
      * Clears the cache of toggled tree paths. This does NOT send out
      * any <code>TreeExpansionListener</code> events.
      */
     protected void clearToggledPaths() {
	 expandedState.clear();
     }

     /**
      * Creates and returns an instance of <code>TreeModelHandler</code>.
      * The returned
      * object is responsible for updating the expanded state when the
      * <code>TreeModel</code> changes.
      * <p>
      * For more information on what expanded state means, see the
      * <a href=#jtree_description>JTree description</a> above.
      */
     protected TreeModelListener createTreeModelListener() {
	 return new TreeModelHandler();
     }

    /**
     * Removes any paths in the selection that are descendants of
     * <code>path</code>. If <code>includePath</code> is true and
     * <code>path</code> is selected, it will be removed from the selection.
     *
     * @return true if a descendant was selected
     * @since 1.3
     */
    protected boolean removeDescendantSelectedPaths(TreePath path,
						    boolean includePath) {
	TreePath[]    toRemove = getDescendantSelectedPaths(path, includePath);

	if (toRemove != null) {
	    getSelectionModel().removeSelectionPaths(toRemove);
	    return true;
	}
	return false;
    }

    /**
     * Returns an array of paths in the selection that are descendants of
     * <code>path</code>. The returned array may contain <code>null</code>s.
     */
    private TreePath[] getDescendantSelectedPaths(TreePath path,
						  boolean includePath) {
	TreeSelectionModel   sm = getSelectionModel();
	TreePath[]           selPaths = (sm != null) ? sm.getSelectionPaths() :
	                                null;

	if(selPaths != null) {
	    boolean        shouldRemove = false;

	    for(int counter = selPaths.length - 1; counter >= 0; counter--) {
		if(selPaths[counter] != null &&
		   path.isDescendant(selPaths[counter]) &&
		   (!path.equals(selPaths[counter]) || includePath))
		    shouldRemove = true;
		else
		    selPaths[counter] = null;
	    }
	    if(!shouldRemove) {
		selPaths = null;
	    }
	    return selPaths;
	}
	return null;
    }

    /**
     * Removes any paths from the selection model that are descendants of
     * the nodes identified by in <code>e</code>.
     */
    void removeDescendantSelectedPaths(TreeModelEvent e) {
	TreePath            pPath = e.getTreePath();
	Object[]            oldChildren = e.getChildren();
	TreeSelectionModel  sm = getSelectionModel();

	if (sm != null && pPath != null && oldChildren != null &&
	    oldChildren.length > 0) {
	    for (int counter = oldChildren.length - 1; counter >= 0;
		 counter--) {
		// Might be better to call getDescendantSelectedPaths
		// numerous times, then push to the model.
		removeDescendantSelectedPaths(pPath.pathByAddingChild
					      (oldChildren[counter]), true);
	    }
	}
    }


     /**
      * Listens to the model and updates the <code>expandedState</code>
      * accordingly when nodes are removed, or changed.
      */
    protected class TreeModelHandler implements TreeModelListener {
	public void treeNodesChanged(TreeModelEvent e) { }

	public void treeNodesInserted(TreeModelEvent e) { }

	public void treeStructureChanged(TreeModelEvent e) {
	    if(e == null)
		return;

	    // NOTE: If I change this to NOT remove the descendants
	    // and update BasicTreeUIs treeStructureChanged method
	    // to update descendants in response to a treeStructureChanged
	    // event, all the children of the event won't collapse!
	    TreePath            parent = e.getTreePath();

	    if(parent == null)
		return;

	    if (parent.getPathCount() == 1) {
		// New root, remove everything!
		clearToggledPaths();
		if(!treeModel.isLeaf(treeModel.getRoot())) {
		    // Mark the root as expanded, if it isn't a leaf.
		    expandedState.put(parent, Boolean.TRUE);
		}
	    }
	    else if(expandedState.get(parent) != null) {
		Vector              toRemove = new Vector(1);
		boolean             isExpanded = isExpanded(parent);

		toRemove.addElement(parent);
		removeDescendantToggledPaths(toRemove.elements());
		if(isExpanded) {
		    TreeModel         model = getModel();

		    if(model == null || model.isLeaf
		       (parent.getLastPathComponent()))
			collapsePath(parent);
		    else
			expandedState.put(parent, Boolean.TRUE);
		}
	    }
	    removeDescendantSelectedPaths(parent, false);
	}

	public void treeNodesRemoved(TreeModelEvent e) {
	    if(e == null)
		return;

	    TreePath            parent = e.getTreePath();
	    Object[]            children = e.getChildren();

	    if(children == null)
		return;

	    TreePath            rPath;
	    Vector              toRemove = new Vector(Math.max
						      (1, children.length));

	    for(int counter = children.length - 1; counter >= 0; counter--) {
		rPath = parent.pathByAddingChild(children[counter]);
		if(expandedState.get(rPath) != null)
		    toRemove.addElement(rPath);
	    }
	    if(toRemove.size() > 0)
		removeDescendantToggledPaths(toRemove.elements());

	    TreeModel         model = getModel();

	    if(model == null || model.isLeaf(parent.getLastPathComponent()))
		expandedState.remove(parent);

	    removeDescendantSelectedPaths(e);
	}
    }


    /**
     * <code>DynamicUtilTreeNode</code> can wrap 
     * vectors/hashtables/arrays/strings and
     * create the appropriate children tree nodes as necessary. It is
     * dynamic in that it will only create the children as necessary.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class DynamicUtilTreeNode extends DefaultMutableTreeNode {
        /**
         * Does the this <code>JTree</code> have children?
         * This property is currently not implemented.
         */
        protected boolean            hasChildren;
        /** Value to create children with. */
        protected Object             childValue;
        /** Have the children been loaded yet? */
        protected boolean            loadedChildren;

        /**
         * Adds to parent all the children in <code>children</code>.
         * If <code>children</code> is an array or vector all of its
         * elements are added is children, otherwise if <code>children</code>
         * is a hashtable all the key/value pairs are added in the order
         * <code>Enumeration</code> returns them.
         */
        public static void createChildren(DefaultMutableTreeNode parent,
                                          Object children) {
            if(children instanceof Vector) {
                Vector          childVector = (Vector)children;

                for(int counter = 0, maxCounter = childVector.size();
                    counter < maxCounter; counter++)
                    parent.add(new DynamicUtilTreeNode
                               (childVector.elementAt(counter),
                                childVector.elementAt(counter)));
            }
            else if(children instanceof Hashtable) {
                Hashtable           childHT = (Hashtable)children;
                Enumeration         keys = childHT.keys();
                Object              aKey;

                while(keys.hasMoreElements()) {
                    aKey = keys.nextElement();
                    parent.add(new DynamicUtilTreeNode(aKey,
                                                       childHT.get(aKey)));
                }
            }
            else if(children instanceof Object[]) {
                Object[]             childArray = (Object[])children;

                for(int counter = 0, maxCounter = childArray.length;
                    counter < maxCounter; counter++)
                    parent.add(new DynamicUtilTreeNode(childArray[counter],
                                                       childArray[counter]));
            }
        }

        /**
         * Creates a node with the specified object as its value and
         * with the specified children. For the node to allow children,
         * the children-object must be an array of objects, a
         * <code>Vector</code>, or a <code>Hashtable</code> -- even
         * if empty. Otherwise, the node is not
         * allowed to have children.
         *
         * @param value  the <code>Object</code> that is the value for the
         *		new node
         * @param children an array of <code>Object</code>s, a
         *		<code>Vector</code>, or a <code>Hashtable</code>
         *		used to create the child nodes; if any other
         *		object is specified, or if the value is
         *		<code>null</code>,
         *		then the node is not allowed to have children
         */
        public DynamicUtilTreeNode(Object value, Object children) {
            super(value);
            loadedChildren = false;
            childValue = children;
            if(children != null) {
                if(children instanceof Vector)
                    setAllowsChildren(true);
                else if(children instanceof Hashtable)
                    setAllowsChildren(true);
                else if(children instanceof Object[])
                    setAllowsChildren(true);
                else
                    setAllowsChildren(false);
            }
            else
                setAllowsChildren(false);
        }

        /**
         * Returns true if this node allows children. Whether the node
         * allows children depends on how it was created.
         *
         * @return true if this node allows children, false otherwise
         * @see #JTree.DynamicUtilTreeNode
         */
        public boolean isLeaf() {
            return !getAllowsChildren();
        }

        /**
         * Returns the number of child nodes.
         *
         * @return the number of child nodes
         */
        public int getChildCount() {
            if(!loadedChildren)
                loadChildren();
            return super.getChildCount();
        }

        /**
         * Loads the children based on <code>childValue</code>.
         * If <code>childValue</code> is a <code>Vector</code>
         * or array each element is added as a child,
         * if <code>childValue</code> is a <code>Hashtable</code>
         * each key/value pair is added in the order that
         * <code>Enumeration</code> returns the keys.
         */
        protected void loadChildren() {
            loadedChildren = true;
            createChildren(this, childValue);
        }

	/**
	 * Subclassed to load the children, if necessary.
	 */
	public TreeNode getChildAt(int index) {
	    if(!loadedChildren)
		loadChildren();
	    return super.getChildAt(index);
	}

	/**
	 * Subclassed to load the children, if necessary.
	 */
	public Enumeration children() {
	    if(!loadedChildren)
		loadChildren();
	    return super.children();
	}
    }


    /**
     * Returns a string representation of this <code>JTree</code>.
     * This method 
     * is intended to be used only for debugging purposes, and the 
     * content and format of the returned string may vary between      
     * implementations. The returned string may be empty but may not 
     * be <code>null</code>.
     * 
     * @return  a string representation of this <code>JTree</code>.
     */
    protected String paramString() {
        String rootVisibleString = (rootVisible ?
                                    "true" : "false");
        String showsRootHandlesString = (showsRootHandles ?
					 "true" : "false");
        String editableString = (editable ?
				 "true" : "false");
        String largeModelString = (largeModel ?
				   "true" : "false");
        String invokesStopCellEditingString = (invokesStopCellEditing ?
					       "true" : "false");
        String scrollsOnExpandString = (scrollsOnExpand ?
					"true" : "false");

        return super.paramString() +
        ",editable=" + editableString +
        ",invokesStopCellEditing=" + invokesStopCellEditingString +
        ",largeModel=" + largeModelString +
        ",rootVisible=" + rootVisibleString +
        ",rowHeight=" + rowHeight +
        ",scrollsOnExpand=" + scrollsOnExpandString +
        ",showsRootHandles=" + showsRootHandlesString +
        ",toggleClickCount=" + toggleClickCount +
        ",visibleRowCount=" + visibleRowCount;
    }

/////////////////
// Accessibility support
////////////////

    /**
     * Gets the AccessibleContext associated with this JTree. 
     * For JTrees, the AccessibleContext takes the form of an 
     * AccessibleJTree. 
     * A new AccessibleJTree instance is created if necessary.
     *
     * @return an AccessibleJTree that serves as the 
     *         AccessibleContext of this JTree
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJTree();
        }
        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the 
     * <code>JTree</code> class.  It provides an implementation of the 
     * Java Accessibility API appropriate to tree user-interface elements.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    protected class AccessibleJTree extends AccessibleJComponent 
            implements AccessibleSelection, TreeSelectionListener, 
	               TreeModelListener, TreeExpansionListener  {

        TreePath   leadSelectionPath;
	Accessible leadSelectionAccessible;

        public AccessibleJTree() {
            // Add a tree model listener for JTree
            JTree.this.getModel().addTreeModelListener(this);
	    JTree.this.addTreeExpansionListener(this);      
	    JTree.this.addTreeSelectionListener(this);      
            leadSelectionPath = JTree.this.getLeadSelectionPath();
	    leadSelectionAccessible = (leadSelectionPath != null) 
		    ? new AccessibleJTreeNode(JTree.this,
		                              leadSelectionPath,
		                              JTree.this)
		    : null;
        }
 
        /**
         * Tree Selection Listener value change method. Used to fire the 
	 * property change
         *
         * @param e ListSelectionEvent
         *
         */
        public void valueChanged(TreeSelectionEvent e) {
	    TreePath oldLeadSelectionPath = e.getOldLeadSelectionPath();
            leadSelectionPath = e.getNewLeadSelectionPath();
	    if (oldLeadSelectionPath != leadSelectionPath) {
		Accessible oldLSA = leadSelectionAccessible;
		leadSelectionAccessible = (leadSelectionPath != null) 
			? new AccessibleJTreeNode(JTree.this,
						  leadSelectionPath,
		                  		  JTree.this)
			: null;
                firePropertyChange(AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY,
                                   oldLSA, leadSelectionAccessible);
	    }
            firePropertyChange(AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                               new Boolean(false), new Boolean(true));
	}

        /**
         * Fire a visible data property change notification.
         * A 'visible' data property is one that represents
         * something about the way the component appears on the
         * display, where that appearance isn't bound to any other
         * property. It notifies screen readers  that the visual 
         * appearance of the component has changed, so they can 
         * notify the user.
         */
        public void fireVisibleDataPropertyChange() {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              new Boolean(false), new Boolean(true));
        }
 
        // Fire the visible data changes for the model changes.
 
        /**
         * Tree Model Node change notification.
         *
         * @param e  a Tree Model event
         */
        public void treeNodesChanged(TreeModelEvent e) {
           fireVisibleDataPropertyChange();
        }
 
        /**
         * Tree Model Node change notification.
         *
         * @param e  a Tree node insertion event
         */
        public void treeNodesInserted(TreeModelEvent e) {
           fireVisibleDataPropertyChange();
        }
 
        /**
         * Tree Model Node change notification.
         *
         * @param e  a Tree node(s) removal event
         */
        public  void treeNodesRemoved(TreeModelEvent e) {
           fireVisibleDataPropertyChange();
        }
 
        /**
         * Tree Model structure change change notification.
         *
         * @param e  a Tree Model event
         */
        public  void treeStructureChanged(TreeModelEvent e) {
           fireVisibleDataPropertyChange();
        }
 
        /**
         * Tree Collapsed notification.
         *
         * @param e  a TreeExpansionEvent
         */
        public  void treeCollapsed(TreeExpansionEvent e) {
           fireVisibleDataPropertyChange();
           firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                              AccessibleState.EXPANDED,
                              AccessibleState.COLLAPSED);
        }
 
        /**
         * Tree Model Expansion notification.
         *
         * @param e  a Tree node insertion event
         */
        public  void treeExpanded(TreeExpansionEvent e) {
            fireVisibleDataPropertyChange();
            firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                               AccessibleState.COLLAPSED,
                               AccessibleState.EXPANDED);
         }

 
        private AccessibleContext getCurrentAccessibleContext() {
            Component c = getCurrentComponent();
            if (c instanceof Accessible) {
                return (((Accessible) c).getAccessibleContext());
            } else {
                return null;
            }
        }
 
        private Component getCurrentComponent() {
            // is the object visible?
            // if so, get row, selected, focus & leaf state, 
            // and then get the renderer component and return it
            TreeModel model = JTree.this.getModel();
            TreePath path = new TreePath(model.getRoot());
            if (JTree.this.isVisible(path)) {
                TreeCellRenderer r = JTree.this.getCellRenderer();
                TreeUI ui = JTree.this.getUI();
                if (ui != null) {
                    int row = ui.getRowForPath(JTree.this, path);
		    int lsr = JTree.this.getLeadSelectionRow();
                    boolean hasFocus = JTree.this.hasFocus()
				       && (lsr == row);
                    boolean selected = JTree.this.isPathSelected(path);
                    boolean expanded = JTree.this.isExpanded(path);

                    return r.getTreeCellRendererComponent(JTree.this, 
                        model.getRoot(), selected, expanded, 
                        model.isLeaf(model.getRoot()), row, hasFocus);
                }
            } 
            return null;
        }

        // Overridden methods from AccessibleJComponent

        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the 
         * object
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TREE;
        }

        /**
         * Returns the Accessible child, if one exists, contained at the local
         * coordinate Point.
         *
         * @param p point in local coordinates of this Accessible
         * @return the Accessible, if it exists, at the specified location;
         * else null
         */
        public Accessible getAccessibleAt(Point p) {
            TreePath path = getClosestPathForLocation(p.x, p.y);
            if (path != null) {
		// JTree.this is NOT the parent; parent will get computed later
                return new AccessibleJTreeNode(JTree.this, path, null);
            } else {
                return null;
            }
        }

        /**
         * Returns the number of top-level children nodes of this 
         * JTree.  Each of these nodes may in turn have children nodes.
         *
         * @return the number of accessible children nodes in the tree.
         */
        public int getAccessibleChildrenCount() {
	    TreeModel model = JTree.this.getModel();
	    if (model != null) {
		return 1;
	    } else {
		return 0;
	    }
        }

        /**
         * Return the nth Accessible child of the object.
         *
         * @param i zero-based index of child
         * @return the nth Accessible child of the object
         */
        public Accessible getAccessibleChild(int i) {
            TreeModel model = JTree.this.getModel();
            if (model != null) {
                if (i != 0) {
                    return null;
                } else {
                    Object[] objPath = {model.getRoot()};
                    TreePath path = new TreePath(objPath);
                    return new AccessibleJTreeNode(JTree.this, path, 
						   JTree.this);
                }
            }
            return null;
        }

        /**
         * Get the index of this object in its accessible parent. 
         *
         * @return the index of this object in its parent.  Since a JTree
         * top-level object does not have an accessible parent.
         * @see #getAccessibleParent
         */
        public int getAccessibleIndexInParent() {
	    // didn't ever need to override this...
            return super.getAccessibleIndexInParent();
	}

        // AccessibleSelection methods
	/**
         * Get the AccessibleSelection associated with this object.  In the
         * implementation of the Java Accessibility API for this class, 
	 * return this object, which is responsible for implementing the
         * AccessibleSelection interface on behalf of itself.
	 * 
	 * @return this object
	 */
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        /**
         * Returns the number of items currently selected.
         * If no items are selected, the return value will be 0.
         *
         * @return the number of items currently selected.
         */
        public int getAccessibleSelectionCount() {
            Object[] rootPath = new Object[1];
            rootPath[0] = treeModel.getRoot();
            TreePath childPath = new TreePath(rootPath);
            if (JTree.this.isPathSelected(childPath)) {
                return 1;
            } else {
                return 0;
            }
        }

        /**
         * Returns an Accessible representing the specified selected item
         * in the object.  If there isn't a selection, or there are 
         * fewer items selected than the integer passed in, the return
         * value will be null.
         *
         * @param i the zero-based index of selected items
         * @return an Accessible containing the selected item
         */
        public Accessible getAccessibleSelection(int i) {
            if (i == 0) {
                Object[] rootPath = new Object[1];
                rootPath[0] = treeModel.getRoot();
                TreePath childPath = new TreePath(rootPath);
                if (JTree.this.isPathSelected(childPath)) {
                    return new AccessibleJTreeNode(JTree.this, childPath, JTree.this);
                }
            }
            return null;
        }

        /**
         * Returns true if the current child of this object is selected.
         *
         * @param i the zero-based index of the child in this Accessible object.
         * @see AccessibleContext#getAccessibleChild
         */
        public boolean isAccessibleChildSelected(int i) {
            TreePath[] paths = JTree.this.getSelectionPaths();
            TreeModel treeModel = JTree.this.getModel();
            Object o;
            for (int j = 0; j < paths.length; j++) {
                o = paths[j].getLastPathComponent();
                if (i == treeModel.getIndexOfChild(treeModel.getRoot(), o)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Adds the specified selected item in the object to the object's
         * selection.  If the object supports multiple selections,
         * the specified item is added to any existing selection, otherwise
         * it replaces any existing selection in the object.  If the
         * specified item is already selected, this method has no effect.
         *
         * @param i the zero-based index of selectable items
         */
        public void addAccessibleSelection(int i) {
           TreeModel model = JTree.this.getModel();
           if (model != null) {
               if (i == 0) {
                   Object[] objPath = {model.getRoot()};
                   TreePath path = new TreePath(objPath);
                   JTree.this.addSelectionPath(path);
                }
            }
        }

        /**
         * Removes the specified selected item in the object from the object's
         * selection.  If the specified item isn't currently selected, this
         * method has no effect.
         *
         * @param i the zero-based index of selectable items
         */
        public void removeAccessibleSelection(int i) {
	    TreeModel model = JTree.this.getModel();
	    if (model != null) {
                if (i == 0) {
                    Object[] objPath = {model.getRoot()};
                    TreePath path = new TreePath(objPath);
                    JTree.this.removeSelectionPath(path);
                }
            }
        }

        /**
         * Clears the selection in the object, so that nothing in the
         * object is selected.
         */
        public void clearAccessibleSelection() {
            int childCount = getAccessibleChildrenCount();
            for (int i = 0; i < childCount; i++) {
                removeAccessibleSelection(i);
            }
        }

        /**
         * Causes every selected item in the object to be selected
         * if the object supports multiple selections.
         */
        public void selectAllAccessibleSelection() {
            TreeModel model = JTree.this.getModel();
            if (model != null) {
                Object[] objPath = {model.getRoot()};
                TreePath path = new TreePath(objPath);
                JTree.this.addSelectionPath(path);
            }
        }

        /**
         * This class implements accessibility support for the 
         * <code>JTree</code> child.  It provides an implementation of the 
         * Java Accessibility API appropriate to tree nodes.
         */
        protected class AccessibleJTreeNode extends AccessibleContext
            implements Accessible, AccessibleComponent, AccessibleSelection, 
            AccessibleAction {

            private JTree tree = null;
            private TreeModel treeModel = null;
            private Object obj = null;
            private TreePath path = null;
            private Accessible accessibleParent = null;
            private int index = 0;
            private boolean isLeaf = false;

            /**
             *  Constructs an AccessibleJTreeNode
             */
            public AccessibleJTreeNode(JTree t, TreePath p, Accessible ap) {
                tree = t;
                path = p;
                accessibleParent = ap;
                treeModel = t.getModel();
                obj = p.getLastPathComponent();
                if (treeModel != null) {
                    isLeaf = treeModel.isLeaf(obj);
                }
            }

            private TreePath getChildTreePath(int i) {
                // Tree nodes can't be so complex that they have
                // two sets of children -> we're ignoring that case
                if (i < 0 || i >= getAccessibleChildrenCount()) {
                    return null;
                } else {
                    Object childObj = treeModel.getChild(obj, i);
                    Object[] objPath = path.getPath();
                    Object[] objChildPath = new Object[objPath.length+1];
                    java.lang.System.arraycopy(objPath, 0, objChildPath, 0, objPath.length);
                    objChildPath[objChildPath.length-1] = childObj;
                    return new TreePath(objChildPath);
                }
            }

            /**
             * Get the AccessibleContext associated with this tree node. 
             * In the implementation of the Java Accessibility API for 
	     * this class, return this object, which is its own 
	     * AccessibleContext.
             *
             * @return this object
             */
            public AccessibleContext getAccessibleContext() {
                return this;
            }

            private AccessibleContext getCurrentAccessibleContext() {
                Component c = getCurrentComponent();
                if (c instanceof Accessible) {
                    return (((Accessible) c).getAccessibleContext());
                } else {
                    return null;
                }
            }

            private Component getCurrentComponent() {
                // is the object visible?
                // if so, get row, selected, focus & leaf state, 
                // and then get the renderer component and return it
                if (tree.isVisible(path)) {
                    TreeCellRenderer r = tree.getCellRenderer();
		    if (r == null) {
			return null;
		    }
                    TreeUI ui = tree.getUI();
                    if (ui != null) {
                        int row = ui.getRowForPath(JTree.this, path);
                        boolean selected = tree.isPathSelected(path);
                        boolean expanded = tree.isExpanded(path);
                        boolean hasFocus = false; // how to tell?? -PK
                        return r.getTreeCellRendererComponent(tree, obj, 
                            selected, expanded, isLeaf, row, hasFocus);
                    }
                } 
                return null;
            }

        // AccessibleContext methods
    
             /**
              * Get the accessible name of this object.
              *
              * @return the localized name of the object; null if this 
              * object does not have a name
              */
             public String getAccessibleName() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    String name = ac.getAccessibleName();
                    if ((name != null) && (name != "")) {
                        return ac.getAccessibleName();
                    } else {
                        return null;
                    }
                }
                if ((accessibleName != null) && (accessibleName != "")) {
                    return accessibleName;
                } else {
                    return null;
                }
            }
    
            /**
             * Set the localized accessible name of this object.
             *
             * @param s the new localized name of the object.
             */
            public void setAccessibleName(String s) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleName(s);
                } else {
                    super.setAccessibleName(s);
                }
            }
    
            //
            // *** should check tooltip text for desc. (needs MouseEvent)
            //
            /**
             * Get the accessible description of this object.
             *
             * @return the localized description of the object; null if 
             * this object does not have a description
             */
            public String getAccessibleDescription() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleDescription();
                } else {
                    return super.getAccessibleDescription();
                }
            }
    
            /**
             * Set the accessible description of this object.
             *
             * @param s the new localized description of the object
             */
            public void setAccessibleDescription(String s) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleDescription(s);
                } else {
                    super.setAccessibleDescription(s);
                }
            }
    
            /**
             * Get the role of this object.
             *
             * @return an instance of AccessibleRole describing the role of the object
             * @see AccessibleRole
             */
            public AccessibleRole getAccessibleRole() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleRole();
                } else {
                    return AccessibleRole.UNKNOWN;
                }
            }
    
            /**
             * Get the state set of this object.
             *
             * @return an instance of AccessibleStateSet containing the 
             * current state set of the object
             * @see AccessibleState
             */
            public AccessibleStateSet getAccessibleStateSet() {
                AccessibleContext ac = getCurrentAccessibleContext();
                AccessibleStateSet states;
		int row = tree.getUI().getRowForPath(tree,path);
		int lsr = tree.getLeadSelectionRow();
                if (ac != null) {
                    states = ac.getAccessibleStateSet();
                } else {
                    states = new AccessibleStateSet();
                }
                // need to test here, 'cause the underlying component 
                // is a cellRenderer, which is never showing...
                if (isShowing()) {
                    states.add(AccessibleState.SHOWING);
                } else if (states.contains(AccessibleState.SHOWING)) {
                    states.remove(AccessibleState.SHOWING);
                }
                if (isVisible()) {
                    states.add(AccessibleState.VISIBLE);
                } else if (states.contains(AccessibleState.VISIBLE)) {
                    states.remove(AccessibleState.VISIBLE);
                }
                if (tree.isPathSelected(path)){
                    states.add(AccessibleState.SELECTED);
                }
		if (lsr == row) {
                    states.add(AccessibleState.ACTIVE);
                }
                if (!isLeaf) {
                    states.add(AccessibleState.EXPANDABLE);
                }
                if (tree.isExpanded(path)) {
                    states.add(AccessibleState.EXPANDED);
                } else {
                    states.add(AccessibleState.COLLAPSED);
                }
                if (tree.isEditable()) {
                    states.add(AccessibleState.EDITABLE);
                }
                return states;
            }
    
            /**
             * Get the Accessible parent of this object.
             *
             * @return the Accessible parent of this object; null if this
             * object does not have an Accessible parent
             */
            public Accessible getAccessibleParent() {
		// someone wants to know, so we need to create our parent
		// if we don't have one (hey, we're a talented kid!)
		if (accessibleParent == null) {
		    Object[] objPath = path.getPath();
		    if (objPath.length > 1) {
			Object objParent = objPath[objPath.length-2];
			if (treeModel != null) {
			    index = treeModel.getIndexOfChild(objParent, obj);
			}
			Object[] objParentPath = new Object[objPath.length-1];
			java.lang.System.arraycopy(objPath, 0, objParentPath,
						   0, objPath.length-1);
			TreePath parentPath = new TreePath(objParentPath);
			accessibleParent = new AccessibleJTreeNode(tree, 
								   parentPath, 
								   null);
			this.setAccessibleParent(accessibleParent);
		    } else if (treeModel != null) {
			accessibleParent = tree; // we're the top!
			index = 0; // we're an only child!
			this.setAccessibleParent(accessibleParent);
		    }
		}
                return accessibleParent;
            }
    
            /**
             * Get the index of this object in its accessible parent. 
             *
             * @return the index of this object in its parent; -1 if this 
             * object does not have an accessible parent.
             * @see #getAccessibleParent
             */
            public int getAccessibleIndexInParent() {
		// index is invalid 'till we have an accessibleParent...
		if (accessibleParent == null) {
		    getAccessibleParent();
		}
		Object[] objPath = path.getPath();
		if (objPath.length > 1) {
		    Object objParent = objPath[objPath.length-2];
		    if (treeModel != null) {
			index = treeModel.getIndexOfChild(objParent, obj);
		    }
		}
                return index;
            }
    
            /**
             * Returns the number of accessible children in the object.
             *
             * @return the number of accessible children in the object.
             */
            public int getAccessibleChildrenCount() {
                // Tree nodes can't be so complex that they have 
                // two sets of children -> we're ignoring that case
                return treeModel.getChildCount(obj);
            }
    
            /**
             * Return the specified Accessible child of the object.
             *
             * @param i zero-based index of child
             * @return the Accessible child of the object
             */
            public Accessible getAccessibleChild(int i) {
                // Tree nodes can't be so complex that they have 
                // two sets of children -> we're ignoring that case
                if (i < 0 || i >= getAccessibleChildrenCount()) {
                    return null;
                } else {
                    Object childObj = treeModel.getChild(obj, i);
                    Object[] objPath = path.getPath();
                    Object[] objChildPath = new Object[objPath.length+1];
                    java.lang.System.arraycopy(objPath, 0, objChildPath, 0, objPath.length);
                    objChildPath[objChildPath.length-1] = childObj;
                    TreePath childPath = new TreePath(objChildPath);
                    return new AccessibleJTreeNode(JTree.this, childPath, this);
                }
            }
    
            /** 
             * Gets the locale of the component. If the component does not have
             * a locale, then the locale of its parent is returned.  
             *
             * @return This component's locale. If this component does not have 
	     * a locale, the locale of its parent is returned.
             * @exception IllegalComponentStateException 
             * If the Component does not have its own locale and has not yet 
	     * been added to a containment hierarchy such that the locale can be
             * determined from the containing parent. 
             * @see #setLocale
             */
            public Locale getLocale() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getLocale();
                } else {
                    return tree.getLocale();
                }
            }
    
            /**
             * Add a PropertyChangeListener to the listener list.
             * The listener is registered for all properties.
             *
             * @param listener  The PropertyChangeListener to be added
             */
            public void addPropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.addPropertyChangeListener(l);
                } else {
                    super.addPropertyChangeListener(l);
                }
            }
    
            /**
             * Remove a PropertyChangeListener from the listener list.
             * This removes a PropertyChangeListener that was registered
             * for all properties.
             *
             * @param listener  The PropertyChangeListener to be removed
             */
            public void removePropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.removePropertyChangeListener(l);
                } else {
                    super.removePropertyChangeListener(l);
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
             * Get the AccessibleComponent associated with this object.  In the
             * implementation of the Java Accessibility API for this class, 
             * return this object, which is responsible for implementing the
             * AccessibleComponent interface on behalf of itself.
             * 
             * @return this object
             */
            public AccessibleComponent getAccessibleComponent() {
                return this; // to override getBounds()
            }

            /**
             * Get the AccessibleSelection associated with this object if one
             * exists.  Otherwise return null.
             *
             * @return the AccessibleSelection, or null
             */
            public AccessibleSelection getAccessibleSelection() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null && isLeaf) {
                    return getCurrentAccessibleContext().getAccessibleSelection();
                } else {
                    return this;
                }
            }

            /**
             * Get the AccessibleText associated with this object if one
             * exists.  Otherwise return null.
             *
             * @return the AccessibleText, or null
             */
            public AccessibleText getAccessibleText() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return getCurrentAccessibleContext().getAccessibleText();
                } else {
                    return null;
                }
            }

            /**
             * Get the AccessibleValue associated with this object if one
             * exists.  Otherwise return null.
             *
             * @return the AccessibleValue, or null
             */
            public AccessibleValue getAccessibleValue() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return getCurrentAccessibleContext().getAccessibleValue();
                } else {
                    return null;
                }
            }


        // AccessibleComponent methods
    
            /**
             * Get the background color of this object.
             *
             * @return the background color, if supported, of the object; 
             * otherwise, null
             */
            public Color getBackground() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getBackground();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getBackground();
                    } else {
                        return null;
                    }
                }
            }
    
            /**
             * Set the background color of this object.
             *
             * @param c the new Color for the background
             */
            public void setBackground(Color c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setBackground(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setBackground(c);
                    }
                }
            }
    
        
            /**
             * Get the foreground color of this object.
             *
             * @return the foreground color, if supported, of the object; 
             * otherwise, null
             */
            public Color getForeground() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getForeground();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getForeground();
                    } else {
                        return null;
                    }
                }
            }
    
            public void setForeground(Color c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setForeground(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setForeground(c);
                    }
                }
            }
    
            public Cursor getCursor() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getCursor();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getCursor();
                    } else {
                        Accessible ap = getAccessibleParent();
                        if (ap instanceof AccessibleComponent) {
                            return ((AccessibleComponent) ap).getCursor();
                        } else {
                            return null;
                        }
                    }
                }
            }
    
            public void setCursor(Cursor c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setCursor(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setCursor(c);
                    }
                }
            }
    
            public Font getFont() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getFont();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getFont();
                    } else {
                        return null;
                    }
                }
            }
    
            public void setFont(Font f) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setFont(f);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setFont(f);
                    }
                }
            }
    
            public FontMetrics getFontMetrics(Font f) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getFontMetrics(f);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getFontMetrics(f);
                    } else {
                        return null;
                    }
                }
            }
    
            public boolean isEnabled() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).isEnabled();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isEnabled();
                    } else {
                        return false;
                    }
                }
            }
    
            public void setEnabled(boolean b) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setEnabled(b);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setEnabled(b);
                    }
                }
            }
    
            public boolean isVisible() {
                Rectangle pathBounds = tree.getPathBounds(path);
                Rectangle parentBounds = tree.getVisibleRect();
                if (pathBounds != null && parentBounds != null && 
                    parentBounds.intersects(pathBounds)) {
                    return true;
                } else {
                    return false;
                }
            }
    
            public void setVisible(boolean b) {
            }
    
            public boolean isShowing() {
                return (tree.isShowing() && isVisible());
            }
    
            public boolean contains(Point p) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    Rectangle r = ((AccessibleComponent) ac).getBounds();
                    return r.contains(p);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        Rectangle r = c.getBounds();
                        return r.contains(p);
                    } else {
                        return getBounds().contains(p);
                    }
                }
            }
    
            public Point getLocationOnScreen() {
                if (tree != null) {
                    Point parentLocation = tree.getLocationOnScreen();
                    Point componentLocation = getLocation();
                    componentLocation.translate(parentLocation.x, parentLocation.y);
                    return componentLocation;
                } else {
                    return null;
                }
            }
    
            protected Point getLocationInJTree() {
                Rectangle r = tree.getPathBounds(path);
                if (r != null) {
                    return r.getLocation();
                } else {
                    return null;
                }
            }

            public Point getLocation() {
                Rectangle r = getBounds();
                if (r != null) {
                    return r.getLocation();
                } else {
                    return null;
                }
            }
    
            public void setLocation(Point p) {
            }
                
            public Rectangle getBounds() {
                Rectangle r = tree.getPathBounds(path);
                Accessible parent = getAccessibleParent();
                if (parent != null) {
                    if (parent instanceof AccessibleJTreeNode) {
                        Point parentLoc = ((AccessibleJTreeNode) parent).getLocationInJTree();
                        if (parentLoc != null && r != null) {
                            r.translate(-parentLoc.x, -parentLoc.y);
                        } else {
                            return null;        // not visible!
                        }
                    } 
                }
                return r;
            }
    
            public void setBounds(Rectangle r) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setBounds(r);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setBounds(r);
                    }
                }
            }
    
            public Dimension getSize() {
                return getBounds().getSize();
            }
    
            public void setSize (Dimension d) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setSize(d);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setSize(d);
                    }
                }
            }
    
            public Accessible getAccessibleAt(Point p) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getAccessibleAt(p);
                } else {
                    return null;
                }
            }
    
            public boolean isFocusTraversable() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).isFocusTraversable();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isFocusTraversable();
                    } else {
                        return false;
                    }
                }
            }
    
            public void requestFocus() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).requestFocus();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.requestFocus();
                    }
                }
            }
    
            public void addFocusListener(FocusListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).addFocusListener(l);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.addFocusListener(l);
                    }
                }
            }
    
            public void removeFocusListener(FocusListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).removeFocusListener(l);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.removeFocusListener(l);
                    }
                }
            }

        // AccessibleSelection methods

            /**
             * Returns the number of items currently selected.
             * If no items are selected, the return value will be 0.
             *
             * @return the number of items currently selected.
             */
            public int getAccessibleSelectionCount() {
                int count = 0;
                int childCount = getAccessibleChildrenCount();
                for (int i = 0; i < childCount; i++) {
                    TreePath childPath = getChildTreePath(i);
                    if (tree.isPathSelected(childPath)) {
                       count++;
                    }
                } 
                return count;
            }

            /**
             * Returns an Accessible representing the specified selected item
             * in the object.  If there isn't a selection, or there are 
             * fewer items selected than the integer passed in, the return
             * value will be null.
             *
             * @param i the zero-based index of selected items
             * @return an Accessible containing the selected item
             */
            public Accessible getAccessibleSelection(int i) {
                int childCount = getAccessibleChildrenCount();
                if (i < 0 || i >= childCount) {
                    return null;        // out of range
                }
                int count = 0;
                for (int j = 0; j < childCount && i >= count; j++) {
                    TreePath childPath = getChildTreePath(j);
                    if (tree.isPathSelected(childPath)) { 
                        if (count == i) {
                            return new AccessibleJTreeNode(tree, childPath, this);
                        } else {
                            count++;
                        }
                    }
                }
                return null;
            }

            /**
             * Returns true if the current child of this object is selected.
             *
             * @param i the zero-based index of the child in this Accessible 
             * object.
             * @see AccessibleContext#getAccessibleChild
             */
            public boolean isAccessibleChildSelected(int i) {
                int childCount = getAccessibleChildrenCount();
                if (i < 0 || i >= childCount) {
                    return false;       // out of range
                } else {
                    TreePath childPath = getChildTreePath(i);
                    return tree.isPathSelected(childPath);
                }
            }

            /**
             * Adds the specified selected item in the object to the object's
             * selection.  If the object supports multiple selections,
             * the specified item is added to any existing selection, otherwise
             * it replaces any existing selection in the object.  If the
             * specified item is already selected, this method has no effect.
             *
             * @param i the zero-based index of selectable items
             */
            public void addAccessibleSelection(int i) {
               TreeModel model = JTree.this.getModel();
               if (model != null) {
                   if (i >= 0 && i < getAccessibleChildrenCount()) {
                       TreePath path = getChildTreePath(i);
                       JTree.this.addSelectionPath(path);
                    }
                }
            }

            /**
             * Removes the specified selected item in the object from the 
             * object's
             * selection.  If the specified item isn't currently selected, this
             * method has no effect.
             *
             * @param i the zero-based index of selectable items
             */
            public void removeAccessibleSelection(int i) {
               TreeModel model = JTree.this.getModel();
               if (model != null) {
                   if (i >= 0 && i < getAccessibleChildrenCount()) {
                       TreePath path = getChildTreePath(i);
                       JTree.this.removeSelectionPath(path);
                    }
                }
            }

            /**
             * Clears the selection in the object, so that nothing in the
             * object is selected.
             */
            public void clearAccessibleSelection() {
                int childCount = getAccessibleChildrenCount();
                for (int i = 0; i < childCount; i++) {
                    removeAccessibleSelection(i);
                }
            }

            /**
             * Causes every selected item in the object to be selected
             * if the object supports multiple selections.
             */
            public void selectAllAccessibleSelection() {
               TreeModel model = JTree.this.getModel();
               if (model != null) {
                   int childCount = getAccessibleChildrenCount();
                   TreePath path;
                   for (int i = 0; i < childCount; i++) {
                       path = getChildTreePath(i);
                       JTree.this.addSelectionPath(path);
                   }
                }
            }

        // AccessibleAction methods

            /**
             * Returns the number of accessible actions available in this 
             * tree node.  If this node is not a leaf, there is at least 
             * one action (toggle expand), in addition to any available
             * on the object behind the TreeCellRenderer.
             *
             * @return the number of Actions in this object
             */
            public int getAccessibleActionCount() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    AccessibleAction aa = ac.getAccessibleAction();
                    if (aa != null) {
                        return (aa.getAccessibleActionCount() + (isLeaf ? 0 : 1));
                    }
                }
                return isLeaf ? 0 : 1;
            }

            /**
             * Return a description of the specified action of the tree node.
             * If this node is not a leaf, there is at least one action
             * description (toggle expand), in addition to any available
             * on the object behind the TreeCellRenderer.
             *
             * @param i zero-based index of the actions
             * @return a description of the action
             */
            public String getAccessibleActionDescription(int i) {
                if (i < 0 || i >= getAccessibleActionCount()) {
                    return null;
                }
                AccessibleContext ac = getCurrentAccessibleContext();
                if (i == 0) {
                    return "toggle expand";
                } else if (ac != null) {
                    AccessibleAction aa = ac.getAccessibleAction();
                    if (aa != null) {
                        return aa.getAccessibleActionDescription(i - 1);
                    }
                }
                return null;
            }

            /**
             * Perform the specified Action on the tree node.  If this node
             * is not a leaf, there is at least one action which can be
             * done (toggle expand), in addition to any available on the 
             * object behind the TreeCellRenderer.
             *
             * @param i zero-based index of actions
             * @return true if the the action was performed; else false.
             */
            public boolean doAccessibleAction(int i) {
                if (i < 0 || i >= getAccessibleActionCount()) {
                    return false;
                }
                AccessibleContext ac = getCurrentAccessibleContext();
                if (i == 0) {
                    if (JTree.this.isExpanded(path)) {
                        JTree.this.collapsePath(path);
                    } else {
                        JTree.this.expandPath(path);
                    }
                    return true;
                } else if (ac != null) {
                    AccessibleAction aa = ac.getAccessibleAction();
                    if (aa != null) {
                        return aa.doAccessibleAction(i - 1);
                    }
                }
                return false;
            }

        } // inner class AccessibleJTreeNode

    }  // inner class AccessibleJTree

} // End of class JTree

