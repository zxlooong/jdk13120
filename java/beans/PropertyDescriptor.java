/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.beans;

import java.lang.ref.*;
import java.lang.reflect.*;

/**
 * A PropertyDescriptor describes one property that a Java Bean
 * exports via a pair of accessor methods.
 */

public class PropertyDescriptor extends FeatureDescriptor {

    private Reference propertyTypeRef;
    private Reference readMethodRef;
    private Reference writeMethodRef;
    private Reference propertyEditorClassRef;

    private boolean bound;
    private boolean constrained;

    // The base name of the method name which will be prefixed with the
    // read and write method. If name == "foo" then the baseName is "Foo"
    private String baseName;

    private String writeMethodName;
    private String readMethodName;

    /**
     * Constructs a PropertyDescriptor for a property that follows
     * the standard Java convention by having getFoo and setFoo 
     * accessor methods.  Thus if the argument name is "fred", it will
     * assume that the writer method is "setFred" and the reader method 
     * is "getFred" (or "isFred" for a boolean property).  Note that the
     * property name should start with a lower case character, which will
     * be capitalized in the method names.
     *
     * @param propertyName The programmatic name of the property.
     * @param beanClass The Class object for the target bean.  For
     *		example sun.beans.OurButton.class.
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     */
    public PropertyDescriptor(String propertyName, Class beanClass)
		throws IntrospectionException {
	this(propertyName, beanClass, 
	     "is" + capitalize(propertyName), 
	     "set" + capitalize(propertyName));
    }

    /**
     * This constructor takes the name of a simple property, and method
     * names for reading and writing the property.
     *
     * @param propertyName The programmatic name of the property.
     * @param beanClass The Class object for the target bean.  For
     *		example sun.beans.OurButton.class.
     * @param readMethodName The name of the method used for reading the property
     *           value.  May be null if the property is write-only.
     * @param writeMethodName The name of the method used for writing the property
     *           value.  May be null if the property is read-only.
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     */
    public PropertyDescriptor(String propertyName, Class beanClass,
		String readMethodName, String writeMethodName)
		throws IntrospectionException {
        if (beanClass == null) {
            throw new IntrospectionException("Target Bean class is null");
        }
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name");
        }
        if ("".equals(readMethodName) || "".equals(writeMethodName)) {
            throw new IntrospectionException("read or write method name should not be the empty string");
        }
        setName(propertyName);
        setClass0(beanClass);
	
        this.readMethodName = readMethodName;
        if (readMethodName != null && getReadMethod() == null) {
            throw new IntrospectionException("Method not found: " + readMethodName);
        }
        this.writeMethodName = writeMethodName;
        if (writeMethodName != null && getWriteMethod() == null) {
            throw new IntrospectionException("Method not found: " + writeMethodName);
        }
    }

    /**
     * This constructor takes the name of a simple property, and Method
     * objects for reading and writing the property.
     *
     * @param propertyName The programmatic name of the property.
     * @param readMethod The method used for reading the property value.
     *          May be null if the property is write-only.
     * @param writeMethod The method used for writing the property value.
     *          May be null if the property is read-only.
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     */
    public PropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
		throws IntrospectionException {
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name");
        }
        setName(propertyName);
        setReadMethod(readMethod);
        setWriteMethod(writeMethod);
    }
    
    /**
     * Gets the Class object for the property.
     *
     * @return The Java type info for the property.  Note that
     * the "Class" object may describe a built-in Java type such as "int".
     * The result may be "null" if this is an indexed property that
     * does not support non-indexed access.
     * <p>
     * This is the type that will be returned by the ReadMethod.
     */
    public Class getPropertyType() {
	Class type = getPropertyType0();
        if (type  == null) {
            try {
                type = findPropertyType(getReadMethod(), getWriteMethod());
                setPropertyType(type);
            } catch (IntrospectionException ex) {
                // Fall
            }
        }
        return type;
    }

    private void setPropertyType(Class type) {
        propertyTypeRef = createReference(type);
    }

    private Class getPropertyType0() {
        return (Class)getObject(propertyTypeRef);
    }

    /**
     * Gets the method that should be used to read the property value.
     *
     * @return The method that should be used to read the property value.
     * May return null if the property can't be read.
     */
    public Method getReadMethod() {
       Method readMethod = getReadMethod0();
        if (readMethod == null) {
            Class cls = getClass0();
            if (cls == null || (readMethodName == null && readMethodRef == null)) {
                // The read method was explicitly set to null.
                return null;
            }
            if (readMethodName == null) {
                Class type = getPropertyType0();
                if (type == boolean.class || type == null) {
                    readMethodName = "is" + getBaseName();
                } else {
                    readMethodName = "get" + getBaseName();
                }
            }

            // Since there can be multiple write methods but only one getter
            // method, find the getter method first so that you know what the
            // property type is.  For booleans, there can be "is" and "get"
            // methods.  If an "is" method exists, this is the official
            // reader method so look for this one first.
            try {
		readMethod = Introspector.findMethod(cls, readMethodName, 0);
	    } catch (Exception e) {
		readMethod = null;
	    }
            if (readMethod == null) {
                readMethodName = "get" + getBaseName();
		try {                
		    readMethod = Introspector.findMethod(cls, readMethodName, 0);
		} catch (Exception e) {
		    readMethod = null;
		}
            }
            try {
                setReadMethod(readMethod);
            } catch (IntrospectionException ex) {
                // fall
            }
        }
        return readMethod;
    }

    /**
     * Sets the method that should be used to read the property value.
     *
     * @param getter The new getter method.
     */
    public void setReadMethod(Method readMethod) 
				throws IntrospectionException {
        if (readMethod == null) {
            readMethodName = null;
            readMethodRef = null;
            return;
        }
        // The property type is determined by the read method.
        setPropertyType(findPropertyType(readMethod, getWriteMethod0()));
	setClass0(readMethod.getDeclaringClass());

        readMethodName = readMethod.getName();
        readMethodRef = createReference(readMethod, true);
    }

    /**
     * Gets the method that should be used to write the property value.
     * 
     * @return The method that should be used to write the property value.
     * May return null if the property can't be written.
     */
    public Method getWriteMethod() {
        Method writeMethod = getWriteMethod0();
        if (writeMethod == null) {
            Class cls = getClass0();

            if (cls == null || (writeMethodName == null && writeMethodRef == null)) {
                // The write method was explicitly set to null.
                return null;
            }

            // We need the type to fetch the correct method.
            Class type = getPropertyType0();
            if (type == null) {
                try {
                    // Can't use getPropertyType since it will lead to recursive loop.
                    type = findPropertyType(getReadMethod(), null);
                    setPropertyType(type);
                } catch (IntrospectionException ex) {
                    // Without the correct property type we can't be guaranteed
                    // to find the correct method.
                    return null;
                }
            }

            if (writeMethodName == null) {
                writeMethodName = "set" + getBaseName();
            }
	    
	    try {
		writeMethod = Introspector.findMethod(cls, writeMethodName, 1,
			      (type == null) ? null : new Class[] { type });
	    } catch (Exception e) {
		writeMethod = null;
	    }
            try {
                setWriteMethod(writeMethod);
            } catch (IntrospectionException ex) {
                // fall through
            }
        } 
        return writeMethod;
    }

    /**
     * Sets the method that should be used to write the property value.
     *
     * @param setter The new setter method.
     */
    public void setWriteMethod(Method writeMethod)
				throws IntrospectionException {
        if (writeMethod == null) {
            writeMethodName = null;
            writeMethodRef = null;
            return;
        }
        // Set the property type - which validates the method
        setPropertyType(findPropertyType(getReadMethod(), writeMethod));
	setClass0(writeMethod.getDeclaringClass());

	writeMethodName = writeMethod.getName();
        writeMethodRef = createReference(writeMethod, true);
    }

    private Method getReadMethod0() {
        return (Method)getObject(readMethodRef);
    }

    private Method getWriteMethod0() {
        return (Method)getObject(writeMethodRef);
    }

    /**
     * Overridden to ensure that a super class doesn't take precedent
     */
    void setClass0(Class clz) {
	if (getClass0() != null && clz.isAssignableFrom(getClass0())) {
	    // dont replace a subclass with a superclass
	    return;
	}
	super.setClass0(clz);
    }
 
    /**
     * Updates to "bound" properties will cause a "PropertyChange" event to
     * get fired when the property is changed.
     *
     * @return True if this is a bound property.
     */
    public boolean isBound() {
	return bound;
    }

    /**
     * Updates to "bound" properties will cause a "PropertyChange" event to 
     * get fired when the property is changed.
     *
     * @param bound True if this is a bound property.
     */
    public void setBound(boolean bound) {
	this.bound = bound;
    }

    /**
     * Attempted updates to "Constrained" properties will cause a "VetoableChange"
     * event to get fired when the property is changed.
     *
     * @return True if this is a constrained property.
     */
    public boolean isConstrained() {
	return constrained;
    }

    /**
     * Attempted updates to "Constrained" properties will cause a "VetoableChange"
     * event to get fired when the property is changed.
     *
     * @param constrained True if this is a constrained property.
     */
    public void setConstrained(boolean constrained) {
	this.constrained = constrained;
    }


    /**
     * Normally PropertyEditors will be found using the PropertyEditorManager.
     * However if for some reason you want to associate a particular
     * PropertyEditor with a given property, then you can do it with
     * this method.
     *
     * @param propertyEditorClass  The Class for the desired PropertyEditor.
     */
    public void setPropertyEditorClass(Class propertyEditorClass) {
	if (propertyEditorClass == null) {
	    propertyEditorClassRef = null;
	    return;
	}
	propertyEditorClassRef = new WeakReference(propertyEditorClass);
    }

    /**
     * Gets any explicit PropertyEditor Class that has been registered
     * for this property.
     *
     * @return Any explicit PropertyEditor Class that has been registered
     *		for this property.  Normally this will return "null",
     *		indicating that no special editor has been registered,
     *		so the PropertyEditorManager should be used to locate
     *		a suitable PropertyEditor.
     */
    public Class getPropertyEditorClass() {
	if (propertyEditorClassRef == null) {
	    return null;
	}
	return (Class)propertyEditorClassRef.get();
    }

    /**
     * Package private helper method for Descriptor .equals methods.
     *
     * @param a first method to compare
     * @param b second method to compare
     * @return boolean to indicate that the methods are equivalent
     */
    boolean compareMethods(Method a, Method b) {
        // Note: perhaps this should be a protected method in FeatureDescriptor
        if ((a == null) != (b == null)) {
            return false;
        }
	
        if (a != null && b != null) {
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }
    

   /**
     * Compares this <code>PropertyDescriptor</code> against the specified object.
     * Returns true if the objects are the same. Two <code>PropertyDescriptor</code>s
     * are the same if the read, write, property types, property editor and
     * flags  are equivalent.
     *
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof PropertyDescriptor) {
            PropertyDescriptor other = (PropertyDescriptor)obj;
            Method otherReadMethod = other.getReadMethod();
            Method otherWriteMethod = other.getWriteMethod();

            if (!compareMethods(getReadMethod(), otherReadMethod)) {
                return false;
            }

            if (!compareMethods(getWriteMethod(), otherWriteMethod)) {
                return false;
            }

            if (getPropertyType() == other.getPropertyType() &&
                getPropertyEditorClass() == other.getPropertyEditorClass() &&
                bound == other.isBound() && constrained == other.isConstrained() &&
                writeMethodName == other.writeMethodName &&
                readMethodName == other.readMethodName) {
                return true;
            }
        }
        return false;
    }

    /*
     * Package-private constructor.
     * Merge two property descriptors.  Where they conflict, give the
     * second argument (y) priority over the first argument (x).
     *
     * @param x  The first (lower priority) PropertyDescriptor
     * @param y  The second (higher priority) PropertyDescriptor
     */
    PropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
	super(x,y);

        if (y.baseName != null) {
            baseName = y.baseName;
        } else {
            baseName = x.baseName;
        }

        if (y.readMethodName != null) {
            readMethodName = y.readMethodName;
        } else {
            readMethodName = x.readMethodName;
        }

        if (y.writeMethodName != null) {
            writeMethodName = y.writeMethodName;
        } else {
            writeMethodName = x.writeMethodName;
        }

        if (y.propertyTypeRef != null) {
            propertyTypeRef = y.propertyTypeRef;
        } else {
            propertyTypeRef = x.propertyTypeRef;
        }

	// Figure out the merged read method.
	Method xr = x.getReadMethod();
	Method yr = y.getReadMethod();

	// Normally give priority to y's readMethod.
        try {
            if (yr != null && yr.getDeclaringClass() == getClass0()) {
                setReadMethod(yr);
            } else {
                setReadMethod(xr);
            }
        } catch (IntrospectionException ex) {
            // fall through
        }
           
	// However, if both x and y reference read methods in the same class,
	// give priority to a boolean "is" method over a boolean "get" method.
	if (xr != null && yr != null &&
		   xr.getDeclaringClass() == yr.getDeclaringClass() &&
		   xr.getReturnType() == boolean.class &&
		   yr.getReturnType() == boolean.class && 
		   xr.getName().indexOf("is") == 0 &&
		   yr.getName().indexOf("get") == 0) {
	    try {
                setReadMethod(xr);
            } catch (IntrospectionException ex) {
                // fall through
            }
	}

        Method xw = x.getWriteMethod();
        Method yw = y.getWriteMethod();

       try {
            if (yw != null && yw.getDeclaringClass() == getClass0()) {
                setWriteMethod(yw);
            } else {
                setWriteMethod(xw);
            }
        } catch (IntrospectionException ex) {
            // Fall through
        }

        if (y.getPropertyEditorClass() != null) {
            setPropertyEditorClass(y.getPropertyEditorClass());
        } else {
            setPropertyEditorClass(x.getPropertyEditorClass());
        }


        bound = x.bound | y.bound;
        constrained = x.constrained | y.constrained;
    }

    /*
     * Package-private dup constructor.
     * This must isolate the new object from any changes to the old object.
     */
    PropertyDescriptor(PropertyDescriptor old) {
	super(old);
        propertyTypeRef = old.propertyTypeRef;
        readMethodRef = old.readMethodRef;
        writeMethodRef = old.writeMethodRef;
        propertyEditorClassRef = old.propertyEditorClassRef;

        writeMethodName = old.writeMethodName;
        readMethodName = old.readMethodName;
        baseName = old.baseName;

        bound = old.bound;
        constrained = old.constrained;
    }

    private Class findPropertyType(Method readMethod, Method writeMethod) 
	throws IntrospectionException {
        Class propertyType = null;
        try {
            if (readMethod != null) {
                Class[] params = readMethod.getParameterTypes();
                if (params.length != 0) {
                    throw new IntrospectionException("bad read method arg count: "
                                                     + readMethod);
                }
                propertyType = readMethod.getReturnType();
                if (propertyType == Void.TYPE) {
                    throw new IntrospectionException("read method " +
                                        readMethod.getName() + " returns void");
                }
            }
            if (writeMethod != null) {
                Class params[] = writeMethod.getParameterTypes();
                if (params.length != 1) {
                    throw new IntrospectionException("bad write method arg count: "
                                                     + writeMethod);
                }
                if (propertyType != null && propertyType != params[0]) {
                    throw new IntrospectionException("type mismatch between read and write methods");
                }
                propertyType = params[0];
            }
        } catch (IntrospectionException ex) {
            throw ex;
        }
        return propertyType;
    }

    // Calculate once since capitalize() is expensive.
    String getBaseName() {
        if (baseName == null) {
            baseName = capitalize(getName());
        }
        return baseName;
    }
}
