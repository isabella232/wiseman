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
 * $Id: EnumerationExtensionsTest.java,v 1.7.2.1 2006-12-21 14:56:02 jfdenise Exp $
 */

package management;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableNonNegativeInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.transport.HttpClient;

/**
 * Unit test for WS-Enumeration extensions in WS-Management
 */
public class EnumerationExtensionsTest extends TestBase {
    
    public EnumerationExtensionsTest(final String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(EnumerationExtensionsTest.class);
        return suite;
    }
    
    public void testEnumerateVisual() throws Exception {
        
        final EnumerationExtensions enu = new EnumerationExtensions();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        
        final EndpointReferenceType endTo = Addressing.createEndpointReference("http://host/endTo", null, null, null, null);
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final DialectableMixedDataType filter = Management.FACTORY.createDialectableMixedDataType();
        filter.setDialect("http://mydomain/my.filter.dialect");
        filter.getContent().add("my/filter/expression");
        final EnumerationExtensions.Mode mode = EnumerationExtensions.Mode.EnumerateObjectAndEPR;
        enu.setEnumerate(endTo, false, true, 2, expires, filter, mode);
        
        enu.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final EnumerationExtensions e2 = new EnumerationExtensions(new ByteArrayInputStream(bos.toByteArray()));
        
        e2.prettyPrint(logfile);
        final Enumerate enu2 = e2.getEnumerate();
        assertEquals(expires, enu2.getExpires());
        assertEquals(endTo.getAddress().getValue(), enu2.getEndTo().getAddress().getValue());
        assertEquals(filter.getDialect(), e2.getWsmanFilter().getDialect());
        assertEquals(filter.getContent().get(0), e2.getWsmanFilter().getContent().get(0));
        assertEquals(mode, e2.getMode());
    }
    
    public void testEnumerateItemCountEstimateVisual() throws Exception {
        
        final EnumerationExtensions enu = new EnumerationExtensions();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        
        enu.setEnumerate(null, false, false, 0, null, null, null);
        
        enu.setRequestTotalItemsCountEstimate();
        
        enu.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final EnumerationExtensions e2 = new EnumerationExtensions(new ByteArrayInputStream(bos.toByteArray()));
        
        assertNotNull(e2.getRequestTotalItemsCountEstimate());
    }
    
    public void testEnumerateTotalItemCountEstimateVisual() throws Exception {
        totalItemCountEstimateVisual(BigInteger.TEN);
        totalItemCountEstimateVisual(BigInteger.ONE);
        totalItemCountEstimateVisual(BigInteger.ZERO);
        // totalItemCountEstimateVisual(null);
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
    
    public void totalItemCountEstimateVisual(final BigInteger itemCount) throws Exception {
        
        final EnumerationExtensions enu = new EnumerationExtensions();
        enu.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
        
        enu.setEnumerateResponse(null, null, null, null, true);
        enu.setTotalItemsCountEstimate(itemCount);
        
        enu.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final EnumerationExtensions e2 = new EnumerationExtensions(new ByteArrayInputStream(bos.toByteArray()));
        
        final AttributableNonNegativeInteger count = e2.getTotalItemsCountEstimate();
        assertNotNull(count);
        assertEquals(itemCount, count.getValue());
    }
    
    /**
     * Workhorse method for the enumeration test with EPRs
     * @param includeObjects determines whether the objects should be included
     * in addition to the EPRs themselves
     */
    private void enumerateTestWithEPRs(final boolean includeObjects) throws Exception {
        
        final String RESOURCE = "wsman:test/java/system/properties";
        // final String RESOURCE = "wsman:test/pull_source";
        // prepare the test
        final EnumerationExtensions enu = new EnumerationExtensions();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        final DatatypeFactory factory = DatatypeFactory.newInstance();
        final EnumerationExtensions.Mode enumerationMode =
                includeObjects ?
                    EnumerationExtensions.Mode.EnumerateObjectAndEPR
                :
                    EnumerationExtensions.Mode.EnumerateEPR ;
        
        enu.setEnumerate(null, false, false, -1, factory.newDuration(60000).toString(),
                (DialectableMixedDataType)null, enumerationMode);
        
        // prepare the request
        final Management mgmt = new Management(enu);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);
        mgmt.prettyPrint(logfile);
        
        // retrieve the response
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
        
        // Prepare response objects
        final EnumerationExtensions enuResponse = new EnumerationExtensions(response);
        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
        String context = (String) enr.getEnumerationContext().getContent().get(0);
        
        // walk the response
        boolean done = false;
        do {
            // Set up a request to pull the next item of the enumeration
            final EnumerationExtensions pullRequest = new EnumerationExtensions();
            pullRequest.setAction(Enumeration.PULL_ACTION_URI);
            pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
            pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
            pullRequest.setPull(context, 0, 3, factory.newDuration(30000));
            
            // Set up the target
            final Management mp = new Management(pullRequest);
            mp.setTo(DESTINATION);
            mp.setResourceURI(RESOURCE);
            mp.prettyPrint(logfile);
            final Addressing praddr = HttpClient.sendRequest(mp);
            praddr.prettyPrint(logfile);
            
            // Fail if response is an error
            if (praddr.getBody().hasFault()) {
                praddr.prettyPrint(System.err);
                fail(praddr.getBody().getFault().getFaultString());
            }
            
            // Check the response for appropriate EPRs
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
            List<EnumerationItem> items = pullResponse.getItems();
            assertNotNull(items);
            final Iterator<EnumerationItem> itemsIterator = items.iterator();
            while (itemsIterator.hasNext()) {
                EnumerationItem item = itemsIterator.next();
                assertNotNull(item);
                if (includeObjects)
                   assertNotNull(item.getItem());
                else
                	assertNull(item.getItem());
                assertNotNull(item.getEndpointReference());
                final EndpointReferenceType epr = item.getEndpointReference();
                
                // validate that the element is an EndpointReference
                final String address = epr.getAddress().getValue();
                assertEquals(address, DESTINATION);
            }            
            
            if (pr.getEndOfSequence() != null) {
                done = true;
            }
        } while (!done);
    }
    
    public void testOptimizedEnumeration() throws Exception {
        // first do the tests with EPRs turned off
        optimizedEnumerationTest(null, 2);
        // do not specify MaxElements, letting it default to its implied value of 1
        optimizedEnumerationTest(null, -1);
        
        // now repeat the same tests with EPRs turned on
        optimizedEnumerationTest(EnumerationExtensions.Mode.EnumerateObjectAndEPR, 2);
        // do not specify MaxElements, letting it default to its implied value of 1
        optimizedEnumerationTest(EnumerationExtensions.Mode.EnumerateObjectAndEPR, -1);
        
        // finally, repeat the same tests with only EPRs (no items)
        optimizedEnumerationTest(EnumerationExtensions.Mode.EnumerateEPR, 2);
        // do not specify MaxElements, letting it default to its implied value of 1
        optimizedEnumerationTest(EnumerationExtensions.Mode.EnumerateEPR, -1);
    }
    
    public void optimizedEnumerationTest(final EnumerationExtensions.Mode mode,
            final int maxElements) throws Exception {
        final String RESOURCE = "wsman:test/java/system/properties";
        final EnumerationExtensions enu = new EnumerationExtensions();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        final DatatypeFactory factory = DatatypeFactory.newInstance();
        enu.setEnumerate(null, true, true, maxElements, 
        		factory.newDuration(60000).toString(),
        		null, mode);
        
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
        
        final EnumerationExtensions enuResponse = new EnumerationExtensions(response);
        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
        String context = (String) enr.getEnumerationContext().getContent().get(0);
        for (final EnumerationItem item : enuResponse.getItems()) {
            assertMode(mode, item);
        }
        
        final AttributableNonNegativeInteger ee = enuResponse.getTotalItemsCountEstimate();
        assertNotNull(ee);
        assertTrue(ee.getValue().intValue() > 0);
        
        boolean done = enuResponse.isEndOfSequence();
        while (!done) {
            final EnumerationExtensions pullRequest = new EnumerationExtensions();
            pullRequest.setAction(Enumeration.PULL_ACTION_URI);
            pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
            pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
            pullRequest.setPull(context, 0, 3, factory.newDuration(30000), true);
            
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
            
            final EnumerationExtensions pullResponse = new EnumerationExtensions(praddr);
            final PullResponse pr = pullResponse.getPullResponse();
            // update context for the next pull (if any)
            if (pr.getEnumerationContext() != null) {
                context = (String) pr.getEnumerationContext().getContent().get(0);
            }
            for (final EnumerationItem item : pullResponse.getItems()) {
                assertMode(mode, item);
            }
            if (pr.getEndOfSequence() != null) {
                done = true;
            }
            
            final AttributableNonNegativeInteger pe = pullResponse.getTotalItemsCountEstimate();
            assertNotNull(pe);
            assertTrue(pe.getValue().intValue() > 0);
        }
    }
    
    private static void assertMode(final EnumerationExtensions.Mode mode,
            final EnumerationItem item) {
        
        final Object elt = item.getItem();
        final EndpointReferenceType epr = item.getEndpointReference();
        
        if (mode == null) {
            assertNotNull(elt);
            assertNull(epr);
        } else if (EnumerationExtensions.Mode.EnumerateObjectAndEPR.equals(mode)) {
            assertNotNull(elt);
            assertNotNull(epr);
        } else if (EnumerationExtensions.Mode.EnumerateEPR.equals(mode)) {
            assertNull(elt);
            assertNotNull(epr);
        } else {
            fail("invalid mode");
        }
    }
}
