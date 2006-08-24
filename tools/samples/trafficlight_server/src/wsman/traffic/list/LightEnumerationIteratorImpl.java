package wsman.traffic.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;

import org.publicworks.light.model.ui.TrafficLight;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import wsman.traffic.resource.LightHandlerImpl;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.xml.XmlBinding;

public class LightEnumerationIteratorImpl {
	private static final String WSMAN_TRAFFIC_RESOURCE = "wsman:traffic/resource";
	@SuppressWarnings("unchecked")
	public static List<EnumerationItem> next(XmlBinding binding,ListHandlerEnumerationIterator iterator,DocumentBuilder db, Object context, boolean includeItem, boolean includeEPR, int startPos, int count) {
     	// Create a set of elements to be returned
        // In this case each will be an EPR starting in the
        // list from startPos totalling count items
    	Map<String,TrafficLight> lights=(Map<String,TrafficLight>)context;
        int returnCount = Math.min(count, lights.size() - startPos);
        List<EnumerationItem> items = new ArrayList<EnumerationItem>(returnCount);
        Set<String> keysSet = lights.keySet();
        Object[] keys = keysSet.toArray();
        for (int i = 0; i < returnCount && !iterator.isCancelled(); i++)
        {
            String lightName = keys[startPos + i].toString();
            TrafficLight light = lights.get(lightName);
            Map<String, String> selectors = new HashMap<String, String>();
            selectors.put("name", light.getName());
            try
            {
            	EndpointReferenceType epr = TransferSupport.createEpr(null, WSMAN_TRAFFIC_RESOURCE, selectors);
            	
            	// Make a traffic light type to support epr or element enum
            	Element lightElement = LightHandlerImpl.createLightElement(light,binding);
           
            	EnumerationItem item = new EnumerationItem(lightElement,epr);
                items.add(item);
            }
            catch (JAXBException e)
            {
                throw new InternalErrorFault(e.getMessage());
            }

        }
        return items;
    }
	
	public static int getSize(Object context){
		Map<String,TrafficLight> lights=(Map<String,TrafficLight>)context;
		return lights.size();
	}
	public static boolean hasNext(final Object context, final int startPos){
    	Map<String,TrafficLight> lights=(Map<String,TrafficLight>)context;
       if (startPos >= lights.size())
        {
            return false;
        }
        else
        {
            return true;
        }

	}
}
