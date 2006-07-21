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
 * $Id: EnumerationTest.java,v 1.15 2006-07-21 20:26:24 pmonday Exp $
 */

package management;

import com.sun.ws.management.Management;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationElement;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XPath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableNonNegativeInteger;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Pull;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Release;

/**
 * Unit test for WS-Enumeration
 */
public class EnumerationTest extends TestBase {
    
    public EnumerationTest(final String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(EnumerationTest.class);
        return suite;
    }
    
    public void testEnumerateVisual() throws Exception {
        
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        
        final EndpointReferenceType endTo = enu.createEndpointReference("http://host/endTo", null, null, null, null);
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final FilterType filter = Enumeration.FACTORY.createFilterType();
        filter.setDialect("http://mydomain/my.filter.dialect");
        filter.getContent().add("my/filter/expression");
        enu.setEnumerate(endTo, expires, filter);
        
        enu.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        
        final Enumerate enu2 = e2.getEnumerate();
        assertEquals(expires, enu2.getExpires());
        assertEquals(endTo.getAddress().getValue(), enu2.getEndTo().getAddress().getValue());
        assertEquals(filter.getDialect(), enu2.getFilter().getDialect());
        assertEquals(filter.getContent().get(0), enu2.getFilter().getContent().get(0));
    }
    public void testEnumerateResponseVisual() throws Exception {
        
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
        
        final String context = "context";
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        enu.setEnumerateResponse(context, expires);
        
        enu.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        
        final EnumerateResponse er2 = e2.getEnumerateResponse();
        assertEquals(expires, er2.getExpires());
        assertEquals(context, er2.getEnumerationContext().getContent().get(0));
    }
    
    public void testPullVisual() throws Exception {
        
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.PULL_ACTION_URI);
        
        final String context = "context";
        final int maxChars = 4096;
        final int maxElements = 2;
        final Duration duration = DatatypeFactory.newInstance().newDuration(30000);
        enu.setPull(context, maxChars, maxElements, duration);
        
        enu.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        
        final Pull p2 = e2.getPull();
        assertEquals(context, p2.getEnumerationContext().getContent().get(0));
        assertEquals(maxChars, p2.getMaxCharacters().intValue());
        assertEquals(maxElements, p2.getMaxElements().intValue());
        assertEquals(duration, p2.getMaxTime());
    }
    
    public void testPullResponseVisual() throws Exception {
        
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.PULL_RESPONSE_URI);
        
        final String context = "context";
        final List<EnumerationElement> items = new ArrayList<EnumerationElement>();
        final Document doc = enu.newDocument();
        final Element itemElement = doc.createElementNS(NS_URI, NS_PREFIX + ":anItem");
        EnumerationElement ee = new EnumerationElement();
        ee.setElement(itemElement);
        items.add(ee);
        enu.setPullResponse(items, context, null, true);
        
        enu.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        
        final PullResponse pr2 = e2.getPullResponse();
        assertEquals(context, pr2.getEnumerationContext().getContent().get(0));
        assertEquals(itemElement.getNodeName(), ((Element) pr2.getItems().getAny().get(0)).getNodeName());
        assertNull(pr2.getEndOfSequence());
    }
    
    public void testReleaseVisual() throws Exception {
        
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.RELEASE_ACTION_URI);
        
        final String context = "context";
        enu.setRelease(context);
        
        enu.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        
        final Release r2 = e2.getRelease();
        assertEquals(context, r2.getEnumerationContext().getContent().get(0));
    }
    
    public void testEnumerate() throws Exception {
        enumerateTest(null);
        enumerateTest("/java:java.specification.version");
    }
    
    /**
     * Test ability to return EPRs in an enumeration
     * rather than objects
     */

    public void testEnumerateEPR() throws Exception {
        enumerateTestWithEPRs(false);
    }

    
    /**
     * Test ability to return objects and their associated Endpoint References
     * in an enumeration
     */
    public void testEnumerateObjectAndEPR() throws Exception {
        enumerateTestWithEPRs(true);
    }

    public void testRelease() throws Exception {
        
        final String RESOURCE = "wsman:test/java/system/properties";
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        enu.setEnumerate(null, null, null);
        
        final Management mgmt = new Management(enu);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            response.prettyPrint(System.err);
            fail(response.getBody().getFault().getFaultString());
        }
        
        final Enumeration enuResponse = new Enumeration(response);
        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
        String context = (String) enr.getEnumerationContext().getContent().get(0);
        
        final Enumeration releaseRequest = new Enumeration();
        releaseRequest.setAction(Enumeration.RELEASE_ACTION_URI);
        releaseRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        releaseRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        releaseRequest.setRelease(context);
        
        final Management mr = new Management(releaseRequest);
        mr.setTo(DESTINATION);
        mr.setResourceURI(RESOURCE);
        
        mr.prettyPrint(logfile);
        final Addressing rraddr = HttpClient.sendRequest(mr);
        rraddr.prettyPrint(logfile);
        if (rraddr.getBody().hasFault()) {
            rraddr.prettyPrint(System.err);
            fail(rraddr.getBody().getFault().getFaultString());
        }
        
        final Enumeration pullRequest = new Enumeration();
        pullRequest.setAction(Enumeration.PULL_ACTION_URI);
        pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        pullRequest.setPull(context, 0, 3, null);
        
        final Management mp = new Management(pullRequest);
        mp.setTo(DESTINATION);
        mp.setResourceURI(RESOURCE);
        
        mp.prettyPrint(logfile);
        final Addressing praddr = HttpClient.sendRequest(mp);
        praddr.prettyPrint(logfile);
        if (!praddr.getBody().hasFault()) {
            fail("pull after release succeeded");
        }
        
        final Fault fault = new Addressing(praddr).getFault();
        assertEquals(SOAP.RECEIVER, fault.getCode().getValue());
        assertEquals(InvalidEnumerationContextFault.INVALID_ENUM_CONTEXT, fault.getCode().getSubcode().getValue());
        assertEquals(InvalidEnumerationContextFault.INVALID_ENUM_CONTEXT_REASON, fault.getReason().getText().get(0).getValue());
    }
    
    private void enumerateTest(final String filter) throws Exception {
        
        final String RESOURCE = "wsman:test/java/system/properties";
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        final DatatypeFactory factory = DatatypeFactory.newInstance();
        final FilterType filterType = Enumeration.FACTORY.createFilterType();
        filterType.setDialect(XPath.NS_URI);
        filterType.getContent().add(filter);
        enu.setEnumerate(null, factory.newDuration(60000).toString(),
                filter == null ? null : filterType);
        
        // request total item count estimates
        final EnumerationExtensions enqx = new EnumerationExtensions(enu);
        enqx.setRequestTotalItemsCountEstimate();
        
        final Management mgmt = new Management(enu);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            response.prettyPrint(System.err);
            // this test fails with an AccessDenied fault if the server is
            // running in the sun app server with a security manager in
            // place (the default), which disallows enumeration of
            // system properties
            fail(response.getBody().getFault().getFaultString());
        }
        
        final Enumeration enuResponse = new Enumeration(response);
        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
        String context = (String) enr.getEnumerationContext().getContent().get(0);
        
        final EnumerationExtensions erx = new EnumerationExtensions(enuResponse);
        final AttributableNonNegativeInteger ee = erx.getTotalItemsCountEstimate();
        assertNotNull(ee);
        assertTrue(ee.getValue().intValue() > 0);
        
        boolean done = false;
        do {
            final Enumeration pullRequest = new Enumeration();
            pullRequest.setAction(Enumeration.PULL_ACTION_URI);
            pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
            pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
            pullRequest.setPull(context, 0, 3, factory.newDuration(30000));
            
            // request total item count estimates
            final EnumerationExtensions pqx = new EnumerationExtensions(pullRequest);
            pqx.setRequestTotalItemsCountEstimate();
        
            final Management mp = new Management(pullRequest);
            mp.setTo(DESTINATION);
            mp.setResourceURI(RESOURCE);
            
            mp.prettyPrint(logfile);
            final Addressing praddr = HttpClient.sendRequest(mp);
            praddr.prettyPrint(logfile);
            if (praddr.getBody().hasFault()) {
                praddr.prettyPrint(System.err);
                fail(praddr.getBody().getFault().getFaultString());
            }
            
            final Enumeration pullResponse = new Enumeration(praddr);
            final PullResponse pr = pullResponse.getPullResponse();
            // update context for the next pull (if any)
            if (pr.getEnumerationContext() != null) {
                context = (String) pr.getEnumerationContext().getContent().get(0);
            }
            for (Object obj : pr.getItems().getAny()) {
                final Element el = (Element) obj;
                // commented to reduce clutter: uncomment to see the output
                // System.out.println(el.getNodeName() + " = " + el.getTextContent());
            }
            if (pr.getEndOfSequence() != null) {
                done = true;
            }

            final EnumerationExtensions prx = new EnumerationExtensions(pullResponse);
            final AttributableNonNegativeInteger pe = prx.getTotalItemsCountEstimate();
            assertNotNull(pe);
            assertTrue(pe.getValue().intValue() > 0);
        } while (!done);
    }

    /**
     * Workhorse method for the enumeration test with EPRs
     * @param includeObjects determines whether the objects should be included
     * in addition to the EPRs themselves
     */
    private void enumerateTestWithEPRs(final boolean includeObjects) throws Exception {
        
        final String RESOURCE = "wsman:test/java/system/properties";
        
        /*
         * Prepare the Enumeration Request
         */
        final EnumerationExtensions enu = new EnumerationExtensions();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        final DatatypeFactory factory = DatatypeFactory.newInstance();
        final EnumerationExtensions.Mode enumerationMode = 
                includeObjects ? 
                    /* EnumerationModeType.valueOf("EnumerateObjectAndEPR") */
                    EnumerationExtensions.Mode.EnumerateObjectAndEPR
                    : 
                    /* EnumerationModeType.valueOf("EnumerateEPR") */
                    EnumerationExtensions.Mode.EnumerateEPR ;
                    
        enu.setEnumerate(null, factory.newDuration(60000).toString(),
                null, enumerationMode);

        /*
         * Prepare the request
         */
        final Management mgmt = new Management(enu);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);
        mgmt.prettyPrint(logfile);
        
        /*
         * Retrieve the response
         */
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            response.prettyPrint(System.err);
            // this test fails with an AccessDenied fault if the server is
            // running in the sun app server with a security manager in
            // place (the default), which disallows enumeration of
            // system properties
            fail(response.getBody().getFault().getFaultString());
        }
        
        /*
         * Prepare response objects 
         */
        final EnumerationExtensions enuResponse = new EnumerationExtensions(response);
        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
        String context = (String) enr.getEnumerationContext().getContent().get(0);

        /*
         * Walk through the response
         */
        boolean done = false;
        do {
            /*
             * Set up a request to pull the next item of the enumeration
             */
            final EnumerationExtensions pullRequest = new EnumerationExtensions();
            pullRequest.setAction(Enumeration.PULL_ACTION_URI);
            pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
            pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
            pullRequest.setPull(context, 0, 3, factory.newDuration(30000));

            /*
             * Set up the target
             */
            final Management mp = new Management(pullRequest);
            mp.setTo(DESTINATION);
            mp.setResourceURI(RESOURCE);
            mp.prettyPrint(logfile);
            final Addressing praddr = HttpClient.sendRequest(mp);
            praddr.prettyPrint(logfile);
            
            /*
             * Fail if response is an error
             */
            if (praddr.getBody().hasFault()) {
                praddr.prettyPrint(System.err);
                fail(praddr.getBody().getFault().getFaultString());
            }
            
            /*
             * Check the response for appropriate EPRs
             */
            final EnumerationExtensions pullResponse = new EnumerationExtensions(praddr);
            final PullResponse pr = pullResponse.getPullResponse();
            // update context for the next pull (if any)
            if (pr.getEnumerationContext() != null) {
                context = (String) pr.getEnumerationContext().getContent().get(0);
            }

            /*
             * The returned items are in one of two forms, if we are doing
             * an enumeration of the form EnumerateEPR, then every item will
             * be an EPR, as in:
             * <wsa:EndpointReference> ... </wsa:EndpointReference>
             *
             * If the enumeration is of the form EnumerateObjectAndEPR, then
             * every item will be of the form
             * <payloadobject>...</payloadobject>
             * <wsa:EndpointReference> ... </wsa:EndpointReference>
             * Basically, the EPRs will go right after the payload objects
             *
             * We will verify that the proper sequence of EPRs
             * are in place.  We will <i>not</i> validate that the EPRs are
             * usable
             */
            final List<Object> items = pr.getItems().getAny();
            final Iterator itemsIterator = items.iterator();
            while(itemsIterator.hasNext()) {
                Object obj = itemsIterator.next();
                
                if(includeObjects) {
                    // this should be a payload object, ignore and retrieve the next
                    // which should be an EPRs
                    if(itemsIterator.hasNext()) {
                        obj = itemsIterator.next();
                    } else {
                        fail("EnumerateObjectAndEPR should be in pairs and was not");
                    }
                }
                
                final JAXBElement<EndpointReferenceType> eprJaxb = (JAXBElement<EndpointReferenceType>)obj;
                final EndpointReferenceType epr = eprJaxb.getValue();
                
                // validate that the element is an EndpointReference
                final String address = epr.getAddress().getValue();
                assertEquals(address, DESTINATION);
            }
            
            if (pr.getEndOfSequence() != null) {
                done = true;
            }

        } while (!done);
    }
}
