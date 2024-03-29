/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.util.Vector;
import java.util.Locale;
import java.util.EventListener;
import java.awt.peer.ListPeer;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.accessibility.*;


/**
 * The <code>List</code> component presents the user with a
 * scrolling list of text items. The list can be set up so that
 * the user can choose either one item or multiple items.
 * <p>
 * For example, the code&nbsp;.&nbsp;.&nbsp;.
 * <p>
 * <hr><blockquote><pre>
 * List lst = new List(4, false);
 * lst.add("Mercury");
 * lst.add("Venus");
 * lst.add("Earth");
 * lst.add("JavaSoft");
 * lst.add("Mars");
 * lst.add("Jupiter");
 * lst.add("Saturn");
 * lst.add("Uranus");
 * lst.add("Neptune");
 * lst.add("Pluto");
 * cnt.add(lst);
 * </pre></blockquote><hr>
 * <p>
 * where <code>cnt</code> is a container, produces the following
 * scrolling list:
 * <p>
 * <img src="doc-files/List-1.gif"
 * ALIGN=center HSPACE=10 VSPACE=7>
 * <p>
 * Clicking on an item that isn't selected selects it. Clicking on
 * an item that is already selected deselects it. In the preceding
 * example, only one item from the scrolling list can be selected
 * at a time, since the second argument when creating the new scrolling
 * list is <code>false</code>. Selecting an item causes any other
 * selected item to be automatically deselected.
 * <p>
 * Beginning with Java&nbsp;1.1, the Abstract Window Toolkit
 * sends the <code>List</code> object all mouse, keyboard, and focus events
 * that occur over it. (The old AWT event model is being maintained
 * only for backwards compatibility, and its use is discouraged.)
 * <p>
 * When an item is selected or deselected, AWT sends an instance
 * of <code>ItemEvent</code> to the list.
 * When the user double-clicks on an item in a scrolling list,
 * AWT sends an instance of <code>ActionEvent</code> to the
 * list following the item event. AWT also generates an action event
 * when the user presses the return key while an item in the
 * list is selected.
 * <p>
 * If an application wants to perform some action based on an item
 * in this list being selected or activated, it should implement
 * <code>ItemListener</code> or <code>ActionListener</code>
 * as appropriate and register the new listener to receive
 * events from this list.
 * <p>
 * For multiple-selection scrolling lists, it is considered a better
 * user interface to use an external gesture (such as clicking on a
 * button) to trigger the action.
 * @version 	1.79, 02/06/02
 * @author 	Sami Shaio
 * @see         java.awt.event.ItemEvent
 * @see         java.awt.event.ItemListener
 * @see         java.awt.event.ActionEvent
 * @see         java.awt.event.ActionListener
 * @since       JDK1.0
 */
public class List extends Component implements ItemSelectable, Accessible {
    /**
     * A vector created to contain items which will become
     * part of the List Component.
     *
     * @serial
     * @see addItem()
     * @see getItem()
     */
    Vector	items = new Vector();
    
    /**
     * This field will represent the number of rows in the
     * List Component.  It is specified only once, and
     * that is when the list component is actually
     * created.  It will never change.
     *
     * @serial
     * @see getRows()
     */
    int		rows = 0;

    /**
     * <code>multipleMode</code> is a variable that will
     * be set to <code>true</code> if a list component is to be set to
     * multiple selection mode, that is where the user can
     * select more than one item in a list at one time.
     * <code>multipleMode</code> will be set to false if the
     * list component is set to single selection, that is where
     * the user can only select one item on the list at any
     * one time.
     *
     * @serial
     * @see isMultipleMode()
     * @see setMultipleMode()
     */
    boolean	multipleMode = false;

    /**
     * <code>selected</code> is an array that will contain
     * the indices of items that have been selected.
     *
     * @serial
     * @see getSelectedIndexes()
     * @see getSelectedIndex()
     */
    int		selected[] = new int[0];

    /**
     * This variable contains the value that will be used
     * when trying to make a particular list item visible.
     *
     * @serial
     * @see makeVisible()
     */
    int		visibleIndex = -1;

    transient ActionListener actionListener;
    transient ItemListener itemListener;

    private static final String base = "list";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -3304312411574666869L;

    /**
     * Creates a new scrolling list.
     * By default, there are four visible lines and multiple selections are
     * not allowed.
     */
    public List() {
	this(0, false);
    }

    /**
     * Creates a new scrolling list initialized with the specified
     * number of visible lines. By default, multiple selections are
     * not allowed.
     * @param       rows the number of items to show.
     * @since       JDK1.1
     */
    public List(int rows) {
    	this(rows, false);
    }

    /**
     * The default number of visible rows is 4.  A list with
     * zero rows is unusable and unsightly.
     */
    final static int 	DEFAULT_VISIBLE_ROWS = 4;

    /**
     * Creates a new scrolling list initialized to display the specified
     * number of rows. If the value of <code>multipleMode</code> is
     * <code>true</code>, then the user can select multiple items from
     * the list. If it is <code>false</code>, only one item at a time
     * can be selected.
     * @param       rows   the number of items to show.
     * @param       multipleMode   if <code>true</code>,
     *                     then multiple selections are allowed;
     *                     otherwise, only one item can be selected at a time.
     */
    public List(int rows, boolean multipleMode) {
	this.rows = (rows != 0) ? rows : DEFAULT_VISIBLE_ROWS;
	this.multipleMode = multipleMode;
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
     * Creates the peer for the list.  The peer allows us to modify the
     * list's appearance without changing its functionality.
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
	    if (peer == null)
	        peer = getToolkit().createList(this);
	    super.addNotify();
	    visibleIndex = -1;
	}
    }

    /**
     * Removes the peer for this list.  The peer allows us to modify the
     * list's appearance without changing its functionality.
     */
    public void removeNotify() {
    	synchronized (getTreeLock()) {
	    ListPeer peer = (ListPeer)this.peer;
	    if (peer != null) {
		selected = peer.getSelectedIndexes();
	    }
	    super.removeNotify();
	}
    }

    /**
     * Gets the number of items in the list.
     * @return     the number of items in the list.
     * @see        java.awt.List#getItem
     * @since      JDK1.1
     */
    public int getItemCount() {
	return countItems();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getItemCount()</code>.
     */
    public int countItems() {
	return items.size();
    }

    /**
     * Gets the item associated with the specified index.
     * @return       an item that is associated with
     *                    the specified index.
     * @param        index the position of the item.
     * @see          java.awt.List#getItemCount
     */
    public String getItem(int index) {
	return getItemImpl(index);
    }

    // NOTE: This method may be called by privileged threads.
    //       We implement this functionality in a package-private method 
    //       to insure that it cannot be overridden by client subclasses. 
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    final String getItemImpl(int index) {
	return (String)items.elementAt(index);
    }

    /**
     * Gets the items in the list.
     * @return       a string array containing items of the list.
     * @see          java.awt.List#select
     * @see          java.awt.List#deselect
     * @see          java.awt.List#isIndexSelected
     * @since        JDK1.1
     */
    public synchronized String[] getItems() {
	String itemCopies[] = new String[items.size()];
    	items.copyInto(itemCopies);
	return itemCopies;
    }

    /**
     * Adds the specified item to the end of scrolling list.
     * @param item the item to be added.
     * @since JDK1.1
     */
    public void add(String item) {
	addItem(item);
    }

    /**
     * @deprecated      replaced by <code>add(String)</code>.
     */
    public void addItem(String item) {
	addItem(item, -1);
    }

    /**
     * Adds the specified item to the the scrolling list
     * at the position indicated by the index.  The index is 
     * zero-based.  If the value of the index is less than zero, 
     * or if the value of the index is greater than or equal to 
     * the number of items in the list, then the item is added 
     * to the end of the list.
     * @param       item   the item to be added.
     *              If this parameter is null then the item is
     *              treated as an empty string, <code>""</code>.
     * @param       index  the position at which to add the item.
     * @since       JDK1.1
     */
    public void add(String item, int index) {
	addItem(item, index);
    }

    /**
     * @deprecated      replaced by <code>add(String, int)</code>.
     */
    public synchronized void addItem(String item, int index) {
	if (index < -1 || index >= items.size()) {
	    index = -1;
	}

        if (item == null) {
            item = "";
        }

	if (index == -1) {
	    items.addElement(item);
	} else {
	    items.insertElementAt(item, index);
	}

	ListPeer peer = (ListPeer)this.peer;
	if (peer != null) {
	    peer.addItem(item, index);
	}
    }

    /**
     * Replaces the item at the specified index in the scrolling list
     * with the new string.
     * @param       newValue   a new string to replace an existing item.
     * @param       index      the position of the item to replace.
     */
    public synchronized void replaceItem(String newValue, int index) {
	remove(index);
	add(newValue, index);
    }

    /**
     * Removes all items from this list.
     * @see #remove
     * @see #delItems
     * @since JDK1.1
     */
    public void removeAll() {
	clear();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>removeAll()</code>.
     */
    public synchronized void clear() {
	ListPeer peer = (ListPeer)this.peer;
	if (peer != null) {
	    peer.clear();
	}
	items = new Vector();
	selected = new int[0];
    }

    /**
     * Removes the first occurrence of an item from the list.
     * @param        item  the item to remove from the list.
     * @exception    IllegalArgumentException
     *                     if the item doesn't exist in the list.
     * @since        JDK1.1
     */
    public synchronized void remove(String item) {
    	int index = items.indexOf(item);
    	if (index < 0) {
	    throw new IllegalArgumentException("item " + item +
					       " not found in list");
	} else {
	    remove(index);
	}
    }

    /**
     * Remove the item at the specified position
     * from this scrolling list.
     * @param      position   the index of the item to delete.
     * @see        java.awt.List#add(String, int)
     * @since      JDK1.1
     * @exception    ArrayIndexOutOfBoundsException
     *               if the <code>position</code> is less than 0 or
     *               greater than <code>getItemCount()-1</code>
     */
    public void remove(int position) {
	delItem(position);
    }

    /**
     * @deprecated     replaced by <code>remove(String)</code>
     *                         and <code>remove(int)</code>.
     */
    public void delItem(int position) {
	delItems(position, position);
    }

    /**
     * Gets the index of the selected item on the list,
     * @return        the index of the selected item, or
     *                     <code>-1</code> if no item is selected,
     *                     or if more that one item is selected.
     * @see           java.awt.List#select
     * @see           java.awt.List#deselect
     * @see           java.awt.List#isIndexSelected
     */
    public synchronized int getSelectedIndex() {
	int sel[] = getSelectedIndexes();
	return (sel.length == 1) ? sel[0] : -1;
    }

    /**
     * Gets the selected indexes on the list.
     * @return        an array of the selected indexes
     *                of this scrolling list. If no items are 
     *                selected, a zero-length array is returned.
     * @see           java.awt.List#select
     * @see           java.awt.List#deselect
     * @see           java.awt.List#isIndexSelected
     */
    public synchronized int[] getSelectedIndexes() {
	ListPeer peer = (ListPeer)this.peer;
	if (peer != null) {
	    selected = ((ListPeer)peer).getSelectedIndexes();
	}
	return selected;
    }

    /**
     * Get the selected item on this scrolling list.
     * @return        the selected item on the list,
     *                     or null if no item is selected.
     * @see           java.awt.List#select
     * @see           java.awt.List#deselect
     * @see           java.awt.List#isIndexSelected
     */
    public synchronized String getSelectedItem() {
	int index = getSelectedIndex();
	return (index < 0) ? null : getItem(index);
    }

    /**
     * Get the selected items on this scrolling list.
     * @return        an array of the selected items
     *                            on this scrolling list.
     * @see           java.awt.List#select
     * @see           java.awt.List#deselect
     * @see           java.awt.List#isIndexSelected
     */
    public synchronized String[] getSelectedItems() {
	int sel[] = getSelectedIndexes();
	String str[] = new String[sel.length];
	for (int i = 0 ; i < sel.length ; i++) {
	    str[i] = getItem(sel[i]);
	}
	return str;
    }

    /**
     * Returns the selected items on the list in an array of Objects.
     * @see ItemSelectable
     */
    public Object[] getSelectedObjects() {
        return getSelectedItems();
    }

    /**
     * Selects the item at the specified index in the scrolling list.
     * @param        index the position of the item to select.
     * @see          java.awt.List#getSelectedItem
     * @see          java.awt.List#deselect
     * @see          java.awt.List#isIndexSelected
     */
    public void select(int index) {
        // Bug #4059614: select can't be synchronized while calling the peer, 
        // because it is called from the Window Thread.  It is sufficient to 
        // synchronize the code that manipulates 'selected' except for the 
        // case where the peer changes.  To handle this case, we simply 
        // repeat the selection process. 
         
        ListPeer peer; 
        do { 
            peer = (ListPeer)this.peer; 
            if (peer != null) { 
                peer.select(index); 
                return; 
            } 
             
            synchronized(this) 
            { 
                boolean alreadySelected = false; 
 
                for (int i = 0 ; i < selected.length ; i++) { 
                    if (selected[i] == index) { 
                        alreadySelected = true; 
                        break; 
                    } 
                } 
 
                if (!alreadySelected) { 
                    if (!multipleMode) { 
                        selected = new int[1]; 
                        selected[0] = index; 
                    } else { 
                        int newsel[] = new int[selected.length + 1]; 
                        System.arraycopy(selected, 0, newsel, 0, 
                                         selected.length); 
                        newsel[selected.length] = index; 
                        selected = newsel; 
                    } 
                } 
            } 
        } while (peer != this.peer); 
    }

    /**
     * Deselects the item at the specified index.
     * <p>
     * If the item at the specified index is not selected, or if the
     * index is out of range, then the operation is ignored.
     * @param        index the position of the item to deselect.
     * @see          java.awt.List#select
     * @see          java.awt.List#getSelectedItem
     * @see          java.awt.List#isIndexSelected
     */
    public synchronized void deselect(int index) {
	ListPeer peer = (ListPeer)this.peer;
	if (peer != null) {
	    peer.deselect(index);
	}

	for (int i = 0 ; i < selected.length ; i++) {
	    if (selected[i] == index) {
		int newsel[] = new int[selected.length - 1];
		System.arraycopy(selected, 0, newsel, 0, i);
		System.arraycopy(selected, i+1, newsel, i, selected.length - (i+1));
		selected = newsel;
		return;
	    }
	}
    }

    /**
     * Determines if the specified item in this scrolling list is
     * selected.
     * @param      index   the item to be checked.
     * @return     <code>true</code> if the specified item has been
     *                       selected; <code>false</code> otherwise.
     * @see        java.awt.List#select
     * @see        java.awt.List#deselect
     * @since      JDK1.1
     */
    public boolean isIndexSelected(int index) {
	return isSelected(index);
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>isIndexSelected(int)</code>.
     */
    public boolean isSelected(int index) {
	int sel[] = getSelectedIndexes();
	for (int i = 0 ; i < sel.length ; i++) {
	    if (sel[i] == index) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Get the number of visible lines in this list.
     * @return     the number of visible lines in this scrolling list.
     */
    public int getRows() {
	return rows;
    }

    /**
     * Determines whether this list allows multiple selections.
     * @return     <code>true</code> if this list allows multiple
     *                 selections; otherwise, <code>false</code>.
     * @see        java.awt.List#setMultipleMode
     * @since      JDK1.1
     */
    public boolean isMultipleMode() {
	return allowsMultipleSelections();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>isMultipleMode()</code>.
     */
    public boolean allowsMultipleSelections() {
	return multipleMode;
    }

    /**
     * Sets the flag that determines whether this list
     * allows multiple selections.
     * @param       b   if <code>true</code> then multiple selections
     *                      are allowed; otherwise, only one item from
     *                      the list can be selected at once.
     * @see         java.awt.List#isMultipleMode
     * @since       JDK1.1
     */
    public void setMultipleMode(boolean b) {
    	setMultipleSelections(b);
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setMultipleMode(boolean)</code>.
     */
    public synchronized void setMultipleSelections(boolean b) {
	if (b != multipleMode) {
	    multipleMode = b;
	    ListPeer peer = (ListPeer)this.peer;
	    if (peer != null) {
		peer.setMultipleSelections(b);
	    }
	}
    }

    /**
     * Gets the index of the item that was last made visible by
     * the method <code>makeVisible</code>.
     * @return      the index of the item that was last made visible.
     * @see         java.awt.List#makeVisible
     */
    public int getVisibleIndex() {
	return visibleIndex;
    }

    /**
     * Makes the item at the specified index visible.
     * @param       index    the position of the item.
     * @see         java.awt.List#getVisibleIndex
     */
    public synchronized void makeVisible(int index) {
	visibleIndex = index;
	ListPeer peer = (ListPeer)this.peer;
	if (peer != null) {
	    peer.makeVisible(index);
	}
    }

    /**
     * Gets the preferred dimensions for a list with the specified
     * number of rows.
     * @param      rows    number of rows in the list.
     * @return     the preferred dimensions for displaying this scrolling list
     *             given that the specified number of rows must be visible.
     * @see        java.awt.Component#getPreferredSize
     * @since      JDK1.1
     */
    public Dimension getPreferredSize(int rows) {
	return preferredSize(rows);
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getPreferredSize(int)</code>.
     */
    public Dimension preferredSize(int rows) {
        synchronized (getTreeLock()) {
	    ListPeer peer = (ListPeer)this.peer;
	    return (peer != null) ?
		       peer.preferredSize(rows) :
		       super.preferredSize();
        }
    }

    /**
     * Gets the preferred size of this scrolling list.
     * @return     the preferred dimensions for displaying this scrolling list.
     * @see        java.awt.Component#getPreferredSize
     * @since      JDK1.1
     */
    public Dimension getPreferredSize() {
	return preferredSize();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getPreferredSize()</code>.
     */
    public Dimension preferredSize() {
        synchronized (getTreeLock()) {
	    return (rows > 0) ?
		       preferredSize(rows) :
		       super.preferredSize();
        }
    }

    /**
     * Gets the minumum dimensions for a list with the specified
     * number of rows.
     * @param      rows    number of rows in the list.
     * @return     the minimum dimensions for displaying this scrolling list
     *             given that the specified number of rows must be visible.
     * @see        java.awt.Component#getMinimumSize
     * @since      JDK1.1
     */
    public Dimension getMinimumSize(int rows) {
	return minimumSize(rows);
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getMinimumSize(int)</code>.
     */
    public Dimension minimumSize(int rows) {
        synchronized (getTreeLock()) {
	    ListPeer peer = (ListPeer)this.peer;
	    return (peer != null) ?
		       peer.minimumSize(rows) :
		       super.minimumSize();
        }
    }

    /**
     * Determines the minimum size of this scrolling list.
     * @return       the minimum dimensions needed
     *                        to display this scrolling list.
     * @see          java.awt.Component#getMinimumSize()
     * @since        JDK1.1
     */
    public Dimension getMinimumSize() {
	return minimumSize();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getMinimumSize()</code>.
     */
    public Dimension minimumSize() {
        synchronized (getTreeLock()) {
	    return (rows > 0) ? minimumSize(rows) : super.minimumSize();
        }
    }

    /**
     * Adds the specified item listener to receive item events from
     * this list.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param         l the item listener.
     * @see           java.awt.event.ItemEvent
     * @see           java.awt.event.ItemListener
     * @see           java.awt.List#removeItemListener
     * @since         JDK1.1
     */
    public synchronized void addItemListener(ItemListener l) {
	if (l == null) {
	    return;
	}
        itemListener = AWTEventMulticaster.add(itemListener, l);
        newEventsOnly = true;
    }

    /**
     * Removes the specified item listener so that it no longer
     * receives item events from this list.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param         	l the item listener.
     * @see           	java.awt.event.ItemEvent
     * @see           	java.awt.event.ItemListener
     * @see           	java.awt.List#addItemListener
     * @since         	JDK1.1
     */
    public synchronized void removeItemListener(ItemListener l) {
	if (l == null) {
	    return;
	}
        itemListener = AWTEventMulticaster.remove(itemListener, l);
    }

    /**
     * Adds the specified action listener to receive action events from
     * this list. Action events occur when a user double-clicks
     * on a list item.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param         l the action listener.
     * @see           java.awt.event.ActionEvent
     * @see           java.awt.event.ActionListener
     * @see           java.awt.List#removeActionListener
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
     * receives action events from this list. Action events
     * occur when a user double-clicks on a list item.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param         	l     the action listener.
     * @see           	java.awt.event.ActionEvent
     * @see           	java.awt.event.ActionListener
     * @see           	java.awt.List#addActionListener
     * @since         	JDK1.1
     */
    public synchronized void removeActionListener(ActionListener l) {
	if (l == null) {
	    return;
	}
	actionListener = AWTEventMulticaster.remove(actionListener, l);
    }

    /**
     * Return an array of all the listeners that were added to the List
     * with addXXXListener(), where XXX is the name of the <code>listenerType</code>
     * argument.  For example, to get all of the ItemListener(s) for the
     * given List <code>l</code>, one would write:
     * <pre>
     * ItemListener[] ils = (ItemListener[])(l.getListeners(ItemListener.class))
     * </pre>
     * If no such listener list exists, then an empty array is returned.
     * 
     * @param    listenerType   Type of listeners requested
     * @return   all of the listeners of the specified type supported by this list
     * @since 1.3
     */
    public EventListener[] getListeners(Class listenerType) { 
	EventListener l = null; 
	if  (listenerType == ActionListener.class) { 
	    l = actionListener;
	} else if  (listenerType == ItemListener.class) { 
	    l = itemListener;
	} else {
	    return super.getListeners(listenerType);
	}
	return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        switch(e.id) {
          case ActionEvent.ACTION_PERFORMED:
            if ((eventMask & AWTEvent.ACTION_EVENT_MASK) != 0 ||
                actionListener != null) {
                return true;
            }
            return false;
          case ItemEvent.ITEM_STATE_CHANGED:
            if ((eventMask & AWTEvent.ITEM_EVENT_MASK) != 0 ||
                itemListener != null) {
                return true;
            }
            return false;
          default:
            break;
        }
        return super.eventEnabled(e);
    }

    /**
     * Processes events on this scrolling list. If an event is
     * an instance of <code>ItemEvent</code>, it invokes the
     * <code>processItemEvent</code> method. Else, if the
     * event is an instance of <code>ActionEvent</code>,
     * it invokes <code>processActionEvent</code>.
     * If the event is not an item event or an action event,
     * it invokes <code>processEvent</code> on the superclass.
     * @param        e the event.
     * @see          java.awt.event.ActionEvent
     * @see          java.awt.event.ItemEvent
     * @see          java.awt.List#processActionEvent
     * @see          java.awt.List#processItemEvent
     * @since        JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ItemEvent) {
            processItemEvent((ItemEvent)e);
            return;
        } else if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
            return;
        }
	super.processEvent(e);
    }

    /**
     * Processes item events occurring on this list by
     * dispatching them to any registered
     * <code>ItemListener</code> objects.
     * <p>
     * This method is not called unless item events are
     * enabled for this component. Item events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>An <code>ItemListener</code> object is registered
     * via <code>addItemListener</code>.
     * <li>Item events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the item event.
     * @see         java.awt.event.ItemEvent
     * @see         java.awt.event.ItemListener
     * @see         java.awt.List#addItemListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processItemEvent(ItemEvent e) {
        if (itemListener != null) {
            itemListener.itemStateChanged(e);
        }
    }

    /**
     * Processes action events occurring on this component
     * by dispatching them to any registered
     * <code>ActionListener</code> objects.
     * <p>
     * This method is not called unless action events are
     * enabled for this component. Action events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>An <code>ActionListener</code> object is registered
     * via <code>addActionListener</code>.
     * <li>Action events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the action event.
     * @see         java.awt.event.ActionEvent
     * @see         java.awt.event.ActionListener
     * @see         java.awt.List#addActionListener
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
     * scrolling list. This string is useful for debugging.
     * @return    the parameter string of this scrolling list.
     */
    protected String paramString() {
	return super.paramString() + ",selected=" + getSelectedItem();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * Not for public use in the future.
     * This method is expected to be retained only as a package
     * private method.
     */
    public synchronized void delItems(int start, int end) {
	for (int i = end; i >= start; i--) {
	    items.removeElementAt(i);
	}
	ListPeer peer = (ListPeer)this.peer;
	if (peer != null) {
	    peer.delItems(start, end);
	}
    }

    /*
     * Serialization support.  Since the value of the selected
     * field isn't neccessarily up to date we sync it up with the
     * peer before serializing.
     */

    /**
     * The List Components Serialized Data Version.
     *
     * @serial
     */
    private int listSerializedDataVersion = 1;

    /**
     * Writes default serializable fields to stream.  Writes
     * a list of serializable ItemListener(s) as optional data.
     * The non-serializable ItemListner(s) are detected and
     * no attempt is made to serialize them.
     *
     * @serialData Null terminated sequence of 0 or more pairs.
     *             The pair consists of a String and Object.
     *             The String indicates the type of object and
     *             is one of the following :
     *             itemListenerK indicating and ItemListener object.
     *
     * @see AWTEventMulticaster.save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component.itemListenerK
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
      synchronized (this) {
	ListPeer peer = (ListPeer)this.peer;
	if (peer != null) {
	  selected = peer.getSelectedIndexes();
	}
      }
      s.defaultWriteObject();

      AWTEventMulticaster.save(s, itemListenerK, itemListener);
      AWTEventMulticaster.save(s, actionListenerK, actionListener);
      s.writeObject(null);
    }

    /**
     * Read the ObjectInputStream and if it isnt null
     * add a listener to receive item events fired
     * by the List.
     * Unrecognised keys or values will be Ignored.
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

	if (itemListenerK == key)
	  addItemListener((ItemListener)(s.readObject()));

	else if (actionListenerK == key)
	  addActionListener((ActionListener)(s.readObject()));

	else // skip value for unrecognized key
	  s.readObject();
      }
    }


/////////////////
// Accessibility support
////////////////


    /**
     * Gets the AccessibleContext associated with this List. 
     * For lists, the AccessibleContext takes the form of an 
     * AccessibleAWTList. 
     * A new AccessibleAWTList instance is created if necessary.
     *
     * @return an AccessibleAWTList that serves as the 
     *         AccessibleContext of this List
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTList();
        }
        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the 
     * <code>List</code> class.  It provides an implementation of the 
     * Java Accessibility API appropriate to list user-interface elements.
     */
    protected class AccessibleAWTList extends AccessibleAWTComponent
        implements AccessibleSelection, ItemListener, ActionListener {

	public AccessibleAWTList() {
	    super();
            List.this.addActionListener(this);
            List.this.addItemListener(this);
	}

        public void actionPerformed(ActionEvent event)  {
        }

        public void itemStateChanged(ItemEvent event)  {
        }

        /**
         * Get the state set of this object.
         *
         * @return an instance of AccessibleState containing the current state
         * of the object
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (List.this.isMultipleMode())  {
                states.add(AccessibleState.MULTISELECTABLE);
            }
            return states;
        }

        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the 
	 * object
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.LIST;
        }

        /**
         * Returns the Accessible child contained at the local coordinate
         * Point, if one exists.
         *
         * @return the Accessible at the specified location, if it exists
         */
        public Accessible getAccessibleAt(Point p) {
            return null; // fredxFIXME Not implemented yet
        }

        /**
         * Returns the number of accessible children in the object.  If all
         * of the children of this object implement Accessible, than this
         * method should return the number of children of this object.
         *
         * @return the number of accessible children in the object.
         */
        public int getAccessibleChildrenCount() {
            return List.this.getItemCount();
        }

        /**
         * Return the nth Accessible child of the object.
         *
         * @param i zero-based index of child
         * @return the nth Accessible child of the object
         */
        public Accessible getAccessibleChild(int i) {
            synchronized(List.this)  {
                if (i >= List.this.getItemCount()) {
                    return null;
                } else {
                    return new AccessibleAWTListChild(List.this, i);
                }
            }
        }

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

    // AccessibleSelection methods

        /**
         * Returns the number of items currently selected.
         * If no items are selected, the return value will be 0.
         *
         * @return the number of items currently selected.
         */
         public int getAccessibleSelectionCount() {
             return List.this.getSelectedIndexes().length;
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
             synchronized(List.this)  {
                 int len = getAccessibleSelectionCount();
                 if (i <= 0 || i >= len) {
                     return null;
                 } else {
                     return getAccessibleChild(List.this.getSelectedIndexes()[i]);
                 }
             }
         }

        /**
         * Returns true if the current child of this object is selected.
         *
         * @param i the zero-based index of the child in this Accessible
         * object.
         * @see AccessibleContext#getAccessibleChild
         */
        public boolean isAccessibleChildSelected(int i) {
            return List.this.isIndexSelected(i);
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
             List.this.select(i);
         }

        /**
         * Removes the specified selected item in the object from the object's
         * selection.  If the specified item isn't currently selected, this
         * method has no effect.
         *
         * @param i the zero-based index of selectable items
         */
         public void removeAccessibleSelection(int i) {
             List.this.deselect(i);
         }

        /**
         * Clears the selection in the object, so that nothing in the
         * object is selected.
         */
         public void clearAccessibleSelection() {
             synchronized(List.this)  {
                 int selectedIndexes[] = List.this.getSelectedIndexes();
                 if (selectedIndexes == null)
                     return;
                 for (int i = selectedIndexes.length; i >= 0; i--) {
                     List.this.deselect(selectedIndexes[i]);
                 }
             }
         }

        /**
         * Causes every selected item in the object to be selected
         * if the object supports multiple selections.
         */
         public void selectAllAccessibleSelection() {
             synchronized(List.this)  {
                 for (int i = List.this.getItemCount() - 1; i >= 0; i--) {
                     List.this.select(i);
                 }
             }
         }

       /**
        * This class implements accessibility support for 
        * List children.  It provides an implementation of the 
        * Java Accessibility API appropriate to list children 
	* user-interface elements.
        */
        protected class AccessibleAWTListChild extends AccessibleAWTComponent
	implements Accessible {  

	
	// [[[FIXME]]] need to finish implementing this!!!

            private List parent;
            private int  indexInParent;

            public AccessibleAWTListChild(List parent, int indexInParent)  {
                this.parent = parent;
		this.setAccessibleParent(parent);
                this.indexInParent = indexInParent;
            }

	    //
	    // required Accessible methods
	    //
	  /**
	   * Gets the AccessibleContext for this object.  In the
           * implementation of the Java Accessibility API for this class, 
	   * return this object, which acts as its own AccessibleContext.
	   * 
	   * @return this object
	   */
	    public AccessibleContext getAccessibleContext() {
		return this;
	    }

	    //
	    // required AccessibleContext methods
	    //

	    /**
	     * Get the role of this object.  
	     *
	     * @return an instance of AccessibleRole describing the role of 
	     * the object
	     * @see AccessibleRole
	     */
	    public AccessibleRole getAccessibleRole() {
		return AccessibleRole.LIST_ITEM;
	    }

	    /**
	     * Get the state set of this object.  The AccessibleStateSet of an 
	     * object is composed of a set of unique AccessibleState's.  A 
	     * change in the AccessibleStateSet of an object will cause a 
	     * PropertyChangeEvent to be fired for the 
	     * ACCESSIBLE_STATE_PROPERTY property.
	     *
	     * @return an instance of AccessibleStateSet containing the
	     * current state set of the object
	     * @see AccessibleStateSet
	     * @see AccessibleState
	     * @see #addPropertyChangeListener
	     */
	    public AccessibleStateSet getAccessibleStateSet() {
		AccessibleStateSet states = super.getAccessibleStateSet();
		if (parent.isIndexSelected(indexInParent)) {
		    states.add(AccessibleState.SELECTED);
		}
		return states;
	    }

	    /**
	     * Gets the locale of the component. If the component does not 
	     * have a locale, then the locale of its parent is returned.
	     *
	     * @return This component's locale.  If this component does not have
	     * a locale, the locale of its parent is returned.
	     *
	     * @exception IllegalComponentStateException
	     * If the Component does not have its own locale and has not yet 
	     * been added to a containment hierarchy such that the locale can
	     * be determined from the containing parent.
	     */
	    public Locale getLocale() {
		return parent.getLocale();
	    }

	    /**
	     * Get the 0-based index of this object in its accessible parent.
	     *
	     * @return the 0-based index of this object in its parent; -1 if 
	     * this object does not have an accessible parent.
	     *
	     * @see #getAccessibleParent
	     * @see #getAccessibleChildrenCount
	     * @see #getAccessibleChild
	     */
	    public int getAccessibleIndexInParent() {
		return indexInParent;
	    }

	    /**
	     * Returns the number of accessible children of the object.
	     *
	     * @return the number of accessible children of the object.
	     */
	    public int getAccessibleChildrenCount() {
		return 0;	// list elements can't have children
	    }

	    /**
	     * Return the specified Accessible child of the object.  The 
	     * Accessible children of an Accessible object are zero-based, 
	     * so the first child of an Accessible child is at index 0, the 
	     * second child is at index 1, and so on.
	     *
	     * @param i zero-based index of child
	     * @return the Accessible child of the object
	     * @see #getAccessibleChildrenCount
	     */
	    public Accessible getAccessibleChild(int i) {
		return null;	// list elements can't have children
	    }


	    //
	    // AccessibleComponent delegatation to parent List
	    //
	    
	    /**
	     * Get the background color of this object.
	     *
	     * @return the background color, if supported, of the object; 
	     * otherwise, null
	     * @see #setBackground
	     */
	    public Color getBackground() {
		return parent.getBackground();
	    }

	    /**
	     * Set the background color of this object.
	     *
	     * @param c the new Color for the background
	     * @see #setBackground
	     */
	    public void setBackground(Color c) {
		parent.setBackground(c);
	    }

	    /**
	     * Get the foreground color of this object.
	     *
	     * @return the foreground color, if supported, of the object; 
	     * otherwise, null
	     * @see #setForeground
	     */
	    public Color getForeground() {
		return parent.getForeground();
	    }

	    /**
	     * Set the foreground color of this object.
	     *
	     * @param c the new Color for the foreground
	     * @see #getForeground
	     */
	    public void setForeground(Color c) {
		parent.setForeground(c);
	    }

	    /**
	     * Get the Cursor of this object.
	     *
	     * @return the Cursor, if supported, of the object; otherwise, null
	     * @see #setCursor
	     */
	    public Cursor getCursor() {
		return parent.getCursor();
	    }

	    /**
	     * Set the Cursor of this object.
	     *
	     * @param c the new Cursor for the object
	     * @see #getCursor
	     */
	    public void setCursor(Cursor cursor) {
		parent.setCursor(cursor);
	    }

	    /**
	     * Get the Font of this object.
	     *
	     * @return the Font,if supported, for the object; otherwise, null
	     * @see #setFont
	     */
	    public Font getFont() {
		return parent.getFont();
	    }

	    /**
	     * Set the Font of this object.
	     *
	     * @param f the new Font for the object
	     * @see #getFont
	     */
	    public void setFont(Font f) {
		parent.setFont(f);
	    }

	    /**
	     * Get the FontMetrics of this object.
	     *
	     * @param f the Font
	     * @return the FontMetrics, if supported, the object; otherwise, null
	     * @see #getFont
	     */
	    public FontMetrics getFontMetrics(Font f) {
		return parent.getFontMetrics(f);
	    }

	    /**
	     * Determine if the object is enabled.  Objects that are enabled
	     * will also have the AccessibleState.ENABLED state set in their
	     * AccessibleStateSet.
	     *
	     * @return true if object is enabled; otherwise, false
	     * @see #setEnabled
	     * @see AccessibleContext#getAccessibleStateSet
	     * @see AccessibleState#ENABLED
	     * @see AccessibleStateSet
	     */
	    public boolean isEnabled() {
		return parent.isEnabled();
	    }

	    /**
	     * Set the enabled state of the object.
	     *
	     * @param b if true, enables this object; otherwise, disables it 
	     * @see #isEnabled
	     */
	    public void setEnabled(boolean b) {
		parent.setEnabled(b);
	    }

	    /**
	     * Determine if the object is visible.  Note: this means that the
	     * object intends to be visible; however, it may not be
	     * showing on the screen because one of the objects that this object
	     * is contained by is currently not visible.  To determine if an 
	     * object is showing on the screen, use isShowing().
	     * <p>Objects that are visible will also have the 
	     * AccessibleState.VISIBLE state set in their AccessibleStateSet.
	     *
	     * @return true if object is visible; otherwise, false
	     * @see #setVisible
	     * @see AccessibleContext#getAccessibleStateSet
	     * @see AccessibleState#VISIBLE
	     * @see AccessibleStateSet
	     */
	    public boolean isVisible() {
		// [[[FIXME]]] needs to work like isShowing() below
		return false;
		// return parent.isVisible();
	    }

	    /**
	     * Set the visible state of the object.
	     *
	     * @param b if true, shows this object; otherwise, hides it 
	     * @see #isVisible
	     */
	    public void setVisible(boolean b) {
		// [[[FIXME]]] should scroll to item to make it show!
		parent.setVisible(b);
	    }

	    /**
	     * Determine if the object is showing.  This is determined by 
	     * checking the visibility of the object and visibility of the 
	     * object ancestors.
	     * Note: this will return true even if the object is obscured 
	     * by another (for example, it to object is underneath a menu 
	     * that was pulled down).
	     *
	     * @return true if object is showing; otherwise, false
	     */
	    public boolean isShowing() {
		// [[[FIXME]]] only if it's showing!!!
		return false;
		// return parent.isShowing();
	    }

	    /** 
	     * Checks whether the specified point is within this object's 
	     * bounds, where the point's x and y coordinates are defined to 
	     * be relative to the coordinate system of the object. 
	     *
	     * @param p the Point relative to the coordinate system of the 
	     * object
	     * @return true if object contains Point; otherwise false
	     * @see #getBounds
	     */
	    public boolean contains(Point p) {
		// [[[FIXME]]] - only if p is within the list element!!!
		return false;
		// return parent.contains(p);
	    }

	    /** 
	     * Returns the location of the object on the screen.
	     *
	     * @return location of object on screen; null if this object
	     * is not on the screen
	     * @see #getBounds
	     * @see #getLocation
	     */
	    public Point getLocationOnScreen() {
		// [[[FIXME]]] sigh
		return null;
	    }

	    /** 
	     * Gets the location of the object relative to the parent in the 
	     * form of a point specifying the object's top-left corner in the 
	     * screen's coordinate space.
	     *
	     * @return An instance of Point representing the top-left corner of
	     * the objects's bounds in the coordinate space of the screen; null
	     * if this object or its parent are not on the screen
	     * @see #getBounds
	     * @see #getLocationOnScreen
	     */
	    public Point getLocation() {
		// [[[FIXME]]]
		return null;
	    }

	    /** 
	     * Sets the location of the object relative to the parent.
	     * @param p the new position for the top-left corner
	     * @see #getLocation
	     */
	    public void setLocation(Point p) {
		// [[[FIXME]]] maybe - can simply return as no-op
	    }

	    /** 
	     * Gets the bounds of this object in the form of a Rectangle object. 
	     * The bounds specify this object's width, height, and location
	     * relative to its parent. 
	     *
	     * @return A rectangle indicating this component's bounds; null if 
	     * this object is not on the screen.
	     * @see #contains
	     */
	    public Rectangle getBounds() {
		// [[[FIXME]]]
		return null;
	    }

	    /** 
	     * Sets the bounds of this object in the form of a Rectangle 
	     * object.  The bounds specify this object's width, height, and 
	     * location relative to its parent.
	     *	
	     * @param r rectangle indicating this component's bounds
	     * @see #getBounds
	     */
	    public void setBounds(Rectangle r) {
		// no-op; not supported
	    }

	    /** 
	     * Returns the size of this object in the form of a Dimension 
	     * object.  The height field of the Dimension object contains this 
	     * objects's height, and the width field of the Dimension object 
	     * contains this object's width. 
	     *
	     * @return A Dimension object that indicates the size of this 
	     * component; null if this object is not on the screen
	     * @see #setSize
	     */
	    public Dimension getSize() {
		// [[[FIXME]]]
		return null;
	    }

	    /** 
	     * Resizes this object so that it has width and height. 
	     *	
	     * @param d - The dimension specifying the new size of the object. 
	     * @see #getSize
	     */
	    public void setSize(Dimension d) {
		// not supported; no-op
	    }

	    /**
	     * Returns the Accessible child, if one exists, contained at the 
	     * local coordinate Point.
	     *
	     * @param p The point relative to the coordinate system of this 
	     * object.
	     * @return the Accessible, if it exists, at the specified location; 
	     * otherwise null
	     */
	    public Accessible getAccessibleAt(Point p) {
		return null;	// object cannot have children!
	    }

	    /**
	     * Returns whether this object can accept focus or not.   Objects 
	     * that can accept focus will also have the 
	     * AccessibleState.FOCUSABLE state set in their AccessibleStateSet.
	     *
	     * @return true if object can accept focus; otherwise false
	     * @see AccessibleContext#getAccessibleStateSet
	     * @see AccessibleState#FOCUSABLE
	     * @see AccessibleState#FOCUSED
	     * @see AccessibleStateSet
	     */
	    public boolean isFocusTraversable() {
		return false;	// list element cannot receive focus!
	    }

	    /**
	     * Requests focus for this object.  If this object cannot accept 
	     * focus, nothing will happen.  Otherwise, the object will attempt 
	     * to take focus.
	     * @see #isFocusTraversable
	     */
	    public void requestFocus() {
		// nothing to do; a no-op
	    }

	    /**
	     * Adds the specified focus listener to receive focus events from 
	     * this component. 
	     *
	     * @param l the focus listener
	     * @see #removeFocusListener
	     */
	    public void addFocusListener(FocusListener l) {
		// nothing to do; a no-op
	    }

	    /**
	     * Removes the specified focus listener so it no longer receives 
	     * focus events from this component.
	     *
	     * @param l the focus listener
	     * @see #addFocusListener
	     */
	    public void removeFocusListener(FocusListener l) {
		// nothing to do; a no-op
	    }



        } // inner class AccessibleAWTListChild

    } // inner class AccessibleAWTList

}
