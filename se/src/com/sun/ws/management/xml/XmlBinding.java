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
 * $Id: XmlBinding.java,v 1.4 2006-05-01 23:32:24 akhilarora Exp $
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
import org.w3c.dom.Node;

public final class XmlBinding implements ValidationEventHandler {
    
    private static final String DEFAULT_PACKAGES =
            "org.w3._2003._05.soap_envelope:" +
            "org.xmlsoap.schemas.ws._2004._08.addressing:" +
            "org.xmlsoap.schemas.ws._2004._08.eventing:" +
            "org.xmlsoap.schemas.ws._2004._09.enumeration:" +
            "org.xmlsoap.schemas.ws._2004._09.transfer:" +
            "org.xmlsoap.schemas.ws._2005._06.wsmancat:" +
            "org.dmtf.schemas.wbem.wsman._1.wsman";
    
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;
    
    private FaultException validationException;
    
    public XmlBinding(final String... customPackages) throws JAXBException {
        StringBuilder packageNames = new StringBuilder(DEFAULT_PACKAGES);
        for (final String p : customPackages) {
            packageNames.append(":");
            packageNames.append(p);
        }
        final JAXBContext context = JAXBContext.newInstance(packageNames.toString());
        marshaller = context.createMarshaller();
        unmarshaller = context.createUnmarshaller();
        unmarshaller.setEventHandler(this);
    }
    
    public void marshal(final Object obj, final Node node) throws JAXBException {
        marshaller.marshal(obj, node);
    }
    
    public synchronized Object unmarshal(final Node node) throws JAXBException {
        validationException = null;
        final Object obj = unmarshaller.unmarshal(node);
        if (validationException != null) {
            throw validationException;
        }
        return obj;
    }
    
    public boolean handleEvent(final ValidationEvent event) {
        validationException = new SchemaValidationErrorFault(event.getMessage());
        return false;
    }
}