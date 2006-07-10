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
 * $Id: IdentifyTest.java,v 1.2 2006-07-10 01:41:11 akhilarora Exp $
 */

package management;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.transport.HttpClient;
import javax.xml.soap.SOAPElement;

/**
 * Unit test for the Identify operation
 */
public class IdentifyTest extends TestBase {
    
    public IdentifyTest(final String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite(IdentifyTest.class);
        return suite;
    }
    
    public void testIdentify() throws Exception {
        
        final Identify identify = new Identify();
        identify.setIdentify();

        identify.prettyPrint(logfile);
        logfile.write("\n\n".getBytes());
        final Addressing response = HttpClient.sendRequest(identify.getMessage(), DESTINATION);
        response.prettyPrint(logfile);
        if (response.getBody().hasFault()) {
            fail(response.getBody().getFault().getFaultString());
        }
        
        final Identify id = new Identify(response);
        final SOAPElement idr = id.getIdentifyResponse();
        assertNotNull(idr);
        assertNotNull(id.getChildren(idr, Identify.PRODUCT_VENDOR));
        assertNotNull(id.getChildren(idr, Identify.PRODUCT_VERSION));
        assertNotNull(id.getChildren(idr, Identify.PROTOCOL_VERSION));
        assertNotNull(id.getChildren(idr, Identify.BUILD_ID));
        assertNotNull(id.getChildren(idr, Identify.SPEC_VERSION));
    }
}
