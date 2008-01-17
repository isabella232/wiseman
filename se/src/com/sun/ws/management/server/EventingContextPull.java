package com.sun.ws.management.server;

import javax.xml.datatype.XMLGregorianCalendar;

import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;

class EventingContextPull extends EnumerationContext {

	EventingContextPull(XMLGregorianCalendar expiration, Filter filter,
			EnumerationModeType mode, EnumerationIterator iterator,
			ContextListener listener) {
		super(expiration, filter, mode, iterator, listener);
	}

}
