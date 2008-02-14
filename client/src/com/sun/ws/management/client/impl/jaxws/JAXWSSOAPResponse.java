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

package com.sun.ws.management.client.impl.jaxws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Dispatch;

import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;

/**
 *
 * JAX-WS Message API based implementation.
 */
class JAXWSSOAPResponse implements SOAPResponse {
    
    private final Message message;
    private final HeaderList headers;
    private final XmlBinding binding;
	private final Dispatch<Message> proxy;
    
    private boolean payloadRead;
    private Object payload;
    
    /** Creates a new instance of WSMessage */
    public JAXWSSOAPResponse(Message message, XmlBinding binding, Dispatch<Message> proxy) {
        this.message = message;
        this.headers = message.getHeaders();
        this.binding = binding;
        this.proxy = proxy;
    }
    
    private Unmarshaller newUnmarshaller() throws JAXBException {
        return binding.createUnmarshaller();
    }

    public Object getPayload() throws IOException {
         if(!payloadRead) {
            payloadRead = true;
            try {
                payload = message.readPayloadAsJAXB(newUnmarshaller());
            } catch(JAXBException ex) {
                // OK will return XMLStreamReader
                try {
					payload = message.readPayload();
				} catch (XMLStreamException e) {
					throw new IOException(e.getMessage());
				}
            }
        }
        return payload;
    }
    
	public Map<String, ?> getResponseContext() {
		return proxy.getResponseContext();
	}

	public Object getHeader(QName name) throws JAXBException, IOException {
		Header header = headers.get(name, true);
		if (header != null)
			try {
				// Return the JAXBElement
				return header.readAsJAXB(newUnmarshaller());
			} catch (JAXBException ex) {
				// OK will return XMLStreamReader
				try {
					return header.readHeader();
				} catch (XMLStreamException e) {
					throw new IOException(e.getMessage());
				}
			}
		else
			return null;
	}

	public Map<String, String> getNamespaceDeclarations() {
		// TODO Auto-generated method stub
		return new HashMap<String, String>();
	}

	public boolean isFault() {
		// TODO Auto-generated method stub
		return false;
	}
}
