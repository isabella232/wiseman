package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
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

public class ResourceImpl extends EnumerationResourceImpl implements Resource {
	
    protected static final String UUID_SCHEME = "uuid:";
    private static Logger log = Logger.getLogger(ResourceImpl.class.getName());

	
	public ResourceImpl(String destination, String resourceURI,SelectorSetType selectors) throws SOAPException, JAXBException{
		super(destination, resourceURI,selectors);//
	}

	public ResourceImpl(String destination, String resourceURI) throws SOAPException, JAXBException {
		super(destination, resourceURI, null);
	}

	public ResourceImpl(Element eprElement, String endpointUrl) throws SOAPException, JAXBException {
		super(eprElement, endpointUrl);
	}

	public ResourceState invoke(String action, Document document) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
//		Transfer xf=new Transfer();
//		xf.setAction(action);
//		xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
//		xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
//
//		final Management mgmt = new Management(xf);
//		mgmt.setTo(destination);
//		mgmt.setResourceURI(resourceURI);

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
        	log.severe("FAULT:\n"+response+"\n");
            SOAPFault fault = response.getBody().getFault();
            throw new FaultException(fault.getFaultString());
        }
        
        //Process the response to extract useful information.
        log.info("RESPONSE:\n"+response+"\n");
        
        //parse response and retrieve contents.
        // Iterate through the create response to obtain the selectors
        SOAPBody body = response.getBody();
     
        Document bodyDoc = body.extractContentAsDocument();
		return new ResourceStateImpl(bodyDoc);
	}


}
