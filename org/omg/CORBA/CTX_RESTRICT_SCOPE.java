/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;

/**
 * A flag that can be used as the second parameter to the method
 * <code>Context.get_values</code> to restrict the search scope.
 * When this flag is used, it restricts the search for
 * context values to this particular <code>Context</code> object
 * or to the scope specified in the first parameter to
 * <code>Context.get_values</code>.
 * <P>
 * Usage:
 * <PRE>
 *     NVList props = myContext.get_values("_USER",
 *                     CTX_RESTRICT_SCOPE.value, "id*");
 * </PRE>
 *
 * @see org.omg.CORBA.Context#get_values(String, int, String)
 * @version 1.3, 09/09/97
 * @since   JDK1.2
 */
public interface CTX_RESTRICT_SCOPE {

/**
 * The field containing the <code>int</code> value of a
 * <code>CTX_RESTRICT_SCOPE</code> flag.
 */
  int value = 15;
}

