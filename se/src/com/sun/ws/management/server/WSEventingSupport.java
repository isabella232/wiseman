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
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **Revision 1.27  2007/10/30 09:27:47  jfdenise
 **WiseMan to take benefit of Sun JAX-WS RI Message API and WS-A offered support.
 **Commit a new JAX-WS Endpoint and a set of Message abstractions to implement WS-Management Request and Response processing on the server side.
 **
 **Revision 1.26  2007/10/02 10:43:44  jfdenise
 **Fix for bug ID 134, Enumeration Iterator look up is static
 **Applied to Enumeration and Eventing
 **
 **Revision 1.25  2007/09/18 13:06:56  denis_rachal
 **Issue number:  129, 130 & 132
 **Obtained from:
 **Submitted by:
 **Reviewed by:
 **
 **129  ENHANC  P2  All  denis_rachal  NEW   Need support for ReNew Operation in Eventing
 **130  DEFECT  P3  x86  jfdenise  NEW   Should return a boolean variable result not a constant true
 **132  ENHANC  P3  All  denis_rachal  NEW   Make ServletRequest attributes available as properties in Ha
 **
 **Added enhancements and fixed issue # 130.
 **
 **Revision 1.24  2007/06/13 13:19:02  jfdenise
 **Fix for BUG ID 115 : EventingSupport should be able to create an event msg
 **
 **Revision 1.23  2007/05/30 20:31:04  nbeers
 **Add HP copyright header
 **
 **
 * $Id: WSEventingSupport.java,v 1.1 2007-10-31 12:25:38 jfdenise Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.Message;
import com.sun.ws.management.server.message.WSEventingRequest;
import com.sun.ws.management.server.message.WSEventingResponse;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.Renew;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;
import org.xmlsoap.schemas.ws._2004._08.eventing.Unsubscribe;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.eventing.DeliveryModeRequestedUnavailableFault;
import com.sun.ws.management.eventing.EventSourceUnableToProcessFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.HttpClient;

/**
 * A helper class that encapsulates some of the arcane logic to manage
 * subscriptions using the WS-Eventing protocol.
 */
public final class WSEventingSupport extends WSEventingBaseSupport {
    private WSEventingSupport() {}
    
    // the EventingExtensions.PULL_DELIVERY_MODE is handled by
    // EnumerationSupport
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final ContextListener listener)
            throws DatatypeConfigurationException, SOAPException,
            JAXBException, FaultException {
        return subscribe(handlerContext, request, response, listener, null);
    }
    
    // the EventingExtensions.PULL_DELIVERY_MODE is handled by
    // EnumerationSupport
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final ContextListener listener, final WSEventingIteratorFactory factory)
            throws DatatypeConfigurationException, SOAPException,
            JAXBException, FaultException {
        return subscribe(handlerContext, request, response, false, DEFAULT_QUEUE_SIZE, listener, factory);
    }
    
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final boolean isFiltered,
            final int queueSize,
            final ContextListener listener, 
            final WSEventingIteratorFactory factory)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
         return subscribe(handlerContext, request, response,isFiltered,queueSize,listener,(Object)factory);
    }
    
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final boolean isFiltered,
            final int queueSize,
            final ContextListener listener,
            final Object factory)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
        final Subscribe subscribe = request.getSubscribe();
        if (subscribe == null) {
            throw new InvalidMessageFault();
        }
        
        final EndpointReferenceType endTo = subscribe.getEndTo();
        if (endTo != null) {
            throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.ADDRESSING_MODE);
        }
        
        final DeliveryType delivery = subscribe.getDelivery();
        String deliveryMode = delivery.getMode();
        if (deliveryMode == null) {
            // implied value
            deliveryMode = Eventing.PUSH_DELIVERY_MODE;
        }
        
        Filter filter = null;
        
        if (deliveryMode.equals(EventingExtensions.PULL_DELIVERY_MODE)) {
            // this is a pull event mode subscribe request so setup an enumeration
            
            EnumerationIterator iterator = newIterator(factory, handlerContext,
                    request,
                    response,
                    isFiltered,
                    queueSize);

            if (iterator.isFiltered() == false) {
                // We will do the filtering
                try {
                    filter = createFilter(request);
                } catch (FilteringRequestedUnavailableFault fex) {
                    throw fex;
                } catch (Exception ex) {
                    throw new EventSourceUnableToProcessFault(ex.getMessage());
                }
            }
            
            if (subscribe.getEndTo() != null) {
                throw new UnsupportedFeatureFault(
                        UnsupportedFeatureFault.Detail.ADDRESSING_MODE);
            }
            
            // create and register an EnumerationContext
            EnumerationContext ctx = new EnumerationContext(initExpiration(subscribe.getExpires()),
                    filter, null, iterator, listener);
            
            // Set single thread use of this context
            synchronized (ctx) {
                final UUID context = initContext(handlerContext, ctx);
                response.setSubscribeResponse(WSEventingSupport
                        .createSubscriptionManagerEpr(request, response,
                        context), ctx.getExpiration(), context
                        .toString());
                return context;
            }
            
        } else {
            // one of the push modes
            // XXX REVISIT ONLY DONE WITH OLD WAY
            // NEED TO FIX THE NAMESPACE MAP
            if (isFiltered == false) {
                // We will do the filtering
                try {
                    filter = createFilter(request);
                } catch (FilteringRequestedUnavailableFault fex) {
                    throw fex;
                } catch (Exception ex) {
                    throw new EventSourceUnableToProcessFault(ex.getMessage());
                }
            }
            if (!isDeliveryModeSupported(deliveryMode)) {
                throw new DeliveryModeRequestedUnavailableFault(
                        getSupportedDeliveryModes());
            }
            
            EndpointReferenceType notifyTo = null;
            for (final Object content : delivery.getContent()) {
                final Class contentClass = content.getClass();
                if (JAXBElement.class.equals(contentClass)) {
                    final JAXBElement element = (JAXBElement) content;
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
                throw new InvalidMessageFault(
                        "Event destination not specified: missing NotifyTo element");
            }
            if (notifyTo.getAddress() == null) {
                throw new InvalidMessageFault(
                        "Event destination not specified: missing NotifyTo.Address element");
            }
            
            EventingContext ctx = new EventingContext(initExpiration(subscribe
                    .getExpires()), filter, notifyTo, listener);
            
            final UUID context = initContext(handlerContext, ctx);
            response.setSubscribeResponseExt(createSubscriptionManagerEpr(request,
                    response, context), ctx.getExpiration());
            return context;
        }
     }
     
    public static EndpointReferenceType createSubscriptionManagerEpr(
            final WSEventingRequest request, final WSEventingResponse response,
            final Object context) throws SOAPException, JAXBException {
        
        final ReferenceParametersType refp = Addressing.FACTORY.createReferenceParametersType();
        final AttributableURI attributableURI = Management.FACTORY.createAttributableURI();
        attributableURI.setValue(request.getResourceURIForEventing().toString());
        refp.getAny().add(Management.FACTORY.createResourceURI(attributableURI));
        
        final Document doc = Message.newDocument();
        final Element identifier = doc.createElementNS(Eventing.IDENTIFIER.getNamespaceURI(),
                Eventing.IDENTIFIER.getPrefix() + ":" + Eventing.IDENTIFIER.getLocalPart());
        identifier.setTextContent(context.toString());
        doc.appendChild(identifier);
        refp.getAny().add(doc.getDocumentElement());
        String to;
        try {
            to = request.getAddressURI().toString();
        }catch(Exception ex) {
            if(ex instanceof SOAPException) {
                throw (SOAPException)ex;
            }
            if(ex instanceof JAXBException) {
                throw (JAXBException)ex;
            }
            throw new RuntimeException(ex.toString());
        }
        
        return Addressing.createEndpointReference(to, null, refp, null, null);
    }
    
    public static void renew(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response)
            throws SOAPException, JAXBException, FaultException {
        
        final Renew renew = request.getRenew();
        if (renew == null) {
            throw new InvalidMessageFault("Missing Renew element");
        }
        
        final String identifier = request.getIdentifier();
        if (identifier == null) {
            throw new InvalidMessageFault("Missing Identifier header element");
            
        }
        
        final Object found = renewContext(initExpiration(renew.getExpires()),
                UUID.fromString(identifier));
        if (found == null) {
            /*
             * TODO: Convert to InvalidContextFault when available in
             * updated WS-Management specification
             */
            throw new InvalidMessageFault("Subscription with Identifier: " +
                    identifier + " not found");
        }
        
        response.setIdentifier(identifier);
    }
     
    public static void unsubscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response)
            throws SOAPException, JAXBException, FaultException {
        
        final Unsubscribe unsubscribe = request.getUnsubscribe();
        if (unsubscribe == null) {
            throw new InvalidMessageFault("Missing Unsubsribe element");
        }
        
        final String identifier = request.getIdentifier();
        if (identifier == null) {
            throw new InvalidMessageFault("Missing Identifier header element");
            
        }
        
        unsubscribe(identifier, handlerContext);
        
        response.setIdentifier(identifier);
    }
    
    /**
     * In some cases, Unsubscribe is due to explicit server side unsubscription
     * not related to any received request.
     */
    public static void unsubscribe(String identifier) throws FaultException {
        unsubscribe(identifier, null);
    }
    
    private static void unsubscribe(String identifier,  
            HandlerContext handlerContext) throws FaultException {
        final Object found = removeContext(handlerContext,
                UUID.fromString(identifier));
        if (found == null) {
            /*
             * TODO: Convert to InvalidContextFault when available in
             * updated WS-Management specification
             */
            throw new InvalidMessageFault("Subscription with Identifier: " +
                    identifier + " not found");
        }
    }
    
    /**
     * Create a Filter from an Eventing request
     *
     * @return Returns a Filter object if a filter exists in the request, otherwise null.
     * @throws CannotProcessFilterFault, FilteringRequestedUnavailableFault, InternalErrorFault
     */
    public static Filter createFilter(final WSEventingRequest request)
    throws CannotProcessFilterFault, FilteringRequestedUnavailableFault {
        try {
            final Subscribe subscribe = request.getSubscribe();
            final org.xmlsoap.schemas.ws._2004._08.eventing.FilterType evtFilter = subscribe.getFilter();
            final DialectableMixedDataType evtxFilter = request.getWsmanEventingFilter();
            
            if ((evtFilter == null) && (evtxFilter == null)) {
                return null;
            }
            if ((evtFilter != null) && (evtxFilter != null)) {
                // Both are not allowed. Throw an exception
                throw new CannotProcessFilterFault(
                        SOAP.createFaultDetail(
                        "Both wse:Filter and wsman:Filter were specified in the request. Only one is allowed.",
                        null, null, null));
            }
            
            final NamespaceMap nsMap = getNamespaceMap(request);
            
            if (evtxFilter != null)
                return createFilter(evtxFilter.getDialect(),
                        evtxFilter.getContent(), nsMap);
            else
                return createFilter(evtFilter.getDialect(),
                        evtFilter.getContent(), nsMap);
        } catch (SOAPException e) {
            throw new InternalErrorFault(e.getMessage());
        } catch (JAXBException e) {
            throw new InternalErrorFault(e.getMessage());
        }
    }
    
    public static NamespaceMap getNamespaceMap(final WSEventingRequest request) {
        final NamespaceMap nsMap;
        SOAPBody body;
        try {
            body = request.toSOAPMessage().getSOAPBody();
        }catch(Exception ex) {
           throw new RuntimeException(ex.toString());
        }
        
        NodeList wsmanFilter = body.getElementsByTagNameNS(EventingExtensions.FILTER.getNamespaceURI(),
                EventingExtensions.FILTER.getLocalPart());
        NodeList evtFilter = body.getElementsByTagNameNS(Eventing.FILTER.getNamespaceURI(),
                Eventing.FILTER.getLocalPart());
        if ((wsmanFilter != null) && (wsmanFilter.getLength() > 0)) {
            nsMap = new NamespaceMap(wsmanFilter.item(0));
        } else if ((evtFilter != null) && (evtFilter.getLength() > 0)) {
            nsMap = new NamespaceMap(evtFilter.item(0));
        } else {
            NodeList evtElement = body.getElementsByTagNameNS(Eventing.SUBSCRIBE.getNamespaceURI(),
                    Eventing.SUBSCRIBE.getLocalPart());
            nsMap = new NamespaceMap(evtElement.item(0));
        }
        return nsMap;
    }
    
    private synchronized static EnumerationIterator newIterator(
            final Object factory,
            final HandlerContext context,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final boolean isFiltered,
            final int queueSize) throws SOAPException, JAXBException {
        
        if (factory == null) {
            // Build a default iterator for pull
            return new EventingIterator(isFiltered, queueSize);
        } else {
            // Build a custom iterator for pull
            if(factory instanceof EventingIteratorFactory) {
                Management mgt;
                try {
                    mgt = new Management(request.toSOAPMessage());
                }catch (Exception ex) {
                   throw new SOAPException(ex.toString());
                }
                // XXX Need to make these calls using reflection
                // to remove de[endency on Eventing and EventingIteratorFactory
                Eventing evt = new Eventing(mgt);
                return ((EventingIteratorFactory)factory).newIterator(context, evt, Message.getDocumentBuilder(), true, false);
            } else {
               return ((WSEventingIteratorFactory)factory).newIterator(context, request, true, false);
            }
        }
    }
    

}
