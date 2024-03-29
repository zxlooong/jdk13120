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
 * This class exists to wrap one or more data arrays.  Each data array in
 * the DataBuffer is referred to as a bank.  Accessor methods for getting
 * and setting elements of the DataBuffer's banks exist with and without
 * a bank specifier.  The methods without a bank specifier use the default 0th
 * bank.  The DataBuffer can optionally take an offset per bank, so that
 * data in an existing array can be used even if the interesting data
 * doesn't start at array location zero.  Getting or setting the 0th
 * element of a bank, uses the (0+offset)th element of the array.  The
 * size field specifies how much of the data array is available for
 * use.  Size + offset for a given bank should never be greater
 * than the length of the associated data array.  The data type of
 * a data buffer indicates the type of the data array(s) and may also
 * indicate additional semantics, e.g. storing unsigned 8-bit data
 * in elements of a byte array.  The data type may be TYPE_UNDEFINED
 * or one of the types defined below.  Other types may be added in
 * the future.  Generally, an object of class DataBuffer will be cast down
 * to one of its data type specific subclasses to access data type specific
 * methods for improved performance.  Currently, the Java 2D(tm) API 
 * image classes use only TYPE_BYTE, TYPE_USHORT, and TYPE_INT DataBuffers 
 * to store image data.
 * @see java.awt.image.Raster
 * @see java.awt.image.SampleModel
 */

public abstract class DataBuffer
{

    /** Tag for unsigned byte data. */
    public static final int TYPE_BYTE  = 0;

    /** Tag for unsigned short data. */
    public static final int TYPE_USHORT = 1;
 
    /** Tag for signed short data.  Placeholder for future use. */
    public static final int TYPE_SHORT = 2;

    /** Tag for int data. */
    public static final int TYPE_INT   = 3;

    /** Tag for float data.  Placeholder for future use. */
    public static final int TYPE_FLOAT  = 4;

    /** Tag for double data.  Placeholder for future use. */
    public static final int TYPE_DOUBLE  = 5;

    /** Tag for undefined data */
    public static final int TYPE_UNDEFINED = 32;
 
    /** The data type of this DataBuffer. */
    protected int dataType;
 
    /** The number of banks in this DataBuffer. */
    protected int banks;

    /** Offset into default (first) bank from which to get the first element. */
    protected int offset;
 
    /** Usable size of all banks. */
    protected int size;

    /** Offsets into all banks. */
    protected int offsets[];

    /** Size of the data types indexed by DataType tags defined above. */
    private static final int dataTypeSize[] = {8,16,16,32,32,64};
 
    /** Returns the size (in bits) of the data type, given a datatype tag. 
      * @param type the value of one of the defined datatype tags
      * @return the size of the data type
      * @throws IllegalArgumentException if <code>type</code> is less than 
      *         zero or greater than {@link #TYPE_DOUBLE}
      */
    public static int getDataTypeSize(int type) {
        if (type < 0 || type > TYPE_DOUBLE) {
            throw new IllegalArgumentException("Unknown data type "+type);
        }
        return dataTypeSize[type];
    }

    /**
     *  Constructs a DataBuffer containing one bank of the specified
     *  data type and size.
     *  @param dataType the data type of this <code>DataBuffer</code>
     *  @param size the size of the banks
     */
    protected DataBuffer(int dataType, int size) {
        this.dataType = dataType;
        this.banks = 1;
        this.size = size;
        this.offset = 0;
        this.offsets = new int[1];  // init to 0 by new
    }

    /**
     *  Constructs a DataBuffer containing the specified number of
     *  banks.  Each bank has the specified size and an offset of 0.
     *  @param dataType the data type of this <code>DataBuffer</code>
     *  @param size the size of the banks
     *  @param numBanks the number of banks in this
     *         <code>DataBuffer</code>
     */
    protected DataBuffer(int dataType, int size, int numBanks) {
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = 0;
        this.offsets = new int[banks]; // init to 0 by new
    }

    /**
     *  Constructs a DataBuffer that contains the specified number
     *  of banks.  Each bank has the specified datatype, size and offset.
     *  @param dataType the data type of this <code>DataBuffer</code>
     *  @param size the size of the banks
     *  @param numBanks the number of banks in this
     *         <code>DataBuffer</code>
     *  @param offset the offset for each bank
     */
    protected DataBuffer(int dataType, int size, int numBanks, int offset) {
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = offset;
        this.offsets = new int[numBanks];
        for (int i = 0; i < numBanks; i++) {
            this.offsets[i] = offset;
        }
    }

    /**
     *  Constructs a DataBuffer which contains the specified number
     *  of banks.  Each bank has the specified datatype and size.  The
     *  offset for each bank is specified by its respective entry in
     *  the offsets array.
     *  @param dataType the data type of this <code>DataBuffer</code>
     *  @param size the size of the banks
     *  @param numBanks the number of banks in this
     *         <code>DataBuffer</code>
     *  @param offsets an array containing an offset for each bank.
     *  @throws ArrayIndexOutOfBoundsException if <code>numBanks</code>
     *          does not equal the length of <code>offsets</code>
     */
    protected DataBuffer(int dataType, int size, int numBanks, int offsets[]) {
        if (numBanks != offsets.length) {
            throw new ArrayIndexOutOfBoundsException("Number of banks" +
                 " does not match number of bank offsets");
        }
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = offsets[0];
        this.offsets = (int[])offsets.clone();
    }
 
    /**  Returns the data type of this DataBuffer.  */
    public int getDataType() {
        return dataType;
    }
  
    /**  Returns the size (in array elements) of all banks. */
    public int getSize() {
        return size;
    }

    /** Returns the offset of the default bank in array elements.  */
    public int getOffset() {
        return offset;
    }
 
    /** Returns the offsets (in array elements) of all the banks. */
    public int[] getOffsets() {
        return (int[])offsets.clone();
    }

    /** Returns the number of banks in this DataBuffer. */
    public int getNumBanks() {
        return banks;
    }

    /**
     * Returns the requested data array element from the first (default) bank
     * as an integer.
     */
    public int getElem(int i) {
        return getElem(0,i);
    }

    /**
     * Returns the requested data array element from the specified bank
     * as an integer.
     */
    public abstract int getElem(int bank, int i);

    /**
     * Sets the requested data array element in the first (default) bank
     * from the given integer.
     */
    public void  setElem(int i, int val) {
        setElem(0,i,val);
    }

    /**
     * Sets the requested data array element in the specified bank
     * from the given integer.
     */
    public abstract void setElem(int bank, int i, int val);

    /**
     * Returns the requested data array element from the first (default) bank
     * as a float.  The implementation in this class is to cast getElem(i)
     * to a float.  Subclasses may override this method if another
     * implementation is needed.
     */
    public float getElemFloat(int i) {
        return (float)getElem(i);
    }

    /**
     * Returns the requested data array element from the specified bank
     * as a float.  The implementation in this class is to cast getElem(bank, i)
     * to a float.  Subclasses may override this method if another 
     * implementation is needed.
     */
    public float getElemFloat(int bank, int i) {
        return (float)getElem(bank,i);
    }

    /**
     * Sets the requested data array element in the first (default) bank
     * from the given float.  The implementation in this class is to cast
     * val to an int and call setElem.  Subclasses may override this method
     * if another implementation is needed.
     */ 
    public void setElemFloat(int i, float val) {
        setElem(i,(int)val);
    }

    /**
     * Sets the requested data array element in the specified bank
     * from the given float.  The implementation in this class is to cast            * val to an int and call setElem.  Subclasses may override this method 
     * if another implementation is needed.
     */
    public void setElemFloat(int bank, int i, float val) {
        setElem(bank,i,(int)val);
    }

    /**
     * Returns the requested data array element from the first (default) bank
     * as a double.  The implementation in this class is to cast getElem(i)
     * to a double.  Subclasses may override this method if another 
     * implementation is needed.
     */
    public double getElemDouble(int i) {
        return (double)getElem(i);
    }

    /**
     * Returns the requested data array element from the specified bank as
     * a double.  The implementation in this class is to cast getElem(bank, i)
     * to a double.  Subclasses may override this method if another
     * implementation is needed.
     */
    public double getElemDouble(int bank, int i) {
        return (double)getElem(bank,i);
    }

    /**
     * Sets the requested data array element in the first (default) bank
     * from the given double.  The implementation in this class is to cast
     * val to an int and call setElem.  Subclasses may override this method
     * if another implementation is needed.
     */
    public void setElemDouble(int i, double val) {
        setElem(i,(int)val);
    }

    /**
     * Sets the requested data array element in the specified bank
     * from the given double.  The implementation in this class is to cast
     * val to an int and call setElem.  Subclasses may override this method
     * if another implementation is needed.
     */
    public void setElemDouble(int bank, int i, double val) {
        setElem(bank,i,(int)val);
    }

    static int[] toIntArray(Object obj) {
        if (obj instanceof int[]) {
            return (int[])obj;
        } else if (obj == null) {
            return null;
        } else if (obj instanceof short[]) {
            short sdata[] = (short[])obj;
            int idata[] = new int[sdata.length];
            for (int i = 0; i < sdata.length; i++) {
                idata[i] = (int)sdata[i] & 0xffff;
            }
            return idata;
        } else if (obj instanceof byte[]) {
            byte bdata[] = (byte[])obj;
            int idata[] = new int[bdata.length];
            for (int i = 0; i < bdata.length; i++) {
                idata[i] = 0xff & (int)bdata[i];
            }
            return idata;
        }
        return null;
    }
 }
