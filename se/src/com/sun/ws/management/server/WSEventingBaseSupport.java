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
 ***
 *** Fudan University
 *** Author: Chuan Xiao (cxiao@fudan.edu.cn)
 ***
 **$Log: not supported by cvs2svn $
 **Revision 1.4.2.2  2008/01/28 08:00:44  denis_rachal
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
 **Revision 1.4.2.1  2008/01/18 07:08:43  denis_rachal
 **Issue number:  150
 **Obtained from:
 **Submitted by:  Chuan Xiao
 **Reviewed by:
 **Eventing with Ack added to branch. (not in main).
 **
 **Revision 1.4  2007/12/20 20:47:52  jfdenise
 **Removal of ACK contribution. The contribution has been commited in the trunk instead of the branch.
 **
 **Revision 1.2  2007/11/07 11:15:36  denis_rachal
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
 * $Id: WSEventingBaseSupport.java,v 1.4.2.3 2008-02-01 21:01:34 denis_rachal Exp $
 */

package com.sun.ws.management.server;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathException;

import org.dmtf.schemas.wbem.wsman._1.wsman.EventsType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.InvalidSubscriptionException;
import com.sun.ws.management.server.message.WSEventingRequest;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XmlBinding;

/**
 * A helper class that encapsulates some of the arcane logic to manage
 * subscriptions using the WS-Eventing protocol.
 */
public class WSEventingBaseSupport extends BaseSupport {
    
    public static final int DEFAULT_QUEUE_SIZE = 1024;
    public static final int DEFAULT_EXPIRATION_MILLIS = 60000;
    
    public static final String[] SUPPORTED_DELIVERY_MODES = {
        Eventing.PUSH_DELIVERY_MODE,
        EventingExtensions.PULL_DELIVERY_MODE,
        EventingExtensions.PUSH_WITH_ACK_DELIVERY_MODE,
        EventingExtensions.EVENTS_DELIVERY_MODE
    };
    
    protected WSEventingBaseSupport() {}
    
    public static String[] getSupportedDeliveryModes() {
        return SUPPORTED_DELIVERY_MODES;
    }
    
    public static boolean isDeliveryModeSupported(final String deliveryMode) {
        for (final String mode : SUPPORTED_DELIVERY_MODES) {
            if (mode.equals(deliveryMode)) {
                return true;
            }
        }
        return false;
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
    
    protected static BaseContext retrieveContext(UUID id) 
                     throws InvalidSubscriptionException {
        assert datatypeFactory != null : UNINITIALIZED;
        
        BaseContext bctx = contextMap.get(id);
        if ((bctx == null) || (bctx.isDeleted())) {
            throw new InvalidSubscriptionException("Context not found: subscription does not exist");
        }
        
        // Check if context is expired
        final GregorianCalendar now = new GregorianCalendar();
        final XMLGregorianCalendar nowXml = datatypeFactory.newXMLGregorianCalendar(now);
        if (bctx.isExpired(nowXml)) {
            removeContext(null, bctx);
            throw new InvalidSubscriptionException("Subscription expired");
        }
        return bctx;
    }
    
    /**
     * Filters the event object. If object passes the filter is will be
     * returned as a Document, Element, or JAXBElement<MixedDataType>,
     * otherwise null is returned.
     * 
     * @param ctx
     * @param content
     * @param binding
     * @return null if filter does not pass,
     * 		   otherwise a Document, Element, or JAXBElement<MixedDataType>
     * @throws SOAPException 
     */
    public static Object filterEvent(final BaseContext ctx,
    		                         final Object content,
    		                         XmlBinding binding) throws SOAPException {
    	
    	if (ctx.getFilter() == null)
			return content;
    	
        final Element item;
        
        // Convert the content to an Element
        if (content instanceof Element) {
            item = (Element) content;
        } else if (content instanceof Document) {
            item = ((Document) content).getDocumentElement();
            // append the Element to the owner document
            // if it has not been done
            // this is critical for XPath filtering to work
            final Document owner = item.getOwnerDocument();
            if (owner.getDocumentElement() == null) {
                owner.appendChild(item);
            }
        } else {
            final Document doc = Management.newDocument();
            try {
            	if (binding == null) {
            		// TODO: Reuse the binding from somewhere? This is very expensive!
            		binding = new XmlBinding(null);
            	}
                binding.marshal(content, doc);
            } catch (Exception e) {
                removeContext(null, ctx);
                final String explanation = "XML Binding marshall failed for object of type: "
                        + content.getClass().getName();
                throw new InternalErrorFault(SOAP
                        .createFaultDetail(explanation, null,
                        e, null));
            }
            item = doc.getDocumentElement();
        }
        final NodeList filteredContent;
        try {
            filteredContent = ctx.evaluate(item);
        } catch (XPathException xpx) {
            removeContext(null, ctx);
            throw new CannotProcessFilterFault(
                    "Error evaluating XPath: "
                    + xpx.getMessage());
        } catch (Exception ex) {
            removeContext(null, ctx);
            throw new CannotProcessFilterFault(
                    "Error evaluating Filter: "
                    + ex.getMessage());
        }
        if ((filteredContent != null) && (filteredContent.getLength() > 0)) {
            // Then send this instance
            if (filteredContent.item(0).equals(item)) {
                // Whole node was selected
                return item;
            } else {
                // Fragment(s) selected
                final JAXBElement<MixedDataType> fragment = createXmlFragment(filteredContent);
                return fragment;
            }
        } else {
            return null;
        }
    }
     
    public static Addressing createEventPushMessage(final EventingContext ctx,
			final Object content) throws SOAPException, JAXBException,
			IOException, InvalidSubscriptionException {
		// Push mode, send the data
		final Addressing msg = new Addressing();
		msg.setAction(Management.EVENT_URI);
		msg.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);

		if (content == null)
			return null;

		if (content instanceof Document) {
			msg.getBody().addDocument((Document) content);
		} else {
			msg.getXmlBinding().marshal(content, msg.getBody());
		}
		final EndpointReferenceType notifyTo = ctx.getNotifyTo();
		msg.setTo(notifyTo.getAddress().getValue());
		final ReferenceParametersType refparams = notifyTo
				.getReferenceParameters();
		if (refparams != null) {
			msg.addHeaders(refparams);
		}
		final ReferencePropertiesType refprops = notifyTo
				.getReferenceProperties();
		if (refprops != null) {
			msg.addHeaders(refprops);
		}
		msg.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

		return msg;
	}
    
    /**
     * Retrieve the Context associated to passed ID, then create
     * the WS-Man request for the provided content.
     */
    public static Addressing createEventPushMessage(UUID id, Object content)
    throws SOAPException, JAXBException, IOException, InvalidSubscriptionException {
        
        final BaseContext bctx = retrieveContext(id);
        if (bctx instanceof EventingContext)
            return createEventPushMessage((EventingContext)bctx, content);
        else
        	throw new InvalidSubscriptionException("ID is not a for a push eveting subscription.");
    }
    
    public static Addressing createEventMessageBatched(
			final EventingContextBatched ctx,
			final EventsType events)
    	throws SOAPException, JAXBException {

		final Management mgmt = new Management();
		mgmt.setAction(Management.EVENTS_URI);
		mgmt.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
		mgmt.setReplyTo(ctx.getEventReplyTo());
		mgmt.setTimeout(ctx.getOperationTimeout());

		final EndpointReferenceType notifyTo = ctx.getNotifyTo();
		mgmt.setTo(notifyTo.getAddress().getValue());
		final ReferenceParametersType refparams = notifyTo
				.getReferenceParameters();
		if (refparams != null) {
			mgmt.addHeaders(refparams);
		}
		final ReferencePropertiesType refprops = notifyTo
				.getReferenceProperties();
		if (refprops != null) {
			mgmt.addHeaders(refprops);
		}

		final EventingExtensions evtx = new EventingExtensions(mgmt);
		
		evtx.setAckRequested();
		evtx.setBatchedEvents(events);

		return evtx;
	}

	public static Addressing createEventMessageBatched(final UUID id,
													   final EventsType events) 
		throws InvalidSubscriptionException, SOAPException, JAXBException {

		Addressing wseReq = null;

		final BaseContext bctx = retrieveContext(id);
		if (bctx instanceof EventingContextBatched) {
			final EventingContextBatched ctxbatched = (EventingContextBatched) bctx;
			try {
				wseReq = createEventMessageBatched(ctxbatched, events);
			} catch (SOAPException e) {
				removeContext(null, ctxbatched);
				throw e;
			} catch (JAXBException e) {
				removeContext(null, ctxbatched);
				throw e;
			}
		}
		return wseReq;
	}
    
	/**
	 * Create the WS-Man event message for the provided content.
	 * 
	 * @param ctx
	 * @param content
	 * @return
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws InvalidSubscriptionException
	 */
	public static Addressing createEventMessagePushWithAck(
			final EventingContextWithAck ctx, final Object content)
			throws SOAPException, JAXBException, IOException,
			InvalidSubscriptionException {
		final Addressing msg = createEventPushMessage(ctx, content);
		
		// Check if it has been filtered out
		if (msg == null)
			return null;
		
		final Management mgmt = new Management(msg);
		mgmt.setReplyTo(ctx.getEventReplyTo());
		mgmt.setTimeout(ctx.getOperationTimeout());
		
		// Set the AckRequested header
		final EventingExtensions evt = new EventingExtensions(mgmt);
		evt.setAckRequested();

		return evt;
	}  
}
