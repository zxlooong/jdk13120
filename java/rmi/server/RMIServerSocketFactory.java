/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.rmi.server;

import java.io.*;
import java.net.*;

/**
 * An <code>RMIServerSocketFactory</code> instance is used by the RMI runtime
 * in order to obtain server sockets for RMI calls.  A remote object can be
 * associated with an <code>RMIServerSocketFactory</code> when it is
 * created/exported via the constructors or <code>exportObject</code> methods
 * of <code>java.rmi.server.UnicastRemoteObject</code> and
 * <code>java.rmi.activation.Activatable</code> .
 *
 * <p>An <code>RMIServerSocketFactory</code> instance associated with a remote
 * object is used to obtain the <code>ServerSocket</code> used to accept
 * incoming calls from clients.
 *
 * <p>An <code>RMIServerSocketFactory</code> instance can also be associated
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
public interface RMIServerSocketFactory {

    /**
     * Create a server socket on the specified port (port 0 indicates
     * an anonymous port).
     * @param  port the port number
     * @return the server socket on the specified port
     * @exception IOException if an I/O error occurs during server socket
     * creation
     * @since 1.2
     */
    public ServerSocket createServerSocket(int port)
	throws IOException;
}
