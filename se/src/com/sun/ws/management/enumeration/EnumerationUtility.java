package com.sun.ws.management.enumeration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AnyListType;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
//import com.sun.ws.management.client.EnumerationResourceState;
//import com.sun.ws.management.client.impl.EnumerationResourceStateImpl;
import com.sun.ws.management.enumeration.EnumerationExtensions.Mode;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.xml.XmlBinding;

/** This class is meant to provide general utility functionality for
 *  Enumeration message instances and all of their related extensions.
 *  To enable messages ready for submission, Management instances are 
 *  returned from some of the methods below. 
 * 
 *  The following default values are defined when newer values have not 
 *  been supplied:
 *  	UID_SCHEME = uuid:
 *  	DEFAULT_TIMEOUT = 30000
 *      ACTION = Enumeration.ENUMERATE_ACTION_URI 
 *  	FILTER_TYPE.DIALECT = XPATH
 *  	REPLY_TO = Anonymous URI
 *  
 * @author Simeon Pinder
 */
public class EnumerationUtility {
	
	private static final Logger LOG = 
		Logger.getLogger(EnumerationUtility.class.getName());
	
	public static Enumeration buildMessage(Enumeration existingEnum,
			EnumerationMessageValues settings) 
		throws SOAPException, JAXBException, DatatypeConfigurationException{
		
		if(existingEnum == null){//build default instances
			Management mgmt = ManagementUtility.buildMessage(null, settings);
		   existingEnum = new Enumeration(mgmt);
		}
		if(settings ==null){//grab a default instance if 
			settings = EnumerationMessageValues.newInstance();
		}
		
	//Process the EnumerationConstants instance passed in.
		 //Processing ACTION for the message
		if((settings.getEnumerationMessageActionType()!=null)&&
				(settings.getEnumerationMessageActionType().trim().length()>0)){
		   existingEnum.setAction(settings.getEnumerationMessageActionType());
		}else{
		   existingEnum.setAction(
			 EnumerationMessageValues.DEFAULT_ENUMERATION_MESSAGE_ACTION_TYPE);
		}
		
		 //Processing ReplyTo
		if((settings.getReplyTo()!=null)&&
		    settings.getReplyTo().trim().length()>0){
			existingEnum.setReplyTo(settings.getReplyTo());
		}else{
			existingEnum.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
		}
		
		 //Processing MessageId component
		if((settings.getUidScheme()!=null)&&
				(settings.getUidScheme().trim().length()>0)){
		   existingEnum.setMessageId(settings.getUidScheme() +
				   UUID.randomUUID().toString());
		}else{
			existingEnum.setMessageId(EnumerationMessageValues.DEFAULT_UID_SCHEME +
			  UUID.randomUUID().toString());
		}
		
		//processing Timeout/Duration and Filter
        final DatatypeFactory factory = DatatypeFactory.newInstance();
        final FilterType filterType = Enumeration.FACTORY.createFilterType();
        if((settings.getFilterDialect()!=null)&&
           (settings.getFilterDialect().trim().length()>0)){
           filterType.setDialect(settings.getFilterDialect());
        }else{
           filterType.setDialect(EnumerationMessageValues.DEFAULT_FILTER_DIALECT);	
        }
        	//filter component
        if((settings.getFilter()!=null)&&
            (settings.getFilter().trim().length()>0)){
            filterType.getContent().add(settings.getFilter());
        }
        
        if(settings.getTimeout()>-1){
        	existingEnum.setEnumerate(null, 
        			factory.newDuration(
        					settings.getTimeout()).toString(),
              settings.getFilter() == null ? null : filterType);
        }else{
        	existingEnum.setEnumerate(null, 
        			factory.newDuration(
        					settings.getTimeout()).toString(),
              settings.getFilter() == null ? null : filterType);
        }
        
        //processing the EnumContext
        if(settings.getEnumerationContext()!=null){
        	//Process for PULL action
        	if(existingEnum.getAction().equals(Enumeration.PULL_ACTION_URI)){
        		EnumerationExtensions enx = new EnumerationExtensions(existingEnum);
        		enx.setPull(
        		settings.getEnumerationContext(), 
        		   settings.getMaxCharacters(), 
        		   settings.getMaxElements(), 
        		   EnumerationMessageValues.newDuration(settings.getDefaultTimeout()),
        		   settings.isRequestForTotalItemsCount());
        		existingEnum = new Enumeration(enx);
        	}else if(existingEnum.getAction().equals(Enumeration.ENUMERATE_ACTION_URI)){
        		EnumerationExtensions enx = new EnumerationExtensions(existingEnum);
        		EndpointReferenceType endTo = null;
        		if (settings.getEndTo() == null || settings.getEndTo().length() == 0) {
        			endTo = null;
        		} else {
        			endTo = Addressing.createEndpointReference(settings.getEndTo(), null, null, null, null);
        		}
        		enx.setEnumerate(
        			endTo, 
        			settings.isRequestForTotalItemsCount(), 
    				settings.isRequestForOptimizedEnumeration(), 
    				settings.getMaxElements(), 
//    				Long.valueOf(settings.getDefaultTimeout()).toString(),
    				factory.newDuration(settings.getExpires()).toString(),
            		EnumerationMessageValues.newFilter(
            				settings.getFilter(), 
            				settings.getFilterDialect()), 
            		settings.getEnumerationMode());
        		existingEnum = new Enumeration(enx);
           	}else if(existingEnum.getAction().equals(Enumeration.RELEASE_ACTION_URI)){
           		existingEnum.setRelease(settings.getEnumerationContext());
           	
           	}
        }
        
        //Add namespace elements if defined.
        if((settings.getNamespaceMap()!=null)&&
        	!(settings.getNamespaceMap().isEmpty())){
        	existingEnum.addNamespaceDeclarations(
        			settings.getNamespaceMap());
        }
        
        //process the custom packages list that needs to be added to Binding
        if((settings.getCustomXmlBindingPackageList()!=null)&&
           (settings.getCustomXmlBindingPackageList().length>0)){
           String existingCustPackageList =	
        	   System.getProperty(XmlBinding.class.getPackage().getName() + 
        			   ".custom.packagenames");
           String custBindList="";
           String[] bindings = settings.getCustomXmlBindingPackageList();
           for (int i = 0; i < bindings.length; i++) {
			 String pack = bindings[i];
			 if(existingCustPackageList!=null){
				if(existingCustPackageList.indexOf(pack)==-1){
				  if(custBindList.trim().length()==0){
					 custBindList = pack; 
				  }else{
					 custBindList = ","+pack;
				  }
				}
			 }
           }
           
	   	   // Set the system property to always create bindings with our package
	   	   System.setProperty(XmlBinding.class.getPackage().getName() + ".custom.packagenames",
	   				custBindList);
	
        }
        return existingEnum;
	}

	public static List<EnumerationItem> extractEnumeratedValues(
			Addressing responseMessage) 
		throws SOAPException, JAXBException {
		//Generate the response object and set to empty if message null.
		List<EnumerationItem> items = null;
		if(responseMessage ==null){
			return items = new ArrayList<EnumerationItem>();
		}
		
		//Attempt to instantiate an Enumeration
		Enumeration enu = new Enumeration(responseMessage);
		if(enu ==null){
		   return items= new ArrayList<EnumerationItem>();	
		}
		
		//Attempt to extract data from a PullResponse object
		//TODO: this is not complete or tested.
		PullResponse pullResponse = enu.getPullResponse();
		if(pullResponse!=null){
			ItemListType itemsList = null;
			if((itemsList = pullResponse.getItems())!=null){
//				state= new EnumerationResourceStateImpl(body.extractContentAsDocument());				
//			  items= enu.get;
			  if(itemsList.getAny()!=null){
			   items = new ArrayList<EnumerationItem>();
			   for (Iterator iter = itemsList.getAny().iterator(); iter.hasNext();) {
				Object element = (Object) iter.next();
				
			   }
			  }
			}
		}
		
		//Attempt to extract optimized Enumeration request values as response object
		EnumerationExtensions enx = new EnumerationExtensions(enu);
		if(enx!=null){
			items = enx.getItems();
		}
		
		return items;
	}

	//TODO: THIS SHOULD BE exposed by a CLIENT SIDE CLASS,RESPONSE.
	/**Method attempts to extract each Item, from a pullResponse object
	 * as an array of Elements.  For Items listed in duplicate(ObjectAndEpr), then 
	 * this method attempts to locate each element and an associated EPR as well. 
	 * An EPR is an open ended element and the Element[] array list may not be 
	 * easy to evaluate. Use this method at your own discretion.  
	 * @param pullResponse encapsulates the Enumeration response received.
	 * @throws SOAPException
	 * @throws JAXBException
	 */
	public static Element[] extractItemsAsElementArray(final Enumeration pullResponse) 
	throws SOAPException, JAXBException {
		if(pullResponse==null){
			throw new IllegalArgumentException("Enumeration instance cannot be null.");
		}
		Element[] elementList = null;
		Vector<Element> bag = new Vector<Element>();
		EnumerateResponse er =  pullResponse.getEnumerateResponse();
		PullResponse pr =  pullResponse.getPullResponse();
		if(((er==null)&(pr==null))){
			String msg="Unable to find an EnumeratResponse/PullResponse element in the message.";
			throw new IllegalArgumentException(msg);
		}
		
		List<Object> allAnys = null;
		if(pr!=null){//Process a stereotypical PullResponse object
		  ItemListType itemList = pr.getItems();
		  allAnys =itemList.getAny();	
		  for (Iterator iter = allAnys.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			Element elem = (Element) element;
			bag.add(elem);
		  }
		}else{//Else process the stereotypical EnumerateResponse object
			allAnys =er.getAny();	
		
		  for (Iterator iter = allAnys.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
	       AnyListType lst1 = null;
		   JAXBElement<AnyListType> itemsList = (JAXBElement<AnyListType>) element;
	
		  if(itemsList.getValue() instanceof AnyListType){	
			lst1 = itemsList.getValue();
			List<Object> elements = lst1.getAny();
			for (Object obj : elements) {
			    if (obj instanceof Element) {//Is a simple Element
			        Element el = (Element) obj;
					bag.add(el);
			    }
			    else if(obj instanceof JAXBElement){
				    JAXBElement gen = (JAXBElement) obj;
				    EndpointReferenceType eprT = (EndpointReferenceType) gen.getValue();
	 				if(eprT!=null){
	
					Document eprDocument = Management.newDocument();
					JAXBElement<EndpointReferenceType> eprElement = 
						new org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory().
						createEndpointReference(
								eprT);
					//binding.marshal(eprElement, eprDocument);
					pullResponse.getXmlBinding().marshal(eprElement, eprDocument);
					  bag.add(eprDocument.getDocumentElement());
	 				}
			    	
			    }
			}
		  }
		 }//End of processing for EnumerateResponse
	 }
		if(bag.size()>0){
			elementList = new Element[bag.size()];  
			bag.toArray(elementList);  
		}else{
			elementList = new Element[0];  
		}
		return elementList;  
	}	
}
