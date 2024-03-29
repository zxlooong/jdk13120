/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;

/**
 * The root class for CORBA IDL-defined user exceptions.
 * All CORBA user exceptions are checked exceptions, which
 * means that they need to
 * be declared in method signatures.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @version	1.28 09/09/97
 */
public abstract class UserException extends java.lang.Exception implements org.omg.CORBA.portable.IDLEntity {

    /**
     * Constructs a <code>UserException</code> object.
     * This method is called only by subclasses.
     */
    protected UserException() {
	super();
    }

    /**
     * Constructs a <code>UserException</code> object with a
     * detail message. This method is called only by subclasses.
     */
    protected UserException(String reason) {
	super(reason);
    }
}

