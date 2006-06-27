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
 * $Id: base_Handler.java,v 1.2 2006-06-27 19:53:01 akhilarora Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.Management;
import javax.servlet.http.HttpServletRequest;

public abstract class base_Handler implements Handler {
    
    public void handle(final String action, final String resource,
            final HttpServletRequest httpRequest,
            final Management request, final Management response) throws Exception {
        throw new InternalErrorFault("abstract handler invoked");
    }
}
