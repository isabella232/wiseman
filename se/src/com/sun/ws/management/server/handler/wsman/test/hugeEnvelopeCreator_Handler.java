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
 * $Id: hugeEnvelopeCreator_Handler.java,v 1.1 2005-11-08 22:40:20 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.transfer.Transfer;
import java.util.Calendar;
import java.util.UUID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class hugeEnvelopeCreator_Handler implements Handler {
    
    public void handle(final String action, final String resource,
      final Management request, final Management response) throws Exception {
        
        if (Transfer.GET_ACTION_URI.equals(action)) {
            response.setAction(Transfer.GET_RESPONSE_URI);
            final Calendar now = Calendar.getInstance();

            final Document doc = response.newDocument();
            final String ns = "https://wiseman.dev.java.net/ws/";
            final Element root = doc.createElementNS(ns +
              now.get(Calendar.YEAR) + "/" + now.get(Calendar.MONTH) + "/wsman",
              "wiseman:SomeFakeRootElement");
            doc.appendChild(root);

            for (int i=0; i < 100; i++) {
                final Element uuid = doc.createElementNS("http://sun.com/ws/" +
                  now.get(Calendar.YEAR) + "/" + now.get(Calendar.MONTH) + "/wsman",
                  "wiseman:SomeFakeElement");
                uuid.setTextContent(UUID.randomUUID().toString());
                root.appendChild(uuid);
            }
            response.getBody().addDocument(doc);
            response.writeTo(System.out);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}
