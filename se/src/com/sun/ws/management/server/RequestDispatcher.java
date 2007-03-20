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
 * $Id: RequestDispatcher.java,v 1.12 2007-03-20 20:35:38 simeonpinder Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.Management;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.transport.HttpClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

public abstract class RequestDispatcher implements Callable {
    
    private static final Logger LOG = Logger.getLogger(RequestDispatcher.class.getName());
    private static final String UUID_SCHEME = "uuid:";

    protected final HandlerContext context;
    protected final Management request;
    protected final Management response;
    
    protected final WSManAgent dispatchingAgent;
    
    public RequestDispatcher(final Management req, final HandlerContext ctx, 
    		final WSManAgent rootAgent) 
    throws JAXBException, SOAPException {
        
    	dispatchingAgent = rootAgent;
        request = req;
        context = ctx;
        response = new Management();
        response.setXmlBinding(req.getXmlBinding());

        final ContentType contentType = 
                ContentType.createFromHttpContentType(
                context.getContentType());
        response.setContentType(contentType);
    }
    
    public void validateRequest()
    throws SOAPException, JAXBException, FaultException {
        request.validate();
    }
    
    public void authenticate() throws SecurityException, JAXBException, SOAPException {
    	
    	final Principal user = context.getPrincipal();
    	final String resource = request.getResourceURI();
    	// TODO: perform access control, throw SecurityException to deny access
    }

    /** This method is called when processing Identify requests for 
     *  each request dispatcher.  Modify this method to adjust the Identify
     *  processing functionality, but you may be able to simply override 
     *  the getAdditionalIdentifyElements() method to add your own custom 
     *  elements.  
     * 
     * @return Identify  This is the Identify instance to be returned to identify Request
     * @throws SecurityException
     * @throws JAXBException
     * @throws SOAPException
     */
    public Identify processForIdentify() throws SecurityException, JAXBException, SOAPException {
    	//Test for identify message
        final Identify identify = new Identify(request);
        identify.setXmlBinding(request.getXmlBinding());
        
        final SOAPElement id = identify.getIdentify();
        if (id == null) {
            return null;//else exit
        }
        
    	//As this is an indentify message then populate the response.
        Identify response = new Identify();
        response.setXmlBinding(request.getXmlBinding()); 
        response.setIdentifyResponse(
            dispatchingAgent.getProperties().get("impl.vendor") + " - " + 
            		dispatchingAgent.getProperties().get("impl.url"),
            dispatchingAgent.getProperties().get("impl.version"),
            Management.NS_URI,
            getAdditionalIdentifyElements());

        return response;
    }
    
    /** Override this method to define additional Identify elements
     *  to be returned.  This method is usually called in processForIdentify()
     *  method to add additional nodes.
     * 
     * @return Map containing information to simple xml nodes.
     */
    public Map<QName, String> getAdditionalIdentifyElements(){
    	Map<QName, String> additional = null;
    	return additional;
    }
    
    private void log(final byte[] bits) throws UnsupportedEncodingException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(new String(bits, context.getCharEncoding()));
        }
    }
}
