/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.sql;
 
/**
 * The representation (mapping) in
 * the Java<sup><font size=-2>TM</font></sup> programming
 * language of an SQL 
 * <code>BLOB</code> value.  An SQL <code>BLOB</code> is a built-in type 
 * that stores a Binary Large Object as a column value in a row of 
 * a database table. The driver implements <code>Blob</code> using
 * an SQL <code>locator(BLOB)</code>, which means that a
 * <code>Blob</code> object contains a logical pointer to the
 * SQL <code>BLOB</code> data rather than the data itself.
 * A <code>Blob</code> object is valid for the duration of the
 * transaction in which is was created.
 * 
 * <P>Methods in the interfaces {@link ResultSet}, 
 * {@link CallableStatement}, and {@link PreparedStatement}, such as
 * <code>getBlob</code> and <code>setBlob</code> allow a programmer to 
 * access an SQL <code>BLOB</code> value.
 * The <code>Blob</code> interface provides methods for getting the
 * length of an SQL <code>BLOB</code> (Binary Large Object) value,
 * for materializing a <code>BLOB</code> value on the client, and for
 * determining the position of a pattern of bytes within a 
 * <code>BLOB</code> value. 
 *<P>
 * This class is new in the JDBC 2.0 API.
 * @since 1.2
 */

public interface Blob {

  /**
   * Returns the number of bytes in the <code>BLOB</code> value
   * designated by this <code>Blob</code> object.
   * @return length of the <code>BLOB</code> in bytes
   * @exception SQLException if there is an error accessing the
   * length of the <code>BLOB</code>
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
  long length() throws SQLException;

  /**
   * Returns as an array of bytes, part or all of the <code>BLOB</code>
   * value that this <code>Blob</code> object designates.  The byte
   * array contains up to <code>length</code> consecutive bytes
   * starting at position <code>pos</code>.
   * @param pos the ordinal position of the first byte in the 
   * <code>BLOB</code> value to be extracted; the first byte is at
   * position 1
   * @param length the number of consecutive bytes to be copied
   * @return a byte array containing up to <code>length</code> 
   * consecutive bytes from the <code>BLOB</code> value designated
   * by this <code>Blob</code> object, starting with the
   * byte at position <code>pos</code>
   * @exception SQLException if there is an error accessing the
   * <code>BLOB</code>
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
  byte[] getBytes(long pos, int length) throws SQLException; 

  /**
   * Retrieves the <code>BLOB</code> designated by this
   * <code>Blob</code> instance as a stream.
   * @return a stream containing the <code>BLOB</code> data
   * @exception SQLException if there is an error accessing the
   * <code>BLOB</code>
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
  java.io.InputStream getBinaryStream () throws SQLException;

  /** 
   * Determines the byte position at which the specified byte 
   * <code>pattern</code> begins within the <code>BLOB</code>
   * value that this <code>Blob</code> object represents.  The
   * search for <code>pattern</code> begins at position
   * <code>start</code>.  
   * @param pattern the byte array for which to search
   * @param start the position at which to begin searching; the
   *        first position is 1
   * @return the position at which the pattern appears, else -1
   * @exception SQLException if there is an error accessing the 
   * <code>BLOB</code>
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
  long position(byte pattern[], long start) throws SQLException;

  /** 
   * Determines the byte position in the <code>BLOB</code> value
   * designated by this <code>Blob</code> object at which 
   * <code>pattern</code> begins.  The search begins at position
   * <code>start</code>.
   * @param pattern the <code>Blob</code> object designating
   * the <code>BLOB</code> value for which to search
   * @param start the position in the <code>BLOB</code> value
   *        at which to begin searching; the first position is 1
   * @return the position at which the pattern begins, else -1
   * @exception SQLException if there is an error accessing the
   * <code>BLOB</code>
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
  long position(Blob pattern, long start) throws SQLException;
}


