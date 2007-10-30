/*
 * WSAddressingRequest.java
 *
 * Created on October 18, 2007, 11:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.ws.management.server.message;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

/**
 *
 * @author jfdenise
 */
public interface WSAddressingRequest extends SOAPRequest {
     public URI getAddressURI() throws SOAPException, JAXBException, URISyntaxException;
     public URI getActionURI() throws SOAPException, JAXBException, URISyntaxException;
}
