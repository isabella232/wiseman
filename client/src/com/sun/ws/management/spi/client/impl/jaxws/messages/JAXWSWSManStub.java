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

package com.sun.ws.management.spi.client.impl.jaxws.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.message.api.client.soap.SOAPRequest;
import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.server.WSManAgent;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.spi.client.WSManStub;
import com.sun.ws.management.spi.client.WSManStubConstants;
import com.sun.ws.management.spi.client.impl.jaxws.JAXWSLoggingHandler;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.xml.XMLSchema;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;

/**
 * 
 * JAX-WS SOAPMessage API based implementation.
 */
public class JAXWSWSManStub extends WSManStub {

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

	private final static Logger logger = Logger.getLogger(JAXWSWSManStub.class
			.getCanonicalName());

	private static final Set<QName> predefinedHeaders;
	private static Map<String, String> extensionNamespaces;

	static {
		predefinedHeaders = new HashSet<QName>(4);

		predefinedHeaders.add(Addressing.ACTION);
		predefinedHeaders.add(Addressing.REPLY_TO);
		predefinedHeaders.add(Addressing.MESSAGE_ID);
		predefinedHeaders.add(Addressing.TO);

		extensionNamespaces = WSManAgent.locateExtensionNamespaces();
	}

	private static class WSManHeadersHandler implements
			MessageHandler<MessageHandlerContext> {

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

		public boolean handleMessage(MessageHandlerContext smc) {
			// ACCEPTING ALL THE HEADERS WITHOUT ANY CHECKS
			// XXX REVISIT WE SHOULD CHECK THEM
			logger.fine("WSManHeadersHandler.handleMessage() called");
			return true;
		}

		public boolean handleFault(MessageHandlerContext smc) {
			logger.fine("WSManHeadersHandler.handleFault() called");
			return true;
		}

		// nothing to clean up
		public void close(MessageContext messageContext) {
		}

		// nothing to clean up
		public void destroy() {
		}
	}

	public JAXWSWSManStub() {
		super();
	}

	public SOAPResponse invoke(final SOAPRequest soapReq) throws SOAPException,
			IOException, JAXBException {

		final Message request = buildMessage(soapReq);
		final String action = soapReq.getAction();

		if ((action == null) || (action.length() == 0))
			throw new IllegalStateException(
					"wsa:Action header has not been set.");

		final Dispatch<Message> proxy = createProxy(soapReq);
		final Map<String, Object> context = proxy.getRequestContext();
		context.put(BindingProvider.SOAPACTION_URI_PROPERTY, action);
		context.put(BindingProvider.SOAPACTION_USE_PROPERTY, true);

		// Handle Basic authentication
		final String username = (String) context
				.get(WSManStubConstants.USERNAME_PROPERTY);
		final String password = (String) context
				.get(WSManStubConstants.PASSWORD_PROPERTY);
		if ((username != null) && (username.length() > 0))
			context.put(BindingProvider.USERNAME_PROPERTY, username);
		if ((password != null) && (password.length() > 0))
			context.put(BindingProvider.PASSWORD_PROPERTY, password);

		// if (logger.isLoggable(Level.FINE))
		// logger.fine("<request>\n" + toString(request.copy()) +
		// "</request>\n");
		final Message response = proxy.invoke(request);
		if (response == null) {
			return null;
		}
		// if (logger.isLoggable(Level.FINE))
		// logger.fine("<response>\n" + toString(response.copy()) +
		// "</response>\n");
		return new JAXWSSOAPResponse(response, soapReq.getXmlBinding(), proxy);
	}

	public void invokeOneWay(final SOAPRequest soapReq) throws SOAPException,
			JAXBException, IOException {

		final String action = soapReq.getAction();

		if ((action == null) || (action.length() == 0))
			throw new IllegalStateException(
					"wsa:Action header has not been set.");

		final Message message = buildMessage(soapReq);

		final Dispatch<Message> proxy = createProxy(soapReq);
		final Map<String, Object> context = proxy.getRequestContext();
		context.put(BindingProvider.SOAPACTION_URI_PROPERTY, action);
		context.put(BindingProvider.SOAPACTION_USE_PROPERTY, true);

		// Handle Basic authentication
		final String username = (String) context
				.get(WSManStubConstants.USERNAME_PROPERTY);
		final String password = (String) context
				.get(WSManStubConstants.PASSWORD_PROPERTY);
		if ((username != null) && (username.length() > 0))
			context.put(BindingProvider.USERNAME_PROPERTY, username);
		if ((password != null) && (password.length() > 0))
			context.put(BindingProvider.PASSWORD_PROPERTY, password);

		// if (logger.isLoggable(Level.FINE))
		// logger.fine("<request>\n" + toString(message) + "</request>\n");
		proxy.invokeOneWay(message);
	}

	public void writeTo(SOAPRequest soapReq, final OutputStream os,
			final boolean formatted) throws Exception {
		final Message message = buildMessage(soapReq);
		final XMLOutputFactory factory = XMLOutputFactory.newInstance();
		if (formatted == true) {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final XMLStreamWriter writer = factory.createXMLStreamWriter(bos);
			message.writeTo(writer);
			writer.flush();
			writer.close();
			final byte[] content = bos.toByteArray();
			final ByteArrayInputStream bis = new ByteArrayInputStream(content);
			final Document doc = Addressing.getDocumentBuilder().parse(bis);
			final OutputFormat format = new OutputFormat(doc);
			format.setLineWidth(72);
			format.setIndenting(true);
			format.setIndent(2);
			final XMLSerializer serializer = new XMLSerializer(os, format);
			serializer.serialize(doc);
			os.write("\n".getBytes());
		} else {
			final XMLStreamWriter writer = factory.createXMLStreamWriter(os);
			message.writeTo(writer);
		}
	}

	// Private methods

	private static Dispatch<Message> createProxy(final SOAPRequest soapReq)
			throws IOException {

		QName serviceName = (QName) soapReq.getServiceName();
		QName portName = (QName) soapReq.getPortName();

		if (serviceName == null) {
			serviceName = new QName(Management.NS_URI, "WSManService",
					Management.NS_PREFIX);
		}
		if (portName == null) {
			portName = new QName(Management.NS_URI, "WSManPort",
					Management.NS_PREFIX);
		}
		return createJAXWSStub(soapReq.getTo(), soapReq.getRequestContext(),
				serviceName, portName);
	}

	private static SOAPMessage buildSOAPMessage(final SOAPRequest soapReq)
			throws SOAPException, JAXBException {
		final SOAPMessage request = MessageFactory.newInstance(
				SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

		final SOAPEnvelope env = request.getSOAPPart().getEnvelope();

		// Add WS Management namespace declarations to the Envelope
		env.addNamespaceDeclaration(XMLSchema.NS_PREFIX, XMLSchema.NS_URI);
		env.addNamespaceDeclaration(SOAP.NS_PREFIX, SOAP.NS_URI);
		env.addNamespaceDeclaration(Addressing.NS_PREFIX, Addressing.NS_URI);
		env.addNamespaceDeclaration(Eventing.NS_PREFIX, Eventing.NS_URI);
		env.addNamespaceDeclaration(Enumeration.NS_PREFIX, Enumeration.NS_URI);
		env.addNamespaceDeclaration(Transfer.NS_PREFIX, Transfer.NS_URI);
		env.addNamespaceDeclaration(Management.NS_PREFIX, Management.NS_URI);

		// Add extension namespace declarations to the Envelope
		if ((extensionNamespaces != null) && (extensionNamespaces.size() > 0)) {
			for (final String prefix : extensionNamespaces.keySet()) {
				env.addNamespaceDeclaration(prefix.trim(), extensionNamespaces
						.get(prefix).trim());
			}
		}

		// Add request namespace declarations to the Envelope
		final Map<String, String> namespaces = soapReq
				.getNamespaceDeclarations();

		for (final String prefix : namespaces.keySet())
			env.addNamespaceDeclaration(prefix, namespaces.get(prefix));

		// Add the headers
		addMsgHeaders(soapReq, soapReq.getHeaders(), request);
		if (soapReq.getPayload() != null)
			soapReq.getXmlBinding().marshal(soapReq.getPayload(),
					request.getSOAPBody());
		return request;
	}

	private static Message buildMessage(SOAPRequest soapReq)
			throws SOAPException, JAXBException {
		// Workaround to add namespaces to outgoing request
		final SOAPMessage request = buildSOAPMessage(soapReq);

		// Create the message
		final Message msg = Messages.create(request);

		return msg;
	}

	private static void addMsgHeaders(final SOAPRequest soapReq,
			final List<Object> anyList, final SOAPMessage msg)
			throws JAXBException, SOAPException {
		if ((anyList == null) || (anyList.size() == 0)) {
			return;
		}

		final SOAPHeader header = msg.getSOAPHeader();
		final XmlBinding binding = soapReq.getXmlBinding();
		for (final Object any : anyList) {
			if (any instanceof Node) {
				final Node node = (Node) any;
				if (isValidHeader(node)) {
					// NOTE: can be a performance hog if the node is deeply
					// nested
					header.appendChild(header.getOwnerDocument().importNode(
							node, true));
				}
			} else {
				if (header instanceof JAXBElement) {
					if (isValidHeader((JAXBElement) header))
						binding.marshal(any, header);
				} else {
					binding.marshal(any, header);
				}
			}
		}
	}

	private static boolean isValidHeader(JAXBElement header) {
		if (predefinedHeaders.contains(header.getName()))
			return false;
		else
			return true;
	}

	private static boolean isValidHeader(final Node node) {
		final QName name = new QName(node.getNamespaceURI(), node
				.getLocalName());
		if (predefinedHeaders.contains(name))
			return false;
		else
			return true;
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
					.get(WSManStubConstants.SSL_SOCKET_FACTORY);

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
			handlers.add(new JAXWSLoggingHandler());

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
