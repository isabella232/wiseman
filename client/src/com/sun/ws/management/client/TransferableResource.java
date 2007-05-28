package com.sun.ws.management.client;

import java.io.IOException;
import java.util.HashSet;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.w3c.dom.Document;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.xml.XmlBinding;

/**
 * An abstract representation of a WSManagement resource that focuses on 
 * WS-Transfer. Provides the basis for implementation of enumeration. 
 * 
 * @see EnumerableResource
 * @author wire
 * @author spinder
 * 
 */
public interface TransferableResource {
    public static final ObjectFactory managementFactory = new ObjectFactory();

	
	/**
	 * Returns the SelectorSet used by this resource.
	 * @return a SelectorSet.
	 */
	public SelectorSetType getSelectorSet();



	/** Generates a DELETE request over WS-Man protocol for this resource.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 * @throws AccessDeniedFault
	 */
	public abstract void delete() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException;

	/** Generates a fragment DELETE request over WS-Man protocol.
	 * @param fragmentRequest fragment expression
	 * @param fragmentDialect the dialect used in fragment expression.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 *        can be used for XPath expressions
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 * @throws AccessDeniedFault
	 */
	public abstract void delete(String fragmentRequest, String fragmentDialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException, AccessDeniedFault;
   

	/** Generates a PUT request over WS-Man protocol with contents of Document 
	 *  passed in.
	 * @param content a w3c document representing the resource to put
	 * @return {@link ResourceState} representing the resource after the put
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState put(Document content) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	/** Generates a fragment PUT request over WS-Man protocol with the fragment
	 *  for update defined by fragmentExpression, using the fragmentDialect to be 
	 *  updated with the contents of the Document passed in,.
	 * @param content a w3c document representing the fragment resource to put
	 * @param fragmentExpression fragment expression
	 * @param fragmentDialect the dialect used in fragment expression.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 *        can be used for XPath expressions
	 * @return {@link ResourceState} representing the resource after the put
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState put(Document content, String fragmentExpression,
			String fragmentDialect) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException;

	/** Generates a PUT request over WS-Man protocol with contents of ResourceState 
	 *  passed in.
	 * @param newState the resource to update
	 * @return {@link ResourceState} representing the resource after the put
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public ResourceState put(ResourceState newState) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	
	/** Generates a WS-Man GET message and returns the contents of the Resource
	 *  as a ResoruceState instance.
	 *  
	 * @return {@link ResourceState} representing the resource obtained
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState get() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException;

	/** Generates a WS-Man fragment GET message with fragmentExpression defining
	 *  content to operate on in the agreed upon dialect and returns the contents 
	 *  of the Resource as a ResoruceState instance.
	 *  
	 * @param fragmentExpression fragment expression for selecting parts of a resource
	 * @param dialect the dialect used in fragment expression.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 *        can be used for XPath expressions
	 * @return {@link ResourceState} representing the fragment resource obtained
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState get(String fragmentExpression, String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	/**
	 * Get the ResourceURI set for this Resource.
	 * @return resourceURI
	 */
	public abstract String getResourceUri();
	
	/**
	 * Set the ResourceURI for this Resource.
	 * @param uri URI identifying the resource
	 */
	public abstract void setResourceUri(String uri);

	/**
	 * Get the destination URL address of the resource.
	 * 
	 * @return URL address set for this resource
	 */
	public abstract String getDestination();
	
	/**
	 * Set the destination URL address of the resource.
	 * @param destination URL address for this resource
	 */
	public abstract void setDestination(String destination);

	/**
	 * Get the currently set message timeout value. This value
	 * sets the WS Management <code>OperationTimeout</code> 
	 * control header in the request.
	 * 
	 * @return currently set timeout value.
	 * If &lt;= 0 <code>OperationTimeout</code> will not be set in the request message.
	 */
	public abstract long getMessageTimeout();
	
	/**
	 * Set the message timeout value. This value
	 * sets the WS Management <code>OperationTimeout</code> 
	 * control header in the request.
	 * 
	 * @param i number of milliseconds.
	 * If &lt;= 0 <code>OperationTimeout</code> will not be set in the request message.
	 */
	public abstract void setMessageTimeout(long i);
	
	/**
	 * Sets the maximum envelope size desired for the SOAP response.
	 * This value set the WS Management <code>MaxEnvelopeSize</code>
	 * control header in the request.
	 * 
	 * @param i maximum desired size in characters.
	 * If &lt;= 0 <code>MaxEnvelopeSize</code> will not be set in the request message.
	 */
	public abstract void setMaxEnvelopeSize(long i);
	
	/**
	 * Get the currently set maximum envelope size
	 * desired for the SOAP response.
	 * This value set the WS Management <code>MaxEnvelopeSize</code>
	 * control header in the request.
	 * 
	 * @return currently set maximum desired size in characters.
	 * If &lt;= 0 <code>MaxEnvelopeSize</code> will not be set in the request message. 
	 */
	public abstract long getMaxEnvelopeSize();
	
	/**
	 * Get the WS Management ReplyTo value to set in the request message.
	 * 
	 * @return currently set value. Default is {@link Addressing#ANONYMOUS_ENDPOINT_URI}
	 */
	public abstract String getReplyTo();
	
	/**
	 * Set the desired WS Management ReplyTo value to set in the request message.
	 * 
	 * @param replyTo URL to reply to
	 */
	public abstract void setReplyTo(String replyTo);
	
	/**
	 * Add an option to the option set
	 * 
	 * @param name option name
	 * @param value option value
	 */
	public abstract void addOption(String name, Object value);
	
	/**
	 * Add an option to the option set
	 * 
	 * @param name option name
	 * @param value option value
	 * @param mustComply option must comply flag
	 */
	public abstract void addOption(String name, Object value, boolean mustComply);
	
	/**
	 * Add an option to the option set
	 * 
	 * @param name option name
	 * @param value option value
	 * @param type qualified type of the option
	 * @param mustComply option must comply flag
	 */
	public abstract void addOption(String name, Object value, QName type, boolean mustComply);
	
	/**
	 * Add an option to the option set
	 * 
	 * @param name option name
	 * @param value option value
	 * @param type qualified type of the option
	 */
	public abstract void addOption(String name, Object value, QName type);
	
	/**
	 * Get the current option set
	 * 
	 * @return Returns the optionSet.
	 */
	public abstract HashSet<OptionType> getOptionSet();

	/**
	 * Remove all of the current options from the option set
	 * 
	 */
	public abstract void resetOptionSet();
	
    /**
     * Gets the XmlBinding used by this Resource.
     * 
     * @return the XmlBinding used by this Resource
     */
	public abstract XmlBinding getBinding();

    /**
     * Sets the XmlBinding used by this Resource.
     * 
     * @param binding the XmlBinding used by this Resource
     */
	public abstract void setBinding(XmlBinding binding);
}