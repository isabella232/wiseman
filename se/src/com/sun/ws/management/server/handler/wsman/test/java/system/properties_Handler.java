/*
 * Copyright 2005 Sun Microsystems, Inc.
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
 * $Id: properties_Handler.java,v 1.6 2006-06-07 17:56:52 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.test.java.system;

import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class properties_Handler implements Handler, EnumerationIterator {
    
    private static final String NS_URI = "https://wiseman.dev.java.net/java";
    private static final String NS_PREFIX = "java";
    
    private static final class Context {
        boolean cancelled;
        Properties properties;
    }
    
    public void handle(final String action, final String resource,
            final Management request, final Management response) throws Exception {
        
        final Enumeration enuRequest = new Enumeration(request);
        final Enumeration enuResponse = new Enumeration(response);
        
        if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            
            final Map<String, String> namespaces = new HashMap<String, String>();
            namespaces.put(NS_PREFIX, NS_URI);
            
            final Context context = new Context();
            // this generates an AccessDenied exception which is returned
            // to the client as an AccessDenied fault if the server is
            // running in the sun app server with a security manager in
            // place (the default), which disallows enumeration of
            // system properties
            context.properties = System.getProperties();
            context.cancelled = false;
            
            EnumerationSupport.enumerate(enuRequest, enuResponse, this,
                    context, namespaces);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.PULL_RESPONSE_URI);
            EnumerationSupport.pull(enuRequest, enuResponse);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.RELEASE_RESPONSE_URI);
            EnumerationSupport.release(enuRequest, enuResponse);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
    
    public List<Element> next(final DocumentBuilder db, final Object context,
            final int start, final int count) {
        final Context ctx = (Context) context;
        final Properties props = ctx.properties;
        final int returnCount = Math.min(count, props.size() - start);
        final List<Element> items = new ArrayList(returnCount);
        final Object[] keys = props.keySet().toArray();
        for (int i = 0; i < returnCount && !ctx.cancelled; i++) {
            final Object key = keys[start + i];
            final Object value = props.get(key);
            final Document doc = db.newDocument();
            final Element item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + key);
            item.setTextContent(value.toString());
            items.add(item);
        }
        return items;
    }
    
    public boolean hasNext(final Object context, final int start) {
        final Context ctx = (Context) context;
        final Properties props = ctx.properties;
        return start < props.size();
    }
    
    public void cancel(final Object context) {
        final Context ctx = (Context) context;
        ctx.cancelled = true;
    }

    public int estimateTotalItems(final Object context) {
        final Context ctx = (Context) context;
        final Properties props = ctx.properties;
        return props.size();
    }
}
