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
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.1.2.1  2008/01/28 08:00:47  denis_rachal
 **The commit adds several prototype changes to the fudan_contribution. They are described below:
 **
 **1. A new Handler interface has been added to support the newer message types WSManagementRequest & WSManagementResponse. It is called WSHandler. Additionally a new servlet WSManReflectiveServlet2 has been added to allow calling this new handler.
 **
 **2. A new base handler has been added to support creation of WS Eventing Sink handlers: WSEventingSinkHandler.
 **
 **3. WS Eventing "Source" and "Sink" test handlers have been added to the unit tests, sink_Handler & source_Handler. Both are based upon the new WSHandler interface.
 **
 **4. The EventingExtensionsTest has been updated to test "push" events. Push events are sent from a source to a sink. The sink will forward them on to and subscribers (sink subscribers). The unit test subscribes for pull events at the "sink" and then gets the "source" to send events to the "sink". The test then pulls the events from the "sink" and checks them. Does not always run, so the test needs some work. Sometimes some of the events are lost. between the source and the sink.
 **
 **5. A prototype for handling basic authentication with the sink has been added. Events from the source can now be sent to a sink using Basic authentication (credentials are specified per subscription). This needs some additional work, but basically now works.
 **
 **6. Additional methods added to the WSManagementRequest, WSManagementResponse, WSEventingRequest & WSEventingResponse, etc... interfaces to allow access to more parts of the messages.
 **
 **Additional work is neede in all of the above changes, but they are OK for a prototype in the fudan_contributaion branch.
 **
 **Revision 1.10  2007/11/09 12:33:33  denis_rachal
 **Performance enhancements that better reuse the XmlBinding.
 **
 **Revision 1.9  2007/09/18 20:08:56  nbeers
 **Add support for SOAP with attachments.  Issue #136.
 **
 **Revision 1.8  2007/09/18 13:06:57  denis_rachal
 **Issue number:  129, 130 & 132
 **Obtained from:
 **Submitted by:
 **Reviewed by:
 **
 **129  ENHANC  P2  All  denis_rachal  NEW   Need support for ReNew Operation in Eventing
 **130  DEFECT  P3  x86  jfdenise  NEW   Should return a boolean variable result not a constant true
 **132  ENHANC  P3  All  denis_rachal  NEW   Make ServletRequest attributes available as properties in Ha
 **
 **Added enhancements and fixed issue # 130.
 **
 **Revision 1.7  2007/06/19 15:25:38  denis_rachal
 **Issue number:  120
 **Obtained from:
 **Submitted by:  denis_rachal
 **Reviewed by:
 **
 **Checks put in to check for missing filename.
 **
 **Revision 1.6  2007/06/14 07:28:06  denis_rachal
 **Issue number:  110
 **Obtained from:
 **Submitted by:  ywu
 **Reviewed by:
 **
 **Updated wsman.war test warfile and sample traffic.war file along with tools to expose a WSDL file. Additionally added default index.html file for sample warfile.
 **
 **Revision 1.5  2007/06/04 06:25:14  denis_rachal
 **The following fixes have been made:
 **
 **   * Moved test source to se/test/src
 **   * Moved test handlers to /src/test/src
 **   * Updated logging calls in HttpClient & Servlet
 **   * Fxed compiler warning in AnnotationProcessor
 **   * Added logging files for client junit tests
 **   * Added changes to support Maven builds
 **   * Added JAX-WS libraries to CVS ignore
 **
 **Revision 1.4  2007/05/30 20:30:15  nbeers
 **Add HP copyright header
 **
 **
 * $Id: WSManServlet2.java,v 1.1.2.2 2008-01-29 15:53:42 denis_rachal Exp $
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
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
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
import javax.xml.validation.SchemaFactory;

import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.HandlerContextImpl;
import com.sun.ws.management.server.WSManAgentSupport;
import com.sun.ws.management.server.message.SAAJMessage;
import com.sun.ws.management.server.message.WSManagementRequest;
import com.sun.ws.management.server.message.WSManagementResponse;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.ContentType;
import com.sun.ws.management.transport.HttpClient;

/**
 * Rewritten WSManServlet that delegates to a WSManAgent instance.
 *
 */
public abstract class WSManServlet2 extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(WSManServlet2.class.getName());
    private static Properties wisemanProperties = null;
    private static final String WISEMAN_PROPERTY_FILE_NAME = "/wiseman.properties";
    private static final String SERVICE_WSDL = "service.wsdl";
    private static final String SERVICE_XSD = "service.xsd";
    private static final String SERVICE_URL = "$$SERVICE_URL";
    private static final String UUID_SCHEME = "uuid:";
    private static final long MIN_ENVELOPE_SIZE = 8192;

    // This class implements all the WS-Man logic decoupled from transport

    WSManAgentSupport agent;

    public void init() throws ServletException {
		SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
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

    private List<String> getFilenames(ServletContext context, String path) {
    	final List<String> xsdFilenames =  new ArrayList<String>();
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

	protected abstract WSManAgentSupport createWSManAgent(Source[] schemas) throws SAXException;

    public void doGet(final HttpServletRequest req,
			          final HttpServletResponse resp)
                throws ServletException,
			IOException {

        final ContentType contentType = ContentType.createFromHttpContentType(req.getContentType());
		boolean isWsdlOrSchemaRequest = processForWsdlOrSchemaRequest(req);
		if (!isWsdlOrSchemaRequest) {
			resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
		} else {
			// insert method to generate HTTP response here
			// if (redirectQuery(req, resp, contentType) == false)
			processAsHttpRequest(req, resp, contentType);
		}
		return;
	}

    public void doPost(final HttpServletRequest req,
                       final HttpServletResponse resp)
                throws ServletException, IOException {

        final ContentType contentType = ContentType.createFromHttpContentType(req.getContentType());
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

    /*
    private boolean redirectQuery(HttpServletRequest req,
			HttpServletResponse resp, ContentType contentType) throws IOException {
		String query = req.getQueryString();
		if ((query != null) && (query.trim().length() > 0)) {
			query = query.toLowerCase().trim();
			if ((query.equals("wsdl")) || (query.startsWith("wsdl="))
					|| (query.equals("xsd")) || (query.startsWith("xsd="))) {
				// Get the xsd/wsdl full request URL
				final String filename = getQueryFilename(req);
				String url = req.getRequestURL().toString();
				// Remove any Servlet Path
				final String servletPath = req.getServletPath().trim();
				final int newlen = url.length() - servletPath.length();
				if (newlen > 0)
					url = url.substring(0, newlen) + filename;
				// Redirect the request to the full path
				final String encodedURL = resp.encodeRedirectURL(url);
				resp.sendRedirect(encodedURL);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	} */

	/**
	 * Process the http request.
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

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(req.getInputStream());
            os = new BufferedOutputStream(resp.getOutputStream());

            Map<String, Object> props = new HashMap<String, Object>(1);
            props.put(HandlerContext.SERVLET_CONTEXT, getServletContext());

            // check for filename in URL query
            String filename = getQueryFilename(req);

			if ((filename == null) || (filename.length() == 0)) {
				// check for the filename in the path
				filename = req.getServletPath().trim();
				if ((filename.length() == 0) || (filename.equals("/")))
					filename = "/index.html";
			}
			
	        if (filename.equals("/index.html")) {
				resp.setContentType("text/html");
			} else {
				resp.setContentType("text/xml");
			}

            ServletContext srvContext = getServletContext();
             InputStream inputStream =
            	 srvContext.getResourceAsStream(filename);
             // Set paths = srvContext.getResourcePaths(filename);
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
        	 String replacement = req.getRequestURL().toString();
        	 // Remove any Servlet Path
        	 final String servletPath = req.getServletPath().trim();
        	 int position = replacement.lastIndexOf(servletPath);
        	 if (position < 0)
        		  position = replacement.length() - 1;
        	 if (position >= 0)
            	  replacement = replacement.substring(0, position);
             String line = null;
             while ((line=br.readLine()) != null) {
            	 // Replace any $$SERVICE_URL variables with the current service URL
            	line = line.replace(SERVICE_URL, replacement);
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
				if ((filename == null) || (filename.length() == 0))
					filename = "service.wsdl";
				if (filename.charAt(0) == '/')
					filename = "/wsdls" + filename;
				else
					filename = "/wsdls/" + filename;
			} else if (query.equals("xsd")) {
				// Locate the default xsd filename
				filename = getWisemanProperty(SERVICE_XSD);
				if ((filename == null) || (filename.length() == 0))
					filename = "service.xsd";
				if (filename.charAt(0) == '/')
					filename = "/schemas" + filename;
				else
					filename = "/schemas/" + filename;
			} else if (query.startsWith("wsdl=")) {
				// extract the filename from the query
				filename = req.getQueryString().trim().substring(
						"wsdl=".length());
				if ((filename == null) || (filename.length() == 0))
					filename = "service.wsdl";
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
				if ((filename == null) || (filename.length() == 0))
					filename = "service.xsd";
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
			final InputStream ism = WSManServlet2.class
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
				wisemanProperties = new Properties();
			}
		}
		final String value = wisemanProperties.getProperty(property);
		return (value == null) ? "" : value;
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
    	   String requestUri = req.getServletPath().trim();
			if ((requestUri != null) && (requestUri.length() > 0)) {
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
					if ((requestUri.startsWith("/wsdls"))
							|| (requestUri.startsWith("/schemas"))
							|| (requestUri.equals("/"))
							|| (requestUri.equals("/index.html"))) {
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
            throws SOAPException, JAXBException, IOException, Exception {

        final Management request = new Management(is);
        request.setXmlBinding(agent.getXmlBinding());
        processForMissingTrailingSlash(request);

        request.setContentType(contentType);

        final String contentype = req.getContentType();
        final Principal user = req.getUserPrincipal();
        final String charEncoding = req.getCharacterEncoding();
        final String url = req.getRequestURL().toString();
        final Map<String, Object> props = new HashMap<String, Object>(2);
        props.put(HandlerContext.SERVLET_CONTEXT, getServletContext());
        
        final Map<String, Object> attributes = new HashMap<String, Object>();
        for (final Enumeration<String> e = req.getAttributeNames(); e.hasMoreElements();) {
        	final String name = e.nextElement();
        	attributes.put(name, req.getAttribute(name));
        }
        props.put(HandlerContext.SERVLET_REQUEST_ATTRIBUTES, attributes);
        final HandlerContext context = new HandlerContextImpl(user, contentype,
                                                              charEncoding, url, props);

        // final Message response = agent.handleRequest(request, context);
        final SAAJMessage saajReq = new SAAJMessage(request);
        final SAAJMessage saajRes = new SAAJMessage(new Management());
        
        final WSManagementResponse response = agent.handleRequest(saajReq, saajRes, context);
        
        final Management mgmtResponse;
        if (response != null) {
        	final Addressing addrResponse = new Addressing(response.toSOAPMessage());
        	final Identify identify = new Identify(addrResponse);
        	if (identify.getIdentify() != null) {
        		// This is an identify response
        		resp.setStatus(HttpServletResponse.SC_OK);
        		identify.writeTo(os);
        		return;
        	} else {
        	    mgmtResponse = new Management(addrResponse);
                mgmtResponse.setXmlBinding(agent.getXmlBinding());
                mgmtResponse.setContentType(request.getContentType());
            
                fillReturnAddress(request, mgmtResponse);
        	}
        } else {
        	mgmtResponse = null;
        }
        
        sendResponse(request, mgmtResponse, os, resp, getValidEnvelopeSize(saajReq));
    }
    
    private static long getValidEnvelopeSize(WSManagementRequest request) throws JAXBException, SOAPException {
        long maxEnvelopeSize = Long.MAX_VALUE;
        final MaxEnvelopeSizeType maxSize = request.getMaxEnvelopeSize();
        if (maxSize != null) {
            maxEnvelopeSize = maxSize.getValue().longValue();
        }
        return maxEnvelopeSize;
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
    
    private Management processForMissingTrailingSlash(Management request) {
    	String address = null;
    	try {
			if((request!=null)&&
				((address = request.getTo())!=null)&&
				(address.trim().length()>0)){
				//does not have the trailing slash
			   if(address.lastIndexOf("/")!= address.length()-1){
				 request.setTo(address+"/");
			   }
			}
		}
    	/* Silently fail as this is only a nicety for the developers/clients if they forget to
    	 * add the trailing slash to ensure servlet engine processing.  Not sure what the right
    	 * WsManagement response should be here as it's really just a Wiseman-using servlet problem.
    	 */
    	catch (JAXBException e) {
		} catch (SOAPException e) {
		}
		return request;
	}

    private static void sendResponse(final Management request,
    		final Management response, final OutputStream os,
            final HttpServletResponse resp, final long maxEnvelopeSize)
    	throws SOAPException, JAXBException,
            IOException {
        
        // Check if reply is to another endpoint
        final String dest = request.getReplyTo().getAddress().getValue();
        if (!Addressing.ANONYMOUS_ENDPOINT_URI.equals(dest)) {
            LOG.fine("Non anonymous reply to send to : " + dest);
            final int httpStatus = sendAsyncReply(request, response);
            if (httpStatus != HttpURLConnection.HTTP_OK) {
                // TODO: Do we send a fault to the requester?
            }
            resp.setStatus(httpStatus);
            return;
        }
        
        LOG.fine("Anonymous reply to send.");
        resp.setStatus(HttpServletResponse.SC_OK);
        
        // No reply is sent if response equals null.
        if (response != null) {
			if (response.getBody().hasFault()) {
				// sender faults need to set error code to BAD_REQUEST for client errors
				if (SOAP.SENDER.equals(response.getBody().getFault()
						.getFaultCodeAsQName())) {
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				} else {
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}

			if (maxEnvelopeSize < MIN_ENVELOPE_SIZE) {
				final String err = "MaxEnvelopeSize of '" + maxEnvelopeSize
						+ "' is set too small to encode faults "
						+ "(needs to be at least " + MIN_ENVELOPE_SIZE + ")";
				LOG.fine(err);
				final EncodingLimitFault fault = new EncodingLimitFault(err,
						EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE);
				response.setFault(fault);
			} else {
				if (maxEnvelopeSize >= Integer.MAX_VALUE) {
					// Don't check MaxEnvelopeSize if not specified or set to maximum.
					// NOTE: The official maximum is actually Long.MAX_VALUE,
					//        but the ByteArrayOutputStream we use has a maximum
					//        size of Integer.MAX_VALUE. We therefore cannot actually
					//        check if the size exceeds Integer.MAX_VALUE.
					LOG.fine("MaxEnvelopeSize not specified or set to maxiumum value.");
				} else {
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					response.writeTo(baos);
					int length = baos.size();

					if (length > maxEnvelopeSize) {
						final String err = "MaxEnvelopeSize of '"
								+ maxEnvelopeSize
								+ "' is smaller than the size of the response message: "
								+ Integer.toString(length);
						LOG.fine(err);
						final EncodingLimitFault fault = new EncodingLimitFault(
								err,
								EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE_EXCEEDED);
						response.setFault(fault);
					} else {
						// Message size is OK.
						LOG.fine("Response actual size is smaller than specified MaxEnvelopeSize.");
					}
				}
			}

			resp.setContentType(response.getMessage().getMimeHeaders()
					.getHeader("Content-Type")[0]);
			response.writeTo(os);
		}
    }
    
    private static int sendAsyncReply(final Management request,
    		final Management response)
    throws IOException, SOAPException, JAXBException {
    	// TODO: Extract any credentials from request message
        return HttpClient.sendResponse(response);
    }
}
