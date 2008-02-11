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

package com.sun.ws.management.client.impl.j2se;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.Message;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.xml.bind.api.JAXBRIContext;

/**
 * 
 * JAX-WS Message API based implementation.
 */
class J2SESOAPRequest implements SOAPRequest {

	private final static Logger logger = Logger
			.getLogger("com.sun.ws.management.client");

	private Object payload;
	private JAXBRIContext payloadContext;
	
	private final XmlBinding binding;
	private final JAXBRIContext jaxbContext;
	private final List<Object> headers = new ArrayList<Object>();
	private final Map<String, String> namespaces;
	private final Map<String, ?> context;


	public J2SESOAPRequest(String endpoint, Map<String, ?> context,
			QName serviceName, QName portName, XmlBinding binding) throws IOException, SOAPException {
		
		this.binding = binding;
		this.jaxbContext = (JAXBRIContext) binding.getJAXBContext();
		this.namespaces = new HashMap<String, String>();
		this.context = context;
	}

	public J2SESOAPRequest(EndpointReferenceType epr, Map<String, ?> context,
			XmlBinding binding) throws IOException, SOAPException {
		
		this.binding = binding;
		this.jaxbContext = (JAXBRIContext) binding.getJAXBContext();
		this.namespaces = new HashMap<String, String>();
		this.context = context;
	}

	public void addHeader(final Object header, final JAXBContext ctx)
			throws JAXBException {
		// TODO: Save the context too
		this.headers.add(header);
	}
	
	public void setPayload(final Object content, final JAXBContext ctx) {
		payload = content;
		payloadContext = (ctx != null) ? (JAXBRIContext) ctx : jaxbContext;
	}

	public void addNamespaceDeclaration(final String prefix, final String uri) {
		this.namespaces.put(prefix, uri);
	}

	public void addNamespaceDeclarations(Map<String, String> declarations) {
		if ((declarations != null) && (declarations.size() > 0))
			this.namespaces.putAll(declarations);
	}

	public SOAPResponse invoke() throws SOAPException, JAXBException, IOException {
		final Addressing request = buildRequestMessage();
		final Addressing response = sendRequest(request.getMessage(), request.getTo());
		
		return new J2SESOAPResponse(response, binding, context);
	}

	public void invokeOneWay() throws SOAPException, JAXBException, IOException {
		final Addressing request = buildRequestMessage();
		sendOneWayRequest(request.getMessage(), request.getTo());
	}
	
	public Map<String, ?> getRequestContext() {
		return context;
	}
	
	public XmlBinding getXmlBinding() {
		return binding;
	}
	
	private Addressing buildRequestMessage() throws SOAPException, JAXBException {
		final Addressing request = new Addressing();
		request.setXmlBinding(binding);
		request.addNamespaceDeclarations(this.namespaces);
		addMsgHeaders(headers, request);
		if (payload != null)
			payloadContext.createMarshaller().marshal(payload, request.getBody());
		return request;
	}
	
    private static void addMsgHeaders(final List<Object> anyList, final Addressing msg) throws JAXBException {
        if (anyList == null) {
            return;
        }

        final XmlBinding binding = msg.getXmlBinding();
        final Node header = msg.getHeader();
        for (final Object any : anyList) {
            if (any instanceof Node) {
                Node node = (Node) any;
                NodeList existingHeaders = null;
                Node hNode =null;
                //prevent duplicate additions.
                if(((existingHeaders = header.getChildNodes())!=null)
                		&&(existingHeaders.getLength()>0)){
                   for (int i = 0; i < existingHeaders.getLength(); i++) {
					 hNode = existingHeaders.item(i);
					 if((node.getNamespaceURI().equals(hNode.getNamespaceURI()))&
						(node.getLocalName().equals(hNode.getLocalName()))){
						header.removeChild(hNode);
					 }
                   }
                }
                // NOTE: can be a performance hog if the node is deeply nested
                header.appendChild(header.getOwnerDocument().importNode(node, true));
            } else {
            	try {
                    binding.marshal(any, header);
            	}
                catch (JAXBException e) {
                    throw new RuntimeException("RefP " + any.toString() +
                            " of class " + any.getClass() + " is being ignored");
                }
            }
        }
    }
	
    private static Addressing sendRequest(final SOAPMessage msg,
    									  final String destination) //,
    //		final byte[] username, final byte[] password)
    throws IOException, SOAPException, JAXBException {

		if (logger.isLoggable(Level.FINE))
			logger.fine("<request>\n" + msg + "</request>\n");
        final HttpURLConnection http = initRequest(destination,
                ContentType.createFromEncoding((String) msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING)));
        /*
        if ((username != null) && (username.length > 0)) {
    		final ByteArrayOutputStream credentials = new ByteArrayOutputStream();
    		final Base64OutputStream b64 = new Base64OutputStream(credentials);
    		b64.write(username);
    		b64.write(":".getBytes("UTF8"));
    		b64.write(password);
    		b64.close();
    		http.setRequestProperty("Authorization", "Basic " + credentials.toString("UTF8"));
        }
        */
        transfer(http, msg);
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
			final String destination) // ,
			// final byte[] username, final byte[] password)
			throws IOException, SOAPException, JAXBException {
		if (logger.isLoggable(Level.FINE))
			logger.fine("<request>\n" + msg + "</request>\n");
        final HttpURLConnection http = initRequest(destination,
                ContentType.createFromEncoding((String) msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING)));
        /*
        if ((username != null) && (username.length > 0)) {
    		final ByteArrayOutputStream credentials = new ByteArrayOutputStream();
    		final Base64OutputStream b64 = new Base64OutputStream(credentials);
    		b64.write(username);
    		b64.write(":".getBytes("UTF8"));
    		b64.write(password);
    		b64.close();
    		http.setRequestProperty("Authorization", "Basic " + credentials.toString("UTF8"));
        }
        */
        transfer(http, msg);
		int rc = http.getResponseCode();
		final Addressing response = readResponse(http);
		if (response != null) {
			if (logger.isLoggable(Level.FINE)) {
				if (response.getBody() != null) {
					if (response.getBody().hasFault())
						logger.fine("<fault>\n" + response + "</fault>\n");
					else
						logger.fine("<response>\n" + response + "</response>\n");
				}
			}
		}
		return rc;
	}
    
    private static HttpURLConnection initRequest(final String destination, final ContentType contentType)
    throws IOException {

        final HttpURLConnection http = initConnection(destination, contentType);
        http.setRequestProperty("Accept", ContentType.ACCEPTABLE_CONTENT_TYPES);
        http.setInstanceFollowRedirects(false);
        return http;
    }
    
    private static HttpURLConnection initConnection(final String to, final ContentType ct) throws IOException {
        if (to == null) {
            throw new IllegalArgumentException("Required Element is missing: " +
                    Addressing.TO);
        }

        final URL dest = new URL(to);
        final URLConnection conn = dest.openConnection();

        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type",
                ct == null ? ContentType.DEFAULT_CONTENT_TYPE.toString() : ct.toString());
        // TODO: get this from the properties
        conn.setRequestProperty("User-Agent", "https://wiseman.dev.java.net");

        final HttpURLConnection http = (HttpURLConnection) conn;
        http.setRequestMethod("POST");

        return http;
    }
    
    // type of data can be Message or byte[], others will throw IllegalArgumentException
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
                throw new IllegalArgumentException("Type of data not handled: " +
                        data.getClass().getName());
            }
        } finally {
            if (os != null) { os.close(); }
        }
    }
    
    private static Addressing readResponse(final HttpURLConnection http)
    throws IOException, SOAPException {

        final InputStream is;
        final int response = http.getResponseCode();
        if (response == HttpURLConnection.HTTP_OK) {
            is = http.getInputStream();
        } else if (response == HttpURLConnection.HTTP_BAD_REQUEST ||
                response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            // read the fault from the error stream
            is = http.getErrorStream();
        } else {
            final String detail = http.getResponseMessage();
            throw new IOException(detail == null ? Integer.toString(response) : detail);
        }

        final String responseType = http.getContentType();
        final ContentType contentType = ContentType.createFromHttpContentType(responseType);
        if (contentType==null||!contentType.isAcceptable()) {
            // dump the first 4k bytes of the response for help in debugging
            if ((logger.isLoggable(Level.INFO)) && (null != is)) {
                final byte[] buffer = new byte[4096];
                final int nread = is.read(buffer);
                if (nread > 0) {
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
                    bos.write(buffer, 0, nread);
                    logger.info("Response discarded: " + new String(bos.toByteArray()));
                }
            }
            throw new IOException("Content-Type of response is not acceptable: " + responseType);
        }

        final Addressing addr;
        try {
            addr = new Addressing(is, responseType);
        } finally {
            if (is != null) { is.close(); }
        }

        addr.setContentType(contentType);
        return addr;
    }
}
