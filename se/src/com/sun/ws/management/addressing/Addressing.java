/*
 * Copyright 2005 Sun Microsystems, Inc.
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
 * $Id: Addressing.java,v 1.10 2006-07-24 13:14:58 pmonday Exp $
 */

package com.sun.ws.management.addressing;

import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.Relationship;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

public class Addressing extends SOAP {
    
    public static final String NS_PREFIX = "wsa";
    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    
    public static final String UNSPECIFIED_MESSAGE_ID = "http://schemas.xmlsoap.org/ws/2004/08/addressing/id/unspecified";
    public static final String ANONYMOUS_ENDPOINT_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";
    public static final String FAULT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";
    
    public static final QName ACTION = new QName(NS_URI, "Action", NS_PREFIX);
    public static final QName TO = new QName(NS_URI, "To", NS_PREFIX);
    public static final QName MESSAGE_ID = new QName(NS_URI, "MessageID", NS_PREFIX);
    public static final QName REPLY_TO = new QName(NS_URI, "ReplyTo", NS_PREFIX);
    public static final QName FAULT_TO = new QName(NS_URI, "FaultTo", NS_PREFIX);
    public static final QName FROM = new QName(NS_URI, "From", NS_PREFIX);
    public static final QName ADDRESS = new QName(NS_URI, "Address", NS_PREFIX);
    public static final QName RELATES_TO = new QName(NS_URI, "RelatesTo", NS_PREFIX);
    public static final QName RETRY_AFTER = new QName(NS_URI, "RetryAfter", NS_PREFIX);
    public static final QName ENDPOINT_REFERENCE = new QName(NS_URI, "EndpointReference", NS_PREFIX);
    
    public static final ObjectFactory FACTORY = new ObjectFactory();
    
    public Addressing() throws SOAPException {
        super();
    }
    
    public Addressing(final Addressing addr) throws SOAPException {
        super(addr);
    }
    
    public Addressing(final InputStream is) throws SOAPException, IOException {
        super(is);
    }
    
    public void validate() throws SOAPException, JAXBException, FaultException {
        validateElementPresent(getAction(), ACTION);
        validateElementPresent(getTo(), TO);
        validateElementPresent(getMessageId(), MESSAGE_ID);
        validateElementPresent(getReplyTo(), REPLY_TO);
        final String replyToAddress = getReplyTo().getAddress().getValue();
        validateElementPresent(replyToAddress, ADDRESS);
        
        validateURISyntax(getAction());
        validateURISyntax(getTo());
        validateURISyntax(getMessageId());
        validateURISyntax(replyToAddress);
    }
    
    protected void validateElementPresent(final Object element, final QName elementName) throws FaultException {
        if (element == null) {
            throw new MessageInformationHeaderRequiredFault(elementName);
        }
    }
    
    protected void validateURISyntax(final String uri)
    throws FaultException {
        try {
            new URI(uri);
        } catch (URISyntaxException syntax) {
            throw new InvalidMessageInformationHeaderFault(uri);
        }
    }
    
    // only address is mandatory, the rest of the params are optional and can be null
    public static EndpointReferenceType createEndpointReference(final String address,
            final ReferencePropertiesType props, final ReferenceParametersType params,
            final AttributedQName portType, final ServiceNameType serviceName) {
        
        final EndpointReferenceType epr = FACTORY.createEndpointReferenceType();
        
        final AttributedURI addressURI = FACTORY.createAttributedURI();
        addressURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        addressURI.setValue(address.trim());
        epr.setAddress(addressURI);
        
        if (params != null) {
            epr.setReferenceParameters(params);
        }
        
        if (props != null) {
            epr.setReferenceProperties(props);
        }
        
        if (serviceName != null) {
            epr.setServiceName(serviceName);
        }
        
        if (portType != null) {
            epr.setPortType(portType);
        }
        
        return epr;
    }
    
    public Node[] unwrapEndpointReference(final Node wrappedEPR) {
        if (ENDPOINT_REFERENCE.getLocalPart().equals(wrappedEPR.getLocalName()) &&
                ENDPOINT_REFERENCE.getNamespaceURI().equals(wrappedEPR.getNamespaceURI())) {
            final NodeList children = wrappedEPR.getChildNodes();
            final List<Node> nl = new ArrayList<Node>(children.getLength());
            for (int i = children.getLength() - 1; i >= 0; i--) {
                nl.add(children.item(i));
            }
            return (Node[]) nl.toArray(new Node[nl.size()]);
        }
        throw new IllegalArgumentException("Can only unwrap EndpointReferences");
    }
    
    // setters
    
    public void addHeaders(final ReferenceParametersType params) throws JAXBException {
        if (params == null) {
            return;
        }
        
        final Node header = getHeader();
        for (final Object param : params.getAny()) {
            // cannot simply use the following - we get a JAXB unable to marshal exception
            // getXmlBinding().marshal(param, getHeader());
            if (param instanceof Node) {
                final Node node = (Node) param;
                // TODO: can be a performance hog if the node is deeply nested
                header.appendChild(header.getOwnerDocument().adoptNode(node.cloneNode(true)));
            } else {
                throw new RuntimeException("ReferenceParam " + param.toString() +
                        " of class " + param.getClass() + " is being ignored");
            }
        }
    }
    
    public void setAction(final String action) throws JAXBException, SOAPException {
        removeChildren(getHeader(), ACTION);
        final AttributedURI actionURI = FACTORY.createAttributedURI();
        actionURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        actionURI.setValue(action.trim());
        final JAXBElement<AttributedURI> actionElement = FACTORY.createAction(actionURI);
        getXmlBinding().marshal(actionElement, getHeader());
    }
    
    public void setTo(final String to) throws JAXBException, SOAPException {
        removeChildren(getHeader(), TO);
        final AttributedURI toURI = FACTORY.createAttributedURI();
        toURI.setValue(to.trim());
        toURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        final JAXBElement<AttributedURI> toElement = FACTORY.createTo(toURI);
        getXmlBinding().marshal(toElement, getHeader());
    }
    
    public void setMessageId(final String msgId) throws JAXBException, SOAPException {
        removeChildren(getHeader(), MESSAGE_ID);
        final AttributedURI msgIdURI = FACTORY.createAttributedURI();
        msgIdURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, Boolean.TRUE.toString());
        msgIdURI.setValue(msgId.trim());
        final JAXBElement<AttributedURI> msgIdElement = FACTORY.createMessageID(msgIdURI);
        getXmlBinding().marshal(msgIdElement, getHeader());
    }
    
    // convenience method
    public void setReplyTo(final String uri) throws JAXBException, SOAPException {
        setReplyTo(createEndpointReference(uri.trim(), null, null, null, null));
    }
    
    public void setReplyTo(final EndpointReferenceType epr) throws JAXBException, SOAPException {
        removeChildren(getHeader(), REPLY_TO);
        final JAXBElement<EndpointReferenceType> element = FACTORY.createReplyTo(epr);
        getXmlBinding().marshal(element, getHeader());
    }
    
    // convenience method
    public void setFaultTo(final String uri) throws JAXBException, SOAPException {
        setFaultTo(createEndpointReference(uri.trim(), null, null, null, null));
    }
    
    public void setFaultTo(final EndpointReferenceType epr) throws JAXBException, SOAPException {
        removeChildren(getHeader(), FAULT_TO);
        final JAXBElement<EndpointReferenceType> element = FACTORY.createFaultTo(epr);
        getXmlBinding().marshal(element, getHeader());
    }
    
    // convenience method
    public void setFrom(final String uri) throws JAXBException, SOAPException {
        setFrom(createEndpointReference(uri.trim(), null, null, null, null));
    }
    
    public void setFrom(final EndpointReferenceType epr) throws JAXBException, SOAPException {
        removeChildren(getHeader(), FROM);
        final JAXBElement<EndpointReferenceType> element = FACTORY.createFrom(epr);
        getXmlBinding().marshal(element, getHeader());
    }
    
    public void addRelatesTo(final String relationshipURI) throws JAXBException {
        final Relationship relationship = FACTORY.createRelationship();
        relationship.setValue(relationshipURI.trim());
        final JAXBElement<Relationship> element = FACTORY.createRelatesTo(relationship);
        getXmlBinding().marshal(element, getHeader());
    }
    
    public void addRelatesTo(final String relationshipURI, final QName relationshipType) throws JAXBException {
        final Relationship relationship = FACTORY.createRelationship();
        relationship.setRelationshipType(relationshipType);
        relationship.setValue(relationshipURI.trim());
        final JAXBElement<Relationship> element = FACTORY.createRelatesTo(relationship);
        getXmlBinding().marshal(element, getHeader());
    }
    
    // getters
    
    public SOAPElement[] getHeaders() throws SOAPException {
        return getChildren(getHeader());
    }
    
    public String getAction() throws JAXBException, SOAPException {
        return getAttributedURI(ACTION);
    }
    
    public String getTo() throws JAXBException, SOAPException {
        return getAttributedURI(TO);
    }
    
    public String getMessageId() throws JAXBException, SOAPException {
        return getAttributedURI(MESSAGE_ID);
    }
    
    public EndpointReferenceType getReplyTo() throws JAXBException, SOAPException {
        return getEndpointReference(REPLY_TO);
    }
    
    public EndpointReferenceType getFaultTo() throws JAXBException, SOAPException {
        return getEndpointReference(FAULT_TO);
    }
    
    public EndpointReferenceType getFrom() throws JAXBException, SOAPException {
        return getEndpointReference(FROM);
    }
    
    public Relationship[] getRelatesTo() throws JAXBException, SOAPException {
        final SOAPElement[] relations = getChildren(getHeader(), RELATES_TO);
        final Relationship[] relationships = new Relationship[relations.length];
        for (int i=0; i < relations.length; i++) {
            final Object relation = getXmlBinding().unmarshal(relations[i]);
            relationships[i] = ((JAXBElement<Relationship>) relation).getValue();
        }
        return relationships;
    }
    
    public EndpointReferenceType getEndpointReference(final SOAPElement parent, final QName... qname) throws JAXBException, SOAPException {
        final Object value = unbind(parent, qname);
        return value == null ? null : ((JAXBElement<EndpointReferenceType>) value).getValue();
    }
    
    // get helpers
    
    private String getAttributedURI(final QName qname) throws JAXBException, SOAPException {
        final Object value = unbind(getHeader(), qname);
        return value == null ? null : ((JAXBElement<AttributedURI>) value).getValue().getValue().trim();
    }
    
    private EndpointReferenceType getEndpointReference(final QName... qname) throws JAXBException, SOAPException {
        return getEndpointReference(getHeader(), qname);
    }
    
}
