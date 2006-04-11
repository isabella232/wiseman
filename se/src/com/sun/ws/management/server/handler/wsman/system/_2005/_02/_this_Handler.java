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
 * $Id: _this_Handler.java,v 1.2 2006-04-11 21:20:41 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.system._2005._02;

import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.transfer.Transfer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class _this_Handler implements Handler {
    
    private static final String VENDOR = "The Wiseman Project. https://wiseman.dev.java.net";
    private static final String VERSION = "0.3";
    
    private static final String THIS_NS_URI = Management.NS_URI + "/this";
    private static final String THIS_NS_PREFIX = "t";
    
    public void handle(final String action, final String resource,
            final Management request, final Management response) throws Exception {
        
        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            final Document thisDoc = response.newDocument();
            final Element thisElement = thisDoc.createElementNS(THIS_NS_URI,
                    THIS_NS_PREFIX + ":" + "This");
            thisDoc.appendChild(thisElement);
            final Element vendorElement = thisDoc.createElementNS(THIS_NS_URI,
                    THIS_NS_PREFIX + ":" + "Vendor");
            vendorElement.setTextContent(VENDOR);
            thisElement.appendChild(vendorElement);
            final Element versionElement = thisDoc.createElementNS(THIS_NS_URI,
                    THIS_NS_PREFIX + ":" + "Version");
            versionElement.setTextContent(VERSION);
            thisElement.appendChild(versionElement);
            
            response.getBody().addDocument(thisDoc);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}
