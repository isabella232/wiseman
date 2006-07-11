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
 * $Id: concrete_Handler.java,v 1.3 2006-07-11 21:30:31 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.transfer.Transfer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class concrete_Handler extends base_Handler {
    
    public static final String NS_PREFIX = "c";
    public static final String NS_URI = "https://wiseman.dev.java.net/1/concrete";
    
    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {

        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            final Document doc = response.newDocument();
            final Element element = doc.createElementNS(NS_URI, NS_PREFIX + ":class");
            element.setTextContent(getClass().getName());
            doc.appendChild(element);
            response.getBody().addDocument(doc);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}
