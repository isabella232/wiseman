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

package com.sun.ws.management.client.impl.jaxws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.WSManMessageConfiguration;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;

/**
 * 
 * JAX-WS Message API based implementation.
 */
class JAXWSSOAPRequest implements SOAPRequest {

	/**
	 * Map entry specifying the SSL Socket factory to be used by JAX-WS stub.
	 */
	public static final String JAXWS_SSL_SOCKET_FACTORY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory";
	/**
	 * Map entry to disable Fast-Infoset. Value is meaningless. By default,
	 * Fast-Infoset is enabled.
	 */
	public static final String JAXWS_NO_FAST_INFOSET = "com.sun.wiseman.jaxws.fastinfoset.disable";
	/**
	 * Map entry specifying a List&lt;{@link javax.xml.ws.handler.Handler Handler}&gt;
	 * that will be passed to JAX-WS.
	 */
	public static final String JAXWS_HANDLER_CHAIN = "com.sun.wiseman.jaxws.handlerchain";

	private final static Logger logger = Logger
			.getLogger("com.sun.ws.management.client");

	private Object payload;
	private XmlBinding binding;
	private JAXBRIContext jaxbContext;
	private JAXBRIContext payloadContext;
	private HeaderList headers = new HeaderList();
	private SOAPMessage soapMessage;
	private Map<String, String> namespaces;
	
	private final Dispatch<Message> proxy;

	private static class WSManHeadersHandler implements
			SOAPHandler<SOAPMessageContext> {

		public Set<QName> getHeaders() {
			Set<QName> headers = new HashSet<QName>();
			headers.add(Addressing.ACTION);
			headers.add(Addressing.REPLY_TO);
			headers.add(Addressing.ADDRESS);
			headers.add(Addressing.MESSAGE_ID);
			headers.add(Addressing.TO);
			headers.add(Addressing.RELATES_TO);
			headers.add(TransferExtensions.FRAGMENT_TRANSFER);
			headers.add(Management.RESOURCE_URI);
			headers.add(Management.MAX_ENVELOPE_SIZE);
			return headers;
		}

		public boolean handleMessage(SOAPMessageContext smc) {
			// ACCEPTING ALL THE HEADERS WITHOUT ANY CHECKS
			// XXX REVISIT WE SHOULD CHECK THEM
			return true;
		}

		public boolean handleFault(SOAPMessageContext smc) {

			return true;
		}

		// nothing to clean up
		public void close(MessageContext messageContext) {
		}

		// nothing to clean up
		public void destroy() {
		}
	}

	public JAXWSSOAPRequest(String endpoint, Map<String, ?> context,
			QName serviceName, QName portName, XmlBinding binding) throws IOException {
		
		this.proxy = createJAXWSStub(endpoint, context, serviceName, portName);
		this.jaxbContext = (JAXBRIContext) binding.getJAXBContext();
		this.namespaces = new HashMap<String, String>();
	}

	public JAXWSSOAPRequest(EndpointReferenceType epr, Map<String, ?> context,
			XmlBinding binding) throws IOException {
		
		final ServiceNameType service = epr.getServiceName();
		
		final QName serviceName;
		final QName portName;
		
		if (service == null) {
			// TODO: Should we throw an exception instead?
			serviceName = new QName(Management.NS_URI, "Service", Management.NS_PREFIX);
			portName = new QName(Management.NS_URI, "Port", Management.NS_PREFIX);
		} else {
			serviceName = service.getValue();
			String port = service.getPortName();
			if ((port == null) || (port.trim().length() == 0))
				port = "Port";
			portName = new QName(serviceName.getNamespaceURI(), port, serviceName.getPrefix());
		}
		this.proxy = createJAXWSStub(epr.getAddress().getValue(), context, serviceName, portName);
		this.binding = binding;
		this.jaxbContext = (JAXBRIContext) binding.getJAXBContext();
		this.namespaces = new HashMap<String, String>();
	}

	public Message buildMessage() {
		if (soapMessage != null)
			return Messages.create(soapMessage);

		Message msg;
		if (payload == null)
			msg = Messages.createEmpty(SOAPVersion.SOAP_12);
		else if (payload instanceof Element)
			msg = Messages.createUsingPayload((Element) payload,
					SOAPVersion.SOAP_12);
		else {
			msg = Messages.create(payloadContext, payload, SOAPVersion.SOAP_12);
		}

		HeaderList headerList = msg.getHeaders();

		if (headers.size() > 0)
			headerList.addAll(headers);

		return msg;
	}

	public void setPayload(Object jaxb, JAXBContext ctx) {
		payload = jaxb;
		payloadContext = (ctx != null) ? (JAXBRIContext) ctx : jaxbContext;
	}

	public void addHeader(Object obj, JAXBContext ctx)
			throws JAXBException {
		final JAXBRIContext context = 
			(ctx != null) ? (JAXBRIContext) ctx : jaxbContext;
		if (obj instanceof Element)
			headers.add(Headers.create((Element) obj));
		else
			headers.add(Headers.create(context, obj));
	}

	public void addNamespaceDeclaration(final String prefix, final String uri) {
		// TODO: I believe this can be ignored for JAX-WS
		this.namespaces.put(prefix, uri);
	}

	public void addNamespaceDeclarations(Map<String, String> declarations) {
		// TODO: I believe this can be ignored for JAX-WS
		if ((declarations != null) && (declarations.size() > 0))
			this.namespaces.putAll(declarations);
	}

	public void setSOAPMessage(SOAPMessage msg) {
		soapMessage = msg;
	}

	public SOAPResponse invoke() {
		final Header action = headers.get(Addressing.ACTION, true);
		if (action == null)
			throw new IllegalStateException("wsa:Action header has not been set.");
		proxy.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,
				action.getStringContent());
		proxy.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY,
				true);
		final Message request = this.buildMessage();
		final Message response = proxy.invoke(request);
		if (response == null) {
			return null;
		}
		return new JAXWSSOAPResponse(response, binding, proxy);
	}

	public void invokeOneWay() {
		final Header action = headers.get(Addressing.ACTION, true);
		if (action == null)
			throw new IllegalStateException("wsa:Action header has not been set.");
		proxy.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,
				action.getStringContent());
		proxy.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY,
				true);
		final Message message = buildMessage();
		proxy.invokeOneWay(message);
	}

	public Map<String, ?> getRequestContext() {
		return proxy.getRequestContext();
	}
	
	public XmlBinding getXmlBinding() {
		return binding;
	}

	private static Dispatch<Message> createJAXWSStub(String endpoint,
			Map<String, ?> reqContext, QName serviceName, QName portName)
			throws IOException {
		try {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Creating proxy " + endpoint);
			}
			if (reqContext == null)
				reqContext = Collections.emptyMap();

			Service service = Service.create(serviceName);

			String binding = SOAPBinding.SOAP12HTTP_BINDING;

			service.addPort(portName, binding, endpoint);

			Dispatch<Message> port = service.createDispatch(portName,
					Message.class, Service.Mode.MESSAGE,
					new MemberSubmissionAddressingFeature(true, true));

			BindingProvider provider = (BindingProvider) port;

			Map<String, Object> requestContext = provider.getRequestContext();

			SSLSocketFactory factory = (SSLSocketFactory) reqContext
					.get(WSManMessageConfiguration.SSL_SOCKET_FACTORY);

			if (factory != null)
				requestContext.put(JAXWS_SSL_SOCKET_FACTORY, factory);

			requestContext.putAll(reqContext);

			// Fast Infoset
			boolean disablefastInfoset = reqContext
					.containsKey(JAXWS_NO_FAST_INFOSET);
			if (!disablefastInfoset) {
				String p = System.getProperty(JAXWS_NO_FAST_INFOSET);
				if (p != null) {
					disablefastInfoset = true;
				}
			}

			if (!disablefastInfoset) {
				String fastinfoset = (String) reqContext
						.get("com.sun.xml.ws.client." + "ContentNegotiation");
				if (fastinfoset == null) {
					requestContext.put(
							"com.sun.xml.ws.client.ContentNegotiation",
							"pessimistic");
				}
			}

			// Add Session support based on Cookies If needed.
			Boolean session = (Boolean) reqContext
					.get(BindingProvider.SESSION_MAINTAIN_PROPERTY);

			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Maintaining session " + session);
			}

			Boolean useSoapAction = (Boolean) reqContext
					.get(BindingProvider.SOAPACTION_USE_PROPERTY);
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Using SOAP action " + useSoapAction);
			}

			String soapActionURI = (String) reqContext
					.get(BindingProvider.SOAPACTION_URI_PROPERTY);

			if (logger.isLoggable(Level.FINER)) {
				logger.finer("SOAP action URI " + soapActionURI);
			}

			List<Handler> handlers = (List<Handler>) reqContext
					.get(JAXWS_HANDLER_CHAIN);
			if (handlers == null) {
				handlers = new ArrayList<Handler>();
			}

			// WS-MAN headers
			handlers.add(new WSManHeadersHandler());

			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Adding handlers " + handlers);
			}

			provider.getBinding().setHandlerChain(handlers);

			return port;
		} catch (RuntimeException r) {
			throw r;
		} catch (Exception ex) {
			if (ex instanceof IOException)
				throw (IOException) ex;

			final IllegalArgumentException iae = new IllegalArgumentException(
					endpoint + ": " + ex);
			iae.initCause(ex);
			throw iae;
		} catch (Error e) {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("PortType creation failed: " + e);
			}
			throw e;
		}
	}
}
