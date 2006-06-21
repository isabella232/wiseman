/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: JarResourceLoader.java,v 1.1 2006-06-21 00:32:37 akhilarora Exp $
 */

package com.sun.ws.cim.mapping.loader;

import com.sun.ws.cim.mapping.Dom;
import com.sun.ws.cim.mapping.loader.ResourceLoader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class JarResourceLoader extends ResourceLoader {
    
    public InputStream loadResource(final String resource) throws IOException {
	return JarResourceLoader.class.getResourceAsStream(resource);
    }

    public Set getResourcePaths(final String prefix) throws IOException {
        // since ClassLoader lacks a method to list resources, 
        // we read the list of all classes from mof.xml 
        Document doc;
        try {
            doc = loadResourceAsDocument("mof");
        } catch (SAXException ex) {
            throw (IOException) new IOException().initCause(ex);
        }

        final NodeList nl = doc.getDocumentElement().getChildNodes();
        final int count = nl.getLength();
        final Set<String> resources = new LinkedHashSet<String>(count);
        for (int i = 0; i < count; i++) {
            final Node classNode = nl.item(i);
            if ("CLASS".equals(classNode.getNodeName())) {
                final String className = Dom.getAttr(classNode, "NAME");
                resources.add(className + XML_SUFFIX);
            }
        }
        return resources;
    }
}
