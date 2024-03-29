/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.dnd;

import java.awt.event.InputEvent;
import java.awt.Component;
import java.awt.Point;

import java.util.TooManyListenersException;
import java.util.ArrayList;

/**
 * The <code>DragGestureRecognizer</code> is an 
 * abstract base class for the specification
 * of a platform-dependent listener that can be associated with a particular
 * <code>Component</code> in order to 
 * identify platform-dependent drag initiating gestures.
 * <p>
 * The appropriate <code>DragGestureRecognizer</code>
 * subclass is obtained from the
 * <code>DragSource</code> asssociated with 
 * a particular <code>Component</code>, or from the <code>Toolkit</code>
 * object via its createDragGestureRecognizer() method.
 * <p>
 * Once the <code>DragGestureRecognizer</code> 
 * is associated with a particular <code>Component</code>
 * it will register the appropriate listener interfaces on that 
 * <code>Component</code>
 * in order to track the input events delivered to the <code>Component</code>.
 * <p>
 * Once the <code>DragGestureRecognizer</code> identifies a sequence of events
 * on the <code>Component</code> as a drag initiating gesture, it will notify
 * its unicast <code>DragGestureListener</code> by 
 * invoking its gestureRecognized() method.
 * <P>
 * When a concrete <code>DragGestureRecognizer</code> 
 * instance detects a drag initiating
 * gesture on the <code>Component</code> it is associated with,
 * it will fire a <code>DragGestureEvent</code> to 
 * the <code>DragGestureListener</code> registered on
 * its unicast event source for <code>DragGestureListener</code>
 * events. This <code>DragGestureListener</code> is responsible 
 * for causing the associated
 * <code>DragSource</code> to start the Drag and Drop operation (if
 * appropriate). 
 * <P>
 * @author Laurence P. G. Cable
 * @version 1.14
 * @see java.awt.dnd.DragGestureListener
 * @see java.awt.dnd.DragGestureEvent
 * @see java.awt.dnd.DragSource
 */

public abstract class DragGestureRecognizer {

    /**
     * Construct a new <code>DragGestureRecognizer</code> 
     * given the <code>DragSource</code> to be used 
     * in this Drag and Drop operation, the <code>Component</code> 
     * this <code>DragGestureRecognizer</code> should "observe" 
     * for drag initiating gestures, the action(s) supported 
     * for this Drag and Drop operation, and the 
     * <code>DragGestureListener</code> to notify
     * once a drag initiating gesture has been detected.
     * <P>
     * @param ds  the <code>DragSource</code> this 
     * <code>DragGestureRecognizer</code> 
     * will use to process the Drag and Drop operation
     *
     * @param c the <code>Component</code> 
     * this <code>DragGestureRecognizer</code> 
     * should "observe" the event stream to, 
     * in order to detect a drag initiating gesture.
     * If this value is <code>null</code>, the 
     * <code>DragGestureRecognizer</code>
     * is not associated with any <code>Component</code>.
     *
     * @param sa  the set (logical OR) of the 
     * <code>DnDConstants</code> 
     * that this Drag and Drop operation will support
     *
     * @param dgl the <code>DragGestureRecognizer</code> 
     * to notify when a drag gesture is detected
     * <P>
     * @throws <code>IllegalArgumentException</code> 
     * if ds is <code>null</code>.
     */

    protected DragGestureRecognizer(DragSource ds, Component c, int sa, DragGestureListener dgl) {
	super();

	if (ds == null) throw new IllegalArgumentException("null DragSource");

	dragSource    = ds;
	component     = c;
	sourceActions = sa & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);

	try {
	    if (dgl != null) addDragGestureListener(dgl);
	} catch (TooManyListenersException tmle) {
	    // cant happen ...
	}
    }

    /**
     * Construct a new <code>DragGestureRecognizer</code> 
     * given the <code>DragSource</code> to be used in this 
     * Drag and Drop
     * operation, the <code>Component</code> this 
     * <code>DragGestureRecognizer</code> should "observe" 
     * for drag initiating gestures, and the action(s) 
     * supported for this Drag and Drop operation.
     * <P>
     * @param ds  the <code>DragSource</code> this 
     * <code>DragGestureRecognizer</code> will use to 
     * process the Drag and Drop operation
     *
     * @param c   the <code>Component</code> this 
     * <code>DragGestureRecognizer</code> should "observe" the event 
     * stream to, in order to detect a drag initiating gesture.
     * If this value is <code>null</code>, the 
     * <code>DragGestureRecognizer</code>
     * is not associated with any <code>Component</code>.
     *
     * @param sa the set (logical OR) of the <code>DnDConstants</code> 
     * that this Drag and Drop operation will support
     * <P>
     * @throws <code>IllegalArgumentException</code> 
     * if ds is <code>null</code>.
     */

    protected DragGestureRecognizer(DragSource ds, Component c, int sa) {
	this(ds, c, sa, null);
    }

    /**
     * Construct a new <code>DragGestureRecognizer</code> 
     * given the <code>DragSource</code> to be used 
     * in this Drag and Drop operation, and 
     * the <code>Component</code> this 
     * <code>DragGestureRecognizer</code> 
     * should "observe" for drag initiating gestures.
     * <P>
     * @param ds the <code>DragSource</code> this 
     * <code>DragGestureRecognizer</code> 
     * will use to process the Drag and Drop operation
     *
     * @param c the <code>Component</code> 
     * this <code>DragGestureRecognizer</code> 
     * should "observe" the event stream to, 
     * in order to detect a drag initiating gesture.
     * If this value is <code>null</code>, 
     * the <code>DragGestureRecognizer</code>
     * is not associated with any <code>Component</code>.
     * <P>
     * @throws <code>IllegalArgumentException</code> 
     * if ds is <code>null</code>.
     */

    protected DragGestureRecognizer(DragSource ds, Component c) {
	this(ds, c, DnDConstants.ACTION_NONE);
    }

    /**
     * Construct a new <code>DragGestureRecognizer</code> 
     * given the <code>DragSource</code> to be used in this 
     * Drag and Drop operation.
     * <P>
     * @param ds the <code>DragSource</code> this 
     * <code>DragGestureRecognizer</code> will 
     * use to process the Drag and Drop operation
     * <P>
     * @throws <code>IllegalArgumentException</code> 
     * if ds is <code>null</code>.
     */

    protected DragGestureRecognizer(DragSource ds) {
	this(ds, null);
    }

    /**
     * register this DragGestureRecognizer's Listeners with the Component
     *
     * subclasses must override this method
     */

    protected abstract void registerListeners();

    /**
     * unregister this DragGestureRecognizer's Listeners with the Component
     *
     * subclasses must override this method
     */

    protected abstract void unregisterListeners();

    /**
     * This method returns the <code>DragSource</code> 
     * this <code>DragGestureRecognizer</code> 
     * will use in order to process the Drag and Drop 
     * operation.
     * <P>
     * @return the DragSource
     */

    public DragSource getDragSource() { return dragSource; }

    /**
     * This method returns the <code>Component</code> 
     * that is to be "observed" by the 
     * <code>DragGestureRecognizer</code> 
     * for drag initiating gestures.
     * <P>
     * @return The Component this DragGestureRecognizer 
     * is associated with
     */

    public synchronized Component getComponent() { return component; }

    /**
     * set the Component that the DragGestureRecognizer is associated with
     *
     * registerListeners() and unregisterListeners() are called as a side
     * effect as appropriate.
     * <P>
     * @param c The <code>Component</code> or <code>null</code>
     */

    public synchronized void setComponent(Component c) {
	if (component != null && dragGestureListener != null)
	    unregisterListeners();

	component = c;

	if (component != null && dragGestureListener != null)
	    registerListeners();
    }

    /**
     * This method returns an int representing the 
     * type of action(s) this Drag and Drop 
     * operation will support.
     * <P>
     * @return the currently permitted source action(s)
     */

    public synchronized int getSourceActions() { return sourceActions; }

    /**
     * This method sets the permitted source drag action(s) 
     * for this Drag and Drop operation.
     * <P>
     * @param actions the permitted source drag action(s)
     */

    public synchronized void setSourceActions(int actions) {
	sourceActions = actions & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
    }

    /**
     * This method returns the first event in the 
     * series of events that initiated 
     * the Drag and Drop operation.
     * <P>
     * @return the initial event that triggered the drag gesture
     */

    public InputEvent getTriggerEvent() { return events.isEmpty() ? null : (InputEvent)events.get(0); }

    /**
     * Reset the Recognizer, if its currently recognizing a gesture, ignore
     * it.
     */

    public void resetRecognizer() { events.clear(); }

    /**
     * Register a new <code>DragGestureListener</code>.
     * <P>
     * @param dgl the <code>DragGestureListener</code> to register 
     * with this <code>DragGestureRecognizer</code>.
     * <P>
     * @throws java.util.TooManyListenersException if a 
     * <code>DragGestureListener</code> has already been added.
     */

    public synchronized void addDragGestureListener(DragGestureListener dgl) throws TooManyListenersException {
	if (dragGestureListener != null)
	    throw new TooManyListenersException();
	else {
	    dragGestureListener = dgl;

	    if (component != null) registerListeners();
	}
    }

    /**
     * unregister the current DragGestureListener
     * <P>
     * @param dgl the <code>DragGestureListener</code> to unregister 
     * from this <code>DragGestureRecognizer</code>
     * <P>
     * @throws <code>IllegalArgumentException</code> if 
     * dgl is not (equal to) the currently registered <code>DragGestureListener</code>.
     */

    public synchronized void removeDragGestureListener(DragGestureListener dgl) {
	if (dragGestureListener == null || !dragGestureListener.equals(dgl))
	    throw new IllegalArgumentException();
	else {
	    dragGestureListener = null;

	    if (component != null) unregisterListeners();
	}
    }

    /**
     * Notify the DragGestureListener that a Drag and Drop initiating
     * gesture has occurred. Then reset the state of the Recognizer.
     * <P>
     * @param dragAction The action initially selected by the users gesture
     * @param p          The point (in Component coords) where the gesture originated
     */
    protected synchronized void fireDragGestureRecognized(int dragAction, Point p) {
	if (dragGestureListener != null) {
	    dragGestureListener.dragGestureRecognized(new DragGestureEvent(this, dragAction, p, events));
	}
	events.clear();
    }

    /**
     * Listeners registered on the Component by this Recognizer shall record
     * all Events that are recognized as part of the series of Events that go
     * to comprise a Drag and Drop initiating gesture via this API.
     *<P>
     * This method is used by a <code>DragGestureRecognizer</code> 
     * implementation to add an <code>InputEvent</code> 
     * subclass (that it believes is one in a series
     * of events that comprise a Drag and Drop operation) 
     * to the array of events that this 
     * <code>DragGestureRecognizer</code> maintains internally.
     * <P>
     * @param awtie the <code>InputEvent</code> 
     * to add to this <code>DragGestureRecognizer</code>'s 
     * internal array of events. Note that <code>null</code>
     * is not a valid value, and will be ignored.
     */

    protected synchronized void appendEvent(InputEvent awtie) {
	events.add(awtie);
    }

    /*
     * fields
     */

    /**
     * The <code>DragSource</code> 
     * associated with this 
     * <code>DragGestureRecognizer</code>.
     */
    protected DragSource          dragSource;
 
    /**
     * The <code>Component</code> 
     * associated with this <code>DragGestureRecognizer</code>.
     */
    protected Component           component;

    /**
     * The <code>DragGestureListener</code> 
     * associated with this <code>DragGestureRecognizer</code>.
     */
    protected DragGestureListener dragGestureListener;

  /**
   * An <code>int</code> representing 
   * the type(s) of action(s) used 
   * in this Drag and Drop operation.  
   */
  protected int	 sourceActions;

   /**
    * The list of events (in order) that 
    * the <code>DragGestureRecognizer</code> 
    * "recognized" as a "gesture" that triggers a drag.
    */
   protected ArrayList events = new ArrayList(1);
}







