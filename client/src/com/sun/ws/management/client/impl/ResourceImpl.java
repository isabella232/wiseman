package com.sun.ws.management.client.impl;

import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.w3c.dom.Element;

import com.sun.ws.management.client.Resource;

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

}
