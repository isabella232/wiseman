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
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.17  2007/06/15 12:13:20  jfdenise
 **Cosmetic change. Make OPERATION_TIMEOUT_DEFAULT public and added a trace.
 **
 **Revision 1.16  2007/05/30 20:31:04  nbeers
 **Add HP copyright header
 **
 **
 * $Id: WSManAgent.java,v 1.18 2007-10-30 09:27:47 jfdenise Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.server.message.SAAJMessage;
import com.sun.ws.management.server.message.WSManagementRequest;
import com.sun.ws.management.server.message.WSManagementResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

/**
 * WS-MAN agent decoupled from transport. Can be used in Servlet / JAX-WS / ...
 * context.
 *
 */

public abstract class WSManAgent extends WSManAgentSupport {
    private static final Logger LOG = Logger.getLogger(WSManAgent.class.getName());
    private static final String UUID_SCHEME = "uuid:";
    
    private class RequestDispatcherWrapper extends WSManRequestDispatcher {
        private RequestDispatcher dispatcher;
        RequestDispatcherWrapper(RequestDispatcher dispatcher) throws JAXBException, SOAPException {
            super(new SAAJMessage(dispatcher.request), 
                  new SAAJMessage(dispatcher.response), 
                    dispatcher.context);
            this.dispatcher = dispatcher;
        }
        
        public Object call() throws Exception {
            return new SAAJMessage((Management)dispatcher.call());
        }
    }
    
    protected WSManAgent() throws SAXException {
         super();
    }
    protected WSManAgent(Map<String,String> wisemanConf, Source[] customSchemas,
            Map<String,String> bindingConf)
            throws SAXException {
        super(wisemanConf, customSchemas, bindingConf);
    }
    protected WSManRequestDispatcher createDispatcher(WSManagementRequest request,
            WSManagementResponse response,
            HandlerContext context) throws Exception {
        return new RequestDispatcherWrapper(createDispatcher(new Management(request.toSOAPMessage()), 
                context));
    }
    public long getValidEnvelopeSize(Management request) throws JAXBException, SOAPException {
        return getEnvelopeSize(new SAAJMessage((request)));
    }
    /**
     * Hook your own dispatcher
     * @param agent
     */
    abstract protected RequestDispatcher createDispatcher(final Management request,
            final HandlerContext context) throws SOAPException, JAXBException,
            IOException;
    
    /**
     * Agent request handling entry point. Return a Message due to Identify reply.
     */
    public Message handleRequest(final Management request, final HandlerContext context) {
        try {
            SAAJMessage req = new SAAJMessage(request);
            SAAJMessage resp = new SAAJMessage(new Management());
            
            WSManagementResponse response = handleRequest(req, resp, context);
            Management saajResponse = new Management(response.toSOAPMessage());
            fillReturnAddress(request, saajResponse);
            if(LOG.isLoggable(Level.FINE))
                LOG.log(Level.FINE, "Request / Response content type " +
                        request.getContentType());
            saajResponse.setContentType(request.getContentType());
            
            Message ret = handleResponse(saajResponse, getEnvelopeSize(req));
            return ret;
        }catch(Exception ex) {
            try {
                Management response = new Management();
                if(ex instanceof SecurityException)
                    response.setFault(new AccessDeniedFault());
                else
                    if(ex instanceof FaultException)
                        response.setFault((FaultException)ex);
                    else
                        response.setFault(new InternalErrorFault(ex)); 
                
                return response;
            }catch(Exception ex2) {
                throw new RuntimeException(ex2.toString());
            }
        }
    }
    
    private static void fillReturnAddress(Addressing request,
            Addressing response)
            throws JAXBException, SOAPException {
        response.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        // messageId can be missing in a malformed request
        final String msgId = request.getMessageId();
        if (msgId != null) {
            response.addRelatesTo(msgId);
        }
        
        if (response.getBody().hasFault()) {
            final EndpointReferenceType faultTo = request.getFaultTo();
            if (faultTo != null) {
                response.setTo(faultTo.getAddress().getValue());
                response.addHeaders(faultTo.getReferenceParameters());
                return;
            }
        }
        
        final EndpointReferenceType replyTo = request.getReplyTo();
        if (replyTo != null) {
            response.setTo(replyTo.getAddress().getValue());
            response.addHeaders(replyTo.getReferenceParameters());
            return;
        }
        
        final EndpointReferenceType from = request.getFrom();
        if (from != null) {
            response.setTo(from.getAddress().getValue());
            response.addHeaders(from.getReferenceParameters());
            return;
        }
        
        response.setTo(Addressing.ANONYMOUS_ENDPOINT_URI);
    }
    
    private static Message handleResponse(final Message response,
            final long maxEnvelopeSize) throws SOAPException, JAXBException,
            IOException {
        
        if(response instanceof Identify) {
            return response;
        }
        
        if(!(response instanceof Management))
            throw new IllegalArgumentException(" Invalid internal response " +
                    "message " + response);
        
        Management mgtResp = (Management) response;
        return handleResponse(mgtResp, null, maxEnvelopeSize, false);
    }
    
    private static Message handleResponse(final Management response,
            final FaultException fex, final long maxEnvelopeSize,
            boolean responseTooBig) throws SOAPException, JAXBException,
            IOException {
        if (fex != null)
            response.setFault(fex);
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeTo(baos);
        final byte[] content = baos.toByteArray();
        
        logMessage(LOG, response);
        
        if (content.length > maxEnvelopeSize) {
            
            // although we check earlier that the maxEnvelopeSize is > 8192, we still
            // need to use the responseTooBig flag to break possible infinite recursion if
            // the serialization of the EncodingLimitFault happens to exceed 8192 bytes
            if (responseTooBig) {
                LOG.warning("MaxEnvelopeSize set too small to send an EncodingLimitFault");
                // Let's try the underlying stack to send the reply. Best effort
            } else {
                if(LOG.isLoggable(Level.FINE))
                    LOG.log(Level.FINE, "Response actual size is bigger than maxSize.");
                handleResponse(response,
                        new EncodingLimitFault(Integer.toString(content.length),
                        EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE_EXCEEDED), maxEnvelopeSize, true);
            }
        }else
            if(LOG.isLoggable(Level.FINE))
                LOG.log(Level.FINE, "Response actual size is smaller than maxSize.");
        
        
        final String dest = response.getTo();
        if (!Addressing.ANONYMOUS_ENDPOINT_URI.equals(dest)) {
            if(LOG.isLoggable(Level.FINE))
                LOG.log(Level.FINE, "Non anonymous reply to send to : " + dest);
            final int status = sendAsyncReply(response.getTo(), content, response.getContentType());
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Response to " + dest + " returned " + status);
            }
            return null;
        }
        
        if(LOG.isLoggable(Level.FINE))
            LOG.log(Level.FINE, "Anonymous reply to send.");
        
        return response;
    }
    
    private static int sendAsyncReply(final String to, final byte[] bits, final ContentType contentType)
    throws IOException, SOAPException, JAXBException {
        return HttpClient.sendResponse(to, bits, contentType);
    }
    
    static void logMessage(Logger logger,
            final Message msg) throws IOException, SOAPException {
        // expensive serialization ahead, so check first
        if (logger.isLoggable(Level.FINE)) {
            if(msg == null) {
                logger.fine("Null message to log. Reply has perhaps been " +
                        "sent asynchronously");
                return;
            }
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
            final byte[] content = baos.toByteArray();
            
            String encoding = msg.getContentType() == null ? null :
                msg.getContentType().getEncoding();
            
            logger.fine("Encoding [" + encoding + "]");
            
            if(encoding == null)
                logger.fine(new String(content));
            else
                logger.fine(new String(content, encoding));
            
        }
    }
}
