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

import java.math.BigInteger;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableEmpty;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Pull;

import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.message.api.client.wsman.WSManRequest;
import com.sun.ws.management.message.api.constants.WSManEnumerationConstants;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.xml.XmlBinding;

public class WSManPullRequest extends WSManRequest {

	private EndpointReferenceType epr;
	private int maxElements = -1;

	public WSManPullRequest(final EndpointReferenceType epr,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		super(epr, WSManEnumerationConstants.PULL_ACTION_URI, context, binding);
		this.epr = epr;
		addNamespaceDeclaration(WSManEnumerationConstants.NS_PREFIX,
				WSManEnumerationConstants.NS_URI);
	}

	public WSManPullRequest(final String to, final String action,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		this(to, action, null, null, context, binding);
	}

	public WSManPullRequest(final String to, final String action,
			final QName service, final QName port,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		super(to, action, service, port, context, binding);
		addNamespaceDeclaration(WSManEnumerationConstants.NS_PREFIX,
				WSManEnumerationConstants.NS_URI);
	}

	public void setPull(final Object context, final int maxChars,
			final int maxElements, final Duration maxDuration) {
		final Pull pull = WSManEnumerationRequest.FACTORY.createPull();

		final EnumerationContextType contextType = WSManEnumerationRequest.FACTORY
				.createEnumerationContextType();
		contextType.getContent().add(context);
		pull.setEnumerationContext(contextType);

		if (maxChars > 0) {
			pull.setMaxCharacters(BigInteger.valueOf((long) maxChars));
		}
		if (maxElements > 0) {
			pull.setMaxElements(BigInteger.valueOf((long) maxElements));
		}
		if (maxDuration != null) {
			pull.setMaxTime(maxDuration);
		}
		setPayload(pull);
	}

	public void requestTotalItemsCountEstimate() throws JAXBException {
		final AttributableEmpty empty = new AttributableEmpty();
		final JAXBElement<AttributableEmpty> emptyElement = WSManRequest.FACTORY
				.createRequestTotalItemsCountEstimate(empty);
		addHeader(emptyElement);
	}

	public SOAPResponse invoke() throws Exception {
		this.validateThis();
		if (this.epr != null) {
			return new WSManPullResponse(super.invoke(), this.epr, this
					.getRequestContext(), this.getXmlBinding(),
					this.maxElements);
		} else {
			final EndpointReferenceType endpoint = createEndpointReference(
					getTo(), getResourceURI(), getServiceName(), getPortName()
							.getLocalPart(), null);
			return new WSManPullResponse(super.invoke(), this.epr, this
					.getRequestContext(), this.getXmlBinding(),
					this.maxElements);
		}
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
		if (!(payload instanceof Pull)) {
			throw new IllegalStateException("Payload must be of type Pull.");
		}
	}
}