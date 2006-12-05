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
 * $Id: Filter.java,v 1.1 2006-12-05 10:34:44 jfdenise Exp $
 */

package com.sun.ws.management.server;

import org.w3c.dom.Node;

/**
 * Filtering support interface
 * 
 */
public interface Filter {
    /**
     * Filter evaluation
     * @param content A node to apply filter on.
     * @param ns XML Namespaces Map
     * @throws java.lang.Exception In case filtering fail.
     * @return true means that the content is filtered in by this filter. 
     * false that the content is filtered out by this filter 
     */
    public boolean evaluate(final Node content, NamespaceMap... ns) 
    throws Exception;
}
