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
 * $Id: TestBase.java,v 1.11 2007-06-08 15:38:39 denis_rachal Exp $
 */

package management;

import java.io.FileOutputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import junit.framework.TestCase;

import com.sun.ws.management.Message;
import com.sun.ws.management.transport.HttpClient;

/**
 * Base class for Unit tests
 */
public abstract class TestBase extends TestCase {
    
    public static final String UUID_SCHEME = "uuid:";
    public static final String DESTINATION =
            System.getProperty("wsman.dest", "http://localhost:8080/wsman/");
    
    protected static final String NS_URI = "http://schemas.company.com/model";
    protected static final String NS_PREFIX = "model";
    
    protected FileOutputStream logfile = null;
	protected static Logger LOG = Logger.getLogger("com.sun.ws.management");
	private FileHandler LOG_HANDLER = null;
    
    public TestBase(final String testName) {
        super(testName);
        
		// Set the level in logging.properties
		// LOG.setLevel(Level.FINE);
		LOG.setUseParentHandlers(false);
    }
    
    protected void setUp() throws java.lang.Exception {
        logfile = new FileOutputStream(getClass().getCanonicalName() + "." +
                getName() + ".Output.xml");

		if (LOG.getLevel() != Level.OFF) {
			LOG_HANDLER = new FileHandler(getClass().getName() + "."
					+ getName() + ".Log.txt");
			LOG_HANDLER.setFormatter(new SimpleFormatter());
			LOG.addHandler(LOG_HANDLER);
		}
        final String basicAuth = System.getProperty("wsman.basicauthentication");
        if ("true".equalsIgnoreCase(basicAuth)) {
            HttpClient.setAuthenticator(new transport.BasicAuthenticator());
        }
    }
    
    protected void tearDown() throws java.lang.Exception {
        logfile.close();
        
		if (LOG_HANDLER != null) {
			LOG.removeHandler(LOG_HANDLER);
			LOG_HANDLER.close();
		}
    }
    
    public void log(final Message msg) throws Exception {
        msg.prettyPrint(logfile);
    }
}
