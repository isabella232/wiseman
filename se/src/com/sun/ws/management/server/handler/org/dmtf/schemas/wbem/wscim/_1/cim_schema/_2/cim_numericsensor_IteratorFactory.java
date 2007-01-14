
package com.sun.ws.management.server.handler.org.dmtf.schemas.wbem.wscim._1.cim_schema._2;

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

/* This enumeration factory is for the "cim/numericsensor" resource.
 * It handles creation of an iterator for use to access the "cim/numericsensor" resources.
 * @see com.sun.ws.management.server.IteratorFactory#newIterator(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.enumeration.Enumeration, javax.xml.parsers.DocumentBuilder, boolean, boolean)
 *
 * @author denis
 */
public class cim_numericsensor_IteratorFactory implements IteratorFactory {

	public final String RESOURCE_URI;
	
	protected cim_numericsensor_IteratorFactory(String resource) {
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
		return new cim_numericsensor_Iterator(context, RESOURCE_URI, request, db, includeItem, includeEPR);
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
	public class cim_numericsensor_Iterator implements EnumerationIterator {
		
        private int index = 0;
        private final int count = 2;
        private final String address;
        private final String resourceURI;
        private String noPrefix;
        private final DocumentBuilder db;
        private final boolean includeEPR;
        private final HandlerContext hcontext;
		
    	protected cim_numericsensor_Iterator(final HandlerContext hcontext,
				final String resource,
				final Enumeration request, final DocumentBuilder db,
				final boolean includeItem, final boolean includeEPR) {

			this.db = db;
			this.address = hcontext.getURL();
			this.noPrefix = "";
			this.resourceURI = resource;
			this.includeEPR = includeEPR;
			this.hcontext = hcontext;

			try {
				Management mgmt = new Management(request);

				final Set<SelectorType> selectors = mgmt.getSelectors();
				if (selectors != null) {
					Iterator<SelectorType> si = selectors.iterator();
					while (si.hasNext()) {
						final SelectorType st = si.next();
						if ("NoPrefix".equals(st.getName())
								&& "true".equalsIgnoreCase(st.getContent().get(
										0).toString())) {
							noPrefix = "_NoPrefix";
							break;
						}
					}
				}
			} catch (SOAPException e) {
				throw new InternalErrorFault(e);
			} catch (JAXBException e) {
				throw new InternalErrorFault(e);
			}
		}
    	
        public EnumerationItem next() {

    		Document resourceDoc = null;
    		final String resourceDocName = "Pull" + noPrefix + "_"
    				+ index + ".xml";
    		final InputStream is = cim_numericsensor_Handler.load((ServletContext)hcontext
    				.getRequestProperties().get(HandlerContext.SERVLET_CONTEXT),
    				resourceDocName);
    		if (is == null) {
    			throw new InternalErrorFault("Failed to load " + resourceDocName
    					+ " from war");
    		}
    		try {
    			resourceDoc = db.parse(is);
    		} catch (Exception ex) {
    			throw new InternalErrorFault("Error parsing " + resourceDocName
    					+ " from war");
    		}
    		final Element root = resourceDoc.getDocumentElement();

    		EndpointReferenceType epr = null;
    		if (includeEPR) {
    			final Map<String, String> selectors = new HashMap<String, String>();
    			for (final String selector : cim_numericsensor_Handler.SELECTOR_KEYS) {
    				selectors
    						.put(selector, root.getElementsByTagNameNS(
    								resourceURI, selector).item(0)
    								.getTextContent());
    			}
    			epr = EnumerationSupport.createEndpointReference(address,
    					resourceURI, selectors);
    		}
    		index++;
    		return new EnumerationItem(root, epr);
    	}
        
        public boolean hasNext() {
            return index < count;
        }
        
        public int estimateTotalItems() {
            return count;
        }
        
        public boolean isFiltered() {
            return false;
        }
        
        public void release() {
        	
        }
	}
}
