/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: Enumeration.java,v 1.6 2005-12-21 23:48:05 akhilarora Exp $
 */

package com.sun.ws.management.enumeration;

import com.sun.ws.management.addressing.Addressing;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationEnd;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Pull;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Release;

public class Enumeration extends Addressing {
    
    public static final String NS_PREFIX = "wsen";
    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration";
    
    public static final String ENUMERATE_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate";
    public static final String ENUMERATE_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerateResponse";
    
    public static final String PULL_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull";
    public static final String PULL_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/PullResponse";
    
    public static final String RENEW_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Renew";
    public static final String RENEW_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/RenewResponse";
    
    public static final String GET_STATUS_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatus";
    public static final String GET_STATUS_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatusResponse";
    
    public static final String RELEASE_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Release";
    public static final String RELEASE_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/ReleaseResponse";
    
    public static final String ENUMERATION_END_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerationEnd";
    
    public static final String SOURCE_SHUTTING_DOWN_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/SourceShuttingDown";
    public static final String SOURCE_CANCELING_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/SourceCanceling";
    
    public static final QName FILTER_DIALECT_REQUESTED_UNAVAILABLE = new QName(NS_URI, "FilterDialectRequestedUnavailable", NS_PREFIX);
    public static final String FILTER_DIALECT_REQUESTED_UNAVAILABLE_REASON = 
            "The requested filtering dialect is not supported.";

    public static final QName FILTERING_NOT_SUPPORTED = new QName(NS_URI, "FilteringNotSupported", NS_PREFIX);
    public static final String FILTERING_NOT_SUPPORTED_REASON = 
        "Filtered enumeration is not supported.";
    
    public static final QName CANNOT_PROCESS_FILTER = new QName(NS_URI, "CannotProcessFilter", NS_PREFIX);
    public static final String CANNOT_PROCESS_FILTER_REASON = 
            "The requested filter could not be processed.";

    public static final QName INVALID_ENUM_CONTEXT = new QName(NS_URI, "InvalidEnumerationContext", NS_PREFIX);
    public static final String INVALID_ENUM_CONTEXT_REASON =
        "The supplied enumeration context is invalid.";
    
    public static final QName INVALID_EXPIRATION_TIME = new QName(NS_URI, "InvalidExpirationTime", NS_PREFIX);
    public static final String INVALID_EXPIRATION_TIME_REASON = 
        "The expiration time was not valid.";
    
    public static final QName TIMED_OUT = new QName(NS_URI, "TimedOut", NS_PREFIX);
    public static final String TIMED_OUT_REASON = 
        "The enumerator has timed out and is no longer valid.";
    
    public static final QName UNSUPPORTED_EXPIRATION_TYPE = new QName(NS_URI, "UnsupportedExpirationType", NS_PREFIX);
    public static final String UNSUPPORTED_EXPIRATION_TYPE_REASON = 
        "The specified expiration type is not supported.";
    
    public static final QName ENUMERATE = new QName(NS_URI, "Enumerate", NS_PREFIX);
    public static final QName ENUMERATE_RESPONSE = new QName(NS_URI, "EnumerateResponse", NS_PREFIX);
    public static final QName PULL = new QName(NS_URI, "Pull", NS_PREFIX);
    public static final QName PULL_RESPONSE = new QName(NS_URI, "PullResponse", NS_PREFIX);
    public static final QName RELEASE = new QName(NS_URI, "Release", NS_PREFIX);
    public static final QName ENUMERATION_END = new QName(NS_URI, "EnumerationEnd", NS_PREFIX);
    public static final QName SUPPORTED_DIALECT = new QName(NS_URI, "SupportedDialect", NS_PREFIX);
    public static final QName ENUMERATION_CONTEXT = new QName(NS_URI, "EnumerationContext", NS_PREFIX);
    
    private ObjectFactory objectFactory = null;
    
    public Enumeration() throws SOAPException, JAXBException {
        super();
        init();
    }
    
    public Enumeration(final Addressing addr) throws SOAPException, JAXBException {
        super(addr);
        init();
    }
    
    public Enumeration(final InputStream is) throws SOAPException, JAXBException, IOException {
        super(is);
        init();
    }
    
    private void init() throws SOAPException, JAXBException {
        objectFactory = new ObjectFactory();
    }
    
    public void setEnumerate(final EndpointReferenceType endTo, 
            final String expires, final FilterType filter) 
            throws JAXBException, SOAPException {

        removeChildren(getBody(), ENUMERATE);
        final Enumerate enu = objectFactory.createEnumerate();
        if (endTo != null) {
            enu.setEndTo(endTo);
        }
        if (expires != null) {
            enu.setExpires(expires.trim());
        }
        if (filter != null) {
            enu.setFilter(filter);
        }
        getXmlBinding().marshal(enu, getBody());
    }
    
    public void setEnumerateResponse(final Object context, final String expires) 
    throws JAXBException, SOAPException {

        removeChildren(getBody(), ENUMERATE_RESPONSE);
        final EnumerateResponse response = objectFactory.createEnumerateResponse();
        
        final EnumerationContextType contextType = objectFactory.createEnumerationContextType();
        contextType.getContent().add(context);
        response.setEnumerationContext(contextType);
        
        if (expires != null) {
            response.setExpires(expires.trim());
        }
        
        getXmlBinding().marshal(response, getBody());
    }
    
    // context must not be null, the others can be null
    // context must be either java.lang.String or org.w3c.dom.Element
    public void setPull(final Object context, final int maxChars,
        final int maxElements, final Duration maxDuration)
        throws JAXBException, SOAPException, DatatypeConfigurationException {
        
        removeChildren(getBody(), PULL);
        final Pull pull = objectFactory.createPull();
        
        final EnumerationContextType contextType = objectFactory.createEnumerationContextType();
        contextType.getContent().add(context);
        pull.setEnumerationContext(contextType);
        
        if (maxChars > 0) {
            pull.setMaxCharacters(BigInteger.valueOf((long) maxChars));
        }
        if (maxElements > 0) {
            pull.setMaxElements(BigInteger.valueOf((long) maxElements));
        }
        if (maxDuration != null) {
            pull.setMaxTime(maxDuration);
        }
        
        getXmlBinding().marshal(pull, getBody());
    }
    
    public void setPullResponse(final List<Element> items, final Object context, final boolean haveMore) 
    throws JAXBException, SOAPException {

        removeChildren(getBody(), PULL_RESPONSE);
        final PullResponse response = objectFactory.createPullResponse();
        
        ItemListType itemList = objectFactory.createItemListType();
        itemList.getAny().addAll(items);
        response.setItems(itemList);
        
        if (haveMore) {
            final EnumerationContextType contextType = objectFactory.createEnumerationContextType();
            contextType.getContent().add(context);
            response.setEnumerationContext(contextType);
        } else {
            response.setEndOfSequence("");
        }
        
        getXmlBinding().marshal(response, getBody());
    }
    
    public void setRelease(final Object context) throws JAXBException, SOAPException {
        removeChildren(getBody(), RELEASE);
        final Release release = objectFactory.createRelease();
        
        final EnumerationContextType contextType = objectFactory.createEnumerationContextType();
        contextType.getContent().add(context);
        release.setEnumerationContext(contextType);
        
        getXmlBinding().marshal(release, getBody());
    }
    
    public Enumerate getEnumerate() throws JAXBException, SOAPException {
        Object value = unbind(getBody(), ENUMERATE);
        return value == null ? null : (Enumerate) value;
    }
    
    public EnumerateResponse getEnumerateResponse() throws JAXBException, SOAPException {
        Object value = unbind(getBody(), ENUMERATE_RESPONSE);
        return value == null ? null : (EnumerateResponse) value;
    }
    
    public Pull getPull() throws JAXBException, SOAPException {
        Object value = unbind(getBody(), PULL);
        return value == null ? null : (Pull) value;
    }
    
    public PullResponse getPullResponse() throws JAXBException, SOAPException {
        Object value = unbind(getBody(), PULL_RESPONSE);
        return value == null ? null : (PullResponse) value;
    }
    
    public Release getRelease() throws JAXBException, SOAPException {
        Object value = unbind(getBody(), RELEASE);
        return value == null ? null : (Release) value;
    }
    
    public EnumerationEnd getEnumerationEnd() throws JAXBException, SOAPException {
        Object value = unbind(getBody(), ENUMERATION_END);
        return value == null ? null : (EnumerationEnd) value;
    }
}
