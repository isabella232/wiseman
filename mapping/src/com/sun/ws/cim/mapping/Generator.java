/*
 * Copyright 2006 Sun Microsystems, Inc.
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
 * $Id: Generator.java,v 1.1 2006-06-21 00:32:36 akhilarora Exp $
 */

package com.sun.ws.cim.mapping;

import com.sun.ws.cim.CimClass;
import com.sun.ws.cim.CimMethod;
import com.sun.ws.cim.CimProperty;
import com.sun.ws.cim.mapping.loader.ResourceLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.wbem.cim.CIMClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class Generator {
    
    private static final String TARGET_NAMESPACE = "targetNamespace";
    private static final String ELEMENT_FORM_DEFAULT = "elementFormDefault";
    private static final String QUALIFIED = "qualified";
    private static final String IMPORT = "import";
    private static final String SCHEMA_LOCATION = "schemaLocation";
    private static final String NAMESPACE = "namespace";
    private static final String XMLNS = "xmlns";

    private final ResourceLoader loader;
    private final Properties genprops;
    private final String GENERATED;
    private final String GENERATOR;
    
    public Generator(final ResourceLoader loader) throws IOException {
        this.loader = loader;
        genprops = loader.loadGeneratedProperties();
        GENERATED = " generated " + new Date().toString() + " ";
        GENERATOR = " generator https://wiseman.dev.java.net build " +
                genprops.getProperty("build.tstamp") + " ";
    }
    
    public void generate(final String cimClassName, 
            final Document xsd, final Document wsdl, final Document qualifiers)
    throws Exception {
        
        final CIMClass cimClass = loader.loadCimClass(cimClassName);
        final CimClass root = new CimClass(cimClass);
        
        final List<CimClass> classHierarchy = new ArrayList<CimClass>();
        scanClassHierarchy(classHierarchy, cimClass);
        
        // the LinkedHashSet will preserve the insertion order, so that properties
        // and methods appear in the order they are encountered in the src doc
        final Set<CimProperty> properties = new LinkedHashSet<CimProperty>();
        final Set<CimMethod> methods = new LinkedHashSet<CimMethod>();
        
        // scan the list in reverse order - root first, most derived class at the end
        final int classHierarchySize = classHierarchy.size();
        for (int i = classHierarchySize - 1; i >= 0; i--) {
            scanProperties(classHierarchy.get(i), properties);
            scanMethods(classHierarchy.get(i), methods);
        }
        
        final Namespaces schemaNamespaces = 
                Namespaces.createSchemaNamespaces(genprops, cimClassName);
        generateHeader(xsd);
        generateSchema(root, xsd, schemaNamespaces, properties, methods);
        
        final Namespaces interfaceNamespaces = 
                Namespaces.createInterfaceNamespaces(genprops, cimClassName);
        generateHeader(wsdl);
        generateInterface(root, wsdl, interfaceNamespaces, methods);
        
        final Namespaces qualifierNamespaces = 
                Namespaces.createQualifierNamespaces(genprops, cimClassName);
        generateHeader(qualifiers);
        generateQualifiers(root, qualifiers, qualifierNamespaces, properties, methods);
    }
    
    private void scanClassHierarchy(final List<CimClass> classHierarchy,
            final CIMClass cimClass) throws Exception {
        
        // append - this will create a list with the most-derived class first and root last
        classHierarchy.add(new CimClass(cimClass));
        
        final String superClassName = cimClass.getSuperClass();
        if (superClassName != null && !"".equals(superClassName)) {
            final CIMClass superClass = loader.loadCimClass(superClassName);
            scanClassHierarchy(classHierarchy, superClass);
        }
    }
    
    private void scanProperties(final CimClass cc, final Set<CimProperty> allProperties) {
        // TODO: check for override, presently subclasses always override
        for (CimProperty cp : cc.getProperties()) {
            if (allProperties.contains(cp)) {
                allProperties.remove(cp);
            }
            if (!allProperties.add(cp)) {
                throw new RuntimeException("Failed to override Property " +
                        cp.getName() + " in " + cc.getName());
            }
        }
    }
    
    private void scanMethods(final CimClass cc, final Set<CimMethod> allMethods) {
        // TODO: check for override, presently subclasses always override
        for (CimMethod method : cc.getMethods()) {
            if (allMethods.contains(method)) {
                allMethods.remove(method);
            }
            if (!allMethods.add(method)) {
                throw new RuntimeException("Failed to override Method " +
                        method.getName() + " in " + cc.getName());
            }
        }
    }
    
    private void generateHeader(final Document doc) {
        doc.appendChild(doc.createComment(GENERATOR));
        doc.appendChild(doc.createComment(GENERATED));
    }
    
    private void generateSchema(final CimClass cimClass, final Document xsd, 
            final Namespaces ns, final Set<CimProperty> properties, 
            final Set<CimMethod> methods) {

        final Element root = Dom.createElement(xsd, ns, Namespaces.XS_NS_PREFIX, "schema");
        root.setAttribute(TARGET_NAMESPACE, ns.getURI(Namespaces.CLASS_XSD_NS_PREFIX));
        final Iterator<String> nsi = ns.getKeys().iterator();
        while (nsi.hasNext()) {
            final String prefix = nsi.next();
            final String uri = ns.getURI(prefix);
            root.setAttribute(XMLNS + Namespaces.COLON + prefix, uri);
        }
        root.setAttribute(ELEMENT_FORM_DEFAULT, QUALIFIED);
        xsd.appendChild(root);
        
        final Element wsaImport = Dom.createElement(xsd, ns, Namespaces.XS_NS_PREFIX, IMPORT);
        wsaImport.setAttribute(NAMESPACE, Namespaces.WSA_NS_URI);
        root.appendChild(wsaImport);

        final Element cimBaseImport = Dom.createElement(xsd, ns, Namespaces.XS_NS_PREFIX, IMPORT);
        cimBaseImport.setAttribute(NAMESPACE, Namespaces.CIM_COMMON_NS_URI);
        // TODO: remove when done debugging
        cimBaseImport.setAttribute(SCHEMA_LOCATION, "common.xsd");
        root.appendChild(cimBaseImport);
        
        final Element cimQImport = Dom.createElement(xsd, ns, Namespaces.XS_NS_PREFIX, IMPORT);
        cimQImport.setAttribute(NAMESPACE, ns.CIMQ_NS_URI);
        root.appendChild(cimQImport);
        
        // generate the GEDs
        
        for (final Iterator<CimProperty> pi = properties.iterator(); pi.hasNext();) {
            pi.next().generate(root, xsd, ns);
        }
        for (final Iterator<CimMethod> mi = methods.iterator(); mi.hasNext();) {
            mi.next().generate(root, xsd, ns);
        }
        
        // now generate the class with references to the GEDs
        
        final Element complexType = Dom.createElement(xsd, ns, Namespaces.XS_NS_PREFIX, "complexType");
        complexType.setAttribute("name", cimClass.getName() + "_Type");
        root.appendChild(complexType);
        
        final Element sequence = Dom.createElement(xsd, ns, Namespaces.XS_NS_PREFIX, "sequence");
        complexType.appendChild(sequence);
        
        for (final Iterator<CimProperty> pi = properties.iterator(); pi.hasNext();) {
            pi.next().generateReference(root, xsd, ns, sequence);
        }
        
        final Element any = Dom.createAny(xsd, ns);
        sequence.appendChild(any);
        
        final Element anyAttribute = Dom.createAnyAttribute(xsd, ns);
        complexType.appendChild(anyAttribute);
    }

    private void generateInterface(final CimClass cimClass, 
            final Document wsdl, final Namespaces ns, final Set<CimMethod> methods) {
        
        final Element root = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, "definitions");
        root.setAttribute(TARGET_NAMESPACE, ns.getURI(Namespaces.CLASS_WSDL_NS_PREFIX));
        final Iterator<String> nsi = ns.getKeys().iterator();
        while (nsi.hasNext()) {
            final String prefix = nsi.next();
            final String uri = ns.getURI(prefix);
            root.setAttribute(XMLNS + Namespaces.COLON + prefix, uri);
        }
        root.setAttribute(ELEMENT_FORM_DEFAULT, QUALIFIED);
        wsdl.appendChild(root);
        
        final Element wsaImport = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, IMPORT);
        wsaImport.setAttribute(NAMESPACE, Namespaces.WSA_NS_URI);
        wsaImport.setAttribute(SCHEMA_LOCATION, Namespaces.WSA_NS_LOCATION);
        root.appendChild(wsaImport);
        
        final Element types = Dom.createElement(wsdl, ns, Namespaces.WSA_NS_PREFIX, "types");
        root.appendChild(types);
        
        final Element schema = Dom.createElement(wsdl, ns, Namespaces.XS_NS_PREFIX, "schema");
        types.appendChild(schema);
        
        final Element classImport = Dom.createElement(wsdl, ns, Namespaces.XS_NS_PREFIX, IMPORT);
        classImport.setAttribute(NAMESPACE, ns.CLASS_XSD_NS_URI);
        classImport.setAttribute(SCHEMA_LOCATION, ns.CLASS_XSD_NS_LOCATION);
        schema.appendChild(classImport);
        
        for (final Iterator<CimMethod> mi = methods.iterator(); mi.hasNext();) {
            mi.next().generateInterface(root, wsdl, ns);
        }
    }

    private void generateQualifiers(final CimClass cimClass, 
            final Document qualifiers, final Namespaces ns, 
            final Set<CimProperty> properties,
            final Set<CimMethod> methods) {
        
        final Element root = Dom.createElement(qualifiers, ns, Namespaces.VENDOR_QUALIFIER_NS_PREFIX, "qualifiers");
        root.setAttribute(TARGET_NAMESPACE, ns.getURI(Namespaces.VENDOR_QUALIFIER_NS_PREFIX));
        final Iterator<String> nsi = ns.getKeys().iterator();
        while (nsi.hasNext()) {
            final String prefix = nsi.next();
            final String uri = ns.getURI(prefix);
            root.setAttribute(XMLNS + Namespaces.COLON + prefix, uri);
        }
        root.setAttribute(ELEMENT_FORM_DEFAULT, QUALIFIED);
        qualifiers.appendChild(root);
        
        final Element classImport = Dom.createElement(qualifiers, ns, Namespaces.XS_NS_PREFIX, IMPORT);
        classImport.setAttribute(NAMESPACE, ns.CLASS_XSD_NS_URI);
        classImport.setAttribute(SCHEMA_LOCATION, ns.CLASS_XSD_NS_LOCATION);
        root.appendChild(classImport);
        
        final Element vendorImport = Dom.createElement(qualifiers, ns, Namespaces.XS_NS_PREFIX, IMPORT);
        vendorImport.setAttribute(NAMESPACE, ns.VENDOR_QUALIFIER_NS_URI);
        vendorImport.setAttribute(SCHEMA_LOCATION, ns.VENDOR_QUALIFIER_NS_LOCATION);
        root.appendChild(vendorImport);

        final Element classQualifier = Dom.createElement(qualifiers, ns, Namespaces.CIMQ_NS_PREFIX, cimClass.getName());
        cimClass.generateQualifiers(classQualifier, qualifiers, ns);
        root.appendChild(classQualifier);
        
        for (final Iterator<CimProperty> pi = properties.iterator(); pi.hasNext();) {
            final CimProperty property = pi.next();
            final Element propertyQualifier = Dom.createElement(qualifiers, ns, Namespaces.CIMQ_NS_PREFIX, property.getName());
            property.generateQualifiers(propertyQualifier, qualifiers, ns);
            classQualifier.appendChild(propertyQualifier);
        }
        for (final Iterator<CimMethod> mi = methods.iterator(); mi.hasNext();) {
            final CimMethod method = mi.next();
            final Element methodQualifier = Dom.createElement(qualifiers, ns, Namespaces.CIMQ_NS_PREFIX, method.getName());
            method.generateQualifiers(methodQualifier, qualifiers, ns);
            classQualifier.appendChild(methodQualifier);
        }
    }
}
