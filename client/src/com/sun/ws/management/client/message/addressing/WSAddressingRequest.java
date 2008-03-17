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

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.WSManMessageFactory;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.server.WSManAgent;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XMLSchema;
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
	
	private boolean isFromSet = false;
	private boolean isFaultToSet = false;
	private boolean isPayloadSet = false;

	private static Map<String,String> extensionNamespaces = WSManAgent.locateExtensionNamespaces();
	
	protected WSAddressingRequest() {
        this.request = null;
	}
	
	public WSAddressingRequest(final EndpointReferenceType epr,
                               final Map<String, ?> context,
                               final XmlBinding binding) throws Exception {
		this.request = WSManMessageFactory.newInstance().newRequest(epr, context, binding);
		addNamespaceDeclarations();
	}
	
	public void addNamespaceDeclaration(String prefix, String uri) {
		this.request.addNamespaceDeclaration(prefix, uri);
	}
	
	public void addNamespaceDeclarations(Map<String, String> declarations) {
		if ((declarations != null) && (declarations.size() > 0)) {
			final Set<String> prefixes = declarations.keySet();
			final Iterator<String> iter = prefixes.iterator();
			while (iter.hasNext()) {
				final String prefix = iter.next();
				final String uri = declarations.get(prefix);
				this.request.addNamespaceDeclaration(prefix, uri);
			}
		}
	}

	public synchronized void setPayload(final Object content) {
		if (isPayloadSet)
			throw new IllegalStateException("env:Body payload is already set.");
		this.request.setPayload(content);
		isPayloadSet = true;
	}

	public void setAction(final String action) throws JAXBException {
		this.request.setAction(action);
	}

	public void setReplyTo(final EndpointReferenceType replyToEPR) throws JAXBException {
		this.request.setReplyTo(replyToEPR);
	}
	
    public void setMessageId(final String msgId) throws JAXBException, SOAPException {
        this.request.setMessageId(msgId);
    }

	public synchronized void setFaultTo(final EndpointReferenceType faultToEPR) throws JAXBException {
		if (isFaultToSet)
			throw new IllegalStateException("Header wsa:From is already set.");
		final JAXBElement<EndpointReferenceType> faultToElement =
			FACTORY.createFaultTo(faultToEPR);
		this.request.addHeader(faultToElement);
		isFaultToSet = true;
	}

	public synchronized void setFrom(final EndpointReferenceType from) throws JAXBException {
		if (isFromSet)
			throw new IllegalStateException("Header wsa:From is already set.");
		final JAXBElement<EndpointReferenceType> fromElement = 
			FACTORY.createFrom(from);
		this.request.addHeader(fromElement);
		isFromSet = true;
	}
	

	public void addHeader(Object header)
			throws JAXBException {
		this.request.addHeader(header);
	}

	public Map<String, ?> getRequestContext() {
		return this.request.getRequestContext();
	}

	public XmlBinding getXmlBinding() {
		return this.request.getXmlBinding();
	}
	
	public SOAPResponse invoke() throws Exception {
		return new WSAddressingResponse(this.request.invoke());
	}
	
	public void invokeOneWay() throws Exception {
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

	public void writeTo(OutputStream os, boolean formatted) throws Exception {
		this.request.writeTo(os, formatted);
	}
	
	public String toString() {
		return this.request.toString();
	}
	
    private void addNamespaceDeclarations() throws SOAPException {
		// TODO: Find a better place for this?
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
