package com.sun.ws.management.message.api.constants;

import javax.xml.namespace.QName;

public class WSAddressingConstants {

	public static final String NS_PREFIX = "wsa";
	public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

	public static final String DEFAULT_UID_SCHEME = "urn:uuid:";
	public static final String UNSPECIFIED_MESSAGE_ID = "http://schemas.xmlsoap.org/ws/2004/08/addressing/id/unspecified";
	public static final String ANONYMOUS_ENDPOINT_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";
	public static final String FAULT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";

	public static final QName ACTION = new QName(NS_URI, "Action", NS_PREFIX);
	public static final QName TO = new QName(NS_URI, "To", NS_PREFIX);
	public static final QName MESSAGE_ID = new QName(NS_URI, "MessageID",
			NS_PREFIX);
	public static final QName REPLY_TO = new QName(NS_URI, "ReplyTo", NS_PREFIX);
	public static final QName FAULT_TO = new QName(NS_URI, "FaultTo", NS_PREFIX);
	public static final QName FROM = new QName(NS_URI, "From", NS_PREFIX);
	public static final QName ADDRESS = new QName(NS_URI, "Address", NS_PREFIX);
	public static final QName RELATES_TO = new QName(NS_URI, "RelatesTo",
			NS_PREFIX);
	public static final QName RETRY_AFTER = new QName(NS_URI, "RetryAfter",
			NS_PREFIX);
	public static final QName ENDPOINT_REFERENCE = new QName(NS_URI,
			"EndpointReference", NS_PREFIX);
}
