/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;

/**
 * The CORBA <code>TRANSACTION_ROLLEDBACK</code> exception, which is thrown
 * when a transactional operation did not complete
 * because the transaction was rolled back. See the OMG Transaction
 * Service specification for details.
 * It contains a minor code, which gives more detailed information about
 * what caused the exception, and a completion status. It may also contain
 * a string describing the exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @version     1.5 09/09/97
 */

public final class TRANSACTION_ROLLEDBACK extends SystemException {
    /**
     * Constructs a <code>TRANSACTION_ROLLEDBACK</code> exception with a default minor code
     * of 0, a completion state of CompletionStatus.COMPLETED_NO,
     * and a null description.
     */
    public TRANSACTION_ROLLEDBACK() {
	this("");
    }

    /**
     * Constructs a <code>TRANSACTION_ROLLEDBACK</code> exception with the
     * specified description message,
     * a minor code of 0, and a completion state of COMPLETED_NO.
     * @param s the String containing a detail message
     */
    public TRANSACTION_ROLLEDBACK(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>TRANSACTION_ROLLEDBACK</code> exception with the specified
     * minor code and completion status.
     * @param minor the minor code
     * @param completed the completion status
     */
    public TRANSACTION_ROLLEDBACK(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>TRANSACTION_ROLLEDBACK</code> exception with the
     * specified description message, minor code, and completion status.
     * @param s the String containing a description message
     * @param minor the minor code
     * @param completed the completion status
     */
    public TRANSACTION_ROLLEDBACK(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
