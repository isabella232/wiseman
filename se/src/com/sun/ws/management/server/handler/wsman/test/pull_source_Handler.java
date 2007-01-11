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
 * $Id: pull_source_Handler.java,v 1.13 2007-01-11 13:12:53 jfdenise Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.eventing.DeliveryModeRequestedUnavailableFault;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.server.EventingSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;

public class pull_source_Handler implements Handler, EnumerationIterator {

    private static final String SELECTOR_KEY = "log";
    private static final String NS_URI = "https://wiseman.dev.java.net/test/events/pull";
    private static final String NS_PREFIX = "log";
    private static final Map<String, String> NAMESPACES = new HashMap<String, String>();

    private static NamespaceMap nsMap = null;
    
    /**
     * A context class to pass with enumeration requests
     */
    private static final class Context {
        /**
         * Indication of whether the request was cancelled during processing
         */
        boolean cancelled;
        
        /**
         * System properties
         */
        Properties properties;
        
        /**
         * Server request path that can be used for creating an EPR
         */
        String requestPath;
        
        String resourceURI;
        
        /**
         * A log of events to pass.  These should be replaced with real events.
         */
        String[][] eventLog = {
            { "event1", "critical" },
            { "event2", "warning" },
            { "event3", "info" },
            { "event4", "debug" }
        };
    }
    
    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {
        
        if (nsMap == null) {
            NAMESPACES.put(NS_PREFIX, NS_URI);
            nsMap = new NamespaceMap(NAMESPACES);
        }
        
        final Enumeration enuRequest = new Enumeration(request);
        final Enumeration enuResponse = new Enumeration(response);
        
        if (Eventing.SUBSCRIBE_ACTION_URI.equals(action)) {
            final EventingExtensions evtx = new EventingExtensions(request);
            final Subscribe subscribe = evtx.getSubscribe();
            final DeliveryType deliveryType = subscribe.getDelivery();
            if (EventingExtensions.PULL_DELIVERY_MODE.equals(deliveryType.getMode())) {
                enuResponse.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
                final Context ctx = new Context();
                ctx.resourceURI = resource;
                ctx.cancelled = false;
                // retrieve the request path for use in EPR construction and store
                //  it in the context for later retrieval
                final String path = context.getURL();
                ctx.requestPath = path;
                EnumerationSupport.enumerate(enuRequest, enuResponse, this, ctx, nsMap);
            } else {
                throw new DeliveryModeRequestedUnavailableFault(
                        EventingSupport.getSupportedDeliveryModes());
            }
        } else if (Eventing.UNSUBSCRIBE_ACTION_URI.equals(action)) {
            response.setAction(Eventing.UNSUBSCRIBE_RESPONSE_URI);
            EnumerationSupport.release(enuRequest, enuResponse);
        } else if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            
            final Context ctx = new Context();
            ctx.cancelled = false;
            
            // retrieve the request path for use in EPR construction and store
            //  it in the context for later retrieval
            final String path = context.getURL();
            ctx.requestPath = path;
            
            // call the server to process the enumerate request
            EnumerationSupport.enumerate(enuRequest, enuResponse, this, ctx, nsMap);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.PULL_RESPONSE_URI);
            EnumerationSupport.pull(enuRequest, enuResponse);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.RELEASE_RESPONSE_URI);
            EnumerationSupport.release(enuRequest, enuResponse);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
    
    public List<EnumerationItem> next(final DocumentBuilder db, final Object context,
            final boolean includeItem, final boolean includeEPR,
            final int start, final int count) {
        final Context ctx = (Context) context;
        final int returnCount = Math.min(count, ctx.eventLog.length - start);
        final List<EnumerationItem> items = new ArrayList(returnCount);
        for (int i = 0; i < returnCount && !ctx.cancelled; i++) {
            final String key = ctx.eventLog[start + i][0];

            // construct an item if necessary for the enumeration
            Element item = null;
            if (includeItem) {
                final String value = ctx.eventLog[start + i][1];
                final Document doc = db.newDocument();
                item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + key);
                item.setTextContent(value);
            }

            // construct an endpoint reference to accompany the element, if needed
            EndpointReferenceType epr = null;
            if (includeEPR) {
                final Map<String, String> selectors = new HashMap<String, String>();
                selectors.put(SELECTOR_KEY, key);
                epr = EnumerationSupport.createEndpointReference(ctx.requestPath, ctx.resourceURI, selectors);
            }

            final EnumerationItem ei = new EnumerationItem(item, epr);
            items.add(ei);
        }
        return items;
    }
    
    public boolean hasNext(final Object context, final int startPos) {
        final String[][] events = ((Context)context).eventLog;
        return startPos < events.length;
    }
    
    public void cancel(final Object context) {
        final Context ctx = (Context) context;
        ctx.cancelled = true;
    }
    
    public int estimateTotalItems(final Object context) {
        // choose not to provide an estimate
        return -1;
    }
    
    public NamespaceMap getNamespaces() {
        return nsMap;
    }
}
