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
 * $Id: timeout_Handler.java,v 1.4 2007-03-02 16:12:29 denis_rachal Exp $
 */

package com.sun.ws.management.server.handler.wsman.test;

import java.util.GregorianCalendar;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.TimedOutFault;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;

public class timeout_Handler implements Handler {
    
    public void handle(final String action, final String resource,
            final HandlerContext context,
            final Management request, final Management response) throws Exception {

        // while (true) {
        //     Thread.currentThread().sleep(Long.MAX_VALUE);
        // }
    	
    	if (request.getTimeout() == null) {
    		throw new InternalErrorFault("Missing expected wsman:OperationTimeout");
    	}
    	long timeout = request.getTimeout().getTimeInMillis(new GregorianCalendar());
    	Thread.currentThread().sleep(timeout);
    	throw new TimedOutFault();
    }
}
