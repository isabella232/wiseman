package com.sun.ws.management.framework;

import java.util.Set;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;

public class Utilities {
	   public static SelectorType getSelectorByName(String name,Set<SelectorType> selectorSet){
	    	for (SelectorType selectorType : selectorSet) {
	    		if(selectorType.getName().equals(name)){
	    			return selectorType;
	    		}
			}
			return null;
		}

}
