/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package java.sql;

import java.security.*;

/**
 * The permission for which the <code>SecurityManager</code> will check
 * when code that is running in an applet calls one of the 
 * <code>setLogWriter</code> methods.  These methods include those in the
 * following list.
 * <UL>
 * <LI><code>DriverManager.setLogWriter</code> <br>
 * <LI><code>DriverManager.setLogStream</code> (deprecated)<br>
 * <LI><code>javax.sql.DataSource.setLogWriter</code><br>
 * <LI><code>javax.sql.ConnectionPoolDataSource.setLogWriter</code><br>
 * <LI><code>javax.sql.XADataSource.setLogWriter</code><br>
 * </UL>
 * If there is no <code>SQLPermission</code> object, this method
 * throws a <code>java.lang.SecurityException</code> as a runtime exception.
 * <P> 
 * A <code>SQLPermission</code> object contains
 * a name (also referred to as a "target name") but no actions
 * list; there is either a named permission or there is not.
 * The target name is the name of the permission (see below). The
 * naming convention follows the  hierarchical property naming convention.
 * In addition, an asterisk
 * may appear at the end of the name, following a ".", or by itself, to
 * signify a wildcard match. For example: <code>loadLibrary.*</code>
 * or <code>*</code> is valid,
 * but <code>*loadLibrary</code> or <code>a*b</code> is not valid.
 * <P>
 * The following table lists all the possible <code>SQLPermission</code> target names.
 * Currently, the only name allowed is <code>setLog</code>.
 * The table gives a description of what the permission allows
 * and a discussion of the risks of granting code the permission.
 * <P>
 *
 * <table border=1 cellpadding=5>
 * <tr>
 * <th>Permission Target Name</th>
 * <th>What the Permission Allows</th>
 * <th>Risks of Allowing this Permission</th>
 * </tr>
 *
 * <tr>
 *   <td>setLog</td>
 *   <td>Setting of the logging stream</td>
 *   <td>This is a dangerous permission to grant.
 * The contents of the log may contain usernames and passwords,
 * SQL statements, and SQL data.</td>
 * </tr>
 * 
 * </table>
 *
 * The person running an applet decides what permissions to allow
 * and will run the <code>Policy Tool</code> to create an
 * <code>SQLPermission</code> in a policy file.  A programmer does
 * not use a constructor directly to create an instance of <code>SQLPermission</code>
 * but rather uses a tool.
 * @since 1.3
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 */

public final class SQLPermission extends BasicPermission {

    /**
     * Creates a new <code>SQLPermission</code> object with the specified name.
     * The name is the symbolic name of the <code>SQLPermission</code>; currently,
     * the only name allowed is "setLog".
     *
     * @param name the name of this <code>SQLPermission</code> object, which must
	 *             be <code>setLog</code>
     */

    public SQLPermission(String name) {
        super(name);
    }

    /**
     * Creates a new <code>SQLPermission</code> object with the specified name.
     * The name is the symbolic name of the <code>SQLPermission</code>; the
     * actions <code>String</code> is currently unused and should be
	 * <code>null</code>.
     *
     * @param name the name of this <code>SQLPermission</code> object, which must
	 *             be <code>setLog</code>
     * @param actions should be <code>null</code>
     */

    public SQLPermission(String name, String actions) {
        super(name, actions);
    }
}
