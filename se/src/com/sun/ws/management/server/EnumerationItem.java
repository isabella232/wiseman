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
 * $Id: EnumerationItem.java,v 1.1 2006-07-24 13:16:52 pmonday Exp $
 */

package com.sun.ws.management.server;

import java.io.Serializable;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

/**
 * EnumerationElement instances contain all of the necessary contents
 * from a handler for the server to return elements for a request.
 * {@link EnumerationIterator EnumerationIterator}.
 *
 * @see EnumerationIterator
 */
public final class EnumerationItem {
    
    /**
     * Holds value of property element.
     */
    private Element element = null;

    /**
     * Holds value of property endpointReference.
     */
    private EndpointReferenceType endpointReference;

    public EnumerationItem() {
        
    }
    
    public EnumerationItem(Element element, EndpointReferenceType endpointReference) {
        this.element = element;
        this.endpointReference = endpointReference;
    }
    
    /**
     * Getter for property element.
     * @return Value of property element.
     */
    public Element getElement()
    {
        return this.element;
    }

    /**
     * Getter for property endpointReference.
     * @return Value of property endpointReference.
     */
    public EndpointReferenceType getEndpointReference()
    {
        return this.endpointReference;
    }
}
