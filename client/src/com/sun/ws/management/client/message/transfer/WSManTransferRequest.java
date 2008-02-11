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

package com.sun.ws.management.client.message.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.transfer.ObjectFactory;

import com.sun.ws.management.Management;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.client.message.wsman.WSManRequest;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

public class WSManTransferRequest extends WSManRequest {

	public static final ObjectFactory FACTORY = new ObjectFactory();

	WSManTransferRequest() {
		super(null);
	}

	public WSManTransferRequest(final EndpointReferenceType epr,
			final Map<String, ?> context, final XmlBinding binding)
	throws Exception  {
		super(epr, context, binding);
	}

	public WSManTransferRequest(final String endpoint,
			final Map<String, ?> context, final QName serviceName,
			final QName portName, final XmlBinding binding)
			throws Exception  {
		super(endpoint, context, serviceName, portName, binding);
	}

	public WSManTransferRequest(final SOAPRequest request) {
		super(request);
	}

	public void setFragmentHeader(final String xpath) throws SOAPException,
			JAXBException {
		setFragmentHeader(xpath, null);
	}

	public void setFragmentHeader(final String xpath,
			final Map<String, String> namespaces) throws SOAPException,
			JAXBException {
		final List<Object> expression = new ArrayList<Object>();
		expression.add(xpath);
		setFragmentHeader(XPath.NS_URI, expression, namespaces);
	}

	public void setFragmentHeader(final String dialect,
			final List<Object> expression, final Map<String, String> namespaces)
			throws SOAPException, JAXBException {
		if (expression == null)
			return;

		final DialectableMixedDataType dialectableMixedDataType = Management.FACTORY
				.createDialectableMixedDataType();
		if (dialect != null) {
			dialectableMixedDataType.setDialect(dialect);
		}

		dialectableMixedDataType.getOtherAttributes().put(SOAP.MUST_UNDERSTAND,
				Boolean.TRUE.toString());

		// add the query string to the content of the FragmentTransfer Header
		dialectableMixedDataType.getContent().add(expression);

		final JAXBElement<DialectableMixedDataType> fragmentTransfer = Management.FACTORY
				.createFragmentTransfer(dialectableMixedDataType);

		// We need a special treatment for this header in order
		// to add the specified namespace declarations to the
		// wsman:FragmentTransfer header
		if (namespaces != null) {
			// TODO: Make SOAP protocol configurable via "context"
			// TODO: This is J2EE dependent not J2SE! Change to use J2SE API.
			SOAPMessage msg = MessageFactory.newInstance(
					SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
			SOAPHeaderElement el = (SOAPHeaderElement) msg.getSOAPHeader()
					.addChildElement(WSManTransferResponse.FRAGMENT_TRANSFER);
			for (Map.Entry<String, String> decl : namespaces.entrySet()) {
				el.addNamespaceDeclaration(decl.getKey(), decl.getValue());
			}
			getXmlBinding().getJAXBContext().createMarshaller().marshal(
					fragmentTransfer.getValue(), el);
			addHeader(el);
		} else
			addHeader(fragmentTransfer);
	}
}
