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
 **Revision 1.4  2007/05/30 20:30:27  nbeers
 **Add HP copyright header
 **
 **
 *
 * $Id: eventsubman_Handler.java,v 1.6 2007-06-19 12:31:14 simeonpinder Exp $
 */
package com.sun.ws.management.server.handler.wsman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.eventing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingMessageValues.CreationTypes;
import com.sun.ws.management.framework.eventing.EventSourceInterface;
import com.sun.ws.management.framework.eventing.SubscriptionManagerInterface;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementEnumerationAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementOperationDefinitionAnnotation;
import com.sun.ws.management.mex.MetadataUtility;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.IteratorFactory;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.xml.XmlBinding;

@WsManagementEnumerationAnnotation(
	getDefaultAddressModelDefinition=
		@WsManagementDefaultAddressingModelAnnotation(
			getDefaultAddressDefinition=
				@WsManagementAddressDetailsAnnotation(
					wsaTo=eventsubman_Handler.DESTINATION,
					wsmanResourceURI=eventsubman_Handler.RESOURCE_URI
				),
		resourceMetaDataUID = eventsubman_Handler.DEFAULT_SUBSCRIPTION_MANAGER_UID,
		schemaList={
				eventsubman_Handler.schema1
			},
			definedOperations={
				@WsManagementOperationDefinitionAnnotation(
					operationInputTypeMap = 
						"empty:EMPTY-BODY=http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate", 
					operationName = "Enumeration", 
					operationOutputTypeMap = 
						"xs:any=http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerateResponse"
				)
			}
		),
	resourceEnumerationAccessRecipe =
		"Enumerate and Optimized Enumeration with no arguments returns all available Event Sources.",
	resourceFilterUsageDescription =
		 "Filtering via RESOURCE_META_DATA_UID. Ex. env:Envelope/env:Header/wsmeta:ResourceMetaDataUID/text()='"+
		 eventsubman_Handler.DEFAULT_SUBSCRIPTION_MANAGER_UID+"'"
)
public class eventsubman_Handler implements SubscriptionManagerInterface {

	public static final String DESTINATION = ManagementMessageValues.WSMAN_DESTINATION;
	public static final String RESOURCE_URI = "wsman:eventsubman";
	public static final String schema1 = eventsubman_Handler.DESTINATION+"schemas/wiseman/enumeration.xsd";
	public static final String DEFAULT_SUBSCRIPTION_MANAGER_UID ="https://wiseman.java.net/ri/SubscriptionManager";
	private static final Logger LOG =
		Logger.getLogger(eventsubman_Handler.class.getName());
	//Use HashMap to store event_src_key and EventSource instances
	protected static HashMap<String,EventSourceInterface> eventSources =
		new HashMap<String, EventSourceInterface>();
	protected static HashMap<String,Object> eventSinks =
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
		 //locate this Metadata
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}
	//Share the Xpath instance
	protected static XPath xpath = XPathFactory.newInstance().newXPath();
	static{
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
			response = create(context,request,response);
		}
	}

	public String getSubscriptionManagerAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSubscriptionManagerResourceURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsubsubscribe(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse) {
		// TODO Auto-generated method stub

	}

	public synchronized Management create(HandlerContext context, Management request, 
			Management response) throws SOAPException, 
			JAXBException, DatatypeConfigurationException {
		//locate the CreationSubType
		SOAPElement creationHeader =  ManagementUtility.locateHeader(request.getHeaders(),
				EventingMessageValues.EVENTING_CREATION_TYPES);
		String type =null;
		//Create section for SUBSCRIPTION_SOURCES
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
				xferResponse.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
				response = new Management(xferResponse);
			}
		}//End of Event source creation processing

		Subscribe subscribeContent = null;
		//Create portion for EVENT_SINK creation
		if((creationHeader!=null)&&((type=creationHeader.getTextContent())!=null)&&
				(CreationTypes.NEW_SUBSCRIBER.name().equals(type.trim()))){
			//attempt to extract the subscribe message sent if any
			Management createRequest = request;
			Document createBodyDoc =
				createRequest.getBody().extractContentAsDocument();
			if(createBodyDoc!=null){
				Object ob=	binding.unmarshal(createBodyDoc);
			  if(ob instanceof Subscribe){	
			    subscribeContent = (Subscribe) ob;
			  }else{
				 String msg="The expected Subscribe node content could not be found.";
				 throw new IllegalArgumentException(msg);
			  }
			}
			//locate the EventSinkId if sent.
			SOAPElement eventSinkId =  ManagementUtility.locateHeader(request.getHeaders(),
					EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID);
			String eventSinkUID = null;
			if(eventSinkId!=null){
				eventSinkUID = eventSinkId.getTextContent();
			  //Check that this id is not already used. If generate new otherwise use suggested.
			  if(eventSinks.containsKey(eventSinkUID)){
				  LOG.warning("An Event Sink with that name already exists. Generating a new one.");				  
				  eventSinkUID = EventingMessageValues.EVENT_SINK_NODE_NAME+
				    UUID.randomUUID();
			  }
			  //Now populate the ResourceCreated value for the response object
				HashMap<String, String> selectorMap = new HashMap<String,String>();
				selectorMap.put(EventingMessageValues.EVENT_SINK_NODE_NAME,
						eventSinkUID);
				
				//register this eventSink with it's associated EventSource here
				SOAPElement evtSrcMetId = ManagementUtility.locateHeader(request.getHeaders(), 
						Eventing.IDENTIFIER);
				if((evtSrcMetId!=null)&&(evtSrcMetId.getTextContent()!=null)){
					String evtSrcId =null;
					if((evtSrcId =evtSrcMetId.getTextContent().trim()).length()>0){
					  eventSinks.put(eventSinkUID, evtSrcId);
					  selectorMap.put(EventingMessageValues.EVENT_SOURCE_NODE_NAME,
						evtSrcId);
					}
				}
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
				 //generate the subscribeResponse object and return it as body of CreateResponse.
				 SubscribeResponse subscribeResponseBody =
					 env_factory.createSubscribeResponse();
				  //locate the details to define the SubscriptionManager EPR
				 if(subscriptionManMetaData==null){
				   subscriptionManMetaData = AnnotationProcessor.stripMetadataContent(request, true);
				 }
				 if(subscriptionManMetaData!=null){
				     //translate into subscription manager details
				     EndpointReferenceType subscriptionManEpr =
					   ManagementUtility.extractEprType(subscriptionManMetaData);
					//add the ref params passed in
					//extract epr from Subscribe/Mode/Notify
					///##########
					   //locate notify to
					    EndpointReferenceType notifyTo = null;
						for (final Object content : subscribeContent.getDelivery().getContent()) {
							final Class contentClass = content.getClass();
							if (JAXBElement.class.equals(contentClass)) {
								final JAXBElement<Object> element = (JAXBElement<Object>) content;
								final QName name = element.getName();
								final Object item = element.getValue();
								if (item instanceof EndpointReferenceType) {
									final EndpointReferenceType eprT = (EndpointReferenceType) item;
									if (Eventing.NOTIFY_TO.equals(name)) {
										notifyTo = eprT;
									}
								}
							}
						}
						if(notifyTo==null){
							String msg="The Subscribe.Delivery.NotifyTo element could not be located";
							msg+=" in your request.";
							throw new IllegalArgumentException(msg);
						}
						ReferenceParametersType refs = notifyTo.getReferenceParameters();
						//iterate through and copy all over
					   if((refs.getAny()!=null)&&(refs.getAny().size()>0)){
						   subscriptionManEpr.setReferenceParameters(refs);
					   }
                     EventingExtensions evext = new EventingExtensions(subscriptionManMetaData);
                     evext.setSubscribeResponse(subscriptionManEpr, requestedExpiration.toString());
				    	 response = new Management(evext);
				  }
		   }//End of event sink id !=null
		}//End of CREATE action for new SUBSCRIBER
		response.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	  return response;
	}//End of CREATE ACTION processing

	 /** Implements the IteratorFactory to generate iterators for
	  *  subscriptions.
	  */
	protected class EventingSubscriptionManagerIterator implements IteratorFactory{

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
				Management[] metaDataList =
					MetadataUtility.getExposedMetadata(this.requestPath,0);
			        ArrayList<Management> evtSrcBag = new ArrayList<Management>();
			        for (int i = 0; i < metaDataList.length; i++) {
						Management metaDescription = metaDataList[i];
						SOAPElement element =
							ManagementUtility.locateHeader(metaDescription.getHeaders(),
								AnnotationProcessor.META_DATA_CATEGORY);
						if(element.getTextContent().trim().equals(
								CreationTypes.SUBSCRIPTION_SOURCE.name())){
							if((xpathFilterString!=null)&&
								(xpathFilterString.trim().length()>0)){
							  	try {
								  Object nodes = xpath.evaluate(xpathFilterString,
									metaDescription.getEnvelope().getOwnerDocument(),
//									XPathConstants.NODESET);
									XPathConstants.BOOLEAN);
//									XPathConstants.STRING);
								  if(nodes!=null){
								  Boolean located = (Boolean) nodes;
								  if(located.booleanValue()){
									evtSrcBag.add(metaDescription);
								  }else{
								  }
								 }
								} catch (XPathExpressionException e) {
								}
							}else{
							  evtSrcBag.add(metaDescription);
							}
						}
					}
			        eventSrces = new Management[evtSrcBag.size()];
			        if(evtSrcBag.size()>0){
			        	System.arraycopy(evtSrcBag.toArray(), 0, eventSrces, 0,
			        	  evtSrcBag.size());
			        }
				return eventSrces;
			}

			public EnumerationItem next() {

				// construct an item if necessary for the enumeration
				Element item = null;

				// Always include the item to allow filtering by EnumerationSupport
				final Document doc = db.newDocument();
				item = doc.createElementNS(Management.NS_URI, Management.NS_PREFIX);
				Management node =allEventSources[iterCount++];

				// construct an endpoint reference to accompany the element, if
				// needed
				EndpointReferenceType epr = null;
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

	public Management unsubsubscribe(HandlerContext context, 
			Management request, Management response) {
		return response;
	}
}
