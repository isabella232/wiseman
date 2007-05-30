/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: EnumerationResourceStateImpl.java,v 1.2 2007-05-30 20:30:28 nbeers Exp $
 */
package com.sun.ws.management.client.impl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.ws.management.client.EnumerationResourceState;

/**
 * Represents any enumeration response document. Provides access to the list of items returned
 * by an enumeration pull or an optimized enumeration call.  This list can be a list of the actual
 * items or a list of XML fragments if an enumeration fragment request is performed.
 * 
 * @author nabee
 *
 */
public class EnumerationResourceStateImpl extends ResourceStateImpl implements
		EnumerationResourceState {
	
	// List of child items contained in the response
	private List<Node> enumItems = null;

	public EnumerationResourceStateImpl(Document stateDocument) {
		super(stateDocument);
	}

	/**
	 * Return a list of all the item elements contained in the resource
	 * 
	 * @return
	 */
	public List<Node> getEnumerationItems() {
		
		if (enumItems != null){
			return enumItems;
		}
		
		NodeList rootChildren = getDocument().getChildNodes();
		//PullResponse node
		if (rootChildren != null) {
			Node child = rootChildren.item(0);
			//Items node
			if (child != null){
				enumItems = new ArrayList<Node>();
				NodeList children = child.getChildNodes().item(1).getChildNodes();
		 		 
				//iterate through and add each child to the list
				for(int i=0;i<children.getLength();i++){
					enumItems.add(children.item(i));
				}
			 }
		 }
		return enumItems;
	}
	
}
