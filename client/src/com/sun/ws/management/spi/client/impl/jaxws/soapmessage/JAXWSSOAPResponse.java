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
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 *
 */

package com.sun.ws.management.spi.client.impl.jaxws.soapmessage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;

import org.w3c.dom.Node;

import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.xml.XmlBinding;

/**
 *
 * JAX-WS Message API based implementation.
 */
class JAXWSSOAPResponse implements SOAPResponse {
    
    private final SOAPMessage message;
    private final XmlBinding binding;
	private final Dispatch<SOAPMessage> proxy;
    
    private boolean payloadRead;
    private Object payload;
    
    /** Creates a new instance of WSMessage */
    public JAXWSSOAPResponse(SOAPMessage message, XmlBinding binding, Dispatch<SOAPMessage> proxy) {
        this.message = message;
        this.binding = binding;
        this.proxy = proxy;
    }
    
    private Unmarshaller newUnmarshaller() throws JAXBException {
        return binding.createUnmarshaller();
    }

    public Object getPayload() throws IOException, SOAPException {
		if (!payloadRead) {
			payloadRead = true;
			final Node node = message.getSOAPBody().getFirstChild();
			if (node != null) {
				try {
					payload = newUnmarshaller().unmarshal(node);
				} catch (JAXBException ex) {
					// OK will return node
					payload = node;
				}
			}
		}
		return payload;
	}
    
	public Map<String, ?> getResponseContext() {
		return proxy.getResponseContext();
	}

	public Object getHeader(QName name) throws JAXBException, IOException, SOAPException {
		final SOAPHeader header = message.getSOAPHeader();
        final Iterator<SOAPElement> children = header.getChildElements(name);
        if ((children == null) || (children.hasNext() == false))
        	return null;
        return newUnmarshaller().unmarshal(children.next());
        // TODO: Fix to handle multiple headers with the same name..
	}

	public boolean isFault() {
		// TODO Auto-generated method stub
		return false;
	}

	public void writeTo(OutputStream os, boolean formatted) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public String toString() {
		return message.toString();
	}
}
