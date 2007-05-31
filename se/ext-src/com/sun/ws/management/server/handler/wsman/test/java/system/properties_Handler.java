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
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **
 * $Id: properties_Handler.java,v 1.2 2007-05-31 19:47:46 nbeers Exp $
 */

package com.sun.ws.management.server.handler.wsman.test.java.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;

import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.handler.wsman.test.enumeration.filter.custom_filter_IteratorFactory;
import com.sun.ws.management.transfer.Transfer;

public class properties_Handler implements Handler {
    
    public static final String NS_URI = "https://wiseman.dev.java.net/java";
    public static final String NS_PREFIX = "java";
    
    private final Map<String, String> NAMESPACES;


	public properties_Handler() {
		this.NAMESPACES = new HashMap<String, String>();
		NAMESPACES.put(NS_PREFIX, NS_URI);
	}
    
    public void handle(final String action, final String resource,
            final HandlerContext hcontext,
            final Management request, final Management response) throws Exception {
        
        final EnumerationExtensions enuRequest = new EnumerationExtensions(request);
        final EnumerationExtensions enuResponse = new EnumerationExtensions(response);
        
        if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            
            synchronized (this) {
            	// Make sure there is an Iterator factory registered for this resource
            	if (EnumerationSupport.getIteratorFactory(resource) == null) {
            		EnumerationSupport.registerIteratorFactory(resource,
            				new properties_IteratorFactory(resource));
            	}
            }
            EnumerationSupport.enumerate(hcontext, enuRequest, enuResponse);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.PULL_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            EnumerationSupport.pull(hcontext,enuRequest, enuResponse);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.RELEASE_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            EnumerationSupport.release(hcontext,enuRequest, enuResponse);
        } else if (Transfer.GET_ACTION_URI.equals(action)) {
            enuResponse.setAction(Transfer.GET_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            // return all the properties in a single response
            final Document doc = enuResponse.newDocument();
            final Element root = doc.createElementNS(Enumeration.NS_URI, Enumeration.NS_PREFIX + ":Items");
            doc.appendChild(root);
            final Iterator<Entry<Object, Object> > pi = System.getProperties().entrySet().iterator();
            while (pi.hasNext()) {
                final Entry<Object, Object> p = pi.next();
                final Element item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + p.getKey());
                item.setTextContent(p.getValue().toString());
                root.appendChild(item);
            }
            enuResponse.getMessage().getSOAPBody().addDocument(doc);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}