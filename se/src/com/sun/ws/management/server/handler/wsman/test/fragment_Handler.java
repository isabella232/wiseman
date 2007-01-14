/*
 * Copyright 2006 Hewlett-Packard Development Company, L.P.
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
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.ws.management.FragmentDialectNotSupportedFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.server.BaseSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.xml.XPath;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPHeaderElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

/**
 * A test handler for fragment level operations.
 * <p/>
 * Fragement requests should be made against this document
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;jb:this xmlns:jb="http://test.foo" &gt;
 *   &lt;jb:is&gt;
 *     &lt;jb:a&gt;
 *       &lt;jb:foo&gt;
 *         &lt;jb:bar&gt;
 *             this is a test of fragments
 *         &lt;/jb:bar&gt;
 *       &lt;/jb:foo&gt;
 *     &lt;/jb:a&gt;
 *   &lt;/jb:is&gt;
 * &lt;/jb:this&gt;
 * </pre>
 */
public class fragment_Handler extends base_Handler {
    
    private static final String CUSTOM_JAXB_PREFIX = "jb";
    private static final String CUSTOM_JAXB_NS = "http://test.foo";
    private static final Map<String, String> NAMESPACES = new HashMap<String, String>();
    
    private static NamespaceMap nsMap = null;
    
    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {
        
        if (nsMap == null) {
            NAMESPACES.put(CUSTOM_JAXB_PREFIX, CUSTOM_JAXB_NS);
            nsMap = new NamespaceMap(NAMESPACES);
        }
        
        final Document doc = response.newDocument();
        buildContentDocument(doc);
        
        final TransferExtensions transExtRequest = new TransferExtensions(request);
        final TransferExtensions transExtResponse = new TransferExtensions(response);
        
        final SOAPHeaderElement fragmentHeader = transExtRequest.getFragmentHeader();
        final String expression = fragmentHeader == null ? null : fragmentHeader.getTextContent();
        final String dialect = fragmentHeader == null ? null : fragmentHeader.getAttributeValue(TransferExtensions.DIALECT);
        if (!BaseSupport.isSupportedDialect(dialect)) {
            throw new FragmentDialectNotSupportedFault(BaseSupport.getSupportedDialects());
        }
        
        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.addNamespaceDeclarations(NAMESPACES);
            response.setAction(Transfer.GET_RESPONSE_URI);
            transExtResponse.setFragmentGetResponse(fragmentHeader, doc);
            return;
        }
        
        if (Transfer.PUT_ACTION_URI.equals(action)) {
            response.addNamespaceDeclarations(NAMESPACES);
            response.setAction(Transfer.PUT_RESPONSE_URI);
            
            if (fragmentHeader == null) {
                // this is a regular transfer: not a fragment transfer, update the entire doc
                // TODO
            } else {
                final Node xmlFragmentNode = (Node) transExtRequest.getBody().getChildElements().next();
                final NodeList childNodes = xmlFragmentNode.getChildNodes();
                final List<Object> nodeContent = new ArrayList<Object>();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    nodeContent.add(childNodes.item(i));
                }
                transExtResponse.setFragmentPutResponse(fragmentHeader, nodeContent,
                        expression, XPath.filter(doc.getDocumentElement(), expression, nsMap));
            }
            // dump the modified doc for debugging
            prettyPrint(doc);
            return;
        }
        
        if (Transfer.DELETE_ACTION_URI.equals(action)) {
            response.addNamespaceDeclarations(NAMESPACES);
            response.setAction(Transfer.DELETE_RESPONSE_URI);
            
            if (fragmentHeader == null) {
                // this is a regular transfer: not a fragment transfer, delete the entire doc
                // TODO
            } else {
                transExtResponse.setFragmentDeleteResponse(fragmentHeader,
                        XPath.filter(doc.getDocumentElement(), expression, nsMap));
            }
            // dump the modified doc for debugging
            prettyPrint(doc);
            return;
        }
        
        if (Transfer.CREATE_ACTION_URI.equals(action)) {
            response.addNamespaceDeclarations(NAMESPACES);
            response.setAction(Transfer.CREATE_RESPONSE_URI);
            
            if (fragmentHeader == null) {
                // this is a regular transfer: not a fragment transfer, create the entire doc
                // TODO
            } else {
                final Node xmlFragmentNode = (Node) transExtRequest.getBody().getChildElements().next();
                final NodeList childNodes = xmlFragmentNode.getChildNodes();
                final List<Object> nodeContent = new ArrayList<Object>();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    nodeContent.add(childNodes.item(i));
                }
                final EndpointReferenceType epr =
                        Addressing.createEndpointReference(transExtRequest.getTo(),
                        null, null, null, null);
                transExtResponse.setFragmentCreateResponse(fragmentHeader, nodeContent,
                        expression, XPath.filter(doc.getDocumentElement(), expression, nsMap),
                        epr);
            }
            // dump the modified doc for debugging
            prettyPrint(doc);
            return;
        }
        
        throw new ActionNotSupportedFault(action);
    }
    
    /**
     * Method to construct the document to be traversed with the fragment
     * request.
     * <p/>
     * In reality this document may originate from some backend, and is itself
     * the backend representation
     *
     * @param doc
     */
    private void buildContentDocument(final Document doc) {
        final Element thisElement = doc.createElementNS(CUSTOM_JAXB_NS, "this");
        final Element isElement = doc.createElementNS(CUSTOM_JAXB_NS, "is");
        final Element aElement = doc.createElementNS(CUSTOM_JAXB_NS, "a");
        final Element fooElement = doc.createElementNS(CUSTOM_JAXB_NS, "foo");
        final Element barElement = doc.createElementNS(CUSTOM_JAXB_NS, "bar");
        barElement.setTextContent("this is a test of fragments");
        thisElement.appendChild(isElement);
        isElement.appendChild(aElement);
        aElement.appendChild(fooElement);
        fooElement.appendChild(barElement);
        doc.appendChild(thisElement);
    }
    
    private void prettyPrint(final Document doc) throws IOException {
        final OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(72);
        format.setIndenting(true);
        format.setIndent(2);
        final XMLSerializer serializer = new XMLSerializer(System.out, format);
        serializer.serialize(doc);
    }
}
