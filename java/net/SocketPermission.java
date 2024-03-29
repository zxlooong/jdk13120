/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package java.net;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.net.InetAddress;
import java.security.Permission;
import java.security.PermissionCollection;
import java.io.Serializable;
import java.io.IOException;

/**
 * This class represents access to a network via sockets.
 * A SocketPermission consists of a 
 * host specification and a set of "actions" specifying ways to
 * connect to that host. The host is specified as
 * <pre>
 *    host = (hostname | IPaddress)[:portrange]
 *    portrange = portnumber | -portnumber | portnumber-[portnumber]
 * </pre>
 * The host is expressed as a DNS name, as a numerical IP address,
 * or as "localhost" (for the local machine).
 * The wildcard "*" may be included once in a DNS name host
 * specification. If it is included, it must be in the leftmost 
 * position, as in "*.sun.com".
 * <p>
 * The port or portrange is optional. A port specification of the 
 * form "N-", where <i>N</i> is a port number, signifies all ports
 * numbered <i>N</i> and above, while a specification of the
 * form "-N" indicates all ports numbered <i>N</i> and below.
 * <p>
 * The possible ways to connect to the host are 
 * <pre>
 * accept
 * connect
 * listen
 * resolve
 * </pre>
 * The "listen" action is only meaningful when used with "localhost". 
 * The "resolve" (resolve host/ip name service lookups) action is implied
 * when any of the other actions are present.
 * 
 * <p>As an example of the creation and meaning of SocketPermissions,  
 * note that if the following permission:
 * 
 * <pre>
 *   p1 = new SocketPermission("puffin.eng.sun.com:7777", "connect,accept");
 * </pre>
 * 
 * is granted to some code, it allows that code to connect to port 7777 on
 * <code>puffin.eng.sun.com</code>, and to accept connections on that port.
 * 
 * <p>Similarly, if the following permission:
 * 
 * <pre>
 *   p1 = new SocketPermission("puffin.eng.sun.com:7777", "connect,accept");
 *   p2 = new SocketPermission("localhost:1024-", "accept,connect,listen");
 * </pre>
 * 
 * is granted to some code, it allows that code to 
 * accept connections on, connect to, or listen on any port between
 * 1024 and 65535 on the local host.
 *
 * <p>Note: Granting code permission to accept or make connections to remote
 * hosts may be dangerous because malevolent code can then more easily
 * transfer and share confidential data among parties who may not
 * otherwise have access to the data.
 * 
 * @see java.security.Permissions
 * @see SocketPermission
 *
 * @version 1.38 02/02/06
 *
 * @author Marianne Mueller
 * @author Roland Schemers 
 *
 * @serial exclude
 */

public final class SocketPermission extends Permission 
implements java.io.Serializable 
{

    /**
     * Connect to host:port
     */
    private final static int CONNECT	= 0x1;

    /**
     * Listen on host:port
     */
    private final static int LISTEN	= 0x2;

    /**
     * Accept a connection from host:port
     */
    private final static int ACCEPT	= 0x4;

    /**
     * Resolve DNS queries
     */
    private final static int RESOLVE	= 0x8;

    /**
     * No actions
     */
    private final static int NONE		= 0x0;

    /**
     * All actions
     */ 
    private final static int ALL	= CONNECT|LISTEN|ACCEPT|RESOLVE;

    // various port constants
    private static final int PORT_MIN = 0;
    private static final int PORT_MAX = 65535;
    private static final int PRIV_PORT_MAX = 1023;

    // the actions mask
    private transient int mask;

    /**
     * the actions string. 
     *
     * @serial
     */

    private String actions; // Left null as long as possible, then
                            // created and re-used in the getAction function.

    // the canonical name of the host
    // in the case of "*.foo.com", cname is ".foo.com".

    private transient String cname;

    // all the IP addresses of the host 
    private transient InetAddress[] addresses;

    // true if the hostname is a wildcard (e.g. "*.sun.com")
    private transient boolean wildcard;

    // true if we were initialized with a single numeric IP address
    private transient boolean init_with_ip;

    // true if this SocketPermission represents an invalid/unknown host
    // used for implies when the delayed lookup has already failed
    private transient boolean invalid;

    // port range on host
    private transient int[] portrange; 

    // true if the trustProxy system property is set
    private static boolean trustProxy;

    static {
	Boolean tmp = (Boolean) java.security.AccessController.doPrivileged(
                new sun.security.action.GetBooleanAction("trustProxy"));
	trustProxy = tmp.booleanValue();
    }

    /**
     * Creates a new SocketPermission object with the specified actions.
     * The host is expressed as a DNS name, or as a numerical IP address.
     * Optionally, a port or a portrange may be supplied (separated
     * from the DNS name or IP address by a colon).
     * <p>
     * To specify the local machine, use "localhost" as the <i>host</i>.
     * Also note: An empty <i>host</i> String ("") is equivalent to "localhost".
     * <p>
     * The <i>actions</i> parameter contains a comma-separated list of the
     * actions granted for the specified host (and port(s)). Possible actions are
     * "connect", "listen", "accept", "resolve", or 
     * any combination of those. "resolve" is automatically added
     * when any of the other three are specified.
     * <p>
     * Examples of SocketPermission instantiation are the following: 
     * <pre>
     *    nr = new SocketPermission("www.catalog.com", "connect");
     *    nr = new SocketPermission("www.sun.com:80", "connect");
     *    nr = new SocketPermission("*.sun.com", "connect");
     *    nr = new SocketPermission("*.edu", "resolve");
     *    nr = new SocketPermission("204.160.241.0", "connect");
     *    nr = new SocketPermission("localhost:1024-65535", "listen");
     *    nr = new SocketPermission("204.160.241.0:1024-65535", "connect");
     * </pre>
     * 
     * @param host the hostname or IPaddress of the computer, optionally
     * including a colon followed by a port or port range. 
     * @param action the action string.
     */
    public SocketPermission(String host, String action) {
	super(getHost(host));
	init(host, getMask(action));
    }


    SocketPermission(String host, int mask) {
	super(getHost(host));
	init(host, mask);
    }

    private static String getHost(String host)
    {
	if (host.equals(""))
	    return "localhost";
	else 
	    return host;
    }

    private boolean isDottedIP(String host) 
    {
	char[] h = host.toCharArray();
	int n = h.length - 1;
	int components = 0;

	if (n > -1 && h[0] == '.')
	    return false;

	while (n != -1) {
	    char c0, c1, c2;
	    c0 = h[n];
	    if (n < 2) {
		c2 = '.';
		if (n == 1) {
		    c1 = h[0];
		} else {
		    c1 = '.';
		}
		n = -1;
	    } else {
		c1 = h[n-1];
		if (c1 == '.') {
		    c2 = '.';
		    n -= 2;
		} else {
		    c2 = h[n-2];
		    if (c2 == '.') {
			n -= 3;
		    } else {
			if ((n-3) != -1) {
			    if (h[n-3] != '.')
				return false;
			    n -= 4;
			} else
			    n -= 3;
		    }
		}
	    }
	    if (c0 < '0' || c0 > '9' ||
		    (c1 < '0' && c1 != '.') || c1 > '9' ||
		    (c2 < '0' && c2 != '.') || c2 > '2' ||
		    (c2 == '2' && (c1 > '5' || (c1 == '5' && c0 > '5'))))
		return false;
	    components++;
	}

	return (components == 4);
    }

    private int[] parsePort(String port) 
	throws Exception
    {

	if (port == null || port.equals("") || port.equals("*")) {
	    return new int[] {PORT_MIN, PORT_MAX};
	}

	int dash = port.indexOf('-');

	if (dash == -1) {
	    int p = Integer.parseInt(port);
	    return new int[] {p, p};
	} else {
	    String low = port.substring(0, dash);
	    String high = port.substring(dash+1);
	    int l,h;

	    if (low.equals("")) {
		l = PORT_MIN;
	    } else {
		l = Integer.parseInt(low);
	    }

	    if (high.equals("")) {
		h = PORT_MAX;
	    } else {
		h = Integer.parseInt(high);
	    }
	    if (h<l) 
		throw new IllegalArgumentException("invalid port range");

	    return new int[] {l, h};
	}
    }

    /**
     * Initialize the SocketPermission object. We don't do any DNS lookups
     * as this point, instead we hold off until the implies method is
     * called.
     */
    private void init(String host, int mask) {

	if (host == null) 
		throw new NullPointerException("host can't be null");

	host = getHost(host);

	// Set the integer mask that represents the actions

	if ((mask & ALL) != mask) 
	    throw new IllegalArgumentException("invalid actions mask");

	// always OR in RESOLVE if we allow any of the others
	this.mask = mask | RESOLVE;

	// Parse the host name.  A name has up to three components, the
	// hostname, a port number, or two numbers representing a port
	// range.   "www.sun.com:8080-9090" is a valid host name.  

	int sep = host.indexOf(':');

	if (sep != -1) {
	    String port = host.substring(sep+1);
	    host = host.substring(0, sep);
	    try {
		portrange = parsePort(port);
	    } catch (Exception e) {
		throw new 
		    IllegalArgumentException("invalid port range: "+port);
	    }
	} else {
	    portrange = new int[] { PORT_MIN, PORT_MAX };
	}
	    
	// is this a domain wildcard specification

	if (host.startsWith("*")) {
	    wildcard = true;
	    if (host.equals("*")) {
		cname = "";
	    } else if (host.startsWith("*.")) {
		cname = host.substring(1).toLowerCase();
	    } else {
	      throw new 
	       IllegalArgumentException("invalid host wildcard specification");
	    }
	    return;
	} else {
	    // see if we are being initialized with an IP address.
	    if (isDottedIP(host)) {
		try {
		    addresses = 
			new InetAddress[] {InetAddress.getByName(host) };
		    init_with_ip = true;
		} catch (UnknownHostException uhe) {
		    // this shouldn't happen
		    invalid = true;
		}
	    }
	}
    }

    /**
     * Convert an action string to an integer actions mask. 
     *
     * @param action the action string
     * @return the action mask
     */
    private static int getMask(String action) {

	if (action == null) {
	    throw new NullPointerException("action can't be null");
	}

	if (action.equals("")) {
	    throw new IllegalArgumentException("action can't be empty");
	}

	int mask = NONE;

	if (action == null) {
	    return mask;
	}

	char[] a = action.toCharArray();

	int i = a.length - 1;
	if (i < 0)
	    return mask;

	while (i != -1) {
	    char c;

	    // skip whitespace
	    while ((i!=-1) && ((c = a[i]) == ' ' ||
			       c == '\r' ||
			       c == '\n' ||
			       c == '\f' ||
			       c == '\t'))
		i--;

	    // check for the known strings
	    int matchlen;

	    if (i >= 6 && (a[i-6] == 'c' || a[i-6] == 'C') &&
			  (a[i-5] == 'o' || a[i-5] == 'O') &&
			  (a[i-4] == 'n' || a[i-4] == 'N') &&
			  (a[i-3] == 'n' || a[i-3] == 'N') &&
			  (a[i-2] == 'e' || a[i-2] == 'E') &&
			  (a[i-1] == 'c' || a[i-1] == 'C') &&
			  (a[i] == 't' || a[i] == 'T'))
	    {
		matchlen = 7;
		mask |= CONNECT;

	    } else if (i >= 6 && (a[i-6] == 'r' || a[i-6] == 'R') &&
				 (a[i-5] == 'e' || a[i-5] == 'E') &&
				 (a[i-4] == 's' || a[i-4] == 'S') &&
				 (a[i-3] == 'o' || a[i-3] == 'O') &&
				 (a[i-2] == 'l' || a[i-2] == 'L') &&
				 (a[i-1] == 'v' || a[i-1] == 'V') &&
				 (a[i] == 'e' || a[i] == 'E'))
	    {
		matchlen = 7;
		mask |= RESOLVE;

	    } else if (i >= 5 && (a[i-5] == 'l' || a[i-5] == 'L') &&
				 (a[i-4] == 'i' || a[i-4] == 'I') &&
				 (a[i-3] == 's' || a[i-3] == 'S') &&
				 (a[i-2] == 't' || a[i-2] == 'T') &&
				 (a[i-1] == 'e' || a[i-1] == 'E') &&
				 (a[i] == 'n' || a[i] == 'N'))
	    {
		matchlen = 6;
		mask |= LISTEN;

	    } else if (i >= 5 && (a[i-5] == 'a' || a[i-5] == 'A') &&
				 (a[i-4] == 'c' || a[i-4] == 'C') &&
				 (a[i-3] == 'c' || a[i-3] == 'C') &&
				 (a[i-2] == 'e' || a[i-2] == 'E') &&
				 (a[i-1] == 'p' || a[i-1] == 'P') &&
				 (a[i] == 't' || a[i] == 'T'))
	    {
		matchlen = 6;
		mask |= ACCEPT;

	    } else {
		// parse error
		throw new IllegalArgumentException(
			"invalid permission: " + action);
	    }

	    // make sure we didn't just match the tail of a word
	    // like "ackbarfaccept".  Also, skip to the comma.
	    boolean seencomma = false;
	    while (i >= matchlen && !seencomma) {
		switch(a[i-matchlen]) {
		case ',':
		    seencomma = true;
		    /*FALLTHROUGH*/
		case ' ': case '\r': case '\n':
		case '\f': case '\t':
		    break;
		default:
		    throw new IllegalArgumentException(
			    "invalid permission: " + action);
		}
		i--;
	    }

	    // point i at the location of the comma minus one (or -1).
	    i -= matchlen;
	}

	return mask;
    }

    /**
     * attempt to get the fully qualified domain name
     *
     */
    void getCanonName()
	throws UnknownHostException
    {
	if (cname != null || invalid) return;

	// attempt to get the canonical name

	try { 
	    // first get the IP addresses if we don't have them yet
	    // this is because we need the IP address to then get 
	    // FQDN.
	    if (addresses == null) {
		getIP();
	    }

	    // we have to do this check, otherwise we might not
	    // get the fully qualified domain name
	    if (init_with_ip) {
		cname = addresses[0].getHostName(false).toLowerCase();
	    } else {
	     cname = InetAddress.getByName(addresses[0].getHostAddress()).
                                              getHostName(false).toLowerCase();
	    }
	} catch (UnknownHostException uhe) {
	    invalid = true;
	    throw uhe;
	}
    }

    /**
     * get IP addresses. Sets invalid to true if we can't get them.
     *
     */
    void getIP()
	throws UnknownHostException 
    {
	if (addresses != null || wildcard || invalid) return;

	try { 
	    // now get all the IP addresses
	    String host;
	    int i = getName().indexOf(":");
	    if (i == -1)
		host = getName();
	    else {
		host = getName().substring(0,i);
	    }

	    addresses = 
		new InetAddress[] {InetAddress.getAllByName0(host, false)[0]};

	} catch (UnknownHostException uhe) {
	    invalid = true;
	    throw uhe;
	}
    }

    /**
     * Checks if this socket permission object "implies" the 
     * specified permission.
     * <P>
     * More specifically, this method first ensures that all of the following
     * are true (and returns false if any of them are not):<p>
     * <ul>
     * <li> <i>p</i> is an instanceof SocketPermission,<p>
     * <li> <i>p</i>'s actions are a proper subset of this
     * object's actions, and<p>
     * <li> <i>p</i>'s port range is included in this port range.<p>
     * </ul>
     * 
     * Then <code>implies</code> checks each of the following, in order,
     * and for each returns true if the stated condition is true:<p>
     * <ul>
     * <li> If this object was initialized with a single IP address and one of <i>p</i>'s 
     * IP addresses is equal to this object's IP address.<p>
     * <li>If this object is a wildcard domain (such as *.sun.com), and
     * <i>p</i>'s canonical name (the name without any preceding *)
     * ends with this object's canonical host name. For example, *.sun.com
     * implies *.eng.sun.com..<p>
     * <li>If this object was not initialized with a single IP address, and one of this
     * object's IP addresses equals one of <i>p</i>'s IP addresses.<p>
     * <li>If this canonical name equals <i>p</i>'s canonical name.<p>
     * </ul>
     * 
     * If none of the above are true, <code>implies</code> returns false.
     * @param p the permission to check against.
     *
     * @return true if the specified permission is implied by this object,
     * false if not.  
     */

    public boolean implies(Permission p) {
	int i,j;

	if (!(p instanceof SocketPermission))
	    return false;

	SocketPermission that = (SocketPermission) p;

	return ((this.mask & that.mask) == that.mask) && 
	                                impliesIgnoreMask(that);
    }

    /**
     * Checks if the incoming Permission's action are a proper subset of
     * the this object's actions.
     * <P>
     * Check, in the following order:
     * <ul>
     * <li> Checks that "p" is an instanceof a SocketPermission
     * <li> Checks that "p"'s actions are a proper subset of the
     * current object's actions.
     * <li> Checks that "p"'s port range is included in this port range
     * <li> If this object was initialized with an IP address, checks that 
     *      one of "p"'s IP addresses is equal to this object's IP address.
     * <li> If either object is a wildcard domain (i.e., "*.sun.com"),
     *      attempt to match based on the wildcard.
     * <li> If this object was not initialized with an IP address, attempt
     *      to find a match based on the IP addresses in both objects.
     * <li> Attempt to match on the canonical hostnames of both objects.
     * </ul>
     * @param p the incoming permission request
     *
     * @return true if "permission" is a proper subset of the current object,
     * false if not.  
     */

    boolean impliesIgnoreMask(SocketPermission that) {
	int i,j;

	if ((that.mask & RESOLVE) != that.mask) {
	    // check port range
	    if ((that.portrange[0] < this.portrange[0]) ||
		    (that.portrange[1] > this.portrange[1])) {
		    return false;
	    }
	}

	// allow a "*" wildcard to always match anything
	if (this.wildcard && this.getName().equals("*"))
	    return true;

	// return if either one of these NetPerm objects are invalid...
	if (this.invalid || that.invalid) {
	    return (trustProxy ? inProxyWeTrust(that) : false);
	}


	try {
	    if (this.init_with_ip) { // we only check IP addresses
		if (that.wildcard) 
		    return false;

		if (that.init_with_ip) {
		    return (this.addresses[0].equals(that.addresses[0]));
		} else {
		    if (that.addresses == null) {
			that.getIP();
		    }
		    for (i=0; i < that.addresses.length; i++) {
			if (this.addresses[0].equals(that.addresses[i]))
			    return true;
		    }
		}
		// since "this" was initialized with an IP address, we
		// don't check any other cases
		return false;
	    }

	    // check and see if we have any wildcards...
	    if (this.wildcard || that.wildcard) {
		// if they are both wildcards, return true iff
		// that's cname ends with this cname (i.e., *.sun.com
		// implies *.eng.sun.com)
		if (this.wildcard && that.wildcard)
		    return (that.cname.endsWith(this.cname));

		// a non-wildcard can't imply a wildcard
		if (that.wildcard)
		    return false;

		// this is a wildcard, lets see if that's cname ends with
		// it...
		if (that.cname == null) {
		    that.getCanonName();
		}
		return (that.cname.endsWith(this.cname));
	    }

	    // comapare IP addresses
	    if (this.addresses == null) {
		this.getIP();
	    }

	    if (that.addresses == null) {
		that.getIP();
	    }

	    for (j = 0; j < this.addresses.length; j++) {
		for (i=0; i < that.addresses.length; i++) {
		    if (this.addresses[j].equals(that.addresses[i]))
			return true;
		}
	    }

	    // XXX: if all else fails, compare hostnames?
	    // Do we really want this?
	    if (this.cname == null) {
		this.getCanonName();
	    }

	    if (that.cname == null) {
		that.getCanonName();
	    }

	    return (this.cname.equalsIgnoreCase(that.cname));

	} catch (UnknownHostException uhe) {
	    if (trustProxy)
		return inProxyWeTrust(that);
	}

	// make sure the first thing that is done here is to return
	// false. If not, uncomment the return false in the above catch.

	return false; 
    }

    private boolean inProxyWeTrust(SocketPermission that) {
	// if we trust the proxy, we see if the original names/IPs passed
	// in were equal.

	String thisHost = getName();
	String thatHost = that.getName();

	int sep = thisHost.indexOf(':');
	if (sep != -1)
	    thisHost = thisHost.substring(0, sep);

	sep = thatHost.indexOf(':');
	if (sep != -1)
	    thatHost = thatHost.substring(0, sep);

	if (thisHost == null) 
	    return false;
	else 
	    return thisHost.equalsIgnoreCase(thatHost);

    }
    /**
     * Checks two SocketPermission objects for equality. 
     * <P>
     * @param obj the object to test for equality with this object.
     * 
     * @return true if <i>obj</i> is a SocketPermission, and has the same hostname,
     *  port range, and
     *  actions as this SocketPermission object.
     */
    public boolean equals(Object obj) {
	if (obj == this)
	    return true;

	if (! (obj instanceof SocketPermission))
	    return false;

	SocketPermission that = (SocketPermission) obj;

	//this is (overly?) complex!!!

	// check the mask first
	if (this.mask != that.mask) return false;

	// now check the port range...
	if ((this.portrange[0] != that.portrange[0]) ||
	    (this.portrange[1] != that.portrange[1])) {
	    return false;
	}

	// short cut. This catches:
	//  "crypto" equal to "crypto", or
	// "1.2.3.4" equal to "1.2.3.4.", or 
	//  "*.edu" equal to "*.edu", but it 
	//  does not catch "crypto" equal to
	// "crypto.eng.sun.com".

	if (this.getName().equalsIgnoreCase(that.getName())) {
	    return true;
	}

	// we now attempt to get the Canonical (FQDN) name and
	// compare that. If this fails, about all we can do is return
	// false.

	try {
	    this.getCanonName();
	    that.getCanonName();
	} catch (UnknownHostException uhe) {
	    return false;
	}

	if (this.invalid || that.invalid) 
	    return false;

	if (this.cname != null) {
	    return this.cname.equalsIgnoreCase(that.cname);
	}

	return false;
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return a hash code value for this object.
     */

    public int hashCode() {
	/*
	 * If this SocketPermission was initialized with an IP address
	 * or a wildcard, use getName().hashCode(), otherwise use
	 * the hashCode() of the host name returned from 
	 * java.net.InetAddress.getHostName method.
	 */

	if (init_with_ip || wildcard) {
	    return this.getName().hashCode();
	}

	try {
	    getCanonName();
	} catch (UnknownHostException uhe) {	    

	}

	if (invalid || cname == null)
	    return this.getName().hashCode();
	else
	    return this.cname.hashCode();
    }

    /**
     * Return the current action mask.
     *
     * @return the actions mask.
     */

    int getMask() {
	return mask;
    }

    /**
     * Returns the "canonical string representation" of the actions in the
     * specified mask.
     * Always returns present actions in the following order: 
     * connect, listen, accept, resolve.  
     *
     * @param mask a specific integer action mask to translate into a string
     * @return the canonical string representation of the actions
     */
    private static String getActions(int mask)
    {
	StringBuffer sb = new StringBuffer();
        boolean comma = false;

	if ((mask & CONNECT) == CONNECT) {
	    comma = true;
	    sb.append("connect");
	}

	if ((mask & LISTEN) == LISTEN) {
	    if (comma) sb.append(',');
    	    else comma = true;
	    sb.append("listen");
	}

	if ((mask & ACCEPT) == ACCEPT) {
	    if (comma) sb.append(',');
    	    else comma = true;
	    sb.append("accept");
	}


	if ((mask & RESOLVE) == RESOLVE) {
	    if (comma) sb.append(',');
    	    else comma = true;
	    sb.append("resolve");
	}

	return sb.toString();
    }

    /**
     * Returns the canonical string representation of the actions.
     * Always returns present actions in the following order: 
     * connect, listen, accept, resolve.  
     *
     * @return the canonical string representation of the actions.
     */
    public String getActions()
    {
	if (actions == null)
	    actions = getActions(this.mask);

	return actions;
    }

    /**
     * Returns a new PermissionCollection object for storing SocketPermission 
     * objects.
     * <p>
     * SocketPermission objects must be stored in a manner that allows them 
     * to be inserted into the collection in any order, but that also enables the 
     * PermissionCollection <code>implies</code>
     * method to be implemented in an efficient (and consistent) manner.
     *
     * @return a new PermissionCollection object suitable for storing SocketPermissions.
     */

    public PermissionCollection newPermissionCollection() {
	return new SocketPermissionCollection();
    }

    /**
     * WriteObject is called to save the state of the SocketPermission 
     * to a stream. The actions are serialized, and the superclass
     * takes care of the name.
     */
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
	// Write out the actions. The superclass takes care of the name
	// call getActions to make sure actions field is initialized
	if (actions == null)
	    getActions();
	s.defaultWriteObject();
    }

    /**
     * readObject is called to restore the state of the SocketPermission from
     * a stream.
     */
    private synchronized void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
	// Read in the action, then initialize the rest
	s.defaultReadObject();
	init(getName(),getMask(actions));
    }

    /*
    public String toString() 
    {
	StringBuffer s = new StringBuffer(super.toString() + "\n" +
	    "cname = " + cname + "\n" +
	    "wildcard = " + wildcard + "\n" +
	    "invalid = " + invalid + "\n" +
    	    "portrange = " + portrange[0] + "," + portrange[1] + "\n");
	if (addresses != null) for (int i=0; i<addresses.length; i++) {
	    s.append( addresses[i].getHostAddress());
	    s.append("\n");
	} else {
	    s.append("(no addresses)\n");
	}

	return s.toString();
    }

    public static void main(String args[]) throws Exception {
	SocketPermission this_ = new SocketPermission(args[0], "connect");
	SocketPermission that_ = new SocketPermission(args[1], "connect");
	System.out.println("-----\n");
	System.out.println("this.implies(that) = " + this_.implies(that_));
	System.out.println("-----\n");
	System.out.println("this = "+this_);
	System.out.println("-----\n");
	System.out.println("that = "+that_);
	System.out.println("-----\n");

	SocketPermissionCollection nps = new SocketPermissionCollection();
	nps.add(this_);
	nps.add(new SocketPermission("www-leland.stanford.edu","connect"));
	nps.add(new SocketPermission("www-sun.com","connect"));
	System.out.println("nps.implies(that) = " + nps.implies(that_));
	System.out.println("-----\n");
    }
    */
}

/**

if (init'd with IP, key is IP as string)
if wildcard, its the wild card
else its the cname?

 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 *
 * @version 1.38 02/06/02
 *
 * @author Roland Schemers
 *
 * @serial include
 */

final class SocketPermissionCollection extends PermissionCollection 
implements Serializable
{
    /**
     * The SocketPermissions for this set.
     */

    private Vector permissions;

    /**
     * Create an empty SocketPermissions object.
     *
     */

    public SocketPermissionCollection() {
	permissions = new Vector();
    }

    /**
     * Adds a permission to the SocketPermissions. The key for the hash is
     * the name in the case of wildcards, or all the IP addresses.
     *
     * @param permission the Permission object to add.
     *
     * @exception IllegalArgumentException - if the permission is not a
     *                                       SocketPermission
     *
     * @exception SecurityException - if this SocketPermissionCollection object
     *                                has been marked readonly
     */

    public void add(Permission permission)
    {
	if (! (permission instanceof SocketPermission))
	    throw new IllegalArgumentException("invalid permission: "+
					       permission);
	if (isReadOnly())
	    throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");

	// optimization to ensure perms most likely to be tested
	// show up early (4301064)
	permissions.add(0, permission);
    }

    /**
     * Check and see if this collection of permissions implies the permissions 
     * expressed in "permission".
     *
     * @param p the Permission object to compare
     *
     * @return true if "permission" is a proper subset of a permission in 
     * the collection, false if not.
     */

    public boolean implies(Permission permission) 
    {
	if (! (permission instanceof SocketPermission))
   		return false;

	SocketPermission np = (SocketPermission) permission;

	int desired = np.getMask();
	int effective = 0;
	int needed = desired;

	Enumeration e = permissions.elements();
	//System.out.println("implies "+np);
	while (e.hasMoreElements()) {
	    SocketPermission x = (SocketPermission) e.nextElement();
	    //System.out.println("  trying "+x);
	    if (((needed & x.getMask()) != 0) && x.impliesIgnoreMask(np)) {
		effective |=  x.getMask();
		if ((effective & desired) == desired)
		    return true;
		needed = (desired ^ effective);
	    }
	}
	return false;
    }

    /**
     * Returns an enumeration of all the SocketPermission objects in the 
     * container.
     *
     * @return an enumeration of all the SocketPermission objects.
     */

    public Enumeration elements()
    {
	return permissions.elements();
    }
}
