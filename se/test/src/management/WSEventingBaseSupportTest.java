package management;

import java.io.IOException;
import java.util.GregorianCalendar;

import management.SOAPTest;

import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.server.WSEventingBaseSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import com.sun.ws.management.server.Filter;
import javax.xml.datatype.Duration;

import org.xmlsoap.schemas.ws._2004._08.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferenceParametersType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ReferencePropertiesType;
import org.xmlsoap.schemas.ws._2004._08.addressing.ServiceNameType;

import  com.sun.ws.management.addressing.Addressing;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import util.TestBase;
import com.sun.ws.management.server.BaseSupport;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.soap.FaultException;

import com.sun.ws.management.server.EventingContextWithAck;

public class WSEventingBaseSupportTest extends TestBase {
	 protected static final String NS_URI = "http://schemas.InStech.com/model";
	    protected static final String NS_PREFIX = "model";
	public WSEventingBaseSupportTest(final String testName) {
        super(testName);
    }
  
  
 


	
		

	public void testCreateEventMessagePushWithAck() throws SOAPException {

		


		 final String recvrAddress = "http://localhost:8080/events";
		 final EndpointReferenceType notifyTo = Addressing.createEndpointReference(recvrAddress, null, null, null, null);
		 
		 final Addressing request = new Addressing();
		 
		 final ReferencePropertiesType props= Addressing.FACTORY.createReferencePropertiesType();
		 final Document tempDoc1 = request.newDocument();
	        final Element tempelement = tempDoc1.createElementNS(NS_URI, NS_PREFIX + ":" + "tempelement");
	        tempelement.appendChild(tempDoc1.createTextNode("tempelement"));
	        tempDoc1.appendChild(tempelement);
	        props.getAny().add(tempDoc1.getDocumentElement());
	        
	        final ReferenceParametersType params = Addressing.FACTORY.createReferenceParametersType();
	        final Document refParametersDoc = request.newDocument();
	        final Element refParameters = refParametersDoc.createElementNS(NS_URI, NS_PREFIX + ":" + "refParameters");
	        refParameters.setAttributeNS(NS_URI, NS_PREFIX + ":" + "type", "celsius");
	        refParametersDoc.appendChild(refParameters);
	        params.getAny().add(refParametersDoc.getDocumentElement());
	        
	        final AttributedQName portType = Addressing.FACTORY.createAttributedQName();
	        final QName port = new QName(NS_URI, "thePortType", NS_PREFIX);
	        portType.setValue(port);
	        
	        final ServiceNameType serviceName = Addressing.FACTORY.createServiceNameType();
	        final String portName = "thePortName";
	        serviceName.setPortName(portName);
	        final QName service = new QName(NS_URI, "theServiceName", NS_PREFIX);
	        serviceName.setValue(service);
		 
		 EndpointReferenceType eventReplyTo = Addressing.createEndpointReference(Addressing.NS_URI,props,params,portType,serviceName);
		
		
		 try{
			 EventingContextWithAck ctx = new EventingContextWithAck ( null ,null,
				                                              notifyTo,null,eventReplyTo,DatatypeFactory.newInstance().newDuration(5 * 1000));

			 final Document tempDoc = Management.newDocument();
		     final Element temp = tempDoc.createElementNS(NS_URI, NS_PREFIX + ":" + "temp");
		     temp.appendChild(tempDoc.createTextNode("temp"));
		     tempDoc.appendChild(temp);
			 Addressing addr = WSEventingBaseSupport.createEventMessagePushWithAck(ctx , temp);
			 
		     assertEquals(eventReplyTo.getAddress().getValue(),addr.getReplyTo().getAddress().getValue());
		     assertEquals(eventReplyTo.getReferenceProperties().getAny().toString(),addr.getReplyTo().getReferenceProperties().getAny().toString());
		     assertEquals(eventReplyTo.getReferenceParameters().getAny().toString(),addr.getReplyTo().getReferenceParameters().getAny().toString());
		     assertEquals(eventReplyTo.getPortType().getValue(),addr.getReplyTo().getPortType().getValue());
		     assertEquals(eventReplyTo.getPortType().getOtherAttributes(),addr.getReplyTo().getPortType().getOtherAttributes());
		     assertEquals(eventReplyTo.getServiceName().getValue(),addr.getReplyTo().getServiceName().getValue());
		     assertEquals(eventReplyTo.getServiceName().getPortName(),addr.getReplyTo().getServiceName().getPortName());
		     assertEquals(eventReplyTo.getServiceName().getOtherAttributes(),addr.getReplyTo().getServiceName().getOtherAttributes());
		    
		     assertEquals(DatatypeFactory.newInstance().newDuration(5 * 1000).toString(),addr.getHeader().getElementsByTagName("wsman:OperationTimeout").item(0).getTextContent());
		     
		     final QName ACK_Requested = new QName(Management.NS_URI,WSEventingBaseSupport.ACKREQUESTED,Management.NS_PREFIX);
			 assertTrue(addr.getHeader().getChildElements(ACK_Requested).hasNext());
			 
		     System.out.println(addr);     
		    
		 }
		 catch(Exception e){
			 e.printStackTrace();
		 } 
		 


	}

}
