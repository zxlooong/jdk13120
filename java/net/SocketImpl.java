/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileDescriptor;

/**
 * The abstract class <code>SocketImpl</code> is a common superclass 
 * of all classes that actually implement sockets. It is used to 
 * create both client and server sockets. 
 * <p>
 * A "plain" socket implements these methods exactly as 
 * described, without attempting to go through a firewall or proxy. 
 *
 * @author  unascribed
 * @version 1.31, 02/06/02
 * @since   JDK1.0
 */
public abstract class SocketImpl implements SocketOptions {
    /**
     * The file descriptor object for this socket. 
     */
    protected FileDescriptor fd;
    
    /**
     * The IP address of the remote end of this socket. 
     */
    protected InetAddress address;
   
    /**
     * The port number on the remote host to which this socket is connected. 
     */
    protected int port;

    /**
     * The local port number to which this socket is connected. 
     */
    protected int localport;   

    /**
     * Creates either a stream or a datagram socket. 
     *
     * @param      stream   if <code>true</code>, create a stream socket;
     *                      otherwise, create a datagram socket.
     * @exception  IOException  if an I/O error occurs while creating the
     *               socket.
     */
    protected abstract void create(boolean stream) throws IOException;

    /**
     * Connects this socket to the specified port on the named host. 
     *
     * @param      host   the name of the remote host.
     * @param      port   the port number.
     * @exception  IOException  if an I/O error occurs when connecting to the
     *               remote host.
     */
    protected abstract void connect(String host, int port) throws IOException;

    /**
     * Connects this socket to the specified port number on the specified host.
     *
     * @param      address   the IP address of the remote host.
     * @param      port      the port number.
     * @exception  IOException  if an I/O error occurs when attempting a
     *               connection.
     */
    protected abstract void connect(InetAddress address, int port) throws IOException;

    /**
     * Binds this socket to the specified port number on the specified host. 
     *
     * @param      host   the IP address of the remote host.
     * @param      port   the port number.
     * @exception  IOException  if an I/O error occurs when binding this socket.
     */
    protected abstract void bind(InetAddress host, int port) throws IOException;

    /**
     * Sets the maximum queue length for incoming connection indications 
     * (a request to connect) to the <code>count</code> argument. If a 
     * connection indication arrives when the queue is full, the 
     * connection is refused. 
     *
     * @param      backlog   the maximum length of the queue.
     * @exception  IOException  if an I/O error occurs when creating the queue.
     */
    protected abstract void listen(int backlog) throws IOException;

    /**
     * Accepts a connection. 
     *
     * @param      s   the accepted connection.
     * @exception  IOException  if an I/O error occurs when accepting the
     *               connection.
     */
    protected abstract void accept(SocketImpl s) throws IOException;

    /**
     * Returns an input stream for this socket.
     *
     * @return     a stream for reading from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *               input stream.
    */
    protected abstract InputStream getInputStream() throws IOException;

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *               output stream.
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    /**
     * Returns the number of bytes that can be read from this socket
     * without blocking.
     *
     * @return     the number of bytes that can be read from this socket
     *             without blocking.
     * @exception  IOException  if an I/O error occurs when determining the
     *               number of bytes available.
     */
    protected abstract int available() throws IOException;

    /**
     * Closes this socket. 
     *
     * @exception  IOException  if an I/O error occurs when closing this socket.
     */
    protected abstract void close() throws IOException;

    /**
     * Places the input stream for this socket at "end of stream".
     * Any data sent to this socket is acknowledged and then
     * silently discarded.
     *
     * If you read from a socket input stream after invoking 
     * shutdownInput() on the socket, the stream will return EOF.
     *
     * @exception IOException if an I/O error occurs when shutting down this
     * socket.
     * @see java.net.Socket#shutdownOutput()
     * @see java.net.Socket#close()
     * @see java.net.Socket#setSoLinger(boolean, int)
     */
    protected void shutdownInput() throws IOException {
      throw new IOException("Method not implemented!");
    }
    
    /**
     * Disables the output stream for this socket.
     * For a TCP socket, any previously written data will be sent
     * followed by TCP's normal connection termination sequence.
     *
     * If you write to a socket output stream after invoking 
     * shutdownOutput() on the socket, the stream will throw 
     * an IOException.
     *
     * @exception IOException if an I/O error occurs when shutting down this
     * socket.
     * @see java.net.Socket#shutdownInput()
     * @see java.net.Socket#close()
     * @see java.net.Socket#setSoLinger(boolean, int)
     */
    protected void shutdownOutput() throws IOException {
      throw new IOException("Method not implemented!");
    }  

    /**
     * Returns the value of this socket's <code>fd</code> field.
     *
     * @return  the value of this socket's <code>fd</code> field.
     * @see     java.net.SocketImpl#fd
     */
    protected FileDescriptor getFileDescriptor() {
	return fd;
    }

    /**
     * Returns the value of this socket's <code>address</code> field.
     *
     * @return  the value of this socket's <code>address</code> field.
     * @see     java.net.SocketImpl#address
     */
    protected InetAddress getInetAddress() {
	return address;
    }

    /**
     * Returns the value of this socket's <code>port</code> field.
     *
     * @return  the value of this socket's <code>port</code> field.
     * @see     java.net.SocketImpl#port
     */
    protected int getPort() {
	return port;
    }

    /**
     * Returns the value of this socket's <code>localport</code> field.
     *
     * @return  the value of this socket's <code>localport</code> field.
     * @see     java.net.SocketImpl#localport
     */
    protected int getLocalPort() {
	return localport;
    }
    
    /**
     * Returns the address and port of this socket as a <code>String</code>.
     *
     * @return  a string representation of this socket.
     */
    public String toString() {
	return "Socket[addr=" + getInetAddress() +
	    ",port=" + getPort() + ",localport=" + getLocalPort()  + "]";
    }

	void reset() throws IOException {
    address = null;
    port = 0;
    localport = 0;
    close();
    }
}
