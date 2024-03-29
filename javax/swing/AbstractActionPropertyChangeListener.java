/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

/**
 * A package-private PropertyChangeListener which listens for
 * property changes on an Action and updates the properties
 * of an ActionEvent source.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.8 02/06/02
 * @author Georges Saab
 * @see AbstractButton
 */

abstract class AbstractActionPropertyChangeListener implements PropertyChangeListener {
    private static ReferenceQueue queue;
    private WeakReference target;
    private Action action;
    
    AbstractActionPropertyChangeListener(JComponent c, Action a) {
	super();
	setTarget(c);
	this.action = a;
    }

    public void setTarget(JComponent c) {
        if (queue==null) {
	    queue = new ReferenceQueue();
	}
	// Check to see whether any old buttons have
	// been enqueued for GC.  If so, look up their
	// PCL instance and remove it from its Action.
	OwnedWeakReference r;
	while ( (r = (OwnedWeakReference)queue.poll()) != null) {
	    AbstractActionPropertyChangeListener oldPCL = 
	        (AbstractActionPropertyChangeListener)r.getOwner();
	    Action oldAction = oldPCL.getAction();
	    if (oldAction!=null) {
	        oldAction.removePropertyChangeListener(oldPCL);
	    }
	}
	this.target = new OwnedWeakReference(c, queue, this);
    }
    
    public JComponent getTarget() {
        return (JComponent)this.target.get();
    }

    public Action getAction() {
	  return action;
    }

    private static class OwnedWeakReference extends WeakReference {
        private Object owner;

        OwnedWeakReference(Object target, ReferenceQueue queue, Object owner) {
	    super(target, queue);
	    this.owner = owner;
	}

	public Object getOwner() {
	    return owner;
	}
    }
}
