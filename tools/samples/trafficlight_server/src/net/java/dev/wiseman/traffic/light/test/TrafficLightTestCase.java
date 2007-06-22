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
 **Revision 1.4  2007/05/30 20:30:32  nbeers
 **Add HP copyright header
 **
 ** 
 *
 * $Id: TrafficLightTestCase.java,v 1.5 2007-06-22 06:13:54 simeonpinder Exp $
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
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import util.WsManBaseTestSupport;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.server.BaseSupport;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

public class TrafficLightTestCase extends WsManBaseTestSupport {

	private static final String TRANSFER_NS = "http://schemas.xmlsoap.org/ws/2004/09/transfer";

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private static final String RESOURCE_URI = "urn:resources.wiseman.dev.java.net/traffic/1/light";

	private static final String DESTINATION = "http://localhost:8080/traffic/";
//	private static final String DESTINATION = "http://192.168.0.6:8080/traffic/";

	private static final String WSMAN_NS = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";

	private static final String WSADD_NS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

	private net.java.dev.wiseman.schemas.traffic._1.light.ObjectFactory lightFactory = new net.java.dev.wiseman.schemas.traffic._1.light.ObjectFactory();

	public static final org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory managementFactory = new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();

	private XmlBinding binding;

	public TrafficLightTestCase() {
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
	 * Create a traffic light and then run XPaths over the result to confirm
	 * content of Resource Created. This test relies on xpaths to validate
	 * results.
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
		Management ret = sendCreateRequest(DESTINATION, RESOURCE_URI, doc);

		// Is there a body?
		assertNotNull(ret.getBody());

		// Is there a valid resource URI?
		String xpathBase = "*[namespace-uri()=\"" + TRANSFER_NS
				+ "\" and local-name()=\"ResourceCreated\"]/"
				+ "*[namespace-uri()=\"" + WSADD_NS
				+ "\" and local-name()=\"ReferenceParameters\"]";

		assertTrue(getXPathValues(xpathBase, ret.getBody()).getLength() > 0);

		String xPathResourceUri = xpathBase + "/*[namespace-uri()=\""
				+ WSMAN_NS + "\" and local-name()=\"ResourceURI\"]";
		assertEquals("urn:resources.wiseman.dev.java.net/traffic/1/light", 
				getXPathText(xPathResourceUri,
				ret.getBody()));

		String xPathSelector = xpathBase + "/*[namespace-uri()=\"" + WSMAN_NS
				+ "\" and local-name()=\"SelectorSet\"]/*"
				+ "[namespace-uri()=\"" + WSMAN_NS
				+ "\" and local-name()=\"Selector\" and @Name='name']";
		assertEquals("TestLight1", getXPathText(xPathSelector, ret.getBody()));

	}

	/**
	 * A unit tests that test a get after a create. It uses JAXB to test the
	 * returned resource state.
	 * 
	 * @throws XPathExpressionException
	 * @throws JAXBException
	 * @throws SOAPException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 */
	public void testGetColor() throws XPathExpressionException, JAXBException,
			SOAPException, DatatypeConfigurationException, IOException {

		// Create a light document
		TrafficLightType light = lightFactory.createTrafficLightType();
		light.setColor("red");
		light.setName("TestLight2");
		light.setX(200);
		light.setY(200);

		// Submit this document
		Document doc = Management.newDocument();
		JAXBElement<TrafficLightType> lightElement = lightFactory
				.createTrafficlight(light);
		binding.marshal(lightElement, doc);
		Management ret = sendCreateRequest(DESTINATION, RESOURCE_URI, doc);

		// Now request the state of TestLight2
		SelectorType nameSelectorType = managementFactory.createSelectorType();
		nameSelectorType.setName("name");
		nameSelectorType.getContent().add("TestLight2");
		HashSet<SelectorType> selectorsHash = new HashSet<SelectorType>();
		selectorsHash.add(nameSelectorType);
		Management response = sendGetRequest(DESTINATION, RESOURCE_URI,
				selectorsHash, null);

		// Now convert the body into a JAXB Type
		TrafficLightType lightState = (TrafficLightType) (((JAXBElement<TrafficLightType>) binding
				.unmarshal(response.getBody().getFirstChild())).getValue());

		// Compare fields
		assertEquals(light.getColor(), lightState.getColor());
		assertEquals(light.getName(), lightState.getName());
		assertEquals(light.getX(), lightState.getX());
		assertEquals(light.getY(), lightState.getY());

	}
	////FOLLOWING IS A SAMPLE UNIT TEST to exercise fragment GET/PUT functionality.
	////SERVER side backend functionality is commented as well so will be unable to run valid tests.
//	public void testFragmentLight() throws XPathExpressionException, JAXBException,
//			SOAPException, DatatypeConfigurationException, IOException {
//		
//		String lightName="TrafficLight-Fragment";
//		
//		// Create a light document
//		TrafficLightType light = lightFactory.createTrafficLightType();
//		light.setColor("red");
//		light.setName(lightName);
//		light.setX(200);
//		light.setY(200);
//		
//		// Submit this document
//		Document doc = Management.newDocument();
//		JAXBElement<TrafficLightType> lightElement = lightFactory
//				.createTrafficlight(light);
//		binding.marshal(lightElement, doc);
//		Management fragmentMetadata = AnnotationProcessor.findAnnotatedResourceByUID(
//				DESTINATION+lightName, 
//				ManagementMessageValues.WSMAN_DESTINATION);
//		Management ret = new Management(fragmentMetadata);
//		//set action
//		ret.setAction(Transfer.CREATE_ACTION_URI);
//		//set put the body in.
//		ret.getBody().addDocument(doc);
//		ret = ManagementUtility.buildMessage(ret,null);
//System.out.println("Request:"+ret);		
//		Addressing resp = HttpClient.sendRequest(ret);
//System.out.println("Response:"+resp);
//		Management response = new Management(resp);
//		
//		//########## FRAGMENT GET REQUEST
//		//Now do fragment get request
//		Management fragGet = new Management(fragmentMetadata);
//		//Set the action 
//		fragGet.setAction(Transfer.GET_ACTION_URI);
//		//set the fragment porting indicating what to return
//		String xPathReq = "//*[local-name()='color']";
//		TransferExtensions trnx = new TransferExtensions(fragGet);
//		trnx.setFragmentHeader(xPathReq,null, XPath.NS_URI);
//		fragGet = ManagementUtility.buildMessage(trnx,null);
//		fragGet.getBody().removeContents();
//System.out.println("Request:"+fragGet);		
//		resp = HttpClient.sendRequest(fragGet);
//System.out.println("Response:"+resp);
//		response = new Management(resp);
//		
//		//########## FRAGMENT PUT REQUEST
//		//Now do fragment get request
//		Management fragPut = new Management(fragmentMetadata);
//		//Set the action 
//		fragGet.setAction(Transfer.PUT_ACTION_URI);
//		//set the fragment porting indicating what to return
//		xPathReq = "//*[local-name()='color']";
//		trnx = new TransferExtensions(fragPut);
//		  trnx.setFragmentHeader(xPathReq,null, XPath.NS_URI);
//		  fragPut = ManagementUtility.buildMessage(trnx,null);
//		  //build fragment put body
//		  Document content =Management.newDocument();
//		final DocumentFragment fragment = content.
//							createDocumentFragment();
//		// Insert the root element node
//		//<tl:color>red</tl:color >
//		final Element element = content.createElementNS(
//				"http://schemas.wiseman.dev.java.net/traffic/1/light.xsd", "t1:color");
//		element.setTextContent("green");
//		fragment.appendChild(element);
//		final Object xmlFragment = BaseSupport
//			.createXmlFragment(((DocumentFragment)fragment).getChildNodes());
//			fragPut.getXmlBinding().marshal(xmlFragment, fragPut.getBody());
//System.out.println("Request-Put:"+fragPut);		
//		resp = HttpClient.sendRequest(fragPut);
//System.out.println("Response-PUT:"+resp);
//		response = new Management(resp);
//		
//	}

}
