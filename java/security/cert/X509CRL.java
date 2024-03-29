/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package java.security.cert;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.Principal;
import java.security.PublicKey;

import java.math.BigInteger;
import java.util.Date;
import java.util.Set;

/**
 * <p>
 * Abstract class for an X.509 Certificate Revocation List (CRL).
 * A CRL is a time-stamped list identifying revoked certificates.
 * It is signed by a Certificate Authority (CA) and made freely
 * available in a public repository.  
 * 
 * <p>Each revoked certificate is
 * identified in a CRL by its certificate serial number. When a
 * certificate-using system uses a certificate (e.g., for verifying a
 * remote user's digital signature), that system not only checks the
 * certificate signature and validity but also acquires a suitably-
 * recent CRL and checks that the certificate serial number is not on
 * that CRL.  The meaning of "suitably-recent" may vary with local
 * policy, but it usually means the most recently-issued CRL.  A CA
 * issues a new CRL on a regular periodic basis (e.g., hourly, daily, or
 * weekly).  Entries are added to CRLs as revocations occur, and an
 * entry may be removed when the certificate expiration date is reached.
 * <p>
 * The X.509 v2 CRL format is described below in ASN.1:
 * <pre>
 * CertificateList  ::=  SEQUENCE  {
 *     tbsCertList          TBSCertList,
 *     signatureAlgorithm   AlgorithmIdentifier,
 *     signature            BIT STRING  }
 * </pre>
 * <p>
 * More information can be found in RFC 2459,
 * "Internet X.509 Public Key Infrastructure Certificate and CRL
 * Profile" at <A HREF="http://www.ietf.org/rfc/rfc2459.txt">
 * http://www.ietf.org/rfc/rfc2459.txt </A>.    
 * <p>
 * The ASN.1 definition of <code>tbsCertList</code> is:
 * <pre>
 * TBSCertList  ::=  SEQUENCE  {
 *     version                 Version OPTIONAL,
 *                             -- if present, must be v2
 *     signature               AlgorithmIdentifier,
 *     issuer                  Name,
 *     thisUpdate              ChoiceOfTime,
 *     nextUpdate              ChoiceOfTime OPTIONAL,
 *     revokedCertificates     SEQUENCE OF SEQUENCE  {
 *         userCertificate         CertificateSerialNumber,
 *         revocationDate          ChoiceOfTime,
 *         crlEntryExtensions      Extensions OPTIONAL
 *                                 -- if present, must be v2
 *         }  OPTIONAL,
 *     crlExtensions           [0]  EXPLICIT Extensions OPTIONAL
 *                                  -- if present, must be v2
 *     }
 * </pre>
 * <p>
 * CRLs are instantiated using a certificate factory. The following is an
 * example of how to instantiate an X.509 CRL:
 * <pre><code> 
 * InputStream inStream = new FileInputStream("fileName-of-crl");
 * CertificateFactory cf = CertificateFactory.getInstance("X.509");
 * X509CRL crl = (X509CRL)cf.generateCRL(inStream);
 * inStream.close();
 * </code></pre>
 *
 * @author Hemma Prafullchandra
 *
 * @version 1.19
 *
 * @see CRL
 * @see CertificateFactory
 * @see X509Extension
 */

public abstract class X509CRL extends CRL implements X509Extension {

    /**
     * Constructor for X.509 CRLs.
     */
    protected X509CRL() {
	super("X.509");
    }

    /**
     * Compares this CRL for equality with the given 
     * object. If the <code>other</code> object is an 
     * <code>instanceof</code> <code>X509CRL</code>, then
     * its encoded form is retrieved and compared with the
     * encoded form of this CRL.
     * 
     * @param other the object to test for equality with this CRL.
     * 
     * @return true iff the encoded forms of the two CRLs
     * match, false otherwise.
     */  
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof X509CRL))
            return false;
        try {
            byte[] thisCRL = this.getEncoded();
            byte[] otherCRL = ((X509CRL)other).getEncoded();

            if (thisCRL.length != otherCRL.length)
                return false;
            for (int i = 0; i < thisCRL.length; i++)
                 if (thisCRL[i] != otherCRL[i])
                     return false;
            return true;
        } catch (CRLException e) {
	    return false;
        }
    }

    /**
     * Returns a hashcode value for this CRL from its
     * encoded form.
     *
     * @return the hashcode value.
     */  
    public int hashCode() {
        int     retval = 0;
        try {
            byte[] crlData = this.getEncoded();
            for (int i = 1; i < crlData.length; i++) {
                 retval += crlData[i] * i;
            }
            return(retval);
        } catch (CRLException e) {
            return(retval);
        }
    }

    /**
     * Returns the ASN.1 DER-encoded form of this CRL.
     *
     * @return the encoded form of this certificate
     * @exception CRLException if an encoding error occurs.
     */
    public abstract byte[] getEncoded()
        throws CRLException;

    /**
     * Verifies that this CRL was signed using the 
     * private key that corresponds to the given public key.
     *
     * @param key the PublicKey used to carry out the verification.
     *
     * @exception NoSuchAlgorithmException on unsupported signature
     * algorithms.
     * @exception InvalidKeyException on incorrect key.
     * @exception NoSuchProviderException if there's no default provider.
     * @exception SignatureException on signature errors.
     * @exception CRLException on encoding errors.
     */  
    public abstract void verify(PublicKey key)
        throws CRLException,  NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException;

    /**
     * Verifies that this CRL was signed using the 
     * private key that corresponds to the given public key.
     * This method uses the signature verification engine
     * supplied by the given provider.
     *
     * @param key the PublicKey used to carry out the verification.
     * @param sigProvider the name of the signature provider.
     * 
     * @exception NoSuchAlgorithmException on unsupported signature
     * algorithms.
     * @exception InvalidKeyException on incorrect key.
     * @exception NoSuchProviderException on incorrect provider.
     * @exception SignatureException on signature errors.
     * @exception CRLException on encoding errors.
     */  
    public abstract void verify(PublicKey key, String sigProvider)
        throws CRLException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException;

    /**
     * Gets the <code>version</code> (version number) value from the CRL.
     * The ASN.1 definition for this is:
     * <pre>
     * version    Version OPTIONAL,
     *             -- if present, must be v2<p>
     * Version  ::=  INTEGER  {  v1(0), v2(1), v3(2)  }
     *             -- v3 does not apply to CRLs but appears for consistency
     *             -- with definition of Version for certs
     * </pre>
     *
     * @return the version number, i.e. 1 or 2.
     */
    public abstract int getVersion();

    /**
     * Gets the <code>issuer</code> (issuer distinguished name) value from 
     * the CRL. The issuer name identifies the entity that signed (and
     * issued) the CRL. 
     * 
     * <p>The issuer name field contains an
     * X.500 distinguished name (DN).
     * The ASN.1 definition for this is:
     * <pre>
     * issuer    Name
     *
     * Name ::= CHOICE { RDNSequence }
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     * RelativeDistinguishedName ::=
     *     SET OF AttributeValueAssertion
     *
     * AttributeValueAssertion ::= SEQUENCE {
     *                               AttributeType,
     *                               AttributeValue }
     * AttributeType ::= OBJECT IDENTIFIER
     * AttributeValue ::= ANY
     * </pre>
     * The <code>Name</code> describes a hierarchical name composed of
     * attributes,
     * such as country name, and corresponding values, such as US.
     * The type of the <code>AttributeValue</code> component is determined by
     * the <code>AttributeType</code>; in general it will be a 
     * <code>directoryString</code>. A <code>directoryString</code> is usually 
     * one of <code>PrintableString</code>,
     * <code>TeletexString</code> or <code>UniversalString</code>.
     * 
     * @return a Principal whose name is the issuer distinguished name.
     */
    public abstract Principal getIssuerDN();

    /**
     * Gets the <code>thisUpdate</code> date from the CRL.
     * The ASN.1 definition for this is:
     * <pre>
     * thisUpdate   ChoiceOfTime
     * ChoiceOfTime ::= CHOICE {
     *     utcTime        UTCTime,
     *     generalTime    GeneralizedTime }
     * </pre>
     *
     * @return the <code>thisUpdate</code> date from the CRL.
     */
    public abstract Date getThisUpdate();

    /**
     * Gets the <code>nextUpdate</code> date from the CRL.
     *
     * @return the <code>nextUpdate</code> date from the CRL, or null if
     * not present.
     */
    public abstract Date getNextUpdate();

    /**
     * Gets the CRL entry, if any, with the given certificate serialNumber.
     *
     * @param serialNumber the serial number of the certificate for which a CRL entry
     * is to be looked up
     * @return the entry with the given serial number, or null if no such entry
     * exists in this CRL.
     * @see X509CRLEntry
     */
    public abstract X509CRLEntry
        getRevokedCertificate(BigInteger serialNumber);
  
    /**
     * Gets all the entries from this CRL.
     * This returns a Set of X509CRLEntry objects.
     *
     * @return all the entries or null if there are none present.
     * @see X509CRLEntry
     */
    public abstract Set getRevokedCertificates();
  
    /**
     * Gets the DER-encoded CRL information, the
     * <code>tbsCertList</code> from this CRL.
     * This can be used to verify the signature independently.
     *
     * @return the DER-encoded CRL information.
     * @exception CRLException if an encoding error occurs.
     */
    public abstract byte[] getTBSCertList() throws CRLException;

    /**
     * Gets the <code>signature</code> value (the raw signature bits) from 
     * the CRL.
     * The ASN.1 definition for this is:
     * <pre>
     * signature     BIT STRING  
     * </pre>
     *
     * @return the signature.
     */
    public abstract byte[] getSignature();

    /**
     * Gets the signature algorithm name for the CRL
     * signature algorithm. An example is the string "SHA-1/DSA".
     * The ASN.1 definition for this is:
     * <pre>
     * signatureAlgorithm   AlgorithmIdentifier<p>
     * AlgorithmIdentifier  ::=  SEQUENCE  {
     *     algorithm               OBJECT IDENTIFIER,
     *     parameters              ANY DEFINED BY algorithm OPTIONAL  }
     *                             -- contains a value of the type
     *                             -- registered for use with the
     *                             -- algorithm object identifier value
     * </pre>
     * 
     * <p>The algorithm name is determined from the <code>algorithm</code>
     * OID string.
     *
     * @return the signature algorithm name.
     */
    public abstract String getSigAlgName();

    /**
     * Gets the signature algorithm OID string from the CRL.
     * An OID is represented by a set of positive whole numbers separated
     * by periods.
     * For example, the string "1.2.840.10040.4.3" identifies the SHA-1
     * with DSA signature algorithm, as per RFC 2459.
     * 
     * <p>See {@link getSigAlgName()#getSigAlgName} for 
     * relevant ASN.1 definitions.
     *
     * @return the signature algorithm OID string.
     */
    public abstract String getSigAlgOID();

    /**
     * Gets the DER-encoded signature algorithm parameters from this
     * CRL's signature algorithm. In most cases, the signature
     * algorithm parameters are null; the parameters are usually
     * supplied with the public key.
     * If access to individual parameter values is needed then use
     * {@link java.security.AlgorithmParameters AlgorithmParameters}
     * and instantiate with the name returned by
     * {@link getSigAlgName()#getSigAlgName}.
     * 
     * <p>See {@link getSigAlgName()#getSigAlgName} for 
     * relevant ASN.1 definitions.
     *
     * @return the DER-encoded signature algorithm parameters, or
     *         null if no parameters are present.
     */
    public abstract byte[] getSigAlgParams();
}
