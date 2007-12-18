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
 **Revision 1.29  2007/11/07 11:15:35  denis_rachal
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
 **Revision 1.28  2007/10/31 12:25:17  jfdenise
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
 * $Id: EventingSupport.java,v 1.30 2007-12-18 14:56:10 jfdenise Exp $
 */

package com.sun.ws.management.server;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.eventing.InvalidSubscriptionException;
import com.sun.ws.management.server.message.SAAJMessage;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transport.HttpClient;

/**
 * A helper class that encapsulates some of the arcane logic to manage
 * subscriptions using the WS-Eventing protocol.
 */
public final class EventingSupport extends WSEventingBaseSupport {
    
    private static Map<String, EventingIteratorFactory> registeredIterators =
            new HashMap<String, EventingIteratorFactory>();
    
    private EventingSupport() {}
    
    // the EventingExtensions.PULL_DELIVERY_MODE is handled by
    // EnumerationSupport
    public static UUID subscribe(final HandlerContext handlerContext,
            final Eventing request,
            final Eventing response,
            final ContextListener listener)
            throws DatatypeConfigurationException, SOAPException,
            JAXBException, FaultException {
        return subscribe(handlerContext, request, response, listener, null);
    }
    
    // the EventingExtensions.PULL_DELIVERY_MODE is handled by
    // EnumerationSupport
    public static UUID subscribe(final HandlerContext handlerContext,
            final Eventing request,
            final Eventing response,
            final ContextListener listener, final EventingIteratorFactory factory)
            throws DatatypeConfigurationException, SOAPException,
            JAXBException, FaultException {
        return subscribe(handlerContext, request, response, false, DEFAULT_QUEUE_SIZE, listener, factory);
    }
    
     public static UUID subscribe(final HandlerContext handlerContext,
            final Eventing request,
            final Eventing response,
            final boolean isFiltered,
            final int queueSize,
            final ContextListener listener, 
            EventingIteratorFactory factory)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
         SAAJMessage msg = new SAAJMessage(new Management(request));
         SAAJMessage resp = new SAAJMessage(new Management(response));
         return WSEventingSupport.subscribe(handlerContext, msg, resp, isFiltered, queueSize, listener, factory);
     }
    
    public static UUID subscribe(final HandlerContext handlerContext,
            final Eventing request,
            final Eventing response,
            final boolean isFiltered,
            final int queueSize,
            final ContextListener listener)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
         return subscribe(handlerContext, request, response,isFiltered,queueSize,listener, null);
    }
    
    public static EndpointReferenceType createSubscriptionManagerEpr(
            final Eventing request, final Eventing response,
            final Object context) throws SOAPException, JAXBException {
         SAAJMessage msg = new SAAJMessage(new Management(request));
         SAAJMessage resp = new SAAJMessage(new Management(response));
         return WSEventingSupport.createSubscriptionManagerEpr(msg, resp,context);
     }
     
    public static void renew(final HandlerContext handlerContext,
            final Eventing request,
            final Eventing response)
            throws SOAPException, JAXBException, FaultException {
         SAAJMessage msg = new SAAJMessage(new Management(request));
         SAAJMessage resp = new SAAJMessage(new Management(response));
         WSEventingSupport.renew(handlerContext, msg, resp);
    }
    
    public static void unsubscribe(final HandlerContext handlerContext,
            final Eventing request,
            final Eventing response)
            throws SOAPException, JAXBException, FaultException {
          SAAJMessage msg = new SAAJMessage(new Management(request));
         SAAJMessage resp = new SAAJMessage(new Management(response));
         WSEventingSupport.unsubscribe(handlerContext, msg, resp);
    }
    
    // TODO: avoid blocking the sender - use a thread pool to send notifications
    public static boolean sendEvent(final Object context, final Addressing msg,
            final NamespaceMap nsMap)
            throws SOAPException, JAXBException, IOException, XPathExpressionException, Exception {
        
        assert datatypeFactory != null : UNINITIALIZED;
        
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
            removeContext(null, context);
            throw new RuntimeException("Subscription expired");
        }
        
        // the filter is only applied to the first child in soap body
        if (ctx.getFilter() != null) {
            final Node content = msg.getBody().getFirstChild();
    	try {
                if (ctx.evaluate(content) == null)
                    return false;
            } catch (XPathExpressionException ex) {
                throw ex;
    		}
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
    
    /**
     * Create a Filter from an Eventing request
     *
     * @return Returns a Filter object if a filter exists in the request, otherwise null.
     * @throws CannotProcessFilterFault, FilteringRequestedUnavailableFault, InternalErrorFault
     */
    public static Filter createFilter(final Eventing request)
    throws CannotProcessFilterFault, FilteringRequestedUnavailableFault {
        SAAJMessage msg;
        try {
            msg = new SAAJMessage(new Management(request));
        } catch (SOAPException ex) {
            throw new InternalErrorFault(ex.getMessage());
        }
        return WSEventingSupport.createFilter(msg);
    }
    
    public static NamespaceMap getNamespaceMap(final Eventing request) {
        SAAJMessage msg;
        try {
            msg = new SAAJMessage(new Management(request));
        } catch (SOAPException ex) {
            throw new InternalErrorFault(ex.getMessage());
        }
         return WSEventingSupport.getNamespaceMap(msg);
    }
    
    /**
     * Add an iterator factory to EnumerationSupport.
     *
     * @param resourceURI ResourceURI for which this iterator factory
     * is to be used to fufill Enumeration requests.
     * @param iteratorFactory The Iterator Factory that creates <code>EnumerationIterator</code>
     * objects that are used by EnumerationSupport to fufill Enumeration requests.
     * If a factory is already registered it will be overwritten with the specified
     * factory.
     */
    public synchronized static void registerIteratorFactory(String resourceURI,
            EventingIteratorFactory iteratorFactory) throws Exception {
        registeredIterators.put(resourceURI, iteratorFactory);
    }
    
    /**
     * Gets an IteratorFactory for the specified resource URI.
     *
     * @param resourceURI the URI associated with the IteratorFactory
     * @return the IteratorFactory if one is registered, otherwise null
     */
    public synchronized static EventingIteratorFactory getIteratorFactory(String resourceURI) {
        return registeredIterators.get(resourceURI);
    }
       
    //  TODO: avoid blocking the sender - use a thread pool to send notifications
    public static boolean sendEvent(UUID id, Object content)
    throws SOAPException, JAXBException, IOException {
        
        BaseContext bctx = retrieveContext(id);
        boolean result = false;
        
        if (bctx instanceof EnumerationContext) { // while pull delivery mode
            // Pull, add data to iterator
            final EnumerationContext ctx = (EnumerationContext) bctx;
            final EventingIterator iterator = (EventingIterator) ctx.getIterator();
            synchronized (iterator) {
                if (iterator != null) {
                    result = iterator.add(new EnumerationItem(content, null));
                    iterator.notifyAll();
}
            }
        } else if (bctx instanceof EventingContextBatched  ) { // while Batched delivery mode 
 
            final EventingContextBatched  ctxbatched = (EventingContextBatched) bctx; 

           // TODO:Here we
           // Put the content into the related event-queue which was initialized when the EventingContextBatched instance is created.
           // that is, the event-queue will be one private member of the EventingContextBatched instance.
           // In the event-queue, one thread will pack multiple contents into one soap message and send the soap to the ctxbatched.getEventReplyTo().
           // Because this is one asynchronized operation, we can not get the result in time,
           // when the content is put into the event-queue successfully, the result is set to true, else is set to false.
            
         } else if (bctx instanceof EventingContextWithAck ) { // while PushWithAck delivery mode 
             final EventingContextWithAck  ctxwithack = (EventingContextWithAck) bctx; 
 
             Addressing eventMessage = createEventMessagePushWithAck(ctxwithack,content);
             	
              if (eventMessage == null)	return false;

              Addressing response = HttpClient.sendRequest(eventMessage,ctxwithack.getNotifyTo().getAddress().toString());
              
              result = parseEventResponse(response);   
 
              if (!result) WSEventingSupport.unsubscribe(id.toString());
 
         } else if (bctx instanceof EventingContext){ // while push delivery mode
        	 
             final Addressing msg = createPushEventMessage(bctx, content);
            if(msg == null)
                result = false;
            else {
                HttpClient.sendResponse(msg);
                result = true;
            }
        } else {
            // Invalid context. Delete the subscription & throw an InvalidSubscriptionException.
        	WSEventingSupport.unsubscribe(id.toString());
        	
        	//TODO : throw an InvalidSubscriptionException.
        } 
        return result;
    } 

    
    //	TODO: parse the event ACK response.
    private static boolean parseEventResponse(Addressing eventResponse)
    throws SOAPException, JAXBException, IOException{
    	if(eventResponse.getAction().equals(Management.ACK_URI)) 
    		return true;
    	else if(eventResponse.getAction().equals(Management.FAULT_ACTION_URI))
    		return false;
    	else return false;
    }

}
