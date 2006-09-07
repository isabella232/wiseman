package com.sun.traffic.light.test;

import java.io.IOException;
import java.util.HashSet;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;

import util.WsManBaseTestSupport;

import com.sun.traffic.light.types.TrafficLightType;
import com.sun.ws.management.Management;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

public class TrafficLightTestCase extends WsManBaseTestSupport {


	private static final String TRANSFER_NS = "http://schemas.xmlsoap.org/ws/2004/09/transfer";
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	private static final String FACTORY_URI = "wsman:traffic/factory";
	private static final String RESOURCE_URI = "wsman:traffic/resource";
	private static final String LIST_URI = "wsman:traffic/list";
	private static final String DESTINATION = "http://localhost:8080/traffic/";
	private static final String WSMAN_NS="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";
	private static final String WSADD_NS="http://schemas.xmlsoap.org/ws/2004/08/addressing";
	
	private com.sun.traffic.light.types.ObjectFactory lightFactory=new com.sun.traffic.light.types.ObjectFactory();
    public static final org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory managementFactory = new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
	private XmlBinding binding;
	public TrafficLightTestCase() {
		super();
		try {
			binding = new XmlBinding(null,"com.sun.traffic.light.types");
		} catch (JAXBException e) {
			fail(e.getMessage());
		}
		
		// Enable basic authenticaton for tests
		System.getProperties().put("wsman.user", "wsman");
		System.getProperties().put("wsman.password", "secret");
		HttpClient.setAuthenticator(new transport.BasicAuthenticator());

	}
	
	/**
	 * Create a traffic light and then run XPaths over the result to confirm content
	 * of Resource Created. This test relies on xpaths to validate results.
	 * @throws JAXBException
	 * @throws SOAPException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */

	public void testCreateLight() throws JAXBException, SOAPException, DatatypeConfigurationException, IOException, XPathExpressionException{
		
		// Create a light document
		TrafficLightType light = lightFactory.createTrafficLightType();
		light.setColor("red");
		light.setName("TestLight1");
		light.setX(200);
		light.setY(200);
		
		// Submit this document
		Document doc = Management.newDocument();
		JAXBElement<TrafficLightType> lightElement =lightFactory.createTrafficlight(light);		 
		binding.marshal(lightElement,doc);
		Management ret = sendCreateRequest(DESTINATION,FACTORY_URI,doc);
		
		// Is there a body?
		assertNotNull(ret.getBody());
		
		// Is there a valid resource URI?
		String xpathBase = "*[namespace-uri()=\""+TRANSFER_NS+"\" and local-name()=\"ResourceCreated\"]/"+
		"*[namespace-uri()=\""+WSADD_NS+"\" and local-name()=\"ReferenceParameters\"]";

		assertTrue(getXPathValues(xpathBase,ret.getBody()).getLength()>0);

		
		String xPathResourceUri=xpathBase+"/*[namespace-uri()=\""+WSMAN_NS+"\" and local-name()=\"ResourceURI\"]";
		assertEquals("wsman:traffic/resource", getXPathText(xPathResourceUri,ret.getBody()));

		String xPathSelector=xpathBase+"/*[namespace-uri()=\""+WSMAN_NS+"\" and local-name()=\"SelectorSet\"]/*"
			+"[namespace-uri()=\""+WSMAN_NS+"\" and local-name()=\"Selector\" and @Name='name']";
		assertEquals("TestLight1", getXPathText(xPathSelector,ret.getBody()));

		
	}
/**
 * A unit tests that test a get after a create. 
 * It uses JAXB to test the returned resource state.
 * @throws XPathExpressionException
 * @throws JAXBException
 * @throws SOAPException
 * @throws DatatypeConfigurationException
 * @throws IOException
 */	
	public void testGetColor() throws XPathExpressionException, JAXBException, SOAPException, DatatypeConfigurationException, IOException{

		// Create a light document
		TrafficLightType light = lightFactory.createTrafficLightType();
		light.setColor("red");
		light.setName("TestLight2");
		light.setX(200);
		light.setY(200);

		// Submit this document
		Document doc = Management.newDocument();
		JAXBElement<TrafficLightType> lightElement =lightFactory.createTrafficlight(light);		 
		binding.marshal(lightElement,doc);
		Management ret = sendCreateRequest(DESTINATION,FACTORY_URI,doc);
		
		// Now request the state of TestLight2
		SelectorType nameSelectorType = managementFactory.createSelectorType();
		nameSelectorType.setName("name");
		nameSelectorType.getContent().add("TestLight2");
		HashSet<SelectorType> selectorsHash = new HashSet<SelectorType>();
		selectorsHash.add(nameSelectorType);
		Management response = sendGetRequest(DESTINATION,RESOURCE_URI, selectorsHash);

		// Now convert the body into a JAXB Type
		TrafficLightType lightState = (TrafficLightType)(((JAXBElement<TrafficLightType>)binding.unmarshal(response.getBody().getFirstChild())).getValue());

		// Compare fields
		assertEquals(light.getColor(), lightState.getColor());
		assertEquals(light.getName(), lightState.getName());
		assertEquals(light.getX(), lightState.getX());
		assertEquals(light.getY(), lightState.getY());

	}
	
}
