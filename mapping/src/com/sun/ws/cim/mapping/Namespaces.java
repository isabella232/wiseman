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
 * $Id: Namespaces.java,v 1.1 2006-06-21 00:32:36 akhilarora Exp $
 */

package com.sun.ws.cim.mapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;

public final class Namespaces implements NamespaceContext {
    
    public static final String COLON = ":";
    
    public static final String XS_NS_PREFIX = "xs";
    public static final String XS_NS_URI = "http://www.w3.org/2001/XMLSchema";
    
    public static final String XSI_NS_PREFIX = "xsi";
    public static final String XSI_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";
    
    public static final String WSDL_NS_PREFIX = "wsdl";
    public static final String WSDL_NS_URI = "http://schemas.xmlsoap.org/wsdl/";
    
    public static final String WSA_NS_PREFIX = "wsa";
    public static final String WSA_NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    public static final String WSA_NS_LOCATION = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    
    public static final String CIM_COMMON_NS_PREFIX = "cim";
    public static final String CIM_COMMON_NS_URI = "http://schemas.dmtf.org/wbem/wscim/1/common";
    public static final String CIM_COMMON_NS_LOCATION = "http://schemas.dmtf.org/wbem/wscim/1/common.xsd";
    
    public static final String CLASS_XSD_NS_PREFIX = "cls";
    public static final String CLASS_WSDL_NS_PREFIX = "cld";
    private static final String CLASS_NS_BASE_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/%s/%s";
    private static final String CLASS_NS_BASE_LOCATION = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/%s/%s.%s";
    public final String CLASS_XSD_NS_URI;
    public final String CLASS_XSD_NS_LOCATION;
    public final String CLASS_WSDL_NS_URI;
    public final String CLASS_WSDL_NS_LOCATION;
    
    public static final String CIMQ_NS_PREFIX = "cimQ";
    private static final String CIMQ_NS_BASE_URI = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/%s/qualifiers";
    private static final String CIMQ_NS_BASE_LOCATION = "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/%s/qualifiers.xsd";
    public final String CIMQ_NS_URI;
    public final String CIMQ_NS_LOCATION;
    
    public static final String VENDOR_QUALIFIER_NS_PREFIX = "vendorQ";
    private static final String VENDOR_QUALIFIER_NS_BASE_URI = "http://schemas.vendor.com/wbem/wscim/1/cim-schema/%s/qualifiers";
    private static final String VENDOR_QUALIFIER_NS_BASE_LOCATION = "http://schemas.vendor.com/wbem/wscim/1/cim-schema/%s/qualifiers.xsd";
    public final String VENDOR_QUALIFIER_NS_URI;
    public final String VENDOR_QUALIFIER_NS_LOCATION;
    
    public final String CIM_VERSION;
    public final String CIM_VERSION_MAJOR;
    
    // key:prefix value:URI
    private final Map<String, String> namespaces = new HashMap<String, String>();
    
    private static String printf(final String pattern, final String... values) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.printf(pattern, (Object[]) values);
        return sw.toString();
    }
    
    private Namespaces(final Properties props, 
            final String cimClassName) throws IOException {
        
        CIM_VERSION = props.getProperty("cim.version");
        if (CIM_VERSION == null) {
            throw new IOException("property cim.version not found in generated properties");
        }
        CIM_VERSION_MAJOR = CIM_VERSION.split("\\.")[0];

        CIMQ_NS_URI = printf(CIMQ_NS_BASE_URI, CIM_VERSION_MAJOR);
        CIMQ_NS_LOCATION = printf(CIMQ_NS_BASE_LOCATION, CIM_VERSION_MAJOR);
        CLASS_XSD_NS_URI = printf(CLASS_NS_BASE_URI, CIM_VERSION_MAJOR, cimClassName);
        CLASS_XSD_NS_LOCATION = printf(CLASS_NS_BASE_LOCATION, CIM_VERSION_MAJOR, cimClassName, "xsd");
        CLASS_WSDL_NS_URI = printf(CLASS_NS_BASE_URI, CIM_VERSION_MAJOR, cimClassName);
        CLASS_WSDL_NS_LOCATION = printf(CLASS_NS_BASE_LOCATION, CIM_VERSION_MAJOR, cimClassName, "wsdl");
        VENDOR_QUALIFIER_NS_URI = printf(VENDOR_QUALIFIER_NS_BASE_URI, CIM_VERSION_MAJOR);
        VENDOR_QUALIFIER_NS_LOCATION = printf(VENDOR_QUALIFIER_NS_BASE_LOCATION, CIM_VERSION_MAJOR);
        
        namespaces.put(WSA_NS_PREFIX, WSA_NS_URI);
    }
    
    public static Namespaces createSchemaNamespaces(final Properties props, 
            final String cimClassName) throws IOException {

        Namespaces ns = new Namespaces(props, cimClassName);

        ns.namespaces.put(CLASS_XSD_NS_PREFIX, ns.CLASS_XSD_NS_URI);
        ns.namespaces.put(CIM_COMMON_NS_PREFIX, CIM_COMMON_NS_URI);
        ns.namespaces.put(XS_NS_PREFIX, XS_NS_URI);
        ns.namespaces.put(XSI_NS_PREFIX, XSI_NS_URI);
        
        return ns;
    }
    
    public static Namespaces createInterfaceNamespaces(final Properties props,
            final String cimClassName) throws IOException {

        Namespaces ns = new Namespaces(props, cimClassName);
        
        ns.namespaces.put(CLASS_XSD_NS_PREFIX, ns.CLASS_XSD_NS_URI);
        ns.namespaces.put(CLASS_WSDL_NS_PREFIX, ns.CLASS_WSDL_NS_URI);
        ns.namespaces.put(WSDL_NS_PREFIX, WSDL_NS_URI);
        ns.namespaces.put(XS_NS_PREFIX, XS_NS_URI);
        ns.namespaces.put(XSI_NS_PREFIX, XSI_NS_URI);
        
        return ns;
    }
    
    public static Namespaces createQualifierNamespaces(final Properties props, 
            final String cimClassName) throws IOException {

        Namespaces ns = new Namespaces(props, cimClassName);

        ns.namespaces.put(CLASS_XSD_NS_PREFIX, ns.CLASS_XSD_NS_URI);
        ns.namespaces.put(CIM_COMMON_NS_PREFIX, CIM_COMMON_NS_URI);
        ns.namespaces.put(CIMQ_NS_PREFIX, ns.CIMQ_NS_URI);
        ns.namespaces.put(XS_NS_PREFIX, XS_NS_URI);
        ns.namespaces.put(XSI_NS_PREFIX, XSI_NS_URI);
        ns.namespaces.put(VENDOR_QUALIFIER_NS_PREFIX, ns.VENDOR_QUALIFIER_NS_URI);
        
        return ns;
    }
    
    public String put(final String prefix, final String uri) {
        return namespaces.put(prefix, uri);
    }
    
    public String getURI(final String prefix) {
        return namespaces.get(prefix);
    }
    
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(namespaces.keySet());
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
