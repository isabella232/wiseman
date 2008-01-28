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
 * $Id: WSHandler.java,v 1.1.2.1 2008-01-28 08:00:44 denis_rachal Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.server.message.WSManagementRequest;
import com.sun.ws.management.server.message.WSManagementResponse;

public interface WSHandler {
    
    void handle(final String action,
    		    final String resource,
                final HandlerContext context,
                final WSManagementRequest request,
                final WSManagementResponse response) throws Exception;
}
