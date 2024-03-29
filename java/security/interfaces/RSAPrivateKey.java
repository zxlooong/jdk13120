/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.security.interfaces;

import java.math.BigInteger;

/**
 * The interface to an RSA private key.
 *
 * @author Jan Luehe
 *
 * @version 1.8 02/02/06
 *
 * @see RSAPrivateCrtKey
 */

public interface RSAPrivateKey extends java.security.PrivateKey, RSAKey
{
    /**
     * Returns the private exponent.
     *
     * @return the private exponent
     */
    public BigInteger getPrivateExponent();
}
