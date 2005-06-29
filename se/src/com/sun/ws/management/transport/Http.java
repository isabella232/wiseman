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
 * $Id: Http.java,v 1.1 2005-06-29 19:18:26 akhilarora Exp $
 */

package com.sun.ws.management.transport;

public final class Http {
    
    private static final String SOAP_MIME_TYPE = "application/soap+xml";
    private static final String CHARSET = "charset";
    private static final String DEFAULT_CHARSET= "utf-8";

    public static final String SOAP_MIME_TYPE_WITH_CHARSET = 
        SOAP_MIME_TYPE + ";" + CHARSET + "=" + DEFAULT_CHARSET;
    
    public static boolean isContentTypeAcceptable(final String contentType) {
        if (contentType == null) {
            return false;
        }
        
        boolean foundCharSet = false;
        boolean foundMimeType = false;
        for (final String type : contentType.split(";")) {
            final String trimType = type.trim();
            if (SOAP_MIME_TYPE.equals(trimType)) {
                foundMimeType = true;
            } else {
                final String[] charset = trimType.split("=");
                for (int i = 0; i < charset.length; i ++) {
                    if (i + 1 < charset.length &&
                        CHARSET.equals(charset[i].trim()) &&
			// TODO: also allow UTF-16
                        DEFAULT_CHARSET.equalsIgnoreCase(unquote(charset[i + 1].trim()))) {
                        foundCharSet = true;
                        break;
                    }
                }
            }
        }
        
        return foundCharSet && foundMimeType;
    }
    
    // remove leading and ending quotes from input string
    // TODO: improve the algorithm - possibly using regex
    private static String unquote(final String s) {
        int start = 0;
        if (s.startsWith("\"")) {
            start ++;
        }
        int end = s.length();
        if (s.endsWith("\"")) {
            end --;
        }
        return s.substring(start, end);
    }
}
