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

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import util.WsManBaseTestSupport;

import com.sun.ws.management.Management;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.HandlerContextImpl;
import com.sun.ws.management.server.WSEnumerationSupport;
import com.sun.ws.management.server.message.SAAJMessage;

public class WSEnumerationSupportTestCase extends WsManBaseTestSupport {
	
	private static final String TO = "http://localhost:8080/wsman";
	private static final String RESOURCE_URI = "http://wiseman.dev.java.net/resource";
	private static final String ENCODING = "UTF-8";
	private static final String CONTENTTYPE = "text/xml";
	
	public class TestIterator implements EnumerationIterator {

		private final int count = 10;
		private final Iterator<EnumerationItem> iterator;
		
		public TestIterator() {
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
	
	public WSEnumerationSupportTestCase() {
		
	}
	
	public void testEnumerateCancel() throws SOAPException, JAXBException, DatatypeConfigurationException {
    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
    	settings.setFilter(null);
    	settings.setTo(TO);
    	settings.setResourceUri(RESOURCE_URI);
    	final Management enu = new Management(EnumerationUtility.buildMessage(null, settings));
    	final SAAJMessage request = new SAAJMessage(enu);
    	final SAAJMessage response = new SAAJMessage(new Management());
    	
		final HandlerContext context = new HandlerContextImpl(null, CONTENTTYPE,
				                                              ENCODING, TO, null);
		request.cancel();
		try {
		    WSEnumerationSupport.enumerate(context, request, response,
		    		                       new TestIterator(), null);
			fail("Expected TimedOutFault exception.");
		    
		} catch (TimedOutFault e) {
			// Success
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
}
