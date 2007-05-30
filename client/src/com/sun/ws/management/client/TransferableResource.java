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
 * $Id: TransferableResource.java,v 1.9 2007-05-30 20:30:21 nbeers Exp $
 */
package com.sun.ws.management.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

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
 * An abstract representation of a WS Management resource that focuses on 
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



	/** 
	 * Generates a DELETE request over WS Management protocol for this resource.
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

	/** 
	 * Generates a fragment DELETE request over WS Management protocol.
	 * 
	 * @param expression a filter expression to be applied against the resource.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT}
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 * @throws AccessDeniedFault
	 */
	public abstract void delete(final Object expression, 
			                    final String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException, AccessDeniedFault;

	/** 
	 * Generates a fragment DELETE request over WS Management protocol.
	 * 
	 * @param expression a filter expression to be applied against the resource.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param namespaces prefix and namespace map for namespaces used in the filter
	 *        expression.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 * @throws AccessDeniedFault
	 */
	public abstract void delete(final Object expression, 
			                    final Map<String, String> namespaces,
			                    final String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException, AccessDeniedFault;

	/** 
	 * Generates a PUT request over WS Management protocol with contents of Document 
	 * passed in.
	 * 
	 * @param content a w3c document representing the resource to put
	 * @return {@link ResourceState} representing the resource after the put
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState put(final Document content) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	/** 
	 * Generates a fragment PUT request over WS Management protocol with the fragment
	 * for update defined by fragmentExpression, using the fragmentDialect to be 
	 * updated with the contents of the Document passed in.
	 * 
	 * @param content a w3c document representing the fragment resource to put
	 * @param expression a filter expression to be applied against the resource.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 * @return {@link ResourceState} representing the resource after the put
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState put(final Document content, 
			                          final Object expression, 
                                      final String dialect) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException;
	
	/** 
	 * Generates a fragment PUT request over WS Management protocol with the fragment
	 * for update defined by fragmentExpression, using the fragmentDialect to be 
	 * updated with the contents of the Document passed in.
	 * 
	 * @param content a w3c document representing the fragment resource to put
	 * @param expression a filter expression to be applied against the resource.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param namespaces prefix and namespace map for namespaces used in the filter
	 *        expression.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} 
	 * @return {@link ResourceState} representing the resource after the put
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState put(final Document content, 
                                      final Object expression, 
                                      final Map<String, String> namespaces,
                                      final String dialect) throws SOAPException, JAXBException,
IOException, FaultException, DatatypeConfigurationException;

	/** 
	 * Generates a PUT request over WS Management protocol with contents of ResourceState 
	 * passed in.
	 * 
	 * @param newState the resource to update
	 * @return {@link ResourceState} representing the resource after the put
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public ResourceState put(final ResourceState newState) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	
	/** 
	 * Generates a WS Management GET message and returns the contents of the Resource
	 * as a ResoruceState instance.
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

	/** 
	 * Generates a WS Management fragment GET message with fragmentExpression defining
	 * content to operate on in the agreed upon dialect and returns the contents 
	 * of the Resource as a ResoruceState instance.
	 *  
	 * @param expression a filter expression to be applied against the resource.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT}
	 * @return {@link ResourceState} representing the fragment resource obtained
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState get(final Object expression, 
                                      final String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException;
	

	/** 
	 * Generates a WS Management fragment GET message with fragmentExpression defining
	 * content to operate on in the agreed upon dialect and returns the contents 
	 * of the Resource as a ResoruceState instance.
	 *  
	 * @param expression a filter expression to be applied against the resource.
	 *        For {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT} this should be a string
	 *        containing the XPath expression. For other dialects this must be an
	 *        object recognized by the marshaller.
	 * @param namespaces prefix and namespace map for namespaces used in the filter
	 *        expression.
	 * @param dialect the dialect to be used in filter expressions.
	 *        {@link Resource#XPATH_DIALECT Resource.XPATH_DIALECT}
	 * @return {@link ResourceState} representing the fragment resource obtained
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceState get(final Object expression,
			                          final Map<String, String> namespaces,
                                      final String dialect)
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
	public abstract void setDestination(final String destination);

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
	public abstract void setMessageTimeout(final long i);
	
	/**
	 * Sets the maximum envelope size desired for the SOAP response.
	 * This value set the WS Management <code>MaxEnvelopeSize</code>
	 * control header in the request.
	 * 
	 * @param i maximum desired size in characters.
	 * If &lt;= 0 <code>MaxEnvelopeSize</code> will not be set in the request message.
	 */
	public abstract void setMaxEnvelopeSize(final long i);
	
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
	public abstract void setReplyTo(final String replyTo);
	
	/**
	 * Add an option to the option set
	 * 
	 * @param name option name
	 * @param value option value
	 */
	public abstract void addOption(final String name, 
			                       final Object value);
	
	/**
	 * Add an option to the option set
	 * 
	 * @param name option name
	 * @param value option value
	 * @param mustComply option must comply flag
	 */
	public abstract void addOption(final String name, 
			                       final Object value,
			                       final boolean mustComply);
	
	/**
	 * Add an option to the option set
	 * 
	 * @param name option name
	 * @param value option value
	 * @param type qualified type of the option
	 * @param mustComply option must comply flag
	 */
	public abstract void addOption(final String name,
			                       final Object value,
			                       final QName type,
			                       final boolean mustComply);
	
	/**
	 * Add an option to the option set
	 * 
	 * @param name option name
	 * @param value option value
	 * @param type qualified type of the option
	 */
	public abstract void addOption(final String name,
			                       final Object value,
			                       final QName type);
	
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