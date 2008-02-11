/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ** Copyright (C) 2006, 2007, 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 *
 */

package com.sun.ws.management.client;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPException;
import javax.xml.validation.Schema;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.message.enueration.WSManEnumerationRequest;
import com.sun.ws.management.client.message.transfer.WSManCreateRequest;
import com.sun.ws.management.client.message.transfer.WSManCreateResponse;
import com.sun.ws.management.client.message.transfer.WSManDeleteRequest;
import com.sun.ws.management.client.message.transfer.WSManDeleteResponse;
import com.sun.ws.management.client.message.transfer.WSManGetRequest;
import com.sun.ws.management.client.message.transfer.WSManGetResponse;
import com.sun.ws.management.client.message.transfer.WSManPutRequest;
import com.sun.ws.management.client.message.transfer.WSManPutResponse;
import com.sun.ws.management.xml.XmlBinding;

/**
 * General WS Management Client interface to initiate
 * standard WS Management operations as well as
 * custom operations.
 */
public class WSManClient {
	
	final XmlBinding binding;
	
    /**
     * Constructor
     * 
     * @throws JAXBException
     */
	public WSManClient() throws JAXBException {
		this.binding = null;
	}
	

	/**
	 * Constructor
	 * 
	 * @param customPackages list of custom package names that will be
	 *        specified when the JAXBContext is constructed. This will
	 *        be used when marshaling and unmarshaling objects.
	 * @throws JAXBException 
	 *        
	 * @throws JAXBException
	 */
	public WSManClient(String keystore, String... customPackages)
		throws JAXBException {
		this(null, keystore, customPackages);
	}
	
	/**
	 * Constructor
	 * 
	 * @param schema schemas to use if XML validation is enabled.
	 * @param keystore location of the certificate keystore.
	 * @param customPackages list of custom package names that will be
	 *        specified when the JAXBContext is constructed. This will
	 *        be used when marshaling and unmarshaling objects.
	 * @throws JAXBException 
	 *        
	 * @throws JAXBException
	 */
	public WSManClient(Schema schema, String keystore, String... customPackages)
		throws JAXBException {
		// Initialize XmlBinding, etc...
		this.binding = new XmlBinding(schema, customPackages);
	}

	/**
	 * Create a new resource
	 * 
	 * @param content the resource to be created. This must be a JAXBElement
	 *        located in a package specified in the constructor, or a w3c Node
	 *        object.
	 * @param epr EndpointReference of the resource creation factory.
	 * 
	 * @return EndpointReference pointing to the resource created.
	 * @throws FaultException 
	 * @throws IOException 
	 * @throws TimedOutFault 
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws TimedOutFault
	 * @throws FaultException
	 * @throws JAXBException 
	 */
	public EndpointReferenceType create(final EndpointReferenceType factoryEPR,
			                        final Object resource,
			                        final Duration timeout) throws TimedOutFault, FaultException, Exception {
		final WSManCreateRequest request = new WSManCreateRequest(factoryEPR, null, binding);
		request.setCreate(resource);
		request.setOperationTimeout(timeout);
		return create(request);
		
	}
	
	/**
	 * Create a new resource
	 * 
	 * @param content resource to be created. This must be a JAXBElement
	 *        located in a package specified in the constructor, or a w3c Node
	 *        object. If a filter is specified this is expected to be an
	 *        XmlFragment.
	 * @param epr EndpointReference of the resource creation factory.
	 * @param filter filter used if only a fragment of the resource is to be created
	 * @param filterNamespaces namespace declarations for any namespaces used
	 *        in the filter.
	 * @param filterDialect filter dialect used. Default is XPath 1.0
	 * @param maxTime the maximum amount of time to wait for the completion
	 *        of this request. If this time is exceeded before the resource is created
	 *        a TimedOutFault will be thrown.
	 * @param options a set of WS Management application options.
	 * 
	 * @return EndpointReference pointing to the resource created.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws TimedOutFault
	 * @throws FaultException
	 */
	public EndpointReferenceType create(final WSManCreateRequest request) 
	throws TimedOutFault, FaultException, Exception {
		return ((WSManCreateResponse)request.invoke()).getCreateResponse();
	}
		
	/**
	 * Read resource
	 * 
	 * @param epr EndpointReference pointing to the resource
	 * 
	 * @return requested resource
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws TimedOutFault
	 * @throws FaultException
	 * @throws JAXBException 
	 */
	public Object get(final EndpointReferenceType resourceEPR,
            final Duration timeout)	
	throws TimedOutFault, FaultException, Exception {
		final WSManGetRequest request = new WSManGetRequest(resourceEPR, null, binding);
		request.setOperationTimeout(timeout);
		return get(request);
	}
	
	/**
	 * Read resource
	 * 
	 * @param epr EndpointReference pointing to the resource
	 * @param filter filter used if only a fragment of the resource is desired
	 * @param filterNamespaces namespace declarations for any namespaces used
	 *        in the filter.
	 * @param filterDialect filter dialect used. Default is XPath 1.0
	 * @param maxTime the maximum amount of time to wait for the completion
	 *        of this request. If this time is exceeded before the resource is read
	 *        a TimedOutFault will be thrown.
	 * @param options a set of WS Management application options.
	 * 
	 * @return requested resource
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws TimedOutFault
	 * @throws FaultException
	 */
	public Object get(final WSManGetRequest request) 
	throws TimedOutFault, FaultException, Exception {
		return ((WSManGetResponse)request.invoke()).getPayload();
	}
	
	/**
	 * Update resource
	 * 
	 * @param content resource to be updated. This must be a JAXBElement
	 *        located in a package specified in the constructor, or a w3c Node
	 *        object. If a filter is specified this is expected to be an
	 *        XmlFragment.
	 * @param epr EndpointReference pointing to the resource
	 * 
	 * @return updated resource. Server may optionally return null
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws TimedOutFault
	 * @throws FaultException
	 * @throws JAXBException 
	 */
	public Object put(final EndpointReferenceType resourceEPR,
            final Duration timeout,
            final Object resource)	
	throws TimedOutFault, FaultException, Exception {
		final WSManPutRequest request = new WSManPutRequest(resourceEPR, null, binding);
		request.setPayload(resource);
		request.setOperationTimeout(timeout);
		return put(request);
	}
	
	/**
	 * Update resource
	 * 
	 * @param content resource to be updated. This must be a JAXBElement
	 *        located in a package specified in the constructor, or a w3c Node
	 *        object. If a filter is specified this is expected to be an
	 *        XmlFragment.
	 * @param epr EndpointReference pointing to the resource
	 * @param filter filter used if only a fragment of the resource is to be updated
	 * @param filterNamespaces namespace declarations for any namespaces used
	 *        in the filter.
	 * @param filterDialect filter dialect used. Default is XPath 1.0
	 * @param maxTime the maximum amount of time to wait for the completion
	 *        of this request. If this time is exceeded before the resource is updated
	 *        a TimedOutFault will be thrown.
	 * @param options a set of WS Management application options
	 * 
	 * @return updated resource. Server may optionally return null
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 * @throws IOException
	 * @throws TimedOutFault
	 * @throws FaultException
	 */
	public Object put(final WSManPutRequest request) 
	throws TimedOutFault, FaultException, Exception {
		return ((WSManPutResponse)request.invoke()).getPayload();
	}
	
	/**
	 * Delete resource
	 * 
	 * @param epr EndpointReference pointing to the resource
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 * @throws DatatypeConfigurationException
	 * @throws TimedOutFault
	 * @throws FaultException
	 * @throws JAXBException 
	 */
	public void delete(final EndpointReferenceType resourceEPR,
            final Duration timeout)	
	throws TimedOutFault, FaultException, Exception {
		final WSManDeleteRequest request = new WSManDeleteRequest(resourceEPR, null, binding);
		request.setOperationTimeout(timeout);
		delete(request);
	}
	
	/**
	 * Delete resource
	 *  
	 * @param epr EndpointReference pointing to the resource
	 * @param filter filter used if only a fragment of the resource is to be deleted
	 * @param filterNamespaces namespace declarations for any namespaces used
	 *        in the filter.
	 * @param filterDialect filter dialect used. Default is XPath 1.0
	 * @param maxTime the maximum amount of time to wait for the completion
	 *        of this request. If this time is exceeded before the resource is deleted
	 *        a TimedOutFault will be thrown.
	 * @param options a set of WS Management application options
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 * @throws DatatypeConfigurationException
	 * @throws TimedOutFault
	 * @throws FaultException
	 */
	public WSManDeleteResponse delete(final WSManDeleteRequest request) 
	throws TimedOutFault, FaultException, Exception {
		return ((WSManDeleteResponse)request.invoke());
	}
	
	/**
	 * Create an iterator to iterate over a resource
	 * 
	 * @param epr EndpointReference pointing to a set of resources
	 * @param filter filter used if only a subset of the set of resources is desired
	 * @param filterNamespaces namespace declarations for any namespaces used
	 *        in the filter.
	 * @param filterDialect filter dialect used. Default is XPath 1.0
	 * @param maxElements the maximum number of elements to buffer at one time
	 * 
	 * @return the iterator
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws TimedOutFault
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public ResourceIterator newIterator(final EndpointReferenceType resourceEPR,
			                            final String filter, 
			                            final Map<String, String> filterNamespaces, 
			                            final String filterDialect,
			                            final int maxElements,
			                            final Duration timeout)
			throws SOAPException, JAXBException, IOException, TimedOutFault,
			       FaultException, DatatypeConfigurationException {
		// TODO:
		return null;
	}
	
	/**
	 * Create an iterator to iterate over a resource
	 * 
	 * @param epr EndpointReference pointing to a set of resources
	 * @param maxElements the maximum number of elements to buffer at one time
	 * @param maxTime the maximum amount of time to wait for a block of resources
	 * @param mode specifies if the resource and/or its EPR are requested.
	 *        Default is just the resource.
	 * 
	 * @return the iterator
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws TimedOutFault
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public ResourceIterator newIterator(final WSManEnumerationRequest request,
					                    final EndpointReferenceType resourceEPR,
                                        final int maxElements,
                                        final Duration timeout)
			throws SOAPException, JAXBException, IOException, TimedOutFault,
			       FaultException, DatatypeConfigurationException {
        // TODO:
		return null;
	}
}
