//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-06/22/2005 01:29 PM(ryans)-EA2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2005.10.27 at 11:49:42 AM PDT 
//


package org.xmlsoap.schemas.ws._2005._06.wsmancat;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.xmlsoap.schemas.ws._2005._06.wsmancat.SchemaRefType;
import org.xmlsoap.schemas.ws._2005._06.wsmancat.SimpleRefType;


/**
 * Java class for OperationType complex type.
 *  <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="OperationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Action" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded"/>
 *         &lt;element name="SelectorSetRef" type="{http://schemas.xmlsoap.org/ws/2005/06/wsmancat}SimpleRefType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="OptionSetRef" type="{http://schemas.xmlsoap.org/ws/2005/06/wsmancat}SimpleRefType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SchemaRef" type="{http://schemas.xmlsoap.org/ws/2005/06/wsmancat}SchemaRefType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="FilterDialect" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="DeliveryMode" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="WsdlLocation" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="WsdlPort" type="{http://www.w3.org/2001/XMLSchema}token" />
 *       &lt;attribute name="WsdlRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
@XmlAccessorType(AccessType.FIELD)
@XmlType(name = "OperationType")
public class OperationType {

    @XmlElement(name = "Action", namespace = "http://schemas.xmlsoap.org/ws/2005/06/wsmancat")
    protected List<String> action;
    @XmlElement(name = "SelectorSetRef", namespace = "http://schemas.xmlsoap.org/ws/2005/06/wsmancat")
    protected List<SimpleRefType> selectorSetRef;
    @XmlElement(name = "OptionSetRef", namespace = "http://schemas.xmlsoap.org/ws/2005/06/wsmancat")
    protected List<SimpleRefType> optionSetRef;
    @XmlElement(name = "SchemaRef", namespace = "http://schemas.xmlsoap.org/ws/2005/06/wsmancat")
    protected List<SchemaRefType> schemaRef;
    @XmlElement(name = "FilterDialect", namespace = "http://schemas.xmlsoap.org/ws/2005/06/wsmancat")
    protected List<String> filterDialect;
    @XmlElement(name = "DeliveryMode", namespace = "http://schemas.xmlsoap.org/ws/2005/06/wsmancat")
    protected List<String> deliveryMode;
    @XmlAttribute(name = "WsdlLocation")
    protected String wsdlLocation;
    @XmlAttribute(name = "WsdlPort")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String wsdlPort;
    @XmlAttribute(name = "WsdlRef")
    protected String wsdlRef;

    protected List<String> _getAction() {
        if (action == null) {
            action = new ArrayList<String>();
        }
        return action;
    }

    /**
     * Gets the value of the action property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the action property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAction() {
        return this._getAction();
    }

    protected List<SimpleRefType> _getSelectorSetRef() {
        if (selectorSetRef == null) {
            selectorSetRef = new ArrayList<SimpleRefType>();
        }
        return selectorSetRef;
    }

    /**
     * Gets the value of the selectorSetRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selectorSetRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelectorSetRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SimpleRefType }
     * 
     * 
     */
    public List<SimpleRefType> getSelectorSetRef() {
        return this._getSelectorSetRef();
    }

    protected List<SimpleRefType> _getOptionSetRef() {
        if (optionSetRef == null) {
            optionSetRef = new ArrayList<SimpleRefType>();
        }
        return optionSetRef;
    }

    /**
     * Gets the value of the optionSetRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the optionSetRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOptionSetRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SimpleRefType }
     * 
     * 
     */
    public List<SimpleRefType> getOptionSetRef() {
        return this._getOptionSetRef();
    }

    protected List<SchemaRefType> _getSchemaRef() {
        if (schemaRef == null) {
            schemaRef = new ArrayList<SchemaRefType>();
        }
        return schemaRef;
    }

    /**
     * Gets the value of the schemaRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schemaRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchemaRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SchemaRefType }
     * 
     * 
     */
    public List<SchemaRefType> getSchemaRef() {
        return this._getSchemaRef();
    }

    protected List<String> _getFilterDialect() {
        if (filterDialect == null) {
            filterDialect = new ArrayList<String>();
        }
        return filterDialect;
    }

    /**
     * Gets the value of the filterDialect property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filterDialect property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilterDialect().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFilterDialect() {
        return this._getFilterDialect();
    }

    protected List<String> _getDeliveryMode() {
        if (deliveryMode == null) {
            deliveryMode = new ArrayList<String>();
        }
        return deliveryMode;
    }

    /**
     * Gets the value of the deliveryMode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deliveryMode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeliveryMode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDeliveryMode() {
        return this._getDeliveryMode();
    }

    /**
     * Gets the value of the wsdlLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * Sets the value of the wsdlLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWsdlLocation(String value) {
        this.wsdlLocation = value;
    }

    /**
     * Gets the value of the wsdlPort property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWsdlPort() {
        return wsdlPort;
    }

    /**
     * Sets the value of the wsdlPort property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWsdlPort(String value) {
        this.wsdlPort = value;
    }

    /**
     * Gets the value of the wsdlRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWsdlRef() {
        return wsdlRef;
    }

    /**
     * Sets the value of the wsdlRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWsdlRef(String value) {
        this.wsdlRef = value;
    }

}
