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

package com.sun.ws.management.message.api.client.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory;

import com.sun.ws.management.message.api.constants.SOAPConstants;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.spi.client.WSManStub;
import com.sun.ws.management.xml.XmlBinding;

public class SOAPRequest {

	private final static Logger logger = Logger.getLogger(SOAPRequest.class
			.getCanonicalName());

	private static WSManStub wsManStub;

	private static XmlBinding defaultBinding;

	final static ObjectFactory FACTORY;

	private static final DocumentBuilder db;
	private static final DocumentBuilderFactory docFactory;

	static {
		try {
			FACTORY = new ObjectFactory();
			docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true);

			db = docFactory.newDocumentBuilder();
		} catch (Exception pex) {
			pex.printStackTrace();
			throw new RuntimeException("Message static Initialization failed "
					+ pex);
		}
	}

	private boolean isPayloadSet = false;

	private Object payload;

	private final String to;
	private final String action;
	private final QName serviceName;
	private final QName portName;
	private final XmlBinding binding;
	private final List<Object> headers = new ArrayList<Object>();
	private final Map<String, String> namespaces;
	private final Map<String, ?> context;

	public SOAPRequest(final String to, final String action,
			final QName serviceName, final QName portName,
			final Map<String, ?> context, final XmlBinding binding)
			throws IOException, SOAPException, JAXBException {

		this.to = to;
		this.action = action;
		this.serviceName = serviceName;
		this.portName = portName;
		this.binding = (binding == null) ? getDefaultBinding() : binding;
		this.namespaces = new HashMap<String, String>();
		this.context = (context == null) ? new HashMap<String, Object>()
				: context;
		addNamespaceDeclaration(SOAPConstants.NS_PREFIX, SOAPConstants.NS_URI);
	}

	public synchronized static XmlBinding getDefaultBinding()
			throws JAXBException {
		if (defaultBinding == null) {
			defaultBinding = new XmlBinding(null, (Map<String, String>) null);
		}
		return defaultBinding;
	}

	public synchronized void addHeader(final Object header)
			throws JAXBException {
		this.headers.add(header);
	}

	public synchronized void addHeaders(final List<Object> headers)
			throws JAXBException {
		if (headers == null)
			return;
		this.headers.addAll(headers);
	}

	public synchronized List<Object> getHeaders() {
		return this.headers;
	}

	public String getTo() {
		return this.to;
	}

	public String getAction() {
		return this.action;
	}

	public QName getServiceName() {
		return this.serviceName;
	}

	public QName getPortName() {
		return this.portName;
	}

	public synchronized void setPayload(final Object content) {
		if (isPayloadSet())
			throw new IllegalStateException("Payload is already set.");
		payload = content;
		isPayloadSet = true;
	}

	public synchronized Object getPayload() {
		return this.payload;
	}

	public synchronized boolean isPayloadSet() {
		return this.isPayloadSet;
	}

	public synchronized void addNamespaceDeclaration(final String prefix,
			final String uri) {
		this.namespaces.put(prefix, uri);
	}

	public synchronized void addNamespaceDeclarations(
			Map<String, String> declarations) {
		if ((declarations != null) && (declarations.size() > 0)) {
			this.namespaces.putAll(declarations);
		}
	}

	public synchronized Map<String, String> getNamespaceDeclarations() {
		return this.namespaces;
	}

	public static Element createStringElement(final QName name,
			final String content) {

		// Check for valid name
		if (name == null) {
			throw new IllegalArgumentException("QName cannot be null.");
		}

		// Create and populate the Element and it's value.
		final Document document = db.newDocument();
		final Element identifier = document.createElementNS(name
				.getNamespaceURI(), name.getPrefix() + ":"
				+ name.getLocalPart());
		if (content == null) {
			// TODO: ???? Set the nil attribute ????
		} else {
			identifier.setTextContent(content);
		}
		document.appendChild(identifier);
		return document.getDocumentElement();
	}

	@SuppressWarnings("unchecked")
	public Object getHeader(final QName name) {
		final List<Object> headerList = getHeaders(name);
		if (headerList.size() > 0) {
			return headerList.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Object> getHeaders(final QName name) {
		final List<Object> headerList = new ArrayList<Object>();

		for (final Object header : this.headers) {
			if ((header instanceof JAXBElement)
					&& (((JAXBElement) header).getName().equals(name)))
				headerList.add(header);
			else if (header instanceof Element) {
				final Element element = (Element) header;
				if ((element.getNodeName().equals(name.getLocalPart()))
						&& (element.getNamespaceURI().equals(name
								.getNamespaceURI())))
					headerList.add(header);
			}
		}
		return headerList;
	}

	public synchronized Map<String, ?> getRequestContext() {
		return this.context;
	}

	public synchronized XmlBinding getXmlBinding() {
		return this.binding;
	}

	public synchronized SOAPResponse invoke() throws Exception {
		if (wsManStub == null)
			wsManStub = WSManStub.newInstance();
		return wsManStub.invoke(this);
	}

	public synchronized void invokeOneWay() throws Exception {
		if (wsManStub == null)
			wsManStub = WSManStub.newInstance();
		wsManStub.invokeOneWay(this);
	}

	public void validate() throws FaultException {

	}

	public synchronized void writeTo(final OutputStream os,
			final boolean formatted) throws Exception {
		if (wsManStub == null)
			wsManStub = WSManStub.newInstance();
		wsManStub.writeTo(this, os, formatted);
	}

	public String toString() {
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			writeTo(bos, false);
			return bos.toString();
		} catch (Exception e) {
			logger
					.severe("Could not serialize message due to unexpected Exception: "
							+ e.getMessage());
		}
		return "";
	}
}
