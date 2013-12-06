/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * Defines the base interface for all custom value types 
 * generated from IDL.
 *
 * All value types implement ValueBase either directly 
 * or indirectly by implementing either the StreamableValue 
 * or CustomValue interface.
 * @author OMG
 * @version 1.9 02/06/02
 */

package org.omg.CORBA.portable;

import org.omg.CORBA.CustomMarshal;

public interface CustomValue extends ValueBase, CustomMarshal {

}

