/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.lang;
import java.lang.ref.*;

/**
 * This class provides ThreadLocal variables.  These variables differ from
 * their normal counterparts in that each thread that accesses one (via its
 * get or set method) has its own, independently initialized copy of the
 * variable.  ThreadLocal objects are typically private static variables in
 * classes that wish to associate state with a thread (e.g., a user ID or
 * Transaction ID).
 * 
 * <p>Each thread holds an implicit reference to its copy of a ThreadLocal
 * as long as the thread is alive and the ThreadLocal object is accessible;
 * after a thread goes away, all of its copies of ThreadLocal variables are
 * subject to garbage collection (unless other references to these copies
 * exist).
 *
 * @author  Josh Bloch and Doug Lea
 * @version 1.18, 05/20/02
 * @since   1.2
 */

public class ThreadLocal {
    /**
     * ThreadLocals rely on per-thread hash maps attached to each thread
     * (Thread.threadLocals and inheritableThreadLocals).  The ThreadLocal
     * objects act as keys, searched via threadLocalHashCode.  This is a
     * custom hash code (useful only within ThreadLocalMaps) that eliminates
     * collisions in the common case where consecutively constructed
     * ThreadLocals are used by the same threads, while remaining well-behaved
     * in less common cases.
     */
    private final int threadLocalHashCode = nextHashCode();

    /**
     * The next hash code to be given out. Accessed only by like-named method.
     */
    private static int nextHashCode = 0;

    /**
     * The difference between successively generated hash codes - turns
     * implicit sequential thread-local IDs into near-optimally spread
     * multiplicative hash values for power-of-two-sized tables.
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * Compute the next hash code. The static synchronization used here
     * should not be a performance bottleneck. When ThreadLocals are
     * generated in different threads at a fast enough rate to regularly
     * contend on this lock, memory contention is by far a more serious
     * problem than lock contention.
     */
    private static synchronized int nextHashCode() {
        int h = nextHashCode;
        nextHashCode = h + HASH_INCREMENT;
        return h;
    }

    /**
     * Returns the calling thread's initial value for this ThreadLocal
     * variable. This method will be called once per accessing thread for
     * each ThreadLocal, the first time each thread accesses the variable
     * with get or set.  If the programmer desires ThreadLocal variables
     * to be initialized to some value other than null, ThreadLocal must
     * be subclassed, and this method overridden.  Typically, an anonymous
     * inner class will be used.  Typical implementations of initialValue
     * will call an appropriate constructor and return the newly constructed
     * object.
     *
     * @return the initial value for this ThreadLocal
     */
    protected Object initialValue() {
	return null;
    }

    /**
     * Returns the value in the calling thread's copy of this ThreadLocal
     * variable.  Creates and initializes the copy if this is the first time
     * the thread has called this method.
     *
     * @return the current thread's value of this ThreadLocal
     */
    public Object get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            return map.get(this);

        // Maps are constructed lazily.  if the map for this thread
        // doesn't exist, create it, with this ThreadLocal and its
        // initial value as its only entry.
        Object value = initialValue();
        createMap(t, value);
        return value;
    }

    /**
     * Sets the calling thread's instance of this ThreadLocal variable
     * to the given value.  This is only used to change the value from
     * the one assigned by the initialValue method, and many applications
     * will have no need for this functionality.
     *
     * @param value the value to be stored in the calling threads' copy of
     *	      this ThreadLocal.
     */
    public void set(Object value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }

    /**
     * Get the map associated with a ThreadLocal. Overridden in
     * InheritableThreadLocal.
     *
     * @param  t the current thread
     * @return the map
     */
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    /**
     * Create the map associated with a ThreadLocal. Overridden in
     * InheritableThreadLocal.
     *
     * @param t the current thread
     * @param firstValue value for the initial entry of the map
     * @param map the map to store.
     */
    void createMap(Thread t, Object firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    /**
     * Factory method to create map of inherited thread locals.
     * Designed to be called only from Thread constructor.
     *
     * @param  parentMap the map associated with parent thread
     * @return a map containing the parent's inheritable bindings
     */
    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    /**
     * Method childValue is visibly defined in subclass
     * InheritableThreadLocal, but is internally defined here for the
     * sake of providing createInheritedMap factory method without
     * needing to subclass the map class in InheritableThreadLocal.
     * This technique is preferable to the alternative of embedding
     * instanceof tests in methods.
     */
    Object childValue(Object parentValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * ThreadLocalMap is a customized hash map suitable only for
     * maintaining thread local values. No operations are exported
     * outside of the ThreadLocal class. The class is package private to
     * allow declaration of fields in class Thread.  To help deal with
     * very large and long-lived usages, the hash table entries use
     * WeakReferences for keys. However, since reference queues are not
     * used, stale entries are guaranteed to be removed only when
     * the table starts running out of space.
     */
    static class ThreadLocalMap {

        /**
         * The entries in this hash map extend WeakReference, using
         * its main ref field as the key (which is always a
         * ThreadLocal object).  Note that null keys (i.e. entry.get()
         * == null) mean that the key is no longer referenced, so the
         * entry can be expunged from table.  Such entries are referred to
         * as "stale entries" in the code that follows.
         */ 
        private static class Entry extends WeakReference {
            /** The value associated with this ThreadLocal. */
            private Object value;

            private Entry(ThreadLocal k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * The initial capacity -- MUST be a power of two.
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * The table, resized as necessary. 
         * table.length MUST always be a power of two.
         */
        private Entry[] table;

        /**
         * The number of entries in the table.  
         */
        private int size = 0;

        /**
         * The next size value at which to resize. 
         */
        private int threshold;

        /**
         * Set the resize threshold to maintain at worst a 2/3 load factor.
         */
        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        /**
         * Increment i modulo len.
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        /**
         * Decrement i modulo len.
         */
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

        /**
         * Construct a new map initially containing (firstKey, firstValue).
         * ThreadLocalMaps are constructed lazily, so we only create
         * one when we have at least one entry to put in it.
         */
        ThreadLocalMap(ThreadLocal firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        /**
         * Construct a new map including all Inheritable ThreadLocals
         * from given parent map. Called only by createInheritedMap.
         *
         * @param parentMap the map associated with parent thread.
         */
        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];
      
            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    Object k = e.get();
                    if (k != null) {
                        ThreadLocal key = (ThreadLocal)(k);
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);  
                        while (table[h] != null) 
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        /**
         * Get the value associated with key with code h.  This method itself
         * handles only the fast path: a direct hit of existing key. It
         * otherwise relays to getAfterMiss.  This is designed to maximize
         * performance for direct hits, in part by making this method readily
         * inlinable.
         *
         * @param  key the thread local object
         * @param  h key's  hash code
         * @return the value associated with key
         */
        private Object get(ThreadLocal key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            if (e != null && e.get() == key)
                return e.value;

            return getAfterMiss(key, i, e);
        }

        /**
         * Version of get method for use when key is not found in its
         * direct hash slot.
         *
         * @param  key the thread local object
         * @param  i the table index for key's hash code
         * @param  e the entry at table[i];
         * @return the value associated with key
         */
        private Object getAfterMiss(ThreadLocal key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                Object k = e.get();
                if (k == key) 
                    return e.value;
                if (k == null) 
                    return replaceStaleEntry(key, null, i, true);

                i = nextIndex(i, len);
                e = tab[i];
            }

            Object value = key.initialValue();
            tab[i] = new Entry(key, value);
            if (++size >= threshold) 
                rehash();

            return value;
        }

        /**
         * Set the value associated with key.
         *
         * @param key the thread local object
         * @param value the value to be set
         */
        private void set(ThreadLocal key, Object value) {

            // We don't use a fast path as with get() because it is at 
            // least as common to use set() to create new entries as 
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);

            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                Object k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i, false);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            if (++size >= threshold) 
                rehash();
        }

        /**
         * Remove the entry for key.

           THIS IS USED ONLY BY ThreadLocal.remove, WHICH IS NOT CURRENTLY
           PART OF THE PUBLIC API.  IF IT IS ADDED TO THE PUBLIC API AT SOME
           POINT, THIS METHOD MUST BE UNCOMMENTED.

        private void remove(ThreadLocal key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        */

        /**
         * Replace a stale entry encountered during a get or set operation
         * with an entry for the specified key.  If actAsGet is true and an
         * entry for the key already exists, the value in the entry is
         * unchanged; if no entry exists for the key, the value in the new
         * entry is obtained by calling key.initialValue.  If actAsGet is
         * false, the value passed in the value parameter is stored in the
         * entry, whether or not an entry already exists for the specified key.
         *
         * As a side effect, this method expunges all stale entries in the
         * "run" containing the stale entry.  (A run is a sequence of entries
         * between two null slots.)
         *
         * @param  key the key
         * @param  value the value to be associated with key; meaningful only
         *         if actAsGet is false.
         * @param  staleSlot index of the first stale entry encountered while
         *         searching for key.
         * @param  actAsGet true if this method should act as a get; false
         *         it should act as a set.
         * @return the value associated with key after the operation completes.
         */
        private Object replaceStaleEntry(ThreadLocal key, Object value, 
                                         int staleSlot, boolean actAsGet) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            // Back up to check for prior stale entry in current run.
            // We clean out whole runs at a time to avoid continual
            // incremental rehashing due to garbage collector freeing
            // up refs in bunches (i.e., whenever the collector runs).
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len); (e = tab[i]) != null;
                 i = prevIndex(i, len)) 
                if (e.get() == null) 
                    slotToExpunge = i;

            // Find either the key or trailing null slot of run, whichever
            // occurs first
            for (int i = nextIndex(staleSlot, len); (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                Object k = e.get();

                // If we find key, then we need to swap it
                // with the stale entry to maintain hash table order.
                // The newly stale slot, or any other stale slot
                // encountered above it, can then be sent to expungeStaleEntry
                // to remove or rehash all of the other entries in run.
                if (k == key) {
                    if (actAsGet) 
                        value = e.value;
                    else
                        e.value = value;

                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;

                    // Start expunge at preceding stale entry if it exists
                    if (slotToExpunge == staleSlot) 
                        slotToExpunge = i;
                    expungeStaleEntry(slotToExpunge);
                    return value;
                }
                
                // If we didn't find stale entry on backward scan, the
                // the first stale entry seen while scanning for key is the
                // first still present in the run.
                if (k == null && slotToExpunge == staleSlot) 
                    slotToExpunge = i; 
            }

            // If key not found, put new entry in stale slot
            if (actAsGet)
                value = key.initialValue();
            tab[staleSlot].value = null;   // Help the GC
            tab[staleSlot] = new Entry(key, value);

            // If there are any other stale entries in run, expunge them
            if (slotToExpunge != staleSlot) 
                expungeStaleEntry(slotToExpunge);

            return value;
        }

        /**
         * Expunge a stale entry by rehashing any possibly colliding entries
         * lying between staleSlot and the next null slot.  This also expunges
         * any other stale entries encountered before the trailing null.  See
         * Knuth, Section 6.4
         *
         * @param staleSlot index of slot known to have null key
         */
        private void expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // expunge entry at staleSlot
            tab[staleSlot].value = null;   // Help the GC
            tab[staleSlot] = null;
            size--;

            // Rehash until we encounter null
            Entry e;
            for (int i = nextIndex(staleSlot, len); (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                Object k = e.get();
                if (k == null) {
                    e.value = null;   // Help the GC
                    tab[i] = null;
                    size--;
                } else {
                    ThreadLocal key = (ThreadLocal)(k);
                    int h = key.threadLocalHashCode & (len - 1);  
                    if (h != i) {
                        tab[i] = null;

                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        while (tab[h] != null) 
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
        }

        /**
         * Re-pack and/or re-size the table. First scan the entire
         * table removing stale entries. If this doesn't sufficiently
         * shrink the size of the table, double the table size.
         */
        private void rehash() {
            expungeStaleEntries();

            // Use lower threshold for doubling to avoid hysteresis
            if (size >= threshold - threshold / 4) 
                resize();
        }

        /**
         * Double the capacity of the table.
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                oldTab[j] = null; // Help the GC
                if (e != null) {
                    Object k = e.get();
                    if (k == null) {
                        e.value = null; // Help the GC
                    } else {
                        ThreadLocal key = (ThreadLocal)(k);
                        int h = key.threadLocalHashCode & (newLen - 1);  
                        while (newTab[h] != null) 
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        /**
         * Expunge all stale entries in the table.
         */
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null) 
                    expungeStaleEntry(j);
            }
        }
    }
}
