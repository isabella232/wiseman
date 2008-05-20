package com.sun.ws.management.message.api.constants;

import javax.xml.namespace.QName;


public class WSManEnumerationConstants {

	public static final String NS_PREFIX = "wsen";
	public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration";

	public static final String ENUMERATE_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate";
	public static final String ENUMERATE_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerateResponse";

	public static final String PULL_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull";
	public static final String PULL_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/PullResponse";

	public static final String RENEW_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Renew";
	public static final String RENEW_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/RenewResponse";

	public static final String GET_STATUS_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatus";
	public static final String GET_STATUS_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatusResponse";

	public static final String RELEASE_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Release";
	public static final String RELEASE_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/ReleaseResponse";

	public static final String ENUMERATION_END_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerationEnd";

	public static final String SOURCE_SHUTTING_DOWN_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/SourceShuttingDown";
	public static final String SOURCE_CANCELING_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/SourceCanceling";

	public static final String FAULT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/fault";

	public static final QName ENUMERATE = new QName(NS_URI, "Enumerate",
			NS_PREFIX);
	public static final QName ENUMERATE_RESPONSE = new QName(NS_URI,
			"EnumerateResponse", NS_PREFIX);
	public static final QName PULL = new QName(NS_URI, "Pull", NS_PREFIX);
	public static final QName PULL_RESPONSE = new QName(NS_URI, "PullResponse",
			NS_PREFIX);
	public static final QName RELEASE = new QName(NS_URI, "Release", NS_PREFIX);
	public static final QName ENUMERATION_END = new QName(NS_URI,
			"EnumerationEnd", NS_PREFIX);
	public static final QName SUPPORTED_DIALECT = new QName(NS_URI,
			"SupportedDialect", NS_PREFIX);
	public static final QName ENUMERATION_CONTEXT = new QName(NS_URI,
			"EnumerationContext", NS_PREFIX);
	public static final QName FILTER = new QName(NS_URI, "Filter", NS_PREFIX);

	public static final QName REQUEST_TOTAL_ITEMS_COUNT_ESTIMATE = new QName(
			WSManConstants.NS_URI, "RequestTotalItemsCountEstimate",
			WSManConstants.NS_PREFIX);

	public static final QName TOTAL_ITEMS_COUNT_ESTIMATE = new QName(
			WSManConstants.NS_URI, "TotalItemsCountEstimate",
			WSManConstants.NS_PREFIX);

	public static final QName OPTIMIZE_ENUMERATION = new QName(
			WSManConstants.NS_URI, "OptimizeEnumeration",
			WSManConstants.NS_PREFIX);

	public static final QName MAX_ELEMENTS = new QName(WSManConstants.NS_URI,
			"MaxElements", WSManConstants.NS_PREFIX);

	public static final QName ENUMERATION_MODE = new QName(
			WSManConstants.NS_URI, "EnumerationMode", WSManConstants.NS_PREFIX);

	public static final QName ITEMS = new QName(WSManConstants.NS_URI, "Items",
			WSManConstants.NS_PREFIX);

	public static final QName ITEM = new QName(WSManConstants.NS_URI, "Item",
			WSManConstants.NS_PREFIX);

	public static final QName END_OF_SEQUENCE = new QName(
			WSManConstants.NS_URI, "EndOfSequence", WSManConstants.NS_PREFIX);

	public static final QName WSMAN_FILTER = new QName(WSManConstants.NS_URI,
			"Filter", WSManConstants.NS_PREFIX);
}
