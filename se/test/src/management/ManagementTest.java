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
 * $Id: ManagementTest.java,v 1.15 2006-05-02 17:19:03 akhilarora Exp $
 */

package management;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.addressing.DestinationUnreachableFault;
import com.sun.ws.management.addressing.MessageInformationHeaderRequiredFault;
import com.sun.ws.management.transport.HttpClient;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XMLSchema;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeaderElement;
import org.dmtf.schemas.wbem.wsman._1.wsman.Locale;
import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.dmtf.schemas.wbem.wsman._1.wsman.PolicyType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;

/**
 * Unit test for WS-Management
 */
public class ManagementTest extends TestBase {
    
    private static final String RESOURCE = "wsman:system/2005/02/this";
    private static final int TIMEOUT = 30000;
    
    private final Set<SelectorType> selectors = new HashSet<SelectorType>();
    
    public ManagementTest(final String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        final SelectorType selector = new SelectorType();
        selector.setName("Name");
        selector.getContent().add("WSMAN");
        selectors.add(selector);
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
        final Fault fault = new Addressing(response).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(DestinationUnreachableFault.DESTINATION_UNREACHABLE, fault.getCode().getSubcode().getValue());
        assertEquals(DestinationUnreachableFault.DESTINATION_UNREACHABLE_REASON, fault.getReason().getText().get(0).getValue());
        for (final Object detail : fault.getDetail().getAny()) {
            if (detail instanceof Node) {
                final Node de = (Node) detail;
            } else {
                final String str = ((JAXBElement<String>) detail).getValue();
                assertEquals(DestinationUnreachableFault.Detail.INVALID_RESOURCE_URI.toString(), str);
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
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);
        final Duration timeout = DatatypeFactory.newInstance().newDuration(TIMEOUT);
        mgmt.setTimeout(timeout);
        mgmt.setSelectors(selectors);
        
        final MaxEnvelopeSizeType maxEnvSize = Management.FACTORY.createMaxEnvelopeSizeType();
        final long envSize = 4096;
        maxEnvSize.setValue(new BigInteger(Long.toString(envSize)));
        final String envPolicy = "Skip";
        final PolicyType policyType = PolicyType.fromValue(envPolicy);
        maxEnvSize.setPolicy(policyType);
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        final Locale locale = Management.FACTORY.createLocale();
        final String localeString = "en-US";
        locale.setLang(localeString);
        mgmt.setLocale(locale);
        
        final Set<OptionType> options = new HashSet<OptionType>();
        final String verboseOptionName = "--output-file";
        final String verboseOptionValue = "/dev/null";
        final QName verboseOptionType = new QName(XMLSchema.NS_URI, "string", XMLSchema.NS_PREFIX);
        final OptionType verboseOption = new OptionType();
        verboseOption.setName(verboseOptionName);
        verboseOption.setValue(verboseOptionValue);
        verboseOption.setType(verboseOptionType);
        options.add(verboseOption);
        mgmt.setOptions(options);
        
        mgmt.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mgmt.writeTo(bos);
        final Management m2 = new Management(new ByteArrayInputStream(bos.toByteArray()));
        
        assertEquals(RESOURCE, m2.getResourceURI());
        assertEquals(timeout, m2.getTimeout());
        
        final Set<SelectorType> selectors2 = mgmt.getSelectors();
        assertEquals(selectors.size(), selectors2.size());
        final SelectorType selector = selectors.iterator().next();
        final SelectorType selector2 = selectors2.iterator().next();
        assertEquals(selector.getName(), selector2.getName());
        assertEquals(selector.getContent().get(0), selector2.getContent().get(0));
        
        final MaxEnvelopeSizeType maxEnvSize2 = m2.getMaxEnvelopeSize();
        assertEquals(envSize, maxEnvSize2.getValue().longValue());
        assertEquals(envPolicy, maxEnvSize2.getPolicy().value());
        
        assertEquals(localeString, m2.getLocale().getLang());
        
        final Set<OptionType> options2 = m2.getOptions();
        final OptionType option2 = options2.iterator().next();
        assertEquals(verboseOptionName, option2.getName());
        assertEquals(verboseOptionValue, option2.getValue());
        assertEquals(verboseOptionType, option2.getType());
    }
    
    public void testActionNotSupported() throws Exception {
        
        final Transfer xf = new Transfer();
        final String UNSUPPORTED_ACTION = "some/random/action";
        xf.setAction(UNSUPPORTED_ACTION);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final Management mgmt = new Management(xf);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (!response.getBody().hasFault()) {
            fail("Unsupported action accepted");
        }

        final Fault fault = new Addressing(response).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(ActionNotSupportedFault.ACTION_NOT_SUPPORTED, fault.getCode().getSubcode().getValue());
        assertEquals(ActionNotSupportedFault.ACTION_NOT_SUPPORTED_REASON, fault.getReason().getText().get(0).getValue());
        assertEquals(UNSUPPORTED_ACTION, 
                ((JAXBElement<AttributedURI>) fault.getDetail().getAny().get(0)).getValue().getValue());
    }
    
    public void testGet() throws Exception {
        
        final Transfer xf = new Transfer();
        xf.setAction(Transfer.GET_ACTION_URI);
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final Management mgmt = new Management(xf);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);
        final Duration timeout = DatatypeFactory.newInstance().newDuration(TIMEOUT);
        mgmt.setTimeout(timeout);
        
        final Set<SelectorType> selectorSet = new HashSet<SelectorType>();
        final SelectorType selector = new SelectorType();
        selector.setName("SystemName");
        selector.getContent().add("sun-v20z-1");
        mgmt.setSelectors(selectorSet);
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        final Document doc = response.getBody().extractContentAsDocument();
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
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        mgmt.prettyPrint(logfile);
        
        final SOAPHeaderElement[] she = mgmt.getAllMustUnderstand();
        // wsa:Action, wsa:To, wsa:MessageID are the three headers with MU=1
        assertEquals(3, she.length);
        for (final SOAPHeaderElement hdr : she) {
            assertTrue(hdr.getMustUnderstand());
        }
        
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        try {
            mgmt.validate();
        } catch (FaultException fex) {
            fail("Validation failed: " + fex.toString());
        }
        
        final Management mgmt2 = new Management();
        mgmt2.setTo(DESTINATION);
        mgmt2.setResourceURI(RESOURCE);
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
        mgmt4.setTo(DESTINATION);
        mgmt4.setResourceURI(RESOURCE);
        mgmt4.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        doValidation(mgmt4, Addressing.NS_PREFIX + ":ReplyTo");
        
        final Management mgmt5 = new Management();
        mgmt5.setAction(Transfer.GET_ACTION_URI);
        mgmt5.setTo(DESTINATION);
        mgmt5.setResourceURI(RESOURCE);
        mgmt5.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        doValidation(mgmt5, Addressing.NS_PREFIX + ":MessageID");
    }
    
    private void doValidation(final Management mgmt, final String elementName) throws Exception {
        mgmt.prettyPrint(logfile);
        try {
            mgmt.validate();
            fail("Validation succeeded with no " + elementName);
        } catch (FaultException ignore) {}
        
        if (mgmt.getTo() != null) {
            final Addressing response = HttpClient.sendRequest(mgmt);
            assertTrue(response.getBody().hasFault());
            final Fault fault = new Addressing(response).getFault();
            assertEquals(SOAP.SENDER, fault.getCode().getValue());
            assertEquals(MessageInformationHeaderRequiredFault.MESSAGE_INFORMATION_HEADER_REQUIRED, fault.getCode().getSubcode().getValue());
            assertEquals(MessageInformationHeaderRequiredFault.MESSAGE_INFORMATION_HEADER_REQUIRED_REASON, fault.getReason().getText().get(0).getValue());
        }
    }
    
    public void testAccessDenied() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/access_denied");
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (!response.getBody().hasFault()) {
            fail("fault not returned");
        }
        final Fault fault = new Addressing(response).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(AccessDeniedFault.ACCESS_DENIED, fault.getCode().getSubcode().getValue());
        assertEquals(AccessDeniedFault.ACCESS_DENIED_REASON, fault.getReason().getText().get(0).getValue());
    }
    
    public void testNonHandler() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/non_handler");
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (!response.getBody().hasFault()) {
            fail("fault not returned");
        }
        final Fault fault = new Addressing(response).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(DestinationUnreachableFault.DESTINATION_UNREACHABLE, fault.getCode().getSubcode().getValue());
        assertEquals(DestinationUnreachableFault.DESTINATION_UNREACHABLE_REASON, fault.getReason().getText().get(0).getValue());
    }
    
    public void testTimeout() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/timeout");
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        final DatatypeFactory durationFactory = DatatypeFactory.newInstance();
        mgmt.setTimeout(durationFactory.newDuration(2000));
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (!response.getBody().hasFault()) {
            fail("fault not returned");
        }
        final Fault fault = new Addressing(response).getFault();
        assertEquals(SOAP.RECEIVER, fault.getCode().getValue());
        assertEquals(TimedOutFault.TIMED_OUT, fault.getCode().getSubcode().getValue());
        assertEquals(TimedOutFault.TIMED_OUT_REASON, fault.getReason().getText().get(0).getValue());
    }
    
    public void testMaxEnvelopeSizeTooSmall() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setTo(DESTINATION);
        // the particular handler used does not matter in this case -
        // a fault should be returned before a handler is invoked
        mgmt.setResourceURI("wsman:test/timeout");
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final MaxEnvelopeSizeType maxEnvSize = new MaxEnvelopeSizeType();
        maxEnvSize.setValue(new BigInteger(Integer.toString(4)));
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (!response.getBody().hasFault()) {
            fail("fault not returned");
        }
        
        final Fault fault = new Addressing(response).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(EncodingLimitFault.ENCODING_LIMIT, fault.getCode().getSubcode().getValue());
        assertEquals(EncodingLimitFault.ENCODING_LIMIT_REASON, fault.getReason().getText().get(0).getValue());
        for (final Object detail : fault.getDetail().getAny()) {
            if (detail instanceof Node) {
                final Node de = (Node) detail;
            } else {
                final String str = ((JAXBElement<String>) detail).getValue();
                assertEquals(EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE.toString(), str);
            }
        }
    }
    
    public void testMaxEnvelopeSizeTooBig() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/huge_envelope_creator");
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        final MaxEnvelopeSizeType maxEnvSize = new MaxEnvelopeSizeType();
        maxEnvSize.setValue(new BigInteger(Integer.toString(8192 + 1)));
        mgmt.setMaxEnvelopeSize(maxEnvSize);
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (!response.getBody().hasFault()) {
            fail("fault not returned");
        }
        
        final Fault fault = new Addressing(response).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(EncodingLimitFault.ENCODING_LIMIT, fault.getCode().getSubcode().getValue());
        assertEquals(EncodingLimitFault.ENCODING_LIMIT_REASON, fault.getReason().getText().get(0).getValue());
        for (final Object detail : fault.getDetail().getAny()) {
            if (detail instanceof Node) {
                final Node de = (Node) detail;
            } else {
                final String str = ((JAXBElement<String>) detail).getValue();
                assertEquals(EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE_EXCEEDED.toString(), str);
            }
        }
    }

    public void testBaseHandler() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/base");
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (!response.getBody().hasFault()) {
            fail("fault not returned");
        }
        final Fault fault = new Addressing(response).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(DestinationUnreachableFault.DESTINATION_UNREACHABLE, fault.getCode().getSubcode().getValue());
        assertEquals(DestinationUnreachableFault.DESTINATION_UNREACHABLE_REASON, fault.getReason().getText().get(0).getValue());
        assertEquals(DestinationUnreachableFault.Detail.INVALID_RESOURCE_URI.toString(), 
                ((JAXBElement<String>) fault.getDetail().getAny().get(1)).getValue());
    }

    public void testConcreteHandler() throws Exception {
        final Management mgmt = new Management();
        mgmt.setAction(Transfer.GET_ACTION_URI);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/concrete");
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        final SOAPBody body = response.getBody();
        if (body.hasFault()) {
            fail("invocation of concrete handler failed: " + 
                    body.getFault().getFaultString());
        }
        assertNotNull(body.getFirstChild());
    }
}
