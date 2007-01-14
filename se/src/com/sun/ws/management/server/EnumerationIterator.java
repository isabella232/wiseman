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
 * $Id: EnumerationIterator.java,v 1.10 2007-01-14 17:52:35 denis_rachal Exp $
 */

package com.sun.ws.management.server;


/**
 * The inteface to be presented by a data source that would like to be
 * enumerated by taking advantage of the functionality present in
 * {@link EnumerationSupport EnumerationSupport}.
 *
 * @see EnumerationSupport
 */
public interface EnumerationIterator {
    
    /**
     * Estimate the total number of elements available.
     *
     * @return an estimate of the total number of elements available
     * in the enumeration.
     * Return a negative number if an estimate is not available.
     */
    int estimateTotalItems();
     
    /**
     * Indicates if the iterator has already been filtered.
     * This indicates that further filtering is not required
     * by the framwork.
     * 
     * @return {@code true} if the iterator has already been filtered,
     * {@code false} otherwise.
     */
    boolean isFiltered();
    
    /**
     * Indicates if there are more elements remaining in the iteration.
     * 
     * @return {@code true} if there are more elements in the iteration,
     * {@code false} otherwise.
     */
    boolean hasNext();
    
    /**
     * Supply the next element of the iteration. This is invoked to
     * satisfy a {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
     * request. The operation must return within the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
     * specified in the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request,
     * otherwise {@link #release release} will
     * be invoked and the current thread interrupted. When cancelled,
     * the implementation can return the result currently
     * accumulated (in which case no
     * {@link com.sun.ws.management.soap.Fault Fault} is generated) or it can
     * return {@code null} in which case a
     * {@link com.sun.ws.management.enumeration.TimedOutFault TimedOutFault}
     * is returned.
     *
     * @return an {@link EnumerationElement Elements} that is used to
     * construct proper responses for a
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse PullResponse}.
     */
    EnumerationItem next();
    
    /**
     * Release any resources being used by the iterator. Calls
     * to other methods of this iterator instance will exhibit
     * undefined behaviour, after this method completes.
     */
    void release();

}
