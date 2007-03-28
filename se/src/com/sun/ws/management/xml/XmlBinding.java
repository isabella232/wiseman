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
 * $Id: XmlBinding.java,v 1.18 2007-03-28 14:23:09 jfdenise Exp $
 */

package com.sun.ws.management.xml;

import com.sun.ws.management.SchemaValidationErrorFault;
import com.sun.ws.management.server.WSManAgent;
import com.sun.ws.management.soap.FaultException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class XmlBinding {
    
    private static final String[] DEFAULT_PACKAGES = {
        "org.w3._2003._05.soap_envelope",
        "org.xmlsoap.schemas.ws._2004._08.addressing",
        "org.xmlsoap.schemas.ws._2004._08.eventing",
        "org.xmlsoap.schemas.ws._2004._09.enumeration",
        "org.xmlsoap.schemas.ws._2004._09.transfer",
        "org.dmtf.schemas.wbem.wsman._1.wsman"
    };
    
    private static final String BINDING_PROPERTIES_FILE = "/binding.properties";
    public static final String CUSTOM_PACKAGE_NAMES =
            XmlBinding.class.getPackage().getName() + ".custom.packagenames";
    public static final String CUSTOM_SCHEMA_NAMES =
            XmlBinding.class.getPackage().getName() + ".custom.schemas";
    public static final String VALIDATE =
            XmlBinding.class.getPackage().getName() + ".validate";
    
    private JAXBContext context;
    private Schema schema;
    private boolean validate;
    private Set packageNamesHandled = new HashSet<String>();
    
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
    
    
    public XmlBinding(final Schema schema, String... customPackages)
    throws JAXBException {
        init(schema, null, null, null, 
             false,
             customPackages);
    }
    
    public XmlBinding(final Schema schema, Map<String, String> bindingConf)
    throws JAXBException {
        init(schema, null, bindingConf, 
                Thread.currentThread().getContextClassLoader(), false);
    }
    
    public XmlBinding(final Source[] customSchemas, Map<String, String> bindingConf,
            ClassLoader loader,
            String... customPackages)
            throws JAXBException {
        init(null, customSchemas, bindingConf, 
             loader,true, customPackages);
    }
    
    public XmlBinding(Schema schema, Source[] customSchemas,
            Map<String, String> bindingConf,
            ClassLoader loader,
            String... customPackages)
            throws JAXBException {
         init(schema, customSchemas, bindingConf, loader, true,
              customPackages);
    }
    
    private void init(Schema schema, Source[] customSchemas,
            Map<String, String> bindingConf,
            ClassLoader loader, boolean validation,
            String... customPackages) throws JAXBException {
        
        if(loader == null)
            loader = Thread.currentThread().getContextClassLoader();
   
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
        
        final Map<String, String> propertySet = new HashMap<String, String>();
        WSManAgent.getProperties(BINDING_PROPERTIES_FILE, propertySet);
        // Put all passed properties
        if(bindingConf != null)
            propertySet.putAll(bindingConf);
        String customPackageNames = System.getProperty(CUSTOM_PACKAGE_NAMES);
        if(customPackageNames == null || customPackageNames.equals("")) {
            customPackageNames = propertySet.get(CUSTOM_PACKAGE_NAMES);
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
        
        context = JAXBContext.newInstance(packageNames.toString(), loader);
        
        // Compute a schema based on passed sources and custom ones.
        if(schema == null && validation) {
            String customSchemaNames = System.getProperty(CUSTOM_SCHEMA_NAMES);
            if(customSchemaNames == null)
                customSchemaNames = propertySet.get(CUSTOM_SCHEMA_NAMES);
            Source[] customSchemas1 = null;
            if(customSchemaNames != null)
                customSchemas1 = WSManAgent.newSources(customSchemaNames, "/");
            Source[] finalSchemas = null;
            if(customSchemas != null || customSchemas1 != null) {
                finalSchemas = customSchemas ==  null ? customSchemas1 : customSchemas;
                if(customSchemas != null && customSchemas1 != null) {
                    // Need to merge both
                    finalSchemas = new Source[customSchemas.length +
                            customSchemas1.length];
                    System.arraycopy(customSchemas1,0,finalSchemas,0,customSchemas1.length);
                    System.arraycopy(customSchemas,0,finalSchemas,customSchemas1.length,
                            customSchemas.length);
                }
            }
            try {
                schema = WSManAgent.createSchema(finalSchemas);
            }catch(SAXException sx) {
                sx.printStackTrace();
                throw new IllegalArgumentException("Invalid schema, " +
                        "can't validate. " + sx);
            }
        } 
        
        this.schema = schema;
        
        // Allow enabling and disabling validation via properties
        if (this.schema != null) {
            String doValidate = null;
            // Check System properties for validate flag first
            doValidate = System.getProperty(VALIDATE);
            if ((doValidate == null) || (doValidate.length() == 0)) {
                // Check for the validation flag in 'binding.properties'
                doValidate = propertySet.get(VALIDATE);
            }
            if(doValidate == null)
                this.validate = true;
            else
                this.validate = Boolean.getBoolean(doValidate);
        } else
            this.validate = false;
    }
    
    public void marshal(final Object obj, final Node node) throws JAXBException {
        final Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(obj, node);
    }
    
    public Object unmarshal(final Node node) throws JAXBException {
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        if (this.validate) {
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
        return this.validate;
    }
    
    public boolean isPackageHandled(final String pkg) {
        return packageNamesHandled.contains(pkg);
    }
}
