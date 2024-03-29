/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.net;

import java.io.InputStream;
import java.io.IOException;
import java.security.Permission;
import java.util.Date;

/**
 * A URLConnection with support for HTTP-specific features. See
 * <A HREF="http://www.w3.org/pub/WWW/Protocols/"> the spec </A> for
 * details.
 * <p>
 * 
 * Each HttpURLConnection instance is used to make a single request  
 * but the underlying network connection to the HTTP server may be
 * transparently shared by other instances. Calling the close() methods
 * on the InputStream or OutputStream of an HttpURLConnection
 * after a request may free network resources associated with this
 * instance but has no effect on any shared persistent connection.
 * Calling the disconnect() method may close the underlying socket
 * if a persistent connection is otherwise idle at that time.
 * 
 * @see     java.net.HttpURLConnection#disconnect()
 * @since JDK1.1
 */
abstract public class HttpURLConnection extends URLConnection {
    /* instance variables */

    /**
     * The HTTP method (GET,POST,PUT,etc.). 
     */
    protected String method = "GET";

    /**
     * An <code>int</code> representing the three digit HTTP Status-Code.
     * <ul>
     * <li> 1xx: Informational
     * <li> 2xx: Success
     * <li> 3xx: Redirection
     * <li> 4xx: Client Error
     * <li> 5xx: Server Error
     * </ul>
     */
    protected int responseCode = -1;

    /**
     * The HTTP response message.
     */
    protected String responseMessage = null;

    /* static variables */

    /* do we automatically follow redirects? The default is true. */
    private static boolean followRedirects = true;

    /**
     * If <code>true</code>, the protocol will automatically follow redirects.
     * If <code>false</code>, the protocol will not automatically follow 
     * redirects.
     * <p>
     * This field is set by the <code>setInstanceFollowRedirects</code> 
     * method. Its value is returned by the <code>getInstanceFollowRedirects</code> 
     * method.
     * <p>
     * Its default value is based on the value of the static followRedirects 
     * at HttpURLConnection construction time.
     *
     * @see     java.net.HttpURLConnection#setInstanceFollowRedirects(boolean)
     * @see     java.net.HttpURLConnection#getInstanceFollowRedirects()
     * @see     java.net.HttpURLConnection#setFollowRedirects(boolean)
     */
    protected boolean instanceFollowRedirects = followRedirects;

    /* valid HTTP methods */
    private static final String[] methods = {
    "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
    };

    /**
     * Constructor for the URLStreamHandler.
     * @param u the URL
     */
    protected HttpURLConnection (URL u) {
    super(u);
    }
    
    /**
     * Sets whether HTTP redirects  (requests with response code 3xx) should 
     * be automatically followed by this class.  True by default.  Applets
     * cannot change this variable.
     * <p>
     * If there is a security manager, this method first calls
     * the security manager's <code>checkSetFactory</code> method 
     * to ensure the operation is allowed. 
     * This could result in a SecurityException.
     * 
     * @param set a <code>boolean</code> indicating whether or not
     * to follow HTTP redirects.
     * @exception  SecurityException  if a security manager exists and its  
     *             <code>checkSetFactory</code> method doesn't 
     *             allow the operation.
     * @see        SecurityManager#checkSetFactory
     * @see #getFollowRedirects()
     */
    public static void setFollowRedirects(boolean set) {
    SecurityManager sec = System.getSecurityManager();
    if (sec != null) {
        // seems to be the best check here...
        sec.checkSetFactory();
    }
    followRedirects = set;
    }

    /**
     * Returns a <code>boolean</code> indicating
     * whether or not HTTP redirects (3xx) should
     * be automatically followed.
     *
     * @return <code>true</code> if HTTP redirects should
     * be automatically followed, <tt>false</tt> if not.
     * @see #setFollowRedirects(boolean)
     */
    public static boolean getFollowRedirects() {
    return followRedirects;
    }

    /**
     * Sets whether HTTP redirects (requests with response code 3xx) should
     * be automatically followed by this <code>HttpURLConnection</code> 
     * instance.
     * <p>
     * The default value comes from followRedirects, which defaults to
     * true.
     *
     * @param followRedirects a <code>boolean</code> indicating 
     * whether or not to follow HTTP redirects.
     *
     * @see    java.net.HttpURLConnection#instanceFollowRedirects
     * @see #getInstanceFollowRedirects
     */
     public void setInstanceFollowRedirects(boolean followRedirects) {
    instanceFollowRedirects = followRedirects;
     }

     /**
     * Returns the value of this <code>HttpURLConnection</code>'s
     * <code>instanceFollowRedirects</code> field.
     *
     * @return  the value of this <code>HttpURLConnection</code>'s
     *          <code>instanceFollowRedirects</code> field.
     * @see     java.net.HttpURLConnection#instanceFollowRedirects
     * @see #setInstanceFollowRedirects(boolean)
     */
     public boolean getInstanceFollowRedirects() {
         return instanceFollowRedirects;
     }

    /**
     * Set the method for the URL request, one of:
     * <UL>
     *  <LI>GET
     *  <LI>POST
     *  <LI>HEAD
     *  <LI>OPTIONS
     *  <LI>PUT
     *  <LI>DELETE
     *  <LI>TRACE
     * </UL> are legal, subject to protocol restrictions.  The default
     * method is GET.
     * 
     * @param method the HTTP method
     * @exception ProtocolException if the method cannot be reset or if
     *              the requested method isn't valid for HTTP.
     * @see #getRequestMethod()
     */
    public void setRequestMethod(String method) throws ProtocolException {
    if (connected) {
        throw new ProtocolException("Can't reset method: already connected");
    }
    // This restriction will prevent people from using this class to 
    // experiment w/ new HTTP methods using java.  But it should 
    // be placed for security - the request String could be
    // arbitrarily long.

    for (int i = 0; i < methods.length; i++) {
        if (methods[i].equals(method)) {
        this.method = method;
        return;
        }
    }
    throw new ProtocolException("Invalid HTTP method: " + method);
    }

    /**
     * Get the request method.
     * @return the HTTP request method
     * @see #setRequestMethod(java.lang.String)
     */
    public String getRequestMethod() {
    return method;
    }
    
    /**
     * Gets HTTP response status from responses like:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * </PRE>
     * Extracts the ints 200 and 401 respectively.
     * Returns -1 if none can be discerned
     * from the response (i.e., the response is not valid HTTP).
     * @throws IOException if an error occurred connecting to the server.
     * @return the HTTP Status-Code
     */
    public int getResponseCode() throws IOException {
      /*
       * We're got the response code already
       */
      if (responseCode != -1) {
          return responseCode;
      }

      /*
       * Ensure that we have connected to the server. Record
       * exception as we need to re-throw it if there isn't
       * a status line.
       */
      Exception exc = null;
      try {
            getInputStream();
      } catch (Exception e) {
          exc = e;
      }

      /*
       * If we can't a status-line then re-throw any exception
       * that getInputStream threw.
       */
      String statusLine = getHeaderField(0);
      if (statusLine == null) {
          if (exc != null) {
              if (exc instanceof RuntimeException)
                    throw (RuntimeException)exc;
                else
                    throw (IOException)exc;
          }
          return -1;
      }

      /*
       * Examine the status-line - should be formatted as per
       * section 6.1 of RFC 2616 :-
       *
       * Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase
       *
       * If status line can't be parsed return -1.
       */
      if (statusLine.startsWith("HTTP/1.")) {
          int codePos = statusLine.indexOf(' ');
          if (codePos > 0) {

              int phrasePos = statusLine.indexOf(' ', codePos+1);
              if (phrasePos > 0 && phrasePos < statusLine.length()) {
                  responseMessage = statusLine.substring(phrasePos+1);
              }

              // deviation from RFC 2616 - don't reject status line
              // if SP Reason-Phrase is not included.
              if (phrasePos < 0) 
                  phrasePos = statusLine.length();

              try {
                  responseCode = Integer.parseInt
                            (statusLine.substring(codePos+1, phrasePos));     
                  return responseCode;
              } catch (NumberFormatException e) { }
          }
      }
      return -1;
    }

    /**
     * Gets the HTTP response message, if any, returned along with the
     * response code from a server.  From responses like:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 404 Not Found
     * </PRE>
     * Extracts the Strings "OK" and "Not Found" respectively.
     * Returns null if none could be discerned from the responses 
     * (the result was not valid HTTP).
     * @throws IOException if an error occurred connecting to the server.
     * @return the HTTP response message, or <code>null</code>
     */
    public String getResponseMessage() throws IOException {
    getResponseCode();
    return responseMessage;
    }

    public long getHeaderFieldDate(String name, long Default) {
    try {
        String dateString = getHeaderField(name);
        dateString.trim();
        if (dateString.indexOf("GMT") == -1) {
          dateString = dateString+" GMT";
        }
        return Date.parse(dateString);
    } catch (ThreadDeath td) {
        throw td;
    } catch(Throwable t) {
    }
    return Default;
    }


    /**
     * Indicates that other requests to the server
     * are unlikely in the near future. Calling disconnect() 
     * should not imply that this HttpURLConnection 
     * instance can be reused for other requests.
     */
    public abstract void disconnect();

    /**
     * Indicates if the connection is going through a proxy.
     * @return a boolean indicating if the connection is
     * using a proxy.
     */
    public abstract boolean usingProxy();

    public Permission getPermission() throws IOException {
    int port = url.getPort();
    port = port < 0 ? 80 : port;
    String host = url.getHost() + ":" + port;
    Permission permission = new SocketPermission(host, "connect");
    return permission;
    }

   /**
    * Returns the error stream if the connection failed
    * but the server sent useful data nonetheless. The
    * typical example is when an HTTP server responds
    * with a 404, which will cause a FileNotFoundException 
    * to be thrown in connect, but the server sent an HTML
    * help page with suggestions as to what to do.
    *
    * <p>This method will not cause a connection to be initiated.
    * If there the connection was not connected, or if the server
    * did not have an error while connecting or if the server did
    * have an error but there no error data was sent, this method
    * will return null. This is the default.
    *
    * @return an error stream if any, null if there have been
    * no errors, the connection is not connected or the server
    * sent no useful data.
    */
    public InputStream getErrorStream() {
    return null;
    }

    /**
     * The response codes for HTTP, as of version 1.1.
     */

    // REMIND: do we want all these??
    // Others not here that we do want??

    /* 2XX: generally "OK" */

    /**
     * HTTP Status-Code 200: OK.
     */
    public static final int HTTP_OK = 200;

    /**
     * HTTP Status-Code 201: Created.
     */
    public static final int HTTP_CREATED = 201;

    /**
     * HTTP Status-Code 202: Accepted.
     */
    public static final int HTTP_ACCEPTED = 202;

    /**
     * HTTP Status-Code 203: Non-Authoritative Information.
     */    
    public static final int HTTP_NOT_AUTHORITATIVE = 203; 

    /**
     * HTTP Status-Code 204: No Content.
     */
    public static final int HTTP_NO_CONTENT = 204;

    /**
     * HTTP Status-Code 205: Reset Content.
     */
    public static final int HTTP_RESET = 205;

    /**
     * HTTP Status-Code 206: Partial Content.
     */
    public static final int HTTP_PARTIAL = 206;

    /* 3XX: relocation/redirect */

    /**
     * HTTP Status-Code 300: Multiple Choices.
     */
    public static final int HTTP_MULT_CHOICE = 300;

    /**
     * HTTP Status-Code 301: Moved Permanently.
     */
    public static final int HTTP_MOVED_PERM = 301;

    /**
     * HTTP Status-Code 302: Temporary Redirect.
     */
    public static final int HTTP_MOVED_TEMP = 302;
 
    /**
     * HTTP Status-Code 303: See Other.
     */
    public static final int HTTP_SEE_OTHER = 303;

    /**
     * HTTP Status-Code 304: Not Modified.
     */
    public static final int HTTP_NOT_MODIFIED = 304;

    /**
     * HTTP Status-Code 305: Use Proxy.
     */
    public static final int HTTP_USE_PROXY = 305;

    /* 4XX: client error */

    /**
     * HTTP Status-Code 400: Bad Request.
     */
    public static final int HTTP_BAD_REQUEST = 400;

    /**
     * HTTP Status-Code 401: Unauthorized.
     */
    public static final int HTTP_UNAUTHORIZED = 401;

    /**
     * HTTP Status-Code 402: Payment Required.
     */
    public static final int HTTP_PAYMENT_REQUIRED = 402;

    /**
     * HTTP Status-Code 403: Forbidden.
     */
    public static final int HTTP_FORBIDDEN = 403;

    /**
     * HTTP Status-Code 404: Not Found.
     */
    public static final int HTTP_NOT_FOUND = 404;

    /**
     * HTTP Status-Code 405: Method Not Allowed.
     */
    public static final int HTTP_BAD_METHOD = 405;

    /**
     * HTTP Status-Code 406: Not Acceptable.
     */
    public static final int HTTP_NOT_ACCEPTABLE = 406;

    /**
     * HTTP Status-Code 407: Proxy Authentication Required.
     */
    public static final int HTTP_PROXY_AUTH = 407;

    /**
     * HTTP Status-Code 408: Request Time-Out.
     */
    public static final int HTTP_CLIENT_TIMEOUT = 408;

    /**
     * HTTP Status-Code 409: Conflict.
     */
    public static final int HTTP_CONFLICT = 409;

    /**
     * HTTP Status-Code 410: Gone.
     */
    public static final int HTTP_GONE = 410;

    /**
     * HTTP Status-Code 411: Length Required.
     */
    public static final int HTTP_LENGTH_REQUIRED = 411;

    /**
     * HTTP Status-Code 412: Precondition Failed.
     */
    public static final int HTTP_PRECON_FAILED = 412;

    /**
     * HTTP Status-Code 413: Request Entity Too Large.
     */
    public static final int HTTP_ENTITY_TOO_LARGE = 413;

    /**
     * HTTP Status-Code 414: Request-URI Too Large.
     */
    public static final int HTTP_REQ_TOO_LONG = 414;

    /**
     * HTTP Status-Code 415: Unsupported Media Type.
     */
    public static final int HTTP_UNSUPPORTED_TYPE = 415;
    
    /* 5XX: server error */

    /**
    * @deprecated   it is misplaced and shouldn't have existed.
    */
    public static final int HTTP_SERVER_ERROR = 500;

    /** 
     * HTTP Status-Code 500: Internal Server Error. 
     */
    public static final int HTTP_INTERNAL_ERROR = 500;

    /** 
     * HTTP Status-Code 501: Not Implemented.
     */
    public static final int HTTP_NOT_IMPLEMENTED = 501;

    /**
     * HTTP Status-Code 502: Bad Gateway.
     */
    public static final int HTTP_BAD_GATEWAY = 502;

    /**
     * HTTP Status-Code 503: Service Unavailable.
     */
    public static final int HTTP_UNAVAILABLE = 503;

    /**
     * HTTP Status-Code 504: Gateway Timeout.
     */
    public static final int HTTP_GATEWAY_TIMEOUT = 504;

    /**
     * HTTP Status-Code 505: HTTP Version Not Supported.
     */
    public static final int HTTP_VERSION = 505;

}
