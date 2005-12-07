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
 * $Id: CmdLineDemo.java,v 1.1 2005-12-07 05:30:14 akhilarora Exp $
 */

package demo;

import com.sun.ws.management.Management;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XmlBinding;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.LogManager;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPFault;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;
import transport.BasicAuthenticator;

public final class CmdLineDemo {
    
    private static final String GET = "get";
    private static final String ENUMERATE = "enumerate";
    
    private static String dest = null;
    private static String verb = GET;
    private static String resource = null;
    
    private static String enumContext = null;
    
    public static void main(java.lang.String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("USAGE: verb resource");
            System.err.println("  where verb is get or enumerate");
            return;
        }
        verb = args[0];
        resource = args[1];
        
        // echo cmdline
        System.out.print(verb + " ");
        System.out.print(resource + " ");
        System.out.println();
        
        InputStream is = CmdLineDemo.class.getResourceAsStream("/log.properties");
        if (is != null) {
            LogManager.getLogManager().readConfiguration(is);
        }
        
        dest = System.getProperty("wsman.dest", "http://localhost:8080/wsman");
        
        final String basicAuth = System.getProperty("wsman.basicauthentication");
        if ("true".equalsIgnoreCase(basicAuth)) {
            HttpClient.setAuthenticator(new BasicAuthenticator());
        }
        
        SOAP.setXmlBinding(new XmlBinding());
        
        sendRequest();
    }
    
    private static void sendRequest() throws Exception {
        String action = Transfer.GET_ACTION_URI;
        if (verb.equals(GET)) {
            action = Transfer.GET_ACTION_URI;
        } else if (verb.equals(ENUMERATE)) {
            if (enumContext == null) {
                action = Enumeration.ENUMERATE_ACTION_URI;
            } else {
                action = Enumeration.PULL_ACTION_URI;
            }
        } else {
            throw new IllegalArgumentException("Unsupported Action: " + verb);
        }
        
        Management mgmt = new Management();
        mgmt.setTo(dest);
        mgmt.setResourceURI(resource);
        mgmt.setAction(action);
        mgmt.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        mgmt.setMessageId("uuid:" + UUID.randomUUID().toString());
        
        if (verb.equals(ENUMERATE)) {
            Enumeration enu = new Enumeration(mgmt);
            if (enumContext == null) {
                enu.setEnumerate(null, null, null);
            } else {
                // pull two items at a time to make things interesting
                enu.setPull(enumContext, -1, 2, null);
            }
        }
        
        System.out.println("\n  ---- request ----  \n");
        mgmt.prettyPrint(System.out);
        
        Addressing addr = HttpClient.sendRequest(mgmt);
        
        System.out.println("\n  ---- response ----  \n");
        addr.prettyPrint(System.out);
        
        if (addr.getBody().hasFault()) {
            handleFault(addr.getBody().getFault());
            return;
        }
        
        action = addr.getAction();
        if (Transfer.GET_RESPONSE_URI.equals(action)) {
            handleGetResponse(addr);
        } else if (Enumeration.ENUMERATE_RESPONSE_URI.equals(action)) {
            handleEnumerateResponse(addr);
        } else if (Enumeration.PULL_RESPONSE_URI.equals(action)) {
            handlePullResponse(addr);
        } else {
            System.err.println("ERROR: Unrecognized action in response: " + action);
        }
    }
    
    private static void handleGetResponse(Addressing addr) throws Exception {
        Transfer xf = new Transfer(addr);
    }
    
    private static void handleEnumerateResponse(Addressing addr) throws Exception {
        Enumeration en = new Enumeration(addr);
        EnumerateResponse response = en.getEnumerateResponse();
        EnumerationContextType contextType = response.getEnumerationContext();
        List<Object> contextRoot = contextType.getContent();
        enumContext = contextRoot.get(0).toString();
        sendRequest();
    }
    
    private static void handlePullResponse(Addressing addr) throws Exception {
        Enumeration en = new Enumeration(addr);
        PullResponse response = en.getPullResponse();
        
        // update context
        EnumerationContextType contextType = response.getEnumerationContext();
        if (contextType != null) {
            List<Object> contextRoot = contextType.getContent();
            if (contextRoot != null) {
                String newContext = contextRoot.get(0).toString();
                if (newContext != null) {
                    enumContext = newContext;
                }
            }
        }
        
        ItemListType items = response.getItems();
        List<Object> elements = items.getAny();
        for (Object obj : elements) {
            if (obj instanceof Element) {
                Element el = (Element) obj;
                System.out.println("  " + el.getLocalName());
            }
        }
        
        // continue pulling if there's more
        if (response.getEndOfSequence() == null) {
            sendRequest();
        }
    }
    
    private static void handleFault(SOAPFault fault) throws Exception {
        Detail detail = fault.getDetail();
        Iterator di = detail.getDetailEntries();
        while (di.hasNext()) {
            DetailEntry de = null;
            Object obj = di.next();
            if (obj instanceof DetailEntry) {
                de = (DetailEntry) obj;
                System.err.println("fault detail: " + de.getTextContent());
            }
        }
    }
}
