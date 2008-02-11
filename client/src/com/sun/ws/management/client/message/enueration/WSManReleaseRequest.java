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

package com.sun.ws.management.client.message.enueration;

import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Release;

import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.client.message.wsman.WSManRequest;
import com.sun.ws.management.xml.XmlBinding;

public class WSManReleaseRequest extends WSManRequest {
	
    public static final String ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Release";

	WSManReleaseRequest() {
        super(null);
	}
	
	public WSManReleaseRequest(final EndpointReferenceType epr,
			final Map<String, ?> context, final XmlBinding binding)
	throws Exception  {
		super(epr, context, binding);
		setAction(ACTION_URI);
	}

	public WSManReleaseRequest(final String endpoint,
			final Map<String, ?> context, final QName serviceName,
			final QName portName, final XmlBinding binding) throws Exception {
		super(endpoint, context, serviceName, portName, binding);
		setAction(ACTION_URI);
	}
	
	public WSManReleaseRequest(final SOAPRequest request) throws JAXBException {
		super(request);
		setAction(ACTION_URI);
	}

	public void setRelease(final String context) {
        final Release release = WSManEnumerationRequest.FACTORY.createRelease();

        final EnumerationContextType contextType = 
        	WSManEnumerationRequest.FACTORY.createEnumerationContextType();
        contextType.getContent().add(context);
        release.setEnumerationContext(contextType);
        setPayload(release);
	}
	
	public SOAPResponse invoke() throws Exception {
		// TODO: Message sanity checks go here.
		return new WSManReleaseResponse(super.invoke());
	}
}