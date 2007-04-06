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
 * $Id: WSManReflectiveServlet.java,v 1.1 2007-04-06 10:03:11 jfdenise Exp $
 */

package com.sun.ws.management.server.reflective;

import com.sun.ws.management.server.WSManAgent;
import com.sun.ws.management.server.servlet.WSManServlet;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import org.xml.sax.SAXException;

/**
 * Rewritten WSManServlet that delegates to a WSManAgent instance.
 *
 */
public class WSManReflectiveServlet extends WSManServlet {
    
    private static final Logger LOG = Logger.getLogger(WSManReflectiveServlet.class.getName());
    protected WSManAgent createWSManAgent(Source[] schemas) throws SAXException {
        // It is an extension of WSManAgent to handle Reflective Dispatcher
        return new WSManReflectiveAgent(null, schemas, null);
    }    
}
