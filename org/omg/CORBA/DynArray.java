/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package org.omg.CORBA;


/** The DynArray interface represents a DynAny object which is associated
 *  with an array.
 */

public interface DynArray extends org.omg.CORBA.Object, org.omg.CORBA.DynAny
{
    /**
     * Returns the value of all the elements of this array.
     *
     * @return the array of <code>Any</code> objects that is the value
	 *         for this <code>DynArray</code> object
	 * @see #set_elements
     */
    public org.omg.CORBA.Any[] get_elements();

    /**
     * Sets the value of this
     * <code>DynArray</code> object to the given array.
     *
     * @param value the array of <code>Any</code> objects
     * @exception InvalidSeq if the sequence is bad
	 * @see #get_elements
     */
    public void set_elements(org.omg.CORBA.Any[] value)
        throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
