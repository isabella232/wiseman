package com.sun.ws.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.MetadataSection;

import com.sun.ws.management.metadata.annotations.AnnotationProcessor;

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
	
	/** This method takes a GetResponse Management instance containing a 
	 *  a MetaDataExchange element.  An array of Management instances located
	 *  is returned in response.
	 * 
	 * @param metaDataResponse
	 * @return
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
