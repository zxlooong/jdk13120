/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package javax.naming.directory;

import javax.naming.NamingException;

/**
  * This exception is thrown when the specification of
  * a search filter is invalid.  The expression of the filter may
  * be invalid, or there may be a problem with one of the parameters
  * passed to the filter.
  * <p>
  * Synchronization and serialization issues that apply to NamingException
  * apply directly here.
  *
  * @author Rosanna Lee
  * @author Scott Seligman
  * @version 1.5 02/02/06
  * @since 1.3
  */
public class InvalidSearchFilterException extends NamingException {
    /**
     * Constructs a new instance of InvalidSearchFilterException.
     * All fields are set to null.
     */
    public InvalidSearchFilterException() {
	super();
    }

    /**
     * Constructs a new instance of InvalidSearchFilterException
     * with an explanation. All other fields are set to null.
     * @param msg Detail about this exception. Can be null.
     * @see java.lang.Throwable#getMessage
     */
    public InvalidSearchFilterException(String msg) {
	super(msg);
    }

    /**
     * Use serialVersionUID from JNDI 1.1.1 for interoperability
     */
    private static final long serialVersionUID = 2902700940682875441L;
}
