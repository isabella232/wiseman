/*
 * Copyright 2006 Sun Microsystems, Inc.
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
 * $Id: EnumerationItem.java,v 1.2 2006-07-25 05:49:04 akhilarora Exp $
 */

package com.sun.ws.management.server;

import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

/**
 * EnumerationItem instances contain all of the necessary contents
 * from a handler for the server to return elements for an enumeation request.
 */
public final class EnumerationItem {
    
    /**
     * Holds an enumeration item.
     */
    private final Element item;

    /**
     * Holds a reference to the item.
     */
    private final EndpointReferenceType endpointReference;

    public EnumerationItem(final Element it, final EndpointReferenceType epr) {
        item = it;
        endpointReference = epr;
    }
    
    /**
     * Getter for enumeration item.
     * @return Value of item element.
     */
    public Element getItem() {
        return item;
    }

    /**
     * Getter for item's EndpointReference.
     * @return Value of item endpointReference.
     */
    public EndpointReferenceType getEndpointReference() {
        return endpointReference;
    }
}
