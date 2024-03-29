/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;

/**
 * A container (holder) for an exception that is used in <code>Request</code>
 * operations to make exceptions available to the client.  An
 * <code>Environment</code> object is created with the <code>ORB</code>
 * method <code>create_environment</code>.
 *
 * @version 1.11, 09/09/97
 * @since   JDK1.2
 */

public abstract class Environment {

    /**
     * Retrieves the exception in this <code>Environment</code> object.
     *
     * @return			the exception in this <code>Environment</code> object
     */

    public abstract java.lang.Exception exception();

    /**
     * Inserts the given exception into this <code>Environment</code> object.
     *
     * @param except		the exception to be set
     */

    public abstract void exception(java.lang.Exception except);

    /**
     * Clears this <code>Environment</code> object of its exception.
     */

    public abstract void clear();

}
