/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package java.security.interfaces;

import java.math.BigInteger;

/**
 * The interface to an RSA public or private key.
 *
 * @author Jan Luehe
 * @version 1.4 02/06/02
 *
 * @see RSAPublicKey
 * @see RSAPrivateKey
 *
 * @since 1.3
 */

public interface RSAKey {

    /**
     * Returns the modulus.
     *		
     * @return the modulus
     */
    public BigInteger getModulus();
}
