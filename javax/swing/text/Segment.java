/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.text.CharacterIterator;

/**
 * A segment of a character array representing a fragment
 * of text.  It should be treated as immutable even though
 * the array is directly accessable.  This gives fast access
 * to fragments of text without the overhead of copying
 * around characters.  This is effectively an unprotected
 * String.
 * <p>
 * The Segment implements the java.text.CharacterIterator
 * interface to support use with the i18n support without
 * copying text into a string.
 *
 * @author  Timothy Prinzing
 * @version 1.19 02/06/02
 */
public class Segment implements Cloneable, CharacterIterator {

    /**
     * This is the array containing the text of
     * interest.  This array should never be modified;
     * it is available only for efficiency.
     */
    public char[] array;

    /**
     * This is the offset into the array that
     * the desired text begins.
     */
    public int offset;

    /**
     * This is the number of array elements that
     * make up the text of interest.
     */
    public int count;

    /**
     * Creates a new segment.
     */
    public Segment() {
	array = null;
	offset = 0;
	count = 0;
    }

    /**
     * Creates a new segment referring to an existing array.
     *
     * @param array the array to refer to
     * @param offset the offset into the array
     * @param count the number of characters
     */
    public Segment(char[] array, int offset, int count) {
	this.array = array;
	this.offset = offset;
	this.count = count;
    }

    /**
     * Converts a segment into a String.
     *
     * @return the string
     */
    public String toString() {
	if (array != null) {
	    return new String(array, offset, count);
	}
	return new String();
    }

    // --- CharacterIterator methods -------------------------------------

    /**
     * Sets the position to getBeginIndex() and returns the character at that
     * position.
     * @return the first character in the text, or DONE if the text is empty
     * @see #getBeginIndex
     */
    public char first() {
	pos = offset;
	if (count != 0) {
	    return array[pos];
	}
	return DONE;
    }

    /**
     * Sets the position to getEndIndex()-1 (getEndIndex() if the text is empty)
     * and returns the character at that position.
     * @return the last character in the text, or DONE if the text is empty
     * @see #getEndIndex
     */
    public char last() {
	pos = offset + count;
	if (count != 0) {
	    pos -= 1;
	    return array[pos];
	}
	return DONE;
    }

    /**
     * Gets the character at the current position (as returned by getIndex()).
     * @return the character at the current position or DONE if the current
     * position is off the end of the text.
     * @see #getIndex
     */
    public char current() {
        if (count != 0 && pos < offset + count) {
	    return array[pos];
	}
	return DONE;
    }

    /**
     * Increments the iterator's index by one and returns the character
     * at the new index.  If the resulting index is greater or equal
     * to getEndIndex(), the current index is reset to getEndIndex() and
     * a value of DONE is returned.
     * @return the character at the new position or DONE if the new
     * position is off the end of the text range.
     */
    public char next() {
	pos += 1;
	int end = offset + count;
	if (pos >= end) {
	    pos = end;
	    return DONE;
	}
	return current();
    }

    /**
     * Decrements the iterator's index by one and returns the character
     * at the new index. If the current index is getBeginIndex(), the index
     * remains at getBeginIndex() and a value of DONE is returned.
     * @return the character at the new position or DONE if the current
     * position is equal to getBeginIndex().
     */
    public char previous() {
	if (pos == offset) {
	    return DONE;
	}
	pos -= 1;
	return current();
    }

    /**
     * Sets the position to the specified position in the text and returns that
     * character.
     * @param position the position within the text.  Valid values range from
     * getBeginIndex() to getEndIndex().  An IllegalArgumentException is thrown
     * if an invalid value is supplied.
     * @return the character at the specified position or DONE if the specified position is equal to getEndIndex()
     */
    public char setIndex(int position) {
	int end = offset + count;
	if ((position < offset) || (position > end)) {
	    throw new IllegalArgumentException("bad position: " + position);
	}
	pos = position;
	if ((pos != end) && (count != 0)) {
	    return array[pos];
	}
	return DONE;
    }

    /**
     * Returns the start index of the text.
     * @return the index at which the text begins.
     */
    public int getBeginIndex() {
	return offset;
    }

    /**
     * Returns the end index of the text.  This index is the index of the first
     * character following the end of the text.
     * @return the index after the last character in the text
     */
    public int getEndIndex() {
	return offset + count;
    }

    /**
     * Returns the current index.
     * @return the current index.
     */
    public int getIndex() {
	return pos;
    }

    /**
     * Creates a shallow copy.
     *
     * @return the copy
     */
    public Object clone() {
	Object o;
	try {
	    o = super.clone();
	} catch (CloneNotSupportedException cnse) {
	    o = null;
	}
	return o;
    }

    private int pos;


}


