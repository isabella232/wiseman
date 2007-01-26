
package wsman.traffic.list;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;

import java.util.logging.Logger;

/**
 * ListHandler deligate is responsible for processing enumeration actions.
 *
 * @GENERATED
 */
public class ListHandler extends EnumerationHandler {

    public static final String resourceURI = "wsman:traffic/list";
       
    // Log for logging messages
    @SuppressWarnings("unused")
    private final Logger log;

    public ListHandler() {
        super();
        
        // Initialize our member variables
        log = Logger.getLogger(ListHandler.class.getName());
        try {
            EnumerationSupport.registerIteratorFactory("wsman:traffic/list",
                                                       new ListIteratorFactory("wsman:traffic/list"));
        } catch (Exception e) {
			throw new InternalErrorFault(e);
		}
    }

     public void EnumerateOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse )
     {
         enumerate( context, enuRequest, enuResponse);
     }

     public void ReleaseOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse )
     {
         release( context, enuRequest, enuResponse);
     }

     public void PullOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse )
     {
         pull( context, enuRequest, enuResponse);
     }

     public void GetStatusOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse )
     {
         getStatus( context, enuRequest, enuResponse);
     }

     public void RenewOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse )
     {
         renew( context, enuRequest, enuResponse);
     }
}