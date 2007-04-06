
package com.sun.ws.management.server.handler.wsman.test;

import javax.xml.parsers.DocumentBuilder;

import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationIterator;
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
}
