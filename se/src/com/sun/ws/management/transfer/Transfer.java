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
 * $Id: Transfer.java,v 1.1 2005-06-29 19:18:26 akhilarora Exp $
 */

package com.sun.ws.management.transfer;

import com.sun.ws.management.addressing.Addressing;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

public class Transfer extends Addressing {
    
    public static final String NS_PREFIX = "wxf";
    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer";
    
    public static final String GET_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Get";
    public static final String GET_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse";
    
    public static final String PUT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Put";
    public static final String PUT_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer/PutResponse";
    
    public static final String DELETE_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete";
    public static final String DELETE_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer/DeleteResponse";
    
    public static final String CREATE_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Create";
    public static final String CREATE_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse";

    public static final QName INVALID_REPRESENTATION = new QName(NS_URI, "InvalidRepresentation", NS_PREFIX);
    public static final String INVALID_REPRESENTATION_REASON = 
        "The XML content was invalid.";

    public Transfer() throws SOAPException, JAXBException {
        super();
        init();
    }
        
    public Transfer(final Addressing addr) throws SOAPException, JAXBException {
        super(addr);
        init();
    }
    
    public Transfer(final InputStream is) throws SOAPException, JAXBException, IOException {
        super(is);
        init();
    }
    
    private void init() throws SOAPException, JAXBException {
    }
}
