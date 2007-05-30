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
 * $Id: EnumerableResource.java,v 1.9 2007-05-30 20:30:21 nbeers Exp $
 */
package com.sun.ws.management.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;
import com.sun.ws.management.server.EnumerationItem;

/**
 * An abstract representation of a WS Management resource that focuses on 
 * WS-Enumeration. 
 * 
 * @see TransferableResource
 */
public interface EnumerableResource extends TransferableResource {

	/**
	 * Initiates an enumeration transaction by obtaining an enumeration context.
	 * This is a ticket which must be used in all future calls to access this
	 * enumeration.
	 * 
	 * @param filter a filter expression to be applied against the enumeration.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param namespaces prefix and namespace map for namespaces used in the filter
	 *        expression.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 *        can be used for XPath expressions. 
	 * @param getEpr indicates that the EndpointReference (EPR) for each item is to
	 *        returned in the pull.
	 * @param getResource indicates that the individual items are to be returned in
	 *        the pull.
	 * @return an enumeration context
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract EnumerationCtx enumerate(final Object filter, 
			final Map<String, String> namespaces, final String dialect,
			final boolean getEpr, final boolean getResource) 
	throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException;
	
	/**
	 * Initiates an enumeration transaction by obtaining an enumeration context.
	 * This is a ticket which must be used in all future calls to access this
	 * enumeration.
	 * 
	 * @param filter a filter expression to be applied against the enumeration.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param namespaces prefix and namespace map for namespaces used in the filter
	 *        expression.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 *        can be used for XPath expressions. 
	 * @param getEpr indicates that the EndpointReference (EPR) for each item is to
	 *        returned in the pull.
	 * @param getResource indicates that the individual items are to be returned in
	 *        the pull.
	 * @param getItemCount if true indicates that the estimated item count should be returned
	 * @param optimized if true the EnumerationItems will be requested to be returned
	 *        in the enumerate call. Call {@link #getEnumerationItems()} to get the
	 *        list of resources returned in this call.
	 * @param timeout the <code>OperationTimeout</code>. This is the
	 *        maximum amount of time in milliseconds the client is willing
	 *        to wait for the operation to complete.
	 *        If &lt;= 0 this parameter is ignored.
	 * @param maxElements
	 *        the maximum number of elements which should be returned.
	 *        Ignored if optimized is false.
	 * @return an enumeration context
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract EnumerationCtx enumerate(final Object filter,
			final Map<String, String> namespaces, final String dialect,
			final boolean getEpr, final boolean getResource,
			final boolean getItemCount, final boolean optimized,
			final long timeout, final long maxElements)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException;
	
	/**
	 * Initiates an enumeration transaction by obtaining an enumeration context.
	 * This is a ticket which must be used in all future calls to access this
	 * enumeration.
	 * 
	 * @param filter a filter expression to be applied against the enumeration.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param namespaces prefix and namespace map for namespaces used in the filter
	 *        expression.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 *        can be used for XPath expressions. 
	 * @param getEpr indicates that the EndpointReference (EPR) for each item is to
	 *        returned in the pull.
	 * @param getResource indicates that the individual items are to be returned in
	 *        the pull.
	 * @param getItemCount if true indicates that the estimated item count should be returned
	 * @param optimized if true the EnumerationItems will be requested to be returned
	 *        in the enumerate call. Call {@link #getEnumerationItems()} to get the
	 *        list of resources returned in this call.
	 * @param timeout the <code>OperationTimeout</code>. This is the
	 *        maximum amount of time in milliseconds the client is willing
	 *        to wait for the operation to complete.
	 *        If &lt;= 0 this parameter is ignored.
	 * @param maxElements
	 *        the maximum number of elements which should be returned.
	 *        Ignored if optimized is false.
	 * @param params additional parameters to be marshalled into the <code>Enumerate</code>
	 *        element in the Body of the request.
	 * @return an enumeration context
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract EnumerationCtx enumerate(final Object filter, 
			final Map<String, String> namespaces, final String dialect,
			final boolean getEpr, final boolean getResource, 
			final boolean getItemCount, final boolean optimized, 
			final long timeout, final long maxElements, final Object... params)
	throws SOAPException, JAXBException, IOException,
	       FaultException, DatatypeConfigurationException;
	
	/**
	 * Assumes getEpr was set to true in the original
	 * {@link #enumerate(Object, Map, String, boolean, boolean)} call.
	 * Each EPR that is found in the
	 * returned resource state will be converted into its own Resource implementation.
	 * This is very useful when the response is a collection of EPR's such as when
	 * the getEpr parameter is set in the {@link #enumerate(Object,
	 * Map, String, boolean, boolean) enumerate()} call.
	 * 
	 * @param enumerationContext the context created in your call to enumerate
	 * @param maxTime the maxium timeout you are willing to wait for a response
	 * @param maxElements the maximum number of elements which should be returned
	 * @param maxCharacters the total number of the characters to be contained in
	 *        the returned message. If &lt;= 0 this parameter is ignored.
	 *        NOTE: Not all servers support this feature. 
	 * @param endpointUrl URL address of the service.
	 * @return array of Resources pulled
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundException
	 */
	public abstract Resource[] pullResources(final EnumerationCtx enumerationContext,
			final int maxTime, final int maxElements, 
			final int maxCharacters, final String endpointUrl)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException, XPathExpressionException, NoMatchFoundException;
	
	/**
	 * Requests a list of eprs or objects. If you request EPRs or some fragment
	 * of the state of an object this version of pull will just return them as
	 * a resource state and you will have to extract the EPRs yourself. Use pullResources
	 * for better access to EPRs.
	 * 
	 * @param enumerationContext the context created in your call to enumerate
	 * @param maxTime the maxium timeout you are willing to wait for a response
	 * @param maxElements the maximum number of elements which should be returned
	 * @param maxCharacters the total number of the characters to be contained in
	 *        the returned message. If &lt;= 0 this parameter is ignored.
	 *        NOTE: Not all servers support this feature. 
	 * @return A resource state representing the returned complex type of the pull. 
	 * Often this state will contain multiple entries from a number of resources.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState pull(final EnumerationCtx enumerationContext,
			final int maxTime, final int maxElements, final int maxCharacters)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException;
	
	/**
	 * Requests a list of EnumerationItem objects. Depending on the request
	 * options specified in the {@link #enumerate(Object, Map, String, boolean, boolean)}
	 * call this may return the resource, its EPR or both.
	 * 
	 * @param enumerationContext
	 *            The context created from a previous enumerate call.
	 * @param timeout the <code>OperationTimeout</code>. This is the
	 *        maximum amount of time in milliseconds the client is willing
	 *        to wait for the operation to complete.
	 *        If &lt;= 0 this parameter is ignored.
	 * @param maxElements
	 *        the maximum number of elements which should be returned
	 * @param maxEnvelopeSize the desired maximum envelope size for the returned SOAP
	 *        envelope. If &lt;= 0 this parameter is ignored.
	 * @param getItemCount if true indicates that the estimated item count should be returned
	 * @return a list of EnumerationItem objects
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract List<EnumerationItem> pullItems(EnumerationCtx enumerationContext,
			int timeout, int maxElements, int maxEnvelopeSize,
			boolean getItemCount) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException;

	/**
	 * Releases a context for enumeration.
	 * 
	 * @param enumerationContext
	 *            ID of the context to release
	 * @throws DatatypeConfigurationException
	 * @throws FaultException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public abstract void release(final EnumerationCtx enumerationContext)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException;
	
	/**
	 * Renew a context for enumeration.
	 * 
	 * @param enumerationContext
	 *            ID of the context to renew
	 * @throws DatatypeConfigurationException
	 * @throws FaultException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public abstract void renew(final EnumerationCtx enumerationContext) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

    /**
     * Get the total estimated item count, if available.
     * 
     * @return the total estimated item count.
     */
	public abstract Long getItemCount();
	
	/**
	 * Get the list of the EnumerationItem objects returned from the
	 * last successful optimized enumerate or pull.
	 * 
	 * @return list of EnumerationItem objects from last successful optimized enumerate or pull
	 */
	public abstract List<EnumerationItem> getEnumerationItems();
	
	/**
	 * Indicate if the end of sequence has been reached for this EnumerableResource.
	 * 
	 * @return true if this EnumerableResource is at the end of sequence
	 */
	public abstract boolean isEndOfSequence();

}