package my.test;

import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.client.Stub;
import org.apache.xmlbeans.XmlObject;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateDocument;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponseDocument;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullDocument;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponseDocument;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateDocument.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullDocument.Pull;
import org.xmlsoap.schemas.ws._2004._09.transfer.ResourceCreatedDocument;

import net.java.dev.wiseman.schemas.traffic._1.light_xsd.LightServiceStub;
import net.java.dev.wiseman.schemas.traffic._1.light_xsd.TrafficLightType;
import net.java.dev.wiseman.schemas.traffic._1.light_xsd.TrafficlightDocument;

public class TestAxisClient {

	public static String WSMAN_ADDR = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";
	public static String WSMAN_PREFIX = "wsman";

	protected static final String UUID_SCHEME = "uuid:";

	public TestAxisClient() {
		super();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
        try{
        	LightServiceStub stub = new LightServiceStub("http://localhost:7777/traffic/");
  	
			addCreateHeaders(stub);   
         	
        	// Create light
        	TrafficlightDocument newLightDoc = TrafficlightDocument.Factory.newInstance();
        	
        	TrafficLightType newLight= newLightDoc.addNewTrafficlight();
        	newLight.setName("Test1");
        	newLight.setX(100);
        	newLight.setY(200);
        	newLight.setColor("green");
        	
        	ResourceCreatedDocument respDoc = stub.Create(newLightDoc);
        	
         	addGetHeaders(stub, "Test1");
			
         	// Get the newly created traffic light
        	TrafficlightDocument light = stub.Get();
        	
        	System.out.println(light.getTrafficlight().getName());
        	
        	// Enumerate all of the traffic lights
        	addEnumerateHeaders(stub);
        	EnumerateDocument enumParam = EnumerateDocument.Factory.newInstance();
        	Enumerate enumVal = enumParam.addNewEnumerate();
        	
        	OMFactory fact = OMAbstractFactory.getOMFactory();
        	OMElement mode = fact.createOMElement("EnumerationMode", WSMAN_ADDR, WSMAN_PREFIX);
        	mode.setText("EnumerateEPR");
        	
        	XmlObject type = XmlObject.Factory.parse(mode.toString());
        	enumVal.set(type);
        	
         	EnumerateResponseDocument enumresp = stub.EnumerateOp(enumParam);
         	
         	addPullHeaders(stub);
         	         	        	
         	PullDocument pullDoc = PullDocument.Factory.newInstance();
         	
         	Pull pullVal = pullDoc.addNewPull();
         	
         	pullVal.setEnumerationContext(enumresp.getEnumerateResponse().getEnumerationContext());
			PullResponseDocument pullRet = stub.PullOp(pullDoc);

        } catch(Exception e){
            e.printStackTrace();
            System.out.println("\n\n\n");
        }
		

	}

	protected static void addCreateHeaders(Stub stub) throws DatatypeConfigurationException {

    	stub._getServiceClient().removeHeaders();
    	
		stub._getServiceClient().addHeader(WsmanAxisUtils.createResourceUriHeader("urn:resources.wiseman.dev.java.net/traffic/1/light"));
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createToHeader("http://localhost:8080/traffic/"));
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createCreateActionHeader());        	
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createMessageIDHeader(UUID_SCHEME + UUID.randomUUID().toString()));   
		
		Duration timeout = DatatypeFactory.newInstance().newDuration(30000);
		stub._getServiceClient().addHeader(WsmanAxisUtils.createOperationTimeoutHeader(timeout.toString()));
		
    	stub._getServiceClient().addHeader(WsmanAxisUtils.createReplyToHeader("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));  		

	}

	protected static void addGetHeaders(Stub stub, String lightName) throws DatatypeConfigurationException {

    	stub._getServiceClient().removeHeaders();
    	
		stub._getServiceClient().addHeader(WsmanAxisUtils.createResourceUriHeader("urn:resources.wiseman.dev.java.net/traffic/1/light"));
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createToHeader("http://localhost:8080/traffic/"));
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createGetActionHeader());        	
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createMessageIDHeader(UUID_SCHEME + UUID.randomUUID().toString()));   
		
		Duration timeout = DatatypeFactory.newInstance().newDuration(30000);
		stub._getServiceClient().addHeader(WsmanAxisUtils.createOperationTimeoutHeader(timeout.toString()));
		
    	stub._getServiceClient().addHeader(WsmanAxisUtils.createReplyToHeader("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));  		

    	stub._getServiceClient().addHeader(WsmanAxisUtils.createSelectorSetHeader("name", lightName));
	}

	protected static void addEnumerateHeaders(Stub stub) throws DatatypeConfigurationException {

    	stub._getServiceClient().removeHeaders();
    	
		stub._getServiceClient().addHeader(WsmanAxisUtils.createResourceUriHeader("urn:resources.wiseman.dev.java.net/traffic/1/light"));
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createToHeader("http://localhost:8080/traffic/"));
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createEnumerateActionHeader());        	
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createMessageIDHeader(UUID_SCHEME + UUID.randomUUID().toString()));   
		
		Duration timeout = DatatypeFactory.newInstance().newDuration(30000);
		stub._getServiceClient().addHeader(WsmanAxisUtils.createOperationTimeoutHeader(timeout.toString()));
		
    	stub._getServiceClient().addHeader(WsmanAxisUtils.createReplyToHeader("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));  		
	}
	
	protected static void addPullHeaders(Stub stub) throws DatatypeConfigurationException {

    	stub._getServiceClient().removeHeaders();
    	
		stub._getServiceClient().addHeader(WsmanAxisUtils.createResourceUriHeader("urn:resources.wiseman.dev.java.net/traffic/1/light"));
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createToHeader("http://localhost:8080/traffic/"));
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createPullActionHeader());        	
		
		stub._getServiceClient().addHeader(WsmanAxisUtils.createMessageIDHeader(UUID_SCHEME + UUID.randomUUID().toString()));   
		
		Duration timeout = DatatypeFactory.newInstance().newDuration(30000);
		stub._getServiceClient().addHeader(WsmanAxisUtils.createOperationTimeoutHeader(timeout.toString()));
		
    	stub._getServiceClient().addHeader(WsmanAxisUtils.createReplyToHeader("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));  		
       	OMFactory fact = OMAbstractFactory.getOMFactory();

	
	}


}
