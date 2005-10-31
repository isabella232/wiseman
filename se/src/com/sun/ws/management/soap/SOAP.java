/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: SOAP.java,v 1.2 2005-10-31 21:23:53 akhilarora Exp $
 */

package com.sun.ws.management.soap;

import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.java.JavaException;
import com.sun.ws.management.xml.XML;
import com.sun.ws.management.xml.XmlBinding;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import org.w3._2003._05.soap_envelope.Detail;
import org.w3._2003._05.soap_envelope.Fault;
import org.w3._2003._05.soap_envelope.Faultcode;
import org.w3._2003._05.soap_envelope.Faultreason;
import org.w3._2003._05.soap_envelope.ObjectFactory;
import org.w3._2003._05.soap_envelope.Reasontext;
import org.w3._2003._05.soap_envelope.Subcode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SOAP extends Message {
    
    public static final String NS_PREFIX = "env";
    public static final String NS_URI = "http://www.w3.org/2003/05/soap-envelope";
    
    public static final QName MUST_UNDERSTAND = new QName(NS_URI, "mustUnderstand", NS_PREFIX);
    public static final QName TEXT = new QName(NS_URI, "Text", NS_PREFIX);
    public static final QName FAULT = new QName(NS_URI, "Fault", NS_PREFIX);
    public static final QName BODY = new QName(NS_URI, "Body", NS_PREFIX);
    public static final QName SENDER = new QName(NS_URI, "Sender", NS_PREFIX);
    public static final QName RECEIVER = new QName(NS_URI, "Receiver", NS_PREFIX);
    
    public static final String TRUE = "true";
    
    private static XmlBinding binding = null;
    
    private ObjectFactory objectFactory = null;
    
    public SOAP() throws SOAPException, JAXBException {
        super();
        init();
    }
    
    public SOAP(final SOAP soap) throws SOAPException, JAXBException {
        super(soap);
        init();
    }
    
    public SOAP(final InputStream is) throws SOAPException, JAXBException, IOException {
        super(is);
        init();
    }
    
    private void init() throws SOAPException, JAXBException {
        objectFactory = new ObjectFactory();
        // use a default if none has been set so far
        if (binding == null) {
            binding = new XmlBinding();
        }
    }
    
    public static void setXmlBinding(final XmlBinding bind) {
        binding = bind;
    }
    
    public XmlBinding getXmlBinding() {
        return binding;
    }
    
    public void validate() throws SOAPException, JAXBException, FaultException {
    }
    
    public SOAPHeaderElement[] getAllMustUnderstand() throws SOAPException {
        final SOAPElement[] headers = getChildren(getHeader());
        final List<SOAPElement> ml = new ArrayList<SOAPElement>(headers.length);
        for (final SOAPElement hdr : headers) {
            if (hdr instanceof SOAPHeaderElement) {
                final SOAPHeaderElement she = (SOAPHeaderElement) hdr;
                final String mu = she.getAttributeValue(MUST_UNDERSTAND);
                if (TRUE.equalsIgnoreCase(mu)) {
                    ml.add(she);
                }
            }
        }
        return (SOAPHeaderElement[]) ml.toArray(new SOAPHeaderElement[ml.size()]);
    }
    
    protected SOAPElement[] getChildren(final SOAPElement parent) throws SOAPException {
        return getChildren(parent, null);
    }
    
    protected SOAPElement[] getChildren(final SOAPElement parent, final QName qname) throws SOAPException {
        final List<SOAPElement> al = new ArrayList<SOAPElement>();
        final Iterator<SOAPElement> ei;
        if (qname == null) {
            ei = parent.getChildElements();
        } else {
            ei = parent.getChildElements(qname);
        }
        while (ei.hasNext()) {
            al.add(ei.next());
        }
        return (SOAPElement[]) al.toArray(new SOAPElement[al.size()]);
    }
    
    protected Object unbind(final SOAPElement parent, final QName qname) throws JAXBException, SOAPException {
        final SOAPElement[] elements = getChildren(parent, qname);
        if (elements.length == 0) {
            return null;
        }
        return binding.unmarshal(elements[0]);
    }
    
    protected void removeChildren(final SOAPElement parent, final QName qname) throws SOAPException {
        SOAPElement[] elements = getChildren(parent, qname);
        for (final SOAPElement se : elements) {
            se.detachNode();
        }
    }
    
    public static Node[] createFaultDetail(final String text,
            final String faultDetail, final Throwable source,
            final QName wrapper, final Object... values) {
        
        final List<Node> nodeList = new ArrayList<Node>();
        final Document doc = newDocument();
        if (text != null) {
            final Element textElement = createElement(doc, SOAP.TEXT);
            setAttribute(textElement, XML.LANG, XML.DEFAULT_LANG);
            textElement.setTextContent(text);
            nodeList.add(textElement);
        }
        
        if (faultDetail != null) {
            final Element faultDetailElement = createElement(doc, Management.FAULT_DETAIL);
            faultDetailElement.setTextContent(faultDetail);
            nodeList.add(faultDetailElement);
        }
        
        if (wrapper != null) {
            if (values != null) {
                for (final Object value : values) {
                    final Element valueElement = createElement(doc, Management.FAULT_DETAIL);
                    valueElement.setTextContent(value.toString());
                    
                    final Element wrapperElement = createElement(doc, wrapper);
                    wrapperElement.appendChild(valueElement);
                    nodeList.add(wrapperElement);
                }
            }
        }
        
        if (source != null) {
            final Element exceptionElement = createElement(doc, JavaException.EXCEPTION);
            exceptionElement.setTextContent(source.toString());
            
            final Throwable cause = source.getCause();
            if (cause != null) {
                final Element causeElement = createElement(doc, JavaException.CAUSE);
                causeElement.setTextContent(cause.toString());
                exceptionElement.appendChild(causeElement);
                // TODO: add the cause's stacktrace?
            }
            
            final Element element = createElement(doc, JavaException.STACK_TRACE);
            exceptionElement.appendChild(element);
            for (final StackTraceElement st : source.getStackTrace()) {
                final Element ste = createElement(doc, JavaException.STACK_TRACE_ELEMENT);
                // return a compact stack trace with just the file names
                // (with the .java suffix removed) and line numbers
                ste.setTextContent(st.getFileName().replaceAll("\\.java$", COLON) + st.getLineNumber());
                element.appendChild(ste);
            }
            nodeList.add(exceptionElement);
        }
        
        return nodeList.toArray(new Node[nodeList.size()]);
    }
    
    public void setFault(final FaultException ex) throws JAXBException, SOAPException {
        setFault(ex.getCode(), ex.getSubcode(), ex.getReason(), ex.getDetails());
    }
    
    private void setFault(final QName code, final QName subcode, final String reason,
            final Node... details) throws JAXBException, SOAPException {
        
        removeChildren(getBody(), FAULT);

        final Fault fault = objectFactory.createFault();

        final Faultcode faultcode = objectFactory.createFaultcode();
        faultcode.setValue(code);
        fault.setCode(faultcode);
        
        if (subcode != null) {
            final Subcode faultsubcode = objectFactory.createSubcode();
            faultsubcode.setValue(subcode);
            faultcode.setSubcode(faultsubcode);
        }
        
        final Reasontext reasontext = objectFactory.createReasontext();
        reasontext.setValue(reason);
        reasontext.setLang(XML.DEFAULT_LANG);        
        final Faultreason faultreason = objectFactory.createFaultreason();
        faultreason.getText().add(reasontext);
        fault.setReason(faultreason);
        
        final Detail faultdetail = objectFactory.createDetail();
        final List<Object> detailsList = faultdetail.getAny();
        if (details != null) {
            for (final Node detail : details) {
                detailsList.add(detail);
            }
            fault.setDetail(faultdetail);
        }
        
        final JAXBElement<Fault> faultElement = objectFactory.createFault(fault);
        getXmlBinding().marshal(faultElement, getBody());
    }
    
    public Fault getFault() throws JAXBException, SOAPException {
        Object value = unbind(getBody(), FAULT);
        return value == null ? null : ((JAXBElement<Fault>) value).getValue();
    }
}
