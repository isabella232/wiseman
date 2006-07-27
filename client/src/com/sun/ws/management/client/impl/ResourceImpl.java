package com.sun.ws.management.client.impl;

import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XmlBinding;

public class ResourceImpl extends EnumerationResourceImpl implements Resource {
	
    protected static final String UUID_SCHEME = "uuid:";
    private static Logger log = Logger.getLogger(ResourceImpl.class.getName());

    //Attributes
//	private String resourceURI=null; 		//Specific resource access details
//	private String destination = null;		//Specific host and port access details
//	private long messageTimeout = 40000; 	//Message timeout before reaping

//	private Document xmlPayload = null; 	//Xml content
	//private EndpointReferenceType resourceEpr = null; //stores ordered selector values
//	private SelectorSetType selectorSet = null;
	
	public ResourceImpl(){};
	
	public ResourceImpl(String destination, String resourceURI,long timeout,SelectorSetType selectors) throws SOAPException, JAXBException{
		super(destination, resourceURI,timeout,selectors);//
	}

	public ResourceImpl(String destination, String resourceURI, long timeout) throws SOAPException, JAXBException {
		super(destination, resourceURI, timeout,null);
	}

	private void initJAXB() {
		//initialize JAXB bindings
        try {
			if(new Addressing().getXmlBinding()==null){
				SOAP.setXmlBinding(new XmlBinding(null));
			}
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ResourceImpl(Element eprElement, String endpointUrl) {
		initJAXB();
		XmlBinding binding=null;
		try {
			binding=new XmlBinding(null);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EndpointReferenceType epr = null;
		try {
			epr = ((JAXBElement<EndpointReferenceType>)binding.unmarshal(eprElement)).getValue();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.destination=epr.getAddress().getValue();
		if(endpointUrl!=null)
			this.destination=endpointUrl;
		List<Object> refParams = epr.getReferenceParameters().getAny();
		for (Object param : refParams) {
			Object testType= ((JAXBElement)param).getValue();
			if(testType instanceof AttributableURI){
				AttributableURI rUri = (AttributableURI)testType;
				setResourceURI(rUri.getValue());				
			}
			if(testType instanceof SelectorSetType){
				this.selectorSet=(SelectorSetType)testType;
			}
		}
		//messageTimeout=30000;
		
	}

}
