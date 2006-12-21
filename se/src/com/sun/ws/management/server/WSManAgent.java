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
 * $Id: WSManAgent.java,v 1.1.2.1 2006-12-21 08:24:52 jfdenise Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Message;
import com.sun.ws.management.Management;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.xml.XmlBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.UUID;
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

/**
 * WS-MAN agent decoupled from transport. Can be used in Servlet / JAX-WS / ...
 * context.
 *
 */

public abstract class WSManAgent {
    
    private static final Logger LOG = Logger.getLogger(WSManAgent.class.getName());
    private static final long DEFAULT_TIMEOUT = 30000;
    private static final long MIN_ENVELOPE_SIZE = 8192;
    private static final String PROPERTY_FILE_NAME = "/wsman.properties";
    private static final String UUID_SCHEME = "uuid:";
    
    private static final String SCHEMA_PATH =
            "/com/sun/ws/management/resources/schemas/";
    
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    
    private static final Map<QName, String> extraIdInfo = new HashMap<QName, String>();
    private static Map<String, String> properties = null;
    private static Schema schema = null;
    
    static {
        // NO MORE LOGGING CONFIGURATION
        // THE CODE HAS BEEN REMOVED
        
        // load subsystem properties and save them in a type-safe, unmodifiable Map
        final InputStream ism = Management.class.getResourceAsStream(PROPERTY_FILE_NAME);
        if (ism != null) {
            final Properties props = new Properties();
            try {
                props.load(ism);
            } catch (IOException iex) {
                LOG.log(Level.WARNING, "Error reading properties from " +
                        PROPERTY_FILE_NAME, iex);
                throw new RuntimeException("Error reading properties from " +
                        PROPERTY_FILE_NAME +  " " + iex);
            }
            final Map<String, String> propertySet = new HashMap<String, String>();
            final Iterator<Entry<Object, Object>> ei = props.entrySet().iterator();
            while (ei.hasNext()) {
                final Entry<Object, Object> entry = ei.next();
                final Object key = entry.getKey();
                final Object value = entry.getValue();
                if (key instanceof String && value instanceof String) {
                    propertySet.put((String) key, (String) value);
                }
            }
            properties = Collections.unmodifiableMap(propertySet);
        }
        
        extraIdInfo.put(Identify.BUILD_ID, properties.get("build.version"));
        extraIdInfo.put(Identify.SPEC_VERSION, properties.get("spec.version"));
        
        final SchemaFactory schemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        // The returned list of schemas is already sorted
        String schemaNames = properties.get("schemas");
        StringTokenizer t = new StringTokenizer(schemaNames, ";");
        if(LOG.isLoggable(Level.FINE))
            LOG.log(Level.FINE, t.countTokens() + " schemas to load.");
        final Source[] schemas = new Source[t.countTokens()];
        int i = 0;
        while(t.hasMoreTokens()) {
            String name = t.nextToken();
            if(LOG.isLoggable(Level.FINE))
                LOG.log(Level.FINE, "Schema to load " + SCHEMA_PATH + name);
            final InputStream xsd =
                    Management.class.getResourceAsStream(SCHEMA_PATH + name);
            if(LOG.isLoggable(Level.FINE))
                LOG.log(Level.FINE, "Loaded schema " + xsd);
            schemas[i] = new StreamSource(xsd);
            i++;
        }
        
        try {
            schema = schemaFactory.newSchema(schemas);
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, "Error setting schemas", ex);
            throw new RuntimeException("schemaFactory.newSchema failed " + ex);
        }
    }
    
    protected WSManAgent() {
        
    }
    
    /**
     * Hook your own dispatcher
     */
    abstract protected RequestDispatcher createDispatcher(final Management request,
            final HandlerContext context) throws SOAPException, JAXBException, IOException;
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    private static long getEnvelopeSize(Management request) throws JAXBException, SOAPException {
        long maxEnvelopeSize = Long.MAX_VALUE;
        final MaxEnvelopeSizeType maxSize = request.getMaxEnvelopeSize();
        if (maxSize != null) {
            maxEnvelopeSize = maxSize.getValue().longValue();
        }
        return maxEnvelopeSize;
    }
    
    public static long getValidEnvelopeSize(Management request) throws JAXBException, SOAPException {
        long maxEnvelopeSize = getEnvelopeSize(request);
        if(maxEnvelopeSize < MIN_ENVELOPE_SIZE)
            maxEnvelopeSize =  Long.MAX_VALUE;
        return maxEnvelopeSize;
    }
    /**
     * Agent request handling entry point. Return a Message due to Identify reply.
     */
    public Message handleRequest(final Management request, final HandlerContext context) {
        Addressing response = null;
       
        try {
            // XXX WARNING, CREATING A JAXBCONTEXT FOR EACH REQUEST IS TOO EXPENSIVE.
            // JAXB team says that you should share as much as you can JAXBContext.
            // I propose to make XmlBinding back to be static and locked. I mean
            // that no custom package nor schema should be added to this JAXBContext.
            // It is private to wiseman and only used to handle the protocol.
            // Any model dependent JAXB processing must be done in separate JAXBContext(s).
            // Model layer(s) can implement their own JAXBContext strategies.
            // BTW, doing so, we make clear that any rechnology can be used to marsh/unmarsh.
            // Relaying on JAXB becomes an implementation detail.
            
            // schema might be null if no XSDs were found in the war
            request.setXmlBinding(new XmlBinding(schema));
        } catch (JAXBException jex) {
            LOG.log(Level.SEVERE, "Error initializing XML Binding", jex);
            // TODO throw new ServletException(jex);
        }
        
        try {
             log(request);
             
            Identify identifyResponse = null;
            
            if ((identifyResponse = handleIfIdentify(request)) != null) {
                return identifyResponse;
            }
            
            long timeout = getTimeout(request);
            
            if((response = isValidEnvelopSize(request)) == null) {
                final RequestDispatcher dispatcher = createDispatcher(request,
                        context);
                try {
                    dispatcher.authenticate();
                    dispatcher.validateRequest();
                    response = dispatch(dispatcher, timeout);
                } catch (SecurityException sx) {
                    response = new Management();
                    response.setFault(new AccessDeniedFault());
                } catch (FaultException fex) {
                    response = new Management();
                    response.setFault(fex);
                } catch (Throwable th) {
                    response = new Management();
                    response.setFault(new InternalErrorFault(th));
                }
            }
            
            fillReturnAddress(request, response);
            response.setContentType(request.getContentType());
        }catch(Exception ex) {
            try {
                response = new Management();
                response.setFault(new InternalErrorFault(ex.getMessage()));
                fillReturnAddress(request, response);
                response.setContentType(request.getContentType());
            }catch(Exception ex2) {
                // We can't handle the internal error.
                throw new RuntimeException(ex2.getMessage());
            }
        }
        
        return response;
    }
    
    private static long getTimeout(Management request) throws Exception {
        long timeout = DEFAULT_TIMEOUT;
        final Duration timeoutDuration = request.getTimeout();
        if (timeoutDuration != null) {
            timeout = timeoutDuration.getTimeInMillis(new Date());
        }
        return timeout;
    }
    
    private static Management isValidEnvelopSize(Management request)
    throws Exception {
        Management response = null;
        long maxEnvelopeSize = getEnvelopeSize(request);
        if (maxEnvelopeSize < MIN_ENVELOPE_SIZE) {
            EncodingLimitFault fault =
                    new EncodingLimitFault("MaxEnvelopeSize is set too " +
                    "small to encode faults " +
                    "(needs to be atleast " + MIN_ENVELOPE_SIZE + ")",
                    EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE);
            response = new Management();
            response.setFault(fault);
        }
        return response;
    }
    
    private static Addressing dispatch(final Callable dispatcher, final long timeout)
    throws Throwable {
        final FutureTask<Management> task =
                new FutureTask<Management>(dispatcher);
        // the Future returned by pool.submit does not propagate
        // ExecutionException, perform the get on FutureTask itself
        pool.submit(task);
        try {
            return task.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            throw ex.getCause();
        } catch (InterruptedException ix) {
            // ignore
        } catch (TimeoutException tx) {
            throw new TimedOutFault();
        } finally {
            task.cancel(true);
        }
        return null;
    }
    
    private static Identify handleIfIdentify(final Management msg)
    throws SOAPException, JAXBException, IOException {
        final Identify identify = new Identify(msg);
        identify.setXmlBinding(msg.getXmlBinding());
        
        final SOAPElement id = identify.getIdentify();
        if (id == null) {
            return null;
        }
        final Identify response = new Identify();
        response.setXmlBinding(msg.getXmlBinding()); // TODO ???
        response.setIdentifyResponse(
                properties.get("impl.vendor") + " - " + properties.get("impl.url"),
                properties.get("impl.version"),
                Management.NS_URI,
                extraIdInfo);
        return response;
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
    
    private static void log(final Message msg) throws IOException, SOAPException {
        // expensive serialization ahead, so check first
        if (LOG.isLoggable(Level.FINE)) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
            final byte[] content = baos.toByteArray();
            
            String encoding = msg.getContentType() == null ? null : 
                msg.getContentType().getEncoding();
            
            LOG.fine("Encoding [" + encoding + "]");
            
            if(encoding == null)
                LOG.fine(new String(content));
            else
                LOG.fine(new String(content, encoding));
        }
    }
}
