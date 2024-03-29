/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA.ORBPackage;

/**
 * The <code>InvalidName</code> exception is raised when
 * <code>ORB.resolve_initial_references</code> is passed a name
 * for which there is no initial reference.
 *
 * @see org.omg.CORBA.ORB#resolve_initial_references(String)
 * @version 1.6, 03/18/98
 * @since   JDK1.2
 */

public class InvalidName extends org.omg.CORBA.UserException {
    /**
     * Constructs an <code>InvalidName</code> exception with no reason message.
     */
    public InvalidName() {
	super();
    }

    /**
     * Constructs an <code>InvalidName</code> exception with the specified 
     * reason message.
     * @param reason the String containing a reason message
     */
    public InvalidName(String reason) {
	super(reason);
    }
}
