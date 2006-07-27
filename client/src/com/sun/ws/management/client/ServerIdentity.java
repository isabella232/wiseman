package com.sun.ws.management.client;

import javax.xml.xpath.XPathExpressionException;

import com.sun.ws.management.client.exceptions.NoMatchFoundException;

public interface ServerIdentity {

	public abstract String getBuildId() throws XPathExpressionException,
			NoMatchFoundException;

	public abstract String getProductVendor() throws XPathExpressionException,
			NoMatchFoundException;

	public abstract String getProductVersion() throws XPathExpressionException,
			NoMatchFoundException;

	public abstract String getProtocolVersion()
			throws XPathExpressionException, NoMatchFoundException;

	public abstract String getSpecVersion() throws XPathExpressionException,
			NoMatchFoundException;

}