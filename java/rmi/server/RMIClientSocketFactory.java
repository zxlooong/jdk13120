/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.rmi.server;

import java.io.*;
import java.net.*;

/**
 * An <code>RMIClientSocketFactory</code> instance is used by the RMI runtime
 * in order to obtain client sockets for RMI calls.  A remote object can be
 * associated with an <code>RMIClientSocketFactory</code> when it is
 * created/exported via the constructors or <code>exportObject</code> methods
 * of <code>java.rmi.server.UnicastRemoteObject</code> and
 * <code>java.rmi.activation.Activatable</code> .
 *
 * <p>An <code>RMIClientSocketFactory</code> instance associated with a remote
 * object will be downloaded to clients when the remote object's reference is
 * transmitted in an RMI call.  This <code>RMIClientSocketFactory</code> will
 * be used to create connections to the remote object for remote method calls.
 *
 * <p>An <code>RMIClientSocketFactory</code> instance can also be associated
 * with a remote object registry so that clients can use custom socket
 * communication with a remote object registry.
 *
 * @version 1.8, 02/06/02
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   1.2
 * @see     java.rmi.server.UnicastRemoteObject
 * @see     java.rmi.activation.Activatable
 * @see     java.rmi.registry.LocateRegistry
 */
public interface RMIClientSocketFactory {

    /**
     * Create a client socket connected to the specified host and port.
     * @param  host   the host name
     * @param  port   the port number
     * @return a socket connected to the specified host and port.
     * @exception IOException if an I/O error occurs during socket creation
     * @since 1.2
     */
    public Socket createSocket(String host, int port)
	throws IOException;
}
