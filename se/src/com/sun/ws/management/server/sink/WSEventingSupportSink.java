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
package com.sun.ws.management.server.sink;

import java.io.IOException;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;

import com.sun.ws.management.DeliveryRefusedFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XMLSchema;

public class WSEventingSupportSink {

    protected static final String UUID_SCHEME = "urn:uuid:";
	
    /**
     *  Create a DeliveryRefusedFault Message like following:
     *  This function is called When responding from EventSink.  
     * 	<s:Envelope ... ...>
	 *	<s:Header>
	 *		<wsa:Action>http://schemas.dmtf.org/wbem/wsman/1/wsman/fault</wsa:Action>
	 *		<wsa:MessageID>uuid:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</wsa:MessageID>
	 *		<wsa:RelatesTo>reference from the messageID field of request</wsa:RelatesTo>
	 *	</s:Header>
	 *	<s:Body>
	 *		<s:Fault>
	 *			<s:Code>
	 *				<s:Value>s:Receiver</s:Value>
	 *				<s:Subcode> 
	 *					<s:Value>wsman:DeliveryRefused</s:Value>
	 *				</s:Subcode>
	 *			</s:Code>
	 *			<s:Reason>
	 *				<s:Text xml:lang=en>reference from Spec</s:Text>
	 *			</s:Reason>
	 *			<s:Detail/>
	 *		</s:Fault>
	 *	<s:Body/>
	 *  </s:Envelope>
	 *
     */
	public static Addressing createDeliveryRefusedFaultMessage(
			Addressing request, String reason) throws SOAPException,
			JAXBException, IOException {

		final Addressing fault_msg = new Addressing();
		if (fault_msg == null)
			return null;
		final String UUID_fault = "uuid:";
		fault_msg.setAction(Management.FAULT_ACTION_URI); // set action
		fault_msg.setMessageId(UUID_fault + UUID.randomUUID().toString()); // set
																			// messageID
		final String request_URI = request.getMessageId(); // set relatesTo
		fault_msg.addRelatesTo(request_URI);
		// set fault
		final FaultException Fault_ex = new FaultException(null, SOAP.RECEIVER,
				DeliveryRefusedFault.DELIVERY_REFUSED, reason, (Node[])null);
		fault_msg.setFault(Fault_ex);
		// System.out.println(fault_msg);
		return fault_msg;

	}
	
    /**
	 * Create an ACK Message like following: This function is called When
	 * responding from EventSink. <s:Envelope> <s:Header> ...
	 * <wsa:To>reference from the ReplyTo field of request</wsa:To>
	 * <wsa:Action>http://schemas.dmtf.org/wbem/wsman/1/wsman/Ack</wsa:Action>
	 * <wsa:RelatesTo>reference from the messageID field of request</wsa:RelatesTo>
	 * ... </s:Header> <s:Body/> </s:Envelope>
	 */	
	public static Addressing createEventACKAcknowledgement(Addressing request)
	throws SOAPException, JAXBException, IOException {

		final Addressing response = new Addressing();
		response.getEnvelope().addNamespaceDeclaration(XMLSchema.NS_PREFIX,
				XMLSchema.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(SOAP.NS_PREFIX,
				SOAP.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Addressing.NS_PREFIX,
				Addressing.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Eventing.NS_PREFIX,
				Eventing.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Enumeration.NS_PREFIX,
				Enumeration.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Transfer.NS_PREFIX,
				Transfer.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Management.NS_PREFIX,
				Management.NS_URI);
		final EndpointReferenceType replyTo = request.getReplyTo();
		if (replyTo != null) {
			response.setTo(replyTo.getAddress().getValue());
			final ReferenceParametersType refparams = replyTo
					.getReferenceParameters();
			if (refparams != null) {
				response.addHeaders(refparams);
			}
			final ReferencePropertiesType refprops = replyTo
					.getReferenceProperties();
			if (refprops != null) {
				response.addHeaders(refprops);
			}
			response.setAction(EventingExtensions.ACK_ACTION_URI);
			response.addRelatesTo(request.getMessageId());
			response.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

			return response;
		}
		return null;
	}		 
}