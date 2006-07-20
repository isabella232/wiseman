package com.sun.ws.management.framework.eventing;

import java.io.IOException;
import java.util.Properties;


public class EventingModelManager {
	static private EventingModelManager singleton;
	private EventingModelManager() {
		super();
	}

	public static EventingModelManager getManager() {
		if(singleton==null)
			singleton=new EventingModelManager();
		return singleton;
	}

	/** 
	 * Called to register all model listeners that plan to emit
	 * events
	 */
	public void init() {
		
		
	}
	
	public static void main(String[] args) {
		EventingModelManager manager = getManager();
		//manager.getClass().get
		Properties p = new Properties();
		try {
			p.load(manager.getClass().getResourceAsStream("/WsManListeners.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String res=p.getProperty("listener");
		System.out.println(res);
	}
	
	
}
