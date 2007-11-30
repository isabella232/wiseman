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
 * $Id: timeout_Handler.java,v 1.2 2007-11-30 14:32:38 denis_rachal Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.IteratorFactory;
import com.sun.ws.management.soap.FaultException;

public class timeout_Handler implements Handler {
	
	public class TimeoutIteratorFactory implements IteratorFactory {

		public TimeoutIteratorFactory() {
		}

		public EnumerationIterator newIterator(HandlerContext context,
				Enumeration request, DocumentBuilder db, boolean includeItem,
				boolean includeEPR) throws UnsupportedFeatureFault,
				FaultException {
			return new TimeoutIterator();
		}

		public class TimeoutIterator implements EnumerationIterator {
			
			private final ObjectFactory FACTORY = new ObjectFactory();
			private final int count = 10;
			private final Iterator<EnumerationItem> iterator;

			public TimeoutIterator() {
				final List<EnumerationItem> list = new ArrayList<EnumerationItem>();
				for (int i = 1; i <= count; i++) {
					final UserType user = new UserType();
					user.setAddress( i + " Main Street");
					user.setAge(i);
					user.setCity("Bellevue");
					user.setFirstname("Denis");
					user.setLastname("Baldwin");
					user.setState("WA");
					user.setZip("98008");
					final JAXBElement<UserType> jaxbUser = FACTORY.createUser(user);
					final EnumerationItem item = new EnumerationItem(jaxbUser, null);
					list.add(item);
				}
				iterator = list.iterator();
			}

			public int estimateTotalItems() {
				return count;
			}

			public boolean hasNext() {
				return iterator.hasNext();
			}

			public boolean isFiltered() {
				return false;
			}

			public EnumerationItem next() {
				// return null;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				return iterator.next();
			}

			public void release() {

			}
		}
	}
    
	private final TimeoutIteratorFactory factory = new TimeoutIteratorFactory();
	
    public void handle(final String action,
    		           final String resource,
                       final HandlerContext context,
                       final Management request,
                       final Management response) throws Exception {
    	
    	// Test EnumerationSupport's handling of timeouts.
        if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            response.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            Enumeration enuRequest = new Enumeration(request);
            Enumeration enuResponse = new Enumeration(response);
            EnumerationSupport.enumerate(context, enuRequest, enuResponse, null, factory);
            return;
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            response.setAction(Enumeration.PULL_RESPONSE_URI);
            final Enumeration enuRequest = new Enumeration(request);
            final Enumeration enuResponse = new Enumeration(response);
            EnumerationSupport.pull(context, enuRequest, enuResponse);
            return;
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            response.setAction(Enumeration.RELEASE_RESPONSE_URI);
            final Enumeration enuRequest = new Enumeration(request);
            final Enumeration enuResponse = new Enumeration(response);
            EnumerationSupport.release(context, enuRequest, enuResponse);
            return;
        } else {
			if (request.getTimeout() == null) {
				throw new InternalErrorFault(
						"Missing expected wsman:OperationTimeout");
			}

			try {
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {
				// Nothing to do for this. Handled by WsManAgentSupport.
				return;
			}
			// We should not be here.
			throw new InternalErrorFault(
					"Missing expected wsman:OperationTimeout");
		}
    }
}
