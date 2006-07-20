package com.sun.ws.management.framework.eventing;

import javax.servlet.ServletException;

import com.sun.ws.management.server.WSManServlet;

public class EventingWSManServlet extends WSManServlet {

	public EventingWSManServlet() {
		super();
	}

	@Override
	public void init() throws ServletException {
		super.init();
		EventingModelManager.getManager().init();
	}

}
