/**
 * 
 */
package com.sun.ws.management.client;

import java.util.List;

import org.w3c.dom.Node;

/**
 * Represents any enumeration response document. Provides access to the list of items returned
 * by an enumeration pull or an optimized enumeration call.  This list can be a list of the actual
 * items or a list of XML fragments if an enumeration fragment request is performed.
 * 
 * @author nabee
 *
 */
public interface EnumerationResourceState extends ResourceState {
	
	/**
	 * Return a list of all the item elements contained in the resource
	 * 
	 * @return a list of Nodes
	 */
	List<Node> getEnumerationItems();

}
