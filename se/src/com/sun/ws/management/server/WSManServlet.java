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
 * $Id: WSManServlet.java,v 1.27.2.1 2006-12-21 08:24:52 jfdenise Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.Message;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.transport.HttpClient;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

/**
 * Rewritten WSManServlet that delegates to a WSManAgent instance.
 * 
 */
public class WSManServlet extends HttpServlet {
  
    private static final Logger LOG = Logger.getLogger(WSManServlet.class.getName());
    
    // This class implements all the WS-Man logic decoupled from transport
    
    WSManAgent agent;
    
    public void init() throws ServletException {
        agent = createWSManAgent();
    }
    
    protected WSManAgent createWSManAgent() {
        // It is an extension of WSManAgent to handle Reflective Dispatcher
        return new WSManReflectiveAgent();
    }
    
    public void doGet(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        
        doPost(req, resp);
    }
    
    public void doPost(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        
        final ContentType contentType = ContentType.createFromHttpContentType(req.getContentType());
        if (contentType==null||!contentType.isAcceptable()) {
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(contentType.toString());
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(req.getInputStream());
            os = new BufferedOutputStream(resp.getOutputStream());
            handle(is, contentType, bos, req, resp);
            final byte[] content = bos.toByteArray();
            resp.setContentLength(content.length);
            os.write(content);
        } catch (Throwable th) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, th.getMessage());
            LOG.log(Level.WARNING, th.getMessage(), th);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }
    
    protected void handle(final InputStream is, final ContentType contentType,
            final OutputStream os, final HttpServletRequest req, final HttpServletResponse resp)
            throws SOAPException, JAXBException, IOException {
        
        final Management request = new Management(is);
        request.setContentType(contentType); 
        
        log(request);
        
        String contentype = req.getContentType();
        final Principal user = req.getUserPrincipal();
        String charEncoding = req.getCharacterEncoding();
        String url = req.getRequestURL().toString();
        Map<String, Object> props = new HashMap<String, Object>(1);
        props.put(HandlerContext.SERVLET_CONTEXT, getServletContext());
        final HandlerContext context = new HandlerContextImpl(user, contentype, 
                charEncoding, url, props, agent.getProperties());
        
        Message response = agent.handleRequest(request, context);

        sendResponse(response, os, resp, agent.getValidEnvelopeSize(request));
    }
    
     private static void sendResponse(final Message response, final OutputStream os,
            final HttpServletResponse resp,  final long maxEnvelopeSize)
            throws SOAPException, JAXBException, IOException {
         
            if(response instanceof Identify) {
                response.writeTo(os);
                return;
            }
            
            if(!(response instanceof Management))
                throw new IllegalArgumentException(" Invalid internal response " +
                        "message " + response);
            
            Management mgtResp = (Management) response;
            
            sendResponse(mgtResp, os, resp, null, maxEnvelopeSize, false);
     }
     
     private static void sendResponse(final Management response, final OutputStream os,
            final HttpServletResponse resp, final FaultException fex, final long maxEnvelopeSize, 
             boolean responseTooBig) throws SOAPException, JAXBException, 
             IOException {     
         
        if (fex != null)
            response.setFault(fex);
        
        resp.setStatus(HttpServletResponse.SC_OK);
        if (response.getBody().hasFault()) {
            // sender faults need to set error code to BAD_REQUEST for client errors
            if (SOAP.SENDER.equals(response.getBody().getFault().getFaultCodeAsQName())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeTo(baos);
        final byte[] content = baos.toByteArray();
        
        log(response);
        
        if (content.length > maxEnvelopeSize) {
            
            // although we check earlier that the maxEnvelopeSize is > 8192, we still
            // need to use the responseTooBig flag to break possible infinite recursion if
            // the serialization of the EncodingLimitFault happens to exceed 8192 bytes
            if (responseTooBig) {
                LOG.warning("MaxEnvelopeSize set too small to send an EncodingLimitFault");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                sendResponse(response, os, resp,
                        new EncodingLimitFault(Integer.toString(content.length),
                        EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE_EXCEEDED), maxEnvelopeSize, true);
            }
            return;
        }

        
        final String dest = response.getTo();
        if (Addressing.ANONYMOUS_ENDPOINT_URI.equals(dest)) {
            os.write(content);
        } else {
            final int status = sendAsyncReply(response.getTo(), content, response.getContentType());
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Response to " + dest + " returned " + status);
            }
        }
    }
    
    protected static int sendAsyncReply(final String to, final byte[] bits, final ContentType contentType)
    throws IOException, SOAPException, JAXBException {
        return HttpClient.sendResponse(to, bits, contentType);
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
