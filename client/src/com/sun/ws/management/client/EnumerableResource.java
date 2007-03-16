package com.sun.ws.management.client;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;

public interface EnumerableResource extends TransferableResource {

	/**
	 * Starts an enumeration transaction by obtaining an enumeration context.
	 * This is a ticket which must be used in all future calls to access this
	 * enumeration.
	 *
	 * @param filter a filter expression to be applied against the enumeration.
	 * @param dialect The dialect to be used in filter expressions. XPATH_DIALECT
	 * can be used for XPath. 
	 * @param useEprs  useEprs sets the EnumerateEpr Element causing subsequent pulls to
	 * contain erps
	 * @return An enumeration context
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract EnumerationCtx enumerate(final Object filter, 
			final Map<String, String> namespaces, final String dialect,
			final boolean useEprs, final boolean useObjects) 
	throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException;
	
	/**
	 * Assumes the return type will contain EPR's. Each EPR that is found in the
	 * returned resource state will be converted into its own Resource implementation.
	 * This is very useful when the response is a collection of EPR's such as when
	 * the UseEpr element is set.
	 * 
	 * @param enumerationContext the context create in your call to enumerate
	 * @param maxTime The maxium timeout you are willing to wait for a response
	 * @param maxElements the maximum number of elements which should be returned
	 * @param maxCharacters the total size of the characters to be contained in
	 * @param endpointUrl
	 * @return
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
	 * @param enumerationContext The context created from a previous enumerate call.
	 * @param maxTime The maxium timeout you are willing to wait for a response
	 * @param maxElements the maximum number of elements which should be returned
	 * @param maxCharacters the total size of the characters to be contained in
	 * the response
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
	 *            Nameof the context to release
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
	 * Releases a context for enumeration.
	 * 
	 * @param enumerationContext
	 *            Nameof the context to release
	 * @throws DatatypeConfigurationException
	 * @throws FaultException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public abstract void renew(final EnumerationCtx EnumerationCtx) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

}