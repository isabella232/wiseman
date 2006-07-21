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
 * $Id: EnumerationContext.java,v 1.4 2006-07-21 20:26:22 pmonday Exp $
 */

package com.sun.ws.management.server;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xpath.XPathExpressionException;
import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;

final class EnumerationContext extends BaseContext {
    
    private final Object clientContext;
    private final EnumerationIterator iterator;
    private final EnumerationModeType mode;
    
    // implied value
    private int count = 1;
    private int cursor = 0;
    
    EnumerationContext(final XMLGregorianCalendar expiration,
            final String filter,
            final EnumerationModeType mode,
            final NamespaceMap namespaces,
            final Object clientContext,
            final EnumerationIterator iterator) throws XPathExpressionException {
        super(expiration, filter, namespaces);
        this.clientContext = clientContext;
        this.iterator = iterator;
        this.mode = mode;
    }
    
    /**
     * Returns the EnumerationMode
     * @return the EnumerationModeType, null if the mode was not set
     */
    public EnumerationModeType getEnumerationMode(){
        return mode;
    }
    
    int getCursor() {
        return cursor;
    }
    
    void setCursor(final int cursor) {
        this.cursor = cursor;
    }
    
    int getCount() {
        return count;
    }
    
    void setCount(final int count) {
        this.count = count;
    }
    
    Object getClientContext() {
        return clientContext;
    }

    EnumerationIterator getIterator() {
        return iterator;
    }
}
