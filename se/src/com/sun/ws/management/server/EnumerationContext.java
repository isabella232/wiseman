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
 * $Id: EnumerationContext.java,v 1.1 2006-02-16 20:12:40 akhilarora Exp $
 */

package com.sun.ws.management.server;

import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xpath.XPathExpressionException;

final class EnumerationContext extends BaseContext {
    
    private final Object clientContext;
    private final EnumerationIterator iterator;

    // implied value
    private int count = 1;
    private int cursor = 0;
    
    EnumerationContext(final XMLGregorianCalendar expiration,
            final String filter,
            final Map<String, String> namespaces,
            final Object clientContext,
            final EnumerationIterator iterator) throws XPathExpressionException {
        super(expiration, filter, namespaces);
        this.clientContext = clientContext;
        this.iterator = iterator;
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
