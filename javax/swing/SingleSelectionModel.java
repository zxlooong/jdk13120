/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing;

import javax.swing.event.*;

/**
 * A model that supports at most one indexed selection.
 *
 * @version %I% %G%
 * @author Dave Moore
 */
public interface SingleSelectionModel {
    /**
     * Returns the model's selection.
     *
     * @return  the model's selection, or -1 if there is no selection
     * @see     #setSelectedIndex
     */
    public int getSelectedIndex();

    /**
     * Sets the model's selected index to <I>index</I>.
     *
     * Notifies any listeners if the model changes
     *
     * @param an int specifying the model selection
     * @see   #getSelectedIndex
     * @see   #addChangeListener
     */
    public void setSelectedIndex(int index);

    /**
     * Clears the selection (to -1).
     */
    public void clearSelection();

    /**
     * Returns true if the selection model currently has a selected value.
     * @return true if a value is currently selected
     */
    public boolean isSelected();

    /**
     * Adds <I>listener</I> as a listener to changes in the model.
     * @param l the ChangeListener to add
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes <I>listener</I> as a listener to changes in the model.
     * @param l the ChangeListener to remove
     */
    void removeChangeListener(ChangeListener listener);
}
