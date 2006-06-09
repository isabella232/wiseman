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
 * $Id: IdentifyTest.java,v 1.1 2006-06-09 18:49:14 akhilarora Exp $
 */

package management;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.transport.HttpClient;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import org.dmtf.schemas.wbem.wsman.identity._1.wsmanidentity.IdentifyResponseType;

/**
 * Unit test for the Identify operation
 */
public class IdentifyTest extends TestBase {
    
    public IdentifyTest(final String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(IdentifyTest.class);
        return suite;
    }
    
    public void testIdentify() throws Exception {
        
        final MessageFactory sf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        final SOAPMessage msg = sf.createMessage();
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(Identify.NS_PREFIX, Identify.NS_URI);
        msg.getSOAPBody().addBodyElement(Identify.IDENTIFY);

        msg.writeTo(logfile);
        logfile.write("\n\n".getBytes());
        final Addressing response = HttpClient.sendRequest(msg, DESTINATION);
        response.writeTo(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        final Identify id = new Identify(response);
        final IdentifyResponseType idr = id.getIdentifyResponse();
        assertNotNull(idr.getProductVendor());
        assertNotNull(idr.getProductVersion());
        assertNotNull(idr.getProtocolVersion().get(0));
    }
}
