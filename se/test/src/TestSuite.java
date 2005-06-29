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
 * $Id: TestSuite.java,v 1.1 2005-06-29 19:18:28 akhilarora Exp $
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import junit.framework.*;

public class TestSuite extends TestCase {
    
    public TestSuite(final String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        final junit.framework.TestSuite suite = new junit.framework.TestSuite("TestSuite");
        suite.addTest(management.ManagementSuite.suite());
        return suite;
    }

    public static void main(final java.lang.String[] args) throws IOException {
        final InputStream is = TestSuite.class.getResourceAsStream("/log.properties");
        if (is != null) {
            LogManager.getLogManager().readConfiguration(is);
        }
        
        junit.textui.TestRunner.run(suite());
    }
}
