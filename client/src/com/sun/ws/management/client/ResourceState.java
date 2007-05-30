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
 * $Id: ResourceState.java,v 1.4 2007-05-30 20:30:21 nbeers Exp $
 */
package com.sun.ws.management.client;


import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import com.sun.ws.management.ResourceStateDocument;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;


/**
 * Represents any response document. Provides access via XPath to an part of a
 * returned document. Can be used to access action reponses or pull responses. A
 * wrapper for any document.
 * 
 * @author wire
 *
 */
public interface ResourceState extends ResourceStateDocument{
	/** 
	 * Returns a list of nodes that match the provided XPath criteria.
	 * 
	 * @param xPathExpression
	 * @return A list of matching nodes.
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundException 
	 */
	public NodeList getValues(String xPathExpression)
			throws XPathExpressionException, NoMatchFoundException;

	/**
	 * Returns the element text of the Element pointed to by the provided XPath.
	 * @param xPathExpression
	 * @return A string containg the element text.
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundException 
	 */
	public String getValueText(String xPathExpression)
			throws XPathExpressionException, NoMatchFoundException;

	public String getWrappedValueText(QName name)
		throws XPathExpressionException, NoMatchFoundException;

	public String getValueText(QName name,Node context) throws XPathExpressionException, NoMatchFoundException;
	

	/**
	 * Sets all the text elements of the selected nodes to the value provided.
	 * 
	 * <b>Warning:</b> Make sure your xpath results in a unique node because if you
	 * select more than one, they all will get set to value.
	 * @param xPathExpression
	 * @param value
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundException 
	 */
	public void setFieldValues(String xPathExpression, String value)
			throws XPathExpressionException, NoMatchFoundException;

	/**
	 * Sets the element text of the specified QName to null. Skips document node
	 * and first wrapper element as a conveniance.
	 * @param name
	 * @param value
	 * @throws NoMatchFoundException
	 */
	public void setWrappedFieldValue(QName name, String value)
			throws NoMatchFoundException;

	/**
	 * Sets the element text of the first element that matches QName relative
	 * to the provided dom node.
	 * @param name A QName of an element which is a direct decendant of the context node.
	 * @param value Text to assign to the text element of the selected element
	 * @param context This value cannot be null.
	 * @throws NoMatchFoundException
	 */
	public void setFieldValue(QName name, String value, Node context)
			throws NoMatchFoundException;

}