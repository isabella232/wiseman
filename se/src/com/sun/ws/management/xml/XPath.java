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
 * $Id: XPath.java,v 1.4 2006-05-24 00:31:26 akhilarora Exp $
 */

package com.sun.ws.management.xml;

import javax.xml.xpath.XPathFactory;

public final class XPath {
    
    public static final String NS_PREFIX = "xpath";
    public static final String NS_URI = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    public static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
    public static final String[] SUPPORTED_FILTER_DIALECTS = {
        NS_URI
    };
    
    /**
     * Determines if the passed-in dialect is a supported dialect
     *
     * @param dialect
     * @return true if it is a supported dialect (or if dialect is null=default), else false
     */
    public static boolean isSupportedDialect(final String dialect) {
        //if dialect is null, then it is default so return true
        if (dialect == null) {
            return true;
        }
        boolean isSupportedDialect = false;
        final String[] supportedFilterDialects = SUPPORTED_FILTER_DIALECTS;
        for (final String supportedFilterDialect : supportedFilterDialects) {
            if (supportedFilterDialect.equals(dialect)) {
                isSupportedDialect = true;
                break;
            }
        }
        return isSupportedDialect;
    }

}
