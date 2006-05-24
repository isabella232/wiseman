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

import com.sun.ws.management.FragmentDialectNotSupportedFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.xml.XPath;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.w3c.dom.Node;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;

/**
 * @author Sal Campana
 */
public class TransferExtensions extends Transfer {
    
    public static final QName FRAGMENT_TRANSFER =
            new QName(Management.NS_URI, "FragmentTransfer", Management.NS_PREFIX);
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
    
    public void setFragmentGet(final String query, final String dialect) throws SOAPException, JAXBException {
        
        final DialectableMixedDataType dialectableMixedDataType =
                Management.FACTORY.createDialectableMixedDataType();
        if (dialect != null) {
            if (!XPath.isSupportedDialect(dialect)) {
                throw new FragmentDialectNotSupportedFault(XPath.SUPPORTED_FILTER_DIALECTS);
            }
            dialectableMixedDataType.setDialect(dialect);
        }
        dialectableMixedDataType.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, "true");
        
        //add the query string to the content of the FragmentTransfer Header
        dialectableMixedDataType.getContent().add(query);
        
        final JAXBElement<DialectableMixedDataType> fragmentTransfer =
                Management.FACTORY.createFragmentTransfer(dialectableMixedDataType);
        
        //set the SOAP Header for Fragment Transfer
        getXmlBinding().marshal(fragmentTransfer, getHeader());
    }
    
    /**
     * Sets the response for the Fragment-level request by wrapping the content into
     * an XmlFragment element and adding the FragmentTransfer Header to the SOAP Headers.
     *
     * @param fragmentTransferHeader The Fragement Transfer Header to be returned in the headers
     * @param content                A Collection of Objects
     * @throws JAXBException
     */
    public void setFragmentResponse(final SOAPElement fragmentTransferHeader, final List<Node> content) throws JAXBException, SOAPException {
        //build the JAXB Wrapper Element
        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
        final List<Object> xmlFragmentContent = mixedDataType.getContent();
        
        xmlFragmentContent.addAll(content);
        
        //create the XmlFragmentElement
        final JAXBElement<MixedDataType> xmlFragment = Management.FACTORY.createXmlFragment(mixedDataType);
        
        //if there was no content, then this is NIL
        if (xmlFragmentContent.size() <= 0) {
            xmlFragment.setNil(true);
        }
        
        //echo the fragment transfer header
        getHeader().addChildElement(fragmentTransferHeader);
        //add payload to the body
        getXmlBinding().marshal(xmlFragment, getBody());
    }
}
