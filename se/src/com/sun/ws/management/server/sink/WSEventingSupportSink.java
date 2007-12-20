package com.sun.ws.management.server.sink;

import com.sun.ws.management.server.message.WSAddressingRequest;
import com.sun.ws.management.server.message.WSAddressingResponse;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.xml.XMLSchema;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.Management;
import com.sun.ws.management.DeliveryRefusedFault;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XMLSchema;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;

//Testing 
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import java.io.IOException;
import javax.xml.soap.SOAPException;
import javax.xml.bind.JAXBException;
import java.util.UUID;

public class WSEventingSupportSink {

    protected static final String UUID_SCHEME = "urn:uuid:";
	
    /**
     *  Create a DeliveryRefusedFault Message like following:
     *  This function is called When responsing from EventSink.  
     * 	<s:Envelope ... ...>
	 *	<s:Header>
	 *		<wsa:Action>http://schemas.dmtf.org/wbem/wsman/1/wsman/fault</wsa:Action>
	 *		<wsa:MessageID>uuid:xxx-xxxxx…</wsa:MessageID>
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
	 *				<s:Text xml:lang=“en”>reference from Spec</s:Text>
	 *			</s:Reason>
	 *			<s:Detail/>
	 *		</s:Fault>
	 *	<s:Body/>
	 *  </s:Envelope>
	 *
     */
	public static Addressing createDeliveryRefusedFaultMessage(Addressing request,String reason)       
	throws SOAPException, JAXBException, IOException{
	 

                           


		final Addressing fault_msg = new Addressing();
		if(fault_msg == null) return null;
	    final String UUID_fault= "uuid:";
		fault_msg.setAction(Management.FAULT_ACTION_URI);                         //set action
		fault_msg.setMessageId(UUID_fault + UUID.randomUUID().toString()); 	//set messageID
		final String request_URI = request.getMessageId();                         //set relatesto
		fault_msg.addRelatesTo(request_URI);                                
                                                                              //		set fault
		final FaultException Fault_ex = new FaultException( null,                       
		                    	SOAP.RECEIVER, DeliveryRefusedFault.DELIVERY_REFUSED,
		                    	reason,null ); 
		fault_msg.setFault( Fault_ex );                                     
	   // System.out.println(fault_msg);
		return fault_msg;

	}
	
    /**
     *  Create an ACK Message like following:
     *  This function is called When responsing from EventSink.
     *  <s:Envelope …>
     *	<s:Header>
	 *		...
	 *		<wsa:To>reference from the ReplyTo field of request</wsa:To>
	 *		<wsa:Action>http://schemas.dmtf.org/wbem/wsman/1/wsman/Ack</wsa:Action>
	 *		<wsa:RelatesTo>reference from the messageID field of request</wsa:RelatesTo>
	 *		...
	 *	</s:Header>
	 *	<s:Body/>
	 *	</s:Envelope>
     */	
	public static Addressing createEventACKAcknowledgement(Addressing request)
	throws SOAPException, JAXBException, IOException {
	 
		final Addressing response = new Addressing();
		response.getEnvelope().addNamespaceDeclaration(XMLSchema.NS_PREFIX, XMLSchema.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(SOAP.NS_PREFIX, SOAP.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Addressing.NS_PREFIX, Addressing.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Eventing.NS_PREFIX, Eventing.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Enumeration.NS_PREFIX, Enumeration.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Transfer.NS_PREFIX, Transfer.NS_URI);
		response.getEnvelope().addNamespaceDeclaration(Management.NS_PREFIX, Management.NS_URI);
		final EndpointReferenceType replyTo = request.getReplyTo();
		if(replyTo != null){
			response.setTo(replyTo.getAddress().getValue()); 
			final ReferenceParametersType refparams = replyTo.getReferenceParameters();
			if(refparams != null){
				response.addHeaders(refparams);
			}
	        final ReferencePropertiesType refprops = replyTo.getReferenceProperties();
	        if(refprops != null){
	        	response.addHeaders(refprops);
	        }
	        response.setAction(EventingExtensions.ACK_ACTION_URI);
	        response.addRelatesTo(request.getMessageId());
	        response.setMessageId(UUID_SCHEME+UUID.randomUUID().toString());

	        return response;
		}
		return null;
	}		 
}




