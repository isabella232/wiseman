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

package com.sun.ws.management.spi.client.impl.j2se;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.w3._2003._05.soap_envelope.Envelope;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3._2003._05.soap_envelope.ObjectFactory;
import org.w3c.dom.Node;

import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.xml.XmlBinding;

/**
 * 
 * JAX-WS Message API based implementation.
 */
class J2SESOAPResponse_1 implements SOAPResponse {

	private final static Logger logger = Logger
			.getLogger(J2SESOAPResponse_1.class.getCanonicalName());
	
    private final static ObjectFactory FACTORY = new ObjectFactory();

	private final Envelope envelope;
	private final XmlBinding binding;
	private final Map<String, ?> context;
	private final boolean isFault;

	private boolean payloadRead = false;
	private Object payload = null;

	/** Creates a new instance of WSMessage */
	public J2SESOAPResponse_1(final Envelope message, 
			final XmlBinding binding,
			final Map<String, ?> context) {
		this.envelope = message;
		this.binding = binding;
		this.context = context;
		
		// Check if the body contains a fault
		final List<Object> body = envelope.getBody().getAny();
		if ((body != null) && (body.size() > 0)) {
			final Object obj = body.get(0);
			if ((obj instanceof JAXBElement) && 
					(((JAXBElement)obj).getDeclaredType().equals(Fault.class)))
				isFault = true;
			else
				isFault = false;
		} else
			isFault = false;
	}

	public Object getPayload() {
		if (!payloadRead) {
			payloadRead = true;
			final List<Object> bodyList = envelope.getBody().getAny();
			if (bodyList.size() > 0)
			    payload = bodyList.get(0);
			final Object body = envelope.getBody();
		}
		return payload;
	}

	public Map<String, ?> getResponseContext() {
		return this.context;
	}

	public Object getHeader(QName name) throws SOAPException, JAXBException {
		// TODO: Can have multiple entries for some headers
		
		final List<Object> headerList = envelope.getHeader().getAny();
		
		for (Object header : headerList) {
			if (header instanceof JAXBElement) {
				final JAXBElement jaxb =  (JAXBElement) header;
				if (jaxb.getName().equals(name))
					return jaxb;
			} else if (header instanceof Node) {
				final Node node = (Node)header;
				if ((node.getNamespaceURI().equals(name.getNamespaceURI())) &&
						(node.getLocalName().equals(name.getLocalPart())))
					return node;
			}
		}
		return null;
	}

	public boolean isFault() throws JAXBException, SOAPException {
		return isFault;
	}

	public void writeTo(final OutputStream os, final boolean formatted)
			throws Exception {
		final JAXBElement<Envelope> jaxb = FACTORY.createEnvelope(envelope);
		binding.marshal(jaxb, os, formatted);
	}

	public String toString() {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			writeTo(bos, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return "";
		}
		return bos.toString();
	}
}
