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
 * $Id: user_Handler.java,v 1.4 2007-05-30 20:30:17 nbeers Exp $
 */
package com.sun.ws.management.server.handler.wsman.auth;

import management.MetadataTest;

import com.sun.ws.management.eventing.EventingMessageValues.CreationTypes;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementOperationDefinitionAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementQNamedNodeWithValueAnnotation;

import framework.models.UserHandler;

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
	
    public user_Handler() {
        super(new UserHandler());
    }
}
