package com.sun.ws.management.server.handler.wsman.auth;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import management.MetadataTest;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
//import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingUtility;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.eventing.EventingMessageValues.CreationTypes;
import com.sun.ws.management.framework.eventing.EventSourceInterface;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementEnumerationAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementOperationDefinitionAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementQNamedNodeWithValueAnnotation;
import com.sun.ws.management.mex.MetadataUtility;
import com.sun.ws.management.server.EventingSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.server.handler.wsman.eventsubman_Handler;
//import com.sun.ws.management.server.handler.wsman.eventsubman_Handler;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.xml.ws.addressing.model.ActionNotSupportedException;

@WsManagementDefaultAddressingModelAnnotation(
	getDefaultAddressDefinition=
		@WsManagementAddressDetailsAnnotation(
			wsaTo=eventcreator_Handler.TO, 
			wsmanResourceURI=eventcreator_Handler.RESOURCE_URI
	),
	 metaDataCategory = eventcreator_Handler.CATEGORY, 
	 metaDataDescription = "This is a test event source", 
	 resourceMetaDataUID = eventcreator_Handler.UID
)
public class eventcreator_Handler implements Handler, EventSourceInterface {
	
	public final static String TO =ManagementMessageValues.WSMAN_DESTINATION;
	public final static String RESOURCE_URI ="wsman:auth/eventcreator";
	public final static String CATEGORY =EventingMessageValues.SUBSCRIPTION_SOURCE;
	public final static String UID =
		"http://wiseman.dev.java.net/EventSource/eventcreator/uid-20000747652";
	
	//This should be NULL to start but is set upon successful initialization.
	private static String subscription_source_id =null;
	private static final Logger LOG = Logger.getLogger(eventcreator_Handler.class.getName());
	private static NamespaceMap nsMap;
	private static XmlBinding binding = null;
	static{
		if(binding==null){
		 try {
			Eventing events = new Eventing();
			binding = events.getXmlBinding();
		 } catch (SOAPException e) {
			e.printStackTrace();
		 }
		}
	}
	
	public eventcreator_Handler() throws SOAPException, JAXBException, 
		DatatypeConfigurationException, IOException{
		//register this handler in the metadata directory
//		Eventing registerMessage = EventingUtility.buildMessage(null, null);
//System.out.println("############In eventCreator constructor");		
//		if(subscription_source_id==null){
//System.out.println("############In eventCreator constructor: Before register");			
//		  String regEvtSrcId=
//			  EventingUtility.registerEventSourceWithSubscriptionManager(this,true,true);
//		  if((regEvtSrcId!=null)&&(regEvtSrcId.trim().length()>0)){
//			 subscription_source_id = regEvtSrcId; 
//		  }
//		}

//        if (nsMap == null) {
////            final Map<String, String> map = new HashMap<String, String>();
////            map.put(NS_PREFIX, NS_URI);
////            nsMap = new NamespaceMap(map);
//        }
		
	}
	
	private Management subscriptionManager;
	public void handle(String action, String resource, HandlerContext context,
			Management request, Management response) throws Exception {
		if(action.equals(Eventing.SUBSCRIBE_ACTION_URI)){
			//pipe the request processing to 
			//  subscribe(context, request, response);
			response = subscribe(context, request, response);
		}
		else if(action.equals(Transfer.CREATE_ACTION_URI)){
			create(context, request, response);
		}
//		else if(action.equals(Transfer.INITIALIZE_ACTION_URI)){
		else if(action.equals(com.sun.ws.management.mex.Metadata.INITIALIZE_ACTION_URI)){
			//Lazy instantiation
//			if(subscription_source_id==null){
//System.out.println("############In eventCreator handle: TransferInitialize");			
//		  String regEvtSrcId=
//			  EventingUtility.registerEventSourceWithSubscriptionManager(this,true,true);
//			  if((regEvtSrcId!=null)&&(regEvtSrcId.trim().length()>0)){
//				 subscription_source_id = regEvtSrcId; 
//			  }
//		    }
		  initialize(context, request, response);	
		}else{
			throw new ActionNotSupportedException(action);
		}
	}

	/** Write this method so that it is an initialization call.  This should be 
	 * 
	 */
	public void create(HandlerContext context, Management request, Management response) {
		// TODO Auto-generated method stub
		
	}

	/**This method should be implemented to lazily instantiate.  This method should
	 * only be executed once.  This method is meant to be used for initialization
	 * tasks that:
	 * -could require SOAP communication with other handlers
	 * -could require expensive database initialization 
	 * 
	 * @param context
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws DatatypeConfigurationException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 */
	public void initialize(HandlerContext context, Management request, 
			Management response) throws SOAPException, JAXBException, 
			DatatypeConfigurationException, IOException {
		if(subscription_source_id==null){
			//Then must locate the subscription manager details
			//Send create request for EVENT_SOURCE to register with SUB_MAN
			//Extract the created id and initalize the sub_source reference
			String srcId = MetadataUtility.registerEventSourceWithSubscriptionManager(
					this, true, true);
			subscription_source_id = srcId;
		}
	}

//	public void subscribe(HandlerContext context, Management eventRequest, 
	public Management subscribe(HandlerContext context, Management eventRequest, 
			Management eventResponse) throws SOAPException, JAXBException, 
			DatatypeConfigurationException, IOException {

		//call the initialize method which lazily instantiates
		initialize(context, eventRequest, eventResponse);
		//process and build subscription response.
		Eventing evtResponse = new Eventing(eventResponse);
		Eventing evtRequest = new Eventing(eventRequest);
		evtResponse.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
		
		//register this new subscriber with the sub_man
//		String suggestedEventSinkId = null;

		//test that message processing components exist.
		if(evtRequest!=null){
		   if((evtRequest.getSubscribe()==null)||
		      (evtRequest.getSubscribe().getDelivery()==null)){
			//Return insufficient information
			String msg="Request did not have required [Subscribe] and/or [Delivery] information.";
			msg+="This message is invalid and cannot be processed.";
			 LOG.warning(msg);
			throw new InvalidMessageFault(msg);
		   }
		}else{//Throw IllegalArgument
		   String msg = "The Event request message cannot be null.";
		   throw new IllegalArgumentException(msg);	
		}
		
//		//insert the elements signalling subman how to process
//		//indicate that this is a NewSubscriber request
//		eventRequest.addHeaders(Management.createReferenceParametersType(
//				EventingMessageValues.EVENTING_CREATION_TYPES, 
//				EventingMessageValues.CreationTypes.NEW_SUBSCRIBER.name()));
////		//indicate which event source this is
////		eventRequest.addHeaders(Management.createReferenceParametersType(
////				EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID, 
////				e));
	
		
//	    //extract suggestedEventSinkId from the message	if one exist	
//	    DeliveryType delivery = evtRequest.getSubscribe().getDelivery();
//		EndpointReferenceType notifyTo = null;
//		for (final Object content : delivery.getContent()) {
//			final Class contentClass = content.getClass();
//			if (JAXBElement.class.equals(contentClass)) {
//				final JAXBElement<Object> element = (JAXBElement<Object>) content;
//				final QName name = element.getName();
//				final Object item = element.getValue();
//				if (item instanceof EndpointReferenceType) {
//					final EndpointReferenceType epr = (EndpointReferenceType) item;
//					if (Eventing.NOTIFY_TO.equals(name)) {
//						notifyTo = epr;
//					}
//				}
//			}
//		}
//		if(notifyTo!=null){
//		  EndpointReferenceType resCreatedElement = notifyTo;
//		    if((resCreatedElement!=null)&&
//		    	(resCreatedElement.getReferenceParameters()!=null)&&
//		    	(resCreatedElement.getReferenceParameters().getAny()!=null)){
//		      //Parse the contents for suggestedEventSinkId	
//		      List<Object> refContents = 
//		    	  resCreatedElement.getReferenceParameters().getAny();
//		      if((refContents!=null)&&(refContents.size()>0)){
//		    	  for(Object node: refContents){
//					Node sugEvtSinkNode = (Node) node;
//				suggestedEventSinkId = sugEvtSinkNode.getTextContent();
//		    	}
//		      }
//		    }
//		  }
		//pass the suggested event sink id and retrieve actual eventSinkId assigned
		//?? should instead return a management instance with all relevant data?
//		String evtSinkId = 
//			MetadataUtility.registerEventSinkWithSubscriptionManager(
//					suggestedEventSinkId,
//					this,eventRequest,
//					true, true);
		Management subManResponse = 
			MetadataUtility.registerEventSinkWithSubscriptionManager(
				this,eventRequest,
				true, true);
System.out.println("@@@@ actual subManResponse:"+subManResponse);		
		//TODO: put that eventSinkId in the subscribe response to subscriber
		EventingMessageValues settings = EventingMessageValues.newInstance();
		settings.setEventingMessageActionType(Eventing.SUBSCRIBE_RESPONSE_URI);
		//Translate the metadata from SubMan into EPR type
			Management subManDet = getMetadataForSubscriptionManager();
System.out.println("@@@ subManRep:"+subManDet);			
			EndpointReferenceType subManEpr = ManagementUtility.extractEprType(subManDet);
System.out.println("@@@ man->epr instance:"+subManEpr);			
			settings.setSubscriptionManagerEpr(subManEpr);
		Eventing eventResponseBase = 
			EventingUtility.buildMessage(
				evtResponse, settings);
System.out.println("@@@ EventingIntMedMess:"+eventResponseBase);		
		eventResponse = ManagementUtility.buildMessage(eventResponseBase, null);
//		eventResponse.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
//		-Enumeration.
//		evtResponse.getSubscribe().
		SubscribeResponse subScribeResp = evtResponse.getSubscribeResponse();
		subScribeResp.setSubscriptionManager(subManEpr);
//		evtResponse.
	  return eventResponse;
	}

	public boolean isAlsoTheSubscriptionManager() {
		return false;
	}

	public Management getMetadataForEventSource() throws SOAPException, JAXBException, 
	DatatypeConfigurationException, IOException {
		Management eventSourceInfo =null;
		eventSourceInfo = AnnotationProcessor.findAnnotatedResourceByUID(
				eventcreator_Handler.UID, 
				ManagementMessageValues.WSMAN_DESTINATION);
		if(eventSourceInfo==null){
			String msg="Unable to locate metadata for event source.";
			throw new IllegalArgumentException(msg);
		}
		
		return eventSourceInfo;
	}

	public Management getMetadataForSubscriptionManager() throws SOAPException, 
	  JAXBException, DatatypeConfigurationException, IOException {
		Management subscriptionManagerDet = null;
		
		if(subscriptionManager==null){
			subscriptionManagerDet = AnnotationProcessor.findAnnotatedResourceByUID(
					eventsubman_Handler.DEFAULT_SUBSCRIPTION_MANAGER_UID, 
					ManagementMessageValues.WSMAN_DESTINATION);
		}else{
			subscriptionManagerDet = subscriptionManager;
		}
		
		return subscriptionManagerDet;
	}

	final static private ArrayList<QName> wiseManFamiliarQnameList = new ArrayList<QName>();
	static {
		wiseManFamiliarQnameList.add(Management.RESOURCE_URI);
		wiseManFamiliarQnameList.add(Management.TO);
		wiseManFamiliarQnameList.add(AnnotationProcessor.ENUMERATION_ACCESS_RECIPE);
		wiseManFamiliarQnameList.add(AnnotationProcessor.ENUMERATION_FILTER_USAGE);
		wiseManFamiliarQnameList.add(AnnotationProcessor.META_DATA_CATEGORY);
		wiseManFamiliarQnameList.add(AnnotationProcessor.META_DATA_DESCRIPTION);
//		wiseManFamiliarQnameList.add(AnnotationProcessor.META_DATA_);
	 }
	
//	class MetaDataEnumerationAnnotation implements WsManagementEnumerationAnnotation{
//		public MetaDataEnumerationAnnotation(Management instance) throws SOAPException, 
//		DOMException, JAXBException {
//			if(instance!=null){
//			   SOAPElement[] headers = null;	
//			   if((headers = instance.getHeaders())!=null){
//				   SOAPElement elem = null;
//				   //Enum access recipe processing
//				   elem =ManagementUtility.locateHeader(headers, 
//						   AnnotationProcessor.ENUMERATION_ACCESS_RECIPE);
//				   if((elem!=null)&&(elem.getTextContent()!=null)){
//					   setResourceEnumerationAccessRecipe(elem.getTextContent());
//				   }
//				   //Enum filter processing
//				   elem =ManagementUtility.locateHeader(headers, 
//						   AnnotationProcessor.ENUMERATION_FILTER_USAGE);
//				   if((elem!=null)&&(elem.getTextContent()!=null)){
//					   setResourceFilterUsageDescription(elem.getTextContent());
//				   }
//				   //Default Address model processing
//				   WsManagementDefaultAddressingModelAnnotation defMod = null;
//				   defMod =new MetaDataDefaultAddressModelAnnotation(instance);
//				   if(defMod!=null){
//					   setDefaultAddressModelDefinition(defMod);
//				   }
//			   }
//			}
//		}
//		
//		WsManagementDefaultAddressingModelAnnotation defAdd = null;
//		public WsManagementDefaultAddressingModelAnnotation getDefaultAddressModelDefinition() {
//			return this.defAdd;
//		}
//		private void setDefaultAddressModelDefinition(
//				WsManagementDefaultAddressingModelAnnotation addMod) {
//			this.defAdd = addMod;
//		}
//	
//		private String enumRecipe;
//		public String resourceEnumerationAccessRecipe() {
//			return this.enumRecipe;
//		}
//		private void setResourceEnumerationAccessRecipe(String accessRecipe) {
//			this.enumRecipe= accessRecipe;
//		}
//		
//		private String filterUsage;
//		public String resourceFilterUsageDescription() {
//			return null;
//		}
//		private void setResourceFilterUsageDescription(String filterUsage) {
//			this.filterUsage = filterUsage;
//		}
//	
//		public Class<? extends Annotation> annotationType() {
//			return null;
//		}
//		
//	}
//	class MetaDataDefaultAddressModelAnnotation implements
//	    WsManagementDefaultAddressingModelAnnotation{
//
//		MetaDataDefaultAddressModelAnnotation(Management instance) throws DOMException, 
//		SOAPException, JAXBException{
//			if(instance!=null){
//			   SOAPElement[] headers = null;	
//			   if((headers = instance.getHeaders())!=null){
//				   SOAPElement elem = null;
//				   //Metadata category processing
//				   elem =ManagementUtility.locateHeader(headers, 
//						   AnnotationProcessor.META_DATA_CATEGORY);
//				   if((elem!=null)&&(elem.getTextContent()!=null)){
//					   setMetaDataCategory(elem.getTextContent());
//				   }
//				   //Metadata UID processing
//				   elem =ManagementUtility.locateHeader(headers, 
//						   AnnotationProcessor.RESOURCE_META_DATA_UID);
//				   if((elem!=null)&&(elem.getTextContent()!=null)){
//					   setResourceMetaDataUid(elem.getTextContent());
//				   }
//				   //Metadata MiscInfo processing
//				   elem =ManagementUtility.locateHeader(headers, 
//						   AnnotationProcessor.RESOURCE_MISC_INFO);
//				   if((elem!=null)&&(elem.getTextContent()!=null)){
//					   setResourceMiscellaneousInformation(elem.getTextContent());
//				   }
//				   //Metadata description processing
//				   elem =ManagementUtility.locateHeader(headers, 
//						   AnnotationProcessor.META_DATA_DESCRIPTION);
//				   if((elem!=null)&&(elem.getTextContent()!=null)){
//					   setMetaDataDescription(elem.getTextContent());
//				   }
////				   //Default Address model processing
////				   WsManagementAddressDetailsAnnotation defAdd = null;
////				   defAdd =new MetaDataAddressDetailsAnnotation(instance);
////				   if(defAdd!=null){
////					   setDefaultAddressDefinition(defAdd);
////				   }
//				   
//			   }
//			}
//		}
//		
//		private WsManagementAddressDetailsAnnotation defAddressDefinition;
//		private void setDefaultAddressDefinition(
//				WsManagementAddressDetailsAnnotation defaultAddressDefinition){
//			this.defAddressDefinition = defaultAddressDefinition;
//		}
//		public WsManagementAddressDetailsAnnotation getDefaultAddressDefinition() {
//			return this.defAddressDefinition;
//		}
//
//		private String metaCategory;
//		private void setMetaDataCategory(String metaCategory){
//			this.metaCategory =metaCategory;
//		}
//		public String metaDataCategory() {
//			return this.metaCategory;
//		}
//
//		private String description;
//		private void setMetaDataDescription(String description){
//			this.description = description;
//		}
//		public String metaDataDescription() {
//			return this.description;
//		}
//
//		private String uid;
//		private void setResourceMetaDataUid(String uid){
//			this.uid= uid;
//		}
//		public String resourceMetaDataUID() {
//			return this.uid;
//		}
//
//		private String miscellaneousInfo;
//		private void setResourceMiscellaneousInformation(String miscellaneousInfo){
//			this.miscellaneousInfo = miscellaneousInfo;
//		}
//		public String resourceMiscellaneousInformation() {
//			return this.miscellaneousInfo;
//		}
//
//		public Class<? extends Annotation> annotationType() {
//			return WsManagementDefaultAddressingModelAnnotation.class;
//		}
//		public WsManagementOperationDefinitionAnnotation[] definedOperations() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		public String[] schemaList() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//	}
	
//	class MetaDataAddressDetailsAnnotation implements 
//	WsManagementAddressDetailsAnnotation{
//		
//	    MetaDataAddressDetailsAnnotation(Management instance) throws SOAPException, 
//				JAXBException {
//			if(instance!=null){
//			   SOAPElement[] headers = null;	
//			   if((headers = instance.getHeaders())!=null){
//				   SOAPElement elem = null;
//				   //resourceUri processing
//				   if((instance.getResourceURI()!=null)&&
//						   (instance.getResourceURI().trim().length()>0)){
//					   setWsmanResourceUri(instance.getResourceURI());
//				   }
//				   //to/Destination processing
//				   if((instance.getTo()!=null)&&
//						(instance.getTo().trim().length()>0)){
//					   setWsaTo(instance.getTo());
//				   }
//				   //SelectorSet processing
//				   Set<SelectorType> retSelectors = instance.getSelectors();
//				   if(retSelectors!=null){
//					   Map<String, String> selMap = 
//						   ManagementUtility.extractSelectorsAsMap(null, 
//								   (List)new ArrayList<SelectorType>(retSelectors));
//					   String[] selectors = null;
//					   ArrayList<String> bag = new ArrayList<String>();
//					   for (Iterator iter = selMap.keySet().iterator(); iter
//					   .hasNext();) {
//						   String key = (String) iter.next();
//						   String value = selMap.get(key);
//						   bag.add(key+"="+value);
//					   }
////					   selectors = new String[bag.size()]; int index = 0;
////					   for (Iterator iter = bag.iterator(); iter.hasNext();) {
////						String element = (String) iter.next();
////						selectors[index++] = element;
////					   }
//					   selectors = bag.toArray(selectors);
//					   setWsmanSelectorSetContents(selectors);
//				   }
//				   //ReferenceProperties/Parameter processing
//				   QName[] customList = locateCustomQNames(instance.getHeaders());
//				   SOAPElement locatedElement;
//				   WsManagementQNamedNodeWithValueAnnotation[] refValues = null;
//				   ArrayList<WsManagementQNamedNodeWithValueAnnotation> nodes 
//				     = new ArrayList<WsManagementQNamedNodeWithValueAnnotation>();
//				   for (int i = 0; i < customList.length; i++) {
//					   QName element = customList[i];
//						locatedElement = ManagementUtility.locateHeader(instance.getHeaders(),
//								element);
//					   WsManagementQNamedNodeWithValueAnnotation namedNode = 
//						   new MetaDataQNamedAnnotations(locatedElement);
//					   nodes.add(namedNode);
//				   }
//				   if(nodes.size()>0){
//					   refValues= new 
//					   WsManagementQNamedNodeWithValueAnnotation[nodes.size()];
//					   refValues =nodes.toArray(refValues);
//				   }
//				   if(refValues!=null){
//					   setReferenceParameterContents(refValues);
//				   }
//				   
////					//Test Custom ReferenceParameter Types
////					QName custQName = new QName(custQnsuri,custQlocPart,custQprefix);
////					locatedElement = ManagementUtility.locateHeader(instance.getHeaders(),
////							custQName);				   
////				   Set<SelectorType> retSelectors = instance.getEndpointReference(parent, qname);
////				   if(retSelectors!=null){
////					 Map<String, String> selMap = 
////						 ManagementUtility.extractSelectorsAsMap(null, 
////							  (List)new ArrayList<SelectorType>(retSelectors));
//////					   WsManagementQNamedNodeWithValueAnnotation[]
////					   for (Iterator iter = selMap.keySet().iterator(); iter
////							.hasNext();) {
////						String key = (String) iter.next();
////						String value = selMap.get(key);
////						
////					   }
////					   setReferenceParameterContents(refParams);
////				   }
//////				   if((elem!=null)&&(elem.getTextContent()!=null)){
//////					   setReferenceParameterContents(refParams);
//////				   }
////				   //Metadata MiscInfo processing
////				   elem =ManagementUtility.locateHeader(headers, 
////						   AnnotationProcessor.RESOURCE_MISC_INFO);
////				   if((elem!=null)&&(elem.getTextContent()!=null)){
////					   setResourceMiscellaneousInformation(elem.getTextContent());
////				   }
////				   //Metadata description processing
////				   elem =ManagementUtility.locateHeader(headers, 
////						   AnnotationProcessor.META_DATA_DESCRIPTION);
////				   if((elem!=null)&&(elem.getTextContent()!=null)){
////					   setMetaDataDescription(elem.getTextContent());
////				   }
////				   //Default Address model processing
////				   WsManagementAddressDetailsAnnotation defAdd = null;
////				   defAdd =new MetaDataAddressDetailsAnnotation(instance);
////				   if(defAdd!=null){
////					   setDefaultAddressDefinition(defAdd);
////				   }
//				   
//			   }
//			}
//
//		}
//		private QName[] locateCustomQNames(SOAPElement[] headers) {
//			QName[] unrecognizedQNames = null;
//			ArrayList<QName> unrecognized = new ArrayList<QName>();
//			if(headers!=null){
//				for (int i = 0; i < headers.length; i++) {
//					SOAPElement element = headers[i];
//					if(!wiseManFamiliarQnameList.contains(element.getElementQName())){
//						unrecognized.add(element.getElementQName());
//					}
//				}
//				if(unrecognized.size()>0){
//				  unrecognizedQNames = new QName[unrecognized.size()];
//				  unrecognizedQNames = unrecognized.toArray(unrecognizedQNames);
//				}
//			}
//			return unrecognizedQNames;
//		}
//		private WsManagementQNamedNodeWithValueAnnotation[] refParameters;
//		private void setReferenceParameterContents(
//				WsManagementQNamedNodeWithValueAnnotation[] refParams){
//			this.refParameters = refParams;
//		}
//		public WsManagementQNamedNodeWithValueAnnotation[] referenceParametersContents() {
//			return this.refParameters;
//		}
//
//		private WsManagementQNamedNodeWithValueAnnotation[] refProps;
//		private void setReferencePropertiesContents(
//				WsManagementQNamedNodeWithValueAnnotation[] refProps){
//			this.refProps = refProps;
//		}
//		public WsManagementQNamedNodeWithValueAnnotation[] referencePropertiesContents() {
//			return this.refProps;
//		}
//
//		private String wsaTo;
//		private void setWsaTo(String to){
//			this.wsaTo = to;
//		}
//		public String wsaTo() {
//			return this.wsaTo;
//		}
//
//		private String resourceUri;
//		private void setWsmanResourceUri(String resourceUri){
//			this.resourceUri = resourceUri;
//		}
//		public String wsmanResourceURI() {
//			return this.resourceUri;
//		}
//
//		private String[] selectors;
//		private void setWsmanSelectorSetContents(String[] selectors){
//			this.selectors = selectors;
//		}
//		public String[] wsmanSelectorSetContents() {
//			return this.selectors;
//		}
//
//		public Class<? extends Annotation> annotationType() {
//			return WsManagementAddressDetailsAnnotation.class;
//		}
//		
//	}
//	class MetaDataQNamedAnnotations implements 
//		WsManagementQNamedNodeWithValueAnnotation{
//
//		private String localpart;
//		public MetaDataQNamedAnnotations(SOAPElement locatedElement) {
//			QName identifier = null;
//			String nodeValue = null;
//			if((locatedElement!=null)&&
//					((identifier = locatedElement.getElementQName())!=null)){
//				String value;
//				if(((value = identifier.getLocalPart())!=null)&&
//						(value.trim().length()>0)){
//					setLocalpart(value.trim());
//				}
//				if(((value = identifier.getNamespaceURI())!=null)&&
//						(value.trim().length()>0)){
//					setNamespaceUri(value.trim());
//				}
//				if(((value = identifier.getPrefix())!=null)&&
//						(value.trim().length()>0)){
//					setPrefix(value.trim());
//				}
//				if(((nodeValue = locatedElement.getTextContent())!=null)&&
//						(nodeValue.trim().length()>0)){
//					setNodevalue(nodeValue.trim());
//				}
//			}
//		}
//		public String localpart() {
//			return this.localpart;
//		}
//		private void setLocalpart(String localPart){
//			this.localpart = localPart;
//		}
//
//		private String namespace;
//		public String namespaceURI() {
//			return this.namespace;
//		}
//		private void setNamespaceUri(String namespace){
//			this.namespace=namespace;
//		}
//		
//		private String nodeValue;
//		private void setNodevalue(String nodeValue){
//			this.nodeValue = nodeValue;
//		}
//		public String nodeValue() {
//			return this.nodeValue;
//		}
//
//		private String prefix;
//		private void setPrefix(String prefix){
//			this.prefix = prefix;
//		}
//		public String prefix() {
//			return this.prefix;
//		}
//
////		public Class<? extends Annotation> annotationType() {
//		public Class<? extends Annotation> annotationType() {
//			return WsManagementQNamedNodeWithValueAnnotation.class;
//		}
//		
//	}
	
	public void setRemoteSubscriptionManager(Management subscriptionManagerMetaData) {
		this.subscriptionManager = subscriptionManagerMetaData;
	}

}
