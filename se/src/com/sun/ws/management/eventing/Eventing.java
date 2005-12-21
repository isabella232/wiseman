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
 * $Id: Eventing.java,v 1.4 2005-12-21 23:48:05 akhilarora Exp $
 */

package com.sun.ws.management.eventing;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.xml.XML;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.eventing.DeliveryType;
import org.xmlsoap.schemas.ws._2004._08.eventing.FilterType;
import org.xmlsoap.schemas.ws._2004._08.eventing.GetStatus;
import org.xmlsoap.schemas.ws._2004._08.eventing.GetStatusResponse;
import org.xmlsoap.schemas.ws._2004._08.eventing.LanguageSpecificStringType;
import org.xmlsoap.schemas.ws._2004._08.eventing.ObjectFactory;
import org.xmlsoap.schemas.ws._2004._08.eventing.Renew;
import org.xmlsoap.schemas.ws._2004._08.eventing.RenewResponse;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscribeResponse;
import org.xmlsoap.schemas.ws._2004._08.eventing.SubscriptionEnd;
import org.xmlsoap.schemas.ws._2004._08.eventing.Unsubscribe;

public class Eventing extends Addressing {
    
    public static final String NS_PREFIX = "wse";
    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing";
    
    public static final String SUBSCRIBE_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe";
    public static final String SUBSCRIBE_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscribeResponse";
    public static final String RENEW_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Renew";
    public static final String RENEW_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/RenewResponse";
    public static final String GET_STATUS_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatus";
    public static final String GET_STATUS_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatusResponse";
    public static final String UNSUBSCRIBE_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe";
    public static final String UNSUBSCRIBE_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/UnsubscribeResponse";
    public static final String SUBSCRIPTION_END_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscriptionEnd";
    
    public static final String PUSH_DELIVERY_MODE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/DeliveryModes/Push";
    
    public static final String DELIVERY_FAILURE_STATUS = "http://schemas.xmlsoap.org/ws/2004/08/eventing/DeliveryFailure";
    public static final String SOURCE_SHUTTING_DOWN_STATUS = "http://schemas.xmlsoap.org/ws/2004/08/eventing/SourceShuttingDown";
    public static final String SOURCE_CANCELING_STATUS = "http://schemas.xmlsoap.org/ws/2004/08/eventing/SourceCanceling";
    
    public static final QName DELIVERY_MODE_REQUESTED_UNAVAILABLE = new QName(NS_URI, "DeliveryModeRequestedUnavailable", NS_PREFIX);
    public static final String DELIVERY_MODE_REQUESTED_UNAVAILABLE_REASON =
            "The requested delivery mode is not supported.";
    public static final QName SUPPORTED_DELIVERY_MODE = new QName(NS_URI, "SupportedDeliveryMode", NS_PREFIX);
    
    public static final QName EVENT_SOURCE_UNABLE_TO_PROCESS = new QName(NS_URI, "EventSourceUnableToProcess", NS_PREFIX);
    public static final String EVENT_SOURCE_UNABLE_TO_PROCESS_REASON =
            "The event source cannot process the subscription.";
    
    public static final QName FILTERING_NOT_SUPPORTED = new QName(NS_URI, "FilteringNotSupported", NS_PREFIX);
    public static final String FILTERING_NOT_SUPPORTED_REASON =
            "Filtering over the event source is not supported.";
    
    public static final QName FILTERING_REQUESTED_UNAVAILABLE = new QName(NS_URI, "FilteringRequestedUnavailable", NS_PREFIX);
    public static final String FILTERING_REQUESTED_UNAVAILABLE_REASON =
            "The requested filter dialect is not supported.";
    public static final QName SUPPORTED_DIALECT = new QName(NS_URI, "SupportedDialect", NS_PREFIX);
    
    public static final QName INVALID_EXPIRATION_TIME = new QName(NS_URI, "InvalidExpirationTime", NS_PREFIX);
    public static final String INVALID_EXPIRATION_TIME_REASON =
            "Invalid expiration time.";
    
    public static final QName INVALID_MESSAGE = new QName(NS_URI, "InvalidMessage", NS_PREFIX);
    public static final String INVALID_MESSAGE_REASON =
            "The request message had unknown or invalid content and could not be processed.";
    
    public static final QName UNABLE_TO_RENEW = new QName(NS_URI, "UnableToRenew", NS_PREFIX);
    public static final String UNABLE_TO_RENEW_REASON =
            "The subscription could not be renewed.";
    
    public static final QName UNSUPPORTED_EXPIRATION_TYPE = new QName(NS_URI, "UnsupportedExpirationType", NS_PREFIX);
    public static final String UNSUPPORTED_EXPIRATION_TYPE_REASON =
            "The specified expiration type is not supported.";
    
    public static final QName SUBSCRIBE = new QName(NS_URI, "Subscribe", NS_PREFIX);
    public static final QName SUBSCRIBE_RESPONSE = new QName(NS_URI, "SubscribeResponse", NS_PREFIX);
    public static final QName RENEW = new QName(NS_URI, "Renew", NS_PREFIX);
    public static final QName RENEW_RESPONSE = new QName(NS_URI, "RenewResponse", NS_PREFIX);
    public static final QName GET_STATUS = new QName(NS_URI, "GetStatus", NS_PREFIX);
    public static final QName GET_STATUS_RESPONSE = new QName(NS_URI, "GetStatusResponse", NS_PREFIX);
    public static final QName UNSUBSCRIBE = new QName(NS_URI, "Unsubscribe", NS_PREFIX);
    public static final QName SUBSCRIPTION_END = new QName(NS_URI, "SubscriptionEnd", NS_PREFIX);
    public static final QName IDENTIFIER = new QName(NS_URI, "Identifier", NS_PREFIX);
    public static final QName NOTIFY_TO = new QName(NS_URI, "NotifyTo", NS_PREFIX);
    
    private ObjectFactory objectFactory = null;
    
    public Eventing() throws SOAPException, JAXBException {
        super();
        init();
    }
    
    public Eventing(final Addressing addr) throws SOAPException, JAXBException {
        super(addr);
        init();
    }
    
    public Eventing(final InputStream is) throws SOAPException, JAXBException, IOException {
        super(is);
        init();
    }
    
    private void init() throws SOAPException, JAXBException {
        objectFactory = new ObjectFactory();
    }
    
    public void setSubscribe(final EndpointReferenceType endTo, final String deliveryMode,
            final EndpointReferenceType notifyTo, final String expires, final FilterType filter,
            final Element... extensions)
            throws SOAPException, JAXBException {
        
        removeChildren(getBody(), SUBSCRIBE);
        final Subscribe sub = objectFactory.createSubscribe();
        
        if (endTo != null) {
            sub.setEndTo(endTo);
        }
        
        final DeliveryType delivery = objectFactory.createDeliveryType();
        
        if (deliveryMode != null) {
            delivery.setMode(deliveryMode);
        }
        
        if (notifyTo != null) {
            final Document doc = newDocument();
            final Element notifyElement = doc.createElementNS(
                    NOTIFY_TO.getNamespaceURI(),
                    NOTIFY_TO.getPrefix() + COLON +
                    NOTIFY_TO.getLocalPart());
            doc.appendChild(notifyElement);
            getXmlBinding().marshal(notifyTo, notifyElement);
            delivery.getContent().add(doc.getDocumentElement());
        }
        
        if (extensions != null) {
            for (final Element ext : extensions) {
                delivery.getContent().add(ext);
            }
        }
        
        sub.setDelivery(delivery);
        
        if (expires != null) {
            sub.setExpires(expires);
        }
        
        if (filter != null) {
            sub.setFilter(filter);
        }
        
        getXmlBinding().marshal(sub, getBody());
    }
    
    public void setSubscribeResponse(final EndpointReferenceType mgr, final String expires,
            final Object... extensions)
            throws SOAPException, JAXBException {
        
        removeChildren(getBody(), SUBSCRIBE_RESPONSE);
        final SubscribeResponse response = objectFactory.createSubscribeResponse();
        response.setSubscriptionManager(mgr);
        response.setExpires(expires);
        if (extensions != null) {
            for (final Object ext : extensions) {
                response.getAny().add(ext);
            }
        }
        getXmlBinding().marshal(response, getBody());
    }
    
    public void setRenew(final String expires) throws SOAPException, JAXBException {
        removeChildren(getBody(), RENEW);
        final Renew renew = objectFactory.createRenew();
        renew.setExpires(expires.trim());
        getXmlBinding().marshal(renew, getBody());
    }
    
    public void setRenewResponse(final String expires) throws SOAPException, JAXBException {
        removeChildren(getBody(), RENEW_RESPONSE);
        final RenewResponse response = objectFactory.createRenewResponse();
        response.setExpires(expires);
        getXmlBinding().marshal(response, getBody());
    }
    
    public void setGetStatus() throws SOAPException, JAXBException {
        removeChildren(getBody(), GET_STATUS);
        final GetStatus status = objectFactory.createGetStatus();
        getXmlBinding().marshal(status, getBody());
    }
    
    public void setGetStatusResponse(final String expires) throws SOAPException, JAXBException {
        removeChildren(getBody(), GET_STATUS_RESPONSE);
        final GetStatusResponse response = objectFactory.createGetStatusResponse();
        response.setExpires(expires);
        getXmlBinding().marshal(response, getBody());
    }
    
    public void setUnsubscribe() throws SOAPException, JAXBException {
        removeChildren(getBody(), UNSUBSCRIBE);
        final Unsubscribe unsub = objectFactory.createUnsubscribe();
        getXmlBinding().marshal(unsub, getBody());
    }
    
    public void setSubscriptionEnd(final EndpointReferenceType mgr,
            final String status, final String reason) throws SOAPException, JAXBException {
        
        if (!DELIVERY_FAILURE_STATUS.equals(status) &&
                !SOURCE_SHUTTING_DOWN_STATUS.equals(status) &&
                !SOURCE_CANCELING_STATUS.equals(status)) {
            throw new IllegalArgumentException("Status must be one of " +
                    DELIVERY_FAILURE_STATUS + ", " +
                    SOURCE_SHUTTING_DOWN_STATUS + " or " +
                    SOURCE_CANCELING_STATUS);
        }
        
        removeChildren(getBody(), SUBSCRIPTION_END);
        final SubscriptionEnd end = objectFactory.createSubscriptionEnd();
        end.setSubscriptionManager(mgr);
        end.setStatus(status);
        
        if (reason != null) {
            final LanguageSpecificStringType localizedReason = objectFactory.createLanguageSpecificStringType();
            localizedReason.setLang(XML.DEFAULT_LANG);
            localizedReason.setValue(reason);
            end.getReason().add(localizedReason);
        }
        
        getXmlBinding().marshal(end, getBody());
    }
    
    public Subscribe getSubscribe() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), SUBSCRIBE);
        return value == null ? null : (Subscribe) value;
    }
    
    public SubscribeResponse getSubscribeResponse() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), SUBSCRIBE_RESPONSE);
        return value == null ? null : (SubscribeResponse) value;
    }
    
    public Renew getRenew() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), RENEW);
        return value == null ? null : (Renew) value;
    }
    
    public RenewResponse getRenewResponse() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), RENEW_RESPONSE);
        return value == null ? null : (RenewResponse) value;
    }
    
    public GetStatus getGetStatus() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), GET_STATUS);
        return value == null ? null : (GetStatus) value;
    }
    
    public GetStatusResponse getGetStatusResponse() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), GET_STATUS_RESPONSE);
        return value == null ? null : (GetStatusResponse) value;
    }
    
    public Unsubscribe getUnsubscribe() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), UNSUBSCRIBE);
        return value == null ? null : (Unsubscribe) value;
    }
    
    public SubscriptionEnd getSubscriptionEnd() throws JAXBException, SOAPException {
        final Object value = unbind(getBody(), SUBSCRIPTION_END);
        return value == null ? null : (SubscriptionEnd) value;
    }
}
