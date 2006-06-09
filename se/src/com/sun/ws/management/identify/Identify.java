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
 * $Id: Identify.java,v 1.1 2006-06-09 18:49:15 akhilarora Exp $
 */

package com.sun.ws.management.identify;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.soap.SOAP;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import org.dmtf.schemas.wbem.wsman.identity._1.wsmanidentity.IdentifyResponseType;
import org.dmtf.schemas.wbem.wsman.identity._1.wsmanidentity.ObjectFactory;

public class Identify extends SOAP {
    
    public static final String NS_PREFIX = "id";
    public static final String NS_URI = "http://schemas.dmtf.org/wbem/wsman/identity/1/wsmanidentity.xsd";
    
    public static final QName IDENTIFY = new QName(NS_URI, "Identify", NS_PREFIX);
    public static final QName IDENTIFY_RESPONSE = new QName(NS_URI, "IdentifyResponse", NS_PREFIX);

    public static final ObjectFactory FACTORY = new ObjectFactory();
    
    public Identify() throws SOAPException {
        super();
    }
    
    public Identify(final Addressing addr) throws SOAPException {
        super(addr);
    }
    
    public Identify(final InputStream is) throws SOAPException, IOException {
        super(is);
    }
    
    public void setIdentifyResponse(final String vendor, final String productVersion,
            final String protocolVersion, final Map<QName, String> more) throws SOAPException, JAXBException {
        final IdentifyResponseType ir = FACTORY.createIdentifyResponseType();
        final List<Object> any = ir.getAny();
        any.add(FACTORY.createProductVendor(vendor));
        any.add(FACTORY.createProductVersion(productVersion));
        any.add(FACTORY.createProtocolVersion(protocolVersion));
        if (more != null) {
            final Iterator<Entry<QName, String> > mi = more.entrySet().iterator();
            while (mi.hasNext()) {
                final Entry<QName, String> entry = mi.next();
                any.add(new JAXBElement<String>(entry.getKey(), String.class, null, entry.getValue()));
            }
        }
        final JAXBElement<IdentifyResponseType> re = FACTORY.createIdentifyResponse(ir);
        getXmlBinding().marshal(re, getBody());
    }

    public IdentifyResponseType getIdentifyResponse() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), IDENTIFY_RESPONSE);
        return value == null ? null : ((JAXBElement<IdentifyResponseType>) value).getValue();
    }
}
