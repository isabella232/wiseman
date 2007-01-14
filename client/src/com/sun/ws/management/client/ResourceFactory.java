package com.sun.ws.management.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.impl.JAXBResourceImpl;
import com.sun.ws.management.client.impl.ResourceImpl;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

/**
 * Creates and configures Resources for use by a client.
 * 
 * @author spinder
 * 
 */
public class ResourceFactory {

	protected static final String UUID_SCHEME = "uuid:";

	public static final String LATEST = "LATEST";
	
    private static Logger log = Logger.getLogger(ResourceFactory.class.getName());

    /*
    {
		try {
			Message.initialize();
		} catch (SOAPException e) {
			log.severe("Failed to initalize wiseman with error: "+e.getMessage());
		}
    }
    */
	/**
	 * You should never create a factory. Access it statically.
	 */
	private static org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory wsManFactory= 
		new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
	private ResourceFactory() {
		super();
	}

	/**
	 * Creates a new resource instance from of a resource on the server.
	 *  
	 * @param destination A URL for the destination port of this service.
	 * @param resourceURI A resource URI indicating the type of resource to create 
	 * @param timeoutInMilliseconds Time to wait before giving up on creation
	 * @param content a w3c document representing the inital resource state 
	 * @param specVersion The wsman spec version of the client to create. You can 
	 * use null or the constant LATEST. 
	 * @return A Resource class representing the new resource created on the server. 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static Resource create(String destination, String resourceURI,
			long timeoutInMilliseconds, Document content, String specVersion)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException	{
				return create(destination, resourceURI,
						timeoutInMilliseconds, content, specVersion, null);
	}
	
	/**
	 * Creates a new resource instance from of a resource on the server.
	 *  
	 * @param destination A URL for the destination port of this service.
	 * @param resourceURI A resource URI indicating the type of resource to create 
	 * @param timeoutInMilliseconds Time to wait before giving up on creation
	 * @param content a w3c document representing the inital resource state 
	 * @param specVersion The wsman spec version of the client to create. You can 
	 * use null or the constant LATEST. 
	 * @param optionSet set of user defined options to use during the create operation 
	 * @return A Resource class representing the new resource created on the server. 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static Resource create(String destination, String resourceURI,
			long timeoutInMilliseconds, Document content, String specVersion,
			HashSet<OptionType> optionSet)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		// required: host, resourceUri
		if ((destination == null) | (resourceURI == null)) {
			String msg = "Host Address and ResourceURI cannot be null.";
			throw new IllegalArgumentException(msg);
		}


		// Build the document
		final Transfer xf = new Transfer();
		xf.setAction(Transfer.CREATE_ACTION_URI);
		// Replying to creator
		xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); 
		xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

		final Management mgmt = new Management(xf);
		mgmt.setXmlBinding(xf.getXmlBinding());
		mgmt.setTo(destination);
		mgmt.setResourceURI(resourceURI);
		final Duration timeout = DatatypeFactory.newInstance().newDuration(
				timeoutInMilliseconds);
		mgmt.setTimeout(timeout);

		// populate attribute details
		// setMessageTimeout(timeoutInMilliseconds);
		
		if (optionSet != null)
		{
			mgmt.setOptions(optionSet);
		}

		// Add the payload: plug the content passed in into the document
		if (content != null) {
			// Now take the created DOM and append it to the SOAP body
			mgmt.getBody().addDocument(content);
		}

		log.info("REQUEST:\n"+mgmt+"\n");
		// Send the request
		final Addressing response = HttpClient.sendRequest(mgmt);

		// Check for fault during message generation
		if (response.getBody().hasFault()) {
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault.getFaultString());
		}


		// parse response and retrieve contents.
		// Iterate through the create response to obtain the selectors
		SOAPBody body = response.getBody();
		JAXBElement element=(JAXBElement) response.getXmlBinding().unmarshal(body.getFirstChild());
		EndpointReferenceType createResponse = (EndpointReferenceType) element.getValue();
		ReferenceParametersType refParams = createResponse.getReferenceParameters();
		List<Object> parmList = refParams.getAny();
		AttributableURI uriType = null;
		for (Object parameterObj : parmList) {
			JAXBElement<AttributableURI> parameter2 = (JAXBElement<AttributableURI>) parameterObj;
			if (parameter2.getName().toString().indexOf("ResourceURI") > -1) {
				uriType = (AttributableURI) parameter2.getValue();
			}
		}
		EndpointReferenceType rcEpr = createResponse;
		//setResourceEpr(rcEpr);

		// Locate and populate SelectorSet if it's available
		Set<SelectorType> sels = mgmt.getSelectors();
		if (sels != null) {
			//TODO: retrieve the SelectorSet when stored in Mgmt object.
//			for (Iterator iter = sels.entrySet().iterator(); iter.hasNext();) {
//				String element = (String) iter.next();
//				Object value = sels.get(element);
//				System.out.println("K:V::"+element+":"+value);
			throw new FaultException();
//			}
		}else{
			String returnedResourceUri = "";
			if(uriType !=null){
				returnedResourceUri = uriType.getValue();
			}else{
			   returnedResourceUri = resourceURI;
			}
			ResourceImpl resource = new ResourceImpl(destination,returnedResourceUri,populateSelectorSet( createResponse));
			resource.setMessageTimeout(timeoutInMilliseconds);
			return resource;

		}
		
			
//			return create(destination,resourceURI, timeoutInMilliseconds,
//					content,specVersion,null,null);
		}
		
		
		/* (non-Javadoc)
		 * @see com.sun.ws.management.client.Resource#create(java.lang.String, java.lang.String, long, org.w3c.dom.Document)
		 */
		public static Resource createFragment(String destination,
				String resourceURI, SelectorSetType existingResourceId,
				long timeoutInMilliseconds,Document content,
				String specVersion, String fragmentExp, String dialect) throws SOAPException, 
				JAXBException, IOException, FaultException, 
				DatatypeConfigurationException {
			
			//required: host, resourceUri
			if((destination == null)||(resourceURI ==null)){
				String msg="Host Address and ResourceURI cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			//required: fragmentExp,existingResourceId
			if((fragmentExp == null)||(existingResourceId==null)){
				String msg="FragmentExpression or existingResourceId cannot be null.";
				throw new IllegalArgumentException(msg);
			}
			

			//Build the document
			final Transfer xf = new Transfer();
			xf.setAction(Transfer.CREATE_ACTION_URI);
			xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
			xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
			
			final Management mgmt = new Management(xf);
			mgmt.setXmlBinding(xf.getXmlBinding());
			mgmt.setTo(destination);
			mgmt.setResourceURI(resourceURI);
			final Duration timeout = 
				DatatypeFactory.newInstance().newDuration(timeoutInMilliseconds);
			mgmt.setTimeout(timeout);
			
	        //DONE: if xpathExpression is not null then generate fragment GET 
	        if((fragmentExp!=null)&&(fragmentExp.trim().length()>0)){
	        	//DONE: add the Fragement Header
	        	setFragmentHeader(fragmentExp,dialect,mgmt);
	        }
			
	        //DONE: add the selectorSetType to the request as well.
	        if(existingResourceId!=null){
	        	//Convert type for correct insertion
	        	List<SelectorType> list = existingResourceId.getSelector();
	        	Set<SelectorType> collection = new HashSet<SelectorType>();
	        	for (Iterator iter = list.iterator(); iter.hasNext();) {
					SelectorType element = (SelectorType) iter.next();
					collection.add(element);
				}
				mgmt.setSelectors(collection);
	        }

		// Add the payload: plug the content passed in into the document
		if (content != null) {
			// Now take the created DOM and wrap it into an Xml Fragment
			final MixedDataType mixedDataType = Management.FACTORY
					.createMixedDataType();
			mixedDataType.getContent().add(content.getDocumentElement());
			// create the XmlFragmentElement
			JAXBElement<MixedDataType> fragment = Management.FACTORY
					.createXmlFragment(mixedDataType);
			// Add the Fragment to the body
			xf.getXmlBinding().marshal(fragment, mgmt.getBody());
		}
			
			log.info("REQUEST:\n"+mgmt+"\n");
			//Send the request
			final Addressing response = HttpClient.sendRequest(mgmt);
			
			//Process the response to extract useful information.
			log.info("RESPONSE:\n"+response+"\n");

			//Check for fault during message generation
			if (response.getBody().hasFault()) {
				SOAPFault fault = response.getBody().getFault();
				throw new FaultException(fault.getFaultString());
			}
			
			//parse response and retrieve contents.
			// Iterate through the create response to obtain the selectors
			SOAPBody body = response.getBody();
			JAXBElement createResponse = (JAXBElement)response.getXmlBinding().unmarshal(body.getFirstChild());
			EndpointReferenceType resCreated = (EndpointReferenceType)createResponse.getValue();
			
			ReferenceParametersType refParams = resCreated.getReferenceParameters();
			List<Object> parmList = refParams.getAny();
			AttributableURI uriType =null;
			SelectorSetType selType = null;
			for (Object parameterObj : parmList) {
				JAXBElement<AttributableURI> parameter2=(JAXBElement<AttributableURI>)parameterObj;
				if(parameter2.getName().toString().indexOf("ResourceURI")>-1 ){
					uriType = (AttributableURI)parameter2.getValue();
				}
				if(parameter2.getName().toString().indexOf("SelectorSet")>-1 ){
				  Object obj = parameterObj;
				  JAXBElement<SelectorSetType> parameter3=(JAXBElement<SelectorSetType>)parameterObj;
				  selType = (SelectorSetType)parameter3.getValue();
//				  selType = (SelectorSetType)parameterObj;
				}
			}
			//EndpointReferenceType rcEpr = createResponse.getResourceCreated();
			//setResourceEpr(rcEpr);
			
			//Locate and populate SelectorSet if it's available
//			Set<SelectorType> sels = mgmt.getSelectors();
			Set<SelectorType> sels = null;
			if((selType!=null)&&(!selType.getSelector().isEmpty())){
				sels=new HashSet<SelectorType>();
				for (Iterator iter = selType.getSelector().iterator(); iter.hasNext();) {
					SelectorType element = (SelectorType) iter.next();
					sels.add(element);
				}
			}
			sels = null;
//			Set<SelectorType> sels = mgmt.getSelectors();
			if(sels!=null){
				//TODO: retrieve the SelectorSet when stored in Mgmt object.
//				for (Iterator iter = sels.entrySet().iterator(); iter.hasNext();) {
//					String element = (String) iter.next();
//					Object value = sels.get(element);
//					System.out.println("K:V::"+element+":"+value);
			throw new FaultException();
		} else {
			String returnedResourceUri = "";
			if (uriType != null) {
				returnedResourceUri = uriType.getValue();
			} else {
				returnedResourceUri = resourceURI;
			}
			ResourceImpl resource = new ResourceImpl(destination,
					returnedResourceUri,
					populateSelectorSet(resCreated));
			resource.setMessageTimeout(timeoutInMilliseconds);
			return resource ;
		}

	}

	/**
	 * Extracts and builds a selector set from the response from create. 
	 * @param createResponse
	 * @param rcEpr
	 * @return
	 */
	private static SelectorSetType populateSelectorSet(
			EndpointReferenceType createResponse) {
		EndpointReferenceType rcEpr = createResponse;
		// Selectors sought for in 3 obvious places: right after
		// ResourceCreated, RefPar | ResProp
		boolean foundSelectors = false;
		if (createResponse.getAny() != null) {
			// convert to String, parse for <SelectorSet>
			if (createResponse.getAny().toString().indexOf("<SelectorSet>") > -1) {
				// Extract, populate, set flag
				foundSelectors = true;
			}
		}

		List<Object> value = null;
		if (!foundSelectors) {
			ReferenceParametersType prTypes = rcEpr.getReferenceParameters();
			value = null;
			if ((prTypes.getAny() != null)
					&& ((value = prTypes.getAny()) != null)) {
				if (value.size() > 0) {
					for (Iterator iter = value.iterator(); iter.hasNext();) {
						JAXBElement element = (JAXBElement) iter.next();
						if (element.getDeclaredType().equals(
								SelectorSetType.class)) {
							// Extract, populate, set flag
							return ((JAXBElement<SelectorSetType>) element)
									.getValue();
						}
					}
				}
			}
		}
		if (!foundSelectors) {
			ReferencePropertiesType paTypes = rcEpr.getReferenceProperties();
			if ((paTypes.getAny() != null)
					&& ((value = paTypes.getAny()) != null)) {
				if (value.size() > 0) {
					for (Iterator iter = value.iterator(); iter.hasNext();) {
						JAXBElement element = (JAXBElement) iter.next();
						if (element.getDeclaredType().equals(
								SelectorSetType.class)) {
							// Extract, populate, set flag
							// this.selectorSet =
							// ((JAXBElement<SelectorSetType>)element).getValue();
							return ((JAXBElement<SelectorSetType>) element)
									.getValue();

						}
					}
				}
			}
		}
		return null;
	}

	public static void delete(Resource res) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		res.delete();
	}

	/**
	 * Static method for locating existing exposed resource(s). If SelectorSet
	 * is null, then an EnumerationResource is returned by default.
	 * 
	 * @param destination
	 * @param resourceURI
	 * @param timeout
	 * @param selectors
	 * @return
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static Resource[] find(String destination, String resourceURI,
			long timeout, SelectorSetType selectors) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		Resource[] resourceList = null;
			resourceList = new Resource[1];
			// lazy instantiation
			Resource enumerationResource = new ResourceImpl(destination, resourceURI,selectors);
			enumerationResource.setMessageTimeout(timeout);
			resourceList[0] = enumerationResource;

			return resourceList;
	}

	public static Resource[] find(String destination, String resourceURI,
			long timeout, Map<String,String> selectors) throws SOAPException,JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		SelectorSetType selectorsSetType=null;
		if(selectors!=null){
			selectorsSetType=wsManFactory.createSelectorSetType();
			List<SelectorType> selectorList = selectorsSetType.getSelector();
			for (String name : selectors.keySet()) {
				SelectorType selectorType = wsManFactory.createSelectorType();
				selectorType.setName(name);
				List<Serializable> content = selectorType.getContent();
				content.add(selectors.get(name));
				selectorList.add(selectorType);
			}
		}
		
		
		return find( destination,  resourceURI, timeout,  selectorsSetType);
	}
	
	//TODO: Once this method included in next build use that version
    public static void setFragmentHeader(final String expression, final String dialect,
    		Management mgmt) throws SOAPException, JAXBException {

		        // remove existing, if any
//		        removeChildren(mgmt.getHeader(), FRAGMENT_TRANSFER);
		        
		        final DialectableMixedDataType dialectableMixedDataType = 
		                Management.FACTORY.createDialectableMixedDataType();
		        if (dialect != null) {
//		            if (!XPath.isSupportedDialect(dialect)) {
//		                throw new FragmentDialectNotSupportedFault(XPath.SUPPORTED_FILTER_DIALECTS);
//		            }
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

	public static ServerIdentity getIdentity(String destination) throws SOAPException, IOException, JAXBException{
        try {
			return getIdentity(destination, -1);
		} catch (InterruptedException e) {
			throw new RuntimeException("getIdentity attempt interrupted.");
		} catch (TimeoutException e) {
			throw new RuntimeException("getIdentity attempt exceeded timout.");
		}
	}
	
	public static ServerIdentity getIdentity(final String destination,int timeout) throws SOAPException, IOException, JAXBException, InterruptedException, TimeoutException{
		return getIdentity(destination, timeout,null);
	}
	
	public static ServerIdentity getIdentity(final String destination,int timeout, final
    		Entry<String, String>... headers) throws SOAPException, IOException, JAXBException, InterruptedException, TimeoutException{
		
		IdentifyTask identifyTask = new IdentifyTask(destination,headers);
		
         	Thread identifyThread=new Thread(identifyTask);
        	identifyThread.start();
        	if(timeout<0)
        		identifyThread.join();
        	else
        		identifyThread.join(timeout);
        	
        	if(identifyThread.isAlive()){
        		String timeoutMessage = "An identify attempt to "+destination+" exceeded timeout interval and has been abandoned";
				log.info(timeoutMessage);
        		throw new TimeoutException(timeoutMessage);
        	}
        	
        return identifyTask.servIdent;
	}

	public static JAXBResource createJAXB(String destination, String resourceURI,
			long timeoutInMilliseconds, Object content, String specVersion,XmlBinding binding)
	throws SOAPException, JAXBException, IOException, FaultException,
	DatatypeConfigurationException {
		
		// Actually create this object on the server
		Document domContent = Management.newDocument();
		if(content != null) {
			binding.marshal(content, domContent);
		}

		Resource resource=create( destination, resourceURI,
				timeoutInMilliseconds,  domContent, specVersion);
		
		// now construct a JAXBResourceImpl to talk with it
		JAXBResource jaxbResource=new JAXBResourceImpl(resource.getDestination(),resource.getResourceUri(),resource.getSelectorSet(),binding);
		return jaxbResource ;
		
	}
	
	public static JAXBResource createJAXB(String destination, String resourceURI,
			long timeoutInMilliseconds, Object content,String specVersion, final String... packageNames)
	throws SOAPException, JAXBException, IOException, FaultException,
	DatatypeConfigurationException {
		
		return createJAXB( destination,  resourceURI,
				 timeoutInMilliseconds,  content,  specVersion, new XmlBinding(null,packageNames));
	}

}
