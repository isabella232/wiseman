/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.5  2007/06/18 17:57:28  nbeers
 **Fix for Issue #119 (EnumerationUtility.buildMessage() generates incorrect msg).
 **
 **Revision 1.4  2007/05/30 20:30:19  nbeers
 **Add HP copyright header
 **
 ** 
 *
 * $Id: WisemanCmdLine.java,v 1.6 2007-06-22 06:13:56 simeonpinder Exp $
 */
package com.sun.ws.management.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
// import java.net.PasswordAuthentication;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

// import net.java.dev.wiseman.traffic.light.client.TrafficAuthenticator;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferMessageValues;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;

/**
 * Simple command class to process wiseman requests.  This allows the user to perform simple get, delete, create and 
 * put requests.
 * 
 * @author NABEE
 *
 */
public class WisemanCmdLine {

	protected String destination = null;
	protected String resourceUri = null;
	protected Set<SelectorType> selectors = null;
	protected Document docIn = null;
	private static String action = null;
	protected String xpathVal = null;
	protected int maxElements = 5;
	protected String user = null;
	protected String password = null;
	
	// Command line argument flags
	public final static String ACTION_ARG = "-a";
	public final static String DESTINATION_ARG = "-d";
	public final static String RESOURCE_URI_ARG = "-r";
	public final static String SELECTOR_ARG = "-s";
	public final static String BODY_ARG = "-b";
	public final static String XPATH_ARG = "-x";
	public final static String MAX_ELEM_ARG = "-m";
	public final static String USER_ARG = "-u";
	public final static String PASSWORD_ARG = "-p";
	
	// Values for action argument
	public final static String GET_ACTION = "get";
	public final static String PUT_ACTION = "put";
	public final static String CREATE_ACTION = "create";
	public final static String DELETE_ACTION = "delete";
	public final static String ENUMERATE_ACTION = "enumerate";
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		WisemanCmdLine cmdLine = new WisemanCmdLine();
		cmdLine.parse(args);
		
		// Setup security (if needed)
		cmdLine.setSecurity();
		
		if (action.equals(GET_ACTION)) {
			cmdLine.sendGetRequest();
		} else if (action.equals(DELETE_ACTION)) {
			cmdLine.sendDeleteRequest();
		} else if (action.equals(CREATE_ACTION)) {
			cmdLine.sendCreateRequest();
		} else if (action.equals(PUT_ACTION)) {
			cmdLine.sendPutRequest();
		} else if (action.equals(ENUMERATE_ACTION)) {
			cmdLine.sendEnumerateRequest();
		}
		

	}

	private void setSecurity() {
		
		if (user != null) {
			// Temporarily set authentication manually
			System.setProperty("wsman.user", user);
			System.setProperty("wsman.password", password);
			
			HttpClient.setAuthenticator(new BasicAuthenticator());
		}
		
	}

	/**
	 * Send a put request to the WISEMAN server
	 * 
	 * @throws Exception
	 */
	private void sendPutRequest() throws Exception{
    	TransferMessageValues settings = TransferMessageValues.newInstance();
    	settings.setResourceUri(resourceUri);
    	settings.setTo(destination);
    	settings.setSelectorSet(selectors);
    	settings.setTransferMessageActionType(Transfer.PUT_ACTION_URI);
    	final Transfer mgmt = TransferUtility.buildMessage(null, settings);

        mgmt.getBody().addDocument(docIn);

        // Send request
        Map.Entry<String, String>[] entry = null;
        Addressing response = HttpClient.sendRequest(mgmt, entry);
		printResponse(response);
	}

	/**
	 * Send a create request to the WISEMAN server
	 * 
	 * @throws Exception
	 */
	private void sendCreateRequest() throws Exception {
    	TransferMessageValues settings = TransferMessageValues.newInstance();
    	settings.setResourceUri(resourceUri);
    	settings.setTo(destination);
    	settings.setTransferMessageActionType(Transfer.CREATE_ACTION_URI);
    	final Transfer mgmt = TransferUtility.buildMessage(null, settings);

    	// If the user specified a body for the request, add it
    	if (docIn != null) {
    		mgmt.getBody().addDocument(docIn);
    	}

        // Send request
        Map.Entry<String, String>[] entry = null;
        Addressing response = HttpClient.sendRequest(mgmt, entry);
        
        // Look for returned faults
        if (response.getBody().hasFault())
        {
            SOAPFault fault = response.getBody().getFault();
            throw new SOAPException(fault.getFaultString());
        }
        
        printResponse(response);
		
	}

	/**
	 * Send a delete request to the WISEMAN server
	 * 
	 * @throws Exception
	 */
	private void sendDeleteRequest() throws Exception {
		
    	TransferMessageValues settings = TransferMessageValues.newInstance();
    	settings.setResourceUri(resourceUri);
    	settings.setTo(destination);
    	settings.setSelectorSet(selectors);
    	settings.setTransferMessageActionType(Transfer.DELETE_ACTION_URI);
    	final Transfer mgmt = TransferUtility.buildMessage(null, settings);

		// Send request
        Map.Entry<String, String>[] entry = null;
        Addressing response = HttpClient.sendRequest(mgmt, entry);

        // Look for returned faults
        if (response.getBody().hasFault())
        {
            SOAPFault fault = response.getBody().getFault();
            throw new SOAPException(fault.getFaultString());
        }
        
        printResponse(response);
	}

	/**
	 * Send a delete request to the WISEMAN server
	 * 
	 * @throws Exception
	 */
	private void sendEnumerateRequest() throws Exception {
		
    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
    	//settings.setFilter(filter);
    	settings.setMaxTime(6000);
    	settings.setTo(destination);
    	settings.setResourceUri(resourceUri);
    	settings.setMaxElements(maxElements);
    	settings.setRequestForOptimizedEnumeration(true);
    	final Enumeration enu = EnumerationUtility.buildMessage(null, settings);
    	
        Map.Entry<String, String>[] entry = null;
        final Addressing response = HttpClient.sendRequest(enu, entry);

        if (response.getBody().hasFault())
        {
            SOAPFault fault = response.getBody().getFault();
            throw new SOAPException(fault.getFaultString());
        }
        
        printResponse(response);
	}

	/**
	 * Pare the command line arguments and populate the member variables.
	 * 
	 * @param args command line arguments
	 * @return value of the action paramater
	 * 
	 * @throws Exception
	 */
	protected String parse(String[] args) throws Exception{
		// parse command line options 
				
		if (args.length == 0) {
			printArgs();
		}
	       for (int i = 0; i < args.length; i++)
	        {
	            String key = args[i];
	            if (i + 1 < args.length)
	            {
	                i++;//increment var i to get the value
	                String val = args[i];
	                if (key.equals(ACTION_ARG)) {
	                	action = val;
	                } else if (key.equals(DESTINATION_ARG)) {
	                	destination = val;
	                } else if (key.equals(RESOURCE_URI_ARG)) {
	                	resourceUri = val;
	                }
	                else if (key.equals(SELECTOR_ARG)) {
	                	if (selectors == null) {
	                		selectors = new HashSet<SelectorType>();
	                	}
	                	SelectorType sel = new SelectorType();
	                	sel.setName(val);
	                	sel.getContent().add(args[i + 1]);
	                	i++;  
	                	selectors.add(sel);
	                } else if (key.equals(BODY_ARG)) {
	                	// Read body from a file and store as document
	                	final File file = new File(val);
	                	Transformer trans = TransformerFactory.newInstance().newTransformer();
	                	DOMResult outputTarget = new DOMResult();
						Source xmlSource = new StreamSource(new FileInputStream(file));
						trans.transform(xmlSource , outputTarget );
						docIn = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Node newNode = docIn.importNode(outputTarget.getNode().getFirstChild(), true);
						docIn.appendChild(newNode);
	                } else if (key.equals(XPATH_ARG)) {
	                	xpathVal = val;
	                } else if (key.equals(MAX_ELEM_ARG)) {
	                	maxElements = new Integer(val).intValue();
	                } else if (key.equals(USER_ARG)) {
	                	user = val;
	                } else if (key.equals(PASSWORD_ARG)) {
	                	password = val;
	                }
	            }
	            else
	            {
	                System.err.println("Argument is missing a value! Argument: " + args[i]);
	                printArgs();
	            }
	        }
		
	       // Check for required action argument
	       if (action == null) {
               System.err.println("Argument is missing a the action value!");
               printArgs();
	    	   
	       }
		
		return action;
	}
	
	/**
	 * Send a get request to the WISEMAN server
	 * 
	 * @throws Exception
	 */
	protected void sendGetRequest() throws Exception {
		
        // Build Request Document
    	TransferMessageValues settings = TransferMessageValues.newInstance();
    	settings.setResourceUri(resourceUri);
    	settings.setTo(destination);
    	settings.setSelectorSet(selectors);
    	settings.setTransferMessageActionType(Transfer.GET_ACTION_URI);
    	if (xpathVal != null) {
    		settings.setFragment(xpathVal);
    		settings.setFragmentDialect(com.sun.ws.management.xml.XPath.NS_URI);
    	}
    	
    	final Transfer mgmt = TransferUtility.buildMessage(null, settings);

        // Send the get request to the server
        Map.Entry<String, String>[] entry = null;
        Addressing response = HttpClient.sendRequest(mgmt, entry);

        // Look for returned faults
        if (response.getBody().hasFault())
        {
            SOAPFault fault = response.getBody().getFault();
            throw new SOAPException(fault.getFaultString());
        }
		
        printResponse(response);
		
	}
	/**
	 * Print the results of the command
	 * 
	 * @param response server SOAP response
	 * 
	 * @throws SOAPException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void printResponse(Addressing response) throws SOAPException, ParserConfigurationException, SAXException, IOException {
		
		if (response.getBody().hasFault()) {
			response.prettyPrint(System.out);
		}
		else if (response.getBody().hasChildNodes()) {
			Document body = response.getBody().extractContentAsDocument();
			OutputStream os = System.out;
			
	        final OutputFormat format = new OutputFormat(body);
	        format.setLineWidth(72);
	        format.setIndenting(true);
	        format.setIndent(2);
	        final XMLSerializer serializer = new XMLSerializer(os, format);
	        serializer.serialize(body);
	        os.write("\n".getBytes());
		}
				
	}
	
    /**
     * Prints the commandline arguments.
     */
    private void printArgs()
    {
        System.out.println("Usage: java " + WisemanCmdLine.class.getName() + " " + ACTION_ARG  + " " + DESTINATION_ARG + " " + RESOURCE_URI_ARG + " [ "  + SELECTOR_ARG + " | " + BODY_ARG + " | " + XPATH_ARG + " | " + MAX_ELEM_ARG + " | " + USER_ARG + " | " + PASSWORD_ARG + " ] " );
        System.out.println();
        System.out.println(ACTION_ARG + "\t action to perform (get, put, create, delete, enumeration) REQUIRED");
        System.out.println(DESTINATION_ARG + "\t URL destination for request. REQUIRED");
        System.out.println(RESOURCE_URI_ARG + "\t Resource URI value. REQUIRED");
        System.out.println(SELECTOR_ARG + "\t Name and value for the item requested.  The first value is the name of the \"key\" field, the second is the value of the \"key\" field. ");
        System.out.println(BODY_ARG + "\t File name of the XML body of the request.  REQUIRED for Create and Put.");
        System.out.println(XPATH_ARG + "\t XPATH expression to apply to result set.");
        System.out.println(MAX_ELEM_ARG + "\t Maximum number of items to return (Enumeration only).");
        System.out.println(USER_ARG + "\t User name for web service security.");
        System.out.println(PASSWORD_ARG + "\t Password for web service security.");
        System.out.println();
        System.out.println("i.e. " + "wise.bat" + " " + ACTION_ARG + " get " + DESTINATION_ARG + " \"http://localhost:8080/traffic/\"  " + RESOURCE_URI_ARG + " \"urn:resources.wiseman.dev.java.net/traffic/1/light\" " + SELECTOR_ARG + " name Light1");
        System.out.println();
        System.out.println("i.e. " + "java " + WisemanCmdLine.class.getName() + " " + ACTION_ARG + " get " + DESTINATION_ARG + " \"http://localhost:8080/traffic/\"  " + RESOURCE_URI_ARG + " \"urn:resources.wiseman.dev.java.net/traffic/1/light\" " + SELECTOR_ARG + " name Light1");
        System.out.println();
        System.out.println("i.e. " + "java " + WisemanCmdLine.class.getName() + " " + ACTION_ARG + " put " + DESTINATION_ARG + " \"http://localhost:8080/traffic/\"  " + RESOURCE_URI_ARG + " \"urn:resources.wiseman.dev.java.net/traffic/1/light\" " + SELECTOR_ARG + " name Light1 " + BODY_ARG + " \"c:\\putbody.xml\" ");
        System.out.println();
        System.exit(0);
    }
	
}
