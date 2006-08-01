/*
 * Copyright 2006 Sun Microsystems, Inc.
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
 * $Id: cim_numericsensor_Handler.java,v 1.2 2006-08-01 01:32:02 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.org.dmtf.schemas.wbem.wscim._1.cim_schema._2;

import com.sun.ws.management.FragmentDialectNotSupportedFault;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.xml.XPath;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPHeaderElement;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

public class cim_numericsensor_Handler implements Handler, EnumerationIterator {
    
    private static final String NS_PREFIX = "p";
    
    private static String[] SELECTOR_KEYS = {
        "CreationClassName",
        "DeviceID",
        "SystemCreationClassName",
        "SystemName"
    };
    
    private NamespaceMap nsMap = null;
    
    private static final class Context {
        HandlerContext hcontext = null;
        boolean cancelled = false;
        int index = 0;
        int count = 2;
        String address;
        String resourceURI;
        String noPrefix = "";
    }
    
    public void handle(final String action, final String resource,
        final HandlerContext hcontext,
        final Management request, final Management response) throws Exception {
        
        if (nsMap == null) {
            final Map<String, String> map = new HashMap<String, String>();
            map.put(NS_PREFIX, resource);
            nsMap = new NamespaceMap(map);
        }
        
        String noPrefix = "";
        final Set<SelectorType> selectors = request.getSelectors();
        if (selectors != null) {
            Iterator<SelectorType> si = selectors.iterator();
            while (si.hasNext()) {
                final SelectorType st = si.next();
                if ("NoPrefix".equals(st.getName()) &&
                    "true".equalsIgnoreCase(st.getContent().get(0).toString())) {
                    noPrefix = "_NoPrefix";
                    break;
                }
            }
        }
        
        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            
            if (selectors.size() < SELECTOR_KEYS.length) {
                throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
            }
            
            Document resourceDoc = null;
            final String resourceDocName = "Pull" + noPrefix + "_0.xml";
            final InputStream is = load(hcontext.getServletConfig().getServletContext(), resourceDocName);
            if (is == null) {
                throw new InternalErrorFault("Failed to load " + resourceDocName + " from war");
            }
            try {
                resourceDoc = response.getDocumentBuilder().parse(is);
            } catch (Exception ex) {
                throw new InternalErrorFault("Error parsing " + resourceDocName + " from war");
            }
            response.getBody().addDocument(resourceDoc);
        } else if (Transfer.PUT_ACTION_URI.equals(action)) {
            response.setAction(Transfer.PUT_RESPONSE_URI);
            final TransferExtensions txi = new TransferExtensions(request);
            final SOAPHeaderElement hdr = txi.getFragmentHeader();
            if (hdr != null) {
                // this is a fragment transfer, update the resource fragment
                final TransferExtensions txo = new TransferExtensions(response);
                final String expression = hdr.getTextContent();
                final String dialect = hdr.getAttributeValue(TransferExtensions.DIALECT);
                if (!XPath.isSupportedDialect(dialect)) {
                    throw new FragmentDialectNotSupportedFault(XPath.SUPPORTED_FILTER_DIALECTS);
                }
                
                final Node xmlFragmentNode = (Node) txi.getBody().getChildElements().next();
                final NodeList childNodes = xmlFragmentNode.getChildNodes();
                final List<Node> nodeContent = new ArrayList<Node>();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    nodeContent.add(childNodes.item(i));
                }
                
                Document resourceDoc = null;
                final String resourceDocName = "Pull" + noPrefix + "_0.xml";
                final InputStream is = load(hcontext.getServletConfig().getServletContext(), resourceDocName);
                if (is == null) {
                    throw new InternalErrorFault("Failed to load " + resourceDocName + " from war");
                }
                try {
                    resourceDoc = response.getDocumentBuilder().parse(is);
                } catch (Exception ex) {
                    throw new InternalErrorFault("Error parsing " + resourceDocName + " from war");
                }
                
                // TODO - need XSD and JAXB artifacts pre-compiled for this to work
                /*
                final List<Node> content = XPath.filter(resourceDoc, expression, nsMap);
                txo.setFragmentPutResponse(hdr, nodeContent, expression, content);
                 */
                
                response.getHeader().addChildElement(hdr);
                
                final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
                final JAXBElement<MixedDataType> xmlFragment = Management.FACTORY.createXmlFragment(mixedDataType);
                Element lowerThresholdNonCriticalElement =
                    response.getBody().getOwnerDocument().createElementNS(resource,
                    "p:LowerThresholdNonCritical");
                lowerThresholdNonCriticalElement.setTextContent("100");
                mixedDataType.getContent().add(lowerThresholdNonCriticalElement);
                response.getXmlBinding().marshal(xmlFragment, response.getBody());
            } else {
                // this is regular transfer, update the entire resource
                final Document resourceDoc = request.getBody().extractContentAsDocument();
                // TODO: upate the resource
                response.getBody().addDocument(resourceDoc);
            }
        } else if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            final Context context = new Context();
            context.hcontext = hcontext;
            context.address = hcontext.getHttpServletRequest().getRequestURL().toString();
            context.resourceURI = resource;
            context.noPrefix = noPrefix;
            EnumerationSupport.enumerate(ereq, eres, this, context);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.PULL_RESPONSE_URI);
            EnumerationSupport.pull(ereq, eres);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.RELEASE_RESPONSE_URI);
            EnumerationSupport.release(ereq, eres);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
    
    public List<EnumerationItem> next(final DocumentBuilder db, final Object context,
        final boolean includeItem, final boolean includeEPR,
        final int start, final int count) {
        final Context ctx = (Context) context;
        final int returnCount = Math.min(count, ctx.count - start);
        final List<EnumerationItem> items = new ArrayList(returnCount);
        for (int i = 0; i < returnCount && !ctx.cancelled; i++) {
            
            Element root = null;
            if (includeItem) {
                Document resourceDoc = null;
                final String resourceDocName = "Pull" + ctx.noPrefix + "_" + start + ".xml";
                final InputStream is = load(ctx.hcontext.getServletConfig().getServletContext(), resourceDocName);
                if (is == null) {
                    throw new InternalErrorFault("Failed to load " + resourceDocName + " from war");
                }
                try {
                    resourceDoc = db.parse(is);
                } catch (Exception ex) {
                    throw new InternalErrorFault("Error parsing " + resourceDocName + " from war");
                }
                root = resourceDoc.getDocumentElement();
            }
            
            EndpointReferenceType epr = null;
            if (includeEPR) {
                final Map<String, String> selectors = new HashMap<String, String>();
                for (final String selector : SELECTOR_KEYS) {
                    selectors.put(selector, root.getElementsByTagNameNS(ctx.resourceURI, selector).item(0).getTextContent());
                }
                epr = EnumerationSupport.createEndpointReference(
                    ctx.address,
                    ctx.resourceURI,
                    selectors);
            }
            
            items.add(new EnumerationItem(root, epr));
        }
        return items;
    }
    
    public boolean hasNext(final Object context, final int start) {
        final Context ctx = (Context) context;
        return start < ctx.count;
    }
    
    public void cancel(final Object context) {
        final Context ctx = (Context) context;
        ctx.cancelled = true;
    }
    
    public int estimateTotalItems(final Object context) {
        final Context ctx = (Context) context;
        return ctx.count;
    }
    
    public NamespaceMap getNamespaces() {
        return nsMap;
    }
    
    private static final InputStream load(final ServletContext context, final String docName) {
        final String xml =
            cim_numericsensor_Handler.class.getPackage().getName().replace('.', '/') +
            "/" +
            cim_numericsensor_Handler.class.getSimpleName() + "_" + docName;
        return context.getResourceAsStream(xml);
    }
}
