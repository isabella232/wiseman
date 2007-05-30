/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: IdentifyTask.java,v 1.3 2007-05-30 20:30:21 nbeers Exp $
 */
package com.sun.ws.management.client;

import java.util.Map.Entry;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.impl.ServerIdentityImpl;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.transport.HttpClient;

public class IdentifyTask implements Runnable {
	String destination;
	public ServerIdentityImpl servIdent;
	public Exception e;
	private Entry<String, String>[] headers;
	public IdentifyTask(String destination,  final Entry<String, String>... headers) {
		this.destination=destination;
		this.headers=headers;
	}
	public void run() {
			Identify identify;
			try {
				identify = new Identify();
		        identify.setIdentify();
		        final Addressing response = HttpClient.sendRequest(identify.getMessage(),destination,headers);
		        servIdent = new ServerIdentityImpl(response.getBody().extractContentAsDocument());				
			} catch (Exception err) {
				e=err;
			} 
		}			
	
}

