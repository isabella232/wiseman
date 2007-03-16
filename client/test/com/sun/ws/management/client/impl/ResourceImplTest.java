package com.sun.ws.management.client.impl;

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
import java.util.concurrent.TimeoutException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.WsManBaseTestSupport;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.Management;
import com.sun.ws.management.client.EnumerationCtx;
import com.sun.ws.management.client.EnumerationResourceState;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceFactory;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.ServerIdentity;
import com.sun.ws.management.client.TransferableResource;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

/**
 * This class tests basic WS Transfer behavior and also demonstrates the
 * capabilies of the client library.
 *
 * @author wire
 *
 */
public class ResourceImplTest extends WsManBaseTestSupport {

	private static ObjectFactory userFactory = new ObjectFactory();

	public static String destUrl = "http://localhost:8080/wsman/";

	public static String resourceUri = "wsman:auth/user";

	public static long timeoutInMilliseconds = 9400000;
	
	private static final String USER_NS = "http://examples.hp.com/ws/wsman/user";

	XmlBinding binding = null;

	protected void setUp() throws Exception {
		super.setUp();
		try {
			new Management();
		} catch (SOAPException e) {
			fail("Can't init wiseman");
		}
		try {
			binding = new XmlBinding(null,"com.hp.examples.ws.wsman.user");
		} catch (JAXBException e) {
			fail(e.getMessage());
		}
		userFactory = new ObjectFactory();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testIdentity() throws XPathExpressionException, NoMatchFoundException, SOAPException, IOException, JAXBException{
		ServerIdentity serverInfo = ResourceFactory.getIdentity(destUrl);
		assertNotNull(serverInfo);
		assertNotNull(serverInfo.getProductVendor());
		assertNotNull(serverInfo.getProductVersion());
		assertNotNull(serverInfo.getProtocolVersion());
		assertNotNull(serverInfo.getSpecVersion());
		assertNotNull(serverInfo.getBuildId());
		assertEquals(serverInfo.getProductVendor(),"The Wiseman Project - https://wiseman.dev.java.net");
		assertEquals(serverInfo.getProductVersion(),"0.6");
		assertEquals(serverInfo.getProtocolVersion(),"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd");
		assertEquals(serverInfo.getSpecVersion(),"1.0.0a");
		// assertTrue(serverInfo.getBuildId().startsWith("2006"));

	}

	public void testIdentityHeaders() throws XPathExpressionException, NoMatchFoundException, SOAPException, IOException, JAXBException{
//		ServerIdentity serverInfo = ResourceFactory.getIdentity(destUrl,1000,new Entry<String, String>("",""));
//		
//		String respose="HTTP/1.0 400 Bad Request";
	}

	
	/** 
	 * Tests to see if an identity request will timout
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundException
	 * @throws SOAPException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws InterruptedException 
	 */
	public void testIdentityTimeout() throws XPathExpressionException, NoMatchFoundException, SOAPException, IOException, JAXBException, InterruptedException{
	    // Create a non-blocking server socket and check for connections
	        ServerSocketChannel ssChannel = ServerSocketChannel.open();
	        ssChannel.configureBlocking(false);
	        int port = 10666;
	        ssChannel.socket().bind(new InetSocketAddress(port));
	    
	        SocketChannel sChannel = ssChannel.accept();

	        try {
	        ServerIdentity serverInfo = ResourceFactory.getIdentity("http://localhost:10666/wsman",500);
	        } catch (TimeoutException e){
	        	return;
	        } 
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

//		String className ="com.sun.ws.management.server.ReflectiveRequestDispatcher";
//		 locateClass(className);
//		 className ="com.sun.tools.xjc.reader.Util";
//		 locateClass(className);

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

		Resource resource = ResourceFactory.create(ResourceImplTest.destUrl,
				ResourceImplTest.resourceUri,
				ResourceImplTest.timeoutInMilliseconds, content,
				ResourceFactory.LATEST);

		resource.addOption("opt1", "value1", new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string"));
		resource.addOption("opt2", new Integer(7), new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int"), true);
		resource.addOption("opt3", new Boolean(true));
		resource.addOption("opt2", new Integer(99), new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int"), true);
		
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
	 * Test method for 'com.sun.ws.management.client.impl.TransferableResourceImpl.get(Map<String,
	 * Object>)'
	 */
	public void testFragmentGet() throws JAXBException, SOAPException, IOException,
			DatatypeConfigurationException, TransformerException, FaultException {

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
		JAXBElement<UserType> userElement =userFactory.createUser(user);
		binding.marshal(userElement,content);

		Resource resource = ResourceFactory.create(ResourceImplTest.destUrl,
				ResourceImplTest.resourceUri,
				ResourceImplTest.timeoutInMilliseconds,
				content,ResourceFactory.LATEST);

		
		resource.addOption("opt1", "value1");
		resource.addOption("opt2", new Integer(7));
		resource.addOption("opt3", new Boolean(true));
		
		//Now build the XPath expression for fragment GET
		String xPathReq = "//*[local-name()='age']";
		//TODO: write test using /text() method as currently fails on the server.
//		String xPathReq = "//*[local-name()='age']/text()";
		ResourceState resourceState = resource.get(xPathReq,null);

		assertNotNull("Retrieved resource is NULL.", resourceState);

		//Retrieve content
		String fragmentContent = "";
		try {
		  fragmentContent = resourceState.getValueText("*");
		} catch (XPathExpressionException e) {
			fail(e.getMessage());
		} catch (NoMatchFoundException e) {
			fail("Bad XPath expression:"+e.getMessage());
		}
		//Test expected age content
		assertTrue(fragmentContent.indexOf(user.getAge()+"")>-1);
//		System.out.println("RsState:"+resourceState.toString());
		//Test that no other part of the fragment, like fName was returned.
		assertTrue(resourceState.toString().indexOf(fName)==-1);
		resource.delete();

	}


	public void testInvoke() throws JAXBException, SOAPException, IOException,
	DatatypeConfigurationException, FaultException {
		
		// Now create an instance and test it's contents
		String dest = ResourceImplTest.destUrl;
		String resource = ResourceImplTest.resourceUri;

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
		
		Resource newResource = ResourceFactory.create(ResourceImplTest.destUrl,
				ResourceImplTest.resourceUri,
				ResourceImplTest.timeoutInMilliseconds, stateDocument,
				ResourceFactory.LATEST, null);
		
		ResourceState response = newResource.invoke("http://unit.test.org/customaction", stateDocument);
		assertNotNull(response.getDocument());
		assertNotNull(response.getDocument().getFirstChild());
		assertNotNull(response.getDocument().getFirstChild().getFirstChild());
		assertEquals("The answer is plastics",response.getDocument().getFirstChild().getFirstChild().getTextContent());
	}
	
	/*
	 * Test method for
	 * 'com.sun.ws.management.client.impl.TransferableResourceImpl.create(Map<QName,
	 * String>)'
	 */
	public void testCreate() throws JAXBException, SOAPException, IOException,
			DatatypeConfigurationException, FaultException {

		// Now create an instance and test it's contents
		String dest = ResourceImplTest.destUrl;
		String resource = ResourceImplTest.resourceUri;

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

		Resource newResource = ResourceFactory.create(ResourceImplTest.destUrl,
				ResourceImplTest.resourceUri,
				ResourceImplTest.timeoutInMilliseconds, stateDocument,
				ResourceFactory.LATEST, options);

		assertEquals("Values not identical.", dest, newResource
				.getDestination());
		assertEquals("Values not identical.", resource, newResource
				.getResourceUri());
		assertEquals("Values not identical.",
				ResourceImplTest.timeoutInMilliseconds, newResource
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
	 * Test method for 'com.sun.ws.management.client.impl.TransferableResourceImpl.create(Map<QName,
	 * String>)'
	 */
	public void testFragmentCreate() throws JAXBException, SOAPException, IOException,
			 DatatypeConfigurationException, FaultException {

		// Now create an instance and test it's contents
		String dest = ResourceImplTest.destUrl;
		String resource = ResourceImplTest.resourceUri;

		String lastName ="Finkle-Fragment"+Calendar.getInstance().getTimeInMillis();
		UserType user = userFactory.createUserType();
		user.setLastname(lastName);
		user.setFirstname("Joe");
		user.setAddress("6000 Irwin Drive");
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
//		user.setAge(16);

		Document stateDocument = Management.newDocument();
		JAXBElement<UserType> userElement =userFactory.createUser(user);
		binding.marshal(userElement,stateDocument);

		Resource newResource = ResourceFactory.create(ResourceImplTest.destUrl, ResourceImplTest.resourceUri,
				ResourceImplTest.timeoutInMilliseconds, stateDocument,ResourceFactory.LATEST);

		assertEquals("Values not identical.", dest, newResource.getDestination());
		assertEquals("Values not identical.", resource, newResource
				.getResourceUri());
		assertEquals("Values not identical.", ResourceImplTest.timeoutInMilliseconds, newResource
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

		//TODO: test that Age field was never set
		ResourceState resourceState = newResource.get();
//		System.out.println("resState:"+resourceState.toString());
		assertNotNull("Retrieved resource is NULL.", resourceState);

		Document resourceStateDom = resourceState.getDocument();
		JAXBElement<UserType> userReturnedElement =
			(JAXBElement<UserType>)binding.unmarshal(resourceStateDom);
		UserType returnedUser = (UserType)userReturnedElement.getValue();

		// Compare the created state to the returned state
		assertEquals(returnedUser.getLastname(),user.getLastname());
		assertEquals(returnedUser.getFirstname(),user.getFirstname());
		assertNull("Age field should be null:"+returnedUser.getAge(),returnedUser.getAge());

		//TODO: build the fragment create method and object changes
		String fragmentRequest = "//*[local-name()='age']";
		  stateDocument = Management.newDocument();
		    // Insert the root element node
		    Element element =
		    	stateDocument.createElementNS("http://examples.hp.com/ws/wsman/user","user:age");
	    int number = 10;
		    element.setTextContent(""+number);
		    stateDocument.appendChild(element);
		  userElement =userFactory.createUser(user);
//		 binding.marshal(userElement,stateDocument);
		Resource newFragResource = ResourceFactory.createFragment(ResourceImplTest.destUrl,
				ResourceImplTest.resourceUri,
				newResource.getSelectorSet(),
				ResourceImplTest.timeoutInMilliseconds,
				stateDocument,ResourceFactory.LATEST,
				fragmentRequest,
				XPath.NS_URI);

		assertEquals("Values not identical.", dest, newResource.getDestination());
		assertEquals("Values not identical.", resource, newResource
				.getResourceUri());
		assertEquals("Values not identical.", ResourceImplTest.timeoutInMilliseconds, newResource
				.getMessageTimeout());

		resourceState = newResource.get();
		assertNotNull("Retrieved resource is NULL.", resourceState);

		resourceStateDom = resourceState.getDocument();
		userReturnedElement =
			(JAXBElement<UserType>)binding.unmarshal(resourceStateDom);
		returnedUser = (UserType)userReturnedElement.getValue();

		// Compare the created state to the returned state
		assertEquals(returnedUser.getLastname(),user.getLastname());
		assertEquals(returnedUser.getFirstname(),user.getFirstname());
		assertEquals("Value not retrieved correctly.",(int)returnedUser.getAge(),number);
		newResource.delete();

	}

	/*
	 * Test method for
	 * 'com.sun.ws.management.client.impl.TransferableResourceImpl.delete()'
	 */
	public void testDelete() throws JAXBException, SOAPException, IOException,
			FaultException, DatatypeConfigurationException {
		String dest = ResourceImplTest.destUrl;
		String resource = ResourceImplTest.resourceUri;
		long timeoutInMilliseconds = ResourceImplTest.timeoutInMilliseconds;
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

	public void testFragmentDelete() throws JAXBException, SOAPException, IOException,
			FaultException, DatatypeConfigurationException {
		String dest = ResourceImplTest.destUrl;
		String resource = ResourceImplTest.resourceUri;
		long timeoutInMilliseconds = ResourceImplTest.timeoutInMilliseconds;

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

//		Resource created = ResourceFactory.create(dest, resource,
		TransferableResourceImpl created = (TransferableResourceImpl) ResourceFactory.create(dest, resource,
				timeoutInMilliseconds, content,ResourceFactory.LATEST);
		// pull out Epr and parse for selectors
		// EndpointReferenceType retrievedEpr = created.getResourceEpr();

		SelectorSetType selectorSet = null;
		selectorSet = created.getSelectorSet();
		assertNotNull("SelectorSet is null.", selectorSet);

		String xPathReq = "//*[local-name()='age']";
		created.delete(xPathReq,null);
		
		created.addOption("opt1", "value1");
		created.addOption("opt2", new Integer(7));
		created.addOption("opt3", new Boolean(true), true);
		
		try {
			ResourceState res = created.get();
			assertTrue(true);
//			System.out.println("res"+res.toString());
		} catch (InvalidRepresentationFault df) {
			fail("Unable to retrieve resource that should NOT have been deleted.");
		} catch (FaultException fex) {
			fail("Unable to retrieve resource that should NOT have been deleted.");
		}

	}


	public void testPut() throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException {
		// Now create an instance and test it's contents
		String dest = ResourceImplTest.destUrl;
		String resource = ResourceImplTest.resourceUri;
		long timeoutInMilliseconds = ResourceImplTest.timeoutInMilliseconds;
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
		JAXBElement<UserType> ob = (JAXBElement<UserType>) binding
				.unmarshal(userChildNode);
		user = (UserType) ob.getValue();

		assertEquals("Values not equal.", addressModified, user.getAddress());

		created.delete();
	}

	public void testFragmentPut() throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException{
		// Now create an instance and test it's contents
		String dest = ResourceImplTest.destUrl;
		String resource = ResourceImplTest.resourceUri;
		long timeoutInMilliseconds = ResourceImplTest.timeoutInMilliseconds;
		Document content = null;

		// now build Create XML body contents.
		String fName = "Fragment";
		String lName = "Put";
		String state = "NJ";
		String stateUpdated = "New Jersey";
		String address = "6000 Irwin Drive";
//		String addressModified = address + "FragmentPut";

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
//		TransferableResource created =(TransferableResource) ResourceFactory.create(dest, resource,
		TransferableResource created =(TransferableResource) ResourceFactory.create(dest, resource,
				timeoutInMilliseconds, content,ResourceFactory.LATEST);

		//DONE: create fragment request to update state field only.
		String fragmentRequest = "//*[local-name()='state']";
		  content = Management.newDocument();
		    // Insert the root element node
		    Element element =
		    	content.createElementNS("http://examples.hp.com/ws/wsman/user","user:state");
		    element.setTextContent(stateUpdated);
		    content.appendChild(element);
		    
			created.addOption("opt1", "value1");
			created.addOption("opt2", new Integer(7));
			created.addOption("opt3", new Boolean(true));
			
		    created.put(content,
		    		fragmentRequest,
					XPath.NS_URI);

			
		ResourceState retrieved = created.get();

		assertNotNull("Retrieved resource is NULL.", retrieved);
		Document payLoad = retrieved.getDocument();

		Element node = payLoad.getDocumentElement();
		user = userFactory.createUserType();
		// Init the user by transfering the fields from
		Node userChildNode = node;
		JAXBElement<UserType> ob = (JAXBElement<UserType>) binding
				.unmarshal(userChildNode);
		user = (UserType) ob.getValue();

		assertEquals("Values not equal.", stateUpdated, user.getState());

	}


	public void testFind() throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException {

		// test that Find works to retrieve the Enumeration instance.
		String dest = "";
		String resUri = "";
		HashMap<String,String> selectors=null;
		Resource[] enumerableResources = ResourceFactory.find(
				ResourceImplTest.destUrl, ResourceImplTest.resourceUri,
				ResourceImplTest.timeoutInMilliseconds, selectors);
		assertEquals("Expected one resource.", 1, enumerableResources.length);
		// TODO: write test for specific resource retrieval.
		// Source src = null;
		// JAXBSource.sourceToInputSource(src);

	}

	 public void testEnumeration() throws SOAPException, JAXBException,
	 IOException, FaultException, DatatypeConfigurationException{
	 
		 //define enumeration handler url
		 String resourceUri = "wsman:auth/userenum";
	
		 SelectorSetType selectors = null;
		 //test that Find works to retrieve the Enumeration instance.
		 Resource[] enumerableResources = ResourceFactory.find(
		 ResourceImplTest.destUrl,
		 resourceUri,
		 ResourceImplTest.timeoutInMilliseconds,
		 selectors);
		 
		 assertEquals("Expected one resource.",1,enumerableResources.length);
		 Resource retrieved = enumerableResources[0];
		 assertTrue(retrieved instanceof EnumerationResourceImpl);
		
		 retrieved.addOption("opt1", "value1");
		 retrieved.addOption("opt2", new Integer(7));
		 retrieved.addOption("opt3", new Boolean(true));		 
 		 
		 //Build the filters
		 final String testName = "James";//See users.store for more valid search values
		 final String xpathFilter = "/user:user[user:firstname='"+testName+"']";
//		 String xpathFilter = "/user:user/user:firstname/text()";
		 final Map<String, String> namespaces = new HashMap<String, String>(1);
		 namespaces.put("user", USER_NS);
		 
		 long timeout = 1000000;
		 
		 // Add custom user param
		 Document userDoc = Management.newDocument();
		 
		 Element userChildNode = userDoc.createElementNS("http://my.schema", "me:MyParam");
		 userChildNode.setTextContent("888");
		 
		 Element userChildNode2 = userDoc.createElementNS("http://my.schema", "me:MyOtherParam");
		 userChildNode2.setTextContent("9999");
		 
		 //Retrieve the Enumeration context.
		 EnumerationCtx enumContext = ((EnumerationResourceImpl)retrieved).enumerate(xpathFilter,namespaces,XPath.NS_URI,false,false, timeout, 
				 new Object[] {userChildNode, userChildNode2});
		  assertNotNull("Enum context retrieval problem.",enumContext);
		  assertTrue("Context id is empty.",(enumContext.getContext().trim().length()>0));
		
		 //DONE: now test the pull mechanism
		 int maxTime =1000*60*15;
		 int maxElements = 5;
		 int maxChar = 0; // 20000; //random limit. NOT yet supported.
		 
		 ResourceState retrievedValues = retrieved.pull(enumContext,maxTime,
				 maxElements,maxChar);
		 //Navigate down to retrieve Items children
		 	//Document Children
		 NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		  //PullResponse node
		 assertNotNull("No root node for PullResponse.",rootChildren);
		 	Node child = rootChildren.item(0);
		 	String toString = xmlToString(child);
		 //Items node
		 assertNotNull("No child node for PullResponse found.",child);
		 //Check number of enumerated values returned.
		 NodeList children = child.getChildNodes().item(1).getChildNodes();
		 assertEquals("Incorrect number of elements returned!",
				 maxElements, children.getLength());
		 
		 //DONE: iterate through to make sure that 
		 for(int i=0;i<children.getLength();i++){
			 Node node = children.item(i);
			 if(node.getNodeName().indexOf("EnumerationContext")>-1){
				 //ignore
			 }else{
				 UserType user = null;
				 String[] pkgList = {"com.hp.examples.ws.wsman.user"};
				 XmlBinding empBinding = new XmlBinding(null,pkgList);
				 JAXBElement<UserType> ob =
					 (JAXBElement<UserType>)empBinding.unmarshal(node);
				 user=(UserType)ob.getValue();
				 assertTrue("Filter for user " + testName.trim() + " failed!",
						    user.getFirstname().trim().equalsIgnoreCase(testName.trim()));
			 }
		 }
		 
		 //Execute another pull. We know the data set users.store so another
		 //   pull is valid.
		 retrievedValues = retrieved.pull(enumContext,maxTime,
				 maxElements,maxChar);
		  assertNotNull("No pull results obtained.",retrievedValues);
		
		 //Now do a find again to make sure that correct context is retrieved.
		 Resource[] enumResources = ResourceFactory.find(
				 ResourceImplTest.destUrl,
				 resourceUri,
				 ResourceImplTest.timeoutInMilliseconds,
				 selectors);
		 //Only resource retrieved should be the enumerable resource
		 Resource retEnumRes = enumResources[0];
		 retrievedValues = retEnumRes.pull(enumContext,maxTime,
		 maxElements,maxChar);
		
		 //DONE: test release
		 retrieved.release(enumContext);
		 try{
			 retrievedValues = retEnumRes.pull(enumContext,maxTime,
					 maxElements,maxChar);
			 fail("This context should have been destroyed.");
		 }catch(FaultException ex){
			 //Do nothing, should fail.
		 }
	 }
	
	 public void testDomEnumeration() throws SOAPException, JAXBException,
	 IOException, FaultException, DatatypeConfigurationException{
	 
		 //define enumeration handler url
		 String resourceUri = "wsman:auth/userenum";
	
		 SelectorSetType selectors = null;
		 //test that Find works to retrieve the Enumeration instance.
		 Resource[] enumerableResources = ResourceFactory.find(
		 ResourceImplTest.destUrl,
		 resourceUri,
		 ResourceImplTest.timeoutInMilliseconds,
		 selectors);
		 
		 assertEquals("Expected one resource.",1,enumerableResources.length);
		 Resource retrieved = enumerableResources[0];
		 assertTrue(retrieved instanceof EnumerationResourceImpl);
		
		 retrieved.addOption("opt1", "value1");
		 retrieved.addOption("opt2", new Integer(7));
		 retrieved.addOption("opt3", new Boolean(true));		 
 
		 Document userDoc = Management.newDocument();

		 //Build the filters
		 final String testName = "Simpson";//See users.store for more valid search values
		 Element filterElem = userDoc.createElement("Expression");
		 filterElem.setTextContent(testName);
		 final Map<String, String> namespaces = new HashMap<String, String>(1);
		 namespaces.put("user", USER_NS);
		 
		 long timeout = 1000000;
		 
		 // Add custom user param
		 
		 Element userChildNode = userDoc.createElementNS("http://my.schema", "me:MyParam");
		 userChildNode.setTextContent("888");
		 
		 Element userChildNode2 = userDoc.createElementNS("http://my.schema", "me:MyOtherParam");
		 userChildNode2.setTextContent("9999");
		 
		 String dialect = "http://examples.hp.com/ws/wsman/user/filter/custom";
		 //Retrieve the Enumeration context.
		 EnumerationCtx enumContext = ((EnumerationResourceImpl)retrieved).enumerate(filterElem,namespaces,dialect,false,false, timeout, 
				 new Object[] {userChildNode, userChildNode2});
		  assertNotNull("Enum context retrieval problem.",enumContext);
		  assertTrue("Context id is empty.",(enumContext.getContext().trim().length()>0));
		
		 //DONE: now test the pull mechanism
		 int maxTime =1000*60*15;
		 int maxElements = 5;
		 int maxChar = 0; // 20000; //random limit. NOT yet supported.
		 
		 ResourceState retrievedValues = retrieved.pull(enumContext,maxTime,
				 maxElements,maxChar);
		 //Navigate down to retrieve Items children
		 	//Document Children
		 NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		  //PullResponse node
		 assertNotNull("No root node for PullResponse.",rootChildren);
		 	Node child = rootChildren.item(0);
		 	String toString = xmlToString(child);
		 //Items node
		 assertNotNull("No child node for PullResponse found.",child);
		 //Check number of enumerated values returned.
		 NodeList children = child.getChildNodes().item(1).getChildNodes();
		 assertEquals("Incorrect number of elements returned!",
				 maxElements, children.getLength());
		 
		 //DONE: iterate through to make sure that 
		 for(int i=0;i<children.getLength();i++){
			 Node node = children.item(i);
			 if(node.getNodeName().indexOf("EnumerationContext")>-1){
				 //ignore
			 }else{
				 UserType user = null;
				 String[] pkgList = {"com.hp.examples.ws.wsman.user"};
				 XmlBinding empBinding = new XmlBinding(null,pkgList);
				 JAXBElement<UserType> ob =
					 (JAXBElement<UserType>)empBinding.unmarshal(node);
				 user=(UserType)ob.getValue();
				 assertTrue("Filter for user " + testName.trim() + " failed!",
						    user.getLastname().trim().equalsIgnoreCase(testName.trim()));
			 }
		 }
		 
	
		 //DONE: test release
		 retrieved.release(enumContext);

	 }	 
	 public void testEnumerationWithCount() throws SOAPException, JAXBException,
	 IOException, FaultException, DatatypeConfigurationException{
	 
		 //define enumeration handler url
		 String resourceUri = "wsman:auth/userenum";
	
		 SelectorSetType selectors = null;
		 //test that Find works to retrieve the Enumeration instance.
		 Resource[] enumerableResources = ResourceFactory.find(
		 ResourceImplTest.destUrl,
		 resourceUri,
		 ResourceImplTest.timeoutInMilliseconds,
		 selectors);
		 
		 assertEquals("Expected one resource.",1,enumerableResources.length);
		 Resource retrieved = enumerableResources[0];
		 assertTrue(retrieved instanceof EnumerationResourceImpl);
		
		 retrieved.addOption("opt1", "value1");
		 retrieved.addOption("opt2", new Integer(7));
		 retrieved.addOption("opt3", new Boolean(true));		 
 		 
		 //Build the filters
		 final String testName = "James";//See users.store for more valid search values
		 final String xpathFilter = "/user:user[user:firstname='"+testName+"']";
//		 String xpathFilter = "/user:user/user:firstname/text()";
		 final Map<String, String> namespaces = new HashMap<String, String>(1);
		 namespaces.put("user", USER_NS);

		 long timeout = 1000000;
		 
		 // Add custom user param
		 Document userDoc = Management.newDocument();
		 
		 Element userChildNode = userDoc.createElementNS("http://my.schema", "me:MyParam");
		 userChildNode.setTextContent("888");
		 
		 Element userChildNode2 = userDoc.createElementNS("http://my.schema", "me:MyOtherParam");
		 userChildNode2.setTextContent("9999");
		 
		 //Retrieve the Enumeration context.
		 EnumerationCtx enumContext = ((EnumerationResourceImpl)retrieved).enumerate(xpathFilter,namespaces,XPath.NS_URI,false,
				 false, timeout, true, new Object[] {userChildNode, userChildNode2});
		  assertNotNull("Enum context retrieval problem.",enumContext);
		  assertTrue("Context id is empty.",(enumContext.getContext().trim().length()>0));
		  
		  assertTrue("Item count null", ((EnumerationResourceImpl)retrieved).getItemCount() != null);
		  assertTrue("Item count <= 0", ((EnumerationResourceImpl)retrieved).getItemCount().longValue() > 0);
		
		 //DONE: now test the pull mechanism
		 int maxTime =1000*60*15;
		 int maxElements = 5;
		 int maxChar = 0; // 20000; //random limit. NOT yet supported.
		 
		 ResourceState retrievedValues = ((EnumerationResourceImpl)retrieved).pull(enumContext,maxTime,
				 maxElements,maxChar, true, null, null);
		 //Navigate down to retrieve Items children
		 	//Document Children
		 NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		  //PullResponse node
		 assertNotNull("No root node for PullResponse.",rootChildren);
		 	Node child = rootChildren.item(0);
		 	String toString = xmlToString(child);
		 //Items node
		 assertNotNull("No child node for PullResponse found.",child);
		 //Check number of enumerated values returned.
		 NodeList children = child.getChildNodes().item(1).getChildNodes();
		 assertEquals("Incorrect number of elements returned!",
				 maxElements, children.getLength());
		 
		  assertTrue("Item count <= 0", ((EnumerationResourceImpl)retrieved).getItemCount() > 0);
		 //DONE: iterate through to make sure that 
		 for(int i=0;i<children.getLength();i++){
			 Node node = children.item(i);
			 if(node.getNodeName().indexOf("EnumerationContext")>-1){
				 //ignore
			 }else{
				 UserType user = null;
				 String[] pkgList = {"com.hp.examples.ws.wsman.user"};
				 XmlBinding empBinding = new XmlBinding(null,pkgList);
				 JAXBElement<UserType> ob =
					 (JAXBElement<UserType>)empBinding.unmarshal(node);
				 user=(UserType)ob.getValue();
				 assertTrue(user.getFirstname().trim().equalsIgnoreCase(testName.trim()));
			 }
		 }
		 
		 //DONE: release
		 retrieved.release(enumContext);

	 }
	 
	 public void testFragmentEnumeration() throws SOAPException, JAXBException,
	 IOException, FaultException, DatatypeConfigurationException{
	 
		 //define enumeration handler url
		 String resourceUri = "wsman:auth/userenum";
	
		 SelectorSetType selectors = null;
		 //test that Find works to retrieve the Enumeration instance.
		 Resource[] enumerableResources = ResourceFactory.find(
		 ResourceImplTest.destUrl,
		 resourceUri,
		 ResourceImplTest.timeoutInMilliseconds,
		 selectors);
		 
		 assertEquals("Expected one resource.",1,enumerableResources.length);
		 Resource retrieved = enumerableResources[0];
		 assertTrue(retrieved instanceof EnumerationResourceImpl);
		
		 //Build the filters
		 final String testName = "James";//See users.store for more valid search values
		 final String xpathFilter = "/user:user[user:firstname='"+testName+"']/user:age";
		 final Map<String, String> namespaces = new HashMap<String, String>(1);
		 namespaces.put("user", USER_NS);
		  
	     //Now build the XPath expression for fragment GET
		 String xPathReq = null; // TODO: "//*[local-name()='age']";
		
		 //Retrieve the Enumeration context.
		 EnumerationCtx enumContext = ((EnumerationResourceImpl)retrieved).enumerate(xpathFilter,namespaces,XPath.NS_URI,false,false, false, 
				 xPathReq, null, false, 0);
		  assertNotNull("Enum context retrieval problem.",enumContext);
		  assertTrue("Context id is empty.",(enumContext.getContext().trim().length()>0));
		
		 //DONE: now test the pull mechanism
		 int maxTime =1000*60*15;
		 int maxElements = 10;
		 int maxChar = 0; // 20000; //random limit. NOT yet supported.
		 
		 EnumerationResourceState retrievedValues = ((EnumerationResourceImpl)retrieved).pull(enumContext,maxTime,
				 maxElements,maxChar, xPathReq, null);
		 //Navigate down to retrieve Items children
		 	//Document Children
		 NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		  //PullResponse node
		 assertNotNull("No root node for PullResponse.",rootChildren);
		 	Node child = rootChildren.item(0);
		 //Items node
		 assertNotNull("No child node for PullResponse found.",child);
		 //Check number of enumerated values returned.
		 NodeList children = child.getChildNodes().item(1).getChildNodes();
		 assertEquals("Incorrect number of elements returned!",
				 maxElements, children.getLength());
		 
		 List <Node> enumList = retrievedValues.getEnumerationItems();
		 //DONE: iterate through to make sure that only XML Fragment nodes are returned
		 for(int i=0;i<enumList.size();i++){
			 Node node = enumList.get(i);
			 if(node.getNodeName().indexOf("EnumerationContext")>-1){
				 //ignore
			 }else {
				  assertTrue(node.getNodeName().indexOf("XmlFragment")>-1);
				  
				  // Now make sure that the right field for the XPATH expression is returned
				  assertTrue(node.getFirstChild().getNodeName().indexOf("age") > -1);
			 }
		 }
		 
		
		 //DONE: test release
		 retrieved.release(enumContext);
	 }
		 

	 public void testEnumerationResourceState() throws SOAPException, JAXBException,
	 IOException, FaultException, DatatypeConfigurationException{
	 
		 //define enumeration handler url
		 String resourceUri = "wsman:auth/userenum";
	
		 SelectorSetType selectors = null;
		 //test that Find works to retrieve the Enumeration instance.
		 Resource[] enumerableResources = ResourceFactory.find(
		 ResourceImplTest.destUrl,
		 resourceUri,
		 ResourceImplTest.timeoutInMilliseconds,
		 selectors);
		 
		 assertEquals("Expected one resource.",1,enumerableResources.length);
		 Resource retrieved = enumerableResources[0];
		 assertTrue(retrieved instanceof EnumerationResourceImpl);
		
		 
		 //Build the filters
		 final String testName = "James";//See users.store for more valid search values
		 final String xpathFilter = "/user:user[user:firstname='"+testName+"']";
//		 String xpathFilter = "/user:user/user:firstname/text()";
		 final Map<String, String> namespaces = new HashMap<String, String>(1);
		 namespaces.put("user", USER_NS);

		 long timeout = 1000000; 
		 
		 //Retrieve the Enumeration context.
		 EnumerationCtx enumContext = ((EnumerationResourceImpl)retrieved).enumerate(xpathFilter,namespaces,XPath.NS_URI,false,false, timeout);
		  assertNotNull("Enum context retrieval problem.",enumContext);
		  assertTrue("Context id is empty.",(enumContext.getContext().trim().length()>0));
		
		 //DONE: now test the pull mechanism
		 int maxTime =1000*60*15;
		 int maxElements = 5;
		 int maxChar = 0; // 20000; //random limit. NOT yet supported.
		 
		 EnumerationResourceState retrievedValues = ((EnumerationResourceImpl)retrieved).pull(enumContext,maxTime,
				 maxElements,maxChar);
		 //Navigate down to retrieve Items children
		 	//Document Children
		 NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		  //PullResponse node
		 assertNotNull("No root node for PullResponse.",rootChildren);
         Node child = rootChildren.item(0);

		 //Items node
		 assertNotNull("No child node for PullResponse found.",child);
		 //Check number of enumerated values returned.
		 List<Node> children = retrievedValues.getEnumerationItems();
		 assertEquals("Incorrect number of elements returned!",
				 maxElements, children.size());
		 
		 //DONE: iterate through to make sure that we retrieved the correct item type and value
		 for(int i=0;i<children.size();i++){
			 Node node = children.get(i);
			 if(node.getNodeName().indexOf("EnumerationContext")>-1){
				 //ignore
			 }else{
				 UserType user = null;
				 String[] pkgList = {"com.hp.examples.ws.wsman.user"};
				 XmlBinding empBinding = new XmlBinding(null,pkgList);
				 JAXBElement<UserType> ob =
					 (JAXBElement<UserType>)empBinding.unmarshal(node);
				 user=(UserType)ob.getValue();
				 assertTrue(user.getFirstname().trim().equalsIgnoreCase(testName.trim()));
			 }
		 }
	
		 //DONE: test release
		 retrieved.release(enumContext);
		 
	 }
	 
	 public void testOptimizedEnumeration() throws SOAPException, JAXBException,
	 IOException, FaultException, DatatypeConfigurationException{
	 
		 //define enumeration handler url
		 String resourceUri = "wsman:auth/userenum";
	
		 SelectorSetType selectors = null;
		 //test that Find works to retrieve the Enumeration instance.
		 Resource[] enumerableResources = ResourceFactory.find(
		 ResourceImplTest.destUrl,
		 resourceUri,
		 ResourceImplTest.timeoutInMilliseconds,
		 selectors); 
		 
		 assertEquals("Expected one resource.",1,enumerableResources.length);
		 Resource retrieved = enumerableResources[0];
		 assertTrue(retrieved instanceof EnumerationResourceImpl);
		
		 //Build the filters
		 final String testName = "James";//See users.store for more valid search values
		 final String xpathFilter = "/user:user[user:firstname='"+testName+"']";
		 final Map<String, String> namespaces = new HashMap<String, String>(1);
		 namespaces.put("user", USER_NS);
 
		 //now test the pull mechanism
		 int maxTime =1000*60*15;
		 int maxElements = 10;
		 int maxChar = 0; // 20000; //random limit. NOT yet supported.
		
		 //Retrieve the Enumeration context.
		 EnumerationCtx enumContext = ((EnumerationResourceImpl)retrieved).enumerate(xpathFilter,namespaces,XPath.NS_URI,false,false, false, 
				 null, null, true, maxElements);
		 assertNotNull("Enum context retrieval problem.",enumContext);
		 assertTrue("Context id is empty.",(enumContext.getContext().trim().length()>0));
		
		 List<EnumerationItem> enumItems = ((EnumerationResourceImpl)retrieved).getEnumItems();
		 assertNotNull("No items returned in optimized enumeration", enumItems);
		 assertTrue("Wrong number of optimized enum items returned", enumItems.size() == maxElements);
		 
		 //iterate through to make sure that the correct fragment nodes are returned
		 for(int i=0;i<enumItems.size();i++){
			 EnumerationItem node = enumItems.get(i);
			 JAXBElement<UserType> item = (JAXBElement<UserType>) node.getItem();
			 assertTrue(item != null);
			 UserType user = item.getValue();
			 assertTrue(user.getFirstname().trim().equalsIgnoreCase(testName.trim()));
			 }
		 
		 EnumerationResourceState retrievedValues = ((EnumerationResourceImpl)retrieved).pull(enumContext,maxTime,
				 maxElements,maxChar, null, null);
		 //Navigate down to retrieve Items children
		 	//Document Children
		 NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		  //PullResponse node
		 assertNotNull("No root node for PullResponse.",rootChildren);
		 Node child = rootChildren.item(0);
		 //Items node
		 assertNotNull("No child node for PullResponse found.",child);
		 //Check number of enumerated values returned.
		 NodeList children = child.getChildNodes().item(1).getChildNodes();
		 assertEquals("Incorrect number of elements returned!",
				 maxElements, children.getLength());
		 
		
		 //DONE: test release
		 retrieved.release(enumContext);
	 }
		 	 
	
	 public void testFragmentOptimizedEnumeration() throws SOAPException, JAXBException,
	 IOException, FaultException, DatatypeConfigurationException{
	 
		 //define enumeration handler url
		 String resourceUri = "wsman:auth/userenum";
	
		 SelectorSetType selectors = null;
		 //test that Find works to retrieve the Enumeration instance.
		 Resource[] enumerableResources = ResourceFactory.find(
		 ResourceImplTest.destUrl,
		 resourceUri,
		 ResourceImplTest.timeoutInMilliseconds,
		 selectors); 
		 
		 assertEquals("Expected one resource.",1,enumerableResources.length);
		 Resource retrieved = enumerableResources[0];
		 assertTrue(retrieved instanceof EnumerationResourceImpl);
		
		 //Build the filters
		 final String testName = "James";//See users.store for more valid search values
		 final String xpathFilter = "/user:user[user:firstname='"+testName+"']/user:age";
		 final Map<String, String> namespaces = new HashMap<String, String>(1);
		 namespaces.put("user", USER_NS);

	     //Now build the XPath expression for fragment GET
		 String xPathReq = null; // TODO: "//*[local-name()='age']";
		 //now test the pull mechanism
		 int maxTime =1000*60*15;
		 int maxElements = 10;
		 int maxChar = 0; // 20000; //random limit. NOT yet supported.
		
		 //Retrieve the Enumeration context.
		 EnumerationCtx enumContext = ((EnumerationResourceImpl)retrieved).enumerate(xpathFilter,namespaces,XPath.NS_URI,false,false, false, 
				 xPathReq, null, true, maxElements);
		 assertNotNull("Enum context retrieval problem.",enumContext);
		 assertTrue("Context id is empty.",(enumContext.getContext().trim().length()>0));
		
		 List<EnumerationItem> enumItems = ((EnumerationResourceImpl)retrieved).getEnumItems();
		 
		 assertNotNull("No items returned in optimized enumeration", enumItems);
		 assertTrue("Wrong number of optimized enum items returned", enumItems.size() == maxElements);
		 
		 // iterate through to make sure that the correct fragment nodes are returned
		 for(int i=0;i<enumItems.size();i++){
			 EnumerationItem node = enumItems.get(i);
			 Object item = node.getItem();
			 assertFalse(item == null);
			 assertTrue(item instanceof JAXBElement);
			 JAXBElement<MixedDataType> fragment = (JAXBElement<MixedDataType>) item;
			 assertTrue(fragment.getDeclaredType().equals(MixedDataType.class));
			 assertTrue(fragment.getName().equals(TransferExtensions.XML_FRAGMENT));
			 List<Object> fragments = fragment.getValue().getContent();
			 for (int j = 0; j < fragments.size(); j++) {
				 Object obj = fragments.get(j);
				 if (obj instanceof Element) {
			        assertTrue(((Element)obj).getNodeName().indexOf("age")>-1);
				 }
			 }
		 }
		 
		 EnumerationResourceState retrievedValues = ((EnumerationResourceImpl)retrieved).pull(enumContext,maxTime,
				 maxElements,maxChar, xPathReq, null);
		 //Navigate down to retrieve Items children
		 	//Document Children
		 NodeList rootChildren = retrievedValues.getDocument().getChildNodes();
		  //PullResponse node
		 assertNotNull("No root node for PullResponse.",rootChildren);
		 Node child = rootChildren.item(0);
		 //Items node
		 assertNotNull("No child node for PullResponse found.",child);
		 //Check number of enumerated values returned.
		 NodeList children = child.getChildNodes().item(1).getChildNodes();
		 assertEquals("Incorrect number of elements returned!",
				 maxElements, children.getLength());
		 
		 List <Node> enumList = retrievedValues.getEnumerationItems();
		 //iterate through to make sure that only XML Fragment nodes are returned
		 for(int i=0;i<enumList.size();i++){
			 Node node = enumList.get(i);
			 if(node.getNodeName().indexOf("EnumerationContext")>-1){
				 //ignore
			 }else {
				  // Make sure we have the XmlFragment
				  assertTrue(node.getNodeName().indexOf("XmlFragment")>-1);
				  // Now make sure at least one field was returned
				  assertTrue(node.getFirstChild() != null);
				  // Now make sure that the right field for the XPATH expression is returned
				  assertTrue(node.getFirstChild().getNodeName().indexOf("age") > -1);
			 }
		 }
		 
		
		 //DONE: test release
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
		JAXBElement<UserType> ob = (JAXBElement<UserType>) binding
				.unmarshal(userChildNode);
		user = (UserType) ob.getValue();

		return user;
	}

	public void locateClass(String className){
		 if (!className.startsWith("/")) {
	         className = "/" + className;
	       }
	       className = className.replace('.', '/');
	       className = className + ".class";
	      java.net.URL classUrl =
	        EnumerationResourceImpl.class.getResource(className);
	      if(classUrl != null) {
	        System.out.println("\nClass '" + className +
	          "' found in \n'" + classUrl.getFile() + "'");
	      } else {
	        System.out.println("\nClass '" + className +
	          "' not found in \n'" +
	          System.getProperty("java.class.path") + "'");
	      }
	}
	
}
