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
 * $Id: exec_Handler.java,v 1.2 2006-05-01 23:32:23 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Handler;
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
import javax.xml.parsers.DocumentBuilder;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    
    private static final String EXEC = "exec";
    
    private boolean cancelled = false;
    
    public void handle(final String action, final String resource,
            final Management request, final Management response) throws Exception {
        
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
        
        final Process process = Runtime.getRuntime().exec(cmd.toString());
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
            final Map<String, String> namespaces = new HashMap<String, String>();
            namespaces.put(NS_PREFIX, NS_URI);
            EnumerationSupport.enumerate(enuRequest, enuResponse, this, result.split("\n"), namespaces);
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
    
    public List<Element> next(final DocumentBuilder db, final Object context, final int startPos, final int count) {
        cancelled = false;
        final String[] lines = (String[]) context;
        final int returnCount = Math.min(count, lines.length - startPos);
        final List<Element> items = new ArrayList(returnCount);
        for (int i = 0; i < returnCount && !cancelled; i++) {
            final Document doc = db.newDocument();
            final Element item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + EXEC);
            item.setTextContent(lines[startPos + i]);
            items.add(item);
        }
        return items;
    }
    
    public boolean hasNext(final Object context, final int startPos) {
        final String[] lines = (String[]) context;
        return startPos < lines.length;
    }
    
    public void cancel(final Object context) {
        cancelled = true;
    }
}
