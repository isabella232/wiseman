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
 * $Id: EnumerationIterator.java,v 1.2 2005-07-22 21:57:56 akhilarora Exp $
 */

package com.sun.ws.management.server;

import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The inteface to be presented by a data source that would like to be 
 * enumerated by taking advantage of the functionality present in 
 * {@link EnumerationSupport EnumerationSupport}.
 *
 * @see EnumerationSupport
 */
public interface EnumerationIterator {
    
    /**
     * Supply the next few elements of the iteration. This is invoked to
     * satisfy a {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} 
     * request. The operation must return within the 
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout} 
     * specified in the 
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request, 
     * otherwise {@link #cancel cancel} will
     * be invoked and the current thread interrupted. When cancelled, 
     * the implementation can return the results currently 
     * accumulated (in which case no 
     * {@link com.sun.ws.management.soap.Fault Fault} is generated) or it can 
     * return {@code null} in which case a
     * {@link com.sun.ws.management.enumeration.TimedOutFault TimedOutFault}
     * is returned.
     *
     * @param doc The document into which the returned items will be placed.
     *
     * @param context The client context that was specified to
     * {@link EnumerationSupport#enumerate enumerate} is returned.
     *
     * @param startPos The starting position (cursor) for this 
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @param count The number of items desired in this 
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @return a List of {@link org.w3c.dom.Element Elements} that will be 
     * returned in the 
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse PullResponse}.
     */
    List<Element> next(final Document doc, final Object context,
            final int startPos, final int count);
    /**
     * Indicates if there are more elements remaining in the iteration.
     *
     * @param context The client context that was specified to
     * {@link EnumerationSupport#enumerate enumerate} is returned.
     *
     * @param startPos The starting position (cursor) for this 
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @return {@code true} if there are more elements in the iteration,
     * {@code false} otherwise.
     */
    boolean hasNext(final Object context, final int startPos);
    
    /**
     * Invoked when a {@link #next next} call exceeds the 
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
     * specified in the 
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} 
     * request. An implementation is expected to set a flag that 
     * causes the currently-executing {@link #next next} operation to return 
     * gracefully.
     *
     * @param context The client context that was specified to
     * {@link EnumerationSupport#enumerate enumerate} is returned.
     */
    void cancel(final Object context);
}
