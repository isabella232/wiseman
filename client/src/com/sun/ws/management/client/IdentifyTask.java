package com.sun.ws.management.client;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.impl.ServerIdentityImpl;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.transport.HttpClient;

public class IdentifyTask implements Runnable {
	String destination;
	public ServerIdentityImpl servIdent;
	public Exception e;
	public IdentifyTask(String destination) {
		this.destination=destination;
	}
	public void run() {
			Identify identify;
			try {
				identify = new Identify();
		        identify.setIdentify();
		        final Addressing response = HttpClient.sendRequest(identify.getMessage(), destination);
		        servIdent = new ServerIdentityImpl(response.getBody().extractContentAsDocument());				
			} catch (Exception err) {
				e=err;
			} 
		}			
	
}

