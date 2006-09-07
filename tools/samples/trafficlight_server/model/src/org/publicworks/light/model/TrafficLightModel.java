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
