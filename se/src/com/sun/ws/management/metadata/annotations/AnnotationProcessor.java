package com.sun.ws.management.metadata.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3._2003._05.soap_envelope.Envelope;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.MetadataSection;
import org.xmlsoap.schemas.ws._2004._09.mex.ObjectFactory;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.server.Handler;

/** This class is responsible for processing Wiseman-annotated code
 *  to populate information on how to contact these handler/endpoints. 
 * 
 * @author Simeon
 *
 */
public class AnnotationProcessor {
	
	private static final Logger LOG = Logger.getLogger(AnnotationProcessor.class.getName());

	/* Define contants helpful in Annotation Processing for metadata.
	 */
	public static final String NS_PREFIX ="wsmeta"; 
	public static final String NS_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/version1.0.0.a/default-addressing-model.xsd";
	// QNAMES used in meta data processing
	public static final QName META_DATA_CATEGORY = new QName(NS_URI,"MetaDataCategory",NS_PREFIX);
	public static final QName META_DATA_DESCRIPTION = new QName(NS_URI,"MetaDataDescription",NS_PREFIX);
	public static final QName META_DATA_TO = new QName(NS_URI,"MetaDataTo",NS_PREFIX);
	public static final QName META_DATA_ENABLED = new QName(NS_URI,"MetaDataEnabled",NS_PREFIX);
	public static final QName META_DATA_RESOURCE_URI = new QName(NS_URI,"MetaDataResourceURI",NS_PREFIX);
	public static final QName RESOURCE_META_DATA_UID = new QName(NS_URI,"ResourceMetaDataUID",NS_PREFIX);
	public static final QName RESOURCE_MISC_INFO = new QName(NS_URI,"ResourceMiscInfo",NS_PREFIX);
	public static final QName ENUMERATION_ACCESS_RECIPE = new QName(NS_URI,"EnumerationAccessRecipe",NS_PREFIX);
	public static final QName ENUMERATION_FILTER_USAGE = new QName(NS_URI,"EnumerationFilterUsage",NS_PREFIX);
	
	public static org.w3._2003._05.soap_envelope.ObjectFactory envFactory 
	= new org.w3._2003._05.soap_envelope.ObjectFactory();
	
	//Define the JAXB object factory references for un/marshalling
	private static org.xmlsoap.schemas.ws._2004._09.mex.ObjectFactory metaFactory = 
		new org.xmlsoap.schemas.ws._2004._09.mex.ObjectFactory();
	
	/** Method takes a defaultAddressingModelAnnotation instance and places
	 * all of the values into a Management instance. 
	 * 
	 * @param defAddMod is the annotation instance.
	 * @return Management instance with all of the properties for a resource using the
	 * 		   defaultAddressingModel.
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public static Management 
		populateManagementInstance(
			WsManagementDefaultAddressingModelAnnotation defAddMod) 
			throws JAXBException, 
			SOAPException {
		
	//Walk through the values of the annotation to populate Management reference
	Management metaData = new Management();
	
	//exit out if the annotation passed in is invalid
	if(defAddMod==null){
		LOG.log(Level.FINE,"The Annotation passed in is null.");
		return metaData;
	}
		//if address details are present, then populate
	   if(defAddMod.getDefaultAddressDefinition()!=null){
		populateManagementAddressDetails(defAddMod.getDefaultAddressDefinition(), metaData);
	   }
		//Process additional metaData values and add to Mgmt inst.
		if((defAddMod.metaDataCategory()!=null)&&
				(defAddMod.metaDataCategory().trim().length()>0)){
			metaData.addHeaders(
					Management.createReferenceParametersType(
							META_DATA_CATEGORY,
							defAddMod.metaDataCategory()));
//			LOG.log(Level.FINE,"The MetaDataCategory is added");
		}
		if((defAddMod.metaDataDescription()!=null)&&
				(defAddMod.metaDataDescription().trim().length()>0)){
			metaData.addHeaders(
					Management.createReferenceParametersType(
							META_DATA_DESCRIPTION,
							defAddMod.metaDataDescription()));
		}
		if((defAddMod.resourceMetaDataUID()!=null)&&
				(defAddMod.resourceMetaDataUID().trim().length()>0)){
			metaData.addHeaders(
					Management.createReferenceParametersType(
							RESOURCE_META_DATA_UID,
							defAddMod.resourceMetaDataUID()));
		}
		if((defAddMod.resourceMiscellaneousInformation()!=null)&&
				(defAddMod.resourceMiscellaneousInformation().trim().length()>0)){
			metaData.addHeaders(
					Management.createReferenceParametersType(
					RESOURCE_MISC_INFO,
					defAddMod.resourceMiscellaneousInformation()));
		}
		
	  return metaData;
	}

	/** Method takes a defaultAddressingModelAnnotation instance and places
	 * all of the values into a Management instance. 
	 * 
	 * @param enumSrc is the annotation instance.
	 * @return Management instance with all of the properties for a resource using the
	 * 		   defaultAddressingModel.
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public static Management 
		populateManagementInstance(
			WsManagementEnumerationAnnotation enumSrc) 
			throws JAXBException, 
			SOAPException {
			Management defAddValue = 
				populateManagementInstance(enumSrc.getDefaultAddressModelDefinition());
			//Add the additional Enumeration values
			if((enumSrc.resourceEnumerationAccessRecipe()!=null)&&
					(enumSrc.resourceEnumerationAccessRecipe().trim().length()>0)){
				defAddValue.addHeaders(
						Management.createReferenceParametersType(
								ENUMERATION_ACCESS_RECIPE,
								enumSrc.resourceEnumerationAccessRecipe()));
			}
			if((enumSrc.resourceFilterUsageDescription()!=null)&&
					(enumSrc.resourceFilterUsageDescription().trim().length()>0)){
				defAddValue.addHeaders(
						Management.createReferenceParametersType(
							ENUMERATION_FILTER_USAGE,
							enumSrc.resourceFilterUsageDescription()));
			}
		return defAddValue;
	}
	
	/**Takes the WsManagementAddressDetailsAnnotation annotation and puts all of the
	 * values into the Management instance passed in.
	 * @param defAddMod
	 * @param metaData
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	private static void populateManagementAddressDetails(
			WsManagementAddressDetailsAnnotation defAddMod, 
			Management metaData) throws JAXBException, SOAPException {
		if(defAddMod!=null){
			  
			//Retrieve the DefaultAddressDetails annotation data
			WsManagementAddressDetailsAnnotation addDetails = 
				defAddMod;
			if(addDetails!=null){
			  //populate the address details node
				//Process referenceParameters entered
				if(addDetails.referenceParametersContents()!=null){
					WsManagementQNamedNodeWithValueAnnotation[] refParams = 
						addDetails.referenceParametersContents();
					//For each reference parameter located...
					for (int i = 0; i < refParams.length; i++) {
						WsManagementQNamedNodeWithValueAnnotation param = refParams[i];
						QName nodeId = new QName(param.namespaceURI(),
								param.localpart(),param.prefix());
					  if((param.nodeValue()!=null)&&(param.nodeValue().trim().length()>0)){	
						ReferenceParametersType refParamType = 
							Management.createReferenceParametersType(nodeId, 
									param.nodeValue());
						metaData.addHeaders(refParamType);
					  }
					}
				}
				//Process referenceProperties entered
				if(addDetails.referencePropertiesContents()!=null){
				   WsManagementQNamedNodeWithValueAnnotation[] refProps = 
					   addDetails.referencePropertiesContents();
				   //For each reference property located...
				   for (int i = 0; i < refProps.length; i++) {
					  WsManagementQNamedNodeWithValueAnnotation param = refProps[i];
					  QName nodeId = new QName(param.namespaceURI(),
							  param.localpart(),param.prefix());
					if((param.nodeValue()!=null)&&(param.nodeValue().trim().length()>0)){							  
					  ReferencePropertiesType refPropType = 
						  Management.createReferencePropertyType(nodeId, 
								  param.nodeValue());
					  metaData.addHeaders(refPropType);
					}
				   }
				}
				//process additional metadata information
				if((addDetails.wsaTo()!=null)&&(addDetails.wsaTo().trim().length()>0)){
					metaData.setTo(addDetails.wsaTo().trim());
				}
				if((addDetails.wsmanResourceURI()!=null)&&
						(addDetails.wsmanResourceURI().trim().length()>0)){
					metaData.setResourceURI(addDetails.wsmanResourceURI().trim());
				}
				//Process the selectorSet values from metadata
				if((addDetails.wsmanSelectorSetContents()!=null)&&
						(addDetails.wsmanSelectorSetContents().length>0)){
					//build the selector set contents
					Set<SelectorType> set = new HashSet<SelectorType>();
					String[] array = addDetails.wsmanSelectorSetContents();
					for (int i = 0; i < array.length; i++) {
						String pair = array[i].trim();
						 pair=pair.substring(0,pair.length());
						 pair=pair.trim();
						StringTokenizer tokens = 
							new StringTokenizer(pair,"=");
						SelectorType st = 
							Management.FACTORY.createSelectorType();
						if((tokens.hasMoreTokens())&&(tokens.countTokens()>1)){
						 st.setName(tokens.nextToken());
						 st.getContent().add(tokens.nextToken());
						 set.add(st);
						}
					}
					if(set.size()>0){
					 metaData.setSelectors(set);
					}
				}
			}
		  }//end of defaultAddressDefinition
	}

	public static Vector<Annotation> 
	  populateAnnotationsFromClass(Class element){
		Vector<Annotation> allAnots = new Vector<Annotation>();
		  //null check. 
		  if(element==null){
			  return allAnots;
		  }
		  //TODO: analyze to see if this is too stringent
		  //make sure than annotated classes are instances of Handler 
		  boolean isHandlerInst = true;
//		  boolean isHandlerInst = false;
//		  Class[] vals = element.getInterfaces();
//		  for (int i = 0; i < vals.length; i++) {
//			  if(vals[i].getCanonicalName().equals(Handler.class.getCanonicalName())){
//				 isHandlerInst = true; 
//			  }
//		  }
		  if(isHandlerInst){
		   //process the class for annotations
			  if(element.isAnnotationPresent(WsManagementDefaultAddressingModelAnnotation.class)){
				  Annotation annotation = 
					  element.getAnnotation(WsManagementDefaultAddressingModelAnnotation.class);
				  WsManagementDefaultAddressingModelAnnotation defAddMod = 
					  (WsManagementDefaultAddressingModelAnnotation)annotation;
				  allAnots.add(defAddMod);
				  
				  //test the Annotated class to see if class fields are also annotated. 
				  //Enables a scalable Annotation model. 
				  Field[] classFields = element.getDeclaredFields();
				  for (int i = 0; i < classFields.length; i++) {
					  Field variable = classFields[i];
					  if(variable.isAnnotationPresent(WsManagementDefaultAddressingModelAnnotation.class)){
						  WsManagementDefaultAddressingModelAnnotation defAddModAnnot = 
							  (WsManagementDefaultAddressingModelAnnotation)variable.getAnnotation(
									  WsManagementDefaultAddressingModelAnnotation.class);
						  allAnots.add(defAddModAnnot);	
					  }
				  }
			  }
			  if(element.isAnnotationPresent(WsManagementEnumerationAnnotation.class)){
				  Annotation annotation = 
					  element.getAnnotation(WsManagementEnumerationAnnotation.class);
				  WsManagementEnumerationAnnotation enumSource = 
					  (WsManagementEnumerationAnnotation)annotation;
				  allAnots.add(enumSource);
				  
				  //test the Annotated class to see if class fields are also annotated. 
				  //Enables a scalable Annotation model. 
				  Field[] classFields = element.getDeclaredFields();
				  for (int i = 0; i < classFields.length; i++) {
					Field variable = classFields[i];
					if(variable.isAnnotationPresent(WsManagementEnumerationAnnotation.class)){
					  WsManagementEnumerationAnnotation enuSrcAnnot = 
						  (WsManagementEnumerationAnnotation)variable.getAnnotation(
								  WsManagementEnumerationAnnotation.class);
					  allAnots.add(enuSrcAnnot);	
					}
				  }
			 }
          }
//		  else{//class is not an instance of Handler.
//        	  LOG.log(Level.FINE,"The class '"+element.getCanonicalName()+
//        			  "' not an instance of Handler."); 
//          }	
		return allAnots;	
	 }
	 
		/**
		 * @param section
		 * @param instance
		 * @throws JAXBException
		 * @throws SOAPException
		 */
		public static Management populateMetadataInformation(MetadataSection section, 
				Management instance) throws JAXBException, SOAPException {
			if(instance==null){
				instance = new Management();
			}
			if(section==null){
				return instance;
			}
			
			//Retrieve the custom content of this MetadataSection.
			Object customDialectContent = section.getAny(); 				
			
			//Translate the Metadata node to a Management instance.
			Envelope env = envFactory.createEnvelope();
			JAXBElement<Envelope> envelope = (JAXBElement<Envelope>) customDialectContent;
			env = envelope.getValue();
			
			   List<Object> headerList = env.getHeader().getAny();
			   for (Iterator iter = headerList.iterator(); iter.hasNext();) {
				Object element = (Object) iter.next();
				  
				  //if header is instance of ElementNSImpl
				  if(element instanceof ElementNSImpl){
				   ElementNSImpl e = (ElementNSImpl) element;
				    QName node = AnnotationProcessor.populateNode(e);
				    instance.addHeaders(
						Management.createReferenceParametersType(
							node,
							e.getTextContent()));
				  }else if(element instanceof JAXBElement){
					  JAXBElement jel = (JAXBElement) element;
					  if(jel.getDeclaredType().equals(AttributableURI.class)){
						AttributableURI e = (AttributableURI) jel.getValue();
						if(Management.RESOURCE_URI.equals(jel.getName())){
							instance.setResourceURI(e.getValue());
						}
					  }
					  else if(jel.getDeclaredType().equals(AttributedURI.class)){
						  AttributedURI atUri = (AttributedURI) jel.getValue();
						  if(Addressing.TO.equals(jel.getName())){
							  instance.setTo(atUri.getValue());
						  }else if(Management.RESOURCE_URI.equals(jel.getName())){
							  instance.setResourceURI(atUri.getValue());
						  }
					  }
					  else if(jel.getDeclaredType().equals(SelectorSetType.class)){
						  SelectorSetType sel = (SelectorSetType) jel.getValue();
						  HashSet selSet = new HashSet<SelectorType>(sel.getSelector());
						  instance.setSelectors(selSet);
					  }
				  }else{
				  }
			   }
			  return instance; 
		}

	public static QName populateNode(ElementNSImpl e) {
		return new QName(e.getNamespaceURI(),e.getLocalName(),e.getPrefix());
	}
	
	/**
	 * @param metaElement
	 * @param element
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public static Metadata populateMetaDataElement(Metadata metaElement, Management element) 
				throws JAXBException, SOAPException {
		if((metaElement==null)||(element ==null)){
			return metaElement;
		}
		
		//Create enclosing MetaDataSection element
		MetadataSection metaSection = metaFactory.createMetadataSection();
		 metaElement.getMetadataSection().add(metaSection);
		 metaSection.setDialect(AnnotationProcessor.NS_URI); 
		 metaSection.setIdentifier(AnnotationProcessor.NS_URI);
		 
		 //Now populate the MetaData specific element/any node.
		 metaSection.setAny(element.getEnvelope());
		    
		return metaElement;    
	}
}
