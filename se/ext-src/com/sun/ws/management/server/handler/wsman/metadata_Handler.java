package com.sun.ws.management.server.handler.wsman;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
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

import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.ObjectFactory;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.eventing.EventingMessageValues.CreationTypes;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementEnumerationAnnotation;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.IteratorFactory;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XmlBinding;

/** This handler is meant to expose metaData information for annotated
 *  handlers. First priority is a simplistic implementation of MetadataExchange1.1,
 *  where metadata for Wiseman is returned in response to simple Transfer.Get
 *  and Metadata.GET reqeusts.  The exposed metadata is represented via a custom 
 *  metadata filter dialect.  The embedded metadata representation are just 
 *  instances of Management messages with all of the addressing details prepopulated
 *  by the service implementors.
 * 
 *  Enumeration and Optimized enumeration functionality is be added to allow users
 *  to filter the requested Enumeration values.
 * 
 * @author Simeon Pinder
 *
 */
@WsManagementEnumerationAnnotation(
	getDefaultAddressModelDefinition=
		@WsManagementDefaultAddressingModelAnnotation(
			getDefaultAddressDefinition=
				@WsManagementAddressDetailsAnnotation(
					wsaTo=metadata_Handler.wsaTo, 
					wsmanResourceURI=metadata_Handler.wsaResourceURI), 
			resourceMetaDataUID = metadata_Handler.resourceMetaUID
		),
	resourceEnumerationAccessRecipe = 
		"Enumerate and Optimized Enumeration with no arguments returns all available Event Sources.",
	resourceFilterUsageDescription = 
		"Filtering via RESOURCE_META_DATA_UID. Ex. env:Envelope/env:Header/wsmeta:ResourceMetaDataUID/text()='"+
		metadata_Handler.resourceMetaUID+"' to isolate a particular resource."
)
public class metadata_Handler implements Handler {
	public static final String wsaTo="http://localhost:8080/wsman/";
	public static final String wsaResourceURI="wsman:metadata";
	public static final String resourceMetaUID=
		"http://wiseman.dev.java.net/MetaData/ri/uid-200007-met";
	//Class properties
	private static final Logger LOG = Logger.getLogger(metadata_Handler.class.getName());
	//millisecond value for metadata refresh interval
	private static long timeout = 1000*60*15;
	//property file for describing annotated handlers
	private static String ANNOTATED_LIST="/metadata-handlers.properties";
	//handler list.
	private static HashMap<String,String> handlerList = new HashMap<String,String>();
	
	//Define the JAXB object factory references for un/marshalling
	private static ObjectFactory metaFactory = new ObjectFactory();
	private static final Vector<Management> metaDataValues = new Vector<Management>();
	private static XmlBinding binding = null;
	private static boolean metaDataInitialized =false;
	
	//Static initialization block.
	static{
		//load list of customPackages to the XmlBinding instance.
		try {
			String[] custPackages = {
					"org.xmlsoap.schemas.ws._2004._09.mex",
					"org.dmtf.schemas.wbem.wsman._1.wsman",
					"org.w3._2003._05.soap_envelope"};
			binding=new XmlBinding(null,custPackages);
			LOG.log(Level.FINE, "Custom JAXB packages loaded.");
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "Error loading Custom JAXB packages defined.");			
			throw new InternalErrorFault(e.getMessage());
		}
		
		//Load the metadata.refresh.interval from the properties file.
		boolean refreshEnabled = false;
		long refreshInterval =0; 
		 try{
		  refreshEnabled=Boolean.valueOf(System.getProperty("metadata.refresh.enabled"));
		  refreshInterval=Long.valueOf(System.getProperty("metadata.refresh.interval"));
		 }catch(NumberFormatException nfex){
		 }
		if(refreshInterval>60*1000*5){timeout = refreshInterval;}
		
		//launch a task to repeatedly/periodically launch the refreshMetaDataInformation method
        final TimerTask ttask = new TimerTask() {
            public void run() {
                try {
					metadata_Handler.refreshMetaDataInformation();
					LOG.log(Level.FINEST, "Refreshing annotation details.");
				} catch (ServletException e) {
	            	LOG.log(Level.FINE, "An error occurred while refreshing annotation details:"+e.getMessage());
					e.printStackTrace();
				}
            }
        };
        
        //launch timer that will die with the enclosing thread.
        final Timer timeoutTimer = new Timer(true);
        if(refreshEnabled){
          timeoutTimer.schedule(ttask,5000, timeout);
        }
		
        initializeAnnotationProcessing();
	}

	/**
	 * 
	 */
	private static void initializeAnnotationProcessing() {
		// load subsystem properties and save them in a type-safe map
        try {
			populateHandlerList();
		} catch (ServletException e) {
			LOG.log(Level.FINE, "Error loading annotated handler list from "+ANNOTATED_LIST);
			e.printStackTrace();
		}

		//This resources needs to poll the existing classes for metatdata annotations on some interval
		//Each get will return the current state of the known metadata.
		ArrayList<Class> bag = getAnnotatedClassesFromHandlerMap();
		populateMasterMetaDataList(bag);
	}

	/**Takes an container with Class instances, parses for relevant annotations
	 * and then populates the master list of annotation data maintained by this 
	 * handler
	 * @param bag ArrayList<Class> containing list of annnotated classes.
	 */
	private static void populateMasterMetaDataList(ArrayList<Class> bag) {

		Vector<WsManagementDefaultAddressingModelAnnotation> allDefaultAddressingAnnots = null;
		Vector<WsManagementEnumerationAnnotation> allEnumerationAnnots = null;
		Vector<Annotation> annotated = null;
		Vector<Annotation> allAnnotations = new Vector<Annotation>();
		
		//iterate through list of classes passed in.
		for (Iterator iter = bag.iterator(); iter.hasNext();) {
			Class element = (Class) iter.next();
			
			LOG.log(Level.FINE, "Populating annotations for '"+element.getCanonicalName()+".");
			annotated = AnnotationProcessor.populateAnnotationsFromClass(element);
			if(annotated!=null){
				for (Iterator iterator = annotated.iterator(); iterator
						.hasNext();) {
					Annotation aEl = (Annotation) iterator.next();
					allAnnotations.add(aEl);
				}
			}
				
		}
		if((allAnnotations!=null)&&(allAnnotations.size()>0)){
			LOG.log(Level.FINE, "Located "+allAnnotations.size()+" annotation(s).");
		}
		//For correctly annotated instances...
	  try{	
		 for (Iterator iter = allAnnotations.iterator(); iter.hasNext();) {
			Annotation element = (Annotation) iter.next();
			if(element instanceof WsManagementDefaultAddressingModelAnnotation){
				WsManagementDefaultAddressingModelAnnotation anotElement = 
					(WsManagementDefaultAddressingModelAnnotation)element;
				Management wsmanMetaData = null;
				wsmanMetaData = 
					AnnotationProcessor.populateManagementInstance(anotElement);
				if(wsmanMetaData !=null){
					metaDataValues.add(wsmanMetaData);
				}
			}
			if(element instanceof WsManagementEnumerationAnnotation){
				WsManagementEnumerationAnnotation anotElement = 
				  (WsManagementEnumerationAnnotation)element;
			  Management wsmanMetaData = null;
				wsmanMetaData = 
					AnnotationProcessor.populateManagementInstance(anotElement);
			  if(wsmanMetaData !=null){
				  metaDataValues.add(wsmanMetaData);
			  }
			}
		 }
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	private static ArrayList<Class> getAnnotatedClassesFromHandlerMap() {
		ArrayList<Class> bag;
		bag = new ArrayList<Class>();
		for (Iterator iter = handlerList.keySet().iterator(); iter.hasNext();) {
			String annotHandKey = (String) iter.next();
			String className = (String) handlerList.get(annotHandKey);
			try {
				Class anot = Class.forName(className);
				if(!bag.contains(anot)){
					bag.add(anot);
				}
			} catch (ClassNotFoundException e) {
            	LOG.log(Level.WARNING, "The class '"+className+"' loaded from "+
            			ANNOTATED_LIST+" could not be found.");
				e.printStackTrace();
			}
		}
		return bag;
	}
	
	public void handle(String action, String resource, HandlerContext context, 
			Management request, Management response) throws Exception {
		//if action is Transfer.GET or MetaDataExchange.GetMetaData then process 
		// else ActionNotSupported
		if(Transfer.GET_ACTION_URI.equals(action)){
			//refresh metaDataInformation
			
			// Create an empty DOM document for marshalling metadata content.
	        Document responseDoc = Management.newDocument();

			//Create enclosing MetaData
			Metadata metaElement = metaFactory.createMetadata();
			int i=0;
	        //iterate through all of the metaData containers(Management)
	        for (Iterator iter = metaDataValues.iterator(); iter.hasNext();) {
				Management element = (Management) iter.next();
				AnnotationProcessor.populateMetaDataElement(metaElement, element);
			}
	
	        try {
			  binding.marshal(metaElement, responseDoc );
			} catch (Exception e) {
				LOG.log(Level.SEVERE,"Error marshalling content :"+e.getMessage());
//				  System.out.println("Exception:"+e.getMessage());
				throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
			} 	         

	        response.getBody().addDocument(responseDoc);
		}
		else if(Enumeration.ENUMERATE_ACTION_URI.equals(action)){
			Enumeration enuResponse = new Enumeration(response);
			Enumeration enuRequest = new Enumeration(request);
	        enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
	        synchronized (this) {
        	 // Make sure there is an Iterator factory registered for this resource
        	  EnumerationSupport.registerIteratorFactory(resource,
        		new MetaDataIteratorFactory(resource));
	        }
	        EnumerationSupport.enumerate(context, enuRequest, enuResponse);
		}
		else if(com.sun.ws.management.mex.Metadata.METADATA_ACTION_URI.equals(action)){
			//TODO: implement
		}
		//implement below to start the lazy initialization process
//		else if(Transfer.INITIALIZE_ACTION_URI.equals(action)){
		else if(com.sun.ws.management.mex.Metadata.INITIALIZE_ACTION_URI.equals(action)){
		   if(!metaDataInitialized){
			   
			initializationMetaDataHandlerFunctionality();
			   
			//Completed static initialization   
			metaDataInitialized = true;
//			response.setAction(Transfer.INITIALIZE_RESPONSE_URI);
			response.setAction(com.sun.ws.management.mex.Metadata.INITIALIZE_RESPONSE_URI);
		   }
		}
		else{
			throw new ActionNotSupportedFault(action);
		}
	}

	/**
	 * 
	 */
	private void initializationMetaDataHandlerFunctionality() {
//		//Load the metadata.refresh.interval from the properties file.
//		boolean refreshEnabled = false;
//		long refreshInterval =0; 
//		 try{
//		  refreshEnabled=Boolean.valueOf(System.getProperty("metadata.refresh.enabled"));
//		  refreshInterval=Long.valueOf(System.getProperty("metadata.refresh.interval"));
//		 }catch(NumberFormatException nfex){
//		 }
//		if(refreshInterval>60*1000*5){timeout = refreshInterval;}
//		
//		//launch a task to repeatedly/periodically launch the refreshMetaDataInformation method
//		final TimerTask ttask = new TimerTask() {
//		    public void run() {
//		        try {
//					metadata_Handler.refreshMetaDataInformation();
//					LOG.log(Level.FINEST, "Refreshing annotation details.");
//				} catch (ServletException e) {
//		        	LOG.log(Level.FINE, "An error occurred while refreshing annotation details:"+e.getMessage());
//					e.printStackTrace();
//				}
//		    }
//		};
//		
//		//launch timer that will die with the enclosing thread.
//		final Timer timeoutTimer = new Timer(true);
//		if(refreshEnabled){
//		  timeoutTimer.schedule(ttask,5000, timeout);
//		}
		
//		initializeAnnotationProcessing();
	}

	 private static synchronized void refreshMetaDataInformation() throws ServletException {
		//Find classes from classpath that implement Handler and have annotations
		//parse the classes list to update the list of metadata information
		//Each get will return the current state of the known metadata.
	       initializeAnnotationProcessing();		 
	 }

	/**
	 * @throws ServletException
	 */
	private static void populateHandlerList() throws ServletException {
		final InputStream ism = metadata_Handler.class.getResourceAsStream(ANNOTATED_LIST);
         if (ism != null) {
             final Properties props = new Properties();
             try {
                 props.load(ism);
             } catch (IOException iex) {
                 LOG.log(Level.WARNING, "Error reading properties from " + ANNOTATED_LIST, iex);
                 throw new ServletException(iex);
             }
             handlerList = new HashMap<String, String>();
             final Iterator<Entry<Object, Object>> ei = props.entrySet().iterator();
             while (ei.hasNext()) {
                 final Entry<Object, Object> entry = ei.next();
                 final Object key = entry.getKey();
                 final Object value = entry.getValue();
                 if (key instanceof String && value instanceof String) {
                     handlerList.put((String) key, (String) value);
                 }
             }
//             properties = Collections.unmodifiableMap(propertySet);
         }
	}
	
	public static String domToString(Node node) {
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
	
	private static XPath xpath = null;
	
	public class MetaDataIteratorFactory implements IteratorFactory{
		
		public final String RESOURCE_URI;
		protected MetaDataIteratorFactory(String resource) {
			RESOURCE_URI = resource;
		}
		public EnumerationIterator newIterator(HandlerContext context, Enumeration request, 
				DocumentBuilder db, boolean includeItem, 
				boolean includeEPR) 
		throws UnsupportedFeatureFault, FaultException {
			return new MetaDataIterator(context, RESOURCE_URI, 
					request, 
					db, includeItem, includeEPR);
		}
		
		public class MetaDataIterator implements EnumerationIterator {
			
			private java.util.Enumeration<Object> keys;
			private Management[] allMetadataSources;
			private final DocumentBuilder db;
			private final boolean includeEPR;
			private final String requestPath;
			private final String resourceURI;
			int iterCount = 0;
			
			public MetaDataIterator(final HandlerContext hcontext,
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
				  if((enx!=null)&&(enx.getWsmanFilter()!=null)){
					 DialectableMixedDataType type = enx.getWsmanFilter();
					 if(type.getContent()!=null){
						 String filterValue ="";
						 for(Object filterArg : type.getContent()){
							filterValue+= filterArg; 
						 }
						xpathFilterString = filterValue;
						if(xpath==null){
						   xpath = XPathFactory.newInstance().newXPath();
						}
					 }
				  }
				  allMetadataSources = filterMetadataList(xpathFilterString);
//System.out.println("@@@@ In Enum instance, filtered :"+allMetadataSources.length);				  
				}catch (Exception ex){
				  LOG.severe("There was an error filtering the Metatadata instances : "+ex.getMessage());
				  ex.printStackTrace();
//System.out.println("@@@@ In Enum instance, exception :"+ex.toString());				  
				  allMetadataSources = new Management[0];	
				}
			}

			private Management[] filterMetadataList(String xpathFilterString) throws SOAPException, 
				IOException, JAXBException, DatatypeConfigurationException {
				Management[] eventSrces = null;
				int indx=0;
				
//				for(Management m: metaDataValues){
////				for (Iterator iter = metaDataValues.iterator(); iter.hasNext();) {				
//System.out.println("Enum metaList:"+(indx++)+":"+m);				
////System.out.println("Enum metaList:"+(indx++)+":"+(Management)iter.next());				
//				}
				Management[] metaDataList = new Management[metaDataValues.size()];
							 metaDataList =	metaDataValues.toArray(metaDataList); 
			        ArrayList<Management> metaDataBag = new ArrayList<Management>();
			        for (int i = 0; i < metaDataList.length; i++) {
						Management metaDescription = metaDataList[i];
						//insert filter processing.
						if((xpathFilterString!=null)&&
							(xpathFilterString.trim().length()>0)){
							if(xpath==null){
							  xpath = XPathFactory.newInstance().newXPath();
							}
							NamespaceContext nsContext = new NameSpacer();
							xpath.setNamespaceContext(nsContext);
						  	try {
							  Object nodes = xpath.evaluate(xpathFilterString, 
								metaDescription.getEnvelope().getOwnerDocument(), 
								XPathConstants.BOOLEAN);
							  if(nodes!=null){
							   Boolean located = (Boolean) nodes;
							   if(located.booleanValue()){
								 metaDataBag.add(metaDescription);
							   }
							 }
							} catch (XPathExpressionException e) {
								e.printStackTrace();
							} 
						}else{
						  metaDataBag.add(metaDescription);
						}
					}
			        eventSrces = new Management[metaDataBag.size()];
			        if(metaDataBag.size()>0){
			        	System.arraycopy(metaDataBag.toArray(), 0, eventSrces, 0, 
			        	  metaDataBag.size());
			        }
				return eventSrces;
			}

			public EnumerationItem next() {

				// construct an item if necessary for the enumeration
				Element item = null;

				// Always include the item to allow filtering by EnumerationSupport
				final Document doc = db.newDocument();
				item = doc.createElementNS(Management.NS_URI, Management.NS_PREFIX);
				Management node =allMetadataSources[iterCount++];

				// construct an endpoint reference to accompany the element, if
				// needed
				EndpointReferenceType epr = null;
//				return new EnumerationItem(node.getEnvelope(), epr);
				if((node.getBody()!=null)&&(node.getBody().getFirstChild()!=null)){
//					return new EnumerationItem(node.getBody().getFirstChild(),epr);
					return new EnumerationItem(node.getEnvelope(),epr);
				}
				return new EnumerationItem(node.getEnvelope(), epr);
			}

			public boolean hasNext() {
				return (iterCount<allMetadataSources.length);
			}

			/**Needs to return true as we're doing filtering.
			 */
			public boolean isFiltered() {
				return true;
			}

			public void release() {
				allMetadataSources = new Management[0];
			}

			public int estimateTotalItems() {
				return allMetadataSources.length;
			}
		}//End of MetaDataIterator
	}
	
	class NameSpacer implements NamespaceContext{
		public String getNamespaceURI(String prefix) {
		   if ( prefix.equals( "wsmeta")) {
		      return "http://schemas.dmtf.org/wbem/wsman/1/wsman/version1.0.0.a/default-addressing-model.xsd";
		   } 
		   else if(prefix.equals("env")){
			   return "http://www.w3.org/2003/05/soap-envelope";
		   }
		   else if(prefix.equals("wsman")){
			   return "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";
		   }
		   else if(prefix.equals("mdo")){
			   return "http://schemas.wiseman.dev.java.net/metadata/messagetypes";
		   }
		 return "no-prefix-located";
		}

		public String getPrefix(String namespaceURI) {
		   if ( namespaceURI.equals( 
				   "http://schemas.dmtf.org/wbem/wsman/1/wsman/version1.0.0.a/default-addressing-model.xsd")) {
		      return "wsmeta";
		   } 
		   else if(namespaceURI.equals("http://www.w3.org/2003/05/soap-envelope")){
			   return "env";
		   }
		   else if(namespaceURI.equals("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd")){
			   return "wsman";
		   }
		   else if(namespaceURI.equals("http://schemas.wiseman.dev.java.net/metadata/messagetypes")){
			   return "mdo";
		   }
		 return "empty";
		}

		public Iterator getPrefixes(String namespaceURI) {
			return null;
		}
	}//End of NameSpacer

}
