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
 * $Id: EventingSupport.java,v 1.3 2006-02-10 01:08:32 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.eventing.DeliveryModeRequestedUnavailableFault;
import com.sun.ws.management.eventing.EventSourceUnableToProcessFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.eventing.InvalidExpirationTimeFault;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transport.HttpClient;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
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
public final class EventingSupport {
    
    private static final String UUID_SCHEME = "urn:uuid:";
    private static final int CLEANUP_INTERVAL = 60000;
    
    private final Map<UUID, Context> contextMap = new HashMap();
    private static final Timer cleanupTimer = new Timer(true);
    
    private static final XPath XPATH = XPathFactory.newInstance().newXPath();
    private static final String[] SUPPORTED_FILTER_DIALECTS = {
        com.sun.ws.management.xml.XPath.NS_URI
    };
    
    private static DatatypeFactory datatypeFactory;
    
    private static final class Context {
        private XMLGregorianCalendar expiration = null;
        private EndpointReferenceType notifyTo = null;
        private XPathExpression filter = null;
    }
    
    public EventingSupport() {}
    
    public void subscribe(final Eventing request, final Eventing response)
    throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
        
        if (datatypeFactory == null) {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        
        final Subscribe subscribe = request.getSubscribe();
        final FilterType filterType = subscribe.getFilter();
        String filterExpression = null;
        if (filterType != null) {
            if (!com.sun.ws.management.xml.XPath.NS_URI.equals(filterType.getDialect())) {
                throw new FilteringRequestedUnavailableFault(null, SUPPORTED_FILTER_DIALECTS);
            }
            final List<Object> expressions = filterType.getContent();
            if (expressions == null) {
                throw new InvalidMessageFault("Missing a filter expression");
            }
            final Object expr = expressions.get(0);
            if (expr == null) {
                throw new InvalidMessageFault("Missing filter expression");
            }
            if (expr instanceof String) {
                filterExpression = (String) expr;
            } else {
                throw new InvalidMessageFault("Invalid filter expression type: " + expr);
            }
        }
        
        final EndpointReferenceType endTo = subscribe.getEndTo();
        if (endTo != null) {
            // TODO: don't want to silently ignore - which fault to throw?
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
        
        final String expires = subscribe.getExpires();
        final GregorianCalendar now = new GregorianCalendar();
        final XMLGregorianCalendar nowXml = datatypeFactory.newXMLGregorianCalendar(now);
        XMLGregorianCalendar expiration = null;
        if (expires != null) {
            try {
                // first try if it's a Duration
                final Duration duration = datatypeFactory.newDuration(expires);
                expiration = datatypeFactory.newXMLGregorianCalendar(now);
                expiration.add(duration);
            } catch (IllegalArgumentException ndex) {
                try {
                    // now see if it is a calendar time
                    expiration = datatypeFactory.newXMLGregorianCalendar(expires);
                } catch (IllegalArgumentException ncex) {
                    throw new InvalidExpirationTimeFault();
                }
            }
            if (nowXml.compare(expiration) > 0) {
                // expiration cannot be in the past
                throw new InvalidExpirationTimeFault();
            }
        }
        
        final UUID context = UUID.randomUUID();
        final Context ctx = new Context();
        ctx.expiration = expiration;
        ctx.notifyTo = notifyTo;
        if (filterExpression != null) {
            try {
                ctx.filter= XPATH.compile(filterExpression);
            } catch (XPathExpressionException xpx) {
                throw new EventSourceUnableToProcessFault("Unable to compile XPath expression: " + 
                        "\"" + filterExpression + "\" detail: " + xpx.getMessage());
            }
        }
        contextMap.put(context, ctx);
        
        final TimerTask ttask = new TimerTask() {
            public void run() {
                final GregorianCalendar now = new GregorianCalendar();
                final XMLGregorianCalendar nowXml =
                        datatypeFactory.newXMLGregorianCalendar(now);
                final UUID[] keys = contextMap.keySet().toArray(new UUID[contextMap.size()]);
                for (int i = 0; i < keys.length; i++) {
                    final UUID key = keys[i];
                    final Context value = contextMap.get(key);
                    if (value.expiration == null) {
                        // no expiration defined, never expires
                        continue;
                    }
                    if (nowXml.compare(value.expiration) > 0) {
                        // context expired, GC
                        contextMap.remove(key);
                    }
                }
            }
        };
        cleanupTimer.schedule(ttask, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
        
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
        response.setSubscribeResponse(subMgrEPR, expiration.toXMLFormat());
    }
    
    // TODO: avoid blocking the sender - use a thread pool to send notifications
    public void sendEvent(final Addressing msg)
    throws SOAPException, JAXBException, IOException, XPathExpressionException {
        // TODO: verify if subscription has expired -
        // depending on the cleanup timer to GC expired subscriptions leaves a window
        // where events will still be delivered, OTOH checking here will have a perf impact
        for (final Context ctx : contextMap.values()) {
            if (ctx.filter != null) {
                final Node content = msg.getBody().getFirstChild();
                final Boolean pass = (Boolean) ctx.filter.evaluate(content, XPathConstants.BOOLEAN);
                if (!pass.booleanValue()) {
                    return;
                }
            }
            final ReferenceParametersType refp = ctx.notifyTo.getReferenceParameters();
            if (refp != null) {
                msg.addHeaders(refp);
            }
            msg.setTo(ctx.notifyTo.getAddress().getValue());
            msg.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
            HttpClient.sendResponse(msg);
        }
    }
}
