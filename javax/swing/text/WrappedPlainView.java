/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.util.Vector;
import java.util.Properties;
import java.awt.*;
import javax.swing.event.*;

/**
 * View of plain text (text with only one font and color)
 * that does line-wrapping.  This view expects that its
 * associated element has child elements that represent
 * the lines it should be wrapping.  It is implemented
 * as a vertical box that contains logical line views.
 * The logical line views are nested classes that render
 * the logical line as multiple physical line if the logical
 * line is too wide to fit within the allocation.  The
 * line views draw upon the outer class for its state
 * to reduce their memory requirements.
 * <p>
 * The line views do all of their rendering through the
 * <code>drawLine</code> method which in turn does all of
 * its rendering through the <code>drawSelectedText</code>
 * and <code>drawUnselectedText</code> methods.  This 
 * enables subclasses to easily specialize the rendering
 * without concern for the layout aspects.
 *
 * @author  Timothy Prinzing
 * @version 1.23 02/06/02
 * @see     View
 */
public class WrappedPlainView extends BoxView implements TabExpander {

    /**
     * Creates a new WrappedPlainView.  Lines will be wrapped
     * on character boundaries.
     *
     * @param elem the element underlying the view
     */
    public WrappedPlainView(Element elem) {
	this(elem, false);
    }

    /**
     * Creates a new WrappedPlainView.  Lines can be wrapped on
     * either character or word boundaries depending upon the
     * setting of the wordWrap parameter.
     *
     * @param elem the element underlying the view
     * @param wordWrap should lines be wrapped on word boundaries?
     */
    public WrappedPlainView(Element elem, boolean wordWrap) {
	super(elem, Y_AXIS);
	lineBuffer = new Segment();
	this.wordWrap = wordWrap;
    }

    /**
     * Returns the tab size set for the document, defaulting to 8.
     *
     * @return the tab size
     */
    protected int getTabSize() {
        Integer i = (Integer) getDocument().getProperty(PlainDocument.tabSizeAttribute);
        int size = (i != null) ? i.intValue() : 8;
        return size;
    }

    /**
     * Renders a line of text, suppressing whitespace at the end
     * and expanding any tabs.  This is implemented to make calls
     * to the methods <code>drawUnselectedText</code> and 
     * <code>drawSelectedText</code> so that the way selected and 
     * unselected text are rendered can be customized.
     *
     * @param p0 the starting document location to use >= 0
     * @param p1 the ending document location to use >= p1
     * @param g the graphics context
     * @param x the starting X position >= 0
     * @param y the starting Y position >= 0
     * @see #drawUnselectedText
     * @see #drawSelectedText
     */
    protected void drawLine(int p0, int p1, Graphics g, int x, int y) {
        Element lineMap = getElement();
	Element line = lineMap.getElement(lineMap.getElementIndex(p0));
	Element elem;

        try {
	    if (line.isLeaf()) {
	        drawText(line, p0, p1, g, x, y);
	    } else {
		// this line contains the composed text.
		int idx = line.getElementIndex(p0);
		int lastIdx = line.getElementIndex(p1);
		for(; idx <= lastIdx; idx++) {
		    elem = line.getElement(idx);
		    int start = Math.max(elem.getStartOffset(), p0);
		    int end = Math.min(elem.getEndOffset(), p1);
		    x = drawText(elem, start, end, g, x, y);
		}
	    }
        } catch (BadLocationException e) {
            throw new StateInvariantError("Can't render: " + p0 + "," + p1);
        }
    }
        
    private int drawText(Element elem, int p0, int p1, Graphics g, int x, int y) throws BadLocationException {
        p1 = Math.min(getDocument().getLength(), p1);
	AttributeSet attr = elem.getAttributes();

	if (Utilities.isComposedTextAttributeDefined(attr)) {
	    g.setColor(unselected);
	    x = Utilities.drawComposedText(attr, g, x, y, 
					p0-elem.getStartOffset(), 
					p1-elem.getStartOffset());
	} else {
	    if (sel0 == sel1) {
		// no selection
		x = drawUnselectedText(g, x, y, p0, p1);
	    } else if ((p0 >= sel0 && p0 <= sel1) && (p1 >= sel0 && p1 <= sel1)) {
		x = drawSelectedText(g, x, y, p0, p1);
	    } else if (sel0 >= p0 && sel0 <= p1) {
		if (sel1 >= p0 && sel1 <= p1) {
		    x = drawUnselectedText(g, x, y, p0, sel0);
		    x = drawSelectedText(g, x, y, sel0, sel1);
		    x = drawUnselectedText(g, x, y, sel1, p1);
		} else {
		    x = drawUnselectedText(g, x, y, p0, sel0);
		    x = drawSelectedText(g, x, y, sel0, p1);
		}
	    } else if (sel1 >= p0 && sel1 <= p1) {
		x = drawSelectedText(g, x, y, p0, sel1);
		x = drawUnselectedText(g, x, y, sel1, p1);
	    } else {
		x = drawUnselectedText(g, x, y, p0, p1);
	    }
	}

        return x;
    }

    /**
     * Renders the given range in the model as normal unselected
     * text.  
     *
     * @param g the graphics context
     * @param x the starting X coordinate >= 0
     * @param y the starting Y coordinate >= 0
     * @param p0 the beginning position in the model >= 0
     * @param p1 the ending position in the model >= p0
     * @returns the X location of the end of the range >= 0
     * @exception BadLocationException if the range is invalid
     */
    protected int drawUnselectedText(Graphics g, int x, int y, 
                                     int p0, int p1) throws BadLocationException {
        g.setColor(unselected);
        Document doc = getDocument();
        doc.getText(p0, p1 - p0, lineBuffer);
        return Utilities.drawTabbedText(lineBuffer, x, y, g, this, p0);
    }

    /**
     * Renders the given range in the model as selected text.  This
     * is implemented to render the text in the color specified in
     * the hosting component.  It assumes the highlighter will render
     * the selected background.
     *
     * @param g the graphics context
     * @param x the starting X coordinate >= 0
     * @param y the starting Y coordinate >= 0
     * @param p0 the beginning position in the model >= 0
     * @param p1 the ending position in the model >= p0
     * @returns the location of the end of the range.
     * @exception BadLocationException if the range is invalid
     */
    protected int drawSelectedText(Graphics g, int x, 
                                   int y, int p0, int p1) throws BadLocationException {
        g.setColor(selected);
        Document doc = getDocument();
        doc.getText(p0, p1 - p0, lineBuffer);
        return Utilities.drawTabbedText(lineBuffer, x, y, g, this, p0);
    }

    /**
     * Gives access to a buffer that can be used to fetch 
     * text from the associated document.
     *
     * @returns the buffer
     */
    protected final Segment getLineBuffer() {
        return lineBuffer;
    }

    /**
     * This is called by the nested wrapped line
     * views to determine the break location.  This can
     * be reimplemented to alter the breaking behavior.
     * It will either break at word or character boundaries
     * depending upon the break argument given at
     * construction.
     */
    protected int calculateBreakPosition(int p0, int p1) {
	int p;
	loadText(p0, p1);
	if (wordWrap) {
	    p = p0 + Utilities.getBreakLocation(lineBuffer, metrics,
						tabBase, tabBase + getWidth(),
						this, p0);
	} else {
	    p = p0 + Utilities.getTabbedTextOffset(lineBuffer, metrics, 
						   tabBase, tabBase + getWidth(),
						   this, p0);
	}
	return p;
    }

    /**
     * Loads all of the children to initialize the view.
     * This is called by the <code>setParent</code> method.
     * Subclasses can reimplement this to initialize their
     * child views in a different manner.  The default
     * implementation creates a child view for each 
     * child element.
     *
     * @param f the view factory
     */
    protected void loadChildren(ViewFactory f) {
        Element e = getElement();
        int n = e.getElementCount();
        if (n > 0) {
            View[] added = new View[n];
            for (int i = 0; i < n; i++) {
                added[i] = new WrappedLine(e.getElement(i));
            }
            replace(0, 0, added);
        }
    }

    /**
     * Update the child views in response to a 
     * document event.
     */
    void updateChildren(DocumentEvent e, Shape a) {
        Element elem = getElement();
        DocumentEvent.ElementChange ec = e.getChange(elem);
        if (ec != null) {
            // the structure of this element changed.
            Element[] removedElems = ec.getChildrenRemoved();
            Element[] addedElems = ec.getChildrenAdded();
            View[] added = new View[addedElems.length];
            for (int i = 0; i < addedElems.length; i++) {
                added[i] = new WrappedLine(addedElems[i]);
            }
            replace(ec.getIndex(), removedElems.length, added);

            // should damge a little more intelligently.
            if (a != null) {
                preferenceChanged(null, true, true);
                getContainer().repaint();
            }
        }

	// update font metrics which may be used by the child views
	updateMetrics();
    }

    /**
     * Load the text buffer with the given range
     * of text.  This is used by the fragments 
     * broken off of this view as well as this 
     * view itself.
     */
    final void loadText(int p0, int p1) {
	try {
	    Document doc = getDocument();
	    doc.getText(p0, p1 - p0, lineBuffer);
	} catch (BadLocationException bl) {
	    throw new StateInvariantError("Can't get line text");
	}
    }

    final void updateMetrics() {
	Component host = getContainer();
	Font f = host.getFont();
	metrics = host.getFontMetrics(f);
	tabSize = getTabSize() * metrics.charWidth('m');
    }

    // --- TabExpander methods ------------------------------------------

    /**
     * Returns the next tab stop position after a given reference position.
     * This implementation does not support things like centering so it
     * ignores the tabOffset argument.
     *
     * @param x the current position >= 0
     * @param tabOffset the position within the text stream
     *   that the tab occurred at >= 0.
     * @return the tab stop, measured in points >= 0
     */
    public float nextTabStop(float x, int tabOffset) {
        int ntabs = ((int) x - tabBase) / tabSize;
        return tabBase + ((ntabs + 1) * tabSize);
    }

    
    // --- View methods -------------------------------------

    /**
     * Renders using the given rendering surface and area 
     * on that surface.  This is implemented to stash the
     * selection positions, selection colors, and font
     * metrics for the nested lines to use.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     *
     * @see View#paint
     */
    public void paint(Graphics g, Shape a) {
	Rectangle alloc = (Rectangle) a;
	tabBase = alloc.x;
	JTextComponent host = (JTextComponent) getContainer();
	sel0 = host.getSelectionStart();
	sel1 = host.getSelectionEnd();
	unselected = (host.isEnabled()) ? 
	    host.getForeground() : host.getDisabledTextColor();
	Caret c = host.getCaret();
        selected = c.isSelectionVisible() ? host.getSelectedTextColor() : unselected;
	g.setFont(host.getFont());

        // superclass paints the children
        super.paint(g, a);
    }

    /**
     * Sets the size of the view.  If the size has changed, layout
     * is redone.  The size is the full size of the view including
     * the inset areas.  
     *
     * @param width the width >= 0
     * @param height the height >= 0
     */
    public void setSize(float width, float height) {
	updateMetrics();
	if ((int) width != getWidth()) {
	    // invalidate the view itself since the childrens
	    // desired widths will be based upon this views width.
	    preferenceChanged(null, true, true);
	    widthChanging = true;
	}
	super.setSize(width, height);
	widthChanging = false;
    }

    /**
     * Determines the preferred span for this view along an
     * axis.  This is implemented to provide the superclass
     * behavior after first making sure that the current font
     * metrics are cached (for the nested lines which use
     * the metrics to determine the height of the potentially
     * wrapped lines).
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns  the span the view would like to be rendered into.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.  
     *           The parent may choose to resize or break the view.
     * @see View#getPreferredSpan
     */
    public float getPreferredSpan(int axis) {
	updateMetrics();
	return super.getPreferredSpan(axis);
    }

    /**
     * Determines the minimum span for this view along an
     * axis.  This is implemented to provide the superclass
     * behavior after first making sure that the current font
     * metrics are cached (for the nested lines which use
     * the metrics to determine the height of the potentially
     * wrapped lines).
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns  the span the view would like to be rendered into.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.  
     *           The parent may choose to resize or break the view.
     * @see View#getMinimumSpan
     */
    public float getMinimumSpan(int axis) {
	updateMetrics();
	return super.getMinimumSpan(axis);
    }

    /**
     * Determines the maximum span for this view along an
     * axis.  This is implemented to provide the superclass
     * behavior after first making sure that the current font
     * metrics are cached (for the nested lines which use
     * the metrics to determine the height of the potentially
     * wrapped lines).
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns  the span the view would like to be rendered into.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.  
     *           The parent may choose to resize or break the view.
     * @see View#getMaximumSpan
     */
    public float getMaximumSpan(int axis) {
	updateMetrics();
	return super.getMaximumSpan(axis);
    }

    /**
     * Gives notification that something was inserted into the 
     * document in a location that this view is responsible for.
     * This is implemented to simply update the children.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#insertUpdate
     */
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        updateChildren(e, a);

        Rectangle alloc = ((a != null) && isAllocationValid()) ? 
            getInsideAllocation(a) : null;
        int pos = e.getOffset();
        View v = getViewAtPosition(pos, alloc);
        if (v != null) {
            v.insertUpdate(e, alloc, f);
        }
    }

    /**
     * Gives notification that something was removed from the 
     * document in a location that this view is responsible for.
     * This is implemented to simply update the children.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#removeUpdate
     */
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        updateChildren(e, a);

        Rectangle alloc = ((a != null) && isAllocationValid()) ? 
            getInsideAllocation(a) : null;
        int pos = e.getOffset();
        View v = getViewAtPosition(pos, alloc);
        if (v != null) {
            v.removeUpdate(e, alloc, f);
        }
    }

    /**
     * Gives notification from the document that attributes were changed
     * in a location that this view is responsible for.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#changedUpdate
     */
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        updateChildren(e, a);
    }

    // --- variables -------------------------------------------

    FontMetrics metrics;
    Segment lineBuffer;
    boolean widthChanging;
    int tabBase;
    int tabSize;
    boolean wordWrap;
    
    int sel0;
    int sel1;
    Color unselected;
    Color selected;


    /**
     * Simple view of a line that wraps if it doesn't
     * fit withing the horizontal space allocated.
     * This class tries to be lightweight by carrying little 
     * state of it's own and sharing the state of the outer class 
     * with it's sibblings.
     */
    class WrappedLine extends View {

        WrappedLine(Element elem) {
            super(elem);
        }

        /**
         * Calculate the number of lines that will be rendered
         * by logical line when it is wrapped.
         */
        final int calculateLineCount() {
            int nlines = 0;
            int p1 = getEndOffset();
            for (int p0 = getStartOffset(); p0 < p1; ) {
                nlines += 1;
		int p = calculateBreakPosition(p0, p1);
                p0 = (p == p0) ? p1 : p;
            }
            return nlines;
        }

        /**
         * Determines the preferred span for this view along an
         * axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @returns  the span the view would like to be rendered into.
         *           Typically the view is told to render into the span
         *           that is returned, although there is no guarantee.  
         *           The parent may choose to resize or break the view.
         * @see View#getPreferredSpan
         */
        public float getPreferredSpan(int axis) {
            switch (axis) {
            case View.X_AXIS:
                return getWidth();
            case View.Y_AXIS:
		if (nlines == 0 || widthChanging) {
		    nlines = calculateLineCount();
		}
                int h = nlines * metrics.getHeight();
                return h;
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

        /**
         * Renders using the given rendering surface and area on that
         * surface.  The view may need to do layout and create child
         * views to enable itself to render into the given allocation.
         *
         * @param g the rendering surface to use
         * @param a the allocated region to render into
         * @see View#paint
         */
        public void paint(Graphics g, Shape a) {
            Rectangle alloc = (Rectangle) a;
            int y = alloc.y + metrics.getAscent();
            int x = alloc.x;

	    JTextComponent host = (JTextComponent)getContainer();
	    Highlighter h = host.getHighlighter();
	    LayeredHighlighter dh = (h instanceof LayeredHighlighter) ?
		                     (LayeredHighlighter)h : null;
            int p1 = getEndOffset();
            for (int p0 = getStartOffset(); p0 < p1; ) {
		int p = calculateBreakPosition(p0, p1);
		if (dh != null) {
 		    if (p == p1) {
 			dh.paintLayeredHighlights(g, p0, p - 1, a, host, this);
 		    }
 		    else {
 			dh.paintLayeredHighlights(g, p0, p, a, host, this);
 		    }
		}
                drawLine(p0, p, g, x, y);
                
                p0 = (p == p0) ? p1 : p;
                y += metrics.getHeight();
            }
        }

        /**
         * Provides a mapping from the document model coordinate space
         * to the coordinate space of the view mapped to it.
         *
         * @param pos the position to convert
         * @param a the allocated region to render into
         * @return the bounding box of the given position is returned
         * @exception BadLocationException  if the given position does not represent a
         *   valid location in the associated document
         * @see View#modelToView
         */
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
	    Rectangle alloc = a.getBounds();
            alloc.height = metrics.getHeight();
            alloc.width = 1;
            
            int p1 = getEndOffset();
	    int p0 = getStartOffset();
	    int testP = (b == Position.Bias.Forward) ? pos :
		        Math.max(p0, pos - 1);
            while (p0 < p1) {
		int p = calculateBreakPosition(p0, p1);
                if ((pos >= p0) && (testP < p)) {
                    // it's in this line
                    loadText(p0, pos);
                    alloc.x += Utilities.getTabbedTextWidth(lineBuffer, metrics, 
                                                            alloc.x, 
                                                            WrappedPlainView.this, p0);
                    return alloc;
                }
		if (p == p1 && pos == p1) {
		    // Wants end.
		    if (pos > p0) {
			loadText(p0, pos);
			alloc.x += Utilities.getTabbedTextWidth(lineBuffer,
					     metrics, alloc.x, 
					     WrappedPlainView.this, p0);
		    }
                    return alloc;
		}
                p0 = (p == p0) ? p1 : p;
                alloc.y += alloc.height;
            }
            throw new BadLocationException(null, pos);
        }

        /**
         * Provides a mapping from the view coordinate space to the logical
         * coordinate space of the model.
         *
         * @param x the X coordinate
         * @param y the Y coordinate
         * @param a the allocated region to render into
         * @return the location within the model that best represents the
         *  given point in the view
         * @see View#viewToModel
         */
        public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
	    // PENDING(prinz) implement bias properly
	    bias[0] = Position.Bias.Forward;

	    Rectangle alloc = (Rectangle) a;
	    Document doc = getDocument();
	    int x = (int) fx;
	    int y = (int) fy;
	    if (y < alloc.y) {
		// above the area covered by this icon, so the the position
		// is assumed to be the start of the coverage for this view.
		return getStartOffset();
	    } else if (y > alloc.y + alloc.height) {
		// below the area covered by this icon, so the the position
		// is assumed to be the end of the coverage for this view.
		return getEndOffset() - 1;
	    } else {
		// positioned within the coverage of this view vertically,
		// so we figure out which line the point corresponds to.
		// if the line is greater than the number of lines contained, then
		// simply use the last line as it represents the last possible place
		// we can position to.
		alloc.height = metrics.getHeight();
		int p1 = getEndOffset();
		for (int p0 = getStartOffset(); p0 < p1; ) {
		    int p = calculateBreakPosition(p0, p1);
		    if ((y >= alloc.y) && (y < (alloc.y + alloc.height))) {
			// it's in this line
			if (x < alloc.x) {
			    // point is to the left of the line
			    return p0;
			} else if (x > alloc.x + alloc.width) {
			    // point is to the right of the line
			    return p;
			} else {
			    // Determine the offset into the text
			    int n = Utilities.getTabbedTextOffset(lineBuffer, metrics, 
								    alloc.x, x, 
								    WrappedPlainView.this, p0);
			    return Math.min(p0 + n, p1 - 1);
			}
		    }
		    
		    p0 = (p == p0) ? p1 : p;
		    alloc.y += alloc.height;
		}
		return getEndOffset() - 1;
	    }
	}

        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
	    int n = calculateLineCount();
	    if (this.nlines != n) {
		this.nlines = n;
		WrappedPlainView.this.preferenceChanged(this, false, true);
		// have to repaint any views after the receiver.
		getContainer().repaint();
	    }
	    else if (a != null) {
                Component c = getContainer();
                Rectangle alloc = (Rectangle) a;
                c.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
            }
        }

        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
	    int n = calculateLineCount();
	    if (this.nlines != n) {
		// have to repaint any views after the receiver.
		this.nlines = n;
		WrappedPlainView.this.preferenceChanged(this, false, true);
		getContainer().repaint();
	    }
	    else if (a != null) {
                Component c = getContainer();
                Rectangle alloc = (Rectangle) a;
                c.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
            }
        }

        // --- variables ---------------------------------------

        int nlines;
    }
    
}

