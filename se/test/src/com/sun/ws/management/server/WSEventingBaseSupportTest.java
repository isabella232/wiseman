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
package com.sun.ws.management.server;

import java.util.ArrayList;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.EventType;
import org.dmtf.schemas.wbem.wsman._1.wsman.EventsType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import util.TestBase;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.server.EventingContextBatched;
import com.sun.ws.management.server.EventingContextWithAck;
import com.sun.ws.management.server.WSEventingBaseSupport;

import foo.test.Foo;

public class WSEventingBaseSupportTest extends TestBase {
	protected static final String NS_URI = "http://schemas.InStech.com/model";
	protected static final String NS_PREFIX = "model";
	public static final String EVENTS = "Events";

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

			Foo object = new Foo();
			object.setBar("object foo ..." );

			final Addressing addr = WSEventingBaseSupport
					.createEventMessagePushWithAck(ctx, object);
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
	public void testCreateEventMessageBatched()throws SOAPException, DatatypeConfigurationException{
		
		String ACTION = "http://schemas.xmlsoap.org/2005/02/diskspacechange";
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
		int sizeOfEvent = 3;
		int maxElements = 5;
		Duration maxTime = DatatypeFactory.newInstance().newDuration(50* 1000);
		EventingContextBatched ctx = new EventingContextBatched(null, null,
					notifyTo, null, eventReplyTo,DatatypeFactory.newInstance().newDuration(5* 1000),maxElements,maxTime);
		
		EventsType events = Management.FACTORY.createEventsType();		
		ArrayList<EventType> event = new ArrayList<EventType>(maxElements);
		for(int i=0;i<maxElements;i++)
		{
			EventType evt = new EventType();
			evt.setAction(ACTION);
			for(int j = 0;j<sizeOfEvent;j++){
				Foo object = new Foo();
				object.setBar("object foo "+i);
				evt.getAny().add(object);
			}
			event.add(evt);
			events.getEvent().add(evt);
		}
		
		
		try{
			
			Addressing addr = WSEventingBaseSupport.createEventMessageBatched(ctx,events);
			NodeList eventsList = addr.getBody().getElementsByTagName(Management.NS_PREFIX + ":"+
		    		 EVENTS).item(0).getChildNodes();
			final Management mgmt = new Management(addr);
			final EventingExtensions evt = new EventingExtensions(mgmt);
			assertEquals(event.size(),eventsList.getLength());
			//System.out.println(eventsList.item(0).getAttributes().item(0).getTextContent());
		    for(int i=0;i<event.size();i++){
		    	assertEquals(eventsList.item(i).getChildNodes().getLength(),event.get(i).getAny().size());
		    	assertEquals(eventsList.item(i).getAttributes().item(0).getTextContent(),ACTION);
		    	for(int j=0;j<sizeOfEvent;j++){
		    		assertEquals(eventsList.item(i).getChildNodes().item(j).getTextContent(),((Foo)event.get(i).getAny().get(j)).getBar());
		    	}
		    }
		    
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
			// System.out.println(addr);
			addr.prettyPrint(logfile);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
