/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.util.zip;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;

/**
 * This class implements a stream filter for uncompressing data in the
 * "deflate" compression format. It is also used as the basis for other
 * decompression filters, such as GZIPInputStream.
 *
 * @see		Inflater
 * @version 	1.30, 03/20/02
 * @author 	David Connelly
 */
public
class InflaterInputStream extends FilterInputStream {
    /**
     * Decompressor for this stream.
     */
    protected Inflater inf;

    /**
     * Input buffer for decompression.
     */
    protected byte[] buf;

    /**
     * Length of input buffer.
     */
    protected int len;

    private boolean closed = false;
    // this flag is set to true after EOF has reached
    private boolean reachEOF = false;
    
    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
	if (closed) {
	    throw new IOException("Stream closed");
        }
    }


    /**
     * Creates a new input stream with the specified decompressor and
     * buffer size.
     * @param in the input stream
     * @param inf the decompressor ("inflater")
     * @param size the input buffer size
     * @exception IllegalArgumentException if size is <= 0
     */
    public InflaterInputStream(InputStream in, Inflater inf, int size) {
	super(in);
        if (in == null || inf == null) {
            throw new NullPointerException();
        } else if (size <= 0) {
            throw new IllegalArgumentException("buffer size <= 0");
        }
	this.inf = inf;
	buf = new byte[size];
    }

    /**
     * Creates a new input stream with the specified decompressor and a
     * default buffer size.
     * @param in the input stream
     * @param inf the decompressor ("inflater")
     */
    public InflaterInputStream(InputStream in, Inflater inf) {
	this(in, inf, 512);
    }

	private boolean usesDefaultInflater = false;

    /**
     * Creates a new input stream with a default decompressor and buffer size.
     * @param in the input stream
     */
    public InflaterInputStream(InputStream in) {
	this(in, new Inflater());
	usesDefaultInflater = true;
    }

    /**
     * Reads a byte of uncompressed data. This method will block until
     * enough input is available for decompression.
     * @return the byte read, or -1 if end of compressed input is reached
     * @exception IOException if an I/O error has occurred
     */
    public int read() throws IOException {
	ensureOpen();
        byte[] b = new byte[1];
	return read(b, 0, 1) == -1 ? -1 : b[0] & 0xff;
    }

    /**
     * Reads uncompressed data into an array of bytes. This method will
     * block until some input can be decompressed.
     * @param b the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the
     *         compressed input is reached or a preset dictionary is needed
     * @exception ZipException if a ZIP format error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public int read(byte[] b, int off, int len) throws IOException {
	ensureOpen();
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}
	try {
	    int n;
	    while ((n = inf.inflate(b, off, len)) == 0) {
		if (inf.finished() || inf.needsDictionary()) {
                    reachEOF = true;
		    return -1;
		}
		if (inf.needsInput()) {
		    fill();
		}
	    }
	    return n;
	} catch (DataFormatException e) {
	    String s = e.getMessage();
	    throw new ZipException(s != null ? s : "Invalid ZLIB data format");
	}
    }

    /**
     * Returns 0 after EOF has reached, otherwise always return 1.
     * <p>
     * Programs should not count on this method to return the actual number
     * of bytes that could be read without blocking.
     *
     * @return     1 before EOF and 0 after EOF.
     * @exception  IOException  if an I/O error occurs.
     * 
     */
    public int available() throws IOException {
        ensureOpen();
        if (reachEOF) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Skips specified number of bytes of uncompressed data.
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped.
     * @exception IOException if an I/O error has occurred
     * @exception IllegalArgumentException if n < 0
     */
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
	ensureOpen();
	int max = (int)Math.min(n, Integer.MAX_VALUE);
	int total = 0;
	byte[] b = new byte[512];
	while (total < max) {
	    int len = max - total;
	    if (len > b.length) {
		len = b.length;
	    }
	    len = read(b, 0, len);
	    if (len == -1) {
                reachEOF = true;
		break;
	    }
	    total += len;
	}
	return total;
    }

    /**
     * Closes the input stream.
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException {
		if (usesDefaultInflater)
        	inf.end();
		in.close();
        closed = true;
    }

    /**
     * Fills input buffer with more data to decompress.
     * @exception IOException if an I/O error has occurred
     */
    protected void fill() throws IOException {
	ensureOpen();
	len = in.read(buf, 0, buf.length);
	if (len == -1) {
	    throw new EOFException("Unexpected end of ZLIB input stream");
	}
	inf.setInput(buf, 0, len);
    }
}
