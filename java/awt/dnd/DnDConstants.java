/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.dnd;

/**
 * This class contains constant values representing
 * the type of action(s) to be performed by a Drag and Drop operation.
 * @version 	1.17, 02/06/02
 * @since 1.2
 */

public final class DnDConstants {

    private DnDConstants() {} // define null private constructor.

    /**
     * An <code>int</code> representing no action. 
     */
    public static final int ACTION_NONE		= 0x0;

    /**
     * An <code>int</code> representing a &quot;copy&quot; action.
     */
    public static final int ACTION_COPY		= 0x1;

    /**
     * An <code>int</code> representing a &quot;move&quot; action.
     */
    public static final int ACTION_MOVE		= 0x2;

    /**
     * An <code>int</code> representing a &quot;copy&quot; or 
     * &quot;move&quot; action.
     */
    public static final int ACTION_COPY_OR_MOVE	= ACTION_COPY | ACTION_MOVE;

    /**
     * An <code>int</code> representing a &quot;link&quot; action.
     *
     * The link verb is found in many, if not all native DnD platforms, and the
     * actual interpretation of LINK semantics is both platform
     * and application dependent. Broadly speaking, the
     * semantic is "do not copy, or move the operand, but create a reference
     * to it". Defining the meaning of "reference" is where ambiguity is
     * introduced.
     *
     * The verb is provided for completeness, but its use is not recommended
     * for DnD operations between logically distinct applications where 
     * misinterpretation of the operations semantics could lead to confusing
     * results for the user.
     */

    public static final int ACTION_LINK	        = 0x40000000;

    /**
     * An <code>int</code> representing a &quot;reference&quot; 
     * action (synonym for ACTION_LINK).
     */
    public static final int ACTION_REFERENCE    = ACTION_LINK;

}


