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
        
        if (request.isAckRequested() == false) {
        	// TODO: Don't send the SOAP response.
        }
    }

	public boolean handleDroppedEvents(final String resource,
			                        final HandlerContext context,
			                        final WSManagementRequest request,
			                        final WSManagementResponse response) {
		// TODO: Override this method
		return true;
	}
	
	public boolean handleEvents(final String resource,
			                    final HandlerContext context,
			                    final WSManagementRequest request,
			                    final WSManagementResponse response) {
		// TODO: Override this method
		LOG.fine("Event notification received.");
		return true;
	}
}
