/*
 * WSEventingResponse.java
 *
 * Created on October 16, 2007, 11:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.ws.management.server.message;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

/**
 *
 * @author jfdenise
 */
public interface WSEventingResponse extends SOAPResponse{
    public void setIdentifier(String id)throws JAXBException, SOAPException;
    public void setSubscribeResponse(final EndpointReferenceType mgr, final String expires,
            final Object context)
            throws SOAPException, JAXBException;
    public void setSubscribeResponseExt(final EndpointReferenceType mgr, final String expires,
            final Object... extensions)
            throws SOAPException, JAXBException;
    public void setSubscriptionManagerEpr(EndpointReferenceType mgr) throws JAXBException, SOAPException;
}
