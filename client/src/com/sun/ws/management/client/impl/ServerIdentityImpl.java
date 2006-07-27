package com.sun.ws.management.client.impl;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.sun.ws.management.client.ServerIdentity;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;
import com.sun.ws.management.identify.Identify;

/**
 * A Wrapper for resource state that knows the xpaths for an Identify Response.
 * @author wire
 *
 */
public class ServerIdentityImpl extends ResourceStateImpl implements ServerIdentity {

	public ServerIdentityImpl(Document stateDocument) {
		super(stateDocument);
	}
	
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.ServerIdentity#getBuildId()
	 */
	public String getBuildId() throws XPathExpressionException, NoMatchFoundException {
		return getWrappedValueText(Identify.BUILD_ID);
	}
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.ServerIdentity#getProductVendor()
	 */
	public String getProductVendor() throws XPathExpressionException, NoMatchFoundException {
		return getWrappedValueText(Identify.PRODUCT_VENDOR);
	}
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.ServerIdentity#getProductVersion()
	 */
	public String getProductVersion() throws XPathExpressionException, NoMatchFoundException {
		return getWrappedValueText(Identify.PRODUCT_VERSION);
	}
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.ServerIdentity#getProtocolVersion()
	 */
	public String getProtocolVersion() throws XPathExpressionException, NoMatchFoundException {
		return getWrappedValueText(Identify.PROTOCOL_VERSION);
	}
	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.ServerIdentity#getSpecVersion()
	 */
	public String getSpecVersion() throws XPathExpressionException, NoMatchFoundException {
		return getWrappedValueText(Identify.SPEC_VERSION);
	}

}
