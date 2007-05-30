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
 * $Id: ResourceFactory.java,v 1.16 2007-05-30 20:30:21 nbeers Exp $
 */
package com.sun.ws.management.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableURI;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.impl.JAXBResourceImpl;
import com.sun.ws.management.client.impl.ResourceImpl;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

/**
 * Factory to create and configure {@link Resource}
 * objects for use by a client. Provides additional support
 * methods to obtain the {@link ServerIdentity} metadata.
 *
 * @see Resource
 * @see EnumerableResource
 * @see TransferableResource
 * @see ServerIdentity
 *
 * @author spinder
 *
 */
public class ResourceFactory {

	protected static final String UUID_SCHEME = "uuid:";

	public static final String LATEST = "LATEST";

    private static Logger log = Logger.getLogger(ResourceFactory.class.getName());
    private static XmlBinding xmlBinding = null;

    /*
    {
		try {
			Message.initialize();
		} catch (SOAPException e) {
			log.severe("Failed to initalize wiseman with error: "+e.getMessage());
		}
    }
    */
	/**
	 * You should never create a factory. Access it statically.
	 */
	private static org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory wsManFactory=
		new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
	private ResourceFactory() {
		super();
	}

	/**
	 * Creates a new resource instance on the server.
	 *
	 * @param destination A URL for the destination port of this service.
	 * @param resourceURI A resource URI indicating the type of resource to create
	 * @param timeoutInMilliseconds Time to wait before giving up on creation
	 * @param content a w3c document representing the inital resource state
	 * @param specVersion The wsman spec version of the client to create. You can
	 * use null or the constant #LATEST.
	 * @return A Resource class representing the new resource created on the server.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static Resource create(String destination, String resourceURI,
			long timeoutInMilliseconds, Document content, String specVersion)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException	{
				return create(destination, resourceURI,
						timeoutInMilliseconds, content, specVersion, null);
	}

	/**
	 * Creates a new resource instance on the server.
	 *
	 * @param destination A URL for the destination port of this service.
	 * @param resourceURI A resource URI indicating the type of resource to create
	 * @param timeoutInMilliseconds Time to wait before giving up on creation
	 * @param content a w3c document representing the inital resource state
	 * @param specVersion The wsman spec version of the client to create. You can
	 * use null or the constant #LATEST.
	 * @param optionSet set of user defined options to use during the create operation
	 * @return A Resource class representing the new resource created on the server.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static Resource create(String destination, String resourceURI,
			long timeoutInMilliseconds, Document content, String specVersion,
			HashSet<OptionType> optionSet)
			throws SOAPException, JAXBException, IOException, FaultException,
			DatatypeConfigurationException {

		// required: host, resourceUri
		if ((destination == null) | (resourceURI == null)) {
			String msg = "Host Address and ResourceURI cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		if (xmlBinding == null) {
			xmlBinding = new XmlBinding(null);
		}

		// Build the document
		final Transfer xf = new Transfer();
		xf.setXmlBinding(xmlBinding);
		xf.setAction(Transfer.CREATE_ACTION_URI);
		// Replying to creator
		xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
		xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

		final Management mgmt = new Management(xf);
		mgmt.setXmlBinding(xf.getXmlBinding());
		mgmt.setTo(destination);
		mgmt.setResourceURI(resourceURI);
		final Duration timeout = DatatypeFactory.newInstance().newDuration(
				timeoutInMilliseconds);
		mgmt.setTimeout(timeout);

		// populate attribute details
		// setMessageTimeout(timeoutInMilliseconds);

		if (optionSet != null)
		{
			mgmt.setOptions(optionSet);
		}

		// Add the payload: plug the content passed in into the document
		if (content != null) {
			// Now take the created DOM and append it to the SOAP body
			mgmt.getBody().addDocument(content);
		}

		log.info("REQUEST:\n"+mgmt+"\n");
		// Send the request
		final Addressing response = HttpClient.sendRequest(mgmt);
		response.setXmlBinding(mgmt.getXmlBinding());

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
		JAXBElement element = (JAXBElement) response.getXmlBinding().unmarshal(
				body.getFirstChild());
		EndpointReferenceType createResponse = (EndpointReferenceType) element
				.getValue();
		ReferenceParametersType refParams = createResponse
				.getReferenceParameters();
		List<Object> parmList = refParams.getAny();
		AttributableURI uriType = null;
		for (Object parameterObj : parmList) {
			if (parameterObj instanceof JAXBElement) {
				final JAXBElement parameterJaxb = (JAXBElement) parameterObj;
				if ((parameterJaxb.getValue() instanceof AttributableURI)
						&& (parameterJaxb.getName().toString().equals("ResourceURI"))) {
					uriType = (AttributableURI) parameterJaxb.getValue();
				}
			}
		}

		String returnedResourceUri = "";
		if (uriType != null) {
			returnedResourceUri = uriType.getValue();
		} else {
			returnedResourceUri = resourceURI;
		}
		ResourceImpl resource = new ResourceImpl(destination,
				returnedResourceUri, populateSelectorSet(createResponse), xmlBinding);
		resource.setMessageTimeout(timeoutInMilliseconds);
		return resource;
	}


        /**
         * Create a fragment resource on the server.
         *
         * @param destination A URL for the destination port of this service.
	     * @param resourceURI A resource URI indicating the type of resource to create
         * @param existingResourceId selector set identifying the resource
	     * @param timeoutInMilliseconds Time to wait before giving up on creation
	     * @param content a w3c document representing the fragment to create
	     * @param specVersion The wsman spec version of the client to create. You can
	     *        use null or the constant #LATEST.
         * @param fragmentExp the fragment expression
         * @param dialect the fragment expression dialect
         * @return TransferableResource referencing the resource created
         * @throws SOAPException
         * @throws JAXBException
         * @throws IOException
         * @throws FaultException
         * @throws DatatypeConfigurationException
         */
		public static Resource createFragment(String destination,
				String resourceURI, SelectorSetType existingResourceId,
				long timeoutInMilliseconds,Document content,
				String specVersion, String fragmentExp, String dialect) throws SOAPException,
				JAXBException, IOException, FaultException,
				DatatypeConfigurationException {

			//required: host, resourceUri
			if((destination == null)||(resourceURI ==null)){
				String msg="Host Address and ResourceURI cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			//required: fragmentExp,existingResourceId
			if((fragmentExp == null)||(existingResourceId==null)){
				String msg="FragmentExpression or existingResourceId cannot be null.";
				throw new IllegalArgumentException(msg);
			}

			if (xmlBinding == null) {
				xmlBinding = new XmlBinding(null);
			}

			//Build the document
			final Transfer xf = new Transfer();
			xf.setXmlBinding(xmlBinding);
			xf.setAction(Transfer.CREATE_ACTION_URI);
			xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI); //Replying to creator
			xf.setMessageId(UUID_SCHEME + UUID.randomUUID().toString());

			final Management mgmt = new Management(xf);
			mgmt.setXmlBinding(xf.getXmlBinding());
			mgmt.setTo(destination);
			mgmt.setResourceURI(resourceURI);
			final Duration timeout =
				DatatypeFactory.newInstance().newDuration(timeoutInMilliseconds);
			mgmt.setTimeout(timeout);

	        //DONE: if xpathExpression is not null then generate fragment GET
	        if((fragmentExp!=null)&&(fragmentExp.trim().length()>0)){
	        	//DONE: add the Fragement Header
	        	setFragmentHeader(fragmentExp,dialect,mgmt);
	        }

	        //DONE: add the selectorSetType to the request as well.
	        if(existingResourceId!=null){
	        	//Convert type for correct insertion
	        	List<SelectorType> list = existingResourceId.getSelector();
	        	Set<SelectorType> collection = new HashSet<SelectorType>();
	        	for (Iterator iter = list.iterator(); iter.hasNext();) {
					SelectorType element = (SelectorType) iter.next();
					collection.add(element);
				}
				mgmt.setSelectors(collection);
	        }

		// Add the payload: plug the content passed in into the document
		if (content != null) {
			// Now take the created DOM and wrap it into an Xml Fragment
			final MixedDataType mixedDataType = Management.FACTORY
					.createMixedDataType();
			mixedDataType.getContent().add(content.getDocumentElement());
			// create the XmlFragmentElement
			JAXBElement<MixedDataType> fragment = Management.FACTORY
					.createXmlFragment(mixedDataType);
			// Add the Fragment to the body
			xf.getXmlBinding().marshal(fragment, mgmt.getBody());
		}

			log.info("REQUEST:\n"+mgmt+"\n");
			//Send the request
			final Addressing response = HttpClient.sendRequest(mgmt);
			response.setXmlBinding(mgmt.getXmlBinding());

			//Check for fault during message generation
			if (response.getBody().hasFault()) {
				log.severe("FAULT:\n" + response + "\n");
				SOAPFault fault = response.getBody().getFault();
				throw new FaultException(fault);
			}
			log.info("RESPONSE:\n" + response + "\n");

			//parse response and retrieve contents.
			// Iterate through the create response to obtain the selectors
			SOAPBody body = response.getBody();
			JAXBElement createResponse = (JAXBElement)response.getXmlBinding().unmarshal(body.getFirstChild());
			EndpointReferenceType resCreated = (EndpointReferenceType)createResponse.getValue();

			ReferenceParametersType refParams = resCreated.getReferenceParameters();
			List<Object> parmList = refParams.getAny();
			AttributableURI uriType =null;
			for (Object parameterObj : parmList) {
				if (parameterObj instanceof JAXBElement) {
					final JAXBElement parameterJaxb = (JAXBElement) parameterObj;
					if ((parameterJaxb.getValue() instanceof AttributableURI)
							&& (parameterJaxb.getName().toString().equals("ResourceURI"))) {
						uriType = (AttributableURI) parameterJaxb.getValue();
					}
				}
			}

			String returnedResourceUri = "";
			if (uriType != null) {
				returnedResourceUri = uriType.getValue();
			} else {
				returnedResourceUri = resourceURI;
			}
			ResourceImpl resource = new ResourceImpl(destination,
					returnedResourceUri,
					populateSelectorSet(resCreated), xmlBinding);
			resource.setMessageTimeout(timeoutInMilliseconds);
			return resource ;
	}

	/**
	 * Extracts and builds a selector set from the response from create.
	 * @param createResponse
	 * @return the SelectorSetType created
	 */
	private static SelectorSetType populateSelectorSet(
			EndpointReferenceType createResponse) {
		EndpointReferenceType rcEpr = createResponse;
		// Selectors sought for in 3 obvious places: right after
		// ResourceCreated, RefPar | ResProp
		boolean foundSelectors = false;
		if (createResponse.getAny() != null) {
			// convert to String, parse for <SelectorSet>
			if (createResponse.getAny().toString().indexOf("<SelectorSet>") > -1) {
				// Extract, populate, set flag
				foundSelectors = true;
			}
		}

		List<Object> value = null;
		if (!foundSelectors) {
			ReferenceParametersType prTypes = rcEpr.getReferenceParameters();
			value = null;
			if ((prTypes.getAny() != null)
					&& ((value = prTypes.getAny()) != null)) {
				if (value.size() > 0) {
					for (Iterator iter = value.iterator(); iter.hasNext();) {
						JAXBElement element = (JAXBElement) iter.next();
						if (element.getDeclaredType().equals(
								SelectorSetType.class)) {
							// Extract, populate, set flag
							return (SelectorSetType)(element.getValue());
						}
					}
				}
			}
		}
		if (!foundSelectors) {
			ReferencePropertiesType paTypes = rcEpr.getReferenceProperties();
			if ((paTypes.getAny() != null)
					&& ((value = paTypes.getAny()) != null)) {
				if (value.size() > 0) {
					for (Iterator iter = value.iterator(); iter.hasNext();) {
						JAXBElement element = (JAXBElement) iter.next();
						if (element.getDeclaredType().equals(
								SelectorSetType.class)) {
							// Extract, populate, set flag
							// this.selectorSet =
							// ((JAXBElement<SelectorSetType>)element).getValue();
							return (SelectorSetType)(element.getValue());

						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Delete the specified resource on the server.
	 * @see TransferableResource#delete()
	 *
	 * @param res resource to delete.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static void delete(Resource res) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		res.delete();
	}

	/**
	 * Static method for locating existing exposed resource(s).
	 * This method returns a single Resource object in the first
	 * element of an array. It may be used as an
	 * {@link EnumerableResource EnumerableResource} or
	 * {@link TransferableResource TransferableResource},
	 * depending upon the desired operation.
	 *
	 * @param destination URL of the target service
	 * @param resourceURI URI identifying the resource
	 * @param timeout the <code>OperationTimeout</code>. This is the
	 *        maximum amount of time the client is willing to wait
	 *        for the operation to complete.
	 * @param selectors set of selectors used to identify a single
	 *        or subset of resources at the target service.
	 * @return an array containing a single Resource object.
	 *         It may be used as an
	 *         {@link EnumerableResource EnumerableResource} or
	 *         {@link TransferableResource TransferableResource},
	 *         depending upon the desired operation.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static Resource[] find(String destination, String resourceURI,
			long timeout, SelectorSetType selectors) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		if (xmlBinding == null) {
			xmlBinding = new XmlBinding(null);
		}
		Resource[] resourceList = null;
			resourceList = new Resource[1];
			// lazy instantiation
			Resource enumerationResource = new ResourceImpl(destination, resourceURI, selectors, xmlBinding);
			enumerationResource.setMessageTimeout(timeout);
			resourceList[0] = enumerationResource;

			return resourceList;
	}

	/**
	 * Static method for locating existing exposed resource(s).
	 * This method returns a single Resource object in the first
	 * element of an array. It may be used as an
	 * {@link EnumerableResource EnumerableResource} or
	 * {@link TransferableResource TransferableResource},
	 * depending upon the desired operation.
	 *
	 * @param destination URL of the target service
	 * @param resourceURI URI identifying the resource
	 * @param timeout the <code>OperationTimeout</code>. This is the
	 *        maximum amount of time the client is willing to wait
	 *        for the operation to complete.
	 * @param selectors set of selectors used to identify a single
	 *        or subset of resources at the target service.
	 * @return an array containing a single Resource object.
	 *         It may be used as an
	 *         {@link EnumerableResource EnumerableResource} or
	 *         {@link TransferableResource TransferableResource},
	 *         depending upon the desired operation.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static Resource[] find(String destination, String resourceURI,
			long timeout, Map<String,String> selectors) throws SOAPException,JAXBException, IOException, FaultException,
			DatatypeConfigurationException {
		SelectorSetType selectorsSetType=null;
		if(selectors!=null){
			selectorsSetType=wsManFactory.createSelectorSetType();
			List<SelectorType> selectorList = selectorsSetType.getSelector();
			for (String name : selectors.keySet()) {
				SelectorType selectorType = wsManFactory.createSelectorType();
				selectorType.setName(name);
				List<Serializable> content = selectorType.getContent();
				content.add(selectors.get(name));
				selectorList.add(selectorType);
			}
		}


		return find( destination,  resourceURI, timeout,  selectorsSetType);
	}

    private static void setFragmentHeader(final String expression, final String dialect,
    		Management mgmt) throws SOAPException, JAXBException {
		if (xmlBinding == null) {
			xmlBinding = new XmlBinding(null);
		}

		        // remove existing, if any
//		        removeChildren(mgmt.getHeader(), FRAGMENT_TRANSFER);

		        final DialectableMixedDataType dialectableMixedDataType =
		                Management.FACTORY.createDialectableMixedDataType();
		        if (dialect != null) {
//		            if (!XPath.isSupportedDialect(dialect)) {
//		                throw new FragmentDialectNotSupportedFault(XPath.SUPPORTED_FILTER_DIALECTS);
//		            }
		            dialectableMixedDataType.setDialect(dialect);
		        }
		        dialectableMixedDataType.getOtherAttributes().put(SOAP.MUST_UNDERSTAND,
		                Boolean.TRUE.toString());

		        //add the query string to the content of the FragmentTransfer Header
		        dialectableMixedDataType.getContent().add(expression);

		        final JAXBElement<DialectableMixedDataType> fragmentTransfer =
		                Management.FACTORY.createFragmentTransfer(dialectableMixedDataType);

		        //set the SOAP Header for Fragment Transfer
		        xmlBinding.marshal(fragmentTransfer, mgmt.getHeader());
		    }

    /**
     * Gets the server identity.
     * @see ServerIdentity
     *
     * @param destination URL of the target service
     * @return {@link ServerIdentity ServerIdentity}
     * @throws SOAPException
     * @throws IOException
     * @throws JAXBException
     */
	public static ServerIdentity getIdentity(String destination) throws SOAPException, IOException, JAXBException{
        try {
			return getIdentity(destination, -1);
		} catch (InterruptedException e) {
			throw new RuntimeException("getIdentity attempt interrupted.");
		} catch (TimeoutException e) {
			throw new RuntimeException("getIdentity attempt exceeded timout.");
		}
	}

	/**
     * Gets the server identity.
     * @see ServerIdentity
     *
     * @param destination URL of the target service
	 * @param timeout the <code>OperationTimeout</code>. This is the
	 *        maximum amount of time the client is willing to wait
	 *        for the operation to complete.
	 * @return {@link ServerIdentity ServerIdentity}
	 * @throws SOAPException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public static ServerIdentity getIdentity(final String destination,int timeout) throws SOAPException, IOException, JAXBException, InterruptedException, TimeoutException{
		Map.Entry<String, String>[] map = null;
		ServerIdentity identity = getIdentity(destination, timeout, map);
		return identity;
	}

	/**
     * Gets the server identity.
     * @see ServerIdentity
     *
     * @param destination URL of the target service
	 * @param timeout the <code>OperationTimeout</code>. This is the
	 *        maximum amount of time the client is willing to wait
	 *        for the operation to complete.
	 * @param headers additional SOAP headers to set on the request
	 * @return {@link ServerIdentity ServerIdentity}
	 * @throws SOAPException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public static ServerIdentity getIdentity(final String destination,int timeout, final
    		Entry<String, String>... headers) throws SOAPException, IOException, JAXBException, InterruptedException, TimeoutException{

		IdentifyTask identifyTask = new IdentifyTask(destination,headers);

         	Thread identifyThread=new Thread(identifyTask);
        	identifyThread.start();
        	if(timeout<0)
        		identifyThread.join();
        	else
        		identifyThread.join(timeout);

        	if(identifyThread.isAlive()){
        		String timeoutMessage = "An identify attempt to "+destination+" exceeded timeout interval and has been abandoned";
				log.info(timeoutMessage);
        		throw new TimeoutException(timeoutMessage);
        	}

        return identifyTask.servIdent;
	}

	/**
	 * Sets the XmlBinding that will be used for marshalling and unmarshalling.
	 *
	 * @param binding
	 */
	public static void setBinding(XmlBinding binding) {
		xmlBinding = binding;
	}

	/**
	 * Gets the XmlBinding that is used for marshaling and unmarshaling.
	 * @return the XmlBinding.
	 */
	public static XmlBinding getBinding() {
		return xmlBinding;
	}

	/**
	 * Creates a new resource instance on the server.
	 *
	 * @param destination A URL for the destination port of this service.
	 * @param resourceURI A resource URI indicating the type of resource to create
	 * @param timeoutInMilliseconds Time to wait before giving up on creation
	 * @param content object to be marshaled into the SOAP document that represents
	 *        the resource to be created.
	 * @param specVersion The wsman spec version of the client to create. You can
	 * use null or the constant #LATEST.
	 * @param binding {@link XmlBinding XmlBinding}
	 *        to use to marshal and unmarshal the SOAP documents
	 *        sent over the wire.
	 * @return A Resource class representing the new resource created on the server.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static JAXBResource createJAXB(String destination, String resourceURI,
			long timeoutInMilliseconds, Object content, String specVersion,XmlBinding binding)
	throws SOAPException, JAXBException, IOException, FaultException,
	DatatypeConfigurationException {

		// Actually create this object on the server
		Document domContent = Management.newDocument();
		if(content != null) {
			binding.marshal(content, domContent);
		}

		Resource resource=create( destination, resourceURI,
				timeoutInMilliseconds,  domContent, specVersion);

		// now construct a JAXBResourceImpl to talk with it
		JAXBResource jaxbResource=new JAXBResourceImpl(resource.getDestination(),resource.getResourceUri(),resource.getSelectorSet(),binding);
		return jaxbResource ;

	}

	/**
	 * Creates a new resource instance on the server.
	 *
	 * @param destination A URL for the destination port of this service.
	 * @param resourceURI A resource URI indicating the type of resource to create
	 * @param timeoutInMilliseconds Time to wait before giving up on creation
	 * @param content object to be marshaled into the SOAP document that represents
	 *        the resource to be created.
	 * @param specVersion The wsman spec version of the client to create. You can
	 * use null or the constant #LATEST.
	 * @param packageNames names of packages to be used to construct an
	 *        {@link XmlBinding XmlBinding} object that will be used to marshal
	 *        the resource into the SOAP document. NOTE: For performance reasons
	 *        it is highly recommended to use
	 *        {@link #createJAXB(String, String, long, Object, String, XmlBinding)}
	 *        instead of this method and reuse the {@link XmlBinding} object.
	 * @return A Resource class representing the new resource created on the server.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public static JAXBResource createJAXB(String destination, String resourceURI,
			long timeoutInMilliseconds, Object content,String specVersion, final String... packageNames)
	throws SOAPException, JAXBException, IOException, FaultException,
	DatatypeConfigurationException {

		return createJAXB( destination,  resourceURI,
				 timeoutInMilliseconds,  content,  specVersion, new XmlBinding(null,packageNames));
	}

}
