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
 * $Id: BaseContext.java,v 1.9 2007-01-14 17:52:34 denis_rachal Exp $
 */

package com.sun.ws.management.server;

import java.util.ArrayList;

import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class BaseContext {
	
	private class BaseNodeList implements NodeList {

		final private ArrayList<Node> list;

		protected BaseNodeList() {
			list = new ArrayList<Node>();
		}
		
		protected BaseNodeList(int size) {
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
    
    private final XMLGregorianCalendar expiration;
    private final Filter filter;
    private boolean deleted;
    
    BaseContext(final XMLGregorianCalendar expiry,
            final Filter filter) {
        
        this.expiration = expiry;
        this.filter = filter;
        this.deleted = false;
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
    
    boolean isDeleted() {
    	return deleted;
    }
    
    void setDeleted() {
    	this.deleted = true;
    }
    
    NodeList evaluate(final Node content) throws Exception {
        // pass-thru if no filter is defined
        if (filter != null) {
            return filter.evaluate(content);
        } else {
        	final BaseNodeList list = new BaseNodeList(1);
        	list.add(content);
            return list;
        }
    }
}
