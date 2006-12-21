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
 * $Id: custom_filter_Handler.java,v 1.1.2.1 2006-12-21 08:26:02 jfdenise Exp $
 */

package com.sun.ws.management.server.handler.wsman.test.enumeration.filter;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Filter;
import com.sun.ws.management.server.FilterFactory;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transfer.Transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

/**
 * Copied form java.system.properties. Added Custom Filter.
 */
public class custom_filter_Handler implements Handler, EnumerationIterator {
    
    private static final String PROPERTY_SELECTOR_KEY = "customfilter";
    private static final String NS_URI = "https://wiseman.dev.java.net/java";
    private static final String NS_PREFIX = "java";
    private static final Map<String, String> NAMESPACES = new HashMap<String, String>();
    
    private static NamespaceMap nsMap = null;
    
    public static final String TEST_CUSTOM_FILTER = "test/custom/filter";
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
    }
    
    private class TestCustomFilterFactory implements FilterFactory {
        private class TestCustomFilter implements Filter {
            public boolean evaluate(final Node content, 
                    NamespaceMap... ns) throws Exception {
                return true;
            }
        }
        public Filter newFilter(List content, 
                NamespaceMap namespaces) throws FaultException, Exception {
            return new TestCustomFilter();
        }
        
    }
    
    public custom_filter_Handler() {
        try {
            EnumerationSupport.addSupportedFilterDialect(TEST_CUSTOM_FILTER, 
                new TestCustomFilterFactory());
        }catch(Exception ex) {
            throw new IllegalArgumentException("Exception " + ex);
        }
    }
    
    public void handle(final String action, final String resource,
            final HandlerContext hcontext,
            final Management request, final Management response) throws Exception {
        
        if (nsMap == null) {
            NAMESPACES.put(NS_PREFIX, NS_URI);
            nsMap = new NamespaceMap(NAMESPACES);
        }
        
        final Enumeration enuRequest = new Enumeration(request);
        final Enumeration enuResponse = new Enumeration(response);
        
        if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            
            final Context context = new Context();
            context.resourceURI = resource;
            // this generates an AccessDenied exception which is returned
            // to the client as an AccessDenied fault if the server is
            // running in the sun app server with a security manager in
            // place (the default), which disallows enumeration of
            // system properties
            context.properties = System.getProperties();
            context.cancelled = false;
            
            // retrieve the request path for use in EPR construction and store
            //  it in the context for later retrieval
            final String path = hcontext.getURL();
            context.requestPath = path;
            
            // call the server to process the enumerate request
            EnumerationSupport.enumerate(enuRequest, enuResponse, this, context, nsMap);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.PULL_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            EnumerationSupport.pull(enuRequest, enuResponse);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.RELEASE_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            EnumerationSupport.release(enuRequest, enuResponse);
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
    
    public List<EnumerationItem> next(final DocumentBuilder db, final Object context,
            final boolean includeItem, final boolean includeEPR,
            final int start, final int count) {
        final Context ctx = (Context) context;
        final Properties props = ctx.properties;
        final int returnCount = Math.min(count, props.size() - start);
        final List<EnumerationItem> items = new ArrayList<EnumerationItem>(returnCount);
        final Object[] keys = props.keySet().toArray();
        for (int i = 0; i < returnCount && !ctx.cancelled; i++) {
            final Object key = keys[start + i];

            // construct an item if necessary for the enumeration
            Element item = null;
            if (includeItem) {
                final Object value = props.get(key);
                final Document doc = db.newDocument();
                item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + key);
                item.setTextContent(value.toString());
            }

            // construct an endpoint reference to accompany the element, if needed
            EndpointReferenceType epr = null;
            if (includeEPR) {
                final Map<String, String> selectors = new HashMap<String, String>();
                selectors.put(PROPERTY_SELECTOR_KEY, key.toString());
                epr = EnumerationSupport.createEndpointReference(ctx.requestPath, ctx.resourceURI, selectors);
            }

            final EnumerationItem ei = new EnumerationItem(item, epr);
            items.add(ei);
        }
        return items;
    }

    public boolean hasNext(final Object context, final int start) {
        final Context ctx = (Context) context;
        final Properties props = ctx.properties;
        return start < props.size();
    }
    
    public void cancel(final Object context) {
        final Context ctx = (Context) context;
        ctx.cancelled = true;
    }
    
    public int estimateTotalItems(final Object context) {
        final Context ctx = (Context) context;
        final Properties props = ctx.properties;
        return props.size();
    }
    
    public NamespaceMap getNamespaces() {
        return nsMap;
    }
}
