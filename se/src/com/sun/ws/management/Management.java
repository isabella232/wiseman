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
 * $Id: Management.java,v 1.3 2005-11-08 22:40:19 akhilarora Exp $
 */

package com.sun.ws.management;

import com.sun.ws.management.addressing.Addressing;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2005._06.management.LocaleType;
import org.xmlsoap.schemas.ws._2005._06.management.MaxEnvelopeSizeType;
import org.xmlsoap.schemas.ws._2005._06.management.ObjectFactory;
import org.xmlsoap.schemas.ws._2005._06.management.OptionSetType;
import org.xmlsoap.schemas.ws._2005._06.management.OptionType;
import org.xmlsoap.schemas.ws._2005._06.management.RenameType;
import org.xmlsoap.schemas.ws._2005._06.management.ResourceURIType;
import org.xmlsoap.schemas.ws._2005._06.management.SelectorSetType;
import org.xmlsoap.schemas.ws._2005._06.management.SelectorType;

public class Management extends Addressing {
    
    public static final String NS_PREFIX = "wsman";
    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2005/06/management";
    
    public static final String RENAME_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Rename";
    public static final String RENAME_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/RenameResponse";
    public static final String EVENTS_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Events";
    public static final String HEARTBEAT_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Heartbeat";
    public static final String DROPPED_EVENTS_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/DroppedEvents";
    public static final String ACK_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Ack";
    public static final String EVENT_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Event";
    public static final String BOOKMARK_EARLIEST_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/bookmark/earliest";
    public static final String PUSH_WITH_ACK_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/PushWithAck";
    public static final String PULL_URI = "http://schemas.xmlsoap.org/ws/2005/06/management/Pull";
    
    public static final QName ACCESS_DENIED = new QName(NS_URI, "AccessDenied", NS_PREFIX);
    public static final String ACCESS_DENIED_REASON =
            "The sender was not authorized to access the resource.";
    
    public static final QName NO_ACK = new QName(NS_URI, "NoAck", NS_PREFIX);
    public static final String NO_ACK_REASON =
            "The receiver did not acknowledge the event delivery.";
    
    public static final QName CONCURRENCY = new QName(NS_URI, "Concurrency", NS_PREFIX);
    public static final String CONCURRENCY_REASON =
            "The action could not be completed due to concurrency or locking problems.";
    
    public static final QName ALREADY_EXISTS = new QName(NS_URI, "AlreadyExists", NS_PREFIX);
    public static final String ALREADY_EXISTS_REASON =
            "The sender attempted to create a resource which already exists.";
    
    public static final QName DELIVERY_REFUSED = new QName(NS_URI, "DeliveryRefused", NS_PREFIX);
    public static final String DELIVERY_REFUSED_REASON =
            "The receiver refuses to accept delivery of events and requests that the subscription be canceled.";
    
    public static final QName ENCODING_LIMIT = new QName(NS_URI, "EncodingLimit", NS_PREFIX);
    public static final String ENCODING_LIMIT_REASON =
            "An internal encoding limit was exceeded in a request or would be violated if the message were processed.";
    
    public static final QName INCOMPATIBLE_EPR = new QName(NS_URI, "IncompatibleEPR", NS_PREFIX);
    public static final String INCOMPATIBLE_EPR_REASON =
            "The EPR format used is not supported.";
    
    public static final QName INTERNAL_ERROR = new QName(NS_URI, "InternalError", NS_PREFIX);
    public static final String INTERNAL_ERROR_REASON =
            "The service cannot comply with the request due to internal processing errors.";
    
    public static final QName INVALID_BOOKMARK = new QName(NS_URI, "InvalidBookmark", NS_PREFIX);
    public static final String INVALID_BOOKMARK_REASON =
            "The bookmark supplied with the subscription is not valid.";
    
    public static final QName INVALID_OPTIONS = new QName(NS_URI, "InvalidOptions", NS_PREFIX);
    public static final String INVALID_OPTIONS_REASON =
            "One or more options were not valid.";
    
    public static final QName INVALID_PARAMETER = new QName(NS_URI, "InvalidParameter", NS_PREFIX);
    public static final String INVALID_PARAMETER_REASON =
            "An operation parameter was not valid.";
    
    public static final QName INVALID_SELECTORS = new QName(NS_URI, "InvalidSelectors", NS_PREFIX);
    public static final String INVALID_SELECTORS_REASON =
            "The Selectors for the resource were not valid.";
    
    public static final QName INVALID_SYSTEM = new QName(NS_URI, "InvalidSystem", NS_PREFIX);
    public static final String INVALID_SYSTEM_REASON =
            "A valid wsman:System URI is required.";
    
    public static final QName METADATA_REDIRECT = new QName(NS_URI, "MetadataRedirect", NS_PREFIX);
    public static final String METADATA_REDIRECT_REASON =
            "The requested metadata is not available at the current address.";
    
    public static final QName QUOTA_LIMIT = new QName(NS_URI, "QuotaLimit", NS_PREFIX);
    public static final String QUOTA_LIMIT_REASON =
            "The service is busy servicing other requests.";
    
    public static final QName RENAME_FAILURE = new QName(NS_URI, "RenameFailure", NS_PREFIX);
    public static final String RENAME_FAILURE_REASON =
            "The Selectors for the resource were not valid.";
    
    public static final QName SCHEMA_VALIDATION_ERROR = new QName(NS_URI, "SchemaValidationError", NS_PREFIX);
    public static final String SCHEMA_VALIDATION_ERROR_REASON =
            "The supplied SOAP violates the corresponding XML Schema definition.";
    
    public static final QName TIMED_OUT = new QName(NS_URI, "TimedOut", NS_PREFIX);
    public static final String TIMED_OUT_REASON =
            "The operation has timed out.";
    
    public static final QName UNSUPPORTED_FEATURE = new QName(NS_URI, "UnsupportedFeature", NS_PREFIX);
    public static final String UNSUPPORTED_FEATURE_REASON =
            "The specified feature is not supported.";
    
    public static final String INSUFFICIENT_SELECTORS_DETAIL = "wsman:faultDetail/InsufficientSelectors";
    public static final String UNEXPECTED_SELECTORS_DETAIL = "wsman:faultDetail/UnexpectedSelectors";
    public static final String TYPE_MISMATCH_DETAIL = "wsman:faultDetail/TypeMismatch";
    public static final String INVALID_VALUE_DETAIL = "wsman:faultDetail/InvalidValue";
    public static final String AMBIGUOUS_SELECTORS_DETAIL = "wsman:faultDetail/AmbiguousSelectors";
    public static final String DUPLICATE_SELECTORS_DETAIL = "wsman:faultDetail/DuplicateSelectors";
    public static final String INVALID_SYSTEM_DETAIL = "wsman:faultDetail/InvalidSystem";
    public static final String INVALID_RESOURCE_URI_DETAIL = "wsman:faultDetail/InvalidResourceURI";
    public static final String FILTERING_REQUIRED_DETAIL = "wsman:faultDetail/FilteringRequired";
    public static final String URI_LIMIT_EXCEEDED_DETAIL = "wsman:faultDetail/URILimitExceeded";
    public static final String MIN_ENVELOPE_LIMIT_DETAIL = "wsman:faultDetail/MinimumEnvelopeLimit";
    public static final String MAX_ENVELOPE_SIZE_DETAIL = "wsman:faultDetail/MaxEnvelopeSize";
    public static final String MAX_ENVELOPE_SIZE_EXCEEDED_DETAIL = "wsman:faultDetail/MaxEnvelopeSizeExceeded";
    public static final String SERVICE_ENVELOPE_LIMIT_DETAIL = "wsman:faultDetail/ServiceEnvelopeLimit";
    public static final String SELECTOR_LIMIT_DETAIL = "wsman:faultDetail/SelectorLimit";
    public static final String OPTION_LIMIT_DETAIL = "wsman:faultDetail/OptionLimit";
    public static final String CHARACTER_SET_DETAIL = "wsman:faultDetail/CharacterSet";
    public static final String UNREPORTABLE_SUCCESS_DETAIL = "wsman:faultDetail/UnreportableSuccess";
    public static final String WHITESPACE_DETAIL = "wsman:faultDetail/Whitespace";
    public static final String ENCODING_TYPE_DETAIL = "wsman:faultDetail/EncodingType";
    public static final String EXPIRED_DETAIL = "wsman:faultDetail/Expired";
    public static final String INVALID_DETAIL = "wsman:faultDetail/Invalid";
    public static final String NOT_SUPPORTED_DETAIL = "wsman:faultDetail/NotSupported";
    public static final String INVALID_NAME_DETAIL = "wsman:faultDetail/InvalidName";
    public static final String INVALID_VALUES_DETAIL = "wsman:faultDetail/InvalidValues";
    public static final String MISSING_VALUES_DETAIL = "wsman:faultDetail/MissingValues";
    public static final String INVALID_NAMESPACE_DETAIL = "wsman:faultDetail/InvalidNamespace";
    public static final String INVALID_FRAGMENT_DETAIL = "wsman:faultDetail/InvalidFragment";
    public static final String WRONG_NAMESPACE_DETAIL = "wsman:faultDetail/WrongNamespace";
    public static final String MISSING_SYSTEM_DETAIL = "wsman:faultDetail/MissingSystem";
    public static final String TARGET_ALREADY_EXISTS_DETAIL = "wsman:faultDetail/TargetAlreadyExists";
    public static final String INVALID_ADDRESS_DETAIL = "wsman:faultDetail/InvalidAddress";
    public static final String INVALID_SELECTOR_ASSIGNMENT_DETAIL = "wsman:faultDetail/InvalidSelectorAssignment";
    public static final String AUTHORIZATION_MODE_DETAIL = "wsman:faultDetail/AuthorizationMode";
    public static final String ADDRESSING_MODE_DETAIL = "wsman:faultDetail/AddressingMode";
    public static final String ACK_DETAIL = "wsman:faultDetail/Ack";
    public static final String OPERATION_TIMEOUT_DETAIL = "wsman:faultDetail/OperationTimeout";
    public static final String LOCALE_DETAIL = "wsman:faultDetail/Locale";
    public static final String EXPIRATION_TIME_DETAIL = "wsman:faultDetail/ExpirationTime";
    public static final String FRAGMENT_LEVEL_ACCESS_DETAIL = "wsman:faultDetail/FragmentLevelAccess";
    public static final String REPLAY_DETAIL = "wsman:faultDetail/Replay";
    public static final String DELIVERY_RETRIES_DETAIL = "wsman:faultDetail/DeliveryRetries";
    public static final String HEARTBEATS_DETAIL = "wsman:faultDetail/Heartbeats";
    public static final String BOOKMARKS_DETAIL = "wsman:faultDetail/Bookmarks";
    public static final String MAX_ELEMENTS_DETAIL = "wsman:faultDetail/MaxElements";
    public static final String MAX_TIME_DETAIL = "wsman:faultDetail/MaxTime";
    public static final String MAX_ENVELOPE_POLICY_DETAIL = "wsman:faultDetail/MaxEnvelopePolicy";
    public static final String INSECURE_ADDRESS_DETAIL = "wsman:faultDetail/InsecureAddress";
    public static final String FORMAT_MISMATCH_DETAIL = "wsman:faultDetail/FormatMismatch";
    public static final String FORMAT_SECURITY_TOKEN_DETAIL = "wsman:faultDetail/FormatSecurityToken";
    
    public static final QName RESOURCE_URI = new QName(NS_URI, "ResourceURI", NS_PREFIX);
    public static final QName OPERATION_TIMEOUT = new QName(NS_URI, "OperationTimeout", NS_PREFIX);
    public static final QName SELECTOR_SET = new QName(NS_URI, "SelectorSet", NS_PREFIX);
    public static final QName OPTION_SET = new QName(NS_URI, "OptionSet", NS_PREFIX);
    public static final QName MAX_ENVELOPE_SIZE = new QName(NS_URI, "MaxEnvelopeSize", NS_PREFIX);
    public static final QName LOCALE = new QName(NS_URI, "Locale", NS_PREFIX);
    public static final QName RENAME = new QName(NS_URI, "Rename", NS_PREFIX);
    public static final QName FAULT_DETAIL = new QName(NS_URI, "FaultDetail", NS_PREFIX);
    public static final QName URL = new QName(NS_URI, "URL", NS_PREFIX);
    public static final QName ENDPOINT_REFERENCE = new QName(NS_URI, "EndpointReference", NS_PREFIX);
    
    private ObjectFactory objectFactory = null;
    
    public Management() throws SOAPException, JAXBException {
        super();
        init();
    }
    
    public Management(final Addressing addr) throws SOAPException, JAXBException {
        super(addr);
        init();
    }
    
    public Management(final InputStream is) throws SOAPException, JAXBException, IOException {
        super(is);
        init();
    }
    
    private void init() throws SOAPException, JAXBException {
        objectFactory = new ObjectFactory();
    }
    
    // setters
    
    public void setResourceURI(final String resource) throws JAXBException, SOAPException {
        removeChildren(getHeader(), RESOURCE_URI);
        final ResourceURIType resType = objectFactory.createResourceURIType();
        resType.setValue(resource);
        final JAXBElement<ResourceURIType> resTypeElement =
                objectFactory.createResourceURI(resType);
        getXmlBinding().marshal(resTypeElement, getHeader());
    }
    
    public void setTimeout(final Duration duration) throws JAXBException, SOAPException {
        removeChildren(getHeader(), OPERATION_TIMEOUT);
        final JAXBElement<Duration> durationElement = objectFactory.createOperationTimeout(duration);
        getXmlBinding().marshal(durationElement, getHeader());
    }
    
    public void setSelectors(final Map<String, Object> selectors) throws JAXBException, SOAPException {
        removeChildren(getHeader(), SELECTOR_SET);
        final SelectorSetType selectorSet = objectFactory.createSelectorSetType();
        final Iterator<String> ki = selectors.keySet().iterator();
        while (ki.hasNext()) {
            final String key = ki.next();
            final SelectorType selector = objectFactory.createSelectorType();
            selector.setName(key);
            selector.getContent().add(selectors.get(key));
            selectorSet.getSelector().add(selector);
        }
        final JAXBElement<SelectorSetType> selectorSetElement = objectFactory.createSelectorSet(selectorSet);
        getXmlBinding().marshal(selectorSetElement, getHeader());
    }
    
    public void setMaxEnvelopeSize(final MaxEnvelopeSizeType size) throws JAXBException, SOAPException {
        removeChildren(getHeader(), MAX_ENVELOPE_SIZE);
        final JAXBElement<MaxEnvelopeSizeType> sizeElement = objectFactory.createMaxEnvelopeSize(size);
        getXmlBinding().marshal(sizeElement, getHeader());
    }
    
    public void setLocale(final LocaleType locale) throws JAXBException, SOAPException {
        removeChildren(getHeader(), LOCALE);
        final JAXBElement<LocaleType> localeElelment = objectFactory.createLocale(locale);
        getXmlBinding().marshal(localeElelment, getHeader());
    }
    
    public void setOptions(final Map<String, String> options) throws JAXBException, SOAPException {
        removeChildren(getHeader(), OPTION_SET);
        final OptionSetType optionSet = objectFactory.createOptionSetType();
        final Iterator<String> ki = options.keySet().iterator();
        while (ki.hasNext()) {
            final String key = ki.next();
            final OptionType option = objectFactory.createOptionType();
            option.setName(key);
            option.setValue(options.get(key));
            optionSet.getOption().add(option);
        }
        final JAXBElement<OptionSetType> optionSetElement = objectFactory.createOptionSet(optionSet);
        getXmlBinding().marshal(optionSetElement, getHeader());
    }
    
    public void setRename(final EndpointReferenceType epr) throws JAXBException, SOAPException {
        removeChildren(getBody(), RENAME);
        final RenameType rename = objectFactory.createRenameType();
        rename.getAny().add(wrapEndpointReference(epr));
        final JAXBElement<RenameType> renameElement = objectFactory.createRename(rename);
        getXmlBinding().marshal(renameElement, getBody());
    }
    
    // getters
    
    public String getResourceURI() throws JAXBException, SOAPException {
        Object value = unbind(getHeader(), RESOURCE_URI);
        return value == null ? null : ((JAXBElement<ResourceURIType>) value).getValue().getValue();
    }
    
    public Duration getTimeout() throws JAXBException, SOAPException {
        Object value = unbind(getHeader(), OPERATION_TIMEOUT);
        return value == null ? null : ((JAXBElement<Duration>) value).getValue();
    }
    
    public Map<String, Object> getSelectors() throws JAXBException, SOAPException {
        Object value = unbind(getHeader(), SELECTOR_SET);
        if (value == null) {
            return null;
        }
        final List<SelectorType> selectors = ((JAXBElement<SelectorSetType>) value).getValue().getSelector();
        final Map<String, Object> selectorMap = new TreeMap();
        final Iterator<SelectorType> si = selectors.iterator();
        while (si.hasNext()) {
            final SelectorType st = si.next();
            selectorMap.put(st.getName(), st.getContent().get(0));
        }
        return selectorMap;
    }
    
    public Map<String, String> getOptions() throws JAXBException, SOAPException {
        Object value = unbind(getHeader(), OPTION_SET);
        if (value == null) {
            return null;
        }
        final List<OptionType> options = ((JAXBElement<OptionSetType>) value).getValue().getOption();
        final Map<String, String> optionMap = new TreeMap();
        final Iterator<OptionType> oi = options.iterator();
        while (oi.hasNext()) {
            final OptionType ot = oi.next();
            optionMap.put(ot.getName(), ot.getValue());
        }
        return optionMap;
    }
    
    public MaxEnvelopeSizeType getMaxEnvelopeSize() throws JAXBException, SOAPException {
        Object value = unbind(getHeader(), MAX_ENVELOPE_SIZE);
        return value == null ? null : ((JAXBElement<MaxEnvelopeSizeType>) value).getValue();
    }
    
    public LocaleType getLocale() throws JAXBException, SOAPException {
        Object value = unbind(getHeader(), LOCALE);
        return value == null ? null : ((JAXBElement<LocaleType>) value).getValue();
    }
    
    public EndpointReferenceType getRename() throws JAXBException, SOAPException {
        Object value = unbind(getBody(), RENAME);
        return value == null ? null : ((JAXBElement<EndpointReferenceType>) ((JAXBElement<RenameType>) value).getValue().getAny().get(0)).getValue();
    }
}
