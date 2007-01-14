package wsman.traffic.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.publicworks.light.model.TrafficLightModel;
import org.publicworks.light.model.ui.TrafficLight;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import wsman.traffic.resource.LightHandlerImpl;

import com.sun.traffic.light.types.TrafficLightType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.server.EnumerationItem;

public class LightEnumerationIteratorImpl {
	private static final String WSMAN_TRAFFIC_RESOURCE = "wsman:traffic/resource";

	private int length;
	private final String address;
	private final boolean includeEPR;

	private Iterator<TrafficLight> lights;

	public LightEnumerationIteratorImpl(String address, boolean includeEPR) {
		this.address = address;
		this.includeEPR = includeEPR;

		Collection<TrafficLight> coll = TrafficLightModel.getModel().getList()
				.values();
		this.length = coll.size();
		this.lights = coll.iterator();
	}

	@SuppressWarnings("unchecked")
	public EnumerationItem next() {

		TrafficLight light = lights.next();
		Map<String, String> selectors = new HashMap<String, String>();
		selectors.put("name", light.getName());
		try {
			final EndpointReferenceType epr;

			if (includeEPR == true) {
				epr = TransferSupport.createEpr(address,
						WSMAN_TRAFFIC_RESOURCE, selectors);
			} else {
				epr = null;
			}

			// Make a traffic light type to support epr or element enum
			// Element lightElement =
			// LightHandlerImpl.createLightElement(light,binding);
			JAXBElement<TrafficLightType> lightElement = LightHandlerImpl
					.createLight(light);
			return new EnumerationItem(lightElement, epr);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}

	public int estimateTotalItems() {
		return length;
	}

	public boolean hasNext() {
		return lights.hasNext();
	}

	public void release() {
		length = 0;
		lights = new ArrayList<TrafficLight>().iterator();
	}
}
