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
 * $Id: WSManServlet.java,v 1.27 2006-11-15 15:06:39 obiwan314 Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Message;
import com.sun.ws.management.Management;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.xml.XmlBinding;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
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

public class WSManServlet extends HttpServlet {
    
    private static final Logger LOG = Logger.getLogger(WSManServlet.class.getName());
    private static final long DEFAULT_TIMEOUT = 30000;
    private static final long MIN_ENVELOPE_SIZE = 8192;
    private static final String PROPERTY_FILE_NAME = "/wsman.properties";
    
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final Map<QName, String> extraIdInfo = new HashMap<QName, String>();
    private Map<String, String> properties = null;
    private Schema schema = null;
    
    public void init() throws ServletException {
        final InputStream isl = WSManServlet.class.getResourceAsStream("/log.properties");
        if (isl != null) {
            try {
                LogManager.getLogManager().readConfiguration(isl);
            } catch (AccessControlException aex) {
                // we get this when running in the sun app server, not fatal
                getServletContext().log("Warning: unable to read log configuration: " + aex.getMessage());
            } catch (IOException iex) {
                LOG.log(Level.WARNING, "Error reading log configuration", iex);
            }
        }

        // load subsystem properties and save them in a type-safe, unmodifiable Map
        final InputStream ism = WSManServlet.class.getResourceAsStream(PROPERTY_FILE_NAME);
        if (ism != null) {
            final Properties props = new Properties();
            try {
                props.load(ism);
            } catch (IOException iex) {
                LOG.log(Level.WARNING, "Error reading properties from " + PROPERTY_FILE_NAME, iex);
                throw new ServletException(iex);
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
        
        try {
            SOAP.initialize();
        } catch (SOAPException ex) {
            LOG.log(Level.SEVERE, "Error initializing SOAP Message", ex);
            throw new ServletException(ex);
        }
        
        
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final ServletContext context = getServletContext();
        final Set<String> xsdLocSet = context.getResourcePaths("/xsd");
        
        if (xsdLocSet != null && xsdLocSet.size() > 0) {
            // sort the list of XSD documents so that dependencies come first
            // it is assumed that the files are named in the desired loading order
            // for example, 1-xml.xsd, 2-soap.xsd, 3-addressing.xsd...
            List<String> xsdLocList = new ArrayList<String>(xsdLocSet);
            Collections.sort(xsdLocList);
            final Source[] schemas = new Source[xsdLocList.size()];
            final Iterator<String> xsdLocIterator = xsdLocList.iterator();
            for (int i = 0; xsdLocIterator.hasNext(); i++) {
                final String xsdLoc = xsdLocIterator.next();
                final InputStream xsd = context.getResourceAsStream(xsdLoc);
                schemas[i] = new StreamSource(xsd);
            }
            
            try {
                schema = schemaFactory.newSchema(schemas);
            } catch (SAXException ex) {
                LOG.log(Level.SEVERE, "Error setting schemas", ex);
                throw new ServletException(ex);
            }
        }

        try {
            BaseSupport.initialize();
            EnumerationSupport.initialize();
        } catch (DatatypeConfigurationException dex) {
            LOG.log(Level.SEVERE, "Error initializing Support", dex);
            throw new ServletException(dex);
        }
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
    
    protected RequestDispatcher createDispatcher(final Management request,
            final HandlerContext context) throws SOAPException, JAXBException, IOException {
        return new ReflectiveRequestDispatcher(request, context);
    }
    
    protected void handle(final InputStream is, final ContentType contentType,
            final OutputStream os, final HttpServletRequest req, final HttpServletResponse resp)
            throws SOAPException, JAXBException, IOException {
        
        final Management request = new Management(is);
        request.setContentType(contentType);
        try {
            // schema might be null if no XSDs were found in the war
        	request.setXmlBinding(new XmlBinding(schema));
        } catch (JAXBException jex) {
            LOG.log(Level.SEVERE, "Error initializing XML Binding", jex);
            // TODO throw new ServletException(jex);
        }        log(request);
        
        if (handleIfIdentify(request, os)) {
            return;
        }
        
        long timeout = DEFAULT_TIMEOUT;
        final Duration timeoutDuration = request.getTimeout();
        if (timeoutDuration != null) {
            timeout = timeoutDuration.getTimeInMillis(new Date());
        }

        final HandlerContext context = 
                new HandlerContextImpl(req, getServletConfig(), properties);
        final RequestDispatcher dispatcher = createDispatcher(request, context);
        
        long maxEnvelopeSize = Long.MAX_VALUE;
        final MaxEnvelopeSizeType maxSize = request.getMaxEnvelopeSize();
        if (maxSize != null) {
            // NOTE: potential loss of precision: conversion from BigInteger to long
            maxEnvelopeSize = maxSize.getValue().longValue();
        }
        if (maxEnvelopeSize < MIN_ENVELOPE_SIZE) {
            dispatcher.sendResponse(os, resp,
                    new EncodingLimitFault("MaxEnvelopeSize is set too small to encode faults " +
                    "(needs to be atleast " + MIN_ENVELOPE_SIZE + ")",
                    EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE), Long.MAX_VALUE);
            return;
        }
        
        try {
            dispatcher.authenticate();
            dispatcher.validateRequest();
            dispatch(dispatcher, timeout);
            dispatcher.sendResponse(os, resp, null, maxEnvelopeSize);
        } catch (SecurityException sx) {
            dispatcher.sendResponse(os, resp, new AccessDeniedFault(), maxEnvelopeSize);
        } catch (FaultException fex) {
            dispatcher.sendResponse(os, resp, fex, maxEnvelopeSize);
        } catch (Throwable th) {
            log(th.getMessage(), th);
            dispatcher.sendResponse(os, resp, new InternalErrorFault(th), maxEnvelopeSize);
        }
    }
    
    private void dispatch(final RequestDispatcher dispatcher, final long timeout) throws Throwable {
        final FutureTask<?> task = new FutureTask<Object>(dispatcher);
        // the Future returned by pool.submit does not propagate
        // ExecutionException, perform the get on FutureTask itself
        pool.submit(task);
        try {
            task.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            throw ex.getCause();
        } catch (InterruptedException ix) {
            // ignore
        } catch (TimeoutException tx) {
            throw new TimedOutFault();
        } finally {
            task.cancel(true);
        }
    }
    
    private boolean handleIfIdentify(final Management msg, final OutputStream os)
    throws SOAPException, JAXBException, IOException {
        final Identify identify = new Identify(msg);
 
       	identify.setXmlBinding(msg.getXmlBinding());
        log(identify);        
        
        final SOAPElement id = identify.getIdentify();
        if (id == null) {
            return false;
        }
        final Identify response = new Identify();
        response.setXmlBinding(msg.getXmlBinding()); // TODO ???
        response.setIdentifyResponse(
                properties.get("impl.vendor") + " - " + properties.get("impl.url"),
                properties.get("impl.version"),
                Management.NS_URI,
                extraIdInfo);
        response.writeTo(os);
        return true;
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