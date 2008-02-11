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

package com.sun.ws.management.client.message;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.sun.ws.management.xml.XmlBinding;

public interface SOAPRequest {
	
	void addNamespaceDeclaration(final String prefix, final String uri);
	
	void addNamespaceDeclarations(Map<String, String> declarations);

	void addHeader(final Object header, final JAXBContext ctx) throws JAXBException;

	void setPayload(final Object content, final JAXBContext ctx);
	
	Map<String, ?> getRequestContext();
	
	XmlBinding getXmlBinding();
	
	SOAPResponse invoke() throws Exception;
	
	void invokeOneWay() throws Exception;
}
