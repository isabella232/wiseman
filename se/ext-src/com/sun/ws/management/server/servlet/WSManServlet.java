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
 * $Id: WSManServlet.java,v 1.2 2007-04-24 03:49:51 simeonpinder Exp $
 */

package com.sun.ws.management.server.servlet;

import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.Message;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.HandlerContextImpl;
import com.sun.ws.management.server.WSManAgent;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.transport.HttpClient;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 * Rewritten WSManServlet that delegates to a WSManAgent instance.
 *
 */
public abstract class WSManServlet extends HttpServlet {
    
    private static final Logger LOG = Logger.getLogger(WSManServlet.class.getName());
    
    // This class implements all the WS-Man logic decoupled from transport
    
    WSManAgent agent;
    
    public void init() throws ServletException {
        Schema schema = null;
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final ServletContext context = getServletContext();
        final Set<String> xsdLocSet = context.getResourcePaths("/xsd");
        Source[] schemas = null;
        if (xsdLocSet != null && xsdLocSet.size() > 0) {
            // sort the list of XSD documents so that dependencies come first
            // it is assumed that the files are named in the desired loading order
            // for example, 1-xml.xsd, 2-soap.xsd, 3-addressing.xsd...
            List<String> xsdLocList = new ArrayList<String>(xsdLocSet);
            Collections.sort(xsdLocList);
            schemas = new Source[xsdLocList.size()];
            final Iterator<String> xsdLocIterator = xsdLocList.iterator();
            for (int i = 0; xsdLocIterator.hasNext(); i++) {
                final String xsdLoc = xsdLocIterator.next();
                final InputStream xsd = context.getResourceAsStream(xsdLoc);
                schemas[i] = new StreamSource(xsd);
                if(LOG.isLoggable(Level.FINE))
                    LOG.log(Level.FINE, "Custom schema " + xsdLoc);
            }
        }
        try {
            agent = createWSManAgent(schemas);
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, "Error setting schemas", ex);
            throw new ServletException(ex);
        }
    }
    
    protected abstract WSManAgent createWSManAgent(Source[] schemas) throws SAXException;
    
    public void doGet(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        
        doPost(req, resp);
    }
    
    public void doPost(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        
        final ContentType contentType = ContentType.createFromHttpContentType(req.getContentType());
        if (contentType==null||!contentType.isAcceptable()) {
        	boolean isWsdlOrSchemaRequest = processForWsdlOrSchemaRequest(req);
        	if(!isWsdlOrSchemaRequest){
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
        	}else{
        	  //insert method to generate HTTP response here	
        	  processAsHttpRequest(req,resp,contentType);	
        	}
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
    
    /**Process the http request.
     * 
     * @param req
     * @param resp
     * @throws IOException 
     */
    private void processAsHttpRequest(HttpServletRequest req, 
    		HttpServletResponse resp,ContentType contentType) throws IOException {
    	//indicate that we agree to process
        resp.setStatus(HttpServletResponse.SC_OK);
        if(contentType!=null){
          resp.setContentType(contentType.toString());
        }
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(req.getInputStream());
            os = new BufferedOutputStream(resp.getOutputStream());
            
            String contentype = req.getContentType();
            final Principal user = req.getUserPrincipal();
            String charEncoding = req.getCharacterEncoding();
            String url = req.getRequestURL().toString();
            Map<String, Object> props = new HashMap<String, Object>(1);
            props.put(HandlerContext.SERVLET_CONTEXT, getServletContext());
//            final HandlerContext context = new HandlerContextImpl(user, contentype,
//            		charEncoding, url, props, agent.getProperties());
            final HandlerContext context = new HandlerContextImpl(user, contentype,
                    charEncoding, url, props);
            
            String requestURI = req.getRequestURI();
            if((requestURI!=null)&&(requestURI.startsWith(req.getContextPath()))){
            	//removing the contextPath in it's entirety
            	requestURI = requestURI.substring(req.getContextPath().trim().length());
            }
            
            ServletContext srvContext = getServletContext();
             InputStream inputStream = 
            	 srvContext.getResourceAsStream(requestURI);
             Set paths = srvContext.getResourcePaths(requestURI);
             if(inputStream==null){
            	resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            	String htmlResponse = "";
            	  String title="'"+requestURI+"' was not found.";
            	  String body="<center><h1>"+HttpServletResponse.SC_NOT_FOUND+
            	     ": File Not Found</h1></center><br></br>";
            	  body+="<center>The resource <b>'"+requestURI+req.getContextPath()+
            	  		"'</b> that you requested could not be found.";
            	  body+="<br></br> Please check that the requested URL is correct.</center>";
            		htmlResponse=generateHtmlResponse(requestURI,title,body);
            	inputStream = new ByteArrayInputStream(htmlResponse.getBytes());
             }
//             //Then the user has put in the directory request
//             if((paths!=null)&&(!paths.isEmpty())){
//             	resp.setStatus(HttpServletResponse.SC_FOUND);
//            	String htmlResponse = "";
//            	  String title="File(s) list for '"+requestURI;
//            	  Iterator iter = paths.iterator();
//            	  String body ="<b>File(s) found:</b><br></br><ul>";
//            	  while (iter.hasNext()) {
//					String file = (String) iter.next();
//            		  file = file.trim();
//            		  if(file.lastIndexOf("/")==file.length()-1){
//            			 //is directory 
//            			 body+="<li><a href=\""+file+"\">"+file+"</a></li>"; 
//            		  }else{
//            			 body+="<li>"+file+"</li>"; 
//            		  }
//            	  }
//            	  if(paths.isEmpty()){
//            		 body+="<li>(No files to display)</li>"; 
//            	  }
//            	  body+="</ul>";
//            	 htmlResponse=generateHtmlResponse(requestURI,title,body);
//            	inputStream = new ByteArrayInputStream(htmlResponse.getBytes());
//             }
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
               String line = null;
             while((line=br.readLine())!=null){
          	    bos.write(line.getBytes());
             }
             
             br.close();
             br = null;
             inputStream.close();
             inputStream = null;
            
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

	private String generateHtmlResponse(String resourceURI,String title,String body) {
		String htmlResponse = "";
			htmlResponse+="<HTML>";
			htmlResponse+="<HEAD><title>"+title+"</title></HEAD>";
			htmlResponse+="<BODY>"+body;
			htmlResponse+="</BODY>";
			htmlResponse+="</HTML>";
		return htmlResponse;
	}

	private boolean processForWsdlOrSchemaRequest(HttpServletRequest req) {
    	boolean isWsdlSchemaReq = false;
    	if(req!=null){
    	   //parse the request URI for /wsdl* or /schema*
    	   //if exists then set to true
    	   String requestUri = req.getRequestURI();
    	   if((requestUri!=null)&&(requestUri.trim().length()>0)){
    		  //disregard spaces and case
    		  requestUri = requestUri.toLowerCase().trim();
    		  //check for /wsdl or /schema
    	   	  if((requestUri.indexOf("/wsdl")>0)||
    	   		 (requestUri.indexOf("/schema")>0)){
    	   		 isWsdlSchemaReq = true; 
    	   	  }
    	   }else{
	    	  String msg="This servlet container does not expose the standard field ";
	    	  msg+="'HttpServletRequest.requestUri'. Unable to proceed.";
	    	  throw new RuntimeException(msg);
    	   }
    	}else{
    	  String msg="HttpServleRequest passed in cannot be NULL.";
    	  throw new IllegalArgumentException(msg);
    	}
		return isWsdlSchemaReq;
	}

    private void handle(final InputStream is, final ContentType contentType,
            final OutputStream os, final HttpServletRequest req, final HttpServletResponse resp)
            throws SOAPException, JAXBException, IOException {
        
        final Management request = new Management(is);
        request.setXmlBinding(agent.getXmlBinding());
        
        request.setContentType(contentType);
        
        String contentype = req.getContentType();
        final Principal user = req.getUserPrincipal();
        String charEncoding = req.getCharacterEncoding();
        String url = req.getRequestURL().toString();
        Map<String, Object> props = new HashMap<String, Object>(1);
        props.put(HandlerContext.SERVLET_CONTEXT, getServletContext());
        final HandlerContext context = new HandlerContextImpl(user, contentype,
                charEncoding, url, props);
        
        Message response = agent.handleRequest(request, context);
        
        sendResponse(response, os, resp, agent.getValidEnvelopeSize(request));
    }
    
    private static void sendResponse(final Message response, final OutputStream os,
            final HttpServletResponse resp,  final long maxEnvelopeSize)
            throws SOAPException, JAXBException, IOException {
        
        if(response instanceof Identify) {
            response.writeTo(os);
            return;
        }
        
        Management mgtResp = (Management) response;
        
        sendResponse(mgtResp, os, resp, maxEnvelopeSize, false);
    }
    
    private static void sendResponse(final Management response, final OutputStream os,
            final HttpServletResponse resp, final long maxEnvelopeSize,
            boolean responseTooBig) throws SOAPException, JAXBException,
            IOException {
        
        resp.setStatus(HttpServletResponse.SC_OK);
        if (response.getBody().hasFault()) {
            // sender faults need to set error code to BAD_REQUEST for client errors
            if (SOAP.SENDER.equals(response.getBody().getFault().getFaultCodeAsQName())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeTo(baos);
        final byte[] content = baos.toByteArray();
        
        // response being null means that no reply is to be sent back. 
        // The reply has been handled asynchronously
        if(response != null)
             os.write(content);
    }
}
