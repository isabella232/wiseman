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

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.xml.XPath;
import java.util.ArrayList;
import java.util.List;
import javax.xml.soap.SOAPHeaderElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A test handler for fragment level operations.
 * <p/>
 * Fragement requests should be made against this document
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;this&gt;
 *   &lt;is&gt;
 *     &lt;a&gt;
 *       &lt;foo&gt;
 *         &lt;bar&gt;
 *             this is a test of fragments
 *         &lt;/bar&gt;
 *       &lt;/foo&gt;
 *     &lt;/a&gt;
 *   &lt;/is&gt;
 * &lt;/this&gt;
 * </pre>
 */
public class fragment_Handler extends base_Handler {
    
    public static final String NS_PREFIX = "f";
    public static final String NS_URI = "https://wiseman.dev.java.net/1/fragment";
    
    public void handle(final String action, final String resource, final Management request, final Management response) throws Exception {
        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            
            final Document doc = response.newDocument();
            buildContentDocument(doc);
            
            final TransferExtensions transExtRequest = new TransferExtensions(request);
            final TransferExtensions transExtResponse = new TransferExtensions(response);
            
            final SOAPHeaderElement fragmentHeader = transExtRequest.getFragmentHeader();
            if (fragmentHeader == null) {
                // this is a regular transfer: not a fragment transfer, return the entire doc
                response.getBody().addDocument(doc);
            } else {
                final String expression = fragmentHeader.getTextContent();
                final String dialect = fragmentHeader.getAttributeValue(TransferExtensions.DIALECT);
                
                transExtResponse.setFragmentResponse(fragmentHeader,
                        XPath.filter(doc.getDocumentElement(), expression, dialect, null));
            }
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
    
    /**
     * Method to construct the document to be traversed with the fragment
     * request.
     * <p/>
     * In reality this document may originate from some backend, and is itself
     * the backend representation
     *
     * @param response
     */
    protected void buildContentDocument(final Document doc) {
        final Element thisElement = doc.createElement("this");
        final Element isElement = doc.createElement("is");
        final Element aElement = doc.createElement("a");
        final Element fooElement = doc.createElement("foo");
        final Element barElement = doc.createElement("bar");
        barElement.setTextContent("this is a test of fragments");
        thisElement.appendChild(isElement);
        isElement.appendChild(aElement);
        aElement.appendChild(fooElement);
        fooElement.appendChild(barElement);
        doc.appendChild(thisElement);
    }
}
