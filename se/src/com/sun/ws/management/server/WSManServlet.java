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
 * $Id: WSManServlet.java,v 1.11 2006-04-11 21:20:41 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Message;
import com.sun.ws.management.Management;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.Http;
import com.sun.ws.management.xml.XmlBinding;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2005._06.management.MaxEnvelopeSizeType;

public class WSManServlet extends HttpServlet {
    
    private static final Logger LOG = Logger.getLogger(WSManServlet.class.getName());
    private static final long DEFAULT_TIMEOUT = 30000;
    private static final long MIN_ENVELOPE_SIZE = 8192;
    
    private static final String THIS =
            "<This xmlns=\"http://schemas.xmlsoap.org/ws/2005/02/management\"> \n" +
            "<Vendor>The Wiseman Project. https://wiseman.dev.java.net</Vendor> \n" +
            "<Version>0.3</Version> \n" +
            "</This> ";
    
    private static final Properties wsmanProperties = new Properties();
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    
    public static Map<Object, Object> getProperties() {
        return Collections.unmodifiableMap(wsmanProperties);
    }
    
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
        
        final InputStream ism = WSManServlet.class.getResourceAsStream("/wsman.properties");
        if (ism != null) {
            try {
                wsmanProperties.load(ism);
            } catch (IOException iex) {
                LOG.log(Level.WARNING, "Error reading wsman properties", iex);
                throw new ServletException(iex);
            }
        }
        
        try {
            SOAP.setXmlBinding(new XmlBinding());
        } catch (JAXBException jex) {
            LOG.log(Level.SEVERE, "Error initializing XML Binding", jex);
            throw new ServletException(jex);
        }
    }
    
    public String getServletInfo() {
        return THIS;
    }
    
    public void doGet(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        
        doPost(req, resp);
    }
    
    public void doPost(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        
        if (!Http.isContentTypeAcceptable(req.getContentType())) {
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(Http.SOAP_MIME_TYPE_WITH_CHARSET);
        final OutputStream os = new BufferedOutputStream(resp.getOutputStream());
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final InputStream is = new BufferedInputStream(req.getInputStream());
        
        try {
            handle(is, bos, req, resp);
            final byte[] content = bos.toByteArray();
            resp.setContentLength(content.length);
            os.write(content);
        } catch (Throwable th) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, th.getMessage());
            LOG.log(Level.WARNING, th.getMessage(), th);
        } finally {
            os.flush();
            os.close();
        }
    }
    
    protected RequestDispatcher createDispatcher(final Management request,
            final HttpServletRequest req) throws SOAPException, JAXBException, IOException {
        return new ReflectiveRequestDispatcher(request, req);
    }
    
    protected void handle(final InputStream is, final OutputStream os,
            final HttpServletRequest req, final HttpServletResponse resp)
            throws SOAPException, JAXBException, IOException {
        
        final Management request = new Management(is);
        log(request);
        
        long timeout = DEFAULT_TIMEOUT;
        final Duration timeoutDuration = request.getTimeout();
        if (timeoutDuration != null) {
            timeout = timeoutDuration.getTimeInMillis(new Date());
        }
        
        final RequestDispatcher dispatcher = createDispatcher(request, req);
        
        long maxEnvelopeSize = Long.MAX_VALUE;
        final MaxEnvelopeSizeType maxSize = request.getMaxEnvelopeSize();
        if (maxSize != null) {
            maxEnvelopeSize = maxSize.getValue();
        }
        if (maxEnvelopeSize < MIN_ENVELOPE_SIZE) {
            final Node[] details =
                    SOAP.createFaultDetail("MaxEnvelopeSize is set too small to encode faults " +
                    "(needs to be atleast " + MIN_ENVELOPE_SIZE + ")", Management.MIN_ENVELOPE_LIMIT_DETAIL, null, null);
            dispatcher.sendResponse(os, resp, new EncodingLimitFault(details), Long.MAX_VALUE);
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
