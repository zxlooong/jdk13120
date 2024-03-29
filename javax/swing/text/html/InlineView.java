/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import java.awt.Shape;
import java.awt.FontMetrics;
import java.text.BreakIterator;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;

/**
 * Displays the <dfn>inline element</dfn> styles
 * based upon css attributes.
 *
 * @author  Timothy Prinzing
 * @version 1.20 02/06/02
 */
public class InlineView extends LabelView {

    /**
     * Constructs a new view wrapped on an element.
     *
     * @param elem the element
     */
    public InlineView(Element elem) {
	super(elem);
	StyleSheet sheet = getStyleSheet();
	attr = sheet.getViewAttributes(this);
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
	super.changedUpdate(e, a, f);
	StyleSheet sheet = getStyleSheet();
	attr = sheet.getViewAttributes(this);
	preferenceChanged(null, true, true);
    }

    /**
     * Fetches the attributes to use when rendering.  This is
     * implemented to multiplex the attributes specified in the
     * model with a StyleSheet.
     */
    public AttributeSet getAttributes() {
	return attr;
    }

    /**
     * Determines how attractive a break opportunity in 
     * this view is.  This can be used for determining which
     * view is the most attractive to call <code>breakView</code>
     * on in the process of formatting.  A view that represents
     * text that has whitespace in it might be more attractive
     * than a view that has no whitespace, for example.  The
     * higher the weight, the more attractive the break.  A
     * value equal to or lower than <code>BadBreakWeight</code>
     * should not be considered for a break.  A value greater
     * than or equal to <code>ForcedBreakWeight</code> should
     * be broken.
     * <p>
     * This is implemented to provide the default behavior
     * of returning <code>BadBreakWeight</code> unless the length
     * is greater than the length of the view in which case the 
     * entire view represents the fragment.  Unless a view has
     * been written to support breaking behavior, it is not
     * attractive to try and break the view.  An example of
     * a view that does support breaking is <code>LabelView</code>.
     * An example of a view that uses break weight is 
     * <code>ParagraphView</code>.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param pos the potential location of the start of the 
     *   broken view >= 0.  This may be useful for calculating tab
     *   positions.
     * @param len specifies the relative length from <em>pos</em>
     *   where a potential break is desired >= 0.
     * @return the weight, which should be a value between
     *   ForcedBreakWeight and BadBreakWeight.
     * @see LabelView
     * @see ParagraphView
     * @see javax.swing.text.View#BadBreakWeight
     * @see javax.swing.text.View#GoodBreakWeight
     * @see javax.swing.text.View#ExcellentBreakWeight
     * @see javax.swing.text.View#ForcedBreakWeight
     */
    public int getBreakWeight(int axis, float pos, float len) {
	if (nowrap) {
	    return BadBreakWeight;
	}
	return super.getBreakWeight(axis, pos, len);
    }

    /**
     * Fetch the span of the longest word in the view.
     */
    float getLongestWordSpan() {
	// find the longest word
	float span = 0;
	try {
	    Document doc = getDocument();
	    int p0 = getStartOffset();
	    int p1 = getEndOffset();
	    if (p1 > p0) {
		Segment segment = new Segment();
		doc.getText(p0, p1 - p0, segment);
		int word0 = p0;
		int word1 = p0;
		BreakIterator words = BreakIterator.getWordInstance();
		words.setText(segment);
		int start = words.first();
		for (int end = words.next(); end != BreakIterator.DONE;
		     start = end, end = words.next()) {
		    
		    // update longest word boundary
		    if ((end - start) > (word1 - word0)) {
			word0 = start;
			word1 = end;
		    }
		}
		// calculate the minimum
		if ((word1 - word0) > 0) {
		    FontMetrics metrics = getFontMetrics();
		    int offs = segment.offset + word0 - segment.getBeginIndex();
		    span = metrics.charsWidth(segment.array, offs, word1 - word0);
		}
	    }
	} catch (BadLocationException ble) {
	    // If the text can't be retrieved, it can't influence the size.
	}
	return span;
    }
    
    /**
     * Set the cached properties from the attributes.
     */
    protected void setPropertiesFromAttributes() {
	super.setPropertiesFromAttributes();
	AttributeSet a = getAttributes();
	Object decor = a.getAttribute(CSS.Attribute.TEXT_DECORATION);
	boolean u = (decor != null) ? 
	  (decor.toString().indexOf("underline") >= 0) : false;
	setUnderline(u);
	boolean s = (decor != null) ? 
	  (decor.toString().indexOf("line-through") >= 0) : false;
	setStrikeThrough(s);
        Object vAlign = a.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
	s = (vAlign != null) ? (vAlign.toString().indexOf("sup") >= 0) : false;
	setSuperscript(s);
	s = (vAlign != null) ? (vAlign.toString().indexOf("sub") >= 0) : false;
	setSubscript(s);

	Object whitespace = a.getAttribute(CSS.Attribute.WHITE_SPACE);
	if ((whitespace != null) && whitespace.equals("nowrap")) {
	    nowrap = true;
	} else {
	    nowrap = false;
	}
    }


    protected StyleSheet getStyleSheet() {
	HTMLDocument doc = (HTMLDocument) getDocument();
	return doc.getStyleSheet();
    }

    private boolean nowrap;
    private AttributeSet attr;

}
