
package net.java.dev.wiseman.schemas.traffic._1.light;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.java.dev.wiseman.schemas.traffic._1.light package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Trafficlight_QNAME = new QName("http://schemas.wiseman.dev.java.net/traffic/1/light.xsd", "trafficlight");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.java.dev.wiseman.schemas.traffic._1.light
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TrafficLightType }
     * 
     */
    public TrafficLightType createTrafficLightType() {
        return new TrafficLightType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrafficLightType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.wiseman.dev.java.net/traffic/1/light.xsd", name = "trafficlight")
    public JAXBElement<TrafficLightType> createTrafficlight(TrafficLightType value) {
        return new JAXBElement<TrafficLightType>(_Trafficlight_QNAME, TrafficLightType.class, null, value);
    }

}