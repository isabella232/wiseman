/*
 * Copyright 2005-2008 Sun Microsystems, Inc.
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
 ** Copyright (C) 2006, 2007, 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 *
 */

package com.sun.ws.management.client.message.addressing;

import java.io.OutputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.Relationship;

import com.sun.ws.management.client.message.SOAPResponse;

/**
 * 
 * Implementation of a WS-Addressing response
 */
public class WSAddressingResponse implements SOAPResponse {
	
    public static final String NS_PREFIX = "wsa";
    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
	
    public static final QName ACTION = new QName(NS_URI, "Action", NS_PREFIX);
    public static final QName TO = new QName(NS_URI, "To", NS_PREFIX);
    public static final QName MESSAGE_ID = new QName(NS_URI, "MessageID", NS_PREFIX);
    public static final QName REPLY_TO = new QName(NS_URI, "ReplyTo", NS_PREFIX);
    public static final QName FAULT_TO = new QName(NS_URI, "FaultTo", NS_PREFIX);
    public static final QName FROM = new QName(NS_URI, "From", NS_PREFIX);
    public static final QName ADDRESS = new QName(NS_URI, "Address", NS_PREFIX);
    public static final QName RELATES_TO = new QName(NS_URI, "RelatesTo", NS_PREFIX);
    public static final QName RETRY_AFTER = new QName(NS_URI, "RetryAfter", NS_PREFIX);
    public static final QName ENDPOINT_REFERENCE = new QName(NS_URI, "EndpointReference", NS_PREFIX);
		
	private final SOAPResponse response;
	
    private boolean payloadRead;
    private Object payload;
    private boolean isFaultRead;
    private boolean isFault;
    private boolean actionRead;
    private String action;
    private boolean toRead;
    private String to;
    private boolean fromRead;
    private EndpointReferenceType from;
    private boolean messageIdRead;
    private String messageId;
    private boolean relatesToRead;
    private Relationship relatesTo;
	
	WSAddressingResponse() {
        this.response = null;
	}
	
	public WSAddressingResponse(final SOAPResponse response) {
		this.response = response;
	}

	public synchronized Object getPayload() throws Exception  {
        if(!payloadRead) {
        	payloadRead = true;
        	payload = this.response.getPayload();
        }
        return payload;
	}

	public Object getHeader(QName name) throws Exception {
		return this.response.getHeader(name);
	}

	public synchronized boolean isFault() throws Exception {
        if(!isFaultRead) {
        	isFaultRead = true;
        	isFault = this.response.isFault();
        }
        return isFault;
	}
	
	public synchronized String getAction() throws Exception {
		if (!actionRead) {
			actionRead = true;
			action = getAttributedURIHeader(ACTION);
		}
		return action;
	}

	public synchronized String getTo() throws Exception  {
		if (!toRead) {
			toRead = true;
			to = getAttributedURIHeader(TO);
		}
		return to;	
	}

	public synchronized EndpointReferenceType getFrom() throws Exception {
		if (!fromRead) {
			fromRead = true;
			final Object header = this.response.getHeader(FROM);
			if (header != null) {
				if ((header instanceof JAXBElement)
						&& (((JAXBElement)header).getDeclaredType()
								.equals(EndpointReferenceType.class))) {
					from = ((JAXBElement<EndpointReferenceType>)header).getValue();
				}
			}
		}
		return from;	
	}

	public synchronized String getMessageID() throws Exception {
		if (!messageIdRead) {
			messageIdRead = true;
			messageId = getAttributedURIHeader(MESSAGE_ID);
		}
		return messageId;		
	}

	public synchronized Relationship getRelatesTo() throws Exception  {
		if (!relatesToRead) {
			relatesToRead = true;
			final Object header = this.response.getHeader(RELATES_TO);
			if (header != null) {
				if ((header instanceof JAXBElement)
						&& (((JAXBElement)header).getDeclaredType()
								.equals(EndpointReferenceType.class))) {
					relatesTo = ((JAXBElement<Relationship>)header).getValue();
				}
			}
		}
		return relatesTo;		
	}
	
	private String getAttributedURIHeader(final QName name) throws Exception  {
		String value = null;
		
		final Object header = this.response.getHeader(name);
		if (header != null) {
			if ((header instanceof JAXBElement)
					&& (((JAXBElement)header).getDeclaredType()
							.equals(AttributedURI.class))) {
				value = ((JAXBElement<AttributedURI>)header).getValue().getValue().trim();
			}
		}
		return value;
	}

	public void writeTo(OutputStream os, boolean formatted) throws Exception {
		this.response.writeTo(os, formatted);
	}
	
	public String toString() {
		return this.response.toString();
	}
}
