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
 **
 * $Id: EventingTest.java,v 1.18 2007-05-30 20:30:23 nbeers Exp $
 */

package management;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.EventSourceUnableToProcessFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingUtility;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.eventing.GetStatusResponse;
import org.xmlsoap.schemas.ws._2004._08.eventing.Renew;
import org.xmlsoap.schemas.ws._2004._08.eventing.RenewResponse;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscriptionEnd;

/**
 * Unit test for WS-Eventing
 */
public class EventingTest extends TestBase {

    public EventingTest(final String testName) {
        super(testName);
    }

    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(EventingTest.class);
        return suite;
    }

    public void testSubscribeVisual() throws Exception {
        final String recvrAddress = "http://host/notifyTo";
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final EndpointReferenceType notifyToEPR = Addressing.createEndpointReference(recvrAddress, null, null, null, null);
        final String mgrAddress = "http://host/mgr";
        final EndpointReferenceType mgr = Addressing.createEndpointReference(mgrAddress, null, null, null, null);

        EventingMessageValues settings = new EventingMessageValues();
    	settings.setDeliveryMode(Eventing.PUSH_DELIVERY_MODE);
    	settings.setEndTo(mgr);
    	settings.setExpires(expires);
    	settings.setNotifyTo(notifyToEPR);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
    	settings.setFilter("my/filter/expression");
    	settings.setFilterDialect("http://mydomain/my.filter.dialect");

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));

        final Subscribe sub2 = evt2.getSubscribe();
        assertEquals(mgrAddress, sub2.getEndTo().getAddress().getValue());
        assertEquals(Eventing.PUSH_DELIVERY_MODE, sub2.getDelivery().getMode());
        assertEquals(expires, sub2.getExpires());
        assertEquals(settings.getFilter(), sub2.getFilter().getContent().get(0));
        assertEquals(settings.getFilterDialect(), sub2.getFilter().getDialect());
    }

    public void testSubscribeResponseVisual() throws Exception {

        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final String mgrAddress = "http://host/mgr";
        final EndpointReferenceType mgr = Addressing.createEndpointReference(mgrAddress,
        		null, null, null, null);

        EventingMessageValues settings = new EventingMessageValues();
//        settings.setEndTo(mgr);
    	settings.setNotifyTo(mgr);
    	settings.setExpires(expires);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIBE_RESPONSE_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        evt.prettyPrint(System.out);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));

        evt2.prettyPrint(System.out);
        final SubscribeResponse sr2 = evt2.getSubscribeResponse();
        assertNotNull("SubscriptionManager is null.",sr2.getSubscriptionManager());
        assertNotNull("Address is null.",sr2.getSubscriptionManager().getAddress());
        assertEquals(mgrAddress, sr2.getSubscriptionManager().getAddress().getValue());
        assertEquals(expires, sr2.getExpires());
    }

    public void testRenewVisual() throws Exception {
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();

        EventingMessageValues settings = new EventingMessageValues();
    	settings.setExpires(expires);
    	settings.setEventingMessageActionType(Eventing.RENEW_ACTION_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));

        final Renew r2 = evt2.getRenew();
        assertEquals(expires, r2.getExpires());
    }

    public void testRenewResponseVisual() throws Exception {
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();

        EventingMessageValues settings = new EventingMessageValues();
    	settings.setExpires(expires);
    	settings.setEventingMessageActionType(Eventing.RENEW_RESPONSE_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));

        final RenewResponse r2 = evt2.getRenewResponse();
        assertEquals(expires, r2.getExpires());
    }

    public void testGetStatusVisual() throws Exception {
        EventingMessageValues settings = new EventingMessageValues();
    	settings.setEventingMessageActionType(Eventing.GET_STATUS_ACTION_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        evt.prettyPrint(logfile);
    }

    public void testGetStatusResponseVisual() throws Exception {
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();

        EventingMessageValues settings = new EventingMessageValues();
    	settings.setExpires(expires);
    	settings.setEventingMessageActionType(Eventing.GET_STATUS_RESPONSE_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));

        final GetStatusResponse r2 = evt2.getGetStatusResponse();
        assertEquals(expires, r2.getExpires());
    }

    public void testUnsubscribeVisual() throws Exception {
        EventingMessageValues settings = new EventingMessageValues();
    	settings.setEventingMessageActionType(Eventing.UNSUBSCRIBE_ACTION_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);
        evt.prettyPrint(logfile);
    }

    public void testSubscriptionEndVisual() throws Exception {
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final String mgrAddress = "http://host/mgr";
        final EndpointReferenceType mgr = Addressing.createEndpointReference(mgrAddress, null, null, null, null);
        String reason = "getting tired";

        EventingMessageValues settings = new EventingMessageValues();
    	settings.setEndTo(mgr);
    	settings.setExpires(expires);
    	settings.setStatus(Eventing.SOURCE_SHUTTING_DOWN_STATUS);
    	settings.setReason(reason);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIPTION_END_ACTION_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));

        final SubscriptionEnd sub2 = evt2.getSubscriptionEnd();
        assertEquals(mgrAddress, sub2.getSubscriptionManager().getAddress().getValue());
        assertEquals(Eventing.SOURCE_SHUTTING_DOWN_STATUS, sub2.getStatus());
        assertEquals(reason, sub2.getReason().get(0).getValue());
    }

    public void testEventing() throws Exception {

        final String recvrAddress = "http://localhost:8080/events";
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final EndpointReferenceType notifyToEPR = Addressing.createEndpointReference(recvrAddress, null, null, null, null);
     	EventingMessageValues settings = new EventingMessageValues();
    	settings.setDeliveryMode(Eventing.PUSH_DELIVERY_MODE);
    	settings.setEndTo(null);
    	settings.setExpires(expires);
    	settings.setNotifyTo(notifyToEPR);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri("wsman:test/eventing");
    	settings.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        final Addressing addr = HttpClient.sendRequest(evt);
        addr.prettyPrint(logfile);
        if (addr.getBody().hasFault()) {
            fail(addr.getBody().getFault().getFaultString());
        }

        final Eventing response = new Eventing(addr);
        final SubscribeResponse subr = response.getSubscribeResponse();
        final EndpointReferenceType mgr = subr.getSubscriptionManager();
        assertEquals(DESTINATION, mgr.getAddress().getValue());
        final Object identifier = mgr.getReferenceParameters().getAny().get(1);
        assertNotNull(identifier);
        final String expires2 = subr.getExpires();
        assertNotNull(expires2);

    }

    public void testUnsubscribe() throws Exception {

        final String recvrAddress = "http://localhost:8080/events";
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final EndpointReferenceType notifyToEPR = Addressing.createEndpointReference(recvrAddress, null, null, null, null);
     	EventingMessageValues settings = new EventingMessageValues();
    	settings.setDeliveryMode(Eventing.PUSH_DELIVERY_MODE);
    	settings.setEndTo(null);
    	settings.setExpires(expires);
    	settings.setNotifyTo(notifyToEPR);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri("wsman:test/eventing");
    	settings.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);

        final Addressing addr = HttpClient.sendRequest(evt);
        addr.prettyPrint(logfile);
        if (addr.getBody().hasFault()) {
            fail(addr.getBody().getFault().getFaultString());
        }

        final Eventing response = new Eventing(addr);
        final SubscribeResponse subr = response.getSubscribeResponse();
        final EndpointReferenceType mgr = subr.getSubscriptionManager();
        assertEquals(DESTINATION, mgr.getAddress().getValue());
        final Object identifierElement = mgr.getReferenceParameters().getAny().get(1);
        assertNotNull(identifierElement);
        final String identifier = ((JAXBElement<String>) identifierElement).getValue();

        // now send an unsubscribe request using the identifier
        evt.setAction(Eventing.UNSUBSCRIBE_ACTION_URI);
        evt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        evt.setUnsubscribe();
        evt.setIdentifier(identifier);

        evt.prettyPrint(logfile);
        final Addressing addr2 = HttpClient.sendRequest(evt);
        addr2.prettyPrint(logfile);
        if (addr2.getBody().hasFault()) {
            fail(addr2.getBody().getFault().getFaultString());
        }

        final Eventing response2 = new Eventing(addr2);
        final String identifier2 = response2.getIdentifier();
        assertNotNull(identifier2);
        assertEquals(identifier, identifier2);
    }

    public void testBogusFilter() throws Exception {
        final String recvrAddress = "http://localhost:8080/events";
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final EndpointReferenceType notifyToEPR = Addressing.createEndpointReference(recvrAddress, null, null, null, null);

     	EventingMessageValues settings = new EventingMessageValues();
    	settings.setDeliveryMode(Eventing.PUSH_DELIVERY_MODE);
    	settings.setEndTo(null);
    	settings.setExpires(expires);
    	settings.setNotifyTo(notifyToEPR);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
    	settings.setFilter("xyz");
    	settings.setFilterDialect("a/bogus/filter/dialect");
    	settings.setTo(DESTINATION);
    	settings.setResourceUri("wsman:test/eventing");
    	settings.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);
    	evt.prettyPrint(System.out);

        final Addressing addr = HttpClient.sendRequest(evt);
        addr.prettyPrint(logfile);
        if (!addr.getBody().hasFault()) {
            fail("bogus filter accepted");
        }

        final Fault fault = new Addressing(addr).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(FilteringRequestedUnavailableFault.FILTERING_REQUESTED_UNAVAILABLE, fault.getCode().getSubcode().getValue());
        assertEquals(FilteringRequestedUnavailableFault.FILTERING_REQUESTED_UNAVAILABLE_REASON, fault.getReason().getText().get(0).getValue());
        // Need to cope with multiple filter dialects
        List dialects = fault.getDetail().getAny();
        boolean foundXpathDialect = false;
        for(Object dialect : dialects) {
            if(dialect instanceof JAXBElement) {
                JAXBElement elem = (JAXBElement) dialect;
                Object value = elem.getValue();
                if(value instanceof String) {
                    foundXpathDialect = XPath.NS_URI.equals(value);
                    if(foundXpathDialect) break;
                }

            }
        }
        assertTrue("XPath dialect not found", foundXpathDialect);
    }

    public void testInvalidFilterExpression() throws Exception {

        final String recvrAddress = "http://localhost:8080/events";
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final EndpointReferenceType notifyToEPR = Addressing.createEndpointReference(recvrAddress, null, null, null, null);

     	EventingMessageValues settings = new EventingMessageValues();
    	settings.setDeliveryMode(Eventing.PUSH_DELIVERY_MODE);
    	settings.setEndTo(null);
    	settings.setExpires(expires);
    	settings.setNotifyTo(notifyToEPR);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
    	settings.setFilter("a bad xpath expression");
    	settings.setFilterDialect(XPath.NS_URI);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri("wsman:test/eventing");
    	settings.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);

    	Eventing evt = EventingUtility.buildMessage(null, settings);
    	evt.prettyPrint(System.out);

        final Addressing addr = HttpClient.sendRequest(evt);
        addr.prettyPrint(logfile);
        if (!addr.getBody().hasFault()) {
            fail("invalid filter expression accepted");
        }

        final Fault fault = new Addressing(addr).getFault();
        assertEquals(SOAP.SENDER, fault.getCode().getValue());
        assertEquals(EventSourceUnableToProcessFault.EVENT_SOURCE_UNABLE_TO_PROCESS, fault.getCode().getSubcode().getValue());
        assertEquals(EventSourceUnableToProcessFault.EVENT_SOURCE_UNABLE_TO_PROCESS_REASON, fault.getReason().getText().get(0).getValue());
        final String detail = ((Element) fault.getDetail().getAny().get(0)).getTextContent();
        assertNotNull(detail);
    }

    public void testEventFiltering() throws Exception {


        final String recvrAddress = "http://localhost:8080/events";
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final EndpointReferenceType notifyToEPR = Addressing.createEndpointReference(recvrAddress, null, null, null, null);

     	EventingMessageValues settings = new EventingMessageValues();
    	settings.setDeliveryMode(Eventing.PUSH_DELIVERY_MODE);
    	settings.setEndTo(null);
    	settings.setExpires(expires);
    	settings.setNotifyTo(notifyToEPR);
    	settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
    	settings.setFilter("//ev:critical");
    	settings.setFilterDialect(XPath.NS_URI);
        HashMap<String, String> map = new HashMap<String, String>(1);
        map.put("ev", "https://wiseman.dev.java.net/test/events/subscribe");
        settings.setNamespaceMap(map);
        settings.setTo(DESTINATION);
        settings.setResourceUri("wsman:test/eventing");
        settings.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
    	Eventing evt = EventingUtility.buildMessage(null, settings);
    	evt.prettyPrint(System.out);

        final Addressing addr = HttpClient.sendRequest(evt);
        addr.prettyPrint(logfile);
        if (addr.getBody().hasFault()) {
            fail(addr.getBody().getFault().getFaultString());
        }

        final Eventing response = new Eventing(addr);
        final SubscribeResponse subr = response.getSubscribeResponse();
        final EndpointReferenceType mgr = subr.getSubscriptionManager();
        assertEquals(DESTINATION, mgr.getAddress().getValue());
        final Object identifier = mgr.getReferenceParameters().getAny().get(1);
        assertNotNull(identifier);
        final String expires2 = subr.getExpires();
        assertNotNull(expires2);
    }
}
