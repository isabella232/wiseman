
package wsman.traffic.factory;

import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.Management;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.ws.management.InternalErrorFault;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

/**
 * FactoryHandler deligate is responsible for processing WS-Transfer actions.
 * @GENERATED
 */
public class FactoryHandler extends TransferSupport
{
    //Log for logging messages
    private Logger log = Logger.getLogger(FactoryHandler.class.getName());

	private static XmlBinding binding;
	private static final String RESOURCE_JAXB_PACKAGE = "com.sun.traffic.light.types";
	{
		try {
			binding = new XmlBinding(null,RESOURCE_JAXB_PACKAGE);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}
	
	// These were pre-generated but not needed in this example
	//private static final com.sun.traffic.light.types.ObjectFactory resourceFactory = new com.sun.traffic.light.types.ObjectFactory();
    //public static final org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory addressingFactory = new org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory();
    //public static final org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory managementFactory = new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();

	// Added this constant
	private static final String RESOURCE_URI = "wsman:traffic/resource";

     public void Create( String resource, Management request, Management response )
     {
    	// Added an implementation from another class since this code gets re-generated often 
        HashMap<String, String> selectors = LightfactoryHandlerImpl.createLight(request, binding);
 		try {
			appendCreateResponse(response, RESOURCE_URI,selectors);
		} catch (Exception e) {
			log.log(Level.SEVERE,"An error occured during the construction of a response to a create request.",e);
			throw new InternalErrorFault();
		}
     }
}
