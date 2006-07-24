/*
 * Copyright 2006 Sun Microsystems, Inc.
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
 * $Id: TransferSupport.java,v 1.4 2006-07-24 18:39:01 obiwan314 Exp $
 *
 */
package com.sun.ws.management.framework.transfer;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XmlBinding;

/**
 * This class provides default, overrideable behavior for
 * objects that support WS-Transfer. Custom actions can be
 * implemented by creating methids names after actions.
 * @author wire
 *
 */
public class TransferSupport implements Transferable {

	public static final org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory addressingFactory = new org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory();
    public static final ObjectFactory managementFactory = new ObjectFactory();
    public static final org.xmlsoap.schemas.ws._2004._09.transfer.ObjectFactory xferFactory = new  org.xmlsoap.schemas.ws._2004._09.transfer.ObjectFactory();

	public TransferSupport() {
		super();
	}

	public void create(Management request, Management response) {
		throw new ActionNotSupportedFault();		
	}

	public void delete(Management request, Management response) {
		throw new ActionNotSupportedFault();		
	}

	public void get(Management request, Management response) {
		throw new ActionNotSupportedFault();
	}

	public void put(Management request, Management response) {
		throw new ActionNotSupportedFault();
	}
	

	/**
	 * Utility method used to append a create response to a SOAP body.
	 * May have to change as there is sctually no element called CreateResponse
	 * in the spec version of this response.
	 * @param response
	 * @param resourceUri   
	 * @param selectors
	 * @throws JAXBException 
	 */
	protected void appendCreateResponse(Management response, String resourceUri,Map<String,String> selectors) throws JAXBException  {
		EndpointReferenceType epr=null;
		epr = response.createEndpointReference(Addressing.ANONYMOUS_ENDPOINT_URI, null,null,null,null);

		JAXBElement<EndpointReferenceType> resp = xferFactory.createResourceCreated(epr); 

        // Build the reference parameters
        ReferenceParametersType refParams = new ReferenceParametersType();
        epr.setReferenceParameters(refParams);
        List<Object> paramList = refParams.getAny();
        	        
        // Set our resource URI (Similar to our classname)
        AttributableURI resourceURI = new AttributableURI();
        resourceURI.setValue(resourceUri);
        paramList.add(managementFactory.createResourceURI(resourceURI));
        
        // Set the selectors required to find this instance again
        SelectorSetType selectorSetType = new SelectorSetType();
        List<SelectorType> selectorList = selectorSetType.getSelector();
        
        // Add a selector to the list
        for (String key : selectors.keySet()) {
            SelectorType nameSelector = new SelectorType();
            nameSelector.setName(key);        
            nameSelector.getContent().add(selectors.get(key));        
            selectorList.add(nameSelector);			
		}
        
        paramList.add(managementFactory.createSelectorSet(selectorSetType));
       XmlBinding xmlBinding = response.getXmlBinding(); 
        Document responseDoc = Management.newDocument();
		try {
			xmlBinding.marshal(resp, responseDoc );
		} catch (JAXBException e) {
			throw new InternalErrorFault();
		}

		try {
			response.getBody().addDocument(responseDoc );
		} catch (SOAPException e) {
			throw new InternalErrorFault();
		}
	}

    public static EndpointReferenceType createEpr(String endpointUrl,String resourceUri, Map<String, String> selectors) throws JAXBException
    {
        // Get a JAXB Epr
        EndpointReferenceType epr = addressingFactory.createEndpointReferenceType();
        AttributedURI addressURI = addressingFactory.createAttributedURI();
        addressURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        if(endpointUrl==null)
        	addressURI.setValue(Addressing.ANONYMOUS_ENDPOINT_URI);
        else
        	addressURI.setValue(endpointUrl);
        epr.setAddress(addressURI);

        // Build the reference parameters
        ReferenceParametersType refParams = new ReferenceParametersType();
        epr.setReferenceParameters(refParams);
        List<Object> paramList = refParams.getAny();

        // Set our resource URI (Similar to our classname)
        AttributableURI resourceURIType = new AttributableURI();
        resourceURIType.setValue(resourceUri);
        paramList.add(managementFactory.createResourceURI(resourceURIType));

        // Set the selectors required to find this instance again
        SelectorSetType selectorSetType = new SelectorSetType();
        List<SelectorType> selectorList = selectorSetType.getSelector();

        // Add a selector to the list
        for (String key : selectors.keySet())
        {
            SelectorType nameSelector = new SelectorType();
            nameSelector.setName(key);
            nameSelector.getContent().add(selectors.get(key));
            selectorList.add(nameSelector);
        }

        paramList.add(managementFactory.createSelectorSet(selectorSetType));

//        XmlBinding xmlBinding = new XmlBinding(null);
//        Document document = Management.newDocument();
//        xmlBinding.marshal(addressingFactory.createEndpointReference(epr), document);

        return epr;

    }
}
