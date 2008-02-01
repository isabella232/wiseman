package com.sun.ws.management.server.handler.wsman.test.events;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.ws.management.Management;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.server.ContextListener;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.WSEnumerationSupport;
import com.sun.ws.management.server.WSEventingSupport;
import com.sun.ws.management.server.WSHandler;
import com.sun.ws.management.server.message.WSManagementRequest;
import com.sun.ws.management.server.message.WSManagementResponse;

public class source_Handler implements WSHandler {
	
    private static final Logger LOG = Logger.getLogger(source_Handler.class.getName());
	private static final String NS_URI = "https://wiseman.dev.java.net/test/events/source";
	private static final String NS_PREFIX = "evtt";
	private static DatatypeFactory datatypeFactory = null;
	
    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch(Exception ex) {
            throw new RuntimeException("Fail to initialize source_Handler " + ex);
        }
    }
	
	public static final String RESOURCE_URI = "wsman:test/events/source";
	public static final String CREATE_EVENT = "wsman:test/events/source/createEvent";
	
	static final class SubscriptionHandler implements ContextListener {
		
		private final List<UUID> subscribers = new ArrayList<UUID>();

		public void contextBound(HandlerContext requestContext,
				UUID context) {
			LOG.info("Context bound: " + context.toString());
			synchronized (subscribers) {
				subscribers.add(context);
			}
			try {
				byte[] username = "wsman".getBytes("UTF8");
				byte[] password = "secret".getBytes("UTF8");
				WSEventingSupport.setCredentials(context, username, password);
			} catch (UnsupportedEncodingException e) {
				LOG.log(Level.WARNING,
						"Could not set credentials due to UnsupportedEncodingException");
			}
		}

		public void contextUnbound(HandlerContext requestContext, UUID context) {
			LOG.info("Context unbound: " + context.toString());
			synchronized (subscribers) {
				subscribers.remove(context);
			}
		}
		
		void forwardEvent(final WSManagementRequest request) {
			
			// Forward the message as an event on to any subscribers.
			final UUID[] list;
			synchronized (subscribers) {
				list = new UUID[subscribers.size()];
				subscribers.toArray(list);
			}
			LOG.info("Forwarding event to source subscribers: " + list.length);
			for (int i = 0; i < list.length; i++) {
				final UUID uuid = list[i];
				try {
		    		final GregorianCalendar now = new GregorianCalendar();
		    		final XMLGregorianCalendar timestamp = datatypeFactory.newXMLGregorianCalendar(now);
					final Document event = createEvent(timestamp.toString());
					LOG.info("Calling sendEvent() for UUID: " + uuid.toString());
					WSEventingSupport.sendEvent(uuid, event);
				} catch (Exception e) {
					LOG.log(Level.SEVERE, "Unexpected exception: ", e);
                    // Ignore the error. 
					// WSEventingSupport will remove the subscriber automatically.
				}
			}
		}
		
		private Document createEvent(final String value) {
			Document doc = Management.newDocument();
			Element item = doc.createElementNS(NS_URI, NS_PREFIX + ":event");
			item.setTextContent(value);
			doc.appendChild(item);
			return doc;
		}
	}
	
	// Needs to be static or we may lose our subscriptions
	static final SubscriptionHandler subscriptionHandler = new SubscriptionHandler();

	public void handle(final String action,
			final String resource,
			final HandlerContext context,
			final WSManagementRequest request,
			final WSManagementResponse response) throws Exception {

		try {
			LOG.info("Action: " + action);
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
	        } else if (CREATE_EVENT.equals(action)) {
				// Send an event to the subscribers
				subscriptionHandler.forwardEvent(request);
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Unexpected exception: ", e);
			// SOAP Fault message will be automatically built by the Wiseman framework.
			throw e;
		}
	}
}
