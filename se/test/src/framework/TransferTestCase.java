package framework;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;

import util.WsManBaseTestSupport;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.Management;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XmlBinding;

public class TransferTestCase extends WsManBaseTestSupport {
	private static final String RESOURCE_URI = "wsman:auth/user";
	private static final String DESTINATION = "http://localhost:8080/wsman/";
	ObjectFactory userFactory=new ObjectFactory();
	private XmlBinding binding;
	public TransferTestCase() {
		super();
//		try {
//			Message.initialize();
//			//new Management();
//		} catch (SOAPException e) {
//			fail("Can't init wiseman");
//		} 
		try {
			binding = new XmlBinding(null,"com.hp.examples.ws.wsman.user");
		} catch (JAXBException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Objective: Issue a create request for a UserObject to a back end model
	 * through a WS-Man create request.
	 * 
	 * Creates a Create request:<p><pre>
	 * &lt;?xml version="1.0" encoding="UTF-8"?>
	 * &lt;env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
  	 * xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
  	 * xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
  	 * xmlns:wsen="http://schemas.xmlsoap.org/ws/2004/09/enumeration"
  	 * xmlns:wsman="http://schemas.xmlsoap.org/ws/2005/06/management"
  	 * xmlns:wsmancat="http://schemas.xmlsoap.org/ws/2005/06/wsmancat"
  	 * xmlns:wxf="http://schemas.xmlsoap.org/ws/2004/09/transfer" xmlns:xs="http://www.w3.org/2001/XMLSchema">
  	 * &lt;env:Header>
     * &lt;wsa:Action env:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/09/transfer/Create&lt;/wsa:Action>
	 *     	 &lt;wsa:ReplyTo>
	 *       	 &lt;wsa:Address env:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous&lt;/wsa:Address>
	 *     	 &lt;/wsa:ReplyTo>
	 *     	 &lt;wsa:MessageID env:mustUnderstand="true">uuid:86e31cbd-be08-45bb-93b0-74a99a92c7df&lt;/wsa:MessageID>
	 *     	 &lt;wsa:To env:mustUnderstand="true">http://localhost:8080/hpwsman/&lt;/wsa:To>
	 *     &lt;wsman:ResourceURI>wsman:auth/user&lt;/wsman:ResourceURI>
	 *     &lt;wsman:OperationTimeout>PT30.000S&lt;/wsman:OperationTimeout>
	 *   &lt;/env:Header>
	 *   &lt;env:Body>
	 *     &lt;ns9:user
	 *       xmlns:ns2="http://schemas.xmlsoap.org/ws/2004/08/addressing"
	 *       xmlns:ns3="http://schemas.xmlsoap.org/ws/2004/08/eventing"
	 *       xmlns:ns4="http://schemas.xmlsoap.org/ws/2004/09/enumeration"
	 *       xmlns:ns5="http://www.w3.org/2003/05/soap-envelope"
	 *       xmlns:ns6="http://schemas.xmlsoap.org/ws/2004/09/transfer"
	 *       xmlns:ns7="http://schemas.xmlsoap.org/ws/2005/06/management"
	 *       xmlns:ns8="http://schemas.xmlsoap.org/ws/2005/06/wsmancat" xmlns:ns9="http://examples.hp.com/ws/wsman/user">
	 *       &lt;ns9:firstname>Joe&lt;/ns9:firstname>
	 *       &lt;ns9:lastname>Finkle&lt;/ns9:lastname>
	 *       &lt;ns9:address>6000 Irwin Drive&lt;/ns9:address>
	 *       &lt;ns9:city>Mount Laurel&lt;/ns9:city>
	 *       &lt;ns9:state>NJ&lt;/ns9:state>
	 *       &lt;ns9:zip>08054&lt;/ns9:zip>
	 *       &lt;ns9:age>16&lt;/ns9:age>
	 *     &lt;/ns9:user>
	 *   &lt;/env:Body>
	 * &lt;/env:Envelope>
	 * </pre>
	 *  And Expects a response like this.<p>
	 * <pre>
	 * 	   &lt;?xml version="1.0" encoding="UTF-8"?>
	 * &lt;env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
	 *   xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
	 *   xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
	 *   xmlns:wsen="http://schemas.xmlsoap.org/ws/2004/09/enumeration"
	 *   xmlns:wsman="http://schemas.xmlsoap.org/ws/2005/06/management"
	 *   xmlns:wsmancat="http://schemas.xmlsoap.org/ws/2005/06/wsmancat"
	 *   xmlns:wxf="http://schemas.xmlsoap.org/ws/2004/09/transfer" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	 *   &lt;env:Header>
	 *     &lt;wsa:MessageID env:mustUnderstand="true">uuid:2fd80112-ac60-4185-89da-dd3f91b535f3&lt;/wsa:MessageID>
	 *     &lt;wsa:RelatesTo>uuid:0a9cf968-a992-46a1-bf9b-2affb6b63364&lt;/wsa:RelatesTo>
	 *     &lt;wsa:Action env:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse&lt;/wsa:Action>
	 *     &lt;wsa:To env:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous&lt;/wsa:To>
	 *   &lt;/env:Header>
	 *   &lt;env:Body>
	 *     &lt;ns6:CreateResponse
	 *       xmlns:ns2="http://schemas.xmlsoap.org/ws/2004/08/addressing"
	 *       xmlns:ns3="http://schemas.xmlsoap.org/ws/2004/08/eventing"
	 *       xmlns:ns4="http://schemas.xmlsoap.org/ws/2004/09/enumeration"
	 *       xmlns:ns5="http://www.w3.org/2003/05/soap-envelope"
	 *       xmlns:ns6="http://schemas.xmlsoap.org/ws/2004/09/transfer"
	 *       xmlns:ns7="http://schemas.xmlsoap.org/ws/2005/06/management" xmlns:ns8="http://schemas.xmlsoap.org/ws/2005/06/wsmancat">
	 *       &lt;ResourceCreated>
	 *         &lt;ns2:Address ns5:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous&lt;/ns2:Address>
	 *         &lt;ns2:ReferenceParameters>
	 *           &lt;ns7:ResourceURI>wsman:auth/user&lt;/ns7:ResourceURI>
	 *           &lt;ns7:SelectorSet>
	 *             &lt;ns7:Selector Name="lastname">Finkle&lt;/ns7:Selector>
	 *             &lt;ns7:Selector Name="firstname">Joe&lt;/ns7:Selector>
	 *           &lt;/ns7:SelectorSet>
	 *         &lt;/ns2:ReferenceParameters>
	 *       &lt;/ResourceCreated>
	 *     &lt;/ns6:CreateResponse>
	 *   &lt;/env:Body>
	 * &lt;/env:Envelope>
	 * </pre>
	 * 
	 * NOTE: The create response wrapper is a mistake in the schema of wsman and
	 * should be removed in the future.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 */
	
	public void testCreate() throws SOAPException, JAXBException, DatatypeConfigurationException, IOException{
		SelectorSetType ssType = createResource("Finkle","Joe");

		// Examine the selectors
		// Lastname
		List<SelectorType> selectors = ssType.getSelector();
		assertNotNull(selectors.get(0));
		SelectorType selectorLastName = selectors.get(0);
		assertEquals("lastname",selectorLastName.getName());
		assertEquals("Finkle",selectorLastName.getContent().get(0));
		
		// Firstname
		assertNotNull(selectors.get(1));
		SelectorType selectorFirstName = selectors.get(1);
		assertEquals("firstname",selectorFirstName.getName());
		assertEquals("Joe",selectorFirstName.getContent().get(0));		
	}

/**
 * Objective: Create a resource, use the returned selector to get it contents
 * then verify that the returned contents matches the orgiginal data.
 * 
 * The get request looks like this: 
 * 
 * &lt;lt;?xml version="1.0" encoding="UTF-8"?>
 * &lt;lt;env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
 *   xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
 *   xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
 *   xmlns:wsen="http://schemas.xmlsoap.org/ws/2004/09/enumeration"
 *   xmlns:wsman="http://schemas.xmlsoap.org/ws/2005/06/management"
 *   xmlns:wsmancat="http://schemas.xmlsoap.org/ws/2005/06/wsmancat"
 *   xmlns:wxf="http://schemas.xmlsoap.org/ws/2004/09/transfer" xmlns:xs="http://www.w3.org/2001/XMLSchema">
 *   &lt;lt;env:Header>
 *     &lt;lt;wsa:Action env:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/09/transfer/Get&lt;/wsa:Action>
 *     &lt;lt;wsa:ReplyTo>
 *       &lt;lt;wsa:Address env:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous&lt;/wsa:Address>
 *     &lt;lt;/wsa:ReplyTo>
 *     &lt;lt;wsa:MessageID env:mustUnderstand="true">uuid:a75fa0fb-a61f-4c8b-86a3-4501df280715&lt;/wsa:MessageID>
 *     &lt;lt;wsa:To env:mustUnderstand="true">http://localhost:8080/hpwsman/&lt;/wsa:To>
 *     &lt;lt;wsman:ResourceURI>wsman:auth/user&lt;/wsman:ResourceURI>
 *     &lt;lt;wsman:OperationTimeout>PT5M0.000S&lt;/wsman:OperationTimeout>
 *     &lt;lt;wsman:SelectorSet>
 *       &lt;lt;wsman:Selector Name="lastname">Test&lt;/wsman:Selector>
 *       &lt;lt;wsman:Selector Name="firstname">Get&lt;/wsman:Selector>
 *     &lt;lt;/wsman:SelectorSet>
 *   &lt;lt;/env:Header>
 *   &lt;lt;env:Body/>
 * &lt;lt;/env:Envelope>
 *
 * The response looks like this:
 *
 * &lt;lt;?xml version="1.0" encoding="UTF-8"?>
 * &lt;lt;env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
 *   xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
 *   xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
 *   xmlns:wsen="http://schemas.xmlsoap.org/ws/2004/09/enumeration"
 *   xmlns:wsman="http://schemas.xmlsoap.org/ws/2005/06/management"
 *   xmlns:wsmancat="http://schemas.xmlsoap.org/ws/2005/06/wsmancat"
 *   xmlns:wxf="http://schemas.xmlsoap.org/ws/2004/09/transfer" xmlns:xs="http://www.w3.org/2001/XMLSchema">
 *   &lt;lt;env:Header>
 *     &lt;lt;wsa:MessageID env:mustUnderstand="true">uuid:bdc58003-ddc7-4d0e-9414-fc8c8d34eb38&lt;/wsa:MessageID>
 *     &lt;lt;wsa:RelatesTo>uuid:043982ff-e821-4404-b337-ab071313a8b9&lt;/wsa:RelatesTo>
 *     &lt;lt;wsa:Action env:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse&lt;/wsa:Action>
 *     &lt;lt;wsa:To env:mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous&lt;/wsa:To>
 *   &lt;lt;/env:Header>
 *   &lt;lt;env:Body>
 *     &lt;lt;ns9:user
 *       xmlns:ns2="http://schemas.xmlsoap.org/ws/2004/08/addressing"
 *       xmlns:ns3="http://schemas.xmlsoap.org/ws/2004/08/eventing"
 *       xmlns:ns4="http://schemas.xmlsoap.org/ws/2004/09/enumeration"
 *       xmlns:ns5="http://www.w3.org/2003/05/soap-envelope"
 *       xmlns:ns6="http://schemas.xmlsoap.org/ws/2004/09/transfer"
 *       xmlns:ns7="http://schemas.xmlsoap.org/ws/2005/06/management"
 *       xmlns:ns8="http://schemas.xmlsoap.org/ws/2005/06/wsmancat" xmlns:ns9="http://examples.hp.com/ws/wsman/user">
 *       &lt;lt;ns9:firstname>Get&lt;/ns9:firstname>
 *       &lt;lt;ns9:lastname>Test&lt;/ns9:lastname>
 *       &lt;lt;ns9:address>6000 Irwin Drive&lt;/ns9:address>
 *       &lt;lt;ns9:city>Mount Laurel&lt;/ns9:city>
 *       &lt;lt;ns9:state>NJ&lt;/ns9:state>
 *       &lt;lt;ns9:zip>08054&lt;/ns9:zip>
 *       &lt;lt;ns9:age>16&lt;/ns9:age>
 *     &lt;lt;/ns9:user>
 *   &lt;lt;/env:Body>
 * &lt;lt;/env:Envelope>
 * 
 * @throws JAXBException
 * @throws SOAPException
 * @throws DatatypeConfigurationException
 * @throws IOException
 */	
	@SuppressWarnings("unchecked")
	public void testGet() throws JAXBException, SOAPException, DatatypeConfigurationException, IOException{
		// create, will throw an exception on fail
		SelectorSetType ssType = createResource("Test","Get");
		
		// Extract returned selectors and use them to stage a request
		// Examine the selectors
		// Lastname
		List<SelectorType> selectors = ssType.getSelector();
		assertTrue(selectors.size()==2);

		// Now use these selectors to request back the newly created object 
		HashSet<SelectorType> selectors1 = createSelector("Test","Get");
		Management response = sendGetRequest(DESTINATION,RESOURCE_URI, selectors1);

		// Now verify the values of each returned field
		UserType user = (UserType)(((JAXBElement<UserType>)binding.unmarshal(response.getBody().getFirstChild())).getValue());
		assertEquals("6000 Irwin Drive",user.getAddress());
		assertEquals("Mount Laurel",user.getCity());
		assertEquals("Test",user.getLastname());
		assertEquals("Get",user.getFirstname());
		assertEquals("NJ",user.getState());
		assertEquals("08054",user.getZip());
		assertEquals(16,(int)user.getAge());
		
	}

private HashSet<SelectorType> createSelector(String lastnameValue, String firstnameValue) {
	HashSet<SelectorType> selectors1=new HashSet<SelectorType>();
	SelectorType lastNameSelector = new SelectorType();
	lastNameSelector.setName("lastname");
	lastNameSelector.getContent().add(lastnameValue);
	selectors1.add(lastNameSelector);
	SelectorType firstNameSelector = new SelectorType();
	firstNameSelector.setName("firstname");
	firstNameSelector.getContent().add(firstnameValue);
	selectors1.add(firstNameSelector);
	return selectors1;
}

	@SuppressWarnings("unchecked")
	public void testPut() throws JAXBException, SOAPException, DatatypeConfigurationException, IOException{
		// create, will throw an exception on fail
		SelectorSetType ssType = createResource("Test","Put");
		
		// Extract returned selectors and use them to stage a request
		// Examine the selectors, there should be two, lastname and firstname
		List<SelectorType> selectors = ssType.getSelector();
		assertTrue(selectors.size()==2);
		
		// Now make a change in the address and attempt to write it back
		// This could be done with a fragement request but at this point
		// we are not assumeing fragment support so we will get the resource
		// state and then edit a replace it as a test
		// Now use these selectors to request back the newly created object 
		HashSet<SelectorType> selectors1 = createSelector("Test","Put");
		Management getResponse = sendGetRequest(DESTINATION,RESOURCE_URI, selectors1);
		UserType user = (UserType)(((JAXBElement<UserType>)binding.unmarshal(getResponse.getBody().getFirstChild())).getValue());
		
		// Now edit and put it back
		user.setAddress("500 Briggs Road");
		user.setAge(50);
		
		Document doc = Management.newDocument();		
		binding.marshal(userFactory.createUser(user),doc);
		
		sendPutRequest(DESTINATION,RESOURCE_URI,selectors1,doc);
		
		Management getVerifyResponse = sendGetRequest(DESTINATION,RESOURCE_URI, selectors1);
		UserType userToVerify = (UserType)(((JAXBElement<UserType>)binding.unmarshal(getVerifyResponse.getBody().getFirstChild())).getValue());

		assertEquals("500 Briggs Road",userToVerify.getAddress());
		assertEquals(50,(int)userToVerify.getAge());
		
	}
	
	public void testDelete() throws SOAPException, JAXBException, DatatypeConfigurationException, IOException{
		// Create a resource
		createResource("Delete","Test");

		//Set<SelectorType> selectors=new HashSet<SelectorType>();
		HashSet<SelectorType> selectors = createSelector("Delete","Test");


		// Verify its presence
		sendGetRequest(DESTINATION,RESOURCE_URI, selectors);
		
		// Delete created resource
		sendDeleteRequest(DESTINATION,RESOURCE_URI, selectors);

		
		// Try to get it now that is has been deleted
		try {
			sendGetRequest(DESTINATION,RESOURCE_URI, selectors);
		} catch(SOAPException e){
			// Fault here indicates that resource has been removed
			return;
		}
		fail("Failed to delete the User Resource as part of the transfer delete test.");
		
	}
	
	@SuppressWarnings("unchecked")
	private SelectorSetType createResource(String lastName,String firstName) throws JAXBException, SOAPException, DatatypeConfigurationException, IOException {
		UserType user = userFactory.createUserType();
		user.setLastname(lastName);
		user.setFirstname(firstName);
		user.setAddress("6000 Irwin Drive");
		user.setCity("Mount Laurel");
		user.setState("NJ");
		user.setZip("08054");
		user.setAge(16);
		Document doc = Management.newDocument();
		JAXBElement<UserType> userElement =userFactory.createUser(user);		 
		binding.marshal(userElement,doc);
		Management ret = sendCreateRequest(DESTINATION,RESOURCE_URI,doc);
		
		// Verify the server response
		assertEquals(Transfer.CREATE_RESPONSE_URI,ret.getAction());
		
		// Confirm the presence of selectors
		JAXBElement jbe  = (JAXBElement) binding.unmarshal(ret.getBody().getFirstChild());
		EndpointReferenceType epr = (EndpointReferenceType)jbe.getValue();//createResponse.getResourceCreated();
		ReferenceParametersType props = epr.getReferenceParameters();
		List<Object> propList = props.getAny();
		
		// Is there a second RefProp (Selector Set)?
		assertNotNull(propList.get(1));
		assertTrue(propList.get(1) instanceof JAXBElement);
		SelectorSetType ssType=((JAXBElement<SelectorSetType>)propList.get(1)).getValue();
		return ssType;
	}

}
