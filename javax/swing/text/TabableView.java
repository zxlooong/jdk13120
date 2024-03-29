/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;


/**
 * Interface for View's that have size dependant
 * upon tabs.  
 * 
 * @author  Timothy Prinzing
 * @author  Scott Violet
 * @version 1.7 02/06/02
 * @see TabExpander
 * @see LabelView
 * @see ParagraphView
 */
public interface TabableView {

    /**
     * Determines the desired span when using the given 
     * tab expansion implementation.  If a container 
     * calls this method, it will do so prior to the
     * normal layout which would call getPreferredSpan.
     * A view implementing this should give the same
     * result in any subsequent calls to getPreferredSpan
     * along the axis of tab expansion.
     *
     * @param x the position the view would be located
     *  at for the purpose of tab expansion >= 0.
     * @param e how to expand the tabs when encountered.
     * @return the desired span >= 0
     */
    float getTabbedSpan(float x, TabExpander e);

    /**
     * Determines the span along the same axis as tab 
     * expansion for a portion of the view.  This is
     * intended for use by the TabExpander for cases
     * where the tab expansion involves aligning the
     * portion of text that doesn't have whitespace 
     * relative to the tab stop.  There is therefore
     * an assumption that the range given does not
     * contain tabs.
     *
     * @param p0 the starting location in the text document >= 0
     * @param p1 the ending location in the text document >= p0
     * @return the span >= 0
     */
    float getPartialSpan(int p0, int p1);
}
