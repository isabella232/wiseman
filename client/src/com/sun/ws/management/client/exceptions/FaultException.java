/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: FaultException.java,v 1.3 2007-05-30 20:30:32 nbeers Exp $
 */
package com.sun.ws.management.client.exceptions;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPFault;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FaultException extends Exception {

	private static final long serialVersionUID = 8287971277639875397L;

	public FaultException() {
		super();
	}

	public FaultException(String message) {
		super(message);
	}

	public FaultException(String message, Throwable cause) {
		super(message, cause);
	}

	public FaultException(Throwable cause) {
		super(cause);
	}

	public FaultException(SOAPFault fault) {
		super(getSOAPFaultString(fault));
	}
	
	// Convert the SOAPFault into a readable string
	private static String getSOAPFaultString(SOAPFault fault) {
		final StringBuilder sb = new StringBuilder();
		sb.append("SOAP Fault: " + fault.getFaultString() + "\n");
		sb.append("     Actor: "
				+ (fault.getFaultActor() == null ? "" : fault.getFaultActor())
				+ "\n");
		sb.append("      Code: " + fault.getFaultCode() + "\n");
		try {
			Iterator subcodes = fault.getFaultSubcodes();
			sb.append("  Subcodes:");
			while (subcodes.hasNext()) {
				final QName code = (QName) subcodes.next();
				final String prefix = code.getPrefix();
				if ((prefix == null) || (prefix.length() == 0))
					sb.append(" " + code.getLocalPart());
				else
					sb.append(" " + prefix + ":" + code.getLocalPart());
			}
			sb.append("\n");
		} catch (UnsupportedOperationException e) {
			// no subcodes
		}
		if (fault.hasDetail()) {
			Iterator details = fault.getDetail().getDetailEntries();
			sb.append("    Detail: ");
			while (details.hasNext()) {
				DetailEntry entry = (DetailEntry) details.next();

				final NodeList nl = entry.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					final Node child = nl.item(i);
					if (i == 0)
						sb.append(child.getTextContent() + "\n");
					else
						sb.append("            " + child.getTextContent() + "\n");
				}
			}
		}
		return sb.toString();
	}
}
