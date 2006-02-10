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
 * $Id: EventsReceiverServlet.java,v 1.2 2006-02-10 01:15:05 akhilarora Exp $
 */

package server;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class EventsReceiverServlet extends HttpServlet {
    
    private static final Logger LOG =
            Logger.getLogger(EventsReceiverServlet.class.getName());
    
    private final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder db = null;
        
    public void init(ServletConfig config) throws ServletException {
        docFactory.setNamespaceAware(true);
        try {
            db = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException px) {
            throw new ServletException(px);
        }
    }
    
    public void doGet(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        
        doPost(req, resp);
    }
    
    public void doPost(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        
        resp.setStatus(HttpServletResponse.SC_OK);
        try {
            handle(req, resp);
        } catch (Throwable th) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, th.getMessage());
            LOG.log(Level.WARNING, th.getMessage(), th);
        }
    }
    
    protected void handle(final HttpServletRequest req, final HttpServletResponse resp)
    throws IOException, SAXException, ParserConfigurationException {
        
        final InputStream is = new BufferedInputStream(req.getInputStream());
        final Document doc = db.parse(is);
        final OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(72);
        format.setIndenting(true);
        format.setIndent(2);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final XMLSerializer serializer = new XMLSerializer(os, format);
        serializer.serialize(doc);
        final String event = new String(os.toString("utf-8"));
        LOG.info("Got an event: " + event);
    }
}
