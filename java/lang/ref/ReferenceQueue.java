/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.lang.ref;


/**
 * Reference queues, to which registered reference objects are appended by the
 * garbage collector after the appropriate reachability changes are detected.
 *
 * @version  1.15, 02/06/02
 * @author   Mark Reinhold
 * @since    1.2
 */

public class ReferenceQueue {

    /**
     * Constructs a new reference-object queue.
     */
    public ReferenceQueue() { }

    private static class Null extends ReferenceQueue {
	boolean enqueue(Reference r) {
	    return false;
	}
    }

    static ReferenceQueue NULL = new Null();
    static ReferenceQueue ENQUEUED = new Null();

    static private class Lock { };
    private Lock lock = new Lock();
    private Reference head = null;

    boolean enqueue(Reference r) {	/* Called only by Reference class */
	synchronized (r) {
	    if (r.queue == ENQUEUED) return false;
	    synchronized (lock) {
		r.queue = ENQUEUED;
		r.next = (head == null) ? r : head;
		head = r;
		lock.notifyAll();
		return true;
	    }
	}
    }

    private Reference reallyPoll() {	/* Must hold lock */
	if (head != null) {
	    Reference r = head;
	    head = (r.next == r) ? null : r.next;
	    r.queue = NULL;
	    r.next = r;
	    return r;
	}
	return null;
    }

    /**
     * Polls this queue to see if a reference object is available,
     * returning one immediately if so.  If the queue is empty, this
     * method immediately returns <code>null</code>.
     *
     * @return  A reference object, if one was immediately available,
     *          otherwise <code>null</code>
     */
    public Reference poll() {
	synchronized (lock) {
	    return reallyPoll();
	}
    }

    /**
     * Removes the next reference object in this queue, blocking until either
     * one becomes available or the given timeout period expires.
     *
     * @param  timeout  If positive, block for up <code>timeout</code>
     *                  milliseconds while waiting for a reference to be
     *                  added to this queue.  If zero, block indefinitely.
     *
     * @return  A reference object, if one was available within the specified
     *          timeout period, otherwise <code>null</code>
     *
     * @throws  IllegalArgumentException
     *          If the value of the timeout argument is negative
     *
     * @throws  InterruptedException
     *          If the timeout wait is interrupted
     */
    public Reference remove(long timeout)
	throws IllegalArgumentException, InterruptedException
    {
	if (timeout < 0) {
	    throw new IllegalArgumentException("Negative timeout value");
	}
	synchronized (lock) {
	    Reference r = reallyPoll();
	    if (r != null) return r;
	    for (;;) {
		lock.wait(timeout);
		r = reallyPoll();
		if (r != null) return r;
		if (timeout != 0) return null;
	    }
	}
    }

    /**
     * Removes the next reference object in this queue, blocking until one
     * becomes available.
     *
     * @throws  InterruptedException  If the wait is interrupted
     */
    public Reference remove() throws InterruptedException {
	return remove(0);
    }

}
