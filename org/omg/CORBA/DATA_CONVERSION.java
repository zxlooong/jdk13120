/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;

/**
 * The CORBA <code>DATA_CONVERSION</code> exception, which is thrown
 * when there is a data conversion error.
 * It contains a minor code, which gives more detailed information about
 * what caused the exception, and a completion status. It may also contain
 * a string describing the exception.
 * <P>
 * See the section <A href="../../../../guide/idl/jidlExceptions.html#minorcodemeanings">meaning
 * of minor codes</A> to see the minor codes for this exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @version     1.16, 09/09/97
 * @since       JDK1.2
 */

public final class DATA_CONVERSION extends SystemException {

    /**
     * Constructs a <code>DATA_CONVERSION</code> exception with a default minor code
     * of 0 and a completion state of COMPLETED_NO.
     */
    public DATA_CONVERSION() {
        this("");
    }

    /**
     * Constructs a <code>DATA_CONVERSION</code> exception with the specified detail.
     * @param s the String containing a detail message
     */
    public DATA_CONVERSION(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>DATA_CONVERSION</code> exception with the specified
     * minor code and completion status.
     * @param minor the minor code
     * @param completed the completion status
     */
    public DATA_CONVERSION(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>DATA_CONVERSION</code> exception with the specified detail
     * message, minor code, and completion status.
     * A detail message is a String that describes this particular exception.
     * @param s the String containing a detail message
     * @param minor the minor code
     * @param completed the completion status
     */
    public DATA_CONVERSION(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
