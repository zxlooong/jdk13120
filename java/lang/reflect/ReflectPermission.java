/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.lang.reflect;

/**
 * The Permission class for reflective operations.  A
 * ReflectPermission is a <em>named permission</em> and has no
 * actions.  The only name currently defined is <tt>suppressAccessChecks</tt>,
 * which allows suppressing the standard Java language access checks
 * -- for public, default (package) access, protected, and private
 * members -- performed by reflected objects at their point of use.
 * <P>
 * The following table
 * provides a summary description of what the permission allows,
 * and discusses the risks of granting code the permission.
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
 *   <td>suppressAccessChecks</td>
 *   <td>ability to access
 * fields and invoke methods in a class. Note that this includes
 * not only public, but protected and private fields and methods as well.</td>
 *   <td>This is dangerous in that information (possibly confidential) and
 * methods normally unavailable would be accessible to malicious code.</td>
 * </tr>
 *
 * </table>
 *
 * @see java.security.Permission
 * @see java.security.BasicPermission
 * @see AccessibleObject
 * @see Field#get
 * @see Field#set
 * @see Method#invoke
 * @see Constructor#newInstance
 *
 * @since 1.2
 */
public final
class ReflectPermission extends java.security.BasicPermission {

    /**
     * Constructs a ReflectPermission with the specified name.
     *
     * @param name the name of the ReflectPermission
     */
    public ReflectPermission(String name) {
	super(name);
    }

    /**
     * Constructs a ReflectPermission with the specified name and actions.
     * The actions should be null; they are ignored. This
     * constructor exists for use by the <code>Policy</code> object
     * to instantiate new Permission objects.
     *
     * @param name the name of the ReflectPermission
     * @param actions should be null.
     */
    public ReflectPermission(String name, String actions) {
	super(name, actions);
    }

}
