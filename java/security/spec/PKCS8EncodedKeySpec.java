/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.security.spec;

/**
 * This class represents the ASN.1 encoding of a private key,
 * encoded according to the ASN.1 type <code>PrivateKeyInfo</code>,
 * whose syntax is defined in the PKCS#8 standard, as follows:
 *
 * <pre>
 * PrivateKeyInfo ::= SEQUENCE {
 *   version Version,
 *   privateKeyAlgorithm PrivateKeyAlgorithmIdentifier,
 *   privateKey PrivateKey,
 *   attributes [0] IMPLICIT Attributes OPTIONAL }
 *
 * Version ::= INTEGER
 *
 * PrivateKeyAlgorithmIdentifier ::= AlgorithmIdentifier
 *
 * PrivateKey ::= OCTET STRING
 *
 * Attributes ::= SET OF Attribute
 * </pre>
 *
 * @author Jan Luehe
 *
 * @version 1.15, 02/06/02
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see EncodedKeySpec
 * @see X509EncodedKeySpec
 *
 * @since 1.2
 */

public class PKCS8EncodedKeySpec extends EncodedKeySpec {

    /**
     * Creates a new PKCS8EncodedKeySpec with the given encoded key.
     *
     * @param encodedKey the key, which is assumed to be
     * encoded according to the PKCS #8 standard.
     */
    public PKCS8EncodedKeySpec(byte[] encodedKey) {
	super(encodedKey);
    }

    /**
     * Returns the key bytes, encoded according to the PKCS #8 standard.
     *
     * @return the PKCS #8 encoding of the key.
     */
    public byte[] getEncoded() {
	return super.getEncoded();
    }

    /**
     * Returns the name of the encoding format associated with this
     * key specification.
     *
     * @return the string <code>"PKCS#8"</code>.
     */
    public final String getFormat() {
	return "PKCS#8";
    }
}
