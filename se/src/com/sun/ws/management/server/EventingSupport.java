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
 * $Id: EventingSupport.java,v 1.8 2006-06-01 18:47:49 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.DeliveryModeRequestedUnavailableFault;
import com.sun.ws.management.eventing.EventSourceUnableToProcessFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transport.HttpClient;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.FilterType;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;

/**
 * A helper class that encapsulates some of the arcane logic to manage
 * subscriptions using the WS-Eventing protocol.
 */
public final class EventingSupport extends BaseSupport {
    
    private EventingSupport() {}
    
    public static Object subscribe(final Eventing request, final Eventing response,
            final Map<String, String> namespaces)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
        
        final Subscribe subscribe = request.getSubscribe();
        final FilterType filterType = subscribe.getFilter();
        String filterExpression = null;
        if (filterType != null) {
            filterExpression = initFilter(filterType.getDialect(), filterType.getContent());
        }
        
        final EndpointReferenceType endTo = subscribe.getEndTo();
        if (endTo != null) {
            throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.ADDRESSING_MODE);
        }
        
        final DeliveryType delivery = subscribe.getDelivery();
        // TODO: add more delivery modes
        if (!Eventing.PUSH_DELIVERY_MODE.equals(delivery.getMode())) {
            final String[] supportedDeliveryModes = {
                Eventing.PUSH_DELIVERY_MODE
            };
            throw new DeliveryModeRequestedUnavailableFault(supportedDeliveryModes);
        }
        
        EndpointReferenceType notifyTo = null;
        for (final Object content : delivery.getContent()) {
            final Class contentClass = content.getClass();
            if (JAXBElement.class.equals(contentClass)) {
                final JAXBElement<Object> element = (JAXBElement<Object>) content;
                final QName name = element.getName();
                final Object item = element.getValue();
                if (item instanceof EndpointReferenceType) {
                    final EndpointReferenceType epr = (EndpointReferenceType) item;
                    if (Eventing.NOTIFY_TO.equals(name)) {
                        notifyTo = epr;
                    }
                }
            }
        }
        if (notifyTo == null) {
            throw new InvalidMessageFault("Event destination not specified: missing NotifyTo element");
        }
        if (notifyTo.getAddress() == null) {
            throw new InvalidMessageFault("Event destination not specified: missing NotifyTo.Address element");
        }
        
        EventingContext ctx = null;
        try {
            ctx = new EventingContext(initExpiration(subscribe.getExpires()),
                    filterExpression,
                    namespaces,
                    notifyTo);
        } catch (XPathExpressionException xpx) {
            throw new EventSourceUnableToProcessFault("Unable to compile XPath: " +
                    "\"" + filterExpression + "\"");
        }
        final UUID context = initContext(ctx);
        
        final ObjectFactory aof = new ObjectFactory();
        final ReferenceParametersType refParams = aof.createReferenceParametersType();
        final Document doc = response.newDocument();
        final Element identifier = doc.createElementNS(Eventing.IDENTIFIER.getNamespaceURI(),
                Eventing.IDENTIFIER.getPrefix() + ":" + Eventing.IDENTIFIER.getLocalPart());
        identifier.setTextContent(context.toString());
        doc.appendChild(identifier);
        refParams.getAny().add(doc.getDocumentElement());
        final EndpointReferenceType subMgrEPR =
                response.createEndpointReference(request.getTo(),
                null, refParams, null, null);
        response.setSubscribeResponse(subMgrEPR, ctx.getExpiration());
        
        return context;
    }
    
    // TODO: avoid blocking the sender - use a thread pool to send notifications
    public static boolean sendEvent(final Object context, final Addressing msg)
    throws SOAPException, JAXBException, IOException, XPathExpressionException {
        
        assert datatypeFactory != null : UNITIALIZED;
        
        final BaseContext bctx = getContext(context);
        if (bctx == null) {
            throw new RuntimeException("Context not found: subscription expired?");
        }
        if (!(bctx instanceof EventingContext)) {
            throw new RuntimeException("Context not found");
        }
        final EventingContext ctx = (EventingContext) bctx;
        
        final GregorianCalendar now = new GregorianCalendar();
        final XMLGregorianCalendar nowXml = datatypeFactory.newXMLGregorianCalendar(now);
        if (ctx.isExpired(nowXml)) {
            removeContext(context);
            throw new RuntimeException("Subscription expired");
        }
        
        // the filter is only applied to the first child in soap body
        final Node content = msg.getBody().getFirstChild();
        if (!ctx.evaluate(content)) {
            return false;
        }
        
        final EndpointReferenceType notifyTo = ctx.getNotifyTo();
        final ReferenceParametersType refp = notifyTo.getReferenceParameters();
        if (refp != null) {
            msg.addHeaders(refp);
        }
        msg.setTo(notifyTo.getAddress().getValue());
        msg.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        HttpClient.sendResponse(msg);
        return true;
    }
}
