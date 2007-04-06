package com.sun.ws.management.mex;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementUtility;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.MetadataSection;

import com.sun.ws.management.metadata.annotations.AnnotationProcessor;

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
}
