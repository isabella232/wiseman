
package com.hp.itil._2007._05.servicetype;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RollbackPolicyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RollbackPolicyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IsRollbackAllowed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="RollbackRulesList" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RollbackPolicyType", propOrder = {
    "isRollbackAllowed",
    "rollbackRulesList"
})
public class RollbackPolicyType {

    @XmlElement(name = "IsRollbackAllowed")
    protected boolean isRollbackAllowed;
    @XmlElement(name = "RollbackRulesList", required = true)
    protected List<String> rollbackRulesList;

    /**
     * Gets the value of the isRollbackAllowed property.
     * 
     */
    public boolean isIsRollbackAllowed() {
        return isRollbackAllowed;
    }

    /**
     * Sets the value of the isRollbackAllowed property.
     * 
     */
    public void setIsRollbackAllowed(boolean value) {
        this.isRollbackAllowed = value;
    }

    /**
     * Gets the value of the rollbackRulesList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rollbackRulesList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRollbackRulesList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getRollbackRulesList() {
        if (rollbackRulesList == null) {
            rollbackRulesList = new ArrayList<String>();
        }
        return this.rollbackRulesList;
    }

}
