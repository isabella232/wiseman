/*
 * Copyright 2006 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: CimProperty.java,v 1.1 2006-06-21 00:32:34 akhilarora Exp $
 */

package com.sun.ws.cim;

import com.sun.ws.cim.mapping.Dom;
import com.sun.ws.cim.mapping.Namespaces;
import java.util.Vector;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CimProperty extends CimBase {
    
    private final CIMProperty cp;
    
    public CimProperty(final CIMProperty p) {
        super();
        cp = p;
    }
    
    public int hashCode() {
        return cp.getName().hashCode();
    }
    
    public boolean equals(final Object other) {
        if (!(other instanceof CimProperty)) {
            return false;
        }
        final CimProperty op = (CimProperty) other;
        return cp.getName().equals(op.cp.getName());
    }
    
    public String getName() {
        return cp.getName();
    }
    
    protected Vector getCimQualifiers() {
        return cp.getQualifiers();
    }
    
    protected CIMDataType getCimType() {
        return cp.getType();
    }
    
    protected boolean hasCimQualifier(final String qualifier) {
        return cp.hasQualifier(qualifier);
    }
    
    protected CIMQualifier getCimQualifier(final String qualifier) {
        return cp.getQualifier(qualifier);
    }

    protected CIMValue getCimValue() {
        return cp.getValue();
    }
    
    public boolean isKey() {
        return cp.isKey();
    }
    
    public void generate(final Element root, final Document out, final Namespaces ns) {
        final String name = getName();
        final String type = getType();
        final String maxLen = getMaxLen();
        final String minLen = getMinLen();
        final String nameType = name + CimProperty.TYPE;
        
        final Element element = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "element");
        element.setAttribute(NAME, name);
        if (isEmbeddedObject() || isEmbeddedInstance()) {
            element.setAttribute(TYPE, Namespaces.XS_NS_PREFIX + Namespaces.COLON + "anyType");
        } else {
            element.setAttribute(TYPE, type);
        }
        root.appendChild(element);
        
        if (!isKey() && !isRequired()) {
            element.setAttribute("nillable", "true");
        }
        
        String baseType = type;
        if (isArrayType()) {
            final Element complexType = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "complexType");
            complexType.setAttribute(NAME, nameType);
            root.appendChild(complexType);
            
            final Element simpleContent = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "simpleContent");
            complexType.appendChild(simpleContent);
            
            final Element extension = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "extension");
            simpleContent.appendChild(extension);
            
            final Element attribute = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "attribute");
            attribute.setAttribute(NAME, "index");
            attribute.setAttribute(TYPE, Namespaces.XS_NS_PREFIX + Namespaces.COLON + "positiveInteger");
            attribute.setAttribute("use", "required");
            extension.appendChild(attribute);
            
            if (isOctetString()) {
                if (type.equals(Namespaces.CIM_COMMON_NS_PREFIX + Namespaces.COLON + CIM_UNSIGNED_BYTE)) {
                    extension.setAttribute(BASE, Namespaces.CIM_COMMON_NS_PREFIX + Namespaces.COLON + "cimBase64Binary");
                } else if (type.equals(Namespaces.CIM_COMMON_NS_PREFIX + Namespaces.COLON + CIM_STRING)) {
                    extension.setAttribute(BASE, Namespaces.CIM_COMMON_NS_PREFIX + Namespaces.COLON + "cimHexBinary");
                } else {
                    throw new RuntimeException("Cannot handle OctetString of type " + type);
                }
            } else {
                extension.setAttribute(BASE, type);
            }
            
            element.setAttribute(TYPE, Namespaces.CLASS_XSD_NS_PREFIX + Namespaces.COLON + nameType);
            
            baseType = Namespaces.CLASS_XSD_NS_PREFIX + Namespaces.COLON + nameType;
        }
        
        final Element restriction = generateRestriction(element, out, ns, minLen, maxLen, baseType);
        if (restriction != null) {
            for (String valueMap : getValueMap()) {
                final Element enumeration = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "enumeration");
                enumeration.setAttribute(VALUE, valueMap);
                restriction.appendChild(enumeration);
            }
        }
    }
    
    public void generateReference(final Element root, final Document out, final Namespaces ns, final Element sequence) {
        final Element element = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "element");
        element.setAttribute("ref", Namespaces.CLASS_XSD_NS_PREFIX + Namespaces.COLON + getName());
        if (isArrayType()) {
            element.setAttribute("minOccurs", "0");
            // TODO: as of March 21, 2006
            // array size not returned correctly due to a bug in wbemservices 1.0.2
            final int size = getArraySize();
            element.setAttribute("maxOccurs", size < 0 ? "unbounded" : Integer.toString(size));
        }
        sequence.appendChild(element);
    }
    
    public void generateQualifiers(final Element element, final Document out, final Namespaces ns) {
        element.setAttribute(TYPE, "property");
        for (final CimQualifier qual : getQualifiers()) {
            qual.generateQualifiers(element, out, ns);
        }
    }
    
    private Element generateRestriction(final Element element, final Document out,
            final Namespaces ns, final String minLen, final String maxLen,
            final String baseType) throws DOMException {
        
        if (maxLen == null && minLen == null) {
            return null;
        }
        
        element.removeAttribute(TYPE);
        
        final Element restrictionComplexType = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "complexType");
        element.appendChild(restrictionComplexType);
        
        final Element restrictionSimpleContent = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "simpleContent");
        restrictionComplexType.appendChild(restrictionSimpleContent);
        
        final Element restriction = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "restriction");
        restrictionSimpleContent.appendChild(restriction);
        
        if (maxLen != null) {
            final Element maxLength = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "maxLength");
            maxLength.setAttribute(VALUE, maxLen);
            restriction.appendChild(maxLength);
        }
        if (minLen != null) {
            final Element minLength = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "minLength");
            minLength.setAttribute(VALUE, minLen);
            restriction.appendChild(minLength);
        }
        
        restriction.setAttribute(BASE, baseType);
        
        return restriction;
    }
}
