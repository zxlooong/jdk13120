/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import java.util.EventObject;
import java.awt.event.MouseEvent;
import java.awt.Component;


/**
 * MenuDragMouseEvent is used to notify interested parties that
 * the menu element has received a MouseEvent forwarded to it
 * under drag conditions.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.10 02/06/02
 * @author Georges Saab
 */
public class MenuDragMouseEvent extends MouseEvent {
    private MenuElement path[];
    private MenuSelectionManager manager;

    /**
     * Constructs a MenuDragMouseEvent object.
     *
     * @param source        the Component that originated the event
     *                      (typically <code>this</code>)
     * @param id            an int specifying the type of event, as defined
     *                      in {@link java.awt.event.MouseEvent}
     * @param when          a long identifying the time the event occurred
     * @param modifiers     an int specifying any modifier keys held down,
     *                      as specified in {@link java.awt.event.InputEvent}
     * @param x             an int specifying the horizontal position at which
     *                      the event occurred, in pixels
     * @param y             an int specifying the vertical position at which
     *                      the event occurred, in pixels
     * @param clickCount    an int specifying the number of mouse-clicks
     * @param popupTrigger  a boolean -- true if the event {should?/did?}
     *                      trigger a popup
     * @param p             an array of MenuElement objects specifying a path
     *                        to a menu item affected by the drag
     * @param m             a MenuSelectionManager object that handles selections
     */
    public MenuDragMouseEvent(Component source, int id, long when,
			      int modifiers, int x, int y, int clickCount,
			      boolean popupTrigger, MenuElement p[],
			      MenuSelectionManager m) {
        super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
	path = p;
	manager = m;
    }

    /**
     * Returns the path to the selected menu item.
     *
     * @return an array of MenuElement objects representing the path value
     */
    public MenuElement[] getPath() {
	return path;
    }

    /**
     * Returns the current menu selection manager.
     *
     * @return a MenuSelectionManager object
     */
    public MenuSelectionManager getMenuSelectionManager() {
	return manager;
    }
}

