/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;


/**
 * Simple interface to allow for different types of
 * implementations of tab expansion.
 *
 * @author  Timothy Prinzing
 * @version 1.13 02/06/02
 */
public interface TabExpander {

    /**
     * Returns the next tab stop position given a reference
     * position.  Values are expressed in points.
     *
     * @param x the position in points >= 0
     * @param tabOffset the position within the text stream
     *   that the tab occurred at >= 0.
     * @return the next tab stop >= 0
     */
    float nextTabStop(float x, int tabOffset);

}
