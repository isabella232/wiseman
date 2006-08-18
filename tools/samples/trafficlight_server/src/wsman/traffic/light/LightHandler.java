package wsman.traffic.light;

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
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.xml.XmlBinding;


/**
 * LightHandler deligate is responsible for processing WS-Transfer actions.
 * 
 * @GENERATED
 */
public class LightHandler extends TransferSupport {
	// Log for logging messages
	private Logger m_log = Logger.getLogger(LightHandler.class.getName());

	/**************************** Implementation Specific ***************************/
	private static XmlBinding binding;
	{
		try {
			binding = new XmlBinding(null,TRAFFIC_LIGHT_JAXB_PACKAGE);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}
	private static final String TRAFFIC_LIGHT_JAXB_PACKAGE = "com.sun.traffic.light.types";

	private static final ObjectFactory trafficLightFactory = new ObjectFactory();

	public LightHandler() {
		super();
	}

	/*******************************************************************************/

	public void Get(String resource, Management request, Management response) {
		/**************************** Implementation ************************************/
		// Use name selector to find the right light in the model
		String name = getNameSelector(request);
		TrafficLight light = TrafficLightModel.getModel().find(name);
		if (light == null) {
			m_log
					.info("An attempt was made to get a resource that did not exist called "
							+ name);
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}
		TrafficLightType tlType = createLightType(light);

		// Convert JABX to an element and copy it to the SOAP
		// Response Body
		try {
			Document soapBodyDoc = Management.newDocument();
			binding.marshal(trafficLightFactory.createTrafficlight(tlType),
					soapBodyDoc);
			response.getBody().addDocument(soapBodyDoc);
		} catch (JAXBException e) {
			m_log.log(Level.SEVERE,
					"An error occured while creating a traffic light SOAP body for the "
							+ name + " resource", e);
			throw new InternalErrorFault();
		} catch (SOAPException e) {
			m_log.log(Level.SEVERE,
					"An error occured while creating a traffic light SOAP body for the "
							+ name + " resource", e);
			throw new InternalErrorFault();
		}
		/*******************************************************************************/

	}

	public static TrafficLightType createLightType(TrafficLight light) {
		// Create a new, empty JAXB TrafficLight Type
		TrafficLightType tlType = trafficLightFactory.createTrafficLightType();

		// Transfer State from model to JAXB Type
		tlType.setName(light.getName());
		tlType.setColor(light.getColor());
		tlType.setX(light.getX());
		tlType.setY(light.getY());
		return tlType;
	}
	public static Element createLightElement(TrafficLightType light) throws JAXBException {
		Document doc = Management.newDocument();
		binding.marshal(trafficLightFactory.createTrafficlight(light),
				doc);
		return (Element)doc.getFirstChild();
	}
	public void Put(String resource, Management request, Management response) {
    	/*************************** Implementation  ***********************************/
 	    // Use name selector to find the right light
  		String name = getNameSelector(request);
  		TrafficLight light = TrafficLightModel.getModel().find(name);
        copyStateFromRequestToModel(request, light);
      	/******************************************************************************/
	}

	public void Delete(String resource, Management request, Management response) {
		/*************************** Implementation  ***********************************/
  	    // Use name selector to find the right light
  		String name = getNameSelector(request);
          TrafficLight light = TrafficLightModel.getModel().find(name);
          if(light==null){
        	  m_log.log(Level.WARNING,"An attempt was made to delete a resource that did not exist called "+name);
          	throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
          }
          // Remove it from the list and then remove the actual GUI instance.
          // GC can do the rest.
          TrafficLightModel.getModel().destroy(name);
       	/******************************************************************************/
	}

    public void TerminateOp( String resource, Management request, Management response )
    {
   	 // this is, of course, a bad idea
        System.exit(0);
   }
	/*************************** Implementation  ***********************************/
    private String getNameSelector(Management request)  {
		Set<SelectorType> selectors;
		try {
			selectors = request.getSelectors();
		} catch (JAXBException e) {
			m_log.log(Level.WARNING,"A name selector could not be found inside this request",e );
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		} catch (SOAPException e) {
			m_log.log(Level.WARNING,"A name selector could not be found inside this request",e );
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}
		if(getSelectorByName("name",selectors)==null){
			m_log.log(Level.WARNING,"A name selector could not be found inside this request");
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}

		return (String)getSelectorByName("name",selectors).getContent().get(0);
    }
 	/*************************** Implementation  ***********************************/
 	void copyStateFromRequestToModel(Management request, TrafficLight light) {
  	    if(light==null)
  	    	throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);

  		    // Get JAXB Representation of Soap Body property document
  			if(request.getBody().getFirstChild()!=null){
  		        JAXBElement<TrafficLightType> tlElement;
  				try {
  					tlElement = (JAXBElement<TrafficLightType>)binding.unmarshal(request.getBody().getFirstChild());
  				} catch (JAXBException e) {
  					m_log.log(Level.SEVERE,"The body of this request did not conform to its schema.",e );
  					throw new InternalErrorFault();
  				}
  				TrafficLightType tlType = tlElement.getValue();
  		
  				// Transfer values
  				light.setName(tlType.getName());
  				light.setColor(tlType.getColor());
  				light.setX(tlType.getX());
  				light.setY(tlType.getY());
  			} 
  	 }
  	 /******************************************************************************/
	   public SelectorType getSelectorByName(String name,Set<SelectorType> selectorSet){
	    	for (SelectorType selectorType : selectorSet) {
	    		if(selectorType.getName().equals(name)){
	    			return selectorType;
	    		}
			}
			return null;
		}

}
