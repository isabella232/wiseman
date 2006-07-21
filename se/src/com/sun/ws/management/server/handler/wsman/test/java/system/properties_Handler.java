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
 * $Id: properties_Handler.java,v 1.11 2006-07-21 20:26:20 pmonday Exp $
 */

package com.sun.ws.management.server.handler.wsman.test.java.system;

import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.EnumerationElement;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.transfer.Transfer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;

public class properties_Handler implements Handler, EnumerationIterator {
    private static final String PROPERTY_SELECTOR_KEY = "property";
    private static final String NS_URI = "https://wiseman.dev.java.net/java";
    private static final String NS_PREFIX = "java";
    private static final Map<String, String> NAMESPACES = new HashMap<String, String>();
    private final String RESOURCE = "wsman:test/java/system/properties";    
    
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
            // this generates an AccessDenied exception which is returned
            // to the client as an AccessDenied fault if the server is
            // running in the sun app server with a security manager in
            // place (the default), which disallows enumeration of
            // system properties
            context.properties = System.getProperties();
            context.cancelled = false;
            
            // retrieve the request path for use in EPR construction and store
            //  it in the context for later retrieval
            HttpServletRequest servletRequest = hcontext.getHttpServletRequest();
            String path = servletRequest.getRequestURL().toString();
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
    
    public List<EnumerationElement> next(final DocumentBuilder db, final Object context,
            final int start, final int count) {
        final Context ctx = (Context) context;
        final Properties props = ctx.properties;
        final int returnCount = Math.min(count, props.size() - start);
        final List<EnumerationElement> items = new ArrayList(returnCount);
        final Object[] keys = props.keySet().toArray();
        for (int i = 0; i < returnCount && !ctx.cancelled; i++) {
            final Object key = keys[start + i];
            final Object value = props.get(key);
            final Document doc = db.newDocument();
            final Element item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + key);
            item.setTextContent(value.toString());
            
            // construct an endpoint reference to accompany the element
            EndpointReferenceType epr = constructEPR(ctx.requestPath, RESOURCE, (String)key);
            EnumerationElement ee = new EnumerationElement();
            ee.setElement(item);
            ee.setEndpointReference(epr);
            
            items.add(ee);
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
    
    /**
     * Construct an EPR based on information provided during the enumeration
     * @param serverURL a fully qualified URL referring to the server and context
     * path that can facilitate the request
     * @param resourceURI is the resource in which the item resides
     * @param key is a selector that can be used to get a direct path to the item
     * @return a valid EndpointReferenceType
     */
    protected EndpointReferenceType constructEPR(String serverURL, String resourceIdentifier, String key) {
        ObjectFactory factory = new ObjectFactory();
        EndpointReferenceType epr = factory.createEndpointReferenceType();
                
        // set up the return address
        AttributedURI toUri = factory.createAttributedURI();
        toUri.setValue(serverURL);
        epr.setAddress(toUri);
        
        // prepare a reference parameters node to insert the selector and resourceuri
        ReferenceParametersType referenceParameters = factory.createReferenceParametersType();
        
        // setup the resource uri
        org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory wsmanObjectFactory =
                new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
        AttributableURI attributableURI = wsmanObjectFactory.createAttributableURI();
                
        attributableURI.setValue(resourceIdentifier);
        JAXBElement<AttributableURI> resourceURI = 
                wsmanObjectFactory.createResourceURI(attributableURI);
        referenceParameters.getAny().add(resourceURI);
        
        // setup the selectorset
        SelectorSetType selectorSet = wsmanObjectFactory.createSelectorSetType();
        SelectorType selector = wsmanObjectFactory.createSelectorType();
        selector.setName(PROPERTY_SELECTOR_KEY);
        selector.getContent().add(key);
        selectorSet.getSelector().add(selector);
        JAXBElement<SelectorSetType> selectorSetJaxb = wsmanObjectFactory.createSelectorSet(selectorSet);
        referenceParameters.getAny().add(selectorSetJaxb);
        epr.setReferenceParameters(referenceParameters);
        
        return epr;
    }
    
}
