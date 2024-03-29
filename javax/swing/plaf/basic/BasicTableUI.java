/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.plaf.basic;

import javax.swing.table.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.awt.event.*;
import java.awt.*;
import javax.swing.plaf.*;
import java.util.EventObject;

import javax.swing.text.*;

/**
 * BasicTableUI implementation
 *
 * @version 1.103 02/06/02
 * @author Philip Milne
 */
public class BasicTableUI extends TableUI
{

//
// Instance Variables
//

    // The JTable that is delegating the painting to this UI.
    protected JTable table;
    protected CellRendererPane rendererPane;

    // Listeners that are attached to the JTable
    protected KeyListener keyListener;
    protected FocusListener focusListener;
    protected MouseInputListener mouseInputListener; 
	
//
//  Helper class for keyboard actions
//

    private static class NavigationalAction extends AbstractAction {
        protected int dx;
        protected int dy;
	protected boolean toggle; 
	protected boolean extend; 
	protected boolean inSelection; 

	protected int anchorRow; 
	protected int anchorColumn; 
	protected int leadRow; 
	protected int leadColumn; 

        protected NavigationalAction(int dx, int dy, boolean toggle, boolean extend, 
				  boolean inSelection) {
            this.dx = dx;
            this.dy = dy; 
	    this.toggle = toggle; 
	    this.extend = extend; 
	    this.inSelection = inSelection; 
        }

	private int clipToRange(int i, int a, int b) { 
	    return Math.min(Math.max(i, a), b-1);
	}

	private void moveWithinTableRange(JTable table, int dx, int dy, boolean changeLead) { 
	    if (changeLead) { 
		leadRow = clipToRange(leadRow+dy, 0, table.getRowCount()); 
		leadColumn = clipToRange(leadColumn+dx, 0, table.getColumnCount()); 
	    }
	    else { 
		anchorRow = clipToRange(anchorRow+dy, 0, table.getRowCount()); 
		anchorColumn = clipToRange(anchorColumn+dx, 0, table.getColumnCount()); 
	    }
	}

	private int selectionSpan(ListSelectionModel sm) { 
	    return sm.getMaxSelectionIndex() - sm.getMinSelectionIndex() + 1;
	}

	private int compare(int i, ListSelectionModel sm) { 
	    return compare(i, sm.getMinSelectionIndex(), sm.getMaxSelectionIndex()+1); 
	}

	private int compare(int i, int a, int b) { 
	    return (i < a) ? -1 : (i >= b) ? 1 : 0 ; 
	}

	private boolean moveWithinSelectedRange(JTable table, int dx, int dy, boolean ignoreCarry) {
	    ListSelectionModel rsm = table.getSelectionModel(); 
	    ListSelectionModel csm = table.getColumnModel().getSelectionModel(); 

	    int newAnchorRow =    anchorRow + dy;  
	    int newAnchorColumn = anchorColumn + dx; 

	    int rowSgn; 
	    int colSgn; 
	    int rowCount = selectionSpan(rsm); 
	    int columnCount = selectionSpan(csm); 

	    boolean canStayInSelection = (rowCount * columnCount > 1); 
	    if (canStayInSelection) { 
		rowSgn = compare(newAnchorRow, rsm); 
		colSgn = compare(newAnchorColumn, csm); 
	    }
	    else { 
		// If there is only one selected cell, there is no point 
		// in trying to stay within the selected area. Move outside 
		// the selection, wrapping at the table boundaries. 
		rowCount = table.getRowCount(); 
		columnCount = table.getColumnCount(); 
		rowSgn = compare(newAnchorRow, 0, rowCount); 
		colSgn = compare(newAnchorColumn, 0, columnCount); 

	    }

	    anchorRow    = newAnchorRow - rowCount * rowSgn;  
	    anchorColumn = newAnchorColumn - columnCount * colSgn; 

	    if (!ignoreCarry) {
		return moveWithinSelectedRange(table, rowSgn, colSgn, true); 
	    }
	    return canStayInSelection; 
	}

        public void actionPerformed(ActionEvent e) { 
            JTable table = (JTable)e.getSource(); 
            ListSelectionModel rsm = table.getSelectionModel(); 
	    anchorRow =    rsm.getAnchorSelectionIndex(); 
            leadRow =      rsm.getLeadSelectionIndex(); 

	    ListSelectionModel csm = table.getColumnModel().getSelectionModel(); 
	    anchorColumn = csm.getAnchorSelectionIndex(); 
            leadColumn =   csm.getLeadSelectionIndex(); 

	    int oldAnchorRow = anchorRow; 
	    int oldAnchorColumn = anchorColumn; 

            if (table.isEditing() && !table.getCellEditor().stopCellEditing()) { 
		return; 
            }

            if (!inSelection) { 
		moveWithinTableRange(table, dx, dy, extend); 
		if (!extend) {
		    table.changeSelection(anchorRow, anchorColumn, false, extend);
		}
		else {
		    table.changeSelection(leadRow, leadColumn, false, extend);
		}
            }
	    else {
		if (moveWithinSelectedRange(table, dx, dy, false)) { 
		    table.changeSelection(anchorRow, anchorColumn, true, true); 
		} 
		else {
		    table.changeSelection(anchorRow, anchorColumn, false, false); 
		}
	    }
        }
    }

    private static class PagingAction extends NavigationalAction { 

	private boolean forwards; 
	private boolean vertically; 
	private boolean toLimit; 

        private PagingAction(boolean extend, boolean forwards, 
			     boolean vertically, boolean toLimit) { 
            super(0, 0, false, extend, false); 
	    this.forwards = forwards; 
	    this.vertically = vertically; 
	    this.toLimit = toLimit; 
	}

        public void actionPerformed(ActionEvent e) { 
            JTable table = (JTable)e.getSource(); 
	    if (toLimit) { 
		if (vertically) { 
		    int rowCount = table.getRowCount(); 
		    this.dx = 0; 
		    this.dy = forwards ? rowCount : -rowCount; 
		}
		else { 
		    int colCount = table.getColumnCount(); 
		    this.dx = forwards ? colCount : -colCount; 
		    this.dy = 0; 
		}
	    }
	    else { 
		if (!(table.getParent().getParent() instanceof JScrollPane)) {
		    return; 
		}

		Dimension delta = table.getParent().getSize(); 
		ListSelectionModel sm = (vertically) 
		    ? table.getSelectionModel() 
		    : table.getColumnModel().getSelectionModel(); 		

		int start = (extend) ? sm.getLeadSelectionIndex() 
                                     : sm.getAnchorSelectionIndex(); 

		if (vertically) { 
		    Rectangle r = table.getCellRect(start, 0, true); 
		    r.y += forwards ? delta.height : -delta.height; 
		    this.dx = 0; 
		    int newRow = table.rowAtPoint(r.getLocation()); 
		    if (newRow == -1 && forwards) { 
			newRow = table.getRowCount(); 
		    }
		    this.dy = newRow - start; 
		}
		else {
		    Rectangle r = table.getCellRect(0, start, true); 	
		    r.x += forwards ? delta.width : -delta.width;
		    int newColumn = table.columnAtPoint(r.getLocation()); 
		    if (newColumn == -1 && forwards) { 
			newColumn = table.getColumnCount(); 
		    }
		    this.dx = newColumn - start; 
		    this.dy = 0; 
		}
	    }
	    super.actionPerformed(e); 
        }
    }


    /**
     * Action to invoke <code>selectAll</code> on the table.
     */
    private static class SelectAllAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) { 
	    JTable table = (JTable)e.getSource();
	    table.selectAll(); 
	}
    }


    /**
     * Action to invoke <code>removeEditor</code> on the table.
     */
    private static class CancelEditingAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) { 
	    JTable table = (JTable)e.getSource();
	    table.removeEditor(); 
	}
    }


    /**
     * Action to start editing, and pass focus to the editor.
     */
    private static class StartEditingAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) { 
	    JTable table = (JTable)e.getSource(); 
	    if (!table.hasFocus()) { 
		CellEditor cellEditor = table.getCellEditor(); 
		if (cellEditor != null && !cellEditor.stopCellEditing()) { 
		    return;
		}
		table.requestFocus(); 
		return; 
	    }
	    ListSelectionModel rsm = table.getSelectionModel(); 
	    int anchorRow =    rsm.getAnchorSelectionIndex(); 
	    ListSelectionModel csm = table.getColumnModel().getSelectionModel(); 
	    int anchorColumn = csm.getAnchorSelectionIndex(); 
	    table.editCellAt(anchorRow, anchorColumn); 
	    Component editorComp = table.getEditorComponent(); 
	    if (editorComp != null) { 
		editorComp.requestFocus(); 
	    }
	}
    }

//
//  The Table's Key listener
//

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTableUI.
     * <p>As of Java 2 platform v1.3 this class is no longer used.
     * Instead <code>JTable</code>
     * overrides <code>processKeyBinding</code> to dispatch the event to
     * the current <code>TableCellEditor</code>.
     */
     public class KeyHandler implements KeyListener {
        public void keyPressed(KeyEvent e) { }

        public void keyReleased(KeyEvent e) { }

        public void keyTyped(KeyEvent e) { 
            KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getKeyChar(), e.getModifiers());

            // We register all actions using ANCESTOR_OF_FOCUSED_COMPONENT 
            // which means that we might perform the appropriate action 
            // in the table and then forward it to the editor if the editor
            // had focus. Make sure this doesn't happen by checking our 
            // InputMaps.
	    InputMap map = table.getInputMap(JComponent.WHEN_FOCUSED);
	    if (map != null && map.get(keyStroke) != null) {
		return;
	    }
	    map = table.getInputMap(JComponent.
				  WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    if (map != null && map.get(keyStroke) != null) {
		return;
	    }
	    
	    keyStroke = KeyStroke.getKeyStrokeForEvent(e);

            // The AWT seems to generate an unconsumed \r event when
            // ENTER (\n) is pressed. 
            if (e.getKeyChar() == '\r') {
                return; 
            }

            int anchorRow = table.getSelectionModel().getAnchorSelectionIndex();
            int anchorColumn = 
		table.getColumnModel().getSelectionModel().getAnchorSelectionIndex();
            if (anchorRow != -1 && anchorColumn != -1 && !table.isEditing()) {
                if (!table.editCellAt(anchorRow, anchorColumn)) {
                    return;
                }
            }

            // Forwarding events this way seems to put the component 
            // in a state where it believes it has focus. In reality 
            // the table retains focus - though it is difficult for 
            // a user to tell, since the caret is visible and flashing. 
        	
            // Calling table.requestFocus() here, to get the focus back to 
            // the table, seems to have no effect. 
        	
            Component editorComp = table.getEditorComponent();
            if (table.isEditing() && editorComp != null) {
                if (editorComp instanceof JComponent) {
                    JComponent component = (JComponent)editorComp;
		    map = component.getInputMap(JComponent.WHEN_FOCUSED);
		    Object binding = (map != null) ? map.get(keyStroke) : null;
		    if (binding == null) {
			map = component.getInputMap(JComponent.
					 WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			binding = (map != null) ? map.get(keyStroke) : null;
		    }
		    if (binding != null) {
			ActionMap am = component.getActionMap();
			Action action = (am != null) ? am.get(binding) : null;
			if (action != null && SwingUtilities.
			    notifyAction(action, keyStroke, e, component,
					 e.getModifiers())) {
			    e.consume();
			}
		    }
                }
            }
        }
    }

//
//  The Table's focus listener
//

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTableUI.
     */
    public class FocusHandler implements FocusListener {

        private void repaintAnchorCell( ) { 
	    int rc = table.getRowCount(); 
	    int cc = table.getColumnCount(); 
            int ar = table.getSelectionModel().getAnchorSelectionIndex();
            int ac = table.getColumnModel().getSelectionModel().getAnchorSelectionIndex(); 
	    if (ar < 0 || ar >= rc || ac < 0 || ac >= cc) { 
		return; 
	    }

            Rectangle dirtyRect = table.getCellRect(ar, ac, false);
            table.repaint(dirtyRect);
        }

        public void focusGained(FocusEvent e) { 
            repaintAnchorCell();
        }

        public void focusLost(FocusEvent e) {
            repaintAnchorCell();
        }
    }

//
//  The Table's mouse and mouse motion listeners
//

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTableUI.
     */
    public class MouseInputHandler implements MouseInputListener {

        // Component recieving mouse events during editing. May not be editorComponent.
        private Component dispatchComponent;

//  The Table's mouse listener methods.

        public void mouseClicked(MouseEvent e) {}

        private void setDispatchComponent(MouseEvent e) { 
            Component editorComponent = table.getEditorComponent();
            Point p = e.getPoint();
            Point p2 = SwingUtilities.convertPoint(table, p, editorComponent);
            dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent, 
                                                                 p2.x, p2.y);
        }

        private boolean repostEvent(MouseEvent e) { 
            if (dispatchComponent == null) {
                return false; 
            }
            MouseEvent e2 = SwingUtilities.convertMouseEvent(table, e, dispatchComponent);
            dispatchComponent.dispatchEvent(e2); 
            return true; 
        }

        private void setValueIsAdjusting(boolean flag) {
            table.getSelectionModel().setValueIsAdjusting(flag); 
            table.getColumnModel().getSelectionModel().setValueIsAdjusting(flag); 
        }

	private boolean shouldIgnore(MouseEvent e) { 
	    return !(SwingUtilities.isLeftMouseButton(e) && table.isEnabled()); 
	}

        public void mousePressed(MouseEvent e) {
	    if (shouldIgnore(e)) {
	        return;
	    }

            Point p = e.getPoint();
            int row = table.rowAtPoint(p);
            int column = table.columnAtPoint(p);
	    // The autoscroller can generate drag events outside the Table's range. 
            if ((column == -1) || (row == -1)) {
                return;
            }

            if (table.editCellAt(row, column, e)) {
                setDispatchComponent(e); 
                repostEvent(e); 
            } 
	    else { 
		table.requestFocus();
	    }
        	
            CellEditor editor = table.getCellEditor(); 
            if (editor == null || editor.shouldSelectCell(e)) { 
                setValueIsAdjusting(true);
                table.changeSelection(row, column, e.isControlDown(), e.isShiftDown());  
	    }
        }

        public void mouseReleased(MouseEvent e) {
	    if (shouldIgnore(e)) {
	        return;
	    }

	    repostEvent(e); 
	    dispatchComponent = null;
	    setValueIsAdjusting(false);
        }


        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}

//  The Table's mouse motion listener methods.

        public void mouseMoved(MouseEvent e) {}

        public void mouseDragged(MouseEvent e) {
	    if (shouldIgnore(e)) {
	        return;
	    }

            repostEvent(e); 
        	
            CellEditor editor = table.getCellEditor();         
            if (editor == null || editor.shouldSelectCell(e)) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                int column = table.columnAtPoint(p);
	        // The autoscroller can generate drag events outside the Table's range. 
                if ((column == -1) || (row == -1)) {
                    return;
                }
	        table.changeSelection(row, column, false, true); 
            }
        }
    }

//
//  Factory methods for the Listeners
//

    /**
     * Creates the key listener for handling keyboard navigation in the JTable.
     */
    protected KeyListener createKeyListener() {
	return null;
    }

    /**
     * Creates the focus listener for handling keyboard navigation in the JTable.
     */
    protected FocusListener createFocusListener() {
        return new FocusHandler();
    }

    /**
     * Creates the mouse listener for the JTable.
     */
    protected MouseInputListener createMouseInputListener() {
        return new MouseInputHandler();
    }

//
//  The installation/uninstall procedures and support
//

    public static ComponentUI createUI(JComponent c) {
        return new BasicTableUI();
    }

//  Installation

    public void installUI(JComponent c) {
        table = (JTable)c;

        rendererPane = new CellRendererPane();
        table.add(rendererPane);

        installDefaults();
        installListeners();
        installKeyboardActions();
    }

    /**
     * Initialize JTable properties, e.g. font, foreground, and background.
     * The font, foreground, and background properties are only set if their
     * current value is either null or a UIResource, other properties are set
     * if the current value is null.
     *
     * @see #installUI
     */
    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(table, "Table.background",
                                         "Table.foreground", "Table.font");

        Color sbg = table.getSelectionBackground();
        if (sbg == null || sbg instanceof UIResource) {
            table.setSelectionBackground(UIManager.getColor("Table.selectionBackground"));
        }

        Color sfg = table.getSelectionForeground();
        if (sfg == null || sfg instanceof UIResource) {
            table.setSelectionForeground(UIManager.getColor("Table.selectionForeground"));
        }

        Color gridColor = table.getGridColor();
        if (gridColor == null || gridColor instanceof UIResource) {
            table.setGridColor(UIManager.getColor("Table.gridColor"));
        }

        // install the scrollpane border
        Container parent = table.getParent();  // should be viewport
        if (parent != null) {
            parent = parent.getParent();  // should be the scrollpane
            if (parent != null && parent instanceof JScrollPane) {
                LookAndFeel.installBorder((JScrollPane)parent, "Table.scrollPaneBorder");
            }
        }
    }

    /**
     * Attaches listeners to the JTable.
     */
    protected void installListeners() {
        focusListener = createFocusListener();
        keyListener = createKeyListener();
        mouseInputListener = createMouseInputListener();

        table.addFocusListener(focusListener);
        table.addKeyListener(keyListener);
        table.addMouseListener(mouseInputListener);
        table.addMouseMotionListener(mouseInputListener);
    }

    /**
     * Register all keyboard actions on the JTable.
     */
    protected void installKeyboardActions() {
	ActionMap map = getActionMap();

	SwingUtilities.replaceUIActionMap(table, map);
	InputMap inputMap = getInputMap(JComponent.
				  WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	SwingUtilities.replaceUIInputMap(table, JComponent.
				       WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
				       inputMap);
    }

    InputMap getInputMap(int condition) {
	if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
	    return (InputMap)UIManager.get("Table.ancestorInputMap");
	}
	return null;
    }

    ActionMap getActionMap() {
	ActionMap map = (ActionMap)UIManager.get("Table.actionMap");

	if (map == null) {
	    map = createActionMap();
	    if (map != null) {
		UIManager.put("Table.actionMap", map);
	    }
	}
	return map;
    }

    ActionMap createActionMap() {
	ActionMap map = new ActionMapUIResource();

	map.put("selectNextColumn", new NavigationalAction
		(1, 0, false, false, false));
	map.put("selectPreviousColumn", new NavigationalAction
		(-1, 0, false, false, false));
	map.put("selectNextRow", new NavigationalAction
		(0, 1, false, false, false));
	map.put("selectPreviousRow", new NavigationalAction
		(0, -1, false, false, false));

	map.put("selectNextColumnExtendSelection", new NavigationalAction
		(1, 0, false, true, false));
	map.put("selectPreviousColumnExtendSelection", new NavigationalAction
		(-1, 0, false, true, false));
	map.put("selectNextRowExtendSelection", new NavigationalAction
		(0, 1, false, true, false));
	map.put("selectPreviousRowExtendSelection", new NavigationalAction
		(0, -1, false, true, false));

	map.put("scrollUpChangeSelection",
		new PagingAction(false, false, true, false));
	map.put("scrollDownChangeSelection",
		new PagingAction(false, true, true, false));
	map.put("selectFirstColumn",
		new PagingAction(false, false, false, true));
	map.put("selectLastColumn",
		new PagingAction(false, true, false, false));

	map.put("scrollUpExtendSelection",
		new PagingAction(true, false, true, false));
	map.put("scrollDownExtendSelection",
		new PagingAction(true, true, true, false));
	map.put("selectFirstColumnExtendSelection",
		new PagingAction(true, false, false, true));
	map.put("selectLastColumnExtendSelection",
		new PagingAction(true, true, false, false));

	map.put("scrollLeftChangeSelection",
		new PagingAction(false, false, false, false));
	map.put("scrollRightChangeSelection",
		new PagingAction(false, true, false, false));
	map.put("selectFirstRow",
		new PagingAction(false, false, true, true));
	map.put("selectLastRow",
		new PagingAction(false, true, true, true));

	map.put("scrollRightExtendSelection",
		new PagingAction(true, false, false, false));
	map.put("scrollLeftExtendSelection",
		new PagingAction(true, true, false, false));
	map.put("selectFirstRowExtendSelection",
		new PagingAction(true, false, true, true));
	map.put("selectLastRowExtendSelection",
		new PagingAction(true, true, true, true));

	map.put("selectNextColumnCell",
		new NavigationalAction(1, 0, true, false, true));
	map.put("selectPreviousColumnCell",
		new NavigationalAction(-1, 0, true, false, true));
	map.put("selectNextRowCell",
		new NavigationalAction(0, 1, true, false, true));
	map.put("selectPreviousRowCell",
		new NavigationalAction(0, -1, true, false, true));

	map.put("selectAll", new SelectAllAction());
	map.put("cancel", new CancelEditingAction());
	map.put("startEditing", new StartEditingAction());
	return map;
    }

//  Uninstallation

    public void uninstallUI(JComponent c) {
        uninstallDefaults();
        uninstallListeners();
        uninstallKeyboardActions();

        table.remove(rendererPane);
        rendererPane = null;
        table = null;
    }

    protected void uninstallDefaults() {}

    protected void uninstallListeners() {
        table.removeFocusListener(focusListener);
        table.removeKeyListener(keyListener);
        table.removeMouseListener(mouseInputListener);
        table.removeMouseMotionListener(mouseInputListener);

        focusListener = null;
        keyListener = null;
        mouseInputListener = null;
    }

    protected void uninstallKeyboardActions() {
	SwingUtilities.replaceUIInputMap(table, JComponent.
				   WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
	SwingUtilities.replaceUIActionMap(table, null);
    }

//
// Size Methods
//

    private Dimension createTableSize(long width) { 
	int height = 0; 
	int rowCount = table.getRowCount(); 
	if (rowCount > 0 && table.getColumnCount() > 0) { 
	    Rectangle r = table.getCellRect(rowCount-1, 0, true); 
	    height = r.y + r.height; 
	} 
	// Width is always positive. The call to abs() is a workaround for 
	// a bug in the 1.1.6 JIT on Windows. 
	long tmp = Math.abs(width);
        if (tmp > Integer.MAX_VALUE) {
            tmp = Integer.MAX_VALUE;
        }
	return new Dimension((int)tmp, height);
    }

    /**
     * Return the minimum size of the table. The minimum height is the 
     * row height times the number of rows. 
     * The minimum width is the sum of the minimum widths of each column.
     */
    public Dimension getMinimumSize(JComponent c) {
        long width = 0;
        Enumeration enumeration = table.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getMinWidth();
        }
        return createTableSize(width);
    }

    /**
     * Return the preferred size of the table. The preferred height is the 
     * row height times the number of rows. 
     * The preferred width is the sum of the preferred widths of each column.
     */
    public Dimension getPreferredSize(JComponent c) {
        long width = 0;
        Enumeration enumeration = table.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getPreferredWidth();
        }
        return createTableSize(width);
    }

    /**
     * Return the maximum size of the table. The maximum height is the 
     * row heighttimes the number of rows. 
     * The maximum width is the sum of the maximum widths of each column.
     */
    public Dimension getMaximumSize(JComponent c) {
        long width = 0;
        Enumeration enumeration = table.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getMaxWidth();
        }
        return createTableSize(width);
    }

//
//  Paint methods and support
//

    /** Paint a representation of the <code>table</code> instance
     * that was set in installUI().
     */
    public void paint(Graphics g, JComponent c) { 
	if (table.getRowCount() <= 0 || table.getColumnCount() <= 0) { 
	    return; 
	}
	Rectangle clip = g.getClipBounds(); 
	Point minLocation = clip.getLocation(); 
	Point maxLocation = new Point(clip.x + clip.width - 1, clip.y + clip.height - 1); 
        int rMin = table.rowAtPoint(minLocation);
        int rMax = table.rowAtPoint(maxLocation);
        // This should never happen.
        if (rMin == -1) {
	    rMin = 0;
        }
        // If the table does not have enough rows to fill the view we'll get -1.
        // Replace this with the index of the last row.
        if (rMax == -1) {
	    rMax = table.getRowCount()-1;
        }
	int cMin = table.columnAtPoint(minLocation); 
        int cMax = table.columnAtPoint(maxLocation);
        // This should never happen.
        if (cMin == -1) {
	    cMin = 0;
        }        
	// If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
	    cMax = table.getColumnCount()-1;
        }

        // Paint the grid.
        paintGrid(g, rMin, rMax, cMin, cMax);

        // Paint the cells. 
	paintCells(g, rMin, rMax, cMin, cMax);
    }

    /*
     * Paints the grid lines within <I>aRect</I>, using the grid
     * color set with <I>setGridColor</I>. Paints vertical lines
     * if <code>getShowVerticalLines()</code> returns true and paints
     * horizontal lines if <code>getShowHorizontalLines()</code>
     * returns true.
     */
    private void paintGrid(Graphics g, int rMin, int rMax, int cMin, int cMax) {
        g.setColor(table.getGridColor());

	Rectangle minCell = table.getCellRect(rMin, cMin, true); 
	Rectangle maxCell = table.getCellRect(rMax, cMax, true); 

        if (table.getShowHorizontalLines()) {
	    int tableWidth = maxCell.x + maxCell.width;
	    int y = minCell.y; 
	    for (int row = rMin; row <= rMax; row++) { 
		y += table.getRowHeight(row);
		g.drawLine(0, y - 1, tableWidth - 1, y - 1);
	    }     
	}
        if (table.getShowVerticalLines()) {
	    TableColumnModel cm = table.getColumnModel(); 
	    int tableHeight = maxCell.y + maxCell.height; 
	    int x = minCell.x;
	    for (int column = cMin; column <= cMax ; column++) {
		x += cm.getColumn(column).getWidth(); 
		g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
	    }
        }
    }

    private int viewIndexForColumn(TableColumn aColumn) {
        TableColumnModel cm = table.getColumnModel();
        for (int column = 0; column < cm.getColumnCount(); column++) {
            if (cm.getColumn(column) == aColumn) {
                return column;
            }
        }
        return -1;
    }

    private void paintCells(Graphics g, int rMin, int rMax, int cMin, int cMax) {
	JTableHeader header = table.getTableHeader(); 
	TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn(); 

	TableColumnModel cm = table.getColumnModel(); 
	int columnMargin = cm.getColumnMargin(); 

	for(int row = rMin; row <= rMax; row++) { 
	    Rectangle cellRect = table.getCellRect(row, cMin, false); 
	    for(int column = cMin; column <= cMax ; column++) { 
		TableColumn aColumn = cm.getColumn(column); 
		int columnWidth = aColumn.getWidth(); 
		cellRect.width = columnWidth - columnMargin;
		if (aColumn != draggedColumn) {
		    paintCell(g, cellRect, row, column);
		} 
		cellRect.x += columnWidth; 
	    } 
	}

        // Paint the dragged column if we are dragging. 
        if (draggedColumn != null) { 
	    paintDraggedArea(g, rMin, rMax, draggedColumn, header.getDraggedDistance()); 
	}

	// Remove any renderers that may be left in the rendererPane. 
	rendererPane.removeAll(); 
    }

    private void paintDraggedArea(Graphics g, int rMin, int rMax, TableColumn draggedColumn, int distance) {
        int draggedColumnIndex = viewIndexForColumn(draggedColumn); 
        
        Rectangle minCell = table.getCellRect(rMin, draggedColumnIndex, true); 
	Rectangle maxCell = table.getCellRect(rMax, draggedColumnIndex, true); 
	    
	Rectangle vacatedColumnRect = minCell.union(maxCell); 

	// Paint a gray well in place of the moving column. 
	g.setColor(table.getParent().getBackground());
	g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y, 
		   vacatedColumnRect.width, vacatedColumnRect.height);	    

	// Move to the where the cell has been dragged. 
	vacatedColumnRect.x += distance;

	// Fill the background. 
	g.setColor(table.getBackground());
	g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
		   vacatedColumnRect.width, vacatedColumnRect.height);
 
	// Paint the vertical grid lines if necessary.
	if (table.getShowVerticalLines()) {
	    g.setColor(table.getGridColor());
	    int x1 = vacatedColumnRect.x;
	    int y1 = vacatedColumnRect.y;
	    int x2 = x1 + vacatedColumnRect.width - 1;
	    int y2 = y1 + vacatedColumnRect.height - 1;
	    // Left
	    g.drawLine(x1-1, y1, x1-1, y2);
	    // Right
	    g.drawLine(x2, y1, x2, y2);
	}

	for(int row = rMin; row <= rMax; row++) { 
	    // Render the cell value
	    Rectangle r = table.getCellRect(row, draggedColumnIndex, false); 
	    r.x += distance;
	    paintCell(g, r, row, draggedColumnIndex);
 
	    // Paint the (lower) horizontal grid line if necessary.
	    if (table.getShowHorizontalLines()) {
		g.setColor(table.getGridColor());
		Rectangle rcr = table.getCellRect(row, draggedColumnIndex, true); 
		rcr.x += distance;
		int x1 = rcr.x;
		int y1 = rcr.y;
		int x2 = x1 + rcr.width - 1;
		int y2 = y1 + rcr.height - 1;
		g.drawLine(x1, y2, x2, y2);
	    }
	}
    }

    private void paintCell(Graphics g, Rectangle cellRect, int row, int column) {
        if (table.isEditing() && table.getEditingRow()==row &&
                                 table.getEditingColumn()==column) {
            Component component = table.getEditorComponent();
	    component.setBounds(cellRect);
            component.validate();
        }
        else {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component component = table.prepareRenderer(renderer, row, column);
            rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y,
                                        cellRect.width, cellRect.height, true);
        }
    }
}  // End of Class BasicTableUI
        
