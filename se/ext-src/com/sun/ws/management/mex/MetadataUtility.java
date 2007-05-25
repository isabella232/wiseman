package com.sun.ws.management.mex;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.MetadataSection;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingMessageValues.CreationTypes;
import com.sun.ws.management.framework.eventing.EventSourceInterface;
import com.sun.ws.management.framework.eventing.SubscriptionManagerInterface;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.identify.IdentifyUtility;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
//import com.sun.ws.management.server.handler.wsman.auth.eventcreator_Handler;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferMessageValues;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.BasicAuthenticator;
import com.sun.ws.management.transport.HttpClient;
import com.sun.xml.fastinfoset.sax.Properties;

/** This class is meant to provide general utility functionality for
 *  Management instances and all of their related extensions.
 * 
 * @author Simeon
 */
public class MetadataUtility extends ManagementUtility {
	
	//These values are final and static so that they can be uniformly used by many classes  
	private static final Logger LOG = Logger.getLogger(MetadataUtility.class.getName());
	
	/** This method takes a GetResponse Management instance containing a 
	 *  a MetaDataExchange element.  An array of Management instances located
	 *  is returned in response.
	 * 
	 * @param metaDataGetResponse
	 * @return array of Management instances
	 */
	public static Management[] extractEmbeddedMetaDataElements(Management metaDataGetResponse){
		Management[] locatedMetaDataElements = null;
		ArrayList<Management> located = new ArrayList<Management>();
		
   		//Retrieve the MetaData response to build JAXB type
   		SOAPBody body = metaDataGetResponse.getBody();
   		
   		if((body!=null)&&(body.getFirstChild()!=null)){
	   	 //Normal processing to create/retrieve the Metadata object
	   	 Node metaDataNode = body.getFirstChild();
	   	 
			try {
			 //unmarshall the Metadata node content
//				Object bound = binding.unmarshal(metaDataNode);
			 Object bound = metaDataGetResponse.getXmlBinding().unmarshal(metaDataNode);
			 if((bound!=null) && (bound instanceof Metadata)){
				 Metadata ob = (Metadata)bound;
				
				//Parse the MetadataSections that exist
				List<MetadataSection> metaDataSections = 
					ob.getMetadataSection();
				
				if(metaDataSections!=null){
				 for (Iterator iter = metaDataSections.iterator(); iter.hasNext();) {
					MetadataSection element = (MetadataSection) iter.next();
					if((element.getDialect()!=null)&&
							(element.getDialect().equals(AnnotationProcessor.NS_URI))){
						Management instance = new Management();
						//Now parse the Dialect specif component.
						instance = AnnotationProcessor.populateMetadataInformation(element, 
								instance);
						located.add(instance);	
					}
				}//end of for loop.
			 }//end of if metaDataSections exist
		    }
   		   }catch (JAXBException e) {
   			  //log and eat the exception
   			LOG.log(Level.FINE, "JAXBException occurred:"+e.getMessage());
   		   } catch (SOAPException e) {
			  //log and eat the exception
  			LOG.log(Level.FINE, "SOAPException occurred:"+e.getMessage());
   		   }
		}
   		
   		//Now populate the return array.
   		locatedMetaDataElements = new Management[located.size()];
   		System.arraycopy(located.toArray(), 0, 
   				locatedMetaDataElements, 0, located.size());
   		
	   return locatedMetaDataElements;
	}
	
	public static Management[] getExposedMetadata(String wisemanServerAddress,long timeout) 
		throws SOAPException, IOException, JAXBException, DatatypeConfigurationException{
		long timeoutValue = 30000;
		if(timeout>timeoutValue){
			timeoutValue = timeout;
		}
		Management[] metaDataValues = null;
		loadServerAccessCredentials(null);
		
		//Make identify request to the Wiseman server
        final Identify identify = new Identify();
        identify.setIdentify();
        //Send identify request
        final Addressing response = 
        	HttpClient.sendRequest(identify.getMessage(), 
        			wisemanServerAddress);
        
        //Parse the identify response
        final Identify id = new Identify(response);
        final SOAPElement idr = id.getIdentifyResponse();
        SOAPElement el = IdentifyUtility.locateElement(id, 
        		AnnotationProcessor.META_DATA_RESOURCE_URI);

        //retrieve the MetaData ResourceURI
        String resUri=el.getTextContent();
        el = IdentifyUtility.locateElement(id, 
        		AnnotationProcessor.META_DATA_TO);

        //retrieve the MetaData To/Destination
        String metTo=el.getTextContent();
    	
     //exercise the Enumeration annotation mechanism
        //############ REQUEST THE LIST OF METADATA AVAILABLE ######################
 	   //Build the GET request to be submitted for the metadata
        Management m = TransferUtility.createMessage(metTo, resUri,
//        		Transfer.GET_ACTION_URI, null, null, 30000, null);
        		Transfer.GET_ACTION_URI, null, null, timeoutValue, null);
        
          //############ PROCESS THE METADATA RESPONSE ######################
          //Parse the getResponse for the MetaData
          final Addressing getResponse = HttpClient.sendRequest(m);
        Management mResp = new Management(getResponse);
        
		metaDataValues = 
			MetadataUtility.extractEmbeddedMetaDataElements(mResp); 
		
		return metaDataValues;
	}
	
	public static void loadServerAccessCredentials(Properties properties){
		String wsmanDest="wsman.dest";
		String wsmanUser="wsman.user";
		String wsmanPassword="wsman.password";
	    String wsmanBasicAuthenticationEnabled="wsman.basicauthentication";
//        <jvmarg value="-Dwsman.dest=http://localhost:8080/wsman/" />
//        <jvmarg value="-Dwsman.user=wsman" />
//        <jvmarg value="-Dwsman.password=secret" />
//        <jvmarg value="-Dwsman.basicauthentication=true" />
	    String key = null;
	    if((key=System.getProperty(wsmanDest))==null){
	    	System.setProperty(wsmanDest, "http://localhost:8080/wsman/");
	    }
	    if((key=System.getProperty(wsmanUser))==null){
	    	System.setProperty(wsmanUser, "wsman");
	    }
	    if((key=System.getProperty(wsmanPassword))==null){
	    	System.setProperty(wsmanPassword, "secret");
	    }
	    if((key=System.getProperty(wsmanBasicAuthenticationEnabled))==null){
	        System.setProperty(wsmanBasicAuthenticationEnabled, "true");
	    }

	    final String basicAuth = System.getProperty("wsman.basicauthentication");
        if ("true".equalsIgnoreCase(basicAuth)) {
        	HttpClient.setAuthenticator(new BasicAuthenticator());
//            HttpClient.setAuthenticator(new SimpleHttpAuthenticator());
        }
	    
	}

	//EVENTING METHODS
	public static String registerEventSourceWithSubscriptionManager(EventSourceInterface 
			eventSource,boolean logException,boolean throwException) throws 
	SOAPException, JAXBException, DatatypeConfigurationException, IOException{
		String eventSourceUid = null;
		
		if(eventSource==null){
			String msg = "The EventSourceInterface instance was NULL. This is not allowed.";
			if(logException){
				LOG.severe(msg);
			}
			if(throwException){
			  throw new IllegalArgumentException(msg);
			}
			return eventSourceUid;
		}
		
		//Retrieve the SubscriptionManager details
		SubscriptionManagerInterface eventSubManager = null;
		Management subManagerData = null;

		//??
		TransferMessageValues settings = TransferMessageValues.newInstance();
		Transfer tMessage = TransferUtility.buildMessage(null, settings);
		
		//locate the subscriptionManager instance
		if(!eventSource.isAlsoTheSubscriptionManager()){
			subManagerData = eventSource.getMetadataForSubscriptionManager();
			if(subManagerData==null){
				String msg="SubscriptionManager metadata is null. Unable to proceed.";
				if(logException){
					LOG.severe(msg);
				}
				if(throwException){
				  throw new IllegalArgumentException(msg);
				}
				return eventSourceUid;
			}

//			//locate EPR/Addressing details 
//			WsManagementDefaultAddressingModelAnnotation subManMetadata = null;
//			WsManagementAddressDetailsAnnotation smConnectionDetails = null;
//			subManMetadata = subManagerData.getDefaultAddressModelDefinition();
//			if(subManMetadata!=null){
//			  smConnectionDetails= subManMetadata.getDefaultAddressDefinition();
//			}

			//Parameter checking
//			if((subManMetadata==null)||(smConnectionDetails==null)){
//				String msg="The Addressing details for the SubscriptionManager metadata";
//				msg+=" is NULL. Unable to proceed.";
//				if(logException){
//					LOG.severe(msg);
//				}
//				if(throwException){
//				  throw new IllegalArgumentException(msg);
//				}
//				return eventSourceUid;
//			}
			
//			if(smConnectionDetails!=null){
			//Extract MetaData information and register this Event Source with the SubMan
			  //Build the message to CREATE a new SUBSCRIPTION_SOURCE in the SubscripMan
//	    	ManagementMessageValues manSettings = 
//	    		ManagementMessageValues.newInstance();
//	    	if((smConnectionDetails.wsaTo()!=null)&&
//	    		(smConnectionDetails.wsaTo().trim().length()>0)){
//	    		manSettings.setTo(smConnectionDetails.wsaTo().trim());
//	    	}
//	    	if((smConnectionDetails.wsmanResourceURI()!=null)&&
//	    		(smConnectionDetails.wsmanResourceURI().trim().length()>0)){
//	    		manSettings.setResourceUri(
//	    			smConnectionDetails.wsmanResourceURI().trim());
//	    	}
//	    	manSettings.setXmlBinding(tMessage.getXmlBinding());
//			manSettings.addCustomHeader(EventingMessageValues.EVENTING_CREATION_TYPES,
//					CreationTypes.SUBSCRIPTION_SOURCE.name());
			subManagerData.addHeaders(Management.createReferenceParametersType(
					EventingMessageValues.EVENTING_CREATION_TYPES, 
					CreationTypes.SUBSCRIPTION_SOURCE.name()));
//	    	manSettings.addCustomHeader(EventingMessageValues.EVENTING_CREATION_TYPES,
//	    			CreationTypes.SUBSCRIPTION_SOURCE.name());
	    	
	    	//Retrieve the Event Source details
//			WsManagementDefaultAddressingModelAnnotation evtSrcDetails = 
//				eventSource.getMetadataForEventSource();
//			if(evtSrcDetails==null){
//				String msg = "Metadata information for EventSource is NULL. This is not allowed.";
//				if(logException){
//					LOG.severe(msg);
//				}
//				if(throwException){
//				  throw new IllegalArgumentException(msg);
//				}
//				return eventSourceUid;
//			}
			Management evtSrcDetails = 
				eventSource.getMetadataForEventSource();
			
			//Locate the EventSource Metadata UID and add as custom header
//			String evtMetadata = evtSrcDetails.resourceMetaDataUID();
//			//TODO: insert null check.
//			manSettings.addCustomHeader(EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID,
//					evtMetadata);
			SOAPElement evtSrcMetDataId = ManagementUtility.locateHeader(
					evtSrcDetails.getHeaders(), 
					AnnotationProcessor.RESOURCE_META_DATA_UID);
			String evtMetadata = evtSrcMetDataId.getTextContent();
			subManagerData.addHeaders(Management.createReferenceParametersType(
				EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID, 
				evtMetadata));
			subManagerData.setAction(Transfer.CREATE_ACTION_URI);
			
			subManagerData = ManagementUtility.buildMessage(null, 
//					subManagerData, true);
					subManagerData, false);

//			//Locate the EventSource destination and add as custom header
//			String evtSinkLocation = evtSrcDetails.resourceMetaDataUID();
//			//TODO: insert null check.
//	    	manSettings.addCustomHeader(EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID,
//	    			evtMetadata);
//	    	manSettings.setTimeout(1000*60*2);
//	    	final Management mgmt = ManagementUtility.buildMessage(tMessage,manSettings);
//			System.out.println("#####REG_EVT_SRC request:"+mgmt.toString());
//System.out.println("#####REG_EVT_SRC request:"+subManagerData);
	    	
//final Addressing response = HttpClient.sendRequest(mgmt);
	        final Addressing response = HttpClient.sendRequest(subManagerData);
	        if (response.getBody().hasFault())
	        {
System.out.println("####REG_EVT_RESP:"+response.getBody().getFault().getFaultString());	        	
	//            fail(response.getBody().getFault().getFaultString());
	        }else{
	        	//extract the returned selectorSet
	        	Management eventSourceRegistrationResp = new Management(response);
	        	System.out.println("REG_EVT_SRC_RESP:"+eventSourceRegistrationResp);
//		        	ManagementUtility.extractSelectorsAsMap(null,eventSourceRegistrationResp.getSelectors());
	          Map<String,String> selectors=	ManagementUtility.extractSelectors(eventSourceRegistrationResp);
//	          if((eventSourceRegistrationResp.getSelectors()!=null)&&(eventSourceRegistrationResp.getSelectors().size()>0)){	
//	        	  List selList = (List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors());
	          if(selectors.size()>0){	
//	        	List selList = (List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors());
//				Map<String, String> selectorsRetrieved = ManagementUtility.extractSelectorsAsMap(null,
////						(List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors()));
//						selList);
//	        	if(selectorsRetrieved.containsKey(EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME)){
//	        		eventSourceUid = EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME;
//	        		eventSourceUid+="="+selectorsRetrieved.get(EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME);
//	        	}
	        	if(selectors.containsKey(EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME)){
	        		eventSourceUid = selectors.get(
	        			EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME);
	        	}
	          }
//System.out.println("@@@EventSrcID::"+eventSourceUid);	        	
	        }
//			}else{
//				
//			}
				
			return eventSourceUid;
		}else{//Is one and the same handler.
		
		}
		
//		WsManagementDefaultAddressingModelAnnotation eventSrcMetadata = 
//			eventSource.getMetadataForEventSource();
//		WsManagementAddressDetailsAnnotation connectionDetails = null;
//		if(eventSrcMetadata!=null){
//		  connectionDetails= eventSrcMetadata.getDefaultAddressDefinition();
//		}
//		
//		if(connectionDetails!=null){
//	    	ManagementMessageValues manSettings = 
//	    		ManagementMessageValues.newInstance();
//	    	if((connectionDetails.wsaTo()!=null)&&
//	    			(connectionDetails.wsaTo().trim().length()>0)){
//	    		manSettings.setTo(connectionDetails.wsaTo().trim());
//	    	}
//	    	if((connectionDetails.wsmanResourceURI()!=null)&&
//	    		(connectionDetails.wsmanResourceURI().trim().length()>0)){
//	    		manSettings.setResourceUri(connectionDetails.wsmanResourceURI().trim());
//	    	}
//	    	manSettings.setXmlBinding(tMessage.getXmlBinding());
//	    	
//	    	final Management mgmt = ManagementUtility.buildMessage(tMessage,manSettings);
//	    	
//	        final Addressing response = HttpClient.sendRequest(mgmt);
//	        if (response.getBody().hasFault())
//	        {
//	//            fail(response.getBody().getFault().getFaultString());
//	        }
//		}
		return eventSourceUid;
	}

//	public static String registerEventSinkWithSubscriptionManager(
//			String suggestionForEvtSinkId,
//			EventSourceInterface eventSource,Management message, boolean logException, 
//			boolean throwException) throws SOAPException, JAXBException, 
//			DatatypeConfigurationException, IOException {
//		String eventSinkId = null;
////######
//		Management subManagerData = null;
////			subManagerData = eventSource.getMetadataForSubscriptionManager();
//			subManagerData = eventSource.getMetadataForSubscriptionManager();
//		TransferMessageValues settings = TransferMessageValues.newInstance();
//		Transfer tMessage = TransferUtility.buildMessage(null, settings);
//		
//		//locate the subscriptionManager instance
//		if(!eventSource.isAlsoTheSubscriptionManager()){
//			
////			subManagerData = eventSource.getMetadataForSubscriptionManager();
//			subManagerData = eventSource.getMetadataForSubscriptionManager();
//			if(subManagerData==null){
//				String msg="SubscriptionManager metadata is null. Unable to proceed.";
//				if(logException){
//					LOG.severe(msg);
//				}
//				if(throwException){
//				  throw new IllegalArgumentException(msg);
//				}
//				return eventSinkId;
//			}
//
//			//Now add the additional components to this message so that subman can proceed
//			  //indicate for creating new subscriber
//			subManagerData.addHeaders(Management.createReferenceParametersType(
//					EventingMessageValues.EVENTING_CREATION_TYPES, 
////					CreationTypes.SUBSCRIPTION_SOURCE.name()));
//					CreationTypes.NEW_SUBSCRIBER.name()));
//			Management evtSrcDetails = 
//				eventSource.getMetadataForEventSource();
//			//indicate which EventSource is responsible
//			SOAPElement evtSrcMetDataId = ManagementUtility.locateHeader(
//					evtSrcDetails.getHeaders(), 
//					AnnotationProcessor.RESOURCE_META_DATA_UID);
//			String metadataId = null;
//			if((evtSrcMetDataId!=null)&&
//				((metadataId = evtSrcMetDataId.getTextContent())!=null)){
//				//do nothing already have the data
//			}
//			
//			if((metadataId!=null)&&
//					(metadataId.trim().length()>0)){
//			  String evtMetadata = metadataId;
//			  subManagerData.addHeaders(
//					  Management.createReferenceParametersType(
//				EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID, 
//				evtMetadata));
//			  subManagerData.setAction(Transfer.CREATE_ACTION_URI);
//			}
////			if((suggestionForEvtSinkId!=null)&&
////					(suggestionForEvtSinkId.trim().length()>0)){
////			  String evtMetadata = suggestionForEvtSinkId;
////			  subManagerData.addHeaders(
////					  Management.createReferenceParametersType(
////				EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID, 
////				evtMetadata));
////			  subManagerData.setAction(Transfer.CREATE_ACTION_URI);
////			}
//			subManagerData = ManagementUtility.buildMessage(null, 
//					subManagerData, true);
////					subManagerData, false);
//			
//			//insert the body of the message from the message passed in.
//			if((message!=null)&&(message.getBody()!=null)){
//			   subManagerData.getBody().addDocument(
//					   message.getBody().extractContentAsDocument());
//			}
////			subManagerData = 
////				ManagementUtility.removeDescriptiveMetadataHeaders(subManagerData);
//			
//			
//	    	
////final Addressing response = HttpClient.sendRequest(mgmt);
//	        final Addressing response = HttpClient.sendRequest(subManagerData);
//	        if (response.getBody().hasFault())
//	        {
//System.out.println("@@@@ MetDUtil:registerEv");	        	
//	//            fail(response.getBody().getFault().getFaultString());
//	        }else{
//	        	//extract the returned selectorSet
//	        	Management eventSinkRegistrationResp = new Management(response);
////		        	ManagementUtility.extractSelectorsAsMap(null,eventSourceRegistrationResp.getSelectors());
//	          Map<String,String> selectors=	
////	        	  TransferUtility.extractSelectors(eventSinkRegistrationResp);
//	        	  ManagementUtility.extractSelectors(eventSinkRegistrationResp);
////	          if((eventSourceRegistrationResp.getSelectors()!=null)&&(eventSourceRegistrationResp.getSelectors().size()>0)){	
////	        	  List selList = (List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors());
//	          if(selectors.size()>0){	
////	        	List selList = (List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors());
////				Map<String, String> selectorsRetrieved = ManagementUtility.extractSelectorsAsMap(null,
//////						(List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors()));
////						selList);
////	        	if(selectorsRetrieved.containsKey(EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME)){
////	        		eventSourceUid = EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME;
////	        		eventSourceUid+="="+selectorsRetrieved.get(EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME);
////	        	}
//	        	if(selectors.containsKey(EventingMessageValues.EVENT_SINK_NODE_NAME)){
//	        		eventSinkId = selectors.get(
//	        			EventingMessageValues.EVENT_SINK_NODE_NAME);
//	        	}
//	          }
//	        }
////			}else{
////				
////			}
//				
//			return eventSinkId;
//		}else{//Is one and the same handler.
//		
//		}
//		
////######		
//		return eventSinkId;
//	}

	public static Management registerEventSinkWithSubscriptionManager(
			EventSourceInterface eventSource,Management message, boolean logException, 
			boolean throwException) throws JAXBException, SOAPException, DatatypeConfigurationException, IOException {
		
	    Management subManagerData = null;
//	    subManagerData = eventSource.getMetadataForSubscriptionManager();
//	    if(subManagerData==null){
//	    	String msg="The EventSource reference cannot be null.";
//	    	throw new IllegalArgumentException(msg);
//	    }
	    
//	    TransferMessageValues settings = TransferMessageValues.newInstance();
//	    Transfer tMessage = TransferUtility.buildMessage(null, settings);
	
	    //locate the subscriptionManager instance
	    if(!eventSource.isAlsoTheSubscriptionManager()){
	    	subManagerData = eventSource.getMetadataForSubscriptionManager();
			if(subManagerData==null){
				String msg="SubscriptionManager metadata is null. Unable to proceed.";
				if(logException){
					LOG.severe(msg);
				}
				if(throwException){
				  throw new IllegalArgumentException(msg);
				}
				return subManagerData;
			}
			
			//Now insert the relevant parameters to tell the SubscriptionManager how to process
				//Indicate that this is a NEW_SUBSCRIBER packet
			subManagerData.addHeaders(Management.createReferenceParametersType(
					EventingMessageValues.EVENTING_CREATION_TYPES, 
					CreationTypes.NEW_SUBSCRIBER.name()));
				Management evtSrcDetails = 
					eventSource.getMetadataForEventSource();
			//Indicate which EventSource this message comes from
			SOAPElement evtSrcMetDataId = ManagementUtility.locateHeader(
					evtSrcDetails.getHeaders(), 
					AnnotationProcessor.RESOURCE_META_DATA_UID);
			String srcMetadataKey = null;
			if((evtSrcMetDataId!=null)&&((srcMetadataKey =evtSrcMetDataId.getTextContent())!=null)&&
				!(srcMetadataKey.trim().equals(""))){
				//do nothing already have the data
			}
			//Now take the content of the incoming message
			if((srcMetadataKey!=null)&&
			(srcMetadataKey.trim().length()>0)){
			  String evtMetadata = srcMetadataKey;
			  subManagerData.addHeaders(
					  Management.createReferenceParametersType(
				EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID, 
				evtMetadata));
			  subManagerData.setAction(Transfer.CREATE_ACTION_URI);
			}
		
//		if((suggestionForEvtSinkId!=null)&&
//				(suggestionForEvtSinkId.trim().length()>0)){
//		  String evtMetadata = suggestionForEvtSinkId;
//		  subManagerData.addHeaders(
//				  Management.createReferenceParametersType(
//			EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID, 
//			evtMetadata));
//		  subManagerData.setAction(Transfer.CREATE_ACTION_URI);
//		}
		subManagerData = ManagementUtility.buildMessage(null, 
//				subManagerData, true);
				subManagerData, false);
		
		//insert the body of the message from the message passed in.
		if((message!=null)&&(message.getBody()!=null)){
		   subManagerData.getBody().addDocument(
				   message.getBody().extractContentAsDocument());
		}
//		subManagerData = 
//			ManagementUtility.removeDescriptiveMetadataHeaders(subManagerData);
System.out.println("@@@@ Newly completed piped mesg:"+subManagerData);		
		
    	
//final Addressing response = HttpClient.sendRequest(mgmt);
        final Addressing response = HttpClient.sendRequest(subManagerData);
        if (response.getBody().hasFault())
        {
System.out.println("@@@@ A fault occurred:"+response.getBody().getFault().getFaultString());        	
//            fail(response.getBody().getFault().getFaultString());
        }else{
//        	//extract the returned selectorSet
//        	Management eventSinkRegistrationResp = new Management(response);
////	        	ManagementUtility.extractSelectorsAsMap(null,eventSourceRegistrationResp.getSelectors());
//          Map<String,String> selectors=	
////        	  TransferUtility.extractSelectors(eventSinkRegistrationResp);
//        	  ManagementUtility.extractSelectors(eventSinkRegistrationResp);
////          if((eventSourceRegistrationResp.getSelectors()!=null)&&(eventSourceRegistrationResp.getSelectors().size()>0)){	
////        	  List selList = (List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors());
//          if(selectors.size()>0){	
////        	List selList = (List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors());
////			Map<String, String> selectorsRetrieved = ManagementUtility.extractSelectorsAsMap(null,
//////					(List)new ArrayList<SelectorType>(eventSourceRegistrationResp.getSelectors()));
////					selList);
////        	if(selectorsRetrieved.containsKey(EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME)){
////        		eventSourceUid = EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME;
////        		eventSourceUid+="="+selectorsRetrieved.get(EventingMessageValues.EVENT_SOURCE_ID_ATTR_NAME);
////        	}
//        	if(selectors.containsKey(EventingMessageValues.EVENT_SINK_NODE_NAME)){
//        		eventSinkId = selectors.get(
//        			EventingMessageValues.EVENT_SINK_NODE_NAME);
//        	}
//          }
        	return subManagerData;
        }
		}else{
			
		}
			
////		return eventSinkId;
//		}else{//Is one and the same handler.
//		
//		}

	 return null;
	}
	
}
