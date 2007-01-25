/*
 * Copyright 2006 Hewlett-Packard Development Company, L.P.
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
 */

package com.sun.ws.management.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.xpath.XPathExpressionException;

import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.AlreadyExistsFault;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.server.BaseSupport;
import com.sun.ws.management.server.Filter;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XPath;

public class TransferExtensions extends Transfer {
    
    public static final QName FRAGMENT_TRANSFER =
            new QName(Management.NS_URI, "FragmentTransfer", Management.NS_PREFIX);
    
    public static final QName XML_FRAGMENT =
            new QName(Management.NS_URI, "XmlFragment", Management.NS_PREFIX);
    
    public static final QName DIALECT = new QName("Dialect");
    
    public TransferExtensions() throws SOAPException {
        super();
    }
    
    public TransferExtensions(final Addressing addr) throws SOAPException {
        super(addr);
    }
    
    public TransferExtensions(final InputStream is) throws SOAPException, IOException {
        super(is);
    }
    
    /**
     * Returns the FragmentTransfer SOAPElement if it exists
     *
     * @return FragmentTransfer SOAPElement if it exists else null
     */
    public SOAPHeaderElement getFragmentHeader() throws SOAPException {
        for (final SOAPElement se : getChildren(getHeader(), FRAGMENT_TRANSFER)) {
            if (se instanceof SOAPHeaderElement) {
                final SOAPHeaderElement she = (SOAPHeaderElement) se;
                if (she.getMustUnderstand()) {
                    return she;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the FragmentTransfer Dialect if FragmentTrasfer header exists
     *
     * @return FragmentTransfer Dialect if FragmentTrasfer header exists else null
     */
    public String getFragmentDialect() throws SOAPException {
    	return getFragmentDialect(getFragmentHeader());
    }
    
    private static String getFragmentDialect(SOAPHeaderElement header) throws SOAPException {
    	
    	if (header == null)
           return null;
    	
    	String dialect = header.getAttributeValue(TransferExtensions.DIALECT);
    	if ((dialect == null) || (dialect.length() == 0)) {
    		dialect = XPath.NS_URI;
    	}
    	return dialect;
    }
    
    /**
     *
     * @param query
     * @param dialect
     * @throws SOAPException
     * @throws JAXBException
     */
    public void setFragmentHeader(final String expression, final String dialect) 
    throws SOAPException, JAXBException {

        // remove existing, if any
        removeChildren(getHeader(), FRAGMENT_TRANSFER);
        
        final DialectableMixedDataType dialectableMixedDataType = 
                Management.FACTORY.createDialectableMixedDataType();
        if (dialect != null) {
            dialectableMixedDataType.setDialect(dialect);
        }
        dialectableMixedDataType.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, 
                Boolean.TRUE.toString());
        
        //add the query string to the content of the FragmentTransfer Header
        dialectableMixedDataType.getContent().add(expression);
        
        final JAXBElement<DialectableMixedDataType> fragmentTransfer =
                Management.FACTORY.createFragmentTransfer(dialectableMixedDataType);
        
        //set the SOAP Header for Fragment Transfer
        getXmlBinding().marshal(fragmentTransfer, getHeader());
    }
    
    /**
     * Inserts the FragmentTransferHeader into the SOAP Headers
     *
     * @param fragmentTransferHeader
     * @throws SOAPException
     */
    private void setFragmentHeader(final SOAPHeaderElement fragmentTransferHeader) 
    throws SOAPException {
        // remove existing, if any
        removeChildren(getHeader(), FRAGMENT_TRANSFER);
        getHeader().addChildElement(fragmentTransferHeader);
    }
    
    /**
     * Inserts the resource as a Fragment-level Get response into the body 
     * and adds the FragmentTransfer Header to the SOAP Headers.
     * Fragment-level filtering is done on the resource using 
     * filter dialect in fragmentTransferHeader, if fragmentTransferHeader
     * is not null, otherwise the compete resource is added to the response.
     *
     * @param fragmentTransferHeader The Fragement Transfer Header
     * to be returned in the headers, if any
     * @param resource  Resource to be returned for this Get. The binding must be
     *                  able to marshall this object.
     * @throws JAXBException
     * @throws XPathExpressionException 
     */
    public void setFragmentGetResponse(
			final SOAPHeaderElement fragmentTransferHeader,
			final Object resource) throws JAXBException, SOAPException {

		final Object fragment;
		if (fragmentTransferHeader != null) {
			final Filter filter = createFilter(fragmentTransferHeader);

				final Element item;

				if (resource instanceof Element) {
					item = (Element) resource;
				} else if (resource instanceof Document) {
					item = ((Document)resource).getDocumentElement();
				} else {
					final Document content = getDocumentBuilder().newDocument();
					
					try {
						getXmlBinding().marshal(resource, content);
					} catch (Exception e) {
						final String explanation = 
							 "XML Binding marshall failed for object of type: "
		                     + resource.getClass().getName();
						throw new InternalErrorFault(SOAP.createFaultDetail(explanation, null, e, null));
					}
                    item = content.getDocumentElement();
				}
				// Evaluate & create the XmlFragmentElement
				fragment = createXmlFragment(filter.evaluate(item));
				final String nsURI = item.getNamespaceURI();
				final String nsPrefix = item.getPrefix();
				if (nsPrefix != null && nsURI != null) {
					getBody().addNamespaceDeclaration(nsPrefix, nsURI);
				}
		} else {
			fragment = resource;
		}
        
        // add the header
		if (fragmentTransferHeader != null)
		    setFragmentHeader(fragmentTransferHeader);
		
        // add the payload to the body
		if (fragment instanceof Document) {
			getBody().addDocument((Document)fragment);
		} else {
            getXmlBinding().marshal(fragment, getBody());
		}
    }
    
    /**
     * Inserts the Fragment-level Get response into the body 
     * and adds the FragmentTransfer Header to the SOAP Headers.
     *
     * @param fragmentTransferHeader The Fragement Transfer Header to be returned in the headers
     * @param fragment  Fragment object for the response
     * @throws JAXBException
     */
    public void setFragmentGetResponse(final SOAPHeaderElement fragmentTransferHeader, 
            final JAXBElement<MixedDataType> fragment) throws JAXBException, SOAPException {

        setFragmentHeader(fragmentTransferHeader);
        //add payload to the body
        getXmlBinding().marshal(fragment, getBody());
    }
    
    /**
     * Handles the Fragment-level Get response by wrapping the content into
     * an XmlFragment element and adding the FragmentTransfer Header to the SOAP Headers.
     *
     * @param fragmentTransferHeader The Fragement Transfer Header to be returned in the headers
     * @param content                A Collection of Objects
     * @throws JAXBException
     */
    public void setFragmentGetResponse(final SOAPHeaderElement fragmentTransferHeader, 
            final List<Object> content) throws JAXBException, SOAPException {
        
        final JAXBElement<MixedDataType> xmlFragment = buildXmlFragment(content);

        setFragmentGetResponse(fragmentTransferHeader, xmlFragment);
    }
    
    /**
     * Builds the XmlFragment element with the supplied content
     *
     * @param content
     * @return JAXBElement<MixedDataType>
     */
    private JAXBElement<MixedDataType> buildXmlFragment(final List<Object> content) throws SOAPException {
        //build the JAXB Wrapper Element
        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
        mixedDataType.getContent().addAll(content);
        //create the XmlFragmentElement
        final JAXBElement<MixedDataType> xmlFragment = 
                Management.FACTORY.createXmlFragment(mixedDataType);
        //if there was no content, then this is NIL
        if (content.size() <= 0) {
            xmlFragment.setNil(true);
        }
        return xmlFragment;
    }
    
    /**
     * Handles the Fragment Delete operation
     *
     * @param fragmentHeader
     * @param nodes
     * @throws SOAPException
     */
    public void setFragmentDeleteResponse(final SOAPHeaderElement fragmentHeader, 
            final List<Node> nodes) throws SOAPException {
        final Node selectedNode = nodes.get(0);
        synchronized (selectedNode) {
            final Node parentNode = selectedNode.getParentNode();
            if (parentNode != null) {
                final Node childNode = parentNode.removeChild(selectedNode);
                try {
                    getXmlBinding().unmarshal(parentNode);
                } catch (JAXBException e) {
                    // validation failed
                    parentNode.appendChild(childNode);//rollback
                    // TODO: is this the right fault?
                    throw new AccessDeniedFault();
                }
            } else {
                // TODO
            }
            setFragmentHeader(fragmentHeader);
        }
    }
    
    public void setFragmentPutResponse(final SOAPHeaderElement fragmentHeader, 
            final List<Object> requestContent, final String expression, final List<Node> nodes) 
            throws SOAPException, JAXBException {

        final JAXBElement<MixedDataType> xmlFragment = buildXmlFragment(requestContent);
        
        final Node resultNode = nodes.get(0);//TODO will there ever be more than one??
        final Object o = requestContent.get(0);
        if (resultNode instanceof Text) {
            //okay this is text and we need to set it on a text node
            if (o instanceof String) {
                resultNode.setNodeValue((String) o);
            } else {
                throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
            }
        } else {
            if (o instanceof Node) {
                final Node parentNode = resultNode.getParentNode();
                if (parentNode != null) {
                    synchronized (parentNode) {
                        final Node node = (Node) o;
                        //node needs to be imported
                        final Node copyNode = parentNode.getOwnerDocument().importNode(node, true);
                        final Node replacedNode = parentNode.replaceChild(copyNode, resultNode);
                        try {
                            getXmlBinding().unmarshal(parentNode);
                        } catch (JAXBException e) {
                            parentNode.replaceChild(replacedNode, copyNode);
                            throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_FRAGMENT);
                        }
                    }
                } else {
                    // TODO
                }
            } else {
                //TODO fault
            }
        }
        
        //echo the fragment transfer header
        setFragmentHeader(fragmentHeader);
        //add payload to the body
        getXmlBinding().marshal(xmlFragment, getBody());
        
    }
    
    /**
     * Inserts the specified resource into the body of the Create response.
     * If wrapAsFragment is true and a Fragment Trasfer header is specified,
     * the object will be filtered and wrapped within a
     * <code>wsman:XmlFragment</code> node, otherwise the object will be
     * marshalled directly into the body of the response.
     * 
     * Filtering is done on the resource using the
     * filter dialect in specified in the fragmentTransferHeader,
     * if fragmentTransferHeader is not null and wrapAsFragment is true,
     * otherwise the resource is added to the response without
     * filtering or wrapping.
     * 
     * If the Fragment Transfer header is not null,
     * it is always added to the response.
     * 
     * @see BaseSupport.getSupportedDialects() for information on
     * supported filter dialects.
     *
     * @param resource  Resource to be returned for this Create. 
     * The binding must be able to marshall this object.
     * @param wrapAsFragment 
     * @param fragmentTransferHeader The Fragement Transfer Header
     * to be returned in the headers, if any, and to specify Fragment Transfer
     * filter processing
     * @throws JAXBException
     * @throws XPathExpressionException 
     */
    public void setFragmentCreateResponse(final Object resource,
    		final boolean wrapAsFragment,
			final SOAPHeaderElement fragmentTransferHeader
			) throws JAXBException, SOAPException {

		final Object fragment;
		if ((fragmentTransferHeader != null) && (wrapAsFragment)) {
			Filter filter = createFilter(this);

			try {
				final Element item;
				if (resource instanceof Element) {
					item = (Element) resource;
				} else if (resource instanceof Document) {
					item = ((Document) resource).getDocumentElement();
					// append the Element to the owner document if
					// it has
					// not been done
					// this is critical for XPath filtering to work
					final Document owner = item.getOwnerDocument();
					if (owner.getDocumentElement() == null) {
						owner.appendChild(item);
					}
				} else {
					Document content = Management.newDocument(); // db.newDocument();
					try {
						getXmlBinding().marshal(resource, content);
					} catch (Exception e) {
						final String explanation = 
							 "XML Binding marshall failed for object of type: "
		                     + resource.getClass().getName();
						throw new InternalErrorFault(SOAP.createFaultDetail(explanation, null, e, null));
					}
					item = content.getDocumentElement();
				}
				NodeList result = filter.evaluate(item);
				final MixedDataType mixedDataType = Management.FACTORY
						.createMixedDataType();
				if ((result != null) && (result.getLength() > 0)) {
					// XPath returned a set of nodes. Add them.
					for (int i = 0; i < result.getLength(); i++) {
						mixedDataType.getContent().add(result.item(i));
					}
				}
				// create the XmlFragmentElement
				fragment = Management.FACTORY.createXmlFragment(mixedDataType);
			} catch (FaultException e) {
				throw new CannotProcessFilterFault();
			} catch (Exception e) {
				throw new CannotProcessFilterFault();
			}
		} else {
			fragment = resource;
		}
        
        // add the header
		if (fragmentTransferHeader != null)
		    setFragmentHeader(fragmentTransferHeader);
		
		
        // add the payload to the body
        getXmlBinding().marshal(fragment, getBody());

    }
    
    public void setFragmentCreateResponse(final SOAPHeaderElement fragmentHeader, 
            final List<Object> requestContent, final String expression, final List<Node> nodes, 
            final EndpointReferenceType epr) throws SOAPException, JAXBException {
        
        final JAXBElement<MixedDataType> xmlFragment = buildXmlFragment(requestContent);
        
        final Node resultNode = nodes.get(0);//TODO will there ever be more than one??
        final Object o = requestContent.get(0);
        if (resultNode instanceof Text) {
            //okay this is text and we need to set it on a text node
            if (o instanceof String) {
                if (resultNode.getNodeValue() == null) {
                    resultNode.setNodeValue((String) o);
                } else {
                    throw new AlreadyExistsFault();//TODO is this correct?  or do I need to add the node to it
                }
            } else {
                //TODO not sure what to do in this case..its invalid for sure
            }
        } else {  //TODO we may need to check if it is an array and if the element denoted by xpath already exists..if so throw exception
            if (o instanceof Node) {
                final Node parentNode = resultNode.getParentNode(); //right now this assumes that the xpath returned the last element in the array and we add another element
                //I think the spec implies the xpath points to the spot in the array where they want it put...
                if (parentNode != null) {
                    synchronized (parentNode) {
                        final Node node = (Node) o;
                        //node needs to be imported
                        final Node copyNode = parentNode.getOwnerDocument().importNode(node, true);
                        final Node addedNode = parentNode.appendChild(copyNode);
                        try {
                            getXmlBinding().unmarshal(parentNode);
                        } catch (JAXBException e) {
                            parentNode.removeChild(addedNode);
                            throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_FRAGMENT);
                        }
                    }
                } else {
                    // TODO
                }
            } else {
                //TODO fault
            }
        }
        
        //echo the fragment transfer header
        setFragmentHeader(fragmentHeader);
        //add EPR to the body
        final JAXBElement<EndpointReferenceType> eprElement = Addressing.FACTORY.createEndpointReference(epr);
        getXmlBinding().marshal(eprElement, getBody());
        
    }

	public Object getResource(QName element) throws JAXBException, SOAPException {
		Object resource = super.getResource(element);
		if (resource == null) {
	        resource = (JAXBElement<MixedDataType>) unbind(getBody(), XML_FRAGMENT);
		}
		return resource;
	}
	
	/**
	 * Create a Filter from an Enumeration request
	 * 
	 * @return Returns a Filter object if a filter exists in the request,
	 *         otherwise null.
	 * @throws CannotProcessFilterFault,
	 *             FilteringRequestedUnavailableFault, InternalErrorFault
	 */
	public static Filter createFilter(final Transfer request)
			throws CannotProcessFilterFault, FilteringRequestedUnavailableFault {
		try {
			final TransferExtensions transfer = new TransferExtensions(
					request);
			final SOAPHeaderElement fragmentHeader = transfer.getFragmentHeader();
			
            return createFilter(fragmentHeader);
		} catch (SOAPException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}
	
	private static Filter createFilter(final SOAPHeaderElement fragmentHeader)
			throws CannotProcessFilterFault, FilteringRequestedUnavailableFault, SOAPException {

			if (fragmentHeader == null)
				return null;

			final NamespaceMap nsMap = new NamespaceMap(fragmentHeader);
			
			NodeList nodes = fragmentHeader.getChildNodes();
			final List<Node> filter = new ArrayList<Node>(nodes.getLength());
			for (int i = 0; i < nodes.getLength(); i++) {
				filter.add(nodes.item(i));
			}
			return BaseSupport.createFilter(getFragmentDialect(fragmentHeader),
					filter, nsMap);
	}
	
    /**
     * Create a JAXBElement object that is a wsman:XmlFragment from a list
     * of XML Nodes.
     * 
     * @param nodes Nodes to be inserted into the XmlFragment element.
     * @return XmlFragment JAXBElement object.
     */
	public static JAXBElement<MixedDataType> createXmlFragment(List<Node> nodes) {
		final MixedDataType mixedDataType = Management.FACTORY
				.createMixedDataType();
		for (int j = 0; j < nodes.size(); j++) {
			mixedDataType.getContent().add(
					nodes.get(j));
		}
		// create the XmlFragmentElement
		JAXBElement<MixedDataType> fragment = Management.FACTORY
				.createXmlFragment(mixedDataType);
		return fragment;
	}
	
    /**
	 * Create a JAXBElement object that is a wsman:XmlFragment from a NodeList
	 * 
	 * @param nodes Nodes to be inserted into the XmlFragment element.
	 * @return XmlFragment JAXBElement object.
	 */
	public static JAXBElement<MixedDataType> createXmlFragment(NodeList nodes) {
		final MixedDataType mixedDataType = Management.FACTORY
				.createMixedDataType();
		for (int j = 0; j < nodes.getLength(); j++) {
			mixedDataType.getContent().add(nodes.item(j));
		}
		// create the XmlFragmentElement
		JAXBElement<MixedDataType> fragment = Management.FACTORY
				.createXmlFragment(mixedDataType);
		return fragment;

	}
}
