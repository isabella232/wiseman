/**
 * 
 */
package com.sun.ws.management.server;

import com.sun.ws.management.enumeration.Enumeration;

/**
 * This is an extension of the EnumerationIterator interface that provides
 * the application access to the request and response messages.
 *
 */
public interface EnumerationPullIterator extends EnumerationIterator {

	public void startPull(final Enumeration request);
	
	public void endPull(final Enumeration response);
}
