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

package com.sun.ws.management.client.impl.jaxws.messages;

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
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.WSManMessageConfiguration;
import com.sun.ws.management.client.WSManMessageFactory;
import com.sun.ws.management.client.impl.JAXWSLoggingHandler;
import com.sun.ws.management.client.impl.SOAPRequestBase;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.transfer.TransferExtensions;
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
class JAXWSSOAPRequest extends SOAPRequestBase {

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
			.getLogger(JAXWSSOAPRequest.class.getCanonicalName());
	
	private final Dispatch<Message> proxy;
	
	private String action;
	private boolean isActionSet = false;
	private boolean isToSet = false;

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

	public JAXWSSOAPRequest(EndpointReferenceType epr, Map<String, ?> context,
			XmlBinding binding) throws IOException, JAXBException, SOAPException {
		super(epr, context, binding);
		
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
	}
	
	protected SOAPMessage buildSOAPMessage() throws SOAPException, JAXBException {
		final SOAPMessage request = MessageFactory.newInstance(
				SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
		
		// Add the namespaces
		for (Map.Entry<String, String> decl : getNamespaceDeclarations().entrySet()) {
			request.getSOAPPart().getEnvelope().addNamespaceDeclaration(
					decl.getKey(), decl.getValue());
		}
		
		// Add the headers
		addMsgHeaders(getHeaders(), request);
		if (getPayload() != null)
			getXmlBinding().getJAXBContext().createMarshaller().marshal(getPayload(), request.getSOAPBody());
		return request;
	}

	protected Message buildMessage() throws SOAPException, JAXBException {		
		// Workaround to add namespaces to outgoing request
		final SOAPMessage request = buildSOAPMessage();
		
		// Create the message
		final Message msg = Messages.create(request);

		return msg;
	}
	
	public synchronized void setAction(final String action) throws JAXBException {
		if (isActionSet)
			throw new IllegalStateException("Header wsa:Action is already set.");
        this.action = action;
        isActionSet = true;
	}
	
	protected synchronized String getAction() {
		return this.action;
	}

    public boolean isReplyToSet() {
    	return true;
    }
    
    public boolean isMessageIdSet() {
    	return true;
    }
	
	protected void setTo(final String address) throws JAXBException {
		if (isToSet)
			throw new IllegalStateException("Header wsa:To is already set.");
        isToSet = true;
	}
	
	protected synchronized boolean isToSet() {
		return this.isToSet;
	}
		
    private void addMsgHeaders(final List<Object> anyList,
    		final SOAPMessage msg)
			throws JAXBException, SOAPException {
		if ((anyList == null) || (anyList.size() == 0)) {
			return;
		}

		final SOAPHeader header = msg.getSOAPHeader();
		final XmlBinding binding = getXmlBinding();
		for (final Object any : anyList) {
			if (any instanceof Node) {
				final Node node = (Node) any;
				// NOTE: can be a performance hog if the node is deeply nested
				header.appendChild(header.getOwnerDocument().importNode(node,
						true));
			} else {
				binding.marshal(any, header);
			}
		}
	}

	public SOAPResponse invoke() throws SOAPException, IOException, JAXBException {
		// final Header action = headers.get(Addressing.ACTION, true);
		// if (action == null)
		//	throw new IllegalStateException("wsa:Action header has not been set.");
		final Message request = this.buildMessage();
		if ((action == null) || (action.length() == 0))
			throw new IllegalStateException("wsa:Action header has not been set.");
		
		final Map<String, Object> context = proxy.getRequestContext();
		context.put(BindingProvider.SOAPACTION_URI_PROPERTY,
				action);
		context.put(BindingProvider.SOAPACTION_USE_PROPERTY,
				true);
		
		// Handle Basic authentication
		final String username = (String) context.get(WSManMessageFactory.USERNAME_PROPERTY);
        final String password = (String) context.get(WSManMessageFactory.PASSWORD_PROPERTY);
        if ((username != null) && (username.length() > 0))
        	context.put(BindingProvider.USERNAME_PROPERTY, username);
        if ((password != null) && (password.length() > 0))
            context.put(BindingProvider.PASSWORD_PROPERTY, password);

		// if (logger.isLoggable(Level.FINE))
		//	logger.fine("<request>\n" + toString(request.copy()) + "</request>\n");
		final Message response = proxy.invoke(request);
		if (response == null) {
			return null;
		}
		// if (logger.isLoggable(Level.FINE))
		//	logger.fine("<response>\n" + toString(response.copy()) + "</response>\n");
		return new JAXWSSOAPResponse(response, getXmlBinding(), proxy);
	}

	public void invokeOneWay() throws SOAPException, JAXBException {
		// final Header action = headers.get(Addressing.ACTION, true);
		// if (action == null)
		// 	throw new IllegalStateException("wsa:Action header has not been set.");
		final Message message = buildMessage();
		if ((action == null) || (action.length() == 0))
			throw new IllegalStateException("wsa:Action header has not been set.");
		final Map<String, Object> context = proxy.getRequestContext();
		context.put(BindingProvider.SOAPACTION_URI_PROPERTY,
				action);
		context.put(BindingProvider.SOAPACTION_USE_PROPERTY,
				true);
		
		// Handle Basic authentication
		final String username = (String) context.get(WSManMessageFactory.USERNAME_PROPERTY);
        final String password = (String) context.get(WSManMessageFactory.PASSWORD_PROPERTY);
        if ((username != null) && (username.length() > 0))
        	context.put(BindingProvider.USERNAME_PROPERTY, username);
        if ((password != null) && (password.length() > 0))
            context.put(BindingProvider.PASSWORD_PROPERTY, password);
        
		// if (logger.isLoggable(Level.FINE))
		//	logger.fine("<request>\n" + toString(message) + "</request>\n");
		proxy.invokeOneWay(message);
	}
	
	public void writeTo(OutputStream os, boolean formatted) throws Exception {
		final Message message = buildMessage();
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
	
	public String toString() {
		try {
			return toString(buildMessage());
		} catch (SOAPException e) {
			logger
					.severe("Could not serialize message due to unexpected SOAPException: "
							+ e.getMessage());
		} catch (JAXBException e) {
			logger
					.severe("Could not serialize message due to unexpected JAXBException: "
							+ e.getMessage());
		}
		return "";
	}

	
    static String toString(final Message message) {
		try {
			final XMLOutputFactory factory = XMLOutputFactory.newInstance();
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final XMLStreamWriter writer = factory.createXMLStreamWriter(bos);
			message.writeTo(writer);
			writer.flush();
			writer.close();
			return bos.toString();
		} catch (XMLStreamException e) {
			logger
					.severe("Could not serialize message due to unexpected XMLStreamException: "
							+ e.getMessage());
		}
		return "";
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
