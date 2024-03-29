/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.event;

/**
 * An abstract adapter class for receiving keyboard events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 * <P>
 * Extend this class to create a <code>KeyEvent</code> listener 
 * and override the methods for the events of interest. (If you implement the 
 * <code>KeyListener</code> interface, you have to define all of
 * the methods in it. This abstract class defines null methods for them
 * all, so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using the extended class and then register it with 
 * a component using the component's <code>addKeyListener</code> 
 * method. When a key is pressed, released, or typed (pressed and released),
 * the relevant method in the listener object is invoked,
 * and the <code>KeyEvent</code> is passed to it.
 *
 * @author Carl Quinn
 * @version 1.14 02/06/02
 *
 * @see KeyEvent
 * @see KeyListener
 * @see <a href="http://java.sun.com/docs/books/tutorial/post1.0/ui/keylistener.html">Tutorial: Writing a Key Listener</a>
 * @see <a href="http://www.awl.com/cp/javaseries/jcl1_2.html">Reference: The Java Class Libraries (update file)</a>
 *
 * @since 1.1
 */
public abstract class KeyAdapter implements KeyListener {
    /**
     * Invoked when a key has been typed.
     * This event occurs when a key press is followed by a key release.
     */
    public void keyTyped(KeyEvent e) {}

    /**
     * Invoked when a key has been pressed.
     */
    public void keyPressed(KeyEvent e) {}

    /**
     * Invoked when a key has been released.
     */
    public void keyReleased(KeyEvent e) {}
}
