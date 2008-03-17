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
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 *
 */

package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.server.WSManAgent;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XMLSchema;
import com.sun.ws.management.xml.XmlBinding;

/**
 * 
 * J2SE API based implementation.
 */
public abstract class SOAPRequestBase implements SOAPRequest {

	private final static Logger logger = Logger
			.getLogger(SOAPRequestBase.class.getCanonicalName());

	private static XmlBinding defaultBinding;
	private static Map<String,String> extensionNamespaces = WSManAgent.locateExtensionNamespaces();
	
	final static ObjectFactory FACTORY = new ObjectFactory();
	
	private boolean isToSet = false;
	private boolean isActionSet = false;
	private boolean isReplyToSet = false;
	private boolean isMessageIdSet = false;
	private boolean isFromSet = false;
	private boolean isFaultToSet = false;
	private boolean isPayloadSet = false;

	private String action;
	private Object payload;

	private final XmlBinding binding;
	private final List<Object> headers = new ArrayList<Object>();
	private final Map<String, String> namespaces;
	private final Map<String, ?> context;

	public SOAPRequestBase(EndpointReferenceType epr, Map<String, ?> context,
			XmlBinding binding) throws IOException, SOAPException, JAXBException {

		this.binding = (binding == null) ? getDefaultBinding() : binding;
		this.namespaces = new HashMap<String, String>();
		this.context = (context == null) ? new HashMap<String, Object>() : context;
		setTo(epr);
		addDefaultNamespaceDeclarations();
	}
	
	protected synchronized static XmlBinding getDefaultBinding() throws JAXBException {
		if (defaultBinding == null) {
			defaultBinding = new XmlBinding(null, (Map<String, String>)null);
		}
		return defaultBinding;
	}

	public synchronized void addHeader(final Object header) throws JAXBException {
		this.headers.add(header);
	}
	
	protected synchronized void addHeaders(final List<Object> headers) throws JAXBException {
		if (headers == null)
			return;
		final Iterator<Object> iterator = headers.iterator();
		while (iterator.hasNext())
			addHeader(iterator.next());
	}
	
	protected synchronized List<Object> getHeaders() {
		return this.headers;
	}

	public synchronized void setAction(final String action) throws JAXBException {
		if (isActionSet())
			throw new IllegalStateException("Header wsa:Action is already set.");
		this.action = action;
        final AttributedURI actionURI = FACTORY.createAttributedURI();
        actionURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        actionURI.setValue(action.trim());
        final JAXBElement<AttributedURI> actionElement = FACTORY.createAction(actionURI);
        addHeader(actionElement);
        isActionSet = true;
	}
	
	protected synchronized String getAction() {
		return this.action;
	}
	
	protected synchronized boolean isActionSet() {
		return this.isActionSet;
	}

	public synchronized void setReplyTo(final EndpointReferenceType replyToEPR) throws JAXBException {
		if (isReplyToSet())
			throw new IllegalStateException("Header wsa:ReplyTo is already set.");
		final JAXBElement<EndpointReferenceType> replyToElement = 
			FACTORY.createReplyTo(replyToEPR);
		addHeader(replyToElement);
		isReplyToSet = true;
	}	
	
	protected synchronized boolean isReplyToSet() {
		return this.isReplyToSet;
	}
	
    public synchronized void setMessageId(final String msgId) throws JAXBException, SOAPException {
		if (isMessageIdSet())
			throw new IllegalStateException("Header wsa:MessageID is already set.");
        final AttributedURI msgIdURI = FACTORY.createAttributedURI();
        msgIdURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        msgIdURI.setValue(msgId.trim());
        final JAXBElement<AttributedURI> msgIdElement = FACTORY.createMessageID(msgIdURI);
        addHeader(msgIdElement);
        isMessageIdSet = true;
    }
	
	protected synchronized boolean isMessageIdSet() {
		return this.isMessageIdSet;
	}
	
	public synchronized void setFaultTo(final EndpointReferenceType faultToEPR) throws JAXBException {
		if (isFaultToSet())
			throw new IllegalStateException("Header wsa:From is already set.");
		final JAXBElement<EndpointReferenceType> faultToElement =
			FACTORY.createFaultTo(faultToEPR);
		addHeader(faultToElement);
		isFaultToSet = true;
	}
	
	protected synchronized boolean isFaultToSet() {
		return this.isFaultToSet;
	}
	
	public synchronized void setFrom(final EndpointReferenceType from) throws JAXBException {
		if (isFromSet())
			throw new IllegalStateException("Header wsa:From is already set.");
		final JAXBElement<EndpointReferenceType> fromElement = 
			FACTORY.createFrom(from);
		addHeader(fromElement);
		isFromSet = true;
	}
	
	protected synchronized boolean isFromSet() {
		return this.isFromSet;
	}
	
	protected synchronized void setTo(final String address) throws JAXBException {
		if (isToSet())
			throw new IllegalStateException("Header wsa:To is already set.");
        final AttributedURI toURI = FACTORY.createAttributedURI();
        toURI.setValue(address.trim());
        toURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        final JAXBElement<AttributedURI> toElement = FACTORY.createTo(toURI);
        addHeader(toElement);
        isToSet = true;
	}
	
	protected synchronized void setTo(final EndpointReferenceType epr) throws JAXBException {
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
	
	protected synchronized boolean isToSet() {
		return this.isToSet;
	}

	public synchronized void setPayload(final Object content) {
		if (isPayloadSet())
			throw new IllegalStateException("Payload is already set.");
		payload = content;
		isPayloadSet = true;
	}
	
	protected synchronized Object getPayload() {
		return this.payload;
	}
	
	protected synchronized boolean isPayloadSet() {
		return this.isPayloadSet;
	}

	public synchronized void addNamespaceDeclaration(final String prefix, final String uri) {
		this.namespaces.put(prefix, uri);
	}
	
	protected synchronized Map<String, String> getNamespaceDeclarations() {
		return this.namespaces;
	}

	public synchronized Map<String, ?> getRequestContext() {
		return this.context;
	}

	public synchronized XmlBinding getXmlBinding() {
		return this.binding;
	}
	
    private void addDefaultNamespaceDeclarations() throws SOAPException {
		// Having all the namespace declarations in the envelope keeps
		// JAXB from putting these on every element
		addNamespaceDeclaration(XMLSchema.NS_PREFIX, XMLSchema.NS_URI);
		addNamespaceDeclaration(SOAP.NS_PREFIX, SOAP.NS_URI);
		addNamespaceDeclaration(Addressing.NS_PREFIX, Addressing.NS_URI);
		addNamespaceDeclaration(Eventing.NS_PREFIX, Eventing.NS_URI);
		addNamespaceDeclaration(Enumeration.NS_PREFIX, Enumeration.NS_URI);
		addNamespaceDeclaration(Transfer.NS_PREFIX, Transfer.NS_URI);
		addNamespaceDeclaration(Management.NS_PREFIX, Management.NS_URI);

		//Check to see if there are additional namespaces to add
		if ((extensionNamespaces != null) && (extensionNamespaces.size() > 0)) {
			for (String key : extensionNamespaces.keySet()) {
				addNamespaceDeclaration(key.trim(), extensionNamespaces
						.get(key).trim());
			}
		}
	}
}
