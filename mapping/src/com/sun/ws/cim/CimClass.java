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
 * $Id: CimClass.java,v 1.1 2006-06-21 00:32:34 akhilarora Exp $
 */

package com.sun.ws.cim;

import com.sun.ws.cim.mapping.Namespaces;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CimClass extends CimBase {
    
    private final CIMClass cl;
    
    public CimClass(final CIMClass c) {
        super();
        cl = c;
    }
    
    public int hashCode() {
        return cl.getName().hashCode();
    }
    
    public boolean equals(final Object other) {
        if (!(other instanceof CimClass)) {
            return false;
        }
        final CimClass oc = (CimClass) other;
        return cl.getName().equals(oc.cl.getName());
    }
    
    public List<CimProperty> getProperties() {
        final Vector properties = (Vector) cl.getAllProperties();
        final List<CimProperty> ret = new ArrayList<CimProperty>(properties.size());
        final Iterator pi = properties.iterator();
        while (pi.hasNext()) {
            final Object obj = pi.next();
            if (obj instanceof CIMProperty) {
                ret.add(new CimProperty((CIMProperty) obj));
            }
        }
        return ret;
    }
    
    public List<CimMethod> getMethods() {
        final Vector methods = (Vector) cl.getAllMethods();
        final List<CimMethod> ret = new ArrayList<CimMethod>(methods.size());
        final Iterator mi = methods.iterator();
        while (mi.hasNext()) {
            final Object obj = mi.next();
            if (obj instanceof CIMMethod) {
                ret.add(new CimMethod((CIMMethod) obj));
            }
        }
        return ret;
    }
    
    public void generateQualifiers(final Element element, final Document out, final Namespaces ns) {
        element.setAttribute("type", "class");
        for (final CimQualifier qual : getQualifiers()) {
            qual.generateQualifiers(element, out, ns);
        }
    }
    
    public String getName() {
        return cl.getName();
    }

    protected Vector getCimQualifiers() {
        return cl.getQualifiers();
    }

    protected CIMDataType getCimType() {
        return CIMDataType.getPredefinedType(CIMDataType.INVALID);
    }

    protected boolean hasCimQualifier(final String qualifier) {
        return cl.hasQualifier(qualifier);
    }
    
    protected CIMQualifier getCimQualifier(final String qualifier) {
        return cl.getQualifier(qualifier);
    }
    
    protected CIMValue getCimValue() {
        return null;
    }
}
