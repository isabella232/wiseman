
package net.java.dev.wiseman.resources.traffic._1.light;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.Management;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;

import java.util.logging.Logger;

import net.java.dev.wiseman.traffic.light.impl.LightHandlerImpl;


/**
 * LightHandler delegate is responsible for processing enumeration actions.
 *
 * @GENERATED
 */
public class LightHandler extends EnumerationHandler {

    public static final String resourceURI = "urn:resources.wiseman.dev.java.net/traffic/1/light";
       
    // Log for logging messages
    @SuppressWarnings("unused")
    private final Logger log;

    public LightHandler() {
        super();
        
        // Initialize our member variables
        log = Logger.getLogger(LightHandler.class.getName());
        try {
            // Register the IteratorFactory with EnumerationSupport
            EnumerationSupport.registerIteratorFactory("urn:resources.wiseman.dev.java.net/traffic/1/light",
                                                       new LightIteratorFactory("urn:resources.wiseman.dev.java.net/traffic/1/light"));
        } catch (Exception e) {
            throw new InternalErrorFault(e);
        }
    }

    public void EnumerateOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse ) {
        super.enumerate( context, enuRequest, enuResponse);
    }    

    public void Create(HandlerContext context, Management request, Management response ) {
     	// Added an implementation from another class since this code gets re-generated often 
		LightHandlerImpl.create(request, response, log);
    }   

    public void Get(HandlerContext context, Management request, Management response ) {
     	// Added an implementation from another class since this code gets re-generated often 
		LightHandlerImpl.get(request, response, log);
    }   

    public void Put(HandlerContext context, Management request, Management response ) {
     	// Added an implementation from another class since this code gets re-generated often 
		LightHandlerImpl.put(request, response, log);
    }   

    public void ReleaseOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse ) {
        super.release( context, enuRequest, enuResponse);
    }    

    public void PullOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse ) {
        super.pull( context, enuRequest, enuResponse);
    }    

    public void GetStatusOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse ) {
        super.getStatus( context, enuRequest, enuResponse);
    }    

    public void RenewOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse ) {
        super.renew( context, enuRequest, enuResponse);
    }    

    public void Delete(HandlerContext context, Management request, Management response ) {
     	// Added an implementation from another class since this code gets re-generated often 
		LightHandlerImpl.delete(request, response, log);
    }   
}