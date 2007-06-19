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
 **Revision 1.24  2007/06/04 06:25:11  denis_rachal
 **The following fixes have been made:
 **
 **   * Moved test source to se/test/src
 **   * Moved test handlers to /src/test/src
 **   * Updated logging calls in HttpClient & Servlet
 **   * Fxed compiler warning in AnnotationProcessor
 **   * Added logging files for client junit tests
 **   * Added changes to support Maven builds
 **   * Added JAX-WS libraries to CVS ignore
 **
 **Revision 1.23  2007/05/30 20:30:28  nbeers
 **Add HP copyright header
 **
 ** 
 *
 * $Id: EnumerationResourceImpl.java,v 1.25 2007-06-19 19:50:39 nbeers Exp $
 */
package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.xpath.XPathExpressionException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableNonNegativeInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.EnumerableResource;
import com.sun.ws.management.client.EnumerationCtx;
import com.sun.ws.management.client.EnumerationResourceState;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.enumeration.EnumerationExtensions.Mode;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

public class EnumerationResourceImpl extends TransferableResourceImpl implements EnumerableResource {
	
	private static final String XPATH_TO_EPRS = "//*[namespace-uri()=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" and local-name()=\"EndpointReference\"]";

	protected Long itemCount = null;
	protected List<EnumerationItem> enumItems = null;

	private boolean endOfSequence = false;

	public EnumerationResourceImpl(String destination, String resourceURI,
			SelectorSetType selectors, XmlBinding binding) throws SOAPException, JAXBException {
		super(destination, resourceURI, selectors, binding);
	}

	public EnumerationResourceImpl(Element eprElement, String endpointUrl, XmlBinding binding)
			throws SOAPException, JAXBException {
		super(eprElement, endpointUrl, binding);
	}

	// attributes
	// ******************* WS Enumeration *************************************

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
	public EnumerationCtx enumerate(final Object filter,
			final Map<String, String> namespaces, final String dialect,
			final boolean getEpr, final boolean getResource)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		Object[] params = new Object[]{};
		return enumerate(filter, namespaces, dialect, getEpr, getResource,
				true, false, 0, 0, params);

	}
	
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
	public EnumerationCtx enumerate(final Object filter,
			final Map<String, String> namespaces, final String dialect,
			final boolean getEpr, final boolean getResource,
			final boolean getItemCount, final boolean optimized,
			final long timeout, final long maxElements)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		Object[] params = new Object[]{};
		return enumerate(filter, namespaces, dialect, getEpr, getResource,
				         getItemCount, optimized, timeout, maxElements, params);
	}
	
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
	 *        the maximum number of elements which should be returned
	 * @param params additional parameters to be marshalled into the <code>Enumerate</code>
	 *        element in the Body of the request.
	 * @return an enumeration context
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public EnumerationCtx enumerate(final Object filter,
			final Map<String, String> namespaces, final String dialect,
			final boolean getEpr, final boolean getResource,
			final boolean getItemCount, final boolean optimized,
			final long timeout, final long maxElements, final Object... params)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		String enumerationContextId = "";

		this.setMessageTimeout(timeout);
		final Transfer xf = setTransferProperties(Enumeration.ENUMERATE_ACTION_URI);
		final Management mgmt = setManagementProperties(xf);
		addOptionSetHeader(mgmt);
		if (getMessageTimeout() > 0) {
			final Duration duration = DatatypeFactory.newInstance()
					.newDuration(getMessageTimeout());
			mgmt.setTimeout(duration);
		}
		final EnumerationExtensions enu = new EnumerationExtensions(mgmt);

		// Enum Mode
		Mode enumerationMode = null;
		if (getEpr) {
			enumerationMode = EnumerationExtensions.Mode.EnumerateEPR;
		}

		if (getEpr && getResource) {
			enumerationMode = EnumerationExtensions.Mode.EnumerateObjectAndEPR;
		}

		if (filter != null) {
			final DialectableMixedDataType filterType = Management.FACTORY
					.createDialectableMixedDataType();
			if (dialect == null)
				filterType.setDialect(XPath.NS_URI);
			else
				filterType.setDialect(dialect);
			filterType.getContent().add(filter);

			enu.setEnumerate(null, getItemCount, optimized, (int) maxElements,
					null, filterType, enumerationMode, params);
			enu.setFilterNamespaces(namespaces);
		} else {
			enu.setEnumerate(null, getItemCount, optimized, (int) maxElements,
					null, null, enumerationMode, params);
		}

		final Addressing response = HttpClient.sendRequest(mgmt);
		response.setXmlBinding(mgmt.getXmlBinding());

		// Check for fault during message generation
		if (response.getBody().hasFault()) {
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault);
		}

		final EnumerationExtensions enuResponse = new EnumerationExtensions(
				response);
		final EnumerateResponse enr = enuResponse.getEnumerateResponse();

		enumerationContextId = (String) enr.getEnumerationContext()
				.getContent().get(0);

		if (getItemCount) {
			if (enuResponse.getTotalItemsCountEstimate() != null)
				itemCount = enuResponse.getTotalItemsCountEstimate().getValue()
						.longValue();
		}

		EnumerationCtx context = new EnumerationCtx(enumerationContextId);
		if (optimized) {
			// Save the returned values from the optimized enumeration
			enumItems = enuResponse.getItems();
			endOfSequence = enuResponse.isEndOfSequence();

			if (enumItems == null) {
				// Server side does not support 'optimize'
				// Try to fetch the data for the caller.
				if (endOfSequence == false) {
					int maxTime = 0;
					int maxEnvelopeSize = 0;
					if (mgmt.getTimeout() != null) {
						final GregorianCalendar now = new GregorianCalendar();
						maxTime = (int) mgmt.getTimeout().getTimeInMillis(now);
					}
					if (mgmt.getMaxEnvelopeSize() != null) {
						maxEnvelopeSize = (int) mgmt.getMaxEnvelopeSize()
								.getValue().longValue();
					}
					// pull the data
					executePull(context, maxTime, (int) maxElements,
							maxEnvelopeSize, getItemCount);
				} else {
					// Set an empty list
					enumItems = new ArrayList<EnumerationItem>(0);
				}
			}
		}
		return context;
	}

	/**
	 * Assumes the return type will contain EPR's. Each EPR that is found in the
	 * returned resource state will be converted into its own Resource
	 * implementation. This is very useful when the response is a collection of
	 * EPR's such as when the UseEpr element is set.
	 * 
	 * @param enumerationContext
	 *            the context create in your call to enumerate
	 * @param timeout the <code>OperationTimeout</code>. This is the
	 *        maximum amount of time in milliseconds the client is willing
	 *        to wait for the operation to complete.
	 *        If &lt;= 0 this parameter is ignored.
	 * @param maxElements
	 *        the maximum number of elements which should be returned
	 * @param maxEnvelopeSize the desired maximum envelope size for the returned SOAP
	 *        envelope. If &lt;= 0 this parameter is ignored.
	 * @param endpointUrl
	 * @return
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 * @throws NoMatchFoundException
	 * @throws XPathExpressionException
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundException
	 */
	public Resource[] pullResources(EnumerationCtx enumerationContext,
			int timeout, int maxElements, int maxEnvelopeSize,
			String endpointUrl) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException,
			XPathExpressionException, NoMatchFoundException {
		ResourceState resState = pull(enumerationContext, timeout, maxElements,
				maxEnvelopeSize);
		NodeList eprNodes = null;
		eprNodes = resState.getValues(XPATH_TO_EPRS);
		Vector<ResourceImpl> ret = new Vector<ResourceImpl>();
		for (int index = 0; index < eprNodes.getLength(); index++) {
			Element eprElement = (Element) eprNodes.item(index);
			ret.add(new ResourceImpl(eprElement, endpointUrl, getBinding()));
		}
		return (Resource[]) ret.toArray(new Resource[] {});
	}

	/**
	 * Requests a list of erps or objects. If you request EPRs or some fragment
	 * of the state of an object this version of pull will just return them as a
	 * resource state and you will have to extract the EPRs yourself. Use
	 * pullResources for better access to EPRs.
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
	 * @return A resource state representing the returned complex type of the
	 *         pull. Often this state will contain multiple entries from a
	 *         number of resources.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public EnumerationResourceState pull(EnumerationCtx enumerationContext,
			int timeout, int maxElements, int maxEnvelopeSize)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		return pull(enumerationContext, timeout, maxElements, maxEnvelopeSize,
				false);
	}

	/**
	 * Requests a list of erps or objects. If you request EPRs or some fragment
	 * of the state of an object this version of pull will just return them as a
	 * resource state and you will have to extract the EPRs yourself. Use
	 * pullResources for better access to EPRs.
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
	 * @param getItemCount
	 *            Retrieve the total number of enumerated items
	 * @return A resource state representing the returned complex type of the
	 *         pull. Often this state will contain multiple entries from a
	 *         number of resources.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public EnumerationResourceState pull(EnumerationCtx enumerationContext,
			int timeout, int maxElements, int maxEnvelopeSize,
			boolean getItemCount) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {

		SOAPBody body = executePull(enumerationContext, timeout, maxElements,
				maxEnvelopeSize, getItemCount);
		return new EnumerationResourceStateImpl(body.extractContentAsDocument());
	}

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
	public List<EnumerationItem> pullItems(EnumerationCtx enumerationContext, int timeout, int maxElements, int maxEnvelopeSize, boolean getItemCount) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
		executePull(enumerationContext, timeout, maxElements, maxEnvelopeSize, getItemCount);
		return enumItems;
	}
	
	private SOAPBody executePull(EnumerationCtx enumerationContext,
			int maxTime, int maxElements, int maxEnvelopeSize,
			boolean getItemCount) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {

		// Now generate the request for an EnumCtxId with parameters passed in
		final EnumerationExtensions enu = setEnumerationProperties(Enumeration.PULL_ACTION_URI);
		Duration timeout = null;
		if (maxTime > 0)
			timeout = DatatypeFactory.newInstance().newDuration(maxTime);
		enu.setPull(enumerationContext.toString(), maxEnvelopeSize,
				maxElements, timeout);
		final Management mgmt = setManagementProperties(enu);
		if (timeout != null) {
			mgmt.setTimeout(timeout);
		}

		// Add any user defined options to the header
		addOptionSetHeader(mgmt);

		if (getItemCount) {
			enu.setRequestTotalItemsCountEstimate();
		}

		final Addressing response = HttpClient.sendRequest(mgmt);
		response.setXmlBinding(mgmt.getXmlBinding());

		// Check for fault during message generation
		if (response.getBody().hasFault()) {
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault);
		}

		final EnumerationExtensions enuResponse = new EnumerationExtensions(
				response);

		updateEnumContext(enuResponse, enumerationContext);

		if (getItemCount) {
			AttributableNonNegativeInteger countElement = enuResponse
					.getTotalItemsCountEstimate();
			if (countElement != null) {
				itemCount = enuResponse.getTotalItemsCountEstimate().getValue()
						.longValue();
			}
		}

		// Save a reference to the enumItems
		enumItems = enuResponse.getItems();
		endOfSequence = enuResponse.isEndOfSequence();
		return response.getBody();
	}

	private EnumerationExtensions setEnumerationProperties(String action)
			throws JAXBException, SOAPException {
		final EnumerationExtensions enu = new EnumerationExtensions();

		if (getBinding() != null)
			enu.setXmlBinding(getBinding());
		enu.setAction(action);
		enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
		enu.setMessageId("uuid:" + UUID.randomUUID().toString());
		return enu;
	}

	/**
	 * The spec indicates that the Enumeration Context representation can be
	 * changed during each subsequent pull. To support this we must check for an
	 * EnumerationContext element present in the PullResponse and update our
	 * token class enumerationContext so that it remain consistant to the client
	 * user as well as the server.
	 * 
	 * @param enuResponse
	 * @param enumerationContext
	 * @throws SOAPException
	 * @throws JAXBException
	 */
	private void updateEnumContext(Enumeration enuResponse,
			EnumerationCtx enumerationContext) throws JAXBException,
			SOAPException {
		EnumerationContextType pullEnumContext = enuResponse.getPullResponse()
				.getEnumerationContext();
		if (pullEnumContext == null || pullEnumContext.getContent() == null)
			return;
		List<Object> content = pullEnumContext.getContent();
		if (content.size() == 0)
			return;
		enumerationContext.setContext((String) content.get(0));

	}

	/**
	 * Releases a context for enumeration.
	 * 
	 * @param enumerationContext ID of the context to release
	 * @throws DatatypeConfigurationException
	 * @throws FaultException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public void release(EnumerationCtx enumerationContext)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		// Now generate the request for an EnumCtxId with parameters passed in
		final Enumeration enu = setEnumerationProperties(Enumeration.RELEASE_ACTION_URI);
		enu.setRelease(enumerationContext.toString());

		final Management mgmt = setManagementProperties(enu);

		// Add any user defined options to the header
		addOptionSetHeader(mgmt);

		final Addressing response = HttpClient.sendRequest(mgmt);
		response.setXmlBinding(mgmt.getXmlBinding());

		// Check for fault during message generation
		if (response.getBody().hasFault()) {
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault);
		}
	}

	/**
	 * Releases a context for enumeration.
	 * 
	 * @param enumerationContext ID of the context to release
	 * @throws DatatypeConfigurationException
	 * @throws FaultException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public void renew(EnumerationCtx enumerationContext) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		// Now generate the request for an EnumCtxId with parameters passed in
		final Enumeration enu = setEnumerationProperties(Enumeration.RENEW_ACTION_URI);
		enu.setRelease(enumerationContext);

		final Management mgmt = setManagementProperties(enu);

		// Add any user defined options to the header
		addOptionSetHeader(mgmt);

		final Addressing response = HttpClient.sendRequest(mgmt);
		response.setXmlBinding(mgmt.getXmlBinding());

		// Check for fault during message generation
		if (response.getBody().hasFault()) {
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault);
		}
	}

	public Long getItemCount() {
		return itemCount;
	}

	public List<EnumerationItem> getEnumerationItems() {
		return enumItems;
	}

	public boolean isEndOfSequence() {
		return endOfSequence;
	}
}
