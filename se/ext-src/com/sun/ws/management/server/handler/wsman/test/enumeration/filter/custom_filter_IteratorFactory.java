/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: custom_filter_IteratorFactory.java,v 1.2 2007-05-31 19:47:48 nbeers Exp $
 */

package com.sun.ws.management.server.handler.wsman.test.enumeration.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.IteratorFactory;
import com.sun.ws.management.soap.FaultException;

/* This enumeration factory is for the "pull/source" resource.
 * It handles creation of an iterator for use to access the "pull/source" resources.
 * @see com.sun.ws.management.server.IteratorFactory#newIterator(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.enumeration.Enumeration, javax.xml.parsers.DocumentBuilder, boolean, boolean)
 *
 * @author denis
 */
public class custom_filter_IteratorFactory implements IteratorFactory {

	public final String RESOURCE_URI;
	
    private static final String PROPERTY_SELECTOR_KEY = "customfilter";
	
	protected custom_filter_IteratorFactory(String resource) {
		RESOURCE_URI = resource;
	}
	
	/* This creates iterators for the "cim_numericsensor" resource.
	 * 
	 * @see com.sun.ws.management.server.IteratorFactory#newIterator(com.sun.ws.management.server.HandlerContext, 
	 * com.sun.ws.management.enumeration.Enumeration, 
	 * javax.xml.parsers.DocumentBuilder, boolean, boolean)
	 */
	public EnumerationIterator newIterator(HandlerContext context,
			Enumeration request, DocumentBuilder db, boolean includeItem,
			boolean includeEPR) throws UnsupportedFeatureFault, FaultException {
		return new custom_filter_Iterator(context, RESOURCE_URI, request, db, includeItem, includeEPR);
	}
	
	// The Iterator implementation follows
	
	/**
	 * The class to be presented by a data source that would like to be
	 * enumerated.
	 *
	 * Implementations of this class are specific to the data structure being
	 * enumerated.
	 *
	 * @see EnumerationIterator
	 */
    public class custom_filter_Iterator implements EnumerationIterator {
        
    	private java.util.Enumeration<Object> keys;
    	private Properties properties;
    	
    	private final DocumentBuilder db;
    	private final boolean includeEPR;
    	private final String requestPath;
    	private final String resourceURI;
        
        custom_filter_Iterator(final HandlerContext hcontext,
				final String resource,
				final Enumeration request, final DocumentBuilder db,
				final boolean includeItem, final boolean includeEPR) {
        	
        	this.requestPath = hcontext.getURL();
        	this.resourceURI = resource;
        	this.db = db;
        	this.includeEPR = includeEPR;
        	this.properties = System.getProperties();
        	keys = properties.keys();
        }
         
        public EnumerationItem next() {

			// construct an item if necessary for the enumeration
			Element item = null;

			// Always include the item to allow filtering by EnumerationSupport
			final Object key = keys.nextElement();
			final Object value = properties.get(key);
			final Document doc = db.newDocument();
			item = doc.createElementNS(custom_filter_Handler.NS_URI, custom_filter_Handler.NS_PREFIX + ":" + key);
			item.setTextContent(value.toString());

			// construct an endpoint reference to accompany the element, if
			// needed
			EndpointReferenceType epr = null;
			if (includeEPR) {
				final Map<String, String> selectors = new HashMap<String, String>();
				selectors.put(PROPERTY_SELECTOR_KEY, key.toString());
				epr = EnumerationSupport.createEndpointReference(requestPath,
						resourceURI, selectors);
			}
			return new EnumerationItem(item, epr);
		}

        public boolean hasNext() {
            return keys.hasMoreElements();
        }
        
        public boolean isFiltered() {
            return false;
        }
        
        public void release() {
            properties = new Properties();
            keys = properties.keys();
        }
        
        public int estimateTotalItems() {
            return properties.size();
        }
    }
}
