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
 **Revision 1.4  2007/05/30 20:30:17  nbeers
 **Add HP copyright header
 **
 **
 *
 * $Id: eventcreator_Handler.java,v 1.5 2007-06-19 12:29:35 simeonpinder Exp $
 */
package com.sun.ws.management.server.handler.wsman.auth;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import management.MetadataTest;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
//import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingMessageValues;
import com.sun.ws.management.eventing.EventingUtility;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.eventing.EventingMessageValues.CreationTypes;
import com.sun.ws.management.framework.eventing.EventSourceInterface;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementEnumerationAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementOperationDefinitionAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementQNamedNodeWithValueAnnotation;
import com.sun.ws.management.mex.Metadata;
import com.sun.ws.management.mex.MetadataUtility;
import com.sun.ws.management.server.EventingSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.server.handler.wsman.eventsubman_Handler;
//import com.sun.ws.management.server.handler.wsman.eventsubman_Handler;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.xml.ws.addressing.model.ActionNotSupportedException;
//import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation.ANONYMOUS;

@WsManagementDefaultAddressingModelAnnotation(
	getDefaultAddressDefinition=
		@WsManagementAddressDetailsAnnotation(
			wsaTo=eventcreator_Handler.TO,
			wsmanResourceURI=eventcreator_Handler.RESOURCE_URI
	),
	 metaDataCategory = eventcreator_Handler.CATEGORY,
	 metaDataDescription = "This is a test event source",
	 resourceMetaDataUID = eventcreator_Handler.UID
)
public class eventcreator_Handler implements Handler, EventSourceInterface {

	public final static String TO =ManagementMessageValues.WSMAN_DESTINATION;
	public final static String RESOURCE_URI ="wsman:auth/eventcreator";
	public final static String CATEGORY =EventingMessageValues.SUBSCRIPTION_SOURCE;
	public final static String UID =
		"http://wiseman.dev.java.net/EventSource/eventcreator/uid-20000747652";

	//This should be NULL to start but is set upon successful initialization.
	private static String subscription_source_id =null;
	private static final Logger LOG = Logger.getLogger(eventcreator_Handler.class.getName());
	private static NamespaceMap nsMap;
	private static XmlBinding binding = null;
	static{
		if(binding==null){
		 try {
			Eventing events = new Eventing();
			binding = events.getXmlBinding();
		 } catch (SOAPException e) {
			e.printStackTrace();
		 }
		}
	}

	public eventcreator_Handler() throws SOAPException, JAXBException,
		DatatypeConfigurationException, IOException{
		//register this handler in the metadata directory
		//TODO: integrate call to the Transfer.Initialize mechanism.
	}

	private Management subscriptionManager;
	public void handle(String action, String resource, HandlerContext context,
			Management request, Management response) throws Exception {
		if(action.equals(Eventing.SUBSCRIBE_ACTION_URI)){
			//pipe the request processing to
			//  subscribe(context, request, response);
			response = subscribe(context, request, response);
		}
		else if(action.equals(Transfer.CREATE_ACTION_URI)){
			create(context, request, response);
		}
		else if(action.equals(com.sun.ws.management.mex.Metadata.INITIALIZE_ACTION_URI)){
			//Lazy instantiation
		  response = initialize(context, request, response);
		}else{
			throw new ActionNotSupportedException(action);
		}
	}

	/** Write this method so that it is an initialization call.  This should be
	 *
	 */
	public void create(HandlerContext context, Management request, Management response) {
		// TODO Auto-generated method stub

	}

	/**This method should be implemented to lazily instantiate.  This method should
	 * only be executed once.  This method is meant to be used for initialization
	 * tasks that:
	 * -could require SOAP communication with other handlers
	 * -could require expensive database initialization
	 *
	 * @param context
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws DatatypeConfigurationException
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public Management initialize(HandlerContext context, Management request,
			Management response) throws SOAPException, JAXBException,
			DatatypeConfigurationException, IOException {
		if(subscription_source_id==null){
			//Then must locate the subscription manager details
			//Send create request for EVENT_SOURCE to register with SUB_MAN
			//Extract the created id and initalize the sub_source reference
			//?? Wrap in it's own thread?
			String srcId = MetadataUtility.registerEventSourceWithSubscriptionManager(
					this, true, true);
			subscription_source_id = srcId;
		}
		response.setAction(Metadata.INITIALIZE_RESPONSE_URI);
		response.setMessageId(ManagementMessageValues.DEFAULT_UID_SCHEME+UUID.randomUUID());
		response.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	  return response;
	}

	public Management subscribe(HandlerContext context, Management eventRequest,
			Management eventResponse) throws SOAPException, JAXBException,
			DatatypeConfigurationException, IOException {

		Management subManResponse =
			MetadataUtility.registerEventSinkWithSubscriptionManager(
				this,eventRequest,
				true, true);
		eventResponse = new Management(subManResponse);
		eventResponse.setMessageId(EventingMessageValues.DEFAULT_UID_SCHEME+UUID.randomUUID());
		//Make sure that has the right elements
		eventResponse.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);

	  return eventResponse;
	}

	public boolean isAlsoTheSubscriptionManager() {
		return false;
	}

	public Management getMetadataForEventSource() throws SOAPException, JAXBException,
	DatatypeConfigurationException, IOException {
		Management eventSourceInfo =null;
		eventSourceInfo = ManagementUtility.buildMessage(null, null);
		eventSourceInfo.setTo(TO);
		eventSourceInfo.setResourceURI(RESOURCE_URI);
		eventSourceInfo.addHeaders(Management.createReferenceParametersType(
				AnnotationProcessor.RESOURCE_META_DATA_UID,
				UID));

		if(eventSourceInfo==null){
			String msg="Unable to locate metadata for event source.";
			throw new IllegalArgumentException(msg);
		}

		return eventSourceInfo;
	}

	public Management getMetadataForSubscriptionManager() throws SOAPException,
	  JAXBException, DatatypeConfigurationException, IOException {
		Management subscriptionManagerDet = null;

		if(subscriptionManager==null){
			subscriptionManagerDet = ManagementUtility.buildMessage(null, null);
			subscriptionManagerDet.setTo(ManagementMessageValues.WSMAN_DESTINATION);
			subscriptionManagerDet.setResourceURI(eventsubman_Handler.RESOURCE_URI);
		}else{
			subscriptionManagerDet = subscriptionManager;
		}

		return subscriptionManagerDet;
	}

	final static private ArrayList<QName> wiseManFamiliarQnameList = new ArrayList<QName>();
	static {
		wiseManFamiliarQnameList.add(Management.RESOURCE_URI);
		wiseManFamiliarQnameList.add(Management.TO);
		wiseManFamiliarQnameList.add(AnnotationProcessor.ENUMERATION_ACCESS_RECIPE);
		wiseManFamiliarQnameList.add(AnnotationProcessor.ENUMERATION_FILTER_USAGE);
		wiseManFamiliarQnameList.add(AnnotationProcessor.META_DATA_CATEGORY);
		wiseManFamiliarQnameList.add(AnnotationProcessor.META_DATA_DESCRIPTION);
	 }

	public void setRemoteSubscriptionManager(Management subscriptionManagerMetaData) {
		this.subscriptionManager = subscriptionManagerMetaData;
	}

	public Management subscribe(String resource, HandlerContext context, Management request, Management response) throws JAXBException, SOAPException, DatatypeConfigurationException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
