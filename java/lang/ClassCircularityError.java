/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.lang;

/**
 * Thrown when a circularity has been detected while initializing a class.
 *
 * @author     unascribed
 * @version    1.13, 02/06/02
 * @since      JDK1.0
 */
public class ClassCircularityError extends LinkageError {
    /**
     * Constructs a <code>ClassCircularityError</code> with no detail  message.
     */
    public ClassCircularityError() {
	super();
    }

    /**
     * Constructs a <code>ClassCircularityError</code> with the 
     * specified detail message. 
     *
     * @param      s   the detail message.
     */
    public ClassCircularityError(String s) {
	super(s);
    }
}
