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
 * $Id: EventingExtensions.java,v 1.3 2006-02-01 21:50:35 akhilarora Exp $
 */

package com.sun.ws.management.eventing;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.eventing.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2005._06.management.BookmarkType;
import org.xmlsoap.schemas.ws._2005._06.management.ConnectionRetryType;
import org.xmlsoap.schemas.ws._2005._06.management.MaxEnvelopeSizeType;
import org.xmlsoap.schemas.ws._2005._06.management.ObjectFactory;

public class EventingExtensions extends Eventing {
    
    public static final String EVENT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Event";
    public static final String HEARTBEAT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Heartbeat";
    public static final String ACK_ACTION_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Ack";
    public static final String DROPPED_EVENTS_ACTION_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/DroppedEvents";
    
    public static final String PUSH_WITH_ACK_DELIVERY_MODE = "http://schemas.xmlsoap.org/ws/2005/06/management/PushWithAck";
    public static final String EVENTS_DELIVERY_MODE = "http://schemas.xmlsoap.org/ws/2005/06/management/Events";
    public static final String PULL_DELIVERY_MODE = "http://schemas.xmlsoap.org/ws/2005/06/management/Pull";
    
    public static final String EARLIEST_BOOKMARK = "http://schemas.xmlsoap.org/ws/2005/06/management/bookmark/earliest";
    
    public static final String CANCEL_SUBSCRIPTION_POLICY = "CancelSubscription";
    public static final String SKIP_POLICY = "Skip";
    public static final String NOTIFY_POLICY = "Notify";
    
    public static final QName CONNECTION_RETRY = new QName(Management.NS_URI, "ConnectionRetry", Management.NS_PREFIX);
    public static final QName HEARTBEATS = new QName(Management.NS_URI, "Heartbeats", Management.NS_PREFIX);
    public static final QName SEND_BOOKMARKS = new QName(Management.NS_URI, "SendBookmarks", Management.NS_PREFIX);
    public static final QName BOOKMARK = new QName(Management.NS_URI, "Bookmark", Management.NS_PREFIX);
    public static final QName MAX_ELEMENTS = new QName(Management.NS_URI, "MaxElements", Management.NS_PREFIX);
    public static final QName MAX_TIME = new QName(Management.NS_URI, "MaxTime", Management.NS_PREFIX);
    public static final QName EVENTS = new QName(Management.NS_URI, "Events", Management.NS_PREFIX);
    public static final QName EVENT = new QName(Management.NS_URI, "Event", Management.NS_PREFIX);
    public static final QName ACTION = new QName(Management.NS_URI, "Action", Management.NS_PREFIX);
    public static final QName ACK_REQUESTED = new QName(Management.NS_URI, "AckRequested", Management.NS_PREFIX);
    public static final QName DROPPED_EVENTS = new QName(Management.NS_URI, "DroppedEvents", Management.NS_PREFIX);
    
    public static final ObjectFactory FACTORY = new ObjectFactory();
    
    public EventingExtensions() throws SOAPException, JAXBException {
        super();
    }
    
    public EventingExtensions(final Addressing addr) throws SOAPException, JAXBException {
        super(addr);
    }
    
    public EventingExtensions(final InputStream is) throws SOAPException, JAXBException, IOException {
        super(is);
    }
    
    public void setSubscribe(final EndpointReferenceType endTo, final String deliveryMode,
            final EndpointReferenceType notifyTo, final String expires, final FilterType filter,
            final ConnectionRetryType retryType, final Duration heartbeats, final Boolean sendBookmarks,
            final BookmarkType bookmark,
            final MaxEnvelopeSizeType maxEnvelopeSize, final Long maxElements, final Duration maxTime)
            throws SOAPException, JAXBException {
        
        Element retryElement = null;
        if (retryType != null) {
            final Document retryDoc = newDocument();
            getXmlBinding().marshal(FACTORY.createConnectionRetry(retryType), retryDoc);
            retryElement = retryDoc.getDocumentElement();
        }
        
        Element heartbeatsElement = null;
        if (heartbeats != null) {
            final Document heartbeatsDoc = newDocument();
            getXmlBinding().marshal(FACTORY.createHeartbeats(heartbeats), heartbeatsDoc);
            heartbeatsElement = heartbeatsDoc.getDocumentElement();
        }
        
        Element sendBookmarksElement = null;
        if (sendBookmarks != null && sendBookmarks.booleanValue()) {
            final Document sendBookmarksDoc = newDocument();
            // TODO: any parameter generates extra xsi attributes as below -
            // a null is the least annoying. How to eliminate?
            // xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"
            getXmlBinding().marshal(FACTORY.createSendBookmarks(null), sendBookmarksDoc);
            sendBookmarksElement = sendBookmarksDoc.getDocumentElement();
        }
        
        Element bookmarkElement = null;
        if (bookmark != null) {
            final Document bookmarkDoc = newDocument();
            getXmlBinding().marshal(FACTORY.createBookmark(bookmark), bookmarkDoc);
            bookmarkElement = bookmarkDoc.getDocumentElement();
        }
        
        Element maxEnvelopeSizeElement = null;
        if (maxEnvelopeSize != null) {
            final Document maxEnvelopeSizeDoc = newDocument();
            getXmlBinding().marshal(FACTORY.createMaxEnvelopeSize(maxEnvelopeSize), maxEnvelopeSizeDoc);
            maxEnvelopeSizeElement = maxEnvelopeSizeDoc.getDocumentElement();
        }
        
        Element maxElementsElement = null;
        if (maxElements != null) {
            final Document maxElementsDoc = newDocument();
            getXmlBinding().marshal(FACTORY.createMaxElements(maxElements), maxElementsDoc);
            maxElementsElement = maxElementsDoc.getDocumentElement();
        }
        
        Element maxTimeElement = null;
        if (maxTime != null) {
            final Document maxTimeDoc = newDocument();
            getXmlBinding().marshal(FACTORY.createMaxTime(maxTime), maxTimeDoc);
            maxTimeElement = maxTimeDoc.getDocumentElement();
        }
        
        super.setSubscribe(endTo, deliveryMode, notifyTo, expires, filter,
                retryElement, heartbeatsElement, sendBookmarksElement, bookmarkElement,
                maxEnvelopeSizeElement, maxElementsElement, maxTimeElement);
    }
    
    public void setSubscribeResponse(final EndpointReferenceType mgr, final String expires,
            final Object context)
            throws SOAPException, JAXBException {
        
        final EnumerationContextType contextType = Enumeration.FACTORY.createEnumerationContextType();
        contextType.getContent().add(context);
        // TODO: this should have been generated by JAXB as - createEnumerationContextType(contextType);
        final JAXBElement<EnumerationContextType> contextTypeElement = 
                new JAXBElement<EnumerationContextType>(Enumeration.ENUMERATION_CONTEXT, EnumerationContextType.class, null, contextType);
        super.setSubscribeResponse(mgr, expires, contextTypeElement);
    }
}
