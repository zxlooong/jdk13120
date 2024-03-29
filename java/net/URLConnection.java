/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Date;
import java.util.StringTokenizer;
import java.security.Permission;
import java.security.AccessController;

/**
 * The abstract class <code>URLConnection</code> is the superclass 
 * of all classes that represent a communications link between the 
 * application and a URL. Instances of this class can be used both to 
 * read from and to write to the resource referenced by the URL. In 
 * general, creating a connection to a URL is a multistep process: 
 * <p>
 * <center><table border=2>
 * <tr><th><code>openConnection()</code></th>
 *     <th><code>connect()</code></th></tr>
 * <tr><td>Manipulate parameters that affect the connection to the remote 
 *         resource.</td>
 *     <td>Interact with the resource; query header fields and
 *         contents.</td></tr>
 * </table>
 * ----------------------------&gt;
 * <br>time</center>
 *
 * <ol>
 * <li>The connection object is created by invoking the
 *     <code>openConnection</code> method on a URL.
 * <li>The setup parameters and general request properties are manipulated.
 * <li>The actual connection to the remote object is made, using the
 *    <code>connect</code> method.
 * <li>The remote object becomes available. The header fields and the contents
 *     of the remote object can be accessed.
 * </ol>
 * <p>
 * The setup parameters are modified using the following methods: 
 * <ul>
 *   <li><code>setAllowUserInteraction</code>
 *   <li><code>setDoInput</code>
 *   <li><code>setDoOutput</code>
 *   <li><code>setIfModifiedSince</code>
 *   <li><code>setUseCaches</code>
 * </ul>
 * <p>
 * and the general request properties are modified using the method:
 * <ul>
 *   <li><code>setRequestProperty</code>
 * </ul>
 * <p>
 * Default values for the <code>AllowUserInteraction</code> and 
 * <code>UseCaches</code> parameters can be set using the methods 
 * <code>setDefaultAllowUserInteraction</code> and 
 * <code>setDefaultUseCaches</code>.
 * <p>
 * Each of the above <code>set</code> methods has a corresponding 
 * <code>get</code> method to retrieve the value of the parameter or 
 * general request property. The specific parameters and general 
 * request properties that are applicable are protocol specific. 
 * <p>
 * The following methods are used to access the header fields and 
 * the contents after the connection is made to the remote object:
 * <ul>
 *   <li><code>getContent</code>
 *   <li><code>getHeaderField</code>
 *   <li><code>getInputStream</code>
 *   <li><code>getOutputStream</code>
 * </ul>
 * <p>
 * Certain header fields are accessed frequently. The methods:
 * <ul>
 *   <li><code>getContentEncoding</code>
 *   <li><code>getContentLength</code>
 *   <li><code>getContentType</code>
 *   <li><code>getDate</code>
 *   <li><code>getExpiration</code>
 *   <li><code>getLastModifed</code>
 * </ul>
 * <p>
 * provide convenient access to these fields. The 
 * <code>getContentType</code> method is used by the 
 * <code>getContent</code> method to determine the type of the remote 
 * object; subclasses may find it convenient to override the 
 * <code>getContentType</code> method. 
 * <p>
 * In the common case, all of the pre-connection parameters and 
 * general request properties can be ignored: the pre-connection 
 * parameters and request properties default to sensible values. For 
 * most clients of this interface, there are only two interesting 
 * methods: <code>getInputStream</code> and <code>getObject</code>, 
 * which are mirrored in the <code>URL</code> class by convenience methods.
 * <p>
 * More information on the request properties and header fields of 
 * an <code>http</code> connection can be found at:
 * <blockquote><pre>
 * <a href="http://www.ietf.org/rfc/rfc2068.txt">http://www.ietf.org/rfc/rfc2068.txt</a>
 * </pre></blockquote>
 *
 * Note about <code>fileNameMap</code>: In versions prior to JDK 1.1.6, 
 * field <code>fileNameMap</code> of <code>URLConnection</code> was public.
 * In JDK 1.1.6 and later, <code>fileNameMap</code> is private; accessor 
 * and mutator methods {@link #getFileNameMap() getFileNameMap} and 
 * {@link #setFileNameMap(java.net.FileNameMap) setFileNameMap} are added
 * to access it.  This change is also described on the <a href=
 * "http://java.sun.com/products/jdk/1.2/compatibility.html#incompatibilities1.2">
 * Compatibility</a> page.
 *
 * @author  James Gosling
 * @version 1.75, 02/06/02
 * @see     java.net.URL#openConnection()
 * @see     java.net.URLConnection#connect()
 * @see     java.net.URLConnection#getContent()
 * @see     java.net.URLConnection#getContentEncoding()
 * @see     java.net.URLConnection#getContentLength()
 * @see     java.net.URLConnection#getContentType()
 * @see     java.net.URLConnection#getDate()
 * @see     java.net.URLConnection#getExpiration()
 * @see     java.net.URLConnection#getHeaderField(int)
 * @see     java.net.URLConnection#getHeaderField(java.lang.String)
 * @see     java.net.URLConnection#getInputStream()
 * @see     java.net.URLConnection#getLastModified()
 * @see     java.net.URLConnection#getOutputStream()
 * @see     java.net.URLConnection#setAllowUserInteraction(boolean)
 * @see     java.net.URLConnection#setDefaultUseCaches(boolean)
 * @see     java.net.URLConnection#setDoInput(boolean)
 * @see     java.net.URLConnection#setDoOutput(boolean)
 * @see     java.net.URLConnection#setIfModifiedSince(long)
 * @see     java.net.URLConnection#setRequestProperty(java.lang.String, java.lang.String)
 * @see     java.net.URLConnection#setUseCaches(boolean)
 * @since   JDK1.0
 */
public abstract class URLConnection {

   /**
     * The URL represents the remote object on the World Wide Web to 
     * which this connection is opened. 
     * <p>
     * The value of this field can be accessed by the 
     * <code>getURL</code> method. 
     * <p>
     * The default value of this variable is the value of the URL 
     * argument in the <code>URLConnection</code> constructor. 
     *
     * @see     java.net.URLConnection#getURL()
     * @see     java.net.URLConnection#url
     */
    protected URL url;

   /**
     * This variable is set by the <code>setDoInput</code> method. Its 
     * value is returned by the <code>getDoInput</code> method. 
     * <p>
     * A URL connection can be used for input and/or output. Setting the 
     * <code>doInput</code> flag to <code>true</code> indicates that 
     * the application intends to read data from the URL connection. 
     * <p>
     * The default value of this field is <code>true</code>. 
     *
     * @see     java.net.URLConnection#getDoInput()
     * @see     java.net.URLConnection#setDoInput(boolean)
     */
    protected boolean doInput = true;

   /**
     * This variable is set by the <code>setDoOutput</code> method. Its 
     * value is returned by the <code>getDoInput</code> method. 
     * <p>
     * A URL connection can be used for input and/or output. Setting the 
     * <code>doOutput</code> flag to <code>true</code> indicates 
     * that the application intends to write data to the URL connection. 
     * <p>
     * The default value of this field is <code>false</code>. 
     *
     * @see     java.net.URLConnection#getDoOutput()
     * @see     java.net.URLConnection#setDoOutput(boolean)
     */
    protected boolean doOutput = false;

    private static boolean defaultAllowUserInteraction = false;

   /**
     * If <code>true</code>, this <code>URL</code> is being examined in 
     * a context in which it makes sense to allow user interactions such 
     * as popping up an authentication dialog. If <code>false</code>, 
     * then no user interaction is allowed. 
     * <p>
     * The value of this field can be set by the 
     * <code>setAllowUserInteraction</code> method.
     * Its value is returned by the 
     * <code>getAllowUserInteraction</code> method.
     * Its default value is the value of the argument in the last invocation 
     * of the <code>setDefaultAllowUserInteraction</code> method. 
     *
     * @see     java.net.URLConnection#getAllowUserInteraction()
     * @see     java.net.URLConnection#setAllowUserInteraction(boolean)
     * @see     java.net.URLConnection#setDefaultAllowUserInteraction(boolean)
     */
    protected boolean allowUserInteraction = defaultAllowUserInteraction;

    private static boolean defaultUseCaches = true;

   /**
     * If <code>true</code>, the protocol is allowed to use caching 
     * whenever it can. If <code>false</code>, the protocol must always 
     * try to get a fresh copy of the object. 
     * <p>
     * This field is set by the <code>setUseCaches</code> method. Its 
     * value is returned by the <code>getUseCaches</code> method.
     * <p>
     * Its default value is the value given in the last invocation of the 
     * <code>setDefaultUseCaches</code> method. 
     *
     * @see     java.net.URLConnection#setUseCaches(boolean)
     * @see     java.net.URLConnection#getUseCaches()
     * @see     java.net.URLConnection#setDefaultUseCaches(boolean)
     */
    protected boolean useCaches = defaultUseCaches;

   /**
     * Some protocols support skipping the fetching of the object unless 
     * the object has been modified more recently than a certain time. 
     * <p>
     * A nonzero value gives a time as the number of milliseconds since 
     * January 1, 1970, GMT. The object is fetched only if it has been 
     * modified more recently than that time. 
     * <p>
     * This variable is set by the <code>setIfModifiedSince</code> 
     * method. Its value is returned by the 
     * <code>getIfModifiedSince</code> method.
     * <p>
     * The default value of this field is <code>0</code>, indicating 
     * that the fetching must always occur. 
     *
     * @see     java.net.URLConnection#getIfModifiedSince()
     * @see     java.net.URLConnection#setIfModifiedSince(long)
     */
    protected long ifModifiedSince = 0;

   /**
     * If <code>false</code>, this connection object has not created a 
     * communications link to the specified URL. If <code>true</code>, 
     * the communications link has been established. 
     */
    protected boolean connected = false;

   /**
    * @since   JDK1.1
     */
    private static FileNameMap fileNameMap;

    /**
     * @since 1.2.2
     */
    private static boolean fileNameMapLoaded = false;

    /**
     * Loads filename map (a mimetable) from a data file. It will
     * first try to load the user-specific table, defined
     * by &quot;content.types.user.table&quot; property. If that fails,
     * it tries to load the default built-in table at 
     * lib/content-types.properties under java home.
     *
     * @return the FileNameMap
     * @since 1.2
     * @see #setFileNameMap(java.net.FileNameMap)
     */
    public static synchronized FileNameMap getFileNameMap() {
	if ((fileNameMap == null) && !fileNameMapLoaded) {
	    fileNameMap = sun.net.www.MimeTable.loadTable();
	    fileNameMapLoaded = true;
	}

	return new FileNameMap() {
	    private FileNameMap map = fileNameMap;
	    public String getContentTypeFor(String fileName) {
		return map.getContentTypeFor(fileName);
	    }
	};
    }

    /**
     * Sets the FileNameMap.
     * <p>
     * If there is a security manager, this method first calls
     * the security manager's <code>checkSetFactory</code> method 
     * to ensure the operation is allowed. 
     * This could result in a SecurityException.
     *
     * @param map the FileNameMap to be set
     * @exception  SecurityException  if a security manager exists and its  
     *             <code>checkSetFactory</code> method doesn't allow the operation.
     * @see        SecurityManager#checkSetFactory
     * @see #getFileNameMap()
     * @since 1.2
     */
    public static void setFileNameMap(FileNameMap map) {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) sm.checkSetFactory();
	fileNameMap = map;
    }

    /**
     * Opens a communications link to the resource referenced by this 
     * URL, if such a connection has not already been established. 
     * <p>
     * If the <code>connect</code> method is called when the connection 
     * has already been opened (indicated by the <code>connected</code> 
     * field having the value <code>true</code>), the call is ignored. 
     * <p>
     * URLConnection objects go through two phases: first they are
     * created, then they are connected.  After being created, and
     * before being connected, various options can be specified
     * (e.g., doInput and UseCaches).  After connecting, it is an
     * error to try to set them.  Operations that depend on being
     * connected, like getContentLength, will implicitly perform the
     * connection, if necessary.
     *
     * @exception  IOException  if an I/O error occurs while opening the
     *               connection.
     * @see java.net.URLConnection#connected */
    abstract public void connect() throws IOException;

    /**
     * Constructs a URL connection to the specified URL. A connection to 
     * the object referenced by the URL is not created. 
     *
     * @param   url   the specified URL.
     */
    protected URLConnection(URL url) {
	this.url = url;
    }

    /**
     * Returns the value of this <code>URLConnection</code>'s <code>URL</code>
     * field.
     *
     * @return  the value of this <code>URLConnection</code>'s <code>URL</code>
     *          field.
     * @see     java.net.URLConnection#url
     */
    public URL getURL() {
	return url;
    }

    /**
     * Returns the value of the <code>content-length</code> header field.
     *
     * @return  the content length of the resource that this connection's URL
     *          references, or <code>-1</code> if the content length is
     *          not known.
     */
    public int getContentLength() {
	return getHeaderFieldInt("content-length", -1);
    }

    /**
     * Returns the value of the <code>content-type</code> header field.
     *
     * @return  the content type of the resource that the URL references,
     *          or <code>null</code> if not known.
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public String getContentType() {
	return getHeaderField("content-type");
    }

    /**
     * Returns the value of the <code>content-encoding</code> header field.
     *
     * @return  the content encoding of the resource that the URL references,
     *          or <code>null</code> if not known.
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public String getContentEncoding() {
	return getHeaderField("content-encoding");
    }

    /**
     * Returns the value of the <code>expires</code> header field. 
     *
     * @return  the expiration date of the resource that this URL references,
     *          or 0 if not known. The value is the number of milliseconds since
     *          January 1, 1970 GMT.
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public long getExpiration() {
	return getHeaderFieldDate("expires", 0);
    }

    /**
     * Returns the value of the <code>date</code> header field. 
     *
     * @return  the sending date of the resource that the URL references,
     *          or <code>0</code> if not known. The value returned is the
     *          number of milliseconds since January 1, 1970 GMT.
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public long getDate() {
	return getHeaderFieldDate("date", 0);
    }

    /**
     * Returns the value of the <code>last-modified</code> header field. 
     * The result is the number of milliseconds since January 1, 1970 GMT.
     *
     * @return  the date the resource referenced by this
     *          <code>URLConnection</code> was last modified, or 0 if not known.
     * @see     java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public long getLastModified() {
	return getHeaderFieldDate("last-modified", 0);
    }

    /**
     * Returns the name of the specified header field.
     *
     * @param   name   the name of a header field.
     * @return  the value of the named header field, or <code>null</code>
     *          if there is no such field in the header.
     */
    public String getHeaderField(String name) {
	return null;
    }

    /**
     * Returns the value of the named field parsed as a number.
     * <p>
     * This form of <code>getHeaderField</code> exists because some 
     * connection types (e.g., <code>http-ng</code>) have pre-parsed 
     * headers. Classes for that connection type can override this method 
     * and short-circuit the parsing. 
     *
     * @param   name      the name of the header field.
     * @param   Default   the default value.
     * @return  the value of the named field, parsed as an integer. The
     *          <code>Default</code> value is returned if the field is
     *          missing or malformed.
     */
    public int getHeaderFieldInt(String name, int Default) {
	try {
	    return Integer.parseInt(getHeaderField(name));
	} catch(Throwable t) {}
	return Default;
    }

    /**
     * Returns the value of the named field parsed as date.
     * The result is the number of milliseconds since January 1, 1970 GMT
     * represented by the named field. 
     * <p>
     * This form of <code>getHeaderField</code> exists because some 
     * connection types (e.g., <code>http-ng</code>) have pre-parsed 
     * headers. Classes for that connection type can override this method 
     * and short-circuit the parsing. 
     *
     * @param   name     the name of the header field.
     * @param   Default   a default value.
     * @return  the value of the field, parsed as a date. The value of the
     *          <code>Default</code> argument is returned if the field is
     *          missing or malformed.
     */
    public long getHeaderFieldDate(String name, long Default) {
	try {
	    return Date.parse(getHeaderField(name));
	} catch(Throwable t) {}
	return Default;
    }

    /**
     * Returns the key for the <code>n</code><sup>th</sup> header field.
     *
     * @param   n   an index.
     * @return  the key for the <code>n</code><sup>th</sup> header field,
     *          or <code>null</code> if there are fewer than <code>n</code>
     *          fields.
     */
    public String getHeaderFieldKey(int n) {
	return null;
    }

    /**
     * Returns the value for the <code>n</code><sup>th</sup> header field. 
     * It returns <code>null</code> if there are fewer than 
     * <code>n</code> fields. 
     * <p>
     * This method can be used in conjunction with the 
     * <code>getHeaderFieldKey</code> method to iterate through all 
     * the headers in the message. 
     *
     * @param   n   an index.
     * @return  the value of the <code>n</code><sup>th</sup> header field.
     * @see     java.net.URLConnection#getHeaderFieldKey(int)
     */
    public String getHeaderField(int n) {
	return null;
    }

    /**
     * Retrieves the contents of this URL connection. 
     * <p>
     * This method first determines the content type of the object by 
     * calling the <code>getContentType</code> method. If this is 
     * the first time that the application has seen that specific content 
     * type, a content handler for that content type is created: 
     * <ol>
     * <li>If the application has set up a content handler factory instance
     *     using the <code>setContentHandlerFactory</code> method, the
     *     <code>createContentHandler</code> method of that instance is called
     *     with the content type as an argument; the result is a content
     *     handler for that content type.
     * <li>If no content handler factory has yet been set up, or if the
     *     factory's <code>createContentHandler</code> method returns
     *     <code>null</code>, then the application loads the class named:
     *     <blockquote><pre>
     *         sun.net.www.content.&lt;<i>contentType</i>&gt;
     *     </pre></blockquote>
     *     where &lt;<i>contentType</i>&gt; is formed by taking the
     *     content-type string, replacing all slash characters with a
     *     <code>period</code> ('.'), and all other non-alphanumeric characters
     *     with the underscore character '<code>_</code>'. The alphanumeric
     *     characters are specifically the 26 uppercase ASCII letters
     *     '<code>A</code>' through '<code>Z</code>', the 26 lowercase ASCII
     *     letters '<code>a</code>' through '<code>z</code>', and the 10 ASCII
     *     digits '<code>0</code>' through '<code>9</code>'. If the specified
     *     class does not exist, or is not a subclass of
     *     <code>ContentHandler</code>, then an
     *     <code>UnknownServiceException</code> is thrown.
     * </ol>
     *
     * @return     the object fetched. The <code>instanceOf</code> operation
     *               should be used to determine the specific kind of object
     *               returned.
     * @exception  IOException              if an I/O error occurs while
     *               getting the content.
     * @exception  UnknownServiceException  if the protocol does not support
     *               the content type.
     * @see        java.net.ContentHandlerFactory#createContentHandler(java.lang.String)
     * @see        java.net.URLConnection#getContentType()
     * @see        java.net.URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
     */
    public Object getContent() throws IOException {
        // Must call getInputStream before GetHeaderField gets called
        // so that FileNotFoundException has a chance to be thrown up
        // from here without being caught.
        getInputStream();
	return getContentHandler().getContent(this);
    }

    /**
     * Retrieves the contents of this URL connection. 
     *
     * @param classes the <code>Class</code> array 
     * indicating the requested types
     * @return     the object fetched that is the first match of the type
     *               specified in the classes array. null if none of 
     *               the requested types are supported.
     *               The <code>instanceOf</code> operation should be used to 
     *               determine the specific kind of object returned.
     * @exception  IOException              if an I/O error occurs while
     *               getting the content.
     * @exception  UnknownServiceException  if the protocol does not support
     *               the content type.
     * @see        java.net.URLConnection#getContent()
     * @see        java.net.ContentHandlerFactory#createContentHandler(java.lang.String)
     * @see        java.net.URLConnection#getContent(java.lang.Class[])
     * @see        java.net.URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
     */
    public Object getContent(Class[] classes) throws IOException {
        // Must call getInputStream before GetHeaderField gets called
        // so that FileNotFoundException has a chance to be thrown up
        // from here without being caught.
        getInputStream();
	return getContentHandler().getContent(this, classes);
    }

    /**  
     * Returns a permission object representing the permission
     * necessary to make the connection represented by this
     * object. This method returns null if no permission is
     * required to make the connection. By default, this method
     * returns <code>java.security.AllPermission</code>. Subclasses
     * should override this method and return the permission
     * that best represents the permission required to make a 
     * a connection to the URL. For example, a <code>URLConnection</code>
     * representing a <code>file:</code> URL would return a 
     * <code>java.io.FilePermission</code> object.
     *
     * <p>The permission returned may dependent upon the state of the
     * connection. For example, the permission before connecting may be
     * different from that after connecting. For example, an HTTP
     * sever, say foo.com, may redirect the connection to a different
     * host, say bar.com. Before connecting the permission returned by
     * the connection will represent the permission needed to connect
     * to foo.com, while the permission returned after connecting will
     * be to bar.com.
     * 
     * <p>Permissions are generally used for two purposes: to protect
     * caches of objects obtained through URLConnections, and to check
     * the right of a recipient to learn about a particular URL. In
     * the first case, the permission should be obtained
     * <em>after</em> the object has been obtained. For example, in an
     * HTTP connection, this will represent the permission to connect
     * to the host from which the data was ultimately fetched. In the
     * second case, the permission should be obtained and tested
     * <em>before</em> connecting.
     *
     * @return the permission object representing the permission
     * necessary to make the connection represented by this
     * URLConnection. 
     *
     * @exception IOException if the computation of the permission
     * requires network or file I/O and an exception occurs while
     * computing it.  
     */
    public Permission getPermission() throws IOException {
	return new java.security.AllPermission();
    }

    /**
     * Returns an input stream that reads from this open connection.
     *
     * @return     an input stream that reads from this open connection.
     * @exception  IOException              if an I/O error occurs while
     *               creating the input stream.
     * @exception  UnknownServiceException  if the protocol does not support
     *               input.
     */
    public InputStream getInputStream() throws IOException {
	throw new UnknownServiceException("protocol doesn't support input");
    }

    /**
     * Returns an output stream that writes to this connection.
     *
     * @return     an output stream that writes to this connection.
     * @exception  IOException              if an I/O error occurs while
     *               creating the output stream.
     * @exception  UnknownServiceException  if the protocol does not support
     *               output.
     */
    public OutputStream getOutputStream() throws IOException {
	throw new UnknownServiceException("protocol doesn't support output");
    }

    /**
     * Returns a <code>String</code> representation of this URL connection.
     *
     * @return  a string representation of this <code>URLConnection</code>.
     */
    public String toString() {
	return this.getClass().getName() + ":" + url;
    }

    /**
     * Sets the value of the <code>doInput</code> field for this 
     * <code>URLConnection</code> to the specified value. 
     * <p>
     * A URL connection can be used for input and/or output.  Set the DoInput
     * flag to true if you intend to use the URL connection for input,
     * false if not.  The default is true unless DoOutput is explicitly
     * set to true, in which case DoInput defaults to false.
     *
     * @param   doinput   the new value.
     * @see     java.net.URLConnection#doInput
     * @see #getDoInput()
     */
    public void setDoInput(boolean doinput) {
	if (connected)
	    throw new IllegalAccessError("Already connected");
	doInput = doinput;
    }

    /**
     * Returns the value of this <code>URLConnection</code>'s
     * <code>doInput</code> flag.
     *
     * @return  the value of this <code>URLConnection</code>'s
     *          <code>doInput</code> flag.
     * @see     #setDoInput(boolean)
     */
    public boolean getDoInput() {
	return doInput;
    }

    /**
     * Sets the value of the <code>doOutput</code> field for this 
     * <code>URLConnection</code> to the specified value. 
     * <p>
     * A URL connection can be used for input and/or output.  Set the DoOutput
     * flag to true if you intend to use the URL connection for output,
     * false if not.  The default is false.
     *
     * @param   dooutput   the new value.
     * @see #getDoOutput()
     */
    public void setDoOutput(boolean dooutput) {
	if (connected)
	    throw new IllegalAccessError("Already connected");
	doOutput = dooutput;
    }

    /**
     * Returns the value of this <code>URLConnection</code>'s
     * <code>doOutput</code> flag.
     *
     * @return  the value of this <code>URLConnection</code>'s
     *          <code>doOutput</code> flag.
     * @see     #setDoOutput(boolean)
     */
    public boolean getDoOutput() {
	return doOutput;
    }

    /**
     * Set the value of the <code>allowUserInteraction</code> field of 
     * this <code>URLConnection</code>. 
     *
     * @param   allowuserinteraction   the new value.
     * @see     #getAllowUserInteraction()
     */
    public void setAllowUserInteraction(boolean allowuserinteraction) {
	if (connected)
	    throw new IllegalAccessError("Already connected");
	allowUserInteraction = allowuserinteraction;
    }

    /**
     * Returns the value of the <code>allowUserInteraction</code> field for
     * this object.
     *
     * @return  the value of the <code>allowUserInteraction</code> field for
     *          this object.
     * @see     #setAllowUserInteraction(boolean)
     */
    public boolean getAllowUserInteraction() {
	return allowUserInteraction;
    }

    /**
     * Sets the default value of the 
     * <code>allowUserInteraction</code> field for all future 
     * <code>URLConnection</code> objects to the specified value. 
     *
     * @param   defaultallowuserinteraction   the new value.
     * @see     #getDefaultAllowUserInteraction()
     */
    public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
	defaultAllowUserInteraction = defaultallowuserinteraction;
    }

    /**
     * Returns the default value of the <code>allowUserInteraction</code>
     * field.
     * <p>
     * Ths default is "sticky", being a part of the static state of all
     * URLConnections.  This flag applies to the next, and all following
     * URLConnections that are created.
     *
     * @return  the default value of the <code>allowUserInteraction</code>
     *          field.
     * @see     #setDefaultAllowUserInteraction(boolean)
     */
    public static boolean getDefaultAllowUserInteraction() {
	return defaultAllowUserInteraction;
    }

    /**
     * Sets the value of the <code>useCaches</code> field of this 
     * <code>URLConnection</code> to the specified value. 
     * <p>
     * Some protocols do caching of documents.  Occasionally, it is important
     * to be able to "tunnel through" and ignore the caches (e.g., the
     * "reload" button in a browser).  If the UseCaches flag on a connection
     * is true, the connection is allowed to use whatever caches it can.
     *  If false, caches are to be ignored.
     *  The default value comes from DefaultUseCaches, which defaults to
     * true.
     *
     * @param usecaches a <code>boolean</code> indicating whether 
     * or not to allow caching
     * @see #getUseCaches()
     */
    public void setUseCaches(boolean usecaches) {
	if (connected)
	    throw new IllegalAccessError("Already connected");
	useCaches = usecaches;
    }

    /**
     * Returns the value of this <code>URLConnection</code>'s
     * <code>useCaches</code> field.
     *
     * @return  the value of this <code>URLConnection</code>'s
     *          <code>useCaches</code> field.
     * @see #setUseCaches(boolean)
     */
    public boolean getUseCaches() {
	return useCaches;
    }

    /**
     * Sets the value of the <code>ifModifiedSince</code> field of 
     * this <code>URLConnection</code> to the specified value.
     *
     * @param   ifmodifiedsince   the new value.
     * @see     #getIfModifiedSince()
     */
    public void setIfModifiedSince(long ifmodifiedsince) {
	if (connected)
	    throw new IllegalAccessError("Already connected");
	ifModifiedSince = ifmodifiedsince;
    }

    /**
     * Returns the value of this object's <code>ifModifiedSince</code> field.
     *
     * @return  the value of this object's <code>ifModifiedSince</code> field.
     * @see #setIfModifiedSince(long)
     */
    public long getIfModifiedSince() {
	return ifModifiedSince;
    }

   /**
     * Returns the default value of a <code>URLConnection</code>'s
     * <code>useCaches</code> flag.
     * <p>
     * Ths default is "sticky", being a part of the static state of all
     * URLConnections.  This flag applies to the next, and all following
     * URLConnections that are created.
     *
     * @return  the default value of a <code>URLConnection</code>'s
     *          <code>useCaches</code> flag.
     * @see     #setDefaultUseCaches(boolean)
     */
    public boolean getDefaultUseCaches() {
	return defaultUseCaches;
    }

   /**
     * Sets the default value of the <code>useCaches</code> field to the 
     * specified value. 
     *
     * @param   defaultusecaches   the new value.
     * @see     #getDefaultUseCaches()
     */
    public void setDefaultUseCaches(boolean defaultusecaches) {
	defaultUseCaches = defaultusecaches;
    }

    /**
     * Sets the general request property. If a property with the key already
     * exists, overwrite its value with the new value.
     *
     * <p> NOTE: HTTP requires all request properties which can
     * legally have multiple instances with the same key
     * to use a comma-seperated list syntax which enables multiple
     * properties to be appended into a single property.
     *
     * @param   key     the keyword by which the request is known
     *                  (e.g., "<code>accept</code>").
     * @param   value   the value associated with it.
     * @see #getRequestProperty(java.lang.String)
     */
    public void setRequestProperty(String key, String value) {
	if (connected)
	    throw new IllegalAccessError("Already connected");
    }

    /**
     * Returns the value of the named general request property for this
     * connection.
     *
     * @param key the keyword by which the request is known (e.g., "accept").
     * @return  the value of the named general request property for this
     *           connection.
     * @see #setRequestProperty(java.lang.String, java.lang.String)
     */
    public String getRequestProperty(String key) {
	if (connected)
	    throw new IllegalAccessError("Already connected");
	return null;
    }

    /**
     * Sets the default value of a general request property. When a 
     * <code>URLConnection</code> is created, it is initialized with 
     * these properties. 
     *
     * @param   key     the keyword by which the request is known
     *                  (e.g., "<code>accept</code>").
     * @param   value   the value associated with the key.
     *
     * @see java.net.URLConnection#setRequestProperty(java.lang.String,java.lang.String) 
     *
     * @deprecated The instance specific setRequestProperty method
     * should be used after an appropriate instance of URLConnection
     * is obtained.
     *
     * @see #getDefaultRequestProperty(java.lang.String)
     */
    public static void setDefaultRequestProperty(String key, String value) {
    }

    /**
     * Returns the value of the default request property. Default request 
     * properties are set for every connection. 
     *
     * @param key the keyword by which the request is known (e.g., "accept").
     * @return  the value of the default request property 
     * for the specified key.
     *
     * @see java.net.URLConnection#getRequestProperty(java.lang.String)
     *
     * @deprecated The instance specific getRequestProperty method
     * should be used after an appropriate instance of URLConnection
     * is obtained.
     *
     * @see #setDefaultRequestProperty(java.lang.String, java.lang.String)
     */
    public static String getDefaultRequestProperty(String key) {
	return null;
    }

    /**
     * The ContentHandler factory.
     */
    static ContentHandlerFactory factory;

    /**
     * Sets the <code>ContentHandlerFactory</code> of an 
     * application. It can be called at most once by an application. 
     * <p>
     * The <code>ContentHandlerFactory</code> instance is used to 
     * construct a content handler from a content type 
     * <p>
     * If there is a security manager, this method first calls
     * the security manager's <code>checkSetFactory</code> method 
     * to ensure the operation is allowed. 
     * This could result in a SecurityException.
     *
     * @param      fac   the desired factory.
     * @exception  Error  if the factory has already been defined.
     * @exception  SecurityException  if a security manager exists and its  
     *             <code>checkSetFactory</code> method doesn't allow the operation.
     * @see        java.net.ContentHandlerFactory
     * @see        java.net.URLConnection#getContent()
     * @see        SecurityManager#checkSetFactory
     */
    public static synchronized void setContentHandlerFactory(ContentHandlerFactory fac) {
	if (factory != null) {
	    throw new Error("factory already defined");
	}
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    security.checkSetFactory();
	}
	factory = fac;
    }

    private static Hashtable handlers = new Hashtable();
    private static final ContentHandler UnknownContentHandlerP = new UnknownContentHandler();

    /**
     * Gets the Content Handler appropriate for this connection.
     * @param connection the connection to use.
     */
    synchronized ContentHandler getContentHandler()
    throws UnknownServiceException
    {
	String contentType = stripOffParameters(getContentType());
	ContentHandler handler = null;
	if (contentType == null)
	    throw new UnknownServiceException("no content-type");
	try {
	    handler = (ContentHandler) handlers.get(contentType);
	    if (handler != null)
		return handler;
	} catch(Exception e) {
	}

	if (factory != null)
	    handler = factory.createContentHandler(contentType);
	if (handler == null) {
	    try {
		handler = lookupContentHandlerClassFor(contentType);
	    } catch(Exception e) {
		e.printStackTrace();
		handler = UnknownContentHandlerP;
	    }
	    handlers.put(contentType, handler);
	}
	return handler;
    }

    /*
     * Media types are in the format: type/subtype*(; parameter).
     * For looking up the content handler, we should ignore those
     * parameters.
     */
    private String stripOffParameters(String contentType)
    {
	int index = contentType.indexOf(';');

	if (index > 0)
	    return contentType.substring(0, index);
	else
	    return contentType;
    }

    private static final String contentClassPrefix = "sun.net.www.content";
    private static final String contentPathProp = "java.content.handler.pkgs";

    /**
     * Looks for a content handler in a user-defineable set of places.
     * By default it looks in sun.net.www.content, but users can define a 
     * vertical-bar delimited set of class prefixes to search through in 
     * addition by defining the java.content.handler.pkgs property.
     * The class name must be of the form:
     * <pre>
     *     {package-prefix}.{major}.{minor}
     * e.g.
     *     YoyoDyne.experimental.text.plain
     * </pre>
     */
    private ContentHandler lookupContentHandlerClassFor(String contentType)
	throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	String contentHandlerClassName = typeToPackageName(contentType);

	String contentHandlerPkgPrefixes =getContentHandlerPkgPrefixes();

	StringTokenizer packagePrefixIter =
	    new StringTokenizer(contentHandlerPkgPrefixes, "|");
	
	while (packagePrefixIter.hasMoreTokens()) {
	    String packagePrefix = packagePrefixIter.nextToken().trim();

	    try {
		String clsName = packagePrefix + "." + contentHandlerClassName;
		Class cls = null;
		try {
		    cls = Class.forName(clsName);
		} catch (ClassNotFoundException e) {
		    ClassLoader cl = ClassLoader.getSystemClassLoader();
		    if (cl != null) {
			cls = cl.loadClass(clsName);
		    }
		}
		if (cls != null) {
		    ContentHandler handler = 
			(ContentHandler)cls.newInstance();
		    return handler;
		}
	    } catch(Exception e) {
	    }
	}
	
	return UnknownContentHandlerP;
    }

    /**
     * Utility function to map a MIME content type into an equivalent
     * pair of class name components.  For example: "text/html" would
     * be returned as "text.html"
     */
    private String typeToPackageName(String contentType) {
	// make sure we canonicalize the class name: all lower case
	contentType = contentType.toLowerCase();
	int len = contentType.length();
	char nm[] = new char[len];
	contentType.getChars(0, len, nm, 0);
	for (int i = 0; i < len; i++) {
	    char c = nm[i];
	    if (c == '/') {
		nm[i] = '.';
	    } else if (!('A' <= c && c <= 'Z' ||
		       'a' <= c && c <= 'z' ||
		       '0' <= c && c <= '9')) {
		nm[i] = '_';
	    }
	}
	return new String(nm);
    }


    /**
     * Returns a vertical bar separated list of package prefixes for potential
     * content handlers.  Tries to get the java.content.handler.pkgs property
     * to use as a set of package prefixes to search.  Whether or not
     * that property has been defined, the sun.net.www.content is always
     * the last one on the returned package list.
     */
    private String getContentHandlerPkgPrefixes() {
	String packagePrefixList = (String) AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction(contentPathProp, ""));

	if (packagePrefixList != "") {
	    packagePrefixList += "|";
	}
	
	return packagePrefixList + contentClassPrefix;
    }

    /**
     * Tries to determine the content type of an object, based 
     * on the specified "file" component of a URL.
     * This is a convenience method that can be used by 
     * subclasses that override the <code>getContentType</code> method. 
     *
     * @param   fname   a filename.
     * @return  a guess as to what the content type of the object is,
     *          based upon its file name.
     * @see     java.net.URLConnection#getContentType()
     */
    protected static String guessContentTypeFromName(String fname) {
	String contentType = null;
	
	contentType = getFileNameMap().getContentTypeFor(fname);
        
	return contentType;
    }

    /**
     * Tries to determine the type of an input stream based on the 
     * characters at the beginning of the input stream. This method can 
     * be used by subclasses that override the 
     * <code>getContentType</code> method. 
     * <p>
     * Ideally, this routine would not be needed. But many 
     * <code>http</code> servers return the incorrect content type; in 
     * addition, there are many nonstandard extensions. Direct inspection 
     * of the bytes to determine the content type is often more accurate 
     * than believing the content type claimed by the <code>http</code> server.
     *
     * @param      is   an input stream that supports marks.
     * @return     a guess at the content type, or <code>null</code> if none
     *             can be determined.
     * @exception  IOException  if an I/O error occurs while reading the
     *               input stream.
     * @see        java.io.InputStream#mark(int)
     * @see        java.io.InputStream#markSupported()
     * @see        java.net.URLConnection#getContentType()
     */
    static public String guessContentTypeFromStream(InputStream is) throws IOException
    {
	is.mark(10);
	int c1 = is.read();
	int c2 = is.read();
	int c3 = is.read();
	int c4 = is.read();
	int c5 = is.read();
	int c6 = is.read();
	int c7 = is.read();
	int c8 = is.read();
	is.reset();
	if (c1 == 0xCA && c2 == 0xFE && c3 == 0xBA && c4 == 0xBE)
	    return "application/java-vm";
	if (c1 == 0xAC && c2 == 0xED)
	    // next two bytes are version number, currently 0x00 0x05
	    return "application/x-java-serialized-object";
	if (c1 == 'G' && c2 == 'I' && c3 == 'F' && c4 == '8')
	    return "image/gif";
	if (c1 == '#' && c2 == 'd' && c3 == 'e' && c4 == 'f')
	    return "image/x-bitmap";
	if (c1 == '!' && c2 == ' ' && c3 == 'X' && c4 == 'P' && c5 == 'M' && c6 == '2')
	    return "image/x-pixmap";
	if (c1 == 137 && c2 == 80 && c3 == 78 && 
		c4 == 71 && c5 == 13 && c6 == 10 &&
		c7 == 26 && c8 == 10)
	  return "image/png";

	if (c1 == 0x2E && c2 == 0x73 && c3 == 0x6E && c4 == 0x64)
	    return "audio/basic";  // .au format, big endian
	if (c1 == 0x64 && c2 == 0x6E && c3 == 0x73 && c4 == 0x2E)
	    return "audio/basic";  // .au format, little endian
	if (c1 == '<') {
	    if (c2 == '!'
		|| ((c2 == 'h' && (c3 == 't' && c4 == 'm' && c5 == 'l' ||
				   c3 == 'e' && c4 == 'a' && c5 == 'd')
		     || c2 == 'b' && c3 == 'o' && c4 == 'd' && c5 == 'y'))
		|| ((c2 == 'H' && (c3 == 'T' && c4 == 'M' && c5 == 'L' ||
				   c3 == 'E' && c4 == 'A' && c5 == 'D')
		     || c2 == 'B' && c3 == 'O' && c4 == 'D' && c5 == 'Y')))
		return "text/html";
	    if (c2 == '?' && c3 == 'x' && c4 == 'm' && c5 == 'l' && c6 == ' ')
		return "application/xml";
	}

	// big and little endian UTF-16 encodings, with byte order mark
	if (c1 == 0xfe && c2 == 0xff) {
	    if (c3 == 0 && c4 == '<' && c5 == 0 && c6 == '?' &&
		c7 == 0 && c8 == 'x')
		return "application/xml";
	}

	if (c1 == 0xff && c2 == 0xfe) {
	    if (c3 == '<' && c4 == 0 && c5 == '?' && c6 == 0 &&
		c7 == 'x' && c8 == 0)
		return "application/xml";
	}

	if (c1 == 0xFF && c2 == 0xD8 && c3 == 0xFF && c4 == 0xE0)
	    return "image/jpeg";
	if (c1 == 0xFF && c2 == 0xD8 && c3 == 0xFF && c4 == 0xEE)
	    return "image/jpg";

	if (c1 == 'R' && c2 == 'I' && c3 == 'F' && c4 == 'F')
	    /* I don't know if this is official but evidence
	     * suggests that .wav files start with "RIFF" - brown
	     */
	    return "audio/x-wav";  
	if (c1 == 0xD0 && c2 == 0xCF && c3 == 0x11 && c4 == 0xE0 &&
	    c5 == 0xA1 && c6 == 0xB1 && c7 == 0x1A && c8 == 0xE1) {
	    /* Above is signature of Microsoft Structured Storage.
	     * Below this, could have tests for various SS entities.
	     * For now, just test for FlashPix.
	     */
	    if (checkfpx(is))
		return "image/vnd.fpx";
	}
	return null;
    }

    /**
     * Check for FlashPix image data in InputStream is.  Return true if
     * the stream has FlashPix data, false otherwise.  Before calling this
     * method, the stream should have already been checked to be sure it
     * contains Microsoft Structured Storage data.
     */
    static private boolean checkfpx(InputStream is) throws IOException {
        /* Test for FlashPix image data in Microsoft Structured Storage format.
         * In general, should do this with calls to an SS implementation.
         * Lacking that, need to dig via offsets to get to the FlashPix
         * ClassID.  Details:
         *
         * Offset to Fpx ClsID from beginning of stream should be:  
         *
         * FpxClsidOffset = rootEntryOffset + clsidOffset
         *
         * where: clsidOffset = 0x50.  
         *        rootEntryOffset = headerSize + sectorSize*sectDirStart 
         *                          + 128*rootEntryDirectory
         *
         *        where:  headerSize = 0x200 (always)
         *                sectorSize = 2 raised to power of uSectorShift,
         *                             which is found in the header at
         *                             offset 0x1E.
         *                sectDirStart = found in the header at offset 0x30.
         *                rootEntryDirectory = in general, should search for 
         *                                     directory labelled as root.
         *                                     We will assume value of 0 (i.e.,
         *                                     rootEntry is in first directory)
         */

	// Mark the stream so we can reset it. 0x100 is enough for the first
	// few reads, but the mark will have to be reset and set again once
	// the offset to the root directory entry is computed. That offset
	// can be very large and isn't know until the stream has been read from
	is.mark(0x100);
	// Get the byte ordering located at 0x1E. 0xFE is Intel,
	// 0xFF is other 
	long toSkip = (long)0x1C;
	long skipped = 0;
	while (skipped != toSkip) {
	    skipped += is.skip(toSkip - skipped);
	}
	long posn = skipped;
	int byteOrder = is.read();
	is.read();
	posn+=2;
	int uSectorShift;
	if(byteOrder == 0xFE) {
	    uSectorShift = is.read();
	    uSectorShift += is.read() << 8;
	}
	else {
	    uSectorShift = is.read() << 8;
	    uSectorShift += is.read();
	}
	posn += 2;
	toSkip = (long)0x30 - posn;
	skipped = 0;
	while (skipped != toSkip) {
	    skipped += is.skip(toSkip - skipped);
	}
	posn += skipped;
	int sectDirStart;
	if(byteOrder == 0xFE) {
	    sectDirStart = is.read();
	    sectDirStart += is.read()<<8;
	    sectDirStart += is.read()<<16;
	    sectDirStart += is.read()<<24;
	}
	else {
	    sectDirStart = is.read()<<24;
	    sectDirStart += is.read()<<16;
	    sectDirStart += is.read()<<8;
	    sectDirStart += is.read();
	}
	posn += 4;
	is.reset(); // Reset back to the beginning

	toSkip = (long)0x200 + 
		(long)((int)1<<uSectorShift)*sectDirStart + (long)0x50;

	// How far can we skip? Is there any performance problem here?
	// This skip can be fairly long, at least 0x4c650 in at least
	// one case. Have to assume that the skip will fit in an int.
	is.mark((int)toSkip+0x30); // Leave room to read whole root dir
	skipped = 0;
	while (skipped != toSkip) {
	    skipped += is.skip(toSkip - skipped);
	}
	/* should be at beginning of ClassID, which is as follows
	 * (in Intel byte order):
	 *    00 67 61 56 54 C1 CE 11 85 53 00 AA 00 A1 F9 5B
	 *
	 * This is stored from Windows as long,short,short,char[8]
	 * so for byte order changes, the order only changes for 
	 * the first 8 bytes in the ClassID.
	 *
	 * Test against this, ignoring second byte (Intel) since 
	 * this could change depending on part of Fpx file we have.
	 */
	int c[] = new int[16];

	for (int i=0; i<16; i++) c[i] = is.read();
	// intel byte order
	if (byteOrder == 0xFE && 
	    c[0] == 0x00 && c[2] == 0x61 && c[3] == 0x56 &&
	    c[4] == 0x54 && c[5] == 0xC1 && c[6] == 0xCE &&
	    c[7] == 0x11 && c[8] == 0x85 && c[9] == 0x53 &&
	    c[10]== 0x00 && c[11]== 0xAA && c[12]== 0x00 &&
	    c[13]== 0xA1 && c[14]== 0xF9 && c[15]== 0x5B) {
	    is.reset();
	    return true;
	}
	// non-intel byte order
	else if (c[3] == 0x00 && c[1] == 0x61 && c[0] == 0x56 &&
	    c[5] == 0x54 && c[4] == 0xC1 && c[7] == 0xCE &&
	    c[6] == 0x11 && c[8] == 0x85 && c[9] == 0x53 &&
	    c[10]== 0x00 && c[11]== 0xAA && c[12]== 0x00 &&
	    c[13]== 0xA1 && c[14]== 0xF9 && c[15]== 0x5B) {
	    is.reset();
	    return true;
	}
    is.reset();
    return false;
    }
}


class UnknownContentHandler extends ContentHandler {
    public Object getContent(URLConnection uc) throws IOException {
	return uc.getInputStream();
    }
}
