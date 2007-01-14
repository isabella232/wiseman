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
 * $Id: EnumerationHandler.java,v 1.7 2007-01-14 17:52:37 denis_rachal Exp $
 *
 */
package com.sun.ws.management.framework.enumeration;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.soap.FaultException;

/**
 *
 */
public abstract class EnumerationHandler extends TransferSupport implements Enumeratable
{
	private static final Logger LOG = Logger.getLogger(EnumerationHandler.class
			.getName());

	protected EnumerationHandler() {
	}

	// WS-Enumeration operations follow
	
	public void release(HandlerContext context, Enumeration enuRequest,
			Enumeration enuResponse) {

		try {
			EnumerationSupport.release(enuRequest, enuResponse);
		} catch (SOAPException e) {
			LOG.log(Level.SEVERE, "", e);
			throw new InternalErrorFault(e.getMessage());
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "", e);
			throw new InternalErrorFault(e.getMessage());
		} catch (FaultException e) {
			LOG.log(Level.SEVERE, "", e);
			throw e;
		} catch (Throwable t) {
			LOG.log(Level.SEVERE, "", t);
			throw new InternalErrorFault(t.getMessage());
		}
	}

	public void pull(HandlerContext context, Enumeration enuRequest,
			Enumeration enuResponse) {
		try {
			EnumerationSupport.pull(enuRequest, enuResponse);
		} catch (SOAPException e) {
			LOG.log(Level.SEVERE, "", e);
			throw new InternalErrorFault(e.getMessage());
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "", e);
			throw new InternalErrorFault(e.getMessage());
		} catch (FaultException e) {
			LOG.log(Level.SEVERE, "", e);
			throw e;
		} catch (Throwable t) {
			LOG.log(Level.SEVERE, "", t);
			throw new InternalErrorFault(t.getMessage());
		}
	}

	public void enumerate(HandlerContext context, Enumeration enuRequest,
			Enumeration enuResponse) {

		try {
			EnumerationSupport.enumerate(context, enuRequest, enuResponse);
		} catch (DatatypeConfigurationException e) {
			LOG.log(Level.SEVERE, "", e);
			throw new InternalErrorFault(e.getMessage());
		} catch (SOAPException e) {
			LOG.log(Level.SEVERE, "", e);
			throw new InternalErrorFault(e.getMessage());
		} catch (JAXBException e) {
			LOG.log(Level.SEVERE, "", e);
			throw new InternalErrorFault(e.getMessage());
		} catch (FaultException e) {
			LOG.log(Level.SEVERE, "", e);
			throw e;
		} catch (Throwable t) {
			LOG.log(Level.SEVERE, "", t);
			throw new InternalErrorFault(t.getMessage());
		}
	}

	public void getStatus(HandlerContext context, Enumeration enuRequest,
			Enumeration enuResponse) {
		throw new ActionNotSupportedFault();
	}

	public void renew(HandlerContext context, Enumeration enuRequest,
			Enumeration enuResponse) {
		throw new ActionNotSupportedFault();
	}
}