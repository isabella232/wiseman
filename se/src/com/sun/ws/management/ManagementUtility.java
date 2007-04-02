package com.sun.ws.management;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;

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
