package com.sun.ws.management.framework.eventing;

import java.util.Observer;

public interface EventingObserver extends Observer {
	/**
	 * Use this method to initalize this observer by hooking it into your model.
	 *
	 */
	public void init();
}
