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
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.5  2007/11/02 06:16:41  denis_rachal
 **Issue number:  141
 **Obtained from:
 **Submitted by:
 **Reviewed by:
 **
 **Fixed unit test to check if the enumeration context was actually released on the last pull.
 **
 **Revision 1.4  2007/05/30 20:30:30  nbeers
 **Add HP copyright header
 **
 ** 
 *
 * $Id: EnumerationTestCase.java,v 1.6 2007-12-03 09:15:11 denis_rachal Exp $
 */
package framework;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import util.WsManTestBaseSupport;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;

/**
 * 
 * 
 */
public class EnumerationTestCase extends WsManTestBaseSupport {
	private static final String RESOURCE_URI = "wsman:auth/file";

	public EnumerationTestCase(final String testName) {
		super(testName);
	}

	public void testEnumerate() throws JAXBException, IOException,
			DatatypeConfigurationException, SOAPException {
		EnumerateResponse enumerateResponse = sendEnumerateRequest(DESTINATION,
				RESOURCE_URI, null);
		assertNotNull(enumerateResponse);

		EnumerationContextType enumerationContext = enumerateResponse
				.getEnumerationContext();
		List<Object> content = enumerationContext.getContent();

		PullResponse pullResponse = sendPullRequest(DESTINATION, RESOURCE_URI,
				content.get(0));
		assertNotNull(pullResponse);

		if (pullResponse.getEndOfSequence() == null) {
			Addressing response = sendReleaseRequest(DESTINATION, RESOURCE_URI,
					content.get(0));
			assertFalse(response.getBody().hasFault());
		}
		Addressing response = sendReleaseRequest(DESTINATION, RESOURCE_URI,
				content.get(0));
		// Should have fault since we ensured it was released
		assertTrue(response.getBody().hasFault()); 

		enumerateResponse = sendEnumerateRequest(DESTINATION, RESOURCE_URI,
				null);
		assertNotNull(enumerateResponse);
		enumerationContext = enumerateResponse.getEnumerationContext();
		content = enumerationContext.getContent();
		response = sendReleaseRequest(DESTINATION, RESOURCE_URI, content.get(0));
		assertFalse(response.getBody().hasFault());
		response = sendEnumRequest(content.get(0), DESTINATION, RESOURCE_URI,
				Enumeration.PULL_ACTION_URI);
		
		// Should have a fault because it should have been released
		assertTrue(response.getBody().hasFault());
		// TODO: is null for now. Not implemented per WSMAN recommendation.
		assertNull(sendGetStatusRequest()); 
		// TODO: is null for now. Not implemented per WSMAN recommendation.
		assertNull(sendRenewRequest()); 

	}

}
