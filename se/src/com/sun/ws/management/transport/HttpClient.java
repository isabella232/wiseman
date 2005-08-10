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
 * $Id: HttpClient.java,v 1.7 2005-08-10 01:52:42 akhilarora Exp $
 */

package com.sun.ws.management.transport;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.Message;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

public final class HttpClient {
    
    private static final Logger LOG = Logger.getLogger(HttpClient.class.getName());
    
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
    
    public static Addressing sendRequest(final Addressing msg)
    throws IOException, JAXBException, SOAPException {
        
        final String to = msg.getTo();
        if (to == null) {
            throw new IllegalArgumentException("Required Element is missing: " +
                    Addressing.TO);
        }
        
        log(msg);
        
        final URL destination = new URL(to);
        final URLConnection conn = destination.openConnection();
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Http.SOAP_MIME_TYPE_WITH_CHARSET);
        conn.setRequestProperty("Accept", Http.SOAP_MIME_TYPE_WITH_CHARSET);
        conn.setRequestProperty("User-Agent", "Sun WS-Management Java System");
        final HttpURLConnection http = (HttpURLConnection) conn;
        http.setRequestMethod("POST");
        http.setInstanceFollowRedirects(false);
        
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(conn.getOutputStream());
            msg.writeTo(os);
        } finally {
            if (os != null) { os.close(); }
        }
        
        final InputStream is;
        
        final int response = http.getResponseCode();
        if (response == HttpURLConnection.HTTP_OK) {
            is = new BufferedInputStream(http.getInputStream());
        } else if (response == HttpURLConnection.HTTP_BAD_REQUEST ||
                response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            // read the fault from the error stream
            is = new BufferedInputStream(http.getErrorStream());
        } else {
            final String detail = http.getResponseMessage();
            throw new IOException(detail == null ? Integer.toString(response) : detail);
        }
        
        final String responseType = conn.getContentType();
        if (!Http.isContentTypeAcceptable(responseType)) {
            // dump the first 4k bytes of the response for help in debugging
            if (LOG.isLoggable(Level.INFO)) {
                final byte[] buffer = new byte[4096];
                int nread = is.read(buffer);
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
        
        log(addr);
        
        return addr;
    }
    
    public static int sendResponse(final Addressing msg) throws IOException, SOAPException, JAXBException {
        
        final String to = msg.getTo();
        if (to == null) {
            throw new IllegalArgumentException("Required Element is missing: " +
                    Addressing.TO);
        }
        
        log(msg);
        
        final URL destination = new URL(to);
        final URLConnection conn = destination.openConnection();
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Http.SOAP_MIME_TYPE_WITH_CHARSET);
        conn.setRequestProperty("User-Agent", "Sun WS-Management Java System");
        final HttpURLConnection http = (HttpURLConnection) conn;
        http.setRequestMethod("POST");
        
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(conn.getOutputStream());
            msg.writeTo(os);
        } finally {
            if (os != null) { os.close(); }
        }
        
        return http.getResponseCode();
    }
    
    private static void log(final Message msg) throws IOException, SOAPException {
        // expensive serialization ahead, so check first
        if (LOG.isLoggable(Level.FINE)) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
            final byte[] content = baos.toByteArray();
            LOG.fine(new String(content));
        }
    }
}
