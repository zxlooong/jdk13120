/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.util.zip;

/**
 * Signals that a data format error has occurred.
 *
 * @version 	1.11, 02/06/02
 * @author 	David Connelly
 */
public
class DataFormatException extends Exception {
    /**
     * Constructs a DataFormatException with no detail message.
     */
    public DataFormatException() {
	super();
    }

    /**
     * Constructs a DataFormatException with the specified detail message.
     * A detail message is a String that describes this particular exception.
     * @param s the String containing a detail message
     */
    public DataFormatException(String s) {
	super(s);
    }
}
