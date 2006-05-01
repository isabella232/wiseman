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
 * $Id: EnumerationSupport.java,v 1.13 2006-05-01 23:32:22 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.enumeration.TimedOutFault;
import com.sun.ws.management.eventing.EventingExtensions;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.soap.FaultException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.eventing.Subscribe;
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
    private static Duration defaultExpiration;
    
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
            final Map<String, String> namespaces)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {

        initialize();
        
        String expires = null;
        String filterExpression = null;
        
        final Enumerate enumerate = request.getEnumerate();
        if (enumerate == null) {
            // see if this is a pull event mode subscribe request
            final EventingExtensions evtx = new EventingExtensions(request);
            final Subscribe subscribe = evtx.getSubscribe();
            if (subscribe == null) {
                throw new InvalidMessageFault();
            }
            final org.xmlsoap.schemas.ws._2004._08.eventing.FilterType filterType = subscribe.getFilter();
            if (filterType != null) {
                filterExpression = initFilter(filterType.getDialect(), filterType.getContent());
            }
            expires = subscribe.getExpires();
            
            if (subscribe.getEndTo() != null) {
                throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.ADDRESSING_MODE);
            }
        } else {
            final FilterType filterType = enumerate.getFilter();
            if (filterType != null) {
                filterExpression = initFilter(filterType.getDialect(), filterType.getContent());
            }
            expires = enumerate.getExpires();
            
            if (enumerate.getEndTo() != null) {
                throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.ADDRESSING_MODE);
            }
        }
        
        XMLGregorianCalendar expiration = initExpiration(expires);
        if (expiration == null) {
            final GregorianCalendar now = new GregorianCalendar();
            expiration = datatypeFactory.newXMLGregorianCalendar(now);
            expiration.add(defaultExpiration);
        }
        
        EnumerationContext ctx = null;
        try {
            ctx = new EnumerationContext(expiration,
                    filterExpression, namespaces, clientContext, enumIterator);
        } catch (XPathExpressionException xpx) {
            throw new CannotProcessFilterFault("Unable to compile XPath: " +
                    "\"" + filterExpression + "\"");
        }
        
        final UUID context = initContext(ctx);
        if (enumerate == null) {
            // this is a pull event mode subscribe request
            final EventingExtensions evtx = new EventingExtensions(response);
            evtx.setSubscribeResponse(null, ctx.getExpiration(), context.toString());
        } else {
            response.setEnumerateResponse(context.toString(), ctx.getExpiration());
        }
    }

    private static synchronized void initialize() throws DatatypeConfigurationException {
        init();
        if (defaultExpiration == null) {
            defaultExpiration = datatypeFactory.newDuration(DEFAULT_EXPIRATION_MILLIS);
        }
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
        
        final Pull pull = request.getPull();
        
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
        final Object clientContext = ctx.getClientContext();
        final EnumerationIterator iterator = ctx.getIterator();
        
        final BigInteger maxElements = pull.getMaxElements();
        if (maxElements == null) {
            ctx.setCount(DEFAULT_ITEM_COUNT);
        } else {
            // NOTE: downcasting from BigInteger to int
            ctx.setCount(maxElements.intValue());
        }
        
        Duration maxTime = pull.getMaxTime();
        if (maxTime == null) {
            // no timeout - set to a default max timeout
            maxTime = datatypeFactory.newDuration(System.currentTimeMillis() + DEFAULT_MAX_TIMEOUT_MILLIS);
        }
        
        final SOAPEnvelope env = response.getEnvelope();
        final DocumentBuilder db = response.getDocumentBuilder();
        final List<Element> passed = new ArrayList<Element>(ctx.getCount());
        
        while (passed.size() < ctx.getCount() && iterator.hasNext(clientContext, ctx.getCursor())) {
            final TimerTask ttask = new TimerTask() {
                public void run() {
                    iterator.cancel(clientContext);
                }
            };
            final long timeout = maxTime.getTimeInMillis(now);
            final Timer timeoutTimer = new Timer(true);
            timeoutTimer.schedule(ttask, timeout);
            
            final List<Element> items = iterator.next(db, clientContext, ctx.getCursor(), 
                    ctx.getCount() - passed.size());
            if (items == null) {
                throw new TimedOutFault();
            }
            timeoutTimer.cancel();
            ctx.setCursor(ctx.getCursor() + items.size());
            
            // apply filter, if any
            for (final Element item : items) {
                // append the Element to the owner document if it has not been done
                // this is critical for XPath filtering to work
                final Document owner = item.getOwnerDocument();
                if (owner.getDocumentElement() == null) {
                    owner.appendChild(item);
                }
                try {
                    if (ctx.evaluate(item)) {
                        passed.add(item);
                        env.addNamespaceDeclaration(item.getPrefix(), item.getNamespaceURI());
                    }
                } catch (XPathException xpx) {
                    throw new CannotProcessFilterFault("Error evaluating XPath");
                }
            }
        }
        
        if (iterator.hasNext(clientContext, ctx.getCursor() + 1)) {
            // update
            putContext(context, ctx);
            response.setPullResponse(passed, context.toString(), true);
        } else {
            // remove the context - a subsequent release will fault with an invalid context
            removeContext(context);
            response.setPullResponse(passed, null, false);
        }
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
        final EnumerationContextType contextType = release.getEnumerationContext();
        final UUID context = extractContext(contextType);
        final BaseContext ctx = removeContext(context);
        if (ctx == null) {
            throw new InvalidEnumerationContextFault();
        }
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
}
