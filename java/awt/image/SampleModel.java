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

/**
 *  This abstract class defines an interface for extracting samples of pixels
 *  in an image.  All image data is expressed as a collection of pixels.
 *  Each pixel consists of a number of samples. A sample is a datum
 *  for one band of an image and a band consists of all samples of a 
 *  particular type in an image.  For example, a pixel might contain 
 *  three samples representing its red, green and blue components.
 *  There are three bands in the image containing this pixel.  One band
 *  consists of all the red samples from all pixels in the
 *  image.  The second band consists of all the green samples and 
 *  the remaining band consists of all of the blue samples.  The pixel
 *  can be stored in various formats.  For example, all samples from
 *  a particular band can be stored contiguously or all samples from a
 *  single pixel can be stored contiguously.
 *  In the Java 2D(tm) API, all built-in image processing and display
 *  operators process samples which represent unsigned integral values.
 *  <p>
 *  A collection of pixels is represented as a Raster, which consists of
 *  a DataBuffer and a SampleModel.  The SampleModel allows access to
 *  samples in the DataBuffer and may provide low-level information that
 *  a programmer can use to directly manipulate samples and pixels in the
 *  DataBuffer.
 *  <p>
 *  This class is generally a fall back method for dealing with
 *  images.  More efficient code will cast the SampleModel to the
 *  appropriate subclass and extract the information needed to directly
 *  manipulate pixels in the DataBuffer.
 *
 *  @see java.awt.image.DataBuffer
 *  @see java.awt.image.Raster
 *  @see java.awt.image.ComponentSampleModel
 *  @see java.awt.image.PixelInterleavedSampleModel
 *  @see java.awt.image.BandedSampleModel
 *  @see java.awt.image.MultiPixelPackedSampleModel
 *  @see java.awt.image.SinglePixelPackedSampleModel
 */

public abstract class SampleModel
{

    /** Width in pixels of the region of image data that this SampleModel
     *  describes.
     */
    protected int width;

    /** Height in pixels of the region of image data that this SampleModel
     *  describes.
     */
    protected int height;

    /** Number of bands of the image data that this SampleModel describes. */
    protected int numBands;

    /** Data type of the DataBuffer storing the pixel data.
     *  @see java.awt.image.DataBuffer
     */
    protected int dataType; 

    static private native void initIDs();
    static {
        ColorModel.loadLibraries();
        initIDs();
    }

    /**
     * Constructs a SampleModel with the specified parameters.
     * @param dataType	The data type of the DataBuffer storing the pixel data.
     * @param w 	The width (in pixels) of the region of image data.
     * @param h         The height (in pixels) of the region of image data.
     * @param numBands  The number of bands of the image data.
     * @throws IllegalArgumentException if <code>w</code> or <code>h</code>
     *         is not greater than 0
     * @throws IllegalArgumentException if the product of <code>w</code>
     *         and <code>h</code> is greater than 
     *         <code>Integer.MAX_VALUE</code>
     * @throws IllegalArgumentException if <code>dataType</code> is not
     *         one of the supported data types
     */
    public SampleModel(int dataType, int w, int h, int numBands)
    {
        float size = (float)w*h;
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Width ("+w+") and height ("+
                                               height+") must be > 0");
        }
        if (size >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Dimensions (width="+w+
                                               " height="+h+") are too large");
        }

        if (dataType < DataBuffer.TYPE_BYTE ||
            (dataType > DataBuffer.TYPE_DOUBLE &&
             dataType != DataBuffer.TYPE_UNDEFINED))
        {
            throw new IllegalArgumentException("Unsupported dataType: "+
                                               dataType);
        }

        if (numBands <= 0) {
            throw new IllegalArgumentException("Number of bands must be > 0");
        }
        
	this.dataType = dataType;
	this.width = w;
	this.height = h;
	this.numBands = numBands;
    }

    /** Returns the width in pixels. */
    final public int getWidth() {
	 return width;
    }

    /** Returns the height in pixels. */
    final public int getHeight() {
	 return height;
    }

    /** Returns the total number of bands of image data. */
    final public int getNumBands() {
	 return numBands;
    }

    /** Returns the number of data elements needed to transfer a pixel
     *  via the getDataElements and setDataElements methods.  When pixels
     *  are transferred via these methods, they may be transferred in a
     *  packed or unpacked format, depending on the implementation of the
     *  SampleModel.  Using these methods, pixels are transferred as an
     *  array of getNumDataElements() elements of a primitive type given
     *  by getTransferType().  The TransferType may or may not be the same
     *  as the storage DataType.
     *  @see #getDataElements(int, int, Object, DataBuffer)
     *  @see #getDataElements(int, int, int, int, Object, DataBuffer)
     *  @see #setDataElements(int, int, Object, DataBuffer)
     *  @see #setDataElements(int, int, int, int, Object, DataBuffer)
     *  @see #getTransferType
     */
    public abstract int getNumDataElements();
    
    /** Returns the data type of the DataBuffer storing the pixel data. */
    final public int getDataType() {
	return dataType;
    }

    /** Returns the TransferType used to transfer pixels via the
     *  getDataElements and setDataElements methods.  When pixels
     *  are transferred via these methods, they may be transferred in a
     *  packed or unpacked format, depending on the implementation of the
     *  SampleModel.  Using these methods, pixels are transferred as an
     *  array of getNumDataElements() elements of a primitive type given
     *  by getTransferType().  The TransferType may or may not be the same
     *  as the storage DataType.  The TransferType will be one of the types
     *  defined in DataBuffer.
     *  @see #getDataElements(int, int, Object, DataBuffer)
     *  @see #getDataElements(int, int, int, int, Object, DataBuffer)
     *  @see #setDataElements(int, int, Object, DataBuffer)
     *  @see #setDataElements(int, int, int, int, Object, DataBuffer)
     *  @see #getNumDataElements
     *  @see java.awt.image.DataBuffer
     */
    public int getTransferType() {
        return dataType;
    }

    /** 
     * Returns the samples for a specified pixel in an int array,
     * one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param iArray    If non-null, returns the samples in this array.
     * @param data      The DataBuffer containing the image data.
     */
    public int[] getPixel(int x, int y, int iArray[], DataBuffer data) {

	int pixels[];

	if (iArray != null)
	    pixels = iArray;
	else
	    pixels = new int[numBands];

	for (int i=0; i<numBands; i++) {
	    pixels[i] = getSample(x, y, i, data);
	}

	return pixels;
    }

    /** 
     * Returns data for a single pixel in a primitive array of type
     * TransferType.  For image data supported by the Java 2D API, this
     * will be one of DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT, or
     * DataBuffer.TYPE_INT.  Data may be returned in a packed format,
     * thus increasing efficiency for data transfers. Generally, obj
     * should be passed in as null, so that the Object will be created
     * automatically and will be of the right primitive data type.
     * <p>
     * The following code illustrates transferring data for one pixel from
     * DataBuffer <code>db1</code>, whose storage layout is described by
     * SampleModel <code>sm1</code>, to DataBuffer <code>db2</code>, whose
     * storage layout is described by SampleModel <code>sm2</code>.
     * The transfer will generally be more efficient than using
     * getPixel/setPixel.
     * <pre>
     * 	     SampleModel sm1, sm2;
     *	     DataBuffer db1, db2;
     * 	     sm2.setDataElements(x, y, sm1.getDataElements(x, y, null, db1), db2);
     * </pre>
     * Using getDataElements/setDataElements to transfer between two
     * DataBuffer/SampleModel pairs is legitimate if the SampleModels have
     * the same number of bands, corresponding bands have the same number of
     * bits per sample, and the TransferTypes are the same.
     * <p>
     * If obj is non-null, it should be a primitive array of type TransferType.
     * Otherwise, a ClassCastException is thrown.  An
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds, or if obj is non-null and is not large enough to hold
     * the pixel data.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param obj       If non-null, a primitive array in which to return
     *                  the pixel data.
     * @param data      The DataBuffer containing the image data.
     * @see #getNumDataElements
     * @see #getTransferType
     * @see java.awt.image.DataBuffer
     */
    public abstract Object getDataElements(int x, int y,
                                           Object obj, DataBuffer data);

    /** 
     * Returns the pixel data for the specified rectangle of pixels in a
     * primitive array of type TransferType.
     * For image data supported by the Java 2D API, this
     * will be one of DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT, or
     * DataBuffer.TYPE_INT.  Data may be returned in a packed format,
     * thus increasing efficiency for data transfers. Generally, obj
     * should be passed in as null, so that the Object will be created
     * automatically and will be of the right primitive data type.
     * <p>
     * The following code illustrates transferring data for a rectangular
     * region of pixels from
     * DataBuffer <code>db1</code>, whose storage layout is described by
     * SampleModel <code>sm1</code>, to DataBuffer <code>db2</code>, whose
     * storage layout is described by SampleModel <code>sm2</code>.
     * The transfer will generally be more efficient than using
     * getPixels/setPixels.
     * <pre>
     * 	     SampleModel sm1, sm2;
     *	     DataBuffer db1, db2;
     * 	     sm2.setDataElements(x, y, w, h, sm1.getDataElements(x, y, w,
     *                           h, null, db1), db2);
     * </pre>
     * Using getDataElements/setDataElements to transfer between two
     * DataBuffer/SampleModel pairs is legitimate if the SampleModels have
     * the same number of bands, corresponding bands have the same number of
     * bits per sample, and the TransferTypes are the same.
     * <p>
     * If obj is non-null, it should be a primitive array of type TransferType.
     * Otherwise, a ClassCastException is thrown.  An
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds, or if obj is non-null and is not large enough to hold
     * the pixel data.
     * @param x 	The minimum X coordinate of the pixel rectangle.
     * @param y 	The minimum Y coordinate of the pixel rectangle.
     * @param w         The width of the pixel rectangle.
     * @param h         The height of the pixel rectangle.
     * @param obj       If non-null, a primitive array in which to return
     *                  the pixel data.
     * @param data      The DataBuffer containing the image data.
     * @see #getNumDataElements
     * @see #getTransferType
     * @see java.awt.image.DataBuffer
     */
    public Object getDataElements(int x, int y, int w, int h,
                                  Object obj, DataBuffer data) {

	int type = getTransferType();
	int numDataElems = getNumDataElements();
	int cnt = 0;
	Object o = null;

	switch(type) {

	case DataBuffer.TYPE_BYTE:

	    byte[] btemp;
	    byte[] bdata;

	    if (obj == null)
		bdata = new byte[numDataElems*w*h];
	    else
		bdata = (byte[])obj;

	    for (int i=y; i<y+h; i++) {
		for (int j=x; j<x+w; j++) {
		    o = getDataElements(j, i, o, data);
		    btemp = (byte[])o;
		    for (int k=0; k<numDataElems; k++) {
			bdata[cnt++] = btemp[k];
		    }
		}
	    }
	    obj = (Object)bdata;
	    break;

	case DataBuffer.TYPE_USHORT:

	    short[] sdata;
	    short[] stemp;

	    if (obj == null)
		sdata = new short[numDataElems*w*h];
	    else
		sdata = (short[])obj;

	    for (int i=y; i<y+h; i++) {
		for (int j=x; j<x+w; j++) {
		    o = getDataElements(j, i, o, data);
		    stemp = (short[])o;
		    for (int k=0; k<numDataElems; k++) {
			sdata[cnt++] = stemp[k];
		    }
		}
	    }

	    obj = (Object)sdata;
	    break;

	case DataBuffer.TYPE_INT:

	    int[] idata;
	    int[] itemp;

	    if (obj == null)
		idata = new int[numDataElems*w*h];
	    else
		idata = (int[])obj;

	    for (int i=y; i<y+h; i++) {
		for (int j=x; j<x+w; j++) {
		    o = getDataElements(j, i, o, data);
		    itemp = (int[])o;
		    for (int k=0; k<numDataElems; k++) {
			idata[cnt++] = itemp[k];
		    }
		}
	    }

	    obj = (Object)idata;
	    break;
	}

	return obj;
    }

    /** 
     * Sets the data for a single pixel in the specified DataBuffer from a
     * primitive array of type TransferType.  For image data supported by
     * the Java 2D API, this will be one of DataBuffer.TYPE_BYTE,
     * DataBuffer.TYPE_USHORT, or DataBuffer.TYPE_INT.  Data in the array
     * may be in a packed format, thus increasing efficiency for data
     * transfers.
     * <p>
     * The following code illustrates transferring data for one pixel from
     * DataBuffer <code>db1</code>, whose storage layout is described by
     * SampleModel <code>sm1</code>, to DataBuffer <code>db2</code>, whose
     * storage layout is described by SampleModel <code>sm2</code>.
     * The transfer will generally be more efficient than using
     * getPixel/setPixel.
     * <pre>
     * 	     SampleModel sm1, sm2;
     *	     DataBuffer db1, db2;
     * 	     sm2.setDataElements(x, y, sm1.getDataElements(x, y, null, db1),
     *                           db2);
     * </pre>
     * Using getDataElements/setDataElements to transfer between two
     * DataBuffer/SampleModel pairs is legitimate if the SampleModels have
     * the same number of bands, corresponding bands have the same number of
     * bits per sample, and the TransferTypes are the same.
     * <p>
     * obj must be a primitive array of type TransferType.  Otherwise,
     * a ClassCastException is thrown.  An
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds, or if obj is not large enough to hold the pixel data.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param obj       A primitive array containing pixel data.
     * @param data      The DataBuffer containing the image data.
     * @see #getNumDataElements
     * @see #getTransferType
     * @see java.awt.image.DataBuffer
     */
    public abstract void setDataElements(int x, int y,
                                         Object obj, DataBuffer data);

    /** 
     * Sets the data for a rectangle of pixels in the specified DataBuffer
     * from a primitive array of type TransferType.  For image data supported
     * by the Java 2D API, this will be one of DataBuffer.TYPE_BYTE,
     * DataBuffer.TYPE_USHORT, or DataBuffer.TYPE_INT.  Data in the array
     * may be in a packed format, thus increasing efficiency for data
     * transfers.
     * <p>
     * The following code illustrates transferring data for a rectangular
     * region of pixels from
     * DataBuffer <code>db1</code>, whose storage layout is described by
     * SampleModel <code>sm1</code>, to DataBuffer <code>db2</code>, whose
     * storage layout is described by SampleModel <code>sm2</code>.
     * The transfer will generally be more efficient than using
     * getPixels/setPixels.
     * <pre>
     * 	     SampleModel sm1, sm2;
     *	     DataBuffer db1, db2;
     * 	     sm2.setDataElements(x, y, w, h, sm1.getDataElements(x, y, w, h,
     *                           null, db1), db2);
     * </pre>
     * Using getDataElements/setDataElements to transfer between two
     * DataBuffer/SampleModel pairs is legitimate if the SampleModels have
     * the same number of bands, corresponding bands have the same number of
     * bits per sample, and the TransferTypes are the same.
     * <p>
     * obj must be a primitive array of type TransferType.  Otherwise,
     * a ClassCastException is thrown.  An
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds, or if obj is not large enough to hold the pixel data.
     * @param x 	The minimum X coordinate of the pixel rectangle.
     * @param y 	The minimum Y coordinate of the pixel rectangle.
     * @param w         The width of the pixel rectangle.
     * @param h         The height of the pixel rectangle.
     * @param obj       A primitive array containing pixel data.
     * @param data      The DataBuffer containing the image data.
     * @see #getNumDataElements
     * @see #getTransferType
     * @see java.awt.image.DataBuffer
     */    
    public void setDataElements(int x, int y, int w, int h,
                                Object obj, DataBuffer data) {

	int cnt = 0;
	Object o = null;
	int type = getTransferType();
	int numDataElems = getNumDataElements();

	switch(type) {

	case DataBuffer.TYPE_BYTE:

	    byte[] barray = (byte[])obj;
	    byte[] btemp = new byte[numDataElems];

	    for (int i=y; i<y+h; i++) {
		for (int j=x; j<x+w; j++) {
		    for (int k=0; k<numDataElems; k++) {
			btemp[k] = barray[cnt++];
		    }

		    setDataElements(j, i, btemp, data);
		}
	    }
	    break;

	case DataBuffer.TYPE_USHORT:

	    short[] sarray = (short[])obj;
	    short[] stemp = new short[numDataElems];

	    for (int i=y; i<y+h; i++) {
		for (int j=x; j<x+w; j++) {
		    for (int k=0; k<numDataElems; k++) {
			stemp[k] = sarray[cnt++];
		    }

		    setDataElements(j, i, stemp, data);
		}
	    }
	    break;

	case DataBuffer.TYPE_INT:

	    int[] iArray = (int[])obj;
	    int[] itemp = new int[numDataElems];

	    for (int i=y; i<y+h; i++) {
		for (int j=x; j<x+w; j++) {
		    for (int k=0; k<numDataElems; k++) {
			itemp[k] = iArray[cnt++];
		    }

		    setDataElements(j, i, itemp, data);
		}
	    }
	    break;
	}

    }

    /**
     * Returns the samples for the specified pixel in an array of float.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param fArray    If non-null, returns the samples in this array.
     * @param data      The DataBuffer containing the image data.
     */
    public float[] getPixel(int x, int y, float fArray[],
			    DataBuffer data) {

	float pixels[];

	if (fArray != null)
	    pixels = fArray;
	else
	    pixels = new float[numBands];

	for (int i=0; i<numBands; i++)
	    pixels[i] = getSampleFloat(x, y, i, data);

	return pixels;
    }

    /**
     * Returns the samples for the specified pixel in an array of double.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param dArray 	If non-null, returns the samples in this array.
     * @param data 	The DataBuffer containing the image data.
     */
    public double[] getPixel(int x, int y, double dArray[],
			     DataBuffer data) {

	double pixels[];

	if(dArray != null)
	    pixels = dArray;
	else
	    pixels = new double[numBands];

	for (int i=0; i<numBands; i++)
	    pixels[i] = getSampleDouble(x, y, i, data);

	return pixels;
    }

    /**
     * Returns all samples for a rectangle of pixels in an
     * int array, one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param iArray 	If non-null, returns the samples in this array.
     * @param data 	The DataBuffer containing the image data.
     */
    public int[] getPixels(int x, int y, int w, int h,
                           int iArray[], DataBuffer data) {

	int pixels[];
	int Offset=0;

	if (iArray != null)
	    pixels = iArray;
	else
	    pixels = new int[numBands * w * h];

	for (int i=y; i<(h+y); i++) {
	    for (int j=x; j<(w+x); j++) {
		for(int k=0; k<numBands; k++) {
		    pixels[Offset++] = getSample(j, i, k, data);
		}
	    }
	}

	return pixels;
    }

    /**
     * Returns all samples for a rectangle of pixels in a float
     * array, one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param fArray 	If non-null, returns the samples in this array.
     * @param data 	The DataBuffer containing the image data.
     */
    public float[] getPixels(int x, int y, int w, int h,
                             float fArray[], DataBuffer data) {

	float pixels[];
	int Offset = 0;

	if (fArray != null)
	    pixels = fArray;
	else
	    pixels = new float[numBands * w * h];

	for (int i=y; i<(h+y); i++) {
	    for(int j=x; j<(w+x); j++) {
		for(int k=0; k<numBands; k++) {
		    pixels[Offset++] = getSampleFloat(j, i, k, data);
		}
	    }
	}

	return pixels;
    }

    /**
     * Returns all samples for a rectangle of pixels in a double
     * array, one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param dArray 	If non-null, returns the samples in this array.
     * @param data 	The DataBuffer containing the image data.
     */
    public double[] getPixels(int x, int y, int w, int h,
                              double dArray[], DataBuffer data) {
	double pixels[];
	int    Offset = 0;

	if (dArray != null)
	    pixels = dArray;
	else
	    pixels = new double[numBands * w * h];

        // Fix 4217412
	for (int i=y; i<(h+y); i++) {
	    for (int j=x; j<(w+x); j++) {
		for (int k=0; k<numBands; k++) {
		    pixels[Offset++] = getSampleDouble(j, i, k, data);
		}
	    }
	}

	return pixels;
    }


    /**
     * Returns the sample in a specified band for the pixel located
     * at (x,y) as an int.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param b 	The band to return.
     * @param data 	The DataBuffer containing the image data.
     */
    public abstract int getSample(int x, int y, int b, DataBuffer data);


    /**
     * Returns the sample in a specified band
     * for the pixel located at (x,y) as a float.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param b 	The band to return.
     * @param data 	The DataBuffer containing the image data.
     */
    public float getSampleFloat(int x, int y, int b, DataBuffer data) {

	float sample;
	sample = (float) getSample(x, y, b, data);
	return sample;
    }

    /**
     * Returns the sample in a specified band
     * for a pixel located at (x,y) as a double.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param b 	The band to return.
     * @param data 	The DataBuffer containing the image data.
     */
    public double getSampleDouble(int x, int y, int b, DataBuffer data) {

	double sample;

	sample = (double) getSample(x, y, b, data);
	return sample;
    }

    /**
     * Returns the samples for a specified band for the specified rectangle
     * of pixels in an int array, one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param b 	The band to return.
     * @param iArray 	If non-null, returns the samples in this array.
     * @param data 	The DataBuffer containing the image data.
     */
    public int[] getSamples(int x, int y, int w, int h, int b,
                            int iArray[], DataBuffer data) {
	int pixels[];
	int Offset=0;

	if (iArray != null)
	    pixels = iArray;
	else
	    pixels = new int[w * h];

	for(int i=y; i<(h+y); i++) {
	    for (int j=x; j<(w+x); j++) {
		pixels[Offset++] = getSample(j, i, b, data);
	    }
	}

	return pixels;
    }

    /**
     * Returns the samples for a specified band for the specified rectangle
     * of pixels in a float array, one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param b 	The band to return.
     * @param fArray 	If non-null, returns the samples in this array.
     * @param data 	The DataBuffer containing the image data.
     */
    public float[] getSamples(int x, int y, int w, int h,
                              int b, float fArray[],
                              DataBuffer data) {
	float pixels[];
	int   Offset=0;

	if (fArray != null)
	    pixels = fArray;
	else
	    pixels = new float[w * h];

	for (int i=y; i<(h+y); i++) {
	    for (int j=x; j<(w+x); j++) {
		pixels[Offset++] = getSampleFloat(j, i, b, data);
	    }
	}

	return pixels;
    }

    /**
     * Returns the samples for a specified band for a specified rectangle
     * of pixels in a double array, one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param b 	The band to return.
     * @param dArray 	If non-null, returns the samples in this array.
     * @param data 	The DataBuffer containing the image data.
     */
    public double[] getSamples(int x, int y, int w, int h,
                               int b, double dArray[],
                               DataBuffer data) {
	double pixels[];
	int    Offset=0;

	if (dArray != null)
	    pixels = dArray;
	else
	    pixels = new double[w * h];

	for (int i=y; i<(y+h); i++) {
	    for (int j=x; j<(x+w); j++) {
		pixels[Offset++] = getSampleDouble(j, i, b, data);
	    }
	}

	return pixels;
    }

    /**
     * Sets a pixel in 	the DataBuffer using an int array of samples for input.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param iArray 	The input samples in an int array.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setPixel(int x, int y, int iArray[], DataBuffer data) {

	for (int i=0; i<numBands; i++)
	    setSample(x, y, i, iArray[i], data);
    }

    /**
     * Sets a pixel in the DataBuffer using a float array of samples for input.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param fArray 	The input samples in a float array.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setPixel(int x, int y, float fArray[], DataBuffer data) {

	for (int i=0; i<numBands; i++)
	    setSample(x, y, i, fArray[i], data);
    }

    /**
     * Sets a pixel in the DataBuffer using a double array of samples for input.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param dArray 	The input samples in a double array.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setPixel(int x, int y, double dArray[], DataBuffer data) {

	for (int i=0; i<numBands; i++)
	    setSample(x, y, i, dArray[i], data);
    }

    /**
     * Sets all samples for a rectangle of pixels from an int array containing
     * one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param iArray 	The input samples in an int array.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setPixels(int x, int y, int w, int h,
                          int iArray[], DataBuffer data) {
	int Offset=0;

	for (int i=y; i<(y+h); i++) {
	    for (int j=x; j<(x+w); j++) {
		for (int k=0; k<numBands; k++) {
		    setSample(j, i, k, iArray[Offset++], data);
		}
	    }
	}
    }

    /**
     * Sets all samples for a rectangle of pixels from a float array containing
     * one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param fArray 	The input samples in a float array.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setPixels(int x, int y, int w, int h,
                          float fArray[], DataBuffer data) {
	int Offset=0;

	for (int i=y; i<(y+h); i++) {
	    for (int j=x; j<(x+w); j++) {
		for(int k=0; k<numBands; k++) {
		    setSample(j, i, k, fArray[Offset++], data);
		}
	    }
	}
    }

    /**
     * Sets all samples for a rectangle of pixels from a double array containing
     * one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param dArray 	The input samples in a double array.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setPixels(int x, int y, int w, int h,
                          double dArray[], DataBuffer data) {
	int Offset=0;

	for (int i=y; i<(y+h); i++) {
	    for (int j=x; j<(x+w); j++) {
		for (int k=0; k<numBands; k++) {
		    setSample(j, i, k, dArray[Offset++], data);
		}
	    }
	}
    }

    /**
     * Sets a sample in the specified band for the pixel located at (x,y)
     * in the DataBuffer using an int for input.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param b 	The band to set.
     * @param s 	The input sample as an int.
     * @param data 	The DataBuffer containing the image data.
     */
    public abstract void setSample(int x, int y, int b,
                                   int s,
                                   DataBuffer data);

    /**
     * Sets a sample in the specified band for the pixel located at (x,y)
     * in the DataBuffer using a float for input.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param b 	The band to set.
     * @param s 	The input sample as a float.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setSample(int x, int y, int b,
			  float s ,
			  DataBuffer data) {
	int sample = (int)s;

	setSample(x, y, b, sample, data);
    }

    /**
     * Sets a sample in the specified band for the pixel located at (x,y)
     * in the DataBuffer using a double for input.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the pixel location.
     * @param y 	The Y coordinate of the pixel location.
     * @param b 	The band to set.
     * @param s 	The input sample as a double.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setSample(int x, int y, int b,
			  double s,
			  DataBuffer data) {
	int sample = (int)s;

	setSample(x, y, b, sample, data);
    }

    /**
     * Sets the samples in the specified band for the specified rectangle
     * of pixels from an int array containing one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param b 	The band to set.
     * @param iArray 	The input samples in an int array.
     * @param data      The DataBuffer containing the image data.
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           int iArray[], DataBuffer data) {

	int Offset=0;

	for (int i=y; i<(y+h); i++) {
	    for (int j=x; j<(x+w); j++) {
		setSample(j, i, b, iArray[Offset++], data);
	    }
	}
    }

    /**
     * Sets the samples in the specified band for the specified rectangle
     * of pixels from a float array containing one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param b 	The band to set.
     * @param fArray 	The input samples in a float array.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           float fArray[], DataBuffer data) {
	int Offset=0;

	for (int i=y; i<(y+h); i++) {
	    for (int j=x; j<(x+w); j++) {
		setSample(j, i, b, fArray[Offset++], data);
	    }
	}
    }

    /**
     * Sets the samples in the specified band for the specified rectangle
     * of pixels from a double array containing one sample per array element.
     * ArrayIndexOutOfBoundsException may be thrown if the coordinates are
     * not in bounds.
     * @param x 	The X coordinate of the upper left pixel location.
     * @param y 	The Y coordinate of the upper left pixel location.
     * @param w 	The width of the pixel rectangle.
     * @param h 	The height of the pixel rectangle.
     * @param b 	The band to set.
     * @param dArray 	The input samples in a double array.
     * @param data 	The DataBuffer containing the image data.
     */
    public void setSamples(int x, int y, int w, int h, int b,
                           double dArray[], DataBuffer data) {
	int Offset=0;

	for (int i=y; i<(y+h); i++) {
	    for (int j=x; j<(x+w); j++) {
		setSample(j, i, b, dArray[Offset++], data);
	    }
	}
    }

    /**
     *  Creates a SampleModel which describes data in this SampleModel's
     *  format, but with a different width and height.
     */
    public abstract SampleModel createCompatibleSampleModel(int w, int h);

    /**
     * Creates a new SampleModel 
     * with a subset of the bands of this
     * SampleModel.
     */
    public abstract SampleModel createSubsetSampleModel(int bands[]);

    /**
     * Creates a DataBuffer that corresponds to this SampleModel.
     * The DataBuffer's width and height will match this SampleModel's.
     */
    public abstract DataBuffer createDataBuffer();

    /** Returns the size in bits of samples for all bands. */
    public abstract int[] getSampleSize();

    /** Returns the size in bits of samples for the specified band. */
    public abstract int getSampleSize(int band);

}
