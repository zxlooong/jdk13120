/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/* ****************************************************************
 ******************************************************************
 ******************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997
 *** As  an unpublished  work pursuant to Title 17 of the United
 *** States Code.  All rights reserved.
 ******************************************************************
 ******************************************************************
 ******************************************************************/

package java.awt.image;
import java.awt.Rectangle;
import java.awt.Point;

/**
 * This class extends Raster to provide pixel writing capabilities.
 * Refer to the class comment for Raster for descriptions of how
 * a Raster stores pixels.
 *
 * <p> The constructors of this class are protected.  To instantiate
 * a WritableRaster, use one of the createWritableRaster factory methods
 * in the Raster class.
 */
public class WritableRaster extends Raster {

    /**
     *  Constructs a WritableRaster with the given SampleModel.  The
     *  WritableRaster's upper left corner is origin and it is the
     *  same size as the  SampleModel.  A DataBuffer large enough to
     *  describe the WritableRaster is automatically created.
     *  @param sampleModel     The SampleModel that specifies the layout.
     *  @param origin          The Point that specifies the origin.
     */
    protected WritableRaster(SampleModel sampleModel,
                             Point origin) {
        this(sampleModel,
             sampleModel.createDataBuffer(),
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }

    /**
     *  Constructs a WritableRaster with the given SampleModel and DataBuffer.
     *  The WritableRaster's upper left corner is origin and it is the same
     *  size as the SampleModel.  The DataBuffer is not initialized and must
     *  be compatible with SampleModel.
     *  @param sampleModel     The SampleModel that specifies the layout.
     *  @param dataBuffer      The DataBuffer that contains the image data.
     *  @param origin          The Point that specifies the origin.
     */
    protected WritableRaster(SampleModel sampleModel,
                             DataBuffer dataBuffer,
                             Point origin) {
        this(sampleModel,
             dataBuffer,
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }

    /**
     * Constructs a WritableRaster with the given SampleModel, DataBuffer,
     * and parent.  aRegion specifies the bounding rectangle of the new
     * Raster.  When translated into the base Raster's coordinate
     * system, aRegion must be contained by the base Raster.
     * (The base Raster is the Raster's ancestor which has no parent.)
     * sampleModelTranslate specifies the sampleModelTranslateX and
     * sampleModelTranslateY values of the new Raster.
     *
     * Note that this constructor should generally be called by other
     * constructors or create methods, it should not be used directly.
     * @param sampleModel     The SampleModel that specifies the layout.
     * @param dataBuffer      The DataBuffer that contains the image data.
     * @param aRegion         The Rectangle that specifies the image area.     
     * @param sampleModelTranslate  The Point that specifies the translation
     *                        from SampleModel to Raster coordinates.
     * @param parent          The parent (if any) of this raster.
     */
    protected WritableRaster(SampleModel sampleModel,
                             DataBuffer dataBuffer,
                             Rectangle aRegion,
                             Point sampleModelTranslate,
                             WritableRaster parent){
        super(sampleModel,dataBuffer,aRegion,sampleModelTranslate,parent);
    }

    /** Returns the parent WritableRaster (if any) of this WritableRaster,
     *  or else null.
     */
    public WritableRaster getWritableParent() {
        return (WritableRaster)parent;
    }

    /**
     * Create a WritableRaster with the same size, SampleModel and DataBuffer
     * as this one, but with a different location.  The new WritableRaster
     * will possess a reference to the current WritableRaster, accessible
     * through its getParent() and getWritableParent() methods.
     *
     * @param childMinX X coord of the upper left corner of the new Raster.
     * @param childMinY Y coord of the upper left corner of the new Raster.
     */
    public WritableRaster createWritableTranslatedChild(int childMinX,
                                                        int childMinY) {
        return createWritableChild(minX,minY,width,height,
                                   childMinX,childMinY,null);
    }

    /**
     * Returns a new WritableRaster which shares all or part of this
     * WritableRaster's DataBuffer.  The new WritableRaster will
     * possess a reference to the current WritableRaster, accessible
     * through its getParent() and getWritableParent() methods.
     *
     * <p> The parentX, parentY, width and height parameters form a
     * Rectangle in this WritableRaster's coordinate space, indicating
     * the area of pixels to be shared.  An error will be thrown if
     * this Rectangle is not contained with the bounds of the current
     * WritableRaster.
     *
     * <p> The new WritableRaster may additionally be translated to a
     * different coordinate system for the plane than that used by the current
     * WritableRaster.  The childMinX and childMinY parameters give
     * the new (x, y) coordinate of the upper-left pixel of the
     * returned WritableRaster; the coordinate (childMinX, childMinY)
     * in the new WritableRaster will map to the same pixel as the
     * coordinate (parentX, parentY) in the current WritableRaster.
     *
     * <p> The new WritableRaster may be defined to contain only a
     * subset of the bands of the current WritableRaster, possibly
     * reordered, by means of the bandList parameter.  If bandList is
     * null, it is taken to include all of the bands of the current
     * WritableRaster in their current order.
     *
     * <p> To create a new WritableRaster that contains a subregion of
     * the current WritableRaster, but shares its coordinate system
     * and bands, this method should be called with childMinX equal to
     * parentX, childMinY equal to parentY, and bandList equal to
     * null.
     *
     * @param parentX    X coordinate of the upper left corner in this
     *                   WritableRaster's coordinates.
     * @param parentY    Y coordinate of the upper left corner in this
     *                   WritableRaster's coordinates.
     * @param width      Width of the region starting at (parentX, parentY).
     * @param height     Height of the region starting at (parentX, parentY).
     * @param childMinX  X coordinate of the upper left corner of
     *                   the returned WritableRaster.
     * @param childMinY  Y coordinate of the upper left corner of
     *                   the returned WritableRaster.
     * @param bandList   Array of band indices, or null to use all bands.
     * @exception RasterFormatException if the subregion is outside of the
     *                               raster bounds.
     */
    public WritableRaster createWritableChild(int parentX, int parentY,
                                              int w, int h,
                                              int childMinX, int childMinY,
                                              int bandList[]) {
        if (parentX < this.minX) {
            throw new RasterFormatException("parentX lies outside raster");
        }
        if (parentY < this.minY) {
            throw new RasterFormatException("parentY lies outside raster");
        }
        if (parentX+w > this.width + this.minX) {
            throw new RasterFormatException("(parentX + width) is outside raster");
        }
        if (parentY+h > this.height + this.minY) {
            throw new RasterFormatException("(parentY + height) is outside raster");
        }

        SampleModel sm;

        if (bandList != null) {
            sm = sampleModel.createCompatibleSampleModel(sampleModel.width,
                                                         sampleModel.height);
            sm = sm.createSubsetSampleModel(bandList);
        }
        else {
            sm = sampleModel;
        }

        int deltaX = childMinX - parentX;
        int deltaY = childMinY - parentY;

        return new WritableRaster(sm,
                                  dataBuffer,
                                  new Rectangle(childMinX,childMinY,
                                                w, h),
                                  new Point(sampleModelTranslateX+deltaX,
                                            sampleModelTranslateY+deltaY),
                                  this);
    }

    /**
     * Sets the data for a single pixel from a
     * primitive array of type TransferType.  For image data supported by
     * the Java 2D(tm) API, this will be one of DataBuffer.TYPE_BYTE,
     * DataBuffer.TYPE_USHORT, or DataBuffer.TYPE_INT.  Data in the array
     * may be in a packed format, thus increasing efficiency for data
     * transfers.
     * There will be no explicit bounds checking on the parameters.  
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds, or if inData is not large enough to hold the pixel data.
     * A ClassCastException will be thrown if the input object is not null
     * and references anything other than an array of TransferType.
     * @see java.awt.image.SampleModel#setDataElements(int, int, Object, DataBuffer)
     * @param x        The X coordinate of the pixel location.
     * @param y        The Y coordinate of the pixel location.
     * @param inData   An object reference to an array of type defined by
     *                 getTransferType() and length getNumDataElements()
     *                 containing the pixel data to place at x,y.
     */
    public void setDataElements(int x, int y, Object inData) {
        sampleModel.setDataElements(x-sampleModelTranslateX,
                                    y-sampleModelTranslateY,
                                    inData, dataBuffer);
    }

    /**
     * Sets the data for a rectangle of pixels from an input Raster.
     * The input Raster must be compatible with this WritableRaster
     * in that they must have the same number of bands, corresponding bands
     * must have the same number of bits per sample, the TransferTypes
     * and NumDataElements must be the same, and the packing used by
     * the getDataElements/setDataElements must be identical.
     * There will be no explicit bounds checking on the parameters.  
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the pixel location.
     * @param y        The Y coordinate of the pixel location.
     * @param inRaster Raster containing data to place at x,y.
     */
    public void setDataElements(int x, int y, Raster inRaster) {
        int width  = inRaster.getWidth();
        int height = inRaster.getHeight();
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        int dstOffX = x+inRaster.getMinX();
        int dstOffY = y+inRaster.getMinY();
        Object tdata = null;

        for (int startY=0; startY < height; startY++) {
            tdata = inRaster.getDataElements(srcOffX, srcOffY+startY,
                                             width, 1, tdata);
            setDataElements(dstOffX, dstOffY+startY,
                         width, 1, tdata);
        }
    }

    /**
     * Sets the data for a rectangle of pixels from a
     * primitive array of type TransferType.  For image data supported by
     * the Java 2D API, this will be one of DataBuffer.TYPE_BYTE,
     * DataBuffer.TYPE_USHORT, or DataBuffer.TYPE_INT.  Data in the array
     * may be in a packed format, thus increasing efficiency for data
     * transfers.
     * There will be no explicit bounds checking on the parameters.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds, or if inData is not large enough to hold the pixel data.
     * A ClassCastException will be thrown if the input object is not null
     * and references anything other than an array of TransferType.
     * @see java.awt.image.SampleModel#setDataElements(int, int, int, int, Object, DataBuffer)
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param w        Width of the pixel rectangle.
     * @param h        Height of the pixel rectangle.
     * @param inData   An object reference to an array of type defined by
     *                 getTransferType() and length w*h*getNumDataElements()
     *                 containing the pixel data to place between x,y and
     *                 x+w-1, y+h-1.
     */
    public void setDataElements(int x, int y, int w, int h, Object inData) {
        sampleModel.setDataElements(x-sampleModelTranslateX,
                                    y-sampleModelTranslateY,
                                    w,h,inData,dataBuffer);
    }

    /**
     * Copies pixels from Raster srcRaster to this WritableRaster.  Each pixel
     * in srcRaster is copied to the same x,y address in this raster, unless 
     * the address falls outside the bounds of this raster.  srcRaster
     * must have the same number of bands as this WritableRaster.  The
     * copy is a simple copy of source samples to the corresponding destination
     * samples.
     * <p>
     * If all samples of both source and destination Rasters are of
     * integral type and less than or equal to 32 bits in size, then calling
     * this method is equivalent to executing the following code for all
     * <code>x,y</code> addresses valid in both Rasters.
     * <pre>
     *       Raster srcRaster;
     *       WritableRaster dstRaster;
     *       for (int b = 0; b < srcRaster.getNumBands(); b++) {
     *           dstRaster.setSample(x, y, b, srcRaster.getSample(x, y, b));
     *       }
     * </pre>
     * Thus, if the source sample size is greater than the destination
     * sample size for a particular band, the high order bits of the source
     * sample are truncated.  If the source sample size is less than the
     * destination size for a particular band, the high order bits of the
     * destination are zero-extended or sign-extended depending on whether
     * srcRaster's SampleModel treats the sample as a signed or unsigned
     * quantity.
     * <p>
     * @param srcRaster  The  Raster from which to copy pixels.
     */
    public void setRect(Raster srcRaster) {
        setRect(0,0,srcRaster);
    }

    /**
     * Copies pixels from Raster srcRaster to this WritableRaster.
     * For each (x, y) address in srcRaster, the corresponding pixel
     * is copied to address (x+dx, y+dy) in this WritableRaster,
     * unless (x+dx, y+dy) falls outside the bounds of this raster.
     * srcRaster must have the same number of bands as this WritableRaster.
     * The copy is a simple copy of source samples to the corresponding
     * destination samples.  For details, see
     * {@link WritableRaster#setRect(Raster)}.
     *
     * @param dx        The X translation factor from src space to dst space
     *                  of the copy.
     * @param dy        The Y translation factor from src space to dst space
     *                  of the copy.
     * @param srcRaster The Raster from which to copy pixels.
     */
    public void setRect(int dx, int dy, Raster srcRaster) {
        int width  = srcRaster.getWidth();
        int height = srcRaster.getHeight();
        int srcOffX = srcRaster.getMinX();
        int srcOffY = srcRaster.getMinY();
        int dstOffX = dx+srcOffX;
        int dstOffY = dy+srcOffY;

        // Clip to this raster
        if (dstOffX+width > this.minX+this.width) {
            width = this.minX + this.width - dstOffX;
        }
        if (dstOffY+height > this.minY+this.height) {
            height = this.minY + this.height - dstOffY;
        }

        switch (srcRaster.getSampleModel().getDataType()) {
        case DataBuffer.TYPE_BYTE:
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_INT:
            int[] iData = null;    
            for (int startY=0; startY < height; startY++) {
                // Grab one scanline at a time
                iData =
                    srcRaster.getPixels(srcOffX, srcOffY+startY, width, 1,
                                        iData);
                setPixels(dstOffX, dstOffY+startY, width, 1, iData);
            }
            break;

        case DataBuffer.TYPE_FLOAT:
            float[] fData = null;    
            for (int startY=0; startY < height; startY++) {
                fData =
                    srcRaster.getPixels(srcOffX, srcOffY+startY, width, 1,
                                        fData);
                setPixels(dstOffX, dstOffY+startY, width, 1, fData);
            }
            break;

        case DataBuffer.TYPE_DOUBLE:
            double[] dData = null;    
            for (int startY=0; startY < height; startY++) {
                // Grab one scanline at a time
                dData =
                    srcRaster.getPixels(srcOffX, srcOffY+startY, width, 1,
                                        dData);
                setPixels(dstOffX, dstOffY+startY, width, 1, dData);
            }
            break;
        }
    }

    /** 
     * Sets a pixel in the DataBuffer using an int array of samples for input.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x      The X coordinate of the pixel location.
     * @param y      The Y coordinate of the pixel location.
     * @param iArray The input samples in a int array.
     */
    public void setPixel(int x, int y, int iArray[]) {
        sampleModel.setPixel(x-sampleModelTranslateX,y-sampleModelTranslateY,
                             iArray,dataBuffer);
    }

    /**
     * Sets a pixel in the DataBuffer using a float array of samples for input.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x      The X coordinate of the pixel location.
     * @param y      The Y coordinate of the pixel location.
     * @param fArray The input samples in a float array.
     */
    public void setPixel(int x, int y, float fArray[]) {
        sampleModel.setPixel(x-sampleModelTranslateX,y-sampleModelTranslateY,
			     fArray,dataBuffer);
    }

    /**
     * Sets a pixel in the DataBuffer using a double array of samples for input.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x      The X coordinate of the pixel location.
     * @param y      The Y coordinate of the pixel location.
     * @param dArray The input samples in a double array.
     */
    public void setPixel(int x, int y, double dArray[]) {
        sampleModel.setPixel(x-sampleModelTranslateX,y-sampleModelTranslateY,
			     dArray,dataBuffer);
    }

    /** 
     * Sets all samples for a rectangle of pixels from an int array containing
     * one sample per array element.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param w        Width of the pixel rectangle.
     * @param h        Height of the pixel rectangle.
     * @param iArray   The input int pixel array.
     */
    public void setPixels(int x, int y, int w, int h, int iArray[]) {
        sampleModel.setPixels(x-sampleModelTranslateX,y-sampleModelTranslateY,
                              w,h,iArray,dataBuffer);
    }

    /** 
     * Sets all samples for a rectangle of pixels from a float array containing
     * one sample per array element.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param w        Width of the pixel rectangle.
     * @param h        Height of the pixel rectangle.
     * @param fArray   The input float pixel array.
     */
    public void setPixels(int x, int y, int w, int h, float fArray[]) {
        sampleModel.setPixels(x-sampleModelTranslateX,y-sampleModelTranslateY,
                              w,h,fArray,dataBuffer);
    }

    /** 
     * Sets all samples for a rectangle of pixels from a double array containing
     * one sample per array element.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param w        Width of the pixel rectangle.
     * @param h        Height of the pixel rectangle.
     * @param dArray   The input double pixel array.
     */
    public void setPixels(int x, int y, int w, int h, double dArray[]) {
        sampleModel.setPixels(x-sampleModelTranslateX,y-sampleModelTranslateY,
                              w,h,dArray,dataBuffer);
    }

    /** 
     * Sets a sample in the specified band for the pixel located at (x,y)
     * in the DataBuffer using an int for input.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the pixel location.
     * @param y        The Y coordinate of the pixel location.
     * @param b        The band to set.
     * @param s        The input sample.
     */
    public void setSample(int x, int y, int b, int s) {
        sampleModel.setSample(x-sampleModelTranslateX,
                              y-sampleModelTranslateY, b, s,
                              dataBuffer);
    }

    /** 
     * Sets a sample in the specified band for the pixel located at (x,y)
     * in the DataBuffer using a float for input.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the pixel location.
     * @param y        The Y coordinate of the pixel location.
     * @param b        The band to set.
     * @param s        The input sample as a float.
     */
    public void setSample(int x, int y, int b, float s){
        sampleModel.setSample(x-sampleModelTranslateX,y-sampleModelTranslateY,
			      b,s,dataBuffer);
    }

    /**
     * Sets a sample in the specified band for the pixel located at (x,y)
     * in the DataBuffer using a double for input.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the pixel location.
     * @param y        The Y coordinate of the pixel location.
     * @param b        The band to set.
     * @param s        The input sample as a double.
     */
    public void setSample(int x, int y, int b, double s){
        sampleModel.setSample(x-sampleModelTranslateX,y-sampleModelTranslateY,
                                    b,s,dataBuffer);
    }

    /** 
     * Sets the samples in the specified band for the specified rectangle
     * of pixels from an int array containing one sample per array element.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param w        Width of the pixel rectangle.
     * @param h        Height of the pixel rectangle.
     * @param b        The band to set.
     * @param iArray   The input int sample array.
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           int iArray[]) {
        sampleModel.setSamples(x-sampleModelTranslateX,y-sampleModelTranslateY,
                               w,h,b,iArray,dataBuffer);
    }

    /**
     * Sets the samples in the specified band for the specified rectangle
     * of pixels from a float array containing one sample per array element.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param w        Width of the pixel rectangle.
     * @param h        Height of the pixel rectangle.
     * @param b        The band to set.
     * @param fArray   The input float sample array.
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           float fArray[]) {
        sampleModel.setSamples(x-sampleModelTranslateX,y-sampleModelTranslateY,
                               w,h,b,fArray,dataBuffer);
    }

    /**
     * Sets the samples in the specified band for the specified rectangle
     * of pixels from a double array containing one sample per array element.
     * An ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param w        Width of the pixel rectangle.
     * @param h        Height of the pixel rectangle.
     * @param b        The band to set.
     * @param dArray   The input double sample array.
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           double dArray[]) {
        sampleModel.setSamples(x-sampleModelTranslateX,y-sampleModelTranslateY,
			      w,h,b,dArray,dataBuffer);
    }

}
