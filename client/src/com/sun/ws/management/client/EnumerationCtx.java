package com.sun.ws.management.client;

public class EnumerationCtx {
	private String context;
		public EnumerationCtx(String contextAsString){
			context=contextAsString;
		}
	@Override
	public String toString(){
		return context;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	
}
