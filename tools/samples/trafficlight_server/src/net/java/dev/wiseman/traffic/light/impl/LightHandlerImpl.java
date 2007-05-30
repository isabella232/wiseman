/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: LightHandlerImpl.java,v 1.3 2007-05-30 20:30:18 nbeers Exp $
 */
package net.java.dev.wiseman.traffic.light.impl;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.publicworks.light.model.TrafficLightModel;
import org.publicworks.light.model.ui.TrafficLight;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import net.java.dev.wiseman.schemas.traffic._1.light.ObjectFactory;
import net.java.dev.wiseman.schemas.traffic._1.light.TrafficLightType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.framework.Utilities;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.TransferExtensions;

public class LightHandlerImpl {

	private static final ObjectFactory trafficLightFactory = new ObjectFactory();

	private static final QName QNAME = new QName(
			"http://schemas.wiseman.dev.java.net/traffic/1/light.xsd", "trafficlight");

	public static void get(Management request, Management response, Logger log) {

		try {
			// Use name selector to find the right light in the model
			String name = getNameSelector(request);
			TrafficLight light = TrafficLightModel.getModel().find(name);
			if (light == null) {
				log
						.info("An attempt was made to get a resource that did not exist called "
								+ name);
				throw new InvalidSelectorsFault(
						InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
			}

			TransferExtensions xferRequest = new TransferExtensions(request);
			TransferExtensions xferResponse = new TransferExtensions(response);

			xferResponse.setFragmentGetResponse(
					xferRequest.getFragmentHeader(), createLight(light));
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		}
	}

	public static void put(Management request, Management response, Logger log) {

		// Use name selector to find the right light
		String name = getNameSelector(request);
		TrafficLight light = TrafficLightModel.getModel().find(name);
		
		if (light == null) {
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}

		// Get the resource passed in the body
		Object obj = getResource(request);
		if ((obj instanceof JAXBElement) == false) {
			throw new InternalErrorFault("Wrong resource type \n");
		}

		JAXBElement elem = (JAXBElement) obj;
		JAXBElement<TrafficLightType> tlElement = (JAXBElement<TrafficLightType>) obj;
		TrafficLightType tlType = tlElement.getValue();

		// Transfer values
		light.setName(tlType.getName());
		light.setColor(tlType.getColor());
		light.setX(tlType.getX());
		light.setY(tlType.getY());

		try {
			TransferExtensions xferResponse = new TransferExtensions(response);

			xferResponse.setPutResponse();
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		}
	}

	public static void delete(Management request, Management response,
			Logger log) {
		// Use name selector to find the right light
		String name = getNameSelector(request);
		TrafficLight light = TrafficLightModel.getModel().find(name);
		if (light == null) {
			log.log(Level.WARNING,
					"An attempt was made to delete a resource that did not exist called "
							+ name);
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}

		// Remove it from the list and then remove the actual GUI instance.
		TrafficLightModel.getModel().destroy(name);
		try {
			TransferExtensions xferResponse = new TransferExtensions(response);

			xferResponse.setDeleteResponse();
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		}
	}

	public static void create(Management request, Management response,
			Logger log) {

		try {
			// Get the resource passed in the body
			TransferExtensions xferRequest = new TransferExtensions(request);
			TransferExtensions xferResponse = new TransferExtensions(response);
			Object element = xferRequest.getResource(QNAME);

			TrafficLight light;

			if (element != null) {
				JAXBElement<TrafficLightType> tlElement = getResource(request);
				TrafficLightType tlType = tlElement.getValue();

				// Create and store a reference to this object in our mini-model
				light = TrafficLightModel.getModel().create(tlType.getName());

				// Transfer values
				light.setColor(tlType.getColor());
				light.setX(tlType.getX());
				light.setY(tlType.getY());
			} else {
				// Create and store a reference to this object in our mini-model
				light = TrafficLightModel.getModel().create(null);

				log
						.log(Level.INFO,
								"The body of your request is empty but it is optional.");
			}

			// Define a selector (in this case name)
			HashMap<String, String> selectors = new HashMap<String, String>();
			selectors.put("name", light.getName());

			EndpointReferenceType epr = xferResponse.createEndpointReference(
					request.getTo(), request.getResourceURI(), selectors);
			xferResponse.setCreateResponse(epr);
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		}
	}

	private static String getNameSelector(Management request)
			throws InternalErrorFault {
		Set<SelectorType> selectors;
		try {
			selectors = request.getSelectors();
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		}
		if (Utilities.getSelectorByName("name", selectors) == null) {
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}
		return (String) Utilities.getSelectorByName("name", selectors)
				.getContent().get(0);
	}

	private static JAXBElement<TrafficLightType> getResource(Management request) {
		JAXBElement<TrafficLightType> tlElement;

		try {
			// Get JAXB Representation of Soap Body property document
			TransferExtensions transfer = new TransferExtensions(request);

			Object element = transfer.getResource(QNAME);
			
			if (element == null) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.MISSING_VALUES);
			}

			if (element instanceof JAXBElement) {
				if (((JAXBElement) element).getDeclaredType().equals(
						TrafficLightType.class)) {
					tlElement = (JAXBElement<TrafficLightType>) element;
				} else {
					// XmlFragment only supported on Get
					throw new UnsupportedFeatureFault(
							UnsupportedFeatureFault.Detail.FRAGMENT_LEVEL_ACCESS);
				}
			} else {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		}
		return tlElement;
	}

	private static TrafficLightType createLightType(TrafficLight light) {
		// Create a new, empty JAXB TrafficLight Type
		TrafficLightType tlType = trafficLightFactory.createTrafficLightType();

		// Transfer State from model to JAXB Type
		tlType.setName(light.getName());
		tlType.setColor(light.getColor());
		tlType.setX(light.getX());
		tlType.setY(light.getY());
		return tlType;
	}

	public static JAXBElement<TrafficLightType> createLight(TrafficLight light)
			throws JAXBException {
		TrafficLightType lightType = createLightType(light);
		return trafficLightFactory.createTrafficlight(lightType);
	}
}
