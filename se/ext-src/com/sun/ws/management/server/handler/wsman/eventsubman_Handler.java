package com.sun.ws.management.server.handler.wsman;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

//import management.MetadataTest;

import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.w3._2003._05.soap_envelope.Envelope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.eventing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.MetadataSection;

//import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.addressing.Addressing;
//import com.sun.ws.management.client.exceptions.NoMatchFoundException;
//import com.sun.ws.management.client.ResourceState;
//import com.sun.ws.management.client.exceptions.NoMatchFoundException;
//import com.sun.ws.management.client.impl.ResourceStateImpl;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingMessageValues.CreationTypes;
import com.sun.ws.management.framework.eventing.EventSourceInterface;
import com.sun.ws.management.framework.eventing.SubscriptionManagerInterface;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.identify.IdentifyUtility;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementEnumerationAnnotation;
import com.sun.ws.management.mex.MetadataUtility;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.IteratorFactory;
//import com.sun.ws.management.server.handler.wsman.auth.eventcreator_Handler;
import com.sun.ws.management.server.handler.wsman.test.java.system.properties_Handler;
import com.sun.ws.management.server.handler.wsman.test.java.system.properties_IteratorFactory;
import com.sun.ws.management.server.handler.wsman.test.java.system.properties_IteratorFactory.properties_Iterator;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.xml.ws.addressing.model.ActionNotSupportedException;

@WsManagementEnumerationAnnotation(
	getDefaultAddressModelDefinition=
		@WsManagementDefaultAddressingModelAnnotation(
			getDefaultAddressDefinition=
				@WsManagementAddressDetailsAnnotation(
					wsaTo=eventsubman_Handler.DESTINATION, 
					wsmanResourceURI=eventsubman_Handler.RESOURCE_URI
				),
		resourceMetaDataUID = eventsubman_Handler.DEFAULT_SUBSCRIPTION_MANAGER_UID
		),
	resourceEnumerationAccessRecipe = 
		"Enumerate and Optimized Enumeration with no arguments returns all available Event Sources.",
	resourceFilterUsageDescription = 
		 "Filtering via RESOURCE_META_DATA_UID. Ex. env:Envelope/env:Header/wsmeta:ResourceMetaDataUID/text()='"+
		 eventsubman_Handler.DEFAULT_SUBSCRIPTION_MANAGER_UID+"'"
)
public class eventsubman_Handler implements Handler, SubscriptionManagerInterface {

	public static final String DESTINATION = ManagementMessageValues.WSMAN_DESTINATION;
	public static final String RESOURCE_URI = "wsman:eventsubman";
	public static final String DEFAULT_SUBSCRIPTION_MANAGER_UID ="https://wiseman.java.net/ri/SubscriptionManager";
	private static final Logger LOG = 
		Logger.getLogger(eventsubman_Handler.class.getName());
	//Use HashMap to store event_src_key and EventSource instances
	private static HashMap<String,EventSourceInterface> eventSources = 
		new HashMap<String, EventSourceInterface>();
	private static HashMap<String,Object> eventSinks = 
		new HashMap<String, Object>();
	private static ObjectFactory env_factory = new ObjectFactory();
	private static XmlBinding binding = null;
	private static Duration DEFAULT_SUBSCRIPTION_DURATION = null;
	private static Duration DEFAULT_SUBSCRIPTION_DURATION_FLOOR = null;
	static {
	    try {
		 Eventing ev = new Eventing();
		 binding = ev.getXmlBinding();
		 DEFAULT_SUBSCRIPTION_DURATION = 
			 DatatypeFactory.newInstance().newDuration(
					 EventingMessageValues.DEFAULT_SUBSCRIPTION_TIMEOUT);
		 DEFAULT_SUBSCRIPTION_DURATION_FLOOR = 
			 DatatypeFactory.newInstance().newDuration(
					 EventingMessageValues.DEFAULT_SUBSCRIPTION_TIMEOUT_FLOOR);
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}
	//Share the Xpath instance
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	{
		xpath.setNamespaceContext(AnnotationProcessor.getMetaDataNamespaceContext());
	}
	//This is the Metadata description of this Management instance which is lazily instantiated.
	private static Management subscriptionManMetaData =null; 
	
	public void handle(String action, String resource, 
			HandlerContext context, Management request, 
			Management response) throws Exception {

		if(action.equals(Enumeration.ENUMERATE_ACTION_URI)){
			Enumeration enuResponse = new Enumeration(response);
			Enumeration enuRequest = new Enumeration(request);
	        enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
	        
	        synchronized (this) {
	        	// Make sure there is an Iterator factory registered for this resource
	        	if (EnumerationSupport.getIteratorFactory(resource) == null) {
	        		EnumerationSupport.registerIteratorFactory(resource,
	        				new EventingSubscriptionManagerIterator(resource));
	        	}
	        }
	        EnumerationSupport.enumerate(context, enuRequest, enuResponse);
		}
		if(action.equals(Transfer.CREATE_ACTION_URI)){
			//locate the CreationSubType
			SOAPElement creationHeader =  ManagementUtility.locateHeader(request.getHeaders(), 
					EventingMessageValues.EVENTING_CREATION_TYPES);
			String type =null;
			if((creationHeader!=null)&&((type=creationHeader.getTextContent())!=null)&&
					(CreationTypes.SUBSCRIPTION_SOURCE.name().equals(type.trim()))){
				//locate the EventSourceId if sent.
				SOAPElement eventSrcId =  ManagementUtility.locateHeader(request.getHeaders(), 
						EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID);
				String eventSourceUID = null;
				if(eventSrcId!=null){
					eventSourceUID = eventSrcId.getTextContent();
					//Check that this id is not already used. If generate new otherwise use suggested.
					if(eventSources.containsKey(eventSourceUID)){
						eventSourceUID = EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME+
						UUID.randomUUID();
					}
					
					//Now populate the ResourceCreated value for the response object
					HashMap<String, String> selectorMap = new HashMap<String,String>();
					selectorMap.put(EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME,
							eventSourceUID);
					TransferExtensions xferResponse = new TransferExtensions(response);
					EndpointReferenceType epr = 
						TransferExtensions.createEndpointReference(
								request.getTo(), request.getResourceURI(), 
								selectorMap);
					xferResponse.setCreateResponse(epr);
				}
			}//Event source creation processing
			Subscribe subscribeContent = null;
			if((creationHeader!=null)&&((type=creationHeader.getTextContent())!=null)&&
					(CreationTypes.NEW_SUBSCRIBER.name().equals(type.trim()))){
				//attempt to extract the subscribe message sent if any
				Transfer createRequest = new Transfer(request);
				Document createBodyDoc = 
					createRequest.getBody().extractContentAsDocument();
				if(createBodyDoc!=null){
//					Subscribe subscribeContent = env_factory.createSubscribe();
					Object ob=	binding.unmarshal(createBodyDoc);
				  subscribeContent = (Subscribe) ob;
//				  JAXBElement<Subscribe> unmarsh = 
//					  (JAXBElement<Subscribe>) binding.unmarshal(createBodyDoc);
////					JAXBElement<UserType> unmarshal = (JAXBElement<UserType>) binding
////					.unmarshal(resourceStateDom);
////				JAXBElement<UserType> userReturnedElement = unmarshal;
////				  UserType returnedUser = (UserType) userReturnedElement.getValue();
//				  if((unmarsh!=null)&&(unmarsh.getValue()!=null)){
//					  subscribeContent = (Subscribe) unmarsh.getValue();
//					  
//				  }
				}
				//locate the EventSinkId if sent.
				SOAPElement eventSinkId =  ManagementUtility.locateHeader(request.getHeaders(), 
						EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID);
				String eventSinkUID = null;
				if(eventSinkId!=null){
					eventSinkUID = eventSinkId.getTextContent();
//System.out.println("###########subscriptionManagerCreateProcessing:"+eventSrcId.getNodeName()+":"+eventSourceUID);
				  //Check that this id is not already used. If generate new otherwise use suggested.
//TODO: EventSinkId					
				  if(eventSinks.containsKey(eventSinkUID)){
					  eventSinkUID = EventingMessageValues.EVENT_SINK_NODE_NAME+
					    UUID.randomUUID();
//					  cridget
				  }
				  //Now populate the ResourceCreated value for the response object
					HashMap<String, String> selectorMap = new HashMap<String,String>();
					selectorMap.put(EventingMessageValues.EVENT_SINK_NODE_NAME,
							eventSinkUID);
					TransferExtensions xferResponse = new TransferExtensions(response);
					EndpointReferenceType epr = 
						TransferExtensions.createEndpointReference(
								request.getTo(), request.getResourceURI(), 
								selectorMap);
					xferResponse.setCreateResponse(epr);
				
					//Handle/generate the subcription expiration details
					Duration requestedExpiration = null;
					 //attempt to locate the expiration request details
					 if(subscribeContent!=null){
						String expiresContent = subscribeContent.getExpires();
						if((expiresContent!=null)&&(expiresContent.trim().length()>0)){
//							DatatypeFactory.newInstance().newDuration(300000).toString();							
						  
						  try{
						   requestedExpiration =
							  DatatypeFactory.newInstance().newDuration(
									  expiresContent.trim());
						  }catch (IllegalArgumentException isex){
						  }
						  //decide whether to honor that expiration request information
						  if(requestedExpiration!=null){
							 if(requestedExpiration.isLongerThan(
									 DEFAULT_SUBSCRIPTION_DURATION_FLOOR)){
							   //for now as we're only accepting durations then proceed
							   //TODO: add additional processing for specific time instances	 
							   //   where invalid expiration time.
							 }else{//Insert the default timeout value
								requestedExpiration =DEFAULT_SUBSCRIPTION_DURATION;
							 }
						  }else{//attempt to parse the requested expiration timeout but null 
							 //set to default expiration timeout value
							 requestedExpiration = DEFAULT_SUBSCRIPTION_DURATION;
						  }
						}
					 }
					 //TODO: now generate the subscribeResponse object and return it as body of CreateResponse.
					 SubscribeResponse subscribeResponseBody = 
						 env_factory.createSubscribeResponse();
					  //locate the details to define the SubscriptionManager EPR
//					 Management subscriptionManMetaData = ManagementUtility.findAnnotatedResourceByUID(
					 if(subscriptionManMetaData==null){
//						 subscriptionManMetaData = ManagementUtility.findAnnotatedResourceByUID(
					   subscriptionManMetaData = AnnotationProcessor.findAnnotatedResourceByUID(
							 DEFAULT_SUBSCRIPTION_MANAGER_UID, ManagementMessageValues.WSMAN_DESTINATION);
					 }
					  if(subscriptionManMetaData!=null){
					     //translate into subscription manager details
					     EndpointReferenceType subscriptionManEpr = 
						   ManagementUtility.extractEprType(subscriptionManMetaData);
					     //populate subscribeResponseBody
					     subscribeResponseBody.setSubscriptionManager(subscriptionManEpr);
					     //handle the Expiration part of SubscribeResponse object
					     subscribeResponseBody.setExpires(requestedExpiration.toString());
					     //set the populated subscribeResponse as the CreateResponse.body
					     Document createRespBody = Management.newDocument();
					     binding.marshal(subscribeResponseBody, createRespBody);
					     if(createRespBody!=null){
					    	 xferResponse.getBody().addDocument(createRespBody);
					     }
					  }
//					 this.getClass().
					 //TODO: Generate the duration instance and stuff into custom header 
					 //TODO: add header to the response object
			   }				
//			}//Event sink creation processing
			}//End of CREATE action for new SUBSCRIBER
			
		}//End of CREATE ACTION processing
	}

	public String getSubscriptionManagerAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSubscriptionManagerResourceURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public void renew(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse) {
		// TODO Auto-generated method stub
		
	}

	public void unsubsubscribe(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.framework.handlers.DelegatingHandler#enumerate(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.enumeration.Enumeration, com.sun.ws.management.enumeration.Enumeration)
	 */
//	@Override
	public void enumerate(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse) {
//		// TODO Auto-generated method stub
////		super.enumerate(context, enuRequest, enuResponse);
////		throw new ActionNotSupportedException("TestMEssage");
//        enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
////        enuResponse.addNamespaceDeclarations(NAMESPACES);
//        
//        synchronized (this) {
//        	// Make sure there is an Iterator factory registered for this resource
//        	if (EnumerationSupport.getIteratorFactory(resource) == null) {
//        		EnumerationSupport.registerIteratorFactory(resource,
//        				new properties_IteratorFactory(resource));
//        	}
//        }
//        EnumerationSupport.enumerate(context, enuRequest, enuResponse);
//		
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.framework.handlers.DelegatingHandler#pull(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.enumeration.Enumeration, com.sun.ws.management.enumeration.Enumeration)
	 */
//	@Override
	public void pull(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse) {
		// TODO Auto-generated method stub
//		super.pull(context, enuRequest, enuResponse);
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.framework.handlers.DelegatingHandler#release(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.enumeration.Enumeration, com.sun.ws.management.enumeration.Enumeration)
	 */
//	@Override
	public void release(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse) {
		// TODO Auto-generated method stub
//		super.release(context, enuRequest, enuResponse);
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.framework.handlers.DelegatingHandler#create(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.Management, com.sun.ws.management.Management)
	 */
//	@Override
	public void create(HandlerContext context, Management request, Management response) {
		// TODO Auto-generated method stub
//		super.create(context, request, response);
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.framework.handlers.DelegatingHandler#delete(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.Management, com.sun.ws.management.Management)
	 */
//	@Override
	public void delete(HandlerContext context, Management request, Management response) {
		// TODO Auto-generated method stub
//		super.delete(context, request, response);
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
	
	 /** Implements the IteratorFactory to generate iterators for 
	  *  subscriptions.
	  */
	public class EventingSubscriptionManagerIterator implements IteratorFactory{

		public final String RESOURCE_URI;
		protected EventingSubscriptionManagerIterator(String resource) {
			RESOURCE_URI = resource;
		}
		
		public EnumerationIterator newIterator(HandlerContext context, Enumeration request, 
				DocumentBuilder db, boolean includeItem, 
				boolean includeEPR) 
		throws UnsupportedFeatureFault, FaultException {
			return new EventSourcesIterator(context, RESOURCE_URI, 
					request, 
					db, includeItem, includeEPR);
		}
		
		/**Responsible for initializing the iterator and the snapshot
		 * of the underlying Enumeration that is being passed in.
		 * 
		 * @author Simeon
		 */
		public class EventSourcesIterator implements EnumerationIterator {
			private Management[] allEventSources;
			private final DocumentBuilder db;
			private final boolean includeEPR;
			private final String requestPath;
			private final String resourceURI;
			int iterCount = 0;
			
			public EventSourcesIterator(final HandlerContext hcontext,
					final String resource,
					final Enumeration request, final DocumentBuilder db,
					final boolean includeItem, final boolean includeEPR) {
				
				this.requestPath = hcontext.getURL();
				this.resourceURI = resource;
				this.db = db;
				this.includeEPR = includeEPR;
				
				try{
				  String xpathFilterString = "";
				  EnumerationExtensions enx = new EnumerationExtensions(request);
				  
				  //Process if a filter expression has been passed in.
				  if((enx!=null)&&(enx.getWsmanFilter()!=null)){
					 DialectableMixedDataType type = enx.getWsmanFilter();
					 if(type.getContent()!=null){
						 //convert any list to xpathFilter expression
						 String filterValue ="";
						 for(Object filterArg : type.getContent()){
							filterValue+= filterArg; 
						 }
						xpathFilterString = filterValue;
					 }
				  }
				  
				  //Initialize the list of eventSources for the enumeration.
				  allEventSources = locateEventSourceList(xpathFilterString);
				}catch (Exception ex){
				  LOG.severe("There was an error retrieving Metatadata for EventSources : "+ex.getMessage());	
				  allEventSources = new Management[0];	
				}
			}

			private Management[] locateEventSourceList(String xpathFilterString) throws SOAPException, 
				IOException, JAXBException, DatatypeConfigurationException {
				
				Management[] eventSrces = null;
				
//		        //Request identify info to get MetaData root information
//		        final Identify identify = new Identify();
//		        identify.setIdentify();
//		        
//		        System.out.println("@@@@@@@@@@@@@@@@@reqURI:"+requestPath);
//		        
//		        //Send identify request
////		        final Addressing response = HttpClient.sendRequest(identify.getMessage(), DESTINATION);
//		        final Addressing response = 
//		        	HttpClient.sendRequest(identify.getMessage(), requestPath);
////		        response.prettyPrint(logfile);
////		        if (response.getBody().hasFault()) {
////		            fail(response.getBody().getFault().getFaultString());
////		        }
//		        
//		        //Parse the identify response
//		        final Identify id = new Identify(response);
//		        final SOAPElement idr = id.getIdentifyResponse();
////		        assertNotNull(idr);
//		        SOAPElement el =IdentifyUtility.locateElement(id, 
//		        		AnnotationProcessor.META_DATA_RESOURCE_URI); 
////		         assertNotNull("MetaDatResourceURI is null.",el);
//		         //retrieve the MetaData ResourceURI
//		         String resUri=el.getTextContent();
////		          assertNotNull("Retrieved resourceURI is null.",resUri);
//		        el =IdentifyUtility.locateElement(id, 
//		        		AnnotationProcessor.META_DATA_TO);
////		        assertNotNull("MetaDataTo is null",el);
//		        //retrieve the MetaData To/Destination
//		        String metTo=el.getTextContent();
////		        assertNotNull("Retrieved destination is null.",metTo);
//
//		        //############ REQUEST THE LIST OF METADATA AVAILABLE ######################
//			   //Build the GET request to be submitted for the metadata
//		        Management m = null; 
//		        m =TransferUtility.createMessage(metTo, resUri,
//		        		Transfer.GET_ACTION_URI, null, null, 30000, null);
//		        
//		         //############ PROCESS THE METADATA RESPONSE ######################
//		         //Parse the getResponse for the MetaData
//		         final Addressing getResponse = HttpClient.sendRequest(m);
//		       Management mResp = new Management(getResponse);
////		System.out.println("Request MEtaDataResp:"+mResp.toString());       
////		        assertNull("A fault was detected.",mResp.getFault());
//		               
//		   		//Retrieve the MetaData response to build JAXB type
////		   		SOAPBody body = mResp.getBody();
//
//		   		//Normal processing to create/retrieve the Metadata object
////		   		Node metaDataNode = body.getFirstChild();
////				try {
//					//unmarshall the Metadata node content
////					Object bound = binding.unmarshal(metaDataNode);
////					Object bound = m.getXmlBinding().unmarshal(metaDataNode);
//					
////					Metadata ob = (Metadata)bound;
//					
//					//Parse the MetadataSections that exist
////					List<MetadataSection> metaDataSections = ob.getMetadataSection();
////					 assertEquals("The correct number of metadata sections were not found.",
////							3, metaDataSections.size());
//
//		        //############ PROCESS A METADATASECTION ###################### 
//					//Examine Metadatasection attributes 
////					MetadataSection section = metaDataSections.get(0);
////					assertEquals("Dialect does not match.",
////							AnnotationProcessor.NS_URI, 
////							section.getDialect());
////					assertEquals("Identifier does not match.",
////							AnnotationProcessor.NS_URI, 
////							section.getIdentifier());
//
//			    //########### TRANSLATE METADATA TO FAMILIAR MANAGEMENT NODES ##### 
//			        //Extract the MetaData node returned as Management instances
//			        Management[] metaDataList = 
//			        	ManagementUtility.extractEmbeddedMetaDataElements(mResp);
				
				Management[] metaDataList = 
//					ManagementUtility.getExposedMetadata(this.requestPath,0);
					MetadataUtility.getExposedMetadata(this.requestPath,0);
//System.out.println("############ALL EXPOSED METADATA CNT: "+metaDataList.length);				
			        ArrayList<Management> evtSrcBag = new ArrayList<Management>();
			        for (int i = 0; i < metaDataList.length; i++) {
						Management metaDescription = metaDataList[i];
						SOAPElement element = 
							ManagementUtility.locateHeader(metaDescription.getHeaders(),
								AnnotationProcessor.META_DATA_CATEGORY);
//System.out.println("!!!!!!!!!!!!!!!!!!!!MetaDataCategory:"+element.getTextContent());						
						if(element.getTextContent().trim().equals(
								CreationTypes.SUBSCRIPTION_SOURCE.name())){
//TODO: HERE, insert filter processing.
//System.out.println("################XpathFiltString:"+xpathFilterString);
							if((xpathFilterString!=null)&&
								(xpathFilterString.trim().length()>0)){
							  //TODO: implement XPath filtering.
//System.out.println("################XpathFiltString:"+xpathFilterString);
								
//								XPath xpath = XPathFactory.newInstance().newXPath();
//								NamespaceContext nsContext = new NameSpacer();
//								xpath.setNamespaceContext(nsContext);
								
//							  ResourceState filtered = 
//								  new ResourceStateImpl(
//									metaDescription.getEnvelope().getOwnerDocument());
//System.out.println("@@@@@@@@@@ NODE:"+xmlToString(metaDescription.getEnvelope().getOwnerDocument()));
							  	try {
								  Object nodes = xpath.evaluate(xpathFilterString, 
									metaDescription.getEnvelope().getOwnerDocument(), 
//									XPathConstants.NODESET);
									XPathConstants.BOOLEAN);
//									XPathConstants.STRING);
//System.out.println("!!!!!!!!!!NODES REF"+i+":"+nodes);								  
								  if(nodes!=null){
//										throw new NoMatchFoundException(
//												"No Element could be found to match"+
//												" your XPath expression.");
									  
//								   NodeList nodelist = (NodeList)nodes;		
//								   if((nodelist!=null)&(nodelist.getLength()>0)){
//									 evtSrcBag.add(metaDescription); 
//								   }
								  Boolean located = (Boolean) nodes;
								  if(located.booleanValue()){
									evtSrcBag.add(metaDescription); 
								  }else{
//System.out.println("%%%%%%%%% NO XPath boolean match could be found with:"+xpathFilterString);								
								  }
								 }
								} catch (XPathExpressionException e) {
//System.out.println("!!!!!!!!!!!XPATH Exp exc:"+e.getMessage()+":"+e.getCause());									
//									e.printStackTrace();
								} 
//								catch (NoMatchFoundException e) {
////									e.printStackTrace();
//								}
							}else{
							  evtSrcBag.add(metaDescription);
							}
						}
					}
//System.out.println("SUBSCRIPTION_SRC_CNT:"+evtSrcBag.size());
			        eventSrces = new Management[evtSrcBag.size()];
			        if(evtSrcBag.size()>0){
			        	System.arraycopy(evtSrcBag.toArray(), 0, eventSrces, 0, 
			        	  evtSrcBag.size());
			        }
//				}catch(Exception ex){
//					
//				}
//System.out.println("EventSrcsList:"+eventSrces.length);				
				return eventSrces;
			}

			public EnumerationItem next() {

				// construct an item if necessary for the enumeration
				Element item = null;

				// Always include the item to allow filtering by EnumerationSupport
				final Document doc = db.newDocument();
//				item = doc.createElementNS(properties_Handler.NS_URI, properties_Handler.NS_PREFIX + ":" + key);
				item = doc.createElementNS(Management.NS_URI, Management.NS_PREFIX);
//				item.setTextContent(value.toString());
//				item.setTextContent(allEventSources[iterCount++].toString());
				Management node =allEventSources[iterCount++];

				// construct an endpoint reference to accompany the element, if
				// needed
				EndpointReferenceType epr = null;
//				if (includeEPR) {
//					final Map<String, String> selectors = new HashMap<String, String>();
//					selectors.put(PROPERTY_SELECTOR_KEY, key.toString());
//					epr = EnumerationSupport.createEndpointReference(requestPath,
//							resourceURI, selectors);
//				}
//				return new EnumerationItem(item, epr);
				return new EnumerationItem(node.getEnvelope(), epr);
			}

			public boolean hasNext() {
				return (iterCount<allEventSources.length);
			}

			public boolean isFiltered() {
				return true;
			}

			public void release() {
				allEventSources = new Management[0];
			}

			public int estimateTotalItems() {
				return allEventSources.length;
			}
		}
	}
	
}
