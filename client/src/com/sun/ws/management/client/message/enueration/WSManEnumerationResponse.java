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

import javax.xml.namespace.QName;

import com.sun.ws.management.client.message.SOAPResponse;
import com.sun.ws.management.client.message.wsman.WSManResponse;

public class WSManEnumerationResponse extends WSManResponse {
	
    public static final String NS_PREFIX = "wsen";
    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/enumeration";
	
    public static final QName ENUMERATION_CONTEXT = new QName(NS_URI, "EnumerationContext", NS_PREFIX);
	
	WSManEnumerationResponse() {
        super(null);
	}
	
	public WSManEnumerationResponse(final SOAPResponse response) {
		super(response);
	}
}
