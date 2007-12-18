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
