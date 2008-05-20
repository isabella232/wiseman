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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.ws.management.Message;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.message.api.client.soap.SOAPRequest;
import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.spi.client.WSManStub;
import com.sun.ws.management.spi.client.WSManStubConstants;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.xml.XmlBinding;

/**
 * 
 * J2SE SPI implementation.
 */
public class J2SEWSManStub extends WSManStub {

	private final static Logger logger = Logger.getLogger(J2SEWSManStub.class
			.getCanonicalName());

	private final static String DEFAULT_USERAGENT = "https://wiseman.dev.java.net";

	public J2SEWSManStub() {
		super();
	}

	public SOAPResponse invoke(final SOAPRequest soapReq) throws SOAPException,
			JAXBException, IOException {
		final Addressing request = buildMessage(soapReq);
		final Addressing response = sendRequest(request, soapReq
				.getRequestContext(), soapReq.getAction());

		return new J2SESOAPResponse(response, soapReq.getXmlBinding(), soapReq
				.getRequestContext());
	}

	public void invokeOneWay(final SOAPRequest soapReq) throws SOAPException,
			JAXBException, IOException {
		final Addressing addrReq = buildMessage(soapReq);
		sendOneWayRequest(addrReq.getMessage(), addrReq.getTo(), soapReq
				.getRequestContext(), soapReq.getAction());
	}

	public void writeTo(final SOAPRequest soapReq, final OutputStream os,
			final boolean formatted) throws Exception {
		final Addressing msg = buildMessage(soapReq);
		if (formatted == true) {
			msg.prettyPrint(os);
		} else {
			msg.writeTo(os);
		}
	}

	// Private implementation methods follow

	private static Addressing buildMessage(final SOAPRequest soapReq)
			throws SOAPException, JAXBException {
		final Addressing request = new Addressing();
		request.setXmlBinding(soapReq.getXmlBinding());
		request.addNamespaceDeclarations(soapReq.getNamespaceDeclarations());
		addMsgHeaders(soapReq.getHeaders(), request);
		if (soapReq.getPayload() != null)
			soapReq.getXmlBinding()
					.marshal(soapReq.getPayload(), request.getBody());
		return request;
	}

	private static void addMsgHeaders(final List<Object> anyList,
			final Addressing msg) throws JAXBException {
		if (anyList == null) {
			return;
		}

		final XmlBinding binding = msg.getXmlBinding();
		final Node header = msg.getHeader();
		for (final Object any : anyList) {
			if (any instanceof Node) {
				Node node = (Node) any;
				NodeList existingHeaders = null;
				Node hNode = null;
				// prevent duplicate additions.
				if (((existingHeaders = header.getChildNodes()) != null)
						&& (existingHeaders.getLength() > 0)) {
					for (int i = 0; i < existingHeaders.getLength(); i++) {
						hNode = existingHeaders.item(i);
						if ((node.getNamespaceURI().equals(hNode
								.getNamespaceURI()))
								& (node.getLocalName().equals(hNode
										.getLocalName()))) {
							header.removeChild(hNode);
						}
					}
				}
				// NOTE: can be a performance hog if the node is deeply nested
				header.appendChild(header.getOwnerDocument().importNode(node,
						true));
			} else {
				try {
					binding.marshal(any, header);
				} catch (JAXBException e) {
					throw new RuntimeException("RefP " + any.toString()
							+ " of class " + any.getClass()
							+ " is being ignored");
				}
			}
		}
	}

	private static Addressing sendRequest(final Addressing request,
			final Map<String, ?> context, final String action)
			throws IOException, SOAPException, JAXBException {

		if (logger.isLoggable(Level.FINE))
			logger.fine("<request>\n" + request + "</request>\n");

		final String destination = request.getTo();
		final HttpURLConnection http = initRequest(destination, ContentType
				.createFromEncoding((String) context
						.get(WSManStubConstants.CHARACTER_SET_ENCODING)),
				context);

		// Set the SOAPAction header
		http.setRequestProperty("SOAPAction", action);

		// Set any basic authentication credentials
		final String credentials = getCredentials(context);
		if ((credentials != null) && (credentials.length() > 0))
			http.setRequestProperty("Authorization", "Basic " + credentials);

		transfer(http, request);
		final Addressing response = readResponse(http);
		if (logger.isLoggable(Level.FINE)) {
			if (response.getBody().hasFault())
				logger.fine("<fault>\n" + response + "</fault>\n");
			else
				logger.fine("<response>\n" + response + "</response>\n");
		}
		return response;
	}

	private static int sendOneWayRequest(final SOAPMessage msg,
			final String destination, final Map<String, ?> context,
			final String action) throws IOException, SOAPException,
			JAXBException {
		if (logger.isLoggable(Level.FINE))
			logger.fine("<request>\n" + msg + "</request>\n");
		final HttpURLConnection http = initRequest(destination, ContentType
				.createFromEncoding((String) context
						.get(WSManStubConstants.CHARACTER_SET_ENCODING)),
				context);

		// Set the SOAPAction header
		http.setRequestProperty("SOAPAction", action);

		// Set any basic authentication credentials
		final String credentials = getCredentials(context);
		if ((credentials != null) && (credentials.length() > 0))
			http.setRequestProperty("Authorization", "Basic " + credentials);

		transfer(http, msg);
		int rc = http.getResponseCode();
		final Addressing response = readResponse(http);
		if (response != null) {
			if (logger.isLoggable(Level.FINE)) {
				if (response.getBody() != null) {
					if (response.getBody().hasFault())
						logger.fine("<fault>\n" + response + "</fault>\n");
					else
						logger
								.fine("<response>\n" + response
										+ "</response>\n");
				}
			}
		}
		return rc;
	}

	private static String getCredentials(final Map<String, ?> context)
			throws UnsupportedEncodingException, IOException {
		final String username = (String) context
				.get(WSManStubConstants.USERNAME_PROPERTY);
		final String password = (String) context
				.get(WSManStubConstants.PASSWORD_PROPERTY);

		if ((username != null) && (username.length() > 0)) {
			final ByteArrayOutputStream credentials = new ByteArrayOutputStream();
			final Base64OutputStream b64 = new Base64OutputStream(credentials);
			b64.write(username.getBytes("UTF8"));
			b64.write(":".getBytes("UTF8"));
			b64.write(password.getBytes("UTF8"));
			b64.close();
			return credentials.toString("UTF8");
		}
		return null;
	}

	private static HttpURLConnection initRequest(final String destination,
			final ContentType contentType, final Map<String, ?> context)
			throws IOException {

		final HttpURLConnection http = initConnection(destination, contentType,
				context);
		http.setRequestProperty("Accept", ContentType.ACCEPTABLE_CONTENT_TYPES);
		http.setInstanceFollowRedirects(false);
		return http;
	}

	private static HttpURLConnection initConnection(final String to,
			final ContentType ct, final Map<String, ?> context)
			throws IOException {
		if (to == null) {
			throw new IllegalArgumentException("Required Element is missing: "
					+ Addressing.TO);
		}

		final URL dest = new URL(to);
		final URLConnection conn = dest.openConnection();

		conn.setAllowUserInteraction(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type",
				ct == null ? ContentType.DEFAULT_CONTENT_TYPE.toString() : ct
						.toString());

		String useragent = (String) context
				.get(WSManStubConstants.USERAGENT_PROPERTY);
		if ((useragent == null) || (useragent.length() == 0))
			useragent = DEFAULT_USERAGENT;
		conn.setRequestProperty("User-Agent", useragent);

		final HttpURLConnection http = (HttpURLConnection) conn;
		http.setRequestMethod("POST");

		return http;
	}

	// type of data can be Message or byte[], others will throw
	// IllegalArgumentException
	private static void transfer(final URLConnection conn, final Object data)
			throws IOException, SOAPException, JAXBException {
		OutputStream os = null;
		try {
			os = conn.getOutputStream();
			if (data instanceof Message) {
				((Message) data).writeTo(os);
			} else if (data instanceof SOAPMessage) {
				((SOAPMessage) data).writeTo(os);
			} else if (data instanceof byte[]) {
				os.write((byte[]) data);
			} else {
				throw new IllegalArgumentException("Type of data not handled: "
						+ data.getClass().getName());
			}
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	private static Addressing readResponse(final HttpURLConnection http)
			throws IOException, SOAPException {

		final InputStream is;
		final int response = http.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK) {
			is = http.getInputStream();
		} else if (response == HttpURLConnection.HTTP_BAD_REQUEST
				|| response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
			// read the fault from the error stream
			is = http.getErrorStream();
		} else {
			final String detail = http.getResponseMessage();
			throw new IOException(detail == null ? Integer.toString(response)
					: detail);
		}

		final String responseType = http.getContentType();
		final ContentType contentType = ContentType
				.createFromHttpContentType(responseType);
		if (contentType == null || !contentType.isAcceptable()) {
			// dump the first 4k bytes of the response for help in debugging
			if ((logger.isLoggable(Level.INFO)) && (null != is)) {
				final byte[] buffer = new byte[4096];
				final int nread = is.read(buffer);
				if (nread > 0) {
					final ByteArrayOutputStream bos = new ByteArrayOutputStream(
							buffer.length);
					bos.write(buffer, 0, nread);
					logger.info("Response discarded: "
							+ new String(bos.toByteArray()));
				}
			}
			throw new IOException(
					"Content-Type of response is not acceptable: "
							+ responseType);
		}

		final Addressing addr;
		try {
			addr = new Addressing(is, responseType);
		} finally {
			if (is != null) {
				is.close();
			}
		}

		addr.setContentType(contentType);
		return addr;
	}
}
