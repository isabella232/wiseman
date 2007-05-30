/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
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
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 **$Log: not supported by cvs2svn $
 **
 *
 * $Id: JAXBResourceImpl.java,v 1.3 2007-05-30 13:29:44 nbeers Exp $
 */
package com.sun.ws.management.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedURI;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.client.EnumerationCtx;
import com.sun.ws.management.client.JAXBResource;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XmlBinding;

/**
 * NOTE: This class has not yet been tested and is not ready for use yet.
 *
 * WS-Management Resource client handler, providing client side access to a
 * WS-Management service.
 *
 * @author APL
 *
 */

public class JAXBResourceImpl extends ResourceImpl implements JAXBResource {
	protected Resource resourceImpl = null;
	private EndpointReferenceType epr = new EndpointReferenceType();
	private static Logger log = Logger.getLogger(JAXBResourceImpl.class.getName());


	public JAXBResourceImpl(String destination, String resourceURI,SelectorSetType selectors, XmlBinding b) throws SOAPException, JAXBException{
		super(destination, resourceURI,selectors,b);
	}

	JAXBResourceImpl(String destination, String resourceURI,XmlBinding b) throws SOAPException, JAXBException {
		super(destination, resourceURI, null);
	}

	JAXBResourceImpl(Element eprElement, String endpointUrl, XmlBinding b) throws SOAPException, JAXBException {
		super(eprElement, endpointUrl, b);
	}

	JAXBResourceImpl(String destination, String resourceURI,SelectorSetType selectors, final String... packageNames) throws SOAPException, JAXBException{
		super(destination, resourceURI,selectors, null);
		setBinding(packageNames);
	}

	JAXBResourceImpl(String destination, String resourceURI, final String... packageNames) throws SOAPException, JAXBException {
		super(destination, resourceURI, null);
		setBinding(packageNames);
	}

	JAXBResourceImpl(Element eprElement, String endpointUrl, final String... packageNames) throws SOAPException, JAXBException {
		super(eprElement, endpointUrl, null);
		setBinding(packageNames);
	}



	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.JAXBResource#getObject()
	 */
	public Object getObject() throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException {
		ResourceState s = super.get();
		return binding.unmarshal(s.getDocument().getDocumentElement());
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.JAXBResource#putObject(java.lang.Object)
	 */
	public Object putObject(Object newState) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException {
		Document doc = Management.newDocument();
		binding.marshal(newState, doc);
		ResourceState s=super.put(doc);
		return binding.unmarshal(s.getDocument().getDocumentElement());
	}

	/**
	 * ResourceEnumerator provides WS-Enumeration support for WS-Management
	 * endpoints enumeration sessions. Instances of the resource enumerator are
	 * created using
	 *
	 * @see enumerate.
	 *
	 *
	 */
	public class ResourceEnumerator {
		protected EnumerationCtx enumerationContextId;
		protected long enumerationTimeout;
		protected boolean hasNext = true;
		protected JAXBElement<EnumerationModeType> mode = null;
		protected Resource enumerateImpl = null;

		/**
		 *
		 */
		protected ResourceEnumerator() {
		}

		/**
		 * Internal constructor for the ResourceEnumerator
		 *
		 * @param impl
		 * @param id
		 * @param timeout
		 * @param mode
		 * 		Either null (Enumerate Objects), ENUMERATE_EPR or EnumerateObjectAndEPR
		 */
		protected ResourceEnumerator(Resource impl, EnumerationCtx id, long timeout, EnumerationModeType enumModeType) {
			enumerateImpl = impl;
			enumerationContextId = id;
			enumerationTimeout = timeout;
			// Object[] mode = {factory.createEnumerationMode(EnumerationModeType.ENUMERATE_OBJECT_AND_EPR)};
			org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory factory = new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
			this.mode = factory.createEnumerationMode(enumModeType);//mode;
		}

		/**
		 * Simple iterator for the collection
		 *
		 * @param timeout -
		 *            timeout for each fetch from the WS-Management Resource
		 * @param maxElements -
		 *            number of elements to be fetched at a time
		 * @param maxCharacters -
		 *            maximum reply size
		 * @return an iterator.
		 */
		public Iterator iterator(long timeout, int maxElements,
				int maxCharacters) {
			return new ResourceIterator(timeout, maxElements, maxCharacters);
		}

		/**
		 * Simple iterator for the collection.
		 *
		 * @param maxElements -
		 *            number of elements to be fetched at a time
		 * @return an iterator.
		 */
		public Iterator iterator(int maxElements) {
			return new ResourceIterator(enumerationTimeout, maxElements,
					Resource.IGNORE_MAX_CHARS);
		}

		/**
		 * Implements the Iterator for the WS-Management ResoureEnumerator.
		 *
		 *
		 */
		public class ResourceIterator implements Iterator {
			protected boolean end = false;
			protected List results = null;
			protected Iterator listIterator = null;
			protected long timeout;
			protected int maxElements;
			protected int maxCharacters;
			protected ResourceIterator() {
				super();
			}

			/**
			 * Constructor for ResourceIterator. Invoked for ResourceEnumerator
			 * iterator().
			 *
			 * @param timeout -
			 *            Timeout for each fetch from the WS-Management
			 *            endpoint.
			 * @param maxElements -
			 *            number of elements to be fetched at a time
			 * @param maxCharacters -
			 *            maximum reply size
			 */
			protected ResourceIterator(long timeout, int maxElements,
					int maxCharacters) {
				super();
				this.timeout = timeout;
				this.maxElements = maxElements;
				this.maxCharacters = maxCharacters;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see java.util.Iterator#hasNext()
			 */
			public boolean hasNext() {
				if (!end && results == null) {
					try {
						results = pull(timeout, maxElements, maxCharacters);
						listIterator = results.iterator();
					} catch (Exception e) {
						end = true;
					}
				}
				if (!this.hasNext() && !listIterator.hasNext()) {
					end = true;
				}
				return (!end && listIterator.hasNext());
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see java.util.Iterator#next()
			 */
			public Object next() {
				if (!listIterator.hasNext()) {
					if (this.hasNext()) {
						try {
							results = pull(timeout, maxElements, maxCharacters);
							listIterator = results.iterator();
						} catch (Exception e) {
							end = true;
						}
					}
				}
				return listIterator.next();
			}

			/**
			 * Not supported. Throws UnsupportedOperationException
			 *
			 * @see java.util.Iterator#remove()
			 */
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}

		/**
		 * Pulls next enumeration set from the WS-Management endpoint.
		 *
		 * @param timeout -
		 *            Timeout for each fetch from the WS-Management endpoint.
		 * @param maxElements -
		 *            number of elements to be fetched at a time
		 * @param maxCharacters -
		 *            maximum reply size
		 * @return a List of retrieved objects
		 * @throws SOAPException
		 * @throws JAXBException
		 * @throws IOException
		 * @throws FaultException
		 * @throws DatatypeConfigurationException
		 */
		public List pull(long timeout, int maxElements, int maxCharacters)
								throws SOAPException,
									   JAXBException,
									   IOException,
									   FaultException,
									   DatatypeConfigurationException {

			ResourceState rs = enumerateImpl.pull( enumerationContextId,
											(int)timeout,maxElements, maxCharacters);

			PullResponse pullResponse = (PullResponse) binding.unmarshal(rs
					.getDocument().getDocumentElement());
			hasNext = pullResponse.getEndOfSequence() == null;
			ItemListType returnedItemsContainer = pullResponse.getItems();
			List<Object> items = returnedItemsContainer.getAny();
			List<Object> beans = new ArrayList<Object>(items.size());
			for (Object object : items) {

				if (object instanceof JAXBElement) {
					JAXBElement e = (JAXBElement) object;
					object = e.getValue();
				} else  if (object instanceof Element) {
					object = binding.unmarshal((Element)object);
				}

				beans.add(object);
			}
			return beans;
		}

		/**
		 * Pulls next enumeration set from the WS-Management endpoint.
		 *
		 * @param timeout -
		 *            Timeout for each fetch from the WS-Management endpoint.
		 * @param maxElements -
		 *            number of elements to be fetched at a time
		 * @param maxCharacters -
		 *            maximum reply size
		 * @return a List of retrieved objects
		 * @throws SOAPException
		 * @throws JAXBException
		 * @throws IOException
		 * @throws FaultException
		 * @throws DatatypeConfigurationException
		 */
		public List pull(int maxElements) throws SOAPException, JAXBException,
				IOException, FaultException, DatatypeConfigurationException {
			return pull(enumerationTimeout, maxElements,
					Resource.IGNORE_MAX_CHARS);
		}


		/**
		 * Releases the enumeration session.
		 *
		 * @throws SOAPException
		 * @throws JAXBException
		 * @throws IOException
		 * @throws FaultException
		 * @throws DatatypeConfigurationException
		 */
		public void release() throws SOAPException, JAXBException, IOException,
				FaultException, DatatypeConfigurationException {
			enumerateImpl.release(enumerationContextId);
			hasNext = false;
		}

		/**
		 * Renews the period of the enumeration session.
		 *
		 * @throws SOAPException
		 * @throws JAXBException
		 * @throws IOException
		 * @throws FaultException
		 * @throws DatatypeConfigurationException
		 */
		public void renew() throws SOAPException, JAXBException, IOException,
				FaultException, DatatypeConfigurationException {
			enumerateImpl.renew(enumerationContextId);
			// TODO: This is bogus unless there is an enumeration lifetime
			// specification when the enumeration was originally created.
		}

		/**
		 * @return Returns the enumerationContextId.
		 */
		public EnumerationCtx getEnumerationContextId() {
			return enumerationContextId;
		}

		/**
		 * @param enumerationContextId
		 *            The enumerationContextId to set.
		 */
		public void setEnumerationContextId(EnumerationCtx enumerationContextId) {
			this.enumerationContextId = enumerationContextId;
		}

		/**
		 * @return Returns the enumerationTimeout.
		 */
		public long getEnumerationTimeout() {
			return enumerationTimeout;
		}

		/**
		 * @return Returns false if last element of the enumeration has been
		 *         returned.
		 */
		public boolean hasNext() {
			return hasNext;
		}
	}


	/**
	 * @param destination
	 *            The WS-Management endpoint service URL.
	 */
	@Override
	public void setDestination(String destination) {
		this.destination = destination;
		if (epr == null) {
			this.epr = new EndpointReferenceType();
		}
		AttributedURI addressURI = new AttributedURI();
		addressURI.getOtherAttributes().put(SOAP.MUST_UNDERSTAND,
				Boolean.TRUE.toString());
		addressURI.setValue(destination);
		epr.setAddress(addressURI);
		// TODO: should update the Epr as well
		// should update the t.setter also
	}


	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.JAXBResource#getEpr()
	 */
	public EndpointReferenceType getEpr() {
		return epr;
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.JAXBResource#setEpr(org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType)
	 */
	public void setEpr(EndpointReferenceType epr) {
		this.epr = epr;
		destination = epr.getAddress().toString();
		// Get the reference parameters
		ReferenceParametersType refParams = epr.getReferenceParameters();
		List<Object> paramList = refParams.getAny();
		for (Object object : paramList) {
			if (object instanceof JAXBElement) {
				JAXBElement e = (JAXBElement) object;
				object = e.getValue();
			}
			if (object instanceof AttributedURI) {
				AttributedURI uri = (AttributedURI) object;
				this.resourceURI = uri.getValue();
				// FIXME: Update resourceImpl
			}
			if (object instanceof SelectorSetType) {
				SelectorSetType s = (SelectorSetType) object;
				this.selectorSet = s;
				// FIXME: Update resourceImpl
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.JAXBResource#enumerate(java.lang.String[], java.lang.String, org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType)
	 */
	public ResourceEnumerator enumerate(String[] filters, String dialect,
			EnumerationModeType mode) throws SOAPException, JAXBException, IOException,
			FaultException, DatatypeConfigurationException {
		//TODO: Need to properly handle EnumerationMode
		return enumerate(filters, dialect, mode);

	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.JAXBResource#enumerate(java.lang.String[], org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType)
	 */
	public ResourceEnumerator enumerate( String[] filters, EnumerationModeType mode)
										 throws SOAPException, JAXBException,
										 IOException, FaultException, DatatypeConfigurationException {

		return enumerate(filters, Resource.XPATH_DIALECT, mode);
	}

}
