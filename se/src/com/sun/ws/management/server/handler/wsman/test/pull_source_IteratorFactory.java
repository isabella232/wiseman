
package com.sun.ws.management.server.handler.wsman.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
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
public class pull_source_IteratorFactory implements IteratorFactory {

	public final String RESOURCE_URI;
	
	protected pull_source_IteratorFactory(String resource) {
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
		return new pull_source_Iterator(context, RESOURCE_URI, request, db, includeItem, includeEPR);
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
    public class pull_source_Iterator implements  EnumerationIterator {
        
        private static final String SELECTOR_KEY = "log";
        private static final String NS_URI = "https://wiseman.dev.java.net/test/events/pull";
        private static final String NS_PREFIX = "log";
        
        /**
         * A log of events to pass.  These should be replaced with real events.
         */
        private String[][] eventLog = {
            { "event1", "critical" },
            { "event2", "warning" },
            { "event3", "info" },
            { "event4", "debug" }
        };
        
    	private int cursor = 0;
    	private final DocumentBuilder db;
    	private final boolean includeEPR;
    	private final String requestPath;
    	private final String resourceURI;
    	private final boolean includeItem;
    	
        protected pull_source_Iterator(final HandlerContext hcontext,
				final String resource,
				final Enumeration request, final DocumentBuilder db,
				final boolean includeItem, final boolean includeEPR) {
        	
        	this.requestPath = hcontext.getURL();
        	this.resourceURI = resource;
        	this.db = db;
        	this.includeEPR = includeEPR;
        	this.includeItem = includeItem;
        }
        
        public EnumerationItem next() {
        	
                final String key = eventLog[cursor][0];

                // construct an item if necessary for the enumeration
                Element item = null;
                if (includeItem) {
                    final String value = eventLog[cursor][1];
                    final Document doc = db.newDocument();
                    item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + key);
                    item.setTextContent(value);
                }

                // construct an endpoint reference to accompany the element, if needed
                EndpointReferenceType epr = null;
                if (includeEPR) {
                    final Map<String, String> selectors = new HashMap<String, String>();
                    selectors.put(SELECTOR_KEY, key);
                    epr = EnumerationSupport.createEndpointReference(requestPath, resourceURI, selectors);
                }
                cursor++;
                return new EnumerationItem(item, epr);
        }
        
        public boolean hasNext() {
            return cursor < eventLog.length;
        }
        
        public boolean isFiltered() {
            return false;
        }
        
        public int estimateTotalItems() {
            // choose not to provide an estimate
            return -1;
        }

		public void release() {
			eventLog = new String[0][0];
			
		}
    }
}
