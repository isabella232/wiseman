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

package com.sun.ws.management.client.impl.jaxws;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.client.IWSManMessageFactory;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.xml.XmlBinding;

/**
 *
 * Default JAX-WS message factory.
 */
public class JAXWSMessageFactory implements IWSManMessageFactory {

    /**
     * Map entry specifying the SSL Socket factory to be used by JAX-WS stub.
     */
    public static final String JAXWS_SSL_SOCKET_FACTORY =
            "com.sun.xml.ws.transport.https.client.SSLSocketFactory";
    /**
     * Map entry to disable Fast-Infoset. Value is meaningless.
     * By default, Fast-Infoset is enabled.
     */
    public static final String JAXWS_NO_FAST_INFOSET =
            "com.sun.wiseman.jaxws.fastinfoset.disable";
    /**
     * Map entry specifying a
     * List&lt;{@link javax.xml.ws.handler.Handler Handler}&gt;
     * that will be passed to JAX-WS.
     */
    public static final String JAXWS_HANDLER_CHAIN =
            "com.sun.wiseman.jaxws.handlerchain";

    /** Creates a new instance of JAXWSMessageFactory */
    public JAXWSMessageFactory() {
    }

	public SOAPRequest newRequest(String endpoint, Map<String, ?> context,
			QName serviceName, QName portName, XmlBinding binding)
			throws IOException {
		// TODO Auto-generated method stub
		return new JAXWSSOAPRequest(endpoint, context, serviceName, portName, binding);
	}

	public SOAPRequest newRequest(EndpointReferenceType epr,
			Map<String, ?> context, XmlBinding binding) throws IOException {
		// TODO Auto-generated method stub
		return new JAXWSSOAPRequest(epr, context, binding);
	}

}
