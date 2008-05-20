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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableEmpty;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributablePositiveInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;

import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.message.api.client.wsman.WSManRequest;
import com.sun.ws.management.message.api.constants.WSManEnumerationConstants;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

public class WSManEnumerateRequest extends WSManEnumerationRequest {

	private EndpointReferenceType epr = null;
	private int maxElements = -1;

	public WSManEnumerateRequest(final EndpointReferenceType epr,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		super(epr, WSManEnumerationConstants.ENUMERATE_ACTION_URI, context,
				binding);
		this.epr = epr;
		addNamespaceDeclaration(WSManEnumerationConstants.NS_PREFIX,
				WSManEnumerationConstants.NS_URI);
	}

	public WSManEnumerateRequest(final String to, final String action,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		this(to, action, null, null, context, binding);
	}

	public WSManEnumerateRequest(final String to, final String action,
			final QName service, final QName port,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		super(to, action, service, port, context, binding);
		addNamespaceDeclaration(WSManEnumerationConstants.NS_PREFIX,
				WSManEnumerationConstants.NS_URI);
	}

	public Enumerate createEnumerate(final String expires,
			final String xpathFilter, final Map<String, String> namespaces,
			final boolean optimize, final int maxElements,
			final EnumerationModeType mode) throws SOAPException, JAXBException {
		DialectableMixedDataType filter = null;

		if ((xpathFilter != null) && (xpathFilter.trim().length() > 0))
			filter = createFilter(xpathFilter, namespaces);
		return createEnumerate(expires, filter, optimize, maxElements, mode);
	}

	public Enumerate createEnumerate(final String expires,
			final DialectableMixedDataType filter, final boolean optimize,
			final int maxElements, final EnumerationModeType mode) {

		// Create the Enumeration element
		final Enumerate enumerate = WSManEnumerationRequest.FACTORY
				.createEnumerate();
		if (expires != null) {
			final String exp = expires.trim();
			if (exp.length() > 0)
				enumerate.setExpires(exp);
		}

		final List<Object> list = enumerate.getAny();

		// Add the WSMan filter
		if (filter != null) {
			final JAXBElement<DialectableMixedDataType> jaxbFilter = WSManRequest.FACTORY
					.createFilter(filter);
			list.add(jaxbFilter);
		}

		// Add the WSMan Enumeration Mode
		if (mode != null) {
			list.add(WSManRequest.FACTORY.createEnumerationMode(mode));
		}

		// Add the WSMan optimize option
		if (optimize == true) {
			list.add(WSManRequest.FACTORY
					.createOptimizeEnumeration(new AttributableEmpty()));

			if (maxElements > 0) {
				final AttributablePositiveInteger posInt = new AttributablePositiveInteger();
				posInt.setValue(new BigInteger(Integer.toString(maxElements)));
				list.add(WSManRequest.FACTORY.createMaxElements(posInt));
			}
		}

		return enumerate;
	}

	public DialectableMixedDataType createFilter(final String xpath,
			final Map<String, String> namespaces) throws SOAPException,
			JAXBException {
		final List<Object> expression = new ArrayList<Object>();
		expression.add(xpath);
		return createFilter(XPath.NS_URI, expression, namespaces);
	}

	public DialectableMixedDataType createFilter(final String dialect,
			final List<Object> expression, final Map<String, String> namespaces)
			throws SOAPException, JAXBException {
		if (expression == null)
			return null;

		final DialectableMixedDataType dialectableMixedDataType = WSManRequest.FACTORY
				.createDialectableMixedDataType();
		if (dialect != null) {
			dialectableMixedDataType.setDialect(dialect);
		}

		dialectableMixedDataType.getOtherAttributes().put(SOAP.MUST_UNDERSTAND,
				Boolean.TRUE.toString());

		// add the query string to the content of the FragmentTransfer Header
		dialectableMixedDataType.getContent().addAll(expression);

		// Map<QName, String> attributeMap =
		// dialectableMixedDataType.getOtherAttributes();
		for (Map.Entry<String, String> decl : namespaces.entrySet()) {
			// We use a trick here to add the namespace declarations as
			// attributes.
			// final QName key = new QName(XMLConstants.XMLNS_ATTRIBUTE + ":" +
			// decl.getKey());
			// attributeMap.put(key, decl.getValue());
			addNamespaceDeclaration(decl.getKey(), decl.getValue());
		}

		return dialectableMixedDataType;
	}
	
	public Enumerate getEnumerate() {
		return (Enumerate)getHeader(WSManEnumerationConstants.ENUMERATE);
	}

	public void setEnumerate(final String expires, final String xpathFilter,
			final Map<String, String> namespaces, final boolean optimize,
			final int maxElements, final EnumerationModeType mode)
			throws SOAPException, JAXBException {

		final Enumerate enumerate = createEnumerate(expires, xpathFilter,
				namespaces, optimize, maxElements, mode);
		setEnumerate(enumerate);
	}

	public void setEnumerate(final Enumerate enumerate) {
		setPayload(enumerate);
	}
	
	public boolean getRequestTotalItemsCountEstimate() {
		if (getHeader(WSManEnumerationConstants.REQUEST_TOTAL_ITEMS_COUNT_ESTIMATE) != null)
			return true;
		else
			return false;
	}

	public void setRequestTotalItemsCountEstimate() throws JAXBException {
		final AttributableEmpty empty = new AttributableEmpty();
		final JAXBElement<AttributableEmpty> emptyElement = WSManRequest.FACTORY
				.createRequestTotalItemsCountEstimate(empty);
		addHeader(emptyElement);
	}

	public SOAPResponse invoke() throws Exception {
		this.validateThis();
		if (this.epr != null) {
			return new WSManEnumerateResponse(super.invoke(), this.epr, this
					.getRequestContext(), this.getXmlBinding(),
					this.maxElements);
		} else {
			final EndpointReferenceType endpoint = createEndpointReference(
					getTo(), getResourceURI(), getServiceName(), getPortName()
							.getLocalPart(), null);
			return new WSManEnumerateResponse(super.invoke(), endpoint, this
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
		if (!(payload instanceof Enumerate)) {
			throw new IllegalStateException(
					"Payload must be of type Enumerate.");
		}
	}
}