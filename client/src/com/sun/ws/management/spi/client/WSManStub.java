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

package com.sun.ws.management.spi.client;

import java.io.OutputStream;

import com.sun.ws.management.message.api.client.soap.SOAPRequest;
import com.sun.ws.management.message.api.client.soap.SOAPResponse;
import com.sun.ws.management.spi.client.impl.j2se.J2SE2WSManStub;
import com.sun.ws.management.spi.client.impl.j2se.J2SEWSManStub;

public abstract class WSManStub {

	private static String defaultStub =
	// com.sun.ws.management.client.spi.impl.j2se.J2SE2WSManStub.class.getCanonicalName();
	com.sun.ws.management.spi.client.impl.jaxws.messages.JAXWSWSManStub.class
			.getCanonicalName();

	protected WSManStub() {

	}

	public static void setDefaultStub(final String classname) {
		// TODO: Check if it can be loaded.
		defaultStub = classname;
	}

	public static WSManStub newInstance() {
		// TODO: Use reflection to create the default factory.
		if (defaultStub
				.equals(com.sun.ws.management.spi.client.impl.jaxws.messages.JAXWSWSManStub.class
						.getCanonicalName()))
			return new com.sun.ws.management.spi.client.impl.jaxws.messages.JAXWSWSManStub();
		if (defaultStub
				.equals(com.sun.ws.management.spi.client.impl.jaxws.soapmessage.JAXWSWSManStub.class
						.getCanonicalName()))
			return new com.sun.ws.management.spi.client.impl.jaxws.soapmessage.JAXWSWSManStub();
		if (defaultStub.equals(J2SEWSManStub.class.getCanonicalName()))
			return new J2SEWSManStub();
		if (defaultStub.equals(J2SE2WSManStub.class.getCanonicalName()))
			return new J2SE2WSManStub();
		return null;
	}

	public abstract SOAPResponse invoke(final SOAPRequest soapReq)
			throws Exception;

	public abstract void invokeOneWay(final SOAPRequest soapReq)
			throws Exception;

	public abstract void writeTo(final SOAPRequest soapReq,
			final OutputStream os, final boolean formatted) throws Exception;
}
