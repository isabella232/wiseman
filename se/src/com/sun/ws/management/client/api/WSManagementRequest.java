/*
 * Copyright 2006-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.  Use is subject to license terms.
 */

package com.sun.ws.management.client.api;

import com.sun.ws.management.enumeration.EnumerationExtensions;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;

/**
 *
 *Abstraction of a WS-Management request
 */
public interface WSManagementRequest {
    public void setRelease(EnumerationContextType context) throws JAXBException;
    public void setResourceURI(final String resource) throws JAXBException, SOAPException;
    public void setTimeout(final Duration duration) throws JAXBException, SOAPException;
    public void setSelectors(final Set<SelectorType> selectors) throws JAXBException, SOAPException;
    public void setPull(final Object context, final int maxChars,
            final int maxElements, final Duration maxDuration)
            throws JAXBException, SOAPException, DatatypeConfigurationException;
    public void setPayload(Object jaxb, JAXBContext ctx) throws Exception;
    public void addHeaders(final ReferencePropertiesType props) throws JAXBException;
    public void addHeader(Object obj, JAXBContext ctx) throws JAXBException;
    public void setSubscribe(final EndpointReferenceType endTo, final String deliveryMode,
            final EndpointReferenceType notifyTo, final String expires, 
            final org.xmlsoap.schemas.ws._2004._08.eventing.FilterType filter,
            final Object... extensions)
            throws SOAPException, JAXBException;
   public void setUnsubscribe() throws SOAPException, JAXBException;
   public void setFragmentHeader(final Object expression,
            final String dialect, 
            final Map<String,String> nsDeclarations)
            throws SOAPException, JAXBException;
   public void setRequestTotalItemsCountEstimate() throws JAXBException;
   public void setEnumerate(final EndpointReferenceType endTo,
            final boolean requestTotalItemsCountEstimate,
            final boolean optimize,
            final int maxElements,
            final String expires,
            final DialectableMixedDataType filter,
            final EnumerationExtensions.Mode mode,
            final Object... anys) throws JAXBException, SOAPException;
  public void setSubscriptionEnd(final EndpointReferenceType mgr,
            final String status, final String reason) throws SOAPException, JAXBException;
   /**
    * Never call this methid except you are doing some Event pushing filtering
    */
   public void setSOAPMessage(SOAPMessage msg);
}
