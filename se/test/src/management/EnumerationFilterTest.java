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
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.7  2007/06/08 15:38:39  denis_rachal
 **The following enhanceent were made to the testing infrastructure:
 **
 **  * Capture of logs in files for junits test
 **  * Added user.wsdl & user.xsd to wsman.war
 **  * Consolidated userenum & user into single handler that is thread safe for load testing
 **
 **Revision 1.6  2007/05/30 20:30:24  nbeers
 **Add HP copyright header
 **
 **
 * $Id: EnumerationFilterTest.java,v 1.8 2007-06-18 17:57:27 nbeers Exp $
 */

package management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableNonNegativeInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
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

    	final String resource = "wsman:auth/user";
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

    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
    	settings.setEnumerationMessageActionType(Enumeration.ENUMERATE_ACTION_URI);
    	settings.setTo(DESTINATION);
    	settings.setResourceUri(resource);
    	settings.setMaxTime(60000);
    	settings.setMaxElements(maxElements);
    	settings.setRequestForOptimizedEnumeration(true);
    	settings.setRequestForTotalItemsCount(true);
    	settings.setEnumerationMode(mode);

        if ((expression != null) && (expression.length() > 0)) {
            if ((dialect != null) && (dialect.length() > 0))
            	settings.setFilterDialect(dialect);
            settings.setFilter(expression);
        }

        // Add the namespace for the filter
        Map<String, String> map = new  HashMap<String, String>();
        map.put("user", UserEnumerationHandler.NS_URI);
        settings.setNamespaceMap(map);

        // Add any options
        if (options != null)
        	settings.setOptionSet(options);

    	final Enumeration enu = EnumerationUtility.buildMessage(null, settings);

        enu.prettyPrint(logfile);
        final Addressing response = HttpClient.sendRequest(enu);

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

        	settings.setEnumerationMessageActionType(Enumeration.PULL_ACTION_URI);
        	settings.setEnumerationContext(context);
        	settings.setMaxTime(30000);
        	settings.setMaxCharacters(0);
        	settings.setMaxElements(3);

        	final Enumeration enuPull = EnumerationUtility.buildMessage(null, settings);
        	enuPull.prettyPrint(logfile);
            final Addressing praddr = HttpClient.sendRequest(enuPull);
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

        	settings.setEnumerationMessageActionType(Enumeration.RELEASE_ACTION_URI);

        	final Enumeration enuRelease = EnumerationUtility.buildMessage(null, settings);

            final Management mp = new Management(enuRelease);
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