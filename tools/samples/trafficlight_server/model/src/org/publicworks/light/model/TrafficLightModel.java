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
		// TODO Auto-generated constructor stub
	}
	
	public static TrafficLightModel getModel(){
		if(singleton==null)
			singleton=new TrafficLightModel();
		return singleton;
	}
	
	public TrafficLight create(){
		TrafficLight tl = new TrafficLight();

        lights.put(tl.getName(),tl);
        tl.setVisible(true);
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
