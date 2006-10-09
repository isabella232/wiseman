package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import util.WsManBaseTestSupport;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.client.EnumerationCtx;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceFactory;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.xml.XmlBinding;

import junit.framework.TestCase;

public class EnumerationResourceImplTest extends WsManBaseTestSupport  {

	private XmlBinding binding;
	private ObjectFactory userFactory;
	private static String destUrl = "http://localhost:8080/wsman/";
	private static String resourceUri = "wsman:auth/userenum";
	private static long timeout=10000;
	protected void setUp() throws Exception {
		super.setUp();
		try {
			Message.initialize();
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
	
	/** 
	 * Enumerate Users
	 * @throws DatatypeConfigurationException 
	 * @throws FaultException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 * 
	 * This test is very weak. It was craeted until the original unit test code for this class can be found.
	 */
	public void testEnumerate() throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
		
		// Obtain an reference to a resource that represents an enum of users
		Map<String,String> selector=new HashMap<String,String>();
		selector.put("id", "1234");
		Resource[] enumerationResources = ResourceFactory.find(destUrl,resourceUri,timeout,selector);
		assertNotNull(enumerationResources);
		assertTrue(enumerationResources.length>0);
		assertNotNull(enumerationResources[0]);
		Resource enumerationResource = enumerationResources[0];
		EnumerationCtx ticket = enumerationResource.enumerate(null, null, true, false);
		assertNotNull(ticket);
		ResourceState pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		enumerationResource.release(ticket);
	}
	
	public void testCustomDialect() throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException{
		// Obtain an reference to a resource that represents an enum of users
		Map<String,String> selector=new HashMap<String,String>();
		selector.put("id", "1234");
		Resource[] enumerationResources = ResourceFactory.find(destUrl,resourceUri,timeout,selector);
		assertNotNull(enumerationResources);
		assertTrue(enumerationResources.length>0);
		assertNotNull(enumerationResources[0]);
		Resource enumerationResource = enumerationResources[0];
		EnumerationCtx ticket = enumerationResource.enumerate(new String[]{"Simpson"}, "UserCustomDialectLastname", true, false);
		assertNotNull(ticket);
		ResourceState pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		enumerationResource.release(ticket);

	}

}
