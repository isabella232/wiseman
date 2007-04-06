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
 * $Id: WiseManResourceHandler.java,v 1.2 2007-04-06 13:41:55 jfdenise Exp $
 */

package com.sun.ws.management.x.jmx;

import com.sun.ws.management.Management;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.reflective.ReflectiveRequestDispatcher;
import com.sun.ws.management.server.reflective.WSManReflectiveAgent;
import com.sun.ws.management.xml.XmlBinding;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.remote.ws.JMXWSManResourceHandler;
import javax.security.auth.Subject;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

/**
 * Plug this class in JSR 262 Connector Server. See the Test directory 
 * to see how to plug it.
 * WARNING : reuse the same instance for all the resourceURI this handler 
 * is managing.
 *
 * @author jfdenise
 */
public class WiseManResourceHandler implements JMXWSManResourceHandler {
    private XmlBinding binding;
    private static final Logger LOG = 
            Logger.getLogger(JMXWSManResourceHandler.class.getName());
    /**
     * Creates a new instance of WiseManResourceProvider
     * @throws javax.xml.bind.JAXBException In case JAXB exception occurs (When reading Binding configuration)
     */
    public WiseManResourceHandler() throws JAXBException {
        this(null, null, null);
    }
    
    /**
     * Creates a new instance of WiseManResourceProvider
     * @param customSchemas Custom schemas. If you want to perform schema validation on top of your own custom JAXB packages.
     * @param bindingConf The Binding configuration. Map keys are similar to what binding.properties contain.
     * @param loader A classloader that will be used by JAXB when loading your custom packages (if any)
     * @param customPackages List of CustomPackages (: separated list).
     * @throws javax.xml.bind.JAXBException If a JAXBException occurs when handling custom packages.
     */
    public WiseManResourceHandler(final Source[] customSchemas, Map<String, String> bindingConf,
            ClassLoader loader,
            String... customPackages) throws JAXBException { 
        
        // We inject JSR262 packages
        // XXX REVISIT TODO, THIS IS A DEPENDENCY ON Sun JSR 262 RI.
        // String jsr262packages = WSEnvHelp.resolveInternalJAXBContext();
        // Then add it to XmlBinding. Not sure that it is really useful.
        // Will ask Others.
        
        binding = new XmlBinding(customSchemas, 
                bindingConf, loader, customPackages);
    }
    
    class WiseManResourceHandlerContext implements HandlerContext {
        
        private String charEncoding;
        private String contentType;
        private Principal principal;
        private Map<String, Object> requestProperties;
        private String url;
        WiseManResourceHandlerContext(final Principal principal, final String contentType,
                final String charEncoding, final String url, final Map<String, Object> requestProperties) {
            this.principal = principal;
            this.contentType = contentType;
            this.charEncoding = charEncoding;
            this.requestProperties = requestProperties;
            this.url = url;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public Principal getPrincipal() {
            return principal;
        }
        
        public String getCharEncoding() {
            return charEncoding;
        }
        
        public String getURL() {
            return url;
        }
        
        public Map<String, Object> getRequestProperties() {
            return requestProperties;
        }
    }
    
    /**
     * Called by JSR 262 Runtime when a request is received for a resource managed by this handler.
     * @param resourceURI The current requet resourceURI
     * @param msg The received SOAP message
     * @param ctx The request context
     * @throws java.lang.Exception An exception occured when dealing with the request
     * @return A valid WS-Man response message.
     */
    public SOAPMessage handle(String resourceURI, SOAPMessage msg, Map ctx) throws Exception {
         if(LOG.isLoggable(Level.FINE))
            LOG.log(Level.FINE, "Handling request for " +resourceURI);
        
        MBeanServer server = (MBeanServer) ctx.get(JMXWSManResourceHandler.JMX_WS_REQUEST_MBEAN_SERVER);
        Subject subject = (Subject) ctx.get(JMXWSManResourceHandler.JMX_WS_REQUEST_SUBJECT);
        HandlerContext context = (HandlerContext) ctx.get("com.sun.ws.management.handler.context");
        if(context == null) {
            String charEncoding =
                    (String) ctx.get(JMXWSManResourceHandler.
                    JMX_WS_REQUEST_CHAR_ENCODING);
            String contentType =
                    (String) ctx.get(JMXWSManResourceHandler.
                    JMX_WS_REQUEST_CONTENT_TYPE);
            Object webctx = ctx.get(JMXWSManResourceHandler.
                    JMX_WS_REQUEST_JAX_WS_CONTEXT);
            Principal p = (Principal)ctx.get(JMXWSManResourceHandler.
                    JMX_WS_REQUEST_PRINCIPAL);
            
            Map<String, Object> requestProperties =
                    (Map<String, Object>)ctx.get(JMXWSManResourceHandler.
                    JMX_WS_REQUEST_PROPERTIES);
            Object servletctx =
                    ctx.get(JMXWSManResourceHandler.JMX_WS_REQUEST_SERVLET_CONTEXT);
            String url =
                    (String) ctx.get(JMXWSManResourceHandler.JMX_WS_REQUEST_URL);
            context = 
                    new WiseManResourceHandlerContext(p, contentType,
                    charEncoding, url,
                    requestProperties == null ? new HashMap<String, Object>() :
                        requestProperties);
        }
        
        Map props = context.getRequestProperties();
        props.put(HandlerContext.MBEAN_SERVER, server);
        props.put(HandlerContext.SUBJECT, subject);
        
        Management mgt = (Management) ctx.get("com.sun.ws.management.request");
        if(mgt == null) {
            mgt = new Management(msg);
            
        }
        
        // inject Binding
        mgt.setXmlBinding(binding);
        
        ReflectiveRequestDispatcher req = 
                new ReflectiveRequestDispatcher(mgt, context);
        req.authenticate();
        req.validateRequest();
        Management reply = req.call();
        return reply.getMessage();
    }
    
    public static Map<QName, String> getAdditionalIdentifyElements() {
        Map<QName, String> ret = 
                WSManReflectiveAgent.getMetadataConfiguration(null);
         if(LOG.isLoggable(Level.FINE))
            LOG.log(Level.FINE, "Getting additional elements " + ret);
        
        return ret;
    }
}
