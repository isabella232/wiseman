package com.sun.ws.management.server.handler.wsman;

import java.io.IOException;
import java.io.InputStream;
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
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.ObjectFactory;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementEnumerationAnnotation;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XmlBinding;

/** This handler is meant to expose metaData information for annotated
 *  handlers.   
 * 
 * @author Simeon
 *
 */
public class metadata_Handler implements Handler {
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
	private static Vector<Management> metaDataValues = new Vector<Management>();
	private static XmlBinding binding = null;
	
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
            	LOG.log(Level.FINE, "The class '"+className+"' loaded from "+
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
			
	        //iterate through all of the metaData containers(Management)
	        for (Iterator iter = metaDataValues.iterator(); iter.hasNext();) {
	        	
				Management element = (Management) iter.next();
				AnnotationProcessor.populateMetaDataElement(metaElement, element);
				    
			}
	
	        try {
			  binding.marshal(metaElement, responseDoc );
			} catch (Exception e) {
				LOG.log(Level.SEVERE,"Error marshalling content :"+e.getMessage());
				  System.out.println("Exception:"+e.getMessage());
				throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
			} 	         

	        response.getBody().addDocument(responseDoc);
		}
		else if(com.sun.ws.management.mex.Metadata.METADATA_ACTION_URI.equals(action)){
			//TODO: implement
		}
		else{
			throw new ActionNotSupportedFault(action);
		}
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
}
