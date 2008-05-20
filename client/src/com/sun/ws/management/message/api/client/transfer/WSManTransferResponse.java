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

package com.sun.ws.management.message.api.client.transfer;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;

import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.message.api.client.wsman.WSManResponse;

public class WSManTransferResponse extends WSManResponse {
	
    public static final String NS_PREFIX = "wxf";
    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer";

	public static final QName FRAGMENT_TRANSFER = new QName(WSManResponse.NS_URI,
			"FragmentTransfer", WSManResponse.NS_PREFIX);

	public static final QName XML_FRAGMENT = new QName(WSManResponse.NS_PREFIX,
			"XmlFragment", WSManResponse.NS_PREFIX);

	public static final QName DIALECT = new QName("Dialect");
	
	private boolean fragmentTransferRead;
	private DialectableMixedDataType fragmentTransfer;

	WSManTransferResponse() {
		super(null);
	}

	public WSManTransferResponse(final SOAPResponse response) {
		super(response);
	}

	public DialectableMixedDataType getFragmentTransferHeader() throws Exception {
		if (!fragmentTransferRead) {
			fragmentTransferRead = true;
			final Object header = getHeader(XML_FRAGMENT);
			if (header != null) {
				if ((header instanceof JAXBElement)
						&& (((JAXBElement) header).getDeclaredType()
								.equals(DialectableMixedDataType.class))) {
					fragmentTransfer = ((JAXBElement<DialectableMixedDataType>) header)
							.getValue();
				}
			}
		}
		return fragmentTransfer;
	}
	
	// TODO: How to get the namespaces for the FragmentTransfer?
}
