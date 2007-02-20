/*
 * Copyright 2005 Sun Microsystems, Inc.
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
 * $Id: HttpClient.java,v 1.16.4.1 2007-02-20 12:15:09 denis_rachal Exp $
 */

package com.sun.ws.management.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.sun.ws.management.Message;
import com.sun.ws.management.addressing.Addressing;

public final class HttpClient {
    
    private static final Logger LOG = Logger.getLogger(HttpClient.class.getName());
    
    private HttpClient() {}
    
    public static void setAuthenticator(final Authenticator auth) {
        Authenticator.setDefault(auth);
    }
    
    public static void setTrustManager(final X509TrustManager trustManager)
    throws NoSuchAlgorithmException, KeyManagementException {
        
        final TrustManager[] tm = { trustManager };
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }
    
    public static void setHostnameVerifier(final HostnameVerifier hv) {
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
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
    
    public static Addressing sendRequest(final SOAPMessage msg, final String destination)
    throws IOException, SOAPException, JAXBException {
        
        log(msg);
        
        final HttpURLConnection http = initRequest(destination,
                ContentType.createFromEncoding((String) msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING)));
        transfer(http, msg);
        return readResponse(http);
    }

    public static Addressing sendRequest(final SOAPMessage msg, final String destination,Entry<String, String>... headers)
    throws IOException, SOAPException, JAXBException {
        
        log(msg);
        
        final HttpURLConnection http = initRequest(destination,
                ContentType.createFromEncoding((String) msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING)));
        if (headers != null) {
            for (Entry<String,String> entry: headers) {
                http.setRequestProperty(entry.getKey(),entry.getValue());
            }
        }
        
        transfer(http, msg);
        return readResponse(http);
    }

    
    public static Addressing sendRequest(final Addressing msg, final
    		Entry<String, String>... headers)
    throws IOException, JAXBException, SOAPException {
        
        log(msg);
        
        final HttpURLConnection http = initRequest(msg.getTo(), msg.getContentType());
        
        if (headers != null) {
            for (Entry<String,String> entry: headers) {
                http.setRequestProperty(entry.getKey(),entry.getValue());
            }
        }
        
        transfer(http, msg);
        final Addressing response = readResponse(http);
        response.setXmlBinding(msg.getXmlBinding());
        return response;
    }
    
    private static HttpURLConnection initRequest(final String destination, final ContentType contentType)
    throws IOException {
        
        final HttpURLConnection http = initConnection(destination, contentType);
        http.setRequestProperty("Accept", ContentType.ACCEPTABLE_CONTENT_TYPES);
        http.setInstanceFollowRedirects(false);
        return http;
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
            if (LOG.isLoggable(Level.INFO)) {
                final byte[] buffer = new byte[4096];
                final int nread = is.read(buffer);
                if (nread > 0) {
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
                    bos.write(buffer, 0, nread);
                    LOG.info("Response discarded: " + new String(bos.toByteArray()));
                }
            }
            throw new IOException("Content-Type of response is not acceptable: " + responseType);
        }
        
        final Addressing addr;
        try {
            addr = new Addressing(is);
        } finally {
            if (is != null) { is.close(); }
        }
        
        addr.setContentType(contentType);
        log(addr);
        
        return addr;
    }
    
    public static int sendResponse(final String to, final byte[] bits, final ContentType contentType)
    throws IOException, SOAPException, JAXBException {
        log(bits);
        final HttpURLConnection http = initConnection(to, contentType);
        transfer(http, bits);
        return http.getResponseCode();
    }
    
    public static int sendResponse(final Addressing msg) throws IOException, SOAPException, JAXBException {
        log(msg);
        final HttpURLConnection http = initConnection(msg.getTo(), msg.getContentType());
        transfer(http, msg);
        return http.getResponseCode();
    }
    
    private static void log(final Addressing msg) throws IOException, SOAPException {
        // expensive serialization ahead, so check first
        if (LOG.isLoggable(Level.FINE)) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
            final byte[] content = baos.toByteArray();
            final ContentType type = msg.getContentType();
            LOG.fine(type == null ? new String(content) : new String(content, type.getEncoding()));
        }
    }
    
    private static void log(final SOAPMessage msg) throws IOException, SOAPException {
        // expensive serialization ahead, so check first
        if (LOG.isLoggable(Level.FINE)) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
            final byte[] content = baos.toByteArray();
            final String encoding = (String) msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING);
            LOG.fine(encoding == null ? new String(content) : new String(content, encoding));
        }
    }
    
    private static void log(final byte[] bits) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(new String(bits));
        }
    }
}