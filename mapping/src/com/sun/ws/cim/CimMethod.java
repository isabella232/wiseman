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
 * $Id: CimMethod.java,v 1.1 2006-06-21 00:32:34 akhilarora Exp $
 */

package com.sun.ws.cim;

import com.sun.ws.cim.mapping.Dom;
import com.sun.ws.cim.mapping.Namespaces;
import java.util.Vector;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CimMethod extends CimBase {
    
    private CIMMethod md;
    
    public CimMethod(final CIMMethod m) {
        super();
        md = m;
    }
    
    public int hashCode() {
        return md.getName().hashCode();
    }
    
    public boolean equals(final Object other) {
        if (! (other instanceof CimMethod)) {
            return false;
        }
        final CimMethod om = (CimMethod) other;
        return md.getName().equals(om.md.getName());
    }
    
    public String getName() {
        return md.getName();
    }
    
    public String getType() {
        return mapType(md.getType().getType());
    }
    
    public CimParam[] getParams() {
        final CimParam[] empty = {};
        final Vector params = md.getParameters();
        if (params != null) {
            final CimParam[] ret = new CimParam[params.size()];
            for (int i = 0; i < ret.length; i++) {
                final Object mp = params.get(i);
                if (mp instanceof CIMParameter) {
                    final CIMParameter cp = (CIMParameter) mp;
                    ret[i] = new CimParam(cp);
                }
            }
            return ret;
        }
        return empty;
    }
    
    protected Vector getCimQualifiers() {
        return md.getQualifiers();
    }
    
    protected CIMDataType getCimType() {
        return md.getType();
    }

    protected boolean hasCimQualifier(final String qualifier) {
        return md.hasQualifier(qualifier);
    }
    
    protected CIMQualifier getCimQualifier(final String qualifier) {
        return md.getQualifier(qualifier);
    }
    
    protected CIMValue getCimValue() {
        return null;
    }
    
    public void generate(final Element root, final Document out, final Namespaces ns) {
        final String name = getName();
        
        final Element ie = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, ELEMENT);
        ie.setAttribute(NAME, name + _INPUT);
        root.appendChild(ie);
        
        final Element it = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "complexType");
        ie.appendChild(it);
        
        final CimParam[] ip = getParams();
        if (ip.length > 0) {
            final Element sequence = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "sequence");
            it.appendChild(sequence);
            for (final CimParam param : ip) {
                if (param.isInput()) {
                    param.generate(sequence, out, ns);
                }
            }
        }
        
        final Element oe = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, ELEMENT);
        oe.setAttribute(NAME, name + _OUTPUT);
        root.appendChild(oe);
        
        final Element ot = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "complexType");
        oe.appendChild(ot);
        
        final Element sequence = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, "sequence");
        ot.appendChild(sequence);
        
        final CimParam[] op = getParams();
        for (final CimParam param : op) {
            if (param.isOutput()) {
                param.generate(sequence, out, ns);
            }
        }
        
        final Element re = Dom.createElement(out, ns, Namespaces.XS_NS_PREFIX, ELEMENT);
        re.setAttribute(NAME, "ReturnValue");
        re.setAttribute("type", getType());
        sequence.appendChild(re);
    }

    public void generateInterface(final Element root, final Document wsdl, final Namespaces ns) {
        final String name = getName();
        
        final Element im = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, MESSAGE);
        final String imname = name + "_InputMessage";
        im.setAttribute(NAME, imname);
        root.appendChild(im);

        final Element ip = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, "part");
        ip.setAttribute(NAME, "body");
        final String ipname = name + _INPUT;
        ip.setAttribute(ELEMENT, Namespaces.CLASS_WSDL_NS_PREFIX + Namespaces.COLON + ipname);
        im.appendChild(ip);

        final Element om = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, MESSAGE);
        final String omname = name + "_OutputMessage";
        om.setAttribute(NAME, omname);
        root.appendChild(om);

        final Element op = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, "part");
        op.setAttribute(NAME, "body");
        final String opname = name + _OUTPUT;
        op.setAttribute(ELEMENT, Namespaces.CLASS_WSDL_NS_PREFIX + Namespaces.COLON + opname);
        om.appendChild(op);

        final Element operation = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, "operation");
        operation.setAttribute(NAME, name);
        root.appendChild(operation);

        final Element input = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, "input");
        input.setAttribute(NAME, imname);
        input.setAttribute(MESSAGE, Namespaces.CLASS_WSDL_NS_PREFIX + Namespaces.COLON + imname);
        input.setAttribute(Namespaces.WSA_NS_PREFIX + Namespaces.COLON + "Action", 
                ns.CLASS_XSD_NS_URI + "/" + ipname);
        operation.appendChild(input);

        final Element output = Dom.createElement(wsdl, ns, Namespaces.WSDL_NS_PREFIX, "output");
        output.setAttribute(NAME, omname);
        output.setAttribute(MESSAGE, Namespaces.CLASS_WSDL_NS_PREFIX + Namespaces.COLON + omname);
        output.setAttribute(Namespaces.WSA_NS_PREFIX + Namespaces.COLON + "Action", 
                ns.CLASS_XSD_NS_URI + "/" + opname);
        operation.appendChild(output);
    }

    public void generateQualifiers(final Element element, final Document out, final Namespaces ns) {
        element.setAttribute("type", "method");
        for (final CimQualifier qual : getQualifiers()) {
            qual.generateQualifiers(element, out, ns);
        }

        for (final CimParam param : getParams()) {
            param.generateQualifiers(element, out, ns);
        }
    }
}
