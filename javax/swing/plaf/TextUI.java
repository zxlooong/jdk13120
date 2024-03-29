/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Insets;
import javax.swing.text.*;

/**
 * Text editor user interface
 *
 * @author  Timothy Prinzing
 * @version 1.27 02/06/02
 */
public abstract class TextUI extends ComponentUI
{
    /**
     * Converts the given location in the model to a place in
     * the view coordinate system.
     *
     * @param pos  the local location in the model to translate >= 0
     * @return the coordinates as a rectangle
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     */
    public abstract Rectangle modelToView(JTextComponent t, int pos) throws BadLocationException;

    /**
     * Converts the given location in the model to a place in
     * the view coordinate system.
     *
     * @param pos  the local location in the model to translate >= 0
     * @return the coordinates as a rectangle
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     */
    public abstract Rectangle modelToView(JTextComponent t, int pos, Position.Bias bias) throws BadLocationException;

    /**
     * Converts the given place in the view coordinate system
     * to the nearest representative location in the model.
     *
     * @param pt  the location in the view to translate.  This
     *   should be in the same coordinate system as the mouse
     *   events.
     * @returns the offset from the start of the document >= 0
     */
    public abstract int viewToModel(JTextComponent t, Point pt);

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.  The biasReturn argument will be
     * filled in to indicate that the point given is closer to the next
     * character in the model or the previous character in the model.
     *
     * @param x the X coordinate >= 0
     * @param y the Y coordinate >= 0
     * @param a the allocated region to render into
     * @return the location within the model that best represents the
     *  given point in the view >= 0.  The biasReturn argument will be
     * filled in to indicate that the point given is closer to the next
     * character in the model or the previous character in the model.
     */
    public abstract int viewToModel(JTextComponent t, Point pt,
				    Position.Bias[] biasReturn);

    /**
     * Provides a way to determine the next visually represented model 
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard.
     *  This may be SwingConstants.WEST, SwingConstants.EAST, 
     *  SwingConstants.NORTH, or SwingConstants.SOUTH.  
     * @return the location within the model that best represents the next
     *  location visual position.
     * @exception BadLocationException
     * @exception IllegalArgumentException for an invalid direction
     */
    public abstract int getNextVisualPositionFrom(JTextComponent t,
			 int pos, Position.Bias b,
			 int direction, Position.Bias[] biasRet)
	                 throws BadLocationException;

    /**
     * Causes the portion of the view responsible for the
     * given part of the model to be repainted.
     *
     * @param p0 the beginning of the range >= 0
     * @param p1 the end of the range >= p0
     */
    public abstract void damageRange(JTextComponent t, int p0, int p1);

    /**
     * Causes the portion of the view responsible for the 
     * given part of the model to be repainted.
     *
     * @param p0 the beginning of the range >= 0
     * @param p1 the end of the range >= p0
     */
    public abstract void damageRange(JTextComponent t, int p0, int p1,
				     Position.Bias firstBias,
				     Position.Bias secondBias);

    /**
     * Fetches the binding of services that set a policy
     * for the type of document being edited.  This contains
     * things like the commands available, stream readers and
     * writers, etc.
     *
     * @return the editor kit binding
     */
    public abstract EditorKit getEditorKit(JTextComponent t);

    /**
     * Fetches a View with the allocation of the associated
     * text component (i.e. the root of the hierarchy) that
     * can be traversed to determine how the model is being
     * represented spatially.
     *
     * @return the view
     */
    public abstract View getRootView(JTextComponent t);


}
