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
 * $Id: InvalidEnumerationContextFault.java,v 1.1 2005-06-29 19:18:20 akhilarora Exp $
 */

package com.sun.ws.management.enumeration;

import com.sun.ws.management.soap.ReceiverFault;
import org.w3c.dom.Node;

public class InvalidEnumerationContextFault extends ReceiverFault {
    
    public InvalidEnumerationContextFault(final Node... details) {
        super(Enumeration.INVALID_ENUM_CONTEXT, Enumeration.INVALID_ENUM_CONTEXT_REASON, details);
    }
}
