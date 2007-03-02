/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: pull_source_Handler.java,v 1.15 2007-03-02 16:12:28 denis_rachal Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.eventing.DeliveryModeRequestedUnavailableFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.server.ContextListener;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.EventingSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;

public class pull_source_Handler implements Handler {

	private static final String NS_URI = "https://wiseman.dev.java.net/test/events/pull";
	private static final String NS_PREFIX = "log";
	
	private final Timer eventTimer;
	private final AddEventsTask addEventsTask;
	private int i = 0;
    
	final class AddEventsTask extends TimerTask implements ContextListener {
		
		private final ArrayList<UUID> subscriptions;
		
		AddEventsTask() {
			this.subscriptions = new ArrayList<UUID>();
		}

		@Override
		public void run() {

			synchronized (subscriptions) {
				UUID id;
				Iterator iter = this.subscriptions.iterator();
				while (iter.hasNext()) {
					id = (UUID) iter.next();
					// Create 1 new events
					try {
						switch (i) {
						case 0: EventingSupport.sendEvent(id, 
								createEvent("event1", "critical"));
						case 1: EventingSupport.sendEvent(id,
								createEvent("event2", "warning"));
						case 2: EventingSupport.sendEvent(id,
								createEvent("event3", "info"));
						case 3: EventingSupport.sendEvent(id,
								createEvent("event4", "debug"));
						}
						i++;
						if (i >=3) i = 0;
					} catch (Exception e) {
						// TODO: end subscription
					}
				}
			}
		}

		private Element createEvent(final String key, final String value) {
			Document doc = Management.newDocument();
			Element item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + key);
			item.setTextContent(value);
			doc.appendChild(item);
			return item;
		}

		public void contextBound(HandlerContext requestContext, UUID context) {
			// Add the context to our list of subscribers
            synchronized(subscriptions) {
            	subscriptions.add(context);
            }
		}

		public void contextUnbound(HandlerContext requestContext, UUID context) {
			// Remove the context from our list of subscribers
            synchronized(subscriptions) {
            	subscriptions.remove(context);
            }	
		}
	}
	
	public pull_source_Handler() {
		
		// Schedule a task to add data every 5 seconds to any subscriber
        final long REPEAT = 2000;
        eventTimer = new Timer(true);
        addEventsTask = new AddEventsTask();
        eventTimer.schedule(addEventsTask, 0, REPEAT);	
	}

    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {
        
        final EnumerationExtensions enuRequest = new EnumerationExtensions(request);
        final EnumerationExtensions enuResponse = new EnumerationExtensions(response);
        
        if (Eventing.SUBSCRIBE_ACTION_URI.equals(action)) {
            final EventingExtensions evtx = new EventingExtensions(request);
            final EventingExtensions evtxResponse = new EventingExtensions(response);
            final Subscribe subscribe = evtx.getSubscribe();
            final DeliveryType deliveryType = subscribe.getDelivery();
            if (EventingExtensions.PULL_DELIVERY_MODE.equals(deliveryType.getMode())) {
                enuResponse.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
                EventingSupport.subscribe(context, evtx, evtxResponse, false, 4, addEventsTask);
    			Document doc = Management.newDocument();
    			Element item1 = doc.createElementNS(NS_URI, NS_PREFIX + ":" + "param1");
    			item1.setTextContent("Custom parameter 1");
    			Element item2 = doc.createElementNS(NS_URI, NS_PREFIX + ":" + "param2");
    			item2.setTextContent("Custom parameter 2");
                ArrayList<Object> list = new ArrayList<Object>(2);
                list.add(item1);
                list.add(item2);
                evtxResponse.addRefParamsToSubscriptionManagerEpr(list);
            } else {
                throw new DeliveryModeRequestedUnavailableFault(
                        EventingSupport.getSupportedDeliveryModes());
            }
        } else if (Eventing.UNSUBSCRIBE_ACTION_URI.equals(action)) {
            final EventingExtensions evtx = new EventingExtensions(request);
            final EventingExtensions evtxResponse = new EventingExtensions(response);
            response.setAction(Eventing.UNSUBSCRIBE_RESPONSE_URI);
            EventingSupport.unsubscribe(context, evtx, evtxResponse);
        } else if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            
            synchronized (this) {
            	// Make sure there is an Iterator factory registered for this resource
            	if (EnumerationSupport.getIteratorFactory(resource) == null) {
            		EnumerationSupport.registerIteratorFactory(resource,
            				new pull_source_IteratorFactory(resource));
            	}
            }
            EnumerationSupport.enumerate(context, enuRequest, enuResponse);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.PULL_RESPONSE_URI);
            EnumerationSupport.pull(context,enuRequest, enuResponse);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.RELEASE_RESPONSE_URI);
            EnumerationSupport.release(context,enuRequest, enuResponse);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}