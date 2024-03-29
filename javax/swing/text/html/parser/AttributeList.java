/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.text.html.parser;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

/**
 * This class defines the attributes of an SGML element
 * as described in a DTD using the ATTLIST construct.
 * An AttributeList can be obtained from the Element
 * class using the getAttributes() method.
 * <p>
 * It is actually an element in a linked list. Use the
 * getNext() method repeatedly to enumerate all the attributes
 * of an element.
 *
 * @see		Element
 * @author      Arthur Van Hoff
 * @version 	1.7 02/06/02
 *
 */
public final
class AttributeList implements DTDConstants, Serializable {
    public String name;
    public int type;
    public Vector values;
    public int modifier;
    public String value;
    public AttributeList next;

    AttributeList() {
    }

    /**
     * Create an attribute list element.
     */
    public AttributeList(String name) {
	this.name = name;
    }

    /**
     * Create an attribute list element.
     */
    public AttributeList(String name, int type, int modifier, String value, Vector values, AttributeList next) {
	this.name = name;
	this.type = type;
	this.modifier = modifier;
	this.value = value;
	this.values = values;
	this.next = next;
    }

    /**
     * @return attribute name
     */
    public String getName() {
	return name;
    }

    /**
     * @return attribute type
     * @see DTDConstants
     */
    public int getType() {
	return type;
    }

    /**
     * @return attribute modifer
     * @see DTDConstants
     */
    public int getModifier() {
	return modifier;
    }

    /**
     * @return possible attribute values
     */
    public Enumeration getValues() {
	return (values != null) ? values.elements() : null;
    }

    /**
     * @return default attribute value
     */
    public String getValue() {
	return value;
    }

    /**
     * @return the next attribute in the list
     */
    public AttributeList getNext() {
	return next;
    }

    /**
     * @return string representation
     */
    public String toString() {
	return name;
    }

    /**
     * Create a hashtable of attribute types.
     */
    static Hashtable attributeTypes = new Hashtable();

    static void defineAttributeType(String nm, int val) {
	Integer num = new Integer(val);
	attributeTypes.put(nm, num);
	attributeTypes.put(num, nm);
    }

    static {
	defineAttributeType("CDATA", CDATA);
	defineAttributeType("ENTITY", ENTITY);
	defineAttributeType("ENTITIES", ENTITIES);
	defineAttributeType("ID", ID);
	defineAttributeType("IDREF", IDREF);
	defineAttributeType("IDREFS", IDREFS);
	defineAttributeType("NAME", NAME);
	defineAttributeType("NAMES", NAMES);
	defineAttributeType("NMTOKEN", NMTOKEN);
	defineAttributeType("NMTOKENS", NMTOKENS);
	defineAttributeType("NOTATION", NOTATION);
	defineAttributeType("NUMBER", NUMBER);
	defineAttributeType("NUMBERS", NUMBERS);
	defineAttributeType("NUTOKEN", NUTOKEN);
	defineAttributeType("NUTOKENS", NUTOKENS);

	attributeTypes.put("fixed", new Integer(FIXED));
	attributeTypes.put("required", new Integer(REQUIRED));
	attributeTypes.put("current", new Integer(CURRENT));
	attributeTypes.put("conref", new Integer(CONREF));
	attributeTypes.put("implied", new Integer(IMPLIED));
    }

    public static int name2type(String nm) {
	Integer i = (Integer)attributeTypes.get(nm);
	return (i == null) ? CDATA : i.intValue();
    }

    public static String type2name(int tp) {
	return (String)attributeTypes.get(new Integer(tp));
    }
}
