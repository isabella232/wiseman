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
 * $Id: TransferableResourceImpl.java,v 1.15 2007-05-30 20:30:29 nbeers Exp $
 */
package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.MaxEnvelopeSizeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.TransferableResource;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.server.BaseSupport;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

public class TransferableResourceImpl implements TransferableResource {

	protected static final String UUID_SCHEME = "uuid:";

	private static Logger log = Logger.getLogger(TransferableResourceImpl.class
			.getName());

	public static final QName FRAGMENT_TRANSFER = new QName(Management.NS_URI,
			"FragmentTransfer", Management.NS_PREFIX);

	public static final QName DIALECT = new QName("Dialect");

	public XmlBinding binding = null;

	// Attributes
	/**
	 * The Resource URI that should be use to refer to this resource when calls
	 * are made to it.
	 */
	protected String resourceURI = null; // Specific resource access details

	/**
	 * The URL as a string for the HTTP transport target for this transfer
	 * resource.
	 */
	protected String destination = null; // Specific host and port access
											// details

	/**
	 * The default timeout for all transfer operations
	 */
	protected long messageTimeout = 30000; // Message timeout before reaping

	/**
	 * This represents the selector set used to identify this resource. If it is
	 * null there will be no selector set encoded into your requests.
	 */
	protected SelectorSetType selectorSet = null;

	/**
	 * Used to indicate the max size of a response to any transfer request made
	 * by this resource. If it is -1 then it will not appear in your requests.
	 */
	protected long maxEnvelopeSize = -1;

	private String replyTo = Addressing.ANONYMOUS_ENDPOINT_URI;

	protected HashSet<OptionType> optionSet = null;

	/**
	 * A Transferable resource is package local. It is not intended to be
	 * constructed by anything besides its factory and its unit tests. Please to
	 * not make it public.
	 * 
	 * @param destination
	 *            A URL the represents the endpoint of this operation.
	 * @param resourceURI
	 *            a resource URI to refere to this type of resource.
	 * @param timeout
	 *            the default timeout to be used in operations.
	 * @param selectors
	 *            a set of selectors to use to identify this resource uniquely.
	 * @throws SOAPException
	 * @throws JAXBException
	 */
	TransferableResourceImpl(String destination, String resourceURI,
			SelectorSetType selectors, XmlBinding binding)
			throws SOAPException, JAXBException {
		setDestination(destination);
		setResourceUri(resourceURI);
		this.selectorSet = selectors;
		this.binding = binding;

		initJAXB();
	}

	/**
	 * This constructor is intended to allow atransfereable resource to be
	 * constructed as part of an EPR enumeration. It is expected that the
	 * provided epr contains all the required selectors for this resource to be
	 * located.
	 * 
	 * @param eprElement
	 * @param endpointUrl
	 * @throws SOAPException
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	TransferableResourceImpl(Element eprElement, String endpointUrl,
			XmlBinding binding) throws SOAPException, JAXBException {
		initJAXB();
		if (binding == null)
			binding = new XmlBinding(null);
		this.binding = binding;
		EndpointReferenceType epr = null;
		epr = ((JAXBElement<EndpointReferenceType>) this.binding
				.unmarshal(eprElement)).getValue();

		// Determine callers return address. If anonymous, then use calling
		// ERP's address
		this.destination = epr.getAddress().getValue();
		if (destination
				.equals("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"))
			if (endpointUrl != null)
				this.destination = endpointUrl;

		// Repack ref params as separate resource URI and Selectors
		List<Object> refParams = epr.getReferenceParameters().getAny();
		for (Object param : refParams) {
			Object testType = ((JAXBElement) param).getValue();
			if (testType instanceof AttributableURI) {
				AttributableURI rUri = (AttributableURI) testType;
				setResourceUri(rUri.getValue());
			}
			if (testType instanceof SelectorSetType) {
				this.selectorSet = (SelectorSetType) testType;
			}
		}
	}

	/**
	 * If the client is the first part of wiseman to be used in this VM then
	 * this operation makes sure it has been properly initalized before any
	 * transfer is attempted.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 */
	private void initJAXB() throws SOAPException, JAXBException {
		// initialize JAXB bindings
		// Message.initialize();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#delete()
	 */
	public void delete() throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException {
		delete(null, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#delete(java.lang.String,
	 *      java.lang.String)
	 */
	public void delete(final Object expression, final String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException, AccessDeniedFault {
		delete(expression, null, dialect);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#delete(java.lang.Object,
	 *      java.lang.Map, java.lang.String)
	 */
	public void delete(final Object expression,
			final Map<String, String> namespaces, final String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException, AccessDeniedFault {
		// Build the document
		final Transfer xf = setTransferProperties(Transfer.DELETE_ACTION_URI);
		final Management mgmt = setManagementProperties(xf);

		// If expression is not null then generate fragment header
		if (expression != null) {
			// Add the Fragement Header
			setFragmentHeader(expression, namespaces, dialect, mgmt);
		}

		// populate the Selector map
		Set<org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType> selectors1 = new HashSet<SelectorType>();
		if (selectorSet != null) {
			List<SelectorType> selectors = selectorSet.getSelector();
			for (Iterator iter = selectors.iterator(); iter.hasNext();) {
				SelectorType element = (SelectorType) iter.next();
				selectors1.add(element);
			}
			mgmt.setSelectors(selectors1);
		}

		// Add any user defined options to the header
		addOptionSetHeader(mgmt);

		log.fine("REQUEST:\n" + mgmt + "\n");

		// Send the request
		final Addressing response = HttpClient.sendRequest(mgmt);

		// Check for fault during message generation
		if (response.getBody().hasFault()) {
			log.severe("FAULT:\n" + response + "\n");
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault);
		}
		log.info("RESPONSE:\n" + response + "\n");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#put(org.w3c.dom.Document)
	 */
	public ResourceState put(final Document content) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		return put(content, null, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#put(org.w3c.dom.Document,
	 *      java.lang.String, java.lang.String)
	 */
	public ResourceState put(final Document content, final Object expression,
			final String dialect) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {
		return put(content, expression, null, dialect);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#put(org.w3c.dom.Document,
	 *      java.lang.Object, java.lang.Map, java.lang.String)
	 */
	public ResourceState put(final Document content, final Object expression,
			final Map<String, String> namespaces, final String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		// Build the document
		final Transfer xf = setTransferProperties(Transfer.PUT_ACTION_URI);
		final Management mgmt = setManagementProperties(xf);

		// If expression is not null then generate fragment header
		if (expression != null) {
			// Add the Fragement Header
			setFragmentHeader(expression, namespaces, dialect, mgmt);
		}

		// Add the potential new content
		// Add the payload: plug the content passed in into the document
		if (content != null) {
			// Wrap the content value passed in wsman:XmlFragment node
			// Now take the created DOM and append it to the SOAP body

			if (expression != null) {
				if (content.getChildNodes() != null) {
				    final Object xmlFragment = BaseSupport.createXmlFragment(content.getChildNodes());
				    mgmt.getXmlBinding().marshal(xmlFragment, mgmt.getBody());
				}
			} else {
				// NON-FRAGMENT request processing.
				mgmt.getBody().addDocument(content);
			}
		}

		HashSet<SelectorType> selectors1 = new HashSet<SelectorType>();
		if (selectorSet != null) {
			List<SelectorType> selectors = selectorSet.getSelector();
			for (Iterator iter = selectors.iterator(); iter.hasNext();) {
				SelectorType element = (SelectorType) iter.next();
				selectors1.add(element);
			}
			mgmt.setSelectors(selectors1);
		}

		// Add any user defined options to the header
		addOptionSetHeader(mgmt);

		log.info("REQUEST:\n" + mgmt + "\n");
		// Send the request
		final Addressing response = HttpClient.sendRequest(mgmt);

		// Check for fault during message generation
		if (response.getBody().hasFault()) {
			log.severe("FAULT:\n" + response + "\n");
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault);
		}
		log.info("RESPONSE:\n" + response + "\n");

		// parse response and retrieve contents.
		// Iterate through the create response to obtain the selectors
		SOAPBody body = response.getBody();

		if (body.getChildNodes().getLength() == 0)
			return null;

		try {
			Document bodyDoc = body.extractContentAsDocument();
			return new ResourceStateImpl(bodyDoc);
		} catch (SOAPException e) {
			return null;
		}
	}

	/**
	 * Add any user speicied options to the request header.
	 * 
	 * @param mgmt
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	protected void addOptionSetHeader(final Management mgmt)
			throws JAXBException, SOAPException {
		/*
		 * HashSet<OptionType> options1=new HashSet<OptionType>();
		 * if(optionSet!=null){ Set keys = optionSet.keySet();
		 * 
		 * for (Iterator iter = keys.iterator(); iter.hasNext();) { String key =
		 * (String)iter.next(); Object value = optionSet.get(key); OptionType
		 * element = new OptionType(); element.setName(key);
		 * element.setValue(value.toString());
		 * 
		 * options1.add(element); }
		 */
		if (optionSet != null && optionSet.size() > 0) {
			mgmt.setOptions(optionSet);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.Resource#put(com.sun.ws.management.client.ResourceState)
	 */
	public ResourceState put(final ResourceState newState)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		return put(newState.getDocument());
	}

	protected Transfer setTransferProperties(final String action)
			throws JAXBException, SOAPException {
		// required: host, resourceUri
		if ((destination == null) | (resourceURI == null)) {
			String msg = "Host Address and ResourceURI cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		Transfer xf = new Transfer();
		if (binding != null)
			xf.setXmlBinding(binding);
		xf.setAction(action);
		xf.setReplyTo(replyTo); // Replying to creator
		xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());
		return xf;
	}

	protected Management setManagementProperties(final Addressing xf)
			throws SOAPException, JAXBException, DatatypeConfigurationException {
		final Management mgmt = new Management(xf);
		if ((xf.getXmlBinding() == null) && (binding != null))
			mgmt.setXmlBinding(binding);
		mgmt.setTo(destination);
		mgmt.setResourceURI(resourceURI);

		if (messageTimeout > 0) {
			final Duration timeout = DatatypeFactory.newInstance().newDuration(
					messageTimeout);
			mgmt.setTimeout(timeout);
		}

		if (maxEnvelopeSize > 0) {
			MaxEnvelopeSizeType size = TransferableResource.managementFactory
					.createMaxEnvelopeSizeType();
			BigInteger bi = new BigInteger("" + maxEnvelopeSize);
			size.setValue(bi);
			mgmt.setMaxEnvelopeSize(size);
		}

		// populate the map
		HashSet<SelectorType> selectors1 = new HashSet<SelectorType>();
		if (selectorSet != null) {
			List<SelectorType> selectors = selectorSet.getSelector();
			for (Iterator iter = selectors.iterator(); iter.hasNext();) {
				SelectorType element = (SelectorType) iter.next();
				selectors1.add(element);
			}
			mgmt.setSelectors(selectors1);
		}

		return mgmt;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#get()
	 */
	public ResourceState get() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {
		return get(null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#get(java.lang.String,
	 *      java.lang.String)
	 */
	public ResourceState get(final Object expression, final String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		return get(expression, null, dialect);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#get(java.lang.String,
	 *      java.lang.String)
	 */
	public ResourceState get(final Object expression,
			final Map<String, String> namespaces, final String dialect)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		// Build the document
		final Transfer xf = setTransferProperties(Transfer.GET_ACTION_URI);
		final Management mgmt = setManagementProperties(xf);

		// If expression is not null then generate fragment header
		if (expression != null) {
			// Add the Fragement Header
			setFragmentHeader(expression, namespaces, dialect, mgmt);
		}

		// Add any user defined options to the header
		addOptionSetHeader(mgmt);

		log.info("REQUEST:\n" + mgmt + "\n");
		// Send the request
		final Addressing response = HttpClient.sendRequest(mgmt);

		// Check for fault during message generation
		if (response.getBody().hasFault()) {
			log.severe("FAULT:\n" + response + "\n");
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault);
		}
		log.info("RESPONSE:\n" + response + "\n");

		// parse response and retrieve contents.
		// Iterate through the create response to obtain the selectors
		SOAPBody body = response.getBody();

		Document bodyDoc = body.extractContentAsDocument();
		return new ResourceStateImpl(bodyDoc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#getResourceUri()
	 */
	public String getResourceUri() {
		return this.resourceURI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#getDestination()
	 */
	public String getDestination() {
		return this.destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.ws.management.client.impl.TransferableResource#getMessageTimeout()
	 */
	public long getMessageTimeout() {
		return this.messageTimeout;
	}

	public SelectorSetType getSelectorSet() {
		return this.selectorSet;
	}

	/**
	 * @param destination
	 *            The destination to set.
	 */
	public void setDestination(final String destination) {
		this.destination = destination;
	}

	/**
	 * @param messageTimeout
	 *            The messageTimeout to set.
	 */
	public void setMessageTimeout(final long messageTimeout) {
		this.messageTimeout = messageTimeout;
	}

	@Override
	public String toString() {
		if (selectorSet != null) {
			String value = "";
			List<SelectorType> selector = selectorSet.getSelector();
			for (int index = 0; index < selector.size(); index++) {
				SelectorType sel = selector.get(index);
				value = value + sel.getContent() + " ";
			}
			return value;
		} else
			return super.toString();
	}

	public void setFragmentHeader(final Object expression,
			final String dialect, final Management mgmt) throws SOAPException,
			JAXBException {
		final TransferExtensions transfer = new TransferExtensions(mgmt);
		transfer.setFragmentHeader(expression, null, dialect);
	}

	public void setFragmentHeader(final Object expression,
			final Map<String, String> namespaces, final String dialect,
			final Management mgmt) throws SOAPException, JAXBException {
		final TransferExtensions transfer = new TransferExtensions(mgmt);
		transfer.setFragmentHeader(expression, namespaces, dialect);
	}

	public static String xmlToString(final Node node) {
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setMaxEnvelopeSize(final long i) {

		maxEnvelopeSize = i;
	}

	public long getMaxEnvelopeSize() {
		return maxEnvelopeSize;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(final String replyTo) {
		this.replyTo = replyTo;
	}

	/**
	 * @param resourceURI
	 *            The resourceURI to set.
	 */
	public void setResourceUri(final String resourceURI) {
		this.resourceURI = resourceURI;
	}

	/**
	 * Add an option to the option set
	 * 
	 * @param name
	 *            option name
	 * @param value
	 *            option value
	 * @param type
	 *            option type (default to xsd:String)
	 */
	public void addOption(final String name, final Object value,
			final QName type) {
		addOption(name, value, type, false);
	}

	/**
	 * Add an option to the option set
	 * 
	 * @param name
	 *            option name
	 * @param value
	 *            option value
	 * @param mustComply
	 *            option must comply flag
	 */

	public void addOption(final String name, final Object value,
			final boolean mustComply) {
		addOption(name, value, null, false);
	}

	/**
	 * Add an option to the option set
	 * 
	 * @param name
	 *            option name
	 * @param value
	 *            option value
	 */

	public void addOption(final String name, final Object value) {
		addOption(name, value, false);
	}

	/**
	 * Add an option to the option set
	 * 
	 * @param name
	 *            option name
	 * @param value
	 *            option value
	 * @param type
	 *            option type (default to xsd:String)
	 * @param mustComply
	 *            option must comply flag
	 */
	public void addOption(final String name, final Object value,
			final QName type, final boolean mustComply) {
		if (optionSet == null) {
			optionSet = new HashSet<OptionType>();
		}

		OptionType element = new OptionType();
		element.setName(name);
		element.setValue(value.toString());
		if (mustComply) {
			element.setMustComply(mustComply);
		}

		if (type != null) {
			element.setType(type);
		}

		optionSet.add(element);
	}

	/**
	 * @return Returns the optionSet.
	 */
	public HashSet<OptionType> getOptionSet() {
		return optionSet;
	}

	/**
	 * Remove all of the current options from the option set
	 * 
	 */
	public void resetOptionSet() {
		optionSet = null;
	}

	/**
	 * Gets the XmlBinding used by this Resource.
	 * 
	 * @return the XmlBinding used by this Resource
	 */
	public XmlBinding getBinding() {
		return binding;
	}

	/**
	 * Sets the XmlBinding used by this Resource.
	 * 
	 * @param binding
	 *            the XmlBinding used by this Resource
	 */
	public void setBinding(final XmlBinding binding) {
		this.binding = binding;
	}

	/**
	 * Sets the XmlBinding given a list of package names.
	 * 
	 * @param packageNames
	 * @throws JAXBException
	 */
	public void setBinding(final String... packageNames) throws JAXBException {
		this.binding = new XmlBinding(null, packageNames);
	}
}
