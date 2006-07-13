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
 * $Id: NamespaceMap.java,v 1.4 2006-07-13 00:17:44 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.Message;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.soap.SOAPEnvelope;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class NamespaceMap implements NamespaceContext {
    
    private final Map<String, String> namespaces;
    
    // key:prefix value:URI
    public NamespaceMap(final Map<String, String> ns) {
        if (ns == null) {
            throw new IllegalArgumentException("Namespace prefix/URI map cannot be null");
        }
        if (ns.isEmpty()) {
            throw new IllegalArgumentException("Namespace prefix/URI map cannot be empty");
        }
        // make a defensive deep copy
        namespaces = new HashMap<String, String>(ns);
    }
    
    // walk the document tree to extract all namespace declarations,
    // combining namespaces from (optional) supplied maps
    public NamespaceMap(final Node node, final Map<String, String>... nsMaps) {
        namespaces = new HashMap<String, String>();
        
        scanNodeRecursive(node);
        
        // combine declarations from supplied maps, if any
        if (nsMaps != null) {
            for (final Map<String, String> ns : nsMaps) {
                namespaces.putAll(ns);
            }
        }
    }
    
    // walk the message tree to extract all namespace declarations,
    // combining namespaces from (optional) supplied maps
    public NamespaceMap(final Message msg, final Map<String, String>... nsMaps) {
        namespaces = new HashMap<String, String>();
        final SOAPEnvelope env = msg.getEnvelope();
        
        // first collect all namespaces declared in the soap envelope
        final Iterator<String> pi = env.getNamespacePrefixes();
        while (pi.hasNext()) {
            final String prefix = pi.next();
            final String uri = env.getNamespaceURI(prefix);
            assert uri != null : "namespace uri for env prefix " + prefix + " cannot be null";
            namespaces.put(prefix, uri);
        }
        
        // now walk the soap message to collect others
        scanNodeRecursive(env);
        
        // combine declarations from supplied maps, if any
        if (nsMaps != null) {
            for (final Map<String, String> ns : nsMaps) {
                namespaces.putAll(ns);
            }
        }
    }
    
    private void scanNodeRecursive(final Node node) {
        if (node == null) {
            return;
        }
        
        final String prefix = node.getPrefix();
        final String uri = node.getNamespaceURI();
        
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                if ("xmlns".equals(prefix) && "http://www.w3.org/2000/xmlns/".equals(uri)) {
                    namespaces.put(node.getLocalName(), node.getNodeValue());
                }
                break;
                
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.ELEMENT_NODE:
                if (prefix != null) {
                    namespaces.put(prefix, uri);
                }
                
                final NodeList children = node.getChildNodes();
                for (int i = children.getLength(); i >= 0; i--) {
                    scanNodeRecursive(children.item(i));
                }
                
                final NamedNodeMap attributes = node.getAttributes();
                if (attributes != null) {
                    for (int i = attributes.getLength(); i >= 0; i--) {
                        scanNodeRecursive(attributes.item(i));
                    }
                }
                break;
        }
    }
    
    public Iterator getPrefixes(final String namespaceURI) {
        final Set<String> prefixes = new HashSet<String>();
        final Iterator<String> pi = namespaces.keySet().iterator();
        while (pi.hasNext()) {
            final String prefix = pi.next();
            final String uri = namespaces.get(prefix);
            if (uri != null) {
                if (uri.equals(namespaceURI)) {
                    prefixes.add(prefix);
                }
            }
        }
        return prefixes.iterator();
    }
    
    public String getPrefix(final String namespaceURI) {
        final Iterator<String> pi = namespaces.keySet().iterator();
        while (pi.hasNext()) {
            final String prefix = pi.next();
            final String uri = namespaces.get(prefix);
            if (uri != null) {
                if (uri.equals(namespaceURI)) {
                    return prefix;
                }
            }
        }
        return null;
    }
    
    public String getNamespaceURI(final String prefix) {
        return namespaces.get(prefix);
    }
    
    // key:prefix value:URI
    public Map<String, String> getMap() {
        return Collections.unmodifiableMap(namespaces);
    }
}
