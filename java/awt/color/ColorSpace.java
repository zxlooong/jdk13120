/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/

package java.awt.color;


/**
 * This abstract class is used to serve as a color space tag to identify the
 * specific color space of a Color object or, via a ColorModel object,
 * of an Image, a BufferedImage, or a GraphicsDevice.  It contains
 * methods that transform Colors in a specific color space to/from sRGB
 * and to/from a well-defined CIEXYZ color space.
 * <p>Several variables are defined for purposes of referring to color
 * space types (e.g. TYPE_RGB, TYPE_XYZ, etc.) and to refer to specific
 * color spaces (e.g. CS_sRGB and CS_CIEXYZ).
 * sRGB is a proposed standard RGB color space.  For more information,
 * see <A href="http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html">
 * http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html
 * </A>.
 * <p>The purpose of the methods to transform to/from the well-defined
 * CIEXYZ color space is to support conversions between any two color
 * spaces at a reasonably high degree of accuracy.  It is expected that
 * particular implementations of subclasses of ColorSpace (e.g.
 * ICC_ColorSpace) will support high performance conversion based on
 * underlying platform color management systems.
 * <p>The CS_CIEXYZ space used by the toCIEXYZ/fromCIEXYZ methods can be
 * described as follows:
<pre>

&nbsp;     CIEXYZ
&nbsp;     viewing illuminance: 200 lux
&nbsp;     viewing white point: CIE D50
&nbsp;     media white point: "that of a perfectly reflecting diffuser" -- D50 
&nbsp;     media black point: 0 lux or 0 Reflectance
&nbsp;     flare: 1 percent
&nbsp;     surround: 20percent of the media white point
&nbsp;     media description: reflection print (i.e., RLAB, Hunt viewing media)
&nbsp;     note: For developers creating an ICC profile for this conversion
&nbsp;           space, the following is applicable.  Use a simple Von Kries
&nbsp;           white point adaptation folded into the 3X3 matrix parameters
&nbsp;           and fold the flare and surround effects into the three
&nbsp;           one-dimensional lookup tables (assuming one uses the minimal
&nbsp;           model for monitors).

</pre>
 *
 * <p>
 * @see ICC_ColorSpace
 * @version 10 Feb 1997
 */



public abstract class ColorSpace implements java.io.Serializable {

    private int type;
    private int numComponents;

    // Cache of singletons for the predefined color spaces.
    private static ColorSpace sRGBspace;
    private static ColorSpace XYZspace;
    private static ColorSpace PYCCspace;
    private static ColorSpace GRAYspace;
    private static ColorSpace LINEAR_RGBspace;
    
    /**
     * Any of the family of XYZ color spaces.
     */
    public static final int TYPE_XYZ = 0;

    /**
     * Any of the family of Lab color spaces.
     */
    public static final int TYPE_Lab = 1;

    /**
     * Any of the family of Luv color spaces.
     */
    public static final int TYPE_Luv = 2;

    /**
     * Any of the family of YCbCr color spaces.
     */
    public static final int TYPE_YCbCr = 3;

    /**
     * Any of the family of Yxy color spaces.
     */
    public static final int TYPE_Yxy = 4;

    /**
     * Any of the family of RGB color spaces.
     */
    public static final int TYPE_RGB = 5;

    /**
     * Any of the family of GRAY color spaces.
     */
    public static final int TYPE_GRAY = 6;

    /**
     * Any of the family of HSV color spaces.
     */
    public static final int TYPE_HSV = 7;

    /**
     * Any of the family of HLS color spaces.
     */
    public static final int TYPE_HLS = 8;

    /**
     * Any of the family of CMYK color spaces.
     */
    public static final int TYPE_CMYK = 9;

    /**
     * Any of the family of CMY color spaces.
     */
    public static final int TYPE_CMY = 11;

    /**
     * Generic 2 component color spaces.
     */
    public static final int TYPE_2CLR = 12;

    /**
     * Generic 3 component color spaces.
     */
    public static final int TYPE_3CLR = 13;

    /**
     * Generic 4 component color spaces.
     */
    public static final int TYPE_4CLR = 14;

    /**
     * Generic 5 component color spaces.
     */
    public static final int TYPE_5CLR = 15;

    /**
     * Generic 6 component color spaces.
     */
    public static final int TYPE_6CLR = 16;

    /**
     * Generic 7 component color spaces.
     */
    public static final int TYPE_7CLR = 17;

    /**
     * Generic 8 component color spaces.
     */
    public static final int TYPE_8CLR = 18;

    /**
     * Generic 9 component color spaces.
     */
    public static final int TYPE_9CLR = 19;

    /**
     * Generic 10 component color spaces.
     */
    public static final int TYPE_ACLR = 20;

    /**
     * Generic 11 component color spaces.
     */
    public static final int TYPE_BCLR = 21;

    /**
     * Generic 12 component color spaces.
     */
    public static final int TYPE_CCLR = 22;

    /**
     * Generic 13 component color spaces.
     */
    public static final int TYPE_DCLR = 23;

    /**
     * Generic 14 component color spaces.
     */
    public static final int TYPE_ECLR = 24;

    /**
     * Generic 15 component color spaces.
     */
    public static final int TYPE_FCLR = 25;

    /**
     * The sRGB color space defined at
     * <A href="http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html">
     * http://www.w3.org/pub/WWW/Graphics/Color/sRGB.html
     * </A>.
     */
    public static final int CS_sRGB = 1000;

    /**
     * A built-in linear RGB color space.  This space is based on the
     * same RGB primaries as CS_sRGB, but has a linear tone reproduction curve.
     */
    public static final int CS_LINEAR_RGB = 1004;

    /**
     * The CIEXYZ conversion color space defined above.
     */
    public static final int CS_CIEXYZ = 1001;

    /**
     * The Photo YCC conversion color space.
     */
    public static final int CS_PYCC = 1002;

    /**
     * The built-in linear gray scale color space.
     */
    public static final int CS_GRAY = 1003;


    /**
     * Constructs a ColorSpace object given a color space type
     * and the number of components.
     */
    protected ColorSpace (int type, int numcomponents) {
        this.type = type;
        this.numComponents = numcomponents;
    }


    /**
     * Returns a ColorSpace representing one of the specific
     * predefined color spaces.
     * @param colorspace a specific color space identified by one of
     *        the predefined class constants (e.g. CS_sRGB, CS_LINEAR_RGB,
     *        CS_CIEXYZ, CS_GRAY, or CS_PYCC)
     */
    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public static ColorSpace getInstance (int colorspace)
    {
    ColorSpace    theColorSpace;

        switch (colorspace) {
        case CS_sRGB:
            if (sRGBspace == null) {
                ICC_Profile theProfile = ICC_Profile.getInstance (CS_sRGB);
                sRGBspace = new ICC_ColorSpace (theProfile);
            }

            theColorSpace = sRGBspace;
            break;
        
        case CS_CIEXYZ:
            if (XYZspace == null) {
                ICC_Profile theProfile = ICC_Profile.getInstance (CS_CIEXYZ);
                XYZspace = new ICC_ColorSpace (theProfile);
            }

            theColorSpace = XYZspace;
            break;
        
        case CS_PYCC:
            if (PYCCspace == null) {
                ICC_Profile theProfile = ICC_Profile.getInstance (CS_PYCC);
                PYCCspace = new ICC_ColorSpace (theProfile);
            }

            theColorSpace = PYCCspace;
            break;
        

        case CS_GRAY:
            if (GRAYspace == null) {
                ICC_Profile theProfile = ICC_Profile.getInstance (CS_GRAY);
                GRAYspace = new ICC_ColorSpace (theProfile);
            }

            theColorSpace = GRAYspace;
            break;
        

        case CS_LINEAR_RGB:
            if (LINEAR_RGBspace == null) {
                ICC_Profile theProfile = ICC_Profile.getInstance(CS_LINEAR_RGB);
                LINEAR_RGBspace = new ICC_ColorSpace (theProfile);
            }

            theColorSpace = LINEAR_RGBspace;
            break;
        

        default:
            throw new IllegalArgumentException ("Unknown color space");
        }
        
        return theColorSpace;
    }


    /**
     * Returns true if the ColorSpace is CS_sRGB.
     */
    public boolean isCS_sRGB () {
        /* REMIND - make sure we know sRGBspace exists already */
        return (this == sRGBspace);
    }
    
    /**
     * Transforms a color value assumed to be in this ColorSpace
     * into a value in the default CS_sRGB color space.
     * <p>
     * This method transforms color values using algorithms designed
     * to produce the best perceptual match between input and output
     * colors.  In order to do colorimetric conversion of color values,
     * you should use the <code>toCIEXYZ</code>
     * method of this color space to first convert from the input 
     * color space to the CS_CIEXYZ color space, and then use the 
     * <code>fromCIEXYZ</code> method of the CS_sRGB color space to 
     * convert from CS_CIEXYZ to the output color space. 
     * See {@link #toCIEXYZ(float[]) toCIEXYZ} and
     * {@link #fromCIEXYZ(float[]) fromCIEXYZ} for further information.
     * <p>
     * @param colorvalue a float array with length of at least the number
     *        of components in this ColorSpace
     * @return a float array of length 3
     */
    public abstract float[] toRGB(float[] colorvalue);


    /**
     * Transforms a color value assumed to be in the default CS_sRGB
     * color space into this ColorSpace.
     * <p>
     * This method transforms color values using algorithms designed
     * to produce the best perceptual match between input and output
     * colors.  In order to do colorimetric conversion of color values,
     * you should use the <code>toCIEXYZ</code>
     * method of the CS_sRGB color space to first convert from the input
     * color space to the CS_CIEXYZ color space, and then use the
     * <code>fromCIEXYZ</code> method of this color space to
     * convert from CS_CIEXYZ to the output color space.
     * See {@link #toCIEXYZ(float[]) toCIEXYZ} and
     * {@link #fromCIEXYZ(float[]) fromCIEXYZ} for further information.
     * <p>
     * @param rgbvalue a float array with length of at least 3
     * @return a float array with length equal to the number of
     *         components in this ColorSpace
     */
    public abstract float[] fromRGB(float[] rgbvalue);


    /**
     * Transforms a color value assumed to be in this ColorSpace
     * into the CS_CIEXYZ conversion color space.
     * <p>
     * This method transforms color values using relative colorimetry,
     * as defined by the International Color Consortium standard.  This
     * means that the XYZ values returned by this method are represented
     * relative to the D50 white point of the CS_CIEXYZ color space.
     * This representation is useful in a two-step color conversion
     * process in which colors are transformed from an input color
     * space to CS_CIEXYZ and then to an output color space.  This
     * representation is not the same as the XYZ values that would
     * be measured from the given color value by a colorimeter.
     * A further transformation is necessary to compute the XYZ values
     * that would be measured using current CIE recommended practices.
     * See the {@link ICC_ColorSpace#toCIEXYZ(float[]) toCIEXYZ} method of
     * <code>ICC_ColorSpace</code> for further information.
     * <p>
     * @param colorvalue a float array with length of at least the number
     *        of components in this ColorSpace
     * @return a float array of length 3
     */
    public abstract float[] toCIEXYZ(float[] colorvalue);


    /**
     * Transforms a color value assumed to be in the CS_CIEXYZ conversion
     * color space into this ColorSpace.
     * <p>
     * This method transforms color values using relative colorimetry,
     * as defined by the International Color Consortium standard.  This
     * means that the XYZ argument values taken by this method are represented
     * relative to the D50 white point of the CS_CIEXYZ color space.
     * This representation is useful in a two-step color conversion
     * process in which colors are transformed from an input color
     * space to CS_CIEXYZ and then to an output color space.  The color
     * values returned by this method are not those that would produce
     * the XYZ value passed to the method when measured by a colorimeter.
     * If you have XYZ values corresponding to measurements made using
     * current CIE recommended practices, they must be converted to D50
     * relative values before being passed to this method.
     * See the {@link ICC_ColorSpace#fromCIEXYZ(float[]) fromCIEXYZ} method of
     * <code>ICC_ColorSpace</code> for further information.
     * <p>
     * @param colorvalue a float array with length of at least 3
     * @return a float array with length equal to the number of
     *         components in this ColorSpace
     */
    public abstract float[] fromCIEXYZ(float[] colorvalue);

    /**
     * Returns the color space type of this ColorSpace (for example
     * TYPE_RGB, TYPE_XYZ, ...).  The type defines the
     * number of components of the color space and the interpretation,
     * e.g. TYPE_RGB identifies a color space with three components - red,
     * green, and blue.  It does not define the particular color
     * characteristics of the space, e.g. the chromaticities of the
     * primaries.
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the number of components of this ColorSpace.
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * Returns the name of the component given the component index
     */
    public String getName (int idx) {
        /* REMIND - handle common cases here */
        return new String("Unnamed color component("+idx+")");
    }

    /* Returns true if cspace is the XYZspace.
     */
    static boolean isCS_CIEXYZ(ColorSpace cspace) {
        return (cspace == XYZspace);
    }
}
