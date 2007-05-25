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
