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
 * $Id: AddressingTest.java,v 1.5 2007-11-30 14:32:36 denis_rachal Exp $
 */

package management;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.transfer.Transfer;

/**
 * Unit test for WS-Addressing
 */
public class AddressingTest extends TestBase {
    
    public AddressingTest(final String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(AddressingTest.class);
        return suite;
    }
    
    public void testVisual() throws Exception {
        
        final Addressing addr = new Addressing();
        addr.setAction(Transfer.GET_ACTION_URI);
        addr.setTo(DESTINATION);
        addr.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        addr.setFaultTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        final String uuid = UUID_SCHEME + UUID.randomUUID().toString();
        addr.setMessageId(uuid);
        
        addr.getEnvelope().addNamespaceDeclaration(NS_PREFIX, NS_URI);
        
        final ReferencePropertiesType propsFrom = Addressing.FACTORY.createReferencePropertiesType();
        final Document tempDoc = addr.newDocument();
        final Element temperature = tempDoc.createElementNS(NS_URI, NS_PREFIX + ":" + "temperature");
        temperature.appendChild(tempDoc.createTextNode("75"));
        tempDoc.appendChild(temperature);
        propsFrom.getAny().add(tempDoc.getDocumentElement());
        
        final ReferenceParametersType paramsFrom = Addressing.FACTORY.createReferenceParametersType();
        final Document unitsDoc = addr.newDocument();
        final Element units = unitsDoc.createElementNS(NS_URI, NS_PREFIX + ":" + "units");
        units.setAttributeNS(NS_URI, NS_PREFIX + ":" + "type", "celsius");
        unitsDoc.appendChild(units);
        paramsFrom.getAny().add(unitsDoc.getDocumentElement());
        
        final AttributedQName portTypeFrom = Addressing.FACTORY.createAttributedQName();
        final QName portType = new QName(NS_URI, "thePortType", NS_PREFIX);
        portTypeFrom.setValue(portType);
        
        final ServiceNameType serviceNameFrom = Addressing.FACTORY.createServiceNameType();
        final String portName = "thePortName";
        serviceNameFrom.setPortName(portName);
        final QName serviceName = new QName(NS_URI, "theServiceName", NS_PREFIX);
        serviceNameFrom.setValue(serviceName);
        
        final String fromAddr = "https://client:8080/wsman/receiver";
        final EndpointReferenceType eprFrom =
                Addressing.createEndpointReference(fromAddr, propsFrom, paramsFrom, portTypeFrom, serviceNameFrom);
        addr.setFrom(eprFrom);
        
        // test that both forms work and that multiple RelatesTo are allowed
        addr.addRelatesTo(Addressing.UNSPECIFIED_MESSAGE_ID);
        addr.addRelatesTo(Addressing.UNSPECIFIED_MESSAGE_ID, new QName(Addressing.NS_URI, "Reply", "wsa"));
        
        // add ref params to the message header
        addr.addHeaders(paramsFrom);
        
        addr.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        addr.writeTo(bos);
        final Addressing a2 = new Addressing(new ByteArrayInputStream(bos.toByteArray()));

        assertEquals(Transfer.GET_ACTION_URI, a2.getAction());
        assertEquals(DESTINATION, a2.getTo());
        assertEquals(Addressing.ANONYMOUS_ENDPOINT_URI, a2.getReplyTo().getAddress().getValue());
        assertEquals(Addressing.ANONYMOUS_ENDPOINT_URI, a2.getFaultTo().getAddress().getValue());
        assertEquals(uuid, a2.getMessageId());
        
        final EndpointReferenceType epr2 = a2.getFrom();
        final ReferenceParametersType param2 = epr2.getReferenceParameters();
        final Element unit2 = (Element) param2.getAny().get(0);
        assertEquals(units.getNodeName(), unit2.getNodeName());
        assertEquals(units.getNamespaceURI(), unit2.getNamespaceURI());
        assertEquals(portType, epr2.getPortType().getValue());
        assertEquals(portName, epr2.getServiceName().getPortName());
        assertEquals(serviceName, epr2.getServiceName().getValue());
        assertEquals(fromAddr, epr2.getAddress().getValue());
        
        assertEquals(Addressing.UNSPECIFIED_MESSAGE_ID, a2.getRelatesTo()[0].getValue());
        
        // test whether ref params are added to the message header
        boolean foundRefParam = false;
        for (SOAPElement hdr : a2.getHeaders()) {
            if (units.getNodeName().equals(hdr.getNodeName()) &&
                    units.getNamespaceURI().equals(hdr.getNamespaceURI())) {
                foundRefParam = true;
                break;
            }
        }
        assertTrue(foundRefParam);
        
        // repeat test with examineAllHeaderElements SAAJ method
        foundRefParam = false;
        Iterator it = a2.getHeader().examineAllHeaderElements();
        while (it.hasNext()) {
            SOAPElement hdr = (SOAPElement) it.next();
            if (units.getNodeName().equals(hdr.getNodeName()) &&
                    units.getNamespaceURI().equals(hdr.getNamespaceURI())) {
                foundRefParam = true;
                break;
            }
        }
        assertTrue(foundRefParam);
    }
    
    public void testMissingAddress() throws Exception {
    
    	// Test that the createEndpointReference method checks for the required addressing param pre-condition
    	try {
            final EndpointReferenceType eprFrom =
                Addressing.createEndpointReference(null, null, null, null, null);
            

    	}
    	catch (IllegalArgumentException e) {
            // correct exception thrown 
            assertTrue(true);
    		
    	}
    	catch (NullPointerException e) {
            // correct exception not thrown 
            assertTrue("Null Address value not caught.", false);
    		
    	}
    }
}
