/*
 * Copyright 2006 Sun Microsystems, Inc.
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
 * $Id: cim_numericsensor_Handler.java,v 1.8.2.1 2007-02-20 12:15:05 denis_rachal Exp $
 */

package com.sun.ws.management.server.handler.org.dmtf.schemas.wbem.wscim._1.cim_schema._2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeaderElement;

import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.ws.management.EncodingLimitFault;
import com.sun.ws.management.FragmentDialectNotSupportedFault;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.BaseSupport;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;

public class cim_numericsensor_Handler implements Handler {
        
    protected static String[] SELECTOR_KEYS = {
        "CreationClassName",
        "DeviceID",
        "SystemCreationClassName",
        "SystemName"
    };
    
    public void handle(final String action, final String resource,
        final HandlerContext hcontext,
        final Management request, final Management response) throws Exception {
        
        String noPrefix = "";
        final Set<SelectorType> selectors = request.getSelectors();
        if (selectors != null) {
            Iterator<SelectorType> si = selectors.iterator();
            while (si.hasNext()) {
                final SelectorType st = si.next();
                if ("NoPrefix".equals(st.getName()) &&
                    "true".equalsIgnoreCase(st.getContent().get(0).toString())) {
                    noPrefix = "_NoPrefix";
                    break;
                }
            }
        }
    	
        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            
            if (selectors.size() < SELECTOR_KEYS.length) {
                throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
            }
            
            final Set<SelectorType> reqSelectors = request.getSelectors();
            if (reqSelectors != null) {
                Iterator<SelectorType> si = reqSelectors.iterator();
                while (si.hasNext()) {
                    final SelectorType st = si.next();
                    if("LARGE_MESSAGE".equals(st.getContent().get(0).toString().trim())){
                    	noPrefix="_Large";
                    }
                    if("PUT_UPDATE".equals(st.getContent().get(0).toString().trim())){
                    	noPrefix="_Pull_init";
                    }
                }
            }
            
            Document resourceDoc = null;
            final String resourceDocName = "Pull" + noPrefix + "_0.xml";
            final InputStream is = load((ServletContext) hcontext.
                            getRequestProperties().
                            get(HandlerContext.SERVLET_CONTEXT), resourceDocName);
            if (is == null) {
                throw new InternalErrorFault("Failed to load " + resourceDocName + " from war");
            }
            try {
                resourceDoc = response.getDocumentBuilder().parse(is);
            } catch (Exception ex) {
                throw new InternalErrorFault("Error parsing " + resourceDocName + " from war");
            }
            response.getBody().addDocument(resourceDoc);
            
            /**Process for return envelope exceeding requested maximum. This logic should not be moved
             * further up in processing hierarchy as it NEEDS to be handled by the handler implementor
             * for operations that cause permanent changes to data store(DELETE/PUT/etc..). See spec.
             */  
              //Locate the maxEnvelopeSize header
            SOAPHeaderElement maxEnvelopeSizeHdr = null;
            for (final SOAPElement se : request.getChildren(request.getHeader(), Management.MAX_ENVELOPE_SIZE)) {
                if (se instanceof SOAPHeaderElement) {
                    final SOAPHeaderElement she = (SOAPHeaderElement) se;
                    maxEnvelopeSizeHdr = she;
                    if (she.getMustUnderstand()) {
                    	maxEnvelopeSizeHdr = she;
                    }
                }
            }
             //evaluate the size of the completed envelope to be returned
            byte[] byteBag =null;
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
              response.writeTo(baos);
            byteBag = baos.toByteArray();
            if((byteBag!=null)&&(maxEnvelopeSizeHdr!=null)){
            	String maxEvContent = maxEnvelopeSizeHdr.getTextContent();
            	if(maxEvContent!=null){
            	  long maxEnvelopeValue = Long.parseLong(maxEvContent.trim());
            	  String explanation="The WS-Management service could not process the ";
            	  explanation+="request because the response("+byteBag.length+") is larger than the ";
            	  explanation+="soap maximum envelope size of "+maxEnvelopeValue+" set for this request.";
            	  if(byteBag.length>maxEnvelopeValue){
            		 //Throw fault to that effect.
            	  EncodingLimitFault limit= new EncodingLimitFault(explanation,
                              EncodingLimitFault.Detail.MAX_ENVELOPE_SIZE_EXCEEDED);
            	  response.setFault(limit);
            	  }
            	}
            }            
            
        } else if (Transfer.PUT_ACTION_URI.equals(action)) {
            response.setAction(Transfer.PUT_RESPONSE_URI);
            final TransferExtensions txi = new TransferExtensions(request);
            final SOAPHeaderElement hdr = txi.getFragmentHeader();
            if (hdr != null) {
                // this is a fragment transfer, update the resource fragment
                final TransferExtensions txo = new TransferExtensions(response);
                final String expression = hdr.getTextContent();
                final String dialect = hdr.getAttributeValue(TransferExtensions.DIALECT);
                if (!BaseSupport.isSupportedDialect(dialect)) {
                    throw new FragmentDialectNotSupportedFault(BaseSupport.getSupportedDialects());
                }
                
                final Node xmlFragmentNode = (Node) txi.getBody().getChildElements().next();
                final NodeList childNodes = xmlFragmentNode.getChildNodes();
                final List<Node> nodeContent = new ArrayList<Node>();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    nodeContent.add(childNodes.item(i));
                }
                
                Document resourceDoc = null;
                final String resourceDocName = "Pull" + noPrefix + "_0.xml";
                final InputStream is = load((ServletContext) hcontext.
                            getRequestProperties().
                            get(HandlerContext.SERVLET_CONTEXT), resourceDocName);
                if (is == null) {
                    throw new InternalErrorFault("Failed to load " + resourceDocName + " from war");
                }
                try {
                    resourceDoc = response.getDocumentBuilder().parse(is);
                } catch (Exception ex) {
                    throw new InternalErrorFault("Error parsing " + resourceDocName + " from war");
                }
                
                // TODO - need XSD and JAXB artifacts pre-compiled for this to work
                /*
                final List<Node> content = XPath.filter(resourceDoc, expression, nsMap);
                txo.setFragmentPutResponse(hdr, nodeContent, expression, content);
                 */
                
                response.getHeader().addChildElement(hdr);
                
                final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
                final JAXBElement<MixedDataType> xmlFragment = Management.FACTORY.createXmlFragment(mixedDataType);
                Element lowerThresholdNonCriticalElement =
                    response.getBody().getOwnerDocument().createElementNS(resource,
                    "p:LowerThresholdNonCritical");
                lowerThresholdNonCriticalElement.setTextContent("100");
                mixedDataType.getContent().add(lowerThresholdNonCriticalElement);
                response.getXmlBinding().marshal(xmlFragment, response.getBody());
            } else {
                // this is regular transfer, update the entire resource
                final Document resourceDoc = request.getBody().extractContentAsDocument();
                // TODO: upate the resource
                response.getBody().addDocument(resourceDoc);
            }
        } else if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            synchronized (this) {
            	// Make sure there is an Iterator factory registered for this resource
            	if (EnumerationSupport.getIteratorFactory(resource) == null) {
            		EnumerationSupport.registerIteratorFactory(resource,
            				new cim_numericsensor_IteratorFactory(resource));
            	}
            }
            EnumerationSupport.enumerate(hcontext, ereq, eres);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.PULL_RESPONSE_URI);
            EnumerationSupport.pull(hcontext,ereq, eres);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.RELEASE_RESPONSE_URI);
            EnumerationSupport.release(hcontext,ereq, eres);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
    
    protected static final InputStream load(final ServletContext context, final String docName) {
        final String xml =
            cim_numericsensor_Handler.class.getPackage().getName().replace('.', '/') +
            "/" +
            cim_numericsensor_Handler.class.getSimpleName() + "_" + docName;
        return context.getResourceAsStream(xml);
    }
}