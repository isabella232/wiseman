package com.sun.ws.management.client;



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

	static final String XPATH_DIALECT = "http://www.w3.org/TR/1999/REC-xpath-19991116";

	static final int IGNORE_MAX_CHARS = 0;



}
