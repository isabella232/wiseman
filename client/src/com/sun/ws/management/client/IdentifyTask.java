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

