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
 * $Id: ManagementSuite.java,v 1.3 2005-11-12 01:29:15 akhilarora Exp $
 */

package management;

import junit.framework.*;

public class ManagementSuite extends TestCase {
    
    public ManagementSuite(final String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite("ManagementSuite");
        suite.addTest(management.AddressingTest.suite());
        suite.addTest(management.EnumerationTest.suite());
        suite.addTest(management.EventingTest.suite());
        suite.addTest(management.EventingExtensionsTest.suite());
        suite.addTest(management.ManagementTest.suite());
        suite.addTest(management.TransferTest.suite());
        return suite;
    }

    public static void main(final java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
