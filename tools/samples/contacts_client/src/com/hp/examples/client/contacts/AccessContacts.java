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
 ** 
 *
 * $Id: AccessContacts.java,v 1.2 2007-05-31 19:47:45 nbeers Exp $
 */
package com.hp.examples.client.contacts;

import java.util.HashMap;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Node;

import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.ResourceStateDocument;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferMessageValues;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.BasicAuthenticator;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

public class AccessContacts  {
  
	/**The main method launches/demonstrates two approaches to 
	 * access a specific web service.
	 * 
	 * 1)Requires a priori knowledge of the service connection details: aprioriGetExample
	 * 	  also demonstrates model specific processing of GetResponse 
	 * 2)Requires knowledge to the metadataId of the service: mfGetExample
	 *    also demonstrates ResourceState mechanism of processing GetResponse
	 *    
	 * @param args
	 */
	public static void main(String[] args) {
		//The following method demonstrates the simple
		//steps involved in generating a basic client
		// to access the contacts server defined with a priori
		// information only. 
		aprioriGetExample();
		
		//The following mechanism defines how to access 
		// the same Wiseman service using the Metadata-Flavored
		// functionality provided significantly reducing
		// the apriori metadata needed for access.
		mfGetExample();
	}

	/** This method demonstrates the steps that
	 * one would need to take to access a specified
	 * metadata-flavored instance.  The amount of a priori
	 * information required to interoperate is minimal
	 * facilitating automated consumption of these wsmanagement
	 * webservices which is crucial to interoperability itself.
	 */
	private static void mfGetExample(){
		//We know the MetadataResourceURI. This is a priori knowledge,
		// but it is acceptable because most services require that you 
		// know the name of the service to successfully access it.
		  //The service address is known
		String serviceAddress="http://localhost:8080/users/";
		  //The metadataUId is known, determined from MetaDataViewer, 
		  // or can be figured out. 
		String metaDataUID=serviceAddress+"Bill.Gates";
		try {
		  //Locate the metadata necessary to make the call
		  Management get = AnnotationProcessor.findAnnotatedResourceByUID(
				  metaDataUID,serviceAddress);
		  //Now that we have the Message components to succesfully locate the service
		  //we must define which Action to request of the service and final prep to send off
		  get.setAction(Transfer.GET_ACTION_URI);
		  //run this Management instance through ManagementUtility to fill in missing elements
		  get = ManagementUtility.buildMessage(get, null);
		  //Servlet security mechanism in effect. Loads the credentials passed in as vm params  
		  final String basicAuth = System.getProperty("wsman.basicauthentication");
		  if ("true".equalsIgnoreCase(basicAuth)) {
//			  HttpClient.setAuthenticator(new transport.BasicAuthenticator());
		    HttpClient.setAuthenticator(new BasicAuthenticator());
		  }

		  //Use the default HttpClient or your own here to send the message off
		  Addressing response =HttpClient.sendRequest(get);
//System.out.println(response);		  
		  //Check for faults
	      if (response.getBody().hasFault())
	      {
	        SOAPFault fault = response.getBody().getFault();
	        throw new SOAPException(fault.getFaultString());
	      }
	      //Now that we have the content back we'll convert it to ResourceState
	      // so that we can use XPath to process the results
	      ResourceStateDocument resState = ManagementUtility.getAsResourceState(response);
	       //We'll use the [local-name] syntax to avoid specifying namespaces
	      String billsAge = resState.getValueText("//*[local-name()='age']");
	      //TODO: get the XPath processing with resourceState for custom model data working.
//	      String billsAge = resState.getValueText("/ns9:user/ns9:age");
//	      String billsAge = resState.getValueText("//*age");
System.out.println("@@@ Metadata Flavored Example: Bill Gates' age:"+billsAge);		  
	      
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/** This method demonstrates the fundamental steps
	 * necessary to build a simple command line client to
	 * access a Wiseman sample contacts service.
	 * 
	 */
	private static void aprioriGetExample() {
		try {
			//Build simple client to exercise the server instance created.
			TransferMessageValues settings = TransferMessageValues.newInstance();
			 //insert the WsManagement server endpoint details.
	    	  settings.setResourceUri("wsman:contacts");
	    	  settings.setTo("http://localhost:8080/users/");
	    	  //Specify the ActionDispatching action to be used  
	    	  settings.setTransferMessageActionType(Transfer.GET_ACTION_URI);
	    	  
	    	  //Define the instance level descriminant. AKA which specific one to get.
	    	HashMap<String,String> contactKeys = new HashMap<String, String>();
	    	 contactKeys.put("firstname", "Bill");
	    	 contactKeys.put("lastname", "Gates");
	    	Set<SelectorType> selectorSet = ManagementUtility.createSelectorType(contactKeys);
	    	settings.setSelectorSet(selectorSet);
	    	
	    	 //After modifying the settings for the Transfer message, create msg.
			try {
			  Transfer transfer = TransferUtility.buildMessage(null,settings);
			  
			//Servlet security mechanism in effect. Loads the credentials passed in as vm params  
			final String basicAuth = System.getProperty("wsman.basicauthentication");
			if ("true".equalsIgnoreCase(basicAuth)) {
//			    HttpClient.setAuthenticator(new transport.BasicAuthenticator());
			    HttpClient.setAuthenticator(new BasicAuthenticator());
			}
			  //Use an HttpClient to submit the request
	          Addressing response = HttpClient.sendRequest(transfer);

	          // Look for returned faults
		      if (response.getBody().hasFault())
		      {
		        SOAPFault fault = response.getBody().getFault();
		        throw new SOAPException(fault.getFaultString());
		      }
		      //No faults so attempt to extract the contents returned
		      Management contactMessage = new Management(response);
		      //extracted content as org.w3c.dom element
		      Node content = contactMessage.getBody().getFirstChild();
		      
		      //at this point you have the Model specific representation of the content.
		      //You could convert to the JAXB, XMLBeans,etc. type to proceed or you 
		      //can use the ResourceState mechanism described in the ClientSide 
		      //documentation to access returned contents without model specific
		      // processing.
			  XmlBinding binding = new XmlBinding(null,"com.hp.examples.ws.wsman.user");

			  JAXBElement<UserType> unmarshal = (JAXBElement<UserType>) binding
			  .unmarshal(content);
			  JAXBElement<UserType> userReturnedElement = unmarshal;
			  UserType returnedUser = (UserType) userReturnedElement.getValue();
System.out.println("@@@ A Priori Example: Bill Gates' Age:"+returnedUser.getAge());  
			} catch (Exception e) {
				e.printStackTrace();
			} 
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}
}
