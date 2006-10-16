
package wsman.traffic.resource;

import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.xml.XmlBinding;

/**
 * ResourceHandler deligate is responsible for processing WS-Transfer actions.
 * @GENERATED
 */
public class ResourceHandler extends TransferSupport
{
    //Log for logging messages
    private Logger log = Logger.getLogger(ResourceHandler.class.getName());

	private static XmlBinding binding;
	private static final String RESOURCE_JAXB_PACKAGE = "com.sun.traffic.light.types";
	{
		try {
			binding = new XmlBinding(null,RESOURCE_JAXB_PACKAGE);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}
	// These are generated but not needed
	//private static final com.sun.traffic.light.types.ObjectFactory resourceFactory = new com.sun.traffic.light.types.ObjectFactory();
    //public static final org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory addressingFactory = new org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory();
    //public static final org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory managementFactory = new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();



     public void Get( HandlerContext context, Management request, Management response )
     {
     	 LightHandlerImpl.get(request,response,log,binding);
     }

     public void Put( HandlerContext context, Management request, Management response )
     {
     	 LightHandlerImpl.put(request,log,binding);
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
