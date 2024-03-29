/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.util.Enumeration;
import java.lang.Integer;

/**
 * A view implementation to display an html horizontal
 * rule.
 *
 * @author  Timothy Prinzing
 * @author  Sara Swanson
 * @version 1.28 02/06/02
 */
class HRuleView extends View  {

    /**
     * Creates a new view that represents an &lt;hr&gt; element.
     *
     * @param elem the element to create a view for
     */
    public HRuleView(Element elem) {
	super(elem);
	setPropertiesFromAttributes();
    }

    /**
     * Update any cached values that come from attributes.
     */
    protected void setPropertiesFromAttributes() {
	StyleSheet sheet = ((HTMLDocument)getDocument()).getStyleSheet();
	AttributeSet eAttr = getElement().getAttributes();
	attr = sheet.getViewAttributes(this);

	alignment = StyleConstants.ALIGN_LEFT;
	size = 0;
	noshade = null;
	widthValue = null;
	// Get the width/height
	Enumeration attributes = attr.getAttributeNames();
	while (attributes.hasMoreElements()) {
	    Object key = attributes.nextElement();
	}
	if (attr != null) {
            alignment = StyleConstants.getAlignment(attr);
	    noshade = (String)eAttr.getAttribute("noshade");
	    Object value = attr.getAttribute("size");
	    if (value != null && (value instanceof String))
		size = Integer.parseInt((String)value);
	    value = attr.getAttribute(CSS.Attribute.WIDTH);
	    if (value != null && (value instanceof CSS.LengthValue)) {
		widthValue = (CSS.LengthValue)value;
	    }
	    topMargin = getLength(CSS.Attribute.MARGIN_TOP, attr);
	    bottomMargin = getLength(CSS.Attribute.MARGIN_BOTTOM, attr);
	    leftMargin = getLength(CSS.Attribute.MARGIN_LEFT, attr);
	    rightMargin = getLength(CSS.Attribute.MARGIN_RIGHT, attr);
	}
	else {
	    topMargin = bottomMargin = leftMargin = rightMargin = 0;
	}
	if (bevel == null) {
	    bevel = BorderFactory.createLoweredBevelBorder();
	}
    }

    // This will be removed and centralized at some point, need to unify this
    // and avoid private classes.
    private float getLength(CSS.Attribute key, AttributeSet a) {
	CSS.LengthValue lv = (CSS.LengthValue) a.getAttribute(key);
	float len = (lv != null) ? lv.getValue() : 0;
	return len;
    }

    // --- View methods ---------------------------------------------

    /**
     * Paints the view.
     *
     * @param g the graphics context
     * @param a the allocation region for the view
     * @see View#paint
     */
    public void paint(Graphics g, Shape a) {
	Rectangle alloc = a.getBounds();
	int x = 0;
	int y = alloc.y + SPACE_ABOVE + (int)topMargin;
	int width = alloc.width - (int)(leftMargin + rightMargin);
	if (widthValue != null) {
	    width = (int)widthValue.getValue((float)width);
	}
	int height = alloc.height - (SPACE_ABOVE + SPACE_BELOW +
				     (int)topMargin + (int)bottomMargin);
 	if (size > 0)
		height = size;

	// Align the rule horizontally.
        switch (alignment) {
        case StyleConstants.ALIGN_CENTER:
            x = alloc.x + (alloc.width / 2) - (width / 2);
	    break;
        case StyleConstants.ALIGN_RIGHT:
            x = alloc.x + alloc.width - width - (int)rightMargin;
	    break;
        case StyleConstants.ALIGN_LEFT:
        default:
            x = alloc.x + (int)leftMargin;
	    break;
        }

	// Paint either a shaded rule or a solid line.
	if (noshade == HTML.NULL_ATTRIBUTE_VALUE)
	    g.fillRect(x, y, width, height);
	else
	    bevel.paintBorder(getContainer(), g, x, y, width, height);

    }


    /**
     * Calculates the desired shape of the rule... this is
     * basically the preferred size of the border.
     *
     * @param axis may be either X_AXIS or Y_AXIS
     * @return the desired span
     * @see View#getPreferredSpan
     */
    public float getPreferredSpan(int axis) {
	Insets i = bevel.getBorderInsets(getContainer());
	switch (axis) {
	case View.X_AXIS:
	    return i.left + i.right;
	case View.Y_AXIS:
	    if (size > 0) {
	        return size + SPACE_ABOVE + SPACE_BELOW + topMargin +
		    bottomMargin;
	    } else {
		if (noshade == HTML.NULL_ATTRIBUTE_VALUE) {
		    return 1 + SPACE_ABOVE + SPACE_BELOW + topMargin +
			bottomMargin;
		} else {
		    return i.top + i.bottom + SPACE_ABOVE + SPACE_BELOW +
			topMargin + bottomMargin;
		}
	    }
	default:
	    throw new IllegalArgumentException("Invalid axis: " + axis);
	}
    }

    /**
     * Gets the resize weight for the axis.
     * The rule is: rigid vertically and flexible horizontally.
     *
     * @param axis may be either X_AXIS or Y_AXIS
     * @return the weight
     */
    public int getResizeWeight(int axis) {
	if (axis == View.X_AXIS) {
		return 1;
	} else if (axis == View.Y_AXIS) {
		return 0;
	} else {
	    return 0;
	}
    }

    /**
     * Determines how attractive a break opportunity in 
     * this view is.  This is implemented to request a forced break.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param pos the potential location of the start of the 
     *   broken view (greater than or equal to zero).
     *   This may be useful for calculating tab
     *   positions.
     * @param len specifies the relative length from <em>pos</em>
     *   where a potential break is desired. The value must be greater
     *   than or equal to zero.
     * @return the weight, which should be a value between
     *   ForcedBreakWeight and BadBreakWeight.
     */
    public int getBreakWeight(int axis, float pos, float len) {
	if (axis == X_AXIS) {
	    return ForcedBreakWeight;
	}
	return BadBreakWeight;
    }

    public View breakView(int axis, int offset, float pos, float len) {
	return null;
    }

    /**
     * Provides a mapping from the document model coordinate space
     * to the coordinate space of the view mapped to it.
     *
     * @param pos the position to convert
     * @param a the allocated region to render into
     * @return the bounding box of the given position
     * @exception BadLocationException  if the given position does not
     * represent a valid location in the associated document
     * @see View#modelToView
     */
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
	int p0 = getStartOffset();
	int p1 = getEndOffset();
	if ((pos >= p0) && (pos <= p1)) {
	    Rectangle r = a.getBounds();
	    if (pos == p1) {
		r.x += r.width;
	    }
	    r.width = 0;
	    return r;
	}
	return null;
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param a the allocated region to render into
     * @return the location within the model that best represents the
     *  given point of view
     * @see View#viewToModel
     */
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
	Rectangle alloc = (Rectangle) a;
	if (x < alloc.x + (alloc.width / 2)) {
	    bias[0] = Position.Bias.Forward;
	    return getStartOffset();
	}
	bias[0] = Position.Bias.Backward;
	return getEndOffset();
    }

    /**
     * Fetches the attributes to use when rendering.  This is
     * implemented to multiplex the attributes specified in the
     * model with a StyleSheet.
     */
    public AttributeSet getAttributes() {
	return attr;
    }

    public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
	super.changedUpdate(changes, a, f);
	int pos = changes.getOffset();
	if (pos <= getStartOffset() && (pos + changes.getLength()) >=
	    getEndOffset()) {
	    setPropertiesFromAttributes();
	}
    }

    // --- variables ------------------------------------------------

    private Border bevel;
    private float topMargin;
    private float bottomMargin;
    private float leftMargin;
    private float rightMargin;
    private int alignment = StyleConstants.ALIGN_LEFT;
    private String noshade = null;
    private int size = 0;
    private CSS.LengthValue widthValue;

    private static final int SPACE_ABOVE = 3;
    private static final int SPACE_BELOW = 3;

    /** View Attributes. */
    private AttributeSet attr;
}

