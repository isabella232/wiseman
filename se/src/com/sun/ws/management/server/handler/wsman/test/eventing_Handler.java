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
 * $Id: eventing_Handler.java,v 1.12 2006-07-19 22:41:38 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.server.EventingSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class eventing_Handler implements Handler {
    
    private static final String NS_PREFIX = "ev";
    private static final String NS_URI = "http://wiseman.dev.java.net/ws/eventing/test";
    
    private static final Logger LOG =
            Logger.getLogger(eventing_Handler.class.getName());
    private static final String[][] EVENTS = {
        { "event1", "critical" },
        { "event2", "warning" },
        { "event3", "info" },
        { "event4", "debug" },
        { "event5", "critical" }
    };
    
    private static NamespaceMap nsMap = null;
    
    private final Timer eventTimer = new Timer(true);
    
    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {
        
        if (nsMap == null) {
            final Map<String, String> map = new HashMap<String, String>();
            map.put(NS_PREFIX, NS_URI);
            nsMap = new NamespaceMap(map);
        }
        
        final Eventing evtRequest = new Eventing(request);
        final Eventing evtResponse = new Eventing(response);
        
        if (Eventing.SUBSCRIBE_ACTION_URI.equals(action)) {
            evtResponse.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
            final Object evtContext = EventingSupport.subscribe(evtRequest, evtResponse, nsMap);
            
            // setup a timer to send some test events
            final TimerTask sendEventTask = new TimerTask() {
                int eventCount = 0;
                public void run() {
                    try {
                        final Addressing msg = new Addressing();
                        msg.getEnvelope().addNamespaceDeclaration(NS_PREFIX, NS_URI);
                        msg.setAction(Management.EVENT_URI);
                        
                        final Document doc = msg.newDocument();
                        final Element root = doc.createElementNS(NS_URI, NS_PREFIX + ":" + EVENTS[eventCount][1]);
                        root.setTextContent(EVENTS[eventCount][0]);
                        doc.appendChild(root);
                        msg.getBody().addDocument(doc);
                        
                        final String info = root.getNodeName() + " " + root.getTextContent();
                        if (EventingSupport.sendEvent(evtContext, msg, nsMap)) {
                            LOG.info("Sent event " + info);
                        } else {
                            LOG.info("Event filtered " + info);
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Failed to deliver event", ex);
                    }
                    if (++eventCount >= EVENTS.length) {
                        try {
                            eventTimer.cancel();
                        } catch (IllegalStateException ise) {
                            // ignore - sometimes we get a Timer already cancelled
                        }
                    }
                }
            };
            final long DELAY = 1000;
            final long PERIOD = 500;
            eventTimer.schedule(sendEventTask, DELAY, PERIOD);
        } else if (Eventing.UNSUBSCRIBE_ACTION_URI.equals(action)) {
            evtResponse.setAction(Eventing.UNSUBSCRIBE_RESPONSE_URI);
            EventingSupport.unsubscribe(evtRequest, evtResponse);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}
