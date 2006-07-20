package com.sun.ws.management.server.handler.wsman.auth;

import com.sun.ws.management.framework.handlers.DelegatingHandler;

import framework.models.FileEnumerationHandler;

/**
 * This Handler Deligates to The FileEnumerationHandler Class
 * @author sjc
 */
public class file_Handler  extends DelegatingHandler
{
    public file_Handler()
    {
        super(new FileEnumerationHandler());
    }
}
