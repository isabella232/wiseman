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
 *
 * $Id: EmptyTrafficLightTestCase.java,v 1.1 2007-09-18 18:19:03 nbeers Exp $
 */
package net.java.dev.wiseman.traffic.light.test;

import java.io.IOException;
import java.util.HashSet;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import net.java.dev.wiseman.schemas.traffic._1.light.TrafficLightType;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;

import util.WsManBaseTestSupport;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

/**
 * Test case to be run against a newly generated (but not yet implemented) wiseman web service
 * This test case checks that the action not supported exception is thrown when a method is called.
 * 
 * @author nabee
 *
 */
public class EmptyTrafficLightTestCase extends WsManBaseTestSupport {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private static final String RESOURCE_URI = "urn:resources.wiseman.dev.java.net/traffic/1/light";

	private static final String DESTINATION = "http://localhost:8080/gentraffic/";

	private static final String RESOURCE_URI_2 = "urn:resources.www.hp.com/itil/2007/05/ServiceType/";

	private static final String DESTINATION_2 = "http://localhost:8080/servicetype/";
	
	private net.java.dev.wiseman.schemas.traffic._1.light.ObjectFactory lightFactory = new net.java.dev.wiseman.schemas.traffic._1.light.ObjectFactory();

	public static final org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory managementFactory = new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();

	private XmlBinding binding;

	public EmptyTrafficLightTestCase() {
		super();
		try {
			binding = new XmlBinding(null, "net.java.dev.wiseman.schemas.traffic._1.light");
		} catch (JAXBException e) {
			fail(e.getMessage());
		}

		// Enable basic authenticaton for tests
		System.getProperties().put("wsman.user", "wsman");
		System.getProperties().put("wsman.password", "secret");
		HttpClient.setAuthenticator(new transport.BasicAuthenticator());

	}

	/**
	 * Create a traffic light and check to see that the correct "not supported" exception is thrown.
	 * 
	 * @throws JAXBException
	 * @throws SOAPException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */

	public void testCreateLight() throws JAXBException, SOAPException,
			DatatypeConfigurationException, IOException,
			XPathExpressionException {

		// Create a light document
		TrafficLightType light = lightFactory.createTrafficLightType();
		light.setColor("red");
		light.setName("TestLight1");
		light.setX(200);
		light.setY(200);

		// Submit this document
		Document doc = Management.newDocument();
		JAXBElement<TrafficLightType> lightElement = lightFactory
				.createTrafficlight(light);
		binding.marshal(lightElement, doc);

		try {
			Management ret = sendCreateRequest(DESTINATION, RESOURCE_URI, doc);
			
			// If we got here, the correct exception was not thrown
			assertTrue(false);
		} catch (SOAPException e) {
			if (!(e.getMessage().startsWith(ActionNotSupportedFault.ACTION_NOT_SUPPORTED_REASON))) {
				assertTrue(false);
			}
		}		


	}

	/**
	 * A unit tests that test a get after a create. Checjks that the correct "not supported" exception is thrown.
	 * 
	 * @throws XPathExpressionException
	 * @throws JAXBException
	 * @throws SOAPException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 */
	public void testGetColor() throws XPathExpressionException, JAXBException,
			SOAPException, DatatypeConfigurationException, IOException {

		// Now request the state of TestLight2
		SelectorType nameSelectorType = managementFactory.createSelectorType();
		nameSelectorType.setName("name");
		nameSelectorType.getContent().add("TestLight2");
		HashSet<SelectorType> selectorsHash = new HashSet<SelectorType>();
		selectorsHash.add(nameSelectorType);
		try {
			Management response = sendGetRequest(DESTINATION, RESOURCE_URI, selectorsHash,
					null);
			// If we got here, the correct exception was not thrown
			assertTrue(false);
			
		} catch (SOAPException e) {
			if (!(e.getMessage().startsWith(ActionNotSupportedFault.ACTION_NOT_SUPPORTED_REASON))) {
				assertTrue(false);
			}
		}		

	}
/*	public void testAttachments() throws XPathExpressionException, JAXBException,
	SOAPException, DatatypeConfigurationException, IOException {

//		Now request the state of TestLight2
		SelectorType nameSelectorType = managementFactory.createSelectorType();
		nameSelectorType.setName("version");
		nameSelectorType.getContent().add("1.0");
		HashSet<SelectorType> selectorsHash = new HashSet<SelectorType>();
		selectorsHash.add(nameSelectorType);  
		try {
			Management response = sendGetRequest(DESTINATION_2, RESOURCE_URI_2, selectorsHash,
					null);
			System.out.println(response.getAttachment("myData").getContent());
		} catch (SOAPException e) {
			e.printStackTrace();
		}		

	}
*/
}
