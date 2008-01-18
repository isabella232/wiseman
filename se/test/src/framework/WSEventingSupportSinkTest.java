/*
 * Copyright 2008 Sun Microsystems, Inc.
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
 ** Copyright (C) 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 ***
 *** Fudan University
 *** Author: Chuan Xiao (cxiao@fudan.edu.cn)
 */
package framework;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;
import util.TestBase;

import com.sun.ws.management.Management;
import com.sun.ws.management.DeliveryRefusedFault;
import com.sun.ws.management.server.sink.WSEventingSupportSink;

import com.sun.ws.management.addressing.Addressing;

import com.sun.ws.management.soap.SOAP;

public class WSEventingSupportSinkTest extends TestBase {
	public WSEventingSupportSinkTest(final String testName) {
		super(testName);
	}

	public void testcreateDeliveryRefusedFaultMessage() {
		try {
			final Addressing request = new Addressing();
			final String uuid = UUID_SCHEME + UUID.randomUUID().toString();
			request.setMessageId(uuid);
			String Faultreason = "DeliveryRefused";

			final Addressing response = WSEventingSupportSink
					.createDeliveryRefusedFaultMessage(request, Faultreason);
			// System.out.println(response);
			response.prettyPrint(logfile);
			
			assertEquals(Management.FAULT_ACTION_URI, response.getAction()); // test
																				// action
			assertNotNull(response.getMessageId()); // test messageID
			assertEquals(request.getMessageId(), response.getRelatesTo()[0]
					.getValue()); // test relatesto
			assertEquals(SOAP.RECEIVER, response.getBody().getFault()
					.getFaultCodeAsQName()); // test faultcode
			assertEquals(DeliveryRefusedFault.DELIVERY_REFUSED, response
					.getFault().getCode().getSubcode().getValue()); // test
																	// subcode
			assertEquals(Faultreason, response.getFault().getReason().getText()
					.get(0).getValue()); // test reason
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testcreateEventACKAcknowledgement() throws Exception {
		// Construct the request message
		final Addressing request = new Addressing();
		request.setAction(Management.PUSH_WITH_ACK_URI);
		request.setTo(DESTINATION);
		request.setFrom(Addressing.ANONYMOUS_ENDPOINT_URI);
		request.setFaultTo(Addressing.ANONYMOUS_ENDPOINT_URI);
		final String uuid = UUID_SCHEME + UUID.randomUUID().toString();
		request.setMessageId(uuid);

		request.getEnvelope().addNamespaceDeclaration(SOAP.NS_PREFIX,
				SOAP.NS_URI);

		final ReferencePropertiesType propsFrom = Addressing.FACTORY
				.createReferencePropertiesType();
		final Document tempDoc = request.newDocument();
		final Element temperature = tempDoc.createElementNS(SOAP.NS_URI,
				SOAP.NS_PREFIX + ":" + "temperature");
		temperature.appendChild(tempDoc.createTextNode("75"));
		tempDoc.appendChild(temperature);
		propsFrom.getAny().add(tempDoc.getDocumentElement());

		final ReferenceParametersType paramsFrom = Addressing.FACTORY
				.createReferenceParametersType();
		final Document unitsDoc = request.newDocument();
		final Element units = unitsDoc.createElementNS(SOAP.NS_URI,
				SOAP.NS_PREFIX + ":" + "units");
		units.setAttributeNS(SOAP.NS_URI, SOAP.NS_PREFIX + ":" + "type",
				"celsius");
		unitsDoc.appendChild(units);
		paramsFrom.getAny().add(unitsDoc.getDocumentElement());

		final AttributedQName portTypeFrom = Addressing.FACTORY
				.createAttributedQName();
		final QName portType = new QName(SOAP.NS_URI, "thePortType",
				SOAP.NS_PREFIX);
		portTypeFrom.setValue(portType);

		final ServiceNameType serviceNameFrom = Addressing.FACTORY
				.createServiceNameType();
		final String portName = "thePortName";
		serviceNameFrom.setPortName(portName);
		final QName serviceName = new QName(SOAP.NS_URI, "theServiceName",
				SOAP.NS_PREFIX);
		serviceNameFrom.setValue(serviceName);

		final String fromAddr = "https://client:8080/wsman/receiver";
		final EndpointReferenceType eprFrom = Addressing
				.createEndpointReference(fromAddr, propsFrom, paramsFrom,
						portTypeFrom, serviceNameFrom);
		request.setReplyTo(eprFrom);

		request.addHeaders(paramsFrom);

		final Addressing response = WSEventingSupportSink
				.createEventACKAcknowledgement(request);
		// System.out.println(response);
		response.prettyPrint(logfile);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		response.writeTo(bos);

		assertEquals(response.getAction(),
				"http://schemas.dmtf.org/wbem/wsman/1/wsman/Ack");
		assertEquals(response.getTo(), request.getReplyTo().getAddress()
				.getValue());
		assertEquals(response.getRelatesTo()[0].getValue(), request
				.getMessageId());

		// test whether ref params are added to the message header
		boolean foundRefParam = false;
		for (SOAPElement hdr : response.getHeaders()) {
			if (units.getNodeName().equals(hdr.getNodeName())
					&& units.getNamespaceURI().equals(hdr.getNamespaceURI())) {
				foundRefParam = true;
				break;
			}
		}
		assertTrue(foundRefParam);
	}
}