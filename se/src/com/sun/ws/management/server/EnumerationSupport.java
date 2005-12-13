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
 * $Id: EnumerationSupport.java,v 1.5 2005-12-13 21:12:00 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.FilteringNotSupportedFault;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.enumeration.InvalidExpirationTimeFault;
import com.sun.ws.management.enumeration.TimedOutFault;
import com.sun.ws.management.soap.FaultException;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
public final class EnumerationSupport {
    
    private static final int CLEANUP_INTERVAL = 60000;
    private static final int DEFAULT_EXPIRATION_MILLIS = 60000;
    
    private static final Map<UUID, Context> contextMap = new HashMap();
    private static final Timer cleanupTimer = new Timer(true);
    
    private static DatatypeFactory datatypeFactory;
    private static Duration defaultExpiration;
    
    private static final class Context {
        private int cursor;
        private int count;
        private Object clientContext;
        private XMLGregorianCalendar expiration;
        private List<Element> items;
        private EnumerationIterator iterator;
    }
    
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
     * @throws FilteringNotSupportedFault if filtering is not supported.
     *
     * @throws InvalidExpirationTimeFault if the expiration time specified in
     * the request is syntactically-invalid or is in the past.
     */
    public static void enumerate(final Enumeration request, final Enumeration response,
            final EnumerationIterator enumIterator, final Object clientContext)
            throws DatatypeConfigurationException, SOAPException, JAXBException, FaultException {
        
        if (datatypeFactory == null || defaultExpiration == null) {
            datatypeFactory = DatatypeFactory.newInstance();
            defaultExpiration = datatypeFactory.newDuration(DEFAULT_EXPIRATION_MILLIS);
        }
        
        final Enumerate enumerate = request.getEnumerate();
        final FilterType filterType = enumerate.getFilter();
        if (filterType != null) {
            // TODO: add support for filtering
            throw new FilteringNotSupportedFault();
        }
        
        final String expires = enumerate.getExpires();
        final GregorianCalendar now = new GregorianCalendar();
        final XMLGregorianCalendar nowXml = datatypeFactory.newXMLGregorianCalendar(now);
        XMLGregorianCalendar expiration;
        if (expires == null) {
            expiration = datatypeFactory.newXMLGregorianCalendar(now);
            expiration.add(defaultExpiration);
        } else {
            try {
                // first try if it's a Duration
                final Duration duration = datatypeFactory.newDuration(expires);
                expiration = datatypeFactory.newXMLGregorianCalendar(now);
                expiration.add(duration);
            } catch (IllegalArgumentException arg1) {
                try {
                    // now try if it's a calendar time
                    expiration = datatypeFactory.newXMLGregorianCalendar(expires);
                } catch (IllegalArgumentException arg2) {
                    throw new InvalidExpirationTimeFault();
                }
            }
        }
        if (nowXml.compare(expiration) > 0) {
            throw new InvalidExpirationTimeFault();
        }
        
        final UUID context = UUID.randomUUID();
        
        final Context ctx = new Context();
        ctx.cursor = Integer.valueOf(0);
        ctx.clientContext = clientContext;
        ctx.iterator = enumIterator;
        ctx.expiration = expiration;
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
                    if (nowXml.compare(value.expiration) > 0) {
                        // context expired, GC
                        contextMap.remove(key);
                    }
                }
            }
        };
        cleanupTimer.schedule(ttask, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
        
        response.setEnumerateResponse(context.toString(), expiration.toXMLFormat());
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
        final EnumerationContextType contextType = pull.getEnumerationContext();
        final UUID context = extractContext(contextType);
        final Context ctx = contextMap.get(context);
        if (ctx == null) {
            throw new InvalidEnumerationContextFault();
        }
        final GregorianCalendar now = new GregorianCalendar();
        final XMLGregorianCalendar nowXml = datatypeFactory.newXMLGregorianCalendar(now);
        if (nowXml.compare(ctx.expiration) > 0) {
            // context expired
            throw new InvalidEnumerationContextFault();
        }
        
        final BigInteger maxChars = pull.getMaxCharacters();
        if (maxChars != null) {
            // NOTE: downcasting from BigInteger to int
            final int chars = maxChars.intValue();
            // TODO: add support for maxChars
        }
        
        // implied value
        ctx.count = 1;
        final BigInteger maxElements = pull.getMaxElements();
        if (maxElements != null) {
            // NOTE: downcasting from BigInteger to int
            ctx.count = maxElements.intValue();
        }
        
        final Document doc = response.getBody().getOwnerDocument();
        final Duration maxTime = pull.getMaxTime();
        if (maxTime == null) {
            // no timeout - take as long as it takes
            ctx.items = ctx.iterator.next(doc, ctx.clientContext, ctx.cursor, ctx.count);
        } else {
            final TimerTask ttask = new TimerTask() {
                public void run() {
                    ctx.iterator.cancel(ctx.clientContext);
                }
            };
            final long timeout = maxTime.getTimeInMillis(now);
            final Timer timeoutTimer = new Timer(true);
            timeoutTimer.schedule(ttask, new Date(timeout));
            ctx.items = ctx.iterator.next(doc, ctx.clientContext, ctx.cursor, ctx.count);
            if (ctx.items == null) {
                throw new TimedOutFault();
            }
        }
        
        final boolean haveMore = ctx.iterator.hasNext(ctx.clientContext, ctx.cursor);
        ctx.cursor = Integer.valueOf(ctx.cursor + ctx.items.size());
        if (haveMore) {
            // update value but not the key -
            // otherwise Release may fail to find a context
            contextMap.put(context, ctx);
            response.setPullResponse(ctx.items, context.toString(), true);
        } else {
            // remove the context - a subsequent release will fault with an invalid context
            contextMap.remove(context);
            response.setPullResponse(ctx.items, null, false);
        }
        
        // no need to have this (potentially large object) hanging around
        ctx.items = null;
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
        final Context ctx = contextMap.remove(context);
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
