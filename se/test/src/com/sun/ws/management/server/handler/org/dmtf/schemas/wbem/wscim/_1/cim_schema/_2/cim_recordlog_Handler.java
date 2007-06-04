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
 * $Id: cim_recordlog_Handler.java,v 1.1 2007-06-04 06:25:10 denis_rachal Exp $
 */

package com.sun.ws.management.server.handler.org.dmtf.schemas.wbem.wscim._1.cim_schema._2;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class cim_recordlog_Handler implements Handler {
    
    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {
        
        final String CLEAR_RECORD_LOG_ACTION = resource + "/ClearLog";
        final String CLEAR_RECORD_LOG_RESPONSE = resource + "/ClearLogResponse";
        
        if (CLEAR_RECORD_LOG_ACTION.equals(action)) {
            response.setAction(CLEAR_RECORD_LOG_RESPONSE);
            
            final Document doc = response.newDocument();
            final Element output = doc.createElementNS(request.getResourceURI(), "p:ClearLog_OUTPUT");
            final Element retval = doc.createElementNS(request.getResourceURI(), "p:ReturnValue");
            output.appendChild(retval);
            retval.setTextContent("0");
            doc.appendChild(output);
            response.getBody().addDocument(doc);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}
