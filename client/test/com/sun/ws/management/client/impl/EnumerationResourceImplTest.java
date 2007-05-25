package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import util.WsManBaseTestSupport;

import com.sun.ws.management.Management;
import com.sun.ws.management.client.EnumerationCtx;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceFactory;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.exceptions.FaultException;

public class EnumerationResourceImplTest extends WsManBaseTestSupport  {

	private static final String destUrl = "http://localhost:8080/wsman/";
	private static final String resourceUri = "wsman:auth/userenum";
	private static final String USER_CUSTOM_DIALECT = "http://examples.hp.com/ws/wsman/user/filter/custom";
	private static final long timeout=10000;
	
	protected void setUp() throws Exception {
		super.setUp();
		try {
			new Management();
		} catch (SOAPException e) {
			fail("Can't init wiseman");
		}
		/*
		try {
			Message.initialize();
		} catch (SOAPException e) {
			fail("Can't init wiseman");
		}
		*/
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
		Resource[] enumerationResources = ResourceFactory.find(destUrl,resourceUri,timeout,(Map<String,String>)null);
		assertNotNull(enumerationResources);
		assertTrue(enumerationResources.length>0);
		assertNotNull(enumerationResources[0]);
		Resource enumerationResource = enumerationResources[0];
		assertNotNull(enumerationResource);
		EnumerationCtx ticket = enumerationResource.enumerate(null, null, null, true, false);
		assertNotNull(ticket);
		ResourceState pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		assertNotNull(pullResult);
		pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		assertNotNull(pullResult);
		pullResult = enumerationResource.pull(ticket, 30000, 20, -1);
		assertNotNull(pullResult);
		enumerationResource.release(ticket);
	}
	
	public void testCustomDialect() throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException{
		// Obtain an reference to a resource that represents an enum of users
		Resource[] enumerationResources = ResourceFactory.find(destUrl,resourceUri,timeout,(Map<String,String>)null);
		assertNotNull(enumerationResources);
		assertTrue(enumerationResources.length>0);
		assertNotNull(enumerationResources[0]);
		Resource enumerationResource = enumerationResources[0];
		EnumerationCtx ticket = enumerationResource.enumerate("Simpson", null, USER_CUSTOM_DIALECT, true, false);
		assertNotNull(ticket);
		ResourceState pullResult = enumerationResource.pull(ticket, 30000, 4, -1);
		enumerationResource.release(ticket);

	}

}
