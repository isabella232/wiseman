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
 * $Id: XPath.java,v 1.2 2006-02-16 20:12:45 akhilarora Exp $
 */

package com.sun.ws.management.xml;

import javax.xml.xpath.XPathFactory;

public interface XPath {
    
    public static final String NS_PREFIX = "xpath";
    public static final String NS_URI = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    public static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
    public static final String[] SUPPORTED_FILTER_DIALECTS = {
        NS_URI
    };
    
}
