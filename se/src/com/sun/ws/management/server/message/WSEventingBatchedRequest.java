/*
 * Copyright 2006-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.  Use is subject to license terms.
 * 
 ** Copyright (C) 2006-2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 */

package com.sun.ws.management.server.message;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.EventsType;

public interface WSEventingBatchedRequest extends WSEventingRequest {
	
	/* Sink methods */
    public EventsType getBatchedEvents() throws JAXBException, SOAPException;

}
