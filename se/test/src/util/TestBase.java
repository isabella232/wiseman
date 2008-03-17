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
 * $Id: TestBase.java,v 1.1.8.1 2008-03-17 07:31:37 denis_rachal Exp $
 */

package util;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import com.sun.ws.management.Message;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

/**
 * Base class for Unit tests
 */
public abstract class TestBase extends TestCase {
    
    public static final String UUID_SCHEME = "uuid:";
    public static final String JAXB_PACKAGE_FOO_TEST = "foo.test";
    public static final String JAXB_PACKAGE_USER_TEST = "com.hp.examples.ws.wsman.user";
    public static final String USER_NS_PREFIX = "user";
    public static final String USER_NS_URI = "http://examples.hp.com/ws/wsman/user";
    public static final String FOO_NS_PREFIX = "foo";
    public static final String FOO_NS_URI = "http://test.foo";
    
    public static final Map<String, String> NS_MAP = new HashMap<String, String>();
    
	public static final Logger LOG = Logger.getLogger("com.sun.ws.management");
	
    public static String DESTINATION =
        System.getProperty("wsman.dest", "http://localhost:8080/wsman/");
	public static XmlBinding binding;

    public FileOutputStream logfile = null;
    
	private FileHandler LOG_HANDLER = null;
	
	static {
		// Setup defaults
        final String basicAuth = System.getProperty("wsman.basicauthentication");
        if ((basicAuth == null) || (basicAuth.length() == 0)) {
        	System.setProperty("wsman.basicauthentication", "true");
        }
        final String validate = System.getProperty("com.sun.ws.management.xml.validate");
        if ((validate == null) || (validate.length() ==0)) {
        	System.setProperty("com.sun.ws.management.xml.validate", "true");
        }
		final String packagesKey = XmlBinding.class.getPackage().getName() + ".custom.packagenames";
		final String packages = System.getProperty(packagesKey);
		if ((packages == null) || (packages.length() == 0)) {
			System.setProperty(packagesKey,
					JAXB_PACKAGE_FOO_TEST + "," + JAXB_PACKAGE_USER_TEST);
		}
		try {
			binding = new XmlBinding(null, JAXB_PACKAGE_FOO_TEST + ":" + JAXB_PACKAGE_USER_TEST);
		} catch (JAXBException e) {
			fail("Cannot initialize XmlBinding");
		}
		NS_MAP.put(USER_NS_PREFIX, USER_NS_URI);
		NS_MAP.put(FOO_NS_PREFIX, FOO_NS_URI);
	}
    
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
        if (basicAuth.equals("true")) {
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
