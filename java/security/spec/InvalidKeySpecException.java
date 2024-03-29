/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.security.spec;

import java.security.GeneralSecurityException;

/**
 * This is the exception for invalid key specifications.
 *
 * @author Jan Luehe
 *
 * @version 1.12, 02/06/02
 *
 * @see KeySpec
 *
 * @since 1.2
 */

public class InvalidKeySpecException extends GeneralSecurityException {

    /**
     * Constructs an InvalidKeySpecException with no detail message. A
     * detail message is a String that describes this particular
     * exception.
     */
    public InvalidKeySpecException() {
	super();
    }

    /**
     * Constructs an InvalidKeySpecException with the specified detail
     * message. A detail message is a String that describes this
     * particular exception.  
     *
     * @param msg the detail message.  
     */
    public InvalidKeySpecException(String msg) {
	super(msg);
    }
}
