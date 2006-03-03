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
 * $Id: SOAPTest.java,v 1.3 2006-03-03 22:52:29 akhilarora Exp $
 */

package management;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transport.HttpClient;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Unit test for SOAP
 */
public class SOAPTest extends TestBase {
    
    public SOAPTest(final String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(SOAPTest.class);
        return suite;
    }
    
    public void testNotUnderstood() throws Exception {
        
        final Addressing addr = new Addressing();
        addr.setAction(Transfer.GET_ACTION_URI);
        addr.setTo(DESTINATION);
        addr.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        addr.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final Management mgmt = new Management(addr);
        mgmt.setResourceURI("wsman:test/not_understood");
        
        addr.prettyPrint(logfile);
        Addressing response = HttpClient.sendRequest(addr);
        response.prettyPrint(logfile);
        if (!response.getBody().hasFault()) {
            fail("fault not returned");
        }
        
        final Fault fault = new SOAP(response).getFault();
        assertEquals(Addressing.FAULT_ACTION_URI, response.getAction());
        assertEquals(SOAP.MUST_UNDERSTAND, fault.getCode().getValue());
        assertNull(fault.getCode().getSubcode());
        assertEquals(SOAP.NOT_UNDERSTOOD_REASON, fault.getReason().getText().get(0).getValue());
        
        boolean found = false;
        for (final SOAPElement hdr : response.getHeaders()) {
            if (SOAP.NOT_UNDERSTOOD.equals(hdr.getElementQName())) {
                found = true;
                final NamedNodeMap attrs = hdr.getAttributes();
                for (int i = attrs.getLength() - 1; i >= 0; i--) {
                    final Node attr = attrs.item(i);
                    final String attrName = attr.getNodeName();
                    final String attrValue = attr.getNodeValue();
                    if (attrName.equals("qname")) {
                        assertEquals(Addressing.MESSAGE_ID.getPrefix() + Addressing.COLON + Addressing.MESSAGE_ID.getLocalPart(), attrValue);
                    } else if (attrName.equals("xmlns:ns")) {
                        assertEquals(Addressing.NS_URI, attrValue);
                    }
                }
            }
        }
        assertTrue("The header that's not understood is not found in the fault headers", found);
    }
}
