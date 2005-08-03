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
 * $Id: properties_Handler.java,v 1.1 2005-08-03 23:15:19 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.test.java.system;

import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class properties_Handler implements Handler, EnumerationIterator {
    
    private boolean cancelled;
    
    public void handle(final String action, final String resource,
            final Management request, final Management response) throws Exception {
        
        final Enumeration enuRequest = new Enumeration(request);
        final Enumeration enuResponse = new Enumeration(response);
        
        if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            EnumerationSupport.enumerate(enuRequest, enuResponse, this, 
                    System.getProperties());
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
    
    public List<Element> next(final Document doc, final Object context,
            final int start, final int count) {        
        cancelled = false;
        final Properties props = (Properties) context;
        final int returnCount = Math.min(count, props.size() - start);
        final List<Element> items = new ArrayList(returnCount);
        final Object[] keys = props.keySet().toArray();
        for (int i = 0; i < returnCount && !cancelled; i++) {
            final Object key = keys[start + i];
            final Object value = props.get(key);
            final Element item =
                    doc.createElementNS("http://java.sun.com/j2se", 
                    "system:" + key);
            item.setTextContent(value.toString());
            items.add(item);
        }
        return items;
    }
    
    public boolean hasNext(final Object context, final int start) {
        final Properties props = (Properties) context;
        return start < props.size();
    }

    public void cancel(final Object context) {
        cancelled = true;
    }
}
