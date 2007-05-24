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