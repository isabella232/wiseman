package com.sun.ws.management.client.impl;

import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

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
	
	public ResourceImpl(String destination, String resourceURI,long timeout,SelectorSetType selectors){
		super(destination, resourceURI,timeout,selectors);//
	}

	public ResourceImpl(String destination, String resourceURI, long timeout) {
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
			if(testType instanceof AttributedURI){
				AttributedURI rUri = (AttributedURI)testType;
				setResourceURI(rUri.getValue());				
			}
			if(testType instanceof SelectorSetType){
				this.selectorSet=(SelectorSetType)testType;
			}
		}
		//messageTimeout=30000;
		
	}


	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.Resource#get(java.lang.String, java.lang.String, long, org.w3c.dom.Document)
	 */
//	public void delete() throws SOAPException, 
//			JAXBException, IOException, FaultException, 
//			DatatypeConfigurationException {
//		
//		//required: host, resourceUri
//		if((destination == null)|(resourceURI ==null)){
//			String msg="Host Address and ResourceURI cannot be null.";
//			throw new IllegalArgumentException(msg);
//		}
//		
//        //Build the document
//        final Transfer xf = new Transfer();
//        xf.setAction(Transfer.DELETE_ACTION_URI);
//        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
//        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
//        
//        final Management mgmt = new Management(xf);
//        mgmt.setTo(destination);
//        mgmt.setResourceURI(resourceURI);
//        final Duration timeout = 
//        	DatatypeFactory.newInstance().newDuration(messageTimeout);
//        mgmt.setTimeout(timeout);
//        
//        //populate attribute details
//        setMessageTimeout(messageTimeout);
//                
//        //populate the Selector map        
//		//HashMap<String, Object> selectors1=new HashMap<String, Object>();  
//		HashSet<SelectorType> selectors1=new HashSet<SelectorType>();  
//        if(selectorSet!=null){
//        	List<SelectorType> selectors = selectorSet.getSelector();
//        	for (Iterator iter = selectors.iterator(); iter.hasNext();) {
//				SelectorType element = (SelectorType) iter.next();
//				selectors1.add(element);
//			}
//        	mgmt.setSelectors(selectors1);
//        }
//                
//        log.fine("REQUEST:\n"+mgmt+"\n");
//        //Send the request
//        final Addressing response = HttpClient.sendRequest(mgmt);
//
//        //Check for fault during message generation
//        if (response.getBody().hasFault()) {
//            SOAPFault fault = response.getBody().getFault();
//            throw new FaultException(fault.getFaultString());
//        }
//        
//        //Process the response to extract useful information.
//        log.fine("RESPONSE:\n"+response+"\n");
//               
//	}


	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.Resource#create(java.lang.String, java.lang.String, long, org.w3c.dom.Document)
	 */
//	public void put(Document content) throws SOAPException, 
//			JAXBException, IOException, FaultException, 
//			DatatypeConfigurationException {
//		
//		//required: host, resourceUri
//		if((destination == null)|(resourceURI ==null)){
//			String msg="Host Address and ResourceURI cannot be null.";
//			throw new IllegalArgumentException(msg);
//		}
//		
//		//Populate attributes
//		setResourceURI(resourceURI);
//		setDestination(destination);
//				
//		//Build the document
//		final Transfer xf = new Transfer();
//		xf.setAction(Transfer.PUT_ACTION_URI);
//		xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
//		xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
//		
//		final Management mgmt = new Management(xf);
//		mgmt.setTo(destination);
//		mgmt.setResourceURI(resourceURI);
//		final Duration timeout = DatatypeFactory.newInstance().newDuration(messageTimeout);
//		mgmt.setTimeout(timeout);
//		
//		//populate attribute details
//		setMessageTimeout(messageTimeout);
//
//		//add the potential new content
//		//Add the payload: plug the content passed in into the document
//		if(content !=null){
//			// Now take the created DOM and append it to the SOAP body
//			mgmt.getBody().addDocument(content);
//			//setXmlPayload(content);
//		}
//		
//		HashSet<SelectorType> selectors1=new HashSet<SelectorType>();        
//        if(selectorSet!=null){
//        	List<SelectorType> selectors = selectorSet.getSelector();
//        	for (Iterator iter = selectors.iterator(); iter.hasNext();) {
//				SelectorType element = (SelectorType) iter.next();
//				selectors1.add(element);
//			}
//        	mgmt.setSelectors(selectors1);
//        }
//                
//		log.fine("REQUEST:\n"+mgmt+"\n");
//		//Send the request
//		final Addressing response = HttpClient.sendRequest(mgmt);
//		
//		//Check for fault during message generation
//		if (response.getBody().hasFault()) {
//			SOAPFault fault = response.getBody().getFault();
//			throw new FaultException(fault.getFaultString());
//		}
//		
//		//Process the response to extract useful information.
//		log.fine("RESPONSE:\n"+response+"\n");
//		
//		//parse response and retrieve contents.
//		// Iterate through the create response to obtain the selectors
//		//SOAPBody body = response.getBody();
//				
//	}


	
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.Resource#get(java.lang.String, java.lang.String, long, org.w3c.dom.Document)
	 */
//	public ResourceState get() throws SOAPException, 
//			JAXBException, IOException, FaultException, 
//			DatatypeConfigurationException {
//		
//		//required: host, resourceUri
//		if((destination == null)|(resourceURI ==null)){
//			String msg="Host Address and ResourceURI cannot be null.";
//			throw new IllegalArgumentException(msg);
//		}
//		
//		//initialize JAXB bindings
//        if(new Addressing().getXmlBinding()==null){
//        	SOAP.setXmlBinding(new XmlBinding());
//        }
//        
//        //Build the document
//        final Transfer xf = new Transfer();
//        xf.setAction(Transfer.GET_ACTION_URI);
//        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
//        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
//        
//        final Management mgmt = new Management(xf);
//        mgmt.setTo(destination);
//        mgmt.setResourceURI(resourceURI);
//        final Duration timeout = DatatypeFactory.newInstance().newDuration(messageTimeout);
//        mgmt.setTimeout(timeout);
//        
//        //populate attribute details
//        setMessageTimeout(messageTimeout);
//                
//        //populate the map        
//		HashSet<SelectorType> selectors1=new HashSet<SelectorType>();        
//        if(selectorSet!=null){
//        	List<SelectorType> selectors = selectorSet.getSelector();
//        	for (Iterator iter = selectors.iterator(); iter.hasNext();) {
//				SelectorType element = (SelectorType) iter.next();
//				selectors1.add(element);
//			}
//        	mgmt.setSelectors(selectors1);
//        }
//                
//        log.fine("REQUEST:\n"+mgmt+"\n");
//        //Send the request
//        final Addressing response = HttpClient.sendRequest(mgmt);
//
//        //Check for fault during message generation
//        if (response.getBody().hasFault()) {
//            SOAPFault fault = response.getBody().getFault();
//            throw new FaultException(fault.getFaultString());
//        }
//        
//        //Process the response to extract useful information.
//        log.fine("RESPONSE:\n"+response+"\n");
//        
//        //parse response and retrieve contents.
//        // Iterate through the create response to obtain the selectors
//        SOAPBody body = response.getBody();
//        
//		return new ResourceStateImpl(body.extractContentAsDocument());
//	}

//	public String getResourceUri() {
//		return this.resourceURI;
//	}
//
//	public String getDestination() {
//		return this.destination;
//	}
//
//	public long defaultTimeout() {
//		return this.messageTimeout;
//	}
//
//	public long getMessageTimeout() {
//		return this.messageTimeout;
//	}
//
//	public SelectorSetType getSelectorSet() {
//		return this.selectorSet;
//	}


	/**
	 * @param xmlPayload The xmlPayload to set.
	 */
//	private void setXmlPayload(Document createXmlPayload) {
//		this.xmlPayload = createXmlPayload;
//	}

	/**
	 * @param destination The destination to set.
	 */
//	private void setDestination(String destination) {
//		this.destination = destination;
//	}
//
//	/**
//	 * @param messageTimeout The messageTimeout to set.
//	 */
//	private void setMessageTimeout(long messageTimeout) {
//		this.messageTimeout = messageTimeout;
//	}
//
//	/**
//	 * @param resourceEpr The resourceEpr to set.
//	 */
////	private void setResourceEpr(EndpointReferenceType resourceEpr) {
////		this.resourceEpr = resourceEpr;
////	}
//
//	/**
//	 * @param resourceURI The resourceURI to set.
//	 */
//	private void setResourceURI(String resourceURI) {
//		this.resourceURI = resourceURI;
//	}

	/**
	 * @param selectorSet The selectorSet to set.
	 */
//	private void setSelectorSet(SelectorSetType selectorSet) {
//		this.selectorSet = selectorSet;
//	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.Resource#put(com.sun.ws.management.client.ResourceState)
	 */
//	public void put(ResourceState newState) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
//		put(newState.getDocument()); 
//	}
//
//	public String enumerate(String[] filters, String dialect, boolean useEprs) throws SOAPException,JAXBException, IOException, FaultException, DatatypeConfigurationException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public ResourceState pull(String enumerationContext, int maxTime, int maxElements, int maxCharacters) throws SOAPException,JAXBException, IOException, FaultException, DatatypeConfigurationException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public void release(String enumerationContext) throws SOAPException,JAXBException, 
//	IOException, FaultException, DatatypeConfigurationException  {
//		
//	}
//	public void renew(String enumerationContext) throws SOAPException,JAXBException, 
//	IOException, FaultException, DatatypeConfigurationException  {
//		
//	}
//
//	public EndpointReferenceType subscribe(EndpointReferenceType EndToEpr, String deliveryType, Duration expires, String[] filters, String dialect) {
//		// TODO Auto-generated method stub
//		return null;
//	}


//	public Resource[] pullResources(String enumerationContext, int maxTime, int maxElements, int maxCharacters,String endpointUrl) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public ResourceState optomizedEnumerate(String[] filters, String dialect, boolean useEprs) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//

}
