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
 * $Id: WSManReflectiveAgent.java,v 1.3 2007-03-20 20:35:38 simeonpinder Exp $
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
     
	@Override
	protected RequestDispatcher createDispatcher(Management request, HandlerContext context, 
			WSManAgent agent) throws SOAPException, JAXBException, IOException {
		return new ReflectiveRequestDispatcher(request, context,agent);
	}
}

