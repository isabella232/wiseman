package com.sun.ws.management;

import java.util.Vector;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.xml.XmlBinding;

/**This class is meant to be a container for constants
 * that are used or applied to an Management message.
 * No complicated methods or logic should be present.
 * Many of the fields in this class are defined as final
 * static and public so that other message classes may 
 * refer to ManagementMessageValues as the root property
 * definition class.
 * 
 * @author Simeon Pinder
 */
public class ManagementMessageValues {
	
	//static defaults
	public static final long DEFAULT_TIMEOUT =30000;
	public static final String DEFAULT_UID_SCHEME ="uuid:";
	//These must be overridden for any messages.
	public static final String DEFAULT_RESOURCE_URI = "";
	public static final String DEFAULT_TO = "";
	public static final String DEFAULT_REPLY_TO = Addressing.ANONYMOUS_ENDPOINT_URI;
	public static final Vector<ReferenceParametersType> DEFAULT_ADDITIONAL_HEADERS = 
		new Vector<ReferenceParametersType>();
	
	/*These values are added so that each instance has
	 * properties that can be get/set for all the relevant
	 * fields.
	 */
	//Instance level values
	private long timeout = DEFAULT_TIMEOUT;
	private String uidScheme = DEFAULT_UID_SCHEME;
	private String resourceUri = DEFAULT_RESOURCE_URI;
	private String to = DEFAULT_TO;
	private String replyTo = DEFAULT_REPLY_TO;
	private XmlBinding xmlBinding = null;
	private Vector<ReferenceParametersType> additionalHeaders = 
		DEFAULT_ADDITIONAL_HEADERS;
	
	/** Instance has all the default values set. An empty
	 * instance of Management is lazily instantiated to 
	 * obtain a valid xmlbinding instance.
	 * 
	 * @throws SOAPException
	 */
	public ManagementMessageValues() throws SOAPException{
		if(xmlBinding==null){
		  xmlBinding = new Management().getXmlBinding();
		}
	}
	
	public static ManagementMessageValues newInstance() throws SOAPException{
		return new ManagementMessageValues();
	}
	
	//############ GETTERS/SETTERS for the instance variables.
	/**
	 * @return the resourceUri
	 */
	public String getResourceUri() {
		return resourceUri;
	}
	/**
	 * @param resourceUri the resourceUri to set
	 */
	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}
	/**
	 * @return the timeout
	 */
	public long getTimeout() {
		return timeout;
	}
	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}
	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}
	/**
	 * @return the uidScheme
	 */
	public String getUidScheme() {
		return uidScheme;
	}
	/**
	 * @param uidScheme the uidScheme to set
	 */
	public void setUidScheme(String uidScheme) {
		this.uidScheme = uidScheme;
	}

	/**
	 * @return the xmlBinding
	 */
	public XmlBinding getXmlBinding() {
		return xmlBinding;
	}

	/**
	 * @param xmlBinding the xmlBinding to set
	 */
	public void setXmlBinding(XmlBinding binding) {
		this.xmlBinding = binding;
	}

	public boolean addCustomHeader(QName customHeader, String nodeValue) {
		boolean success = false;
		if((customHeader==null)){
			return success;
		}
		ReferenceParametersType customNode = 
			Addressing.createReferenceParametersType(
					customHeader,
					nodeValue);
		if(customNode!=null){
			additionalHeaders.add(customNode);
			success=true;
		}
		return success;
	}

	/**
	 * @return the additionalHeaders
	 */
	public Vector<ReferenceParametersType> getAdditionalHeaders() {
		return additionalHeaders;
	}

	/**
	 * @param additionalHeaders the additionalHeaders to set
	 */
	public void setAdditionalHeaders(
			Vector<ReferenceParametersType> additionalHeaders) {
		this.additionalHeaders = additionalHeaders;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}
	
}
