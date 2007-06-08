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
 **Revision 1.12  2007/05/30 20:30:22  nbeers
 **Add HP copyright header
 **
 ** 
 *
 * $Id: UserHandler.java,v 1.13 2007-06-08 15:38:38 denis_rachal Exp $
 */
package framework.models;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;

import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.framework.Utilities;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.server.Filter;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.TransferExtensions;

/**
 * User delegate is responsible for processing request actions to be performed
 * on users.
 * 
 * @author wire
 * 
 */
public class UserHandler extends EnumerationHandler {

	private ObjectFactory FACTORY = new ObjectFactory();;
	
	public static final QName DIALECT = new QName("Dialect");
	public static final String NS_URI = "http://examples.hp.com/ws/wsman/user";
	public static final String NS_PREFIX = "user";
	
    public static final QName USER = new QName(NS_URI, "user", NS_PREFIX);

	public UserHandler() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void create(HandlerContext context, Management request,
			Management response) {

		// Normal processing to create a new UserObject
		UserType user = null;

		// Determine if this is a fragmentRequest
		SOAPElement[] allHeaders = null;
		SOAPElement fragmentHeader = null;

		try {
			allHeaders = request.getHeaders();

			// Locate Fragment header
			fragmentHeader = locateFragmentHeader(allHeaders);

		} catch (SOAPException sexc) {
			sexc.printStackTrace();
		}

		// Processing for FragmentCreate request
		if (fragmentHeader != null) {
			// retrieve the instance to modify
			final UserType userOrg = findInstance(request);
			user = new UserType();
			
			// Copy the original values into the new object
			user.setAddress(userOrg.getAddress());
			user.setAge(userOrg.getAge());
			user.setCity(userOrg.getCity());
			user.setFirstname(userOrg.getFirstname());
			user.setLastname(userOrg.getLastname());
			user.setState(userOrg.getState());
			user.setZip(userOrg.getZip());
			
			// now retrieve the server specific content to replace based on
			// fragmentExp
			try {
				TransferExtensions transfer = new TransferExtensions(request);

				JAXBElement<MixedDataType> resource = (JAXBElement<MixedDataType>) transfer
						.getResource(transfer.XML_FRAGMENT);

				if (resource == null) {
					throw new InvalidRepresentationFault(
							InvalidRepresentationFault.Detail.INVALID_VALUES);
				}
				List<Object> nodes = resource.getValue().getContent();
				if (nodes == null) {
					throw new InvalidRepresentationFault(
							InvalidRepresentationFault.Detail.INVALID_VALUES);
				}
				Iterator iter = nodes.iterator();
				while (iter.hasNext()) {
					Object obj = iter.next();
					if (obj instanceof Element) {
						Element elem = (Element) obj;
						if (elem.getLocalName().equals("firstname")) {
							user.setFirstname(elem.getTextContent());
						} else if (elem.getLocalName().equals("lastname")) {
							user.setLastname(elem.getTextContent());
						} else if (elem.getLocalName().equals("address")) {
							user.setAddress(elem.getTextContent());
						} else if (elem.getLocalName().equals("city")) {
							user.setCity(elem.getTextContent());
						} else if (elem.getLocalName().equals("state")) {
							user.setState(elem.getTextContent());
						} else if (elem.getLocalName().equals("zip")) {
							user.setZip(elem.getTextContent());
						} else if (elem.getLocalName().equals("age")) {
							user.setAge(new Integer(elem.getTextContent()));
						} else {
							// Don't recognize this element
						}
					}
				}
			} catch (SOAPException e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			} catch (JAXBException e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}

			// Add the fragment response header
			try {
				response.getHeader().addChildElement(fragmentHeader);
			} catch (SOAPException e) {
				throw new InternalErrorFault(e);
			}
			UserStore.update(userOrg, user);
		}
		// Processing for regular NON-Fragment request
		else {
			try {
				TransferExtensions transfer = new TransferExtensions(request);
				JAXBElement<UserType> ob = (JAXBElement<UserType>) transfer.getResource(UserEnumerationHandler.USER);
				if (ob == null) {
					throw new InvalidRepresentationFault(
							InvalidRepresentationFault.Detail.INVALID_VALUES);
				}
				user = ob.getValue();

			} catch (JAXBException e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			} catch (SOAPException e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}

	        // Null out age as it's already set by default
			UserStore.add(user);
		}

		final HashMap<String, String> selectorMap = new HashMap<String, String>();
		selectorMap.put("firstname", user.getFirstname());
		selectorMap.put("lastname", user.getLastname());
		try {
			TransferExtensions xferResponse = new TransferExtensions(response);
			EndpointReferenceType epr = TransferExtensions
					.createEndpointReference(request.getTo(), request
							.getResourceURI(), selectorMap);
			xferResponse.setCreateResponse(epr);
			// appendCreateResponse(response, resourceUri, selectorMap);
		} catch (JAXBException e) {
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		} catch (SOAPException e) {
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		}
	}

	@Override
	public void get(HandlerContext context, Management request,
			Management response) {

		try {
			// Find an existing instance of this object in the model
			final UserType user = findInstance(request);

			final JAXBElement<UserType> resource = FACTORY.createUser(user);

			final TransferExtensions transfer = new TransferExtensions(request);
			final TransferExtensions transferResponse = new TransferExtensions(
					response);

			transferResponse.setFragmentGetResponse(transfer
					.getFragmentHeader(), resource);

		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		}
	}

	public void customaction(HandlerContext context, Management request,
			Management response) {
		Document responseDoc = Management.newDocument();
		Element respElement = responseDoc.createElement("response");
		Element respParam = responseDoc.createElement("param1");
		respParam.setTextContent("The answer is plastics");
		responseDoc.appendChild((Node) respElement);
		respElement.appendChild((Node) respParam);

		// Text nextNode = responseDoc.createTextNode("The answer is plastics");
		// respElement.appendChild(nextNode);
		try {
			response.getBody().addDocument(responseDoc);
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		}
	}

	@Override
	public void put(HandlerContext context, Management request,
			Management response) {
		// Find an existing instance of this object in the model
		final UserType userOrg = findInstance(request);

		// DONE: figure out if this is a fragment transfer request
		try {
			final TransferExtensions xferRequest = new TransferExtensions(request);
			final TransferExtensions xferResponse = new TransferExtensions(response);
			final SOAPHeaderElement fragmentHeader = xferRequest.getFragmentHeader();

			// Check if this is a fragment request
			if (fragmentHeader != null) {
				final UserType user = new UserType();

				user.setFirstname(userOrg.getFirstname());
				user.setLastname(userOrg.getLastname());
				user.setAddress(userOrg.getAddress());
				user.setCity(userOrg.getCity());
				user.setState(userOrg.getState());
				user.setZip(userOrg.getZip());
				user.setAge(userOrg.getAge());

				final Object resource = xferRequest
						.getResource(TransferExtensions.XML_FRAGMENT);

				// Check if this is an XmlFragment
				if (resource == null) {
					throw new InvalidRepresentationFault(
							InvalidRepresentationFault.Detail.INVALID_VALUES);
				} else {
					final JAXBElement fragment = (JAXBElement) resource;
                    
					// Update the user object with the fragment values
					final List<Object> nodelist = 
						((MixedDataType)fragment.getValue()).getContent();
					for (int i = 0; i < nodelist.size(); i++) {
						if (nodelist.get(i) instanceof Node) {
							final Node node = (Node) nodelist.get(i);
							if (node.getLocalName().equals("firstname")) {
								user.setFirstname(node.getTextContent());
							} else if (node.getLocalName().equals("lastname")) {
								user.setLastname(node.getTextContent());
							} else if (node.getLocalName().equals("address")) {
								user.setAddress(node.getTextContent());
							} else if (node.getLocalName().equals("city")) {
								user.setCity(node.getTextContent());
							} else if (node.getLocalName().equals("state")) {
								user.setState(node.getTextContent());
							} else if (node.getLocalName().equals("zip")) {
								user.setZip(node.getTextContent());
							} else if (node.getLocalName().equals("age")) {
								user.setAge(new Integer(node
												.getTextContent()));
							} else {
								throw new InvalidRepresentationFault(
										InvalidRepresentationFault.Detail.INVALID_VALUES);
							}
						}
					}

					final JAXBElement<UserType> userJaxb = FACTORY
							.createUser(user);

					xferResponse.setFragmentPutResponse(fragmentHeader,
							userJaxb);
					UserStore.update(userOrg, user);
				}
			} else { // ELSE Process NON-FRAGMENT requests

				try {
					final Object resource = xferRequest
							.getResource(UserEnumerationHandler.USER);

					// Check if this is a User object
					if (resource == null) {
						throw new InvalidRepresentationFault(
								InvalidRepresentationFault.Detail.INVALID_VALUES);
					} else {
						final JAXBElement ob = (JAXBElement) resource;
						final UserType user = (UserType) ob.getValue();

						xferResponse.setPutResponse(ob);
						UserStore.update(userOrg, user);
					}
				} catch (JAXBException e) {
					throw new InvalidRepresentationFault(
							InvalidRepresentationFault.Detail.INVALID_VALUES);
				}
			}
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		}
	}

	@Override
	public void delete(HandlerContext context, Management request,
			Management response) {
		// Find an existing instance
		UserType userOrg = findInstance(request);

		if (userOrg == null) {
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		} else {
			try {
				final TransferExtensions xferRequest = new TransferExtensions(
						request);
				final TransferExtensions xferResponse = new TransferExtensions(
						response);
				final SOAPHeaderElement fragmentHeader = xferRequest
						.getFragmentHeader();

				// delete only the described sub elements.
				if (fragmentHeader != null) {
					// Delete that one optional component from userOrg
					final UserType user = new UserType();

					user.setFirstname(userOrg.getFirstname());
					user.setLastname(userOrg.getLastname());
					user.setAddress(userOrg.getAddress());
					user.setCity(userOrg.getCity());
					user.setState(userOrg.getState());
					user.setZip(userOrg.getZip());
					user.setAge(userOrg.getAge());
					
					final JAXBElement<UserType> userJaxb = FACTORY.createUser(user);
					final Filter filter = TransferExtensions.createFilter(xferRequest);
					
					final Document content = xferResponse.getDocumentBuilder().newDocument();
					
					try {
						xferResponse.getXmlBinding().marshal(userJaxb, content);
					} catch (Exception e) {
						final String explanation = 
							 "XML Binding marshall failed for object of type: UserType";
						throw new InternalErrorFault(SOAP.createFaultDetail(explanation, null, e, null));
					}
				    // Evaluate & create the XmlFragmentElement
					final JAXBElement<MixedDataType> fragment =
						TransferExtensions.createXmlFragment(filter.evaluate(content.getDocumentElement()));

						// Update the user object with the fragment values
						final List<Object> nodelist = fragment.getValue().getContent();
						for (int i = 0; i < nodelist.size(); i++) {
							if (nodelist.get(i) instanceof Node) {
								final Node node = (Node) nodelist.get(i);
								if (node.getLocalName().equals("age")) {
									user.setAge(null);
								} else {
									// Only allowed to delete the age component
									throw new InvalidRepresentationFault(
											InvalidRepresentationFault.Detail.INVALID_VALUES);
								}
							}
						}

						UserStore.update(userOrg, user);

						// add the Fragment header passed in to the response
						xferResponse.setFragmentDeleteResponse(fragmentHeader);
				} else {// If NO Fragment processing then
					UserStore.delete(userOrg.getFirstname(), userOrg
							.getLastname());
				}
			} catch (SOAPException e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}
		}
	}
	
	// private methods follow

	private UserType findInstance(Management request) {
		final String firstname;
		final String lastname;
		try {
			firstname = Utilities.getSelectorByName("firstname",
					request.getSelectors()).getContent().get(0).toString();
			lastname = Utilities.getSelectorByName("lastname",
					request.getSelectors()).getContent().get(0).toString();
		} catch (Exception e) {
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}
		final UserType userOb = UserStore.get(firstname, lastname);
		if (userOb == null)
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		return userOb;
	}
}