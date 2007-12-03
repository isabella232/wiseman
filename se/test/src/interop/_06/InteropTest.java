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
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.25  2007/05/30 20:30:31  nbeers
 **Add HP copyright header
 **
 **
 * $Id: InteropTest.java,v 1.26 2007-12-03 09:15:11 denis_rachal Exp $
 */

package interop._06;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeaderElement;


import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.Locale;
import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import util.TestBase;

import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.addressing.DestinationUnreachableFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XML;

/**
 * Unit tests for Interop Scenarios
 */
public final class InteropTest extends TestBase {

    private static final String COMPUTER_SYSTEM_RESOURCE =
        "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ComputerSystem";

    private static final String NUMERIC_SENSOR_RESOURCE =
        "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_NumericSensor";

    private static final String TIMEOUT_RESOURCE = "wsman:test/timeout";
    private static final String PULL_SOURCE_RESOURCE = "wsman:test/pull_source";

    private static final String CIM_COMPUTER_SYSTEM = "ComputerSystem";
    private static final String CIM_NUMERIC_SENSOR = "NumericSensor";

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
     * Interop Scenario 6.1 - Identify
     */
    public void testIdentify() throws Exception {
        final Identify identify = new Identify();
        identify.setIdentify();

        final Addressing response = HttpClient.sendRequest(identify.getMessage(), DESTINATION);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }

        final Identify id = new Identify(response);
        final SOAPElement idr = id.getIdentifyResponse();
        assertNotNull(idr);
        assertNotNull(id.getChildren(idr, Identify.PRODUCT_VENDOR));
        assertNotNull(id.getChildren(idr, Identify.PRODUCT_VERSION));
        assertNotNull(id.getChildren(idr, Identify.PROTOCOL_VERSION));
    }

    /**
     * Interop Scenario 6.2 - GET instance of CIM_ComputerSystem
     */
    public void testGet() throws Exception {

        final Set<SelectorType> selectors = new HashSet<SelectorType>();

        final SelectorType selector1 = new SelectorType();
        selector1.setName("CreationClassName");
        selector1.getContent().add(CIM_COMPUTER_SYSTEM);
        selectors.add(selector1);

        final SelectorType selector2 = new SelectorType();
        selector2.setName("Name");
        selector2.getContent().add("IPMI Controller 32");
        selectors.add(selector2);

        final Locale locale = Management.FACTORY.createLocale();
        locale.setLang(XML.DEFAULT_LANG);
        locale.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.FALSE);

        ManagementMessageValues settings = ManagementMessageValues.newInstance();

    	settings.setTo(DESTINATION);
    	settings.setSelectorSet(selectors);
    	settings.setTimeout(60000);
    	settings.setMaxEnvelopeSize(new BigInteger("153600"));
    	settings.setLocale(locale);
    	settings.setResourceUri(COMPUTER_SYSTEM_RESOURCE);

    	final Management mgmt = ManagementUtility.buildMessage(null, settings);
    	mgmt.setAction(Transfer.GET_ACTION_URI);

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
     * Interop Scenario 6.3 - GET failure with invalid resource URI
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
        for (final Object detail : fault.getDetail().getAny()) {
            if (detail instanceof JAXBElement) {
                final String faultDetail = ((JAXBElement) detail).getValue().toString();
                assertEquals(DestinationUnreachableFault.Detail.INVALID_RESOURCE_URI.toString(), faultDetail);
            }
        }
    }

    /**
     * Interop Scenario 6.4 - Get failure with MaxEnvelopeSize exceeded error
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
//        selector4.getContent().add("IPMI Controller 32");
        selector4.getContent().add("LARGE_MESSAGE");
        selectors.add(selector4);

        mgmt.setSelectors(selectors);

        final Duration timeout = DatatypeFactory.newInstance().newDuration(60000);
        mgmt.setTimeout(timeout);

        String bigInteger="8500";
        final BigInteger envSize = new BigInteger(bigInteger);
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        maxEnvSize.setValue(envSize);
        maxEnvSize.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.TRUE);
        mgmt.setMaxEnvelopeSize(maxEnvSize);

        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (!response.getBody().hasFault()) {
//        	fail("Accepted a too small MaxEnvelopeSize of " + envSize.toString());
            fail("MaxEnvelopeSize of " + envSize.toString()+" was not exceeded, but should have been.");
        }

        final Fault fault = response.getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(EncodingLimitFault.ENCODING_LIMIT, fault.getCode().getSubcode().getValue());
        for (final Object detail : fault.getDetail().getAny()) {
            if (detail instanceof JAXBElement) {
                final String faultDetail = ((JAXBElement) detail).getValue().toString();
                // assertEquals(EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE.toString(), faultDetail);
            }
        }
    }

    /**
     * Interop Scenario 6.5 - Get failure with invalid selectors
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
            fail("Insufficient Selectors accepted");
        }

        final Fault fault = response.getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(InvalidSelectorsFault.INVALID_SELECTORS, fault.getCode().getSubcode().getValue());
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

        // special selector to disable the use of namespace prefixes in returned doc
        final SelectorType selector3 = new SelectorType();
        selector3.setName("NoPrefix");
        selector3.getContent().add("true");
        selectors.add(selector3);

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
        // XPath expression with prefixes that work - /p:CIM_ComputerSystem/p:Roles and //p:Roles
        txi.setFragmentHeader("/CIM_ComputerSystem/Roles", null);

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
        assertNotNull(node);

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
        ilt = pr.getItems();
        assertNotNull(ilt);
        il = ilt.getAny();
        assertNotNull(il);
        assertTrue(il.size() == 1);
        obj = il.get(0);
        assertTrue(obj instanceof Node);
        node = (Node) obj;
        assertNotNull(node);
    }

    /**
     * Interop Scenario 7.2 - Optimized Enumeration
     */
    public void testOptimizedEnumeration() throws Exception {

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

        final EnumerationExtensions ei = new EnumerationExtensions(mgmt);
        ei.setEnumerate(null, false, true, 1, null, null, null);

        log(mgmt);
        Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }

        final EnumerationExtensions eo = new EnumerationExtensions(response);
        assertEquals(Enumeration.ENUMERATE_RESPONSE_URI, eo.getAction());
        final EnumerateResponse er = eo.getEnumerateResponse();
        assertNotNull(er);
        EnumerationContextType ect = er.getEnumerationContext();
        assertNotNull(ect);
        Object context = ect.getContent().get(0);
        assertNotNull(context);

        // should contain an item due to the optimization
        List<Object> il = er.getAny();
        assertNotNull(il);
        if (il.size() > 0) {
            // will be 0 if an implementation does not implement the optimize feature
            Object obj = il.get(0);
            if (obj instanceof JAXBElement) {
                EnumerationExtensions.ITEMS.equals(((JAXBElement)obj).getName());
            }
            final List<EnumerationItem> items = eo.getItems();
            assertNotNull(items);
            assertTrue(items.size() == 1);
            Node node = (Node) items.get(0).getItem();
            assertNotNull(node);
        }

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
        ect = pr.getEnumerationContext();
        ItemListType ilt = pr.getItems();
        assertNotNull(ilt);
        il = ilt.getAny();
        assertNotNull(il);
        assertTrue(il.size() == 1);
        final Object obj = il.get(0);
        assertTrue(obj instanceof Node);
        final Node node = (Node) obj;
        assertNotNull(node);
    }

    /**
     * Interop Scenario 7.3 - Enumerate Failure
     */
    public void testEnumerateFailure() throws Exception {

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
        assertNotNull(node);

        // second pull request with invalid context

        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

        pi = new Enumeration(mgmt);
        // create an invalid context
        context = UUID_SCHEME + UUID.randomUUID().toString();
        pi.setPull(context, -1, 1, null);

        log(mgmt);
        response = HttpClient.sendRequest(mgmt);
        log(response);
        if (!response.getBody().hasFault()) {
            fail("Invalid enumeration context accepted");
        }

        final Fault fault = response.getFault();
        assertEquals(SOAP.RECEIVER, fault.getCode().getValue());
        assertEquals(InvalidEnumerationContextFault.INVALID_ENUM_CONTEXT, fault.getCode().getSubcode().getValue());
    }

    /**
     * Interop Scenario 7.4 - Enumerate ObjectAndEPR
     */
    public void testEnumerateObjectAndEPR() throws Exception {

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

        final EnumerationExtensions ei = new EnumerationExtensions(mgmt);
        ei.setEnumerate(null, false, false, -1, null, null,
            EnumerationExtensions.Mode.EnumerateObjectAndEPR);

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

        EnumerationExtensions po = new EnumerationExtensions(response);
        assertEquals(Enumeration.PULL_RESPONSE_URI, po.getAction());
        PullResponse pr = po.getPullResponse();
        assertNotNull(pr);
        ect = pr.getEnumerationContext();

        ItemListType ilt = pr.getItems();
        assertNotNull(ilt);
        List<Object> il = ilt.getAny();
        assertNotNull(il);
        List<EnumerationItem> itemList = po.getItems();
        assertTrue(itemList.size() > 0);
        final EnumerationItem eni = itemList.get(0);
        assertNotNull(eni);
        assertNotNull(eni.getItem());
        final EndpointReferenceType epr = eni.getEndpointReference();
        if (epr != null) {
            // epr might be null if enum mode is not supported
            assertEquals(DESTINATION, epr.getAddress().getValue());
            for (final Object refp : epr.getReferenceParameters().getAny()) {
                if (refp instanceof JAXBElement) {
                    JAXBElement jref = (JAXBElement) refp;
                    if (AttributableURI.class.equals(jref.getDeclaredType())) {
                        assertEquals(Management.RESOURCE_URI, jref.getName());
                        assertEquals(NUMERIC_SENSOR_RESOURCE, ((AttributableURI) jref.getValue()).getValue());
                    } else if (SelectorSetType.class.equals(jref.getDeclaredType())) {
                        final List<SelectorType> ss = ((SelectorSetType) jref.getValue()).getSelector();
                        assertTrue(ss.size() > 0);
                        for (final SelectorType sel : ss) {
                            // System.out.println(sel.getName() + ": " + sel.getContent());
                        }
                    }
                }
            }
        }
    }

    /**
     * Interop Scenario 7.5 - Filtered Enumeration with XPath filter dialect
     */
    public void testFilteredEnumeration() throws Exception {

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

        final Set<SelectorType> selectors = new HashSet<SelectorType>();
        // special selector to disable the use of namespace prefixes in returned doc
        final SelectorType selector = new SelectorType();
        selector.setName("NoPrefix");
        selector.getContent().add("true");
        selectors.add(selector);
        mgmt.setSelectors(selectors);

        final Enumeration ei = new Enumeration(mgmt);
        final FilterType filter = new FilterType();
        filter.getContent().add("/CIM_NumericSensor[SensorType=2]");
        ei.setEnumerate(null, null, filter);

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
        assertNotNull(node);

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
        assertNotNull(ilt);
        il = ilt.getAny();
        assertNotNull(il);
        assertTrue(il.size() == 1);
        obj = il.get(0);
        assertTrue(obj instanceof Node);
        node = (Node) obj;
        assertNotNull(node);
    }

    /**
     * Interop Scenario 8.1 - Invoke ClearLog on an instance of RecordLog class
     */
    public void testInvoke() throws Exception {

        final String RECORD_LOG_RESOURCE =
            "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_RecordLog";

        final String CLEAR_RECORD_LOG_ACTION =
            "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_RecordLog/ClearLog";

        final Management mgmt = new Management();
        mgmt.setAction(CLEAR_RECORD_LOG_ACTION);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RECORD_LOG_RESOURCE);

        final Set<SelectorType> selectors = new HashSet<SelectorType>();

        final SelectorType selector1 = new SelectorType();
        selector1.setName("InstanceID");
        selector1.getContent().add("IPMI:IPMI Controller 32 SEL Log");
        selectors.add(selector1);

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

        final QName INPUT = new QName(RECORD_LOG_RESOURCE, "ClearLog_INPUT", "p");
        mgmt.getBody().addBodyElement(INPUT);

        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }

        final String CLEAR_RECORD_LOG_RESPONSE =
            "http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_RecordLog/ClearLogResponse";

        final QName OUTPUT = new QName(RECORD_LOG_RESOURCE, "ClearLog_OUTPUT", "p");
        final QName RETURN_VALUE = new QName(RECORD_LOG_RESOURCE, "ReturnValue", "p");

        assertEquals(CLEAR_RECORD_LOG_RESPONSE, response.getAction());
        final Node output = response.getBody().getFirstChild();
        assertNotNull(output);
        assertEquals(OUTPUT.getLocalPart(), output.getLocalName());

        final Node retvalue = output.getFirstChild();
        assertNotNull(retvalue);
        assertEquals(RETURN_VALUE.getLocalPart(), retvalue.getLocalName());
        assertEquals("0", retvalue.getTextContent());
    }

    /**
     * Interop Scenario 9.1 - Change Threshold on an instance of CIM_NumericSensor
     */
    public void testPut() throws Exception {

        final Management mgmt = new Management();
        mgmt.setAction(Transfer.PUT_ACTION_URI);
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

        final BigInteger envSize = new BigInteger("153600");
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        maxEnvSize.setValue(envSize);
        maxEnvSize.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.TRUE);
        mgmt.setMaxEnvelopeSize(maxEnvSize);

        final Locale locale = Management.FACTORY.createLocale();
        locale.setLang(XML.DEFAULT_LANG);
        locale.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, SOAP.FALSE);
        mgmt.setLocale(locale);

        Document resourceDoc = null;
        final String resourceDocName = "Put.xml";
        final InputStream is = InteropTest.class.getResourceAsStream(resourceDocName);
        if (is == null) {
            fail("Failed to load " + resourceDocName);
        }
        try {
            resourceDoc = mgmt.getDocumentBuilder().parse(is);
        } catch (Exception ex) {
            fail("Error parsing " + resourceDocName + ": " + ex.getMessage());
        }

        mgmt.getBody().addDocument(resourceDoc);

        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }

        final Transfer to = new Transfer(response);

        assertEquals(Transfer.PUT_RESPONSE_URI, to.getAction());
        final SOAPElement[] item = to.getChildren(to.getBody());
        assertNotNull(item);
        assertTrue(item.length == 1);
        assertNotNull(item[0]);

        final QName lowerThresholdName = new QName(NUMERIC_SENSOR_RESOURCE,
            "LowerThresholdNonCritical", "p");
        final SOAPElement[] lowerThreshold = to.getChildren(item[0], lowerThresholdName);
        assertNotNull(lowerThreshold);
        assertTrue(lowerThreshold.length == 1);
        assertEquals("100", lowerThreshold[0].getTextContent());
    }

    /**
     * Interop Scenario 9.2 - Fragment Put
     */
    public void testFragmentPut() throws Exception {

        final Management mgmt = new Management();
        mgmt.setAction(Transfer.PUT_ACTION_URI);
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
        selector2.getContent().add("81.0.32");
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
        txi.setFragmentHeader("//p:LowerThresholdNonCritical", null);

        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
        final JAXBElement<MixedDataType> xmlFragment = Management.FACTORY.createXmlFragment(mixedDataType);
        Element lowerThresholdNonCriticalElement =
            mgmt.getBody().getOwnerDocument().createElementNS(NUMERIC_SENSOR_RESOURCE,
            "p:LowerThresholdNonCritical");
        lowerThresholdNonCriticalElement.setTextContent("100");
        mixedDataType.getContent().add(lowerThresholdNonCriticalElement);
        mgmt.getXmlBinding().marshal(xmlFragment, mgmt.getBody());

        log(mgmt);
        final Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }

        final TransferExtensions txo = new TransferExtensions(response);

        assertEquals(Transfer.PUT_RESPONSE_URI, txo.getAction());
        final SOAPHeaderElement hdr = txo.getFragmentHeader();
        assertNotNull(hdr);
        final SOAPElement[] fragment = txo.getChildren(txo.getBody(), TransferExtensions.XML_FRAGMENT);
        assertNotNull(fragment);
        assertTrue(fragment.length == 1);
        final SOAPElement[] threshold = txo.getChildren(fragment[0]);
        assertNotNull(threshold);
        assertTrue(threshold.length == 1);
        assertEquals("LowerThresholdNonCritical", threshold[0].getLocalName());
        assertEquals("100", threshold[0].getTextContent());
    }

    /**
     * Interop Scenario 10 - Eventing
     */
    public void testEventing() throws Exception {

        Management mgmt = new Management();
        mgmt.setAction(Eventing.SUBSCRIBE_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(PULL_SOURCE_RESOURCE);

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

        final EventingExtensions exi = new EventingExtensions(mgmt);
        exi.setSubscribe(null, EventingExtensions.PULL_DELIVERY_MODE, null, null, null);

        log(mgmt);
        Addressing response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }

        final Eventing eo = new Eventing(response);
        assertEquals(Eventing.SUBSCRIBE_RESPONSE_URI, eo.getAction());
        final SubscribeResponse sr = eo.getSubscribeResponse();
        assertNotNull(sr);
        assertNotNull(sr.getExpires());
        final EndpointReferenceType sm = sr.getSubscriptionManager();
        assertNotNull(sm);
        assertNotNull(sm.getAddress());
//        final Object identifierElement = sm.getReferenceProperties().getAny().get(0);
        final Object identifierElement = sm.getReferenceParameters().getAny().get(1);
        assertNotNull(identifierElement);
        final String identifier = ((JAXBElement<String>) identifierElement).getValue();
        assertNotNull(identifier);
        Object context = null;
        final Object obj = sr.getAny().get(0);
        if (obj instanceof Element) {
            final Element elt = (Element) obj;
            assertEquals(Enumeration.ENUMERATION_CONTEXT.getLocalPart(), elt.getLocalName());
            context = elt.getTextContent();
        }
        assertNotNull(context);

        // pull an event

        mgmt = new Management();
        mgmt.setAction(Enumeration.PULL_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(PULL_SOURCE_RESOURCE);

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
        EnumerationContextType ect = pr.getEnumerationContext();
        assertNotNull(ect);
        context = ect.getContent().get(0);
        assertNotNull(context);
        ItemListType ilt = pr.getItems();
        assertNotNull(ilt);
        List<Object> il = ilt.getAny();
        assertNotNull(il);
        assertTrue(il.size() == 1);
        assertNotNull(il.get(0));

        // unsubscribe

        mgmt = new Management();
        mgmt.setAction(Eventing.UNSUBSCRIBE_ACTION_URI);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(PULL_SOURCE_RESOURCE);

        mgmt.setTimeout(timeout);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        mgmt.setLocale(locale);

        Eventing unsub = new Eventing(mgmt);
        unsub.setUnsubscribe();
        unsub.setIdentifier(identifier);

        log(mgmt);
        response = HttpClient.sendRequest(mgmt);
        log(response);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }

        Eventing unsubo = new Eventing(response);
        assertEquals(Eventing.UNSUBSCRIBE_RESPONSE_URI, unsubo.getAction());
        assertNull(unsubo.getBody().getFirstChild());
    }
}
