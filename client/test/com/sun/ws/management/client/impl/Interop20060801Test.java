package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import util.WsManBaseTestSupport;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.Management;
import com.sun.ws.management.client.EnumerationCtx;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceFactory;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.ServerIdentity;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;
import com.sun.ws.management.client.impl.EnumerationResourceImpl;
import com.sun.ws.management.client.impl.TransferableResourceImpl;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

/**
 *
 * @author wire
 *
 */
public class Interop20060801Test extends WsManBaseTestSupport {

	private static final QName LOWER_THRESHOLD_NON_CRITICAL = new QName("http://www.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_NumericSensor","LowerThresholdNonCritical");

	public Interop20060801Test(){

		Handler[] handlers =
	        Logger.getLogger( "" ).getHandlers();
	      for ( int index = 0; index < handlers.length; index++ ) {
	        handlers[index].setLevel( Level.FINE );
	      }

	     Logger loggerXfer = Logger.getLogger(TransferableResourceImpl.class.getName());
	     loggerXfer.setLevel(Level.FINE);
	     Logger loggerEnum = Logger.getLogger(EnumerationResourceImpl.class.getName());
	     loggerEnum.setLevel(Level.FINE);
		
	}
	public static String destination = "http://localhost:8080/wsman/";

	public static String resourceUri = "wsman:auth/user";

	public static long timeoutInMilliseconds = 9400000;

	XmlBinding binding = null;

	// Section 6 Variables
	private String resourceURIComputerSystem;
	private int maxEnvelopeSize;
	private String selectorCreationClassName;
	private String selectorName;
	private int operationTimeout;
	
	// Section 7 Variables
	private String resourceURINumericSensor;

	protected void setUp() throws Exception {
		super.setUp();

		// Initalize section 6 Variables to default values
		resourceURIComputerSystem = "http://www.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ComputerSystem";
		maxEnvelopeSize = 153600;
		selectorCreationClassName = "ComputerSystem";
		selectorName = "IPMI Controller 32";
		operationTimeout = 60000;

		// Initalize section 7 Variables to default values
		resourceURINumericSensor="http://www.dmtf.org/wbem/wscim/1/cim-schema/2/cim_numericsensor";

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 6.1	Identify (mandatory)
	 * Identify is used to retrieve information about the WS-Management stack.
	 * @throws JAXBException 
	 * @throws IOException 
	 * @throws SOAPException 
	 * @throws NoMatchFoundException 
	 * @throws XPathExpressionException 
	 *
	 */
	public void testIdentify() throws SOAPException, IOException, JAXBException, XPathExpressionException, NoMatchFoundException{
		System.out.println("_________________________________________________________________________");
		System.out.println("6.1 Identify");
		ServerIdentity serverInfo = ResourceFactory.getIdentity(destination);
		System.out.println(serverInfo);
		assertNotNull(serverInfo);
		assertNotNull(serverInfo.getProductVendor());
		assertNotNull(serverInfo.getProductVersion());
		assertNotNull(serverInfo.getProtocolVersion());
		assertNotNull(serverInfo.getSpecVersion());
		assertNotNull(serverInfo.getBuildId());
		assertEquals(serverInfo.getProtocolVersion(),"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd");
	}
	
	/**
	 * 6.2	GET instance of CIM_ComputerSystem (mandatory)
	 * In this scenario, the client does a GET request to retrieve 
	 * an instance of CIM_ComputerSystem. Since this class has keys that 
	 * need to be specified, the request contains a selector set that 
	 * contains the values for the keys for the instance being retrieved.
	 * @throws DatatypeConfigurationException 
	 * @throws FaultException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 */
	public void testGetInstance() throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException{
		System.out.println("_________________________________________________________________________");
		System.out.println("6.2 Get Instance of CIM_ComputerSystem");
		ResourceState systemState = getComputerSystemState(resourceURIComputerSystem,selectorCreationClassName,selectorName,maxEnvelopeSize,operationTimeout);
		System.out.println("PASS");

		// TODO Verify Actual Values
		
	}


	/**
	 * 6.3	GET failure with invalid resource URI (mandatory)
	 * In this scenario, the client does a GET with an invalid resource URI 
	 * resulting in the server returning an error response.
	 * @throws DatatypeConfigurationException 
	 * @throws  
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 */
	public void testGetInstanceBadURI() throws SOAPException, JAXBException, IOException, DatatypeConfigurationException{
		System.out.println("_________________________________________________________________________");
		System.out.println("6.3 GET failure with invalid resource URI");
		resourceURIComputerSystem = "http://www.dmtf.org/wbem/wscim/1/cim-schema/2/cim_computersyste";
		try {
			ResourceState systemState = getComputerSystemState(resourceURIComputerSystem,selectorCreationClassName,selectorName,maxEnvelopeSize,operationTimeout);
		} catch (FaultException e) {
			System.out.println("PASS");
			return;
		}
		fail("This test is expected to Fault and did not.");
	}
	
	/**
	 * 6.4	Get failure with maxenvelopesize exceeded error (mandatory)
	 * In this scenario, the response exceeds the MaxEnvelopeSize configured on the server resulting 
	 * in an error response.
	 * @throws DatatypeConfigurationException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 */
	public void testGetInstanceFailEnvelopeSize() throws SOAPException, JAXBException, IOException, DatatypeConfigurationException{
		System.out.println("_________________________________________________________________________");
		System.out.println("6.4 Get failure with maxenvelopesize exceeded error");
		int maxEnvelopeSize=8;
		try {
			ResourceState systemState = getComputerSystemState(resourceURIComputerSystem,selectorCreationClassName,selectorName,maxEnvelopeSize,operationTimeout);
		} catch (FaultException e) {
			System.out.println("PASS");
			return;
		}
		fail("This test is expected to Fault and did not.");

	}
	
	/**
	 * 6.5	Get failure with invalid selectors (mandatory)
	 * In this scenario, the client does a GET with a missing selector resulting in the server returning 
	 * an error response.
	 * @throws DatatypeConfigurationException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 */
	public void testGetInstanceMissingSelector() throws SOAPException, JAXBException, IOException, DatatypeConfigurationException{
		System.out.println("_________________________________________________________________________");
		System.out.println("6.5 Get failure with invalid selectors");
		String selectorName=null;
		try {
			ResourceState systemState = getComputerSystemState(resourceURIComputerSystem,selectorCreationClassName,selectorName,maxEnvelopeSize,operationTimeout);
		} catch (FaultException e) {
			System.out.println("PASS");
			return;
		}
		fail("This test is expected to Fault and did not.");
		
	}

	/**
	 * 6.6	Get failure with operational timeout (mandatory)
	 * In this scenario, the request fails since the service cannot respond in the 
	 * time specified in the OperationTimeout in the request.
	 */
	public void testGetInstanceTimeoutExceeded() throws SOAPException, JAXBException, IOException, DatatypeConfigurationException{
		System.out.println("_________________________________________________________________________");
		System.out.println("6.6	Get failure with operational timeout ");
		int operationTimeout=1;
		try {
			ResourceState systemState = getComputerSystemState(resourceURIComputerSystem,selectorCreationClassName,selectorName,maxEnvelopeSize,operationTimeout);
		} catch (FaultException e) {
			System.out.println("PASS");
			return;
		}
		fail("This test is expected to Fault and did not.");	
	}
	
	/**
	 * 6.7	Fragment Get (optional)
	 * In this scenario, a filter is specified to get a single property within 
	 * an instance of CIM_NumericSensor. XPATH is the recommended filter dialect. 
	 */
	//TODO Optional
	
	
	/**
	 * 7.1	Enumerate instances of CIM_NumericSensor (mandatory)
	 * In this scenario, the client does an enumeration request to retrieve all instances of CIM_NumericSensor class.
	 * 
	 * The sequence of operations is as follows:
	 * ¥	Client sends enumeration request
	 * ¥	Server sends response along with enumeration context A
	 * ¥	Client does a Pull request using enumeration context A
	 * ¥	Server responds with results along with enumeration context B
	 * ¥	Client does a Pull request using enumeration context B
	 * ¥	This continues until Server responds with results and EndOfSequence indicating there 
	 * 			are no more results
	 * @throws DatatypeConfigurationException 
	 * @throws FaultException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 * @throws NoMatchFoundException 
	 * @throws XPathExpressionException 
	 */
	public void testEnumerateInstances() throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException, XPathExpressionException {
		System.out.println("_________________________________________________________________________");
		System.out.println("7.1	Enumerate instances of CIM_NumericSensor");
		QName END_OF_SEQUENCE = new QName("http://schemas.xmlsoap.org/ws/2004/09/enumeration","EndOfSequence");
		HashMap<String, String> selectors = null;
		Resource[] numericSensorEnumSet = ResourceFactory.find(destination, resourceURINumericSensor, operationTimeout, selectors);
		assertTrue(numericSensorEnumSet.length>0);
		Resource numericSensorEnum = numericSensorEnumSet[0];
		numericSensorEnum.setMaxEnvelopeSize(maxEnvelopeSize);
		numericSensorEnum.setMessageTimeout(operationTimeout);
		EnumerationCtx contextA = numericSensorEnum.enumerate(null, numericSensorEnum.XPATH_DIALECT, false,false);
		int maxElements = 1;
		int maxCharacters = -1;
		boolean moreResults = true;
		int passcount=0;
		try {
			while(moreResults&&passcount<50){
				ResourceState pullResult = numericSensorEnum.pull(contextA, operationTimeout, maxElements, maxCharacters);
				try{
				if(pullResult.getWrappedValueText(END_OF_SEQUENCE)!=null)
					// End of SEQ found, end iteration.
					moreResults=false;
				} catch(NoMatchFoundException e){
					passcount++;
				}
			}
		} catch (FaultException e){
			fail("A fault occured during iteration: "+e.getMessage());
		}
		assertTrue(passcount<50);
		System.out.println("PASS");
	}
	
	/**
	 * 7.2	Optmized Enumeration (optional)
	 * In this scenario, the client does an enumeration request to retrieve 
	 * all instances of CIM_NumericSensor class using optimized enumeration.
	 */
	//TODO Optional
	
	/**
	 * 7.3	Enumerate Failure (mandatory)
	 * In this scenario, the client does an enumeration request to retrieve all instances of 
	 * CIM_NumericSensor class, when doing the Pull request, it passes an invalid enumeration 
	 * context which is different from the one received from the server. This results in an 
	 * error being returned by the server.
	 * 
	 * The sequence of operations is as follows:
	 * ¥	Client sends enumeration request
	 * ¥	Server sends response along with enumeration context A
	 * ¥	Client does a Pull request using enumeration context A
	 * ¥	Server responds with results along with enumeration context B
	 * ¥	Client does a Pull request using enumeration context C
	 * ¥	Server returns an error
	 * @throws DatatypeConfigurationException 
	 * @throws FaultException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 * @throws XPathExpressionException 
	 */
	public void testEnumerateFailureBadContext() throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException, XPathExpressionException{
		String contextBogas="uuid:bogas-context-0000-00000-0000";
		System.out.println("_________________________________________________________________________");
		System.out.println("7.3	Enumerate Failure");
		QName END_OF_SEQUENCE = new QName("http://schemas.xmlsoap.org/ws/2004/09/enumeration","EndOfSequence");
		HashMap<String, String> selectors = null;
		Resource[] numericSensorEnumSet = ResourceFactory.find(destination, resourceURINumericSensor, operationTimeout, selectors);
		assertTrue(numericSensorEnumSet.length>0);
		Resource numericSensorEnum = numericSensorEnumSet[0];
		numericSensorEnum.setMaxEnvelopeSize(maxEnvelopeSize);
		numericSensorEnum.setMessageTimeout(operationTimeout);
		EnumerationCtx contextA = numericSensorEnum.enumerate(null, numericSensorEnum.XPATH_DIALECT, false,false);
		int maxElements = 1;
		int maxCharacters = -1;
		boolean moreResults = true;
		int passcount=0;
		ResourceState pullResult = null;
		try {
			while(moreResults&&passcount<50){
				if(passcount==1)
					contextA.setContext(contextBogas);
				try {
					pullResult = numericSensorEnum.pull(contextA, operationTimeout, maxElements, maxCharacters);
				} catch (FaultException e){
					if(passcount==1)
						return;//PASSED!
					throw e;
				}
				
				try{
				if(pullResult.getWrappedValueText(END_OF_SEQUENCE)!=null)
					// End of SEQ found, end iteration.
					moreResults=false;
				} catch(NoMatchFoundException e){
					// Not found, Keep looking
					passcount++;
				}
			}
			
		} catch (FaultException e){
			fail("A fault occured during iteration: "+e.getMessage());
		}
		assertTrue(passcount<50);
		System.out.println("PASS");
	}

	/**
	 * 7.4	Enumerate ObjectAndEPR (optional)
	 * One of the common scenarios is to enumerate object and EPRs for a given resource URI, 
	 * this URI could correspond to a standard CIM class. The returned instances can correspond
	 * to the base class as well as derived classes (if polymorphism is suppored). The EPR 
	 * thatÕs returned along with the instance, can be used for modifying the instance using a 
	 * Put.
	 */
	//TODO Optional
	
	/**
	 * 7.5	Filtered Enumeration with XPath filter dialect (optional)
	 * In this scenario, an XPATH filter is used to retrieve a subset of instances where 
	 * SensorType = 2. 
	 */
	//TODO Optional
	
	/**
	 * 7.6	Filtered enumeration using Selector filter dialect (optional)
	 * PrevioPrevious scenario (7.5) is repeated with Selector filter dialect.
	 */
	//TODO Optional
	
	/**
	 * 8.1	Invoke ClearLog on an instance of RecordLog class (optional)
	 * In this scenario, the log records in one of the RecordLogs is deleted using the 
	 * ClearLog action.
	 */	
	//TODO Optional
	
	/**
	 * 9.1	Change Threshold on an instance of CIM_NumericSensor (mandatory)
	 * In this scenario, the client changes the value of LowerThresholdNonCritical threshold
	 * on an instance of CIM_NumericSensor class.
	 * @throws DatatypeConfigurationException 
	 * @throws FaultException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 * @throws XPathExpressionException 
	 * @throws NoMatchFoundException 
	 */	
	public void testPutThreshhold() throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException, XPathExpressionException, NoMatchFoundException{
		System.out.println("_________________________________________________________________________");
		System.out.println("9.1	Change Threshold on an instance of CIM_NumericSensor");
		
//		<w:Selector Name="CreationClassName">CIM_NumericSensor</w:Selector>
//		<w:Selector Name="DeviceID">81.0.32</w:Selector>
//		<w:Selector Name="SystemCreationClassName">ComputerSystem</w:Selector>
//		<w:Selector Name="SystemName">IPMI Controller 32</w:Selector>

		HashMap<String, String> selectors1 = new HashMap<String,String>();
		selectors1.put("CreationClassName", "CIM_NumericSensor");
		selectors1.put("DeviceID", "81.0.32");
		selectors1.put("SystemCreationClassName", "ComputerSystem");
		selectors1.put("SystemName", "IPMI Controller 32");

		Resource[] resources = ResourceFactory.find(destination, "http://www.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_NumericSensor", operationTimeout, selectors1);
		assertTrue(resources.length>0);
		Resource compSysResource = resources[0];
		ResourceState sensorState = compSysResource.get();
		String xPathExpression = "//*[local-name()='LowerThresholdNonCritical']";
		sensorState.setFieldValues(xPathExpression, "50");
		ResourceState newState = compSysResource.put(sensorState);
		assertEquals("50",sensorState.getValueText(xPathExpression));
		
		System.out.println("PASS");

	}

	/**
	 * 9.2	Fragment Transfer (Optional)
	 * In this scenario, a single property (LowerThresholdNonCritical) of an instance of 
	 * CIM_NUmericSensor is updated using Fragment transfer.
	 */
	//TODO Optional
	
	/**
	 * 10	Eventing (Optional)
	 * In this scenario, Pull based subscription is used to retrieve events.
	 */
	//TODO Optional
	private ResourceState getComputerSystemState(String resourceURI,String selectorCreationClassName,String selectorName,int maxEnvelopeSize,int operationTimeout) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
		HashMap<String, String> selectors = new HashMap<String,String>();
		selectors.put("CreationClassName", selectorCreationClassName);
		if(selectorName!=null)
			selectors.put("Name", selectorName);
		Resource[] computerSystemResources = ResourceFactory.find(destination, resourceURI, operationTimeout, selectors);
		assertTrue(computerSystemResources.length>0);
		Resource computerSystemResource = computerSystemResources[0];
		computerSystemResource.setMaxEnvelopeSize(maxEnvelopeSize);
		return computerSystemResource.get();
	}
	
}
