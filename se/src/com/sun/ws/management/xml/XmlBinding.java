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
 * $Id: XmlBinding.java,v 1.15 2007-01-22 12:48:58 denis_rachal Exp $
 */

package com.sun.ws.management.xml;

import com.sun.ws.management.SchemaValidationErrorFault;
import com.sun.ws.management.soap.FaultException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;

public final class XmlBinding {
    
    private static final String[] DEFAULT_PACKAGES = {
            "org.w3._2003._05.soap_envelope",
            "org.xmlsoap.schemas.ws._2004._08.addressing",
            "org.xmlsoap.schemas.ws._2004._08.eventing",
            "org.xmlsoap.schemas.ws._2004._09.enumeration",
            "org.xmlsoap.schemas.ws._2004._09.transfer",
            "org.dmtf.schemas.wbem.wsman._1.wsman"
    };
    
    private static final Properties BINDING_PROPERTIES = new Properties();
    private static final String BINDING_PROPERTIES_FILE = "/binding.properties";
    private static final String CUSTOM_PACKAGE_NAMES = 
            XmlBinding.class.getPackage().getName() + ".custom.packagenames";
    
    final JAXBContext context;
    final Schema schema;
    final Set packageNamesHandled = new HashSet<String>();
    
    private static final class ValidationHandler implements ValidationEventHandler {
        
        private FaultException validationException = null;
        
        public boolean handleEvent(final ValidationEvent event) {
            validationException = new SchemaValidationErrorFault(event.getMessage());
            // stop at the first validation error
            return false;
        }
        
        public FaultException getFault() {
            return validationException;
        }
    }
    
    public XmlBinding(final Schema schema, final String... customPackages) throws JAXBException {
        
        final StringBuilder packageNames = new StringBuilder();
        boolean first = true;
        for (final String p : DEFAULT_PACKAGES) {
            if (first) {
                first = false;
            } else {
                packageNames.append(":");
            }
            packageNames.append(p);
            packageNamesHandled.add(p);
        }
        
        final InputStream ism = XmlBinding.class.getResourceAsStream(BINDING_PROPERTIES_FILE);
        if (ism != null) {
            try {
                BINDING_PROPERTIES.load(ism);
            } catch (IOException ex) {
                throw new JAXBException(ex);
            }
        }
        
        // Check System properties first to allow command line override
        String customPackageNames = System.getProperty(CUSTOM_PACKAGE_NAMES);
        if (customPackageNames == null || customPackageNames.equals("")) {
            customPackageNames = (String) BINDING_PROPERTIES.get(CUSTOM_PACKAGE_NAMES);
        }
        if (customPackageNames != null && !customPackageNames.equals("")) {
            for (final String packageName : customPackageNames.split(",")) {
                final String pkg = packageName.trim();
                packageNames.append(":");
                packageNames.append(pkg);
                packageNamesHandled.add(pkg);
            }
        }
        
        for (final String p : customPackages) {
            packageNames.append(":");
            packageNames.append(p);
            packageNamesHandled.add(p);
        }
        
        context = JAXBContext.newInstance(packageNames.toString(),
                Thread.currentThread().getContextClassLoader());
        
        this.schema = schema;
    }
    
    public void marshal(final Object obj, final Node node) throws JAXBException {
        final Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(obj, node);
    }
    
    public Object unmarshal(final Node node) throws JAXBException {
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        if (schema != null) {
            unmarshaller.setSchema(schema);
        }
        final ValidationHandler handler = new ValidationHandler();
        unmarshaller.setEventHandler(handler);
        final Object obj = unmarshaller.unmarshal(node);
        final FaultException fault = handler.getFault();
        if (fault != null) {
            throw fault;
        }
        return obj;
    }    
    
    public boolean isValidating() {
        return schema != null;
    }
    
    public boolean isPackageHandled(final String pkg) {
        return packageNamesHandled.contains(pkg);
    }
}
