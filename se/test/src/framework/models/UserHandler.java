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
 * $Id: UserHandler.java,v 1.12 2007-05-30 20:30:22 nbeers Exp $
 */
package framework.models;


import java.util.HashMap;
import java.util.HashSet;
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
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.TransferExtensions;

/**
 * User delegate is responsible for processing request actions to be performed
 * on users.
 * 
 * @author wire
 * 
 */
public class UserHandler extends TransferSupport {
	private static HashSet<UserModelObject> users = new HashSet<UserModelObject>(10);

	private ObjectFactory FACTORY = new ObjectFactory();
	private static String resourceUri = "wsman:auth/user";
	
	public static final QName DIALECT = new QName("Dialect");

	public UserHandler() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void create(HandlerContext context, Management request,
			Management response) {
		// create a new user class and add it to the list
		UserModelObject userObject = new UserModelObject();

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
			// retrieve the userModelObject instance to modify
			userObject = findInstance(request);

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
							userObject.setFirstname(elem.getTextContent());
						} else if (elem.getLocalName().equals("lastname")) {
							userObject.setLastname(elem.getTextContent());
						} else if (elem.getLocalName().equals("address")) {
							userObject.setAddress(elem.getTextContent());
						} else if (elem.getLocalName().equals("city")) {
							userObject.setCity(elem.getTextContent());
						} else if (elem.getLocalName().equals("state")) {
							userObject.setState(elem.getTextContent());
						} else if (elem.getLocalName().equals("zip")) {
							userObject.setZip(elem.getTextContent());
						} else if (elem.getLocalName().equals("age")) {
							userObject
									.setAge(new Integer(elem.getTextContent()));
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
				e.printStackTrace();
			}
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
			userObject.setFirstname(user.getFirstname());
			userObject.setLastname(user.getLastname());
			userObject.setAddress(user.getAddress());
			userObject.setCity(user.getCity());
			userObject.setState(user.getState());
			userObject.setZip(user.getZip());
			if (user.getAge() != null) {
				userObject.setAge(user.getAge());
			}
			users.add(userObject);
		}

		final HashMap<String, String> selectorMap = new HashMap<String, String>();
		selectorMap.put("firstname", userObject.getFirstname());
		selectorMap.put("lastname", userObject.getLastname());
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
			final UserModelObject userOb = findInstance(request);
			final UserType user = new UserType();

			user.setFirstname(userOb.getFirstname());
			user.setLastname(userOb.getLastname());
			user.setAddress(userOb.getAddress());
			user.setCity(userOb.getCity());
			user.setState(userOb.getState());
			user.setZip(userOb.getZip());
			user.setAge(userOb.getAge());

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

	private UserModelObject findInstance(Management request) {
		UserModelObject searchUser = new UserModelObject();
		try {
			searchUser.setFirstname(Utilities.getSelectorByName("firstname",
					request.getSelectors()).getContent().get(0).toString());
			searchUser.setLastname(Utilities.getSelectorByName("lastname",
					request.getSelectors()).getContent().get(0).toString());
		} catch (Exception e) {
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}

		if (!users.contains(searchUser))
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);

		UserModelObject userOb = null;
		for (Iterator iter = users.iterator(); iter.hasNext();) {
			UserModelObject userObTest = (UserModelObject) iter.next();
			if (userObTest.hashCode() == searchUser.hashCode()) {
				userOb = userObTest;
				break;
			}
		}
		if (userOb == null)
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		return userOb;
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
		final UserModelObject userOb = findInstance(request);

		// DONE: figure out if this is a fragment transfer request
		try {
			final TransferExtensions xferRequest = new TransferExtensions(request);
			final TransferExtensions xferResponse = new TransferExtensions(response);
			final SOAPHeaderElement fragmentHeader = xferRequest.getFragmentHeader();

			// Check if this is a fragment request
			if (fragmentHeader != null) {

				final Object resource = xferRequest
						.getResource(TransferExtensions.XML_FRAGMENT);

				// Check if this is an XmlFragment
				if (resource == null) {
					throw new InvalidRepresentationFault(
							InvalidRepresentationFault.Detail.INVALID_VALUES);
				} else {
					final JAXBElement<MixedDataType> fragment = (JAXBElement<MixedDataType>) resource;
					
                    users.remove(userOb);
                    
					// Update the user object with the fragment values
					final List<Object> nodelist = fragment.getValue()
							.getContent();
					for (int i = 0; i < nodelist.size(); i++) {
						if (nodelist.get(i) instanceof Node) {
							final Node node = (Node) nodelist.get(i);
							if (node.getLocalName().equals("firstname")) {
								userOb.setFirstname(node.getTextContent());
							} else if (node.getLocalName().equals("lastname")) {
								userOb.setLastname(node.getTextContent());
							} else if (node.getLocalName().equals("address")) {
								userOb.setAddress(node.getTextContent());
							} else if (node.getLocalName().equals("city")) {
								userOb.setCity(node.getTextContent());
							} else if (node.getLocalName().equals("state")) {
								userOb.setState(node.getTextContent());
							} else if (node.getLocalName().equals("zip")) {
								userOb.setZip(node.getTextContent());
							} else if (node.getLocalName().equals("age")) {
								userOb
										.setAge(new Integer(node
												.getTextContent()));
							} else {
								// Don't recognize this element. Ignore it.
							}
						}
					}

					// Set the response
					final UserType user = new UserType();

					user.setFirstname(userOb.getFirstname());
					user.setLastname(userOb.getLastname());
					user.setAddress(userOb.getAddress());
					user.setCity(userOb.getCity());
					user.setState(userOb.getState());
					user.setZip(userOb.getZip());
					user.setAge(userOb.getAge());

					final JAXBElement<UserType> userJaxb = FACTORY
							.createUser(user);

					users.add(userOb);
					xferResponse.setFragmentPutResponse(fragmentHeader,
							userJaxb);
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

						// Set all fields to conform to the provided User
						// document
						users.remove(userOb);
						
						userOb.setFirstname(user.getFirstname());
						userOb.setLastname(user.getLastname());
						userOb.setAddress(user.getAddress());
						userOb.setCity(user.getCity());
						userOb.setState(user.getState());
						userOb.setZip(user.getZip());
						userOb.setAge(user.getAge());

						users.add(userOb);
						xferResponse.setPutResponse(ob);
					}
				} catch (JAXBException e) {
					throw new InvalidRepresentationFault(
							InvalidRepresentationFault.Detail.INVALID_VALUES);
				}
			}
			System.out.println("PutResp:"+response);
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
		UserModelObject searchUser = findInstance(request);

		if (searchUser == null)
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		else {
			try {
				final TransferExtensions xferRequest = new TransferExtensions(request);
				final TransferExtensions xferResponse = new TransferExtensions(response);
				final SOAPHeaderElement fragmentHeader = xferRequest.getFragmentHeader();

				// TODO: do delete only the described sub elements.
				if (fragmentHeader != null) {
					// Delete that one optional component from searchUser
					users.remove(searchUser);

					UserModelObject withoutAge = new UserModelObject();
					withoutAge.setAddress(searchUser.getAddress());
					withoutAge.setCity(searchUser.getCity());
					withoutAge.setFirstname(searchUser.getFirstname());
					withoutAge.setLastname(searchUser.getLastname());
					withoutAge.setState(searchUser.getState());
					withoutAge.setZip(searchUser.getZip());
					
					users.add(withoutAge);

					// add the Fragment header passed in to the response
					xferResponse.setFragmentDeleteResponse(fragmentHeader);
					// System.out.println("DelResp:"+response);
				} else {// If NO Fragment processing then
					users.remove(searchUser);
				}
			} catch (SOAPException e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}

		}

	}
}