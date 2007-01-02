/*
 * WSManRefectiveAgent.java
 *
 * Created on December 8, 2006, 4:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.ws.management.server;

import com.sun.ws.management.Management;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import org.xml.sax.SAXException;

/**
 * Agent that delegates to a Reflective Request Dispatcher
 */
public class WSManReflectiveAgent extends WSManAgent {
    public WSManReflectiveAgent() throws SAXException {
        this(null);
    }
    
    public WSManReflectiveAgent(Source[] schemas, String... customPackages) throws SAXException {
        super(schemas, customPackages);
    }
     
    protected RequestDispatcher createDispatcher(final Management request,
            final HandlerContext context) throws SOAPException, JAXBException, IOException {
        return new ReflectiveRequestDispatcher(request, context);
    }
}

