package wsman.traffic.factory;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.publicworks.light.model.TrafficLightModel;
import org.publicworks.light.model.ui.TrafficLight;

import com.sun.traffic.light.types.TrafficLightType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.xml.XmlBinding;

public class LightfactoryHandlerImpl {
    private static Logger log = Logger.getLogger(LightfactoryHandlerImpl.class.getName());

    /*************************** Implementation  ***********************************/
	 static HashMap<String, String> createLight(Management request,XmlBinding binding) {
	    
			// Create and store a reference to this object in our mini-model
		 TrafficLight light = TrafficLightModel.getModel().create();

		 if(light==null)
	    	throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.AMBIGUOUS_SELECTORS);

		    // Get JAXB Representation of Soap Body property document
			if(request.getBody().getFirstChild()!=null){
		        JAXBElement<TrafficLightType> tlElement;
				try {
					tlElement = (JAXBElement<TrafficLightType>)binding.unmarshal(request.getBody().getFirstChild());
				} catch (JAXBException e) {
					log.log(Level.SEVERE,"The body of this request did not conform to its schema.",e );
					throw new InternalErrorFault();
				}
				TrafficLightType tlType = tlElement.getValue();
		
				// Transfer values
				light.setName(tlType.getName());
				light.setColor(tlType.getColor());
				light.setX(tlType.getX());
				light.setY(tlType.getY());
			} else {
				log.log(Level.INFO,"The body of your request is empty but it is optional." );
				//throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.MISSING_VALUES);
			}
			
			// Define a selector (in this case name)
			HashMap<String, String> selectors = new HashMap<String,String>();
			selectors.put("name",light.getName());

			return selectors;
	 }
	 /******************************************************************************/

}
