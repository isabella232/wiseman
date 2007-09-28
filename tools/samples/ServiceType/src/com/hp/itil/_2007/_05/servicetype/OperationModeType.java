
package com.hp.itil._2007._05.servicetype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OperationModeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="OperationModeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="StateBased"/>
 *     &lt;enumeration value="TaskBased"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "OperationModeType")
@XmlEnum
public enum OperationModeType {

    @XmlEnumValue("StateBased")
    STATE_BASED("StateBased"),
    @XmlEnumValue("TaskBased")
    TASK_BASED("TaskBased");
    private final String value;

    OperationModeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OperationModeType fromValue(String v) {
        for (OperationModeType c: OperationModeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
