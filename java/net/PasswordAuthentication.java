/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.net;


/**
 * The class PasswordAuthentication is a data holder that is used by
 * Authenticator.  It is simply a repository for a user name and a password.
 *
 * @see java.net.Authenticator
 * @see java.net.Authenticator#getPasswordAuthentication()
 *
 * @author  Bill Foote
 * @version 1.11, 02/06/02
 * @since   1.2
 */

public final class PasswordAuthentication {

    private String userName;
    private char[] password;

    /**
     * Creates a new <code>PasswordAuthentication</code> object from the given
     * user name and password.
     *
     * <p> Note that the given user password is cloned before it is stored in
     * the new <code>PasswordAuthentication</code> object.
     *
     * @param userName the user name
     * @param password the user's password
     */
    public PasswordAuthentication(String userName, char[] password) {
	this.userName = userName;
	this.password = (char[])password.clone();
    }

    /**
     * Returns the user name.
     *
     * @return the user name
     */
    public String getUserName() {
	return userName;
    }

    /**
     * Returns the user password.
     *
     * <p> Note that this method returns a reference to the password. It is
     * the caller's responsibility to zero out the password information after
     * it is no longer needed.
     *
     * @return the password
     */
    public char[] getPassword() {
	return password;
    }
}

