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
 * $Id: NamespaceMap.java,v 1.1 2006-02-16 20:12:42 akhilarora Exp $
 */

package com.sun.ws.management.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;

public final class NamespaceMap implements NamespaceContext {
    
    private final Map<String, String> namespaces;
    
    // key:prefix value:URI
    public NamespaceMap(final Map<String, String> ns) {
        if (ns == null) {
            throw new NullPointerException("Namespace prefix/URI map cannot be null");
        }
        if (ns.isEmpty()) {
            throw new IllegalArgumentException("Namespace prefix/URI map cannot be empty");
        }
        // make a defensive deep copy
        namespaces = new HashMap<String, String>(ns);
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
}
