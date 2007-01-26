
package wsman.traffic.factory;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.traffic.light.types.ObjectFactory;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.server.HandlerContext;

/**
 * FactoryHandler deligate is responsible for processing WS-Transfer actions.
 * @GENERATED
 */
public class FactoryHandler extends TransferSupport
{

    // Log for logging messages
    @SuppressWarnings("unused")
    private final Logger log;
    private final ObjectFactory resourceFactory;
    
    public static final org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory addressingFactory =
                         new org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory();
    public static final org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory managementFactory =
                         new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
    
    public FactoryHandler() {
        super();
        
        // Initialize our member variables
        log = Logger.getLogger(FactoryHandler.class.getName());
        resourceFactory = new ObjectFactory();
    } 
     public void Create( HandlerContext context, Management request, Management response ) {
     	// Added an implementation from another class since this code gets re-generated often 
         HashMap<String, String> selectors = LightfactoryHandlerImpl.createLight(request, response.getXmlBinding());
  		try {
 			appendCreateResponse(response, request.getResourceURI(), selectors);
 		} catch (Exception e) {
 			log.log(Level.SEVERE,"An error occured during the construction of a response to a create request.",e);
 			throw new InternalErrorFault();
 		}
     }
}
