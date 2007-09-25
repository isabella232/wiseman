
package com.hp.itil._2007._05.servicetype;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceTypeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceTypeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.hp.com/itil/2007/05/ServiceType}Type"/>
 *         &lt;element ref="{http://www.hp.com/itil/2007/05/ServiceType}Version"/>
 *         &lt;element ref="{http://www.hp.com/itil/2007/05/ServiceType}FormalParameterList" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.hp.com/itil/2007/05/ServiceType}ServiceTypeACL" minOccurs="0"/>
 *         &lt;element ref="{http://www.hp.com/itil/2007/05/ServiceType}ServiceDefinition"/>
 *         &lt;element ref="{http://www.hp.com/itil/2007/05/ServiceType}RollbackPolicy" minOccurs="0"/>
 *         &lt;element ref="{http://www.hp.com/itil/2007/05/ServiceType}ServiceTypeMetadata"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceTypeType", propOrder = {
    "type",
    "version",
    "formalParameterList",
    "serviceTypeACL",
    "serviceDefinition",
    "rollbackPolicy",
    "serviceTypeMetadata"
})
public class ServiceTypeType {

    @XmlElement(name = "Type", namespace = "http://www.hp.com/itil/2007/05/ServiceType", required = true)
    protected OperationModeType type;
    @XmlElement(name = "Version", namespace = "http://www.hp.com/itil/2007/05/ServiceType", required = true)
    protected String version;
    @XmlElement(name = "FormalParameterList", namespace = "http://www.hp.com/itil/2007/05/ServiceType")
    protected List<FormalParameterType> formalParameterList;
    @XmlElement(name = "ServiceTypeACL", namespace = "http://www.hp.com/itil/2007/05/ServiceType")
    protected ServiceTypeACLType serviceTypeACL;
    @XmlElement(name = "ServiceDefinition", namespace = "http://www.hp.com/itil/2007/05/ServiceType", required = true)
    protected Object serviceDefinition;
    @XmlElement(name = "RollbackPolicy", namespace = "http://www.hp.com/itil/2007/05/ServiceType")
    protected RollbackPolicyType rollbackPolicy;
    @XmlElement(name = "ServiceTypeMetadata", namespace = "http://www.hp.com/itil/2007/05/ServiceType", required = true)
    protected ServiceTypeMetadataType serviceTypeMetadata;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link OperationModeType }
     *     
     */
    public OperationModeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperationModeType }
     *     
     */
    public void setType(OperationModeType value) {
        this.type = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the formalParameterList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formalParameterList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormalParameterList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FormalParameterType }
     * 
     * 
     */
    public List<FormalParameterType> getFormalParameterList() {
        if (formalParameterList == null) {
            formalParameterList = new ArrayList<FormalParameterType>();
        }
        return this.formalParameterList;
    }

    /**
     * Gets the value of the serviceTypeACL property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceTypeACLType }
     *     
     */
    public ServiceTypeACLType getServiceTypeACL() {
        return serviceTypeACL;
    }

    /**
     * Sets the value of the serviceTypeACL property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceTypeACLType }
     *     
     */
    public void setServiceTypeACL(ServiceTypeACLType value) {
        this.serviceTypeACL = value;
    }

    /**
     * Gets the value of the serviceDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getServiceDefinition() {
        return serviceDefinition;
    }

    /**
     * Sets the value of the serviceDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setServiceDefinition(Object value) {
        this.serviceDefinition = value;
    }

    /**
     * Gets the value of the rollbackPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link RollbackPolicyType }
     *     
     */
    public RollbackPolicyType getRollbackPolicy() {
        return rollbackPolicy;
    }

    /**
     * Sets the value of the rollbackPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link RollbackPolicyType }
     *     
     */
    public void setRollbackPolicy(RollbackPolicyType value) {
        this.rollbackPolicy = value;
    }

    /**
     * Gets the value of the serviceTypeMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceTypeMetadataType }
     *     
     */
    public ServiceTypeMetadataType getServiceTypeMetadata() {
        return serviceTypeMetadata;
    }

    /**
     * Sets the value of the serviceTypeMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceTypeMetadataType }
     *     
     */
    public void setServiceTypeMetadata(ServiceTypeMetadataType value) {
        this.serviceTypeMetadata = value;
    }

}
