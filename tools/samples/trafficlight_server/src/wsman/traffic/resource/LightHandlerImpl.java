package wsman.traffic.resource;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.publicworks.light.model.TrafficLightModel;
import org.publicworks.light.model.ui.TrafficLight;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.traffic.light.types.ObjectFactory;
import com.sun.traffic.light.types.TrafficLightType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.framework.Utilities;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XmlBinding;


public class LightHandlerImpl {

	private static final ObjectFactory trafficLightFactory = new ObjectFactory();

	static void put(Management request, Logger log,XmlBinding binding)  {
 	    // Use name selector to find the right light
  		String name;
		try {
			name = getNameSelector(request);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		}
  		TrafficLight light = TrafficLightModel.getModel().find(name);
        copyStateFromRequestToModel(request, light,binding);
	}

	static void get(Management request, Management response, Logger log, XmlBinding binding)  {
		// Use name selector to find the right light in the model
		String name=null;
		try {
		name = getNameSelector(request);
		TrafficLight light = TrafficLightModel.getModel().find(name);
		if (light == null) {
			log.info("An attempt was made to get a resource that did not exist called "
							+ name);
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}
		TrafficLightType tlType = createLightType(light);

		// Convert JABX to an element and copy it to the SOAP
		// Response Body
			Document soapBodyDoc = Management.newDocument();

			try {
				binding.marshal(trafficLightFactory.createTrafficlight(tlType),
						soapBodyDoc);
			} catch (Exception e) {
				final String explanation = 
					 "XML Binding marshall failed for JAXBElement<TrafficLightType>. " 
					+ "Ensure 'binding.properties' file is correctly set on server.";
				log.log(Level.SEVERE, explanation, e);
				throw new InternalErrorFault(SOAP.createFaultDetail(explanation, null, e, null));
			}
			response.getBody().addDocument(soapBodyDoc);
		} catch (JAXBException e) {
			log.log(Level.SEVERE,
					"An error occured while creating a traffic light SOAP body for the "
							+ name + " resource", e);
			throw new InternalErrorFault(e);
		} catch (SOAPException e) {
			log.log(Level.SEVERE,
					"An error occured while creating a traffic light SOAP body for the "
							+ name + " resource", e);
			throw new InternalErrorFault(e);
		}
	}

    static void delete(Management request, Logger log) {
  	    // Use name selector to find the right light
    	String name = null;
    	try{
    		name = getNameSelector(request);
	} catch (JAXBException e) {
		throw new InternalErrorFault(e);
	} catch (SOAPException e) {
		throw new InternalErrorFault(e);
	}
          TrafficLight light = TrafficLightModel.getModel().find(name);
          if(light==null){
        	log.log(Level.WARNING,"An attempt was made to delete a resource that did not exist called "+name);
          	throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
          }
          // Remove it from the list and then remove the actual GUI instance.
          // GC can do the rest.
          TrafficLightModel.getModel().destroy(name);

    }

    private static String getNameSelector(Management request) throws JAXBException, SOAPException  {
		Set<SelectorType> selectors = request.getSelectors();
		if(Utilities.getSelectorByName("name",selectors)==null){
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}
		return (String)Utilities.getSelectorByName("name",selectors).getContent().get(0);
    }
    
 	static private void copyStateFromRequestToModel(Management request, TrafficLight light, XmlBinding binding) {
  	    if(light==null)
  	    	throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);

  		    // Get JAXB Representation of Soap Body property document
  			if(request.getBody().getFirstChild()!=null){
  		        JAXBElement<TrafficLightType> tlElement;
  				try {
  					tlElement = (JAXBElement<TrafficLightType>)binding.unmarshal(request.getBody().getFirstChild());
  				} catch (JAXBException e) {
  					final String explanation = 
  						 "XML Binding unmarshall failed for JAXBElement<TrafficLightType>. " 
  						+ "Ensure 'binding.properties' file is correctly set on server.";
  					throw new InternalErrorFault(SOAP.createFaultDetail(explanation, null, e, null));
  				}
  				TrafficLightType tlType = tlElement.getValue();
  		
  				// Transfer values
  				light.setName(tlType.getName());
  				light.setColor(tlType.getColor());
  				light.setX(tlType.getX());
  				light.setY(tlType.getY());
  			} 
  	 }

		
		private static TrafficLightType createLightType(TrafficLight light) {
			// Create a new, empty JAXB TrafficLight Type
			TrafficLightType tlType = trafficLightFactory.createTrafficLightType();

			// Transfer State from model to JAXB Type
			tlType.setName(light.getName());
			tlType.setColor(light.getColor());
			tlType.setX(light.getX());
			tlType.setY(light.getY());
			return tlType;
		}
		
		public static JAXBElement<TrafficLightType> createLight(TrafficLight light) throws JAXBException {
			TrafficLightType lightType=createLightType(light);
			return trafficLightFactory.createTrafficlight(lightType);
		}
		
		public static Element createLightElement(TrafficLight light,XmlBinding binding) throws JAXBException {
			TrafficLightType lightType=createLightType(light);
			Document doc = Management.newDocument();
			binding.marshal(trafficLightFactory.createTrafficlight(lightType),
					doc);
			return (Element)doc.getFirstChild();
		}

}
