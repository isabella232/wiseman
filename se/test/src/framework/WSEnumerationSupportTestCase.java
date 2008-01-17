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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;

import util.WsManTestBaseSupport;

import com.sun.ws.management.Management;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingUtility;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.HandlerContextImpl;
import com.sun.ws.management.server.WSEnumerationSupport;
import com.sun.ws.management.server.WSEventingSupport;
import com.sun.ws.management.server.message.SAAJMessage;
import com.sun.ws.management.soap.FaultException;

public class WSEnumerationSupportTestCase extends WsManTestBaseSupport {
	
	private static final String RESOURCE_URI = "http://examples.hp.com/ws/wsman/user";
	private static final String ENCODING = "UTF-8";
	private static final String CONTENTTYPE = "text/xml";
	private static final ObjectFactory Factory = new ObjectFactory();
	
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
	
	
	public WSEnumerationSupportTestCase(final String testName) {
		super(testName);
		
	}
	
	public void testEnumerateCancel() throws SOAPException, JAXBException, DatatypeConfigurationException {
    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
    	settings.setFilter(null);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri(RESOURCE_URI);
    	final Management enu = new Management(EnumerationUtility.buildMessage(null, settings));
    	final SAAJMessage request = new SAAJMessage(enu);
    	final SAAJMessage response = new SAAJMessage(new Management());
    	
		final HandlerContext context = new HandlerContextImpl(null, CONTENTTYPE,
				                                              ENCODING, DESTINATION, null);
		request.cancel();
		try {
		    WSEnumerationSupport.enumerate(context, request, response,
		    		                       new TestEventingIterator(), null);
			fail("Expected TimedOutFault exception.");
		    
		} catch (TimedOutFault e) {
			// Success
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	public void testIssue151() throws SOAPException, JAXBException, DatatypeConfigurationException {
		// NOTE: This test checks if an Enumeration Renew can be done via Eventing Renew.
    	final EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
    	settings.setFilter(null);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri(RESOURCE_URI);
    	final Management enu = new Management(EnumerationUtility.buildMessage(null, settings));
    	final SAAJMessage request = new SAAJMessage(enu);
    	final SAAJMessage response = new SAAJMessage(new Management());
    	
		final HandlerContext context = new HandlerContextImpl(null, CONTENTTYPE,
				                                              ENCODING, DESTINATION, null);
		try {
			EnumerationContextType enumCtx = null;
		    WSEnumerationSupport.enumerate(context, request, response,
		    		                       new TestEventingIterator(), null);
		    Object payload = response.getPayload(null);
		    if (payload instanceof EnumerateResponse) {
		    	enumCtx = ((EnumerateResponse)payload).getEnumerationContext();
		    	if (null == enumCtx) {
		    		fail("Enumerate did not return an enumeration context.");
		    	}
		    } else {
		    	fail("Enumerate response object payload incorrect.");
		    }
		    final EventingMessageValues eventSettings = EventingMessageValues.newInstance();
		    eventSettings.setFilter(null);
		    eventSettings.setTo(DESTINATION);
		    eventSettings.setResourceUri(RESOURCE_URI);
		    eventSettings.setEventingMessageActionType(Eventing.RENEW_ACTION_URI);
		    eventSettings.setIdentifier((String)enumCtx.getContent().get(0));
		    
		    final Management evt = new Management(EventingUtility.buildMessage(null, eventSettings));
		    final SAAJMessage renewReq = new SAAJMessage(evt);
		    final SAAJMessage renewRes = new SAAJMessage(new Management());
			WSEventingSupport.renew(context, renewReq, renewRes);
			fail("Expected InvalidMessageFault exception.");
		    
		} catch(InvalidMessageFault e) {
			final String msg = e.getDetails()[0].getTextContent();
			if ((msg == null) || (msg.startsWith("Subscription with Identifier:") == false)) {
				fail("Unexpected exception: " + msg);
			}
			// Success
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	public void testAsynchronousRelease() throws SOAPException, JAXBException, DatatypeConfigurationException {
    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
    	settings.setFilter(null);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri(RESOURCE_URI);
    	final Management enu = new Management(EnumerationUtility.buildMessage(null, settings));
    	final SAAJMessage request = new SAAJMessage(enu);
    	final SAAJMessage response = new SAAJMessage(new Management());
    	
		final HandlerContext context = new HandlerContextImpl(null, CONTENTTYPE,
				                                              ENCODING, DESTINATION, null);
		try {
			EnumerationContextType enumCtx = null;
			
		    WSEnumerationSupport.enumerate(context, request, response,
		    		                       new TestEnumerationIterator(), null);
		    Object payload = response.getPayload(null);
		    if (payload instanceof EnumerateResponse) {
		    	enumCtx = ((EnumerateResponse)payload).getEnumerationContext();
		    	if (null == enumCtx) {
		    		fail("Enumerate did not return an enumeration context.");
		    	}
		    } else {
		    	fail("Enumerate response object payload incorrect.");
		    }
			final FutureTask<Boolean> task = new FutureTask<Boolean>(new PullTest(enumCtx));
			final Thread thread = new Thread(task);
			thread.start();
			Thread.sleep(2000); // Let it start the pull.
			settings.setEnumerationContext(enumCtx.getContent().get(0));
			settings.setEnumerationMessageActionType(Enumeration.RELEASE_ACTION_URI);
			final Management rel = new Management(EnumerationUtility.buildMessage(null, settings));
			final SAAJMessage rels = new SAAJMessage(rel);
			final SAAJMessage resp = new SAAJMessage(new Management());
			WSEnumerationSupport.release(context, rels, resp);
			try {
			    task.get(1, TimeUnit.SECONDS);
				fail("Expected InvalidEnumerationContextFault.");
			} catch (ExecutionException e) {
				if (false == (e.getCause() instanceof InvalidEnumerationContextFault)) {
				    fail("Unexpected exception: " + e.getCause().getMessage());
				}
			}
		}
		catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	public void testOptimizedEnumerateExpiration() throws SOAPException, JAXBException, DatatypeConfigurationException {
    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
    	settings.setFilter(null);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri(RESOURCE_URI);
    	settings.setExpires(2000);
    	settings.setMaxElements((2000/200) + 2);
    	settings.setRequestForOptimizedEnumeration(true);
    	final Management enu = new Management(EnumerationUtility.buildMessage(null, settings));
    	final SAAJMessage request = new SAAJMessage(enu);
    	final SAAJMessage response = new SAAJMessage(new Management());
    	
		final HandlerContext context = new HandlerContextImpl(null, CONTENTTYPE,
				                                              ENCODING, DESTINATION, null);
		try {
		    WSEnumerationSupport.enumerate(context, request, response,
		    		                       new TestEnumerationIterator(), null);
			fail("Expected TimedOutFault exception.");
		    
		} catch (TimedOutFault e) {
			// Success
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
}
