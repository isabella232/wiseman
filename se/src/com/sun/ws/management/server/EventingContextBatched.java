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
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.datatype.DatatypeFactory;


public class EventingContextBatched extends EventingContextWithAck {

	private int maxElements;	 //reference the WS-Management Spec Line 3399
	private Duration maxTime;  //reference the WS-Management Spec Line 3414

	private static final int DEFAULT_MAXELEMENTS = 1; // The default value is not mentioned in WS-Management Spec .
	private static final long DEFAULT_MAXTIME_MINUTES = 5; // The default value is not mentioned in WS-Management Spec .
	
	
	EventingContextBatched(final XMLGregorianCalendar expiration,
            final Filter filter,
            final EndpointReferenceType notifyTo,
            final ContextListener listener,
            final EndpointReferenceType eventReplyTo,
            final Duration operationTimeout,
            final int maxElements,
            final Duration maxTime) {
        super(expiration, filter, notifyTo, listener , eventReplyTo, operationTimeout);
        this.maxElements = maxElements;
        this.maxTime = maxTime; 
    }

	EventingContextBatched(final XMLGregorianCalendar expiration,
            final Filter filter,
            final EndpointReferenceType notifyTo,
            final ContextListener listener,
            final int maxElements,
            final Duration maxTime)
            throws DatatypeConfigurationException{
        super(expiration, filter, notifyTo, listener);
        this.maxElements = maxElements;
        this.maxTime = maxTime; 
    }
	
	EventingContextBatched(final XMLGregorianCalendar expiration,
            final Filter filter,
            final EndpointReferenceType notifyTo,
            final ContextListener listener) 
            throws DatatypeConfigurationException {
		super(expiration, filter, notifyTo, listener);
        this.maxElements = DEFAULT_MAXELEMENTS;
       	this.maxTime = DatatypeFactory.newInstance().newDuration(DEFAULT_MAXTIME_MINUTES * 60000);
	}
	
	int getMaxElements(){
		return this.maxElements;
	}
	void setMaxElements(int maxElements){
		this.maxElements = maxElements;
	}
	
	Duration getMaxTime(){
		return this.maxTime;
	}

	void setMaxTime(Duration maxTime){
		this.maxTime = maxTime;
	}
}

