/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt;

/**
 * Signals that an AWT component is not in an appropriate state for
 * the requested operation.
 *
 * @version 	1.10, 02/06/02
 * @author	Jonni Kanerva
 */
public class IllegalComponentStateException extends IllegalStateException {
    /*
     * JDK 1.1 serialVersionUID 
     */
     private static final long serialVersionUID = -1889339587208144238L;

    /**
     * Constructs an IllegalComponentStateException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public IllegalComponentStateException() {
	super();
    }

    /**
     * Constructs an IllegalComponentStateException with the specified detail
     * message.  A detail message is a String that describes this particular
     * exception.
     * @param s the String that contains a detailed message
     */
    public IllegalComponentStateException(String s) {
	super(s);
    }
}
