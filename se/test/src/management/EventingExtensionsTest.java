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
 **Revision 1.14.6.1  2008/01/28 08:00:45  denis_rachal
 **The commit adds several prototype changes to the fudan_contribution. They are described below:
 **
 **1. A new Handler interface has been added to support the newer message types WSManagementRequest & WSManagementResponse. It is called WSHandler. Additionally a new servlet WSManReflectiveServlet2 has been added to allow calling this new handler.
 **
 **2. A new base handler has been added to support creation of WS Eventing Sink handlers: WSEventingSinkHandler.
 **
 **3. WS Eventing "Source" and "Sink" test handlers have been added to the unit tests, sink_Handler & source_Handler. Both are based upon the new WSHandler interface.
 **
 **4. The EventingExtensionsTest has been updated to test "push" events. Push events are sent from a source to a sink. The sink will forward them on to and subscribers (sink subscribers). The unit test subscribes for pull events at the "sink" and then gets the "source" to send events to the "sink". The test then pulls the events from the "sink" and checks them. Does not always run, so the test needs some work. Sometimes some of the events are lost. between the source and the sink.
 **
 **5. A prototype for handling basic authentication with the sink has been added. Events from the source can now be sent to a sink using Basic authentication (credentials are specified per subscription). This needs some additional work, but basically now works.
 **
 **6. Additional methods added to the WSManagementRequest, WSManagementResponse, WSEventingRequest & WSEventingResponse, etc... interfaces to allow access to more parts of the messages.
 **
 **Additional work is neede in all of the above changes, but they are OK for a prototype in the fudan_contributaion branch.
 **
 **Revision 1.14  2007/12/03 09:15:09  denis_rachal
 **General cleanup of Unit tests to make them easier to run and faster.
 **
 **Revision 1.13  2007/11/30 14:32:36  denis_rachal
 **Issue number:  140
 **Obtained from:
 **Submitted by:  jfdenise
 **Reviewed by:
 **
 **WSManAgentSupport and WSEnumerationSupport changed to coordinate their separate threads when handling wsman:OperationTimeout and wsen:MaxTime timeouts. If a timeout now occurs during an enumeration operation the WSEnumerationSupport is notified by the WSManAgentSupport thread. WSEnumerationSupport saves any items collected from the EnumerationIterator in the context so they may be fetched by the client on the next pull. Items are no longer lost on timeouts.
 **
 **Tests were added to correctly test this functionality and older tests were updated to properly test timeout functionality.
 **
 **Additionally some tests were updated to make better use of the XmlBinding object and improve performance on testing.
 **
 **Revision 1.12  2007/05/30 20:30:23  nbeers
 **Add HP copyright header
 **
 **
 * $Id: EventingExtensionsTest.java,v 1.14.6.2 2008-02-01 21:01:36 denis_rachal Exp $
 */

package management;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableAny;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableDuration;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributablePositiveInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.ConnectionRetryType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.PolicyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import util.TestBase;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingUtility;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.server.handler.wsman.test.events.source_Handler;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;

/**
 * Unit test for WS-Eventing extensions in WS-Management
 */
public class EventingExtensionsTest extends TestBase {

    public EventingExtensionsTest(final String testName) throws JAXBException {
        super(testName);
    }

    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(EventingExtensionsTest.class);
        return suite;
    }

    public void testSubscribeVisual() throws Exception {
        final EventingExtensions evtx = new EventingExtensions();
    	evtx.setXmlBinding(binding);
        evtx.setAction(Eventing.SUBSCRIBE_ACTION_URI);

        final ConnectionRetryType retry = new ConnectionRetryType();
        final int retryCount = 3;
        retry.setTotal(new BigInteger(Integer.toString(retryCount)));
        final Duration retryInterval = DatatypeFactory.newInstance().newDuration(5000);
        retry.setValue(retryInterval);

        final Duration heartbeatsInterval = DatatypeFactory.newInstance().newDuration(15000);

        final Boolean sendBookmarks = Boolean.TRUE;

        final AttributableAny bookmark = new AttributableAny();
        final Document bookmarkDoc = evtx.newDocument();
        final Element bookmarkElement = bookmarkDoc.createElement("bookmark-element");
        bookmarkElement.setTextContent("1234");
        bookmark.getAny().add(bookmarkElement);

        final MaxEnvelopeSizeType maxEnvelopeSize = new MaxEnvelopeSizeType();
        final int maxEnvSize = 1024;
        maxEnvelopeSize.setValue(new BigInteger(Integer.toString(maxEnvSize)));
        final PolicyType policy = PolicyType.fromValue(EventingExtensions.SKIP_POLICY);
        maxEnvelopeSize.setPolicy(policy);

        final Long maxElements = new Long(7);

        final Duration maxTime = DatatypeFactory.newInstance().newDuration(3000);

        evtx.setSubscribe(null, null, null, null, null,
                retry, heartbeatsInterval, sendBookmarks, bookmark,
                maxEnvelopeSize, maxElements, maxTime);

        evtx.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evtx.writeTo(bos);
        final EventingExtensions evtx2 = new EventingExtensions(new ByteArrayInputStream(bos.toByteArray()));

        final Subscribe sub2 = evtx2.getSubscribe();
        final DeliveryType delivery2 = sub2.getDelivery();

        final ConnectionRetryType retry2 = ((JAXBElement<ConnectionRetryType>) delivery2.getContent().get(0)).getValue();
        assertEquals(retryCount, retry2.getTotal().intValue());
        assertEquals(retryInterval, retry2.getValue());

        assertEquals(heartbeatsInterval, ((JAXBElement<AttributableDuration>) delivery2.getContent().get(1)).getValue().getValue());
        assertEquals(EventingExtensions.SEND_BOOKMARKS, ((JAXBElement<QName>) delivery2.getContent().get(2)).getName());

        final AttributableAny bookmark2 = ((JAXBElement<AttributableAny>) delivery2.getContent().get(3)).getValue();
        assertEquals(bookmark.getAny().size(), bookmark2.getAny().size());
        assertEquals(bookmarkElement.getNodeName(), ((Element) bookmark2.getAny().get(0)).getNodeName());
        assertEquals(bookmarkElement.getTextContent(), ((Element) bookmark2.getAny().get(0)).getTextContent());

        final MaxEnvelopeSizeType maxEnvelopeSize2 = ((JAXBElement<MaxEnvelopeSizeType>) delivery2.getContent().get(4)).getValue();
        assertEquals(maxEnvelopeSize.getValue(), maxEnvelopeSize2.getValue());
        assertEquals(maxEnvelopeSize.getPolicy(), maxEnvelopeSize2.getPolicy());

        assertEquals(maxElements.longValue(), ((JAXBElement<AttributablePositiveInteger>) delivery2.getContent().get(5)).getValue().getValue().longValue());
        assertEquals(maxTime, ((JAXBElement<AttributableDuration>) delivery2.getContent().get(6)).getValue().getValue());
    }

    public void testPullMode() throws Exception {
        pullModeTest(null, null);
        HashMap<String, String> map = new HashMap<String, String>(1);
        map.put("log", "https://wiseman.dev.java.net/test/events/pull");
        NamespaceMap filterNsMap = new NamespaceMap(map);
        pullModeTest("/log:event3", filterNsMap);
    }

    private void pullModeTest(final String filter, final NamespaceMap filterNsMap) throws Exception {

    	final Duration expires = DatatypeFactory.newInstance().newDuration(20000);
    	final SubscribeResponse subr = subscribe(DESTINATION, "wsman:test/pull_source",
        		EventingExtensions.PULL_DELIVERY_MODE, null, expires, filter, filterNsMap);
        String context = getPullEnumContext(subr);
        assertNotNull(context);

        boolean done = false;
        int count = 0;
        do {
            EnumerationMessageValues enumSettings = new EnumerationMessageValues();
            enumSettings.setTo(DESTINATION);
            enumSettings.setEnumerationMessageActionType(Enumeration.PULL_ACTION_URI);
            enumSettings.setTimeout(20000);
            enumSettings.setEnumerationContext(context);
            enumSettings.setMaxElements(2);
            enumSettings.setResourceUri("wsman:test/pull_source");
        	enumSettings.setXmlBinding(binding);

        	Enumeration enu = EnumerationUtility.buildMessage(null, enumSettings);

        	enu.prettyPrint(logfile);
            Addressing response2 = HttpClient.sendRequest(enu);
            response2.prettyPrint(logfile);
            if (response2.getBody().hasFault()) {
                fail(response2.getBody().getFault().getFaultString());
            }

            final Enumeration pullResponse = new Enumeration(response2);
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
            // We should never get EOS from our event source
            assertTrue(pr.getEndOfSequence() == null);
            // if (pr.getEndOfSequence() != null) {
            //    done = true;
            // }
            // This should go on forever
            count++;
            if (count >= 5)
            	done = true;
        } while (!done);
        assertTrue(count >= 5);
    }
    
    public void testPushMode() throws Exception {

		// First setup a Pull subscription at "wsman:test/events/sink"
		// This will allow us to pull our push events back from the sink
		// and then check them.
    	final Duration expires = DatatypeFactory.newInstance().newDuration(30000);
		final SubscribeResponse pullSubs = subscribe(DESTINATION2,
				"wsman:test/events/sink",
				EventingExtensions.PULL_DELIVERY_MODE, null, expires, null, null);
		final EndpointReferenceType pullMgr = pullSubs.getSubscriptionManager();
		assertNotNull(pullMgr);
		String pullContext = getPullEnumContext(pullSubs);
		assertNotNull(pullContext);

		// Now create a subscription at the source with NotifyTo=sink.
		final EndpointReferenceType sinkEpr = EnumerationSupport
				.createEndpointReference(DESTINATION2,
						"wsman:test/events/sink", null);
		final SubscribeResponse pushSubs = subscribe(DESTINATION2,
				"wsman:test/events/source",
				EventingExtensions.PUSH_DELIVERY_MODE, sinkEpr, expires, null, null);
		final EndpointReferenceType pushMgr = pushSubs.getSubscriptionManager();
		assertNotNull(pushMgr);

		// Now get the source to send some events to the subscribers.
		for (int i = 0; i < 5; i++) {
			final ManagementMessageValues settings = new ManagementMessageValues();
			settings.setTo(DESTINATION2);
			settings.setTimeout(10000);
			settings.setResourceUri("wsman:test/events/source");
			settings.setXmlBinding(binding);
			final Management msg = ManagementUtility.buildMessage(null,
					settings);
			msg.setAction(source_Handler.CREATE_EVENT);

			msg.prettyPrint(logfile);
			final Addressing response = HttpClient.sendRequest(msg);
			response.prettyPrint(logfile);
			if (response.getBody().hasFault()) {
				fail(response.getBody().getFault().getFaultString());
			}
		}

		// Now check if the events arrived at our sink OK

		EnumerationMessageValues enumSettings = new EnumerationMessageValues();
		enumSettings.setTo(DESTINATION2);
		enumSettings
				.setEnumerationMessageActionType(Enumeration.PULL_ACTION_URI);
		enumSettings.setTimeout(10000);
		enumSettings.setEnumerationContext(pullContext);
		enumSettings.setMaxElements(100);
		enumSettings.setResourceUri("wsman:test/events/sink");
		enumSettings.setXmlBinding(binding);

		Enumeration enu = EnumerationUtility.buildMessage(null, enumSettings);

		enu.prettyPrint(logfile);
		Addressing response2 = HttpClient.sendRequest(enu);
		response2.prettyPrint(logfile);
		if (response2.getBody().hasFault()) {
			fail(response2.getBody().getFault().getFaultString());
		}

		final Enumeration pullResponse = new Enumeration(response2);
		final PullResponse pr = pullResponse.getPullResponse();
		// update context for the next pull (if any)
		if (pr.getEnumerationContext() != null) {
			pullContext = 
				(String) pr.getEnumerationContext().getContent().get(0);
		}
		int count = 0;
		for (Object obj : pr.getItems().getAny()) {
			final Element el = (Element) obj;
			count++;
			// System.out.println("Event: " + el.getTextContent());
		}
		// We should never get EOS from our event source
		assertTrue(pr.getEndOfSequence() == null);
		// This should go on forever
		assertEquals(5, count);
	}

	private SubscribeResponse subscribe(final String destination,
										final String resourceURI,
			                            final String mode,
			                            final EndpointReferenceType notifyTo,
			                            final Duration expires,
								        final String filter,
								        final NamespaceMap filterNsMap) throws SOAPException,
			JAXBException, DatatypeConfigurationException,
			ParserConfigurationException, SAXException, IOException {
		EventingMessageValues settings = new EventingMessageValues();
    	settings.setDeliveryMode(mode);
    	settings.setTo(destination);
    	settings.setNotifyTo(notifyTo);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
    	settings.setFilter(filter);
    	settings.setFilterDialect(XPath.NS_URI);
    	settings.setResourceUri(resourceURI);
    	if (expires != null)
    		settings.setExpires(expires.toString());
    	settings.setXmlBinding(binding);
    	
        if ((filter != null) && (filterNsMap != null))
        	settings.setNamespaceMap(filterNsMap.getMap());

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        evt.prettyPrint(logfile);
        Addressing response = HttpClient.sendRequest(evt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }

        final EventingExtensions evtx2 = new EventingExtensions(response);
        final SubscribeResponse subr = evtx2.getSubscribeResponse();
        final EndpointReferenceType mgr = subr.getSubscriptionManager();
        assertNotNull(mgr);
		return subr;
	}

	private String getPullEnumContext(final SubscribeResponse subr) {
		String context = null;
        for (final Object obj : subr.getAny()) {
            if (obj instanceof Element) {
                final Element element = (Element) obj;
                if (Enumeration.ENUMERATION_CONTEXT.getLocalPart().equals(element.getLocalName()) &&
                        Enumeration.ENUMERATION_CONTEXT.getNamespaceURI().equals(element.getNamespaceURI())) {
                    context = element.getTextContent();
                    break;
                }
            }
        }
		return context;
	}
}
