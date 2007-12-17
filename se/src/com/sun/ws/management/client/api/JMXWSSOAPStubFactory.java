/*
 * Copyright 2006-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.  Use is subject to license terms.
 */

package com.sun.ws.management.client.api;

import java.io.IOException;
import java.util.Map;

/**
 *
 * Create a Stub to deal with an endpoint
 */
public interface JMXWSSOAPStubFactory {
    
    /**
     * Create a <CODE>SOAPStub</CODE>
     * @param endpoint The URL to create a stub for.
     * @param env JMX Connector or ConnectorServer env map
     * @param conf Https configuration
     * @throws java.io.IOException In case an Exception occured during stub creation process.
     * @return A <CODE>SOAPStub</CODE> ready to make invocation.
     */
    public JMXWSSOAPStub createSOAPStub(String endpoint,
            Map<String, ?> env, HttpsConfiguration conf) throws IOException;
}
