/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * <code>ActionMap</code> provides mappings from
 * <code>Object</code>s
 * (called <em>keys</em> or <em><code>Action</code> names</em>)
 * to <code>Action</code>s.
 * An <code>ActionMap</code> is usually used with an <code>InputMap</code>
 * to locate a particular action
 * when a key is pressed. As with <code>InputMap</code>,
 * an <code>ActionMap</code> can have a parent 
 * that is searched for keys not defined in the <code>ActionMap</code>.
 * <p>As with <code>InputMap</code> if you create a cycle, eg:
 * <pre>
 *   ActionMap am = new ActionMap();
 *   ActionMap bm = new ActionMap():
 *   am.setParent(bm);
 *   bm.setParent(am);
 * </pre>
 * some of the methods will cause a StackOverflowError to be thrown.
 *
 * @version 1.9 02/06/02
 * @author Scott Violet
 */
public class ActionMap implements Serializable {
    /** Handles the mapping between Action name and Action. */
    private transient AbstractAction.ArrayTable     arrayTable;
    /** Parent that handles any bindings we don't contain. */
    private ActionMap                               parent;


    /**
     * Creates an <code>ActionMap</code> with no parent and no mappings.
     */
    public ActionMap() {
    }

    /**
     * Sets this <code>ActionMap</code>'s parent.
     * 
     * @param map  the <code>ActionMap</code> that is the parent of this one
     */
    public void setParent(ActionMap map) {
	this.parent = map;
    }

    /**
     * Returns this <code>ActionMap</code>'s parent.
     * 
     * @return the <code>ActionMap</code> that is the parent of this one,
     * 	       or null if this <code>ActionMap</code> has no parent
     */
    public ActionMap getParent() {
	return parent;
    }

    /**
     * Adds a binding for <code>key</code> to <code>action</code>.
     * If <code>action</code> is null, this removes the current binding
     * for <code>key</code>.
     * <p>In most instances, <code>key</code> will be
     * <code>action.getValue(NAME)</code>.
     */
    public void put(Object key, Action action) {
	if (key == null) {
	    return;
	}
	if (action == null) {
	    remove(key);
	}
	else {
	    if (arrayTable == null) {
		arrayTable = new AbstractAction.ArrayTable();
	    }
	    arrayTable.put(key, action);
	}
    }

    /**
     * Returns the binding for <code>key</code>, messaging the 
     * parent <code>ActionMap</code> if the binding is not locally defined.
     */
    public Action get(Object key) {
	Action value = (arrayTable == null) ? null :
	               (Action)arrayTable.get(key);

	if (value == null) {
	    ActionMap    parent = getParent();

	    if (parent != null) {
		return parent.get(key);
	    }
	}
	return value;
    }

    /**
     * Removes the binding for <code>key</code> from this <code>ActionMap</code>.
     */
    public void remove(Object key) {
	if (arrayTable != null) {
	    arrayTable.remove(key);
	}
    }

    /**
     * Removes all the mappings from this <code>ActionMap</code>.
     */
    public void clear() {
	if (arrayTable != null) {
	    arrayTable.clear();
	}
    }

    /**
     * Returns the <code>Action</code> names that are bound in this <code>ActionMap</code>.
     */
    public Object[] keys() {
	if (arrayTable == null) {
	    return null;
	}
	return arrayTable.getKeys(null);
    }

    /**
     * Returns the number of <code>KeyStroke</code> bindings.
     */
    public int size() {
	if (arrayTable == null) {
	    return 0;
	}
	return arrayTable.size();
    }

    /**
     * Returns an array of the keys defined in this <code>ActionMap</code> and
     * its parent. This method differs from <code>keys()</code> in that
     * this method includes the keys defined in the parent.
     */
    public Object[] allKeys() {
	int           count = size();
	ActionMap     parent = getParent();

	if (count == 0) {
	    if (parent != null) {
		return parent.allKeys();
	    }
	    return keys();
	}
	if (parent == null) {
	    return keys();
	}
	Object[]    keys = keys();
	Object[]    pKeys =  parent.allKeys();

	if (pKeys == null) {
	    return keys;
	}
	if (keys == null) {
	    // Should only happen if size() != keys.length, which should only
	    // happen if mutated from multiple threads (or a bogus subclass).
	    return pKeys;
	}

	HashMap        keyMap = new HashMap();
	int            counter;

	for (counter = keys.length - 1; counter >= 0; counter--) {
	    keyMap.put(keys[counter], keys[counter]);
	}
	for (counter = pKeys.length - 1; counter >= 0; counter--) {
	    keyMap.put(pKeys[counter], pKeys[counter]);
	}
	return keyMap.keySet().toArray();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
	Object[] keys = keys();
	// Determine valid count
	int validCount = 0;
	if (keys != null) {
	    for(int counter = 0; counter < keys.length; counter++) {
		if (keys[counter] instanceof Serializable) {
		    Object value = get(keys[counter]);
		    if (value instanceof Serializable) {
			validCount++;
		    }
		    else {
			keys[counter] = null;
		    }
		}
		else {
		    keys[counter] = null;
		}
	    }
	}
	s.writeInt(validCount);
	int counter = 0;
	while (validCount > 0) {
	    if (keys[counter] != null) {
		s.writeObject(keys[counter]);
		s.writeObject(get(keys[counter]));
		validCount--;
	    }
	    counter++;
	}
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException,
	                                         IOException {
        s.defaultReadObject();
	for (int counter = s.readInt() - 1; counter >= 0; counter--) {
	    put(s.readObject(), (Action)s.readObject());
	}
    }
}
