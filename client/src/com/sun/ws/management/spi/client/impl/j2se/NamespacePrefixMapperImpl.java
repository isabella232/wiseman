package com.sun.ws.management.spi.client.impl.j2se;

import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

	private final Map<String, String> namespaces;
	
	public NamespacePrefixMapperImpl(final Map<String, String> namespaces) {
		this.namespaces = new HashMap<String, String>(namespaces.size());
		
		// Reverse the map
		for (final String key : namespaces.keySet())
		    this.namespaces.put(namespaces.get(key), key);
	}
	
    /**
     * Returns a preferred prefix for the given namespace URI.
     * 
     * This method is intended to be overrided by a derived class.
     * 
     * @param namespaceUri
     *      The namespace URI for which the prefix needs to be found.
     *      Never be null. "" is used to denote the default namespace.
     * @param suggestion
     *      When the content tree has a suggestion for the prefix
     *      to the given namespaceUri, that suggestion is passed as a
     *      parameter. Typicall this value comes from the QName.getPrefix
     *      to show the preference of the content tree. This parameter
     *      may be null, and this parameter may represent an already
     *      occupied prefix. 
     * @param requirePrefix
     *      If this method is expected to return non-empty prefix.
     *      When this flag is true, it means that the given namespace URI
     *      cannot be set as the default namespace.
     * 
     * @return
     *      null if there's no prefered prefix for the namespace URI.
     *      In this case, the system will generate a prefix for you.
     * 
     *      Otherwise the system will try to use the returned prefix,
     *      but generally there's no guarantee if the prefix will be
     *      actually used or not.
     * 
     *      return "" to map this namespace URI to the default namespace.
     *      Again, there's no guarantee that this preference will be
     *      honored.
     * 
     *      If this method returns "" when requirePrefix=true, the return
     *      value will be ignored and the system will generate one.
     */
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
    	final String prefix = namespaces.get(namespaceUri);
    	
    	if (prefix != null)
    		return prefix;
            
        // otherwise I don't care. Just use the default suggestion, whatever it may be.
        return suggestion;
    }
    
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { };
    }
}
