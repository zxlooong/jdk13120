/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.security;

import java.util.*;
import java.lang.*;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * This MessageDigest class provides applications the functionality of a
 * message digest algorithm, such as MD5 or SHA.
 * Message digests are secure one-way hash functions that take arbitrary-sized
 * data and output a fixed-length hash value.
 *
 * <p>A MessageDigest object starts out initialized. The data is 
 * processed through it using the {@link #update(byte) update}
 * methods. At any point {@link #reset() reset} can be called
 * to reset the digest. Once all the data to be updated has been
 * updated, one of the {@link #digest() digest} methods should 
 * be called to complete the hash computation.
 *
 * <p>The <code>digest</code> method can be called once for a given number 
 * of updates. After <code>digest</code> has been called, the MessageDigest
 * object is reset to its initialized state.
 *
 * <p>Implementations are free to implement the Cloneable interface.
 * Client applications can test cloneability by attempting cloning
 * and catching the CloneNotSupportedException: <p>    
 *
* <pre>
* MessageDigest md = MessageDigest.getInstance("SHA");
*
* try {
*     md.update(toChapter1);
*     MessageDigest tc1 = md.clone();
*     byte[] toChapter1Digest = tc1.digest();
*     md.update(toChapter2);
*     ...etc.
* } catch (CloneNotSupportedException cnse) {
*     throw new DigestException("couldn't make digest of partial content");
* }
* </pre>
 *
 * <p>Note that if a given implementation is not cloneable, it is
 * still possible to compute intermediate digests by instantiating
 * several instances, if the number of digests is known in advance.
 *
 * <p>Note that this class is abstract and extends from
 * <code>MessageDigestSpi</code> for historical reasons.
 * Application developers should only take notice of the methods defined in
 * this <code>MessageDigest</code> class; all the methods in
 * the superclass are intended for cryptographic service providers who wish to
 * supply their own implementations of message digest algorithms.
 *
 * @author Benjamin Renaud 
 *
 * @version 1.72, 02/06/02
 *
 * @see DigestInputStream
 * @see DigestOutputStream
 */

public abstract class MessageDigest extends MessageDigestSpi {

    private String algorithm;

    // The state of this digest
    private static final int INITIAL = 0;
    private static final int IN_PROGRESS = 1;
    private int state = INITIAL;

    // The provider
    private Provider provider;

    /**
     * Creates a message digest with the specified algorithm name.
     * 
     * @param algorithm the standard name of the digest algorithm. 
     * See Appendix A in the <a href=
     * "../../../guide/security/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a> 
     * for information about standard algorithm names.
     */
    protected MessageDigest(String algorithm) {
	this.algorithm = algorithm;
    }

    /**
     * Generates a MessageDigest object that implements the specified digest
     * algorithm. If the default provider package
     * provides an implementation of the requested digest algorithm,
     * an instance of MessageDigest containing that implementation is returned.
     * If the algorithm is not available in the default 
     * package, other packages are searched.
     *
     * @param algorithm the name of the algorithm requested. 
     * See Appendix A in the <a href=
     * "../../../guide/security/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a> 
     * for information about standard algorithm names.
     *
     * @return a Message Digest object implementing the specified
     * algorithm.
     *
     * @exception NoSuchAlgorithmException if the algorithm is
     * not available in the caller's environment.  
     */
    public static MessageDigest getInstance(String algorithm) 
    throws NoSuchAlgorithmException { 
	try {
	    Object[] objs = Security.getImpl(algorithm, "MessageDigest", null);
	    if (objs[0] instanceof MessageDigest) {
		MessageDigest md = (MessageDigest)objs[0];
		md.provider = (Provider)objs[1];
		return md;
	    } else {
		MessageDigest delegate =
		    new Delegate((MessageDigestSpi)objs[0], algorithm);
		delegate.provider = (Provider)objs[1];
		return delegate;
	    }
	} catch(NoSuchProviderException e) {
	    throw new NoSuchAlgorithmException(algorithm + " not found");
	}
    }

    /**
     * Generates a MessageDigest object implementing the specified
     * algorithm, as supplied from the specified provider, if such an 
     * algorithm is available from the provider.
     *
     * @param algorithm the name of the algorithm requested. 
     * See Appendix A in the <a href=
     * "../../../guide/security/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a> 
     * for information about standard algorithm names.
     *
     * @param provider the name of the provider.
     *
     * @return a Message Digest object implementing the specified
     * algorithm.
     *
     * @exception NoSuchAlgorithmException if the algorithm is
     * not available in the package supplied by the requested
     * provider.
     *
     * @exception NoSuchProviderException if the provider is not
     * available in the environment. 
     * 
     * @see Provider 
     */
    public static MessageDigest getInstance(String algorithm, String provider)
	throws NoSuchAlgorithmException, NoSuchProviderException
    {
	if (provider == null || provider.length() == 0)
	    throw new IllegalArgumentException("missing provider");
	Object[] objs = Security.getImpl(algorithm, "MessageDigest", provider);
	if (objs[0] instanceof MessageDigest) {
	    MessageDigest md = (MessageDigest)objs[0];
	    md.provider = (Provider)objs[1];
	    return md;
	} else {
	    MessageDigest delegate =
		new Delegate((MessageDigestSpi)objs[0], algorithm);
	    delegate.provider = (Provider)objs[1];
	    return delegate;
	}
    }

    /** 
     * Returns the provider of this message digest object.
     * 
     * @return the provider of this message digest object
     */
    public final Provider getProvider() {
	return this.provider;
    }

    /**
     * Updates the digest using the specified byte.    
     * 
     * @param input the byte with which to update the digest.
     */
    public void update(byte input) {
	engineUpdate(input);
	state = IN_PROGRESS;
    }

    /**
     * Updates the digest using the specified array of bytes, starting
     * at the specified offset.
     * 
     * @param input the array of bytes.
     *
     * @param offset the offset to start from in the array of bytes.
     *
     * @param len the number of bytes to use, starting at 
     * <code>offset</code>.  
     */
    public void update(byte[] input, int offset, int len) {
	if (input == null) {
	    throw new IllegalArgumentException("No input buffer given");
	}
	if (input.length - offset < len) {
	    throw new IllegalArgumentException("Input buffer too short");
	}
	engineUpdate(input, offset, len);
	state = IN_PROGRESS;
    }

    /**
     * Updates the digest using the specified array of bytes.
     * 
     * @param input the array of bytes.
     */
    public void update(byte[] input) {
	engineUpdate(input, 0, input.length);
	state = IN_PROGRESS;
    }

    /**
     * Completes the hash computation by performing final operations
     * such as padding. The digest is reset after this call is made.
     *
     * @return the array of bytes for the resulting hash value.  
     */
    public byte[] digest() {
	/* Resetting is the responsibility of implementors. */
	byte[] result = engineDigest();
	state = INITIAL;
	return result;
    }

    /**
     * Completes the hash computation by performing final operations
     * such as padding. The digest is reset after this call is made.
     *
     * @param buf output buffer for the computed digest
     *
     * @param offset offset into the output buffer to begin storing the digest
     *
     * @param len number of bytes within buf allotted for the digest
     *
     * @return the number of bytes placed into <code>buf</code>
     * 
     * @exception DigestException if an error occurs.
     */
    public int digest(byte[] buf, int offset, int len) throws DigestException {
	if (buf == null) {
	    throw new IllegalArgumentException("No output buffer given");
	}
	if (buf.length - offset < len) {
	    throw new IllegalArgumentException
		("Output buffer too small for specified offset and length");
	}
	int numBytes = engineDigest(buf, offset, len);
	state = INITIAL;
	return numBytes;
    }

    /**
     * Performs a final update on the digest using the specified array 
     * of bytes, then completes the digest computation. That is, this
     * method first calls {@link #update(byte[]) update(input)},
     * passing the <i>input</i> array to the <code>update</code> method,
     * then calls {@link #digest() digest()}.
     *
     * @param input the input to be updated before the digest is
     * completed.
     *
     * @return the array of bytes for the resulting hash value.  
     */
    public byte[] digest(byte[] input) {
	update(input);
	return digest();
    }

    /**
     * Returns a string representation of this message digest object.  
     */
    public String toString() {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	PrintStream p = new PrintStream(baos);
	p.print(algorithm+" Message Digest from "+provider.getName()+", ");
	switch (state) {
	case INITIAL:
	    p.print("<initialized>");
	    break;
	case IN_PROGRESS:
	    p.print("<in progress>");
	    break;
	}
	p.println();
	return (baos.toString());
    }

    /**
     * Compares two digests for equality. Does a simple byte compare.
     * 
     * @param digesta one of the digests to compare.
     * 
     * @param digestb the other digest to compare.    
     *
     * @return true if the digests are equal, false otherwise.
     */
    public static boolean isEqual(byte digesta[], byte digestb[]) {
	if (digesta.length != digestb.length)
	    return false;

	for (int i = 0; i < digesta.length; i++) {
	    if (digesta[i] != digestb[i]) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Resets the digest for further use.
     */
    public void reset() {
	engineReset();
	state = INITIAL;
    }

    /** 
     * Returns a string that identifies the algorithm, independent of
     * implementation details. The name should be a standard
     * Java Security name (such as "SHA", "MD5", and so on). 
     * See Appendix A in the <a href=
     * "../../../guide/security/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a> 
     * for information about standard algorithm names.
     *
     * @return the name of the algorithm
     */
    public final String getAlgorithm() {
	return this.algorithm;
    }

    /** 
     * Returns the length of the digest in bytes, or 0 if this operation is
     * not supported by the provider and the implementation is not cloneable.
     *
     * @return the digest length in bytes, or 0 if this operation is not
     * supported by the provider and the implementation is not cloneable.
     * 
     * @since 1.2
     */
    public final int getDigestLength() {
	int digestLen = engineGetDigestLength();
	if (digestLen == 0) {
	    try {
		MessageDigest md = (MessageDigest)clone();
		byte[] digest = md.digest();
		return digest.length;
	    } catch (CloneNotSupportedException e) {
		return digestLen;
	    }
	}
	return digestLen;
    }

    /**    
     * Returns a clone if the implementation is cloneable.    
     * 
     * @return a clone if the implementation is cloneable.
     *
     * @exception CloneNotSupportedException if this is called on an
     * implementation that does not support <code>Cloneable</code>.
     */
    public Object clone() throws CloneNotSupportedException {
	if (this instanceof Cloneable) {
	    return super.clone();
	} else {
	    throw new CloneNotSupportedException();
	}
    }




    /*
     * The following class allows providers to extend from MessageDigestSpi
     * rather than from MessageDigest. It represents a MessageDigest with an
     * encapsulated, provider-supplied SPI object (of type MessageDigestSpi).
     * If the provider implementation is an instance of MessageDigestSpi,
     * the getInstance() methods above return an instance of this class, with
     * the SPI object encapsulated.
     *
     * Note: All SPI methods from the original MessageDigest class have been
     * moved up the hierarchy into a new class (MessageDigestSpi), which has
     * been interposed in the hierarchy between the API (MessageDigest)
     * and its original parent (Object).
     */

    static class Delegate extends MessageDigest {

	// The provider implementation (delegate)
	private MessageDigestSpi digestSpi;

	// constructor
	public Delegate(MessageDigestSpi digestSpi, String algorithm) {
	    super(algorithm);
	    this.digestSpi = digestSpi;
	}

	/*
	 * Returns a clone if the delegate is cloneable.    
	 * 
	 * @return a clone if the delegate is cloneable.
	 *
	 * @exception CloneNotSupportedException if this is called on a
	 * delegate that does not support <code>Cloneable</code>.
	 */
	public Object clone() throws CloneNotSupportedException {
	    if (digestSpi instanceof Cloneable) {
		MessageDigestSpi digestSpiClone =
		    (MessageDigestSpi)digestSpi.clone();	
		// Because 'algorithm', 'provider', and 'state' are private
		// members of our supertype, we must perform a cast to
		// access them.
		MessageDigest that =
		    new Delegate(digestSpiClone,
				 ((MessageDigest)this).algorithm);
		that.provider = ((MessageDigest)this).provider;
		that.state = ((MessageDigest)this).state;
		return that;
	    } else {
		throw new CloneNotSupportedException();
	    }
	}

	protected int engineGetDigestLength() {
	    return digestSpi.engineGetDigestLength();
	}

	protected void engineUpdate(byte input) {
	    digestSpi.engineUpdate(input);
	}

	protected void engineUpdate(byte[] input, int offset, int len) {
	    digestSpi.engineUpdate(input, offset, len);
	}

	protected byte[] engineDigest() {
	    return digestSpi.engineDigest();
	}

	protected int engineDigest(byte[] buf, int offset, int len)
	    throws DigestException {
		return digestSpi.engineDigest(buf, offset, len);
	}

	protected void engineReset() {
	    digestSpi.engineReset();
	}
    }
}
