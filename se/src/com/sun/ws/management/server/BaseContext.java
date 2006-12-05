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
 * $Id: BaseContext.java,v 1.8 2006-12-05 10:35:23 jfdenise Exp $
 */

package com.sun.ws.management.server;

import java.util.HashMap;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

class BaseContext {
    
    private final XMLGregorianCalendar expiration;
    private final Filter filter;
    
    BaseContext(final XMLGregorianCalendar expiry,
            final Filter filter) {
        
        expiration = expiry;
        this.filter = filter;
    }
    
    String getExpiration() {
        return expiration.toXMLFormat();
    }
    
    boolean isExpired(final XMLGregorianCalendar now) {
        if (expiration == null) {
            // no expiration defined, never expires
            return false;
        }
        return now.compare(expiration) > 0;
    }
    
    boolean evaluate(final Node content, final NamespaceMap... ns) throws Exception {
        // pass-thru if no filter is defined
        boolean pass = true;
        if (filter != null) {
            pass = filter.evaluate(content, ns);
        }
        return pass;
    }
}
