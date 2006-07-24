
package wsman.traffic.lightfactory;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.sun.traffic.light.model.TrafficLightModel;
import com.sun.traffic.light.types.TrafficLightType;
import com.sun.traffic.light.ui.TrafficLight;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.xml.XmlBinding;

/**
 * LightfactoryHandler deligate is responsible for processing WS-Transfer actions.
 * @GENERATED
 */
public class LightfactoryHandler extends TransferSupport
{
    //Log for logging messages
    private Logger m_log = Logger.getLogger(LightfactoryHandler.class.getName());

	/*************************** Implementation  Specific **************************/
	public static final String RESOURCE_URI="wsman:traffic/light";

	private XmlBinding binding;
    private static final String TRAFFIC_LIGHT_JAXB_PACKAGE="com.sun.traffic.light.types";

	public LightfactoryHandler() {
		super();
	    try {
			binding=new XmlBinding(null,TRAFFIC_LIGHT_JAXB_PACKAGE);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}
	/******************************************************************************/

     public void Create( String resource, Management request, Management response ){
 	/*************************** Implementation  ***********************************/
		// Create and store a reference to this object in our mini-model
       TrafficLight tl = TrafficLightModel.getModel().create();

       // Transfer the state from the soap body to the model
       try {
           copyStateFromRequestToModel(request, tl);
       } catch (InvalidRepresentationFault e) {
			// Its ok to let this go because
       	// We are not requireing inital values
       	// Traffic lights are created with a valid
       	// internal state. Inital settings are optional
		}

		// Define a selector (in this case name)
		HashMap<String, String> selectors = new HashMap<String,String>();
		selectors.put("name",tl.getName());
		try {
			appendCreateResponse(response, RESOURCE_URI,selectors);
		} catch (Exception e) {
			m_log.log(Level.SEVERE,"An error occured during the construction of a response to a create request.",e);
			throw new InternalErrorFault();
		}
		/******************************************************************************/
  }

	/*************************** Implementation  ***********************************/
	void copyStateFromRequestToModel(Management request, TrafficLight light) {
	    if(light==null)
	    	throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.AMBIGUOUS_SELECTORS);

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
			} else {
				m_log.log(Level.INFO,"The body of your request is empty but it is optional." );
				throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.MISSING_VALUES);
			}
	 }
	 /******************************************************************************/

}
