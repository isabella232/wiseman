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
 * $Id: MetadataTest.java,v 1.4 2007-04-24 13:50:19 simeonpinder Exp $
 */

package management;

import com.sun.ws.management.mex.MetadataUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._09.mex.Metadata;
import org.xmlsoap.schemas.ws._2004._09.mex.MetadataSection;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.identify.IdentifyUtility;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

/**
 * Unit test for the MetaData operations
 */
public class MetadataTest extends TestBase {
    
    public MetadataTest(final String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(MetadataTest.class);
        return suite;
    }

	public static final String destUrl = "http://localhost:8080/wsman/";
	public static final String resourceUri = "wsman:auth/user";
	public static final String selector1 ="firstname=Get";
	public static final String selector2 ="lastname=Guy";
	public static final String metaDataCategory ="PERSON_RESOURCES";
	public static final String metaDataDescription ="This resource exposes people information " +
 			"stored in an LDAP repository.";
	public static final String metaDataMiscInfo ="Miscellaneous Information";
//	public static final String metaDataUID ="http://some.company.xyz/ldap/repository/2006/uid_00000000007";
	public static final String metaDataUID ="UID_http://some.company.xyz/ldap/repository/2006/uid_00000000007";
	public static final String custQlocPart="UserParam";
	public static final String custQnsuri="http://sample.custom.com/custom/referenceParameter";
	public static final String custQprefix="other";
	public static final String custQvalue="param-value-1";
	public static final String custQlocPart1="UserProp";
	public static final String custQnsuri1="http://sample.custom.com/custom/referenceProperty";
	public static final String custQprefix1="cust";
	public static final String custQvalue1="prop-value-1";
	//embedded default addressing model instance
	public static final String embeddedAnnotTo="http://embedded.anot.TO";
	public static final String embeddedAnnotResourceURI="wsman:embe/resUri";
//	public static final String embeddedAnnotResourceMetUID="UID-987654321OLUHTR";
	public static final String embeddedAnnotResourceMetUID="UID_http://some.company.xyz/ci/respository/2098764";
	//Enumeration testing constants
	public static final String enuAddress="http://www.test.enu/address";
	public static final String enuResUri="wsman:test/uri";
	public static final String enuAccessRecipe="When accessing this enumeration use Get with last names.";
	public static final String enuFilUsage="Filter is only operational for firstname, lastname, hiredate.";
//	public static final String enuMetaDataUID = "http://sampleEnum/UID-enumeration985739485";
	public static final String enuMetaDataUID = "UID_http://diff.sampleEnum.com/enumeration/employee/985739485";
	
	public static long timeoutInMilliseconds = 9400000;
	
	XmlBinding binding = null;
    private int defAnnotCnt =3;
    
    /* Initialize the unit test variables for each test run.
     * (non-Javadoc)
     * @see management.TestBase#setUp()
     */
	protected void setUp() throws Exception {
		super.setUp();
		Management man = null;
		try {
			man = new Management();
		} catch (SOAPException e) {
			fail("Can't init wiseman");
		}
		binding = man.getXmlBinding();
	}
    
    /**Tests/exercises metadata functionality
     * 
     * @throws Exception
     */
    public void testMetaDataGet() throws Exception {
    	
    	//############ REQUEST THE METADATA REPOSITORY DATA ######################
        //Request identify info to get MetaData root information
        final Identify identify = new Identify();
        identify.setIdentify();
        
        //Send identify request
        final Addressing response = HttpClient.sendRequest(identify.getMessage(), DESTINATION);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        //Parse the identify response
        final Identify id = new Identify(response);
        final SOAPElement idr = id.getIdentifyResponse();
        assertNotNull(idr);
        SOAPElement el =IdentifyUtility.locateElement(id, 
        		AnnotationProcessor.META_DATA_RESOURCE_URI); 
         assertNotNull("MetaDatResourceURI is null.",el);
         //retrieve the MetaData ResourceURI
         String resUri=el.getTextContent();
          assertNotNull("Retrieved resourceURI is null.",resUri);
        el =IdentifyUtility.locateElement(id, 
        		AnnotationProcessor.META_DATA_TO);
        assertNotNull("MetaDataTo is null",el);
        //retrieve the MetaData To/Destination
        String metTo=el.getTextContent();
        assertNotNull("Retrieved destination is null.",metTo);

        //############ REQUEST THE LIST OF METADATA AVAILABLE ######################
	   //Build the GET request to be submitted for the metadata
        Management m = null; 
        m =TransferUtility.createMessage(metTo, resUri,
        		Transfer.GET_ACTION_URI, null, null, 30000, null);
        
         //############ PROCESS THE METADATA RESPONSE ######################
         //Parse the getResponse for the MetaData
         final Addressing getResponse = HttpClient.sendRequest(m);
       Management mResp = new Management(getResponse);
//System.out.println("Request MEtaDataResp:"+mResp.toString());       
        assertNull("A fault was detected.",mResp.getFault());
               
   		//Retrieve the MetaData response to build JAXB type
   		SOAPBody body = mResp.getBody();

   		//Normal processing to create/retrieve the Metadata object
   		Node metaDataNode = body.getFirstChild();
		try {
			//unmarshall the Metadata node content
			Object bound = binding.unmarshal(metaDataNode);
			
			Metadata ob = (Metadata)bound;
			
			//Parse the MetadataSections that exist
			List<MetadataSection> metaDataSections = ob.getMetadataSection();
			 assertEquals("The correct number of metadata sections were not found.",
//					 3, metaDataSections.size());
					 defAnnotCnt, metaDataSections.size());

        //############ PROCESS A METADATASECTION ###################### 
			//Examine Metadatasection attributes 
			MetadataSection section = metaDataSections.get(0);
			assertEquals("Dialect does not match.",
					AnnotationProcessor.NS_URI, 
					section.getDialect());
			assertEquals("Identifier does not match.",
					AnnotationProcessor.NS_URI, 
					section.getIdentifier());

	    //########### TRANSLATE METADATA TO FAMILIAR MANAGEMENT NODES ##### 
	        //Extract the MetaData node returned as Management instances
	        Management[] metaDataList = 
	        	MetadataUtility.extractEmbeddedMetaDataElements(mResp);
	        assertEquals("Correct number of Management instances not returned.",
//	        		3, metaDataList.length);
	        		defAnnotCnt, metaDataList.length);
			
	        //locate the Management instances to evaluate.
	        Management defAddModInst = null;
	        Management embeddedDefModInst = null;
	        Management enumModInst = null;
	        for (int i = 0; i < metaDataList.length; i++) {
	        	Management inst = metaDataList[i];
	        	SOAPElement uid = ManagementUtility.locateHeader(inst.getHeaders(), 
	        			AnnotationProcessor.RESOURCE_META_DATA_UID);
	        	assertNotNull("Unable to locate SoapElement for "+
	        			AnnotationProcessor.RESOURCE_META_DATA_UID,uid);
	        	if(uid.getTextContent().equals(MetadataTest.metaDataUID)){
	        		defAddModInst = inst;
	        	}
	        	if(uid.getTextContent().equals(MetadataTest.embeddedAnnotResourceMetUID)){
	        		embeddedDefModInst = inst;
	        	}
	        	if(uid.getTextContent().equals(MetadataTest.enuMetaDataUID)){
	        		enumModInst = inst;
	        	}
			}
			Management instance = defAddModInst;
			
			assertNotNull("Management instance not successfully retrieved.",instance);
			
			//Now parse the Dialect specif component.
			//############ VERIFY THE METADATA RESPONSE ######################
			//Verify expected contents.
		    assertEquals("The values did not match.",destUrl, instance.getTo());   
			assertEquals("The values did not match.",resourceUri, instance.getResourceURI());
			//Test MetaDataCategory
			SOAPElement locatedElement = ManagementUtility.locateHeader(instance.getHeaders(),
					AnnotationProcessor.META_DATA_CATEGORY);
			assertNotNull("Unable to locate MetaDataCategory element.",locatedElement);
			assertEquals("Values did not match.",
					MetadataTest.metaDataCategory, locatedElement.getTextContent());
			//Test MetaDataDescription
			locatedElement = ManagementUtility.locateHeader(instance.getHeaders(),
					AnnotationProcessor.META_DATA_DESCRIPTION);
			assertNotNull("Unable to locate MetaDataDescription element.",locatedElement);
			assertEquals("Values did not match.",
					MetadataTest.metaDataDescription, locatedElement.getTextContent());
			//Test MetaDataMiscInfo
			locatedElement = ManagementUtility.locateHeader(instance.getHeaders(),
					AnnotationProcessor.RESOURCE_MISC_INFO);
			assertNotNull("Unable to locate MetaDataMisc Info element.",locatedElement);
			assertEquals("Values did not match.",
					MetadataTest.metaDataMiscInfo, locatedElement.getTextContent());
			//Test MetaDataUID
			locatedElement = ManagementUtility.locateHeader(instance.getHeaders(),
					AnnotationProcessor.RESOURCE_META_DATA_UID);
			assertNotNull("Unable to locate MetaDataUID Info element.",locatedElement);
			assertEquals("Values did not match.",
					MetadataTest.metaDataUID, locatedElement.getTextContent());
			//verify SelectorSet from annotation is retrieved correctly...
			Map<String,String> selectorsRetrieved = null;
			selectorsRetrieved =
				ManagementUtility.extractSelectorsAsMap(selectorsRetrieved,
					(List)new ArrayList<SelectorType>(instance.getSelectors()));
			StringTokenizer selTok = new StringTokenizer(selector1,"=");
			String key = null;
				key = selTok.nextToken();
			assertTrue("First selector key was not found.",
					selectorsRetrieved.containsKey(key));
			String value = selTok.nextToken();
			assertEquals("First selector value was not found.",
					value,
					selectorsRetrieved.get(key));
			//Test second selector entered.
			selTok = new StringTokenizer(selector2,"="); 
				key = selTok.nextToken();
 				assertTrue("Second selector key was not found.",
 						selectorsRetrieved.containsKey(key));
			value = selTok.nextToken();
			assertEquals("Second selector value was not found.",
					value,
					selectorsRetrieved.get(key));
			//Test Custom ReferenceParameter Types
			QName custQName = new QName(custQnsuri,custQlocPart,custQprefix);
			locatedElement = ManagementUtility.locateHeader(instance.getHeaders(),
					custQName);
			assertNotNull("Unable to locate CustomRefParam element.",locatedElement);
			assertEquals("Values did not match.",
					custQvalue, locatedElement.getTextContent());
			//Test Custom ReferenceProperty Types
			QName custQName1 = new QName(custQnsuri1,custQlocPart1,custQprefix1);
			locatedElement = ManagementUtility.locateHeader(instance.getHeaders(),
					custQName1);
			assertNotNull("Unable to locate CustomRefParam element.",locatedElement);
			assertEquals("Values did not match.",
					custQvalue1, locatedElement.getTextContent());
 				
			//test that embedded annotations are also retrieved for handler with sets of addressing details.
			Management embedded = null; 
			//Ensure that analysis is done on right element 
			embedded = embeddedDefModInst;
			assertEquals("To values do not match",MetadataTest.embeddedAnnotTo, 
					embedded.getTo());
			assertEquals("To values do not match",MetadataTest.embeddedAnnotResourceURI, 
					embedded.getResourceURI());
			
 			} catch (JAXBException e) {
 				System.out.println("Exception:"+e.getMessage());
 				fail("Unexpected error occurred:"+e.getMessage());
 				throw new InvalidRepresentationFault(
 						InvalidRepresentationFault.Detail.INVALID_VALUES);
 			}
    }
    
    public void testMetaDataEnumeration(){
      try{
    	//############ REQUEST THE METADATA REPOSITORY DATA ######################
        //Request identify info to get MetaData root information
        final Identify identify = new Identify();
        identify.setIdentify();
        
        //Send identify request
        final Addressing response = HttpClient.sendRequest(identify.getMessage(), DESTINATION);
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
        assertNotNull(el);

        //retrieve the MetaData ResourceURI
        String resUri=el.getTextContent();
        assertNotNull("Retrieved resourceURI is null.",resUri);
        el = IdentifyUtility.locateElement(id, 
        		AnnotationProcessor.META_DATA_TO);
        assertNotNull(el);

        //retrieve the MetaData To/Destination
        String metTo=el.getTextContent();
        assertNotNull("Retrieved destination is null.",metTo);
    	
     //exercise the Enumeration annotation mechanism
        //############ REQUEST THE LIST OF METADATA AVAILABLE ######################
 	   //Build the GET request to be submitted for the metadata
        Management m = TransferUtility.createMessage(metTo, resUri,
        		Transfer.GET_ACTION_URI, null, null, 30000, null);
        
          //############ PROCESS THE METADATA RESPONSE ######################
          //Parse the getResponse for the MetaData
          final Addressing getResponse = HttpClient.sendRequest(m);
        Management mResp = new Management(getResponse);
         assertNull("A fault was detected.",mResp.getFault());        
   
        //retrieve all the metadata descriptions 
        Management[] metaDataList = 
        	MetadataUtility.extractEmbeddedMetaDataElements(mResp); 
//        assertEquals("Array count incorrect.",3, metaDataList.length);
        assertEquals("Array count incorrect.",defAnnotCnt, metaDataList.length);
        Management enumModInst = null;
        for (int i = 0; i < metaDataList.length; i++) {
        	Management inst = metaDataList[i];
        	SOAPElement uid = ManagementUtility.locateHeader(inst.getHeaders(), 
        			AnnotationProcessor.RESOURCE_META_DATA_UID);
        	assertNotNull("SOAPElement was not located.",uid);
        	assertNotNull("SOAPElement contained no text element.",uid.getTextContent());
        	if(uid.getTextContent().equals(MetadataTest.enuMetaDataUID)){
        		enumModInst = inst;
        	}
		}

        //Test the additional elements added for enumerations. 
        SOAPElement locatedElement = ManagementUtility.locateHeader(enumModInst.getHeaders(),
        		AnnotationProcessor.ENUMERATION_ACCESS_RECIPE);
        assertNotNull("Unable to locate EnumerationAcccessRecipe Info element.",locatedElement);
        assertEquals("Values did not match.",
        		MetadataTest.enuAccessRecipe, locatedElement.getTextContent());
        locatedElement = ManagementUtility.locateHeader(enumModInst.getHeaders(),
				AnnotationProcessor.ENUMERATION_FILTER_USAGE);
		assertNotNull("Unable to locate EnumerationFilterUsage Info element.",locatedElement);
		assertEquals("Values did not match.",
				MetadataTest.enuFilUsage, locatedElement.getTextContent());
        
      }catch(Exception e){
    	  fail("An error occured:"+e.getMessage());
      }
    }
    public void testWsdlSchemaExposure() throws IOException, SOAPException, JAXBException{
    	//send an http request for a wsdl.
    	//parse the response to see that it was responded to    	
    	String urlToXsd = ManagementMessageValues.WSMAN_DESTINATION+"schemas/light.xsd";
//    	String urlToXsd = ManagementMessageValues.WSMAN_DESTINATION+"schemas/light.xsd1";
    	String parseFor="http://schemas.wiseman.dev.java.net/traffic/1/light.xsd";
    	//Create HTTP connection
        final URL dest = new URL(urlToXsd);
         assertNotNull("Unable to instantiate URL",dest);
        final URLConnection conn = dest.openConnection();
         assertNotNull("Unable to instantiate URLConnection",conn);
        //Simple request details. 
          conn.setAllowUserInteraction(false);
          conn.setDoInput(true);
          conn.setDoOutput(true);
        
        final HttpURLConnection http = (HttpURLConnection) conn;
        http.setRequestMethod("POST");
        //Build HTTP GET request.
	   	 String get = "GET "+urlToXsd+" \r\n";
	   	 
		 OutputStream output = http.getOutputStream();
		 output.write(get.getBytes());
		 //Retrieve HTTP response code
        final int response = http.getResponseCode();
        
        if(response==HttpServletResponse.SC_OK){
       	 InputStream input = http.getInputStream();
     	 BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
    	   output.write(get.getBytes());
    	 assertNotNull("Unable to open reader.",br);
    	boolean located = false;
    	String line = null;
    	while((line = br.readLine())!=null){
    		if(line.indexOf(parseFor)>-1){
    			located = true;
    		}
    	}
    	assertTrue("The expected String could not be found.",located);
          	
        }else{
        	fail("Http request failed.");
        }
    }
}
