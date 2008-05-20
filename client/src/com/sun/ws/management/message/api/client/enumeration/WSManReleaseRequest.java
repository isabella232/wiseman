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

package com.sun.ws.management.message.api.client.enumeration;

import java.util.Map;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Release;

import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.message.api.client.wsman.WSManRequest;
import com.sun.ws.management.message.api.constants.WSManEnumerationConstants;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.xml.XmlBinding;

public class WSManReleaseRequest extends WSManRequest {

	public WSManReleaseRequest(final EndpointReferenceType epr,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		super(epr, WSManEnumerationConstants.RELEASE_ACTION_URI, context,
				binding);
	}

	public void setRelease(final String context) {
		final Release release = WSManEnumerationRequest.FACTORY.createRelease();

		final EnumerationContextType contextType = WSManEnumerationRequest.FACTORY
				.createEnumerationContextType();
		contextType.getContent().add(context);
		release.setEnumerationContext(contextType);
		setPayload(release);
	}

	public SOAPResponse invoke() throws Exception {
		this.validateThis();
		return new WSManReleaseResponse(super.invoke());
	}
	
	public void invokeOneWay() throws Exception {
		this.validateThis();
		super.invokeOneWay();
	}
	
	public void validate() throws FaultException {
		super.validate();
		this.validateThis();
	}
	
	// Private methods follow
	
	private void validateThis() throws FaultException {
		final Object payload = getPayload();
		if (payload == null) {
			throw new IllegalStateException("Payload must be set.");
		}
		if (!(payload instanceof Release)) {
			throw new IllegalStateException("Payload must be of type Release.");
		}
	}
}