/*
 * Copyright 2005-2008 Sun Microsystems, Inc.
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
 ** Copyright (C) 2006, 2007, 2008 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Denis Rachal (denis.rachal@hp.com)
 **
 *
 */

package com.sun.ws.management.client.impl;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class JAXBNamespacePrefixMapper extends NamespacePrefixMapper {
	
	Map<String, String> namespaces = new HashMap<String, String>();

    public String getPreferredPrefix(final String uri, 
    		final String suggested,
			final boolean required) {

		if (namespaces.containsKey(uri)) {
			final String prefix = namespaces.get(uri);
			if (prefix == null)
				return suggested;
			else
				return prefix;
		} else {
			return suggested;
		}
	}
    
    public String[] getPreDeclaredNamespaceUris() {
    	final String[] uris = new String[namespaces.size()];
    	namespaces.keySet().toArray(uris);
        return uris;
    }
    
    public void addPreDeclaredNamespaceUri(final String prefix, final String uri) {
    	namespaces.put(uri, prefix);
    }
}
