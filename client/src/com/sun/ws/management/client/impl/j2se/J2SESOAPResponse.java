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

package com.sun.ws.management.client.impl.j2se;

import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Node;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.xml.XmlBinding;

/**
 *
 * JAX-WS Message API based implementation.
 */
class J2SESOAPResponse implements SOAPResponse {
    
    private final Addressing message;
    private final XmlBinding binding;
    private final Map<String, ?> context;
    
    private boolean payloadRead = false;
    private Object payload = null;
    
    /** Creates a new instance of WSMessage */
    public J2SESOAPResponse(final Addressing message,
    		final XmlBinding binding, 
    		final Map<String, ?> context) {
        this.message = message;
        this.binding = binding;
        this.context = context;
    }

    public Object getPayload() {
         if (!payloadRead) {
			payloadRead = true;
			final SOAPBody body = message.getBody();
			if (body != null) {
				final Node node = body.getFirstChild();
				if (node != null) {
					try {
						payload = binding.unmarshal(node);
					} catch (JAXBException ex) {
						// OK will return first child element
						payload = node;
					}
				}
			}
		}
        return payload;
    }
    
	public Map<String, ?> getResponseContext() {
		return this.context;
	}

	public Object getHeader(QName name) throws SOAPException, JAXBException {
        final SOAPElement[] headers = message.getChildren(message.getHeader(), name);
        if ((headers == null) || (headers.length == 0))
        	return null;
        return binding.unmarshal(headers[0]);
	}

	public Map<String, String> getNamespaceDeclarations() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isFault() throws JAXBException, SOAPException {
		// TODO Auto-generated method stub
		return ((message.getFault() == null) ? false : true);
	}
}
