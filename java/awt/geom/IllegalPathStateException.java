/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.geom;

/**
 * The <code>IllegalPathStateException</code> represents an 
 * exception that is thrown if an operation is performed on a path 
 * that is in an illegal state with respect to the particular
 * operation being performed, such as appending a path segment 
 * to a {@link GeneralPath} without an initial moveto.
 *
 * @version 	1.10, 02/06/02
 */

public class IllegalPathStateException extends RuntimeException {
    /**
     * Constructs an <code>IllegalPathStateException</code> with no
     * detail message.
     *
     * @since   1.2
     */
    public IllegalPathStateException() {
    }

    /**
     * Constructs an <code>IllegalPathStateException</code> with the
     * specified detail message. 
     * @param   s   the detail message
     * @since   1.2
     */
    public IllegalPathStateException(String s) {
        super (s);
    }
}
