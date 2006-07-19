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
 * $Id: BaseContext.java,v 1.7 2006-07-19 22:41:36 akhilarora Exp $
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
    private final String expression;
    private final NamespaceMap initialNamespaceMap;
    private final Map<String, String> aggregateNamespaces = new HashMap<String, String>();
    private final XPath xpath = com.sun.ws.management.xml.XPath.XPATH_FACTORY.newXPath();
    
    private NamespaceMap aggregateNamespaceMap = null;
    
    BaseContext(final XMLGregorianCalendar expiry,
            final String expr,
            final NamespaceMap namespaces) throws XPathExpressionException {
        
        expiration = expiry;
        expression = expr;
        initialNamespaceMap = namespaces;
        if (initialNamespaceMap != null) {
            aggregateNamespaces.putAll(initialNamespaceMap.getMap());
            aggregateNamespaceMap = new NamespaceMap(aggregateNamespaces);
            xpath.setNamespaceContext(aggregateNamespaceMap);
        }
        if (expression != null) {
            // compile the expression just to see if it's valid
            xpath.compile(expression);
        }
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
    
    boolean evaluate(final Node content, final NamespaceMap... ns) throws XPathExpressionException {
        // pass-thru if no filter is defined
        boolean pass = true;
        if (expression != null) {
            if (ns != null && ns.length > 0) {
                for (final NamespaceMap map : ns) {
                    aggregateNamespaces.putAll(map.getMap());
                }
                aggregateNamespaceMap = new NamespaceMap(aggregateNamespaces);
            }
            
            xpath.setNamespaceContext(aggregateNamespaceMap);
            final XPathExpression filter = xpath.compile(expression);
            pass = (Boolean) filter.evaluate(content, XPathConstants.BOOLEAN);
        }
        return pass;
    }
}
