/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;

/**
 * The CORBA <code>INVALID_TRANSACTION</code> exception, which is thrown
 * when there is a transaction error.
 * It contains a minor code, which gives more detailed information about
 * what caused the exception, and a completion status. It may also contain
 * a string describing the exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @version 1.5, 09/09/97
 * @since   JDK1.2
 *
 */

public final class INVALID_TRANSACTION extends SystemException {
    /**
     * Constructs an <code>INVALID_TRANSACTION</code> exception with a default minor code
     * of 0 and a completion state of COMPLETED_MAYBE.
     */
    public INVALID_TRANSACTION() {
	this("");
    }

    /**
     * Constructs an <code>INVALID_TRANSACTION</code> exception
     * with the specified detail message.
     * @param s the String containing a detail message
     */
    public INVALID_TRANSACTION(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs an <code>INVALID_TRANSACTION</code> exception with the specified
     * minor code and completion status.
     * @param minor the minor code
     * @param completed the completion status
     */
    public INVALID_TRANSACTION(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs an <code>INVALID_TRANSACTION</code> exception with the specified detail
     * message, minor code, and completion status.
     * A detail message is a String that describes this particular exception.
     * @param s the String containing a detail message
     * @param minor the minor code
     * @param completed the completion status
     */
    public INVALID_TRANSACTION(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
