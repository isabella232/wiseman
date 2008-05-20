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
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 *
 */

package com.sun.ws.management.spi.client.impl.jaxws;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.message.Message;

/*
 * Logging handler for outgoing and incoming messages.
 */
public class JAXWSLoggingHandler implements MessageHandler<MessageHandlerContext> {

	private final static Logger logger = Logger
	.getLogger(JAXWSLoggingHandler.class.getCanonicalName());
	
    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(MessageHandlerContext mhc) {
		logMessage(mhc);
		return true;
	}

    public boolean handleFault(MessageHandlerContext mhc) {
    	logMessage(mhc);
        return true;
    }

    public void close(MessageContext messageContext) {
    	// Nothing to do here.
    }
    
	private void logMessage(final MessageHandlerContext context) {
		
		if (!logger.isLoggable(Level.FINE))
			return;
		
		final Boolean outbound = (Boolean) context
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		final Message message = context.getMessage().copy();
		if (outbound.booleanValue()) {
			logger.fine("<request>\n" + toString(message) + "</request>\n");
		} else {
			logger.fine("<response>\n" + toString(message) + "</response>\n");
		}
	}

	private String toString(Message message) {
		final XMLOutputFactory factory = XMLOutputFactory.newInstance();
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			final XMLStreamWriter writer = factory.createXMLStreamWriter(bos);
			message.writeTo(writer);
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toString();
		/*
		final byte[] content = bos.toByteArray();
		final ByteArrayInputStream bis = new ByteArrayInputStream(content);
		final Document doc = Addressing.getDocumentBuilder().parse(bis);
		final OutputFormat format = new OutputFormat(doc);
		format.setLineWidth(72);
		format.setIndenting(true);
		format.setIndent(2);
		final XMLSerializer serializer = new XMLSerializer(os, format);
		serializer.serialize(doc);
		os.write("\n".getBytes());
		*/
	}
}
