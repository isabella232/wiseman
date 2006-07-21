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
 * $Id: cim_numericsensor_Handler.java,v 1.4 2006-07-21 20:26:16 pmonday Exp $
 */

package com.sun.ws.management.server.handler.org.dmtf.wbem.wscim._1.cim_schema._2;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationElement;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.transfer.Transfer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class cim_numericsensor_Handler implements Handler, EnumerationIterator {
    
    private NamespaceMap nsMap = null;
    
    private static final class Context {
        HandlerContext hcontext = null;
        boolean cancelled = false;
        int index = 0;
        int count = 2;
    }
    
    public void handle(final String action, final String resource,
            final HandlerContext hcontext,
            final Management request, final Management response) throws Exception {
        
        if (nsMap == null) {
            final Map<String, String> map = new HashMap<String, String>();
            map.put("p", "http://www.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_NumericSensor");
            nsMap = new NamespaceMap(map);
        }
        
        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            
            final Set<SelectorType> selectors = request.getSelectors();
            if (selectors.size() < 4) {
                throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
            }
        } else if (Transfer.PUT_ACTION_URI.equals(action)) {
            response.setAction(Transfer.PUT_RESPONSE_URI);
            final Document resourceDoc = request.getBody().extractContentAsDocument();
            // TODO: upate the resource
            response.getBody().addDocument(resourceDoc);
        } else if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            final Context context = new Context();
            context.hcontext = hcontext;
            EnumerationSupport.enumerate(ereq, eres, this, context);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.PULL_RESPONSE_URI);
            EnumerationSupport.pull(ereq, eres);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            final Enumeration ereq = new Enumeration(request);
            final Enumeration eres = new Enumeration(response);
            eres.setAction(Enumeration.RELEASE_RESPONSE_URI);
            EnumerationSupport.release(ereq, eres);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
    
    public List<EnumerationElement> next(final DocumentBuilder db, final Object context,
            final int start, final int count) {
        final Context ctx = (Context) context;
        final int returnCount = Math.min(count, ctx.count - start);
        final List<EnumerationElement> items = new ArrayList(returnCount);
        for (int i = 0; i < returnCount && !ctx.cancelled; i++) {
            Document resourceDoc = null;
            final String resourceDocName = "Pull" + "_" + start + ".xml";
            final InputStream is = load(ctx.hcontext.getServletConfig().getServletContext(), resourceDocName);
            if (is == null) {
                throw new InternalErrorFault("Failed to load " + resourceDocName + " from war");
            }
            try {
                resourceDoc = db.parse(is);
            } catch (Exception ex) {
                throw new InternalErrorFault("Error parsing " + resourceDocName + " from war");
            }
            
            // create an enumeration element 
            EnumerationElement ee = new EnumerationElement();
            // add the primary item
            ee.setElement(resourceDoc.getDocumentElement());
            // todo: add the EPR
            
            items.add(ee);
        }
        return items;
    }
    
    public boolean hasNext(final Object context, final int start) {
        final Context ctx = (Context) context;
        return start < ctx.count;
    }
    
    public void cancel(final Object context) {
        final Context ctx = (Context) context;
        ctx.cancelled = true;
    }
    
    public int estimateTotalItems(final Object context) {
        final Context ctx = (Context) context;
        return ctx.count;
    }
    
    public NamespaceMap getNamespaces() {
        return nsMap;
    }
    
    private static final InputStream load(final ServletContext context, final String docName) {
        final String xml =
                cim_numericsensor_Handler.class.getPackage().getName().replace('.', '/') +
                "/" +
                cim_numericsensor_Handler.class.getSimpleName() + "_" + docName;
        return context.getResourceAsStream(xml);
    }
}
