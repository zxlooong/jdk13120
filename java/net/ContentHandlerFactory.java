/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.net;

/**
 * This interface defines a factory for content handlers. An
 * implementation of this interface should map a MIME type into an
 * instance of <code>ContentHandler</code>.
 * <p>
 * This interface is used by the <code>URLStreamHandler</code> class
 * to create a <code>ContentHandler</code> for a MIME type.
 *
 * @author  James Gosling
 * @version 1.10, 02/06/02
 * @see     java.net.ContentHandler
 * @see     java.net.URLStreamHandler
 * @since   JDK1.0
 */
public interface ContentHandlerFactory {
    /**
     * Creates a new <code>ContentHandler</code> to read an object from
     * a <code>URLStreamHandler</code>.
     *
     * @param   mimetype   the MIME type for which a content handler is desired.

     * @return  a new <code>ContentHandler</code> to read an object from a
     *          <code>URLStreamHandler</code>.
     * @see     java.net.ContentHandler
     * @see     java.net.URLStreamHandler
     */
    ContentHandler createContentHandler(String mimetype);
}
