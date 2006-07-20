package com.sun.ws.management.client;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.ws.management.client.exceptions.NoMatchFoundException;
/**
 * Represents any response document. Provides access via XPath to an part of a
 * returned document. Can be used to access action reponses or pull responses. A
 * wrapper for any document.
 * 
 * @author wire
 *
 */
public interface ResourceState {

	/**
	 * Returns the underlying DOM model for the SOAP body of this resource.
	 * @return a DOM of the SOAP body.
	 */
	public Document getDocument();

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
	 * Returns the QNames of the topmost wrapper elements in the SOAP body.
	 * @return the QNames of the topmost elements in the SOAP body.
	 */
	public QName[] getFieldNames();

	/**
	 * A conveneince method. Assumes your document is simple having a wrapper element
	 * and a set of child state value elements. This method returns the QNames of the
	 * child elements of the first wrapper element in the body of the SOAP document.
	 * @return
	 */
	public QName[] getWrappedFieldNames();

	/**
	 * If your state is complex you may need to get the QNames present
	 * as the children of any element in the state document. It is assumed
	 * that you would use an XPath to locate a set of elements deep in the
	 * body of the state document. This function will return all the QNames
	 * which are the children of the conext node.
	 * @param context
	 * @return a list of QNames which are the children of context
	 */
	public QName[] getFieldNames(Node context);

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