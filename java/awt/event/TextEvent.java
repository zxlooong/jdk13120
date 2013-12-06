/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.event;

import java.awt.AWTEvent;
import java.awt.Event;

/**
 * A semantic event which indicates that an object's text changed.
 * This high-level event is generated by an object (such as a TextComponent)
 * when its text changes. The event is passed to
 * every <code>TextListener</code> object which registered to receive such
 * events using the component's <code>addTextListener</code> method. 
 * <P>
 * The object that implements the <code>TextListener</code> interface gets
 * this <code>TextEvent</code> when the event occurs. The listener is
 * spared the details of processing individual mouse movements and key strokes
 * Instead, it can process a "meaningful" (semantic) event like "text changed". 
 *
 * @author Georges Saab
 * @version 1.12 02/06/02
 *
 * @see java.awt.TextComponent
 * @see TextListener
 * @see <a href="http://java.sun.com/docs/books/tutorial/post1.0/ui/textlistener.html">Tutorial: Writing a Text Listener</a>
 * @see <a href="http://www.awl.com/cp/javaseries/jcl1_2.html">Reference: The Java Class Libraries (update file)</a>
 *
 * @since 1.1
 */

public class TextEvent extends AWTEvent {

    /**
     * The first number in the range of ids used for text events.
     */
    public static final int TEXT_FIRST 	= 900;

    /**
     * The last number in the range of ids used for text events.
     */
    public static final int TEXT_LAST 	= 900;

    /**
     * This event id indicates that object's text changed.
     */
    public static final int TEXT_VALUE_CHANGED	= TEXT_FIRST;

    /*
     * JDK 1.1 serialVersionUID 
     */
    private static final long serialVersionUID = 6269902291250941179L;

    /**
     * Constructs a TextEvent object.
     *
     * @param source the (TextComponent) object that originated the event
     * @param id     an integer that identifies the event type
     */
    public TextEvent(Object source, int id) {
        super(source, id);
    }


    /**
     * Returns a parameter string identifying this text event.
     * This method is useful for event-logging and for debugging.
     *
     * @return a string identifying the event and its attributes
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case TEXT_VALUE_CHANGED:
              typeStr = "TEXT_VALUE_CHANGED";
              break;
          default:
              typeStr = "unknown type";
        }
        return typeStr;
    }
}

