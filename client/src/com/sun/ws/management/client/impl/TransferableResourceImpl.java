package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
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
import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.addressing.Addressing;
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
	/**
	 * The Resource URI that should be use to refer to this resource when calls are made to it.
	 */
	protected String resourceURI=null; 			//Specific resource access details
	/**
	 * The URL as a string for the HTTP transport target for this transfer resource.
	 */
	protected String destination = null;		//Specific host and port access details
	/**
	 * The default timeout for all transfer operations
	 */
	protected long messageTimeout = 30000; 		//Message timeout before reaping

	/**
	 * This represents the selector set used to identify this resource. If it is null
	 * there will be no selector set encoded into your requests.
	 */
	protected SelectorSetType selectorSet = null;
	/**
	 * Used to indicate the max size of a response to any transfer request made by this resource. If it is -1
	 * then it will not appear in your requests.
	 */
	protected long  maxEnvelopeSize=-1;
	
    private String replyTo=Addressing.ANONYMOUS_ENDPOINT_URI;

    /**
	 * A Transferable resource is package local. It is not intended to be constructed by anything 
	 * besides its factory and its unit tests. Please to not make it public.
	 * @param destination A URL the represents the endpoint of this operation.
	 * @param resourceURI a resource URI to refere to this type of resource.
	 * @param timeout the default timeout to be used in operations.
	 * @param selectors a set of selectors to use to identify this resource uniquely.
	 * @throws SOAPException
	 * @throws JAXBException
	 */
	TransferableResourceImpl(String destination, String resourceURI,SelectorSetType selectors) throws SOAPException, JAXBException{
		setDestination(destination);
		setResourceUri(resourceURI);
		this.selectorSet=selectors;
		
		initJAXB();
	}

	/** This constructor is intended to allow atransfereable resource to be constructed as part
	 * of an EPR enumeration. It is expected that the provided epr contains all the required selectors 
	 * for this resource to be located.
	 * @param eprElement
	 * @param endpointUrl
	 * @throws SOAPException
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	TransferableResourceImpl(Element eprElement, String endpointUrl) throws SOAPException, JAXBException {
		initJAXB();
		XmlBinding binding=new XmlBinding(null);
		EndpointReferenceType epr = null;
		epr = ((JAXBElement<EndpointReferenceType>)binding.unmarshal(eprElement)).getValue();
		
		// Determine callers return address. If anonymous, then use calling ERP's address
		this.destination=epr.getAddress().getValue();
		if(destination.equals("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"))
			if(endpointUrl!=null)
				this.destination=endpointUrl;
		
		// Repack ref params as separate resource URI and Selectors
		List<Object> refParams = epr.getReferenceParameters().getAny();
		for (Object param : refParams) {
			Object testType= ((JAXBElement)param).getValue();
			if(testType instanceof AttributableURI){
				AttributableURI rUri = (AttributableURI)testType;
				setResourceUri(rUri.getValue());				
			}
			if(testType instanceof SelectorSetType){
				this.selectorSet=(SelectorSetType)testType;
			}
		}
	}

	
	/**
	 * If the client is the first part of wiseman to be used in this VM then this operation makes sure it has been properly initalized before any transfer is attempted.
	 * @throws SOAPException
	 * @throws JAXBException
	 */
	private void initJAXB() throws SOAPException, JAXBException {
		//initialize JAXB bindings
		Message.initialize();

		if (new Addressing().getXmlBinding() == null)
		{
		    SOAP.setXmlBinding(new XmlBinding(null));
		}
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
		
		//Build the document
		final Transfer xf = setTransferProperties(Transfer.DELETE_ACTION_URI);		
		final Management mgmt = setManagementProperties(xf);
                           
	    // If xpathExpression is not null then generate fragment GET 
	    if((fragmentRequest!=null)&&(fragmentRequest.trim().length()>0)){
	    	// Add the Fragement Header
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
            log.severe("FAULT:\n"+response+"\n");
            SOAPFault fault = response.getBody().getFault();
            throw new FaultException(fault.getFaultString());
        }
        
        //Process the response to extract useful information.
        log.fine("RESPONSE:\n"+response+"\n");
               
	}


	// TODO obiwan314 We need to get this done
	public Object[] invoke(QName action, Map<QName, String> parameters) {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#put(org.w3c.dom.Document)
	 */
	public ResourceState put(Document content) throws SOAPException, 
			JAXBException, IOException, FaultException, 
			DatatypeConfigurationException {
		return put(content,null,null);
	}
	
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.TransferableResource#put(org.w3c.dom.Document, java.lang.String, java.lang.String)
	 */
	public ResourceState put(Document content,String fragmentExpression, String fragmentDialect ) throws SOAPException, 
			JAXBException, IOException, FaultException, 
			DatatypeConfigurationException {
		
		//Build the document
		final Transfer xf = setTransferProperties(Transfer.PUT_ACTION_URI);		
		final Management mgmt = setManagementProperties(xf);
		
	    // If xpathExpression is not null then generate fragment GET
		
	    if((fragmentExpression!=null)&&(fragmentExpression.trim().length()>0)){
	    	// Add the Fragement Header
	    	setFragmentHeader(fragmentExpression,fragmentDialect,mgmt);	    	
	    }

		// Add the potential new content
		// Add the payload: plug the content passed in into the document
		if(content !=null){
			// Wrap the content value passed in wsman:XmlFragment node
			// Now take the created DOM and append it to the SOAP body
			if(fragmentExpression!=null){
				Document newContent =Message.newDocument();

			    // Insert the root element node
				Element element = 
					newContent.createElementNS("http://schemas.dmtf.org/wbem/1/wsman.xsd","wsman:XmlFragment");
				element.setTextContent(xmlToString(content));
				newContent.appendChild(element);

				mgmt.getBody().addDocument(newContent);
				
			}else { 
				//NON-FRAGMENT request processing.
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
        SOAPBody body = response.getBody();
        
       try {
        Document bodyDoc = body.extractContentAsDocument();
		return new ResourceStateImpl(bodyDoc);
       } catch (SOAPException e){
    	   return null;
       }
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.Resource#put(com.sun.ws.management.client.ResourceState)
	 */
	public ResourceState put(ResourceState newState) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
		return put(newState.getDocument()); 
	}

	protected Transfer setTransferProperties(String action) throws JAXBException, SOAPException {
		//required: host, resourceUri
		if((destination == null)|(resourceURI ==null)){
			String msg="Host Address and ResourceURI cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		Transfer xf=new Transfer();
		xf.setAction(action);
		xf.setReplyTo(replyTo); //Replying to creator
		xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
		return xf;
	}


	
	protected Management setManagementProperties(Addressing xf) throws SOAPException, JAXBException, DatatypeConfigurationException {
		final Management mgmt = new Management(xf);
		mgmt.setTo(destination);
		mgmt.setResourceURI(resourceURI);
		
		if(messageTimeout>0){
			final Duration timeout = DatatypeFactory.newInstance().newDuration(messageTimeout);
			mgmt.setTimeout(timeout);
		}
		
        if(maxEnvelopeSize>0){
        	MaxEnvelopeSizeType size = TransferableResource.managementFactory.createMaxEnvelopeSizeType();
        	BigInteger bi = new BigInteger(""+maxEnvelopeSize);
        	size.setValue(bi);
        	mgmt.setMaxEnvelopeSize(size);
        }

		return mgmt;
		
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
				
		//Build the document
		final Transfer xf = setTransferProperties(Transfer.GET_ACTION_URI);		
		final Management mgmt = setManagementProperties(xf);
        
	    // If xpathExpression is not null then generate fragment GET 
	    if((xpathExpression!=null)&&(xpathExpression.trim().length()>0)){
	    	// Add the Fragement Header
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
	 * @param destination The destination to set.
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @param messageTimeout The messageTimeout to set.
	 */
	public void setMessageTimeout(long messageTimeout) {
		this.messageTimeout = messageTimeout;
	}




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


	//TODO: Once this method included in next build use that version
    public void setFragmentHeader(final String expression, final String dialect,
    		Management mgmt) throws SOAPException, JAXBException {

        
        final DialectableMixedDataType dialectableMixedDataType = 
                Management.FACTORY.createDialectableMixedDataType();
        if (dialect != null) {
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
    
	public void setMaxEnvelopeSize(long i) {
		
		maxEnvelopeSize=i;
	}

	public long getMaxEnvelopeSize() {
		return maxEnvelopeSize;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	/**
	 * @param resourceURI The resourceURI to set.
	 */
	public void setResourceUri(String resourceURI) {
		this.resourceURI = resourceURI;
	}


}
