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
 * $Id: RequestDispatcher.java,v 1.4 2005-11-08 22:40:19 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.Management;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.HttpClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

public abstract class RequestDispatcher implements Callable {
    
    private static final Logger LOG = Logger.getLogger(RequestDispatcher.class.getName());
    private static final String UUID_SCHEME = "uuid:";
    
    protected final Management request;
    protected final Management response;
    
    private boolean responseTooBig = false;
    
    public RequestDispatcher(final Management req) throws JAXBException, SOAPException {
        
        request = req;
        response = new Management();
        
        response.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        // messageId can be missing in a malformed request
        final String msgId = request.getMessageId();
        if (msgId != null) {
            response.addRelatesTo(msgId);
        }
    }
    
    public void fillReturnAddress() throws JAXBException, SOAPException {
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
    
    public void sendResponse(final OutputStream os,
      final HttpServletResponse resp, final FaultException fex, final long maxEnvelopeSize)
      throws SOAPException, JAXBException, IOException {
        
        if (fex != null) {
            new SOAP(response).setFault(fex);
        }
        
        resp.setStatus(HttpServletResponse.SC_OK);
        if (response.getBody().hasFault()) {
            new Addressing(response).setAction(Addressing.FAULT_ACTION_URI);
            
            // sender faults need to set error code to BAD_REQUEST for client errors
            if (SOAP.SENDER.equals(response.getBody().getFault().getFaultCodeAsQName())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        
        fillReturnAddress();
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeTo(baos);
        final byte[] content = baos.toByteArray();
        
        log(content);
        
        if (content.length > maxEnvelopeSize) {
            // although we check earlier that the maxEnvelopeSize is > 8192, we still
            // need to use the responseTooBig flag to break possible infinite recursion if
            // the serialization of the EncodingLimitFault happens to exceed 8192 bytes
            if (responseTooBig) {
                LOG.warning("MaxEnvelopeSize set too small to send an EncodingLimitFault");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                responseTooBig = true;
                final Node[] details = SOAP.createFaultDetail(null, Management.MAX_ENVELOPE_SIZE_DETAIL, null, null);
                sendResponse(os, resp, new EncodingLimitFault(details), maxEnvelopeSize);
            }
            return;
        }
        
        final String dest = response.getTo();
        if (Addressing.ANONYMOUS_ENDPOINT_URI.equals(dest)) {
            os.write(content);
        } else {
            final int status = sendAsyncReply(response.getTo(), content);
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Response to " + dest + " returned " + status);
            }
        }
    }
    
    protected int sendAsyncReply(final String to, final byte[] bits) throws IOException, SOAPException, JAXBException {
        return HttpClient.sendResponse(to, bits);
    }
    
    public void validateRequest()
    throws SOAPException, JAXBException, FaultException {
        request.validate();
    }
    
    private static void log(final byte[] bits) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(new String(bits));
        }
    }
}
