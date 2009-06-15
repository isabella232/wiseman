/*
 * Copyright 2006 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.12  2007/12/03 09:15:09  denis_rachal
 **General cleanup of Unit tests to make them easier to run and faster.
 **
 **Revision 1.11  2007/11/30 14:32:36  denis_rachal
 **Issue number:  140
 **Obtained from:
 **Submitted by:  jfdenise
 **Reviewed by:
 **
 **WSManAgentSupport and WSEnumerationSupport changed to coordinate their separate threads when handling wsman:OperationTimeout and wsen:MaxTime timeouts. If a timeout now occurs during an enumeration operation the WSEnumerationSupport is notified by the WSManAgentSupport thread. WSEnumerationSupport saves any items collected from the EnumerationIterator in the context so they may be fetched by the client on the next pull. Items are no longer lost on timeouts.
 **
 **Tests were added to correctly test this functionality and older tests were updated to properly test timeout functionality.
 **
 **Additionally some tests were updated to make better use of the XmlBinding object and improve performance on testing.
 **
 **Revision 1.10  2007/06/14 07:28:07  denis_rachal
 **Issue number:  110
 **Obtained from:
 **Submitted by:  ywu
 **Reviewed by:
 **
 **Updated wsman.war test warfile and sample traffic.war file along with tools to expose a WSDL file. Additionally added default index.html file for sample warfile.
 **
 **Revision 1.9  2007/06/08 15:38:39  denis_rachal
 **The following enhanceent were made to the testing infrastructure:
 **
 **  * Capture of logs in files for junits test
 **  * Added user.wsdl & user.xsd to wsman.war
 **  * Consolidated userenum & user into single handler that is thread safe for load testing
 **
 **Revision 1.8  2007/05/30 20:30:24  nbeers
 **Add HP copyright header
 **
 **
 * $Id: MetadataTest.java,v 1.13 2009-06-15 05:17:22 denis_rachal Exp $
 */

package management;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.java.dev.wiseman.schemas.metadata.messagetypes.MessageDefinitions;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.MetadataSection;

import util.TestBase;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.identify.IdentifyUtility;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.mex.MetadataUtility;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.handler.wsman.auth.eventcreator_Handler;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;

/**
 * Unit test for the MetaData operations
 */
public class MetadataTest extends TestBase {

	public MetadataTest(final String testName) {
		super(testName);
	}

	public static junit.framework.Test suite() {
		final junit.framework.TestSuite suite = new junit.framework.TestSuite(
				MetadataTest.class);
		return suite;
	}

	public static final String destUrl = ManagementMessageValues.WSMAN_DESTINATION;
	public static final String resourceUri = "wsman:auth/user";
	public static final String selector1 = "firstname=Get";
	public static final String selector2 = "lastname=Guy";
	public static final String metaDataCategory = "PERSON_RESOURCES";
	public static final String metaDataDescription = "This resource exposes people information "
			+ "stored in an LDAP repository.";
	public static final String metaDataMiscInfo = "Miscellaneous Information";
	public static final String metaDataUID = "UID_http://some.company.xyz/ldap/repository/2006/uid_00000000007";
	public static final String custQlocPart = "UserParam";
	public static final String custQnsuri = "http://sample.custom.com/custom/referenceParameter";
	public static final String custQprefix = "other";
	public static final String custQvalue = "param-value-1";
	public static final String custQlocPart1 = "UserProp";
	public static final String custQnsuri1 = "http://sample.custom.com/custom/referenceProperty";
	public static final String custQprefix1 = "cust";
	public static final String custQvalue1 = "prop-value-1";
	//embedded default addressing model instance
	public static final String embeddedAnnotTo = "http://embedded.anot.TO";
	public static final String embeddedAnnotResourceURI = "wsman:embe/resUri";
	public static final String embeddedAnnotResourceMetUID = "UID_http://some.company.xyz/ci/respository/2098764";
	//Enumeration testing constants
	public static final String enuAddress = "http://www.test.enu/address";
	public static final String enuResUri = "wsman:test/uri";
	public static final String enuAccessRecipe = "When accessing this enumeration use Get with last names.";
	public static final String enuFilUsage = "Filter is only operational for firstname, lastname, hiredate.";
	public static final String enuMetaDataUID = "UID_http://diff.sampleEnum.com/enumeration/employee/985739485";

	//Details for the schemas used
	public static final String schem1 = "wxf=http://schemas.xmlsoap.org/ws/2004/09/transfer";
	public static final String schem2 = "tl=http://schemas.wiseman.dev.java.net/traffic/1/light.xsd";

	//Details for the operation annotation fields added.
	public static final String op1Name = "Create";
	public static final String op1inpt = "tl:TrafficLightTypeMessage=http://schemas.xmlsoap.org/ws/2004/09/transfer/Create";
	public static final String op1outpt = "wxf:ResourceCreated=http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse";

	public static long timeoutInMilliseconds = 9400000;
	private int defAnnotCnt = 6;

	private static net.java.dev.wiseman.schemas.metadata.messagetypes.ObjectFactory metadataContent_fact = new net.java.dev.wiseman.schemas.metadata.messagetypes.ObjectFactory();

	/* Initialize the unit test variables for each test run.
	 * (non-Javadoc)
	 * @see management.TestBase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**Tests/exercises metadata functionality
	 *
	 * @throws Exception
	 */
	public void testMetaDataGet() throws Exception {

		//############ REQUEST THE METADATA REPOSITORY DATA ######################
		//Request identify info to get MetaData root information
		final Identify identify = new Identify();
		identify.setXmlBinding(binding);
		identify.setIdentify();

		//Send identify request
		final Addressing response = HttpClient.sendRequest(identify
				.getMessage(), DESTINATION);
		response.prettyPrint(logfile);
		if (response.getBody().hasFault()) {
			fail(response.getBody().getFault().getFaultString());
		}

		//Parse the identify response
		final Identify id = new Identify(response);
		final SOAPElement idr = id.getIdentifyResponse();
		assertNotNull(idr);
		SOAPElement el = IdentifyUtility.locateElement(id,
				AnnotationProcessor.META_DATA_RESOURCE_URI);
		assertNotNull("MetaDatResourceURI is null.", el);
		//retrieve the MetaData ResourceURI
		String resUri = el.getTextContent();
		assertNotNull("Retrieved resourceURI is null.", resUri);
		el = IdentifyUtility
				.locateElement(id, AnnotationProcessor.META_DATA_TO);
		assertNotNull("MetaDataTo is null", el);
		//retrieve the MetaData To/Destination
		String metTo = el.getTextContent();
		assertNotNull("Retrieved destination is null.", metTo);

		//############ REQUEST THE LIST OF METADATA AVAILABLE ######################
		//Build the GET request to be submitted for the metadata
		Management m = null;
		m = TransferUtility.createMessage(metTo, resUri,
				Transfer.GET_ACTION_URI, null, null, 30000, null);
		m.setXmlBinding(binding);

		//############ PROCESS THE METADATA RESPONSE ######################
		//Parse the getResponse for the MetaData
		final Addressing getResponse = HttpClient.sendRequest(m);
		getResponse.prettyPrint(logfile);
		Management mResp = new Management(getResponse);
		assertNull("A fault was detected.", mResp.getFault());

		//Retrieve the MetaData response to build JAXB type
		SOAPBody body = mResp.getBody();

		//Normal processing to create/retrieve the Metadata object
		Node metaDataNode = body.getFirstChild();
		try {
			//unmarshall the Metadata node content
			Object bound = binding.unmarshal(metaDataNode);

			Metadata ob = (Metadata) bound;

			//Parse the MetadataSections that exist
			List<MetadataSection> metaDataSections = ob.getMetadataSection();
			assertEquals(
					"The correct number of metadata sections were not found.",
					defAnnotCnt, metaDataSections.size());

			//############ PROCESS A METADATASECTION ######################
			//Examine Metadatasection attributes
			MetadataSection section = metaDataSections.get(0);
			assertEquals("Dialect does not match.", AnnotationProcessor.NS_URI,
					section.getDialect());
			assertEquals("Identifier does not match.",
					AnnotationProcessor.NS_URI, section.getIdentifier());

			//########### TRANSLATE METADATA TO FAMILIAR MANAGEMENT NODES #####
			//Extract the MetaData node returned as Management instances
			Management[] metaDataList = MetadataUtility
					.extractEmbeddedMetaDataElements(mResp);
			assertEquals(
					"Correct number of Management instances not returned.",
					defAnnotCnt, metaDataList.length);

			//locate the Management instances to evaluate.
			Management defAddModInst = null;
			Management embeddedDefModInst = null;
			Management enumModInst = null;
			for (int i = 0; i < metaDataList.length; i++) {
				Management inst = metaDataList[i];
				SOAPElement uid = ManagementUtility.locateHeader(inst
						.getHeaders(),
						AnnotationProcessor.RESOURCE_META_DATA_UID);
				assertNotNull("Unable to locate SoapElement for "
						+ AnnotationProcessor.RESOURCE_META_DATA_UID, uid);
				if (uid.getTextContent().equals(MetadataTest.metaDataUID)) {
					defAddModInst = inst;
				}
				if (uid.getTextContent().equals(
						MetadataTest.embeddedAnnotResourceMetUID)) {
					embeddedDefModInst = inst;
				}
				if (uid.getTextContent().equals(MetadataTest.enuMetaDataUID)) {
					enumModInst = inst;
				}
			}
			Management instance = defAddModInst;

			assertNotNull("Management instance not successfully retrieved.",
					instance);

			//Now parse the Dialect specif component.
			//############ VERIFY THE METADATA RESPONSE ######################
			//Verify expected contents.
			assertEquals("The values did not match.", destUrl, instance.getTo());
			assertEquals("The values did not match.", resourceUri, instance
					.getResourceURI());
			//Test MetaDataCategory
			SOAPElement locatedElement = ManagementUtility.locateHeader(
					instance.getHeaders(),
					AnnotationProcessor.META_DATA_CATEGORY);
			assertNotNull("Unable to locate MetaDataCategory element.",
					locatedElement);
			assertEquals("Values did not match.",
					MetadataTest.metaDataCategory, locatedElement
							.getTextContent());
			//Test MetaDataDescription
			locatedElement = ManagementUtility.locateHeader(instance
					.getHeaders(), AnnotationProcessor.META_DATA_DESCRIPTION);
			assertNotNull("Unable to locate MetaDataDescription element.",
					locatedElement);
			assertEquals("Values did not match.",
					MetadataTest.metaDataDescription, locatedElement
							.getTextContent());
			//Test MetaDataMiscInfo
			locatedElement = ManagementUtility.locateHeader(instance
					.getHeaders(), AnnotationProcessor.RESOURCE_MISC_INFO);
			assertNotNull("Unable to locate MetaDataMisc Info element.",
					locatedElement);
			assertEquals("Values did not match.",
					MetadataTest.metaDataMiscInfo, locatedElement
							.getTextContent());
			//Test MetaDataUID
			locatedElement = ManagementUtility.locateHeader(instance
					.getHeaders(), AnnotationProcessor.RESOURCE_META_DATA_UID);
			assertNotNull("Unable to locate MetaDataUID Info element.",
					locatedElement);
			assertEquals("Values did not match.", MetadataTest.metaDataUID,
					locatedElement.getTextContent());
			//verify SelectorSet from annotation is retrieved correctly...
			Map<String, String> selectorsRetrieved = null;
			selectorsRetrieved = ManagementUtility.extractSelectorsAsMap(
					selectorsRetrieved, (List) new ArrayList<SelectorType>(
							instance.getSelectors()));
			StringTokenizer selTok = new StringTokenizer(selector1, "=");
			String key = null;
			key = selTok.nextToken();
			assertTrue("First selector key was not found.", selectorsRetrieved
					.containsKey(key));
			String value = selTok.nextToken();
			assertEquals("First selector value was not found.", value,
					selectorsRetrieved.get(key));
			//Test second selector entered.
			selTok = new StringTokenizer(selector2, "=");
			key = selTok.nextToken();
			assertTrue("Second selector key was not found.", selectorsRetrieved
					.containsKey(key));
			value = selTok.nextToken();
			assertEquals("Second selector value was not found.", value,
					selectorsRetrieved.get(key));
			//Test Custom ReferenceParameter Types
			QName custQName = new QName(custQnsuri, custQlocPart, custQprefix);
			locatedElement = ManagementUtility.locateHeader(instance
					.getHeaders(), custQName);
			assertNotNull("Unable to locate CustomRefParam element.",
					locatedElement);
			assertEquals("Values did not match.", custQvalue, locatedElement
					.getTextContent());
			//Test Custom ReferenceProperty Types
			QName custQName1 = new QName(custQnsuri1, custQlocPart1,
					custQprefix1);
			locatedElement = ManagementUtility.locateHeader(instance
					.getHeaders(), custQName1);
			assertNotNull("Unable to locate CustomRefParam element.",
					locatedElement);
			assertEquals("Values did not match.", custQvalue1, locatedElement
					.getTextContent());

			//test that embedded annotations are also retrieved for handler with sets of addressing details.
			Management embedded = null;
			//Ensure that analysis is done on right element
			embedded = embeddedDefModInst;
			assertEquals("To values do not match",
					MetadataTest.embeddedAnnotTo, embedded.getTo());
			assertEquals("To values do not match",
					MetadataTest.embeddedAnnotResourceURI, embedded
							.getResourceURI());
			//TEST the Message Type definitions
			//Test that a body is returned in the metadata Management insts
			assertNotNull("No Metadata body found.", embedded.getBody());
			assertNotNull("No Metadata payload found.", embedded.getBody()
					.getFirstChild());
			//Convert response body to familiar JAXB type
			Node payload = embedded.getBody().getFirstChild();

			MessageDefinitions md = metadataContent_fact
					.createMessageDefinitions();
			Object unmarshalled = binding.unmarshal(payload);
			if (unmarshalled instanceof MessageDefinitions) {
				md = (MessageDefinitions) unmarshalled;
			}

			assertNotNull("MessageTypeDefinition node not found.", md);
			assertEquals("Operations count not correct.", 1, md.getOperations()
					.getOperation().size());
			assertEquals("Schemas count not correct.", 2, md.getSchemas()
					.getSchema().size());

		} catch (JAXBException e) {
			System.out.println("Exception:" + e.getMessage());
			fail("Unexpected error occurred:" + e.getMessage());
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		}
	}

	public void testMetaDataEnumeration() {
		try {

			Management[] metaDataList = MetadataUtility.getExposedMetadata(
					DESTINATION, -1);

			assertEquals("Array count incorrect.", defAnnotCnt,
					metaDataList.length);
			Management enumModInst = null;
			for (int i = 0; i < metaDataList.length; i++) {
				Management inst = metaDataList[i];
				SOAPElement uid = ManagementUtility.locateHeader(inst
						.getHeaders(),
						AnnotationProcessor.RESOURCE_META_DATA_UID);
				assertNotNull("SOAPElement was not located.", uid);
				assertNotNull("SOAPElement contained no text element.", uid
						.getTextContent());
				if (uid.getTextContent().equals(MetadataTest.enuMetaDataUID)) {
					enumModInst = inst;
				}
			}
			assertNotNull("The enumMetadata instance was not found.",
					enumModInst);

			//Test the additional elements added for enumerations.
			SOAPElement locatedElement = ManagementUtility.locateHeader(
					enumModInst.getHeaders(),
					AnnotationProcessor.ENUMERATION_ACCESS_RECIPE);
			assertNotNull(
					"Unable to locate EnumerationAcccessRecipe Info element.",
					locatedElement);
			assertEquals("Values did not match.", MetadataTest.enuAccessRecipe,
					locatedElement.getTextContent());
			locatedElement = ManagementUtility
					.locateHeader(enumModInst.getHeaders(),
							AnnotationProcessor.ENUMERATION_FILTER_USAGE);
			assertNotNull(
					"Unable to locate EnumerationFilterUsage Info element.",
					locatedElement);
			assertEquals("Values did not match.", MetadataTest.enuFilUsage,
					locatedElement.getTextContent());

			//############ TEST OPTIMIZED ENUMERATION CALL TO METADATA HANDLER #########
			//Attempt to locate a specific enumeration instance via filtered enumeration
			//BUILD the filter and message
			String filter = "env:Envelope/env:Header/wsmeta:ResourceMetaDataUID/text()='"
					+ MetadataTest.enuMetaDataUID + "'";
			EnumerationMessageValues enuValues = EnumerationMessageValues
					.newInstance();
			enuValues.setRequestForOptimizedEnumeration(true);
			enuValues.setFilter(filter);
			enuValues.setXmlBinding(binding);
			Enumeration enMesg = EnumerationUtility.buildMessage(null,
					enuValues);
			Management filteredMetaDataReq = ManagementUtility.buildMessage(
					enMesg, null);
			filteredMetaDataReq.setTo(DESTINATION);
			filteredMetaDataReq.setResourceURI("wsman:metadata");
			//Send the Enumeration request
			final Addressing response = HttpClient
					.sendRequest(filteredMetaDataReq);
			if (response.getBody().hasFault()) {
				fail(response.getBody().getFault().getFaultString());
			}
			//Parse response for recognizable Management instances
			//Translate the OptimizedEnumeration results to List<EnumerationItem>
			Management mResp = new Management(response);

			//EnumerationResourceState state = EnumerationUtility.extractResourceState(mResp);
			List<EnumerationItem> state = EnumerationUtility
					.extractEnumeratedValues(mResp);
			assertEquals("EventSources count not correct.", 1, state.size());

			//Locate the Management instances
			List<Management> metadataSrces = AnnotationProcessor
					.extractMetaDataFromEnumerationMessage(state);

			assertEquals("Incorrect number of MetaData instances returned.", 1,
					metadataSrces.size());
			Management inst = metadataSrces.get(0);
			SOAPElement uid = ManagementUtility.locateHeader(inst.getHeaders(),
					AnnotationProcessor.RESOURCE_META_DATA_UID);
			assertNotNull("SOAPElement was not located.", uid);
			assertNotNull("SOAPElement contained no text element.", uid
					.getTextContent());
			assertEquals(uid.getTextContent(), MetadataTest.enuMetaDataUID);

		} catch (Exception e) {
			fail("An error occured:" + e.getMessage());
		}
	}

	public void testExposedMetaData() throws SOAPException, IOException,
			JAXBException, DatatypeConfigurationException {
		Management[] metaDataList = MetadataUtility.getExposedMetadata(
				DESTINATION, -1);
		assertNotNull(metaDataList);
		assertTrue("No metadata exposed.", (metaDataList.length > 0));
	}

	public void testUseOfMetaDataInfo() throws SOAPException, JAXBException,
			DatatypeConfigurationException, IOException {
		//Parse the metadata respository to extract ADDRESS details for transfer instance
		String filter = "env:Envelope/env:Header/wsmeta:ResourceMetaDataUID/text()='"
				+ eventcreator_Handler.UID + "'";
		EnumerationMessageValues enuValues = EnumerationMessageValues
				.newInstance();
		enuValues.setRequestForOptimizedEnumeration(true);
		enuValues.setFilter(filter);
		enuValues.setXmlBinding(binding);
		Enumeration enMesg = EnumerationUtility.buildMessage(null, enuValues);
		Management filteredMetaDataReq = ManagementUtility.buildMessage(enMesg,
				null);
		filteredMetaDataReq.setTo(DESTINATION);
		filteredMetaDataReq.setResourceURI("wsman:metadata");
		//Send the Enumeration request
		final Addressing response = HttpClient.sendRequest(filteredMetaDataReq);
		if (response.getBody().hasFault()) {
			fail(response.getBody().getFault().getFaultString());
		}
		//Parse response for recognizable Management instances
		//Translate the OptimizedEnumeration results to List<EnumerationItem>
		Management mResp = new Management(response);

		//EnumerationResourceState state = EnumerationUtility.extractResourceState(mResp);
		List<EnumerationItem> state = EnumerationUtility
				.extractEnumeratedValues(mResp);
		assertEquals("EventSources count not correct.", 1, state.size());

		//Locate the Management instances
		List<Management> metadataSrces = AnnotationProcessor
				.extractMetaDataFromEnumerationMessage(state);

		assertEquals("Incorrect number of MetaData instances returned.", 1,
				metadataSrces.size());
		Management inst = metadataSrces.get(0);
		SOAPElement uid = ManagementUtility.locateHeader(inst.getHeaders(),
				AnnotationProcessor.RESOURCE_META_DATA_UID);
		assertNotNull("SOAPElement was not located.", uid);
		assertNotNull("SOAPElement contained no text element.", uid
				.getTextContent());
		assertEquals(uid.getTextContent(), eventcreator_Handler.UID);

		//Use that data to connect to that instance
		//Following should be an unsupported action
		inst.setAction(Enumeration.RENEW_ACTION_URI);
		ManagementMessageValues emtpy = null;
		Management man = ManagementUtility.buildMessage(inst, emtpy);
		final Addressing metInfResp = HttpClient.sendRequest(man);
		if (metInfResp.getBody().hasFault()) {
			SOAPFault fault = metInfResp.getBody().getFault();
			//	    	System.out.println("fault:"+fault.getFaultCode());
			//	    	System.out.println("fault:"+fault.getFaultNode());
			//	    	System.out.println("fault:"+fault.getFaultActor());
			//	    	System.out.println("fault:"+fault.getFaultString());
			//	    	System.out.println("fault:"+fault.getFaultCodeAsName().getLocalName());
			//	    	System.out.println("fault:"+fault.getFaultCodeAsQName());
			Iterator reasons = fault.getFaultReasonTexts();
			for (Iterator iter = reasons; iter.hasNext();) {
				String element = (String) iter.next();
				//	    		System.out.println("Reason:"+element);
			}
			Iterator subCodes = fault.getFaultSubcodes();
			for (Iterator iter = subCodes; iter.hasNext();) {
				QName element = (QName) iter.next();
				//			System.out.println("codes:"+element);
			}
		} else {
			fail("This action should not be supported");
		}
	}

	public void testMessageTypeDetails() throws SOAPException, JAXBException,
			DatatypeConfigurationException, IOException {
		//retrieve the exposed metadata where Schema and
		//  operations are already defined.
		Management metaDataInstance = null;
		//    	metaDataInstance = AnnotationProcessor.findAnnotatedResourceByUID(
		//    			MetadataTest.embeddedAnnotResourceMetUID,
		//    			DESTINATION);
		QName[] headers = null;
		metaDataInstance = AnnotationProcessor.findAnnotatedResourceByUID(
				MetadataTest.embeddedAnnotResourceMetUID, DESTINATION, false,
				headers);
		//TODO: RETURN AND DEBUG THIS, ODD BUG!!! Access to Vector.instance randomly fails. ???
		assertNotNull("Unable to locate correct instance.", metaDataInstance);
		//System.out.println("@@@ MetadataReturned:"+metaDataInstance);
		//Test that a body is returned in the metadata Management insts
		assertNotNull("No Metadata body found.", metaDataInstance.getBody());
		assertNotNull("No Metadata payload found.", metaDataInstance.getBody()
				.getFirstChild());
		Node payload = metaDataInstance.getBody().getFirstChild();

		//Make sure that schemas are returned correctly

		//Make sure that the Operations are returned correctly

	}

	public void testSchemaExposure() throws IOException, SOAPException,
			JAXBException {
		//send an http request for a wsdl.
		//parse the response to see that it was responded to
		String urlToXsd = ManagementMessageValues.WSMAN_DESTINATION
				+ "schemas/light.xsd";
		String xsdParseFor = "<xs:schema targetNamespace=\"http://schemas.wiseman.dev.java.net/traffic/1/light.xsd\"";

		// Make the check using the path method in the URL
		boolean located = false;
		located = checkDocument(urlToXsd, xsdParseFor);
		assertTrue("The expected String in the XSD could not be found:"
				+ urlToXsd, located);

		// Make the check using a query string in the URL
		urlToXsd = ManagementMessageValues.WSMAN_DESTINATION + "?xsd=light.xsd";
		located = checkDocument(urlToXsd, xsdParseFor);
		assertTrue("The expected String in the XSD could not be found:"
				+ urlToXsd, located);

		// Make the check using a default query string in the URL (user.xsd)
		urlToXsd = ManagementMessageValues.WSMAN_DESTINATION + "?xsd";
		xsdParseFor = "<xs:schema targetNamespace=\"http://examples.hp.com/ws/wsman/user\"";
		located = checkDocument(urlToXsd, xsdParseFor);
		assertTrue("The expected String in the XSD could not be found:"
				+ urlToXsd, located);
	}

	public void testWsdlExposure() throws IOException, SOAPException,
			JAXBException {
		// send an http request for a wsdl.
		// parse the response to see that it was responded to
		String urlToWsdl = ManagementMessageValues.WSMAN_DESTINATION
				+ "wsdls/light.wsdl";
		String wsdlParseFor = "<service name=\"lightService\">";

		// Make the check using the path method in the URL
		boolean located = false;
		located = checkDocument(urlToWsdl, wsdlParseFor);
		assertTrue("The expected String in the WSDL could not be found:"
				+ urlToWsdl, located);

		// Make the check using a query string in the URL
		urlToWsdl = ManagementMessageValues.WSMAN_DESTINATION
				+ "?wsdl=light.wsdl";
		located = checkDocument(urlToWsdl, wsdlParseFor);
		assertTrue("The expected String in the WSDL could not be found:"
				+ urlToWsdl, located);

		// Make the check using a default query string in the URL
		urlToWsdl = ManagementMessageValues.WSMAN_DESTINATION + "?wsdl";
		wsdlParseFor = "<service name=\"userService\">";
		located = checkDocument(urlToWsdl, wsdlParseFor);
		assertTrue("The expected String in the WSDL could not be found:"
				+ urlToWsdl, located);

	}

	/* Tests whether the metadataLocator mechanism works
	 * and can filter unwanted headers and remove
	 * descriptive metadata body elements.
	 */
	public void testMetaDataLocatorMechanism() throws SOAPException,
			JAXBException, DatatypeConfigurationException, IOException {

		//request a specific metadata instance
		QName[] headersToPrune = null;
		Management userMet = AnnotationProcessor.findAnnotatedResourceByUID(
				embeddedAnnotResourceMetUID,
				ManagementMessageValues.WSMAN_DESTINATION, false,
				headersToPrune);
		assertNotNull("The metadata element was not found.", userMet);

		//locate it's identifier to verify that it was successfully located.
		SOAPElement headerElement = ManagementUtility.locateHeader(userMet
				.getHeaders(), AnnotationProcessor.RESOURCE_META_DATA_UID);
		assertNotNull("MetResUid not located.", headerElement);
		assertEquals("Values did not match.", embeddedAnnotResourceMetUID,
				headerElement.getTextContent());

		//now verify that this metadataElement being exercised has body content
		assertNotNull("Body of metadata exists", userMet.getBody());
		assertNotNull("Body is empty.", userMet.getBody().getFirstChild());

		//Store away the header count for later comparison
		int fullHeaderCount = userMet.getHeaders().length;

		//Now request the same metadata instance, but exclude a few headers
		// and exclude the metadata body content
		//identify headers to trim out
		QName[] trim = { AnnotationProcessor.META_DATA_CATEGORY,
				AnnotationProcessor.META_DATA_DESCRIPTION,
				AnnotationProcessor.RESOURCE_MISC_INFO };
		Management filteredMetaData = AnnotationProcessor
				.findAnnotatedResourceByUID(embeddedAnnotResourceMetUID,
						ManagementMessageValues.WSMAN_DESTINATION, true, trim);
		assertNotNull("The metadata element was not found.", filteredMetaData);
		//Now verify that count is correct
		assertEquals("Incorrect amount returned.", filteredMetaData
				.getHeaders().length, (fullHeaderCount - trim.length));
		//Now verify that body is not longer present
		assertNotNull("Body of metadata exists", filteredMetaData.getBody());
		assertNull("Body is not empty", filteredMetaData.getBody()
				.getFirstChild());

	}

	private boolean checkDocument(final String url, final String parseString)
			throws IOException {
		final URL dest = new URL(url);

		assertNotNull("Unable to instantiate URL", dest);
		final URLConnection conn = dest.openConnection();
		assertNotNull("Unable to instantiate URLConnection", conn);
		//Simple request details.
		conn.setAllowUserInteraction(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		final HttpURLConnection http = (HttpURLConnection) conn;
		http.setRequestMethod("GET");
		//Build HTTP GET request.
		// String get = "GET " + url + " \r\n";

		// OutputStream output = http.getOutputStream();
		// output.write(get.getBytes());
		//Retrieve HTTP response code
		final int response = http.getResponseCode();

		BufferedReader br = null;
		if (response == HttpServletResponse.SC_OK) {
			InputStream input = http.getInputStream();
			br = new BufferedReader(
					new InputStreamReader(http.getInputStream()));
			// output.write(get.getBytes());
		} else {
			fail("Http request failed.");
		}
		assertNotNull("Unable to open reader.", br);

		boolean located = false;
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.indexOf(parseString) > -1) {
				located = true;
			}
		}

		return located;
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

}
