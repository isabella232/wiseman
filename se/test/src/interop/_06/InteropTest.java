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
 * $Id: InteropTest.java,v 1.1 2006-07-13 21:15:42 akhilarora Exp $
 */

package interop._06;

import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.addressing.DestinationUnreachableFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XML;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeaderElement;
import management.TestBase;
import org.dmtf.schemas.wbem.wsman._1.wsman.Locale;
import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

/**
 * Unit tests for Interop Scenarios
 */
public final class InteropTest extends TestBase {
    
    private static final String COMPUTER_SYSTEM_RESOURCE =
            "http://www.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ComputerSystem";
    
    private static final String NUMERIC_SENSOR_RESOURCE =
            "http://www.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_NumericSensor";
    
    private static final String TIMEOUT_RESOURCE = 
            "wsman:test/timeout";
    
    private static final String CIM_COMPUTER_SYSTEM = "CIM_ComputerSystem";
    private static final String CIM_NUMERIC_SENSOR = "CIM_NumericSensor";
    
    public InteropTest(final String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
    }
    
    protected void tearDown() throws java.lang.Exception {
        super.tearDown();
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(InteropTest.class);
        return suite;
    }
    
    /**
     * Interop Scenario 6.2 - GET instance of CIM_ComputerSystem
     */
    public void testGet() throws Exception {
        
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(COMPUTER_SYSTEM_RESOURCE);
        
        final Set<SelectorType> selectors = new HashSet<SelectorType>();
    
        final SelectorType selector1 = new SelectorType();
        selector1.setName("CreationClassName");
        selector1.getContent().add(CIM_COMPUTER_SYSTEM);
        selectors.add(selector1);
        
        final SelectorType selector2 = new SelectorType();
        selector2.setName("Name");
        selector2.getContent().add("IPMI Controller 32");
        selectors.add(selector2);
        
        mgmt.setSelectors(selectors);
        
        final Duration timeout = DatatypeFactory.newInstance().newDuration(60000);
        mgmt.setTimeout(timeout);
        
        final BigInteger envSize = new BigInteger("153600");
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        maxEnvSize.setValue(envSize);
        maxEnvSize.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.TRUE);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        final Locale locale = Management.FACTORY.createLocale();
        locale.setLang(XML.DEFAULT_LANG);
        locale.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.FALSE);
        mgmt.setLocale(locale);
        
        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        assertEquals(Transfer.GET_RESPONSE_URI, response.getAction());
        assertNotNull(response.getBody().getFirstChild());
    }
    
    /**
     * Interop Scenario 6.3 - GET failure with invalide resource URI
     */
    public void testGetFail() throws Exception {
        
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.setTo(DESTINATION);
        // make the resource uri invalid by omitting the last character
        mgmt.setResourceURI(COMPUTER_SYSTEM_RESOURCE.substring(0, COMPUTER_SYSTEM_RESOURCE.length() - 1));
        
        final Set<SelectorType> selectors = new HashSet<SelectorType>();
    
        final SelectorType selector1 = new SelectorType();
        selector1.setName("CreationClassName");
        selector1.getContent().add(CIM_COMPUTER_SYSTEM);
        selectors.add(selector1);
        
        final SelectorType selector2 = new SelectorType();
        selector2.setName("Name");
        selector2.getContent().add("IPMI Controller 32");
        selectors.add(selector2);
        
        mgmt.setSelectors(selectors);
        
        final Duration timeout = DatatypeFactory.newInstance().newDuration(60000);
        mgmt.setTimeout(timeout);
        
        final BigInteger envSize = new BigInteger("153600");
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        maxEnvSize.setValue(envSize);
        maxEnvSize.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.TRUE);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        final Locale locale = Management.FACTORY.createLocale();
        locale.setLang(XML.DEFAULT_LANG);
        locale.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.FALSE);
        mgmt.setLocale(locale);
        
        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (!response.getBody().hasFault()) {
            fail("Invalid ResourceURI accepted");
        }
        
        final Fault fault = response.getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(DestinationUnreachableFault.DESTINATION_UNREACHABLE, fault.getCode().getSubcode().getValue());
        assertEquals(DestinationUnreachableFault.DESTINATION_UNREACHABLE_REASON, fault.getReason().getText().get(0).getValue());
        for (final Object detail : fault.getDetail().getAny()) {
            if (detail instanceof JAXBElement) {
                final String faultDetail = ((JAXBElement) detail).getValue().toString();
                assertEquals(DestinationUnreachableFault.Detail.INVALID_RESOURCE_URI.toString(), faultDetail);
            }
        }
    }
    
    /**
     * Interop Scenario 6.4 - Get failure with maxenvelopesize exceeded error
     */
    public void testGetFailWithMaxEnvelopSizeExceeded() throws Exception {
        
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(NUMERIC_SENSOR_RESOURCE);
        
        final Set<SelectorType> selectors = new HashSet<SelectorType>();
    
        final SelectorType selector1 = new SelectorType();
        selector1.setName("CreationClassName");
        selector1.getContent().add(CIM_NUMERIC_SENSOR);
        selectors.add(selector1);
        
        final SelectorType selector2 = new SelectorType();
        selector2.setName("DeviceID");
        selector2.getContent().add("10.0.32");
        selectors.add(selector2);
        
        final SelectorType selector3 = new SelectorType();
        selector3.setName("SystemCreationClassName");
        selector3.getContent().add(CIM_COMPUTER_SYSTEM);
        selectors.add(selector3);
        
        final SelectorType selector4 = new SelectorType();
        selector4.setName("SystemName");
        selector4.getContent().add("IPMI Controller 32");
        selectors.add(selector4);
        
        mgmt.setSelectors(selectors);
        
        final Duration timeout = DatatypeFactory.newInstance().newDuration(60000);
        mgmt.setTimeout(timeout);
        
        final BigInteger envSize = new BigInteger("8190");
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        maxEnvSize.setValue(envSize);
        maxEnvSize.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.TRUE);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (!response.getBody().hasFault()) {
            fail("Invalid ResourceURI accepted");
        }
        
        final Fault fault = response.getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(EncodingLimitFault.ENCODING_LIMIT, fault.getCode().getSubcode().getValue());
        assertEquals(EncodingLimitFault.ENCODING_LIMIT_REASON, fault.getReason().getText().get(0).getValue());
        for (final Object detail : fault.getDetail().getAny()) {
            if (detail instanceof JAXBElement) {
                final String faultDetail = ((JAXBElement) detail).getValue().toString();
                assertEquals(EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE.toString(), faultDetail);
            }
        }
    }
    
    /**
     * Interop Scenario 6.5 - Get failure with nvalid selectors
     */
    public void testGetFailWithInvalidSelectors() throws Exception {
        
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(NUMERIC_SENSOR_RESOURCE);
        
        final Set<SelectorType> selectors = new HashSet<SelectorType>();
    
        final SelectorType selector1 = new SelectorType();
        selector1.setName("CreationClassName");
        selector1.getContent().add(CIM_NUMERIC_SENSOR);
        selectors.add(selector1);
        
        mgmt.setSelectors(selectors);
        
        final Duration timeout = DatatypeFactory.newInstance().newDuration(60000);
        mgmt.setTimeout(timeout);
        
        final BigInteger envSize = new BigInteger("153600");
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        maxEnvSize.setValue(envSize);
        maxEnvSize.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.TRUE);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (!response.getBody().hasFault()) {
            fail("Invalid ResourceURI accepted");
        }
        
        final Fault fault = response.getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(InvalidSelectorsFault.INVALID_SELECTORS, fault.getCode().getSubcode().getValue());
        assertEquals(InvalidSelectorsFault.INVALID_SELECTORS_REASON, fault.getReason().getText().get(0).getValue());
        for (final Object detail : fault.getDetail().getAny()) {
            if (detail instanceof JAXBElement) {
                final String faultDetail = ((JAXBElement) detail).getValue().toString();
                assertEquals(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS.toString(), faultDetail);
            }
        }
    }
    
    /**
     * Interop Scenario 6.6 - Get failure with operation timeout
     */
    public void testGetFailWithOperationTimeout() throws Exception {
        
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(TIMEOUT_RESOURCE);
        
        final Duration timeout = DatatypeFactory.newInstance().newDuration(5000);
        mgmt.setTimeout(timeout);
        
        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (!response.getBody().hasFault()) {
            fail("Invalid ResourceURI accepted");
        }
        
        final Fault fault = response.getFault();
        assertEquals(SOAP.RECEIVER, fault.getCode().getValue());
        assertEquals(TimedOutFault.TIMED_OUT, fault.getCode().getSubcode().getValue());
        assertEquals(TimedOutFault.TIMED_OUT_REASON, fault.getReason().getText().get(0).getValue());
    }

    /**
     * Interop Scenario 6.7 - Fragment Get
     */
    public void testFragmentGet() throws Exception {
        
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(COMPUTER_SYSTEM_RESOURCE);
        
        final Set<SelectorType> selectors = new HashSet<SelectorType>();
    
        final SelectorType selector1 = new SelectorType();
        selector1.setName("CreationClassName");
        selector1.getContent().add(CIM_COMPUTER_SYSTEM);
        selectors.add(selector1);
        
        final SelectorType selector2 = new SelectorType();
        selector2.setName("Name");
        selector2.getContent().add("IPMI Controller 32");
        selectors.add(selector2);
        
        mgmt.setSelectors(selectors);
        
        final Duration timeout = DatatypeFactory.newInstance().newDuration(60000);
        mgmt.setTimeout(timeout);
        
        final BigInteger envSize = new BigInteger("153600");
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        maxEnvSize.setValue(envSize);
        maxEnvSize.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.TRUE);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        final Locale locale = Management.FACTORY.createLocale();
        locale.setLang(XML.DEFAULT_LANG);
        locale.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.FALSE);
        mgmt.setLocale(locale);
        
        final TransferExtensions txi = new TransferExtensions(mgmt);
        // XPath expression that work - /p:CIM_ComputerSystem/p:Roles and //p:Roles
        txi.setFragmentHeader("/p:CIM_ComputerSystem/p:Roles", null);
        
        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        final TransferExtensions txo = new TransferExtensions(response);
        
        assertEquals(Transfer.GET_RESPONSE_URI, txo.getAction());
        final SOAPHeaderElement hdr = txo.getFragmentHeader();
        assertNotNull(hdr);
        final SOAPElement[] fragment = txo.getChildren(txo.getBody(), TransferExtensions.XML_FRAGMENT);
        assertNotNull(fragment);
        assertTrue(fragment.length == 1);
        final SOAPElement[] roles = txo.getChildren(fragment[0]);
        assertNotNull(roles);
        assertTrue(roles.length == 1);
        assertEquals("p", roles[0].getPrefix());
        assertEquals("http://www.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ComputerSystem", roles[0].getNamespaceURI());
        assertEquals("Roles", roles[0].getLocalName());
        assertEquals("Hardware Management Controller", roles[0].getTextContent());
    }

    /**
     * Interop Scenario 7.1 - Enumerate instances of CIM_NumericSensor
     */
    public void testEnumerate() throws Exception {
        
        Management mgmt = new Management();
        mgmt.setAction(Enumeration.ENUMERATE_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(NUMERIC_SENSOR_RESOURCE);
        
        final Duration timeout = DatatypeFactory.newInstance().newDuration(60000);
        mgmt.setTimeout(timeout);
        
        final BigInteger envSize = new BigInteger("153600");
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        maxEnvSize.setValue(envSize);
        maxEnvSize.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.TRUE);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        final Locale locale = Management.FACTORY.createLocale();
        locale.setLang(XML.DEFAULT_LANG);
        locale.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.FALSE);
        mgmt.setLocale(locale);
        
        final Enumeration ei = new Enumeration(mgmt);
        ei.setEnumerate(null, null, null);
        
        log(mgmt);
        Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        final Enumeration eo = new Enumeration(response);
        assertEquals(Enumeration.ENUMERATE_RESPONSE_URI, eo.getAction());
        final EnumerateResponse er = eo.getEnumerateResponse();
        assertNotNull(er);
        EnumerationContextType ect = er.getEnumerationContext();
        assertNotNull(ect);
        Object context = ect.getContent().get(0);
        assertNotNull(context);
        
        // pull request

        mgmt = new Management();
        mgmt.setAction(Enumeration.PULL_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(NUMERIC_SENSOR_RESOURCE);
        
        mgmt.setTimeout(timeout);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        mgmt.setLocale(locale);
        
        Enumeration pi = new Enumeration(mgmt);
        pi.setPull(context, -1, 1, null);
        
        log(mgmt);
        response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        Enumeration po = new Enumeration(response);
        assertEquals(Enumeration.PULL_RESPONSE_URI, po.getAction());
        PullResponse pr = po.getPullResponse();
        assertNotNull(pr);
        // not end of sequence yet
        assertNull(pr.getEndOfSequence());
        ect = pr.getEnumerationContext();
        assertNotNull(ect);
        context = ect.getContent().get(0);
        assertNotNull(context);
        ItemListType ilt = pr.getItems();
        assertNotNull(ilt);
        List<Object> il = ilt.getAny();
        assertNotNull(il);
        assertTrue(il.size() == 1);
        Object obj = il.get(0);
        assertTrue(obj instanceof Node);
        Node node = (Node) obj;
        assertEquals("p", node.getPrefix());
        assertEquals(NUMERIC_SENSOR_RESOURCE, node.getNamespaceURI());
        assertEquals(CIM_NUMERIC_SENSOR, node.getLocalName());
        
        // second pull request

        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        pi = new Enumeration(mgmt);
        pi.setPull(context, -1, 1, null);
        
        log(mgmt);
        response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        po = new Enumeration(response);
        assertEquals(Enumeration.PULL_RESPONSE_URI, po.getAction());
        pr = po.getPullResponse();
        assertNotNull(pr);
        // at end of sequence now
        assertNotNull(pr.getEndOfSequence());
        ect = pr.getEnumerationContext();
        // there should be no context at end of sequence
        assertNull(ect);
        ilt = pr.getItems();
        ilt = pr.getItems();
        assertNotNull(ilt);
        il = ilt.getAny();
        assertNotNull(il);
        assertTrue(il.size() == 1);
        obj = il.get(0);
        assertTrue(obj instanceof Node);
        node = (Node) obj;
        assertEquals("p", node.getPrefix());
        assertEquals(NUMERIC_SENSOR_RESOURCE, node.getNamespaceURI());
        assertEquals(CIM_NUMERIC_SENSOR, node.getLocalName());
    }
}
