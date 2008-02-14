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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableEmpty;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributablePositiveInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;

import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.client.message.wsman.WSManRequest;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

public class WSManEnumerateRequest extends WSManEnumerationRequest {
	
	public static final String ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate";
    
	private final EndpointReferenceType epr;
	private int maxElements = -1;	

	WSManEnumerateRequest() {
		super(null);
		this.epr = null;
	}
	
	public WSManEnumerateRequest(final EndpointReferenceType epr,
			final Map<String, ?> context, final XmlBinding binding)
	throws Exception {
		super(epr, context, binding);
		setAction(ACTION_URI);
		this.epr = epr;
	}

	// TODO: Remove this constructor.
	public WSManEnumerateRequest(final String endpoint,
			final Map<String, ?> context, final QName serviceName,
			final QName portName, final XmlBinding binding) throws Exception {
		super(endpoint, context, serviceName, portName, binding);
		setAction(ACTION_URI);
		this.epr = null;
	}

	// TODO: Remove this constructor.
	public WSManEnumerateRequest(final SOAPRequest request) throws JAXBException {
		super(request);
		setAction(ACTION_URI);
		this.epr = null;
	}
	
	public Enumerate createEnumerate(final EndpointReferenceType endTo,
			 final String expires,
			 final Object filter,
			 final boolean optimize,
			 final int maxElements,
			 final EnumerationModeType mode,
			 final Object... anys) {
		
		// Save maxElements for use with pull
		this.maxElements = maxElements;
		
        // Create the Enumeration element
        final Enumerate enumerate = WSManEnumerationRequest.FACTORY.createEnumerate();
        if (endTo != null) {
            enumerate.setEndTo(endTo);
        }
        if (expires != null) {
        	final String exp = expires.trim();
        	if (exp.length() > 0)
                enumerate.setExpires(exp);
        }
        
        final List<Object> list = enumerate.getAny();
        
        // Add the wsman elements
        if (filter != null) {
            list.add(filter);
        }
        if (mode != null) {
            list.add(WSManRequest.FACTORY.createEnumerationMode(mode));
        }
        if (optimize == true) {
            list.add(WSManRequest.FACTORY.createOptimizeEnumeration(new AttributableEmpty()));
            
            if (maxElements > 0) {
                final AttributablePositiveInteger posInt = new AttributablePositiveInteger();
                posInt.setValue(new BigInteger(Integer.toString(maxElements)));
                list.add(WSManRequest.FACTORY.createMaxElements(posInt));
            }
        }
        
        // Any the anys elements
        if (anys != null) {
			for (int i = 0; i < anys.length; i++) {
				list.add(anys[i]);
			}
		}
        
        return enumerate;
	}

	public Object createFilter(final String xpath,
			                   final Map<String, String> namespaces)
		throws SOAPException, JAXBException {
		final List<Object> expression = new ArrayList<Object>();
		expression.add(xpath);
		return createFilter(XPath.NS_URI, expression, namespaces);
	}

	public Object createFilter(final String dialect,
			              final List<Object> expression,
			              final Map<String, String> namespaces)
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

		final JAXBElement<DialectableMixedDataType> filter = WSManRequest.FACTORY
				.createFilter(dialectableMixedDataType);

		// We need a special treatment for this header in order
		// to add the specified namespace declarations to the
		// wsman:FragmentTransfer header
		if (namespaces != null) {
			// TODO: This is J2EE dependent not J2SE! Change to use J2SE API.
			final SOAPMessage msg = MessageFactory.newInstance(
					SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
			final SOAPElement body = msg.getSOAPBody();
			getXmlBinding().getJAXBContext().createMarshaller().marshal(
					filter, body);
			final Iterator<SOAPElement> iter = body.getChildElements();
			final SOAPElement element = iter.next();
			for (Map.Entry<String, String> decl : namespaces.entrySet()) {
				element.addNamespaceDeclaration(decl.getKey(), decl.getValue());
			}
			return element;
		} else
			return filter;
	}
	
	
	public void setEnumerate(final String expires,
			                 final Object filter,
			                 final boolean optimize,
			                 final int maxElements,
			                 final EnumerationModeType mode) {
		// Save maxElements for use with pull
		this.maxElements = maxElements;
		
		final Enumerate enumerate = createEnumerate(null, expires, filter, optimize,
				maxElements, mode, (Object[])null);
		setEnumerate(enumerate);
	}
	
	public void setEnumerate(final Enumerate enumerate) {
		// TODO: Save maxElements for use with pull
		this.maxElements = -1;
		
		setPayload(enumerate);
	}
	
	public void requestTotalItemsCountEstimate() throws JAXBException {
        final AttributableEmpty empty = new AttributableEmpty();
        final JAXBElement<AttributableEmpty> emptyElement =
        	WSManRequest.FACTORY.createRequestTotalItemsCountEstimate(empty);
        addHeader(emptyElement);
	}
	
	public SOAPResponse invoke() throws Exception {
		// TODO: Message sanity checks go here.
		return new WSManEnumerateResponse(super.invoke(),
				                          this.epr,
				                          this.getRequestContext(),
				                          this.getXmlBinding(),
				                          this.maxElements);
	}
}