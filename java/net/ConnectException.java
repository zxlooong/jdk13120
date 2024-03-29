/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.net;

/**
 * Signals that an error occurred while attempting to connect a
 * socket to a remote address and port.  Typically, the connection
 * was refused remotely (e.g., no process is listening on the 
 * remote address/port).
 *
 * @since   JDK1.1
 */
public class ConnectException extends SocketException {
    /**
     * Constructs a new ConnectException with the specified detail 
     * message as to why the connect error occurred.
     * A detail message is a String that gives a specific 
     * description of this error.
     * @param msg the detail message
     */
    public ConnectException(String msg) {
	super(msg);
    }

    /**
     * Construct a new ConnectException with no detailed message.
     */
    public ConnectException() {}
}
