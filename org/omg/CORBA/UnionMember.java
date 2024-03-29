/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * File: ./org/omg/CORBA/UnionMember.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package org.omg.CORBA;

/**
 * A description in the Interface Repository of a member of an IDL union.
 */
public final class UnionMember implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables

    /**
     * The name of the union member described by this
     * <code>UnionMember</code> object.
     * @serial
     */
    public String name;

    /**
     * The label of the union member described by this
     * <code>UnionMember</code> object.
     * @serial
     */
    public org.omg.CORBA.Any label;

    /**
     * The type of the union member described by this
     * <code>UnionMember</code> object.
     * @serial
     */
    public org.omg.CORBA.TypeCode type;

    /**
     * The typedef that represents the IDL type of the union member described by this
     * <code>UnionMember</code> object.
     * @serial
     */
    public org.omg.CORBA.IDLType type_def;

    //	constructors

    /**
     * Constructs a new <code>UnionMember</code> object with its fields initialized
     * to null.
     */
    public UnionMember() { }

    /**
     * Constructs a new <code>UnionMember</code> object with its fields initialized
     * to the given values.
     */
    public UnionMember(String __name, org.omg.CORBA.Any __label, org.omg.CORBA.TypeCode __type, org.omg.CORBA.IDLType __type_def) {
	name = __name;
	label = __label;
	type = __type;
	type_def = __type_def;
    }
}
