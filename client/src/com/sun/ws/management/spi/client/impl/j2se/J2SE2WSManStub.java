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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3._2003._05.soap_envelope.Body;
import org.w3._2003._05.soap_envelope.Envelope;
import org.w3._2003._05.soap_envelope.Header;
import org.w3._2003._05.soap_envelope.ObjectFactory;

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
public class J2SE2WSManStub extends WSManStub {

	private final static Logger logger = Logger.getLogger(J2SE2WSManStub.class
			.getCanonicalName());

	private final static String DEFAULT_USERAGENT = "https://wiseman.dev.java.net";
	private final static ObjectFactory FACTORY = new ObjectFactory();

	public J2SE2WSManStub() {
		super();
	}

	public SOAPResponse invoke(final SOAPRequest soapReq) throws SOAPException,
			JAXBException, IOException {
		final Envelope request = buildMessage(soapReq);
		return sendRequest(request, soapReq);
	}

	public void invokeOneWay(final SOAPRequest soapReq) throws SOAPException,
			JAXBException, IOException {
		final Envelope request = buildMessage(soapReq);
		sendOneWayRequest(request, soapReq);
	}

	public void writeTo(final SOAPRequest soapReq, final OutputStream os,
			final boolean formatted) throws Exception {
		
		final Map<String, String> namespaces = soapReq.getNamespaceDeclarations();
		final Map<String, String> preferred = new HashMap<String, String>(namespaces.size());
		
		// TODO: This won't work if the URI is declared with 2 different prefixes.
		// Reverse the map
		for (final String prefix : namespaces.keySet()) {
			preferred.put(namespaces.get(prefix), prefix);
		}
		
		final Envelope envelope = buildMessage(soapReq);
		final JAXBElement<Envelope> jaxb = FACTORY.createEnvelope(envelope);
		soapReq.getXmlBinding().marshal(jaxb, os, true, preferred);
	}

	// Private implementation methods follow

	private static Envelope buildMessage(final SOAPRequest soapReq)
			throws SOAPException, JAXBException {
		final Envelope envelope = FACTORY.createEnvelope();
		final Header header = FACTORY.createHeader();
		final Body body = FACTORY.createBody();
		envelope.setHeader(header);
		envelope.setBody(body);
		header.getAny().addAll(soapReq.getHeaders());
		if (soapReq.getPayload() != null)
			body.getAny().add(soapReq.getPayload());
		return envelope;
	}

	private static J2SESOAPResponse_1 sendRequest(final Envelope reqEnvelope,
			final SOAPRequest soapReq) throws IOException, SOAPException,
			JAXBException {

		if (logger.isLoggable(Level.FINE)) {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				soapReq.writeTo(bos, true);
				logger.fine("<request>\n" + bos.toString() + "</request>\n");
			} catch (Exception e) {
				// Ignore
			}
		}

		final String destination = soapReq.getTo();
		final Map<String, ?> context = soapReq.getRequestContext();
		final String action = soapReq.getAction();

		// Open the connection
		final HttpURLConnection http = initRequest(destination, ContentType
				.createFromEncoding((String) context
						.get(WSManStubConstants.CHARACTER_SET_ENCODING)), context);

		// Set the SOAPAction header
		http.setRequestProperty("SOAPAction", action);

		// Set any basic authentication credentials
		final String credentials = getCredentials(context);
		if ((credentials != null) && (credentials.length() > 0))
			http.setRequestProperty("Authorization", "Basic " + credentials);

		// Get the preferred namespace declarations
		final Map<String, String> namespaces = soapReq.getNamespaceDeclarations();
		final Map<String, String> preferred = new HashMap<String, String>(namespaces.size());
		
		// TODO: This won't work if the URI is declared with 2 different prefixes.
		// Reverse the map
		for (final String prefix : namespaces.keySet()) {
			preferred.put(namespaces.get(prefix), prefix);
		}
		
		// Send the request
		transfer(http, reqEnvelope, soapReq.getXmlBinding(), preferred);

		// Get the response
		final Envelope respEnvelope = readResponse(http, soapReq);
		final J2SESOAPResponse_1 response = new J2SESOAPResponse_1(
				respEnvelope, soapReq.getXmlBinding(), soapReq
						.getRequestContext());

		if (response.isFault()) {
			try {
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				soapReq.writeTo(bos, true);
				logger.fine("<fault>\n" + bos.toString() + "</fault>\n");
			} catch (Exception e) {
				// Ignore
			}
		} else {
			if (logger.isLoggable(Level.FINE)) {
				try {
					final ByteArrayOutputStream bos = new ByteArrayOutputStream();
					soapReq.writeTo(bos, true);
					logger.fine("<response>\n" + bos.toString()
							+ "</response>\n");
				} catch (Exception e) {
					// Ignore
				}
				logger.fine("<response>\n" + respEnvelope.toString()
						+ "</response>\n");
			}
		}
		return response;
	}

	private static int sendOneWayRequest(final Envelope reqEnvelope,
			final SOAPRequest soapReq) throws IOException, SOAPException,
			JAXBException {

		if (logger.isLoggable(Level.FINE)) {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				soapReq.writeTo(bos, true);
				logger.fine("<request>\n" + bos.toString() + "</request>\n");
			} catch (Exception e) {
				// Ignore
			}
		}

		final String destination = soapReq.getTo();
		final Map<String, ?> context = soapReq.getRequestContext();
		final String action = soapReq.getAction();

		// Open the connection
		final HttpURLConnection http = initRequest(destination, ContentType
				.createFromEncoding((String) context
						.get(WSManStubConstants.CHARACTER_SET_ENCODING)), context);

		// Set the SOAPAction header
		http.setRequestProperty("SOAPAction", action);

		// Set any basic authentication credentials
		final String credentials = getCredentials(context);
		if ((credentials != null) && (credentials.length() > 0))
			http.setRequestProperty("Authorization", "Basic " + credentials);
		// Get the preferred namespace declarations
		final Map<String, String> namespaces = soapReq.getNamespaceDeclarations();
		final Map<String, String> preferred = new HashMap<String, String>(namespaces.size());
		
		// TODO: This won't work if the URI is declared with 2 different prefixes.
		// Reverse the map
		for (final String prefix : namespaces.keySet()) {
			preferred.put(namespaces.get(prefix), prefix);
		}
		
		// Send the request
		transfer(http, reqEnvelope, soapReq.getXmlBinding(), preferred);
		int rc = http.getResponseCode();
		if (rc < 299)
			logger.fine("HTTP response code: " + rc);
		else
			logger.severe("HTTP response code: " + rc);
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

		String useragent = (String) context.get(WSManStubConstants.USERAGENT_PROPERTY);
		if ((useragent == null) || (useragent.length() == 0))
			useragent = DEFAULT_USERAGENT;
		conn.setRequestProperty("User-Agent", useragent);

		final HttpURLConnection http = (HttpURLConnection) conn;
		http.setRequestMethod("POST");

		return http;
	}

	// type of data can be Message or byte[], others will throw
	// IllegalArgumentException
	private static void transfer(final URLConnection conn, final Object data,
			final XmlBinding binding, Map<String, String> namespaces) throws IOException, SOAPException,
			JAXBException {
		OutputStream os = null;
		try {
			os = conn.getOutputStream();
			if (data instanceof Envelope) {
				final Envelope envelope = (Envelope) data;
				final JAXBElement<Envelope> jaxb = FACTORY
						.createEnvelope(envelope);
				binding.marshal(jaxb, os, false, namespaces);
			} else if (data instanceof Message) {
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

	private static Envelope readResponse(final HttpURLConnection http,
			final SOAPRequest soapReq) throws IOException, SOAPException,
			JAXBException {

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

		final JAXBElement<Envelope> soapResp;
		try {
			soapResp = (JAXBElement<Envelope>) soapReq.getXmlBinding().unmarshal(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return soapResp.getValue();
	}
}
