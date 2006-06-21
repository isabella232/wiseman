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
 * $Id: CimParam.java,v 1.1 2006-06-21 00:32:34 akhilarora Exp $
 */

package com.sun.ws.cim;

import com.sun.ws.cim.mapping.Dom;
import com.sun.ws.cim.mapping.Namespaces;
import java.util.Vector;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CimParam extends CimBase {
    
    private final CIMParameter cp;
    
    public CimParam(final CIMParameter p) {
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
        final CimParam op = (CimParam) other;
        return cp.getName().equals(op.cp.getName());
    }
    
    public String getName() {
        return cp.getName();
    }
    
    public boolean isInput() {
        return hasCimQualifier("IN");
    }
    
    public boolean isOutput() {
        return hasCimQualifier("OUT");
    }
    
    public void generate(final Element sequence, final Document out, final Namespaces ns) {
        final String name = getName();
        final String type = getType();
        
        final Element element = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "element");
        element.setAttribute("name", name);
        element.setAttribute("type", type);
        if (!isRequired()) {
            element.setAttribute("nillable", "true");
        }
        sequence.appendChild(element);
    }
    
    public void generateQualifiers(final Element base, final Document out, final Namespaces ns) {
        final Element element = Dom.createElement(out, ns, Namespaces.CIMQ_NS_PREFIX, getName());
        element.setAttribute("type", "param");
        for (final CimQualifier qual : getQualifiers()) {
            qual.generateQualifiers(element, out, ns);
        }
        base.appendChild(element);
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
        return null;
    }
}
