/*
 * Copyright 2005 Sun Microsystems, Inc.
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
 * $Id: EnumerationFilterTest.java,v 1.3.2.1 2007-02-20 12:15:08 denis_rachal Exp $
 */

package management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableNonNegativeInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

import framework.models.UserEnumerationHandler;
import framework.models.UserFilterFactory;

/**
 * Unit test for WS-Enumeration extensions in WS-Management
 */
public class EnumerationFilterTest extends TestBase {
	
	XmlBinding binding;

	protected void setUp() throws Exception {
		super.setUp();
	}
    
    public EnumerationFilterTest(final String testName) {
        super(testName);
        
		// Set the system property to always create bindings with our package
		System.setProperty(XmlBinding.class.getPackage().getName() + ".custom.packagenames",
				"com.hp.examples.ws.wsman.user");
		try {
			binding = new XmlBinding(null, "com.hp.examples.ws.wsman.user");
		} catch (JAXBException e) {
			fail(e.getMessage());
		}
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(EnumerationFilterTest.class);
        return suite;
    }
    
    public void testFilterEnumeration() throws Exception {
    	String xpath = XPath.NS_URI;
    	String custom = UserFilterFactory.DIALECT;

    	filterEnumerationTest(xpath, ".");
    	filterEnumerationTest(xpath, "/user:user");
    	filterEnumerationTest(xpath, "/user:user/*");
    	// TODO: The following XPath does not really work: "/user:user[last()]"
    	//       This is because the XPath is run against a single user element and
    	//       not the entire docuemnt. Therefore the last() returns every element.
    	//       This is a bug, as the specification shows that you may select 
    	//       any element from the entire SOAP document, e.g.
    	//       "/env:Envelope/env:Body/user:user" should be valid. The envelope
    	//       is the root, which makes several of the following tests wrong.
    	//       See examples in secion 13.1 of the DMTF DSP0226 specification 1.0.0a
    	filterEnumerationTest(xpath, "/user:user[last()]");
    	filterEnumerationTest(xpath, "//user:lastname");
    	filterEnumerationTest(xpath, "./user:firstname|./user:lastname");
    	filterEnumerationTest(xpath, "/user:user/user:firstname/text()|/user:user/user:lastname/text()");
    	filterEnumerationTest(xpath, "/user:user/user:lastname");
    	filterEnumerationTest(xpath, "/user:user[user:firstname='James']");
    	filterEnumerationTest(xpath, "/user:user[user:firstname='James']/user:lastname|/user:user[user:firstname='James']/user:firstname");

    	filterEnumerationTest(custom, "Gates");
    	filterEnumerationTest(custom, "Washington");
    	filterEnumerationTest(custom, "Ritter");

    }
    
    public void filterEnumerationTest(final String dialect, final String expression) throws Exception {
    	
    	final String resource = "wsman:auth/userenum";
    	final int max = 10;

    	
    	Set<OptionType> handlerFiltered = new HashSet<OptionType>();
		OptionType element = new OptionType();
		element.setName("useHandlerFilter");
		element.setValue("true");
        element.setMustComply(true);
        handlerFiltered.add(element);
    	
        // first do the tests with EPRs turned off
    	optimizedEnumerationTest(null, max, resource, dialect, expression, null);
    	optimizedEnumerationTest(null, max, resource, dialect, expression, handlerFiltered);
        
        // now repeat the same tests with EPRs turned on
    	optimizedEnumerationTest(EnumerationExtensions.Mode.EnumerateObjectAndEPR, max, resource, dialect, expression, null);
    	optimizedEnumerationTest(EnumerationExtensions.Mode.EnumerateObjectAndEPR, max, resource, dialect, expression, handlerFiltered);
    	
        // finally, repeat the same tests with only EPRs (no items)
    	optimizedEnumerationTest(EnumerationExtensions.Mode.EnumerateEPR, max, resource, dialect, expression, null);
    	optimizedEnumerationTest(EnumerationExtensions.Mode.EnumerateEPR, max, resource, dialect, expression, handlerFiltered);
    	
    }
    
    public void optimizedEnumerationTest(
    		final EnumerationExtensions.Mode mode,
            final int maxElements,
            final String resource,
    		final String dialect,
    		final String expression,
    		final Set<OptionType> options) throws Exception {
    	
        final EnumerationExtensions enu = new EnumerationExtensions();
        enu.setXmlBinding(binding);
        
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        final DatatypeFactory factory = DatatypeFactory.newInstance();
        
        // Process any filter
        final DialectableMixedDataType filter;
        if ((expression != null) && (expression.length() > 0)) {
            filter = Management.FACTORY.createDialectableMixedDataType();
            if ((dialect != null) && (dialect.length() > 0))
                filter.setDialect(dialect);
            filter.getContent().add(expression);
        } else {
        	filter = null;
        }
        enu.setEnumerate(null, true, true, maxElements, 
        		factory.newDuration(60000).toString(),
        		filter, mode);
        
        final Management mgmt = new Management(enu);
        mgmt.setTo(DESTINATION);
        mgmt.setResourceURI(resource);
        
        // Add the namespace for the filter
        Map<String, String> map = new  HashMap<String, String>();
        map.put("user", UserEnumerationHandler.NS_URI);
        mgmt.addNamespaceDeclarations(map);
        
        // Add any options
        if (options != null)
            mgmt.setOptions(options);
        
        mgmt.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(mgmt);
        
        response.setXmlBinding(binding);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            response.prettyPrint(System.err);
            // this test fails with an AccessDenied fault if the server is
            // running in the sun app server with a security manager in
            // place (the default), which disallows enumeration of
            // system properties
            fail(response.getBody().getFault().getFaultString());
        }
        
        final EnumerationExtensions enuResponse = new EnumerationExtensions(response);
        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
        String context = (String) enr.getEnumerationContext().getContent().get(0);
        for (final EnumerationItem item : enuResponse.getItems()) {
            assertMode(mode, item);
        }
        
        final AttributableNonNegativeInteger ee = enuResponse.getTotalItemsCountEstimate();
        assertNotNull(ee);
        assertTrue(ee.getValue().intValue() > 0);
        
        if (enuResponse.isEndOfSequence() == false) {
            final EnumerationExtensions pullRequest = new EnumerationExtensions();
            pullRequest.setAction(Enumeration.PULL_ACTION_URI);
            pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
            pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
            pullRequest.setPull(context, 0, 3, factory.newDuration(30000), true);
            
            final Management mp = new Management(pullRequest);
            mp.setTo(DESTINATION);
            mp.setResourceURI(resource);
            
            mp.prettyPrint(logfile);
            final Addressing praddr = HttpClient.sendRequest(mp);
            praddr.prettyPrint(logfile);
            if (praddr.getBody().hasFault()) {
                praddr.prettyPrint(System.err);
                fail(praddr.getBody().getFault().getFaultString());
            }
            
            final EnumerationExtensions pullResponse = new EnumerationExtensions(praddr);
            final PullResponse pr = pullResponse.getPullResponse();
            // update context for the next pull (if any)
            if (pr.getEnumerationContext() != null) {
                context = (String) pr.getEnumerationContext().getContent().get(0);
            }
            for (final EnumerationItem item : pullResponse.getItems()) {
                assertMode(mode, item);
            }
            
            final AttributableNonNegativeInteger pe = pullResponse.getTotalItemsCountEstimate();
            assertNotNull(pe);
            assertTrue(pe.getValue().intValue() > 0);
        }
        
        if (enuResponse.isEndOfSequence() == false) {
            final EnumerationExtensions pullRequest = new EnumerationExtensions();
            pullRequest.setAction(Enumeration.RELEASE_ACTION_URI);
            pullRequest.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
            pullRequest.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
            pullRequest.setRelease(context);
            
            final Management mp = new Management(pullRequest);
            mp.setTo(DESTINATION);
            mp.setResourceURI(resource);
            
            mp.prettyPrint(logfile);
            final Addressing praddr = HttpClient.sendRequest(mp);
            praddr.prettyPrint(logfile);
            if (praddr.getBody().hasFault()) {
                praddr.prettyPrint(System.err);
                fail(praddr.getBody().getFault().getFaultString());
            }
        }
    }
    
    private static void assertMode(final EnumerationExtensions.Mode mode,
            final EnumerationItem item) {
        
        final Object elt = item.getItem();
        final EndpointReferenceType epr = item.getEndpointReference();
        
        if (mode == null) {
            assertNotNull(elt);
            assertNull(epr);
        } else if (EnumerationExtensions.Mode.EnumerateObjectAndEPR.equals(mode)) {
            assertNotNull(elt);
            assertNotNull(epr);
        } else if (EnumerationExtensions.Mode.EnumerateEPR.equals(mode)) {
            assertNull(elt);
            assertNotNull(epr);
        } else {
            fail("invalid mode");
        }
    }
}