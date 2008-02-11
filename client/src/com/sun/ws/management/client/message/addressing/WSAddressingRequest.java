/*
 * Copyright 2005-2008 Sun Microsystems, Inc.
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
 ** Copyright (C) 2006, 2007, 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 *
 */

package com.sun.ws.management.client.message.addressing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import com.sun.ws.management.client.WSManMessageFactory;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XmlBinding;

/**
 * 
 * Implementation of a WS-Addressing request
 */
public class WSAddressingRequest implements SOAPRequest {
	
	public static final String UUID_SCHEME = "uuid:";
    public static final String UNSPECIFIED_MESSAGE_ID = "http://schemas.xmlsoap.org/ws/2004/08/addressing/id/unspecified";
    public static final String ANONYMOUS_ENDPOINT_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";
    public static final String FAULT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";
	
	public static final ObjectFactory FACTORY = new ObjectFactory();
	
	private final SOAPRequest request;
	
	private boolean isToSet = false;
	private boolean isActionSet = false;
	private boolean isReplyToSet = false;
	private boolean isMessageIdSet = false;
	private boolean isFromSet = false;
	private boolean isFaultToSet = false;
	private boolean isPayloadSet = false;

	
	WSAddressingRequest() {
        this.request = null;
	}
	
	// TODO: Remove this constructor.
	public WSAddressingRequest(final SOAPRequest request) {
		this.request = request;
	}
	
	public WSAddressingRequest(final EndpointReferenceType epr,
                               final Map<String, ?> context,
                               final XmlBinding binding) throws Exception {
		this.request = WSManMessageFactory.newInstance().newRequest(epr, context, binding);
		setTo(epr);
	}
	
	public WSAddressingRequest(final String endpoint,
                               final Map<String, ?> context,
                               final QName serviceName,
                               final QName portName,
                               final XmlBinding binding)
			throws Exception {
		this.request = WSManMessageFactory.newInstance().newRequest(endpoint,
				context, serviceName, portName, binding);
		setTo(endpoint);
	}
	
	public void addNamespaceDeclaration(String prefix, String uri) {
		this.request.addNamespaceDeclaration(prefix, uri);
	}
	
	public void addNamespaceDeclarations(Map<String, String> declarations) {
		this.request.addNamespaceDeclarations(declarations);
	}
	
	public void setPayload(final Object content) {
		this.setPayload(content, null);
	}

	public synchronized void setPayload(final Object content, final JAXBContext ctx) {
		if (isPayloadSet)
			throw new IllegalStateException("env:Body payload is already set.");
		this.request.setPayload(content, ctx);
		isPayloadSet = true;
	}

	public synchronized void setAction(final String action) throws JAXBException {
		if (isActionSet)
			throw new IllegalStateException("Header wsa:Action is already set.");
        final AttributedURI actionURI = FACTORY.createAttributedURI();
        actionURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        actionURI.setValue(action.trim());
        final JAXBElement<AttributedURI> actionElement = FACTORY.createAction(actionURI);
        this.request.addHeader(actionElement, null);
        isActionSet = true;
	}
	
	private void setTo(final EndpointReferenceType epr) throws JAXBException {
		// Set the different parts of the EPR
		setTo(epr.getAddress().getValue());
		
		final ReferenceParametersType refParams = epr.getReferenceParameters();
		if (refParams != null) {
			final List<Object> refs = refParams.getAny();
			addHeaders(refs);
		}
		
		final ReferencePropertiesType properties = epr.getReferenceProperties();
		if (properties != null) {
			final List<Object> props = refParams.getAny();
			addHeaders(props);
		}
		
		final List<Object> any = epr.getAny();
		if (any != null) {
			addHeaders(any);
		}
	}
	
	private void addHeaders(final List<Object> headers) throws JAXBException {
		if (headers == null)
			return;
		final Iterator<Object> iterator = headers.iterator();
		while (iterator.hasNext())
			addHeader(iterator.next());
	}

	private void setTo(final String address) throws JAXBException {
		if (isToSet)
			throw new IllegalStateException("Header wsa:To is already set.");
        final AttributedURI toURI = FACTORY.createAttributedURI();
        toURI.setValue(address.trim());
        toURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        final JAXBElement<AttributedURI> toElement = FACTORY.createTo(toURI);
        this.request.addHeader(toElement, null);
        isToSet = true;
	}

	public synchronized void setReplyTo(final EndpointReferenceType replyToEPR) throws JAXBException {
		if (isReplyToSet)
			throw new IllegalStateException("Header wsa:ReplyTo is already set.");
		final JAXBElement<EndpointReferenceType> replyToElement = 
			FACTORY.createReplyTo(replyToEPR);
		this.request.addHeader(replyToElement, null);
		isReplyToSet = true;
	}
	
    public synchronized void setMessageId(final String msgId) throws JAXBException, SOAPException {
		if (isMessageIdSet)
			throw new IllegalStateException("Header wsa:MessageID is already set.");
        final AttributedURI msgIdURI = FACTORY.createAttributedURI();
        msgIdURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        msgIdURI.setValue(msgId.trim());
        final JAXBElement<AttributedURI> msgIdElement = FACTORY.createMessageID(msgIdURI);
        this.request.addHeader(msgIdElement, null);
        isMessageIdSet = true;
    }

	public synchronized void setFaultTo(final EndpointReferenceType faultToEPR) throws JAXBException {
		if (isFaultToSet)
			throw new IllegalStateException("Header wsa:From is already set.");
		final JAXBElement<EndpointReferenceType> faultToElement =
			FACTORY.createFaultTo(faultToEPR);
		this.request.addHeader(faultToElement, null);
		isFaultToSet = true;
	}

	public synchronized void setFrom(final EndpointReferenceType from) throws JAXBException {
		if (isFromSet)
			throw new IllegalStateException("Header wsa:From is already set.");
		final JAXBElement<EndpointReferenceType> fromElement = 
			FACTORY.createFrom(from);
		this.request.addHeader(fromElement, null);
		isFromSet = true;
	}
	

	public synchronized void addHeader(Object header)
			throws JAXBException {
		this.addHeader(header, null);
	}

	// TODO: This should probably be protected.
	public synchronized void addHeader(Object header, JAXBContext ctx)
			throws JAXBException {
		// TODO: Add checks for known headers.
		this.request.addHeader(header, null);
	}

	public Map<String, ?> getRequestContext() {
		return this.request.getRequestContext();
	}

	public XmlBinding getXmlBinding() {
		return this.request.getXmlBinding();
	}
	
	public synchronized SOAPResponse invoke() throws Exception {
		if (!isToSet)
			throw new IllegalStateException("Message is missing required wsa:To header.");
		if (!isActionSet)
			throw new IllegalStateException("Message is missing required wsa:Action header.");
		if (!isReplyToSet)
		    setReplyTo(createEndpointReference(ANONYMOUS_ENDPOINT_URI, null, null, null, null)); // Replying to creator
		if (!isMessageIdSet)
			setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
		return new WSAddressingResponse(this.request.invoke());
	}
	
	public synchronized void invokeOneWay() throws Exception {
		if (!isToSet)
			throw new IllegalStateException("Message is missing required wsa:To header.");
		if (!isActionSet)
			throw new IllegalStateException("Message is missing required wsa:Action header.");
		if (!isMessageIdSet)
			setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
		this.request.invokeOneWay();
	}
	
    // only address is mandatory, the rest of the params are optional and can be null
    public static EndpointReferenceType createEndpointReference(final String address,
            final ReferencePropertiesType props, final ReferenceParametersType params,
            final AttributedQName portType, final ServiceNameType serviceName) {

    	if (address == null) {
    		throw new IllegalArgumentException("Address can not be null.");
    	}
    	
        final EndpointReferenceType epr = FACTORY.createEndpointReferenceType();

        final AttributedURI addressURI = FACTORY.createAttributedURI();
        addressURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        addressURI.setValue(address.trim());
        epr.setAddress(addressURI);

        if (params != null) {
            epr.setReferenceParameters(params);
        }

        if (props != null) {
            epr.setReferenceProperties(props);
        }

        if (serviceName != null) {
            epr.setServiceName(serviceName);
        }

        if (portType != null) {
            epr.setPortType(portType);
        }

        return epr;
    }
}
