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
 * $Id: TransferTest.java,v 1.6 2007-11-30 14:32:36 denis_rachal Exp $
 */

package management;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XmlBinding;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.w3._2003._05.soap_envelope.Body;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for WS-Transfer
 */
public class TransferTest extends TestBase {
    
	final XmlBinding binding;
	
    public TransferTest(final String testName) throws JAXBException {
        super(testName);
		System.setProperty(XmlBinding.class.getPackage().getName() + ".custom.packagenames",
				"");
        binding = new XmlBinding(null, "");
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(TransferTest.class);
        return suite;
    }
    
    public void testGetVisual() throws Exception {
        
        final Transfer xf = new Transfer();
        xf.setXmlBinding(binding);
        xf.setTo(DESTINATION);
        xf.setAction(Transfer.GET_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        xf.prettyPrint(logfile);
    }
    
    public void testPutVisual() throws Exception {
        
        final Transfer xf = new Transfer();
        xf.setXmlBinding(binding);
        xf.setTo(DESTINATION);
        xf.setAction(Transfer.PUT_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final Document doc = xf.newDocument();
        final Element temp = doc.createElementNS(NS_URI, NS_PREFIX + ":" + "temperature");
        temp.appendChild(doc.createTextNode("75"));
        doc.appendChild(temp);
        
        final Body body = SOAP.FACTORY.createBody();
        final List<Object> bodyElements = body.getAny();
        bodyElements.add(doc.getDocumentElement());
        
        xf.getEnvelope().addNamespaceDeclaration(NS_PREFIX, NS_URI);
        xf.getXmlBinding().marshal(SOAP.FACTORY.createBody(body), xf.getBody());
        
        xf.prettyPrint(logfile);
    }
    
    public void testCreateVisual() throws Exception {
        
        final Transfer xf = new Transfer();
        xf.setXmlBinding(binding);
        xf.setTo(DESTINATION);
        xf.setAction(Transfer.CREATE_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        xf.prettyPrint(logfile);
    }
    
    public void testDeleteVisual() throws Exception {
        
        final Transfer xf = new Transfer();
        xf.setXmlBinding(binding);
        xf.setTo(DESTINATION);
        xf.setAction(Transfer.DELETE_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        xf.prettyPrint(logfile);
    }
}
