/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.beans.beancontext;

import java.util.EventObject;

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * A <code>BeanContextMembershipEvent</code> encapsulates 
 * the list of children added to, or removed from, 
 * the membership of a particular <code>BeanContext</code>. 
 * An instance of this event is fired whenever a successful 
 * add(), remove(), retainAll(), removeAll(), or clear() is 
 * invoked on a given <code>BeanContext</code> instance.
 * Objects interested in receiving events of this type must 
 * implement the <code>BeanContextMembershipListener</code> 
 * interface, and must register their intent via the
 * <code>BeanContext</code>'s 
 * <code>addBeanContextMembershipListener(BeanContextMembershipListener bcml)
 * </code> method. 
 *
 * @author	Laurence P. G. Cable
 * @version	1.12
 * @since	1.2
 * @see		java.beans.beancontext.BeanContext
 * @see		java.beans.beancontext.BeanContextEvent
 * @see		java.beans.beancontext.BeanContextMembershipListener
 */
public class BeanContextMembershipEvent extends BeanContextEvent {

    /**
     * Contruct a BeanContextMembershipEvent
     *
     * @param bc	The BeanContext source
     * @param changes	The Children affected
     */

    public BeanContextMembershipEvent(BeanContext bc, Collection changes) {
	super(bc);

	if (changes == null) throw new NullPointerException(
	    "BeanContextMembershipEvent constructor:  changes is null.");

	children = changes;
    }

    /**
     * Contruct a BeanContextMembershipEvent
     *
     * @param bc	The BeanContext source
     * @param changes	The Children effected
     * @exception       NullPointerException if changes associated with this 
     *                  event are null.
     */

    public BeanContextMembershipEvent(BeanContext bc, Object[] changes) {
	super(bc);

	if (changes == null) throw new NullPointerException(
	    "BeanContextMembershipEvent:  changes is null.");

	children = Arrays.asList(changes);
    }

    /**
     * Gets the number of children affected by the notification.
     * @return the number of children affected by the notification
     */
    public int size() { return children.size(); }

    /**
     * Is the child specified affected by the event?
     * @return <code>true</code> if affected, <code>false</code> 
     * if not
     */
    public boolean contains(Object child) {
	return children.contains(child);
    }

    /**
     * Gets the array of children affected by this event.
     * @return the array of children affected
     */
    public Object[] toArray() { return children.toArray(); }

    /**
     * Gets the array of children affected by this event.
     * @return the array of children effected
     */
    public Iterator iterator() { return children.iterator(); }

    /*
     * fields
     */

   /**
    * The list of children affected by this 
    * event notification.
    */
    protected Collection children;
}





