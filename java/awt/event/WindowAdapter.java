/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.event;

/**
 * An abstract adapter class for receiving window events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 * <P>
 * Extend this class to create a <code>WindowEvent</code> listener 
 * and override the methods for the events of interest. (If you implement the 
 * <code>WindowListener</code> interface, you have to define all of
 * the methods in it. This abstract class defines null methods for them
 * all, so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using the extended class and then register it with 
 * a Window using the window's <code>addWindowListener</code> 
 * method. When the window's status changes by virtue of being opened,
 * closed, activated or deactivated, iconified or deiconified, 
 * the relevant method in the listener
 * object is invoked, and the <code>WindowEvent</code> is passed to it.
 *
 * @see WindowEvent
 * @see WindowListener
 * @see <a href="http://java.sun.com/docs/books/tutorial/post1.0/ui/windowlistener.html">Tutorial: Writing a Window Listener</a>
 * @see <a href="http://www.awl.com/cp/javaseries/jcl1_2.html">Reference: The Java Class Libraries (update file)</a>
 *
 * @author Carl Quinn
 * @author Amy Fowler
 * @version 1.16, 02/06/02
 * @since 1.1
 */
public abstract class WindowAdapter implements WindowListener {
    /**
     * Invoked when a window has been opened.
     */
    public void windowOpened(WindowEvent e) {}

    /**
     * Invoked when a window is in the process of being closed.
     * The close operation can be overridden at this point.
     */
    public void windowClosing(WindowEvent e) {}

    /**
     * Invoked when a window has been closed.
     */
    public void windowClosed(WindowEvent e) {}

    /**
     * Invoked when a window is iconified.
     */
    public void windowIconified(WindowEvent e) {}

    /**
     * Invoked when a window is de-iconified.
     */
    public void windowDeiconified(WindowEvent e) {}

    /**
     * Invoked when a window is activated.
     */
    public void windowActivated(WindowEvent e) {}

    /**
     * Invoked when a window is de-activated.
     */
    public void windowDeactivated(WindowEvent e) {}
}
