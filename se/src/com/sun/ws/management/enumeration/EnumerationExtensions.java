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
 * $Id: EnumerationExtensions.java,v 1.1 2006-05-02 20:37:10 akhilarora Exp $
 */

package com.sun.ws.management.enumeration;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;

public class EnumerationExtensions extends Enumeration {
    
    public static final QName ENUMERATION_MODE = new QName(NS_URI, "EnumerationMode", NS_PREFIX);
    
    public enum Mode { 
        EnumerateEPR("EnumerateEPR"), 
        EnumerateObjectAndEPR("EnumerateObjectAndEPR");
        
        public static Mode fromBinding(final JAXBElement<EnumerationModeType> t) { 
            return valueOf(t.getValue().value()); 
        }

        private String mode;
        Mode(final String m) { mode = m; }
        public JAXBElement<EnumerationModeType> toBinding() {
            return Management.FACTORY.createEnumerationMode(EnumerationModeType.fromValue(mode));
        }
    }
    
    public EnumerationExtensions() throws SOAPException {
        super();
    }
    
    public EnumerationExtensions(final Addressing addr) throws SOAPException {
        super(addr);
    }
    
    public EnumerationExtensions(final InputStream is) throws SOAPException, IOException {
        super(is);
    }
    
    public void setEnumerate(final EndpointReferenceType endTo, 
            final String expires, final FilterType filter, final Mode mode) 
            throws JAXBException, SOAPException {
        super.setEnumerate(endTo, expires, filter, mode.toBinding());
    }
}
