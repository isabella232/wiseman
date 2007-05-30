/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: Resource.java,v 1.7 2007-05-30 20:30:21 nbeers Exp $
 */
package com.sun.ws.management.client;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;

import com.sun.ws.management.client.exceptions.FaultException;



/**
 * An abstract representation of a WSManagement resource. Resources are
 * manufactured by the Resource Factory and contain an EPR which acts as a
 * target for all of their functions.
 * 
 * Still needs support for... Support for Max EnvelopeSize Support for
 * OptionsSet Support Fragments for partial sets of properties
 * 
 * @author wire
 * @author spinder
 * 
 */
public interface Resource extends EnumerableResource {

	/**
	 * XPath dialect.
	 * <code>http://www.w3.org/TR/1999/REC-xpath-19991116</code>
	 */
	static final String XPATH_DIALECT = "http://www.w3.org/TR/1999/REC-xpath-19991116";

	/**
	 * Constant used to specify ignore MaxCharacters parameter.
	 */
	static final int IGNORE_MAX_CHARS = 0;

	public ResourceState invoke(String action, Document document) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException;

	

}
