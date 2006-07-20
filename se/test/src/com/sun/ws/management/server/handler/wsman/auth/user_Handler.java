package com.sun.ws.management.server.handler.wsman.auth;

import com.sun.ws.management.framework.handlers.DelegatingHandler;

import framework.models.UserHandler;

/**
 * This Handler Deligates to The UserHandler Class
 * @author wire
 *
 */
public class user_Handler extends DelegatingHandler {
    public user_Handler() {
        super(new UserHandler());
    }
}
