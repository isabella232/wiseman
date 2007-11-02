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
 **Revision 1.4  2007/05/30 20:30:30  nbeers
 **Add HP copyright header
 **
 ** 
 *
 * $Id: EnumerationTestCase.java,v 1.5 2007-11-02 06:16:41 denis_rachal Exp $
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

import util.WsManBaseTestSupport;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;

/**
 * 
 * 
 */
public class EnumerationTestCase extends WsManBaseTestSupport
{
    private static final String RESOURCE_URI = "wsman:auth/file";
    private static final String DESTINATION = "http://localhost:8080/wsman/";

    public EnumerationTestCase()
    {
//        try
//        {
//            new Management();
//        }
//        catch (SOAPException e)
//        {
//            fail("Can't init wiseman");
//        }

    }

    public void testEnumerate() throws JAXBException, IOException, DatatypeConfigurationException, SOAPException
    {
        EnumerateResponse enumerateResponse = sendEnumerateRequest(DESTINATION, RESOURCE_URI, null);
        assertNotNull(enumerateResponse);

        EnumerationContextType enumerationContext = enumerateResponse.getEnumerationContext();
        List<Object> content = enumerationContext.getContent();

        PullResponse pullResponse = sendPullRequest(DESTINATION, RESOURCE_URI, content.get(0));
        assertNotNull(pullResponse);

        if (pullResponse.getEndOfSequence() == null) {
        	Addressing response = sendReleaseRequest(DESTINATION, RESOURCE_URI, content.get(0));
        	assertFalse(response.getBody().hasFault());
        }
        Addressing response = sendReleaseRequest(DESTINATION, RESOURCE_URI, content.get(0));
        assertTrue(response.getBody().hasFault());//should have fault since we ensured it was released

        enumerateResponse = sendEnumerateRequest(DESTINATION, RESOURCE_URI, null);
        assertNotNull(enumerateResponse);
        enumerationContext = enumerateResponse.getEnumerationContext();
        content = enumerationContext.getContent();
        response = sendReleaseRequest(DESTINATION, RESOURCE_URI, content.get(0));
        assertFalse(response.getBody().hasFault());
        response = sendEnumRequest(content.get(0),DESTINATION, RESOURCE_URI, Enumeration.PULL_ACTION_URI);
        assertTrue(response.getBody().hasFault());//should have a fault because it should have been released

        assertNull(sendGetStatusRequest());  //todo is null for now..not implemented
        assertNull(sendRenewRequest());  //todo is null for now..not implemented

    }

}
