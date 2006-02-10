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
 * $Id: eventing_Handler.java,v 1.3 2006-02-10 01:10:03 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.server.EventingSupport;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class eventing_Handler implements Handler {
    
    private static final Logger LOG = 
            Logger.getLogger(eventing_Handler.class.getName());
    
    private EventingSupport eventingSupport = new EventingSupport();
    private Timer eventTimer = new Timer(true);
    private final Calendar NOW = Calendar.getInstance();
    private final String NS_URI = "http://wiseman.dev.java.net/ws/" + 
            NOW.get(Calendar.YEAR) + "/" + NOW.get(Calendar.MONTH) + "/eventing";
    private static final String[][] EVENTS = {
        { "event1", "critical" }, 
        { "event2", "warning" },
        { "event3", "info" },
        { "event4", "debug" },
        { "event5", "critical" }
    };
    
    public void handle(final String action, final String resource,
            final Management request, final Management response) throws Exception {
        
        final Eventing evtRequest = new Eventing(request);
        final Eventing evtResponse = new Eventing(response);
        
        if (Eventing.SUBSCRIBE_ACTION_URI.equals(action)) {
            evtResponse.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
            eventingSupport.subscribe(evtRequest, evtResponse);
            
            // setup a timer to send some test events
            final TimerTask sendEventTask = new TimerTask() {
                int eventCount = 0;
                public void run() {
                    try {
                        final Addressing msg = new Addressing();
                        msg.setAction(Management.EVENT_URI);

                        final Document doc = msg.newDocument();
                        final Element root = doc.createElementNS(NS_URI, "ev:" + EVENTS[eventCount][1]);
                        root.setTextContent(EVENTS[eventCount][0]);
                        doc.appendChild(root);
                        msg.getBody().addDocument(doc);
                        
                        eventingSupport.sendEvent(msg);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Sent event " + msg.getMessageId());
                        }
                    } catch (Throwable th) {
                        LOG.log(Level.SEVERE, "Failed to deliver event", th);
                    }
                    if (++eventCount >= EVENTS.length) {
                        eventTimer.cancel();
                    }
                }
            };
            final long DELAY = 1000;
            final long PERIOD = 500;
            eventTimer.schedule(sendEventTask, DELAY, PERIOD);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}
