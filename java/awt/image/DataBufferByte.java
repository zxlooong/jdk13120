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
 * This class extends <CODE>DataBuffer</CODE> and stores data internally as bytes.
 * Values stored in the byte array(s) of this <CODE>DataBuffer</CODE> are treated as
 * unsigned values.
 */

public final class DataBufferByte extends DataBuffer
{
    /** The default data bank. */
    byte data[];

    /** All data banks */
    byte bankdata[][];

    /**
     * Constructs a byte-based <CODE>DataBuffer</CODE> with a single bank and the
     * specified size.
     *
     * @param size The size of the <CODE>DataBuffer</CODE>.
     */
    public DataBufferByte(int size) {
      super(TYPE_BYTE,size);
      data = new byte[size];
      bankdata = new byte[1][];
      bankdata[0] = data;
    }

    /**
     * Constructs a byte based <CODE>DataBuffer</CODE> with the specified number of
     * banks all of which are the specified size.
     *
     * @param size The size of the banks in the <CODE>DataBuffer</CODE>.
     * @param numBanks The number of banks in the a<CODE>DataBuffer</CODE>.
     */
    public DataBufferByte(int size, int numBanks) {
        super(TYPE_BYTE, size, numBanks);
        bankdata = new byte[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new byte[size];
        }
        data = bankdata[0];
    }

    /**
     * Constructs a byte-based <CODE>DataBuffer</CODE> with a single bank using the
     * specified array.
     * Only the first <CODE>size</CODE> elements should be used by accessors of
     * this <CODE>DataBuffer</CODE>.  <CODE>dataArray</CODE> must be large enough to
     * hold <CODE>size</CODE> elements.
     *
     * @param dataArray The byte array for the <CODE>DataBuffer</CODE>.
     * @param size The size of the <CODE>DataBuffer</CODE> bank.
     */
    public DataBufferByte(byte dataArray[], int size) {
        super(TYPE_BYTE,size);
        data = dataArray;
        bankdata = new byte[1][];
        bankdata[0] = data;
    }

    /**
     * Constructs a byte-based <CODE>DataBuffer</CODE> with a single bank using the
     * specified array, size, and offset.  <CODE>dataArray</CODE> must have at least
     * <CODE>offset</CODE> + <CODE>size</CODE> elements.  Only elements <CODE>offset</CODE> 
     * through <CODE>offset</CODE> + <CODE>size</CODE> - 1
     * should be used by accessors of this <CODE>DataBuffer</CODE>.
     *
     * @param dataArray The byte array for the <CODE>DataBuffer</CODE>.
     * @param size The size of the <CODE>DataBuffer</CODE> bank.
     * @param offset The offset into the <CODE>dataArray</CODE>. <CODE>dataArray</CODE> 
     * must have at least <CODE>offset</CODE> + <CODE>size</CODE> elements.
     */
    public DataBufferByte(byte dataArray[], int size, int offset){
        super(TYPE_BYTE,size,1,offset);
        data = dataArray;
        bankdata = new byte[1][];
        bankdata[0] = data;
    }

    /**
     * Constructs a byte-based <CODE>DataBuffer</CODE> with the specified arrays.
     * The number of banks is equal to <CODE>dataArray.length</CODE>.
     * Only the first <CODE>size</CODE> elements of each array should be used by
     * accessors of this <CODE>DataBuffer</CODE>.
	 *
     * @param dataArray The byte arrays for the <CODE>DataBuffer</CODE>.
     * @param size The size of the banks in the <CODE>DataBuffer</CODE>.
     */
    public DataBufferByte(byte dataArray[][], int size) {
        super(TYPE_BYTE,size,dataArray.length);
        bankdata = dataArray;
        data = bankdata[0];
    }

    /**
     * Constructs a byte-based <CODE>DataBuffer</CODE> with the specified arrays, size,
     * and offsets.
     * The number of banks is equal to <CODE>dataArray.length</CODE>.  Each array must
     * be at least as large as <CODE>size</CODE> + the corresponding <CODE>offset</CODE>.   
     * There must be an entry in the <CODE>offset</CODE> array for each <CODE>dataArray</CODE> 
     * entry.  For each bank, only elements <CODE>offset</CODE> through 
     * <CODE>offset</CODE> + <CODE>size</CODE> - 1 should be used by accessors of this 
     * <CODE>DataBuffer</CODE>.
     *
     * @param dataArray The byte arrays for the <CODE>DataBuffer</CODE>.
     * @param size The size of the banks in the <CODE>DataBuffer</CODE>.
     * @param offsets The offsets into each array.
     */
    public DataBufferByte(byte dataArray[][], int size, int offsets[]) {
        super(TYPE_BYTE,size,dataArray.length,offsets);
        bankdata = dataArray;
        data = bankdata[0];
    }

    /** 
     * Returns the default (first) byte data array. 
     *    
     * @return The first byte data array.
     */
    public byte[] getData() {
        return data;
    }

    /** 
     * Returns the data array for the specified bank.    
     *   
     * @param bank The bank whose data array you want to get. 
     * @return The data array for the specified bank.
     */
    public byte[] getData(int bank) {
        return bankdata[bank];
    }

    /** 
     * Returns the data arrays for all banks. 
     * @return All of the data arrays.
     */
    public byte[][] getBankData() {
       return bankdata;
    }

    /**
     * Returns the requested data array element from the first (default) bank.
     * 
     * @param i The data array element you want to get.
     * @return The requested data array element as an integer.
     */
    public int getElem(int i) {
        return (int)(data[i+offset]) & 0xff;
    }

    /**
     * Returns the requested data array element from the specified bank
     * 
     * @param bank The bank from which you want to get a data array element.
     * @param i The data array element you want to get.
     * @return The requested data array element as an integer.
     */
    public int getElem(int bank, int i) {
        return (int)(bankdata[bank][i+offsets[bank]]) & 0xff;
    }

    /**
     * Sets the requested data array element in the first (default) bank
     * to the specified value.
     *
     * @param i The data array element you want to set.
     * @param val The integer value to which you want to set the data array element.
     */
    public void setElem(int i, int val) {
        data[i+offset] = (byte)val;
    }

    /**
     * Sets the requested data array element in the specified bank
     * from the given integer.
     * @param bank The bank in which you want to set the data array element.
     * @param i The data array element you want to set.
     * @param val The integer value to which you want to set the specified data array element.
     */
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (byte)val;
    }
}
