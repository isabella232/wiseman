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

package com.sun.ws.management.client.message.wsman;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.client.message.addressing.WSAddressingResponse;


public class WSManResponse extends WSAddressingResponse {
	
    public static final String NS_PREFIX = "wsman";
    public static final String NS_URI = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";

    public static final QName FAULT_DETAIL = new QName(NS_URI, "FaultDetail", NS_PREFIX);
	
	WSManResponse() {
        super(null);
	}
	
	public WSManResponse(final SOAPResponse response) {
		super(response);
	}
	
	public static Object getAnyObject(final List<Object> anyList,
			final Class elementType, final QName elementName) {
		for (final Object any : anyList) {
			if (any instanceof JAXBElement) {
				final JAXBElement element = (JAXBElement) any;
				if ((elementType != null && elementType.equals(element
						.getDeclaredType()))
						&& (elementName != null && elementName.equals(element.getName()))) {
					return element.getValue();
				}
			}
		}
		return null;
	}
	
	// TODO: Add ability to get wsman:FAULT_DETAIL
}
