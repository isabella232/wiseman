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
 * $Id: EventingTest.java,v 1.4 2006-02-01 21:50:39 akhilarora Exp $
 */

package management;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.transport.HttpClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javax.xml.datatype.DatatypeFactory;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.FilterType;
import org.xmlsoap.schemas.ws._2004._08.eventing.GetStatusResponse;
import org.xmlsoap.schemas.ws._2004._08.eventing.ObjectFactory;
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
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.SUBSCRIBE_ACTION_URI);
        final String mgrAddress = "http://host/mgr";
        final EndpointReferenceType mgr = evt.createEndpointReference(mgrAddress, null, null, null, null);
        final DeliveryType delivery = Eventing.FACTORY.createDeliveryType();
        delivery.setMode(Eventing.PUSH_DELIVERY_MODE);
        final String recvrAddress = "http://host/notifyTo";
        final EndpointReferenceType notifyToEPR = evt.createEndpointReference(recvrAddress, null, null, null, null);
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        final FilterType filter = Eventing.FACTORY.createFilterType();
        filter.setDialect("http://mydomain/my.filter.dialect");
        filter.getContent().add("my/filter/expression");
        evt.setSubscribe(mgr, Eventing.PUSH_DELIVERY_MODE, notifyToEPR, expires, filter);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));
        
        final Subscribe sub2 = evt2.getSubscribe();
        assertEquals(mgrAddress, sub2.getEndTo().getAddress().getValue());
        assertEquals(Eventing.PUSH_DELIVERY_MODE, sub2.getDelivery().getMode());
        assertEquals(expires, sub2.getExpires());
        assertEquals(filter.getContent().get(0), sub2.getFilter().getContent().get(0));
        assertEquals(filter.getDialect(), sub2.getFilter().getDialect());
    }

    public void testSubscribeResponseVisual() throws Exception {
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.SUBSCRIBE_RESPONSE_URI);
        final String mgrAddress = "http://host/mgr";
        final EndpointReferenceType mgr = evt.createEndpointReference(mgrAddress, null, null, null, null);
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        evt.setSubscribeResponse(mgr, expires);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));
        
        final SubscribeResponse sr2 = evt2.getSubscribeResponse();
        assertEquals(mgrAddress, sr2.getSubscriptionManager().getAddress().getValue());
        assertEquals(expires, sr2.getExpires());
    }

    public void testRenewVisual() throws Exception {
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.RENEW_ACTION_URI);
        final String expires = DatatypeFactory.newInstance().newDuration(600000).toString();
        evt.setRenew(expires);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));
        
        final Renew r2 = evt2.getRenew();
        assertEquals(expires, r2.getExpires());
    }
    
    public void testRenewResponseVisual() throws Exception {
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.RENEW_RESPONSE_URI);
        final String expires = DatatypeFactory.newInstance().newDuration(600000).toString();
        evt.setRenewResponse(expires);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));
        
        final RenewResponse r2 = evt2.getRenewResponse();
        assertEquals(expires, r2.getExpires());
    }
    
    public void testGetStatusVisual() throws Exception {
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.GET_STATUS_ACTION_URI);
        evt.setGetStatus();

        evt.prettyPrint(logfile);
    }
    
    public void testGetStatusResponseVisual() throws Exception {
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.GET_STATUS_RESPONSE_URI);
        final String expires = DatatypeFactory.newInstance().newDuration(600000).toString();
        evt.setGetStatusResponse(expires);

        evt.prettyPrint(logfile);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        evt.writeTo(bos);
        final Eventing evt2 = new Eventing(new ByteArrayInputStream(bos.toByteArray()));
        
        final GetStatusResponse r2 = evt2.getGetStatusResponse();
        assertEquals(expires, r2.getExpires());
    }
    
    public void testUnsubscribeVisual() throws Exception {
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.UNSUBSCRIBE_ACTION_URI);
        evt.setUnsubscribe();

        evt.prettyPrint(logfile);
    }

    public void testSubscriptionEndVisual() throws Exception {
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.SUBSCRIPTION_END_ACTION_URI);
        final String mgrAddress = "http://host/mgr";
        final EndpointReferenceType mgr = evt.createEndpointReference(mgrAddress, null, null, null, null);
        final String reason = "getting tired";
        evt.setSubscriptionEnd(mgr, Eventing.SOURCE_SHUTTING_DOWN_STATUS, reason);

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
        final Eventing evt = new Eventing();
        evt.setAction(Eventing.SUBSCRIBE_ACTION_URI);
        evt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        evt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        final DeliveryType delivery = Eventing.FACTORY.createDeliveryType();
        delivery.setMode(Eventing.PUSH_DELIVERY_MODE);
        final String recvrAddress = "http://localhost:8080/events";
        final EndpointReferenceType notifyToEPR = evt.createEndpointReference(recvrAddress, null, null, null, null);
        final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
        evt.setSubscribe(null, Eventing.PUSH_DELIVERY_MODE, notifyToEPR, expires, null);

        final Management mgmt = new Management(evt);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/eventing");
        
        final Addressing addr = HttpClient.sendRequest(mgmt);
        if (addr.getBody().hasFault()) {
            addr.prettyPrint(System.err);
            fail(addr.getBody().getFault().getFaultString());
        }
        
        final Eventing response = new Eventing(addr);
        final SubscribeResponse subr = response.getSubscribeResponse();
        final EndpointReferenceType mgr = subr.getSubscriptionManager();
    }
}
