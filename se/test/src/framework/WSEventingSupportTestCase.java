/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt package framework;
 */
package framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.eventing.RenewResponse;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;

import util.WsManTestBaseSupport;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.Management;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.enumeration.InvalidExpirationTimeFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingUtility;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.HandlerContextImpl;
import com.sun.ws.management.server.WSEnumerationSupport;
import com.sun.ws.management.server.WSEventingIteratorFactory;
import com.sun.ws.management.server.WSEventingSupport;
import com.sun.ws.management.server.message.SAAJMessage;
import com.sun.ws.management.server.message.WSEventingRequest;
import com.sun.ws.management.soap.FaultException;

public class WSEventingSupportTestCase extends WsManTestBaseSupport {
	
	private static final String RESOURCE_URI = "http://examples.hp.com/ws/wsman/user";
	private static final String ENCODING = "UTF-8";
	private static final String CONTENTTYPE = "text/xml";
	private static final ObjectFactory Factory = new ObjectFactory();
	
	private final DatatypeFactory datatypeFactory;
	
	static {
		
	}
	
	public class TestEnumerationIterator implements EnumerationIterator {
		
		int age = 1;
		
		public TestEnumerationIterator() {
		}
		
		public int estimateTotalItems() {
			return Integer.MAX_VALUE;
		}

		public boolean hasNext() {
			return true;
		}

		public boolean isFiltered() {
			return false;
		}

		public EnumerationItem next() {
			final UserType user = new UserType();
			
			user.setAddress("123 Main Street");
			user.setAge(age);
			user.setCity("Los Angeles");
			user.setFirstname("Bill");
			user.setLastname("Smith");
			user.setState("California");
			user.setZip("90044");
			age++;
			
			final JAXBElement<UserType> jaxbUser = Factory.createUser(user);
			final EndpointReferenceType epr =
				WSEnumerationSupport.createEndpointReference(DESTINATION, RESOURCE_URI, null);
			try {
				// Don't hog the CPU
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			return new EnumerationItem(jaxbUser, epr);
		}

		public void release() {
			
		}
	}
	
	public class TestEventingIterator implements EnumerationIterator {

		private final int count = 10;
		private final Iterator<EnumerationItem> iterator;
		
		public TestEventingIterator() {
			final List<EnumerationItem> list = new ArrayList<EnumerationItem>();
			for (int i=0; i < count; i++) {
				final EnumerationItem item = new EnumerationItem(null, null);
				list.add(item);
			}
			iterator = list.iterator();
		}
		
		public int estimateTotalItems() {
			return count;
		}

		public boolean hasNext() {
			return true;
		}

		public boolean isFiltered() {
			return false;
		}

		public EnumerationItem next() {
			return null;
		}

		public void release() {
			
		}
	}
	
	public class TestEventingIteratorFactory implements WSEventingIteratorFactory {

		public EnumerationIterator newIterator(HandlerContext context,
				WSEventingRequest request) throws UnsupportedFeatureFault,
				FaultException {
			return new TestEventingIterator();
		}
		
	}
	
	public class PullTest implements Callable<Boolean> {

		EnumerationContextType context;
		
		PullTest(EnumerationContextType enumCtx) {
			context = enumCtx;
		}
		
		public Boolean call() throws Exception {
			EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
			settings.setEnumerationContext(context.getContent().get(0));
			settings.setEnumerationMessageActionType(Enumeration.PULL_ACTION_URI);
        	settings.setMaxElements(Integer.MAX_VALUE);
        	settings.setTimeout(30000);
        	final Management pull = new Management(EnumerationUtility.buildMessage(null, settings));
        	final SAAJMessage request = new SAAJMessage(pull);
        	final SAAJMessage response = new SAAJMessage(new Management());
    		final HandlerContext context = new HandlerContextImpl(null, CONTENTTYPE,
                    ENCODING, DESTINATION, null);
    		try {
    		WSEnumerationSupport.pull(context, request, response);
    		} catch (InvalidEnumerationContextFault e) {
				throw e;
			} catch (FaultException e) {
				throw e;
    		}
        	
			return new Boolean(true);
		}
	}
	
	
	public WSEventingSupportTestCase(final String testName)
		throws DatatypeConfigurationException {
		super(testName);
		datatypeFactory = DatatypeFactory.newInstance();
	}
	
    private Object unmarshalExpires(String expires)
			throws InvalidExpirationTimeFault {
		try {
			// first try if it's a Duration
			final Duration duration = datatypeFactory.newDuration(expires);
			return duration;
		} catch (IllegalArgumentException e) {
			try {
				// now see if it is a calendar time
				final XMLGregorianCalendar calendar = datatypeFactory
						.newXMLGregorianCalendar(expires);
				return calendar;
			} catch (IllegalArgumentException ncex) {
				throw new InvalidExpirationTimeFault();
			}
		}
	}
	
	private EndpointReferenceType subscribe(final String deliveryMode,
			                                final String expires) {
		
		try {
			final EndpointReferenceType notifyTo = new EndpointReferenceType();
			final AttributedURI uri = new AttributedURI();
			uri.setValue("http://localhost");
			notifyTo.setAddress(uri);
			final EventingMessageValues eventSettings = EventingMessageValues
					.newInstance();
			eventSettings.setFilter(null);
			eventSettings.setTo(DESTINATION);
			eventSettings.setResourceUri(RESOURCE_URI);
			eventSettings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
			eventSettings.setNotifyTo(notifyTo);
			
			if (deliveryMode != null)
			    eventSettings.setDeliveryMode(deliveryMode);
			eventSettings.setExpires(expires);

			final Management evt = new Management(EventingUtility.buildMessage(
					null, eventSettings));
			final SAAJMessage request = new SAAJMessage(evt);
			final SAAJMessage response = new SAAJMessage(new Management());
			final HandlerContext context = new HandlerContextImpl(null,
					CONTENTTYPE, ENCODING, DESTINATION, null);

			WSEventingSupport.subscribe(context, request, response, false, 0,
					null, null, null, null);

		    Object payload = response.getPayload(null);
		    if (payload instanceof SubscribeResponse) {
		    	final SubscribeResponse subscribeResponse = (SubscribeResponse)payload;
		    	final EndpointReferenceType mgr = subscribeResponse.getSubscriptionManager();
		    	if (null == mgr) {
		    		fail("Subscribe did not return a subscription manager epr.");
		    	}
		    	final String expiresOnServer = subscribeResponse.getExpires();
		    	if (null == expiresOnServer) {
		    		fail("Subscribe did not return a subscription expiration.");
		    	}
		    	
		    	if (expires != null) {
		    		// General test for issue 154
		    		final Object expiresObj = unmarshalExpires(expires);
		    		final Object expiresOnServerObj = unmarshalExpires(expiresOnServer);
		    		if (!expiresObj.getClass().equals(expiresOnServerObj.getClass())) {
		    			// We should have gotten back the same object type that we sent
		    			fail("Incorrect expiration type returned.");
		    		}
		    	}
		    	// System.out.println("Subscription expires: " + expiresOnServer);
		    	return mgr;
		    } else {
		    	fail("Subscribe response object payload incorrect.");
		    }
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
		return null;
	}
	
	
	private void unsubscribe(EndpointReferenceType mgr) {
		try {
			final EventingMessageValues eventSettings = EventingMessageValues
					.newInstance();
			eventSettings.setFilter(null);
			eventSettings.setTo(mgr.getAddress().getValue());
			final ReferenceParametersType parameters = mgr.getReferenceParameters();
			if (parameters == null) {
				fail("Subscription Manager EPR missing parameters.");
			}
			eventSettings.setResourceUri(RESOURCE_URI); //TODO: Get this from the EPR
			eventSettings.setEventingMessageActionType(Eventing.UNSUBSCRIBE_ACTION_URI);
			
			// TODO: This should NOT be done like this. The identifier could be anywhere in the EPR.
	        final Object identifierElement = mgr.getReferenceParameters().getAny().get(1);
	        assertNotNull(identifierElement);
	        final String identifier = ((JAXBElement<String>) identifierElement).getValue();
			eventSettings.setIdentifier(identifier); 

			final Management evt = new Management(EventingUtility.buildMessage(
					null, eventSettings));
			final SAAJMessage request = new SAAJMessage(evt);
			final SAAJMessage response = new SAAJMessage(new Management());
			final HandlerContext context = new HandlerContextImpl(null,
					CONTENTTYPE, ENCODING, DESTINATION, null);

			WSEventingSupport.unsubscribe(context, request, response);

			try {
				response.getPayload(null);
				fail("Unexpected payload returned.");
			} catch (Exception e) {
				// Success as there should not be a payload.
			}
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
		
	}
	
	private void renew(final EndpointReferenceType mgr,
			           final String expires) {
		try {
			final EventingMessageValues eventSettings = EventingMessageValues
					.newInstance();
			eventSettings.setFilter(null);
			eventSettings.setTo(mgr.getAddress().getValue());
			final ReferenceParametersType parameters = mgr.getReferenceParameters();
			if (parameters == null) {
				fail("Subscription Manager EPR missing parameters.");
			}
			eventSettings.setResourceUri(RESOURCE_URI); //TODO: Get this from the EPR
			eventSettings.setEventingMessageActionType(Eventing.RENEW_ACTION_URI);
			eventSettings.setExpires(expires);
			
			// TODO: This should NOT be done like this. The identifier could be anywhere in the EPR.
	        final Object identifierElement = mgr.getReferenceParameters().getAny().get(1);
	        assertNotNull(identifierElement);
	        final String identifier = ((JAXBElement<String>) identifierElement).getValue();
			eventSettings.setIdentifier(identifier); 

			final Management evt = new Management(EventingUtility.buildMessage(
					null, eventSettings));
			final SAAJMessage request = new SAAJMessage(evt);
			final SAAJMessage response = new SAAJMessage(new Management());
			final HandlerContext context = new HandlerContextImpl(null,
					CONTENTTYPE, ENCODING, DESTINATION, null);

			WSEventingSupport.renew(context, request, response, null, null);

			Object payload = null;
			try {
				payload = response.getPayload(null);
			} catch (Exception e) {
				fail("Unable to unmarshal payload in renew response.");
			}
		    if (payload instanceof RenewResponse) {
		    	final RenewResponse renewResponse = (RenewResponse)payload;
		    	final String serverExpires = renewResponse.getExpires();
		    	if (null == serverExpires) {
		    		fail("Renew did not return a subscription expiration.");
		    	}
		    	
		    } else {
		    	fail("Renew response object payload incorrect.");
		    }
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	public void testWSSubscribe() {
		final EndpointReferenceType mgr = subscribe(null, null);
		if (mgr == null) {
			fail("Subscription Manager EPR not returned from subscribe.");
		}
	}
	
	public void testWSSubscribe10Years() throws DatatypeConfigurationException {
		final EndpointReferenceType mgr = subscribe(null,
				datatypeFactory.newDuration(true, 10, 0, 0, 0, 0, 0).toString());
		if (mgr == null) {
			fail("Subscription Manager EPR not returned from subscribe.");
		}
	}
	
	public void testWSRenew() {
		
		final EndpointReferenceType mgr = subscribe(null, null);
		if (mgr == null) {
			fail("Subscription Manager EPR not returned from subscribe.");
		}
		
		renew(mgr, null);
	}
	
	public void testWSSubscribePull() {
		
		final EndpointReferenceType mgr = subscribe(EventingExtensions.PULL_DELIVERY_MODE, null);
		if (mgr == null) {
			fail("Subscription Manager EPR not returned from subscribe.");
		}
	}
	
	public void testWSSubscribePullInfiniteExpire() {
		
		try {
			final EndpointReferenceType notifyTo = new EndpointReferenceType();
			final AttributedURI uri = new AttributedURI();
			uri.setValue("http://localhost");
			notifyTo.setAddress(uri);
			final EventingMessageValues eventSettings = EventingMessageValues
					.newInstance();
			eventSettings.setFilter(null);
			eventSettings.setTo(DESTINATION);
			eventSettings.setResourceUri(RESOURCE_URI);
			eventSettings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
			eventSettings.setDeliveryMode(EventingExtensions.PULL_DELIVERY_MODE);
			eventSettings.setNotifyTo(notifyTo);
			eventSettings.setExpires(WSEventingSupport.infiniteExpiration.toString());

			final Management evt = new Management(EventingUtility.buildMessage(
					null, eventSettings));
			final SAAJMessage request = new SAAJMessage(evt);
			final SAAJMessage response = new SAAJMessage(new Management());
			final HandlerContext context = new HandlerContextImpl(null,
					CONTENTTYPE, ENCODING, DESTINATION, null);
			final TestEventingIteratorFactory factory = new TestEventingIteratorFactory();

			WSEventingSupport.subscribe(context, request, response, false, 0,
					null, factory, WSEventingSupport.infiniteExpiration,
					WSEventingSupport.infiniteExpiration);

		    Object payload = response.getPayload(null);
		    if (payload instanceof SubscribeResponse) {
		    	final EndpointReferenceType mgr = ((SubscribeResponse)payload).getSubscriptionManager();
		    	if (null == mgr) {
		    		fail("Subscribe did not return a subscription manager epr.");
		    	}
		    	final String expires = ((SubscribeResponse)payload).getExpires();
		    	if (null == expires) {
		    		fail("Subscribe did not return a subscription expiration:" + expires);
		    	}
		    } else {
		    	fail("Subscribe response object payload incorrect.");
		    }
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	public void testWSSubscribePullNoExpire() {
		
		try {
			final EndpointReferenceType notifyTo = new EndpointReferenceType();
			final AttributedURI uri = new AttributedURI();
			uri.setValue("http://localhost");
			notifyTo.setAddress(uri);
			final EventingMessageValues eventSettings = EventingMessageValues
					.newInstance();
			eventSettings.setFilter(null);
			eventSettings.setTo(DESTINATION);
			eventSettings.setResourceUri(RESOURCE_URI);
			eventSettings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
			eventSettings.setDeliveryMode(EventingExtensions.PULL_DELIVERY_MODE);
			eventSettings.setNotifyTo(notifyTo);
			eventSettings.setExpires(null);

			final Management evt = new Management(EventingUtility.buildMessage(
					null, eventSettings));
			final SAAJMessage request = new SAAJMessage(evt);
			final SAAJMessage response = new SAAJMessage(new Management());
			final HandlerContext context = new HandlerContextImpl(null,
					CONTENTTYPE, ENCODING, DESTINATION, null);
			final TestEventingIteratorFactory factory = new TestEventingIteratorFactory();

			WSEventingSupport.subscribe(context, request, response, false, 0,
					null, factory, WSEventingSupport.infiniteExpiration,
					WSEventingSupport.infiniteExpiration);

		    Object payload = response.getPayload(null);
		    if (payload instanceof SubscribeResponse) {
		    	final EndpointReferenceType mgr = ((SubscribeResponse)payload).getSubscriptionManager();
		    	if (null == mgr) {
		    		fail("Subscribe did not return a subscription manager epr.");
		    	}
		    	final String expires = ((SubscribeResponse)payload).getExpires();
		    	if (null != expires) {
		    		fail("Subscribe returned a subscription expiration:" + expires);
		    	}
		    } else {
		    	fail("Subscribe response object payload incorrect.");
		    }
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	public void testWSUnsubscribe() throws SOAPException, JAXBException, DatatypeConfigurationException {
		final EndpointReferenceType mgr = subscribe(null, null);
		if (mgr == null) {
			fail("Subscription Manager EPR not returned from subscribe.");
		}
		unsubscribe(mgr);
	}
	
	public void testWSUnsubscribePull() throws SOAPException, JAXBException, DatatypeConfigurationException {
		final EndpointReferenceType mgr = subscribe(EventingExtensions.PULL_DELIVERY_MODE, null);
		if (mgr == null) {
			fail("Subscription Manager EPR not returned from subscribe.");
		}
		unsubscribe(mgr);
	}

	public void testIssue155() {
		final EndpointReferenceType mgr = subscribe(EventingExtensions.PULL_DELIVERY_MODE, "PT0M4.000S");
		if (mgr == null) {
			fail("Subscription Manager EPR not returned from subscribe.");
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		
		// Call renew with infinite expires this time
		renew(mgr, WSEventingSupport.infiniteExpiration.toString());
		
		// Got to sleep for 3 more seconds
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		
		// Try to renew again. It should still be active.
		renew(mgr, WSEventingSupport.infiniteExpiration.toString());
		
	}
}
