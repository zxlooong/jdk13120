/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.beans.beancontext;

import java.beans.DesignMode;
import java.beans.Visibility;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.util.Collection;
import java.util.Locale;

/**
 * <p>
 * The BeanContext acts a logical hierarchical container for JavaBeans.
 * </p>
 *
 * @author Laurence P. G. Cable
 * @version 1.20, 02/06/02
 * @since 1.2
 *
 * @seealso java.beans.Beans
 * @seealso java.beans.beancontext.BeanContextChild
 * @seealso java.beans.beancontext.BeanContextMembershipListener
 * @seealso java.beans.PropertyChangeEvent
 * @seealso java.beans.VetoableChangeEvent
 * @seealso java.beans.DesignMode
 * @seealso java.beans.Visibility
 * @seealso java.util.Collection
 */

public interface BeanContext extends BeanContextChild, Collection, DesignMode, Visibility {

    /**
     * Instantiate the javaBean named as a 
     * child of this <code>BeanContext</code>.
     * The implementation of the JavaBean is 
     * derived from the value of the beanName parameter, 
     * and is defined by the 
     * <code>java.beans.Beans.instantiate()</code> method.
     *
     * @param beanName The name of the JavaBean to instantiate 
     * as a child of this <code>BeanContext</code>
     * @throws <code>IOException</code>
     * @throws <code>ClassNotFoundException</code> if the class identified
     * by the beanName parameter is not found
     */
    Object instantiateChild(String beanName) throws IOException, ClassNotFoundException;

    /**
     * Analagous to <code>java.lang.ClassLoader.getResourceAsStream()</code>, 
     * this method allows a <code>BeanContext</code> implementation 
     * to interpose behavior between the child <code>Component</code> 
     * and underlying <code>ClassLoader</code>.
     * 
     * @param name the resource name
     * @param bcc the specified child
     * @return an <code>InputStream</code> for reading the resource, 
     * or <code>null</code> if the resource could not
     * be found.
     * @throws <code>IllegalArgumentException</code> if 
     * the resource is not valid
     */
    InputStream getResourceAsStream(String name, BeanContextChild bcc) throws IllegalArgumentException;

    /**
     * Analagous to <code>java.lang.ClassLoader.getResource()</code>, this
     * method allows a <code>BeanContext</code> implementation to interpose
     * behavior between the child <code>Component</code> 
     * and underlying <code>ClassLoader</code>.
     * 
     * @param name the resource name
     * @param bcc the specified child
     * @return a <code>URL</code> for the named 
     * resource for the specified child
     * @throws <code>IllegalArgumentException</code> 
     * if the resource is not valid
     */
    URL getResource(String name, BeanContextChild bcc) throws IllegalArgumentException;

     /**
      * Adds the specified <code>BeanContextMembershipListener</code> 
      * to receive <code>BeanContextMembershipEvents</code> from 
      * this <code>BeanContext</code> whenever it adds
      * or removes a child <code>Component</code>(s).
      * 
      * @param bcml the <code>BeanContextMembershipListener</code> to be added
      */
    void addBeanContextMembershipListener(BeanContextMembershipListener bcml);

     /**
      * Removes the specified <code>BeanContextMembershipListener</code> 
      * so that it no longer receives <code>BeanContextMembershipEvent</code>s 
      * when the child <code>Component</code>(s) are added or removed.
      * 
      * @param bcml the <code>BeanContextMembershipListener</code> 
      * to be removed
      */
    void removeBeanContextMembershipListener(BeanContextMembershipListener bcml);

    /**
     * This global lock is used by both <code>BeanContext</code> 
     * and <code>BeanContextServices</code> implementors 
     * to serialize changes in a <code>BeanContext</code> 
     * hierarchy and any service requests etc.
     */
    public static final Object globalHierarchyLock = new Object();
}
