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

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.client.impl.j2se.J2SEMessageFactory;
import com.sun.ws.management.client.impl.jaxws.JAXWSMessageFactory;
import com.sun.ws.management.client.message.SOAPRequest;
import com.sun.ws.management.xml.XmlBinding;

/**
 *
 * A WSManMessageFactory for creating SOAPMessage instances.
 */
public abstract class WSManMessageFactory implements IWSManMessageFactory {
	
	private static String defaultFactory = JAXWSMessageFactory.class.getCanonicalName();
	
	protected WSManMessageFactory() {		
	}
	
	static public void setDefaultFactory(final String classname) {
		// TODO: Check if it can be loaded.
		defaultFactory = classname;
	}
    
	static public IWSManMessageFactory newInstance() {
		// TODO: Use reflection to create the default factory.
		if (defaultFactory.equals(JAXWSMessageFactory.class.getCanonicalName()))
			return new JAXWSMessageFactory();
		if (defaultFactory.equals(J2SEMessageFactory.class.getCanonicalName()))
			return new J2SEMessageFactory();
		return null;
	}
	
    /**
     * Create a new request. 
     * @return a new SOAPRequest object
     * @throws IOException 
     */
    public abstract SOAPRequest newRequest(final String endpoint,
	                                     final Map<String, ?> context,
	                                     final QName serviceName,
	                                     final QName portName,
	                                     final XmlBinding binding) throws IOException;
    
    public abstract SOAPRequest newRequest(final EndpointReferenceType epr,
	                                     final Map<String, ?> context,
	                                     final XmlBinding binding) throws IOException;
    
}
