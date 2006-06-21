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
 * $Id: Dom.java,v 1.1 2006-06-21 00:32:35 akhilarora Exp $
 */

package com.sun.ws.cim.mapping;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class Dom {
    
    private static DocumentBuilderFactory docFactory = null;
    private static DocumentBuilder db = null;
    
    private Dom() {}
    
    public static String getAttr(final Node node, final String name) {
        final NamedNodeMap attrs = node.getAttributes();
        if (attrs == null) {
            return null;
        }
        final Node attr = attrs.getNamedItem(name);
        return attr == null ? null : attr.getNodeValue();
    }
    
    public static Element createElement(final Document doc, final Namespaces ns,
            final String prefix, final String localName) {
        final String nsuri = ns.getURI(prefix);
        if (nsuri == null) {
            throw new IllegalArgumentException("Unknown namespace prefix: " + prefix);
        }
        return doc.createElementNS(nsuri, prefix + Namespaces.COLON + localName);
    }
    
    public static Element createAny(final Document out, final Namespaces ns) {
        final Element any = createElement(out, ns, Namespaces.XS_NS_PREFIX, "any");
        any.setAttribute("namespace", "##other");
        any.setAttribute("processContents", "lax");
        any.setAttribute("minOccurs", "0");
        return any;
    }
    
    public static Element createAnyAttribute(final Document out, final Namespaces ns) {
        final Element anyAttr = createElement(out, ns, Namespaces.XS_NS_PREFIX, "anyAttribute");
        anyAttr.setAttribute("namespace", "##other");
        anyAttr.setAttribute("processContents", "lax");
        return anyAttr;
    }
    
    public static void init() throws ParserConfigurationException {
        docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        db = docFactory.newDocumentBuilder();
    }
    
    public static Document read(final InputStream is) throws SAXException, IOException {
        assert db != null : "class not initialized";
        return db.parse(is);
    }
    
    public static void write(final Document doc, final OutputStream os) throws IOException {
        final OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        format.setIndent(2);
        final XMLSerializer serializer = new XMLSerializer(os, format);
        serializer.serialize(doc);
    }
    
    public static Document newDocument() {
        assert db != null : "class not initialized";
        return db.newDocument();
    }
}
