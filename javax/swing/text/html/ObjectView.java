/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import java.util.Enumeration;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.beans.*;
import java.lang.reflect.*;

/**
 * Component decorator that implements the view interface 
 * for &lt;object&gt; elements.
 * <p>
 * This view will try to load the class specified by the
 * <code>classid</code> attribute.  If possible, the Classloader
 * used to load the associated Document is used.
 * This would typically be the same as the ClassLoader
 * used to load the EditorKit.  If the documents
 * ClassLoader is null, <code>Class.forName</code> is used.
 * <p>
 * If the class can successfully be loaded, an attempt will
 * be made to create an instance of it by calling
 * <code>Class.newInstance</code>.  An attempt will be made
 * to narrow the instance to type <code>java.awt.Component</code>
 * to display the object.
 * <p>
 * This view can also manage a set of parameters with limitations.
 * The parameters to the &lt;object&gt; element are expected to
 * be present on the associated elements attribute set as simple
 * strings.  Each bean property will be queried as a key on
 * the AttributeSet, with the expectation that a non-null value
 * (of type String) will be present if there was a parameter
 * specification for the property.  Reflection is used to 
 * set the parameter.  Currently, this is limited to a very
 * simple single parameter of type String.
 * <p>
 * A simple example html invocation is:
 * <pre><code>

       <object classid="javax.swing.JLabel">
       <param name="text" value="sample text">
       </object>

 * </code></pre>
 *
 * @author Timothy Prinzing
 * @version 1.8 02/06/02
 */
public class ObjectView extends ComponentView  {

    /**
     * Creates a new ObjectView object.
     *
     * @param elem the element to decorate
     */
    public ObjectView(Element elem) {
	super(elem);
    }

    /**
     * Create the component.  The classid is used
     * as a specification of the classname, which
     * we try to load.
     */
    protected Component createComponent() {
	AttributeSet attr = getElement().getAttributes();
	String classname = (String) attr.getAttribute(HTML.Attribute.CLASSID);
	try {
	    Class c = getClass(classname);
	    Object o = c.newInstance();
	    if (o instanceof Component) {
		Component comp = (Component) o;
		setParameters(comp, attr);
		return comp;
	    }
	} catch (Throwable e) {
	    // couldn't create a component... fall through to the 
	    // couldn't load representation.
	}
	
	return getUnloadableRepresentation();
    }

    /**
     * Fetch a component that can be used to represent the
     * object if it can't be created.
     */
    Component getUnloadableRepresentation() {
	// PENDING(prinz) get some artwork and return something
	// interesting here.
	Component comp = new JLabel("??");
	comp.setForeground(Color.red);
	return comp;
    }

    /**
     * Get a Class object to use for loading the 
     * classid.  If possible, the Classloader
     * used to load the associated Document is used.
     * This would typically be the same as the ClassLoader
     * used to load the EditorKit.  If the documents
     * ClassLoader is null,
     * <code>Class.forName</code> is used.
     */
    private Class getClass(String classname) throws ClassNotFoundException {
	Class klass;

	Class docClass = getDocument().getClass();
	ClassLoader loader = docClass.getClassLoader();
	if (loader != null) {
	    klass = loader.loadClass(classname);
	} else {
	    klass = Class.forName(classname);
	}
	return klass;
    }

    /**
     * Initialize this component according the KEY/VALUEs passed in
     * via the &lt;param&gt; elements in the corresponding 
     * &lt;object&gt; element.
     */
    private void setParameters(Component comp, AttributeSet attr) {
	Class k = comp.getClass();
	BeanInfo bi;
	try {
	    bi = Introspector.getBeanInfo(k);
	} catch (IntrospectionException ex) {
	    System.err.println("introspector failed, ex: "+ex);
	    return;		// quit for now
	}
	PropertyDescriptor props[] = bi.getPropertyDescriptors();
	for (int i=0; i < props.length; i++) {
	    //	    System.err.println("checking on props[i]: "+props[i].getName());
	    Object v = attr.getAttribute(props[i].getName());
	    if (v instanceof String) {
		// found a property parameter
		String value = (String) v;
		Method writer = props[i].getWriteMethod();
		if (writer == null) {
		    // read-only property. ignore
		    return;	// for now
		}
		Class[] params = writer.getParameterTypes();
		if (params.length != 1) {
		    // zero or more than one argument, ignore
		    return;	// for now
		}
		String [] args = { value };
		try {
		    writer.invoke(comp, args);
		} catch (Exception ex) {
		    System.err.println("Invocation failed");
		    // invocation code
		}
	    }
	}
    }

}

