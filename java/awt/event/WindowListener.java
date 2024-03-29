/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.event;

import java.util.EventListener;

/**
 * The listener interface for receiving window events.
 * The class that is interested in processing a window event
 * either implements this interface (and all the methods it
 * contains) or extends the abstract <code>WindowAdapter</code> class
 * (overriding only the methods of interest).
 * The listener object created from that class is then registered with a
 * Window using the window's <code>addWindowListener</code> 
 * method. When the window's status changes by virtue of being opened,
 * closed, activated or deactivated, iconified or deiconified, 
 * the relevant method in the listener object is invoked, and the 
 * <code>WindowEvent</code> is passed to it.
 *
 * @author Carl Quinn
 * @version 1.17, 02/06/02
 *
 * @see WindowAdapter
 * @see WindowEvent
 * @see <a href="http://java.sun.com/docs/books/tutorial/post1.0/ui/windowlistener.html">Tutorial: Writing a Window Listener</a>
 * @see <a href="http://www.awl.com/cp/javaseries/jcl1_2.html">Reference: The Java Class Libraries (update file)</a>
 *
 * @since 1.1
 */
public interface WindowListener extends EventListener {
    /**
     * Invoked the first time a window is made visible.
     */
    public void windowOpened(WindowEvent e);

    /**
     * Invoked when the user attempts to close the window
     * from the window's system menu.  If the program does not 
     * explicitly hide or dispose the window while processing 
     * this event, the window close operation will be cancelled.
     */
    public void windowClosing(WindowEvent e);

    /**
     * Invoked when a window has been closed as the result
     * of calling dispose on the window.
     */
    public void windowClosed(WindowEvent e);

    /**
     * Invoked when a window is changed from a normal to a
     * minimized state. For many platforms, a minimized window 
     * is displayed as the icon specified in the window's 
     * iconImage property.
     * @see java.awt.Frame#setIconImage
     */
    public void windowIconified(WindowEvent e);

    /**
     * Invoked when a window is changed from a minimized
     * to a normal state.
     */
    public void windowDeiconified(WindowEvent e);

    /**
     * Invoked when the window is set to be the user's
     * active window, which means the window (or one of its
     * subcomponents) will receive keyboard events.
     */
    public void windowActivated(WindowEvent e);

    /**
     * Invoked when a window is no longer the user's active
     * window, which means that keyboard events will no longer
     * be delivered to the window or its subcomponents.
     */
    public void windowDeactivated(WindowEvent e);
}
