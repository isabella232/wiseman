/*
 * JAXWSAgent.java
 *
 * Created on December 18, 2006, 2:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package server;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.ws.management.server.WSManJAXWSEndpoint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;
import org.w3c.dom.Document;

/**
 *
 * @author jfdenise
 */
public class JAXWSAgent {
    
    @WebServiceProvider()
    @ServiceMode(value=Service.Mode.MESSAGE)
    public static class EventsReceiver implements Provider<SOAPMessage> {
        private static final Logger LOG =
                Logger.getLogger(EventsReceiver.class.getName());
        
        private static final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        private static DocumentBuilder db = null;
        
        static {
            docFactory.setNamespaceAware(true);
            try {
                db = docFactory.newDocumentBuilder();
            } catch (ParserConfigurationException px) {
                throw new RuntimeException(px);
            }
        }
        
        public SOAPMessage invoke(SOAPMessage request) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                request.writeTo(os);
                final ByteArrayInputStream bis =
                        new ByteArrayInputStream(os.toByteArray());
                final Document doc = db.parse(bis);
                final OutputFormat format = new OutputFormat(doc);
                format.setLineWidth(72);
                format.setIndenting(true);
                format.setIndent(2);
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                final XMLSerializer serializer = new XMLSerializer(os, format);
                serializer.serialize(doc);
                final String event = new String(os.toString("utf-8"));
                LOG.info("Got an event: " + event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("WSMan Agent JAX-WS J2SE deployment.");
        
        // Events
        EventsReceiver evtReceiver = new EventsReceiver();
        Endpoint evtedp = Endpoint.publish("http://localhost:8080/events",
                evtReceiver);
        
        // WS-MAN Agent
        String binding = SOAPBinding.SOAP12HTTP_BINDING;
        WSManJAXWSEndpoint wsmanedp = new WSManJAXWSEndpoint();
        //Create Endpoint
        Endpoint endpoint = Endpoint.create(binding, wsmanedp);
        
        endpoint.publish("http://localhost:8080/wsman");
        
        System.out.println("WSMan Agent ready to serve.");
        System.out.println("Type a Key to stop.");
        System.in.read();
        endpoint.stop();
        evtedp.stop();
    }
}
