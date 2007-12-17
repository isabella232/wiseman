/*
 * Copyright 2006-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.  Use is subject to license terms.
 */

package com.sun.ws.management.client.api;

import java.util.Map;

/**
 * 
 * A stub used to interact with a SOAP server
 * @author jfdenise
 */
public interface JMXWSSOAPStub {
    /**
     * Send an asynchronous request.
     * @param msg The msg to send
     * @param action The WS-Adressing Action
     * @throws java.lang.Exception If an exception occured during message sending.
     */
    public void invokeAsync(WSManagementRequest msg, String action) throws Exception;
    /**
     * Send a synchronous request.
     * @param msg The msg to send
     * @param action The WS-Adressing Action
     * @return The SOAP Response
     * @throws java.lang.Exception If an exception occured during message sending.
     */
    public WSManagementResponse invoke(WSManagementRequest msg, String action) throws Exception;
   
    /**
     * Get the Response context
     * @return The response context
     */
    public Map<String, ?> getResponseContext();
    /**
     * Get the Request context
     * @return The request context
     */
    public Map<String, ?> getRequestContext();
    
    public WSManagementRequest newRequest() throws Exception;
}
