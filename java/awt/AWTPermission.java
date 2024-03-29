/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt;

import java.security.BasicPermission;

/**
 * This class is for AWT permissions.
 * An AWTPermission contains a target name but
 * no actions list; you either have the named permission
 * or you don't.
 *
 * <P>
 * The target name is the name of the AWT permission (see below). The naming
 * convention follows the hierarchical property naming convention.
 * Also, an asterisk could be used to represent all AWT permissions.
 *
 * <P>
 * The following table lists all the possible AWTPermission target names,
 * and for each provides a description of what the permission allows
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
 *   <td>accessClipboard</td>
 *   <td>Posting and retrieval of information to and from the AWT clipboard</td>
 *   <td>This would allow malfeasant code to share
 * potentially sensitive or confidential information.</td>
 * </tr>
 *
 * <tr>
 *   <td>accessEventQueue</td>
 *   <td>Access to the AWT event queue</td>
 *   <td>After retrieving the AWT event queue,
 * malicious code may peek at and even remove existing events
 * from its event queue, as well as post bogus events which may purposefully
 * cause the application or applet to misbehave in an insecure manner.</td>
 * </tr>
 *
 * <tr>
 *   <td>listenToAllAWTEvents</td>
 *   <td>Listen to all AWT events, system-wide</td>
 *   <td>After adding an AWT event listener,
 * malicious code may scan all AWT events dispatched in the system,
 * allowing it to read all user input (such as passwords).  Each
 * AWT event listener is called from within the context of that
 * event queue's EventDispatchThread, so if the accessEventQueue
 * permission is also enabled, malicious code could modify the
 * contents of AWT event queues system-wide, causing the application
 * or applet to misbehave in an insecure manner.</td>
 * </tr>
 *
 * <tr>
 *   <td>showWindowWithoutWarningBanner</td>
 *   <td>Display of a window without also displaying a banner warning
 * that the window was created by an applet</td>
 *   <td>Without this warning,
 * an applet may pop up windows without the user knowing that they
 * belong to an applet.  Since users may make security-sensitive
 * decisions based on whether or not the window belongs to an applet
 * (entering a username and password into a dialog box, for example),
 * disabling this warning banner may allow applets to trick the user
 * into entering such information.</td>
 * </tr>
 *
 * <tr>
 *   <td>readDisplayPixels</td>
 *   <td>Readback of pixels from the display screen</td>
 *   <td>Interfaces such as the java.awt.Composite interface or the 
 * java.awt.Robot class allow arbitrary code to examine pixels on the 
 * display enable malicious code to snoop on the activities of the user.</td>
 * </tr>
 *
 * <tr>
 *   <td>createRobot</td>
 *   <td>Create java.awt.Robot objects</td>
 *   <td>The java.awt.Robot object allows code to generate native-level
 * mouse and keyboard events as well as read the screen. It could allow
 * malicious code to control the system, run other programs, read the
 * display, and deny mouse and keyboard access to the user.</td>
 * </tr>
 * </table>
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 * @version 	1.19, 02/06/02
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 */

public final class AWTPermission extends BasicPermission {

    /** use serialVersionUID from the Java 2 platform for interoperability */
    private static final long serialVersionUID = 8890392402588814465L;

    /**
     * Creates a new AWTPermission with the specified name.
     * The name is the symbolic name of the AWTPermission, such as
     * "topLevelWindow", "systemClipboard", etc. An asterisk
     * may be used to indicate all AWT permissions.
     *
     * @param name the name of the AWTPermission.
     */

    public AWTPermission(String name)
    {
	super(name);
    }

    /**
     * Creates a new AWTPermission object with the specified name.
     * The name is the symbolic name of the AWTPermission, and the
     * actions String is currently unused and should be null. This
     * constructor exists for use by the <code>Policy</code> object
     * to instantiate new Permission objects.
     *
     * @param name the name of the AWTPermission.
     * @param actions should be null.
     */

    public AWTPermission(String name, String actions)
    {
	super(name, actions);
    }
}
