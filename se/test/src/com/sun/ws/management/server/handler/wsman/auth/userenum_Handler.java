package com.sun.ws.management.server.handler.wsman.auth;

import com.sun.ws.management.framework.handlers.DelegatingHandler;

import framework.models.UserEnumerationHandler;

/**
 * This Handler Deligates to The EnumerationUserHandler Class
 * @author simeonpinder
 *
 */
public class userenum_Handler extends DelegatingHandler {
    public userenum_Handler() {
        super(new UserEnumerationHandler());
    }
}

