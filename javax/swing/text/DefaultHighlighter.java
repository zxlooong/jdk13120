/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.util.Vector;
import java.awt.*;
import javax.swing.plaf.*;

/**
 * Implements the Highlighter interfaces.  Implements a simple highlight
 * painter that renders in a solid color.
 * 
 * @author  Timothy Prinzing
 * @version 1.32 02/06/02
 * @see     Highlighter
 */
public class DefaultHighlighter extends LayeredHighlighter {

    /**
     * Creates a new DefaultHighlighther object.
     */
    public DefaultHighlighter() {
	drawsLayeredHighlights = true;
    }

    // ---- Highlighter methods ----------------------------------------------

    /**
     * Renders the highlights.
     *
     * @param g the graphics context
     */
    public void paint(Graphics g) {
        // PENDING(prinz) - should cull ranges not visible
        int len = highlights.size();
        for (int i = 0; i < len; i++) {
	    HighlightInfo info = (HighlightInfo) highlights.elementAt(i);
	    if (!(info instanceof LayeredHighlightInfo)) {
		// Avoid allocing unless we need it.
		Rectangle a = component.getBounds();
		Insets insets = component.getInsets();
		a.x = insets.left;
		a.y = insets.top;
		a.width -= insets.left + insets.right;
		a.height -= insets.top + insets.bottom;
		for (; i < len; i++) {
		    info = (HighlightInfo)highlights.elementAt(i);
		    if (!(info instanceof LayeredHighlightInfo)) {
			Highlighter.HighlightPainter p = info.getPainter();
			p.paint(g, info.getStartOffset(), info.getEndOffset(),
				a, component);
		    }
		}
	    }
	}
    }

    /**
     * Called when the UI is being installed into the
     * interface of a JTextComponent.  Installs the editor, and
     * removes any existing highlights.
     *
     * @param c the editor component
     * @see Highlighter#install
     */
    public void install(JTextComponent c) {
	component = c;
	removeAllHighlights();
    }

    /**
     * Called when the UI is being removed from the interface of
     * a JTextComponent.
     *
     * @param c the component
     * @see Highlighter#deinstall
     */
    public void deinstall(JTextComponent c) {
	component = null;
    }

    /**
     * Adds a highlight to the view.  Returns a tag that can be used 
     * to refer to the highlight.
     *
     * @param p0   the start offset of the range to highlight >= 0
     * @param p1   the end offset of the range to highlight >= p0
     * @param p    the painter to use to actually render the highlight
     * @returns    an object that can be used as a tag
     *   to refer to the highlight
     * @exception BadLocationException if the specified location is invalid
     */
    public Object addHighlight(int p0, int p1, Highlighter.HighlightPainter p) throws BadLocationException {
	Document doc = component.getDocument();
	TextUI mapper = component.getUI();
	HighlightInfo i = (getDrawsLayeredHighlights() &&
			   (p instanceof LayeredHighlighter.LayerPainter)) ?
	                  new LayeredHighlightInfo() : new HighlightInfo();
	i.painter = p;
	i.p0 = doc.createPosition(p0);
	i.p1 = doc.createPosition(p1);
	highlights.addElement(i);
	mapper.damageRange(component, p0, p1);
        return i;
    }

    /**
     * Removes a highlight from the view.
     *
     * @param tag the reference to the highlight
     */
    public void removeHighlight(Object tag) {
	if (tag instanceof LayeredHighlightInfo) {
	    LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
	    if (lhi.width > 0 && lhi.height > 0) {
		component.repaint(lhi.x, lhi.y, lhi.width, lhi.height);
	    }
	}
	else {
	    TextUI mapper = component.getUI();
	    HighlightInfo info = (HighlightInfo) tag;
	    mapper.damageRange(component, info.p0.getOffset(),
			       info.p1.getOffset());
	}
	highlights.removeElement(tag);
    }

    /**
     * Removes all highlights.
     */
    public void removeAllHighlights() {
	TextUI mapper = component.getUI();
	if (getDrawsLayeredHighlights()) {
	    int len = highlights.size();
	    if (len != 0) {
		int minX = 0;
		int minY = 0;
		int maxX = 0;
		int maxY = 0;
		for (int i = 0; i < len; i++) {
		    LayeredHighlightInfo info = (LayeredHighlightInfo)
			                    highlights.elementAt(i);
		    minX = Math.min(minX, info.x);
		    minY = Math.min(minY, info.y);
		    maxX = Math.max(maxX, info.x + info.width);
		    maxY = Math.max(maxY, info.y + info.height);
		}
		if (minX != maxX && minY != maxY) {
		    component.repaint(minX, minY, maxX - minX, maxY - minY);
		}
		highlights.removeAllElements();
	    }
	}
	else if (mapper != null) {
	    int len = highlights.size();
	    if (len != 0) {
		int p0 = Integer.MAX_VALUE;
		int p1 = 0;
		for (int i = 0; i < len; i++) {
		    HighlightInfo info = (HighlightInfo) highlights.elementAt(i);
		    p0 = Math.min(p0, info.p0.getOffset());
		    p1 = Math.max(p1, info.p1.getOffset());
		}
		mapper.damageRange(component, p0, p1);
		highlights.removeAllElements();
	    }
	}
    }

    /**
     * Changes a highlight.
     *
     * @param tag the highlight tag
     * @param p0 the beginning of the range >= 0
     * @param p1 the end of the range >= p0
     * @exception BadLocationException if the specified location is invalid
     */
    public void changeHighlight(Object tag, int p0, int p1) throws BadLocationException {
	Document doc = component.getDocument();
	TextUI mapper = component.getUI();
	if (tag instanceof LayeredHighlightInfo) {
	    LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
	    if (lhi.width > 0 && lhi.height > 0) {
		component.repaint(lhi.x, lhi.y, lhi.width, lhi.height);
	    }
	    // Mark the highlights region as invalid, it will reset itself
	    // next time asked to paint.
	    lhi.width = lhi.height = 0;
	    lhi.p0 = doc.createPosition(p0);
	    lhi.p1 = doc.createPosition(p1);
	    mapper.damageRange(component, Math.min(p0, p1), Math.max(p0, p1));
	}
	else {
	    HighlightInfo info = (HighlightInfo) tag;
	    int oldP0 = info.p0.getOffset();
	    int oldP1 = info.p1.getOffset();
	    if (p0 == oldP0) {
		mapper.damageRange(component, Math.min(oldP1, p1),
				   Math.max(oldP1, p1));
	    } else if (p1 == oldP1) {
		mapper.damageRange(component, Math.min(p0, oldP0),
				   Math.max(p0, oldP0));
	    } else {
		mapper.damageRange(component, oldP0, oldP1);
		mapper.damageRange(component, p0, p1);
	    }
	    info.p0 = doc.createPosition(p0);
	    info.p1 = doc.createPosition(p1);
	}
    }

    /**
     * Makes a copy of the highlights.  Does not actually clone each highlight,
     * but only makes references to them.
     *
     * @return the copy
     * @see Highlighter#getHighlights
     */
    public Highlighter.Highlight[] getHighlights() {
	Highlighter.Highlight[] h = new Highlighter.Highlight[highlights.size()];
	highlights.copyInto(h);
	return h;
    }

    /**
     * When leaf Views (such as LabelView) are rendering they should
     * call into this method. If a highlight is in the given region it will
     * be drawn immediately.
     *
     * @param g Graphics used to draw
     * @param p0 starting offset of view
     * @param p1 ending offset of view
     * @param viewBounds Bounds of View
     * @param editor JTextComponent
     * @param view View instance being rendered
     */
    public void paintLayeredHighlights(Graphics g, int p0, int p1,
				       Shape viewBounds,
				       JTextComponent editor, View view) {
	for (int counter = highlights.size() - 1; counter >= 0; counter--) {
	    Object tag = highlights.elementAt(counter);
	    if (tag instanceof LayeredHighlightInfo) {
		LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
		int start = lhi.getStartOffset();
		int end = lhi.getEndOffset();
		if ((p0 < start && p1 > start) ||
		    (p0 >= start && p0 < end)) {
		    lhi.paintLayeredHighlights(g, p0, p1, viewBounds,
					       editor, view);
		}
	    }
	}
    }

    /**
     * If true, highlights are drawn as the Views draw the text. That is
     * the Views will call into <code>paintLayeredHighlight</code> which
     * will result in a rectangle being drawn before the text is drawn
     * (if the offsets are in a highlighted region that is). For this to
     * work the painter supplied must be an instance of
     * LayeredHighlightPainter.
     */
    public void setDrawsLayeredHighlights(boolean newValue) {
	drawsLayeredHighlights = newValue;
    }

    public boolean getDrawsLayeredHighlights() {
	return drawsLayeredHighlights;
    }

    // ---- member variables --------------------------------------------
    
    private Vector highlights = new Vector();  // Vector<HighlightInfo>
    private JTextComponent component;
    private boolean drawsLayeredHighlights;


    public static LayeredHighlighter.LayerPainter DefaultPainter = new DefaultHighlightPainter(null);


    /**
     * Simple highlight painter that fills a highlighted area with
     * a solid color.
     */
    public static class DefaultHighlightPainter extends LayeredHighlighter.LayerPainter {

        /**
         * Constructs a new highlight painter. If <code>c</code> is null,
	 * the JTextComponent will be queried for its selection color.
         *
         * @param c the color for the highlight
         */
        public DefaultHighlightPainter(Color c) {
	    color = c;
	}
	
        /**
         * Returns the color of the highlight.
         *
         * @return the color
         */
	public Color getColor() {
	    return color;
	}

	// --- HighlightPainter methods ---------------------------------------

        /**
         * Paints a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting model offset >= 0
         * @param offs1 the ending model offset >= offs1
         * @param bounds the bounding box for the highlight
         * @param c the editor
         */
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
	    Rectangle alloc = bounds.getBounds();
	    try {
		// --- determine locations ---
		TextUI mapper = c.getUI();
		Rectangle p0 = mapper.modelToView(c, offs0);
		Rectangle p1 = mapper.modelToView(c, offs1);

		// --- render ---
		Color color = getColor();

		if (color == null) {
		    g.setColor(c.getSelectionColor());
		}
		else {
		    g.setColor(color);
		}
		if (p0.y == p1.y) {
		    // same line, render a rectangle
		    Rectangle r = p0.union(p1);
		    g.fillRect(r.x, r.y, r.width, r.height);
		} else {
		    // different lines
		    int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
		    g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
		    if ((p0.y + p0.height) != p1.y) {
			g.fillRect(alloc.x, p0.y + p0.height, alloc.width, 
				   p1.y - (p0.y + p0.height));
		    }
		    g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
		}
	    } catch (BadLocationException e) {
		// can't render
	    }
	}

	// --- LayerPainter methods ----------------------------
        /**
         * Paints a portion of a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting model offset >= 0
         * @param offs1 the ending model offset >= offs1
         * @param bounds the bounding box of the view, which is not
	 *        necessarily the region to paint.
         * @param c the editor
	 * @param view View painting for
	 * @return region drawing occured in
         */
	public Shape paintLayer(Graphics g, int offs0, int offs1,
				Shape bounds, JTextComponent c, View view) {
	    Color color = getColor();

	    if (color == null) {
		g.setColor(c.getSelectionColor());
	    }
	    else {
		g.setColor(color);
	    }
	    if (offs0 == view.getStartOffset() &&
		offs1 == view.getEndOffset()) {
		// Contained in view, can just use bounds.
		Rectangle alloc;
		if (bounds instanceof Rectangle) {
		    alloc = (Rectangle)bounds;
		}
		else {
		    alloc = bounds.getBounds();
		}
		g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
		return alloc;
	    }
	    else {
		// Should only render part of View.
		try {
		    // --- determine locations ---
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                                                   offs1,Position.Bias.Backward,
                                                   bounds);
                    Rectangle r = (shape instanceof Rectangle) ?
                                  (Rectangle)shape : shape.getBounds();
                    g.fillRect(r.x, r.y, r.width, r.height);
                    return r;
		} catch (BadLocationException e) {
		    // can't render
		}
	    }
	    // Only if exception
	    return null;
	}

	private Color color;

    }


    class HighlightInfo implements Highlighter.Highlight {

	public int getStartOffset() {
	    return p0.getOffset();
	}

	public int getEndOffset() {
	    return p1.getOffset();
	}

	public Highlighter.HighlightPainter getPainter() {
	    return painter;
	}

	Position p0;
	Position p1;
        Highlighter.HighlightPainter painter;
    }


    /**
     * LayeredHighlightPainter is used when a drawsLayeredHighlights is
     * true. It maintains a rectangle of the region to paint.
     */
    class LayeredHighlightInfo extends HighlightInfo {

	void union(Shape bounds) {
	    if (bounds == null)
		return;

	    Rectangle alloc;
	    if (bounds instanceof Rectangle) {
		alloc = (Rectangle)bounds;
	    }
	    else {
		alloc = bounds.getBounds();
	    }
	    if (width == 0 || height == 0) {
		x = alloc.x;
		y = alloc.y;
		width = alloc.width;
		height = alloc.height;
	    }
	    else {
		width = Math.max(x + width, alloc.x + alloc.width);
		height = Math.max(y + height, alloc.y + alloc.height);
		x = Math.min(x, alloc.x);
		width -= x;
		y = Math.min(y, alloc.y);
		height -= y;
	    }
	}

	/**
	 * Restricts the region based on the receivers offsets and messages
	 * the painter to paint the region.
	 */
	void paintLayeredHighlights(Graphics g, int p0, int p1,
				    Shape viewBounds, JTextComponent editor,
				    View view) {
	    int start = getStartOffset();
	    int end = getEndOffset();
	    // Restrict the region to what we represent
	    p0 = Math.max(start, p0);
	    p1 = Math.min(end, p1);
	    // Paint the appropriate region using the painter and union
	    // the effected region with our bounds.
	    union(((LayeredHighlighter.LayerPainter)painter).paintLayer
		  (g, p0, p1, viewBounds, editor, view));
	}

	int x;
	int y;
	int width;
	int height;
    }
}
