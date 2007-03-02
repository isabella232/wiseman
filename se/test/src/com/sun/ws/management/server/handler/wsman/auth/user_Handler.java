package com.sun.ws.management.server.handler.wsman.auth;

import management.MetadataTest;

import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
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

	@WsManagementDefaultAddressingModelAnnotation(
		getDefaultAddressDefinition = 
			@WsManagementAddressDetailsAnnotation(
				wsaTo=MetadataTest.embeddedAnnotTo, 
				wsmanResourceURI=MetadataTest.embeddedAnnotResourceURI),
		resourceMetaDataUID = MetadataTest.embeddedAnnotResourceMetUID		
	)
	private final boolean ANNOTATION_HOLDER_VARIABLE = true;
	
    public user_Handler() {
        super(new UserHandler());
    }
}
