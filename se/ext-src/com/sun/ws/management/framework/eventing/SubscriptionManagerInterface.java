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
 * $Id: SubscriptionManagerInterface.java,v 1.1 2007-05-02 19:35:14 simeonpinder Exp $
 *
 */
package com.sun.ws.management.framework.eventing;

import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.HandlerContext;

/** This interface defines the required methods for a 
 * SubscriptionManagerInterface instance.  A subscription manager
 * must support RENEW, UNSUBSCRIBE and optionally
 * may support SUBSCRIPTIONEND.  The GETSTATUS message is 
 * explicitly NOT RECOMMENDED. 
 * 
 * @author Simeon
 */
public interface SubscriptionManagerInterface {
	String getSubscriptionManagerAddress();
    String getSubscriptionManagerResourceURI();
    void renew(HandlerContext context,Enumeration enuRequest, Enumeration enuResponse);
	void unsubsubscribe(HandlerContext context,Enumeration enuRequest, 
	Enumeration enuResponse);

}
