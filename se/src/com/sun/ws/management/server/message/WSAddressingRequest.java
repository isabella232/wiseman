/*
 * Copyright 2006-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.  Use is subject to license terms.
 * 
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 */

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
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.ws.api.message.Header;

/**
 *
 * @author jfdenise
 */
public interface WSAddressingRequest extends SOAPRequest {
     public URI getAddressURI() throws SOAPException, JAXBException, URISyntaxException;
     public URI getActionURI() throws SOAPException, JAXBException, URISyntaxException;
     public Object getSOAPHeader(final QName name) throws SOAPException;
     public List<Header> getSOAPHeaders() throws SOAPException;
     public Object getPayload(final Unmarshaller u)
     	throws SOAPException, XMLStreamException, JAXBException;
     
     /**
      * Check to see if this request has been canceled.
      * This indicates to the service to roll back any uncommitted work.
      * 
      * @return boolean indicating if the request has been canceled.
      */
     public boolean isCanceled();
     
     /**
      * Sets this request to canceled state. If the request has already been set
      * to committed an IllegalStateException will be thrown.
      * 
      * @throws IllegalStateException
      */
     public void cancel() throws IllegalStateException;
     
     /**
      * Check to see if this request has been committed.
      * If marked committed the request has been completed by the service.
      * This does not indicate if the response has been sent to the client.
      * 
      * @return boolean indicating if the request has been canceled.
      */
     public boolean isCommitted();
     
     /**
      * Sets this request to committed state. If the request has already been set
      * to canceled an IllegalStateException will be thrown.
      * 
      * @throws IllegalStateException
      */
     public void commit()throws IllegalStateException;
}
