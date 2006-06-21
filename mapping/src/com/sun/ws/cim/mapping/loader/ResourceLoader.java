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
 * $Id: ResourceLoader.java,v 1.1 2006-06-21 00:32:37 akhilarora Exp $
 */

package com.sun.ws.cim.mapping.loader;

import com.sun.ws.cim.mapping.Dom;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMElement;
import javax.wbem.cimxml.CIMXmlUtilFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class ResourceLoader {
    
    public static final String XML_SUFFIX = ".xml";
    protected static final int XML_SUFFIX_LENGTH = XML_SUFFIX.length();
    
    protected static final String PREFIX = "/";
    protected static final int PREFIX_LENGTH = PREFIX.length();
    
    private static final WeakHashMap<String, Document> docCache = new WeakHashMap<String, Document>();
    private static final WeakHashMap<String, String> xmlCache = new WeakHashMap<String, String>();
    
    protected InputStream load(final String resource) throws IOException {
        InputStream is = loadResource(resource);
        if (is == null) {
            // try again with the prefix - needed with jars
            final String fullResourceName = PREFIX + resource;
            is = loadResource(fullResourceName);
            if (is == null) {
                throw new IOException("Resource not found: " + resource);
            }
        }
        return is;
    }
    
    public CIMClass loadCimClass(final String cimClassName)
    throws IOException, SAXException, ParserConfigurationException {
        final String xml = loadResourceAsString(cimClassName + XML_SUFFIX);
        final CIMElement ce = CIMXmlUtilFactory.getCIMXmlUtil().getCIMElement(xml);
        if (! (ce instanceof CIMClass)) {
            throw new IOException("CIMElement " + ce.getName() + " is not a CIM class");
        }
        return (CIMClass) ce;
    }
    
    public Properties loadGeneratedProperties() throws IOException {
        final String resource = PREFIX + "gen.properties";
        final InputStream is = loadResource(resource);
        if (is == null) {
            throw new IOException("Properties not found: " + resource);
        }
        Properties props = new Properties();
        props.load(new BufferedInputStream(is));
        return props;
    }
    
    public Document loadResourceAsDocument(final String resource) throws IOException, SAXException {
        Document doc = docCache.get(resource);
        if (doc != null) {
            return doc;
        }
        doc = Dom.read(new BufferedInputStream(load(resource)));
        docCache.put(resource, doc);
        return doc;
    }
    
    public String loadResourceAsString(final String resource) throws IOException {
        String xml = xmlCache.get(resource);
        if (xml != null) {
            return xml;
        }
        BufferedInputStream bis = new BufferedInputStream(load(resource));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int nread = 0;
        while ((nread = bis.read(buffer)) > 0) {
            baos.write(buffer, 0, nread);
        }
        xml = new String(baos.toByteArray());
        xmlCache.put(resource, xml);
        return xml;
    }
    
    public String[] listResources() throws IOException {
        return listResources(XML_SUFFIX, XML_SUFFIX_LENGTH);
    }
    
    public String[] listResources(final String suffix, final int suffixLength) throws IOException {
        ArrayList<String> collection = new ArrayList<String>(0);
        final Set rs = getResourcePaths(PREFIX);
        if (rs != null) {
            collection = new ArrayList<String>(rs.size());
            Iterator ri = rs.iterator();
            while (ri.hasNext()) {
                String rps = ri.next().toString();
                if (rps.startsWith(PREFIX)) {
                    rps = rps.substring(PREFIX_LENGTH);
                }
                final int ext = rps.lastIndexOf(suffix);
                if (ext > 0) {
                    final String resource = rps.substring(0, rps.length() - suffixLength);
                    if (!"mof".equals(resource) && !"bigmof".equals(resource)) {
                        collection.add(resource);
                    }
                }
            }
        }
        Collections.sort(collection);
        String[] ret = new String[collection.size()];
        return collection.toArray(ret);
    }
    
    public abstract InputStream loadResource(final String resource) throws IOException;
    public abstract Set getResourcePaths(final String prefix) throws IOException;
}
