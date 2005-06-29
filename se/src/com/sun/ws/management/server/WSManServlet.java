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
 * $Id: WSManServlet.java,v 1.1 2005-06-29 19:18:24 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Message;
import com.sun.ws.management.Management;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transport.Http;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

public class WSManServlet extends HttpServlet {
    
    private static final Logger LOG = Logger.getLogger(WSManServlet.class.getName());
    
    private static final String THIS =
            "<This xmlns=\"http://schemas.xmlsoap.org/ws/2005/02/management\"> \n" +
            "<Vendor> Sun Microsystems, Inc. http://www.sun.com </Vendor> \n" +
            "<Version> 0.2 </Version> \n" +
            "</This> ";
    
    private static final Properties wsmanProperties = new Properties();
    
    public static Map<Object, Object> getProperties() {
        return Collections.unmodifiableMap(wsmanProperties);
    }
    
    public void init() throws ServletException {
        final InputStream isl = WSManServlet.class.getResourceAsStream("/log.properties");
        if (isl != null) {
            try {
                LogManager.getLogManager().readConfiguration(isl);
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
        return new ReflectiveRequestDispatcher(request);
    }
    
    protected void handle(final InputStream is, final OutputStream os,
            final HttpServletRequest req, final HttpServletResponse resp)
            throws SOAPException, JAXBException, IOException {
        
        final Management request = new Management(is);
        log(request);
        
        final RequestDispatcher dispatcher = createDispatcher(request, req);
        try {
            dispatcher.validateRequest();
            dispatcher.dispatch();
            dispatcher.sendResponse(os, resp, null);
        } catch (FaultException fex) {
            dispatcher.sendResponse(os, resp, fex);
        } catch (Throwable th) {
            log(th.getMessage(), th);
            dispatcher.sendResponse(os, resp, new InternalErrorFault(th));
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
