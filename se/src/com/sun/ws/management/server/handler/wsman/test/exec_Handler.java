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
 * $Id: exec_Handler.java,v 1.10.2.1 2006-12-21 08:25:43 jfdenise Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.transfer.Transfer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

/**
 * This handler allows an arbitrary command to be executed and its output
 * returned. The command to be executed is specified as the value of a
 * specially named Selector called <code>exec<code>. The value of this
 * Selector is executed synchronously. If the command returns normally its
 * output stream (stdout) is returned, and if it returns an error then its
 * error stream (stderr) is returned.
 *
 * Both WS-Transfer Get and WS-Enumeration are supported. In case of an
 * enumeration, the output is split into lines and each line is returned
 * as a separate item.
 *
 * NOTE: This handler is meant to be used only for demo and debug purposes
 * and should <b>not</b> be included in a product.
 *
 * NOTE: This handler will only work in app server for which the security
 * manager is disabled or if the execute File permission is added to the
 * appropriate security policy file.
 *
 * For the Sun App Server 8.2, this file is
 * <code>{app-server-home}/domains/domain1/config/server.policy</code> (for domain1).
 * Add the "execute" permission so that
 * <pre>permission java.io.FilePermission "<<ALL FILES>>", "read,write";</pre>
 * becomes
 * <pre>permission java.io.FilePermission "<<ALL FILES>>", "read,write,execute";</pre>
 */
public class exec_Handler implements Handler, EnumerationIterator {
    
    public static final String NS_PREFIX = "ex";
    public static final String NS_URI = "https://wiseman.dev.java.net/1/exec";

    private static NamespaceMap nsMap = null;
    
    private static final String EXEC = "exec";

    /**
     * A context class to pass with enumeration requests
     */
    private static final class Context {
        /**
         * Indication of whether the request was cancelled during processing
         */
        boolean cancelled = false;

        /**
         * Server request path that can be used for creating an EPR
         */
        String requestPath;

        /**
         * URI that identifies the resource being manipulated
         */
        String resourceURI;
        
        /**
         * Results of exec operation for use across calls
         */
        String[] results;
    }

    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {
        
        if (nsMap == null) {
            final Map<String, String> map = new HashMap<String, String>();
            map.put(NS_PREFIX, NS_URI);
            nsMap = new NamespaceMap(map);
        }
        
        final Set<SelectorType> selectors = request.getSelectors();
        final Iterator<SelectorType> si = selectors.iterator();
        String cmd = null;
        while (si.hasNext()) {
            final SelectorType selector = si.next();
            if (EXEC.equals(selector.getName())) {
                final Serializable ser = selector.getContent().get(0);
                if (ser instanceof String) {
                    cmd = (String) ser;
                    break;
                }
            }
        }
        if (cmd == null) {
            throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
        }
        
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];
        
        final Process process = Runtime.getRuntime().exec(cmd);
        int status = process.waitFor();
        final InputStream is = status == 0 ? process.getInputStream() : process.getErrorStream();
        int nread = 0;
        while ((nread = is.read(buffer)) > 0) {
            bos.write(buffer, 0, nread);
        }
        final String result = bos.toString().trim();
        
        final Enumeration enuRequest = new Enumeration(request);
        final Enumeration enuResponse = new Enumeration(response);
        
        if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);

            final Context ctx = new Context();
            ctx.resourceURI = resource;

            // retrieve the request path for use in EPR construction and store
            //  it in the context for later retrieval
            final String path = context.getURL();
            ctx.requestPath = path;

            ctx.results = result.split ("\n");

            EnumerationSupport.enumerate(enuRequest, enuResponse, this, ctx, nsMap);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.PULL_RESPONSE_URI);
            EnumerationSupport.pull(enuRequest, enuResponse);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.RELEASE_RESPONSE_URI);
            EnumerationSupport.release(enuRequest, enuResponse);
        } else if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            final Document doc = response.newDocument();
            final Element element = doc.createElementNS(NS_URI, NS_PREFIX + ":" + EXEC);
            element.setTextContent(result);
            doc.appendChild(element);
            response.getBody().addDocument(doc);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
    
    public List<EnumerationItem> next(final DocumentBuilder db, final Object context,
            final boolean includeItem, final boolean includeEPR,
            final int startPos, final int count) {
        final Context ctx = (Context)context;
        final int returnCount = Math.min(count, ctx.results.length - startPos);
        final List<EnumerationItem> items = new ArrayList(returnCount);
        for (int i = 0; i < returnCount && !ctx.cancelled; i++) {
            // create an enumeration element only if necessary
            Element item = null;
            if (includeItem) {
                final Document doc = db.newDocument();
                item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + EXEC);
                item.setTextContent(ctx.results[startPos + i]);
            }

            // construct an endpoint reference to accompany the element, if needed
            EndpointReferenceType epr = null;
            if (includeEPR) {
                epr = EnumerationSupport.createEndpointReference(ctx.requestPath, ctx.resourceURI);
            }
            final EnumerationItem ei = new EnumerationItem(item, epr);

            items.add(ei);
        }
        return items;
    }
    
    public boolean hasNext(final Object context, final int startPos) {
        final Context ctx = (Context)context;
        return startPos < ctx.results.length;
    }
    
    public void cancel(final Object context) {
        final Context ctx = (Context)context;
        ctx.cancelled = true;
    }
    
    public int estimateTotalItems(final Object context) {
        // choose not to provide an estimate
        return -1;
    }
    
    public NamespaceMap getNamespaces() {
        return nsMap;
    }
}
