package com.sun.ws.management.client;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;

/**
 * An abstract representation of a WSManagement resource that focuses on 
 * WS-Enumeration. 
 * 
 * @see TransferableResource
 */
public interface EnumerableResource extends TransferableResource {

	/**
	 * Starts an enumeration transaction by obtaining an enumeration context.
	 * This is a ticket which must be used in all future calls to access this
	 * enumeration.
	 *
	 * @param filter a filter expression to be applied against the enumeration.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 *        can be used for XPath expressions. 
	 * @param getEpr indicates that the EndpointReference (EPR) for each item is to
	 *        returned in the pull.
	 * @param getObject indicates that the individual items are to be returned in
	 *        the pull.
	 * @return An enumeration context
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract EnumerationCtx enumerate(final Object filter, 
			final Map<String, String> namespaces, final String dialect,
			final boolean getEpr, final boolean getObject) 
	throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException;
	
	/**
	 * Assumes the return type will contain EPR's. Each EPR that is found in the
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

}