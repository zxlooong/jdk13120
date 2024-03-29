/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt;

import java.awt.color.ColorSpace;
import java.awt.image.Raster;
import java.awt.image.ColorModel;

/**
 * The <code>PaintContext</code> interface defines the encapsulated 
 * and optimized environment to generate color patterns in device 
 * space for fill or stroke operations on a 
 * {@link Graphics2D}.  The <code>PaintContext</code> provides
 * the necessary colors for <code>Graphics2D</code> operations in the 
 * form of a {@link Raster} associated with a {@link ColorModel}.  
 * The <code>PaintContext</code> maintains state for a particular paint 
 * operation.  In a multi-threaded environment, several
 * contexts can exist simultaneously for a single {@link Paint} object.
 * @see Paint
 * @version 10 Feb 1997
 */

public interface PaintContext {
    /**
     * Releases the resources allocated for the operation.
     */
    public void dispose();

    /**
     * Returns the <code>ColorModel</code> of the output.  Note that
     * this <code>ColorModel</code> might be different from the hint
     * specified in the 
     * {@link Paint#createContext(ColorModel, Rectangle, Rectangle2D,
AffineTransform, RenderingHints) createContext} method of
     * <code>Paint</code>.  Not all <code>PaintContext</code> objects are
     * capable of generating color patterns in an arbitrary
     * <code>ColorModel</code>.
     * @return the <code>ColorModel</code> of the output.
     */
    ColorModel getColorModel();

    /**
     * Returns a <code>Raster</code> containing the colors generated for 
     * the graphics operation.
     * @param x,&nbsp;y the coordinates of the area in device space
     * for which colors are generated.
     * @param w the width of the area in device space 
     * @param h the height of the area in device space
     * @return a <code>Raster</code> representing the specified 
     * rectangular area and containing the colors generated for
     * the graphics operation.
     */
    Raster getRaster(int x,
		     int y,
		     int w,
		     int h);

}
