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
 * $Id: BaseSupport.java,v 1.9.2.1 2006-12-21 08:24:17 jfdenise Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.enumeration.InvalidExpirationTimeFault;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XPathFilterFactory;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;

public class BaseSupport {
    
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
    
    private static Map<String, FilterFactory> supportedFilters =
            new HashMap<String, FilterFactory>();
    
    protected BaseSupport() {}
    
    static {
        FilterFactory xpathFilter = new XPathFilterFactory();
        supportedFilters.put(com.sun.ws.management.xml.XPath.NS_URI,
                xpathFilter);
        try {
            datatypeFactory = DatatypeFactory.newInstance();
            // try{
            cleanupTimer.schedule(ttask, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
        /*} catch(java.lang.IllegalStateException e){
            // NOTE: the cleanup timer has been throwing this
            // exception during unit tests. Re-initalizing it should not
            // cause it to fail so this exception is being silenced.
            LOG.fine("Base support was re-initalized.");
        }*/
        }catch(Exception ex) {
            throw new RuntimeException("Fail to initialize BaseSupport " + ex);
        }
    }
    /**
     * Add a Filtering support for a specific dialect.
     * @param dialect Filter dialect
     * @param filterFactory The Filter Factory that creates <code>Filter</code> for requests
     * relying on the passed dialect.
     *
     * @throws java.lang.Exception If the filter is already supported.
     */
    public synchronized static void addSupportedFilterDialect(String dialect,
            FilterFactory filterFactory) throws Exception {
        if(supportedFilters.get(dialect) != null)
            throw new Exception("Dialect " + dialect + " already supported");
        supportedFilters.put(dialect, filterFactory);
    }
    
    /**
     * Determines if the passed dialect is a supported dialect
     * @param dialect The dialect to check for support.
     * @return true if it is a supported dialect (or if dialect is null == default), else false
     */
    public synchronized static boolean isSupportedDialect(final String dialect) {
        if(dialect == null) return true;
        return supportedFilters.get(dialect) != null;
    }
    
    /**
     * Supported dialects, returned as Fault Detail when the dialect
     * is not supported.
     * @return An array of supported dialects.
     */
    public synchronized static String[] getSupportedDialects() {
        Set<String> keys =  supportedFilters.keySet();
        String[] dialects = new String[keys.size()];
        return keys.toArray(dialects);
    }
    
    protected synchronized static Filter newFilter(String dialect,
            List content,
            NamespaceMap nsMap) throws Exception {
        if(dialect == null)
            dialect = com.sun.ws.management.xml.XPath.NS_URI;
        FilterFactory factory = supportedFilters.get(dialect);
        if(factory == null)
            throw new FilteringRequestedUnavailableFault(null,
                    getSupportedDialects());
        return factory.newFilter(content, nsMap);
    }
    
    /**
     * Eventing Filter initialization
     */
    protected static Filter initializeFilter(org.xmlsoap.schemas.ws._2004._08.eventing.FilterType filterType,
            NamespaceMap nsMap)throws CannotProcessFilterFault, FilteringRequestedUnavailableFault {
        if(filterType == null) return null;
        return initializeFilter(filterType.getDialect(),
                filterType.getContent(), nsMap);
    }
    
    /**
     * Enumeration Filter initialization
     */
    protected static Filter initializeFilter(FilterType filterType,
            NamespaceMap nsMap)throws CannotProcessFilterFault, FilteringRequestedUnavailableFault {
        if(filterType == null) return null;
        return initializeFilter(filterType.getDialect(),
                filterType.getContent(), nsMap);
    }
    
    private static Filter initializeFilter(String dialect, List content,
            NamespaceMap nsMap)throws CannotProcessFilterFault,
            FilteringRequestedUnavailableFault {
        try {
            return newFilter(dialect, content, nsMap);
        }catch(FaultException fex) {
            throw fex;
        } catch(Exception ex) {
            throw new CannotProcessFilterFault(ex.getMessage());
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

