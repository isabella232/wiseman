package com.sun.ws.management.message.api.constants;

import javax.xml.namespace.QName;

public class WSManConstants {

	public static final String NS_PREFIX = "wsman";
	public static final String NS_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";

    public static final String EVENTS_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/Events";
    public static final String HEARTBEAT_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/Heartbeat";
    public static final String DROPPED_EVENTS_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/DroppedEvents";
    public static final String ACK_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/Ack";
    public static final String EVENT_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/Event";
    public static final String BOOKMARK_EARLIEST_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/bookmark/earliest";
    public static final String PUSH_WITH_ACK_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/PushWithAck";
    public static final String PULL_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/Pull";
    public static final String FAULT_ACTION_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman/fault";

    public static final QName RESOURCE_URI = new QName(NS_URI, "ResourceURI", NS_PREFIX);
    public static final QName OPERATION_TIMEOUT = new QName(NS_URI, "OperationTimeout", NS_PREFIX);
    public static final QName SELECTOR_SET = new QName(NS_URI, "SelectorSet", NS_PREFIX);
    public static final QName OPTION_SET = new QName(NS_URI, "OptionSet", NS_PREFIX);
    public static final QName MAX_ENVELOPE_SIZE = new QName(NS_URI, "MaxEnvelopeSize", NS_PREFIX);
    public static final QName LOCALE = new QName(NS_URI, "Locale", NS_PREFIX);
}
