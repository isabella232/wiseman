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
 **Revision 1.1  2007/06/04 06:25:12  denis_rachal
 **The following fixes have been made:
 **
 **   * Moved test source to se/test/src
 **   * Moved test handlers to /src/test/src
 **   * Updated logging calls in HttpClient & Servlet
 **   * Fxed compiler warning in AnnotationProcessor
 **   * Added logging files for client junit tests
 **   * Added changes to support Maven builds
 **   * Added JAX-WS libraries to CVS ignore
 **
 **Revision 1.5  2007/05/31 07:04:23  denis_rachal
 **Switch test to check for RC2.
 **
 **Revision 1.4  2007/05/30 20:30:17  nbeers
 **Add HP copyright header
 **
 ** 
 *
 * $Id: ResourceTest.java,v 1.2 2007-06-08 15:38:38 denis_rachal Exp $
 */
package com.sun.ws.management.client;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;

import util.WsManBaseTestSupport;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingUtility;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.mex.Metadata;
import com.sun.ws.management.mex.MetadataUtility;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.handler.wsman.eventsubman_Handler;
import com.sun.ws.management.server.handler.wsman.auth.eventcreator_Handler;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

/**
 * This class tests basic WS Transfer behavior and also demonstrates the
 * capabilies of the client library.
 * 
 * @author wire
 * 
 */
public class ResourceTest extends WsManBaseTestSupport {

	private static ObjectFactory userFactory = new ObjectFactory();
	public static String destUrl = ManagementMessageValues.WSMAN_DESTINATION;
	public static String resourceUri = "wsman:auth/user";
	public static long timeoutInMilliseconds = 9400000;

	private static final String USER_NS = "http://examples.hp.com/ws/wsman/user";

	private XmlBinding binding = null;
	
	protected void setUp() throws Exception {
		super.setUp();
	    
		try {
			binding = new XmlBinding(null, "com.hp.examples.ws.wsman.user");
			ResourceFactory.setBinding(binding);
		} catch (JAXBException e) {
			fail(e.getMessage());
		}
		userFactory = new ObjectFactory();
	}
	
	public void testIdentity() throws XPathExpressionException,
			NoMatchFoundException, SOAPException, IOException, JAXBException {
		ServerIdentity serverInfo = ResourceFactory.getIdentity(destUrl);
		assertNotNull(serverInfo);
		assertNotNull(serverInfo.getProductVendor());
		assertNotNull(serverInfo.getProductVersion());
		assertNotNull(serverInfo.getProtocolVersion());
		assertNotNull(serverInfo.getSpecVersion());
		assertNotNull(serverInfo.getBuildId());
		assertEquals(serverInfo.getProductVendor(),
				"The Wiseman Project - https://wiseman.dev.java.net");
		// assertEquals(serverInfo.getProductVersion(),"0.6");
		// assertEquals(serverInfo.getProductVersion(),"1.0_RC1");
		assertEquals(serverInfo.getProductVersion(), "1.0_RC2");
		assertEquals(serverInfo.getProtocolVersion(),
				"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd");
		assertEquals(serverInfo.getSpecVersion(), "1.0.0a");
		// assertTrue(serverInfo.getBuildId().startsWith("2006"));

	}

	public void testIdentityHeaders() throws XPathExpressionException,
			NoMatchFoundException, SOAPException, IOException, JAXBException {
		// ServerIdentity serverInfo =
		// ResourceFactory.getIdentity(destUrl,1000,new Entry<String,
		// String>("",""));
		//		
		// String respose="HTTP/1.0 400 Bad Request";
	}

	/**
	 * Tests to see if an identity request will timout
	 * 
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundException
	 * @throws SOAPException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws InterruptedException
	 */
	public void testIdentityTimeout() throws XPathExpressionException,
			NoMatchFoundException, SOAPException, IOException, JAXBException,
			InterruptedException {
		// Create a non-blocking server socket and check for connections
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		ssChannel.configureBlocking(false);
		int port = 10666;
		ssChannel.socket().bind(new InetSocketAddress(port));

		SocketChannel sChannel = ssChannel.accept();

		try {
			ResourceFactory.getIdentity("http://localhost:10666/wsman", 500);
		} catch (TimeoutException e) {
			return;
		}
		sChannel.close();
		ssChannel.close();

		fail("A timeout did not occur as expected.");
	}

	/*
	 * Test method for
	 * 'com.sun.ws.management.client.impl.TransferableResourceImpl.get(Map<String,
	 * Object>)'
	 */
	@SuppressWarnings("unchecked")
	public void testGet() throws JAXBException, SOAPException, IOException,
			DatatypeConfigurationException, TransformerException,
			FaultException {

		// String className
		// ="com.sun.ws.management.server.ReflectiveRequestDispatcher";
		// locateClass(className);
		// className ="com.sun.tools.xjc.reader.Util";
		// locateClass(className);

		// now build Create XML body contents.
		String fName = "Get";
		String lName = "Guy";
		String address = "Smoky Lane";

		// Create a JAXB type representing a User's internal state
		UserType user = userFactory.createUserType();
		user.setLastname(lName);
		user.setFirstname(fName);
		user.setAddress(address);
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
		user.setAge(16);

		// Convert this into a state XML document dom
		Document content = Management.newDocument();
		JAXBElement<UserType> userElement = userFactory.createUser(user);
		binding.marshal(userElement, content);

		Resource resource = ResourceFactory.create(ResourceTest.destUrl,
				ResourceTest.resourceUri, ResourceTest.timeoutInMilliseconds,
				content, ResourceFactory.LATEST);

		resource.addOption("opt1", "value1", new QName(
				XMLConstants.W3C_XML_SCHEMA_NS_URI, "string"));
		resource.addOption("opt2", new Integer(7), new QName(
				XMLConstants.W3C_XML_SCHEMA_NS_URI, "int"), true);
		resource.addOption("opt3", new Boolean(true));
		resource.addOption("opt2", new Integer(99), new QName(
				XMLConstants.W3C_XML_SCHEMA_NS_URI, "int"), true);

		// pull out Epr and parse for selectors
		// EndpointReferenceType retrievedEpr = created.getResourceEpr();
		ResourceState resourceState = resource.get();

		assertNotNull("Retrieved resource is NULL.", resourceState);

		Document resourceStateDom = resourceState.getDocument();
		JAXBElement<UserType> unmarshal = (JAXBElement<UserType>) binding
				.unmarshal(resourceStateDom);
		JAXBElement<UserType> userReturnedElement = unmarshal;
		UserType returnedUser = (UserType) userReturnedElement.getValue();

		// Compare the created state to the returned state
		assertEquals(returnedUser.getLastname(), user.getLastname());
		assertEquals(returnedUser.getFirstname(), user.getFirstname());
		assertEquals(returnedUser.getAddress(), user.getAddress());
		assertEquals(returnedUser.getCity(), user.getCity());
		assertEquals(returnedUser.getState(), user.getState());
		assertEquals(returnedUser.getZip(), user.getZip());
		assertEquals(returnedUser.getAge(), user.getAge());
		resource.delete();
	}

	/*
	 * Test method for
	 * 'com.sun.ws.management.client.impl.TransferableResourceImpl.get(Map<String,
	 * Object>)'
	 */
	public void testFragmentGet() throws JAXBException, SOAPException,
			IOException, DatatypeConfigurationException, TransformerException,
			FaultException {

		// now build Create XML body contents.
		final String fName = "Get";
		final String lName = "Guy";
		final String address = "Smoky Lane";
		final String state = "NJ";
		final int age = 16;

		// Create a JAXB type representing a User's internal state
		UserType user = userFactory.createUserType();
		user.setLastname(lName);
		user.setFirstname(fName);
		user.setAddress(address);
		user.setCity("Mount Laurel");
		user.setState(state);
		user.setZip("08054");
		user.setAge(age);

		// Convert this into a state XML document dom
		Document content = Management.newDocument();
		JAXBElement<UserType> userElement = userFactory.createUser(user);
		binding.marshal(userElement, content);

		Resource resource = ResourceFactory.create(ResourceTest.destUrl,
				ResourceTest.resourceUri, ResourceTest.timeoutInMilliseconds,
				content, ResourceFactory.LATEST);

		resource.addOption("opt1", "value1");
		resource.addOption("opt2", new Integer(7));
		resource.addOption("opt3", new Boolean(true));

		// Now build the XPath expression for fragment GET
		String xPathReq = "//*[local-name()='age']";
		ResourceState resourceState = resource.get(xPathReq, null);

		assertNotNull("Retrieved resource is NULL.", resourceState);

		// Test expected age content
		Document fragmentContent = resourceState.getDocument();
		Element fragment = fragmentContent.getDocumentElement();
		assertEquals("Retrieved document is not an XmlFragment", "XmlFragment",
				fragment.getLocalName());
		assertEquals("Retrieved XmlFragment is not in WS Management namespace",
				Management.NS_URI, fragment.getNamespaceURI());
		NodeList children = fragment.getChildNodes();
		assertNotNull("Missing fragment elements", children);
		assertEquals("Wrong number of elements returned", 1, children
				.getLength());
		Node child = children.item(0);
		assertTrue("Wrong element returned", ((child.getLocalName()
				.equals("age")) && (child.getNamespaceURI().equals(USER_NS))));

		// Test /u:user/u:age/text()
		xPathReq = "/u:user/u:age/text()";
		HashMap<String, String> namespaces = new HashMap<String, String>(1);
		namespaces.put("u", USER_NS);
		resourceState = resource.get(xPathReq, namespaces, null);

		assertNotNull("Retrieved resource is NULL.", resourceState);

		// Test expected age content
		fragmentContent = resourceState.getDocument();
		fragment = fragmentContent.getDocumentElement();
		assertEquals("Retrieved document is not an XmlFragment", "XmlFragment",
				fragment.getLocalName());
		assertEquals("Retrieved XmlFragment is not in WS Management namespace",
				Management.NS_URI, fragment.getNamespaceURI());
		children = fragment.getChildNodes();
		assertNotNull("Missing fragment elements", children);
		assertEquals("Wrong number of elements returned", 1, children
				.getLength());
		child = children.item(0);
		assertTrue("Element returned is not of type Text.",
				(child instanceof Text));
		assertEquals("Wrong value returned for age.", new Integer(age)
				.toString(), child.getTextContent());

		// Test /u:user/u:firstname|/u:user/u:lastname|/u:user/u:state
		xPathReq = "/u:user/u:firstname|/u:user/u:lastname|/u:user/u:state";
		resourceState = resource.get(xPathReq, namespaces, null);

		assertNotNull("Retrieved resource is NULL.", resourceState);

		// Test expected age content
		fragmentContent = resourceState.getDocument();
		fragment = fragmentContent.getDocumentElement();
		assertEquals("Retrieved document is not an XmlFragment", "XmlFragment",
				fragment.getLocalName());
		assertEquals("Retrieved XmlFragment is not in WS Management namespace",
				Management.NS_URI, fragment.getNamespaceURI());
		children = fragment.getChildNodes();
		assertNotNull("Missing fragment elements", children);
		assertEquals("Wrong number of elements returned", 3, children
				.getLength());
		assertEquals("Element returned has the wrong namespace", USER_NS,
				children.item(0).getNamespaceURI());
		assertEquals("Element returned has the wrong namespace", USER_NS,
				children.item(1).getNamespaceURI());
		assertEquals("Element returned has the wrong namespace", USER_NS,
				children.item(2).getNamespaceURI());

		for (int i = 0; i < children.getLength(); i++) {
			final String name = children.item(i).getLocalName();
			final String value = children.item(i).getTextContent();
			if (name.equals("firstname")) {
				assertEquals("Wrong firstname returned.", fName, value);
			} else if (name.equals("lastname")) {
				assertEquals("Wrong lastname returned.", lName, value);
			} else if (name.equals("state")) {
				assertEquals("Wrong state returned.", state, value);
			} else {
				fail("Unexpected element returned:" + name);
			}
		}

		// Delete the resource
		resource.delete();
	}

	public void testInvoke() throws JAXBException, SOAPException, IOException,
			DatatypeConfigurationException, FaultException {

		// Now create an instance and test it's contents

		UserType user = userFactory.createUserType();
		user.setLastname("Finkle");
		user.setFirstname("Joe");
		user.setAddress("6000 Irwin Drive");
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
		user.setAge(16);

		Document stateDocument = Management.newDocument();
		JAXBElement<UserType> userElement = userFactory.createUser(user);
		binding.marshal(userElement, stateDocument);

		Resource newResource = ResourceFactory.create(ResourceTest.destUrl,
				ResourceTest.resourceUri, ResourceTest.timeoutInMilliseconds,
				stateDocument, ResourceFactory.LATEST, null);

		ResourceState response = newResource.invoke(
				"http://unit.test.org/customaction", stateDocument);
		assertNotNull(response.getDocument());
		assertNotNull(response.getDocument().getFirstChild());
		assertNotNull(response.getDocument().getFirstChild().getFirstChild());
		assertEquals("The answer is plastics", response.getDocument()
				.getFirstChild().getFirstChild().getTextContent());
	}

	/*
	 * Test method for
	 * 'com.sun.ws.management.client.impl.TransferableResourceImpl.create(Map<QName,
	 * String>)'
	 */
	public void testCreate() throws JAXBException, SOAPException, IOException,
			DatatypeConfigurationException, FaultException {

		// Now create an instance and test it's contents
		String dest = ResourceTest.destUrl;
		String resource = ResourceTest.resourceUri;

		UserType user = userFactory.createUserType();
		user.setLastname("Finkle");
		user.setFirstname("Joe");
		user.setAddress("6000 Irwin Drive");
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
		user.setAge(16);

		Document stateDocument = Management.newDocument();
		JAXBElement<UserType> userElement = userFactory.createUser(user);
		binding.marshal(userElement, stateDocument);

		// setup user options

		HashSet<OptionType> options = new HashSet<OptionType>();

		OptionType opt = new OptionType();
		opt.setName("createOpt");
		opt.setValue("abcd");
		options.add(opt);

		Resource newResource = ResourceFactory.create(ResourceTest.destUrl,
				ResourceTest.resourceUri, ResourceTest.timeoutInMilliseconds,
				stateDocument, ResourceFactory.LATEST, options);

		assertEquals("Values not identical.", dest, newResource
				.getDestination());
		assertEquals("Values not identical.", resource, newResource
				.getResourceUri());
		assertEquals("Values not identical.",
				ResourceTest.timeoutInMilliseconds, newResource
						.getMessageTimeout());

		// test contents of the selectors returned. Anything beyond EPR is
		// Xml:any though
		assertNotNull("SelectorSet null!", newResource.getSelectorSet());
		assertTrue("Incorrect number of Selectors returned.", (newResource
				.getSelectorSet().getSelector().size() == 2));
		assertEquals("Values not same.", "Finkle", newResource.getSelectorSet()
				.getSelector().get(0).getContent().get(0));
		assertEquals("Values not same.", "Joe", newResource.getSelectorSet()
				.getSelector().get(1).getContent().get(0));

		newResource.delete();
	}

	/*
	 * Test method for
	 * 'com.sun.ws.management.client.impl.TransferableResourceImpl.create(Map<QName,
	 * String>)'
	 */
	public void testFragmentCreate() throws JAXBException, SOAPException,
			IOException, DatatypeConfigurationException, FaultException {

		// Now create an instance and test it's contents
		String dest = ResourceTest.destUrl;
		String resource = ResourceTest.resourceUri;

		String lastName = "Finkle-Fragment"
				+ Calendar.getInstance().getTimeInMillis();
		UserType user = userFactory.createUserType();
		user.setLastname(lastName);
		user.setFirstname("Joe");
		user.setAddress("6000 Irwin Drive");
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
		// user.setAge(16);

		Document stateDocument = Management.newDocument();
		JAXBElement<UserType> userElement = userFactory.createUser(user);
		binding.marshal(userElement, stateDocument);

		Resource newResource = ResourceFactory.create(ResourceTest.destUrl,
				ResourceTest.resourceUri, ResourceTest.timeoutInMilliseconds,
				stateDocument, ResourceFactory.LATEST);

		assertEquals("Values not identical.", dest, newResource
				.getDestination());
		assertEquals("Values not identical.", resource, newResource
				.getResourceUri());
		assertEquals("Values not identical.",
				ResourceTest.timeoutInMilliseconds, newResource
						.getMessageTimeout());

		// DONE: test contents of the selectors returned. Anything beyond EPR is
		// Xml:any though
		assertNotNull("SelectorSet null!", newResource.getSelectorSet());
		assertTrue("Incorrect number of Selectors returned.", (newResource
				.getSelectorSet().getSelector().size() == 2));
		assertEquals("Values not same.", lastName, newResource.getSelectorSet()
				.getSelector().get(0).getContent().get(0));
		assertEquals("Values not same.", "Joe", newResource.getSelectorSet()
				.getSelector().get(1).getContent().get(0));

		ResourceState resourceState = newResource.get();
		assertNotNull("Retrieved resource is NULL.", resourceState);

		// Check document returned
		Document content = resourceState.getDocument();
		assertNotNull("Get is missing resource.", content);
		Element userDocument = content.getDocumentElement();
		assertEquals("Retrieved document is not a user", "user", userDocument
				.getLocalName());
		assertEquals("Retrieved user has the wrong namespace", USER_NS,
				userDocument.getNamespaceURI());
		NodeList children = userDocument.getChildNodes();
		assertNotNull("Missing user elements", children);
		assertEquals("Wrong number of elements returned", 6, children
				.getLength());

		assertEquals("Element returned has the wrong namespace", USER_NS,
				children.item(0).getNamespaceURI());
		assertEquals("Element returned has the wrong namespace", USER_NS,
				children.item(1).getNamespaceURI());
		assertEquals("Element returned has the wrong namespace", USER_NS,
				children.item(2).getNamespaceURI());

		for (int i = 0; i < children.getLength(); i++) {
			final String name = children.item(i).getLocalName();
			final String namespace = children.item(i).getNamespaceURI();
			final String value = children.item(i).getTextContent();

			assertEquals("Element returned has the wrong namespace", USER_NS,
					namespace);
			if (name.equals("firstname")) {
				assertEquals("Wrong firstname returned.", "Joe", value);
			} else if (name.equals("lastname")) {
				assertEquals("Wrong lastname returned.", lastName, value);
			} else if (name.equals("state")) {
				assertEquals("Wrong state returned.", "NJ", value);
			} else if (name.equals("address")) {
				assertEquals("Wrong address returned.", "6000 Irwin Drive",
						value);
			} else if (name.equals("city")) {
				assertEquals("Wrong city returned.", "Mount Laurel", value);
			} else if (name.equals("zip")) {
				assertEquals("Wrong zip returned.", "08054", value);
			} else {
				fail("Unexpected element returned:" + name);
			}
		}

		// Unmarshall the document & check again
		JAXBElement userReturnedElement = (JAXBElement) binding
				.unmarshal(content);
		UserType returnedUser = (UserType) userReturnedElement.getValue();

		// Compare the created state to the returned state
		assertEquals(returnedUser.getLastname(), user.getLastname());
		assertEquals(returnedUser.getFirstname(), user.getFirstname());
		assertNull("Age field should be null:" + returnedUser.getAge(),
				returnedUser.getAge());

		// Build the fragment create method and object changes
		String fragmentRequest = "//*[local-name()='age']";
		stateDocument = Management.newDocument();
		// Insert the root element node
		Element element = stateDocument.createElementNS(
				"http://examples.hp.com/ws/wsman/user", "user:age");
		Integer number = new Integer(10);
		element.setTextContent(number.toString());
		stateDocument.appendChild(element);
		userElement = userFactory.createUser(user);

		final Resource newFragResource = ResourceFactory.createFragment(
				ResourceTest.destUrl, ResourceTest.resourceUri, newResource
						.getSelectorSet(), ResourceTest.timeoutInMilliseconds,
				stateDocument, ResourceFactory.LATEST, fragmentRequest,
				XPath.NS_URI);

		assertEquals("Values not identical.", dest, newFragResource
				.getDestination());
		assertEquals("Values not identical.", resource, newFragResource
				.getResourceUri());
		assertEquals("Values not identical.",
				ResourceTest.timeoutInMilliseconds, newFragResource
						.getMessageTimeout());

		resourceState = newFragResource.get();
		assertNotNull("Retrieved resource is NULL.", resourceState);

		Document resourceStateDom = resourceState.getDocument();
		userReturnedElement = (JAXBElement) binding.unmarshal(resourceStateDom);
		returnedUser = (UserType) userReturnedElement.getValue();

		// Compare the created state to the returned state
		assertEquals(returnedUser.getLastname(), user.getLastname());
		assertEquals(returnedUser.getFirstname(), user.getFirstname());
		assertEquals("Value not retrieved correctly.", (int) returnedUser
				.getAge(), number.intValue());
		newResource.delete();
	}

	/*
	 * Test method for
	 * 'com.sun.ws.management.client.impl.TransferableResourceImpl.delete()'
	 */
	public void testDelete() throws JAXBException, SOAPException, IOException,
			FaultException, DatatypeConfigurationException {
		String dest = ResourceTest.destUrl;
		String resource = ResourceTest.resourceUri;
		long timeoutInMilliseconds = ResourceTest.timeoutInMilliseconds;
		Document content = null;

		// now build Create XML body contents.
		String fName = "Delete";
		String lName = "Guy";
		String address = "Smoky Lane";
		UserType user = userFactory.createUserType();
		user.setLastname(lName);
		user.setFirstname(fName);
		user.setAddress(address);
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
		user.setAge(16);
		content = Management.newDocument();

		content = loadUserTypeToDocument(content, user, binding);

		Resource created = ResourceFactory.create(dest, resource,
				timeoutInMilliseconds, content, ResourceFactory.LATEST);
		// pull out Epr and parse for selectors
		// EndpointReferenceType retrievedEpr = created.getResourceEpr();

		SelectorSetType selectorSet = null;
		selectorSet = created.getSelectorSet();
		assertNotNull("SelectorSet is null.", selectorSet);

		created.addOption("opt1", "value1");
		created.addOption("opt2", new Integer(7), true);
		created.addOption("opt3", new Boolean(true));

		created.delete();
		try {
			created.get();
			fail("Able to retrieve resource that should have been deleted.");
		} catch (InvalidRepresentationFault df) {
			assertTrue(true);
		} catch (FaultException fex) {
			assertTrue(true);
		}

	}

	public void testFragmentDelete() throws JAXBException, SOAPException,
			IOException, FaultException, DatatypeConfigurationException {
		String dest = ResourceTest.destUrl;
		String resource = ResourceTest.resourceUri;
		long timeoutInMilliseconds = ResourceTest.timeoutInMilliseconds;

		Document content = null;
		// now build Create XML body contents.
		String fName = "Delete";
		String lName = "Guy";
		String address = "Smoky Lane";
		UserType user = userFactory.createUserType();
		user.setLastname(lName);
		user.setFirstname(fName);
		user.setAddress(address);
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
		user.setAge(16);
		content = Management.newDocument();

		content = loadUserTypeToDocument(content, user, binding);

		// Resource created = ResourceFactory.create(dest, resource,
		TransferableResource created = (TransferableResource) ResourceFactory
				.create(dest, resource, timeoutInMilliseconds, content,
						ResourceFactory.LATEST);
		// pull out Epr and parse for selectors
		// EndpointReferenceType retrievedEpr = created.getResourceEpr();

		SelectorSetType selectorSet = null;
		selectorSet = created.getSelectorSet();
		assertNotNull("SelectorSet is null.", selectorSet);

		String xPathReq = "//*[local-name()='age']";
		created.delete(xPathReq, null);

		created.addOption("opt1", "value1");
		created.addOption("opt2", new Integer(7));
		created.addOption("opt3", new Boolean(true), true);

		try {
			created.get();
			assertTrue("Get succeeded after delete.", true);
		} catch (InvalidRepresentationFault df) {
			fail("Unable to retrieve resource that should NOT have been deleted.");
		} catch (FaultException fex) {
			fail("Unable to retrieve resource that should NOT have been deleted.");
		}

	}

	public void testPut() throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException {
		// Now create an instance and test it's contents
		String dest = ResourceTest.destUrl;
		String resource = ResourceTest.resourceUri;
		long timeoutInMilliseconds = ResourceTest.timeoutInMilliseconds;
		Document content = null;

		// now build Create XML body contents.
		String fName = "Joe";
		String lName = "Finkle";
		String address = "6000 Irwin Drive";
		String addressModified = address + "Modified";

		UserType user = userFactory.createUserType();
		user.setLastname(lName);
		user.setFirstname(fName);
		user.setAddress(address);
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
		user.setAge(16);
		content = Management.newDocument();

		JAXBElement<UserType> userElement = userFactory.createUser(user);
		try {
			binding.marshal(userElement, content);
		} catch (JAXBException e1) {
			fail(e1.getMessage());
		}
		Resource created = ResourceFactory.create(dest, resource,
				timeoutInMilliseconds, content, ResourceFactory.LATEST);

		created.addOption("opt1", "value1");
		created.addOption("opt2", new Integer(7));
		created.addOption("opt3", new Boolean(true));

		user.setAddress(addressModified);

		content = Management.newDocument();
		userElement = userFactory.createUser(user);
		binding.marshal(userElement, content);

		// SelectorSetType existingId = created.getSelectorSet();
		created.put(content);

		// DONE: write code to retrieve the payload from Resource
		ResourceState retrieved = created.get();

		assertNotNull("Retrieved resource is NULL.", retrieved);
		Document payLoad = retrieved.getDocument();

		Element node = payLoad.getDocumentElement();
		user = userFactory.createUserType();
		// Init the user by transfering the fields from
		Node userChildNode = node;
		JAXBElement ob = (JAXBElement) binding.unmarshal(userChildNode);
		user = (UserType) ob.getValue();

		assertEquals("Values not equal.", addressModified, user.getAddress());

		created.delete();
	}

	public void testFragmentPut2() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {
		// Now create an instance and test it's contents
		String dest = ResourceTest.destUrl;
		String resource = ResourceTest.resourceUri;
		long timeoutInMilliseconds = ResourceTest.timeoutInMilliseconds;
		Document content = null;

		// now build Create XML body contents.
		String fName = "Fragment";
		String lName = "Put2";
		String state = "NJ";
		String stateUpdated = "California";
		String zipUpdated = "94070";
		String address = "6000 Irwin Drive";
		// String addressModified = address + "FragmentPut";

		UserType user = userFactory.createUserType();
		user.setLastname(lName);
		user.setFirstname(fName);
		user.setAddress(address);
		user.setCity("Mount Laurel");
		user.setState(state);
		user.setZip("08054");
		user.setAge(16);
		content = Management.newDocument();

		JAXBElement<UserType> userElement = userFactory.createUser(user);
		try {
			binding.marshal(userElement, content);
		} catch (JAXBException e1) {
			fail(e1.getMessage());
		}
		// TransferableResource created =(TransferableResource)
		// ResourceFactory.create(dest, resource,
		TransferableResource created = (TransferableResource) ResourceFactory
				.create(dest, resource, timeoutInMilliseconds, content,
						ResourceFactory.LATEST);

		// DONE: create fragment request to update state field only.
		final String fragmentRequest = "/user:user/user:state|/user:user/user:zip";
		final HashMap<String, String> namespaces = new HashMap<String, String>(
				1);
		namespaces.put("user", USER_NS);
		content = Management.newDocument();
		final DocumentFragment fragment = content.createDocumentFragment();
		// Insert the root element node
		final Element element = content.createElementNS(
				"http://examples.hp.com/ws/wsman/user", "user:state");
		element.setTextContent(stateUpdated);
		fragment.appendChild(element);

		final Element element2 = content.createElementNS(
				"http://examples.hp.com/ws/wsman/user", "user:zip");
		element2.setTextContent(zipUpdated);
		fragment.appendChild(element2);

		created.put(fragment, fragmentRequest, namespaces, XPath.NS_URI);

		ResourceState retrieved = created.get();

		assertNotNull("Retrieved resource is NULL.", retrieved);
		Document payLoad = retrieved.getDocument();

		Element node = payLoad.getDocumentElement();
		user = userFactory.createUserType();
		// Init the user by transfering the fields from
		Node userChildNode = node;
		JAXBElement ob = (JAXBElement) binding.unmarshal(userChildNode);
		user = (UserType) ob.getValue();

		assertEquals("Values not equal.", stateUpdated, user.getState());
		assertEquals("Values not equal.", zipUpdated, user.getZip());

	}

	public void testFragmentPut() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {
		// Now create an instance and test it's contents
		String dest = ResourceTest.destUrl;
		String resource = ResourceTest.resourceUri;
		long timeoutInMilliseconds = ResourceTest.timeoutInMilliseconds;
		Document content = null;

		// now build Create XML body contents.
		String fName = "Fragment";
		String lName = "Put";
		String state = "NJ";
		String stateUpdated = "New Jersey";
		String address = "6000 Irwin Drive";
		// String addressModified = address + "FragmentPut";

		UserType user = userFactory.createUserType();
		user.setLastname(lName);
		user.setFirstname(fName);
		user.setAddress(address);
		user.setCity("Mount Laurel");
		user.setState(state);
		user.setZip("08054");
		user.setAge(16);
		content = Management.newDocument();

		JAXBElement<UserType> userElement = userFactory.createUser(user);
		try {
			binding.marshal(userElement, content);
		} catch (JAXBException e1) {
			fail(e1.getMessage());
		}
		// TransferableResource created =(TransferableResource)
		// ResourceFactory.create(dest, resource,
		TransferableResource created = (TransferableResource) ResourceFactory
				.create(dest, resource, timeoutInMilliseconds, content,
						ResourceFactory.LATEST);

		// DONE: create fragment request to update state field only.
		String fragmentRequest = "//*[local-name()='state']";
		content = Management.newDocument();
		// Insert the root element node
		Element element = content.createElementNS(
				"http://examples.hp.com/ws/wsman/user", "user:state");
		element.setTextContent(stateUpdated);
		content.appendChild(element);

		created.addOption("opt1", "value1");
		created.addOption("opt2", new Integer(7));
		created.addOption("opt3", new Boolean(true));

		created.put(content, fragmentRequest, XPath.NS_URI);

		ResourceState retrieved = created.get();

		assertNotNull("Retrieved resource is NULL.", retrieved);
		Document payLoad = retrieved.getDocument();

		Element node = payLoad.getDocumentElement();
		user = userFactory.createUserType();
		// Init the user by transfering the fields from
		Node userChildNode = node;
		JAXBElement ob = (JAXBElement) binding.unmarshal(userChildNode);
		user = (UserType) ob.getValue();

		assertEquals("Values not equal.", stateUpdated, user.getState());

	}

	public void testFind() throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException {

		// test that Find works to retrieve the Enumeration instance.
		HashMap<String, String> selectors = null;
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceTest.destUrl, ResourceTest.resourceUri,
				ResourceTest.timeoutInMilliseconds, selectors);
		assertEquals("Expected one resource.", 1, enumerableResources.length);
		assertTrue("Expected EnumerableResource object.",
				(enumerableResources[0] instanceof EnumerableResource));
	}

	public void testEnumeration() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {

		// define enumeration handler url
		String resourceUri = "wsman:auth/userenum";

		SelectorSetType selectors = null;
		// test that Find works to retrieve the Enumeration instance.
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceTest.destUrl, resourceUri,
				ResourceTest.timeoutInMilliseconds, selectors);

		assertEquals("Expected one resource.", 1, enumerableResources.length);
		Resource retrieved = enumerableResources[0];
		assertTrue(retrieved instanceof EnumerableResource);

		retrieved.addOption("opt1", "value1");
		retrieved.addOption("opt2", new Integer(7));
		retrieved.addOption("opt3", new Boolean(true));

		// Build the filters
		final String testName = "James";// See users.store for more valid search
										// values
		final String xpathFilter = "/user:user[user:firstname='" + testName
				+ "']";
		// String xpathFilter = "/user:user/user:firstname/text()";
		final Map<String, String> namespaces = new HashMap<String, String>(1);
		namespaces.put("user", USER_NS);

		long timeout = 1000000;

		// Add custom user param
		Document userDoc = Management.newDocument();

		Element userChildNode = userDoc.createElementNS("http://my.schema",
				"me:MyParam");
		userChildNode.setTextContent("888");

		Element userChildNode2 = userDoc.createElementNS("http://my.schema",
				"me:MyOtherParam");
		userChildNode2.setTextContent("9999");

		// Retrieve the Enumeration context.
		Object[] params = new Object[] { userChildNode, userChildNode2 };
		EnumerationCtx enumContext = retrieved.enumerate(xpathFilter,
				namespaces, XPath.NS_URI, false, false, false, false, timeout,
				0, params);
		assertNotNull("Enum context retrieval problem.", enumContext);
		assertTrue("Context id is empty.", (enumContext.getContext().trim()
				.length() > 0));

		// DONE: now test the pull mechanism
		int maxTime = 1000 * 60 * 15;
		int maxElements = 5;
		int maxChar = 0; // 20000; //random limit. NOT yet supported.

		ResourceState retrievedValues = retrieved.pull(enumContext, maxTime,
				maxElements, maxChar);
		// Navigate down to retrieve Items children
		// Document Children
		NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		// PullResponse node
		assertNotNull("No root node for PullResponse.", rootChildren);
		Node child = rootChildren.item(0);
		// Items node
		assertNotNull("No child node for PullResponse found.", child);
		// Check number of enumerated values returned.
		NodeList children = child.getChildNodes().item(1).getChildNodes();
		assertEquals("Incorrect number of elements returned!", maxElements,
				children.getLength());

		// DONE: iterate through to make sure that
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeName().indexOf("EnumerationContext") > -1) {
				// ignore
			} else {
				UserType user = null;
				JAXBElement ob = (JAXBElement) binding.unmarshal(node);
				user = (UserType) ob.getValue();
				assertTrue("Filter for user " + testName.trim() + " failed!",
						user.getFirstname().trim().equalsIgnoreCase(
								testName.trim()));
			}
		}

		// Execute another pull. We know the data set users.store so another
		// pull is valid.
		retrievedValues = retrieved.pull(enumContext, maxTime, maxElements,
				maxChar);
		assertNotNull("No pull results obtained.", retrievedValues);

		// Now do a find again to make sure that correct context is retrieved.
		Resource[] enumResources = ResourceFactory.find(ResourceTest.destUrl,
				resourceUri, ResourceTest.timeoutInMilliseconds, selectors);
		// Only resource retrieved should be the enumerable resource
		Resource retEnumRes = enumResources[0];
		retrievedValues = retEnumRes.pull(enumContext, maxTime, maxElements,
				maxChar);

		// Check the end of context
		assertFalse("EndOfContext should not be set to true.", retrieved
				.isEndOfSequence());

		// DONE: test release
		retrieved.release(enumContext);
		try {
			retrievedValues = retEnumRes.pull(enumContext, maxTime,
					maxElements, maxChar);
			fail("This context should have been destroyed.");
		} catch (FaultException ex) {
			// Do nothing, should fail.
		}
	}

	public void testDomEnumeration() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {

		// define enumeration handler url
		String resourceUri = "wsman:auth/userenum";

		SelectorSetType selectors = null;
		// test that Find works to retrieve the Enumeration instance.
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceTest.destUrl, resourceUri,
				ResourceTest.timeoutInMilliseconds, selectors);

		assertEquals("Expected one resource.", 1, enumerableResources.length);
		Resource retrieved = enumerableResources[0];
		assertTrue(retrieved instanceof EnumerableResource);

		retrieved.addOption("opt1", "value1");
		retrieved.addOption("opt2", new Integer(7));
		retrieved.addOption("opt3", new Boolean(true));

		Document userDoc = Management.newDocument();

		// Build the filters
		final String testName = "Simpson";// See users.store for more valid
											// search values
		Element filterElem = userDoc.createElement("Expression");
		filterElem.setTextContent(testName);
		final Map<String, String> namespaces = new HashMap<String, String>(1);
		namespaces.put("user", USER_NS);

		long timeout = 1000000;

		// Add custom user param

		Element userChildNode = userDoc.createElementNS("http://my.schema",
				"me:MyParam");
		userChildNode.setTextContent("888");

		Element userChildNode2 = userDoc.createElementNS("http://my.schema",
				"me:MyOtherParam");
		userChildNode2.setTextContent("9999");

		String dialect = "http://examples.hp.com/ws/wsman/user/filter/custom";
		// Retrieve the Enumeration context.
		EnumerationCtx enumContext = retrieved.enumerate(filterElem,
				namespaces, dialect, false, false, false, false, timeout, 0,
				new Object[] { userChildNode, userChildNode2 });
		assertNotNull("Enum context retrieval problem.", enumContext);
		assertTrue("Context id is empty.", (enumContext.getContext().trim()
				.length() > 0));

		// DONE: now test the pull mechanism
		int maxTime = 1000 * 60 * 15;
		int maxElements = 5;
		int maxChar = 0; // 20000; //random limit. NOT yet supported.

		ResourceState retrievedValues = retrieved.pull(enumContext, maxTime,
				maxElements, maxChar);
		// Navigate down to retrieve Items children
		// Document Children
		NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		// PullResponse node
		assertNotNull("No root node for PullResponse.", rootChildren);
		Node child = rootChildren.item(0);
		// Items node
		assertNotNull("No child node for PullResponse found.", child);
		// Check number of enumerated values returned.
		NodeList children = child.getChildNodes().item(1).getChildNodes();
		assertEquals("Incorrect number of elements returned!", maxElements,
				children.getLength());

		// DONE: iterate through to make sure that
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeName().indexOf("EnumerationContext") > -1) {
				// ignore
			} else {
				UserType user = null;
				JAXBElement ob = (JAXBElement) binding.unmarshal(node);
				user = (UserType) ob.getValue();
				assertTrue("Filter for user " + testName.trim() + " failed!",
						user.getLastname().trim().equalsIgnoreCase(
								testName.trim()));
			}
		}

		// Check the end of context
		assertFalse("EndOfContext should not be set to true.", retrieved
				.isEndOfSequence());

		// DONE: test release
		retrieved.release(enumContext);

	}

	public void testEnumerationWithCount() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {

		// define enumeration handler url
		String resourceUri = "wsman:auth/userenum";

		SelectorSetType selectors = null;
		// test that Find works to retrieve the Enumeration instance.
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceTest.destUrl, resourceUri,
				ResourceTest.timeoutInMilliseconds, selectors);

		assertEquals("Expected one resource.", 1, enumerableResources.length);
		Resource retrieved = enumerableResources[0];
		assertTrue(retrieved instanceof EnumerableResource);

		retrieved.addOption("opt1", "value1");
		retrieved.addOption("opt2", new Integer(7));
		retrieved.addOption("opt3", new Boolean(true));

		// Build the filters
		final String testName = "James";// See users.store for more valid search
										// values
		final String xpathFilter = "/user:user[user:firstname='" + testName
				+ "']";
		// String xpathFilter = "/user:user/user:firstname/text()";
		final Map<String, String> namespaces = new HashMap<String, String>(1);
		namespaces.put("user", USER_NS);

		long timeout = 1000000;

		// Add custom user param
		Document userDoc = Management.newDocument();

		Element userChildNode = userDoc.createElementNS("http://my.schema",
				"me:MyParam");
		userChildNode.setTextContent("888");

		Element userChildNode2 = userDoc.createElementNS("http://my.schema",
				"me:MyOtherParam");
		userChildNode2.setTextContent("9999");

		// Retrieve the Enumeration context.
		EnumerationCtx enumContext = retrieved.enumerate(xpathFilter,
				namespaces, XPath.NS_URI, false, false, true, false, timeout,
				0, new Object[] { userChildNode, userChildNode2 });
		assertNotNull("Enum context retrieval problem.", enumContext);
		assertTrue("Context id is empty.", (enumContext.getContext().trim()
				.length() > 0));

		assertTrue("Item count null", retrieved.getItemCount() != null);
		assertTrue("Item count <= 0", retrieved.getItemCount().longValue() > 0);

		// DONE: now test the pull mechanism
		int maxTime = 1000 * 60 * 15;
		int maxElements = 5;
		int maxChar = 0; // 20000; //random limit. NOT yet supported.

		ResourceState retrievedValues = retrieved.pull(enumContext, maxTime,
				maxElements, maxChar);
		// Navigate down to retrieve Items children
		// Document Children
		NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		// PullResponse node
		assertNotNull("No root node for PullResponse.", rootChildren);
		Node child = rootChildren.item(0);
		// Items node
		assertNotNull("No child node for PullResponse found.", child);
		// Check number of enumerated values returned.
		NodeList children = child.getChildNodes().item(1).getChildNodes();
		assertEquals("Incorrect number of elements returned!", maxElements,
				children.getLength());

		assertTrue("Item count <= 0", retrieved.getItemCount() > 0);
		// DONE: iterate through to make sure that
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeName().indexOf("EnumerationContext") > -1) {
				// ignore
			} else {
				UserType user = null;
				JAXBElement ob = (JAXBElement) binding.unmarshal(node);
				user = (UserType) ob.getValue();
				assertTrue(user.getFirstname().trim().equalsIgnoreCase(
						testName.trim()));
			}
		}

		// DONE: release
		retrieved.release(enumContext);

	}

	public void testFragmentEnumeration() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {

		// define enumeration handler url
		String resourceUri = "wsman:auth/userenum";

		SelectorSetType selectors = null;
		// test that Find works to retrieve the Enumeration instance.
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceTest.destUrl, resourceUri,
				ResourceTest.timeoutInMilliseconds, selectors);

		assertEquals("Expected one resource.", 1, enumerableResources.length);
		Resource retrieved = enumerableResources[0];
		assertTrue(retrieved instanceof EnumerableResource);

		// Build the filters
		final String testName = "James";// See users.store for more valid search
										// values
		final String xpathFilter = "/user:user[user:firstname='" + testName
				+ "']/user:age";
		final Map<String, String> namespaces = new HashMap<String, String>(1);
		namespaces.put("user", USER_NS);

		// Retrieve the Enumeration context.
		EnumerationCtx enumContext = retrieved.enumerate(xpathFilter,
				namespaces, XPath.NS_URI, false, false, true, false, 0, 0);
		assertNotNull("Enum context retrieval problem.", enumContext);
		assertTrue("Context id is empty.", (enumContext.getContext().trim()
				.length() > 0));

		// DONE: now test the pull mechanism
		int maxTime = 1000 * 60 * 15;
		int maxElements = 10;
		int maxChar = 0; // 20000; //random limit. NOT yet supported.

		EnumerationResourceState retrievedValues = (EnumerationResourceState) retrieved
				.pull(enumContext, maxTime, maxElements, maxChar);
		// Navigate down to retrieve Items children
		// Document Children
		NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		// PullResponse node
		assertNotNull("No root node for PullResponse.", rootChildren);
		Node child = rootChildren.item(0);
		// Items node
		assertNotNull("No child node for PullResponse found.", child);
		// Check number of enumerated values returned.
		NodeList children = child.getChildNodes().item(1).getChildNodes();
		assertEquals("Incorrect number of elements returned!", maxElements,
				children.getLength());

		List<Node> enumList = retrievedValues.getEnumerationItems();
		// DONE: iterate through to make sure that only XML Fragment nodes are
		// returned
		for (int i = 0; i < enumList.size(); i++) {
			Node node = enumList.get(i);
			if (node.getNodeName().indexOf("EnumerationContext") > -1) {
				// ignore
			} else {
				assertTrue(node.getNodeName().indexOf("XmlFragment") > -1);

				// Now make sure that the right field for the XPATH expression
				// is returned
				assertTrue(node.getFirstChild().getNodeName().indexOf("age") > -1);
			}
		}

		// DONE: test release
		retrieved.release(enumContext);
	}

	public void testEnumerationResourceState() throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		// define enumeration handler url
		String resourceUri = "wsman:auth/userenum";

		SelectorSetType selectors = null;
		// test that Find works to retrieve the Enumeration instance.
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceTest.destUrl, resourceUri,
				ResourceTest.timeoutInMilliseconds, selectors);

		assertEquals("Expected one resource.", 1, enumerableResources.length);
		Resource retrieved = enumerableResources[0];
		assertTrue(retrieved instanceof EnumerableResource);

		// Build the filters
		final String testName = "James";// See users.store for more valid search
										// values
		final String xpathFilter = "/user:user[user:firstname='" + testName
				+ "']";
		// String xpathFilter = "/user:user/user:firstname/text()";
		final Map<String, String> namespaces = new HashMap<String, String>(1);
		namespaces.put("user", USER_NS);

		long timeout = 1000000;

		// Retrieve the Enumeration context.
		EnumerationCtx enumContext = retrieved
				.enumerate(xpathFilter, namespaces, XPath.NS_URI, false, false,
						true, false, timeout, 0);
		assertNotNull("Enum context retrieval problem.", enumContext);
		assertTrue("Context id is empty.", (enumContext.getContext().trim()
				.length() > 0));

		// DONE: now test the pull mechanism
		int maxTime = 1000 * 60 * 15;
		int maxElements = 5;
		int maxChar = 0; // 20000; //random limit. NOT yet supported.

		EnumerationResourceState retrievedValues = (EnumerationResourceState) retrieved
				.pull(enumContext, maxTime, maxElements, maxChar);
		// Navigate down to retrieve Items children
		// Document Children
		NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		// PullResponse node
		assertNotNull("No root node for PullResponse.", rootChildren);
		Node child = rootChildren.item(0);

		// Items node
		assertNotNull("No child node for PullResponse found.", child);
		// Check number of enumerated values returned.
		List<Node> children = retrievedValues.getEnumerationItems();
		assertEquals("Incorrect number of elements returned!", maxElements,
				children.size());

		// DONE: iterate through to make sure that we retrieved the correct item
		// type and value
		for (int i = 0; i < children.size(); i++) {
			Node node = children.get(i);
			if (node.getNodeName().indexOf("EnumerationContext") > -1) {
				// ignore
			} else {
				UserType user = null;
				JAXBElement ob = (JAXBElement) binding.unmarshal(node);
				user = (UserType) ob.getValue();
				assertTrue(user.getFirstname().trim().equalsIgnoreCase(
						testName.trim()));
			}
		}

		// DONE: test release
		retrieved.release(enumContext);

	}

	public void testOptimizedEnumeration() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {

		// define enumeration handler url
		String resourceUri = "wsman:auth/userenum";

		SelectorSetType selectors = null;
		// test that Find works to retrieve the Enumeration instance.
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceTest.destUrl, resourceUri,
				ResourceTest.timeoutInMilliseconds, selectors);

		assertEquals("Expected one resource.", 1, enumerableResources.length);
		Resource retrieved = enumerableResources[0];
		assertTrue(retrieved instanceof EnumerableResource);

		// Build the filters
		final String testName = "James";// See users.store for more valid search
										// values
		final String xpathFilter = "/user:user[user:firstname='" + testName
				+ "']";
		final Map<String, String> namespaces = new HashMap<String, String>(1);
		namespaces.put("user", USER_NS);

		// now test the pull mechanism
		int maxTime = 1000 * 60 * 15;
		int maxElements = 10;
		int maxEnvelopeSize = 0; // 20000; //random limit. NOT yet supported.

		// Retrieve the Enumeration context (optimized enumerate).
		EnumerationCtx enumContext = retrieved.enumerate(xpathFilter,
				namespaces, XPath.NS_URI, false, false, true, true, maxTime,
				maxElements);
		assertNotNull("Enum context retrieval problem.", enumContext);
		assertTrue("Context id is empty.", (enumContext.getContext().trim()
				.length() > 0));

		List<EnumerationItem> enumItems = retrieved.getEnumerationItems();
		assertNotNull("No items returned in optimized enumeration", enumItems);
		assertTrue("Wrong number of optimized enum items returned", enumItems
				.size() == maxElements);

		// iterate through to make sure that the correct fragment nodes are
		// returned
		for (int i = 0; i < enumItems.size(); i++) {
			EnumerationItem node = enumItems.get(i);
			JAXBElement item = (JAXBElement)node.getItem();
			assertTrue(item != null);
			UserType user = (UserType)item.getValue();
			assertTrue(user.getFirstname().trim().equalsIgnoreCase(
					testName.trim()));
		}

		while (retrieved.isEndOfSequence() == false) {
			enumItems = retrieved.pullItems(enumContext, maxTime,
					maxElements, maxEnvelopeSize, true);
			assertNotNull("No items returned in optimized enumeration",
					enumItems);
			assertTrue("Wrong number of optimized enum items returned",
					(0 < enumItems.size()) && (enumItems.size() <= maxElements));
			for (int i = 0; i < enumItems.size(); i++) {
				EnumerationItem node = enumItems.get(i);
				JAXBElement item = (JAXBElement) node.getItem();
				assertTrue(item != null);
				UserType user = (UserType)item.getValue();
				assertTrue(user.getFirstname().trim().equalsIgnoreCase(
						testName.trim()));
			}
		}
	}

	public void testFragmentOptimizedEnumeration() throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		// define enumeration handler url
		String resourceUri = "wsman:auth/userenum";

		SelectorSetType selectors = null;
		// test that Find works to retrieve the Enumeration instance.
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceTest.destUrl, resourceUri,
				ResourceTest.timeoutInMilliseconds, selectors);

		assertEquals("Expected one resource.", 1, enumerableResources.length);
		Resource retrieved = enumerableResources[0];
		assertTrue(retrieved instanceof EnumerableResource);

		// Build the filters
		final String testName = "James";// See users.store for more valid search
										// values
		final String xpathFilter = "/user:user[user:firstname='" + testName
				+ "']/user:age";
		final Map<String, String> namespaces = new HashMap<String, String>(1);
		namespaces.put("user", USER_NS);

		// now test the pull mechanism
		int maxTime = 1000 * 60 * 15;
		int maxElements = 10;
		int maxChar = 0; // 20000; //random limit. NOT yet supported.

		// Retrieve the Enumeration context.
		EnumerationCtx enumContext = retrieved.enumerate(xpathFilter,
				namespaces, XPath.NS_URI, false, false, true, true, 0,
				maxElements);
		// retrieved.enumerate(xpathFilter,namespaces,XPath.NS_URI,false,false,
		// false,
		// xPathReq, null, true, maxElements);

		assertNotNull("Enum context retrieval problem.", enumContext);
		assertTrue("Context id is empty.", (enumContext.getContext().trim()
				.length() > 0));

		List<EnumerationItem> enumItems = retrieved.getEnumerationItems();

		assertNotNull("No items returned in optimized enumeration", enumItems);
		assertTrue("Wrong number of optimized enum items returned", enumItems
				.size() == maxElements);

		// iterate through to make sure that the correct fragment nodes are
		// returned
		for (int i = 0; i < enumItems.size(); i++) {
			EnumerationItem node = enumItems.get(i);
			Object item = node.getItem();
			assertFalse(item == null);
			assertTrue(item instanceof JAXBElement);
			JAXBElement fragment = (JAXBElement) item;
			assertTrue(fragment.getDeclaredType().equals(MixedDataType.class));
			assertTrue(fragment.getName().equals(
					TransferExtensions.XML_FRAGMENT));
			List<Object> fragments = ((MixedDataType)fragment.getValue()).getContent();
			for (int j = 0; j < fragments.size(); j++) {
				Object obj = fragments.get(j);
				if (obj instanceof Element) {
					assertTrue(((Element) obj).getNodeName().indexOf("age") > -1);
				}
			}
		}

		EnumerationResourceState retrievedValues = (EnumerationResourceState) retrieved
				.pull(enumContext, maxTime, maxElements, maxChar);
		// Navigate down to retrieve Items children
		// Document Children
		NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		// PullResponse node
		assertNotNull("No root node for PullResponse.", rootChildren);
		Node child = rootChildren.item(0);
		// Items node
		assertNotNull("No child node for PullResponse found.", child);
		// Check number of enumerated values returned.
		NodeList children = child.getChildNodes().item(1).getChildNodes();
		assertEquals("Incorrect number of elements returned!", maxElements,
				children.getLength());

		List<Node> enumList = retrievedValues.getEnumerationItems();
		// iterate through to make sure that only XML Fragment nodes are
		// returned
		for (int i = 0; i < enumList.size(); i++) {
			Node node = enumList.get(i);
			if (node.getNodeName().indexOf("EnumerationContext") > -1) {
				// ignore
			} else {
				// Make sure we have the XmlFragment
				assertTrue(node.getNodeName().indexOf("XmlFragment") > -1);
				// Now make sure at least one field was returned
				assertTrue(node.getFirstChild() != null);
				// Now make sure that the right field for the XPATH expression
				// is returned
				assertTrue(node.getFirstChild().getNodeName().indexOf("age") > -1);
			}
		}

		// DONE: test release
		retrieved.release(enumContext);
	}

	public static String xmlToString(Node node) {
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Convenience method that loads a given UserType into Document.
	 */
	public static Document loadUserTypeToDocument(Document content,
			UserType user, XmlBinding binding) throws JAXBException {

		content = Management.newDocument();
		JAXBElement<UserType> userElement = userFactory.createUser(user);
		binding.marshal(userElement, content);

		return content;
	}

	// Build a UserType instance from the Document passed in.
	public static UserType loadUserTypeFromDocument(UserType user,
			Document content, ResourceState retrieved, XmlBinding binding)
			throws JAXBException {

		user = userFactory.createUserType();
		Document payLoad = retrieved.getDocument();
		Element node = payLoad.getDocumentElement();

		Node userChildNode = node;
		JAXBElement ob = (JAXBElement) binding.unmarshal(userChildNode);
		user = (UserType) ob.getValue();

		return user;
	}

	public void testEventingSubscriptionManager() throws JAXBException,
			SOAPException, DatatypeConfigurationException, IOException,
			ParserConfigurationException, SAXException {

		/*
		 * Test the default instance of the reference implementation of the
		 * subscription manager.
		 */
		// Build the Optimized Enumeration request pieces
		String evtSubManDestination = ManagementMessageValues.WSMAN_DESTINATION;
		String evtSubManResourceUri = eventsubman_Handler.RESOURCE_URI;

		// Configure enumeration settings
		EnumerationMessageValues enuSettings = EnumerationMessageValues
				.newInstance();
		enuSettings.setRequestForOptimizedEnumeration(true);
		// enuSettings.setTo(evtSubManDestination);
		// enuSettings.setResourceUri(evtSubManResourceUri);
		// Build the optimized enumeration request message pieces
		Enumeration optimizeEnumRequestForExisingEventSources = EnumerationUtility
				.buildMessage(null, enuSettings);

		// Configure management settings
		ManagementMessageValues manSettings = ManagementMessageValues
				.newInstance();
		// addressing details
		manSettings.setTo(evtSubManDestination);
		manSettings.setResourceUri(evtSubManResourceUri);
		// Now complete message construction....
		Management wsmanRequest = ManagementUtility.buildMessage(
				optimizeEnumRequestForExisingEventSources, manSettings);
		// Management wsmanRequest = ManagementUtility.buildMessage(
		// null,
		// enuSettings);

		// Send the request.
		// System.out.println("reqForExistingSrces:"+wsmanRequest.toString());
		Addressing response = HttpClient.sendRequest(wsmanRequest);
		if (response.getBody().hasFault()) {
			response.prettyPrint(System.err);
			fail(response.getBody().getFault().getFaultString());
		}

		// Translate the OptimizedEnumeration results to List<EnumerationItem>
		Management mResp = new Management(response);
		// System.out.println("mResp:"+mResp.toString());

		List<EnumerationItem> state = EnumerationUtility
				.extractEnumeratedValues(mResp);
		assertEquals("EventSources count not correct.", 2, state.size());

		// ######## EXERCISE STRAIGHT/UNFILTERED ENUMERATION ############
		// Programmatically locate the required eventSource instance.
		// translate EnumerationItem list into List<Management>
		List<Management> eventSrces = AnnotationProcessor
				.extractMetaDataFromEnumerationMessage(state);

		// programmatically locate eventsource Management metadata information
		Management identifiedEvtSrc = null;
		for (Management inst : eventSrces) {
			SOAPElement metaDataHeader = ManagementUtility.locateHeader(inst
					.getHeaders(), AnnotationProcessor.RESOURCE_META_DATA_UID);
			String metaDataUid = metaDataHeader.getTextContent();
			if ((metaDataUid != null)
					&& (eventcreator_Handler.UID.equals(metaDataUid))) {
				identifiedEvtSrc = inst;
			}
		}
		assertNotNull("Correct eventsource not located.", identifiedEvtSrc);

		// ######## EXERCISE FILTERED ENUMERATION ############
		// Test filtered enumeration using default subscrip Man.
		String filter = "env:Envelope/env:Header/wsmeta:ResourceMetaDataUID/text()='"
				+ eventcreator_Handler.UID + "'";
		enuSettings.setFilter(filter);
		enuSettings.setFilterDialect(XPath.NS_URI);

		Enumeration optimizeEnumRequestFilteredEventSources = EnumerationUtility
				.buildMessage(null, enuSettings);
		Management filWsmanRequest = ManagementUtility.buildMessage(
				optimizeEnumRequestFilteredEventSources, manSettings);

		// Send the request.
		Addressing response2 = HttpClient.sendRequest(filWsmanRequest);
		if (response2.getBody().hasFault()) {
			response2.prettyPrint(System.err);
			fail(response2.getBody().getFault().getFaultString());
		}

		// Translate the OptimizedEnumeration results to List<EnumerationItem>
		Management mFilResp = new Management(response2);
		List<EnumerationItem> state2 = EnumerationUtility
				.extractEnumeratedValues(mFilResp);
		assertEquals("EventSources count not correct.", 1, state2.size());

		// translate EnumerationItem list into List<Management>
		List<Management> eventSrcesLocated = AnnotationProcessor
				.extractMetaDataFromEnumerationMessage(state2);
		assertNotNull("No eventSources were located.", eventSrcesLocated);
		assertEquals("Event", 1, eventSrcesLocated.size());
		Management isolated = eventSrcesLocated.get(0);
		SOAPElement metaDataHeader = ManagementUtility.locateHeader(isolated
				.getHeaders(), AnnotationProcessor.RESOURCE_META_DATA_UID);
		String metaDataUid = metaDataHeader.getTextContent();
		assertEquals(eventcreator_Handler.UID, metaDataUid);

		// TODO: Make sure that the following tasks can be dynamically done... a
		// bit much for now
		// Now create a new EventSource and add it to the MetaData list.
		// Verify that the metaData EventSource list has been incremented by
		// one.
		// Verify that the metaData instance values are correct
		// Remove the metadata EventSource added and that list count is correct
		// again.

	}

	public void testEventSource() throws SOAPException, JAXBException,
			DatatypeConfigurationException, IOException,
			ParserConfigurationException, SAXException {
		// Use the eventSource implementation that's already defined in
		// eventsubman
		// Get the metadata and the Management details for the eventsource
		Management eventSource = AnnotationProcessor
				.findAnnotatedResourceByUID(eventcreator_Handler.UID, destUrl);
		assertNotNull("Unable to locate the eventSource resource.", eventSource);
		// ####
		Management init = MetadataUtility
				.buildMessage(eventSource, null, false);
		// add the INIITIALIZE Action request
		init.setAction(Metadata.INITIALIZE_ACTION_URI);
		HttpClient.sendRequest(init);
		// System.out.println("InitRes:"+initResp);
		// if (response2.getBody().hasFault()) {
		// response2.prettyPrint(System.err);
		// fail(response2.getBody().getFault().getFaultString());
		// }
		// ####

		String eventSinkDestination = "http://localhost:8080/wsman/eventsink";

		// Send a subscribe request to the eventSource
		EventingMessageValues evset = EventingMessageValues.newInstance();
		// populate eventing message values.
		evset.setEventSinkDestination("http://localhost:8080/wsman/eventsink");
		// generate a potential event sink ID value.
		QName eprParameter = EventingMessageValues.EVENTING_COMMUNICATION_CONTEXT_ID;
		String paramValue = evset.getEventSinkUidScheme() + UUID.randomUUID();
		evset.setEventSinkReferenceParameterType(Management
				.createReferenceParametersType(eprParameter, paramValue));

		ReferenceParametersType refParam = Management
				.createReferenceParametersType(eprParameter, paramValue);
		EndpointReferenceType notifyTo = Management.createEndpointReference(
				eventSinkDestination, null, refParam, null, null);
		evset.setNotifyTo(notifyTo);

		Eventing ev = EventingUtility.buildMessage(null, evset);
		Management subscribe = ManagementUtility.buildMessage(eventSource
		// ,ev,true);
				, ev, false);
		subscribe.setAction(Eventing.SUBSCRIBE_ACTION_URI);
		// subscribe = AnnotationProcessor.stripMetadataContent(subscribe, false);

		// System.out.println("SubReq:"+subscribe);
		Addressing response2 = HttpClient.sendRequest(subscribe);
		// System.out.println("SubScrRes:"+response2);
		if (response2.getBody().hasFault()) {
			response2.prettyPrint(System.err);
			fail(response2.getBody().getFault().getFaultString());
		}
		// test that return action is correct
		// assertEquals("Correct action was not located.",
		// Eventing.SUBSCRIBE_RESPONSE_URI, response2.getAction());

		// Store away reference to SubscriptionManager
		// Create an event on the event source
		// Wait for 10 seconds for the notification to be sent.
		// If received, then pass else fail.
		// Submit unsubscribe reqeust to subscription manager
		// Send another event, wait 10 seconds. If event sent, then fail, else
		// pass.

		// EventingUtility.
		// Confirm that metadata for the same is also provided
		// Subscribe(Register this unit test as a simple listener)
		// EventingMessageValues evSet = EventingMessageValues.newInstance();
		// evSet.setEventSinkUid("http://localhost:8080/wsman/eventSink");
		// evSet.setEventSourceUid(eventcreator_Handler.UID);
		// evSet.setDefaultTimeout(1000*60*2);
		// Eventing subscribeMessage = EventingUtility.buildMessage(null,
		// evSet);
		//		
		// ManagementMessageValues manSet =
		// ManagementMessageValues.newInstance();
		// manSet.setTo("http://localhost:8080/wsman/");
		// manSet.setResourceUri("wsman:auth/eventcreator");
		// // manSet.setTimeout(1000*60*2);

		// Management subscribe =
		// ManagementUtility.buildMessage(subscribeMessage, manSet);
		// System.out.println("SUBSCRIBE REQ:"+subscribe.toString());
		//
		// //Addressing response = HttpClient.sendRequest(subscribe);
		// Addressing response = HttpClient.sendRequest(subscribe);
		// if (response.getBody().hasFault()) {
		// response.prettyPrint(System.err);
		// fail(response.getBody().getFault().getFaultString());
		// }
		//	      
		// Management subscribeResp = new Management(response);
		// System.out.println("SUBSCRIBE RES:"+subscribeResp.toString());

	}

	public void testEventSourceInitialize() throws Exception {
		// Use the eventSource implementation that's already defined in
		// eventsubman
		// Get the metadata and the Management details for the eventsource
		Management eventSource = AnnotationProcessor
				.findAnnotatedResourceByUID(eventcreator_Handler.UID, destUrl);
		assertNotNull("Unable to locate the eventSource resource.", eventSource);
		// System.out.println("@@@ EvtSource beforManBuldMsg:"+eventSource);
		// eventSource = ManagementUtility.buildMessage(eventSource, null,
		// true);
		eventSource = MetadataUtility.buildMessage(eventSource, null, false);
		// add the INIITIALIZE Action request
		eventSource.setAction(Metadata.INITIALIZE_ACTION_URI);

		// System.out.println("@@@ Initialize:"+eventSource);
		Addressing response = HttpClient.sendRequest(eventSource);
		// System.out.println("@@@ InitializeResp:"+response);
		if (response.getBody().hasFault()) {
			response.prettyPrint(System.err);
			fail(response.getBody().getFault().getFaultString());
		}

		// Check that initialize response was returned
		assertEquals("Did not have correct action response.",
				Metadata.INITIALIZE_RESPONSE_URI, response.getAction());

	}
	
//	public void testAnnot() throws InstantiationException, IllegalAccessException{
////WsManagementAddressDetailsAnnotation annot = WsManagementAddressDetailsAnnotation.class.newInstance();
//Object initArgs = "";
//WsManagementQNamedNodeWithValueAnnotation annot = null;
//
//class metAnnot implements WsManagementQNamedNodeWithValueAnnotation{
//
//	public String localpart() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public String namespaceURI() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public String nodeValue() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public String prefix() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public Class<? extends Annotation> annotationType() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//}
////	new WsManagementQNamedNodeWithValueAnnotation();
//System.out.println("loc:"+annot.localpart());
//System.out.println("URI:"+annot.namespaceURI());
//System.out.println("Val:"+annot.nodeValue());
//}

//public void testFullEventing(){
//String evtSinkDestination ="http://localhost:8080/traffic";	
//String evtSourceDestination =evtSinkDestination;	
//String evtSubManDestination =evtSinkDestination;
//String evtSinkResourceUri ="wsman:traffic/eventsink";
//String evtSourceResourceUri ="wsman:traffic/eventsource";
//String evtSubManResourceUri ="wsman:traffic/eventsubman";
//long subscriptionExpirationTime = 1000*60*5;//300000;
//String filterValue = "my/filter/expression";
////String evtSrcFilterValue = "/EventSource";
//String evtSrcFilterValue = "/"+EventingMessageValues.EVENT_SOURCE_NODE_NAME;
//Duration subscriptionDuration = null;  
//
//try{	
////Create an event subscription
// subscriptionDuration = DatatypeFactory.newInstance().newDuration(
//		 subscriptionExpirationTime);
// 
//Management mgt = constructSubscribeMessage(evtSinkDestination, 
//		evtSourceDestination, 
//		evtSinkResourceUri, 
//		evtSourceResourceUri,subscriptionDuration,filterValue
//		,Eventing.PUSH_DELIVERY_MODE);
//System.out.println("CS#SubscribeRequest:"+mgt.toString());
//
// //Now send the request to the EVENT SOURCE.
//final Addressing response = HttpClient.sendRequest(mgt);
//
////Check for fault during message generation/processing
//examineForFaults(response);
//
//System.out.println("CreateSubscribeRequestRespons:"+new Management(response).toString());	        
////determine if a subscription manager has been successfully retrieved/assigned
//Eventing reply = new Eventing(response);
//SubscribeResponse subscResponse = reply.getSubscribeResponse();
// assertNotNull("Unable to create SubscribeResponse object.",subscResponse);
//EndpointReferenceType subScripMan = subscResponse.getSubscriptionManager();
// assertNotNull("No subscription manager found!",subScripMan);
// assertNotNull("No address for subscription manager found!",
//		subScripMan.getAddress());
//
//    String retrievedEventSourceId = null;	
//    String retrievedEventSourceDescription = null;	
// 
////########## BEGIN: Build optimized Enumerate request Message to retrieve valid Event_Source ids
//org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType filterOption = 
//	createEnumerationFilterType(evtSrcFilterValue, XPath.NS_URI);
//
//EnumerationExtensions sourcesRequest = 
//createOptimizedEnumerateRequest(evtSubManDestination, 
//	  evtSubManResourceUri,
//	  EventingMessageValues.generateNewMessageId(),
//	  filterOption,
//	  (1000*60*5),
//	  5,
//	  true,
//	  EnumerationExtensions.Mode.EnumerateObjectAndEPR
//	  );
//
//System.out.println("))))))))))))))PULL-SRC-MESSG:"+sourcesRequest.toString());
//final Addressing pullResponse = HttpClient.sendRequest(sourcesRequest);
//System.out.println("PullMesgResponse:"+pullResponse.toString());	     
//examineForFaults(pullResponse);
//
////retrieve a valid EVENT SOURCE ID  
// Element[] pullElementList = null;
// pullElementList =extractItemsAsElementArray(new Enumeration(pullResponse));
//
////for this unit test we'll assume that it's the first and only one
//if(pullElementList.length>0){
//retrievedEventSourceId = pullElementList[0].getTextContent();
//retrievedEventSourceDescription = 
//	pullElementList[0].getAttribute(EventingMessageValues.EVENT_SOURCE_DESC_ATTR_NAME);
//}else{
//fail("Unable to retrieve necessary EventSource Id.");
//}
//
////contact the eventSource and create an event.
//EventMessageType event = eventFactory.createEventMessageType();
//String name = "Test Event Created";
//String type = Thread.State.NEW.name();
//String details = "This is a test Event created for testing.";
//XMLGregorianCalendar tstamp = getCurrentTimeAsGC();
// event.setName(name);
// event.setType(type);
// event.setDetails(details);
// event.setTimeStamp(tstamp);
//
////serialize the java entities to org.w3c.* components 
//Document newEventDocument = Management.newDocument();
//JAXBElement<EventMessageType> eventElement = eventFactory.createEvent(event);
//binding.marshal(eventElement, newEventDocument);
//
//System.out.println("~~~~~XMLToString:"+xmlToString(newEventDocument));
////insert the generic content into the wsman message
//Management man = 
//	generateNewEventRequestMessage(evtSourceDestination, 
//			evtSourceResourceUri, retrievedEventSourceId, 
//			newEventDocument);
//System.out.println("^^^^^New Event Creation Request:"+man.toString());
//
////Send the Create request to the Event Source
//final Addressing createEvtResponse = HttpClient.sendRequest(man);
//examineForFaults(createEvtResponse);
//
////Process/instantiate the Event Creation response if necessary.
//Transfer createEventResp = new Transfer(createEvtResponse);
//
//System.out.println("NEW EVT:"+createEventResp.toString());
//
////verify that the event was received in evt sink.
////  in this case just make a TRANSFER.GET request to the EventSink.
//Management getLatestEvent = new Management();
//  getLatestEvent.setAction(Transfer.GET_ACTION_URI);
//  getLatestEvent.setMessageId(EventMessageConstants.generateNewMessageId());
//  getLatestEvent.setReplyTo(Transfer.ANONYMOUS_ENDPOINT_URI);
//  getLatestEvent.setTo(evtSinkDestination+"/"+evtSinkResourceUri);
//  getLatestEvent.setResourceURI(evtSinkResourceUri);
//  
//System.out.println("GGGGGGGGGGGGG GETREQUEST latest evt to eventSink:");
//System.out.println(getLatestEvent.toString()+":GGGGGGGGGGGGGGG");
//
////Send the Get request to the Event Source
//final Addressing getLatestEvtResponse = HttpClient.sendRequest(getLatestEvent);
//
//EventMessageType retrievedEvent = null;
////Check for fault during message generation
//if (getLatestEvtResponse.getBody().hasFault()) {
//  SOAPFault fault = getLatestEvtResponse.getBody().getFault();
//  throw new FaultException(fault.getFaultString());
//}else{
//  Transfer newestGetResp = new Transfer(getLatestEvtResponse);
//System.out.println("GETRESPONSE latest evt from EventSink :"+newestGetResp.toString());
//		Node eventChildNode = null;
//	eventChildNode = newestGetResp.getBody().getFirstChild();
//	if(eventChildNode == null){
//		throw new IllegalArgumentException("Not able to retrive child element.");
//	}
//	JAXBElement<EventMessageType> ob = (JAXBElement<EventMessageType>)binding.unmarshal(eventChildNode);
//	retrievedEvent = (EventMessageType)ob.getValue();
//}
//
////test the received event value
//assertNotNull("Retrieve event is null.",retrievedEvent);
//assertEquals("Values not equal.",event.getDetails(), retrievedEvent.getDetails());
//assertEquals("Values not equal.",event.getName(), retrievedEvent.getName());
//assertEquals("Values not equal.",event.getTimeStamp(), retrievedEvent.getTimeStamp());
//assertEquals("Values not equal.",event.getType(), retrievedEvent.getType());
//
////DONE: test whether unsubscribe message works
//String evtSinksFilterValue ="/"+EventMessageConstants.EVENT_SINK_NODE_NAME;
//evtSinksFilterValue+="[@"+EventMessageConstants.EVENT_SOURCE_ID_ATTR_NAME+"='"+
//			retrievedEventSourceId+"']";
//org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType filterOption2 = 
//	createEnumerationFilterType(evtSinksFilterValue, 
//			XPath.NS_URI);
//
//EnumerationExtensions sinksRequest = 
//createOptimizedEnumerateRequest(evtSubManDestination, 
//	  evtSubManResourceUri,
//	  EventMessageConstants.generateNewMessageId(),
//	  filterOption2,
//	  (1000*60*5),
//	  5,
//	  true,
//	  EnumerationExtensions.Mode.EnumerateObjectAndEPR
//	  );
//
//System.out.println("))))))))))))))PULL-Sinks-MESG:"+sinksRequest.toString());
//Addressing sinkPullResponse = HttpClient.sendRequest(sinksRequest);
//
//System.out.println("SinkPullMesgResponse:"+sinkPullResponse.toString());	     
//examineForFaults(pullResponse);
//
////retrieve a valid EVENT SINK Resource  
// Element[] pullSinkElementList = null;
// pullSinkElementList =extractItemsAsElementArray(new Enumeration(sinkPullResponse));
//
////for this unit test we'll assume that it's the first and only one
//String retrievedEventSinkId ="";
//String locatedEventSourceId ="";
//if(pullSinkElementList.length>0){
//retrievedEventSinkId = pullSinkElementList[0].getTextContent();
//locatedEventSourceId = pullSinkElementList[0].getAttribute(
//		EventMessageConstants.EVENT_SOURCE_ID_ATTR_NAME);
//System.out.println("retSink:"+retrievedEventSinkId);
//System.out.println("retSourceId:"+locatedEventSourceId);
//System.out.println("pullSinkElementSize:"+pullSinkElementList.length);
//for (int i = 0; i < pullSinkElementList.length; i++) {
//	System.out.println("Element-"+i+":"+pullSinkElementList[i]);
//}
//assertEquals("EventSink details not correct.",2,pullSinkElementList.length);
//}else{
//fail("Unable to retrieve necessary EventSink Information.");
//}
//
//Management unsubSubsc = new Management();
//  unsubSubsc.setReplyTo(Transfer.ANONYMOUS_ENDPOINT_URI);
//  unsubSubsc.setTo(evtSubManDestination+"/"+evtSubManResourceUri);
//	  unsubSubsc.setResourceURI(evtSubManResourceUri);
//	  ReferencePropertiesType eventSourceIdentifier = 
//		  Addressing.createReferencePropertyType(EventMessageConstants.EVENTING_COMMUNICATION_CONTEXT_ID,
//				  locatedEventSourceId);
//	  unsubSubsc.removeChildren(man.getHeader(), EventMessageConstants.EVENTING_COMMUNICATION_CONTEXT_ID);
//	  unsubSubsc.addHeaders(eventSourceIdentifier);
//ReferencePropertiesType eventSinkIdentifier = 
//	Addressing.createReferencePropertyType(Eventing.IDENTIFIER,
//		retrievedEventSinkId);
//	 unsubSubsc.removeChildren(man.getHeader(), Eventing.IDENTIFIER);
//	 unsubSubsc.addHeaders(eventSinkIdentifier);
//	  
//
//final Eventing unsubscribeMesg = new Eventing(unsubSubsc);
//// now send an unsubscribe request using the identifier
//unsubscribeMesg.setAction(Eventing.UNSUBSCRIBE_ACTION_URI);
//unsubscribeMesg.setMessageId(EventMessageConstants.generateNewMessageId());
//unsubscribeMesg.setUnsubscribe();
//
//System.out.println("UnssubscribeRequest:"+new Management(unsubscribeMesg).toString());
//final Addressing unsubscribeResponseMesg = HttpClient.sendRequest(unsubscribeMesg);
//System.out.println("UnsubscribeResponse:"+unsubscribeResponseMesg.toString());	     
//examineForFaults(unsubscribeResponseMesg);
//
////attempt to retrieve valid EVENT SINK Resources 
//sinkPullResponse = HttpClient.sendRequest(sinksRequest);
//examineForFaults(sinkPullResponse);
//Element[] pullSinkElementList2 = null;
//pullSinkElementList2 =extractItemsAsElementArray(new Enumeration(sinkPullResponse));
//assertEquals("EventSink records should not exist.",0, pullSinkElementList2.length);
//
//}catch(Exception ex){
//	  System.out.println("Exception: "+ex.getMessage());
//	  ex.printStackTrace(System.out);
//	  fail(ex.getMessage());
//}
//}

///**
//* @param evtSubManDestination
//* @param evtSubManResourceUri
//* @param filterValue
//* @return
//* @throws SOAPException
//* @throws JAXBException
//* @throws DatatypeConfigurationException
//*/
//public EnumerationExtensions createOptimizedEnumerateRequest(
//	String destination, 
//	String resourceUri,
//	String messageId,
//	org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType filterOption,
//	long maxMessageTimeOption,
//	int maxMessageElementsOption,
//	boolean optimizeEnumerationOption,
//	EnumerationExtensions.Mode pullModeOption) throws 
//	SOAPException, 
//	JAXBException, DatatypeConfigurationException {
//Management getEventingSources = new Management();       
//  	  getEventingSources.setAction(Enumeration.ENUMERATE_ACTION_URI);
////	  	  getEventingSources.setMessageId(EventMessageConstants.generateNewMessageId());
//  	  getEventingSources.setMessageId(messageId);
//  	  getEventingSources.setReplyTo(Transfer.ANONYMOUS_ENDPOINT_URI);
////	  	  getEventingSources.setTo(evtSubManDestination+"/"+evtSubManResourceUri);
//  	  getEventingSources.setTo(destination+"/"+resourceUri);
////	  	  getEventingSources.setResourceURI(evtSubManResourceUri);
//  	  getEventingSources.setResourceURI(resourceUri);
//      EnumerationExtensions sourcesRequest = 
//    	 new EnumerationExtensions(getEventingSources);
////		 int maxTime =1000*60*3;
////		 int maxElements = 5;
//     final DatatypeFactory factory = DatatypeFactory.newInstance();
////	     Duration expiration = factory.newDuration(maxTime);
//     Duration expiration = factory.newDuration(maxMessageTimeOption);
//
////	        sourcesRequest.setEnumerate(null, expiration.toString(), filter, 
//      sourcesRequest.setEnumerate(null, expiration.toString(), filterOption, 
////	    		  EnumerationExtensions.Mode.EnumerateObjectAndEPR, true, maxElements);
////	    		  EnumerationExtensions.Mode.EnumerateObjectAndEPR, 
//    		  pullModeOption, 
//    		  optimizeEnumerationOption, maxMessageElementsOption);
//return sourcesRequest;
//}
//
///**Method attempts to extract each Item, from a pullResponse object
// * as an array of Elements.  For Items listed in duplicate(ObjectAndEpr), then 
// * this method attempts to locate each element and an associated EPR as well. 
// * An EPR is an open ended element and the Element[] array list may not be 
// * easy to evaluate. Use this method at your own discretion.  
// * @param pullResponse encapsulates the Enumeration response received.
// * @throws SOAPException
// * @throws JAXBException
// */
//public Element[] extractItemsAsElementArray(final Enumeration pullResponse) throws SOAPException, JAXBException {
//	if(pullResponse==null){
//		throw new IllegalArgumentException("Enumeration instance cannot be null.");
//	}
//	Element[] elementList = null;
//	Vector<Element> bag = new Vector<Element>();
//	EnumerateResponse er =  pullResponse.getEnumerateResponse();
//	PullResponse pr =  pullResponse.getPullResponse();
//	if(((er==null)&(pr==null))){
//		String msg="Unable to find an EnumeratResponse/PullResponse element in the message.";
//		throw new IllegalArgumentException(msg);
//	}
//	
//	List<Object> allAnys = null;
//	if(pr!=null){//Process a stereotypical PullResponse object
//	  ItemListType itemList = pr.getItems();
//	  allAnys =itemList.getAny();	
//	  for (Iterator iter = allAnys.iterator(); iter.hasNext();) {
//		Object element = (Object) iter.next();
//		Element elem = (Element) element;
//		bag.add(elem);
//	  }
//	}else{//Else process the stereotypical EnumerateResponse object
//		allAnys =er.getAny();	
//	
//	
////		ItemListType itemList = pr.getItems();
////		List<Object> allAnys = itemList.getAny();
////		  for (Iterator iter = allAnys.iterator(); iter.hasNext();) {
////			Object element = (Object) iter.next();
////			Element elem = (Element) element;
////			bag.add(elem);
////		  }
//	
//	for (Iterator iter = allAnys.iterator(); iter.hasNext();) {
//		Object element = (Object) iter.next();
////System.out.println("Element:"+element);
////ItemListType lst = null;
////	ItemListType lst = null;
//AnyListType lst1 = null;
//	//	JAXBElement<ItemListType> itemsList = (JAXBElement<ItemListType>) element;
//	JAXBElement<AnyListType> itemsList = (JAXBElement<AnyListType>) element;
////System.out.println("ItemsList:"+itemsList+" "+itemsList.getName().getLocalPart());
////		//lst = itemsList.getValue();
////System.out.println("itemList:$$$"+itemsList.getValue());
//
//if(itemsList.getValue() instanceof AnyListType){	
//	lst1 = itemsList.getValue();
////System.out.println("^^^^^lst1:"+lst1);		
//	List<Object> elements = lst1.getAny();
////System.out.println("EnResp.AnyList size!!!!!!!:"+elements.size());		
//	for (Object obj : elements) {
////System.out.println(")))))))))))Object stuff:"+obj);
//	    if (obj instanceof Element) {//Is a simple Element
//	        Element el = (Element) obj;
////System.out.println("Local name of Element disc in anyList  " + el.getLocalName());
//			bag.add(el);
//	    }
//	    else if(obj instanceof JAXBElement){
//		    JAXBElement gen = (JAXBElement) obj;
////System.out.println("gen:"+gen);
////System.out.println("genVal:"+gen.getValue());
//		    EndpointReferenceType eprT = (EndpointReferenceType) gen.getValue();
////				Node nod = (Node)obj;
////				System.out.println("Nod:"+nod);
////			    JAXBElement<EndpointReferenceType> jaxElem2 = (JAXBElement<EndpointReferenceType>)binding.unmarshal(nod);
////			    EndpointReferenceType eprT2 = jaxElem2.getValue();
////System.out.println("eprT2:"+eprT);
//				if(eprT!=null){
////System.out.println("eprAdd:"+eprT.getAddress().getValue());  					
////System.out.println("eprVal:"+eprT.getReferenceProperties().getAny().toArray()[0].toString());
//
//Document eprDocument = Management.newDocument();
//JAXBElement<EndpointReferenceType> eprElement = 
//new org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory().createEndpointReference(
//		eprT);
//binding.marshal(eprElement, eprDocument);
////System.out.println("EPRTONODE:"+xmlToString(eprDocument));
//			  bag.add(eprDocument.getDocumentElement());
////				  Element sample = null;
////				  Element[] samples = extractContentAsElements(eprT.getReferenceProperties());
////				  if((samples!=null)&(samples.length>0)){
////					for (int i = 0; i < samples.length; i++) {
////						bag.add(samples[i]);
////					}  
////				  }
//				}
////			    JAXBElement<EventMessageType> ob = (JAXBElement<EventMessageType>)binding.unmarshal(eventChildNode);
////			    EventMessageType retrievedEvent = (EventMessageType)ob.getValue();
//
////			       System.out.println("eprT:"+eprT+" "+jaxElem.getDeclaredType());
////			       System.out.println("jaxElem:"+eprT.getAddress().getValue());
////			       System.out.println("jaxElem:"+eprT.getReferenceProperties().getAny().toArray()[0].toString());
//	    	
//	    }
////		    else{
////		    	System.out.println("))))))))))) Else JAXBElement stuff:"+((JAXBElement<EndpointReferenceType>)obj));	
////		       JAXBElement<EndpointReferenceType> jaxElem = ((JAXBElement<EndpointReferenceType>)obj);
////		       EndpointReferenceType eprT = jaxElem.getValue();
//////		       System.out.println("eprT:"+eprT+" "+jaxElem.getDeclaredType()+" "+  n  );
//////		       System.out.println("jaxElem:"+eprT.getAddress().getValue());
//////		       System.out.println("jaxElem:"+eprT.getReferenceProperties().getAny().toArray()[0].toString());
////		    }
//	}
//  }
//}//End of processing for EnumerateResponse
//
//	}
//	if(bag.size()>0){
//		elementList = new Element[bag.size()];  
//		bag.toArray(elementList);  
//	}else{
//		elementList = new Element[0];  
//	}
//	return elementList;  
//}
//
///** Convenience method to create FilterType given a dialect string and the
//*   filter contents.
//* 
//* @param filterValue
//* @param dialect
//* @return FilterType instance.
//*/	
//public org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType createEnumerationFilterType(String filterValue, 
//	String dialect){
//    final org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType filter = 
//    	Enumeration.FACTORY.createFilterType();
//    filter.setDialect(dialect);
//    filter.getContent().add(filterValue);
//return filter;     
//}
//
///**
//* @param evtSourceDestination
//* @param evtSourceResourceUri
//* @param retrievedEventSourceId
//* @param stateDocument
//* @return
//* @throws SOAPException
//* @throws JAXBException
//*/
//private Management generateNewEventRequestMessage(String evtSourceDestination, String evtSourceResourceUri, String retrievedEventSourceId, Document stateDocument) throws SOAPException, JAXBException {
//Management man = new Management();
//  man.getBody().addDocument(stateDocument);
////Add the Creation Type[NEW_EVENT] info as a header
//ReferencePropertiesType creationHeader = 
//Addressing.createReferencePropertyType(EventMessageConstants.EVENTING_COMMUNICATION,
//	CreationTypes.NEW_EVENT.name());
// man.removeChildren(man.getHeader(), EventMessageConstants.EVENTING_COMMUNICATION);
// man.addHeaders(creationHeader);
//
//    //Finish add message info for a new Event to exercise the Eventing framework
//man.setResourceURI(evtSourceResourceUri);
//man.setAction(Transfer.CREATE_ACTION_URI);
//man.setTo(evtSourceDestination+"/"+evtSourceResourceUri);
////	man.setMessageId(getNewMessageId());
//man.setMessageId(EventMessageConstants.generateNewMessageId());
//man.setReplyTo(Transfer.ANONYMOUS_ENDPOINT_URI);
//
////add the Event-Source ID to indicate where the EVT should be created.
//final ReferencePropertiesType eventSourceIdType =
//	Addressing.createReferencePropertyType(Eventing.IDENTIFIER, 
//			retrievedEventSourceId);
//man.removeChildren(man.getHeader(), Eventing.IDENTIFIER);
//man.addHeaders(eventSourceIdType);
//return man;
//}
//
///** Examines the Addressing instance passed in for faults within the SoapBody.
//* @param message
//* @throws FaultException
//*/
//private void examineForFaults(final Addressing message) throws FaultException {
//if(message==null){
//	String msg="The Addressing instance passed in cannot be null.";
//	throw new IllegalArgumentException(msg);
//}
//if (message.getBody().hasFault()) {
//	SOAPFault fault = message.getBody().getFault();
//	throw new FaultException(fault.getFaultString());
//}
//}
//
//
///**
//* @param pullResponse
//* @param pullElementList
//* @throws SOAPException
//* @throws JAXBException
//*/
//public Element[] extractPullResponseAsElementArray(final Addressing pullResponse) throws SOAPException, JAXBException {
//Element[] elementList = null;
//Vector<Element> bag = new Vector<Element>();
//Enumeration pullResp = new Enumeration(pullResponse);
//PullResponse pr =  pullResp.getPullResponse();
//if(pr==null){
//	String msg="Unable to find a PullResponse element in the message.";
//	throw new IllegalArgumentException(msg);
//}
////System.out.println("PullResponse OBJect:"+pr);
//
//ItemListType itemList = pr.getItems();
//List<Object> allAnys = itemList.getAny();
//  for (Iterator iter = allAnys.iterator(); iter.hasNext();) {
//	Object element = (Object) iter.next();
////System.out.println("Any Item:"+element);
//	Element elem = (Element) element;
//	bag.add(elem);
////System.out.println("PullResp EventContext Id:"+elem.getTextContent());
////System.out.println("PullResp EventContext Description:"+elem.getAttribute("info"));
//
////System.out.println("PullResp toString:"+xmlToString(elem));
////			retrievedEventSourceId = elem.getTextContent();
//  }
//  if(bag.size()>0){
//	elementList = new Element[bag.size()];  
////		elementList = bag.toArray(elementList);  
//	bag.toArray(elementList);  
//  }else{
//	elementList = new Element[0];  
//  }
//return elementList;  
//}
//
///**
//* @param evtSubManDestination
//* @param evtSubManResourceUri
//* @return
//* @throws SOAPException
//* @throws JAXBException
//*/
//public Management pullEventingSourcesFromSubscriptionManagerMesg(String evtSubManDestination, String evtSubManResourceUri) throws SOAPException, JAXBException {
//Management pullMessage = new Management();
// 	pullMessage.setAction(Enumeration.PULL_ACTION_URI);
////	 	pullMessage.setMessageId(getNewMessageId());
// 	pullMessage.setMessageId(EventMessageConstants.generateNewMessageId());
//	pullMessage.setReplyTo(Transfer.ANONYMOUS_ENDPOINT_URI);
//	pullMessage.setTo(evtSubManDestination+"/"+evtSubManResourceUri);
//	pullMessage.setResourceURI(evtSubManResourceUri);
//return pullMessage;
//}
//
///**
//* @param evtSinkDestination
//* @param evtSourceDestination
//* @param evtSinkResourceUri
//* @param evtSourceResourceUri
//* @return
//* @throws SOAPException
//* @throws JAXBException
//* @throws DatatypeConfigurationException
//*/
//public Management constructSubscribeMessage(String evtSinkDestination, String evtSourceDestination, 
//	String evtSinkResourceUri, String evtSourceResourceUri,Duration expiration, String filterValue,
//	String eventingMode) 
//throws SOAPException, JAXBException, DatatypeConfigurationException {
//
//final Eventing evt = new Eventing();
//  //DONE: Set the WSMAN action URI 
//  evt.setAction(Eventing.SUBSCRIBE_ACTION_URI);
////	  evt.setMessageId(getNewMessageId());
//  evt.setMessageId(EventMessageConstants.generateNewMessageId());
//  //DONE: create ReplyTo element
//    //ANY for Resource URI of the evt src
//     final ReferencePropertiesType refp = 
//       	createReferencePropertiesElementWithIdentity(evtSinkResourceUri);
//    //END of ANY	        
//    final EndpointReferenceType evtSink = 
//    	evt.createEndpointReference(evtSinkDestination, refp, null, null, null);
//    evt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
//    //DONE: set the TO 
//    evt.setTo(evtSourceDestination+"/"+evtSourceResourceUri);
//
//  //DONE: Build Subscription Message body...
//    //END-TO NOT CURRENTLY SUPPORTED? reference properties::
//    //Build NOTIFY-TO reference properties::: Reuse above as is identical?
//    final DeliveryType delivery = Eventing.FACTORY.createDeliveryType();
////	    delivery.setMode(Eventing.PUSH_DELIVERY_MODE);
//     delivery.setMode(eventingMode);
////	     final String expires = DatatypeFactory.newInstance().newDuration(300000).toString();
//     final String expires = expiration.toString();
//     final FilterType filter = Eventing.FACTORY.createFilterType();
////	     filter.getContent().add("my/filter/expression");
//     filter.getContent().add(filterValue);
//    evt.setSubscribe(null, Eventing.PUSH_DELIVERY_MODE, evtSink, expires, filter);
//
////Build a small event to launch	
//Management mgt = new Management(evt);
//mgt.setResourceURI(evtSourceResourceUri);
//return mgt;
//}
//
////private String getNewMessageId() {
////	 String newMessageId = null;
//////	 newMessageId = UUID_SCHEME + UUID.randomUUID().toString();
////	 newMessageId = EventMessageConstants.UUID_SCHEME + UUID.randomUUID().toString();
////	return newMessageId;
////}
//
//private XMLGregorianCalendar getCurrentTimeAsGC() throws DatatypeConfigurationException {
//XMLGregorianCalendar calInst = null;
//Calendar cal = Calendar.getInstance(TimeZone.getDefault());
//calInst = 
//	 DatatypeFactory.newInstance().newXMLGregorianCalendar(cal.get(Calendar.YEAR),
//			 cal.get(Calendar.MONTH),cal.get(Calendar.DATE),cal.get(Calendar.HOUR),
//			 cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND),
//			 cal.get(Calendar.MILLISECOND),TimeZone.getDefault().SHORT);
//return calInst;
//}
//
///**
//* @param resourceUri
//* @return
//*/
//private ReferencePropertiesType createReferencePropertiesElementWithIdentity(String resourceUri) {
//final ReferencePropertiesType refp = Addressing.FACTORY.createReferencePropertiesType();
//final Document doc1 = Management.newDocument();
//final Element identifier = doc1.createElementNS(Eventing.IDENTIFIER.getNamespaceURI(),
//		Eventing.IDENTIFIER.getPrefix() + ":" + Eventing.IDENTIFIER.getLocalPart());
//identifier.setTextContent(resourceUri);
//doc1.appendChild(identifier);
//refp.getAny().add(doc1.getDocumentElement());
//return refp;
//}
//
/////** Creates a new Header component for communicating TRANSFER:Create SubType
//// * @param Transfer:Create substype[]
//// * @return
//// */
////private ReferencePropertiesType createMessagedTypeRefProp(QName container, String typeContent) {
////	//TODO: put in checks for NULL and empty strings.
////	 final ReferencePropertiesType refProperty = Addressing.FACTORY.createReferencePropertiesType();
//////	QName createSubType = new QName(Eventing.NS_URI,"CreationType",Eventing.NS_PREFIX);
////	 final Document document = Management.newDocument();
//////	final Element identifier = doc1.createElementNS(Eventing.IDENTIFIER.getNamespaceURI(),
//////			Eventing.IDENTIFIER.getPrefix() + ":" + Eventing.IDENTIFIER.getLocalPart());
//////	 final Element identifier = doc1.createElementNS(EVENTING_COMMUNICATION.getNamespaceURI(),
//////			 EVENTING_COMMUNICATION.getPrefix() + ":" + EVENTING_COMMUNICATION.getLocalPart());
////	 final Element identifier = document.createElementNS(container.getNamespaceURI(),
////			container.getPrefix() + ":" + container.getLocalPart());
//////	identifier.setTextContent(resourceUri);
////	 identifier.setTextContent(typeContent);
////	  document.appendChild(identifier);
////	 refProperty.getAny().add(document.getDocumentElement());
////	return refProperty;
////}
//
//private void processForEmptyStrings(String typeContent) {
//if((typeContent!=null)||(typeContent.trim().length()==0)){
//	String msg="Empty strings cannot be added.";
//		throw new IllegalArgumentException(msg);
//}
//}
//
//private void processForNulls(QName container, String typeContent) {
//boolean value = false;
//if((container==null)||(typeContent==null)){
//	String msg="None of the values passed in can be null.";
//	throw new IllegalArgumentException(msg);
//}
//}
//
///**
//* @param request
//* @param creationSubType
//* @throws SOAPException
//*/
//private Element locateChildAsElement(ReferencePropertiesType request, 
//	QName specificQName) throws SOAPException {
//if((request==null)||(specificQName==null)){
////	if(request==null){
//	throw new IllegalArgumentException("ReferencePropertyType and/or QName cannot be null.");
//}
//
//final Document doc1 = Management.newDocument();
//final Element identifier = doc1.createElementNS(specificQName.getNamespaceURI(),
//		specificQName.getPrefix() + ":" + specificQName.getLocalPart());
////	identifier.setTextContent(resourceUri);
//
//Element locatedElement = null;
//
//List<Object> allProperties = request.getAny();
//for (Iterator iter = allProperties.iterator(); iter.hasNext();) {
//	Object element = (Object) iter.next();
////System.out.println("Element is:"+element);
//	if(element instanceof JAXBElement){
//		JAXBElement node = (JAXBElement)element;
//		if(node.getName().equals(specificQName)){
////				locatedElement = new BodyElement1_2Impl((SOAPDocumentImpl) 
////				locatedElement = SOAPElement.((SOAPDocumentImpl)
//			identifier.setTextContent((String) node.getValue());
//			locatedElement = identifier;
//		}
//	}
//}
//return locatedElement; 
//}
//
///** Extracts the contents of a ReferencePropertiesType node and attempt
//*  to return the value as an array of Element[].
//* @param request ReferencePropertiesType instance
//* @throws SOAPException
//*/
//private Element[] extractContentAsElements(ReferencePropertiesType request) throws SOAPException {
//Element[] contents = null;
//ArrayList<Element> located = new ArrayList<Element>();
//
//if(request==null){
//	throw new IllegalArgumentException("ReferencePropertyType cannot be null.");
//}
//
//final Document doc1 = Management.newDocument();
//List<Object> allProperties = request.getAny();
//
//for (Iterator iter = allProperties.iterator(); iter.hasNext();) {
//	Object element = (Object) iter.next();
//	if(element instanceof JAXBElement){
//		JAXBElement node = (JAXBElement)element;
//		if(node.getName()!=null){
//			QName nodeQ = node.getName();
////			   final QName nodeQ = new QName(node.getName().getNamespaceURI(),
////					   node.getName().getLocalPart(),
////					   node.getName().getPrefix());
////System.out.println("nodeQpre:"+nodeQ.getPrefix());
////System.out.println("nodeQURI:"+nodeQ.getNamespaceURI());
////System.out.println("nodeQLP:"+nodeQ.getLocalPart());
////			   final Document doc1 = Management.newDocument();
//			Element identifier = null;
//			if(nodeQ.getPrefix().trim().length()>0){
//			  identifier =doc1.createElementNS(nodeQ.getNamespaceURI(),
//					nodeQ.getPrefix() + ":" + nodeQ.getLocalPart());
//			}else{
//			  identifier =doc1.createElementNS(nodeQ.getNamespaceURI(),
//						nodeQ.getLocalPart());
//			}
//			identifier.setTextContent((String) node.getValue());
//		  	located.add(identifier);
//		}
//	}
//}
//if(located.size()>0){
//	contents = new Element[located.size()];
//	located.toArray(contents);
//}else{
//	contents = new Element[0];
//}
//return contents; 
//}
	
}
