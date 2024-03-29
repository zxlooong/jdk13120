/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.awt.*;
import javax.swing.event.*;
import javax.swing.SizeRequirements;

/**
 * View of a simple line-wrapping paragraph that supports
 * multiple fonts, colors, components, icons, etc.  It is
 * basically a vertical box with a margin around it.  The 
 * contents of the box are a bunch of rows which are special 
 * horizontal boxes.  This view creates a collection of
 * views that represent the child elements of the paragraph 
 * element.  Each of these views are placed into a row 
 * directly if they will fit, otherwise the <code>breakView</code>
 * method is called to try and carve the view into pieces
 * that fit.
 *
 * @author  Timothy Prinzing
 * @author  Scott Violet
 * @version 1.77 02/06/02
 * @see     View
 */
public class ParagraphView extends FlowView implements TabExpander {

    /**
     * Constructs a ParagraphView for the given element.
     *
     * @param elem the element that this view is responsible for
     */
    public ParagraphView(Element elem) {
	super(elem, View.Y_AXIS);
	setPropertiesFromAttributes();
	Document doc = elem.getDocument();
	Object i18nFlag = doc.getProperty(AbstractDocument.I18NProperty);
	if ((i18nFlag != null) && i18nFlag.equals(Boolean.TRUE)) {
	    try {
		if (i18nStrategy == null) {
		    // the classname should probably come from a property file.
		    String classname = "javax.swing.text.TextLayoutStrategy"; 
		    ClassLoader loader = getClass().getClassLoader();
		    if (loader != null) {
			i18nStrategy = loader.loadClass(classname);
		    } else {
			i18nStrategy = Class.forName(classname);
		    }
		}
		Object o = i18nStrategy.newInstance();
		if (o instanceof FlowStrategy) {
		    strategy = (FlowStrategy) o;
		}
	    } catch (Throwable e) {
		throw new StateInvariantError("ParagraphView: Can't create i18n strategy: " 
					      + e.getMessage());
	    }
	}
    }

    /**
     * Set the type of justification.
     */
    protected void setJustification(int j) {
	justification = j;
    }

    /**
     * Set the line spacing.
     *
     * @param ls the value in points
     */
    protected void setLineSpacing(float ls) {
	lineSpacing = ls;
    }

    /**
     * Set the indent on the first line
     *
     * @param ls the value in points
     */
    protected void setFirstLineIndent(float fi) {
	firstLineIndent = (int) fi;
    }

    protected void setPropertiesFromAttributes() {
	AttributeSet attr = getAttributes();
	if (attr != null) {
	    setParagraphInsets(attr);
	    setJustification(StyleConstants.getAlignment(attr));
	    lineSpacing = StyleConstants.getLineSpacing(attr);
	    firstLineIndent = (int)StyleConstants.getFirstLineIndent(attr);
	}
    }

    /**
     * The child views of the paragraph are rows which
     * have been used to arrange pieces of the Views that
     * represent the child elements.  This is the number 
     * of views that have been tiled in two dimensions,
     * and should be equivalent to the number of child elements
     * to the element this view is responsible for.
     */
    protected int getLayoutViewCount() {
	return layoutPool.getViewCount();
    }

    /**
     * The child views of the paragraph are rows which
     * have been used to arrange pieces of the Views that
     * represent the child elements.  This methods returns
     * the view responsible for the child element index
     * (prior to breaking).  These are the Views that were
     * produced from a factory (to represent the child
     * elements) and used for layout.
     */
    protected View getLayoutView(int index) {
	return layoutPool.getView(index);
    }

    /**
     * Adjusts the given row if possible to fit within the
     * layout span.  By default this will try to find the 
     * highest break weight possible nearest the end of
     * the row.  If a forced break is encountered, the
     * break will be positioned there.
     * 
     * @param r the row to adjust to the current layout
     *  span.
     * @param desiredSpan the current layout span >= 0
     * @param x the location r starts at.
     */
    protected void adjustRow(Row r, int desiredSpan, int x) {
    }

    /**
     * Overriden from CompositeView.
     */
    protected int getNextNorthSouthVisualPositionFrom(int pos, Position.Bias b,
						      Shape a, int direction,
						      Position.Bias[] biasRet)
	                                        throws BadLocationException {
	int vIndex;
	if(pos == -1) {
	    vIndex = (direction == NORTH) ?
		     getViewCount() - 1 : 0;
	}
	else {
	    if(b == Position.Bias.Backward && pos > 0) {
		vIndex = getViewIndexAtPosition(pos - 1);
	    }
	    else {
		vIndex = getViewIndexAtPosition(pos);
	    }
	    if(direction == NORTH) {
		if(vIndex == 0) {
		    return -1;
		}
		vIndex--;
	    }
	    else if(++vIndex >= getViewCount()) {
		return -1;
	    }
	}
	// vIndex gives index of row to look in.
	JTextComponent text = (JTextComponent)getContainer();
	Caret c = text.getCaret();
	Point magicPoint;
	magicPoint = (c != null) ? c.getMagicCaretPosition() : null;
	int x;
	if(magicPoint == null) {
	    Shape posBounds = text.getUI().modelToView(text, pos, b);
	    if(posBounds == null) {
		x = 0;
	    }
	    else {
		x = posBounds.getBounds().x;
	    }
	}
	else {
	    x = magicPoint.x;
	}
	return getClosestPositionTo(pos, b, a, direction, biasRet, vIndex, x);
    }

    /**
     * Returns the closest model position to <code>x</code>.
     * <code>rowIndex</code> gives the index of the view that corresponds
     * that should be looked in.
     */
    // NOTE: This will not properly work if ParagraphView contains
    // other ParagraphViews. It won't raise, but this does not message
    // the children views with getNextVisualPositionFrom.
    protected int getClosestPositionTo(int pos, Position.Bias b, Shape a,
				       int direction, Position.Bias[] biasRet,
				       int rowIndex, int x)
	      throws BadLocationException {
	JTextComponent text = (JTextComponent)getContainer();
	Document doc = getDocument();
	AbstractDocument aDoc = (doc instanceof AbstractDocument) ?
	                        (AbstractDocument)doc : null;
	View row = getView(rowIndex);
	int lastPos = -1;
	// This could be made better to check backward positions too.
	biasRet[0] = Position.Bias.Forward;
	for(int vc = 0, numViews = row.getViewCount(); vc < numViews; vc++) {
	    View v = row.getView(vc);
	    int start = v.getStartOffset();
	    boolean ltr = (aDoc != null) ? aDoc.isLeftToRight
		           (start, start + 1) : true;
	    if(ltr) {
		lastPos = start;
		for(int end = v.getEndOffset(); lastPos < end; lastPos++) {
		    if(text.modelToView(lastPos).getBounds().x >= x) {
			return lastPos;
		    }
		}
		lastPos--;
	    }
	    else {
		for(lastPos = v.getEndOffset() - 1; lastPos >= start;
		    lastPos--) {
		    if(text.modelToView(lastPos).getBounds().x >= x) {
			return lastPos;
		    }
		}
		lastPos++;
	    }
	}
	if(lastPos == -1) {
	    return getStartOffset();
	}
	return lastPos;
    }

    protected boolean flipEastAndWestAtEnds(int position,
					    Position.Bias bias) {
	Document doc = getDocument();
	if(doc instanceof AbstractDocument &&
	   !((AbstractDocument)doc).isLeftToRight(getStartOffset(),
						  getStartOffset() + 1)) {
	    return true;
	}
	return false;
    }

    // --- FlowView methods ---------------------------------------------

    /**
     * Fetch the constraining span to flow against for
     * the given child index.
     */
    public int getFlowSpan(int index) {
	View child = getView(index);
	int adjust = 0;
	if (child instanceof Row) {
	    Row row = (Row) child;
	    adjust = row.getLeftInset() + row.getRightInset();
	}
	int span = layoutSpan - adjust;
	return span;
    }

    /**
     * Fetch the location along the flow axis that the
     * flow span will start at.
     */
    public int getFlowStart(int index) {
	View child = getView(index);
	int adjust = 0;
	if (child instanceof Row) {
	    Row row = (Row) child;
	    adjust = row.getLeftInset();
	}
	return tabBase + adjust;
    }

    /**
     * Create a View that should be used to hold a 
     * a rows worth of children in a flow.
     */
    protected View createRow() {
	Element elem = getElement();
	Row row = new Row(elem);

	// Adjust for line spacing
	if(lineSpacing > 1) {
	    float height = row.getPreferredSpan(View.Y_AXIS);
	    float addition = (height * lineSpacing) - height;
	    if(addition > 0) {
		row.setInsets(row.getTopInset(), row.getLeftInset(),
			      (short) addition, row.getRightInset());
	    }
	}

	return row;
    }
	
    // --- TabExpander methods ------------------------------------------

    /**
     * Returns the next tab stop position given a reference position.
     * This view implements the tab coordinate system, and calls
     * <code>getTabbedSpan</code> on the logical children in the process 
     * of layout to determine the desired span of the children.  The
     * logical children can delegate their tab expansion upward to
     * the paragraph which knows how to expand tabs. 
     * <code>LabelView</code> is an example of a view that delegates
     * its tab expansion needs upward to the paragraph.
     * <p>
     * This is implemented to try and locate a <code>TabSet</code>
     * in the paragraph element's attribute set.  If one can be
     * found, its settings will be used, otherwise a default expansion
     * will be provided.  The base location for for tab expansion
     * is the left inset from the paragraphs most recent allocation
     * (which is what the layout of the children is based upon).
     *
     * @param x the X reference position
     * @param tabOffset the position within the text stream
     *   that the tab occurred at >= 0.
     * @return the trailing end of the tab expansion >= 0
     * @see TabSet
     * @see TabStop
     * @see LabelView
     */
    public float nextTabStop(float x, int tabOffset) {
	// If the text isn't left justified, offset by 10 pixels!
	if(justification != StyleConstants.ALIGN_LEFT)
            return x + 10.0f;
        x -= tabBase;
        TabSet tabs = getTabSet();
        if(tabs == null) {
            // a tab every 72 pixels.
            return (float)(tabBase + (((int)x / 72 + 1) * 72));
        }
        TabStop tab = tabs.getTabAfter(x + .01f);
        if(tab == null) {
            // no tab, do a default of 5 pixels.
            // Should this cause a wrapping of the line?
            return tabBase + x + 5.0f;
        }
        int alignment = tab.getAlignment();
        int offset;
        switch(alignment) {
        default:
        case TabStop.ALIGN_LEFT:
            // Simple case, left tab.
            return tabBase + tab.getPosition();
        case TabStop.ALIGN_BAR:
            // PENDING: what does this mean?
            return tabBase + tab.getPosition();
        case TabStop.ALIGN_RIGHT:
        case TabStop.ALIGN_CENTER:
            offset = findOffsetToCharactersInString(tabChars,
                                                    tabOffset + 1);
            break;
        case TabStop.ALIGN_DECIMAL:
            offset = findOffsetToCharactersInString(tabDecimalChars,
                                                    tabOffset + 1);
            break;
        }
        if (offset == -1) {
            offset = getEndOffset();
        }
        float charsSize = getPartialSize(tabOffset + 1, offset);
        switch(alignment) {
        case TabStop.ALIGN_RIGHT:
        case TabStop.ALIGN_DECIMAL:
            // right and decimal are treated the same way, the new
            // position will be the location of the tab less the
            // partialSize.
            return tabBase + Math.max(x, tab.getPosition() - charsSize);
        case TabStop.ALIGN_CENTER: 
            // Similar to right, but half the partialSize.
            return tabBase + Math.max(x, tab.getPosition() - charsSize / 2.0f);
        }
        // will never get here!
        return x;
    }

    /**
     * Gets the Tabset to be used in calculating tabs.
     *
     * @return the TabSet
     */
    protected TabSet getTabSet() {
	return StyleConstants.getTabSet(getElement().getAttributes());
    }

    /**
     * Returns the size used by the views between <code>startOffset</code>
     * and <code>endOffset</code>. This uses getPartialView to calculate the
     * size if the child view implements the TabableView interface. If a 
     * size is needed and a View does not implement the TabableView
     * interface, the preferredSpan will be used.
     *
     * @param startOffset the starting document offset >= 0
     * @param endOffset the ending document offset >= startOffset
     * @return the size >= 0
     */
    protected float getPartialSize(int startOffset, int endOffset) {
        float size = 0.0f;
        int viewIndex;
        int numViews = getViewCount();
        View view;
        int viewEnd;
        int tempEnd;

        // Have to search layoutPool!
        // PENDING: when ParagraphView supports breaking location
        // into layoutPool will have to change!
        viewIndex = getElement().getElementIndex(startOffset);
        numViews = layoutPool.getViewCount();
        while(startOffset < endOffset && viewIndex < numViews) {
            view = layoutPool.getView(viewIndex++);
            viewEnd = view.getEndOffset();
            tempEnd = Math.min(endOffset, viewEnd);
            if(view instanceof TabableView)
                size += ((TabableView)view).getPartialSpan(startOffset, tempEnd);
            else if(startOffset == view.getStartOffset() &&
                    tempEnd == view.getEndOffset())
                size += view.getPreferredSpan(View.X_AXIS);
            else
                // PENDING: should we handle this better?
                return 0.0f;
            startOffset = viewEnd;
        }
        return size;
    }

    /**
     * Finds the next character in the document with a character in
     * <code>string</code>, starting at offset <code>start</code>. If
     * there are no characters found, -1 will be returned.
     *
     * @param string the string of characters
     * @param start where to start in the model >= 0
     * @return the document offset or -1
     */
    protected int findOffsetToCharactersInString(char[] string,
                                                 int start) {
        int stringLength = string.length;
        int end = getEndOffset();
        Segment seg = new Segment();
        try {
            getDocument().getText(start, end - start, seg);
        } catch (BadLocationException ble) {
            return -1;
        }
        for(int counter = seg.offset, maxCounter = seg.offset + seg.count;
            counter < maxCounter; counter++) {
            char currentChar = seg.array[counter];
            for(int subCounter = 0; subCounter < stringLength;
                subCounter++) {
                if(currentChar == string[subCounter])
                    return counter - seg.offset + start;
            }
        }
        // No match.
        return -1;
    }

    /**
     * @return where tabs are calculated from.
     */
    protected float getTabBase() {
	return (float)tabBase;
    }

    // ---- View methods ----------------------------------------------------

    /**
     * Renders using the given rendering surface and area on that
     * surface.  This is implemented to delgate to the superclass
     * after stashing the base coordinate for tab calculations.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     * @see View#paint
     */
    public void paint(Graphics g, Shape a) {
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
        tabBase = alloc.x + getLeftInset();
        super.paint(g, a);
    }

    /**
     * Determines the desired alignment for this view along an
     * axis.  This is implemented to give the alignment to the
     * center of the first row along the y axis, and the default
     * along the x axis.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns the desired alignment.  This should be a value
     *   between 0.0 and 1.0 inclusive, where 0 indicates alignment at the
     *   origin and 1.0 indicates alignment to the full span
     *   away from the origin.  An alignment of 0.5 would be the
     *   center of the view.
     */
    public float getAlignment(int axis) {
        switch (axis) {
        case Y_AXIS:
	    float a = 0.5f;
	    if (getViewCount() != 0) {
		int paragraphSpan = (int) getPreferredSpan(View.Y_AXIS);
		View v = getView(0);
		int rowSpan = (int) v.getPreferredSpan(View.Y_AXIS);
		a = (paragraphSpan != 0) ? ((float)(rowSpan / 2)) / paragraphSpan : 0;
	    }
            return a;
	case X_AXIS:
	    return 0.5f;
	default:
            throw new IllegalArgumentException("Invalid axis: " + axis);
	}
    }

    /**
     * Breaks this view on the given axis at the given length.<p>
     * ParagraphView instances are breakable along the Y_AXIS only, and only if
     * <code>len</code> is after the first line.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param len specifies where a potential break is desired
     *  along the given axis >= 0
     * @param a the current allocation of the view
     * @return the fragment of the view that represents the
     *  given span, if the view can be broken.  If the view
     *  doesn't support breaking behavior, the view itself is
     *  returned.
     * @see View#breakView
     */
    public View breakView(int axis, float len, Shape a) {
        if(axis == View.Y_AXIS) {
            if(a != null) {
                Rectangle alloc = a.getBounds();
                setSize(alloc.width, alloc.height);
            }
            // Determine what row to break on.

            // PENDING(prinz) add break support
            return this;
        }
        return this;
    }

    /**
     * Gets the break weight for a given location.
     * ParagraphView instances are breakable along the Y_AXIS only, and 
     * only if <code>len</code> is after the first row.  If the length
     * is less than one row, a value of BadBreakWeight is returned.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param len specifies where a potential break is desired >= 0
     * @return a value indicating the attractiveness of breaking here
     * @see View#getBreakWeight
     */
    public int getBreakWeight(int axis, float len) {
        if(axis == View.Y_AXIS) {
            // PENDING(prinz) make this return a reasonable value
            // when paragraph breaking support is re-implemented.
            // If less than one row, bad weight value should be 
            // returned.
            //return GoodBreakWeight;
            return BadBreakWeight;
        }
        return BadBreakWeight;
    }

    /**
     * Gives notification from the document that attributes were changed
     * in a location that this view is responsible for.
     *
     * @param changes the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#changedUpdate
     */
    public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        // update any property settings stored, and layout should be 
	// recomputed 
	setPropertiesFromAttributes();
	layoutChanged(X_AXIS);
	layoutChanged(Y_AXIS);
	super.changedUpdate(changes, a, f);
    }

    
    // --- variables -----------------------------------------------

    private int justification;
    private float lineSpacing;
    /** Indentation for the first line, from the left inset. */
    protected int firstLineIndent;

    /**
     * Used by the TabExpander functionality to determine
     * where to base the tab calculations.  This is basically
     * the location of the left side of the paragraph.
     */
    private int tabBase;

    /**
     * Used to create an i18n-based layout strategy
     */
    static Class i18nStrategy;

    /** Used for searching for a tab. */
    static char[] tabChars;
    /** Used for searching for a tab or decimal character. */
    static char[] tabDecimalChars;

    static {
        tabChars = new char[1];
        tabChars[0] = '\t';
        tabDecimalChars = new char[2];
        tabDecimalChars[0] = '\t';
        tabDecimalChars[1] = '.';
    }

    /**
     * Internally created view that has the purpose of holding
     * the views that represent the children of the paragraph
     * that have been arranged in rows.
     */
    class Row extends BoxView {

        Row(Element elem) {
            super(elem, View.X_AXIS);
        }

        /**
         * This is reimplemented to do nothing since the
         * paragraph fills in the row with its needed
         * children.
         */
        protected void loadChildren(ViewFactory f) {
        }

	/**
	 * Fetches the attributes to use when rendering.  This view
	 * isn't directly responsible for an element so it returns
	 * the outer classes attributes.
	 */
        public AttributeSet getAttributes() {
	    View p = getParent();
	    return (p != null) ? p.getAttributes() : null;
	}

        public float getAlignment(int axis) {
            if (axis == View.X_AXIS) {
                switch (justification) {
                case StyleConstants.ALIGN_LEFT:
                    return 0;
                case StyleConstants.ALIGN_RIGHT:
                    return 1;
                case StyleConstants.ALIGN_CENTER:
                case StyleConstants.ALIGN_JUSTIFIED:
                    return 0.5f;
                }
            }
            return super.getAlignment(axis);
        }

        /**
         * Provides a mapping from the document model coordinate space
         * to the coordinate space of the view mapped to it.  This is
         * implemented to let the superclass find the position along 
         * the major axis and the allocation of the row is used 
         * along the minor axis, so that even though the children 
         * are different heights they all get the same caret height.
         *
         * @param pos the position to convert
         * @param a the allocated region to render into
         * @return the bounding box of the given position
         * @exception BadLocationException  if the given position does not represent a
         *   valid location in the associated document
         * @see View#modelToView
         */
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            Rectangle r = a.getBounds();
	    View v = getViewAtPosition(pos, r);
	    if ((v != null) && (!v.getElement().isLeaf())) {
		// Don't adjust the height if the view represents a branch.
		return super.modelToView(pos, a, b);
	    }
	    r = a.getBounds();
            int height = r.height;
            int y = r.y;
            Shape loc = super.modelToView(pos, a, b);
            r = loc.getBounds();
            r.height = height;
            r.y = y;
            return r;
        }

        /**
         * Range represented by a row in the paragraph is only
         * a subset of the total range of the paragraph element.
         * @see View#getRange
         */
        public int getStartOffset() {
	    int offs = Integer.MAX_VALUE;
            int n = getViewCount();
	    for (int i = 0; i < n; i++) {
		View v = getView(i);
		offs = Math.min(offs, v.getStartOffset());
	    }
            return offs;
        }

        public int getEndOffset() {
	    int offs = 0;
            int n = getViewCount();
	    for (int i = 0; i < n; i++) {
		View v = getView(i);
		offs = Math.max(offs, v.getEndOffset());
	    }
            return offs;
        }

	/**
	 * Perform layout for the minor axis of the box (i.e. the
	 * axis orthoginal to the axis that it represents).  The results 
	 * of the layout should be placed in the given arrays which represent 
	 * the allocations to the children along the minor axis.
	 * <p>
	 * This is implemented to do a baseline layout of the children
	 * by calling BoxView.baselineLayout.
	 *
	 * @param targetSpan the total span given to the view, which
	 *  whould be used to layout the children.
	 * @param axis the axis being layed out.
	 * @param offsets the offsets from the origin of the view for
	 *  each of the child views.  This is a return value and is
	 *  filled in by the implementation of this method.
	 * @param spans the span of each child view.  This is a return
	 *  value and is filled in by the implementation of this method.
	 * @returns the offset and span for each child view in the
	 *  offsets and spans parameters.
	 */
        protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
	    baselineLayout(targetSpan, axis, offsets, spans);
	}

        protected SizeRequirements calculateMinorAxisRequirements(int axis, 
								  SizeRequirements r) {
	    return baselineRequirements(axis, r);
	}

	/**
	 * Fetches the child view index representing the given position in
	 * the model.
	 *
	 * @param pos the position >= 0
	 * @returns  index of the view representing the given position, or 
	 *   -1 if no view represents that position
	 */
	protected int getViewIndexAtPosition(int pos) {
	    // This is expensive, but are views are not necessarily layed
	    // out in model order.
	    if(pos < getStartOffset() || pos >= getEndOffset())
		return -1;
	    for(int counter = getViewCount() - 1; counter >= 0; counter--) {
		View v = getView(counter);
		if(pos >= v.getStartOffset() &&
		   pos < v.getEndOffset()) {
		    return counter;
		}
	    }
	    return -1;
	}
    }

}
