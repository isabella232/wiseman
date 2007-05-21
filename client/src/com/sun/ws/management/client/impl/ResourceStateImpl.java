package com.sun.ws.management.client.impl;


import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.ws.management.NoMatchFoundExceptionServer;
import com.sun.ws.management.ResourceStateServerImpl;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;

public class ResourceStateImpl extends ResourceStateServerImpl implements ResourceState {

	public ResourceStateImpl(Document stateDocument) {
		super(stateDocument);
		
	}

	/**
	 * Sets all the text elements of the selected nodes to the value provided.
	 * 
	 * <b>Warning:</b> Make sure your xpath results in a unique node because if you
	 * select more than one, they all will get set to value.
	 * @param xPathExpression
	 * @param value
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundExceptionServer 
	 */
	public void setFieldValues(String xPathExpression,String value) throws XPathExpressionException, NoMatchFoundException{
		try {
			super.setFieldValues(xPathExpression, value);
		} catch (NoMatchFoundExceptionServer e) {
			throw new NoMatchFoundException(e);
		}
	}
	
	/** 
	 * Returns a list of nodes that match the provided XPath criteria.
	 * 
	 * @param xPathExpression
	 * @return A list of matching nodes.
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundExceptionServer 
	 */
	public NodeList getValues(String xPathExpression) throws XPathExpressionException, NoMatchFoundException{
		try {
			return super.getValues(xPathExpression);
		} catch (NoMatchFoundExceptionServer e) {
			throw new NoMatchFoundException(e);
		}
	}
	
	/**
	 * Returns the element text of the Element pointed to by the provided XPath.
	 * @param xPathExpression
	 * @return A string containg the element text.
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundExceptionServer 
	 */
	public String getValueText(String xPathExpression) throws XPathExpressionException, NoMatchFoundException{
		try {
			return super.getValueText(xPathExpression);
		} catch (NoMatchFoundExceptionServer e) {
			throw new NoMatchFoundException(e);
		}
	}

	/**
	 * Sets the element text of the specified QName to null. Skips document node
	 * and first wrapper element as a conveniance.
	 * @param name
	 * @param value
	 * @throws NoMatchFoundExceptionServer
	 */
	public void setWrappedFieldValue(QName name,String value) throws NoMatchFoundException{
		try {
			super.setWrappedFieldValue(name, value);
		} catch (NoMatchFoundExceptionServer e) {
			throw new NoMatchFoundException(e);
		}
	}

	/**
	 * Sets the element text of the first element that matches QName relative
	 * to the provided dom node.
	 * @param name A QName of an element which is a direct decendant of the context node.
	 * @param value Text to assign to the text element of the selected element
	 * @param context This value cannot be null.
	 * @throws NoMatchFoundExceptionServer
	 */
	public void setFieldValue(QName name,String value,Node context) throws NoMatchFoundException{
		try {
			super.setFieldValue(name, value, context);
		} catch (NoMatchFoundExceptionServer e) {
			throw new NoMatchFoundException(e);
		}
	}

	public String getWrappedValueText(QName name) throws XPathExpressionException, NoMatchFoundException {
		try {
			return super.getWrappedValueText(name);
		} catch (NoMatchFoundExceptionServer e) {
			throw new NoMatchFoundException(e);
		}
	}
	public String getValueText(QName name,Node context) throws XPathExpressionException, NoMatchFoundException {
		try {
			return super.getValueText(name, context);
		} catch (NoMatchFoundExceptionServer e) {
			throw new NoMatchFoundException(e);
		}

	}


}
