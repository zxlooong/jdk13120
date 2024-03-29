/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.event;

import java.awt.Component;
import java.awt.Event;
import java.awt.Rectangle;

/**
 * The component-level paint event.
 * This event is a special type which is used to ensure that
 * paint/update method calls are serialized along with the other
 * events delivered from the event queue.  This event is not
 * designed to be used with the Event Listener model; programs
 * should continue to override paint/update methods in order
 * render themselves properly.
 *
 * @author Amy Fowler
 * @version 1.15, 02/06/02
 * @since 1.1
 */
public class PaintEvent extends ComponentEvent {

    /**
     * Marks the first integer id for the range of paint event ids.
     */    
    public static final int PAINT_FIRST		= 800;

    /**
     * Marks the last integer id for the range of paint event ids.
     */
    public static final int PAINT_LAST		= 801;

    /**
     * The paint event type.  
     */
    public static final int PAINT = PAINT_FIRST;

    /**
     * The update event type.  
     */
    public static final int UPDATE = PAINT_FIRST + 1; //801

    /**
     * This is the rectangle that represents the area on the source
     * component that requires a repaint.
     * This rectangle should be non null.
     *
     * @serial
     * @see java.awt.Rectangle
     * @see setUpdateRect()
     * @see getUpdateRect()
     */
    Rectangle updateRect;

    /*
     * JDK 1.1 serialVersionUID 
     */
    private static final long serialVersionUID = 1267492026433337593L;

    /**
     * Constructs a PaintEvent object with the specified source component
     * and type.
     * @param source     the object where the event originated
     * @param id         the event type
     * @param updateRect the rectangle area which needs to be repainted
     */
    public PaintEvent(Component source, int id, Rectangle updateRect) {
        super(source, id);
        this.updateRect = updateRect;
    }

    /**
     * Returns the rectangle representing the area which needs to be
     * repainted in response to this event.
     */
    public Rectangle getUpdateRect() {
        return updateRect;
    }

    /**
     * Sets the rectangle representing the area which needs to be
     * repainted in response to this event.
     * @param updateRect the rectangle area which needs to be repainted
     */
    public void setUpdateRect(Rectangle updateRect) {
        this.updateRect = updateRect;
    }

    public String paramString() {
        String typeStr;
        switch(id) {
          case PAINT:
              typeStr = "PAINT";
              break;
          case UPDATE:
              typeStr = "UPDATE";
              break;
          default:
              typeStr = "unknown type";
        }
        return typeStr + ",updateRect="+(updateRect != null ? updateRect.toString() : "null");
    }
}
