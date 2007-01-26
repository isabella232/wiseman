
package wsman.traffic.resource;

import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.Management;
import com.sun.ws.management.server.HandlerContext;

import com.sun.traffic.light.types.ObjectFactory;

import java.util.logging.Logger;

/**
 * ResourceHandler deligate is responsible for processing WS-Transfer actions.
 * @GENERATED
 */
public class ResourceHandler extends TransferSupport
{

    // Log for logging messages
    @SuppressWarnings("unused")
    private final Logger log;
    private final ObjectFactory resourceFactory;
    
    public static final org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory addressingFactory =
                         new org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory();
    public static final org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory managementFactory =
                         new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
    
    public ResourceHandler() {
        super();
        
        // Initialize our member variables
        log = Logger.getLogger(ResourceHandler.class.getName());
        resourceFactory = new ObjectFactory();
    }
    

     public void Get( HandlerContext context, Management request, Management response )
     {
    	 LightHandlerImpl.get(request,response,log,response.getXmlBinding());
     }

     public void Put( HandlerContext context, Management request, Management response )
     {
    	 LightHandlerImpl.put(request,log,response.getXmlBinding());
     }

     public void Delete( HandlerContext context, Management request, Management response )
     {
    	 LightHandlerImpl.delete(request,log);
     }

     public void CustomOp( HandlerContext context, Management request, Management response )
     {
         //TODO IMPLEMENT CUSTOM OPERATION
     }
}
