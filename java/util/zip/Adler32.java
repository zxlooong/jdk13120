/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.util.zip;

/**
 * A class that can be used to compute the Adler-32 checksum of a data
 * stream. An Adler-32 checksum is almost as reliable as a CRC-32 but
 * can be computed much faster.
 *
 * @see		Checksum
 * @version 	1.25, 06/27/03
 * @author 	David Connelly
 */
public
class Adler32 implements Checksum {
    private int adler = 1;

    /*
     * Loads the ZLIB library.
     */
    static {
	java.security.AccessController.doPrivileged(
		  new sun.security.action.LoadLibraryAction("zip"));
    }

    /**
     * Creates a new Adler32 class.
     */
    public Adler32() {
    }
   

    /**
     * Updates checksum with specified byte.
     * 
     * @param b an array of bytes
     */
    public void update(int b) {
	adler = update(adler, b);
    }

    /**
     * Updates checksum with specified array of bytes.
     */
    public void update(byte[] b, int off, int len) {
	if (b == null) {
	    throw new NullPointerException();
	}
	if (off < 0 || len < 0 || off > b.length - len) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	adler = updateBytes(adler, b, off, len);
    }

    /**
     * Updates checksum with specified array of bytes.
     */
    public void update(byte[] b) {
	adler = updateBytes(adler, b, 0, b.length);
    }

    /**
     * Resets checksum to initial value.
     */
    public void reset() {
	adler = 1;
    }

    /**
     * Returns checksum value.
     */
    public long getValue() {
	return (long)adler & 0xffffffffL;
    }

    private native static int update(int adler, int b);
    private native static int updateBytes(int adler, byte[] b, int off,
					  int len);
}
