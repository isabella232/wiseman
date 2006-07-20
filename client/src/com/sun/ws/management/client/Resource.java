package com.sun.ws.management.client;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;

/**
 * An abstract representation of a WSManagement resource. Resources are
 * manufactured by the Resource Factory and contain an EPR which acts as a
 * target for all of their functions.
 * 
 * Still needs support for... Support for Max EnvelopeSize Support for
 * OptionsSet Support Fragments for partial sets of properties
 * 
 * @author wire
 * @author spinder
 * 
 */
public interface Resource extends EnumerableResource{

	static final String XPATH_DIALECT = "http://www.w3.org/TR/1999/REC-xpath-19991116";

	static final int IGNORE_MAX_CHARS = 0;

	// ****************************** WS Transfer ******************************

	/**
	 * Gets the state of this resource as a resource state object.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public ResourceState get() throws SOAPException, JAXBException,
	IOException, FaultException, DatatypeConfigurationException;

	/**
	 * Gets a fragment state of this resource as a resource state object.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public ResourceState get(String pathReq,String dialect) throws SOAPException,JAXBException, IOException, FaultException,DatatypeConfigurationException ;
	
	/**
	 * Replaces the state of this resource with the provided resource state.
	 * 
	 * @param newState
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public void put(ResourceState newState) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	/**
	 * Replaces the state of this resource with the provided w3c document.
	 * 
	 * @param content
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public void put(Document content) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException;

	/**
	 * Instructs this resource to delete itself from the server. Future calls to
	 * this resource will not be vaild.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public void delete() throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException;

	// TODO wire Add support for RequestTotalItemsCountEstimate 
	// ******************* WS Enumeration *************************************
	/**
	 * Starts an enumeration transaction by obtaining an enumeration context.
	 * This is a ticket which must be used in all future calls to access this
	 * enumeration.
	 *
	 * @param filters and array of filter expressions to be applied to the
	 * enumeration.
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
//	public String enumerate(String[] filters, String dialect, boolean useEprs)
//			throws SOAPException, JAXBException, IOException, FaultException,
//			DatatypeConfigurationException;

	// TODO wire this needs a meaningful implementation 
	// TODO wire must implement EnumerateObjectAndEpr
	/**
	 * Performs an optimized numeration which returns all results in response.
	 * 
	 * @param filters a set of filters the server should use on the list
	 * @param dialect the dialect that the above filters use. XPATH_DIALECT
	 * can be used for XPath. 
	 * @param useEprs sets the EnumerateEpr Element causing subsequent pulls to
	 * contain erps
	 * @return A resource state containing the enumeration response.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public ResourceState optomizedEnumerate(String[] filters, String dialect, boolean useEprs)
	throws SOAPException, JAXBException, IOException, FaultException,
	DatatypeConfigurationException;
 	
	// TODO wire Add support for RequestTotalItemsCountEstimate 
	// TODO wire must implement EnumerateObjectAndEpr
	/**
	 * Requests a list of erps or objects. If you request EPRs or some fragment
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
//	public ResourceState pull(String enumerationContext, int maxTime,
//			int maxElements, int maxCharacters) throws SOAPException,
//			JAXBException, IOException, FaultException,
//			DatatypeConfigurationException;

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
//	public Resource[] pullResources(String enumerationContext, int maxTime,
//			int maxElements, int maxCharacters, String endpointUrl)
//			throws SOAPException, JAXBException, IOException, FaultException,
//			DatatypeConfigurationException, XPathExpressionException,
//			NoMatchFoundException;

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
//	public void release(String enumerationContext) throws SOAPException,
//			JAXBException, IOException, FaultException,
//			DatatypeConfigurationException;

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
//	public void renew(String enumerationContext) throws SOAPException,
//			JAXBException, IOException, FaultException,
//			DatatypeConfigurationException;

	// **************************** WS Eventing *****************************

	/**
	 * Eventing creates as subscription. The returned EPR is
	 * to a subscription manager which should be an extention of Resource with
	 * additional methods. All additional calls to modify the subscritpion
	 * should go to the subscription manager.
	 */
//	public EndpointReferenceType subscribe(EndpointReferenceType EndToEpr,
//			String deliveryType, Duration expires, String[] filters,
//			String dialect);

	// TODO wire SHould really be returning a resource state.
	/**
	 * Invokes a custom operation on the resource.
	 * @param action An action URI name for the custom operation
	 * @param parameters A map of parameters to pass.
	 * @return Raw Jaxb return types from the body
	 */
	public Object[] invoke(QName action, Map<QName, String> parameters);
 	/**
	 * Returns the resourceUri that this resource represents.
	 */
	public String getResourceUri();

	/**
	 * Gets the destination URL of this resource as a string.
	 * @return a URL as a string.
	 */
	public String getDestination();

	public long getMessageTimeout();
	/**
	 * Returns the selectorset used by this resource.
	 * @return A JAXB selector set.
	 */
	public org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType getSelectorSet();

//	public ResourceState get(String pathReq,String dialect) throws SOAPException,JAXBException, IOException, FaultException,DatatypeConfigurationException ;

}
