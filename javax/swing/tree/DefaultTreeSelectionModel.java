/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.tree;

import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.event.*;
import javax.swing.DefaultListSelectionModel;

/**
 * Default implementation of TreeSelectionModel.  Listeners are notified
 * whenever
 * the paths in the selection change, not the rows. In order
 * to be able to track row changes you may wish to become a listener 
 * for expansion events on the tree and test for changes from there.
 * <p>resetRowSelection is called from any of the methods that update
 * the selected paths. If you subclass any of these methods to
 * filter what is allowed to be selected, be sure and message
 * <code>resetRowSelection</code> if you do not message super.
 * 
 * <p>
 * 
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @see javax.swing.JTree
 *
 * @version 1.38 02/06/02
 * @author Scott Violet
 */
public class DefaultTreeSelectionModel extends Object implements Cloneable, Serializable, TreeSelectionModel
{
    /** Property name for selectionMode. */
    public static final String          SELECTION_MODE_PROPERTY = "selectionMode";

    /** Used to messaged registered listeners. */
    protected SwingPropertyChangeSupport     changeSupport;

    /** Paths that are currently selected.  Will be null if nothing is
      * currently selected. */
    protected TreePath[]                selection;

    /** Event listener list. */
    protected EventListenerList   listenerList = new EventListenerList();

    /** Provides a row for a given path. */
    transient protected RowMapper               rowMapper;

    /** Handles maintaining the list selection model. The RowMapper is used
     * to map from a TreePath to a row, and the value is then placed here. */
    protected DefaultListSelectionModel     listSelectionModel;

    /** Mode for the selection, will be either SINGLE_TREE_SELECTION,
     * CONTIGUOUS_TREE_SELECTION or DISCONTIGUOUS_TREE_SELECTION.
     */
    protected int                           selectionMode;

    /** Last path that was added. */
    protected TreePath                      leadPath;
    /** Index of the lead path in selection. */
    protected int                           leadIndex;
    /** Lead row. */
    protected int                           leadRow;

    /** Used to make sure the paths are unique, will contain all the paths
     * in <code>selection</code>.
     */
    private Hashtable                       uniquePaths;
    private Hashtable                       lastPaths;
    private TreePath[]                      tempPaths;


    /**
     * Creates a new instance of DefaultTreeSelectionModel that is
     * empty, with a selection mode of DISCONTIGUOUS_TREE_SELECTION.
     */
    public DefaultTreeSelectionModel() {
	listSelectionModel = new DefaultListSelectionModel();
	selectionMode = DISCONTIGUOUS_TREE_SELECTION;
	leadIndex = leadRow = -1;
	uniquePaths = new Hashtable();
	lastPaths = new Hashtable();
	tempPaths = new TreePath[1];
    }

    /**
     * Sets the RowMapper instance. This instance is used to determine
     * the row for a particular TreePath.
     */
    public void setRowMapper(RowMapper newMapper) {
	rowMapper = newMapper;
	resetRowSelection();
    }

    /**
     * Returns the RowMapper instance that is able to map a TreePath to a
     * row.
     */
    public RowMapper getRowMapper() {
	return rowMapper;
    }

    /**
     * Sets the selection model, which must be one of SINGLE_TREE_SELECTION,
     * CONTIGUOUS_TREE_SELECTION or DISCONTIGUOUS_TREE_SELECTION. If mode
     * is not one of the defined value,
     * <code>DISCONTIGUOUS_TREE_SELECTION</code> is assumed.
     * <p>This may change the selection if the current selection is not valid
     * for the new mode. For example, if three TreePaths are 
     * selected when the mode is changed to <code>SINGLE_TREE_SELECTION</code>,
     * only one TreePath will remain selected. It is up to the particular
     * implementation to decide what TreePath remains selected.
     * <p>
     * Setting the mode to something other than the defined types will
     * result in the mode becoming <code>DISCONTIGUOUS_TREE_SELECTION</code>.
     */
    public void setSelectionMode(int mode) {
	int            oldMode = selectionMode;

	selectionMode = mode;
	if(selectionMode != TreeSelectionModel.SINGLE_TREE_SELECTION &&
	   selectionMode != TreeSelectionModel.CONTIGUOUS_TREE_SELECTION &&
	   selectionMode != TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
	    selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
	if(oldMode != selectionMode && changeSupport != null)
	    changeSupport.firePropertyChange(SELECTION_MODE_PROPERTY,
					     new Integer(oldMode),
					     new Integer(selectionMode));
    }

    /**
     * Returns the selection mode, one of <code>SINGLE_TREE_SELECTION</code>,
     * <code>DISCONTIGUOUS_TREE_SELECTION</code> or 
     * <code>CONTIGUOUS_TREE_SELECTION</code>.
     */
    public int getSelectionMode() {
	return selectionMode;
    }

    /**
      * Sets the selection to path. If this represents a change, then
      * the TreeSelectionListeners are notified. If <code>path</code> is
      * null, this has the same effect as invoking <code>clearSelection</code>.
      *
      * @param path new path to select
      */
    public void setSelectionPath(TreePath path) {
	if(path == null)
	    setSelectionPaths(null);
	else {
	    TreePath[]          newPaths = new TreePath[1];

	    newPaths[0] = path;
	    setSelectionPaths(newPaths);
	}
    }

    /**
      * Sets the selection to the paths in paths.  If this represents a
      * change the TreeSelectionListeners are notified.  Potentially
      * paths will be held by this object; in other words don't change
      * any of the objects in the array once passed in.
      * <p>If <code>paths</code> is
      * null, this has the same effect as invoking <code>clearSelection</code>.
      * <p>The lead path is set to the last path in <code>pPaths</code>.
      * <p>If the selection mode is <code>CONTIGUOUS_TREE_SELECTION</code>,
      * and adding the new paths would make the selection discontiguous,
      * the selection is reset to the first TreePath in <code>paths</code>.
      *
      * @param paths new selection
      */
    public void setSelectionPaths(TreePath[] pPaths) {
	int            newCount, newCounter, oldCount, oldCounter;
	TreePath[]     paths = pPaths;

	if(paths == null)
	    newCount = 0;
	else
	    newCount = paths.length;
	if(selection == null)
	    oldCount = 0;
	else
	    oldCount = selection.length;
	if((newCount + oldCount) != 0) {
	    if(selectionMode == TreeSelectionModel.SINGLE_TREE_SELECTION) {
		/* If single selection and more than one path, only allow
		   first. */
		if(newCount > 1) {
		    paths = new TreePath[1];
		    paths[0] = pPaths[0];
		    newCount = 1;
		}
	    }
	    else if(selectionMode ==
		    TreeSelectionModel.CONTIGUOUS_TREE_SELECTION) {
		/* If contiguous selection and paths aren't contiguous,
		   only select the first path item. */
		if(newCount > 0 && !arePathsContiguous(paths)) {
		    paths = new TreePath[1];
		    paths[0] = pPaths[0];
		    newCount = 1;
		}
	    }

	    int              validCount = 0;
	    TreePath         beginLeadPath = leadPath;
	    Vector           cPaths = new Vector(newCount + oldCount);

	    lastPaths.clear();
	    leadPath = null;
	    /* Find the paths that are new. */
	    for(newCounter = 0; newCounter < newCount; newCounter++) {
		if(paths[newCounter] != null &&
		   lastPaths.get(paths[newCounter]) == null) {
		    validCount++;
		    lastPaths.put(paths[newCounter], Boolean.TRUE);
		    if (uniquePaths.get(paths[newCounter]) == null) {
			cPaths.addElement(new PathPlaceHolder
					  (paths[newCounter], true));
		    }
		    leadPath = paths[newCounter];
		}
	    }

	    /* If the validCount isn't equal to newCount it means there
	       are some null in paths, remove them and set selection to
	       the new path. */
	    TreePath[]     newSelection;

	    if(validCount == 0) {
		newSelection = null;
	    }
	    else if (validCount != newCount) {
		Enumeration keys = lastPaths.keys();

		newSelection = new TreePath[validCount];
		validCount = 0;
		while (keys.hasMoreElements()) {
		    newSelection[validCount++] = (TreePath)keys.nextElement();
		}
	    }
	    else {
		newSelection = new TreePath[paths.length];
		System.arraycopy(paths, 0, newSelection, 0, paths.length);
	    }

	    /* Get the paths that were selected but no longer selected. */
	    for(oldCounter = 0; oldCounter < oldCount; oldCounter++)
		if(selection[oldCounter] != null && 
		    lastPaths.get(selection[oldCounter]) == null)
		    cPaths.addElement(new PathPlaceHolder
				      (selection[oldCounter], false));

	    selection = newSelection;

	    Hashtable      tempHT = uniquePaths;

	    uniquePaths = lastPaths;
	    lastPaths = tempHT;
	    lastPaths.clear();

	    // No reason to do this now, but will still call it.
	    if(selection != null)
		insureUniqueness();

	    updateLeadIndex();

	    resetRowSelection();
	    /* Notify of the change. */
	    if(cPaths.size() > 0)
		notifyPathChange(cPaths, beginLeadPath);
	}
    }

    /**
      * Adds path to the current selection. If path is not currently
      * in the selection the TreeSelectionListeners are notified. This has
      * no effect if <code>path</code> is null.
      *
      * @param path the new path to add to the current selection
      */
    public void addSelectionPath(TreePath path) {
	if(path != null) {
	    TreePath[]            toAdd = new TreePath[1];

	    toAdd[0] = path;
	    addSelectionPaths(toAdd);
	}
    }

    /**
      * Adds paths to the current selection. If any of the paths in
      * paths are not currently in the selection the TreeSelectionListeners
      * are notified. This has
      * no effect if <code>paths</code> is null.
      * <p>The lead path is set to the last element in <code>paths</code>.
      * <p>If the selection mode is <code>CONTIGUOUS_TREE_SELECTION</code>,
      * and adding the new paths would make the selection discontiguous.
      * Then two things can result: if the TreePaths in <code>paths</code>
      * are contiguous, then the selection becomes these TreePaths,
      * otherwise the TreePaths aren't contiguous and the selection becomes
      * the first TreePath in <code>paths</code>.
      *
      * @param path the new path to add to the current selection
      */
    public void addSelectionPaths(TreePath[] paths) {
	int       newPathLength = ((paths == null) ? 0 : paths.length);

	if(newPathLength > 0) {
	    if(selectionMode == TreeSelectionModel.SINGLE_TREE_SELECTION) {
		setSelectionPaths(paths);
	    }
	    else if(selectionMode == TreeSelectionModel.
		    CONTIGUOUS_TREE_SELECTION && !canPathsBeAdded(paths)) {
		if(arePathsContiguous(paths)) {
		    setSelectionPaths(paths);
		}
		else {
		    TreePath[]          newPaths = new TreePath[1];

		    newPaths[0] = paths[0];
		    setSelectionPaths(newPaths);
		}
	    }
	    else {
		int               counter, validCount;
		int               oldCount;
		TreePath          beginLeadPath = leadPath;
		Vector            cPaths = null;

		if(selection == null)
		    oldCount = 0;
		else
		    oldCount = selection.length;
		/* Determine the paths that aren't currently in the
		   selection. */
		lastPaths.clear();
		for(counter = 0, validCount = 0; counter < newPathLength;
		    counter++) {
		    if(paths[counter] != null) {
			if (uniquePaths.get(paths[counter]) == null) {
			    validCount++;
			    if(cPaths == null)
				cPaths = new Vector();
			    cPaths.addElement(new PathPlaceHolder
					      (paths[counter], true));
			    uniquePaths.put(paths[counter], Boolean.TRUE);
			    lastPaths.put(paths[counter], Boolean.TRUE);
			}
			leadPath = paths[counter];
		    }
		}

		if(leadPath == null) {
		    leadPath = beginLeadPath;
		}

		if(validCount > 0) {
		    TreePath         newSelection[] = new TreePath[oldCount +
								  validCount];

		    /* And build the new selection. */
		    if(oldCount > 0) 
			System.arraycopy(selection, 0, newSelection, 0,
					 oldCount);
		    if(validCount != paths.length) {
			/* Some of the paths in paths are already in
			   the selection. */
			Enumeration   newPaths = lastPaths.keys();

			counter = oldCount;
			while (newPaths.hasMoreElements()) {
			    newSelection[counter++] = (TreePath)newPaths.
				                      nextElement();
			}
		    }
		    else {
			System.arraycopy(paths, 0, newSelection, oldCount,
					 validCount);
		    }

		    selection = newSelection;

		    insureUniqueness();

		    updateLeadIndex();

		    resetRowSelection();

		    notifyPathChange(cPaths, beginLeadPath);
		}
		else
		    leadPath = beginLeadPath;
		lastPaths.clear();
	    }
	}
    }

    /**
      * Removes path from the selection. If path is in the selection
      * The TreeSelectionListeners are notified. This has no effect if
      * <code>path</code> is null.
      *
      * @param path the path to remove from the selection
      */
    public void removeSelectionPath(TreePath path) {
	if(path != null) {
	    TreePath[]             rPath = new TreePath[1];

	    rPath[0] = path;
	    removeSelectionPaths(rPath);
	}
    }

    /**
      * Removes paths from the selection.  If any of the paths in paths
      * are in the selection the TreeSelectionListeners are notified.
      * This has no effect if <code>paths</code> is null.
      *
      * @param path the path to remove from the selection
      */
    public void removeSelectionPaths(TreePath[] paths) {
	if (paths != null && selection != null && paths.length > 0) {
	    if(!canPathsBeRemoved(paths)) {
		/* Could probably do something more interesting here! */
		clearSelection();
	    }
	    else {
		Vector      pathsToRemove = null;

		/* Find the paths that can be removed. */
		for (int removeCounter = paths.length - 1; removeCounter >= 0;
		     removeCounter--) {
		    if(paths[removeCounter] != null) {
			if (uniquePaths.get(paths[removeCounter]) != null) {
			    if(pathsToRemove == null)
				pathsToRemove = new Vector(paths.length);
			    uniquePaths.remove(paths[removeCounter]);
			    pathsToRemove.addElement(new PathPlaceHolder
					 (paths[removeCounter], false));
			}
		    }
		}
		if(pathsToRemove != null) {
		    int         removeCount = pathsToRemove.size();
		    TreePath    beginLeadPath = leadPath;

		    if(removeCount == selection.length) {
			selection = null;
		    }
		    else {
			Enumeration          pEnum = uniquePaths.keys();
			int                  validCount = 0;

			selection = new TreePath[selection.length -
						removeCount];
			while (pEnum.hasMoreElements()) {
			    selection[validCount++] = (TreePath)pEnum.
				                          nextElement();
			}
		    }
		    if (leadPath != null &&
			uniquePaths.get(leadPath) == null) {
			if (selection != null) {
			    leadPath = selection[selection.length - 1];
			}
			else {
			    leadPath = null;
			}
		    }
		    else if (selection != null) {
			leadPath = selection[selection.length - 1];
		    }
		    else {
			leadPath = null;
		    }
		    updateLeadIndex();

		    resetRowSelection();

		    notifyPathChange(pathsToRemove, beginLeadPath);
		}
	    }
	}
    }

    /**
      * Returns the first path in the selection. This is useful if there
      * if only one item currently selected.
      */
    public TreePath getSelectionPath() {
	if(selection != null)
	    return selection[0];
	return null;
    }

    /**
      * Returns the paths in the selection. This will return null (or an
      * empty array) if nothing is currently selected.
      */
    public TreePath[] getSelectionPaths() {
	if(selection != null) {
	    int                 pathSize = selection.length;
	    TreePath[]          result = new TreePath[pathSize];

	    System.arraycopy(selection, 0, result, 0, pathSize);
	    return result;
	}
	return null;
    }

    /**
     * Returns the number of paths that are selected.
     */
    public int getSelectionCount() {
	return (selection == null) ? 0 : selection.length;
    }

    /**
      * Returns true if the path, <code>path</code>,
      * is in the current selection.
      */
    public boolean isPathSelected(TreePath path) {
	return (path != null) ? (uniquePaths.get(path) != null) : false;
    }

    /**
      * Returns true if the selection is currently empty.
      */
    public boolean isSelectionEmpty() {
	return (selection == null);
    }

    /**
      * Empties the current selection.  If this represents a change in the
      * current selection, the selection listeners are notified.
      */
    public void clearSelection() {
	if(selection != null) {
	    int                    selSize = selection.length;
	    boolean[]              newness = new boolean[selSize];

	    for(int counter = 0; counter < selSize; counter++)
		newness[counter] = false;

	    TreeSelectionEvent     event = new TreeSelectionEvent
		(this, selection, newness, leadPath, null);

	    leadPath = null;
	    leadIndex = leadRow = -1;
	    uniquePaths.clear();
	    selection = null;
	    resetRowSelection();
	    fireValueChanged(event);
	}
    }

    /**
      * Adds x to the list of listeners that are notified each time the
      * set of selected TreePaths changes.
      *
      * @param x the new listener to be added
      */
    public void addTreeSelectionListener(TreeSelectionListener x) {
	listenerList.add(TreeSelectionListener.class, x);
    }

    /**
      * Removes x from the list of listeners that are notified each time
      * the set of selected TreePaths changes.
      *
      * @param x the listener to remove
      */
    public void removeTreeSelectionListener(TreeSelectionListener x) {
	listenerList.remove(TreeSelectionListener.class, x);
    }

    /**
     * Notifies all listeners that are registered for
     * tree selection events on this object.  
     * @see addTreeSelectionListener
     * @see EventListenerList
     */
    protected void fireValueChanged(TreeSelectionEvent e) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// TreeSelectionEvent e = null;
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==TreeSelectionListener.class) {
		// Lazily create the event:
		// if (e == null)
		// e = new ListSelectionEvent(this, firstIndex, lastIndex);
		((TreeSelectionListener)listeners[i+1]).valueChanged(e);
	    }	       
	}
    }

    /**
     * Returns an array of all the listeners of the given type that 
     * were added to this model. 
     *
     * @return all of the objects receiving <em>listenerType</em> notifications 
     *          from this model
     * 
     * @since 1.3
     */
    public EventListener[] getListeners(Class listenerType) { 
	return listenerList.getListeners(listenerType); 
    }

    /**
      * Returns all of the currently selected rows. This will return
      * null (or an empty array) if there are no selected TreePaths or
      * a RowMapper has not been set.
      * This may return an array of length less that than of the selected
      * TreePaths if some of the rows are not visible (that is the
      * RowMapper returned -1 for the row corresponding to the TreePath).
      */
    public int[] getSelectionRows() {
	// This is currently rather expensive.  Needs
	// to be better support from ListSelectionModel to speed this up.
	if(rowMapper != null && selection != null) {
	    int[]      rows = rowMapper.getRowsForPaths(selection);

	    if (rows != null) {
		int       invisCount = 0;

		for (int counter = rows.length - 1; counter >= 0; counter--) {
		    if (rows[counter] == -1) {
			invisCount++;
		    }
		}
		if (invisCount > 0) {
		    if (invisCount == rows.length) {
			rows = null;
		    }
		    else {
			int[]    tempRows = new int[rows.length - invisCount];

			for (int counter = rows.length - 1, visCounter = 0;
			     counter >= 0; counter--) {
			    if (rows[counter] != -1) {
				tempRows[visCounter++] = rows[counter];
			    }
			}
			rows = tempRows;
		    }
		}
	    }
	    return rows;
	}
	return null;
    }

    /**
     * Returns the smallest value obtained from the RowMapper for the
     * current set of selected TreePaths. If nothing is selected,
     * or there is no RowMapper, this will return -1.
      */
    public int getMinSelectionRow() {
	return listSelectionModel.getMinSelectionIndex();
    }

    /**
     * Returns the largest value obtained from the RowMapper for the
     * current set of selected TreePaths. If nothing is selected,
     * or there is no RowMapper, this will return -1.
      */
    public int getMaxSelectionRow() {
	return listSelectionModel.getMaxSelectionIndex();
    }

    /**
      * Returns true if the row identitifed by row is selected.
      */
    public boolean isRowSelected(int row) {
	return listSelectionModel.isSelectedIndex(row);
    }

    /**
     * Updates this object's mapping from TreePath to rows. This should
     * be invoked when the mapping from TreePaths to integers has changed
     * (for example, a node has been expanded).
     * <p>You do not normally have to call this, JTree and its associated
     * Listeners will invoke this for you. If you are implementing your own
     * View class, then you will have to invoke this.
     * <p>This will invoke <code>insureRowContinuity</code> to make sure
     * the currently selected TreePaths are still valid based on the
     * selection mode.
     */
    public void resetRowSelection() {
	listSelectionModel.clearSelection();
	if(selection != null && rowMapper != null) {
	    int               aRow;
	    int               validCount = 0;
	    int[]             rows = rowMapper.getRowsForPaths(selection);

	    for(int counter = 0, maxCounter = selection.length;
		counter < maxCounter; counter++) {
		aRow = rows[counter];
		if(aRow != -1) {
		    listSelectionModel.addSelectionInterval(aRow, aRow);
		}
	    }
	    if(leadIndex != -1 && rows != null) {
		leadRow = rows[leadIndex];
	    }
	    else if (leadPath != null) {
		// Lead selection path doesn't have to be in the selection.
		tempPaths[0] = leadPath;
		rows = rowMapper.getRowsForPaths(tempPaths);
		leadRow = (rows != null) ? rows[0] : -1;
	    }
	    else {
		leadRow = -1;
	    }
	    insureRowContinuity();

	}
	else
	    leadRow = -1;
    }

    /**
     * Returns the lead selection index. That is the last index that was
     * added.
     */
    public int getLeadSelectionRow() {
	return leadRow;
    }

    /**
     * Returns the last path that was added. This may differ from the 
     * leadSelectionPath property maintained by the JTree.
     */
    public TreePath getLeadSelectionPath() {
	return leadPath;
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     * The listener is registered for all properties.
     * <p>
     * A PropertyChangeEvent will get fired when the selection mode
     * changes.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public synchronized void addPropertyChangeListener(
                                PropertyChangeListener listener) {
        if (changeSupport == null) {
            changeSupport = new SwingPropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * This removes a PropertyChangeListener that was registered
     * for all properties.
     *
     * @param listener  the PropertyChangeListener to be removed
     */

    public synchronized void removePropertyChangeListener(
                                PropertyChangeListener listener) {
        if (changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Makes sure the currently selected TreePaths are valid
     * for the current selection mode.
     * If the selection mode is <code>CONTIGUOUS_TREE_SELECTION</code>
     * and a RowMapper exists, this will make sure all the rows are
     * contiguous. If the selection isn't contiguous, the selection is
     * reset to contain the first set of contiguous paths.
     * <p>
     * If the selection mode is <code>SINGLE_TREE_SELECTION</code> and
     * more than one TreePath is selected, the selection is reset to
     * contain the first path currently selected.
     */
    protected void insureRowContinuity() {
	if(selectionMode == TreeSelectionModel.CONTIGUOUS_TREE_SELECTION &&
	   selection != null && rowMapper != null) {
	    DefaultListSelectionModel lModel = listSelectionModel;
	    int                       min = lModel.getMinSelectionIndex();

	    if(min != -1) {
		for(int counter = min,
			maxCounter = lModel.getMaxSelectionIndex();
		        counter <= maxCounter; counter++) {
		    if(!lModel.isSelectedIndex(counter)) {
			if(counter == min) {
			    clearSelection();
			}
			else {
			    TreePath[]   newSel = new TreePath[counter - min];

			    System.arraycopy(selection, 0, newSel,
					     0, counter - min);
			    setSelectionPaths(newSel);
			    break;
			}
		    }
		}
	    }
	}
	else if(selectionMode == TreeSelectionModel.SINGLE_TREE_SELECTION &&
		selection != null && selection.length > 1) {
	    setSelectionPath(selection[0]);
	}
    }

    /**
     * Returns true if the paths are contiguous,
     * or this object has no RowMapper.
     */
    protected boolean arePathsContiguous(TreePath[] paths) {
	if(rowMapper == null || paths.length < 2)
	    return true;
	else {
	    BitSet                             bitSet = new BitSet(32);
	    int                                anIndex, counter, min;
	    int                                pathCount = paths.length;
	    int                                validCount = 0;
	    TreePath[]                         tempPath = new TreePath[1];

	    tempPath[0] = paths[0];
	    min = rowMapper.getRowsForPaths(tempPath)[0];
	    for(counter = 0; counter < pathCount; counter++) {
		if(paths[counter] != null) {
		    tempPath[0] = paths[counter];
		    int[] rows = rowMapper.getRowsForPaths(tempPath);
		    if (rows == null) {
			return false;
		    }
		    anIndex = rows[0];
		    if(anIndex == -1 || anIndex < (min - pathCount) ||
		       anIndex > (min + pathCount))
			return false;
		    if(anIndex < min)
			min = anIndex;
		    if(!bitSet.get(anIndex)) {
			bitSet.set(anIndex);
			validCount++;
		    }
		}
	    }
	    int          maxCounter = validCount + min;

	    for(counter = min; counter < maxCounter; counter++)
		if(!bitSet.get(counter))
		    return false;
	}
	return true;
    }

    /**
     * Used to test if a particular set of <code>TreePath</code>s can
     * be added. This will return true if <code>paths</code> is null (or
     * empty), or this object has no RowMapper, or nothing is currently selected,
     * or the selection mode is <code>DISCONTIGUOUS_TREE_SELECTION</code>, or
     * adding the paths to the current selection still results in a
     * contiguous set of <code>TreePath</code>s.
     */
    protected boolean canPathsBeAdded(TreePath[] paths) {
	if(paths == null || paths.length == 0 || rowMapper == null ||
	   selection == null || selectionMode ==
	   TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
	    return true;
	else {
	    BitSet                       bitSet = new BitSet();
	    DefaultListSelectionModel    lModel = listSelectionModel;
	    int                          anIndex;
	    int                          counter;
	    int                          min = lModel.getMinSelectionIndex();
	    int	                         max = lModel.getMaxSelectionIndex();
	    TreePath[]                   tempPath = new TreePath[1];

	    if(min != -1) {
		for(counter = min; counter <= max; counter++) {
		    if(lModel.isSelectedIndex(counter))
			bitSet.set(counter);
		}
	    }
	    else {
		tempPath[0] = paths[0];
		min = max = rowMapper.getRowsForPaths(tempPath)[0];
	    }
	    for(counter = paths.length - 1; counter >= 0; counter--) {
		if(paths[counter] != null) {
		    tempPath[0] = paths[counter];
		    int[]   rows = rowMapper.getRowsForPaths(tempPath);
		    if (rows == null) {
			return false;
		    }
		    anIndex = rows[0];
		    min = Math.min(anIndex, min);
		    max = Math.max(anIndex, max);
		    if(anIndex == -1)
			return false;
		    bitSet.set(anIndex);
		}
	    }
	    for(counter = min; counter <= max; counter++)
		if(!bitSet.get(counter))
		    return false;
	}
	return true;
    }

    /**
     * Returns true if the paths can be removed without breaking the
     * continuity of the model.
     * This is rather expensive.
     */
    protected boolean canPathsBeRemoved(TreePath[] paths) {
	if(rowMapper == null || selection == null ||
	   selectionMode == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)
	    return true;
	else {
	    BitSet               bitSet = new BitSet();
	    int                  counter;
	    int                  pathCount = paths.length;
	    int                  anIndex;
	    int                  min = -1;
	    int                  validCount = 0;
	    TreePath[]           tempPath = new TreePath[1];
	    int[]                rows;

	    /* Determine the rows for the removed entries. */
	    lastPaths.clear();
	    for (counter = 0; counter < pathCount; counter++) {
		if (paths[counter] != null) {
		    lastPaths.put(paths[counter], Boolean.TRUE);
		}
	    }
	    for(counter = selection.length - 1; counter >= 0; counter--) {
		if(lastPaths.get(selection[counter]) == null) {
		    tempPath[0] = selection[counter];
		    rows = rowMapper.getRowsForPaths(tempPath);
		    if(rows != null && rows[0] != -1 && !bitSet.get(rows[0])) {
			validCount++;
			if(min == -1)
			    min = rows[0];
			else
			    min = Math.min(min, rows[0]);
			bitSet.set(rows[0]);
		    }
		}
	    }
	    lastPaths.clear();
	    /* Make sure they are contiguous. */
	    if(validCount > 1) {
		for(counter = min + validCount - 1; counter >= min;
		    counter--)
		    if(!bitSet.get(counter))
			return false;
	    }
	}
	return true;
    }

    /**
      * Notifies listeners of a change in path. changePaths should contain
      * instances of PathPlaceHolder.
      */
    protected void notifyPathChange(Vector changedPaths,
				    TreePath oldLeadSelection) {
	int                    cPathCount = changedPaths.size();
	boolean[]              newness = new boolean[cPathCount];
	TreePath[]            paths = new TreePath[cPathCount];
	PathPlaceHolder        placeholder;
	
	for(int counter = 0; counter < cPathCount; counter++) {
	    placeholder = (PathPlaceHolder)changedPaths.elementAt(counter);
	    newness[counter] = placeholder.isNew;
	    paths[counter] = placeholder.path;
	}
	
	TreeSelectionEvent     event = new TreeSelectionEvent
	                  (this, paths, newness, oldLeadSelection, leadPath);
	
	fireValueChanged(event);
    }

    /**
     * Updates the leadIndex instance variable.
     */
    protected void updateLeadIndex() {
	if(leadPath != null) {
	    if(selection == null) {
		leadPath = null;
		leadIndex = leadRow = -1;
	    }
	    else {
		leadRow = leadIndex = -1;
		for(int counter = selection.length - 1; counter >= 0;
		    counter--) {
		    // Can use == here since we know leadPath came from
		    // selection
		    if(selection[counter] == leadPath) {
			leadIndex = counter;
			break;
		    }
		}
	    }
	}
	else {
	    leadIndex = -1;
	}
    }

    /**
     * This method is obsolete and its implementation is now a noop.  It's
     * still called by setSelectionPaths and addSelectionPaths, but only
     * for backwards compatability.
     */
    protected void insureUniqueness() {
    }


    /**
     * Returns a string that displays and identifies this
     * object's properties.
     *
     * @return a String representation of this object
     */
    public String toString() {
	int                selCount = getSelectionCount();
	StringBuffer       retBuffer = new StringBuffer();
	int[]              rows;

	if(rowMapper != null)
	    rows = rowMapper.getRowsForPaths(selection);
	else
	    rows = null;
	retBuffer.append(getClass().getName() + " " + hashCode() + " [ ");
	for(int counter = 0; counter < selCount; counter++) {
	    if(rows != null)
		retBuffer.append(selection[counter].toString() + "@" +
				 Integer.toString(rows[counter])+ " ");
	    else
		retBuffer.append(selection[counter].toString() + " ");
	}
	retBuffer.append("]");
	return retBuffer.toString();
    }

    /**
     * Returns a clone of this object with the same selection.
     * This method does not duplicate
     * selection listeners and property listeners.
     *
     * @exception CloneNotSupportedException never thrown by instances of
     *                                       this class
     */
    public Object clone() throws CloneNotSupportedException {
	DefaultTreeSelectionModel        clone = (DefaultTreeSelectionModel)
	                    super.clone();

	clone.changeSupport = null;
	if(selection != null) {
	    int              selLength = selection.length;

	    clone.selection = new TreePath[selLength];
	    System.arraycopy(selection, 0, clone.selection, 0, selLength);
	}
	clone.listenerList = new EventListenerList();
	clone.listSelectionModel = (DefaultListSelectionModel)
	    listSelectionModel.clone();
	clone.uniquePaths = new Hashtable();
	clone.lastPaths = new Hashtable();
	clone.tempPaths = new TreePath[1];
	return clone;
    }

    // Serialization support.  
    private void writeObject(ObjectOutputStream s) throws IOException {
	Object[]             tValues;

	s.defaultWriteObject();
	// Save the rowMapper, if it implements Serializable
	if(rowMapper != null && rowMapper instanceof Serializable) {
	    tValues = new Object[2];
	    tValues[0] = "rowMapper";
	    tValues[1] = rowMapper;
	}
	else
	    tValues = new Object[0];
	s.writeObject(tValues);
    }


    private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
	Object[]      tValues;

	s.defaultReadObject();

	tValues = (Object[])s.readObject();

	if(tValues.length > 0 && tValues[0].equals("rowMapper"))
	    rowMapper = (RowMapper)tValues[1];
    }
}

/**
 * Holds a path and whether or not it is new.
 */
class PathPlaceHolder {
    protected boolean             isNew;
    protected TreePath           path;

    PathPlaceHolder(TreePath path, boolean isNew) {
	this.path = path;
	this.isNew = isNew;
    }
}
