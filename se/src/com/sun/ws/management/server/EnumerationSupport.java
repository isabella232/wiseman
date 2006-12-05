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
 * $Id: EnumerationSupport.java,v 1.35 2006-12-05 10:35:23 jfdenise Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.Management;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.enumeration.TimedOutFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.soap.FaultException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableEmpty;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributablePositiveInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;
import org.xmlsoap.schemas.ws._2004._08.eventing.Unsubscribe;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Pull;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Release;

/**
 * A helper class that encapsulates some of the arcane logic to allow data
 * sources to be enumerated using the WS-Enumeration protocol.
 *
 * @see EnumerationIterator
 */
public final class EnumerationSupport extends BaseSupport {
    
    private static final int DEFAULT_ITEM_COUNT = 1;
    private static final int DEFAULT_EXPIRATION_MILLIS = 60000;
    private static final long DEFAULT_MAX_TIMEOUT_MILLIS = 300000;
    private static Duration defaultExpiration = null;
    
    private EnumerationSupport() {}
    
    /**
     * Initiate an
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate Enumerate}
     * operation.
     *
     * @param request The incoming SOAP message that contains the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate Enumerate}
     * request.
     *
     * @param response The empty SOAP message that will contain the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse EnumerateResponse}.
     *
     * @param enumIterator The data source that will provide the actual items
     * to be returned as the result of the enumeration.
     *
     * @param enumFilter The Filter handler. If null and filtering is needed,
     * XPath filtering will aply.
     *
     * @param clientContext An Object provided by the data source that is
     * returned to the data source with each request for more elements to
     * help the data source retain context between subsequent operations.
     *
     * @param namespaces A map of namespace prefixes to namespace URIs used in
     * items to be enumerated. The prefix is the key and the URI is the value in the Map.
     * The namespaces map is used during filter evaluation.
     *
     * @throws FilteringNotSupportedFault if filtering is not supported.
     *
     * @throws InvalidExpirationTimeFault if the expiration time specified in
     * the request is syntactically-invalid or is in the past.
     */
    public static void enumerate(final Enumeration request, final Enumeration response,
            final EnumerationIterator enumIterator, final Object clientContext,
            final NamespaceMap... namespaces)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
        
        assert datatypeFactory != null : UNINITIALIZED;
        assert defaultExpiration != null : UNINITIALIZED;
        
        String expires = null;
        Filter filter = null;
        
        final Enumerate enumerate = request.getEnumerate();
        EnumerationModeType enumerationMode = null;
        boolean optimize = false;
        int maxElements = -1;
        
        final NamespaceMap nsMap = enumIterator.getNamespaces();
        
        if (enumerate == null) {
            // see if this is a pull event mode subscribe request
            final EventingExtensions evtx = new EventingExtensions(request);
            final Subscribe subscribe = evtx.getSubscribe();
            if (subscribe == null) {
                throw new InvalidMessageFault();
            }
            filter = initializeFilter(subscribe.getFilter(), nsMap);
            expires = subscribe.getExpires();
            
            if (subscribe.getEndTo() != null) {
                throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.ADDRESSING_MODE);
            }
        } else {
            filter = initializeFilter(enumerate.getFilter(), nsMap);
            
            expires = enumerate.getExpires();
            
            if (enumerate.getEndTo() != null) {
                throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.ADDRESSING_MODE);
            }
            
            // Locate the EnumerationMode, OptimizeEnumeration and MaxElements in the enumerate request
            //   null EnumerationMode after execution of the body implies no special enumeration mode
            for (final Object additionalValue : enumerate.getAny()) {
                if (additionalValue instanceof JAXBElement) {
                    final JAXBElement jaxbElement = (JAXBElement) additionalValue;
                    final QName name = jaxbElement.getName();
                    final Class<Object> type = jaxbElement.getDeclaredType();
                    final Object value = jaxbElement.getValue();
                    if (type.equals(EnumerationModeType.class) &&
                            EnumerationExtensions.ENUMERATION_MODE.equals(name)) {
                        enumerationMode = (EnumerationModeType) value;
                    } else if (type.equals(AttributableEmpty.class) &&
                            EnumerationExtensions.OPTIMIZE_ENUMERATION.equals(name)) {
                        optimize = true;
                    } else if (type.equals(AttributablePositiveInteger.class) &&
                            EnumerationExtensions.MAX_ELEMENTS.equals(name)) {
                        maxElements = ((AttributablePositiveInteger) value).getValue().intValue();
                    }
                }
            }
        }
        
        XMLGregorianCalendar expiration = initExpiration(expires);
        if (expiration == null) {
            final GregorianCalendar now = new GregorianCalendar();
            expiration = datatypeFactory.newXMLGregorianCalendar(now);
            expiration.add(defaultExpiration);
        }
        
        EnumerationContext ctx = new EnumerationContext(
                expiration,
                filter,
                enumerationMode,
                clientContext,
                enumIterator,
                optimize,
                maxElements);
        
        if (maxElements <= 0) {
            ctx.setCount(DEFAULT_ITEM_COUNT);
        } else {
            ctx.setCount(maxElements);
        }
        
        final UUID context = initContext(ctx);
        if (enumerate == null) {
            // this is a pull event mode subscribe request
            final EventingExtensions evtx = new EventingExtensions(response);
            evtx.setSubscribeResponse(
                    EventingSupport.createSubscriptionManager(request, response, context),
                    ctx.getExpiration(),
                    context.toString());
        } else {
            if (optimize) {
                final List<EnumerationItem> passed = new ArrayList<EnumerationItem>();
                final boolean more = doPull(request, response, context, ctx, null, passed);
                
                final EnumerationExtensions enx = new EnumerationExtensions(response);
                enx.setEnumerateResponse(context.toString(), ctx.getExpiration(),
                        passed, enumerationMode, more);
            } else {
                // place an item count estimate if one was requested
                insertTotalItemCountEstimate(request, response, enumIterator, clientContext);
                response.setEnumerateResponse(context.toString(), ctx.getExpiration());
            }
        }
    }
    
    public static void initialize() throws DatatypeConfigurationException {
        defaultExpiration = datatypeFactory.newDuration(DEFAULT_EXPIRATION_MILLIS);
    }
    
    /**
     * Handle a
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
     * request.
     *
     * @param request The incoming SOAP message that contains the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
     * request.
     *
     * @param response The empty SOAP message that will contain the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse PullResponse}.
     *
     * @throws InvalidEnumerationContextFault if the supplied context is
     * missing, is not understood or is not found because it has expired or
     * the server has been restarted.
     *
     * @throws TimedOutFault if the data source fails to provide the items to
     * be returned within the specified
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}.
     */
    public static void pull(final Enumeration request, final Enumeration response)
    throws SOAPException, JAXBException, FaultException {
        
        assert datatypeFactory != null : UNINITIALIZED;
        
        final Pull pull = request.getPull();
        if (pull == null) {
            throw new InvalidEnumerationContextFault();
        }
        
        final BigInteger maxChars = pull.getMaxCharacters();
        if (maxChars != null) {
            // TODO: add support for maxChars
            throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.MAX_ENVELOPE_SIZE);
        }
        
        final EnumerationContextType contextType = pull.getEnumerationContext();
        final UUID context = extractContext(contextType);
        final BaseContext bctx = getContext(context);
        if (bctx == null) {
            throw new InvalidEnumerationContextFault();
        }
        
        final GregorianCalendar now = new GregorianCalendar();
        final XMLGregorianCalendar nowXml = datatypeFactory.newXMLGregorianCalendar(now);
        if (bctx.isExpired(nowXml)) {
            removeContext(context);
            throw new InvalidEnumerationContextFault();
        }
        
        if (!(bctx instanceof EnumerationContext)) {
            throw new InvalidEnumerationContextFault();
        }
        final EnumerationContext ctx = (EnumerationContext) bctx;
        
        final BigInteger maxElements = pull.getMaxElements();
        if (maxElements == null) {
            ctx.setCount(DEFAULT_ITEM_COUNT);
        } else {
            // NOTE: downcasting from BigInteger to int
            ctx.setCount(maxElements.intValue());
        }
        
        final List<EnumerationItem> passed = new ArrayList<EnumerationItem>();
        final boolean more = doPull(request, response, context, ctx, pull.getMaxTime(), passed);
        if (more) {
            response.setPullResponse(passed, context.toString(), ctx.getEnumerationMode(), true);
        } else {
            response.setPullResponse(passed, null, ctx.getEnumerationMode(), false);
        }
    }
    
    private static boolean doPull(final Enumeration request, final Enumeration response,
            final UUID context, final EnumerationContext ctx, final Duration maxTimeout,
            final List<EnumerationItem> passed)
            throws SOAPException, JAXBException, FaultException {
        
        final Object clientContext = ctx.getClientContext();
        final EnumerationIterator iterator = ctx.getIterator();
        
        Duration maxTime = maxTimeout;
        if (maxTime == null) {
            // no timeout - set to a default max timeout
            maxTime = datatypeFactory.newDuration(System.currentTimeMillis() + DEFAULT_MAX_TIMEOUT_MILLIS);
        }
        
        boolean includeItem = false;
        boolean includeEPR = false;
        final EnumerationModeType mode = ctx.getEnumerationMode();
        if (mode == null) {
            includeItem = true;
            includeEPR = false;
        } else {
            final String modeString = mode.value();
            if (modeString.equals(EnumerationExtensions.Mode.EnumerateEPR.toString())) {
                includeItem = false;
                includeEPR = true;
            } else if (modeString.equals(EnumerationExtensions.Mode.EnumerateObjectAndEPR.toString())) {
                includeItem = true;
                includeEPR = true;
            } else {
                throw new InternalErrorFault("Unsupported enumeration mode: " + modeString);
            }
        }
        if (!includeItem && !includeEPR) {
            throw new InternalErrorFault("Must include one or both of Item & EPR for mode " +
                    mode == null ? "null" : mode.value());
        }
        
        final SOAPEnvelope env = response.getEnvelope();
        final DocumentBuilder db = response.getDocumentBuilder();
        
        final NamespaceMap nsMap = iterator.getNamespaces();
        boolean more = false;
        boolean full = false;
        while ((full = passed.size() < ctx.getCount()) &&
                (more = iterator.hasNext(clientContext, ctx.getCursor()))) {
            
            final TimerTask ttask = new TimerTask() {
                public void run() {
                    iterator.cancel(clientContext);
                }
            };
            final GregorianCalendar now = new GregorianCalendar();
            final long timeout = maxTime.getTimeInMillis(now);
            final Timer timeoutTimer = new Timer(true);
            timeoutTimer.schedule(ttask, timeout);
            
            final List<EnumerationItem> items = iterator.next(db,
                    clientContext,
                    includeItem,
                    includeEPR,
                    ctx.getCursor(),
                    ctx.getCount() - passed.size());
            if (items == null) {
                throw new TimedOutFault();
            }
            
            timeoutTimer.cancel();
            ctx.setCursor(ctx.getCursor() + items.size());
            
            // apply filter, if any
            //
            for (final EnumerationItem ee : items) {
                // retrieve the document element from the enumeration element
                final Element item = ee.getItem();
                if (item != null) {
                    // append the Element to the owner document if it has not been done
                    // this is critical for XPath filtering to work
                    final Document owner = item.getOwnerDocument();
                    if (owner.getDocumentElement() == null) {
                        owner.appendChild(item);
                    }
                    try {
                        if (ctx.evaluate(item, nsMap)) {
                            passed.add(ee);
                            final String nsURI = item.getNamespaceURI();
                            final String nsPrefix = item.getPrefix();
                            if (nsPrefix != null && nsURI != null) {
                                env.addNamespaceDeclaration(nsPrefix, nsURI);
                            }
                        }
                    } catch (XPathException xpx) {
                        throw new CannotProcessFilterFault("Error evaluating XPath: " +
                                xpx.getMessage());
                    } catch (Exception ex) {
                        throw new CannotProcessFilterFault("Error evaluating Filter: " +
                                ex.getMessage());
                    }
                } else {
                    if(EnumerationModeType.ENUMERATE_EPR.equals(mode)) {
                        if(ee.getEndpointReference() != null)
                            passed.add(ee);
                    }
                    
                }
            }
        }
        
        if (!full) {
            more = iterator.hasNext(clientContext, ctx.getCursor());
        }
        
        if (more) {
            // update
            putContext(context, ctx);
        } else {
            // remove the context - a subsequent release will fault with an invalid context
            removeContext(context);
        }
        
        // place an item count estimate if one was requested
        insertTotalItemCountEstimate(request, response, iterator, clientContext);
        
        return more;
    }
    
    /**
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Release Release} an
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate Enumeration}
     * in progress.
     *
     * @param request The incoming SOAP message that contains the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Release Release}
     * request.
     *
     * @param response The empty SOAP message that will contain the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Release Release} response.
     *
     * @throws InvalidEnumerationContextFault if the supplied context is
     * missing, is not understood or is not found because it has expired or
     * the server has been restarted.
     */
    public static void release(final Enumeration request, final Enumeration response)
    throws SOAPException, JAXBException, FaultException {
        
        final Release release = request.getRelease();
        if (release == null) {
            // this might be a pull-mode unsubscribe request
            final Eventing evt = new Eventing(request);
            final Unsubscribe unsub = evt.getUnsubscribe();
            if (unsub != null) {
                EventingSupport.unsubscribe(evt, new Eventing(response));
                return;
            }
            throw new InvalidEnumerationContextFault();
        }
        final EnumerationContextType contextType = release.getEnumerationContext();
        if (contextType == null) {
            throw new InvalidEnumerationContextFault();
        }
        final UUID context = extractContext(contextType);
        final BaseContext ctx = removeContext(context);
        if (ctx == null) {
            throw new InvalidEnumerationContextFault();
        }
    }
    
    /**
     * Utility method to create an EPR for accessing individual elements of an
     * enumeration directly.
     *
     * @param address The transport address of the service.
     *
     * @param resource The resource being addressed.
     *
     * @param selectorMaps Selectors used to identify the resource. Optional.
     */
    public static EndpointReferenceType createEndpointReference(final String address,
            final String resource, final Map<String, String>... selectorMaps) {
        
        final ReferenceParametersType refp = Addressing.FACTORY.createReferenceParametersType();
        
        final AttributableURI attributableURI = Management.FACTORY.createAttributableURI();
        attributableURI.setValue(resource);
        refp.getAny().add(Management.FACTORY.createResourceURI(attributableURI));
        
        if (selectorMaps != null) {
            final SelectorSetType selectorSet = Management.FACTORY.createSelectorSetType();
            for (final Map<String, String> sMap : selectorMaps) {
                final Iterator<Entry<String, String> > si = sMap.entrySet().iterator();
                while (si.hasNext()) {
                    final Entry<String, String> entry = si.next();
                    final SelectorType selector = Management.FACTORY.createSelectorType();
                    selector.setName(entry.getKey());
                    selector.getContent().add(entry.getValue());
                    selectorSet.getSelector().add(selector);
                }
            }
            refp.getAny().add(Management.FACTORY.createSelectorSet(selectorSet));
        }
        
        return Addressing.createEndpointReference(address, null, refp, null, null);
    }
    
    private static UUID extractContext(final EnumerationContextType contextType)
    throws FaultException {
        
        if (contextType == null) {
            throw new InvalidEnumerationContextFault();
        }
        
        final String contextString = (String) contextType.getContent().get(0);
        UUID context;
        try {
            context = UUID.fromString(contextString);
        } catch (IllegalArgumentException argex) {
            throw new InvalidEnumerationContextFault();
        }
        
        return context;
    }
    
    private static void insertTotalItemCountEstimate(final Enumeration request,
            final Enumeration response, final EnumerationIterator iterator,
            final Object clientContext)
            throws SOAPException, JAXBException {
        // place an item count estimate if one was requested
        final EnumerationExtensions enx = new EnumerationExtensions(request);
        if (enx.getRequestTotalItemsCountEstimate() != null) {
            final EnumerationExtensions rx = new EnumerationExtensions(response);
            final int estimate = iterator.estimateTotalItems(clientContext);
            if (estimate < 0) {
                // estimate not available
                rx.setTotalItemsCountEstimate(null);
            } else {
                rx.setTotalItemsCountEstimate(new BigInteger(Integer.toString(estimate)));
            }
        }
    }
    
   
}
