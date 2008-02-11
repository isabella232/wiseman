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

package com.sun.ws.management.client.message.wsman;

import java.math.BigInteger;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableDuration;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.Locale;
import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionSet;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.client.message.addressing.WSAddressingRequest;
import com.sun.ws.management.client.message.addressing.WSAddressingResponse;
import com.sun.ws.management.xml.XmlBinding;

/**
 * 
 * Abstraction of a WS-Addressing request
 */
public class WSManRequest extends WSAddressingRequest {

	public static final ObjectFactory FACTORY = new ObjectFactory();

	WSManRequest() {
		super(null);
	}

	public WSManRequest(final SOAPRequest request) {
		super(request);
	}

	public WSManRequest(final EndpointReferenceType epr,
			final Map<String, ?> context, final XmlBinding binding)
	throws Exception  {
		super(epr, context, binding);
	}

	public WSManRequest(final String endpoint, final Map<String, ?> context,
			final QName serviceName, final QName portName,
			final XmlBinding binding) throws Exception  {
		super(endpoint, context, serviceName, portName, binding);
	}

	public void setResourceURI(final String resourceURI) throws JAXBException {
        final AttributableURI resType = FACTORY.createAttributableURI();
        resType.setValue(resourceURI);
        final JAXBElement<AttributableURI> resTypeElement =
        	FACTORY.createResourceURI(resType);
        addHeader(resTypeElement);
	}

	public void setOperationTimeout(final long milliseconds) throws JAXBException, DatatypeConfigurationException {
		final Duration duration = DatatypeFactory.newInstance().newDuration(milliseconds);
        setOperationTimeout(duration);
	}
	
	public void setOperationTimeout(final Duration timeout) throws JAXBException {
        final AttributableDuration durationType = FACTORY.createAttributableDuration();
        durationType.setValue(timeout);
        final JAXBElement<AttributableDuration> timeoutElement =
        	FACTORY.createOperationTimeout(durationType);
        addHeader(timeoutElement);
	}
	
    public void setSelectors(final SelectorSetType selectors) throws JAXBException, SOAPException {
        final JAXBElement<SelectorSetType> selectorSetElement = FACTORY.createSelectorSet(selectors);
        addHeader(selectorSetElement);
    }

	public void setMaxEnvelopeSize(final BigInteger maxSize) throws JAXBException {
		final MaxEnvelopeSizeType maxSizeType = FACTORY.createMaxEnvelopeSizeType();
		maxSizeType.setValue(maxSize);
        final JAXBElement<MaxEnvelopeSizeType> sizeElement =
        	FACTORY.createMaxEnvelopeSize(maxSizeType);
        addHeader(sizeElement);
	}

	public void setLocale(final String locale) throws JAXBException {
		final Locale localeType = FACTORY.createLocale();
		localeType.setLang(locale);
		addHeader(localeType);
	}

	public void setOptions(final OptionSet options) throws JAXBException {
		addHeader(options);
	}
	
	// TODO: Add a createEndpointReference() method...

	public SOAPResponse invoke() throws Exception {
		// TODO: Message sanity checks go here.
		return new WSAddressingResponse(super.invoke());
	}
}
