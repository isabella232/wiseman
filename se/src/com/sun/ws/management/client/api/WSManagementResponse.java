/*
 * Copyright 2006-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.  Use is subject to license terms.
 */

package com.sun.ws.management.client.api;

import com.sun.ws.management.server.EnumerationItem;
import com.sun.xml.ws.api.message.Header;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableNonNegativeInteger;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

/**
 *
 *Abstraction of a WS-Management response
 */
public interface WSManagementResponse {
    public Object getPayload(Unmarshaller u) throws Exception;
    public List<Header> getHeaders() throws Exception ;
    public SubscribeResponse getSubscribeResponse() throws JAXBException, SOAPException;
    public AttributableNonNegativeInteger getTotalItemsCountEstimate() throws JAXBException, SOAPException;
    public EnumerateResponse getEnumerateResponse() throws JAXBException, SOAPException;
    public PullResponse getPullResponse() throws JAXBException, SOAPException;
    public List<EnumerationItem> getItems() throws JAXBException, SOAPException;
    public boolean isEndOfSequence() throws JAXBException, SOAPException;
}
