package com.sun.ws.management.server;

import javax.xml.parsers.DocumentBuilder;

import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.soap.FaultException;

public interface EventingIteratorFactory {
    /**
     * EnumerationIterator creation.
     * 
     * @param context the HandlerContext
     * @param request the Enumeration request that this iterator is to fufill
     * @param db the DocumentBuilder to use for items created by this iterator
     * @param includeItem if true the requester wants the item returned, otherwise
     * just the EPR if includeEPR is true
     * @param includeEPR if true the requestor wants the EPR for each item returned, otherwise
     * just the item if includeItem is true. If EPRs are not supported by the iterator,
     * the iterator should throw an UnsupportedFeatureFault.
     * 
     * @throws com.sun.ws.management.UnsupportedFeatureFault If EPRs are not supported.
     * @throws com.sun.ws.management.soap.FaultException If a WS-MAN protocol related exception occurs.
     * @return An enumeration iterator for the request
     */
    public EnumerationIterator newIterator(final HandlerContext context, 
			final Eventing request, 
			final DocumentBuilder db, 
			final boolean includeItem,
			final boolean includeEPR)
    throws UnsupportedFeatureFault, FaultException;
}
