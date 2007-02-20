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
 * $Id: custom_filter_Handler.java,v 1.3.2.1 2007-02-20 12:15:14 denis_rachal Exp $
 */

package com.sun.ws.management.server.handler.wsman.test.enumeration.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Filter;
import com.sun.ws.management.server.FilterFactory;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transfer.Transfer;

/**
 * Copied form java.system.properties. Added Custom Filter.
 */
public class custom_filter_Handler implements Handler{
    
	public static final String NS_URI = "https://wiseman.dev.java.net/java";
	public static final String NS_PREFIX = "java";
    public static final String TEST_CUSTOM_FILTER_DIALECT = "test/custom/filter";
    
    private final Map<String, String> NAMESPACES;


	private class CustomNodeList implements NodeList {

		final private ArrayList<Node> list;

		protected CustomNodeList() {
			list = new ArrayList<Node>();
		}
		
		protected CustomNodeList(int size) {
			list = new ArrayList<Node>(size);
		}
		
		public int getLength() {
			return list.size();
		}

		public Node item(int index) {
			return list.get(index);
		}
		
		protected void add(Node node) {
			list.add(node);
		}
	}
    
    private class TestCustomFilterFactory implements FilterFactory {
        private class TestCustomFilter implements Filter {
            public NodeList evaluate(final Node content) throws FaultException {
            	final CustomNodeList list = new CustomNodeList(1);
            	list.add(content);
                return list;
            }
            
            public String getDialect() {
            	return TEST_CUSTOM_FILTER_DIALECT;
            }

			public Object getExpression() {
				return "";
			}
        }
        public Filter newFilter(List content, 
                NamespaceMap namespaces) throws FaultException, Exception {
            return new TestCustomFilter();
        } 
    }
    
    public custom_filter_Handler() {
        try {
    		this.NAMESPACES = new HashMap<String, String>();
    		NAMESPACES.put(NS_PREFIX, NS_URI);
    		
            EnumerationSupport.addSupportedFilterDialect(TEST_CUSTOM_FILTER_DIALECT, 
                new TestCustomFilterFactory());
        }catch(Exception ex) {
            throw new IllegalArgumentException("Exception " + ex);
        }
    }
    
    public void handle(final String action, final String resource,
            final HandlerContext hcontext,
            final Management request, final Management response) throws Exception {
        
        final EnumerationExtensions enuRequest = new EnumerationExtensions(request);
        final EnumerationExtensions enuResponse = new EnumerationExtensions(response);
        
        if (Enumeration.ENUMERATE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.ENUMERATE_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            
            synchronized (this) {
            	// Make sure there is an Iterator factory registered for this resource
            	if (EnumerationSupport.getIteratorFactory(resource) == null) {
            		EnumerationSupport.registerIteratorFactory(resource,
            				new custom_filter_IteratorFactory(resource));
            	}
            }
            EnumerationSupport.enumerate(hcontext, enuRequest, enuResponse);
        } else if (Enumeration.PULL_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.PULL_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            EnumerationSupport.pull(hcontext,enuRequest, enuResponse);
        } else if (Enumeration.RELEASE_ACTION_URI.equals(action)) {
            enuResponse.setAction(Enumeration.RELEASE_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            EnumerationSupport.release(hcontext,enuRequest, enuResponse);
        } else if (Transfer.GET_ACTION_URI.equals(action)) {
            enuResponse.setAction(Transfer.GET_RESPONSE_URI);
            enuResponse.addNamespaceDeclarations(NAMESPACES);
            // return all the properties in a single response
            final Document doc = enuResponse.newDocument();
            final Element root = doc.createElementNS(Enumeration.NS_URI, Enumeration.NS_PREFIX + ":Items");
            doc.appendChild(root);
            final Iterator<Entry<Object, Object> > pi = System.getProperties().entrySet().iterator();
            while (pi.hasNext()) {
                final Entry<Object, Object> p = pi.next();
                final Element item = doc.createElementNS(NS_URI, NS_PREFIX + ":" + p.getKey());
                item.setTextContent(p.getValue().toString());
                root.appendChild(item);
            }
            enuResponse.getMessage().getSOAPBody().addDocument(doc);
        } else {
            throw new ActionNotSupportedFault(action);
        }
    }
}