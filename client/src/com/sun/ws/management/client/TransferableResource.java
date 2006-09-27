package com.sun.ws.management.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.w3c.dom.Document;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.client.exceptions.FaultException;

/**
 * An abstract representation of a WSManagement resource that focuses on 
 *  WS-Transfer. Provides the basis for implementation of enumeration. 
 * 
 * @author wire
 * @author spinder
 * 
 */
public interface TransferableResource {
    public static final ObjectFactory managementFactory = new ObjectFactory();

	
	/**
	 * Returns the selectorset used by this resource.
	 * @return A JAXB selector set.
	 */
	public org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType getSelectorSet();



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
	 * @param fragmentRequest 
	 * @param fragmentDialect
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
	 * @param content 
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
	 * @param content 
	 * @param fragmentExpression 
	 * @param fragmentDialect 
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
	 * @param content
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
	 * @return Resource contents as a ResourceState object.
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
	 * @return Resource contents as a ResourceState object.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState get(String fragmentExpression, String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	public abstract String getResourceUri();
	public abstract void setResourceUri(String uri);

	public abstract String getDestination();
	public abstract void setDestination(String destination);

	public abstract long getMessageTimeout();
	public abstract void setMessageTimeout(long i);
	
	public abstract void setMaxEnvelopeSize(long i);
	public abstract long getMaxEnvelopeSize();
	
	public abstract String getReplyTo();
	public abstract void setReplyTo(String replyTo);
	
	/**
	 * Add an option to the option set
	 * @param name option name
	 * @param value option value
	 */
	public void addOption(String name, Object value);
	
	/**
	 * Add an option to the option set
	 * @param name option name
	 * @param value option value
	 * @param mustComply option must comply flag
	 */
	public void addOption(String name, Object value, boolean mustComply);
	
	/**
	 * Add an option to the option set
	 * @param name option name
	 * @param value option value
	 * @param mustComply option must comply flag
	 */
	public void addOption(String name, Object value, QName type, boolean mustComply);
	
	/**
	 * Add an option to the option set
	 * @param name option name
	 * @param value option value
	 * @param mustComply option must comply flag
	 */
	public void addOption(String name, Object value, QName type);
	
		/**
	 * @return Returns the optionSet.
	 */
	public HashSet<OptionType> getOptionSet(); 
	/**
	 * Remove all of the current options from the option set
	 *
	 */
	public void resetOptionSet();


}