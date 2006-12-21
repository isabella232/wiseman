/*
 * WSManJAXWSEndpoint.java
 *
 * Created on October 27, 2005, 10:47 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.ws.management.server;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.xml.XmlBinding;
import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;
import java.lang.management.ManagementFactory;
import javax.annotation.Resource;
import javax.management.MBeanServer;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;

/**
 * JAX-WS Compliant endpoint.
 */
@WebServiceProvider()
@ServiceMode(value=Service.Mode.MESSAGE)
public class WSManJAXWSEndpoint implements Provider<SOAPMessage> {
    
    private static final Logger LOG = Logger.getLogger(WSManJAXWSEndpoint.class.getName());
    private WSManAgent agent;
    // JAXWS Context injection
    @Resource
    private WebServiceContext context;
    
    /**
     * JAX-WS Endpoint constructor
     */
    public WSManJAXWSEndpoint() {
    }
    
    public SOAPMessage invoke(SOAPMessage message) {
        Management request = null;
        HandlerContext ctx = null;
        
        try {
            request = new Management(message);
            
            Principal principal = getWebServiceContext().getUserPrincipal();
            String contentType = ContentType.DEFAULT_CONTENT_TYPE.getMimeType();
            String encoding = request.getContentType() == null ? null : request.getContentType().getEncoding();
            String url = request.getTo();
            Object servletContext = getWebServiceContext().getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            WebServiceContext webctx = getWebServiceContext();
            Map<String, Object> props = new HashMap<String, Object>(1);
            props.put(HandlerContext.SERVLET_CONTEXT, servletContext);
            props.put(HandlerContext.JAX_WS_CONTEXT, webctx);
            
            
             if (LOG.isLoggable(Level.FINE))
                LOG.fine("Context properties : " + " contentType " + contentType + 
                        ", encoding " + encoding + ", url " + url + ", servletContext"
                        + servletContext + ", webctx " + 
                        webctx);

            ctx = new HandlerContextImpl(principal, contentType, encoding, url, props,
                    getAgent().getProperties());
        }catch(Exception ex) {
            try {
                Management response = new Management();
                response.setFault(new InternalErrorFault(ex.getMessage()));
                return response.getMessage();
            }catch(Exception ex2) {
                // We can't handle the internal error.
                throw new RuntimeException(ex2.getMessage());
            }
        }
        Message reply = getAgent().handleRequest(request, ctx);
        
        return reply.getMessage();
    }
    
    private synchronized WSManAgent getAgent() {
        if(agent == null)
            agent = createWSManAgent();
        return agent;
    }
    
    /*
     * In case this class is extended, Annotations @WebServiceProvider()
     * @ServiceMode(value=Service.Mode.MESSAGE) @Resource must be set on the extended class
     * or JAX-WS will not recognize the endpoint as being a valid JAX-WS endpoint.
     * This method is used to retrieve the injected resource of the extended class.
     */
    protected WebServiceContext getWebServiceContext() {
        return context;
    }
    
    protected WSManAgent createWSManAgent() {
        // It is an extension of WSManAgent to handle Reflective Dispatcher
        return new WSManReflectiveAgent();
    }
}
