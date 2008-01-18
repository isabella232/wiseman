/*
 * Copyright 2008 Sun Microsystems, Inc.
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
 ** Copyright (C) 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 ***
 *** Fudan University
 *** Author: Chuan Xiao (cxiao@fudan.edu.cn)
 */
package com.sun.ws.management.server;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xpath.XPathExpressionException;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.datatype.DatatypeFactory;

import com.sun.ws.management.addressing.Addressing;

public class EventingContextWithAck extends EventingContext {
	
	private EndpointReferenceType eventReplyTo;
	private Duration operationTimeout;
	private static final long DEFAULT_TIMEOUT_SECONDS = 5; // the default value is set to 5 seconds.

	public EventingContextWithAck(final XMLGregorianCalendar expiration,
            final Filter filter,
            final EndpointReferenceType notifyTo,
            final ContextListener listener,
            final EndpointReferenceType eventReplyTo ,
            final Duration operationTimeout) {
        super(expiration, filter, notifyTo, listener);
        this.eventReplyTo = eventReplyTo;
        this.operationTimeout = operationTimeout;
    }

	public EventingContextWithAck(final XMLGregorianCalendar expiration,
            final Filter filter,
            final EndpointReferenceType notifyTo,
            final ContextListener listener) 
            throws DatatypeConfigurationException{
		
        this(expiration, filter, notifyTo, listener,
        		Addressing.createEndpointReference(Addressing.ANONYMOUS_ENDPOINT_URI,null,null,null,null),
        		DatatypeFactory.newInstance().newDuration(DEFAULT_TIMEOUT_SECONDS * 1000)
            );
	}
	
    EndpointReferenceType getEventReplyTo() {
        return eventReplyTo;
    }
    
    void setEventReplyTo(EndpointReferenceType eventReplyTo) {
    	this.eventReplyTo = eventReplyTo;
    }

    Duration getOperationTimeout() {
    	return this.operationTimeout;
    }
    
    void setOperationTimeout(Duration operationTimeout){
    	this.operationTimeout = operationTimeout;
    }
}
