/*
 * Copyright 2005-2008 Sun Microsystems, Inc.
 *
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
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 *
 */

package com.sun.ws.management.client.message.enueration;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableNonNegativeInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.ItemType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.client.message.wsman.WSManResponse;
import com.sun.ws.management.server.EnumerationItem;

public class WSManPullResponse extends WSManResponse {

	public static final QName ITEMS = new QName(WSManResponse.NS_URI, "Items",
			WSManResponse.NS_PREFIX);

	public static final QName ITEM = new QName(WSManResponse.NS_URI, "Item",
			WSManResponse.NS_PREFIX);

	public static final QName END_OF_SEQUENCE = new QName(WSManResponse.NS_URI,
			"EndOfSequence", WSManResponse.NS_PREFIX);

	private boolean pullResponseRead;
	private PullResponse pullResponse;
	private boolean itemsCountRead;
	private BigInteger itemsCount;
	private boolean itemsRead;
	private List<EnumerationItem> items;
	private boolean isEosRead;
	private boolean isEos;

	WSManPullResponse() {
		super(null);
	}

	public WSManPullResponse(final SOAPResponse response) {
		super(response);
	}

	public PullResponse getPullResponse() throws Exception {
		if (!pullResponseRead) {
			pullResponseRead = true;
			final Object payload = getPayload();
			if ((payload != null) && (payload instanceof PullResponse))
				pullResponse = (PullResponse) payload;
		}
		return pullResponse;
	}

	public BigInteger getTotalItemsCountEstimate() throws Exception {
		if (!itemsCountRead) {
			itemsCountRead = true;
			final Object header = getHeader(WSManEnumerateResponse.TOTAL_ITEMS_COUNT_ESTIMATE);
			if (header != null) {
				if ((header instanceof JAXBElement)
						&& (((JAXBElement) header).getDeclaredType()
								.equals(AttributableNonNegativeInteger.class))) {
					itemsCount = ((JAXBElement<AttributableNonNegativeInteger>) header)
							.getValue().getValue();
				}
			}
		}
		return itemsCount;
	}

	public List<EnumerationItem> getItems() throws Exception {
		if (!itemsRead) {
			itemsRead = true;

			final PullResponse pullResponse = getPullResponse();
			if (pullResponse != null) {
				final ItemListType itemList = pullResponse.getItems();
				if (itemList != null) {
					items = WSManPullResponse.getEnumerationIteList(itemList
							.getAny());
				}
			}
		}
		return items;
	}

	public boolean isEndOfSequence() throws Exception {
		if (!isEosRead) {
			isEosRead = true;
			final PullResponse pullResponse = getPullResponse();

			if (pullResponse != null)
				isEos = (pullResponse.getEndOfSequence() != null);
		}
		return isEos;
	}

	public static List<EnumerationItem> getEnumerationIteList(
			final List<Object> anyItems) throws JAXBException, IOException {
		if (anyItems == null) {
			return null;
		}
		final int size = anyItems.size();
		final List<EnumerationItem> itemList = new ArrayList<EnumerationItem>();
		for (int i = 0; i < size; i++) {
			final Object object = anyItems.get(i);
			itemList.add(unbindItem(object));
		}
		return itemList;
	}

	private static EnumerationItem unbindItem(Object obj) throws JAXBException {
		// the three possibilities are: EPR only, Item and EPR or Item only

		Object item = null;
		EndpointReferenceType eprt = null;
		if (obj instanceof JAXBElement) {
			final JAXBElement elt = (JAXBElement) obj;
			if (EndpointReferenceType.class.equals(elt.getDeclaredType())) {
				// EPR only
				eprt = ((JAXBElement<EndpointReferenceType>) obj).getValue();
			} else {
				if (ItemType.class.equals(elt.getDeclaredType())) {
					// Item and EPR
					final ItemType wsmanItem = ((JAXBElement<ItemType>) obj)
							.getValue();
					final List<Object> content = wsmanItem.getContent();
					final Iterator iter = content.iterator();
					while (iter.hasNext()) {
						Object itemObj = iter.next();
						// XXX Revisit, JAXB is adding an empty String when
						// unmarshaling
						// a Mixed content. getContent() returns a list
						// containing
						// such empty String element
						// BUG ID is : 6542005
						// Unmarshalled @XmlMixed list contains an additional
						// empty String
						if (itemObj instanceof String) {
							// Having the list being of Mixed content
							// An empty string element is added.
							String str = (String) itemObj;
							if (str.length() == 0)
								continue;
							else
								item = itemObj;
						} else if ((itemObj instanceof JAXBElement)
								&& ((JAXBElement) itemObj).getDeclaredType()
										.equals(EndpointReferenceType.class)) {
							final JAXBElement<EndpointReferenceType> jaxbEpr = (JAXBElement<EndpointReferenceType>) itemObj;
							eprt = jaxbEpr.getValue();
						} else {
							item = itemObj;
						}
					}
				} else {
					// JAXB Item only
					item = elt;
				}
			}
		} else {
			// Item only
			item = obj;
		}
		return new EnumerationItem(item, eprt);
	}
}
