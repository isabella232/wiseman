/*
 * Copyright 2005-2008 Sun Microsystems, Inc.
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
 ** Copyright (C) 2006-2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 ***
 *** Fudan University
 *** Author: Chuan Xiao (cxiao@fudan.edu.cn)
 ***
 **$Log: not supported by cvs2svn $
 **Revision 1.3.2.2  2008/01/28 08:00:44  denis_rachal
 **The commit adds several prototype changes to the fudan_contribution. They are described below:
 **
 **1. A new Handler interface has been added to support the newer message types WSManagementRequest & WSManagementResponse. It is called WSHandler. Additionally a new servlet WSManReflectiveServlet2 has been added to allow calling this new handler.
 **
 **2. A new base handler has been added to support creation of WS Eventing Sink handlers: WSEventingSinkHandler.
 **
 **3. WS Eventing "Source" and "Sink" test handlers have been added to the unit tests, sink_Handler & source_Handler. Both are based upon the new WSHandler interface.
 **
 **4. The EventingExtensionsTest has been updated to test "push" events. Push events are sent from a source to a sink. The sink will forward them on to and subscribers (sink subscribers). The unit test subscribes for pull events at the "sink" and then gets the "source" to send events to the "sink". The test then pulls the events from the "sink" and checks them. Does not always run, so the test needs some work. Sometimes some of the events are lost. between the source and the sink.
 **
 **5. A prototype for handling basic authentication with the sink has been added. Events from the source can now be sent to a sink using Basic authentication (credentials are specified per subscription). This needs some additional work, but basically now works.
 **
 **6. Additional methods added to the WSManagementRequest, WSManagementResponse, WSEventingRequest & WSEventingResponse, etc... interfaces to allow access to more parts of the messages.
 **
 **Additional work is neede in all of the above changes, but they are OK for a prototype in the fudan_contributaion branch.
 **
 **Revision 1.3.2.1  2008/01/18 07:08:42  denis_rachal
 **Issue number:  150
 **Obtained from:
 **Submitted by:  Chuan Xiao
 **Reviewed by:
 **Eventing with Ack added to branch. (not in main).
 **
 **Revision 1.3  2008/01/17 15:19:09  denis_rachal
 **Issue number:  151, 152, 153, 154, & 155
 **Obtained from:
 **Submitted by:  cahei (151), jfdenise (152-155)
 **Reviewed by:
 **
 **The following issues have been resolved with this commit:
 **
 **Issue 151: Eventing-Renew request returns without error for an enumeration context from Enumerate
 **Issue 152: Eventing subscription infinite expiration is not well supported
 **Issue 153: RenewResponse is not set when sending back the response
 **Issue 154: Subscribe response expires is not of the same type as the request
 **Issue 155: Expiration scheduling fails after renewal with no expires
 **
 **Unit tests have been appropriately updated to test for these issues.
 **
 **Revision 1.2  2007/11/07 11:15:35  denis_rachal
 **Issue number:  142 & 146
 **Obtained from:
 **Submitted by:
 **Reviewed by:
 **
 **142: EventingSupport.retrieveContext(UUID) throws RuntimeException
 **
 **Fixed WSEventingSupport to not throw RuntimeException. Instead it throws a new InvalidSubscriptionException. EventingSupport methods still throw RuntimeException to maintain backward compatibility.
 **
 **146: Enhance to allow specifying default expiration per enumeration
 **
 **Also enhanced WSEventingSupport to allow setting the default expiration per subscription. Default if not set by developer or client is now 24 hours for subscriptions.
 **
 **Additionally added javadoc to both EventingSupport and WSEventingSupport.
 **
 **Revision 1.1  2007/10/31 12:25:38  jfdenise
 **Split between new support and previous one.
 **
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
 * $Id: WSEventingSupport.java,v 1.3.2.3 2008-01-29 07:52:07 denis_rachal Exp $
 */

package com.sun.ws.management.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
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
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.DeliveryModeRequestedUnavailableFault;
import com.sun.ws.management.eventing.EventSourceUnableToProcessFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.eventing.InvalidSubscriptionException;
import com.sun.ws.management.server.message.WSEventingRequest;
import com.sun.ws.management.server.message.WSEventingResponse;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.HttpClient;

/**
 * A helper class that encapsulates some of the arcane logic to manage
 * subscriptions using the WS-Eventing protocol.
 */
public final class WSEventingSupport extends WSEventingBaseSupport {
	
	private static final Logger LOG = Logger.getLogger(WSEventingSupport.class.getName());
    
    /**
     * Default subscription expiration if none is specified by the client.
     * This value may be overridden by the handler when calling
     * {@link #subscribe(HandlerContext, WSEventingRequest, WSEventingResponse, boolean, int, ContextListener, Object, Duration, Duration)}
     *  or {@link #renew(HandlerContext, WSEventingRequest, WSEventingResponse, Duration, Duration)}.
     */
    public static final Duration defaultExpiration = datatypeFactory.newDuration(true, 0, 0, 1, 0, 0, 0);
    
    /**
     * Infinite subscription expiration value.
     * This value represents an infinite expiration for a subscription.
     * Any subscription expiration greater than or equal to this value is
     * considered infinite (does not expire). This value is 1 year.
     *
     */
    public static final Duration infiniteExpiration = datatypeFactory.newDuration(true, 1, 0, 0, 0, 0, 0);
    
    private WSEventingSupport() {}
    
    /**
     *  Initiate an
     * {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     * operation.
     * 
     * @param handlerContext
     *        The handler context for this request
     * @param request
     *        The incoming SOAP message that contains the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     *        request.
     * @param response
     *        The empty SOAP message that will contain the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse SubscribeResponse}.
     * @param listener
     *        Will be called when the subscription is successfully created and
     *        when deleted.
     *        
     * @return UUID that identifies the subscription created
     * 
     * @throws DatatypeConfigurationException
     * @throws SOAPException
     * @throws JAXBException
     * @throws FaultException
     */
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final ContextListener listener)
            throws DatatypeConfigurationException, SOAPException,
            JAXBException, FaultException {
        return subscribe(handlerContext, request, response, listener, null);
    }
    
    /**
     *  Initiate an
     * {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     * operation.
     * 
     * @param handlerContext
     *        The handler context for this request
     * @param request
     *        The incoming SOAP message that contains the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     *        request.
     * @param response
     *        The empty SOAP message that will contain the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse SubscribeResponse}.
     * @param listener
     *        Will be called when the subscription is successfully created and
     *        when deleted.
     * @param factory
     *        The iterator factory to use to create the iterator
     *        for pull type subscriptions.
     *        If null and this is a pull type subscription, then the default
     *        iterator EventingIterator will be created.
     *        {@link #sendEvent(UUID, Object)}
     *        may be called to add events to this iterator.
     *        
     * @return UUID that identifies the subscription created
     * 
     * @throws DatatypeConfigurationException
     * @throws SOAPException
     * @throws JAXBException
     * @throws FaultException
     */
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final ContextListener listener,
            final WSEventingIteratorFactory factory)
            throws DatatypeConfigurationException, SOAPException,
            JAXBException, FaultException {
        return subscribe(handlerContext, request, response, false, DEFAULT_QUEUE_SIZE, listener, factory, null, null);
    }
    
    /**
     *  Initiate an
     * {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     * operation.
     * 
     * @param handlerContext
     *        The handler context for this request
     * @param request
     *        The incoming SOAP message that contains the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     *        request.
     * @param response
     *        The empty SOAP message that will contain the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse SubscribeResponse}.
     * @param isFiltered
     *        Indicates that events have been pre-filtered and that this module
     *        should not filter events.
     * @param queueSize
     *        If the request is a pull type subscription and the default iterator
     *        factory is used, this indicates how large to make the queue for this iterator.
     *        If {@link #sendEvent(UUID, Object)}
     *        is called and the queue size is exceeded
     *        the oldest entry is deleted and the new entry is added at the end of the queue.
     * @param listener
     *        Will be called when the subscription is successfully created and
     *        when deleted.
     * @param factory
     *        The iterator factory to use to create the iterator
     *        for pull type subscriptions.
     *        If null and this is a pull type subscription, then the default
     *        iterator EventingIterator will be created.
     *        {@link #sendEvent(UUID, Object)}
     *        may be called to add events to this iterator.
     *        
     * @return UUID that identifies the subscription created
     * 
     * @throws DatatypeConfigurationException
     * @throws SOAPException
     * @throws JAXBException
     * @throws FaultException
     */
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final boolean isFiltered,
            final int queueSize,
            final ContextListener listener, 
            final WSEventingIteratorFactory factory)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
         return subscribe(handlerContext, request, response, isFiltered, queueSize, listener, (Object)factory, null, null);
    }
    
    /**
     *  Initiate an
     * {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     * operation.
     * 
     * @param handlerContext
     *        The handler context for this request
     * @param request
     *        The incoming SOAP message that contains the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     *        request.
     * @param response
     *        The empty SOAP message that will contain the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse SubscribeResponse}.
     * @param isFiltered
     *        Indicates that events have been pre-filtered and that this module
     *        should not filter events.
     * @param queueSize
     *        If the request is a pull type subscription and the default iterator
     *        factory is used, this indicates how large to make the queue for this iterator.
     *        If {@link #sendEvent(UUID, Object)}
     *        is called and the queue size is exceeded
     *        the oldest entry is deleted and the new entry is added at the end of the queue.
     * @param listener
     *        Will be called when the subscription is successfully created and
     *        when deleted.
     * @param factory
     *        The iterator factory to use to create the iterator
     *        for pull type subscriptions.
     *        If null and this is a pull type subscription, then the default
     *        iterator EventingIterator will be created.
     *        {@link #sendEvent(UUID, Object)}
     *        may be called to add events to this iterator.
     *        
     * @return UUID that identifies the subscription created
     * 
     * @throws DatatypeConfigurationException
     * @throws SOAPException
     * @throws JAXBException
     * @throws FaultException
     */
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final boolean isFiltered,
            final int queueSize,
            final ContextListener listener,
            final Object factory)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
    	return subscribe(handlerContext, request, response, isFiltered, queueSize, listener, factory, null, null);
    }
    
    /**
     *  Initiate an
     * {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     * operation.
     * If a client does not specify an expiration the default expiration will
     * be used. To disable expiration completely when a client does not specify
     * an expiration, set both the <tt>defExpiration</tt>
     * and <tt>maxExpiration</tt> to {@link #infiniteExpiration} or greater.
     * 
     * @param handlerContext
     *        The handler context for this request
     * @param request
     *        The incoming SOAP message that contains the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe Subscribe}
     *        request.
     * @param response
     *        The empty SOAP message that will contain the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse SubscribeResponse}.
     * @param isFiltered
     *        Indicates that events have been pre-filtered and that this module
     *        should not filter events.
     * @param queueSize
     *        If the request is a pull type subscription and the default iterator
     *        factory is used, this indicates how large to make the queue for this iterator.
     *        If {@link #sendEvent(UUID, Object)}
     *        is called and the queue size is exceeded
     *        the oldest entry is deleted and the new entry is added at the end of the queue.
     * @param listener
     *        Will be called when the subscription is successfully created and
     *        when deleted.
     * @param factory
     *        The iterator factory to use to create the iterator
     *        for pull type subscriptions.
     *        If null and this is a pull type subscription, then the default
     *        iterator EventingIterator will be created.
     *        {@link #sendEvent(UUID, Object)}
     *        may be called to add events to this iterator.
     * @param defExpiration 
     *        The default expiration for this subscription
     *        if none is specified by the request.
     *        If null the system default of 24 hours is used.           
     * @param maxExpiration
     *        The maximum value a client is allowed to set the Expiration to.
     *        If the requested Expiration exceeds this value, it will be set
     *        to this value. If null infinity is the maximum.
     *        {@link #infiniteExpiration}
     *                     
     * @return UUID that identifies the subscription created
     * 
     * @throws DatatypeConfigurationException
     * @throws SOAPException
     * @throws JAXBException
     * @throws FaultException
     */
    public static UUID subscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final boolean isFiltered,
            final int queueSize,
            final ContextListener listener,
            final Object factory,
            final Duration defExpiration,
            final Duration maxExpiration)
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
            
        	final int sizeOfQueue = (queueSize <= 0) ? (DEFAULT_QUEUE_SIZE) : queueSize;
            EnumerationIterator iterator = newIterator(factory, handlerContext,
                    request,
                    response,
                    isFiltered,
                    sizeOfQueue);

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
        	
        	// Use the WSEventingSupport default if none is supplied.
            final Duration subscriptionDefault = 
            	(null == defExpiration) ? defaultExpiration : defExpiration;
            
        	final String expires = computeExpiration(subscribe.getExpires(),
                                                     subscriptionDefault,
                                                     maxExpiration);
        	
        	final XMLGregorianCalendar expiration = (expires == null) ? null : initExpiration(expires);
        	
            // create and register an EventingContextPull
            final EventingContextPull ctx = new EventingContextPull(expiration,
                                                                    filter,
                                                                    null,
                                                                    iterator,
                                                                    listener);
            
            // Set single thread use of this context
            synchronized (ctx) {
                final UUID context = initContext(handlerContext, ctx);
                final EndpointReferenceType mgrEPR = 
                	createSubscriptionManagerEpr(request, response, context);
                response.setSubscribeResponse(mgrEPR, 
                		expires, createEnumerationContextElement(context));
                return context;
            }
            
        }  else { // including Push, PushWithAck, and Batched Delivery Mode 
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
                if (JAXBElement.class.equals(content.getClass())) {
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
            
        	// Use the WSEventingSupport default if none is supplied.
            
            final Duration subscriptionDefault = 
            	(null == defExpiration) ? defaultExpiration : defExpiration;
            
        	final String expires = computeExpiration(subscribe.getExpires(),
                                                     subscriptionDefault,
                                                     maxExpiration);
        	
        	final XMLGregorianCalendar expiration = (expires == null) ? null : initExpiration(expires);

            final EventingContext ctx;
            if (deliveryMode.equals(Eventing.PUSH_DELIVERY_MODE)) {
            	// Push Mode
            	
            	ctx = new EventingContext(expiration,
                                          filter,
                                          notifyTo,
                                          listener);
            } else if (deliveryMode.equals(EventingExtensions.PUSH_WITH_ACK_DELIVERY_MODE)) {
            	// PushWithAck Mode
            	
            	// Here we use the default eventReplyTo and operationTimeout.
            	// Customers will set explicitly eventReplyTo and 
            	// operationTimeout using the setSendXXX(UUID,xxx)
            	// after this subscribe(...) was called. 
            	ctx = new EventingContextWithAck (expiration,
            			                          filter,
            			                          notifyTo,
            			                          listener);
            	
            } else if (deliveryMode.equals(EventingExtensions.EVENTS_DELIVERY_MODE)) {
            	// Batched Delivery Mode
            
            	// TODO: get the MaxElements and MaxTime from the variable delivery 
            	//      then use them as the parameters of the constructor.  
                // int maxElements;
                // Duration maxTime;
            	
            	// Here we use the default eventReplyTo,default operationTimeout,
            	// default maxElements,default maxTime. After the Batched
            	// Delivery Mode is supported, the maxElements and the maxTime
            	// should be set in the EventingContextBatched constructor.
            	// But the eventReplyTo and operationTimeout are set explicitly using
            	// the setSendXXX(UUID,xxx) after this subscribe(...) was called.
            	// because the eventReplyTo and operationTimeout are not in the
            	// Subscribe Action request.
                ctx = new EventingContextBatched(expiration,
                		                         filter,
                		                         notifyTo,
                		                         listener);  
            } else {
            	// Not one of the supported delivery modes
            	throw new DeliveryModeRequestedUnavailableFault(SUPPORTED_DELIVERY_MODES);
            }
            
            final UUID context = initContext(handlerContext, ctx);
            response.setSubscribeResponse(createSubscriptionManagerEpr(request,
                    response, context), expires);
            return context;
        }
     }
    
    private static JAXBElement<EnumerationContextType> createEnumerationContextElement(final Object context) {
        final EnumerationContextType contextType = Enumeration.FACTORY.createEnumerationContextType();
        contextType.getContent().add(context.toString());

        return new JAXBElement<EnumerationContextType>(Enumeration.ENUMERATION_CONTEXT,
                		EnumerationContextType.class, null, contextType);
    }
     
    /**
     * Create a subscription Manager EPR for the given subscription request.
     * 
     * @param request subscription request
     * @param response response object
     * @param context subscription context
     * 
     * @return EPR to the subscription manager of this subscription
     * 
     * @throws SOAPException
     * @throws JAXBException
     */
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
		} catch (URISyntaxException e) {
			// This should never happen, but if it does throw a RuntimeException
			throw new RuntimeException(e);
		}
        
        return Addressing.createEndpointReference(to, null, refp, null, null);
    }
    
    /**
     * Renews an existing subscription. The subscription must still be
     * active and not already canceled.
     * 
     * @param handlerContext
     *        The handler context for this request
     * @param request
     *      The incoming SOAP message that contains the
     *      {@link org.xmlsoap.schemas.ws._2004._08.eventing.Renew Renew}
     *      request.
     * @param response
     *        The empty SOAP message that will contain the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.RenewResponse RenewResponse}.
     *        
     * @throws SOAPException
     * @throws JAXBException
     * @throws InvalidMessageFault if the subscription does not exist
     */
    public static void renew(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response)
            throws SOAPException, JAXBException, InvalidMessageFault {
    	renew(handlerContext, request, response, null, null);
    }
    
    /**
     * Renews an existing subscription. The subscription must still be
     * active and not already canceled.
     * 
     * If a client does not specify an expiration the default expiration will
     * be used. To disable expiration completely when a client does not specify
     * an expiration, set both the <tt>defExpiration</tt>
     * and <tt>maxExpiration</tt> to {@link #infiniteExpiration} or greater.
     * 
     * @param handlerContext
     *        The handler context for this request
     * @param request
     *      The incoming SOAP message that contains the
     *      {@link org.xmlsoap.schemas.ws._2004._08.eventing.Renew Renew}
     *      request.
     * @param response
     *        The empty SOAP message that will contain the
     *        {@link org.xmlsoap.schemas.ws._2004._08.eventing.RenewResponse RenewResponse}.
     * @param defExpiration 
     *        The default expiration for this subscription
     *        if none is specified by the request.
     *        If null the system default of 24 hours is used.           
     * @param maxExpiration
     *        The maximum value a client is allowed to set the Expiration to.
     *        If the requested Expiration exceeds this value, it will be set
     *        to this value. If null infinity is the maximum.
     *        {@link #infiniteExpiration}
     *        
     * @throws SOAPException
     * @throws JAXBException
     * @throws InvalidMessageFault if the subscription does not exist
     */
    public static void renew(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response,
            final Duration defExpiration,
            final Duration maxExpiration)
            throws SOAPException, JAXBException, InvalidMessageFault {
        
        final Renew renew = request.getRenew();
        if (renew == null) {
            throw new InvalidMessageFault("Missing Renew element");
        }
        
        final String identifier = request.getIdentifier();
		if (identifier == null) {
			throw new InvalidMessageFault("Missing Identifier header element");
		}

        final UUID uuid = UUID.fromString(identifier);
        final BaseContext context = getContext(uuid);
        if ((context == null) || 
        		(!(context instanceof EventingContext) &&
        				!(context instanceof EventingContextPull))) {
            /*
             * TODO: Convert to InvalidContextFault when available in
             * updated WS-Management specification
             */
            throw new InvalidMessageFault("Subscription with Identifier: " +
                    identifier + " not found");
        }
        
    	// Use the WSEventingSupport default if none is supplied.
        final Duration subscriptionDefault = 
        	(null == defExpiration) ? defaultExpiration : defExpiration;
        
    	final String expires = computeExpiration(renew.getExpires(),
                                                 subscriptionDefault,
                                                 maxExpiration);
    	
    	final XMLGregorianCalendar expiration = (expires == null) ? null : initExpiration(expires);
    	
        final Object found = renewContext(expiration, uuid);
        if (found == null) {
            /*
             * TODO: Convert to InvalidContextFault when available in
             * updated WS-Management specification
             */
            throw new InvalidMessageFault("Subscription with Identifier: " +
                    identifier + " not found");
        }
    	
        response.setRenewResponse(expires);
    }

	/**
     * Unsubscribe an existing subscription. This method cancels
     * an existing subscription.
     * 
     * @param handlerContext
     *        The handler context for this request
     * @param request
     *      The incoming SOAP message that contains the
     *      {@link org.xmlsoap.schemas.ws._2004._08.eventing.Unsubscribe Unsubscribe}
     *      request.
     * @param response
     *        The empty SOAP message that will contain the unsubscribe response.
     *        
     * @throws SOAPException
     * @throws JAXBException
     * @throws InvalidMessageFault if the subscription does not exist
     */
    public static void unsubscribe(final HandlerContext handlerContext,
            final WSEventingRequest request,
            final WSEventingResponse response)
            throws SOAPException, JAXBException, InvalidMessageFault {
        
        final Unsubscribe unsubscribe = request.getUnsubscribe();
        if (unsubscribe == null) {
            throw new InvalidMessageFault("Missing Unsubsribe element");
        }
        
        final String identifier = request.getIdentifier();
        if (identifier == null) {
            throw new InvalidMessageFault("Missing Identifier header element");
            
        }
        
        unsubscribe(identifier, handlerContext);
    }
    
    /**
     * Unsubscribe an existing subscription. This method cancels
     * an existing subscription.
     * 
     * @param identifier
     *        UUID that identifies the subscription to cancel.
     *        
     * @throws SOAPException
     * @throws JAXBException
     * @throws InvalidMessageFault if the subscription does not exist
     */
    public static void unsubscribe(String identifier) throws InvalidMessageFault {
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
    
    /**
     * Returns a namespace map in the context of the SOAP Body
     * of the specified request.
     * 
     * @param request Eventing request to build the namespace map from
     * 
     * @return the namespace map
     */
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
    
    /**
     * Sets the username/password credentials for the subscription.
     * This is a workaround until this class supports security profiles
     * in the subscription.
     * 
     * @param uuid UUID identifying the subscription.
     * @param username username
     * @param password password
     */
    public static void setCredentials(final UUID uuid, final byte[] username, final byte[] password) {
        final BaseContext context = getContext(uuid);
        if ((context == null) || 
        		(!(context instanceof EventingContext))) {
            /*
             * TODO: Convert to InvalidContextFault when available in
             * updated WS-Management specification
             */
            throw new InvalidMessageFault("Subscription with Identifier: " +
                    uuid.toString() + " not found");
        }
        ((EventingContext)context).setCredentials(username, password);
    }
    
    /**
     * Send an event for a specified subscription id. If the subscription
     * is of type pull the event will be added to the pull iterator.
     * If the subscription is of type push the event will be sent
     * immidiately to the subscriber.
     * NOTE: For push this method currently blocks until the event
     *       has been successfully delivered to the client.
     * 
     * @param id UUID identifying the subscription this event is for
     * @param content the event to send to the subscriber
     * 
     * @return true if the event was successfully queued or delivered,
     *         otherwise false
     * 
     * @throws SOAPException
     * @throws JAXBException
     * @throws IOException
     * @throws InvalidSubscriptionException 
     */
    public static boolean sendEvent(final UUID id,
    		                        final Object content)
    throws SOAPException, JAXBException, IOException,
			InvalidSubscriptionException {

		// TODO: avoid blocking the sender - use a thread pool to send notifications
    	// TODO: implement retries for push
		final BaseContext bctx = retrieveContext(id);

		boolean result = false;

		if (bctx instanceof EventingContextPull) {
			// Pull, add data to iterator
			final EventingContextPull ctx = (EventingContextPull) bctx;
			final EventingIterator iterator = 
				(EventingIterator) ctx.getIterator();
			synchronized (iterator) {
				if (iterator != null) {
					result = iterator.add(new EnumerationItem(content, null));
					iterator.notifyAll();
				}
			}
		} else if (bctx instanceof EventingContextBatched) {
			// Batched delivery mode
			final EventingContextBatched ctxbatched = (EventingContextBatched) bctx;

			// TODO: Here we
			// Put the content into the related event-queue which was
			// initialized when the EventingContextBatched instance is created.
			// that is, the event-queue will be one private member of the
			// EventingContextBatched instance.
			// In the event-queue, one thread will pack multiple contents into
			// one soap message and send the soap to the
			// ctxbatched.getEventReplyTo().
			// Because this is one asynchronized operation, we can not get the
			// result in time,
			// when the content is put into the event-queue successfully, the
			// result is set to true, else is set to false.

		} else if (bctx instanceof EventingContextWithAck) {
			// Push with acknowledge delivery mode
			final EventingContextWithAck ctxwithack = (EventingContextWithAck) bctx;

			final Addressing eventMessage =
				createEventMessagePushWithAck(ctxwithack, content);

			if (eventMessage != null) {
				final Addressing response = HttpClient.sendRequest(
						eventMessage, (Map.Entry<String, String>[])null);

				result = parseEventResponse(response);

				if (!result)
					WSEventingSupport.unsubscribe(id.toString());
			}
		} else if (bctx instanceof EventingContext) {
			// Standard Push mode, send the data
			final EventingContext ctx = (EventingContext)bctx;
			final Addressing msg = createPushEventMessage(bctx, content);
			
			if (msg != null) {
				int rc = 0;
				if ((ctx.getUsername() != null) && (ctx.getPassword() != null))
					rc = HttpClient.sendResponse(msg, ctx.getUsername(), ctx.getPassword());
				else
					rc = HttpClient.sendResponse(msg);
				LOG.fine("Event sent. RC=" + rc);
				result = ((rc >= 200) && (rc < 300));
			}
		} else {
			// Not an Eventing subscription. Throw an exception.
			throw new InvalidSubscriptionException();
		}
		return result;
	}

	/**
	 * Send an event for a specified subscription context.
	 * 
	 * @param id
	 *            UUID identifying the subscription this event is for
	 * @param msg
	 *            message to use when sending the event
	 * 
	 * @return true is the event was successfully sent, otherwise false
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws InvalidSubscriptionException
	 * @throws Exception
	 */
    public static boolean sendEvent(final UUID id,
    		                        final Addressing msg)
            throws SOAPException, JAXBException, IOException, 
                   XPathExpressionException, InvalidSubscriptionException, Exception {
        
    	// TODO: XXX REVISIT: Change interface to use "WSAddressingRequest msg".
        // TODO: avoid blocking the sender - use a thread pool to send notifications
        assert datatypeFactory != null : UNINITIALIZED;
        
        final BaseContext bctx = retrieveContext(id);

        if (!(bctx instanceof EventingContext)) {
            throw new InvalidSubscriptionException("Eventing subscription context invalid");
        }
        final EventingContext ctx = (EventingContext) bctx;
        
        final GregorianCalendar now = new GregorianCalendar();
        final XMLGregorianCalendar nowXml = datatypeFactory.newXMLGregorianCalendar(now);
        if (ctx.isExpired(nowXml)) {
            removeContext(null, id);
            throw new InvalidSubscriptionException("Subscription expired");
        }
        
        // the filter is only applied to the first child in soap body
        if (ctx.getFilter() != null) {
            final Node content = msg.getBody().getFirstChild();
            if (ctx.evaluate(content) == null)
                return false;
        }
        
        final EndpointReferenceType notifyTo = ctx.getNotifyTo();
        msg.setTo(notifyTo.getAddress().getValue());
        final ReferenceParametersType refparams = notifyTo.getReferenceParameters();
        if (refparams != null) {
            msg.addHeaders(refparams);
        }
        final ReferencePropertiesType refprops = notifyTo.getReferenceProperties();
        if (refprops != null) {
            msg.addHeaders(refprops);
        }
        msg.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
        HttpClient.sendResponse(msg);
        return true;
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
                // to remove dependency on Eventing and EventingIteratorFactory
                Eventing evt = new Eventing(mgt);
                return ((EventingIteratorFactory)factory).newIterator(context, evt, Message.getDocumentBuilder(), true, false);
            } else {
               return ((WSEventingIteratorFactory)factory).newIterator(context, request, true, false);
            }
        }
    }
    
    /**
	 * Parse the event response from Event Sink.
	 * If the eventResponse message is one ACK message , then true;
	 * else return false 
	 * 
	 * @param eventResponse
	 * @return
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 */
	private static boolean parseEventResponse(final Addressing eventResponse)
			throws SOAPException, JAXBException, IOException {
		if (eventResponse.getAction().equals(Management.ACK_URI))
			return true;
		else
			return false;
	}

	/**
	 * Set EventReplyTo in class of EventingContextWithAck.
	 * This includes EventingContextBatched.
	 * 
	 * @param id UUID identifying the subscription this event is for
	 * @param eventReplyTo
	 */
	public static void setSendReplyTo(final UUID id,
			final EndpointReferenceType eventReplyTo)
			throws InvalidSubscriptionException {
		final BaseContext bctx = retrieveContext(id);
		if (bctx instanceof EventingContextWithAck) {
			EventingContextWithAck ctxwithack = (EventingContextWithAck) bctx;
			ctxwithack.setEventReplyTo(eventReplyTo);
		} else
			throw new IllegalArgumentException(
					"Subscription does not have an option named EventReplyTo");
	}

	/**
	 * Set OperationTimeout in class of EventingContextWithAck.
	 * This includes EventingContextBatched.
	 * 
	 * @param id UUID identifying the subscription this event is for
	 * @param operationTimeout
	 */
	public static void setSendOperationTimeout(final UUID id,
			final Duration operationTimeout)
			throws InvalidSubscriptionException {
		final BaseContext bctx = retrieveContext(id);
		if (bctx instanceof EventingContext) {
			EventingContextWithAck ctxwithack = (EventingContextWithAck) bctx;
			ctxwithack.setOperationTimeout(operationTimeout);
		} else
			throw new IllegalArgumentException(
					"Subscription does not have an option named OperationTimeout");
	}

	/**
	 * Set MaxElements in class of EventingContextBatched.
	 * 
	 * @param id UUID identifying the subscription this event is for
	 * @param maxElements
	 */
	public static void setSendMaxElements(final UUID id, final int maxElements)
			throws InvalidSubscriptionException {
		final BaseContext bctx = retrieveContext(id);
		if (bctx instanceof EventingContextBatched) {
			EventingContextBatched ctxbatched = (EventingContextBatched) bctx;
			ctxbatched.setMaxElements(maxElements);
		} else
			throw new IllegalArgumentException(
					"Subscription does not have an option named MaxElements");
	}

	/**
	 * Set MaxTime in class of EventingContextBatched.
	 * 
	 * @param id UUID identifying the subscription this event is for
	 * @param maxTime
	 */
	public static void setSendMaxTime(final UUID id, final Duration maxTime)
			throws InvalidSubscriptionException {
		final BaseContext bctx = retrieveContext(id);
		if (bctx instanceof EventingContextBatched) {
			EventingContextBatched ctxbatched = (EventingContextBatched) bctx;
			ctxbatched.setMaxTime(maxTime);
		} else
			throw new IllegalArgumentException(
					"Subscription does not have an option named MaxTime");
	}
}