/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;


/**
 * A class that contains information about the identity of
 * the client, for access control
 * and other purposes. It contains a single attribute, the name of the
 * <code>Principal</code>, encoded as a sequence of bytes.
 * <P>
 * @deprecated Deprecated by CORBA 2.2.
 */

public abstract class Principal {

    /**
     * Sets the name of this <code>Principal</code> object to the given value.
     * @param value the value to be set in the <code>Principal</code>
     * @deprecated Deprecated by CORBA 2.2.
     */
    public abstract void name(byte[] value);

    /**
     * Gets the name of this <code>Principal</code> object.
     * @return the name of this <code>Principal</code> object
     * @deprecated Deprecated by CORBA 2.2.
     */
    public abstract byte[] name();
}
