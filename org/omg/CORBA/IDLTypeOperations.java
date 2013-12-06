/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;


/*
 tempout/org/omg/CORBA/IDLTypeOperations.java
 Generated by the IBM IDL-to-Java compiler, version 1.0
 from ../../Lib/ir.idl
 Thursday, February 25, 1999 2:11:23 o'clock PM PST
*/

/**
 * This interface must be implemented by all IDLType objects.
 * The IDLType is inherited by all IR objects that 
 * represent IDL types, including interfaces, typedefs, and 
 * anonymous types.
 * @see IDLType
 * @see IRObject
 * @see IRObjectOperations
 */

public interface IDLTypeOperations  extends org.omg.CORBA.IRObjectOperations
{
    /**
     * The type attribute describes the type defined by an object
     * derived from <code>IDLType</code>.
     * @return the <code>TypeCode</code> defined by this object.
     */
    org.omg.CORBA.TypeCode type ();
} // interface IDLTypeOperations
