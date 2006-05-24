/*
 * Copyright 2006 Hewlett-Packard Development Company, L.P.
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
 */

package management;

import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import javax.xml.soap.SOAPHeaderElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TransferExtensionsTest extends TestBase {
    
    public TransferExtensionsTest(String testName) {
        super(testName);
    }

    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(TransferExtensionsTest.class);
        return suite;
    }

    public void testFragmentGetVisual() throws Exception {
        final TransferExtensions transfer = new TransferExtensions();
        transfer.setAction(Transfer.GET_ACTION_URI);
        
        final String query = "//foo/bar";
        transfer.setFragmentGet(query, XPath.NS_URI);
        
        //print contents of Transfer
        transfer.prettyPrint(logfile);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transfer.writeTo(bos);
        final TransferExtensions trans = new TransferExtensions(new ByteArrayInputStream(bos.toByteArray()));

        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader);
        assertEquals(query, fragmentTransferHeader.getTextContent());
        assertEquals(XPath.NS_URI, fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
    }

    public void testFragmentGetResponseVisual() throws Exception {
        final TransferExtensions transfer = new TransferExtensions();
        transfer.setAction(Transfer.GET_RESPONSE_URI);
        
        final String query = "//foo/bar";
        transfer.setFragmentGet(query, null);
        final SOAPHeaderElement fragmentTransferHeader = transfer.getFragmentHeader();

        final List<Node> content = new ArrayList<Node>();
        final Document doc = transfer.newDocument();
        final Element fooElement = doc.createElement("foo");
        final Element barElement = doc.createElement("bar");
        barElement.setTextContent("this is a test of fragments");
        fooElement.appendChild(barElement);
        content.add(fooElement);

        transfer.setFragmentResponse(fragmentTransferHeader, content);
        
        //print contents of Transfer
        transfer.prettyPrint(logfile);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transfer.writeTo(bos);
        final TransferExtensions trans = new TransferExtensions(new ByteArrayInputStream(bos.toByteArray()));

        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader2 = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader2);
        assertEquals(query, fragmentTransferHeader2.getTextContent());
        assertNull(fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
    }

    // a fragment transfer becomes a regular transfer if the fragment header is omitted
    public void testNonFragmentGet() throws Exception {
        final TransferExtensions transfer = new TransferExtensions();
        transfer.setAction(Transfer.GET_ACTION_URI);
        transfer.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        transfer.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final Management mgmt = new Management(transfer);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/fragment");

        //print contents of Transfer
        mgmt.prettyPrint(logfile);

        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        final TransferExtensions trans = new TransferExtensions(response);

        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNull(fragmentTransferHeader);
        
        // this should be the entire response document
        assertNotNull(response.getBody().getFirstChild());
    }

    public void testFragmentGet() throws Exception {
        final TransferExtensions transfer = new TransferExtensions();
        transfer.setAction(Transfer.GET_ACTION_URI);
        transfer.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        transfer.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final String query = "//foo/bar";
        transfer.setFragmentGet(query, null);
        
        final Management mgmt = new Management(transfer);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/fragment");

        //print contents of Transfer
        mgmt.prettyPrint(logfile);

        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        final TransferExtensions trans = new TransferExtensions(response);

        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader);
        assertEquals(query, fragmentTransferHeader.getTextContent());
        assertNull(fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
    }
}
