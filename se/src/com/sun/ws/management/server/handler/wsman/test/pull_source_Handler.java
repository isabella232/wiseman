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
 * $Id: pull_source_Handler.java,v 1.14 2007-01-14 17:52:33 denis_rachal Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import java.util.HashMap;
import java.util.Map;

import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.eventing.DeliveryModeRequestedUnavailableFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.EventingSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;

public class pull_source_Handler implements Handler {

    private static final Map<String, String> NAMESPACES = new HashMap<String, String>();

    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {
        
        final EnumerationExtensions enuRequest = new EnumerationExtensions(request);
        final EnumerationExtensions enuResponse = new EnumerationExtensions(response);
        
        if (Eventing.SUBSCRIBE_ACTION_URI.equals(action)) {
            final EventingExtensions evtx = new EventingExtensions(request);
            final Subscribe subscribe = evtx.getSubscribe();
            final DeliveryType deliveryType = subscribe.getDelivery();
            if (EventingExtensions.PULL_DELIVERY_MODE.equals(deliveryType.getMode())) {
                enuResponse.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
                synchronized (this) {
                	// Make sure there is an Iterator factory registered for this resource
                	if (EnumerationSupport.getIteratorFactory(resource) == null) {
                		EnumerationSupport.registerIteratorFactory(resource,
                				new pull_source_IteratorFactory(resource));
                	}
                }
                EnumerationSupport.enumerate(context, enuRequest, enuResponse);
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
            EnumerationSupport.pull(enuRequest, enuResponse);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.RELEASE_RESPONSE_URI);
            EnumerationSupport.release(enuRequest, enuResponse);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}