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
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPException;
import javax.xml.validation.Schema;

import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;

import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.message.api.client.enumeration.WSManEnumerateRequest;
import com.sun.ws.management.message.api.client.transfer.WSManCreateRequest;
import com.sun.ws.management.message.api.client.transfer.WSManCreateResponse;
import com.sun.ws.management.message.api.client.transfer.WSManDeleteRequest;
import com.sun.ws.management.message.api.client.transfer.WSManDeleteResponse;
import com.sun.ws.management.message.api.client.transfer.WSManGetRequest;
import com.sun.ws.management.message.api.client.transfer.WSManGetResponse;
import com.sun.ws.management.message.api.client.transfer.WSManPutRequest;
import com.sun.ws.management.message.api.client.transfer.WSManPutResponse;
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
	 * @param resource the resource to be created. This must be a JAXBElement
	 *        located in a package specified in the constructor, or a w3c Node
	 *        object.
	 * @param factoryEPR EndpointReference of the resource creation factory.
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
	 * @param request
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
	 * @param resourceEPR EndpointReference pointing to the resource
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
	 * @param request
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
	 * @param resource resource to be updated. This must be a JAXBElement
	 *        located in a package specified in the constructor, or a w3c Node
	 *        object. If a filter is specified this is expected to be an
	 *        XmlFragment.
	 * @param resourceEPR EndpointReference pointing to the resource
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
	 * @param request
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
	 * @param resourceEPR EndpointReference pointing to the resource
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
	 * @param request
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
	 * @param resourceEPR EndpointReference pointing to a set of resources
	 * @param filter filter used if only a subset of the set of resources is desired
	 * @param filterNamespaces namespace declarations for any namespaces used
	 *        in the filter.
	 * @param filterDialect filter dialect used. Default is XPath 1.0
	 * @param maxElements the maximum number of elements to buffer at one time
	 * 
	 * @return the iterator
	 * @throws Exception 
	 */
	public ResourceIterator newIterator(final EndpointReferenceType resourceEPR,
			                            final List<Object> filter, 
			                            final Map<String, String> filterNamespaces, 
			                            final String filterDialect,
			                            final int maxElements,
			                            final Duration timeout)
			throws Exception {
		final WSManEnumerateRequest request = 
			new WSManEnumerateRequest(resourceEPR, null, binding);
		DialectableMixedDataType filterObject = request.createFilter(filterDialect,
				 filter, filterNamespaces);
		
		final Enumerate enumerate = request.createEnumerate(null, filterObject,
	             true,
	             maxElements,
	             EnumerationModeType.ENUMERATE_OBJECT_AND_EPR);
		request.setEnumerate(enumerate);
		TODO:
		// request.addNamespaceDeclarations(filterNamespaces);
		request.setOperationTimeout(timeout);
		return newIterator(request);
	}
	
	/**
	 * Create an iterator to iterate over a resource
	 * 
	 * @param request
	 * 
	 * @return the iterator
	 * @throws Exception 
	 */
	public ResourceIterator newIterator(final WSManEnumerateRequest request)
			throws Exception {
		return new ResourceIterator(request);
	}
}
