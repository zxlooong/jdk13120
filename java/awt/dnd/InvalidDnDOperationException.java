/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.dnd;

/**
 * This exception is thrown by various methods in the java.awt.dnd package.
 * It is usually thrown to indicate that the target in question is unable
 * to undertake the requested operation that the present time, since the
 * undrelying DnD system is not in the appropriate state.
 *
 * @version 	1.10, 02/06/02
 * @since 1.2
 */

public class InvalidDnDOperationException extends IllegalStateException {
    
    static private String dft_msg = "The operation requested cannot be performed by the DnD system since it is not in the appropriate state";

    /**
     * Create a default Exception
     */

    public InvalidDnDOperationException() { super(dft_msg); }

    /**
     * Create an Exception with its own descriptive message
     * <P>
     * @param msg the detail message
     */

    public InvalidDnDOperationException(String msg) { super(msg); }

}



