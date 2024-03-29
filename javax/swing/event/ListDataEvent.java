/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.event;

import java.util.EventObject;


/**
 * Defines an event that encapsulates changes to a list.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.13 02/06/02
 * @author Hans Muller
 */
public class ListDataEvent extends EventObject 
{
    /** Identifies one or more changes in the lists contents. */
    public static final int CONTENTS_CHANGED = 0;
    /** Identifies the addition of one or more contiguous items to the list */
    public static final int INTERVAL_ADDED = 1;
    /** Identifies the removal of one or more contiguous items from the list */
    public static final int INTERVAL_REMOVED = 2;

    private int type;
    private int index0;
    private int index1;

    /**
     * Returns the event type. The possible values are:
     * <ul>
     * <li> {@link #CONTENTS_CHANGED}
     * <li> {@link #INTERVAL_ADDED}
     * <li> {@link #INTERVAL_REMOVED}
     * </ul>
     *
     * @return an int representing the type value
     */
    public int getType() { return type; }

    /**
     * Returns the lower index of the range. For a single
     * element, this value is the same as that returned by {@link #getIndex1}.

     *
     * @return an int representing the lower index value
     */
    public int getIndex0() { return index0; }
    /**
     * Returns the upper index of the range. For a single
     * element, this value is the same as that returned by {@link #getIndex0}.
     *
     * @return an int representing the upper index value
     */
    public int getIndex1() { return index1; }

    /**
     * Constructs a ListDataEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying {@link #CONTENTS_CHANGED},
     *                {@link #INTERVAL_ADDED}, or {@link #INTERVAL_REMOVED}
     * @param index0  an int specifying the bottom of a range
     * @param index1  an int specifying the top of a range
     */
    public ListDataEvent(Object source, int type, int index0, int index1) {
        super(source);
	this.type = type;
	this.index0 = index0;
	this.index1 = index1;
    }
}



