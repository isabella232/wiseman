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
 **Revision 1.5  2007/06/08 15:38:38  denis_rachal
 **The following enhanceent were made to the testing infrastructure:
 **
 **  * Capture of logs in files for junits test
 **  * Added user.wsdl & user.xsd to wsman.war
 **  * Consolidated userenum & user into single handler that is thread safe for load testing
 **
 **Revision 1.4  2007/05/30 20:30:17  nbeers
 **Add HP copyright header
 **
 ** 
 *
 * $Id: user_Handler.java,v 1.6 2007-06-22 06:13:57 simeonpinder Exp $
 */
package com.sun.ws.management.server.handler.wsman.auth;

import java.util.logging.Logger;

import management.MetadataTest;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.eventing.EventingMessageValues.CreationTypes;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementOperationDefinitionAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementQNamedNodeWithValueAnnotation;
import com.sun.ws.management.server.EnumerationSupport;

import framework.models.UserHandler;
import framework.models.UserIteratorFactory;

/**
 * This Handler Deligates to The UserHandler Class
 * @author wire
 *
 */
@WsManagementDefaultAddressingModelAnnotation(
	getDefaultAddressDefinition=
		@WsManagementAddressDetailsAnnotation(
			wsaTo=MetadataTest.destUrl, 
			wsmanResourceURI=MetadataTest.resourceUri,
			wsmanSelectorSetContents={
				MetadataTest.selector1,
				MetadataTest.selector2					
			},
			referenceParametersContents=
			{ 
				@WsManagementQNamedNodeWithValueAnnotation(
					localpart=MetadataTest.custQlocPart, 
					namespaceURI=MetadataTest.custQnsuri, 
					nodeValue=MetadataTest.custQvalue, 
					prefix=MetadataTest.custQprefix) 
			},
			referencePropertiesContents=
			{ 
				@WsManagementQNamedNodeWithValueAnnotation(
				localpart=MetadataTest.custQlocPart1, 
				namespaceURI=MetadataTest.custQnsuri1, 
				nodeValue=MetadataTest.custQvalue1, 
				prefix=MetadataTest.custQprefix1) 
			}
	),
	 metaDataCategory = MetadataTest.metaDataCategory, 
	 metaDataDescription = MetadataTest.metaDataDescription, 
	 resourceMetaDataUID = MetadataTest.metaDataUID,
	 resourceMiscellaneousInformation=MetadataTest.metaDataMiscInfo	 
)
public class user_Handler extends DelegatingHandler {
	public static final String categoryName = CreationTypes.SUBSCRIPTION_SOURCE.name();
	public static final String RESOURCE_URI = "wsman:auth/user";

	@WsManagementDefaultAddressingModelAnnotation(
		getDefaultAddressDefinition = 
			@WsManagementAddressDetailsAnnotation(
				wsaTo=MetadataTest.embeddedAnnotTo, 
				wsmanResourceURI=MetadataTest.embeddedAnnotResourceURI),
			resourceMetaDataUID = MetadataTest.embeddedAnnotResourceMetUID,
			schemaList={
				MetadataTest.schem1,
				MetadataTest.schem2
			},
			definedOperations={
				@WsManagementOperationDefinitionAnnotation(
					operationInputTypeMap = MetadataTest.op1inpt, 
					operationName = MetadataTest.op1Name, 
					operationOutputTypeMap = MetadataTest.op1outpt
				)
			}
	)
	private final boolean ANNOTATION_HOLDER_VARIABLE = true;

	@WsManagementDefaultAddressingModelAnnotation(
		getDefaultAddressDefinition = 
			@WsManagementAddressDetailsAnnotation(
				wsaTo=MetadataTest.embeddedAnnotTo,
				wsmanResourceURI=MetadataTest.embeddedAnnotResourceURI),
		resourceMetaDataUID = "7474746465656565656565656565656",
		metaDataCategory= "SUBSCRIPTION_SOURCE"
	)
	private final boolean ANNOTATION_HOLDER_VARIABLE_1 = true;
	
//	//Add a metadata reference for the traffic light
//	@WsManagementDefaultAddressingModelAnnotation(
//		getDefaultAddressDefinition = 
//			@WsManagementAddressDetailsAnnotation(
//				wsaTo="http://localhost:8080/traffic/",
//				wsmanResourceURI="urn:resources.wiseman.dev.java.net/traffic/1/light",
//				wsmanSelectorSetContents={"name="+user_Handler.trafFragString}
//			),
//		resourceMetaDataUID = "http://localhost:8080/traffic/"+user_Handler.trafFragString,
//		metaDataCategory= "Traffic_Light"
//	)
//	private final boolean ANNOTATION_HOLDER_VARIABLE_2 = true;
//	public static final String trafFragString ="TrafficLight-Fragment";
	
    public user_Handler() {
        super(new UserHandler());
        
		try {
			EnumerationSupport.registerIteratorFactory(RESOURCE_URI,
					new UserIteratorFactory(RESOURCE_URI));
		} catch (Exception e) {
			throw new InternalErrorFault(e.getMessage());
		}
    }
}
