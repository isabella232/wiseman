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
 * $Id: cim_computersystem_Handler.java,v 1.3 2006-07-30 06:21:39 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.org.dmtf.wbem.wscim._1.cim_schema._2;

import com.sun.ws.management.FragmentDialectNotSupportedFault;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.xml.XPath;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.xml.soap.SOAPHeaderElement;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class cim_computersystem_Handler implements Handler {
    
    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {
        
        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            
            Document resourceDoc = null;
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
            final String resourceDocName = "Get" + noPrefix + ".xml";
            final InputStream is = load(context.getServletConfig().getServletContext(), resourceDocName);
            if (is == null) {
                throw new InternalErrorFault("Failed to load " + resourceDocName + " from war");
            }
            resourceDoc = response.getDocumentBuilder().parse(is);
            
            final TransferExtensions txi = new TransferExtensions(request);
            final SOAPHeaderElement hdr = txi.getFragmentHeader();
            if (hdr != null) {
                // this is a fragment transfer, return the resource doc fragment requested
                final TransferExtensions txo = new TransferExtensions(response);
                final String expression = hdr.getTextContent();
                final String dialect = hdr.getAttributeValue(TransferExtensions.DIALECT);
                if (!XPath.isSupportedDialect(dialect)) {
                    throw new FragmentDialectNotSupportedFault(XPath.SUPPORTED_FILTER_DIALECTS);
                }
                final NamespaceMap namespaces = new NamespaceMap(resourceDoc);
                final List<Node> content = XPath.filter(resourceDoc, expression, namespaces);
                txo.setFragmentGetResponse(hdr, content);
                return;
            }
            
            // this is a regular transfer, return the entire resource Document
            response.getBody().addDocument(resourceDoc);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
    
    private static final InputStream load(final ServletContext context, final String docName) {
        final String xml =
                cim_computersystem_Handler.class.getPackage().getName().replace('.', '/') +
                "/" +
                cim_computersystem_Handler.class.getSimpleName() + "_" + docName;
        return context.getResourceAsStream(xml);
    }
}
