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
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.27  2007/05/30 20:30:23  nbeers
 **Add HP copyright header
 **
 **
 * $Id: EnumerationTest.java,v 1.28 2007-06-19 19:50:39 nbeers Exp $
 */

package management;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

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

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.server.handler.wsman.test.enumeration.filter.custom_filter_Handler;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

/**
 * Unit test for WS-Enumeration
 */
public class EnumerationTest extends TestBase {

	XmlBinding binding;

    public EnumerationTest(final String testName) {
        super(testName);

		// Set the system property to always create bindings with our packages
		System.setProperty(XmlBinding.class.getPackage().getName() + ".custom.packagenames",
				"");
		try {
			binding = new XmlBinding(null);
		} catch (JAXBException e) {
			fail(e.getMessage());
		}
    }

    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(EnumerationTest.class);
        return suite;
    }

    public void testEnumerateVisual() throws Exception {

        final Enumeration enu = new Enumeration();
        enu.setXmlBinding(binding);
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);

        final EndpointReferenceType endTo = Addressing.createEndpointReference("http://host/endTo", null, null, null, null);
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final FilterType filter = Enumeration.FACTORY.createFilterType();
        filter.setDialect("http://mydomain/my.filter.dialect");
        filter.getContent().add("my/filter/expression");
        enu.setEnumerate(endTo, expires, filter);

        enu.prettyPrint(logfile);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        e2.setXmlBinding(binding);

        final Enumerate enu2 = e2.getEnumerate();
        assertEquals(expires, enu2.getExpires());
        assertEquals(endTo.getAddress().getValue(), enu2.getEndTo().getAddress().getValue());
        assertEquals(filter.getDialect(), e2.getFilter().getDialect());
        assertEquals(filter.getContent().get(0), e2.getFilter().getContent().get(0));
    }
    public void testEnumerateResponseVisual() throws Exception {

        final Enumeration enu = new Enumeration();
        enu.setXmlBinding(binding);
        enu.setAction(Enumeration.ENUMERATE_RESPONSE_URI);

        final String context = "context";
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        enu.setEnumerateResponse(context, expires);

        enu.prettyPrint(logfile);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        e2.setXmlBinding(binding);

        final EnumerateResponse er2 = e2.getEnumerateResponse();
        assertEquals(expires, er2.getExpires());
        assertEquals(context, er2.getEnumerationContext().getContent().get(0));
    }

    public void testPullVisual() throws Exception {

        final Enumeration enu = new Enumeration();
        enu.setXmlBinding(binding);
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
        e2.setXmlBinding(binding);

        final Pull p2 = e2.getPull();
        assertEquals(context, p2.getEnumerationContext().getContent().get(0));
        assertEquals(maxChars, p2.getMaxCharacters().intValue());
        assertEquals(maxElements, p2.getMaxElements().intValue());
        assertEquals(duration, p2.getMaxTime());
    }

    public void testPullResponseVisual() throws Exception {

        final Enumeration enu = new Enumeration();
        enu.setXmlBinding(binding);
        enu.setAction(Enumeration.PULL_RESPONSE_URI);

        final String context = "context";
        final List<EnumerationItem> items = new ArrayList<EnumerationItem>();
        final Document doc = enu.newDocument();
        final Element itemElement = doc.createElementNS(NS_URI, NS_PREFIX + ":anItem");
        EnumerationItem ee = new EnumerationItem(itemElement, null);
        items.add(ee);
        enu.setPullResponse(items, context, true);

        enu.prettyPrint(logfile);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        e2.setXmlBinding(binding);

        final PullResponse pr2 = e2.getPullResponse();
        assertEquals(context, pr2.getEnumerationContext().getContent().get(0));
        assertEquals(itemElement.getNodeName(), ((Element) pr2.getItems().getAny().get(0)).getNodeName());
        assertNull(pr2.getEndOfSequence());
    }

    public void testReleaseVisual() throws Exception {

        final Enumeration enu = new Enumeration();
        enu.setXmlBinding(binding);
        enu.setAction(Enumeration.RELEASE_ACTION_URI);

        final String context = "context";
        enu.setRelease(context);

        enu.prettyPrint(logfile);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        enu.writeTo(bos);
        final Enumeration e2 = new Enumeration(new ByteArrayInputStream(bos.toByteArray()));
        e2.setXmlBinding(binding);

        final Release r2 = e2.getRelease();
        assertEquals(context, r2.getEnumerationContext().getContent().get(0));
    }

    public void testEnumerate() throws Exception {
        enumerateTest(null, null);
        HashMap<String, String> map = new HashMap<String, String>(1);
        map.put("java", "https://wiseman.dev.java.net/java");
        NamespaceMap filterNsMap = new NamespaceMap(map);
        enumerateTest("/java:java.specification.version", filterNsMap);
        enumerateTest(null, custom_filter_Handler.TEST_CUSTOM_FILTER_DIALECT,
                null, "wsman:test/enumeration/filter/custom_filter");
    }

    public void testRelease() throws Exception {

        final String RESOURCE = "wsman:test/java/system/properties";
        final Enumeration enu = new Enumeration();
        enu.setXmlBinding(binding);
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        enu.setEnumerate(null, null, null);

        final Management mgmt = new Management(enu);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);

        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.setXmlBinding(binding);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            response.prettyPrint(System.err);
            fail(response.getBody().getFault().getFaultString());
        }

        final Enumeration enuResponse = new Enumeration(response);
        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
        String context = (String) enr.getEnumerationContext().getContent().get(0);

        final Enumeration releaseRequest = new Enumeration();
        releaseRequest.setXmlBinding(binding);
        releaseRequest.setAction(Enumeration.RELEASE_ACTION_URI);
        releaseRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        releaseRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        releaseRequest.setRelease(context);

        final Management mr = new Management(releaseRequest);
        mr.setTo(DESTINATION);
        mr.setResourceURI(RESOURCE);

        mr.prettyPrint(logfile);
        final Addressing rraddr = HttpClient.sendRequest(mr);
        rraddr.setXmlBinding(binding);
        rraddr.prettyPrint(logfile);
        if (rraddr.getBody().hasFault()) {
            rraddr.prettyPrint(System.err);
            fail(rraddr.getBody().getFault().getFaultString());
        }

        final Enumeration pullRequest = new Enumeration();
        pullRequest.setXmlBinding(binding);
        pullRequest.setAction(Enumeration.PULL_ACTION_URI);
        pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        pullRequest.setPull(context, 0, 3, null);

        final Management mp = new Management(pullRequest);
        mp.setTo(DESTINATION);
        mp.setResourceURI(RESOURCE);

        mp.prettyPrint(logfile);
        final Addressing praddr = HttpClient.sendRequest(mp);
        praddr.setXmlBinding(binding);
        praddr.prettyPrint(logfile);
        if (!praddr.getBody().hasFault()) {
            fail("pull after release succeeded");
        }

        final Fault fault = new Addressing(praddr).getFault();
        assertEquals(SOAP.RECEIVER, fault.getCode().getValue());
        assertEquals(InvalidEnumerationContextFault.INVALID_ENUM_CONTEXT, fault.getCode().getSubcode().getValue());
        assertEquals(InvalidEnumerationContextFault.INVALID_ENUM_CONTEXT_REASON, fault.getReason().getText().get(0).getValue());
    }

    private void enumerateTest(String filter, final NamespaceMap filterNsMap) throws Exception {
        enumerateTest(filter, XPath.NS_URI, filterNsMap, "wsman:test/java/system/properties");
    }

    private void enumerateTest(final Object filter,
            final String dialect, final NamespaceMap filterNsMap, final String RESOURCE) throws Exception {

        final Enumeration enu = new Enumeration();
        enu.setXmlBinding(binding);
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        final DatatypeFactory factory = DatatypeFactory.newInstance();
        final FilterType filterType = Enumeration.FACTORY.createFilterType();
        filterType.setDialect(dialect);
        filterType.getContent().add(filter);
        enu.setEnumerate(null, factory.newDuration(60000).toString(),
                filter == null ? null : filterType);
        // TODO: Set this in the filter header.
        if ((filter != null) && (filterNsMap != null))
             enu.addNamespaceDeclarations(filterNsMap.getMap());

        final Management mgmt = new Management(enu);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(RESOURCE);

        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.setXmlBinding(binding);
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

        boolean done = false;
        do {
            final Enumeration pullRequest = new Enumeration();
            pullRequest.setXmlBinding(binding);
            pullRequest.setAction(Enumeration.PULL_ACTION_URI);
            pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
            pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
            pullRequest.setPull(context, 0, 3, factory.newDuration(30000));

            final Management mp = new Management(pullRequest);
            mp.setTo(DESTINATION);
            mp.setResourceURI(RESOURCE);
            mp.setTimeout(factory.newDuration(30000));

            mp.prettyPrint(logfile);
            final Addressing praddr = HttpClient.sendRequest(mp);
            praddr.setXmlBinding(binding);
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

        } while (!done);
    }
}