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
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 *
 */
package com.sun.ws.management.client.impl.j2se;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.client.IWSManMessageFactory;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.xml.XmlBinding;

/**
 *
 * Default JAX-WS message factory.
 */
public class J2SEMessageFactory implements IWSManMessageFactory {

    /** Creates a new instance of JAXWSMessageFactory */
    public J2SEMessageFactory() {
    }

	public SOAPRequest newRequest(String endpoint, Map<String, ?> context,
			QName serviceName, QName portName, XmlBinding binding)
			throws IOException, SOAPException {
		return new J2SESOAPRequest(endpoint, context, serviceName, portName, binding);
	}

	public SOAPRequest newRequest(EndpointReferenceType epr,
			Map<String, ?> context, XmlBinding binding) throws IOException, SOAPException {
		return new J2SESOAPRequest(epr, context, binding);
	}

}
