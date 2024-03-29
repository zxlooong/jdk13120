/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.security.cert;

import java.util.Set;

/**
 * Interface for an X.509 extension.
 *
 * <p>The extensions defined for X.509 v3
 * {@link X509Certificate Certificates} and v2
 * {@link X509CRL CRLs} (Certificate Revocation
 * Lists) provide methods
 * for associating additional attributes with users or public keys,
 * for managing the certification hierarchy, and for managing CRL
 * distribution. The X.509 extensions format also allows communities
 * to define private extensions to carry information unique to those
 * communities.
 *
 * <p>Each extension in a certificate/CRL may be designated as
 * critical or non-critical.  A certificate/CRL-using system (an application
 * validating a certificate/CRL) must reject the certificate/CRL if it
 * encounters a critical extension it does not recognize.  A non-critical
 * extension may be ignored if it is not recognized.
 * <p>
 * The ASN.1 definition for this is:
 * <pre>
 * Extensions  ::=  SEQUENCE SIZE (1..MAX) OF Extension
 *
 * Extension  ::=  SEQUENCE  {
 *     extnId        OBJECT IDENTIFIER,
 *     critical      BOOLEAN DEFAULT FALSE,
 *     extnValue     OCTET STRING
 *                   -- contains a DER encoding of a value
 *                   -- of the type registered for use with
 *                   -- the extnId object identifier value
 * }
 * </pre>
 * Since not all extensions are known, the <code>getExtensionValue</code>
 * method returns the DER-encoded OCTET STRING of the
 * extension value (i.e., the <code>extnValue</code>). This can then
 * be handled by a <em>Class</em> that understands the extension.
 *
 * @author Hemma Prafullchandra
 * @version 1.17 02/02/06
 */

public interface X509Extension {

    /**
     * Check if there is a critical extension that is not supported.
     * 
     * @return <tt>true</tt> if a critical extension is found that is
     * not supported, otherwise <tt>false</tt>.
     */
    public boolean hasUnsupportedCriticalExtension();

    /**
     * Gets a Set of the OID strings for the extension(s) marked
     * CRITICAL in the certificate/CRL managed by the object
     * implementing this interface.
     *
     * Here is sample code to get a Set of critical extensions from an
     * X509Certificate and print the OIDs:
     * <pre><code>
     * InputStream inStrm = new FileInputStream("DER-encoded-Cert");
     * CertificateFactory cf = CertificateFactory.getInstance("X.509");
     * X509Certificate cert = (X509Certificate)cf.generateCertificate(inStrm);
     * inStrm.close();<p>
     *
     * Set critSet = cert.getCriticalExtensionOIDs();
     * if (critSet != null && !critSet.isEmpty()) {
     *     System.out.println("Set of critical extensions:");
     *     for (Iterator i = critSet.iterator(); i.hasNext();) {
     *         String oid = (String)i.next();
     *         System.out.println(oid);
     *     }
     * }
     * </code></pre>
     * @return a Set (or an empty Set if none are marked critical) of
     * the extension OID strings for extensions that are marked critical.
     * If there are no extensions present at all, then this method returns
     * null.
     */
    public Set getCriticalExtensionOIDs();

    /**
     * Gets a Set of the OID strings for the extension(s) marked
     * NON-CRITICAL in the certificate/CRL managed by the object
     * implementing this interface.
     *
     * Here is sample code to get a Set of non-critical extensions from an
     * X509CRL revoked certificate entry and print the OIDs:
     * <pre><code>
     * InputStream inStrm = new FileInputStream("DER-encoded-CRL");
     * CertificateFactory cf = CertificateFactory.getInstance("X.509");
     * X509CRL crl = (X509CRL)cf.generateCRL(inStrm);
     * inStrm.close();<p>
     *
     * byte[] certData = &lt;DER-encoded certificate data&gt;
     * ByteArrayInputStream bais = new ByteArrayInputStream(certData);
     * X509Certificate cert = (X509Certificate)cf.generateCertificate(bais);
     * bais.close();
     * X509CRLEntry badCert =
     *              crl.getRevokedCertificate(cert.getSerialNumber());<p>
     *
     * if (badCert != null) {
     *     Set nonCritSet = badCert.getNonCriticalExtensionOIDs();<p>
     *     if (nonCritSet != null)
     *         for (Iterator i = nonCritSet.iterator(); i.hasNext();) {
     *             String oid = (String)i.next();
     *             System.out.println(oid);
     *         }
     * }
     * </code></pre>
     *
     * @return a Set (or an empty Set if none are marked non-critical) of
     * the extension OID strings for extensions that are marked non-critical.
     * If there are no extensions present at all, then this method returns
     * null.
     */
    public Set getNonCriticalExtensionOIDs();

    /**
     * Gets the DER-encoded OCTET string for the extension value
     * (<em>extnValue</em>) identified by the passed-in <code>oid</code>
     * String.
     * The <code>oid</code> string is
     * represented by a set of positive whole numbers separated
     * by periods.
     *
     * <p>For example:<br>
     * <table border=groove>
     * <tr>
     * <th>OID <em>(Object Identifier)</em></th>
     * <th>Extension Name</th></tr>
     * <tr><td>2.5.29.14</td>
     * <td>SubjectKeyIdentifier</td></tr>
     * <tr><td>2.5.29.15</td>
     * <td>KeyUsage</td></tr>
     * <tr><td>2.5.29.16</td>
     * <td>PrivateKeyUsage</td></tr>
     * <tr><td>2.5.29.17</td>
     * <td>SubjectAlternativeName</td></tr>
     * <tr><td>2.5.29.18</td>
     * <td>IssuerAlternativeName</td></tr>
     * <tr><td>2.5.29.19</td>
     * <td>BasicConstraints</td></tr>
     * <tr><td>2.5.29.30</td>
     * <td>NameConstraints</td></tr>
     * <tr><td>2.5.29.33</td>
     * <td>PolicyMappings</td></tr>
     * <tr><td>2.5.29.35</td>
     * <td>AuthorityKeyIdentifier</td></tr>
     * <tr><td>2.5.29.36</td>
     * <td>PolicyConstraints</td></tr>
     * </table>
     *
     * @param oid the Object Identifier value for the extension.
     * @return the DER-encoded octet string of the extension value or
     * null if it is not present.
     */
    public byte[] getExtensionValue(String oid);
}
