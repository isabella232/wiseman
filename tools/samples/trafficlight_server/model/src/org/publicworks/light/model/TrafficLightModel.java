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
 * $Id: TrafficLightModel.java,v 1.3 2007-05-30 20:30:27 nbeers Exp $
 */
package org.publicworks.light.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.publicworks.light.model.ui.TrafficLight;


public class TrafficLightModel extends Observable {
    private static Map<String,TrafficLight> lights=new HashMap<String,TrafficLight>();
    private static TrafficLightModel singleton;
	private TrafficLightModel() {
		super();
	}
	
	public static TrafficLightModel getModel(){
		if(singleton==null)
			singleton=new TrafficLightModel();
		return singleton;
	}
	
	public TrafficLight create(String name){
		TrafficLight tl = new TrafficLight();
        tl.setVisible(true);

		if(name!=null){
			tl.setName(name);
	       lights.put(name,tl);

		} else {
			tl.defaultInit();
		    lights.put(tl.getName(),tl);

		}
 		return tl; 
	};
	public void destroy(String name){
        lights.remove(name).dispose();
	};
	
	public TrafficLight find(String name){
		return lights.get(name);
	};

	public Map<String,TrafficLight> getList(){
		return lights;
	};
}
