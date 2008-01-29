package com.sun.ws.management.framework.eventing;

import java.util.logging.Logger;

import com.sun.ws.management.Management;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.WSHandler;
import com.sun.ws.management.server.message.WSManagementRequest;
import com.sun.ws.management.server.message.WSManagementResponse;

public class WSEventingSinkHandler implements WSHandler {
	
	private static final Logger LOG = Logger.getLogger(WSHandler.class.getName());
	
    public void handle(final String action,
    		           final String resource,
                       final HandlerContext context,
                       final WSManagementRequest request,
                       final WSManagementResponse response) throws Exception {
        
        if (Management.DROPPED_EVENTS_URI.equals(action)) {
        	LOG.fine("Dropped Event notification received.");
        	handleDroppedEvents(resource, context, request, response);
        } else {
        	LOG.fine("Calling handle events.");
        	handleEvents(resource, context, request, response);
        }
        
    }

	public void handleDroppedEvents(final String resource,
			                        final HandlerContext context,
			                        final WSManagementRequest request,
			                        final WSManagementResponse response) throws Exception {
		// TODO: Override this method
		LOG.fine("Dropped events notification received.");
	}
	
	public void handleEvents(final String resource,
			                    final HandlerContext context,
			                    final WSManagementRequest request,
			                    final WSManagementResponse response) throws Exception {
		// TODO: Override this method
		LOG.fine("Event notification received.");
        if (request.isAckRequested() == false) {
        	// TODO: Don't send the SOAP response.
        } else {
        	response.setAction(Management.ACK_URI);
        	// MessageID, RelatesTo & To are set by the Wiseman servlet or JAX-WS
        }
	}
}
