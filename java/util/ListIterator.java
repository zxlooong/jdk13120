/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.util;

/**
 * An iterator for lists that allows the programmer to traverse the
 * list in either direction and modify the list during iteration.
 *
 * @author  Josh Bloch
 * @version 1.17, 02/06/02
 * @see Collection
 * @see List
 * @see Iterator
 * @see Enumeration
 * @since   1.2
 */
public interface ListIterator extends Iterator {
    // Query Operations

    /**
     * Returns <tt>true</tt> if this list iterator has more elements when
     * traversing the list in the forward direction. (In other words, returns
     * <tt>true</tt> if <tt>next</tt> would return an element rather than
     * throwing an exception.)
     *
     * @return <tt>true</tt> if the list iterator has more elements when
     *		traversing the list in the forward direction.
     */
    boolean hasNext();

    /**
     * Returns the next element in the list.  This method may be called
     * repeatedly to iterate through the list, or intermixed with calls to
     * <tt>previous</tt> to go back and forth.  (Note that alternating calls
     * to <tt>next</tt> and <tt>previous</tt> will return the same element
     * repeatedly.)
     *
     * @return the next element in the list.
     * @exception NoSuchElementException if the iteration has no next element.
     */
    Object next();

    /**
     * Returns <tt>true</tt> if this list iterator has more elements when
     * traversing the list in the reverse direction.  (In other words, returns
     * <tt>true</tt> if <tt>previous</tt> would return an element rather than
     * throwing an exception.)
     *
     * @return <tt>true</tt> if the list iterator has more elements when
     *	       traversing the list in the reverse direction.
     */
    boolean hasPrevious();

    /**
     * Returns the previous element in the list.  This method may be called
     * repeatedly to iterate through the list backwards, or intermixed with
     * calls to <tt>next</tt> to go back and forth.  (Note that alternating
     * calls to <tt>next</tt> and <tt>previous</tt> will return the same
     * element repeatedly.)
     *
     * @return the previous element in the list.
     * 
     * @exception NoSuchElementException if the iteration has no previous
     *            element.
     */
    Object previous();

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <tt>next</tt>. (Returns list size if the list iterator is at the
     * end of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     * 	       call to <tt>next</tt>, or list size if list iterator is at end
     *	       of list. 
     */
    int nextIndex();

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <tt>previous</tt>. (Returns -1 if the list iterator is at the
     * beginning of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     * 	       call to <tt>previous</tt>, or -1 if list iterator is at
     *	       beginning of list.
     */ 
    int previousIndex();


    // Modification Operations
    
    /**
     * Removes from the list the last element that was returned by
     * <tt>next</tt> or <tt>previous</tt> (optional operation).  This call can
     * only be made once per call to <tt>next</tt> or <tt>previous</tt>.  It
     * can be made only if <tt>ListIterator.add</tt> has not been called after
     * the last call to <tt>next</tt> or <tt>previous</tt>.
     *
     * @exception UnsupportedOperationException if the <tt>remove</tt>
     * 		  operation is not supported by this list iterator.
     * @exception IllegalStateException neither <tt>next</tt> nor
     *		  <tt>previous</tt> have been called, or <tt>remove</tt> or
     *		  <tt>add</tt> have been called after the last call to *
     *		  <tt>next</tt> or <tt>previous</tt>.
     */
    void remove();

    /**
     * Replaces the last element returned by <tt>next</tt> or
     * <tt>previous</tt> with the specified element (optional operation).
     * This call can be made only if neither <tt>ListIterator.remove</tt> nor
     * <tt>ListIterator.add</tt> have been called after the last call to
     * <tt>next</tt> or <tt>previous</tt>.
     *
     * @param o the element with which to replace the last element returned by
     *          <tt>next</tt> or <tt>previous</tt>.
     * @exception UnsupportedOperationException if the <tt>set</tt> operation
     * 		  is not supported by this list iterator.
     * @exception ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @exception IllegalArgumentException if some aspect of the specified
     *		  element prevents it from being added to this list.
     * @exception IllegalStateException if neither <tt>next</tt> nor
     *	          <tt>previous</tt> have been called, or <tt>remove</tt> or
     *		  <tt>add</tt> have been called after the last call to
     * 		  <tt>next</tt> or <tt>previous</tt>.
     */
    void set(Object o);

    /**
     * Inserts the specified element into the list (optional operation).  The
     * element is inserted immediately before the next element that would be
     * returned by <tt>next</tt>, if any, and after the next element that
     * would be returned by <tt>previous</tt>, if any.  (If the list contains
     * no elements, the new element becomes the sole element on the list.)
     * The new element is inserted before the implicit cursor: a subsequent
     * call to <tt>next</tt> would be unaffected, and a subsequent call to
     * <tt>previous</tt> would return the new element.  (This call increases
     * by one the value that would be returned by a call to <tt>nextIndex</tt>
     * or <tt>previousIndex</tt>.)
     *
     * @param o the element to insert.
     * @exception UnsupportedOperationException if the <tt>add</tt> method is
     * 		  not supported by this list iterator.
     * 
     * @exception ClassCastException if the class of the specified element
     * 		  prevents it from being added to this Set.
     * 
     * @exception IllegalArgumentException if some aspect of this element
     *            prevents it from being added to this Collection.
     */
    void add(Object o);
}
