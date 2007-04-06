/*
 * Copyright 2006 Sun Microsystems, Inc.
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
 * $Id: ResourceHandler.java,v 1.1 2007-04-06 10:03:11 jfdenise Exp $
 *
 */
package com.sun.ws.management.framework.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.framework.enumeration.Enumeratable;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.server.EventingSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.soap.FaultException;

/**
 *
 */
public abstract class ResourceHandler extends EnumerationHandler implements Enumeratable
{
	private static final Logger LOG = Logger.getLogger(ResourceHandler.class
			.getName());

	protected ResourceHandler() {
	}

	// WS-Eventing operations follow

	public Object subscribe(final HandlerContext context, 
			                final Eventing request,
			                final Eventing response) {
		throw new ActionNotSupportedFault();
	}
	
	public void unsubscribe(final HandlerContext context,
			                final Eventing request,
			                final Eventing response) {

		throw new ActionNotSupportedFault();
	}

	public void getSubscriptionStatus(final HandlerContext context,
			              final Eventing request,
			              final Eventing response) {
		throw new ActionNotSupportedFault();
	}

	public void renewSubscription(final HandlerContext context,
			          final Eventing request,
			          final Eventing response) {
		throw new ActionNotSupportedFault();
	}
}