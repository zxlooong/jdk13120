/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.beans.beancontext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import java.beans.PropertyVetoException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <p>
 * This is a general support class to provide support for implementing the
 * BeanContextChild protocol.
 * 
 * This class may either be directly subclassed, or encapsulated and delegated
 * to in order to implement this interface for a given component.
 * </p>
 *
 * @author	Laurence P. G. Cable
 * @version	1.11, 02/06/02
 * @since	1.2
 * 
 * @seealso	java.beans.beancontext.BeanContext
 * @seealso	java.beans.beancontext.BeanContextServices
 * @seealso	java.beans.beancontext.BeanContextChild
 */

public class BeanContextChildSupport implements BeanContextChild, BeanContextServicesListener, Serializable {

    static final long serialVersionUID = 6328947014421475877L;

    /**
     * construct a BeanContextChildSupport where this class has been 
     * subclassed in order to implement the JavaBean component itself.
     */

    public BeanContextChildSupport() {
	super();

	beanContextChildPeer = this;

	pcSupport = new PropertyChangeSupport(beanContextChildPeer);
	vcSupport = new VetoableChangeSupport(beanContextChildPeer);
    }

    /**
     * construct a BeanContextChildSupport where the JavaBean component
     * itself implements BeanContextChild, and encapsulates this, delegating
     * that interface to this implementation
     */

    public BeanContextChildSupport(BeanContextChild bcc) {
	super();

	beanContextChildPeer = (bcc != null) ? bcc : this;

	pcSupport = new PropertyChangeSupport(beanContextChildPeer);
	vcSupport = new VetoableChangeSupport(beanContextChildPeer);
    }

    /**
     * Sets the <code>BeanContext</code> for 
     * this <code>BeanContextChildSupport</code>.
     * @param bc the new value to be assigned to the <code>BeanContext</code> 
     * property
     * @throws <code>PropertyVetoException</code> if the change is rejected
     */
    public synchronized void setBeanContext(BeanContext bc) throws PropertyVetoException {
	if (bc == beanContext) return;

	BeanContext oldValue = beanContext;
	BeanContext newValue = bc;

	if (!rejectedSetBCOnce) {
	    if (rejectedSetBCOnce = !validatePendingSetBeanContext(bc)) {
		throw new PropertyVetoException(
		    "setBeanContext() change rejected:",
		    new PropertyChangeEvent(beanContextChildPeer, "beanContext", oldValue, newValue)
		);
	    }

	    try {
		fireVetoableChange("beanContext",
				   oldValue,
				   newValue
		);
	    } catch (PropertyVetoException pve) {
		rejectedSetBCOnce = true;

		throw pve; // re-throw
	    }
	}

	if (beanContext != null) releaseBeanContextResources();

	beanContext       = newValue;
	rejectedSetBCOnce = false;

	firePropertyChange("beanContext", 
			   oldValue,
			   newValue
	);

	if (beanContext != null) initializeBeanContextResources();
    }

    /**
     * Gets the nesting <code>BeanContext</code> 
     * for this <code>BeanContextChildSupport</code>.
     * @return the nesting <code>BeanContext</code> for 
     * this <code>BeanContextChildSupport</code>.
     */
    public synchronized BeanContext getBeanContext() { return beanContext; }

    /**
     * Adds a property change listener.
     * @param name The name of the property to listen on
     * @param pcl The <code>PropertyChangeListener</code> to be added
     */
    public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
	pcSupport.addPropertyChangeListener(name, pcl);
    }

    /**
     * Remove a property change listener.
     * @param name The name of the property that was listened on
     * @param pcl The PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
	pcSupport.removePropertyChangeListener(name, pcl);
    }

    /**
     * Adds a <code>VetoableChangeListener</code>.
     * @param name The name of the property to listen on
     * @param vcl The <code>VetoableChangeListener</code> to be added
     */
    public void addVetoableChangeListener(String name, VetoableChangeListener vcl) {
	vcSupport.addVetoableChangeListener(name, vcl);
    }

    /**
     * Removes a <code>VetoableChangeListener</code>.
     * @param name The name of the property that was listened on
     * @param vcl The <code>VetoableChangeListener</code> to be removed
     */
    public void removeVetoableChangeListener(String name, VetoableChangeListener vcl) {
	vcSupport.removeVetoableChangeListener(name, vcl);
    }

    /**
     * A service provided by the nesting BeanContext has been revoked.
     * 
     * Subclasses may override this method in order to implement their own
     * behaviors.
     * @param bcsre The <code>BeanContextServiceRevokedEvent</code> fired as a 
     * result of a service being revoked
     */
    public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) { }

    /**
     * A new service is available from the nesting BeanContext.
     * 
     * Subclasses may override this method in order to implement their own
     * behaviors 
     * @param bcsae The BeanContextServiceAvailableEvent fired as a
     * result of a service becoming available
     * 
     */
    public void serviceAvailable(BeanContextServiceAvailableEvent bcsae) { }

    /**
     * Gets the <tt>BeanContextChild</tt> associated with this 
     * <tt>BeanContextChildSupport</tt>.
     *
     * @return the <tt>BeanContextChild</tt> peer of this class
     */
    public BeanContextChild getBeanContextChildPeer() { return beanContextChildPeer; }

    /**
     * Reports whether or not this class is a delegate of another.
     * 
     * @return true if this class is a delegate of another
     */
    public boolean isDelegated() { return !this.equals(beanContextChildPeer); }

    /**
     * Report a bound property update to any registered listeners. No event is
     * fired if old and new are equal and non-null.
     * @param name The programmatic name of the property that was changed
     * @param oldValue  The old value of the property
     * @param newValue  The new value of the property
     */
    public void firePropertyChange(String name, Object oldValue, Object newValue) {
	pcSupport.firePropertyChange(name, oldValue, newValue);
    }

    /**
     * Report a vetoable property update to any registered listeners. 
     * If anyone vetos the change, then fire a new event 
     * reverting everyone to the old value and then rethrow 
     * the PropertyVetoException. <P>
     *
     * No event is fired if old and new are equal and non-null.
     * <P>
     * @param name The programmatic name of the property that is about to
     * change
     * 
     * @param oldValue The old value of the property
     * @param newValue - The new value of the property
     * 
     * @throws PropertyVetoException if the recipient wishes the property
     * change to be rolled back.
     */
    public void fireVetoableChange(String name, Object oldValue, Object newValue) throws PropertyVetoException {
	vcSupport.fireVetoableChange(name, oldValue, newValue);
    }

    /**
     * Called from setBeanContext to validate (or otherwise) the
     * pending change in the nesting BeanContext property value.
     * Returning false will cause setBeanContext to throw
     * PropertyVetoException.
     * @param newValue the new value that has been requested for 
     *  the BeanContext property
     * @return <code>true</code> if the change operation is to be vetoed
     */
    public boolean validatePendingSetBeanContext(BeanContext newValue) {
	return true;
    }

    /**
     * This method may be overridden by subclasses to provide their own
     * release behaviors. When invoked any resources held by this instance
     * obtained from its current BeanContext property should be released
     * since the object is no longer nested within that BeanContext.
     */

    protected  void releaseBeanContextResources() {
	// do nothing
    }

    /**
     * This method may be overridden by subclasses to provide their own
     * initialization behaviors. When invoked any resources requried by the
     * BeanContextChild should be obtained from the current BeanContext.
     */

    protected void initializeBeanContextResources() {
	// do nothing
    }

    /**
     * Write the persistence state of the object.
     */

    private void writeObject(ObjectOutputStream oos) throws IOException {

	/*
	 * dont serialize if we are delegated and the delegator isnt also
	 * serializable.
	 */

	if (!equals(beanContextChildPeer) && !(beanContextChildPeer instanceof Serializable))
	    throw new IOException("BeanContextChildSupport beanContextChildPeer not Serializable");

	else 
            oos.defaultWriteObject();
	    
    }


    /**
     * Restore a persistent object, must wait for subsequent setBeanContext()
     * to fully restore any resources obtained from the new nesting 
     * BeanContext
     */

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
	ois.defaultReadObject();
    }

    /*
     * fields
     */

    /**
     * The <code>BeanContext</code> in which 
     * this <code>BeanContextChild</code> is nested.
     */
    public    BeanContextChild 	    beanContextChildPeer;

   /** 
    * The <tt>PropertyChangeSupport</tt> associated with this
    * <tt>BeanContextChildSupport</tt>.
    */
    protected PropertyChangeSupport pcSupport;

   /**
    * The <tt>VetoableChangeSupport</tt> associated with this
    * <tt>BeanContextChildSupport</tt>.
    */
    protected VetoableChangeSupport vcSupport;

    protected transient BeanContext	      beanContext;

   /**
    * A flag indicating that there has been
    * at least one <code>PropertyChangeVetoException</code>
    * thrown for the attempted setBeanContext operation.
    */
    protected transient boolean     	      rejectedSetBCOnce;

}
