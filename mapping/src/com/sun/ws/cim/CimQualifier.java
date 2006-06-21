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
 * $Id: CimQualifier.java,v 1.1 2006-06-21 00:32:35 akhilarora Exp $
 */

package com.sun.ws.cim;

import com.sun.ws.cim.mapping.Dom;
import com.sun.ws.cim.mapping.Namespaces;
import java.util.Vector;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CimQualifier extends CimBase {
    
    private final CIMQualifier cq;
    
    public CimQualifier(final CIMQualifier q) {
        super();
        cq = q;
    }
    
    public int hashCode() {
        return cq.getName().hashCode();
    }
    
    public boolean equals(final Object other) {
        if (!(other instanceof CimProperty)) {
            return false;
        }
        final CimQualifier oq = (CimQualifier) other;
        return cq.getName().equals(oq.cq.getName());
    }
    
    public String getName() {
        return cq.getName();
    }
    
    public void generateQualifiers(final Element base, final Document out, final Namespaces ns) {
        final String name = getName();
        if ("ValueMap".equals(name) || "Values".equals(name)) {
            int index = 1;
            for (final String value : getValues()) {
                generateQualifierElement(base, out, ns, name, value).setAttribute("index", Integer.toString(index++));
            }
        } else {
            generateQualifierElement(base, out, ns, name, getValue());
        }
    }
    
    private Element generateQualifierElement(final Element base, final Document out, final Namespaces ns, 
            final String name, final String value) {
        final Element element = Dom.createElement(out, ns, Namespaces.CIMQ_NS_PREFIX, name);
        element.setAttribute("qualifier", "true");
        element.setTextContent(value);
        base.appendChild(element);
        return element;
    }
    
    protected Vector getCimQualifiers() {
        return EMPTY_VECTOR;
    }
    
    protected CIMDataType getCimType() {
        return null;
    }
    
    protected boolean hasCimQualifier(final String qualifier) {
        return false;
    }
    
    protected CIMQualifier getCimQualifier(final String qualifier) {
        return null;
    }
    
    protected CIMValue getCimValue() {
        return cq.getValue();
    }
}
