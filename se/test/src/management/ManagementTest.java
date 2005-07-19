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
 * $Id: ManagementTest.java,v 1.3 2005-07-19 19:54:59 akhilarora Exp $
 */

package management;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import com.sun.ws.management.Management;
import com.sun.ws.management.transport.HttpClient;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2005._02.management.Locale;
import org.xmlsoap.schemas.ws._2005._02.management.MaxEnvelopeSize;
import org.xmlsoap.schemas.ws._2005._02.management.ObjectFactory;
import org.xmlsoap.schemas.ws._2005._02.management.SystemType;

/**
 * Unit test for WS-Management
 */
public class ManagementTest extends TestBase {
    
    private static final String RESOURCE = "wsman:system/2005/02/this";
    private static final int TIMEOUT = 30000;
    
    private final Map<String, Object> selectors = new TreeMap();
    private final ObjectFactory of = new ObjectFactory();
    
    public ManagementTest(final String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        selectors.put("Name", "WSMAN");
    }
    
    protected void tearDown() throws java.lang.Exception {
        selectors.clear();
        super.tearDown();
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(ManagementTest.class);
        return suite;
    }
    
    public void testSet() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setAction(Transfer.PUT_ACTION_URI);
        mgmt.setAction(Transfer.CREATE_ACTION_URI);
        mgmt.setAction(Transfer.DELETE_ACTION_URI);
        String uuid = null;
        for (int i=0; i < 100; i++) {
            uuid = UUID_SCHEME + UUID.randomUUID().toString();
            mgmt.setMessageId(uuid);
        }
        mgmt.prettyPrint(logfile);
        // only the last one should remain
        assertEquals(uuid, mgmt.getMessageId());
        assertEquals(Transfer.DELETE_ACTION_URI, mgmt.getAction());
    }
    
    public void testMissingResourceURI() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        mgmt.setTo(DESTINATION);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        
        mgmt.prettyPrint(logfile);
        Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        
        assertTrue("Missing ResourceURI accepted", response.getBody().hasFault());
        Fault fault = new SOAP(response).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(Management.DESTINATION_UNREACHABLE, fault.getCode().getSubcode().getValue());
        assertEquals(Management.DESTINATION_UNREACHABLE_REASON, fault.getReason().getText().get(0).getValue());
        for (final Object detail : fault.getDetail().getAny()) {
            if ((Management.FAULT_DETAIL.getPrefix() + ":" +
                    Management.FAULT_DETAIL.getLocalPart()).equals(((Node) detail).getNodeName())) {
                assertEquals(Management.INVALID_RESOURCE_URI_DETAIL, ((Node) detail).getFirstChild().getNodeValue());
            }
        }
    }
    
    public void testGetVisual() throws Exception {
        
        final Transfer xf = new Transfer();
        xf.setAction(Transfer.GET_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        final String uuid = UUID_SCHEME + UUID.randomUUID().toString();
        xf.setMessageId(uuid);
        
        final Management mgmt = new Management(xf);
        mgmt.setTo(DESTINATION, RESOURCE);
        final Duration timeout = DatatypeFactory.newInstance().newDuration(TIMEOUT);
        mgmt.setTimeout(timeout);
        mgmt.setSelectors(selectors);
        
        final SystemType systemA = of.createSystemType();
        systemA.setName("urn:system:a");
        final SystemType systemB = of.createSystemType();
        systemB.setName("urn:system:b");
        systemA.getSystem().add(systemB);
        mgmt.setSystem(systemA);
        
        final MaxEnvelopeSize maxEnvSize = of.createMaxEnvelopeSize();
        final long envSize = 4096;
        maxEnvSize.setValue(envSize);
        final String envPolicy = "Skip";
        maxEnvSize.setPolicy(envPolicy);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        final Locale locale = of.createLocale();
        final String localeString = "en-US";
        locale.setValue(localeString);
        mgmt.setLocale(locale);
        
        final Map<String, String> options = new TreeMap();
        final String verboseOptionName = "--output-file";
        final String verboseOptionValue = "/dev/null";
        options.put(verboseOptionName, verboseOptionValue);
        mgmt.setOptions(options);
        
        final String renameAddress = "http://host.com/renamed/to/address";
        final EndpointReferenceType eprRename = mgmt.createEndpointReference(renameAddress, null, null, null, null);
        mgmt.setRename(eprRename);
        
        mgmt.prettyPrint(logfile);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mgmt.writeTo(bos);
        final Management m2 = new Management(new ByteArrayInputStream(bos.toByteArray()));
        
        assertEquals(RESOURCE, m2.getResourceURI());
        assertEquals(timeout, m2.getTimeout());
        
        final Map<String, Object> selectors2 = mgmt.getSelectors();
        assertEquals(selectors.size(), selectors2.size());
        assertEquals(selectors.keySet().iterator().next(), selectors2.keySet().iterator().next());
        assertEquals(selectors.values().iterator().next(), selectors2.values().iterator().next());
        
        // cannot compare SystemTypes since classes generated by JAXB lack an equals method
        assertEquals(systemA.getName(), m2.getSystem().getName());
        assertEquals(systemB.getName(), m2.getSystem().getSystem().get(0).getName());
        
        final MaxEnvelopeSize maxEnvSize2 = m2.getMaxEnvelopeSize();
        assertEquals(envSize, maxEnvSize2.getValue());
        assertEquals(envPolicy, maxEnvSize2.getPolicy());
        
        assertEquals(localeString, m2.getLocale().getValue());
        
        final Map<String, String> options2 = m2.getOptions();
        assertTrue(options2.containsKey(verboseOptionName));
        assertTrue(options2.containsValue(verboseOptionValue));
        assertEquals(verboseOptionName, options2.keySet().iterator().next());
        assertEquals(verboseOptionValue, options2.values().iterator().next());
        
        assertEquals(renameAddress, m2.getRename().getAddress().getValue());
    }
    
    public void testGet() throws Exception {
        
        final Transfer xf = new Transfer();
        xf.setAction(Transfer.GET_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final Management mgmt = new Management(xf);
        mgmt.setTo(DESTINATION, RESOURCE);
        final Duration timeout = DatatypeFactory.newInstance().newDuration(TIMEOUT);
        mgmt.setTimeout(timeout);
        
        final Map<String, Object> selectorMap = new TreeMap();
        selectorMap.put("SystemName", "sun-v20z-1");
        mgmt.setSelectors(selectorMap);
        
        Addressing response = HttpClient.sendRequest(mgmt);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        Document doc = response.getBody().extractContentAsDocument();
        final OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(72);
        format.setIndenting(true);
        format.setIndent(2);
        final XMLSerializer serializer = new XMLSerializer(System.out, format);
        System.out.println();
        serializer.serialize(doc);
        System.out.println();
    }
    
    public void testValidate() throws Exception {
        
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setTo(DESTINATION, RESOURCE);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        Addressing response = HttpClient.sendRequest(mgmt);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        try {
            mgmt.validate();
        } catch (FaultException fex) {
            fail("Validation failed: " + fex.toString());
        }
        
        final Management mgmt2 = new Management();
        mgmt2.setTo(DESTINATION, RESOURCE);
        mgmt2.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt2.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        doValidation(mgmt2, Addressing.NS_PREFIX + ":Action");
        
        final Management mgmt3 = new Management();
        mgmt3.setAction(Transfer.GET_ACTION_URI);
        mgmt3.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt3.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        doValidation(mgmt3, Addressing.NS_PREFIX + ":To");
        
        final Management mgmt4 = new Management();
        mgmt4.setAction(Transfer.GET_ACTION_URI);
        mgmt4.setTo(DESTINATION, RESOURCE);
        mgmt4.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        doValidation(mgmt4, Addressing.NS_PREFIX + ":ReplyTo");
        
        final Management mgmt5 = new Management();
        mgmt5.setAction(Transfer.GET_ACTION_URI);
        mgmt5.setTo(DESTINATION, RESOURCE);
        mgmt5.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        doValidation(mgmt5, Addressing.NS_PREFIX + ":MessageID");
    }
    
    private void doValidation(final Management mgmt, final String elementName)
    throws IOException, SOAPException, JAXBException, FaultException {
        
        try {
            mgmt.validate();
            fail("Validation succeeded with no " + elementName);
        } catch (FaultException ignore) {}
        
        if (mgmt.getTo() != null) {
            Addressing response = HttpClient.sendRequest(mgmt);
            assertTrue(response.getBody().hasFault());
            Fault fault = new SOAP(response).getFault();
            assertEquals(SOAP.SENDER, fault.getCode().getValue());
            assertEquals(Addressing.MESSAGE_INFORMATION_HEADER_REQUIRED, fault.getCode().getSubcode().getValue());
            assertEquals(Addressing.MESSAGE_INFORMATION_HEADER_REQUIRED_REASON, fault.getReason().getText().get(0).getValue());
        }
    }
}
