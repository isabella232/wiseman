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
 * $Id: ResourceImpl.java,v 1.10 2007-05-30 20:30:28 nbeers Exp $
 */
package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

public class ResourceImpl extends EnumerationResourceImpl implements Resource {
	
    protected static final String UUID_SCHEME = "uuid:";
    private static Logger log = Logger.getLogger(ResourceImpl.class.getName());

	
	public ResourceImpl(String destination, String resourceURI, SelectorSetType selectors, XmlBinding binding) throws SOAPException, JAXBException{
		super(destination, resourceURI, selectors, binding);//
	}

	public ResourceImpl(String destination, String resourceURI, XmlBinding binding) throws SOAPException, JAXBException {
		super(destination, resourceURI, null, binding);
	}

	public ResourceImpl(Element eprElement, String endpointUrl, XmlBinding binding) throws SOAPException, JAXBException {
		super(eprElement, endpointUrl, binding);
	}

	public ResourceState invoke(String action, Document document) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {

		//Build the document
		final Transfer xf = setTransferProperties(action);		
		final Management mgmt = setManagementProperties(xf);

		// Add selectors
		HashSet<SelectorType> selectors1=new HashSet<SelectorType>();        
        if(selectorSet!=null){
        	List<SelectorType> selectors = selectorSet.getSelector();
        	for (Iterator iter = selectors.iterator(); iter.hasNext();) {
				SelectorType element = (SelectorType) iter.next();
				selectors1.add(element);
			}
        	mgmt.setSelectors(selectors1);
        }

		xf.getBody().addDocument(document);
		
        log.info("REQUEST:\n"+mgmt+"\n");
        //Send the request
        final Addressing response = HttpClient.sendRequest(mgmt);

        //Check for fault during message generation
		if (response.getBody().hasFault()) {
			log.severe("FAULT:\n" + response + "\n");
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault);
		}
		log.info("RESPONSE:\n" + response + "\n");
        
        //parse response and retrieve contents.
        // Iterate through the create response to obtain the selectors
        SOAPBody body = response.getBody();
     
        Document bodyDoc = body.extractContentAsDocument();
		return new ResourceStateImpl(bodyDoc);
	}


}
