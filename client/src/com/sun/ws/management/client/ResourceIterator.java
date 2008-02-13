/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ** Copyright (C) 2006, 2007, 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 *
 */

/**
 * 
 */
package com.sun.ws.management.client;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.message.enueration.WSManEnumerateRequest;
import com.sun.ws.management.client.message.enueration.WSManEnumerateResponse;
import com.sun.ws.management.client.message.enueration.WSManPullRequest;
import com.sun.ws.management.client.message.enueration.WSManPullResponse;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;

/**
 * 
 */
public class ResourceIterator implements EnumerationIterator {

	private static final Logger s_log = Logger.getLogger(ResourceIterator.class
			.getName());

	private final WSManEnumerateResponse enumResponse;
	private WSManPullResponse pullResponse;
	private long itemCount = -1;
	private Iterator<EnumerationItem> items = null;
	private boolean eos = false;

	/**
	 * @param epr
	 * @param filter
	 * @param filterNamespaces
	 * @param filterDialect
	 * @param expires
	 * @param maxElements
	 * @param maxCharacters
	 * @param maxTime
	 * @param mode
	 * @param getItemCount
	 * @param options
	 * @param binding
	 * @param params
	 * @throws Exception
	 */
	public ResourceIterator(final WSManEnumerateRequest request)
			throws Exception {

		if (s_log.isLoggable(Level.FINER))
			s_log.finer("REQUEST:\n" + request.toString() + "\n");
		enumResponse = (WSManEnumerateResponse) request.invoke();

		// Check for fault during message generation
		if (enumResponse.isFault()) {
			s_log.severe("FAULT:\n" + enumResponse + "\n");
			SOAPFault fault = (SOAPFault) enumResponse.getPayload();
			throw new FaultException(fault);
		}
		if (s_log.isLoggable(Level.FINER))
			s_log.finer("RESPONSE:\n" + enumResponse.toString() + "\n");

		eos = enumResponse.isEndOfSequence();

		if (enumResponse.getTotalItemsCountEstimate() != null)
			itemCount = enumResponse.getTotalItemsCountEstimate().longValue();

		final List<EnumerationItem> enumItems = enumResponse.getItems();
		if (enumItems != null) {
			items = enumItems.iterator();
		} else {
			// pull the first block of data
			if (eos == false) {
				final WSManPullRequest pull = enumResponse.createPullRequest();
				if (pull == null) {
					eos = true;
					return;
				}
				pullResponse = (WSManPullResponse) pull.invoke();
				if (pullResponse == null) {
					eos = true;
					return;
				}
				final List<EnumerationItem> pullItems = pullResponse.getItems();
				if (pullItems != null)
					items = pullItems.iterator();
				else
					eos = true;
			}
		}
	}

	private void doPull() throws Exception {

		if (eos == false) {
			final WSManPullRequest pull;
			if (pullResponse == null)
				pull = enumResponse.createPullRequest();
			else
				pull = pullResponse.createPullRequest();
			if (pull == null) {
				eos = true;
				return;
			}
			pullResponse = (WSManPullResponse) pull.invoke();
			if (pullResponse == null) {
				eos = true;
				return;
			}
			final List<EnumerationItem> pullItems = pullResponse.getItems();
			if (pullItems != null)
				items = pullItems.iterator();
			else
				eos = true;
		}
	}

	/**
	 * @see com.sun.ws.management.server.EnumerationIterator#estimateTotalItems()
	 */
	public int estimateTotalItems() {
		return (int) itemCount;
	}

	/**
	 * @see com.sun.ws.management.server.EnumerationIterator#hasNext()
	 */
	public boolean hasNext() {
		if (items.hasNext()) {
			return true;
		} else {
			if (eos) {
				return false;
			} else {
				// pull the next block
				try {
					doPull();
				} catch (SOAPException e) {
					return false;
				} catch (JAXBException e) {
					return false;
				} catch (DatatypeConfigurationException e) {
					return false;
				} catch (IOException e) {
					return false;
				} catch (FaultException e) {
					return false;
				} catch (Exception e) {
					return false;
				}
				return items.hasNext();
			}
		}
	}

	/**
	 * @see com.sun.ws.management.server.EnumerationIterator#next()
	 */
	public EnumerationItem next() {
		if ((items.hasNext() == false) && (eos == false))
			try {
				doPull();
			} catch (SOAPException e) {
				return null;
			} catch (JAXBException e) {
				return null;
			} catch (DatatypeConfigurationException e) {
				return null;
			} catch (IOException e) {
				return null;
			} catch (FaultException e) {
				return null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return null;
			}
		return (EnumerationItem) items.next();
	}

	/**
	 * @see com.sun.ws.management.server.EnumerationIterator#isFiltered()
	 */
	public boolean isFiltered() {
		return true;
	}

	/**
	 * @see com.sun.ws.management.server.EnumerationIterator#release()
	 */
	public void release() {

		try {
			if ((pullResponse != null) && (!pullResponse.isEndOfSequence()))
				pullResponse.release();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean renew(Duration expires) {
		// TODO Auto-generated method stub
		return false;
	}
}
