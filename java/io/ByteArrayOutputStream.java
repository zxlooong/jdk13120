/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.io;


/**
 * This class implements an output stream in which the data is 
 * written into a byte array. The buffer automatically grows as data 
 * is written to it. 
 * The data can be retrieved using <code>toByteArray()</code> and
 * <code>toString()</code>.
 *
 * @author  Arthur van Hoff
 * @version 1.44, 02/06/02
 * @since   JDK1.0
 */

public class ByteArrayOutputStream extends OutputStream {

    /** 
     * The buffer where data is stored. 
     */
    protected byte buf[];

    /**
     * The number of valid bytes in the buffer. 
     */
    protected int count;

    /**
     * Flag indicating whether the stream has been closed.
     */
    private boolean isClosed = false;

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() {
        /* This method does nothing for now.  Once we add throws clauses
	 * to the I/O methods in this class, it will throw an IOException
	 * if the stream has been closed.
	 */
    }

    /**
     * Creates a new byte array output stream. The buffer capacity is 
     * initially 32 bytes, though its size increases if necessary. 
     */
    public ByteArrayOutputStream() {
	this(32);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of 
     * the specified size, in bytes. 
     *
     * @param   size   the initial size.
     * @exception  IllegalArgumentException if size is negative.
     */
    public ByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: "
                                               + size);
        }
	buf = new byte[size];
    }

    /**
     * Writes the specified byte to this byte array output stream. 
     *
     * @param   b   the byte to be written.
     */
    public synchronized void write(int b) {
	ensureOpen();
	int newcount = count + 1;
	if (newcount > buf.length) {
	    byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
	    System.arraycopy(buf, 0, newbuf, 0, count);
	    buf = newbuf;
	}
	buf[count] = (byte)b;
	count = newcount;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    public synchronized void write(byte b[], int off, int len) {
	ensureOpen();
	if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return;
	}
        int newcount = count + len;
        if (newcount > buf.length) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }

    /**
     * Writes the complete contents of this byte array output stream to 
     * the specified output stream argument, as if by calling the output 
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param      out   the output stream to which to write the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void writeTo(OutputStream out) throws IOException {
	out.write(buf, 0, count);
    }

    /**
     * Resets the <code>count</code> field of this byte array output 
     * stream to zero, so that all currently accumulated output in the 
     * ouput stream is discarded. The output stream can be used again, 
     * reusing the already allocated buffer space. 
     *
     * @see     java.io.ByteArrayInputStream#count
     */
    public synchronized void reset() {
	ensureOpen();
	count = 0;
    }

    /**
     * Creates a newly allocated byte array. Its size is the current 
     * size of this output stream and the valid contents of the buffer 
     * have been copied into it. 
     *
     * @return  the current contents of this output stream, as a byte array.
     * @see     java.io.ByteArrayOutputStream#size()
     */
    public synchronized byte toByteArray()[] {
	byte newbuf[] = new byte[count];
	System.arraycopy(buf, 0, newbuf, 0, count);
	return newbuf;
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return  the value of the <code>count</code> field, which is the number
     *          of valid bytes in this output stream.
     * @see     java.io.ByteArrayOutputStream#count
     */
    public int size() {
	return count;
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into
     * characters according to the platform's default character encoding.
     *
     * @return String translated from the buffer's contents.
     * @since   JDK1.1
     */
    public String toString() {
	return new String(buf, 0, count);
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into
     * characters according to the specified character encoding.
     *
     * @param   enc  a character-encoding name.
     * @return String translated from the buffer's contents.
     * @throws UnsupportedEncodingException
     *         If the named encoding is not supported.
     * @since   JDK1.1
     */
    public String toString(String enc) throws UnsupportedEncodingException {
	return new String(buf, 0, count, enc);
    }

    /**
     * Creates a newly allocated string. Its size is the current size of 
     * the output stream and the valid contents of the buffer have been 
     * copied into it. Each character <i>c</i> in the resulting string is 
     * constructed from the corresponding element <i>b</i> in the byte 
     * array such that:
     * <blockquote><pre>
     *     c == (char)(((hibyte &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </pre></blockquote>
     *
     * @deprecated This method does not properly convert bytes into characters.
     * As of JDK&nbsp;1.1, the preferred way to do this is via the
     * <code>toString(String enc)</code> method, which takes an encoding-name
     * argument, or the <code>toString()</code> method, which uses the
     * platform's default character encoding.
     *
     * @param      hibyte    the high byte of each resulting Unicode character.
     * @return     the current contents of the output stream, as a string.
     * @see        java.io.ByteArrayOutputStream#size()
     * @see        java.io.ByteArrayOutputStream#toString(String)
     * @see        java.io.ByteArrayOutputStream#toString()
     */
    public String toString(int hibyte) {
	return new String(buf, hibyte, 0, count);
    }

    /**
     * Closes this output stream and releases any system resources 
     * associated with this stream. A closed stream cannot perform 
     * output operations and cannot be reopened.
     * <p>
     *
     */
    public synchronized void close() throws IOException {
	isClosed = true;
    }

}
