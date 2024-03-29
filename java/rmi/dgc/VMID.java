/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.rmi.dgc;

import java.io.*;
import java.net.*;
import java.rmi.server.UID;
import java.security.*;

/**
 * A VMID is a identifier that is unique across all Java virtual
 * machines.  VMIDs are used by the distributed garbage collector
 * to identify client VMs.
 *
 * @version	1.17, 02/06/02
 * @author	Ann Wollrath
 * @author	Peter Jones
 */
public final class VMID implements java.io.Serializable {

    /** array of bytes uniquely identifying this host */
    private static byte[] localAddr = computeAddressHash();
    
    /**
     * @serial array of bytes uniquely identifying host created on
     */
    private byte[] addr;

    /**
     * @serial unique identifier with respect to host created on
     */
    private UID uid;

    /** indicate compatibility with JDK 1.1.x version of class */
    private static final long serialVersionUID = -538642295484486218L;

    /**
     * Create a new VMID.  Each new VMID returned from this constructor
     * is unique for all Java virtual machines under the following
     * conditions: a) the conditions for uniqueness for objects of
     * the class <code>java.rmi.server.UID</code> are satisfied, and b) an
     * address can be obtained for this host that is unique and constant
     * for the lifetime of this object.  <p>
     */
    public VMID() {
	addr = localAddr;
	uid = new UID();
    }

    /**
     * Return true if an accurate address can be determined for this
     * host.  If false, reliable VMID cannot be generated from this host
     * @return true if host address can be determined, false otherwise
     * @deprecated
     */
    public static boolean isUnique() {
	return true;
    }

    /**
     * Compute hash code for this VMID.
     */
    public int hashCode() {
	return uid.hashCode();
    }

    /**
     * Compare this VMID to another, and return true if they are the
     * same identifier.
     */
    public boolean equals(Object obj) {
	if (obj instanceof VMID) {
	    VMID vmid = (VMID) obj;
	    if (!uid.equals(vmid.uid))
		return false;
	    if ((addr == null) ^ (vmid.addr == null))
		return false;
	    if (addr != null) {
		if (addr.length != vmid.addr.length)
		    return false;
		for (int i = 0; i < addr.length; ++ i)
		    if (addr[i] != vmid.addr[i])
			return false;
	    }
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Return string representation of this VMID.
     */
    public String toString() {
	StringBuffer result = new StringBuffer();
	if (addr != null)
	    for (int i = 0; i < addr.length; ++ i) {
		int x = (int) (addr[i] & 0xFF);
		result.append((x < 0x10 ? "0" : "") +
			      Integer.toString(x, 16));
	    }
	result.append(':');
	result.append(uid.toString());
	return result.toString();
    }
    
    /**
     * Compute the hash an IP address.  The hash is the first 8 bytes
     * of the SHA digest of the IP address.
     */
    private static byte[] computeAddressHash() {

	/*
	 * Get the local host's IP address.
	 */
	byte[] addr = (byte[]) java.security.AccessController.doPrivileged(
	    new PrivilegedAction() {
	    public Object run() {
		try {
		    return InetAddress.getLocalHost().getAddress();
		} catch (Exception e) {
		}
		return new byte[] { 0, 0, 0, 0 };
	    }
	});

	byte[] addrHash;
	final int ADDR_HASH_LENGTH = 8;
	
	try {
	    /*
	     * Calculate message digest of IP address using SHA.
	     */
	    MessageDigest md = MessageDigest.getInstance("SHA");
	    ByteArrayOutputStream sink = new ByteArrayOutputStream(64);
	    DataOutputStream out = new DataOutputStream(
		new DigestOutputStream(sink, md));
	    out.write(addr, 0, addr.length);
	    out.flush();
	    
	    byte digest[] = md.digest();
	    int hashlength = Math.min(ADDR_HASH_LENGTH, digest.length);
	    addrHash = new byte[hashlength];
	    System.arraycopy(digest, 0, addrHash, 0, hashlength);

	} catch (IOException ignore) {
	    /* can't happen, but be deterministic anyway. */
	    addrHash = new byte[0];
	} catch (NoSuchAlgorithmException complain) {
	    throw new InternalError(complain.toString());
	}
	return addrHash;
    }
}
