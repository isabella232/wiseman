package com.sun.ws.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.MetadataSection;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
//import com.sun.ws.management.metadata.annotations.AnnotationProcessor;

/** This class is meant to provide general utility functionality for
 *  Management instances and all of their related extensions.
 * 
 * @author Simeon
 */
public class ManagementUtility {
	
	//These values are final and static so that they can be uniformly used by many classes  
	private static final Logger LOG = Logger.getLogger(ManagementUtility.class.getName());
	private static final String uidScheme ="uuid:";
	private static final long defaultTimeout =30000;
	private static Management defautInst = null;
	{
		try{
		  defautInst = new Management();	
		}catch(Exception ex){
			//eat exception and move on.
		}
	}
	/** Takes an existing SelectorSetType container and a Map<String,String> where
	 *  Key,Value or Name,Value have been supplied are accepted as parameters.  
	 *  A SelectorSetType instance includind the Map values provided are returned.
	 * 
	 * @return SelectorSetType instance.
	 */
	public static SelectorSetType populateSelectorSetType(Map<String,String> selectors,
			SelectorSetType selectorContainer){
			if(selectorContainer==null){
				selectorContainer = new SelectorSetType();
			}
			//Now populate the selectorSetType
		    List<SelectorType> selectorList = selectorContainer.getSelector();
		    
		    // Add a selector to the list
		    for (String key : selectors.keySet()) {
		        SelectorType nameSelector = new SelectorType();
		        nameSelector.setName(key);        
		        nameSelector.getContent().add(selectors.get(key));        
		        selectorList.add(nameSelector);			
			}
	    return selectorContainer;
	}
	
	/**The method takes a SelectorSetType instance and returns the Selectors defined
	 * in a Map<String,String> instance, with Key,Value being the values respectively. 
	 * 
	 * @param selectorContainer
	 * @return Map<String,String> being Selector values
	 */
	public static Map<String,String> extractSelectorsAsMap(SelectorSetType selectorContainer){
		//Create the Map instance to be returned
		Map<String,String> map = new HashMap<String, String>();
		List<SelectorType> selectorsList = null;
		
		//populate the Map with the selectorContainer contents
		if(selectorContainer!=null){
		  selectorsList=selectorContainer.getSelector();
		  map =extractSelectorsAsMap(map, selectorsList);
		}
		
		return map;
	}

	/**The method takes a List<SelectorType> instance and returns the Selectors defined
	 * in a Map<String,String> instance, with Key,Value being the values respectively. 
	 * 
	 * @param map
	 * @param selectorsList
	 */
	public static Map<String,String> extractSelectorsAsMap(Map<String, String> map, 
			List<SelectorType> selectorsList) {
		if(map==null){
			map = new HashMap<String, String>();
		}
		if(selectorsList!=null){
			for (Iterator iter = selectorsList.iterator(); iter.hasNext();) {
				SelectorType element = (SelectorType) iter.next();
				if((element.getName()!=null)
				 &&(element.getContent()!=null)
				 &&(((String)element.getContent().get(0))).trim().length()>0){
				  map.put(element.getName(),
					(String) element.getContent().get(0));
				}
			}
		}
		return map;
	}

	/** Parses the header list to locate the SOAPElement identified by the QName 
	 * passed in.
	 * 
	 * @param headers
	 * @param qualifiedName
	 * @return
	 */
	public static SOAPElement locateHeader(SOAPElement[] headers, QName qualifiedName) {
		SOAPElement located = null;
		if((headers==null)||(qualifiedName == null)){
			return located;
		}else{
			for (int i = 0; i < headers.length; i++) {
				SOAPElement header = headers[i];
				if(qualifiedName.getLocalPart().equals(header.getElementQName().getLocalPart())&&
				   qualifiedName.getNamespaceURI().equals(header.getElementQName().getNamespaceURI())){
					return header;
				}
			}
		}
		return located;
	}
	
//	/** This method takes a GetResponse Management instance containing a 
//	 *  a MetaDataExchange element.  An array of Management instances located
//	 *  is returned in response.
//	 * 
//	 * @param metaDataResponse
//	 * @return
//	 */
//	public static Management[] extractEmbeddedMetaDataElements(Management metaDataGetResponse){
//		Management[] locatedMetaDataElements = null;
//		ArrayList<Management> located = new ArrayList<Management>();
//		
//   		//Retrieve the MetaData response to build JAXB type
//   		SOAPBody body = metaDataGetResponse.getBody();
//   		
//   		if((body!=null)&&(body.getFirstChild()!=null)){
//	   	 //Normal processing to create/retrieve the Metadata object
//	   	 Node metaDataNode = body.getFirstChild();
//	   	 
//			try {
//			 //unmarshall the Metadata node content
////				Object bound = binding.unmarshal(metaDataNode);
//			 Object bound = metaDataGetResponse.getXmlBinding().unmarshal(metaDataNode);
//			 if((bound!=null) && (bound instanceof Metadata)){
//				 Metadata ob = (Metadata)bound;
//				
//				//Parse the MetadataSections that exist
//				List<MetadataSection> metaDataSections = 
//					ob.getMetadataSection();
//				
//				if(metaDataSections!=null){
//				 for (Iterator iter = metaDataSections.iterator(); iter.hasNext();) {
//					MetadataSection element = (MetadataSection) iter.next();
//					if((element.getDialect()!=null)&&
//							(element.getDialect().equals(AnnotationProcessor.NS_URI))){
//						Management instance = new Management();
//						//Now parse the Dialect specif component.
//						instance = AnnotationProcessor.populateMetadataInformation(element, 
//								instance);
//						located.add(instance);	
//					}
//				}//end of for loop.
//			 }//end of if metaDataSections exist
//		    }
//   		   }catch (JAXBException e) {
//   			  //log and eat the exception
//   			LOG.log(Level.FINE, "JAXBException occurred:"+e.getMessage());
//   		   } catch (SOAPException e) {
//			  //log and eat the exception
//  			LOG.log(Level.FINE, "SOAPException occurred:"+e.getMessage());
//   		   }
//		}
//   		
//   		//Now populate the return array.
//   		locatedMetaDataElements = new Management[located.size()];
//   		System.arraycopy(located.toArray(), 0, 
//   				locatedMetaDataElements, 0, located.size());
//   		
//	   return locatedMetaDataElements;
//	}
	
	/**Attempts to build a message from the addressing instance passed in and with
	 * the ManagementMessageValues passed in.  Only if the values has not already
	 * been set in the Addressing instance will the values from the constants be
	 * used.
	 * 
	 * @param instance
	 * @param settings
	 * @return
	 * @throws SOAPException
	 * @throws JAXBException 
	 * @throws DatatypeConfigurationException 
	 */
	public static Management buildMessage(Addressing instance,ManagementMessageValues settings) 
		throws SOAPException, JAXBException, DatatypeConfigurationException {
		//return reference
		Management message = null;
		//initialize if not already
		if(instance == null){
			message = new Management();
		}else{//else use Addressing instance passed in.
			message = new Management(instance);
		}
		//initialize if not already
		if(settings == null){
			settings = new ManagementMessageValues();
		}
	    //Now process the settings values passed in.
		//Processing the To value
		if((message.getTo()==null)||(message.getTo().trim().length()==0)){
			//if defaults set then use them otherwise don't
			if((settings.getTo()!=null)&&(settings.getTo().trim().length()>0)){
				message.setTo(settings.getTo());
			}
		}
		
		  //Processing the ResourceURI value
		if((message.getResourceURI()==null)||
				(message.getResourceURI().trim().length()==0)){
			//if defaults set then use them otherwise don't
			if((settings.getResourceUri()!=null)&&
					(settings.getResourceUri().trim().length()>0)){
				message.setResourceURI(settings.getResourceUri());
			}
		}
		  //Processing for xmlBinding
		if(message.getXmlBinding()==null){
		   if(settings.getXmlBinding()!=null){
			  message.setXmlBinding(settings.getXmlBinding()); 
		   }else{ //otherwise use/create default one for Managemetn class
			  if(defautInst!=null){
				 message.setXmlBinding(defautInst.getXmlBinding());  
			  }else{
			     message.setXmlBinding(new Management().getXmlBinding());
			  }
		   }
		}
		
		 //Processing ReplyTo
		if((settings.getReplyTo()!=null)&&
		    settings.getReplyTo().trim().length()>0){
			message.setReplyTo(settings.getReplyTo());
		}else{
			message.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
		}
		
		 //Processing MessageId component
		if((settings.getUidScheme()!=null)&&
				(settings.getUidScheme().trim().length()>0)){
		   message.setMessageId(settings.getUidScheme() +
				   UUID.randomUUID().toString());
		}else{
			message.setMessageId(EnumerationMessageValues.DEFAULT_UID_SCHEME +
			  UUID.randomUUID().toString());
		}

		
		//Add processing for timeout
		final DatatypeFactory factory = DatatypeFactory.newInstance();
		if(settings.getTimeout()>ManagementMessageValues.DEFAULT_TIMEOUT){
			message.setTimeout(
			factory.newDuration(
					settings.getTimeout()));
		}
		
		//Add processing for other Management components
		if((settings.getAdditionalHeaders()!=null)&&
			(settings.getAdditionalHeaders().size()>0)){
			for (Iterator iter = settings.getAdditionalHeaders().iterator(); iter.hasNext();) {
				ReferenceParametersType element = (ReferenceParametersType) iter.next();
				message.addHeaders(element);
			}
		}
		
		return message;
	}
	
//	public static Management[] getExposedMetadata(String wisemanServerAddress,long timeout) 
//		throws SOAPException, IOException, JAXBException, DatatypeConfigurationException{
//		long timeoutValue = 30000;
//		if(timeout>timeoutValue){
//			timeoutValue = timeout;
//		}
//		Management[] metaDataValues = null;
//		ManagementUtility.loadServerAccessCredentials(null);
//		
//		//Make identify request to the Wiseman server
//        final Identify identify = new Identify();
//        identify.setIdentify();
//        //Send identify request
//        final Addressing response = 
//        	HttpClient.sendRequest(identify.getMessage(), 
//        			wisemanServerAddress);
//        
//        //Parse the identify response
//        final Identify id = new Identify(response);
//        final SOAPElement idr = id.getIdentifyResponse();
//        SOAPElement el = IdentifyUtility.locateElement(id, 
//        		AnnotationProcessor.META_DATA_RESOURCE_URI);
//
//        //retrieve the MetaData ResourceURI
//        String resUri=el.getTextContent();
//        el = IdentifyUtility.locateElement(id, 
//        		AnnotationProcessor.META_DATA_TO);
//
//        //retrieve the MetaData To/Destination
//        String metTo=el.getTextContent();
//    	
//     //exercise the Enumeration annotation mechanism
//        //############ REQUEST THE LIST OF METADATA AVAILABLE ######################
// 	   //Build the GET request to be submitted for the metadata
//        Management m = TransferUtility.createMessage(metTo, resUri,
////        		Transfer.GET_ACTION_URI, null, null, 30000, null);
//        		Transfer.GET_ACTION_URI, null, null, timeoutValue, null);
//        
//          //############ PROCESS THE METADATA RESPONSE ######################
//          //Parse the getResponse for the MetaData
//          final Addressing getResponse = HttpClient.sendRequest(m);
//        Management mResp = new Management(getResponse);
//        
//        //retrieve all the metadata descriptions 
//		metaDataValues = 
//			ManagementUtility.extractEmbeddedMetaDataElements(mResp); 
//		
//		return metaDataValues;
//	}
	
//	public static void loadServerAccessCredentials(Properties properties){
//		String wsmanDest="wsman.dest";
//		String wsmanUser="wsman.user";
//		String wsmanPassword="wsman.password";
//	    String wsmanBasicAuthenticationEnabled="wsman.basicauthentication";
////        <jvmarg value="-Dwsman.dest=http://localhost:8080/wsman/" />
////        <jvmarg value="-Dwsman.user=wsman" />
////        <jvmarg value="-Dwsman.password=secret" />
////        <jvmarg value="-Dwsman.basicauthentication=true" />
//	    String key = null;
//	    if((key=System.getProperty(wsmanDest))==null){
//	    	System.setProperty(wsmanDest, "http://localhost:8080/wsman/");
//	    }
//	    if((key=System.getProperty(wsmanUser))==null){
//	    	System.setProperty(wsmanUser, "wsman");
//	    }
//	    if((key=System.getProperty(wsmanPassword))==null){
//	    	System.setProperty(wsmanPassword, "secret");
//	    }
//	    if((key=System.getProperty(wsmanBasicAuthenticationEnabled))==null){
//	        System.setProperty(wsmanBasicAuthenticationEnabled, "true");
//	    }
//
//	    final String basicAuth = System.getProperty("wsman.basicauthentication");
//        if ("true".equalsIgnoreCase(basicAuth)) {
//        	HttpClient.setAuthenticator(new transport.BasicAuthenticator());
////            HttpClient.setAuthenticator(new SimpleHttpAuthenticator());
//        }
//	    
//	}
	
	//###################### GETTERS/SETTERS for instance 
    /* Exposes the default uid scheme for the ManagementUtility instance.
     * 
     */
	public static String getUidScheme() {
		return uidScheme;
	}

	/**
	 * @return the defaultTimeout
	 */
	public static long getDefaultTimeout() {
		return defaultTimeout;
	}
	
}
