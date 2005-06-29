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
 * $Id: JavaException.java,v 1.1 2005-06-29 19:18:23 akhilarora Exp $
 */

package com.sun.ws.management.java;

import javax.xml.namespace.QName;

// namespace declarations for a Java Exception to be returned in Faults
public interface JavaException {
    
    String NS_PREFIX = "ex";
    String NS_URI = "http://schemas.sun.com/ws/java/exception";

    public static final QName EXCEPTION = new QName(NS_URI, "Exception", NS_PREFIX);
    public static final QName CAUSE = new QName(NS_URI, "Cause", NS_PREFIX);
    public static final QName STACK_TRACE = new QName(NS_URI, "StackTrace", NS_PREFIX);
    public static final QName STACK_TRACE_ELEMENT = new QName(NS_URI, "t", NS_PREFIX);
}
