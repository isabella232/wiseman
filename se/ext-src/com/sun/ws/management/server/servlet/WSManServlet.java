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
 * $Id: WSManServlet.java,v 1.3 2007-04-26 08:34:27 denis_rachal Exp $
 */

package com.sun.ws.management.server.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.HandlerContextImpl;
import com.sun.ws.management.server.WSManAgent;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.ContentType;

/**
 * Rewritten WSManServlet that delegates to a WSManAgent instance.
 *
 */
public abstract class WSManServlet extends HttpServlet {
    
    private static final Logger LOG = Logger.getLogger(WSManServlet.class.getName());
    private static Properties wisemanProperties = null;
    private static final String WISEMAN_PROPERTY_FILE_NAME = "/wiseman.properties";
    private static final String SERVICE_WSDL = "service.wsdl";
    private static final String SERVICE_XSD = "service.xsd";
    
    // This class implements all the WS-Man logic decoupled from transport
    
    WSManAgent agent;
    
    public void init() throws ServletException {
		Schema schema = null;
		final SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final ServletContext context = getServletContext();
		final List<String> xsdLocSet = getFilenames(context, "/xsd");
		Source[] schemas = null;
		if (xsdLocSet != null && xsdLocSet.size() > 0) {
			// sort the list of XSD documents so that dependencies come first
			// it is assumed that the files are named in the desired loading
			// order
			// for example, 1-xml.xsd, 2-soap.xsd, 3-addressing.xsd...
			List<String> xsdLocList = new ArrayList<String>(xsdLocSet);
			Collections.sort(xsdLocList);
			schemas = new Source[xsdLocList.size()];
			final Iterator<String> xsdLocIterator = xsdLocList.iterator();
			for (int i = 0; xsdLocIterator.hasNext(); i++) {
				final String xsdLoc = xsdLocIterator.next();
				final InputStream xsd = context.getResourceAsStream(xsdLoc);
				schemas[i] = new StreamSource(xsd);
				if (LOG.isLoggable(Level.FINE))
					LOG.log(Level.SEVERE, "Custom schema " + xsdLoc);
			}
		}
		try {
			agent = createWSManAgent(schemas);
		} catch (SAXException ex) {
			LOG.log(Level.SEVERE, "Error setting schemas", ex);
			throw new ServletException(ex);
		}
	}
    
    private List<String> getFilenames(ServletContext context, String path) {
    	final List<String> xsdFilenames =  new ArrayList();
		final Set<String> xsdLocSet = context.getResourcePaths(path);

		if ((xsdLocSet == null) || (xsdLocSet.size() == 0))
			return xsdFilenames;

		final List<String> xsdLocList = new ArrayList<String>(xsdLocSet);
		final Iterator<String> xsdLocIterator = xsdLocList.iterator();

		// find the files and add them to the list
		// make a recursive call for directories
		for (int i = 0; xsdLocIterator.hasNext(); i++) {
			String xsdLoc = xsdLocIterator.next();
			final File f = new File(context.getRealPath(xsdLoc));
			if (f.isFile()) {
				xsdFilenames.add(xsdLoc);
			} else {
				if (xsdLoc.charAt(xsdLoc.length() - 1) == '/')
					xsdLoc = xsdLoc.substring(0, xsdLoc.length() - 1);
				List<String> subList = getFilenames(context, xsdLoc);
				xsdFilenames.addAll(subList);
			}
		}
		return xsdFilenames;
	}

	protected abstract WSManAgent createWSManAgent(Source[] schemas) throws SAXException;
    
    public void doGet(final HttpServletRequest req,
			          final HttpServletResponse resp) 
                throws ServletException,
			IOException {

		doPost(req, resp);
	}
    
    public void doPost(final HttpServletRequest req,
                       final HttpServletResponse resp) 
                throws ServletException, IOException {
        
        final ContentType contentType = ContentType.createFromHttpContentType(req.getContentType());
        if (contentType == null || !contentType.isAcceptable()) {
			boolean isWsdlOrSchemaRequest = processForWsdlOrSchemaRequest(req);
			if (!isWsdlOrSchemaRequest) {
				resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
			} else {
				// insert method to generate HTTP response here
				processAsHttpRequest(req, resp, contentType);
			}
			return;
		}
		if (contentType == null || !contentType.isAcceptable()) {
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
    
    /**Process the http request.
     * 
     * @param req
     * @param resp
     * @throws IOException 
     */
    private void processAsHttpRequest(HttpServletRequest req, 
    		                          HttpServletResponse resp,
    		                          ContentType contentType) throws IOException {
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
            
            // check for filename in URL query
            String filename = getQueryFilename(req);
            
			if ((filename == null) || (filename.length() == 0)) {
				// check for the filename in the path
				String requestURI = req.getRequestURI();
				if ((requestURI != null)
						&& (requestURI.startsWith(req.getContextPath()))) {
					// removing the contextPath in it's entirety
					filename = requestURI.substring(req.getContextPath().trim()
							.length());
				}
			}
            
            ServletContext srvContext = getServletContext();
             InputStream inputStream = 
            	 srvContext.getResourceAsStream(filename);
             Set paths = srvContext.getResourcePaths(filename);
             if(inputStream==null){
            	resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            	String htmlResponse = "";
            	  String title="'"+filename+"' was not found.";
            	  String body="<center><h1>"+HttpServletResponse.SC_NOT_FOUND+
            	     ": File Not Found</h1></center><br></br>";
            	  body+="<center>The resource <b>'"+filename+req.getContextPath()+
            	  		"'</b> that you requested could not be found.";
            	  body+="<br></br> Please check that the requested URL is correct.</center>";
            		htmlResponse=generateHtmlResponse(filename,title,body);
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
    
    private String getQueryFilename(HttpServletRequest req) {
		String filename = null;

		// Check if query string was set in the request
		String query = req.getQueryString();
		if ((query != null) && (query.trim().length() > 0)) {
			query = query.toLowerCase().trim();
			if (query.equals("wsdl")) {
				// Locate the default wsdl filename
				filename = getWisemanProperty(SERVICE_WSDL);
				if (filename.charAt(0) == '/')
					filename = "/wsdls" + filename;
				else
					filename = "/wsdls/" + filename;
			} else if (query.equals("xsd")) {
				// Locate the default xsd filename
				filename = getWisemanProperty(SERVICE_XSD);
				if (filename.charAt(0) == '/')
					filename = "/schemas" + filename;
				else
					filename = "/schemas/" + filename;
			} else if (query.startsWith("wsdl=")) {
				// extract the filename from the query
				filename = req.getQueryString().trim().substring(
						"wsdl=".length());
				if (filename.charAt(0) != '/')
					filename = "/" + filename;

				// check if the file exists and is readable
				final String name = "/wsdls" + filename;
				final File f = new File(getServletContext().getRealPath(name));
				if (f.canRead() == true) {
					filename = name;
				} else {
					// assume it is in the wiseman diretory
					filename = "/wsdls/wiseman" + filename;
				}
			} else if (query.startsWith("xsd=")) {
				// extract the filename from the query
				filename = req.getQueryString().trim().substring(
						"xsd=".length());
				if (filename.charAt(0) != '/')
					filename = "/" + filename;

				// check if the file exists and is readable
				final String name = "/schemas" + filename;
				final File f = new File(getServletContext().getRealPath(name));
				if (f.canRead() == true) {
					filename = name;
				} else {
					// assume it is in the wiseman diretory
					filename = "/schemas/wiseman" + filename;
				}
			}
		}
		return filename;
	}
    
    public static String getWisemanProperty(final String property) {
		if (wisemanProperties == null) {
			if (LOG.isLoggable(Level.FINE))
				LOG.log(Level.FINE, "Getting properties ["
						+ WISEMAN_PROPERTY_FILE_NAME + "]");
			final InputStream ism = WSManServlet.class
					.getResourceAsStream(WISEMAN_PROPERTY_FILE_NAME);
			if (ism != null) {
				wisemanProperties = new Properties();
				try {
					wisemanProperties.load(ism);
				} catch (IOException iex) {
					LOG.log(Level.WARNING, "Error reading properties from "
							+ WISEMAN_PROPERTY_FILE_NAME, iex);
					wisemanProperties = new Properties();

				}
			} else {
				LOG.log(Level.WARNING, "Error reading properties from "
						+ WISEMAN_PROPERTY_FILE_NAME);
			}
		}
		return wisemanProperties.getProperty(property);
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
    	   //parse the request URI for /wsdl* or /schema* or ?wsdl* or ?xsd*
    	   //if exists then set to true
    	   String requestUri = req.getRequestURI();
			if ((requestUri != null) && (requestUri.trim().length() > 0)) {
				// Check query string first
				String query = req.getQueryString();
				if ((query != null) && (query.trim().length() > 0)) {
					query = query.toLowerCase().trim();
					if ((query.equals("wsdl"))
							|| (query.startsWith("wsdl="))
							|| (query.equals("xsd"))
							|| (query.startsWith("xsd="))) {
						isWsdlSchemaReq = true;
					}
				} else {
					// Check for addition to path
					requestUri = requestUri.toLowerCase().trim();
					// check for /wsdl or /schema
					if ((requestUri.indexOf("/wsdl") > 0)
							|| (requestUri.indexOf("/schema") > 0)) {
						isWsdlSchemaReq = true;
					}
				}
    	   } else {
	    	  String msg="This servlet container does not expose the standard field ";
	    	  msg+="'HttpServletRequest.requestUri'. Unable to proceed.";
	    	  throw new RuntimeException(msg);
    	   }
    	} else {
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
