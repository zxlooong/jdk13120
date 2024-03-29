/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.awt.*;
import java.text.BreakIterator;
import javax.swing.event.*;

/**
 * A GlyphView is a styled chunk of text that represents a view
 * mapped over an element in the text model. This view is generally 
 * responsible for displaying text glyphs using character level 
 * attributes in some way.
 * An implementation of the GlyphPainter class is used to do the
 * actual rendering and model/view translations.  This separates
 * rendering from layout and management of the association with
 * the model.
 * <p>
 * The view supports breaking for the purpose of formatting.   
 * The fragments produced by breaking share the view that has 
 * primary responsibility for the element (i.e. they are nested 
 * classes and carry only a small amount of state of their own) 
 * so they can share its resources.
 * <p>
 * Since this view 
 * represents text that may have tabs embedded in it, it implements the
 * <code>TabableView</code> interface.  Tabs will only be
 * expanded if this view is embedded in a container that does
 * tab expansion.  ParagraphView is an example of a container
 * that does tab expansion.
 * <p>
 *
 * @author  Timothy Prinzing
 * @version 1.17 02/06/02
 */
public class GlyphView extends View implements TabableView, Cloneable {

    /**
     * Constructs a new view wrapped on an element.
     *
     * @param elem the element
     */
    public GlyphView(Element elem) {
	super(elem);
	text = new Segment();
	offset = 0;
	length = 0;
    }

    /**
     * Creates a shallow copy.  This is used by the
     * createFragment and breakView methods.
     *
     * @return the copy
     */
    protected final Object clone() {
	Object o;
	try {
	    o = super.clone();
	} catch (CloneNotSupportedException cnse) {
	    o = null;
	}
	return o;
    }

    /**
     * Fetch the currently installed glyph painter.
     * If a painter has not yet been installed, and
     * a default was not yet needed, null is returned.
     */
    public GlyphPainter getGlyphPainter() {
	return painter;
    }

    /**
     * Sets the painter to use for rendering glyphs.
     */
    public void setGlyphPainter(GlyphPainter p) {
	painter = p;
    }

    /**
     * Fetch a reference to the text that occupies
     * the given range.  This is normally used by
     * the GlyphPainter to determine what characters
     * it should render glyphs for.
     */
     public Segment getText(int p0, int p1) {
	try {
	    Document doc = getDocument();
	    doc.getText(p0, p1 - p0, text);
	} catch (BadLocationException bl) {
	    throw new StateInvariantError("GlyphView: Stale view: " + bl);
	}
	return text;
    }

    /**
     * Fetch the background color to use to render the
     * glyphs.  If there is no background color, null should
     * be returned.  This is implemented to call 
     * <code>StyledDocument.getBackground</code> if the associated
     * document is a styled document, otherwise it returns null.
     */
    public Color getBackground() {
	Document doc = getDocument();
	if (doc instanceof StyledDocument) {
	    AttributeSet attr = getAttributes();
	    if (attr.isDefined(StyleConstants.Background)) {
		return ((StyledDocument)doc).getBackground(attr);
	    }
	}
	return null;
    }

    /**
     * Fetch the foreground color to use to render the
     * glyphs.  If there is no foreground color, null should
     * be returned.  This is implemented to call
     * <code>StyledDocument.getBackground</code> if the associated
     * document is a StyledDocument.  If the associated document
     * is not a StyledDocument, the associated components foreground
     * color is used.  If there is no associated component, null 
     * is returned.
     */
    public Color getForeground() {
	Document doc = getDocument();
	if (doc instanceof StyledDocument) {
	    AttributeSet attr = getAttributes();
	    return ((StyledDocument)doc).getForeground(attr);
	}
	Component c = getContainer();
	if (c != null) {
	    return c.getForeground();
	}
	return null;
    }

    /**
     * Fetch the font that the glyphs should be based
     * upon.  This is implemented to call
     * <code>StyledDocument.getFont</code> if the associated
     * document is a StyledDocument.  If the associated document
     * is not a StyledDocument, the associated components font
     * is used.  If there is no associated component, null 
     * is returned.
     */
    public Font getFont() {
	Document doc = getDocument();
	if (doc instanceof StyledDocument) {
	    AttributeSet attr = getAttributes();
	    return ((StyledDocument)doc).getFont(attr);
	}
	Component c = getContainer();
	if (c != null) {
	    return c.getFont();
	}
	return null;
    }
    
    /**
     * Determine if the glyphs should be underlined.  If true,
     * an underline should be drawn through the baseline.
     */
    public boolean isUnderline() {
	AttributeSet attr = getAttributes();
	return StyleConstants.isUnderline(attr);
    }

    /**
     * Determine if the glyphs should have a strikethrough
     * line.  If true, a line should be drawn through the center
     * of the glyphs.
     */
    public boolean isStrikeThrough() {
	AttributeSet attr = getAttributes();
	return StyleConstants.isStrikeThrough(attr);
    }

    /**
     * Determine if the glyphs should be rendered as superscript.
     */
    public boolean isSubscript() {
	AttributeSet attr = getAttributes();
	return StyleConstants.isSubscript(attr);
    }

    /**
     * Determine if the glyphs should be rendered as subscript.
     */
    public boolean isSuperscript() {
	AttributeSet attr = getAttributes();
	return StyleConstants.isSuperscript(attr);
    }

    /**
     * Fetch the TabExpander to use if tabs are present in this view.
     */
    public TabExpander getTabExpander() {
	return expander;
    }

    /**
     * Check to see that a glyph painter exists.  If a painter
     * doesn't exist, a default glyph painter will be installed.  
     */
    protected void checkPainter() {
	if (painter == null) {
	    if (defaultPainter == null) {
		// the classname should probably come from a property file.
		String classname = "javax.swing.text.GlyphPainter1"; 
		try {
		    Class c;
		    ClassLoader loader = getClass().getClassLoader();
		    if (loader != null) {
			c = loader.loadClass(classname);
		    } else {
		        c = Class.forName(classname);
		    }
		    Object o = c.newInstance();
		    if (o instanceof GlyphPainter) {
			defaultPainter = (GlyphPainter) o;
		    }
		} catch (Throwable e) {
		    throw new StateInvariantError("GlyphView: Can't load glyph painter: " 
						  + classname);
		}
	    }
	    setGlyphPainter(defaultPainter.getPainter(this, getStartOffset(), 
						      getEndOffset()));
	}
    }

    // --- TabableView methods --------------------------------------

    /**
     * Determines the desired span when using the given 
     * tab expansion implementation.  
     *
     * @param x the position the view would be located
     *  at for the purpose of tab expansion >= 0.
     * @param e how to expand the tabs when encountered.
     * @return the desired span >= 0
     * @see TabableView#getTabbedSpan
     */
    public float getTabbedSpan(float x, TabExpander e) {
	checkPainter();
	expander = e;
	this.x = (int) x;
	int p0 = getStartOffset();
	int p1 = getEndOffset();
	float width = painter.getSpan(this, p0, p1, expander, x);
	return width;
    }
    
    /**
     * Determines the span along the same axis as tab 
     * expansion for a portion of the view.  This is
     * intended for use by the TabExpander for cases
     * where the tab expansion involves aligning the
     * portion of text that doesn't have whitespace 
     * relative to the tab stop.  There is therefore
     * an assumption that the range given does not
     * contain tabs.
     * <p>
     * This method can be called while servicing the
     * getTabbedSpan or getPreferredSize.  It has to
     * arrange for its own text buffer to make the
     * measurements.
     *
     * @param p0 the starting document offset >= 0
     * @param p1 the ending document offset >= p0
     * @return the span >= 0
     */
    public float getPartialSpan(int p0, int p1) {
	checkPainter();
	float width = painter.getSpan(this, p0, p1, expander, x);
	return width;
    }

    // --- View methods ---------------------------------------------

    /**
     * Fetches the portion of the model that this view is responsible for.
     *
     * @return the starting offset into the model
     * @see View#getStartOffset
     */
    public int getStartOffset() {
	Element e = getElement();
	return (length > 0) ? e.getStartOffset() + offset : e.getStartOffset();
    }
    
    /**
     * Fetches the portion of the model that this view is responsible for.
     *
     * @return the ending offset into the model
     * @see View#getEndOffset
     */
    public int getEndOffset() {
	Element e = getElement();
	return (length > 0) ? e.getStartOffset() + offset + length : e.getEndOffset();
    }

    /**
     * Renders a portion of a text style run.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     */
    public void paint(Graphics g, Shape a) {
	checkPainter();

	boolean paintedText = false;
	Component c = getContainer();
	int p0 = getStartOffset();
	int p1 = getEndOffset();
	Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
	Color bg = getBackground();
	Color fg = getForeground();
	if (bg != null) {
	    g.setColor(bg);
	    g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
	}
	if (c instanceof JTextComponent) {
	    JTextComponent tc = (JTextComponent) c;
	    Highlighter h = tc.getHighlighter();
	    if (h instanceof LayeredHighlighter) {
		((LayeredHighlighter)h).paintLayeredHighlights
		    (g, p0, p1, a, tc, this);
	    }
	}

	if (Utilities.isComposedTextElement(getElement())) {
	    Utilities.paintComposedText(g, a.getBounds(), this);
	    paintedText = true;
	} else if(c instanceof JTextComponent) {
	    JTextComponent tc = (JTextComponent) c;
	    Color selFG = tc.getSelectedTextColor();
	    Caret caret = tc.getCaret();
	    if ((caret != null) && (! caret.isSelectionVisible())) {
		// selection currently not visible
		selFG = fg;
	    }

	    if(selFG != null && !selFG.equals(fg)) {
		int selStart = tc.getSelectionStart();
		int selEnd = tc.getSelectionEnd();

		if(selStart != selEnd) {
		    // Something is selected, does p0 - p1 fall in that range?
		    int pMin;
		    int pMax;

		    if(selStart <= p0)
			pMin = p0;
		    else
			pMin = Math.min(selStart, p1);
		    if(selEnd >= p1)
			pMax = p1;
		    else
			pMax = Math.max(selEnd, p0);
		    // If pMin == pMax (also == p0), selection isn't in this
		    // block.
		    if(pMin != pMax) {
			paintedText = true;
			if(pMin > p0) 
			    paintTextUsingColor(g, a, fg, p0, pMin);
			paintTextUsingColor(g, a, selFG, pMin, pMax);
			if(pMax < p1)
			    paintTextUsingColor(g, a, fg, pMax, p1);
		    }
		}
	    }
	}
	if(!paintedText)
	    paintTextUsingColor(g, a, fg, p0, p1);
    }

    /**
     * Paints the specified region of text in the specified color. 
     */
    final void paintTextUsingColor(Graphics g, Shape a, Color c, int p0, int p1) {
	// render the glyphs
	g.setColor(c);
	painter.paint(this, g, a, p0, p1);

	// render underline or strikethrough if set.
	boolean underline = isUnderline();
	boolean strike = isStrikeThrough();
	if (underline || strike) {
	    // calculate x coordinates
	    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
	    View parent = getParent();
	    if ((parent != null) && (parent.getEndOffset() == p1)) {
		// strip whitespace on end
		Segment s = getText(p0, p1);
		while ((s.count > 0) && (Character.isWhitespace(s.array[s.count-1]))) {
		    p1 -= 1;
		    s.count -= 1;
		}
	    }
	    int x0 = alloc.x;
	    int p = getStartOffset();
	    if (p != p0) {
		x0 += (int) painter.getSpan(this, p, p0, getTabExpander(), x0);
	    }
	    int x1 = x0 + (int) painter.getSpan(this, p0, p1, getTabExpander(), x0);

	    // calculate y coordinate
	    int d = (int) painter.getDescent(this);
	    int y = alloc.y + alloc.height - (int) painter.getDescent(this);
	    if (underline) {
		y += 1;
	    } else if (strike) {
		// move y coordinate above baseline
		y -= (int) (painter.getAscent(this) * 0.3f);
	    }
	    g.drawLine(x0, y, x1, y);
	}
    }

    /**
     * Determines the preferred span for this view along an
     * axis. 
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns  the span the view would like to be rendered into >= 0.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.  
     *           The parent may choose to resize or break the view.
     */
    public float getPreferredSpan(int axis) {
	checkPainter();
	int p0 = getStartOffset();
	int p1 = getEndOffset();
	switch (axis) {
	case View.X_AXIS:
	    float width = painter.getSpan(this, p0, p1, expander, this.x);
	    return Math.max(width, 1);
	case View.Y_AXIS:
	    float h = painter.getHeight(this);
	    if (isSuperscript()) {
		h += h/3;
	    }
	    return h;
	default:
	    throw new IllegalArgumentException("Invalid axis: " + axis);
	}
    }

    /**
     * Determines the desired alignment for this view along an
     * axis.  For the label, the alignment is along the font
     * baseline for the y axis, and the superclasses alignment
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
	checkPainter();
	if (axis == View.Y_AXIS) {
	    boolean sup = isSuperscript();
	    boolean sub = isSubscript();
	    float h = painter.getHeight(this);
	    float d = painter.getDescent(this);
	    float a = painter.getAscent(this);
	    float align;
	    if (sup) {
		align = 1.0f;
	    } else if (sub) {
		align = (h > 0) ? (h - (d + (a / 2))) / h : 0;
	    } else {
		align = (h > 0) ? (h - d) / h : 0;
	    }
	    return align;
	} 
	return super.getAlignment(axis);
    }

    /**
     * Provides a mapping from the document model coordinate space
     * to the coordinate space of the view mapped to it.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @return the bounding box of the given position
     * @exception BadLocationException  if the given position does not represent a
     *   valid location in the associated document
     * @see View#modelToView
     */
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
	checkPainter();
	return painter.modelToView(this, pos, b, a);
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.
     *
     * @param x the X coordinate >= 0
     * @param y the Y coordinate >= 0
     * @param a the allocated region to render into
     * @return the location within the model that best represents the
     *  given point of view >= 0
     * @see View#viewToModel
     */
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
	checkPainter();
	return painter.viewToModel(this, x, y, a, biasReturn);
    }

    /**
     * Determines how attractive a break opportunity in 
     * this view is.  This can be used for determining which
     * view is the most attractive to call <code>breakView</code>
     * on in the process of formatting.  The
     * higher the weight, the more attractive the break.  A
     * value equal to or lower than <code>View.BadBreakWeight</code>
     * should not be considered for a break.  A value greater
     * than or equal to <code>View.ForcedBreakWeight</code> should
     * be broken.
     * <p>
     * This is implemented to forward to the superclass for 
     * the Y_AXIS.  Along the X_AXIS the following values
     * may be returned.
     * <dl>
     * <dt><b>View.ExcellentBreakWeight</b>
     * <dd>if there is whitespace proceeding the desired break 
     *   location.  
     * <dt><b>View.BadBreakWeight</b>
     * <dd>if the desired break location results in a break
     *   location of the starting offset.
     * <dt><b>View.GoodBreakWeight</b>
     * <dd>if the other conditions don't occur.
     * </dl>
     * This will normally result in the behavior of breaking
     * on a whitespace location if one can be found, otherwise
     * breaking between characters.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param pos the potential location of the start of the 
     *   broken view >= 0.  This may be useful for calculating tab
     *   positions.
     * @param len specifies the relative length from <em>pos</em>
     *   where a potential break is desired >= 0.
     * @return the weight, which should be a value between
     *   View.ForcedBreakWeight and View.BadBreakWeight.
     * @see LabelView
     * @see ParagraphView
     * @see View#BadBreakWeight
     * @see View#GoodBreakWeight
     * @see View#ExcellentBreakWeight
     * @see View#ForcedBreakWeight
     */
    public int getBreakWeight(int axis, float pos, float len) {
	if (axis == View.X_AXIS) {
	    checkPainter();
	    int p0 = getStartOffset();
	    int p1 = painter.getBoundedPosition(this, p0, pos, len);
	    if (p1 == p0) {
		// can't even fit a single character
		return View.BadBreakWeight;	    
	    }
            if (getBreakSpot(p0, p1) != -1) {
                return View.ExcellentBreakWeight;
            }
	    // Nothing good to break on.
	    return View.GoodBreakWeight;
	}
	return super.getBreakWeight(axis, pos, len);
    }

    /**
     * Breaks this view on the given axis at the given length.
     * This is implemented to attempt to break on a whitespace
     * location, and returns a fragment with the whitespace at
     * the end.  If a whitespace location can't be found, the
     * nearest character is used.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param p0 the location in the model where the
     *  fragment should start it's representation >= 0.
     * @param pos the position along the axis that the
     *  broken view would occupy >= 0.  This may be useful for
     *  things like tab calculations.
     * @param len specifies the distance along the axis
     *  where a potential break is desired >= 0.  
     * @return the fragment of the view that represents the
     *  given span, if the view can be broken.  If the view
     *  doesn't support breaking behavior, the view itself is
     *  returned.
     * @see View#breakView
     */
    public View breakView(int axis, int p0, float pos, float len) {
	if (axis == View.X_AXIS) {
	    checkPainter();
	    int p1 = painter.getBoundedPosition(this, p0, pos, len);
            int breakSpot = getBreakSpot(p0, p1);

            if (breakSpot != -1) {
                p1 = breakSpot;
            }
            // else, no break in the region, return a fragment of the
            // bounded region.
            if (p0 == getStartOffset() && p1 == getEndOffset()) {
                return this;
            }
	    GlyphView v = (GlyphView) createFragment(p0, p1);
	    v.x = (int) pos;
	    return v;
	}
	return this;
    }

    /**
     * Returns a location to break at in the passed in region, or -1 if
     * there isn't a good location to break at in the specified region.
     */
    private int getBreakSpot(int p0, int p1) {
        Document doc = getDocument();

        if (doc != null && Boolean.TRUE.equals(doc.getProperty(
                                   AbstractDocument.MultiByteProperty))) {
            return getBreakSpotUseBreakIterator(p0, p1);
        }
        return getBreakSpotUseWhitespace(p0, p1);
    }

    /**
     * Returns the appropriate place to break based on the last whitespace
     * character encountered.
     */
    private int getBreakSpotUseWhitespace(int p0, int p1) {
        Segment s = getText(p0, p1);

        for (char ch = s.last(); ch != Segment.DONE; ch = s.previous()) {
            if (Character.isWhitespace(ch)) {
                // found whitespace
                return s.getIndex() - s.getBeginIndex() + 1 + p0;
            }
        }
        return -1;
    }
     
    /**
     * Returns the appropriate place to break based on BreakIterator.
     */
    private int getBreakSpotUseBreakIterator(int p0, int p1) {
        // Certain regions require context for BreakIterator, start from
        // our parents start offset.
        Element parent = getElement().getParentElement();
        int parent0;
        int parent1;
        Container c = getContainer();
        BreakIterator breaker;

        if (parent == null) {
            parent0 = p0;
            parent1 = p1;
        }
        else {
            parent0 = parent.getStartOffset();
            parent1 = parent.getEndOffset();
        }
        if (c != null) {
            try {
                breaker = BreakIterator.getLineInstance(c.getLocale());
            } catch (IllegalComponentStateException icse) {
                breaker = BreakIterator.getLineInstance();
            }
        }
        else {
            breaker = BreakIterator.getLineInstance();
        }

        Segment s = getText(parent0, parent1);
        int breakPoint;

        // Needed to initialize the Segment.
        s.first();
        breaker.setText(s);

        if (p1 == parent1) {
            // This will most likely return the end, the assumption is
            // that if parent1 == p1, then we are the last portion of
            // a paragraph
            breakPoint = breaker.last();
        }
        else if (p1 + 1 == parent1) {
            // assert(s.count > 1)
            breakPoint = breaker.next(s.offset + s.count - 2);
            if (breakPoint >= s.count + s.offset) {
                breakPoint = breaker.preceding(s.offset + s.count - 1);
            }
        }
        else {
            breakPoint = breaker.preceding(p1 - parent0 + s.offset + 1);
        }

        if (breakPoint != BreakIterator.DONE) {
            breakPoint = breakPoint - s.offset + parent0;
            if (breakPoint > p0) {
                if (p0 == parent0 && breakPoint == p0) {
                    return -1;
                }
                if (breakPoint <= p1) {
                    return breakPoint;
                }
            }
        }
        return -1;
    }

    /**
     * Creates a view that represents a portion of the element.
     * This is potentially useful during formatting operations
     * for taking measurements of fragments of the view.  If 
     * the view doesn't support fragmenting (the default), it 
     * should return itself.  
     * <p>
     * This view does support fragmenting.  It is implemented
     * to return a nested class that shares state in this view 
     * representing only a portion of the view.
     *
     * @param p0 the starting offset >= 0.  This should be a value
     *   greater or equal to the element starting offset and
     *   less than the element ending offset.
     * @param p1 the ending offset > p0.  This should be a value
     *   less than or equal to the elements end offset and
     *   greater than the elements starting offset.
     * @returns the view fragment, or itself if the view doesn't
     *   support breaking into fragments.
     * @see LabelView
     */
    public View createFragment(int p0, int p1) {
	checkPainter();
	Element elem = getElement();
	GlyphView v = (GlyphView) clone();
	v.offset = (short) (p0 - elem.getStartOffset());
	v.length = (short) (p1 - p0);
	v.painter = painter.getPainter(v, p0, p1);
	return v;
    }

    /**
     * Provides a way to determine the next visually represented model
     * location that one might place a caret.  Some views may not be
     * visible, they might not be in the same order found in the model, or
     * they just might not allow access to some of the locations in the
     * model.
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
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, 
					 int direction,
					 Position.Bias[] biasRet) 
	throws BadLocationException {

        return painter.getNextVisualPositionFrom(this, pos, b, a, direction, biasRet);
    }

    /**
     * Gives notification that something was inserted into 
     * the document in a location that this view is responsible for.  
     * This is implemented to call preferenceChanged along the
     * axis the glyphs are rendered.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#insertUpdate
     */
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
	preferenceChanged(null, true, false);
    }

    /**
     * Gives notification that something was removed from the document
     * in a location that this view is responsible for.
     * This is implemented to call preferenceChanged along the
     * axis the glyphs are rendered.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#removeUpdate
     */
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
	preferenceChanged(null, true, false);
    }

    /**
     * Gives notification from the document that attributes were changed
     * in a location that this view is responsible for.
     * This is implemented to call preferenceChanged along both the
     * horizontal and vertical axis.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#changedUpdate
     */
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
	preferenceChanged(null, true, true);
    }

    // --- variables ------------------------------------------------

    Segment text;
    short offset;
    short length;

    /**
     * how to expand tabs
     */
    TabExpander expander;

    /**
     * location for determining tab expansion against.
     */
    int x;

    /**
     * Glyph rendering functionality.
     */
    GlyphPainter painter;

    /**
     * The prototype painter used by default.
     */
    static GlyphPainter defaultPainter;

    /**
     * A class to perform rendering of the glyphs.
     * This can be implemented to be stateless, or
     * to hold some information as a cache to 
     * facilitate faster rendering and model/view
     * translation.  At a minimum, the GlyphPainter
     * allows a View implementation to perform its
     * duties independant of a particular version
     * of JVM and selection of capabilities (i.e.
     * shaping for i18n, etc).
     */
    public static abstract class GlyphPainter {

	/**
	 * Determine the span the glyphs given a start location
	 * (for tab expansion).
	 */
	public abstract float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x);

	public abstract float getHeight(GlyphView v);

	public abstract float getAscent(GlyphView v);

	public abstract float getDescent(GlyphView v);

	/**
	 * Paint the glyphs representing the given range.
	 */
        public abstract void paint(GlyphView v, Graphics g, Shape a, int p0, int p1);

	/**
	 * Provides a mapping from the document model coordinate space
	 * to the coordinate space of the view mapped to it.
	 * This is shared by the broken views.
	 *
	 * @param pos the position to convert
	 * @param a the allocated region to render into
	 * @param rightToLeft true if the text is rendered right to left.
	 * @return the bounding box of the given position
	 * @exception BadLocationException  if the given position does not represent a
	 *   valid location in the associated document
	 * @see View#modelToView
	 */
	public abstract Shape modelToView(GlyphView v, 
					  int pos, Position.Bias bias,
					  Shape a) throws BadLocationException;

	/**
	 * Provides a mapping from the view coordinate space to the logical
	 * coordinate space of the model.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param a the allocated region to render into
	 * @param rightToLeft true if the text is rendered right to left
	 * @return the location within the model that best represents the
	 *  given point of view
	 * @see View#viewToModel
	 */
        public abstract int viewToModel(GlyphView v, 
					float x, float y, Shape a, 
					Position.Bias[] biasReturn);

	/**
	 * Determines the model location that represents the
	 * maximum advance that fits within the given span.
	 * This could be used to break the given view.  The result 
	 * should be a location just shy of the given advance.  This
	 * differs from viewToModel which returns the closest
	 * position which might be proud of the maximum advance.
	 *
	 * @param v the view to find the model location to break at.
	 * @param p0 the location in the model where the
	 *  fragment should start it's representation >= 0.
	 * @param pos the graphic location along the axis that the
	 *  broken view would occupy >= 0.  This may be useful for
	 *  things like tab calculations.
	 * @param len specifies the distance into the view
	 *  where a potential break is desired >= 0.  
	 * @return the maximum model location possible for a break.
	 * @see View#breakView
	 */
        public abstract int getBoundedPosition(GlyphView v, int p0, float x, float len);

	/**
	 * Create a painter to use for the given GlyphView.  If 
	 * the painter carries state it can create another painter
	 * to represent a new GlyphView that is being created.  If
	 * the painter doesn't hold any significant state, it can
	 * return itself.  The default behavior is to return itself.
	 */
        public GlyphPainter getPainter(GlyphView v, int p0, int p1) {
	    return this;
	}

	/**
	 * Provides a way to determine the next visually represented model
	 * location that one might place a caret.  Some views may not be
	 * visible, they might not be in the same order found in the model, or
	 * they just might not allow access to some of the locations in the
	 * model.
	 *
	 * @param v the view to use
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
        public int getNextVisualPositionFrom(GlyphView v, int pos, Position.Bias b, Shape a, 
					     int direction,
					     Position.Bias[] biasRet) 
	    throws BadLocationException {

	    int startOffset = v.getStartOffset();
	    int endOffset = v.getEndOffset();
	    Segment text;
	    
	    switch (direction) {
	    case View.NORTH:
		break;
	    case View.SOUTH:
		break;
	    case View.EAST:
		if(startOffset == v.getDocument().getLength()) {
		    if(pos == -1) {
			biasRet[0] = Position.Bias.Forward;
			return startOffset;
		    }
		    // End case for bidi text where newline is at beginning
		    // of line.
		    return -1;
		}
		if(pos == -1) {
		    biasRet[0] = Position.Bias.Forward;
		    return startOffset;
		}
		if(pos == endOffset) {
		    return -1;
		}
		if(++pos == endOffset) {
		    text = v.getText(endOffset - 1, endOffset);
		    if(text.array[text.offset] == '\n') {
			return -1;
		    }
		    biasRet[0] = Position.Bias.Backward;
		}
		else {
		    biasRet[0] = Position.Bias.Forward;
		}
		return pos;
	    case View.WEST:
		if(startOffset == v.getDocument().getLength()) {
		    if(pos == -1) {
			biasRet[0] = Position.Bias.Forward;
			return startOffset;
		    }
		    // End case for bidi text where newline is at beginning
		    // of line.
		    return -1;
		}
		if(pos == -1) {
		    text = v.getText(endOffset - 1, endOffset);
		    if(text.array[text.offset] == '\n') {
			biasRet[0] = Position.Bias.Forward;
			return endOffset - 1;
		    }
		    biasRet[0] = Position.Bias.Backward;
		    return endOffset;
		}
		if(pos == startOffset) {
		    return -1;
		}
		biasRet[0] = Position.Bias.Forward;
		return (pos - 1);
	    default:
		throw new IllegalArgumentException("Bad direction: " + direction);
	    }
	    return pos;

	}
    }
}

