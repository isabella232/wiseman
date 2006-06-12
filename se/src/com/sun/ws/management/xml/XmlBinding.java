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
 * $Id: XmlBinding.java,v 1.8 2006-06-12 23:53:57 akhilarora Exp $
 */

package com.sun.ws.management.xml;

import com.sun.ws.management.SchemaValidationErrorFault;
import com.sun.ws.management.soap.FaultException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;

public final class XmlBinding {
    
    private static final String DEFAULT_PACKAGES =
            "org.w3._2003._05.soap_envelope:" +
            "org.xmlsoap.schemas.ws._2004._08.addressing:" +
            "org.xmlsoap.schemas.ws._2004._08.eventing:" +
            "org.xmlsoap.schemas.ws._2004._09.enumeration:" +
            "org.xmlsoap.schemas.ws._2004._09.transfer:" +
            "org.dmtf.schemas.wbem.wsman.identity._1.wsmanidentity:" +
            "org.dmtf.schemas.wbem.wsman._1.wsman";
    
    final JAXBContext context;
    Schema schema = null;
    
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
        StringBuilder packageNames = new StringBuilder(DEFAULT_PACKAGES);
        for (final String p : customPackages) {
            packageNames.append(":");
            packageNames.append(p);
        }
        context = JAXBContext.newInstance(packageNames.toString(),
                Thread.currentThread().getContextClassLoader());
        if (schema != null) {
            this.schema = schema;
        }
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
}
