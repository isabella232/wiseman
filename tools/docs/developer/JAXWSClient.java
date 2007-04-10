package client;

import java.math.BigInteger;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Pull;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;


import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.util.DOMUtil;

import net.java.dev.wiseman.schemas.traffic._1.light.LightResource;
import net.java.dev.wiseman.schemas.traffic._1.light.LightService;
import net.java.dev.wiseman.schemas.traffic._1.light.TrafficLightType;

public class JAXWSClient {

	//@WebServiceRef(wsdlLocation=
	//"file:///C:/hpwsmanserver/samples/trafficlight_server/wsdl/test.wsdl")

	static LightService service; 
	public static String WSMAN_ADDR = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";

	protected static final String UUID_SCHEME = "uuid:";

	public JAXWSClient() {
		super();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			JAXWSClient client = new JAXWSClient();
			client.doTest(args);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void doTest(String[] args) {
		LightResource resource = new LightService().getLightResource();
		WSBindingProvider prv = (WSBindingProvider)resource;
		Duration timeout;
		Document doc = DOMUtil.createDom();


		// Timeout
		try {
			timeout = DatatypeFactory.newInstance().newDuration(30000);
		} catch (DatatypeConfigurationException e1) {
			e1.printStackTrace();
			return;
		}

		try {
			prv.setOutboundHeaders(
					WsmanJaxwsUtils.createResourceUriHeader("urn:resources.wiseman.dev.java.net/traffic/1/light"),
					WsmanJaxwsUtils.createToHeader("http://localhost:8080/traffic/"),
					WsmanJaxwsUtils.createCreateActionHeader(),
					WsmanJaxwsUtils.createMessageIDHeader(UUID_SCHEME + UUID.randomUUID().toString()),
					WsmanJaxwsUtils.createOperationTimeoutHeader(timeout.toString()),
					WsmanJaxwsUtils.createReplyToHeader("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));

			TrafficLightType light = new TrafficLightType();
			light.setColor("yellow");
			light.setName("Fred");


			resource.create(light);
		} catch(Exception e) {
			e.printStackTrace();
		}


		try {

			prv.setOutboundHeaders(
					WsmanJaxwsUtils.createResourceUriHeader("urn:resources.wiseman.dev.java.net/traffic/1/light"),
					WsmanJaxwsUtils.createToHeader("http://localhost:8080/traffic/"),
					WsmanJaxwsUtils.createGetActionHeader(),
					WsmanJaxwsUtils.createMessageIDHeader(UUID_SCHEME + UUID.randomUUID().toString()),
					WsmanJaxwsUtils.createOperationTimeoutHeader(timeout.toString()),
					WsmanJaxwsUtils.createReplyToHeader("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"),
					WsmanJaxwsUtils.createSelectorSetHeader("name", "Fred"));

			TrafficLightType getLight = resource.get();
			System.out.println("color = " + getLight.getColor());

		} catch(Exception e) {
			e.printStackTrace();
		}

		try {
			prv.setOutboundHeaders(
					WsmanJaxwsUtils.createResourceUriHeader("urn:resources.wiseman.dev.java.net/traffic/1/light"),
					WsmanJaxwsUtils.createToHeader("http://localhost:8080/traffic/"),
					WsmanJaxwsUtils.createEnumerateActionHeader(),
					WsmanJaxwsUtils.createMessageIDHeader(UUID_SCHEME + UUID.randomUUID().toString()),
					WsmanJaxwsUtils.createOperationTimeoutHeader(timeout.toString()),
					WsmanJaxwsUtils.createReplyToHeader("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));


			Enumerate enumBody = new Enumerate();
			Element enumMode = doc.createElementNS(WSMAN_ADDR, "wsman:EnumerationMode");
			enumMode.setTextContent("EnumerateEPR");
			enumBody.getAny().add(enumMode);
			EnumerateResponse resp = resource.enumerateOp(enumBody );

			EnumerationContextType ctx = resp.getEnumerationContext();

			prv.setOutboundHeaders(
					WsmanJaxwsUtils.createResourceUriHeader("urn:resources.wiseman.dev.java.net/traffic/1/light"),
					WsmanJaxwsUtils.createToHeader("http://localhost:8080/traffic/"),
					WsmanJaxwsUtils.createPullActionHeader(),
					WsmanJaxwsUtils.createMessageIDHeader(UUID_SCHEME + UUID.randomUUID().toString()),
					WsmanJaxwsUtils.createOperationTimeoutHeader(timeout.toString()),
					WsmanJaxwsUtils.createReplyToHeader("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));

			Pull pullBody = new Pull();

			pullBody.setEnumerationContext(ctx);
			pullBody.setMaxElements(new BigInteger(new Integer(5).toString()));
			PullResponse pullResp = resource.pullOp(pullBody );

			System.out.println(pullResp.getItems().getAny().size());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}




}
