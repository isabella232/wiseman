
package com.hp.itil._2007._05.servicetype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ServiceTypeMetadataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceTypeMetadataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ExpectedDurationTime" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="AverageDurationTime" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="TotalNumberOfInstances" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="DiscoveryDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceTypeMetadataType", propOrder = {
    "expectedDurationTime",
    "averageDurationTime",
    "totalNumberOfInstances",
    "discoveryDate"
})
public class ServiceTypeMetadataType {

    @XmlElement(name = "ExpectedDurationTime", defaultValue = "0.0")
    protected double expectedDurationTime;
    @XmlElement(name = "AverageDurationTime", defaultValue = "0.0")
    protected double averageDurationTime;
    @XmlElement(name = "TotalNumberOfInstances", defaultValue = "0")
    protected long totalNumberOfInstances;
    @XmlElement(name = "DiscoveryDate", required = true)
    protected XMLGregorianCalendar discoveryDate;

    /**
     * Gets the value of the expectedDurationTime property.
     * 
     */
    public double getExpectedDurationTime() {
        return expectedDurationTime;
    }

    /**
     * Sets the value of the expectedDurationTime property.
     * 
     */
    public void setExpectedDurationTime(double value) {
        this.expectedDurationTime = value;
    }

    /**
     * Gets the value of the averageDurationTime property.
     * 
     */
    public double getAverageDurationTime() {
        return averageDurationTime;
    }

    /**
     * Sets the value of the averageDurationTime property.
     * 
     */
    public void setAverageDurationTime(double value) {
        this.averageDurationTime = value;
    }

    /**
     * Gets the value of the totalNumberOfInstances property.
     * 
     */
    public long getTotalNumberOfInstances() {
        return totalNumberOfInstances;
    }

    /**
     * Sets the value of the totalNumberOfInstances property.
     * 
     */
    public void setTotalNumberOfInstances(long value) {
        this.totalNumberOfInstances = value;
    }

    /**
     * Gets the value of the discoveryDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDiscoveryDate() {
        return discoveryDate;
    }

    /**
     * Sets the value of the discoveryDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDiscoveryDate(XMLGregorianCalendar value) {
        this.discoveryDate = value;
    }

}
