/*
 * Copyright 2006 Hewlett-Packard Development Company, L.P.
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
 */

package management;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.transfer.TransferMessageValues;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;
import foo.test.Foo;
import foo.test.Is;
import java.io.IOException;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPHeaderElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

public class TransferExtensionsTest extends TestBase {

    private static final String JAXB_PACKAGE_FOO_TEST = "foo.test";
    
    private static final String CUSTOM_JAXB_PREFIX = "jb";
    private static final String CUSTOM_JAXB_NS = "http://test.foo";
    private static final Map<String, String> NAMESPACES = new HashMap<String, String>();

    static {
        NAMESPACES.put(CUSTOM_JAXB_PREFIX, CUSTOM_JAXB_NS);
    }

    public TransferExtensionsTest(String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(TransferExtensionsTest.class);
        return suite;
    }
    
    /**
     * Visual test of request structure..
     * <p/>
     * Note: Visual tests invoke server-side operations directly and are not
     * meant to be viewed as proper client invocations.
     *
     * @throws Exception
     */
    public void testFragmentGetVisual() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.GET_ACTION_URI);
        
        //xpath expression
        final String expression = "//foo/bar";
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, XPath.NS_URI);
        
        //print contents of Transfer
        transfer.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transfer.writeTo(bos);
        final TransferExtensions trans = new TransferExtensions(new ByteArrayInputStream(bos.toByteArray()));
        
        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader);
        assertEquals(expression, fragmentTransferHeader.getTextContent());
        assertEquals(XPath.NS_URI, fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
    }
    
    /**
     * Visual test of response structure..
     * Note: Visual tests invoke server-side operations directly and are not
     * meant to be viewed as proper client invocations.
     *
     * @throws Exception
     */
    public void testFragmentGetResponseVisual() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.GET_RESPONSE_URI);
        
        //xpath expression
        final String expression = "//foo/bar";
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, null);
        final SOAPHeaderElement fragmentTransferHeader = transfer.getFragmentHeader();
        
        //simulate the server-side DOM model to be used in request
        final List<Object> content = new ArrayList<Object>();
        final Document doc = transfer.newDocument();
        final Element fooElement = doc.createElement("foo");
        final Element barElement = doc.createElement("bar");
        barElement.setTextContent("this is a test of fragments");
        fooElement.appendChild(barElement);
        content.add(fooElement);
        
        //simulate server side request to get the response
        transfer.setFragmentGetResponse(fragmentTransferHeader, content);
        
        //print contents of Transfer
        transfer.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transfer.writeTo(bos);
        final TransferExtensions trans = new TransferExtensions(new ByteArrayInputStream(bos.toByteArray()));
        
        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader2 = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader2);
        assertEquals(expression, fragmentTransferHeader2.getTextContent());
        assertNull(fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
    }
    
    /**
     * a fragment transfer becomes a regular transfer if the fragment header is omitted
     */
    public void testNonFragmentGet() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.GET_ACTION_URI);
        transfer.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        transfer.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        //send request to server
        final Management mgmt = new Management(transfer);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/fragment");
        
        //print contents of Transfer
        mgmt.prettyPrint(logfile);
        
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        final TransferExtensions trans = new TransferExtensions(response);
        
        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNull(fragmentTransferHeader);
        
        // this should be the entire response document
        assertNotNull(response.getBody().getFirstChild());
    }
    
    /**
     * Actual Test of the Fragment Get operation
     *
     * @throws Exception
     */
    public void testFragmentGet() throws Exception {
        //setup Transfer object for request
    	
    	TransferMessageValues settings = new TransferMessageValues();
    	settings.setNamespaceMap(NAMESPACES);
    	settings.setFragment("//jb:foo/jb:bar");
    	settings.setFragmentDialect(null);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri("wsman:test/fragment");
    	
    	Transfer xf = TransferUtility.buildMessage(null, settings);

        //print contents of Transfer
        xf.prettyPrint(logfile);
        
        final Addressing response = HttpClient.sendRequest(xf);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        final TransferExtensions trans = new TransferExtensions(response);
        
        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader);
        assertEquals(settings.getFragment(), fragmentTransferHeader.getTextContent());
        assertNull(fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
    }
    
    /**
     * Visual test of response structure..
     * <p/>
     * Note: Visual tests invoke server-side operations directly and are not
     * meant to be viewed as proper client invocations.
     *
     * @throws Exception
     */
    public void testFragmentDeleteVisual() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.DELETE_RESPONSE_URI);
        
        //xpath expression
        final String expression = "//foo/bar";
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, XPath.NS_URI);
        final SOAPHeaderElement fragmentHeader = transfer.getFragmentHeader();
        
        //simulated Dom model to be modified on server
        final List<Node> content = new ArrayList<Node>();
        final Document doc = transfer.newDocument();
        final Element isElement = doc.createElement("is");
        final Element fooElement = doc.createElement("foo");
        final Element barElement = doc.createElement("bar");
        barElement.setTextContent("this is a test of fragments");
        fooElement.appendChild(barElement);
        isElement.appendChild(fooElement);
        content.add(isElement);
        
        //simulate a server-side request to build the response
        transfer.setFragmentDeleteResponse(fragmentHeader);
        
        //print contents of Transfer
        transfer.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transfer.writeTo(bos);
        final TransferExtensions trans = new TransferExtensions(new ByteArrayInputStream(bos.toByteArray()));
        
        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader2 = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader2);
        assertEquals(expression, fragmentTransferHeader2.getTextContent());
        assertNotNull(fragmentTransferHeader2.getAttributeValue(TransferExtensions.DIALECT));
    }
    
    /**
     * Actual test of Fragment Delete Operation
     *
     * @throws Exception
     */
    public void testFragmentDelete() throws Exception {
        
        //setup Transfer object for request
    	TransferMessageValues settings = new TransferMessageValues();
    	settings.setNamespaceMap(NAMESPACES);
    	settings.setFragment("//jb:foo/jb:bar");
    	settings.setFragmentDialect(XPath.NS_URI);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri("wsman:test/fragment");
    	settings.setTransferMessageActionType(Transfer.DELETE_ACTION_URI);
    	
    	Transfer xf = TransferUtility.buildMessage(null, settings);

        //print contents of Transfer
        xf.prettyPrint(logfile);
        
        final Addressing response = HttpClient.sendRequest(xf);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        final TransferExtensions trans = new TransferExtensions(response);
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader);
        assertEquals(settings.getFragment(), fragmentTransferHeader.getTextContent());
        assertNotNull(fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
        
    }
    
    /**
     * A test of an expected failed delete request.
     *
     * @throws Exception
     */
    public void testFragmentDeleteFail() throws Exception {
        //setup Transfer object for request
    	TransferMessageValues settings = new TransferMessageValues();
    	settings.setNamespaceMap(NAMESPACES);
    	settings.setFragment("//jb:foo");
    	settings.setFragmentDialect(XPath.NS_URI);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri("wsman:test/fragment");
    	settings.setTransferMessageActionType(Transfer.DELETE_ACTION_URI);
    	
    	Transfer xf = TransferUtility.buildMessage(null, settings);
    	
        //print contents of Transfer
        xf.prettyPrint(logfile);
        
        final Addressing response = HttpClient.sendRequest(xf);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            assertNotNull(response.getBody().getFault().getFaultString());
        } else {
            // TODO: a fault will not be thrown if the server is not in validation mode
            // fail("A Fault should have been thrown and was not");
        }
    }
    
    /**
     * Visual test of response structure..
     * <p/>
     * Note: Visual tests invoke server-side operations directly and are not
     * meant to be viewed as proper client invocations.
     *
     * @throws Exception
     */
    public void testFragmentPutResponseVisual() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.PUT_RESPONSE_URI);
        
        //xpath expression
        final String expression = "//foo/bar";
        
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, XPath.NS_URI);
        final SOAPHeaderElement fragmentHeader = transfer.getFragmentHeader();
        
        //build simulated Dom model to be modified
        final List<Node> content = new ArrayList<Node>();
        final Document doc = transfer.newDocument();
        final Element isElement = doc.createElement("a");
        final Element fooElement = doc.createElement("foo");
        final Element barElement = doc.createElement("bar");
        barElement.setTextContent("this is a test of fragments");
        fooElement.appendChild(barElement);
        isElement.appendChild(fooElement);
        content.add(isElement);
        
        //build simulated request content
        final List<Object> requestContent = new ArrayList<Object>();
        final Element requestBarElement = doc.createElement("bar");
        requestBarElement.setTextContent("PUT request value");
        requestContent.add(requestBarElement);
        
        //simulate server-side request to get the response
        transfer.setFragmentPutResponse(fragmentHeader, requestContent, expression, content);
        
        //print contents of Transfer
        transfer.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transfer.writeTo(bos);
        final TransferExtensions trans = new TransferExtensions(new ByteArrayInputStream(bos.toByteArray()));
        
        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader2 = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader2);
        assertEquals(expression, fragmentTransferHeader2.getTextContent());
        assertNotNull(fragmentTransferHeader2.getAttributeValue(TransferExtensions.DIALECT));
    }
    
    /**
     * Actual test of the Fragment Put operation
     *
     * @throws Exception
     */
    public void testFragmentPut() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        if (!checkIfBindingIsAvailable(transfer.getXmlBinding())) {
            // skip this test if JAXB is not initialized with the foo.test package
            return;
        }
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.PUT_ACTION_URI);
        transfer.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        transfer.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        //xpath expression
        final String expression = "//jb:foo/jb:bar";
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, XPath.NS_URI);
        
        //build XmlFragment for request
        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
        final JAXBElement<MixedDataType> xmlFragment = Management.FACTORY.createXmlFragment(mixedDataType);
        
        //create request body content
        final foo.test.ObjectFactory ob = new foo.test.ObjectFactory();
        final JAXBElement<String> bar = ob.createBar("PUT request value");
        mixedDataType.getContent().add(bar);
        
        //marshall XmlFragment into request body
        new XmlBinding(null, JAXB_PACKAGE_FOO_TEST).marshal(xmlFragment, transfer.getBody());
        
        //send request to server
        final Management mgmt = new Management(transfer);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/fragment");
        //print contents of Transfer
        mgmt.prettyPrint(logfile);
        
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        final TransferExtensions trans = new TransferExtensions(response);
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader);
        assertEquals(expression, fragmentTransferHeader.getTextContent());
        assertNotNull(fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
        
    }
    
    /**
     * Test of an expected failed Fragment Put operation
     *
     * @throws Exception
     */
    public void testFragmentPutFail() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.PUT_ACTION_URI);
        transfer.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        transfer.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        //xpath expression
        final String expression = "//jb:foo/jb:bar";
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, XPath.NS_URI);
        
        //build XmlFragment for request
        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
        final JAXBElement<MixedDataType> xmlFragment = Management.FACTORY.createXmlFragment(mixedDataType);
        
        //build request body content
        final foo.test.ObjectFactory ob = new foo.test.ObjectFactory();
        final Is is = ob.createIs();
        mixedDataType.getContent().add(is);
        
        //marshall XmlFragment into the request body
        new XmlBinding(null, JAXB_PACKAGE_FOO_TEST).marshal(xmlFragment, transfer.getBody());
        
        //send request to server
        final Management mgmt = new Management(transfer);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/fragment");
        //print contents of Transfer
        mgmt.prettyPrint(logfile);
        
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            assertNotNull(response.getBody().getFault().getFaultString());
        } else {
            fail("Fault was not thrown");
        }
        
    }
    
    /**
     * Visual test of response structure..
     * <p/>
     * Note: Visual tests invoke server-side operations directly and are not
     * meant to be viewed as proper client invocations.
     *
     * @throws Exception
     */
    public void testFragmentCreateResponseVisual() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.CREATE_RESPONSE_URI);
        transfer.setTo(DESTINATION);
        
        //xpath expression
        final String expression = "//foo";
        
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, XPath.NS_URI);
        final SOAPHeaderElement fragmentHeader = transfer.getFragmentHeader();
        
        //simulate the DOM model being modified
        final List<Node> content = new ArrayList<Node>();
        final Document doc = transfer.newDocument();
        final Element isElement = doc.createElement("a");
        final Element fooElement = doc.createElement("foo");
        final Element barElement = doc.createElement("bar");
        barElement.setTextContent("this is a test of fragments");
        fooElement.appendChild(barElement);
        isElement.appendChild(fooElement);
        content.add(isElement);
        
        //simulate the request content whci came off the wire
        final List<Object> requestContent = new ArrayList<Object>();
        final Element requestFooElement = doc.createElement("foo");//create a foo with no bar
        requestContent.add(requestFooElement);

        final EndpointReferenceType epr = Addressing.createEndpointReference(transfer.getTo(), null, null, null, null);
        transfer.setFragmentCreateResponse(fragmentHeader, requestContent, expression, content, epr);
        
        //print contents of Transfer
        transfer.prettyPrint(logfile);
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transfer.writeTo(bos);
        final TransferExtensions trans = new TransferExtensions(new ByteArrayInputStream(bos.toByteArray()));
        
        //try to get the fragmenttransfer header
        final SOAPHeaderElement fragmentTransferHeader2 = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader2);
        assertEquals(expression, fragmentTransferHeader2.getTextContent());
        assertNotNull(fragmentTransferHeader2.getAttributeValue(TransferExtensions.DIALECT));
    }
    
    /**
     * Actual test of the Fragment Create operation
     *
     * @throws Exception
     */
    public void testFragmentCreate() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        if (!checkIfBindingIsAvailable(transfer.getXmlBinding())) {
            // skip this test if JAXB is not initialized with the foo.test package
            return;
        }
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.CREATE_ACTION_URI);
        transfer.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        transfer.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        //xpath expression
        final String expression = "//jb:foo";
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, XPath.NS_URI);
        
        //build an XmlFragment from XmlBeans for adding request content to
        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
        final JAXBElement<MixedDataType> xmlFragment = Management.FACTORY.createXmlFragment(mixedDataType);
        
        //request body content
        final foo.test.ObjectFactory of = new foo.test.ObjectFactory();
        final Foo foo = of.createFoo();
        mixedDataType.getContent().add(foo);
        
        //marshall content into XmlFragment
        new XmlBinding(null, JAXB_PACKAGE_FOO_TEST).marshal(xmlFragment, transfer.getBody());
        
        //send request
        final Management mgmt = new Management(transfer);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/fragment");
        //print contents of Transfer
        mgmt.prettyPrint(logfile);
        
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        final TransferExtensions trans = new TransferExtensions(response);
        final SOAPHeaderElement fragmentTransferHeader = trans.getFragmentHeader();
        assertNotNull(fragmentTransferHeader);
        assertEquals(expression, fragmentTransferHeader.getTextContent());
        assertNotNull(fragmentTransferHeader.getAttributeValue(TransferExtensions.DIALECT));
        
    }
    
    /**
     * Test of expected failed Fragment Create operation
     *
     * @throws Exception
     */
    public void testFragmentCreateFail() throws Exception {
        //setup Transfer object for request
        final TransferExtensions transfer = new TransferExtensions();
        transfer.addNamespaceDeclarations(NAMESPACES);
        transfer.setAction(Transfer.CREATE_ACTION_URI);
        transfer.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        transfer.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        
        //xpath expression
        final String expression = "//jb:foo";
        //this call ensures the fragment header is initialized
        transfer.setFragmentHeader(expression, XPath.NS_URI);
        
        //build an XmlFragment from XmlBeans for adding request content to
        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
        final JAXBElement<MixedDataType> xmlFragment = Management.FACTORY.createXmlFragment(mixedDataType);
        
        //request body content
        final foo.test.ObjectFactory of = new foo.test.ObjectFactory();
        final Is is = of.createIs();
        mixedDataType.getContent().add(is);
        
        //marshall content into XmlFragment
        new XmlBinding(null, JAXB_PACKAGE_FOO_TEST).marshal(xmlFragment, transfer.getBody());
        
        //send request
        final Management mgmt = new Management(transfer);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI("wsman:test/fragment");
        //print contents of Transfer
        mgmt.prettyPrint(logfile);
        
        final Addressing response = HttpClient.sendRequest(mgmt);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            assertNotNull(response.getBody().getFault().getFaultString());
        } else {
            fail("A fault should have been thrown and wasn't");
        }
        
    }

    private boolean checkIfBindingIsAvailable(final XmlBinding binding) throws IOException {
        final boolean available = binding.isPackageHandled(JAXB_PACKAGE_FOO_TEST);
        if (!available) {
            final String msg = "Skipping test " + getName() + 
                    " since binding is currently not enabled for package " + 
                    JAXB_PACKAGE_FOO_TEST;
            logfile.write(msg.getBytes());
        }
        return available;
    }
}
