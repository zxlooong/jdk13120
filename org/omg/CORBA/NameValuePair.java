/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package org.omg.CORBA;

/**
 * The NameValuePair interface associates  a name with a value that is an
 * attribute of an IDL struct.  This interface is used in the DynStruct APIs.
 */

public final class NameValuePair implements org.omg.CORBA.portable.IDLEntity {

    /**
     * The name to be associated with a value by this <code>NameValuePair</code> object.
     */
    public String id;

    /**
     * The value to be associated with a name by this <code>NameValuePair</code> object.
     */
    public org.omg.CORBA.Any value;

    /**
     * Constructs an empty <code>NameValuePair</code> object.
     * To associate a name with a value after using this constructor, the fields
     * of this object have to be accessed individually.
     */
    public NameValuePair() { }
	
    /**
     * Constructs a <code>NameValuePair</code> object that associates
     * the given name with the given <code>org.omg.CORBA.Any</code> object.
     * @param __id the name to be associated with the given <code>Any</code> object
     * @param __value the <code>Any</code> object to be associated with the given name
     */
    public NameValuePair(String __id, org.omg.CORBA.Any __value) {
	id = __id;
	value = __value;
    }
}
