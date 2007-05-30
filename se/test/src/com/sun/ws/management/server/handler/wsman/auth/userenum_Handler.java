/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: userenum_Handler.java,v 1.5 2007-05-30 20:30:17 nbeers Exp $
 */
package com.sun.ws.management.server.handler.wsman.auth;

import management.MetadataTest;
import java.util.logging.Logger;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementEnumerationAnnotation;

import com.sun.ws.management.server.EnumerationSupport;
import framework.models.FileIteratorFactory;
import framework.models.UserEnumerationHandler;
import framework.models.UserIteratorFactory;

/**
 * This Handler Deligates to The EnumerationUserHandler Class
 * @author simeonpinder
 *
 */
@WsManagementEnumerationAnnotation(
	getDefaultAddressModelDefinition=
		@WsManagementDefaultAddressingModelAnnotation(
			getDefaultAddressDefinition=
				@WsManagementAddressDetailsAnnotation(
					wsaTo=MetadataTest.enuAddress, 
					wsmanResourceURI=MetadataTest.enuResUri
				),
		resourceMetaDataUID = MetadataTest.enuMetaDataUID
		),
	resourceEnumerationAccessRecipe = 
		MetadataTest.enuAccessRecipe,
	resourceFilterUsageDescription = 
		 MetadataTest.enuFilUsage
)
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

