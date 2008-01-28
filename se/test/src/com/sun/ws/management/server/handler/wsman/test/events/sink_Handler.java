package com.sun.ws.management.server.handler.wsman.test.events;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Element;

import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.InvalidSubscriptionException;
import com.sun.ws.management.framework.eventing.WSEventingSinkHandler;
import com.sun.ws.management.server.ContextListener;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.WSEnumerationSupport;
import com.sun.ws.management.server.WSEventingSupport;
import com.sun.ws.management.server.message.WSManagementRequest;
import com.sun.ws.management.server.message.WSManagementResponse;

public class sink_Handler extends WSEventingSinkHandler {
	
	private static final Logger LOG = Logger.getLogger(sink_Handler.class.getName());
	
	static final class SubscriptionHandler implements ContextListener {
		
		private List<UUID> subscribers = new ArrayList<UUID>();

		public synchronized void contextBound(HandlerContext requestContext, UUID context) {
			LOG.info("Context bound: " + context.toString());
			subscribers.add(context);
		}

		public synchronized void contextUnbound(HandlerContext requestContext, UUID context) {
			LOG.info("Context unbound: " + context.toString());
			subscribers.remove(context);	
		}
		
		synchronized void forwardEvent(final WSManagementRequest request) {
			
			// Forward the message as an event on to any subscribers.
			LOG.info("Forwarding event to subscribers: " + subscribers.size());
			final Iterator<UUID> iter = subscribers.iterator();
			while (iter.hasNext()) {
				final UUID uuid = iter.next();
				try {
					LOG.info("Calling sendEvent() for UUID: " + uuid.toString());
					WSEventingSupport.sendEvent(uuid, request.getPayload(null));
				} catch (SOAPException e) {
					LOG.log(Level.SEVERE, "Unexpected exception: ", e);
					subscribers.remove(uuid);
				} catch (JAXBException e) {
					LOG.log(Level.SEVERE, "Unexpected exception: ", e);
					subscribers.remove(uuid);
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Unexpected exception: ", e);
					subscribers.remove(uuid);
				} catch (InvalidSubscriptionException e) {
					LOG.log(Level.SEVERE, "Unexpected exception: ", e);
					subscribers.remove(uuid);
				} catch (XMLStreamException e) {
					LOG.log(Level.SEVERE, "Unexpected exception: ", e);
					subscribers.remove(uuid);
				}
			}
		}
	}
	
	final SubscriptionHandler subscriptionHandler = new SubscriptionHandler();

	public boolean handleEvents(final String resource,
			final HandlerContext context,
			final WSManagementRequest request,
			final WSManagementResponse response) {

		try {
			final String action = request.getActionURI().toString();
			LOG.info("Event action: " + action);
			if (Eventing.SUBSCRIBE_ACTION_URI.equals(action)) {
				response.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
				WSEventingSupport.subscribe(context,
						request, response, false, 1024, subscriptionHandler, null);
			} else if (Eventing.RENEW_ACTION_URI.equals(action)) {
				response.setAction(Eventing.RENEW_RESPONSE_URI);
				WSEventingSupport.renew(context, request, response);
			} else if (Eventing.UNSUBSCRIBE_ACTION_URI.equals(action)) {
				response.setAction(Eventing.UNSUBSCRIBE_RESPONSE_URI);
				WSEventingSupport.unsubscribe(context, request, response);
			} else if (Enumeration.PULL_ACTION_URI.equals(action)) {
				response.setAction(Enumeration.PULL_RESPONSE_URI);
	            WSEnumerationSupport.pull(context, request, response);
	        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
	        	response.setAction(Enumeration.RELEASE_RESPONSE_URI);
	            WSEnumerationSupport.release(context, request, response);
	        } else {
	        	LOG.info("Event received.");
				// Treat everything else as an event. Forward it on to any subscribers.
				subscriptionHandler.forwardEvent(request);
				LOG.info("Event forwarded.");
			}
			// TODO: Fix exception handling
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "Unexpected exception: ", e);
			return false;
		} catch (SOAPException e) {
			LOG.log(Level.SEVERE, "Unexpected exception: ", e);
			return false;
		} catch (URISyntaxException e) {
			LOG.log(Level.SEVERE, "Unexpected exception: ", e);
			return false;
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Unexpected exception: ", e);
			return false;
		}
		return true;
	}
}
