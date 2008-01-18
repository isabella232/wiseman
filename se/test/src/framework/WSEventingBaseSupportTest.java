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

import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import util.TestBase;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.server.EventingContextWithAck;
import com.sun.ws.management.server.WSEventingBaseSupport;

public class WSEventingBaseSupportTest extends TestBase {
	protected static final String NS_URI = "http://schemas.InStech.com/model";
	protected static final String NS_PREFIX = "model";

	public WSEventingBaseSupportTest(final String testName) {
		super(testName);
	}

	public void testCreateEventMessagePushWithAck() throws SOAPException {

		final String recvrAddress = "http://localhost:8080/events";
		final EndpointReferenceType notifyTo = Addressing
				.createEndpointReference(recvrAddress, null, null, null, null);

		final Addressing request = new Addressing();

		final ReferencePropertiesType props = Addressing.FACTORY
				.createReferencePropertiesType();
		final Document tempDoc1 = request.newDocument();
		final Element tempelement = tempDoc1.createElementNS(NS_URI, NS_PREFIX
				+ ":" + "tempelement");
		tempelement.appendChild(tempDoc1.createTextNode("tempelement"));
		tempDoc1.appendChild(tempelement);
		props.getAny().add(tempDoc1.getDocumentElement());

		final ReferenceParametersType params = Addressing.FACTORY
				.createReferenceParametersType();
		final Document refParametersDoc = request.newDocument();
		final Element refParameters = refParametersDoc.createElementNS(NS_URI,
				NS_PREFIX + ":" + "refParameters");
		refParameters.setAttributeNS(NS_URI, NS_PREFIX + ":" + "type",
				"celsius");
		refParametersDoc.appendChild(refParameters);
		params.getAny().add(refParametersDoc.getDocumentElement());

		final AttributedQName portType = Addressing.FACTORY
				.createAttributedQName();
		final QName port = new QName(NS_URI, "thePortType", NS_PREFIX);
		portType.setValue(port);

		final ServiceNameType serviceName = Addressing.FACTORY
				.createServiceNameType();
		final String portName = "thePortName";
		serviceName.setPortName(portName);
		final QName service = new QName(NS_URI, "theServiceName", NS_PREFIX);
		serviceName.setValue(service);

		EndpointReferenceType eventReplyTo = Addressing
				.createEndpointReference(Addressing.NS_URI, props, params,
						portType, serviceName);

		try {
			EventingContextWithAck ctx = new EventingContextWithAck(null, null,
					notifyTo, null, eventReplyTo, DatatypeFactory.newInstance()
							.newDuration(5 * 1000));

			final Document tempDoc = Management.newDocument();
			final Element temp = tempDoc.createElementNS(NS_URI, NS_PREFIX
					+ ":" + "temp");
			temp.appendChild(tempDoc.createTextNode("temp"));
			tempDoc.appendChild(temp);
			final Addressing addr = WSEventingBaseSupport
					.createEventMessagePushWithAck(ctx, temp);
			final Management mgmt = new Management(addr);
			final EventingExtensions evt = new EventingExtensions(mgmt);

			assertEquals(eventReplyTo.getAddress().getValue(), addr
					.getReplyTo().getAddress().getValue());
			assertEquals(eventReplyTo.getReferenceProperties().getAny()
					.toString(), addr.getReplyTo().getReferenceProperties()
					.getAny().toString());
			assertEquals(eventReplyTo.getReferenceParameters().getAny()
					.toString(), addr.getReplyTo().getReferenceParameters()
					.getAny().toString());
			assertEquals(eventReplyTo.getPortType().getValue(), addr
					.getReplyTo().getPortType().getValue());
			assertEquals(eventReplyTo.getPortType().getOtherAttributes(), addr
					.getReplyTo().getPortType().getOtherAttributes());
			assertEquals(eventReplyTo.getServiceName().getValue(), addr
					.getReplyTo().getServiceName().getValue());
			assertEquals(eventReplyTo.getServiceName().getPortName(), addr
					.getReplyTo().getServiceName().getPortName());
			assertEquals(eventReplyTo.getServiceName().getOtherAttributes(),
					addr.getReplyTo().getServiceName().getOtherAttributes());

			assertEquals(DatatypeFactory.newInstance().newDuration(5 * 1000)
					.toString(), mgmt.getTimeout().toString());
			
			
			assertTrue("AckRequested not set in response", evt.isAckRequested());

			// System.out.println(evt);
			evt.prettyPrint(logfile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
