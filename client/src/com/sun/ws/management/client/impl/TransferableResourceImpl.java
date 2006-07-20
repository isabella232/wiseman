package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.TransferableResource;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

public class TransferableResourceImpl implements TransferableResource {
	
    protected static final String UUID_SCHEME = "uuid:";
    private static Logger log = Logger.getLogger(TransferableResourceImpl.class.getName());

    public static final QName FRAGMENT_TRANSFER =
        new QName(Management.NS_URI, "FragmentTransfer", Management.NS_PREFIX);

	public static final QName DIALECT =
	        new QName("Dialect");
    
    //Attributes
	protected String resourceURI=null; 			//Specific resource access details
	protected String destination = null;		//Specific host and port access details
	protected long messageTimeout = 40000; 	//Message timeout before reaping

//	private Document xmlPayload = null; 	//Xml content
	//private EndpointReferenceType resourceEpr = null; //stores ordered selector values
	protected SelectorSetType selectorSet = null;
	
	public TransferableResourceImpl(){};
	
	public TransferableResourceImpl(String destination, String resourceURI,long timeout,SelectorSetType selectors){
		setDestination(destination);
		setResourceURI(resourceURI);
		messageTimeout=timeout;
		this.selectorSet=selectors;
		
		initJAXB();

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

	public TransferableResourceImpl(Element eprElement, String endpointUrl) {
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
		
		// Determine callers return address. If anonymous, then use calling ERP's address
		this.destination=epr.getAddress().getValue();
		if(destination.equals("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"))
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
		messageTimeout=30000;
		
	}


	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#delete()
	 */
	public void delete() throws SOAPException, 
			JAXBException, IOException, FaultException, 
			DatatypeConfigurationException {
		delete(null,null);
	}
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#delete(java.lang.String, java.lang.String)
	 */
	public void delete(String fragmentRequest, String fragmentDialect) throws SOAPException, 
			JAXBException, IOException, FaultException, 
			DatatypeConfigurationException, AccessDeniedFault {
		
		//required: host, resourceUri
		if((destination == null)|(resourceURI ==null)){
			String msg="Host Address and ResourceURI cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
        //Build the document
        final Transfer xf = new Transfer();
        xf.setAction(Transfer.DELETE_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final Management mgmt = new Management(xf);
        mgmt.setTo(destination);
        mgmt.setResourceURI(resourceURI);
        final Duration timeout = 
        	DatatypeFactory.newInstance().newDuration(messageTimeout);
        mgmt.setTimeout(timeout);
        
        //populate attribute details
        setMessageTimeout(messageTimeout);
                
	    //DONE: if xpathExpression is not null then generate fragment GET 
	    if((fragmentRequest!=null)&&(fragmentRequest.trim().length()>0)){
	    	//DONE: add the Fragement Header
	    	setFragmentHeader(fragmentRequest,fragmentDialect,mgmt);
	    }
        
        //populate the Selector map        
		Set<org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType> selectors1=new HashSet<SelectorType>();        
        if(selectorSet!=null){
        	List<SelectorType> selectors = selectorSet.getSelector();
        	for (Iterator iter = selectors.iterator(); iter.hasNext();) {
				SelectorType element = (SelectorType) iter.next();
				selectors1.add(element);
			}
        	mgmt.setSelectors(selectors1);
        }
                
        log.fine("REQUEST:\n"+mgmt+"\n");
        //Send the request
        final Addressing response = HttpClient.sendRequest(mgmt);

        //Check for fault during message generation
        if (response.getBody().hasFault()) {
            SOAPFault fault = response.getBody().getFault();
            throw new FaultException(fault.getFaultString());
        }
        
        //Process the response to extract useful information.
        log.fine("RESPONSE:\n"+response+"\n");
               
	}


	public Object[] invoke(QName action, Map<QName, String> parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#put(org.w3c.dom.Document)
	 */
	public void put(Document content) throws SOAPException, 
			JAXBException, IOException, FaultException, 
			DatatypeConfigurationException {
		put(content,null,null);
	}
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#put(org.w3c.dom.Document, java.lang.String, java.lang.String)
	 */
	public void put(Document content,String fragmentExpression, String fragmentDialect ) throws SOAPException, 
			JAXBException, IOException, FaultException, 
			DatatypeConfigurationException {
		
		//required: host, resourceUri
		if((destination == null)|(resourceURI ==null)){
			String msg="Host Address and ResourceURI cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		//Populate attributes
		setResourceURI(resourceURI);
		setDestination(destination);
				
		//Build the document
		final Transfer xf = new Transfer();
		xf.setAction(Transfer.PUT_ACTION_URI);
		xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
		xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
		
		final Management mgmt = new Management(xf);
		mgmt.setTo(destination);
		mgmt.setResourceURI(resourceURI);
		final Duration timeout = DatatypeFactory.newInstance().newDuration(messageTimeout);
		mgmt.setTimeout(timeout);
		
		//populate attribute details
		setMessageTimeout(messageTimeout);

	    //DONE: if xpathExpression is not null then generate fragment GET
//		MixedDataType xmlFrag = null;
//		JAXBElement<MixedDataType> fragment = null;
		
	    if((fragmentExpression!=null)&&(fragmentExpression.trim().length()>0)){
	    	//DONE: add the Fragement Header
	    	setFragmentHeader(fragmentExpression,fragmentDialect,mgmt);
	    	
//	    	  if(content!=null){
//	    		  MixedDataType type = (new ObjectFactory()).createMixedDataType();
//	    		  type.getContent().add(content);
////	    		  JAXBElement<MixedDataType> frag = (new ObjectFactory()).createXmlFragment(type);
//	    		  fragment = (new ObjectFactory()).createXmlFragment(type);
//	    	  }
	    }

		//add the potential new content
		//Add the payload: plug the content passed in into the document
		if(content !=null){
			//DONE: wrap the content value passed in wsman:XmlFragment node
			// Now take the created DOM and append it to the SOAP body
			if(fragmentExpression!=null){
				Document newContent =mgmt.newDocument();

//				String[] pkgList={
//				 "org.dmtf.schemas.wbem.wsman._1.wsman",
//				 "org.xmlsoap.schemas.ws._2005._06.management"};
//				mgmt.getXmlBinding().marshal(fragment, newContent);
//				new XmlBinding(null, 
//				new XmlBinding(pkgList).marshal(fragment, newContent);
//###################				
//		        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
//		        mixedDataType.getContent().add(content);
//		        //create the XmlFragmentElement
//		        final JAXBElement<MixedDataType> xmlFragment = 
//		                Management.FACTORY.createXmlFragment(mixedDataType);
////		        
////		        //add the Fragment header passed in to the response
////		        fragmentHeader.setTextContent(xpathExp);
////		        response.getHeader().addChildElement(fragmentHeader);
//		        
//		        //add payload to the body
//		        new Addressing().getXmlBinding().marshal(xmlFragment, mgmt.getBody());
				
//###################
			    // Insert the root element node
				Element element = 
					newContent.createElementNS("http://schemas.dmtf.org/wbem/1/wsman.xsd","wsman:XmlFragment");
				element.setTextContent(xmlToString(content));
				newContent.appendChild(element);
//###################				
				mgmt.getBody().addDocument(newContent);
				
			}else{ //NON-FRAGMENT request processing.
			mgmt.getBody().addDocument(content);
			}
		}
		
		HashSet<SelectorType> selectors1=new HashSet<SelectorType>();        
        if(selectorSet!=null){
        	List<SelectorType> selectors = selectorSet.getSelector();
        	for (Iterator iter = selectors.iterator(); iter.hasNext();) {
				SelectorType element = (SelectorType) iter.next();
				selectors1.add(element);
			}
        	mgmt.setSelectors(selectors1);
        }
                
		log.fine("REQUEST:\n"+mgmt+"\n");
		//Send the request
		final Addressing response = HttpClient.sendRequest(mgmt);
		
		//Check for fault during message generation
		if (response.getBody().hasFault()) {
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault.getFaultString());
		}
		
		//Process the response to extract useful information.
		log.fine("RESPONSE:\n"+response+"\n");
		
		//parse response and retrieve contents.
		// Iterate through the create response to obtain the selectors
		//SOAPBody body = response.getBody();
				
	}


	
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#get()
	 */
	public ResourceState get() throws SOAPException, 
			JAXBException, IOException, FaultException, 
			DatatypeConfigurationException {
			return get(null,null);
	}

	
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#get(java.lang.String, java.lang.String)
	 */
	public ResourceState get(String xpathExpression,String dialect) throws SOAPException, 
			JAXBException, IOException, FaultException, 
			DatatypeConfigurationException {
		
		//required: host, resourceUri
		if((destination == null)||(resourceURI ==null)){
			String msg="Host Address and ResourceURI cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		//initialize JAXB bindings
        if(new Addressing().getXmlBinding()==null){
        	SOAP.setXmlBinding(new XmlBinding(null));
        }
        
        //Build the document
        final Transfer xf = new Transfer();
        xf.setAction(Transfer.GET_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final Management mgmt = new Management(xf);
        mgmt.setTo(destination);
        mgmt.setResourceURI(resourceURI);
        final Duration timeout = DatatypeFactory.newInstance().newDuration(messageTimeout);
        mgmt.setTimeout(timeout);
        
        //populate attribute details
        setMessageTimeout(messageTimeout);
                
	    //DONE: if xpathExpression is not null then generate fragment GET 
	    if((xpathExpression!=null)&&(xpathExpression.trim().length()>0)){
	    	//DONE: add the Fragement Header
	    	setFragmentHeader(xpathExpression,dialect,mgmt);
	    }
	    
        //populate the map        
		HashSet<SelectorType> selectors1=new HashSet<SelectorType>();        
        if(selectorSet!=null){
        	List<SelectorType> selectors = selectorSet.getSelector();
        	for (Iterator iter = selectors.iterator(); iter.hasNext();) {
				SelectorType element = (SelectorType) iter.next();
				selectors1.add(element);
			}
        	mgmt.setSelectors(selectors1);
        }
                
        log.fine("REQUEST:\n"+mgmt+"\n");
        //Send the request
        final Addressing response = HttpClient.sendRequest(mgmt);

        //Check for fault during message generation
        if (response.getBody().hasFault()) {
            SOAPFault fault = response.getBody().getFault();
            throw new FaultException(fault.getFaultString());
        }
        
        //Process the response to extract useful information.
        log.fine("RESPONSE:\n"+response+"\n");
        
        //parse response and retrieve contents.
        // Iterate through the create response to obtain the selectors
        SOAPBody body = response.getBody();
        
		return new ResourceStateImpl(body.extractContentAsDocument());
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#getResourceUri()
	 */
	public String getResourceUri() {
		return this.resourceURI;
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#getDestination()
	 */
	public String getDestination() {
		return this.destination;
	}

	public long defaultTimeout() {
		return this.messageTimeout;
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#getMessageTimeout()
	 */
	public long getMessageTimeout() {
		return this.messageTimeout;
	}

	public SelectorSetType getSelectorSet() {
		return this.selectorSet;
	}


	/**
	 * @param xmlPayload The xmlPayload to set.
	 */
//	private void setXmlPayload(Document createXmlPayload) {
//		this.xmlPayload = createXmlPayload;
//	}

	/**
	 * @param destination The destination to set.
	 */
	private void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @param messageTimeout The messageTimeout to set.
	 */
	private void setMessageTimeout(long messageTimeout) {
		this.messageTimeout = messageTimeout;
	}

	/**
	 * @param resourceEpr The resourceEpr to set.
	 */
//	private void setResourceEpr(EndpointReferenceType resourceEpr) {
//		this.resourceEpr = resourceEpr;
//	}

	/**
	 * @param resourceURI The resourceURI to set.
	 */
	protected void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#put(com.sun.ws.management.client.ResourceState)
	 */
//	private void setSelectorSet(SelectorSetType selectorSet) {
//		this.selectorSet = selectorSet;
//	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.Resource#put(com.sun.ws.management.client.ResourceState)
	 */
	public void put(ResourceState newState) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
		put(newState.getDocument()); 
	}

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


	@Override
	public String toString() {
		if(selectorSet!=null){
			String value="";
			List<SelectorType> selector = selectorSet.getSelector();
			for(int index=0;index<selector.size();index++){
				SelectorType sel = selector.get(index);
				value=value+sel.getContent()+" ";
			}
			return value;
		}
		else
			return super.toString();
	}

	public Resource[] pullResources(String enumerationContext, int maxTime, int maxElements, int maxCharacters,String endpointUrl) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceState optomizedEnumerate(String[] filters, String dialect, boolean useEprs) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}
	//TODO: Once this method included in next build use that version
    public void setFragmentHeader(final String expression, final String dialect,
    		Management mgmt) throws SOAPException, JAXBException {

        // remove existing, if any
//        removeChildren(mgmt.getHeader(), FRAGMENT_TRANSFER);
        
        final DialectableMixedDataType dialectableMixedDataType = 
                Management.FACTORY.createDialectableMixedDataType();
        if (dialect != null) {
//            if (!XPath.isSupportedDialect(dialect)) {
//                throw new FragmentDialectNotSupportedFault(XPath.SUPPORTED_FILTER_DIALECTS);
//            }
            dialectableMixedDataType.setDialect(dialect);
        }
        dialectableMixedDataType.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, 
                Boolean.TRUE.toString());
        
        //add the query string to the content of the FragmentTransfer Header
        dialectableMixedDataType.getContent().add(expression);
        
        final JAXBElement<DialectableMixedDataType> fragmentTransfer =
                Management.FACTORY.createFragmentTransfer(dialectableMixedDataType);
        
        //set the SOAP Header for Fragment Transfer
        new Addressing().getXmlBinding().marshal(fragmentTransfer, mgmt.getHeader());
    }	
	
    public static String xmlToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
