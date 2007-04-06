package com.sun.ws.management.server.handler.wsman.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;

/**
 * The class to be presented by a data source that would like to be enumerated.
 * 
 * Implementations of this class are specific to the data structure being
 * enumerated.
 * 
 * @see EnumerationIterator
 */
public class pull_source_Iterator implements EnumerationIterator {

	private static final String SELECTOR_KEY = "log";
	private static final String NS_URI = "https://wiseman.dev.java.net/test/events/pull";
	private static final String NS_PREFIX = "log";

	/**
	 * A log of events to pass. These should be replaced with real events.
	 */
	private String[][] eventLog = { { "event1", "critical" },
			{ "event2", "warning" }, { "event3", "info" },
			{ "event4", "debug" } };

	private int cursor = 0;
	private final DocumentBuilder db;
	private final boolean includeEPR;
	private final String requestPath;
	private final String resourceURI;
	private final boolean includeItem;
	private final Timer eventTimer = new Timer(true);
	
	final class AddEventsTask extends TimerTask {
		
		private final pull_source_Iterator iterator;
		
		AddEventsTask(pull_source_Iterator iterator) {
			this.iterator = iterator;
		}

		@Override
		public void run() {
    		iterator.addEvents();
		}
	}

	protected pull_source_Iterator(final HandlerContext hcontext,
			final String resource, final Addressing request,
			final DocumentBuilder db, final boolean includeItem,
			final boolean includeEPR) {

		this.requestPath = hcontext.getURL();
		this.resourceURI = resource;
		this.db = db;
		this.includeEPR = includeEPR;
		this.includeItem = includeItem;
	}

	protected void addEvents() {
    	synchronized(this) {
            // Reset our cursor to 0
    		cursor = 0;
    		// Notify any waiters   		
    		this.notifyAll();
    	}
	}

	public EnumerationItem next() {

		if (cursor >= eventLog.length) {
			// No more data at the moment
			// Schedule a task to add data in 5 seconds
	        final AddEventsTask addEventsTask = new AddEventsTask(this);
	        final long DELAY = 5000;
	        eventTimer.schedule(addEventsTask, DELAY);
			return null;
		}
		final String key = eventLog[cursor][0];

		// construct an item if necessary for the enumeration
		Element item = null;
		if (includeItem) {
			final String value = eventLog[cursor][1];
			final Document doc = db.newDocument();
			item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + key);
			item.setTextContent(value);
			doc.appendChild(item);
		}

		// construct an endpoint reference to accompany the element, if needed
		EndpointReferenceType epr = null;
		if (includeEPR) {
			final Map<String, String> selectors = new HashMap<String, String>();
			selectors.put(SELECTOR_KEY, key);
			epr = EnumerationSupport.createEndpointReference(requestPath,
					resourceURI, selectors);
		}
		cursor++;
		return new EnumerationItem(item, epr);
	}

	public boolean hasNext() {
		// There are always more events
		return true; // cursor < eventLog.length;
	}

	public boolean isFiltered() {
		return false;
	}

	public int estimateTotalItems() {
		// choose not to provide an estimate
		return -1;
	}

	public void release() {
		// eventLog = new String[0][0];
	}
}