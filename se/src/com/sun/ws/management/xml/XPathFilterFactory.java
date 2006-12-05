/*
 * Copyright 2005 Sun Microsystems, Inc.
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
 * $Id: XPathFilterFactory.java,v 1.1 2006-12-05 10:34:44 jfdenise Exp $
 */

package com.sun.ws.management.xml;

import com.sun.ws.management.eventing.FilteringRequestedUnavailableFault;
import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.server.*;
import com.sun.ws.management.soap.FaultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import javax.xml.xpath.XPath;

/**
 * XPath based Filtering factory
 */
public class XPathFilterFactory implements FilterFactory {

    /**
     * Filter creation
     * @param content The filter content. In this case an XPath expression (String) 
     * located in the list first element.
     * @param namespaces An XML namespaces map.
     * @return A Filter handling XPath filtering.
     * 
     * @throws com.sun.ws.management.soap.FaultException If any WS-MAN related protocol exception occurs.
     * @throws java.lang.Exception If any other exception occurs.
     */
    public Filter newFilter(List content, NamespaceMap namespaces) throws FaultException, Exception {
        return new XPathEnumerationFilter(content, namespaces);
    }
    
    class XPathEnumerationFilter implements Filter {
        private String expression;
        private final XPath xpath = com.sun.ws.management.xml.XPath.XPATH_FACTORY.newXPath();
        private NamespaceMap initialNamespaceMap;
        private final Map<String, String> aggregateNamespaces = new HashMap<String, String>();
        private NamespaceMap aggregateNamespaceMap = null;
        
        /** Creates a new instance of XPathEnumerationFilter */
        public XPathEnumerationFilter(List filterExpressions, NamespaceMap namespaces)
        throws FaultException, Exception {
             if (filterExpressions == null) {
                throw new InvalidMessageFault("Missing a filter expression");
            }
            final Object expr = filterExpressions.get(0);
            if (expr == null) {
                throw new InvalidMessageFault("Missing filter expression");
            }
            if (expr instanceof String) {
                expression = (String) expr;
            } else {
                throw new InvalidMessageFault("Invalid filter expression type: " +
                        expr);
            }
            
            initialNamespaceMap = namespaces;
            if (initialNamespaceMap != null) {
                aggregateNamespaces.putAll(initialNamespaceMap.getMap());
                aggregateNamespaceMap = new NamespaceMap(aggregateNamespaces);
                xpath.setNamespaceContext(aggregateNamespaceMap);
            }
            
            // compile the expression just to see if it's valid
            try {
                xpath.compile(expression);
            } catch(XPathExpressionException ex) {
                throw new Exception("Unable to compile XPath expression : "
                        + expression);
            }
        }
        
        public boolean evaluate(final Node content,
                final NamespaceMap... ns) throws XPathExpressionException {
            NamespaceMap aggregateNamespaceMap = null;
            if (ns != null && ns.length > 0) {
                for (final NamespaceMap map : ns) {
                    aggregateNamespaces.putAll(map.getMap());
                }
                aggregateNamespaceMap = new NamespaceMap(aggregateNamespaces);
            }
            xpath.setNamespaceContext(aggregateNamespaceMap);
            final XPathExpression filter = xpath.compile(expression);
            return (Boolean) filter.evaluate(content, XPathConstants.BOOLEAN);
        }
    }
}
