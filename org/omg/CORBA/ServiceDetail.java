/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.omg.CORBA;

/**
 * An object that represents an ORB service. Its <code>service_detail_type</code>
 * field contains the type of the ORB service, and its <code>service_detail</code>
 * field contains a description of the ORB service..
 *
 * @author RIP Team
 * @version 1.11 02/06/02
 */
public final class ServiceDetail implements org.omg.CORBA.portable.IDLEntity
{
    /**
     * The type of the ORB service that this <code>ServiceDetail</code> 
     * object represents. 
     */
    public int service_detail_type;

    /** 
     * The data describing the ORB service that this <code>ServiceDetail</code>
     * object represents.
     */
    public byte[] service_detail;

    /** 
     * Constructs a <code>ServiceDetail</code> object with 0 for the type of
     * ORB service and an empty description.
     */
    public ServiceDetail() { }

    /** 
     * Constructs a <code>ServiceDetail</code> object with the given 
     * ORB service type and the given description.
     *
     * @param service_detail_type an <code>int</code> specifying the type of 
     *                            ORB service
     * @param service_detail a <code>byte</code> array describing the ORB service
     */
    public ServiceDetail(int service_detail_type, byte[] service_detail) {
	this.service_detail_type = service_detail_type;
	this.service_detail = service_detail;
    }
}
