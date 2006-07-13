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

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.AlreadyExistsFault;
import com.sun.ws.management.FragmentDialectNotSupportedFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.xml.XPath;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TransferExtensions extends Transfer {
    
    public static final QName FRAGMENT_TRANSFER =
            new QName(Management.NS_URI, "FragmentTransfer", Management.NS_PREFIX);
    
    public static final QName XML_FRAGMENT =
            new QName(Management.NS_URI, "XmlFragment", Management.NS_PREFIX);
    
    public static final QName DIALECT =
            new QName("Dialect");
    
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
     * Returns the FragmentTransfer SOAPElement if it exists AND contains mustUnderstand="true"
     *
     * @return FragmentTransfer SOAPElement if it exists AND contains mustUnderstand="true" else null
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
            if (!XPath.isSupportedDialect(dialect)) {
                throw new FragmentDialectNotSupportedFault(XPath.SUPPORTED_FILTER_DIALECTS);
            }
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
     * Inserts the FragmentTransferHeader into the Header
     *
     * @param fragmentTransferHeader
     * @throws SOAPException
     */
    private void setFragmentHeader(final SOAPElement fragmentTransferHeader) 
    throws SOAPException {
        // remove existing, if any
        removeChildren(getHeader(), FRAGMENT_TRANSFER);
        getHeader().addChildElement(fragmentTransferHeader);
    }
    
    /**
     * Handles the Fragment-level Get response by wrapping the content into
     * an XmlFragment element and adding the FragmentTransfer Header to the SOAP Headers.
     *
     * @param fragmentTransferHeader The Fragement Transfer Header to be returned in the headers
     * @param content                A Collection of Objects
     * @throws JAXBException
     */
    public void setFragmentGetResponse(final SOAPElement fragmentTransferHeader, 
            final List<Node> content) throws JAXBException, SOAPException {
        
        final JAXBElement<MixedDataType> xmlFragment = buildXmlFragment(content);
        //echo the fragment transfer header
        setFragmentHeader(fragmentTransferHeader);
        //add payload to the body
        getXmlBinding().marshal(xmlFragment, getBody());
    }
    
    /**
     * Builds the XmlFragment element with the supplied content
     *
     * @param content
     * @return JAXBElement<MixedDataType>
     */
    private JAXBElement<MixedDataType> buildXmlFragment(final List<Node> content) throws SOAPException {
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
            final List<Node> requestContent, final String expression, final List<Node> nodes) 
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
    
    public void setFragmentCreateResponse(final SOAPHeaderElement fragmentHeader, 
            final List<Node> requestContent, final String expression, final List<Node> nodes, 
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
}
