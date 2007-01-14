
package com.sun.ws.management.server.handler.wsman.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPException;

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

/* This enumeration factory is for the "exec" resource.
 * It handles creation of an iterator for use to access the "exec" resources.
 * @see com.sun.ws.management.server.IteratorFactory#newIterator(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.enumeration.Enumeration, javax.xml.parsers.DocumentBuilder, boolean, boolean)
 *
 * @author denis
 */
public class exec_IteratorFactory implements IteratorFactory {

	public final String RESOURCE_URI;
	
	protected exec_IteratorFactory(String resource) {
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
		return new exec_Iterator(context, RESOURCE_URI, request, db, includeItem, includeEPR);
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
    /**
     * An EnumerationIterator to process enumeration requests
     */
    public final class exec_Iterator implements EnumerationIterator {

        /**
         * Server request path that can be used for creating an EPR
         */
        final String requestPath;

        /**
         * URI that identifies the resource being manipulated
         */
        final String resourceURI;
        
        /**
         * Results of exec operation for use across calls
         */
        String[] results;
        
        int cursor = 0;
        final DocumentBuilder db;
        final boolean includeEPR;
        final boolean includeItem;
        
        exec_Iterator(final HandlerContext hcontext,
				final String resource,
				final Enumeration request, final DocumentBuilder db,
				final boolean includeItem, final boolean includeEPR) {
        	this.requestPath = hcontext.getURL();
        	this.resourceURI = resource;
        	this.db = db;
        	this.includeEPR = includeEPR;
        	this.includeItem = includeItem;
        	
        	try {
				this.results = exec_Handler.executeCommand(new Management(request)).split("\n");
			} catch (JAXBException e) {
				throw new InternalErrorFault(e);
			} catch (SOAPException e) {
				throw new InternalErrorFault(e);
			} catch (IOException e) {
				throw new InternalErrorFault(e);
			} catch (InterruptedException e) {
				throw new InternalErrorFault(e);
			}
        }
        
        public EnumerationItem next() {

    		// create an enumeration element only if necessary
    		Element item = null;

    			final Document doc = db.newDocument();
    			item = doc.createElementNS(exec_Handler.NS_URI, 
    					exec_Handler.NS_PREFIX + ":" + exec_Handler.EXEC);
    			item.setTextContent(results[cursor]);

    		// construct an endpoint reference to accompany the element, if needed
    		EndpointReferenceType epr = null;
    		if (includeEPR) {
    			epr = EnumerationSupport.createEndpointReference(requestPath,
    					resourceURI, null);
    		}
    		cursor++;
    		return new EnumerationItem(item, epr);

    	}
        
        public boolean hasNext() {
            return cursor < results.length;
        }
        
        public boolean isFiltered() {
            return false;
        }
        
        public int estimateTotalItems() {
            return results.length;
        }
        
        public void release() {
        	results = new String[0];
        }
    }
}
