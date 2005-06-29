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
 * $Id: HttpClient.java,v 1.1 2005-06-29 19:18:26 akhilarora Exp $
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
import java.io.PushbackInputStream;
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
    
    private static boolean skipUnicodeBOM = false;
    
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
    
    public static void setSkipUnicodeBOM(final boolean state) {
        skipUnicodeBOM = state;
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
        
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(conn.getOutputStream());
            msg.writeTo(os);
        } finally {
            if (os != null) { os.close(); }
        }
        
        final String responseType = conn.getContentType();
        if (responseType == null) {
            throw new IOException("Content-type of response is null");
        }
        
        final InputStream is;
        
        final int response = http.getResponseCode();
        if (response == HttpURLConnection.HTTP_OK) {
            is = new BufferedInputStream(http.getInputStream());
        } else {
            final String detail = http.getResponseMessage();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Connect to " + destination + " failed with error " + response +
                        (detail == null ? "" : " - " + detail));
            }
            // could be a fault
            is = new BufferedInputStream(http.getErrorStream());
        }
        
        if (!Http.isContentTypeAcceptable(responseType)) {
            if (LOG.isLoggable(Level.FINE)) {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                final byte[] buffer = new byte[4096];
                int nread = 0;
                while ((nread = is.read(buffer)) > 0) {
                    bos.write(buffer, 0, nread);
                }
                LOG.fine("Response discarded: " + new String(bos.toByteArray()));
            }
            throw new IOException("Content-type of response not acceptable: " + responseType);
        }
        
        final Addressing addr;
        
        if (skipUnicodeBOM) {
            // there are three extra chars before the start of the soap envelope -
            // the byte order mark @see http://www.unicode.org/faq/utf_bom.html
            // TODO: handle this properly (eg set encoding to utf-16 if so indicated)
            // TODO: delete this workaround when supported properly by JAXP/SAAJ
            // Note: the problem does not manifest itself if both client and server are colocated
            int skipped = 0;
            final PushbackInputStream pbis = new PushbackInputStream(is, 4);
            try {
                // skip upto the first legal xml character
                int c = pbis.read();
                while (c != '<') {
                    c = pbis.read();
                    skipped ++;
                }
                pbis.unread(c);
                
                if (skipped > 0) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Skipped " + skipped + " bytes of Unicode BOM");
                    }
                }
                
                addr = new Addressing(pbis);
            } finally {
                if (pbis != null) { pbis.close(); }
            }
        } else {
            try {
                addr = new Addressing(is);
            } finally {
                if (is != null) { is.close(); }
            }
        }
        
        log(addr);
        
        return addr;
    }
    
    public static int sendResponse(final Addressing msg) throws IOException, SOAPException, JAXBException {
        
        log(msg);
        
        final URL destination = new URL(msg.getTo());
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
