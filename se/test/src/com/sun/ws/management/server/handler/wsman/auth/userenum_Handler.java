package com.sun.ws.management.server.handler.wsman.auth;

import java.util.logging.Logger;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.server.EnumerationSupport;

import framework.models.FileIteratorFactory;
import framework.models.UserEnumerationHandler;
import framework.models.UserIteratorFactory;

/**
 * This Handler Deligates to The EnumerationUserHandler Class
 * @author simeonpinder
 *
 */
public class userenum_Handler extends DelegatingHandler {
	
	public final static String RESOURCE_URI = "wsman:auth/userenum";
	
    // Log for logging messages
    @SuppressWarnings("unused")
    private final Logger log;
    
    public userenum_Handler() {
        super(new UserEnumerationHandler());
        
        log = Logger.getLogger(userenum_Handler.class.getName());
		try {
			EnumerationSupport.registerIteratorFactory(RESOURCE_URI,
					new UserIteratorFactory(RESOURCE_URI));
		} catch (Exception e) {
			throw new InternalErrorFault(e.getMessage());
		}
    }
}

