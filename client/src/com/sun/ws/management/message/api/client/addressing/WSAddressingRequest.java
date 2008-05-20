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

package com.sun.ws.management.message.api.client.addressing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import com.sun.ws.management.addressing.InvalidMessageInformationHeaderFault;
import com.sun.ws.management.addressing.MessageInformationHeaderRequiredFault;
import com.sun.ws.management.message.api.client.soap.SOAPRequest;
import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.message.api.constants.WSAddressingConstants;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XmlBinding;

/**
 * 
 * Implementation of a WS-Addressing request
 */
public class WSAddressingRequest extends SOAPRequest {

	public static final ObjectFactory FACTORY = new ObjectFactory();

	private String uuidScheme = WSAddressingConstants.DEFAULT_UID_SCHEME;

	private String messageId = null;
	private EndpointReferenceType replyTo = null;
	private EndpointReferenceType from = null;
	private EndpointReferenceType faultTo = null;

	public WSAddressingRequest(final EndpointReferenceType epr,
			final String action, final Map<String, ?> context,
			final XmlBinding binding) throws Exception {
		super(epr.getAddress().getValue(), action, getServiceName(epr),
				getPortName(epr), context, binding);
		setTo(epr);
		setAction(action);
		addNamespaceDeclaration(WSAddressingConstants.NS_PREFIX,
				WSAddressingConstants.NS_URI);
	}

	public WSAddressingRequest(final String to, final String action,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		this(to, action, null, null, context, binding);
	}

	public WSAddressingRequest(final String to, final String action,
			final QName service, final QName port,
			final Map<String, ?> context, final XmlBinding binding)
			throws Exception {
		super(to, action, service, port, context, binding);
		setTo(to);
		setAction(action);
		addNamespaceDeclaration(WSAddressingConstants.NS_PREFIX,
				WSAddressingConstants.NS_URI);
	}

	/**
	 * @param uuidScheme
	 *            the uuidScheme to set
	 */
	public synchronized void setUidScheme(String uuidScheme) {
		this.uuidScheme = uuidScheme;
	}

	/**
	 * @return the uuidScheme
	 */
	public synchronized String getUidScheme() {
		return uuidScheme;
	}

	public synchronized String getMessageId() {
		if (messageId == null)
			messageId = getAttributedURIHeader(WSAddressingConstants.MESSAGE_ID);
		return messageId;
	}

	public synchronized void setMessageId(final String msgId)
			throws JAXBException, SOAPException {
		if (getMessageId() != null)
			throw new IllegalStateException(
					"Header wsa:MessageID is already set.");
		final AttributedURI msgIdURI = FACTORY.createAttributedURI();
		msgIdURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND,
				Boolean.TRUE.toString());
		msgIdURI.setValue(msgId.trim());
		final JAXBElement<AttributedURI> msgIdElement = FACTORY
				.createMessageID(msgIdURI);
		addHeader(msgIdElement);
		this.messageId = msgId;
	}

	public synchronized EndpointReferenceType getReplyTo() {
		if (replyTo == null)
			replyTo = getEprHeader(WSAddressingConstants.REPLY_TO);
		return replyTo;
	}

	public synchronized void setReplyTo(final String replyToAddress)
			throws JAXBException {
		setReplyTo(createEndpointReference(replyToAddress, null, null, null,
				null));
	}

	public synchronized void setReplyTo(final EndpointReferenceType replyToEPR)
			throws JAXBException {
		if (getReplyTo() != null)
			throw new IllegalStateException(
					"Header wsa:ReplyTo is already set.");
		final JAXBElement<EndpointReferenceType> replyToElement = FACTORY
				.createReplyTo(replyToEPR);
		addHeader(replyToElement);
		this.replyTo = replyToEPR;
	}

	public synchronized void addReplyToParameter(final QName name,
			final String value) {
		final Element param = createStringElement(name, value);

		addParamToEprHeader(WSAddressingConstants.REPLY_TO, param);
	}

	public synchronized void addReplyToParameter(final Element param) {
		addParamToEprHeader(WSAddressingConstants.REPLY_TO, param);
	}

	@SuppressWarnings("unchecked")
	public synchronized void addReplyToParameter(final JAXBElement param) {
		addParamToEprHeader(WSAddressingConstants.REPLY_TO, param);
	}

	public synchronized EndpointReferenceType getFaultTo() {
		if (faultTo == null)
			faultTo = getEprHeader(WSAddressingConstants.FAULT_TO);
		return faultTo;
	}

	public synchronized void setFaultTo(final String faultToAddress)
			throws JAXBException {
		setFaultTo(createEndpointReference(faultToAddress, null, null, null,
				null));
	}

	public synchronized void setFaultTo(final EndpointReferenceType faultToEPR)
			throws JAXBException {
		if (getFaultTo() != null)
			throw new IllegalStateException(
					"Header wsa:FaultTo is already set.");
		final JAXBElement<EndpointReferenceType> faultToElement = FACTORY
				.createFaultTo(faultToEPR);
		addHeader(faultToElement);
		this.faultTo = faultToEPR;
	}

	public synchronized void addFaultToParameter(final QName name,
			final String value) {
		final Element param = createStringElement(name, value);

		addParamToEprHeader(WSAddressingConstants.FAULT_TO, param);
	}

	public synchronized void addFaultToParameter(final Element param) {
		addParamToEprHeader(WSAddressingConstants.FAULT_TO, param);
	}

	@SuppressWarnings("unchecked")
	public synchronized void addFaultToParameter(final JAXBElement param) {
		addParamToEprHeader(WSAddressingConstants.FAULT_TO, param);
	}

	public synchronized EndpointReferenceType getFrom() {
		if (from == null)
			from = getEprHeader(WSAddressingConstants.FROM);
		return from;
	}

	public synchronized void setFrom(final String fromAddress)
			throws JAXBException {
		setFaultTo(createEndpointReference(fromAddress, null, null, null, null));
	}

	public synchronized void setFrom(final EndpointReferenceType fromEPR)
			throws JAXBException {
		if (getFrom() != null)
			throw new IllegalStateException("Header wsa:From is already set.");
		final JAXBElement<EndpointReferenceType> fromElement = FACTORY
				.createFrom(fromEPR);
		addHeader(fromElement);
		this.from = fromEPR;
	}

	public synchronized void addFromParameter(final QName name,
			final String value) {
		final Element param = createStringElement(name, value);

		addParamToEprHeader(WSAddressingConstants.FROM, param);
	}

	public synchronized void addFromParameter(final Element param) {
		addParamToEprHeader(WSAddressingConstants.FROM, param);
	}

	@SuppressWarnings("unchecked")
	public synchronized void addFromParameter(final JAXBElement param) {
		addParamToEprHeader(WSAddressingConstants.FROM, param);
	}

	// only address is mandatory, the rest of the params are optional and can be
	// null
	public static EndpointReferenceType createEndpointReference(
			final String address, final ReferencePropertiesType props,
			final ReferenceParametersType params,
			final AttributedQName portType, final ServiceNameType serviceName) {

		if (address == null) {
			throw new IllegalArgumentException("Address can not be null.");
		}

		final EndpointReferenceType epr = FACTORY.createEndpointReferenceType();

		final AttributedURI addressURI = FACTORY.createAttributedURI();
		addressURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND,
				Boolean.TRUE.toString());
		addressURI.setValue(address.trim());
		epr.setAddress(addressURI);

		if (params != null) {
			epr.setReferenceParameters(params);
		}

		if (props != null) {
			epr.setReferenceProperties(props);
		}

		if (serviceName != null) {
			epr.setServiceName(serviceName);
		}

		if (portType != null) {
			epr.setPortType(portType);
		}

		return epr;
	}

	public SOAPResponse invoke() throws Exception {
		if (getReplyTo() == null)
			setReplyTo(WSAddressingRequest.createEndpointReference(
					WSAddressingConstants.ANONYMOUS_ENDPOINT_URI, null, null,
					null, null)); // Replying to creator
		if (getMessageId() == null)
			setMessageId(uuidScheme + UUID.randomUUID().toString());
		validateThis();
		validateElementPresent(getReplyTo(), WSAddressingConstants.REPLY_TO);
		return super.invoke();
	}

	public void invokeOneWay() throws Exception {
		if (getMessageId() == null)
			setMessageId(uuidScheme + UUID.randomUUID().toString());
		validateThis();
		super.invokeOneWay();
	}

	public void validate() throws FaultException {
		super.validate();
		validateThis();
	}

	// Protected methods follow

	protected void validateElementPresent(final Object element,
			final QName elementName) throws FaultException {
		if (element == null) {
			String text = "'" + elementName.getLocalPart()
					+ "' element missing.";
			String faultDetail = elementName
					+ " is required but was not found.";
			Object[] details = { "During validation the following ",
					"required element '" + elementName + "'",
					"could not be found. Unable to proceed with processing." };
			throw new MessageInformationHeaderRequiredFault(text, faultDetail,
					null, elementName, details);
		}
	}

	protected void validateURISyntax(final String uri) throws FaultException {
		try {
			new URI(uri);
		} catch (URISyntaxException syntax) {
			throw new InvalidMessageInformationHeaderFault(uri);
		}
	}

	// Private methods follow

	private static QName getServiceName(EndpointReferenceType epr) {
		final ServiceNameType service = epr.getServiceName();

		final QName serviceName;

		if (service == null) {
			serviceName = new QName(WSAddressingConstants.NS_URI, "Service",
					WSAddressingConstants.NS_PREFIX);
		} else {
			serviceName = service.getValue();
		}
		return serviceName;
	}

	private static QName getPortName(EndpointReferenceType epr) {
		final ServiceNameType service = epr.getServiceName();

		final QName portName;

		if (service == null) {
			portName = new QName(WSAddressingConstants.NS_URI, "Port",
					WSAddressingConstants.NS_PREFIX);
		} else {
			final QName serviceName = service.getValue();
			String port = service.getPortName();
			if ((port == null) || (port.trim().length() == 0))
				port = "Port";
			portName = new QName(serviceName.getNamespaceURI(), port,
					serviceName.getPrefix());
		}

		return portName;
	}

	private void setTo(final String address) throws JAXBException {
		final AttributedURI toURI = FACTORY.createAttributedURI();
		toURI.setValue(address.trim());
		toURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND,
				Boolean.TRUE.toString());
		final JAXBElement<AttributedURI> toElement = FACTORY.createTo(toURI);
		addHeader(toElement);
	}

	private void setTo(final EndpointReferenceType epr) throws JAXBException {
		// Set the different parts of the EPR
		setTo(epr.getAddress().getValue());

		final ReferenceParametersType refParams = epr.getReferenceParameters();
		if (refParams != null) {
			final List<Object> refs = refParams.getAny();
			addHeaders(refs);
		}

		final ReferencePropertiesType properties = epr.getReferenceProperties();
		if (properties != null) {
			final List<Object> props = refParams.getAny();
			addHeaders(props);
		}

		final List<Object> any = epr.getAny();
		if (any != null) {

			addHeaders(any);
		}
	}

	private void setAction(final String action) throws JAXBException {
		final AttributedURI actionURI = FACTORY.createAttributedURI();
		actionURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND,
				Boolean.TRUE.toString());
		actionURI.setValue(action.trim());
		final JAXBElement<AttributedURI> actionElement = FACTORY
				.createAction(actionURI);
		addHeader(actionElement);
	}

	private void addParamToEprHeader(final QName name, final Object param) {

		final EndpointReferenceType epr = getEprHeader(name);

		if (epr == null)
			throw new IllegalStateException("Header " + name.getPrefix() + ":"
					+ name.getLocalPart() + " is not set.");

		ReferenceParametersType params = epr.getReferenceParameters();
		if (params == null) {
			params = FACTORY.createReferenceParametersType();
			epr.setReferenceParameters(params);
		}
		params.getAny().add(param);
	}

	private EndpointReferenceType getEprHeader(final QName name) {

		final Object header = getHeader(name);
		if (header == null)
			return null;

		if ((header instanceof JAXBElement)
				&& (((JAXBElement) header).getValue() instanceof EndpointReferenceType))
			return (EndpointReferenceType) ((JAXBElement) header).getValue();
		else {
			throw new IllegalStateException(
					"Header "
							+ name.getPrefix()
							+ ":"
							+ name.getLocalPart()
							+ " is of type "
							+ header.getClass().getCanonicalName()
							+ ". This method only supports type JAXBElement<EndpointReferenceType> for this header.");
		}
	}

	private String getAttributedURIHeader(final QName name) {

		final Object header = getHeader(name);
		if (header == null)
			return null;

		if ((header instanceof JAXBElement)
				&& (((JAXBElement) header).getValue() instanceof AttributedURI))
			return ((AttributedURI) ((JAXBElement) header).getValue())
					.getValue();
		else {
			throw new IllegalStateException(
					"Header "
							+ name.getPrefix()
							+ ":"
							+ name.getLocalPart()
							+ " is of type "
							+ header.getClass().getCanonicalName()
							+ ". This method only supports type JAXBElement<AttributedURI> for this header.");
		}
	}

	private void validateThis() throws FaultException {
		validateElementPresent(getAction(), WSAddressingConstants.ACTION);
		validateElementPresent(getTo(), WSAddressingConstants.TO);
		validateElementPresent(getMessageId(), WSAddressingConstants.MESSAGE_ID);
		final String replyToAddress = getReplyTo().getAddress().getValue();
		validateElementPresent(replyToAddress, WSAddressingConstants.ADDRESS);

		validateURISyntax(getAction());
		validateURISyntax(getTo());
		validateURISyntax(getMessageId());
		validateURISyntax(replyToAddress);
	}
}
