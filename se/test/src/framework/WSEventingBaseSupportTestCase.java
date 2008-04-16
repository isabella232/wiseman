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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableDuration;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributablePositiveInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.EventType;
import org.dmtf.schemas.wbem.wsman._1.wsman.EventsType;
import org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;

import util.TestBase;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingUtility;
import com.sun.ws.management.eventing.InvalidSubscriptionException;
import com.sun.ws.management.server.EventingSupport;
import com.sun.ws.management.server.WSEventingBaseSupport;

import foo.test.Foo;

public class WSEventingBaseSupportTestCase extends TestBase {
	protected static final String NS_URI = "http://schemas.InStech.com/model";
	protected static final String NS_PREFIX = "model";
	public static final String EVENTS = "Events";

	private static final ObjectFactory FACTORY = new ObjectFactory();

	public WSEventingBaseSupportTestCase(final String testName) {
		super(testName);
	}

	public void testCreateEventMessagePushWithAck() throws SOAPException,
			JAXBException, DatatypeConfigurationException,
			InvalidSubscriptionException, IOException,
			ParserConfigurationException, SAXException {

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

		final String recvrAddress = "http://localhost:8080/events";
		final EndpointReferenceType notifyTo = Addressing
				.createEndpointReference(recvrAddress, props, params, portType,
						serviceName);

		final EventingMessageValues settings = new EventingMessageValues();
		settings.setDeliveryMode(EventingExtensions.EVENTS_DELIVERY_MODE);
		settings.setTo("http://localhost:8081");
		settings.setNotifyTo(notifyTo);
		settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
		settings.setResourceUri("http://wiseman.dev.java.net/test/resource");

		final Eventing subReq = EventingUtility.buildMessage(null, settings);
		final Eventing subRes = EventingUtility.buildMessage(null, settings);

		final DeliveryType delivery = subReq.getSubscribe().getDelivery();

		// Set MaxElements in the Delivery element
		final AttributablePositiveInteger maxElements = new AttributablePositiveInteger();
		maxElements.setValue(BigInteger.valueOf(5));
		final JAXBElement<AttributablePositiveInteger> jaxbMaxElements = FACTORY
				.createMaxElements(maxElements);
		delivery.getContent().add(jaxbMaxElements);

		// Set MaxTime in the Delivery element
		final AttributableDuration maxTime = new AttributableDuration();
		maxTime.setValue(DatatypeFactory.newInstance().newDuration(50 * 1000));
		final JAXBElement<AttributableDuration> jaxbMaxTime = FACTORY
				.createMaxTime(maxTime);
		delivery.getContent().add(jaxbMaxTime);

		final UUID uuid = EventingSupport.subscribe(null, subReq, subRes, null);

		// Set an OperationTimeout of 60 seconds
		final Duration opTimeout = DatatypeFactory.newInstance().newDuration(
				6 * 10000);
		EventingSupport.setSendOperationTimeout(uuid, opTimeout);

		Foo object = new Foo();
		object.setBar("object foo ...");

		final Addressing addr = WSEventingBaseSupport
				.createEventMessagePushWithAck(uuid, object);
		// System.out.println(addr);
		addr.prettyPrint(logfile);

		final Management mgmt = new Management(addr);
		final EventingExtensions evt = new EventingExtensions(mgmt);

		// Check the To address in the message
		assertEquals(notifyTo.getAddress().getValue(), addr.getTo());

		// Check that the NotifyTo properties are in the headers of the
		// message
		Iterator<Object> temp = addr.getHeader().getChildElements(
				new QName(NS_URI, "tempelement"));
		assertTrue(temp != null);
		assertTrue(temp.hasNext());
		temp.next();
		assertFalse(temp.hasNext());

		// Check that the NotifyTo parameters are in the headers of the
		// message
		temp = addr.getHeader().getChildElements(
				new QName(NS_URI, "refParameters"));
		assertTrue(temp != null);
		assertTrue(temp.hasNext());
		Element refParam = (Element) temp.next();
		Attr attribute = refParam.getAttributeNodeNS(NS_URI, "type");
		assertTrue(attribute != null);
		assertEquals(attribute.getTextContent(), "celsius");
		assertFalse(temp.hasNext());

		// Check if AckRequested was set
		assertTrue("AckRequested not set in response", evt.isAckRequested());

		// Check the OperationTimeout header value
		assertEquals(opTimeout.toString(), mgmt.getTimeout().toString());
	}

	public void testCreateEventMessageBatched() throws SOAPException,
			DatatypeConfigurationException, JAXBException,
			InvalidSubscriptionException, ParserConfigurationException,
			SAXException, IOException {

		final String ACTION = "http://schemas.xmlsoap.org/2005/02/diskspacechange";

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

		final String recvrAddress = "http://localhost:8080/events";
		final EndpointReferenceType notifyTo = Addressing
				.createEndpointReference(recvrAddress, props, params, portType,
						serviceName);

		int sizeOfEvent = 3;

		final EventingMessageValues settings = new EventingMessageValues();
		settings.setDeliveryMode(EventingExtensions.EVENTS_DELIVERY_MODE);
		settings.setTo("http://localhost:8081");
		settings.setNotifyTo(notifyTo);
		settings.setEventingMessageActionType(Eventing.SUBSCRIBE_ACTION_URI);
		settings.setResourceUri("http://wiseman.dev.java.net/test/resource");

		final Eventing subReq = EventingUtility.buildMessage(null, settings);
		final Eventing subRes = EventingUtility.buildMessage(null, settings);

		final DeliveryType delivery = subReq.getSubscribe().getDelivery();

		// Set MaxElements in the Delivery element
		final AttributablePositiveInteger maxElements = new AttributablePositiveInteger();
		maxElements.setValue(BigInteger.valueOf(5));
		final JAXBElement<AttributablePositiveInteger> jaxbMaxElements = FACTORY
				.createMaxElements(maxElements);
		delivery.getContent().add(jaxbMaxElements);

		// Set MaxTime in the Delivery element
		final AttributableDuration maxTime = new AttributableDuration();
		maxTime.setValue(DatatypeFactory.newInstance().newDuration(50 * 1000));
		final JAXBElement<AttributableDuration> jaxbMaxTime = FACTORY
				.createMaxTime(maxTime);
		delivery.getContent().add(jaxbMaxTime);

		final UUID uuid = EventingSupport.subscribe(null, subReq, subRes, null);

		// Set an OperationTimeout of 60 seconds
		final Duration opTimeout = DatatypeFactory.newInstance().newDuration(
				6 * 10000);
		EventingSupport.setSendOperationTimeout(uuid, opTimeout);

		EventsType events = Management.FACTORY.createEventsType();
		ArrayList<EventType> event = new ArrayList<EventType>(maxElements
				.getValue().intValue());
		for (int i = 0; i < maxElements.getValue().intValue(); i++) {
			EventType evt = new EventType();
			evt.setAction(ACTION);
			for (int j = 0; j < sizeOfEvent; j++) {
				Foo object = new Foo();
				object.setBar("object foo " + i);
				evt.getAny().add(object);
			}
			event.add(evt);
			events.getEvent().add(evt);
		}

		final Addressing addr = WSEventingBaseSupport
				.createEventMessageBatched(uuid, events);
		// System.out.println(addr);
		addr.prettyPrint(logfile);

		NodeList eventsList = addr.getBody().getElementsByTagName(
				Management.NS_PREFIX + ":" + EVENTS).item(0).getChildNodes();
		final Management mgmt = new Management(addr);
		final EventingExtensions evt = new EventingExtensions(mgmt);
		assertEquals(event.size(), eventsList.getLength());
		// System.out.println(eventsList.item(0).getAttributes().item(0).getTextContent());
		for (int i = 0; i < event.size(); i++) {
			assertEquals(eventsList.item(i).getChildNodes().getLength(), event
					.get(i).getAny().size());
			assertEquals(eventsList.item(i).getAttributes().item(0)
					.getTextContent(), ACTION);
			for (int j = 0; j < sizeOfEvent; j++) {
				assertEquals(eventsList.item(i).getChildNodes().item(j)
						.getTextContent(), ((Foo) event.get(i).getAny().get(j))
						.getBar());
			}
		}

		// Check the To address in the message
		assertEquals(notifyTo.getAddress().getValue(), addr.getTo());

		// Check that the NotifyTo properties are in the headers of the message
		Iterator<Object> temp = addr.getHeader().getChildElements(
				new QName(NS_URI, "tempelement"));
		assertTrue(temp != null);
		assertTrue(temp.hasNext());
		temp.next();
		assertFalse(temp.hasNext());

		// Check that the NotifyTo parameters are in the headers of the message
		temp = addr.getHeader().getChildElements(
				new QName(NS_URI, "refParameters"));
		assertTrue(temp != null);
		assertTrue(temp.hasNext());
		Element refParam = (Element) temp.next();
		Attr attribute = refParam.getAttributeNodeNS(NS_URI, "type");
		assertTrue(attribute != null);
		assertEquals(attribute.getTextContent(), "celsius");
		assertFalse(temp.hasNext());

		// Check the OperationTimeout header value
		assertEquals(opTimeout.toString(), mgmt.getTimeout().toString());

		// Check if AckRequested was set
		assertTrue("AckRequested not set in response", evt.isAckRequested());

	}

}
