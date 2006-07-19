/*
 * Copyright 2006 Sun Microsystems, Inc.
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
 * $Id: BaseSupport.java,v 1.7 2006-07-19 16:09:17 obiwan314 Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.enumeration.InvalidExpirationTimeFault;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.xml.XPath;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

class BaseSupport {
    
    protected static final String UUID_SCHEME = "urn:uuid:";
    protected static final String UNINITIALIZED = "uninitialized";
    protected static DatatypeFactory datatypeFactory = null;
    
    private static final Map<UUID, BaseContext> contextMap = new HashMap();
    
    protected static final TimerTask ttask = new TimerTask() {
        public void run() {
            final GregorianCalendar now = new GregorianCalendar();
            final XMLGregorianCalendar nowXml =
                    datatypeFactory.newXMLGregorianCalendar(now);
            final UUID[] keys = contextMap.keySet().toArray(new UUID[contextMap.size()]);
            for (int i = 0; i < keys.length; i++) {
                final UUID key = keys[i];
                final BaseContext context = contextMap.get(key);
                if (context.isExpired(nowXml)) {
                    contextMap.remove(key);
                }
            }
        }
    };
    private static final Logger LOG = Logger.getLogger(BaseSupport.class.getName());

    private static final int CLEANUP_INTERVAL = 60000;
    private static final Timer cleanupTimer = new Timer(true);
    
    protected BaseSupport() {}
    
    public static void initialize() throws DatatypeConfigurationException {
        datatypeFactory = DatatypeFactory.newInstance();
        try{
        	cleanupTimer.schedule(ttask, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
        } catch(java.lang.IllegalStateException e){
        	// NOTE: the cleanup timer has been throwing this
        	// exception during unit tests. Re-initalizing it should not
        	// cause it to fail so this exception is being silenced.
        	LOG.fine("Base support was re-initalized.");
        }
    }
    
    protected static String initFilter(final String filterDialect, final List<Object> filterExpressions)
    throws FaultException {
        
        String dialect = filterDialect;
        if (dialect == null) {
            // implied value
            dialect = XPath.NS_URI;
        } else {
            if (!XPath.isSupportedDialect(dialect)) {
                throw new FilteringRequestedUnavailableFault(null,
                        com.sun.ws.management.xml.XPath.SUPPORTED_FILTER_DIALECTS);
            }
        }
        if (filterExpressions == null) {
            throw new InvalidMessageFault("Missing a filter expression");
        }
        final Object expr = filterExpressions.get(0);
        if (expr == null) {
            throw new InvalidMessageFault("Missing filter expression");
        }
        if (expr instanceof String) {
            return (String) expr;
        } else {
            throw new InvalidMessageFault("Invalid filter expression type: " + expr);
        }
    }
    
    protected static XMLGregorianCalendar initExpiration(final String expires)
    throws InvalidExpirationTimeFault {
        
        assert datatypeFactory != null : UNINITIALIZED;
        
        if (expires == null) {
            // a very large value - effectively never expires
            return datatypeFactory.newXMLGregorianCalendar(Integer.MAX_VALUE,
                    12, 31, 23, 59, 59, 999, DatatypeConstants.MAX_TIMEZONE_OFFSET);
        }
        
        final GregorianCalendar now = new GregorianCalendar();
        final XMLGregorianCalendar nowXml = datatypeFactory.newXMLGregorianCalendar(now);
        
        XMLGregorianCalendar expiration = null;
        try {
            // first try if it's a Duration
            final Duration duration = datatypeFactory.newDuration(expires);
            expiration = datatypeFactory.newXMLGregorianCalendar(now);
            expiration.add(duration);
        } catch (IllegalArgumentException ndex) {
            try {
                // now see if it is a calendar time
                expiration = datatypeFactory.newXMLGregorianCalendar(expires);
            } catch (IllegalArgumentException ncex) {
                throw new InvalidExpirationTimeFault();
            }
        }
        if (nowXml.compare(expiration) > 0) {
            // expiration cannot be in the past
            throw new InvalidExpirationTimeFault();
        }
        return expiration;
    }
    
    protected static UUID initContext(final BaseContext context) {
        final UUID uuid = UUID.randomUUID();
        contextMap.put(uuid, context);
        return uuid;
    }
    
    protected static BaseContext getContext(final Object context) {
        return contextMap.get(context);
    }
    
    protected static BaseContext putContext(final UUID context, final BaseContext ctx) {
        return contextMap.put(context, ctx);
    }
    
    protected static BaseContext removeContext(final Object context) {
        return contextMap.remove(context);
    }
}

    