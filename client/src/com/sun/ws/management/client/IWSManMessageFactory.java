/*
 * Copyright 2005-2008 Sun Microsystems, Inc.
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
 ** Copyright (C) 2006, 2007, 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 *
 */

package com.sun.ws.management.client;

import java.io.IOException;
import java.util.Map;

import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.xml.XmlBinding;

/**
 *
 * A IWSManMessageFactory for creating SOAPMessage instances.
 */
public interface IWSManMessageFactory {
    
    /**
     * Create a new request.
     * 
     * @param epr
     * @param context
     * @param binding
     * @return an object that implements the SOAPResponse interface
     * @throws IOException
     * @throws SOAPException 
     */
    SOAPRequest newRequest(final EndpointReferenceType epr,
	                       final Map<String, ?> context,
	                       final XmlBinding binding) throws Exception;
      
}
