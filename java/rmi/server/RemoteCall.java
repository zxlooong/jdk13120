/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.rmi.server;
import java.rmi.*;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.StreamCorruptedException;
import java.io.IOException;

/**
 * <code>RemoteCall</code> is an abstraction used solely by the RMI runtime
 * (in conjunction with stubs and skeletons of remote objects) to carry out a
 * call to a remote object.  The <code>RemoteCall</code> interface is
 * deprecated in the Java 2 platform since it is only used by deprecated methods of
 * <code>java.rmi.server.RemoteRef</code>.
 *
 * @version 1.16, 02/06/02
 * @since   JDK1.1
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @see     java.rmi.server.RemoteRef
 * @deprecated no replacement.
 */
public interface RemoteCall {

    /**
     * Return the output stream the stub/skeleton should put arguments/results
     * into.
     *
     * @return output stream for arguments/results
     * @exception java.io.IOException if an I/O error occurs.
     * @since JDK1.1
     * @deprecated no replacement
     */
    ObjectOutput getOutputStream()  throws IOException;
    
    /**
     * Release the output stream; in some transports this would release
     * the stream.
     *
     * @exception java.io.IOException if an I/O error occurs.
     * @since JDK1.1
     * @deprecated no replacement
     */
    void releaseOutputStream()  throws IOException;

    /**
     * Get the InputStream that the stub/skeleton should get
     * results/arguments from.
     *
     * @return input stream for reading arguments/results
     * @exception java.io.IOException if an I/O error occurs.
     * @since JDK1.1
     * @deprecated no replacement
     */
    ObjectInput getInputStream()  throws IOException;

    
    /**
     * Release the input stream. This would allow some transports to release
     * the channel early.
     *
     * @exception java.io.IOException if an I/O error occurs.
     * @since JDK1.1
     * @deprecated no replacement
     */
    void releaseInputStream() throws IOException;

    /**
     * Returns an output stream (may put out header information
     * relating to the success of the call). Should only succeed
     * once per remote call.
     *
     * @param success If true, indicates normal return, else indicates
     * exceptional return.
     * @return output stream for writing call result
     * @exception java.io.IOException              if an I/O error occurs.
     * @exception java.io.StreamCorruptedException If already been called.
     * @since JDK1.1
     * @deprecated no replacement
     */
    ObjectOutput getResultStream(boolean success) throws IOException,
	StreamCorruptedException;
    
    /**
     * Do whatever it takes to execute the call.
     *
     * @exception java.lang.Exception if a general exception occurs.
     * @since JDK1.1
     * @deprecated no replacement
     */
    void executeCall() throws Exception;

    /**
     * Allow cleanup after the remote call has completed.
     *
     * @exception java.io.IOException if an I/O error occurs.
     * @since JDK1.1
     * @deprecated no replacement
     */
    void done() throws IOException;
}
