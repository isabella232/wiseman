/*
 * Copyright 2006 Sun Microsystems, Inc.
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
 * $Id: EnumerationHandler.java,v 1.5 2006-08-24 13:57:28 obiwan314 Exp $
 *
 */
package com.sun.ws.management.framework.enumeration;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.addressing.ActionNotSupportedFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.NamespaceMap;

/**
 *
 */
public abstract class EnumerationHandler extends TransferSupport implements Enumeratable
{
    /**
     * namespaces A map of namespace prefixes to namespace URIs used in
     * items to be enumerated. The prefix is the key and the URI is the value in the Map.
     * The namespaces map is used during filter evaluation.
     */
    private NamespaceMap namespaces;

    /**
     * The implemented Iterator which can traverse the "dataset"
     */
    public EnumerationIterator enumIterator = null;

    /**
     * The "dataset" which the Enumeration will traverse
     */
    private Object clientContext = null;

    //private static final QName GETSTATUS = new QName(Enumeration.NS_URI,"GetStatus",Enumeration.NS_PREFIX);
    private static final Logger LOG = Logger.getLogger(EnumerationHandler.class.getName());

    protected EnumerationHandler(EnumerationIterator enumIterator)
    {
        this.enumIterator = enumIterator;
    }

    public void release(Enumeration enuRequest, Enumeration enuResponse)
    {
        try
        {
            EnumerationSupport.release(enuRequest,enuResponse);
        }
        catch (SOAPException e)
        {
            LOG.log(Level.SEVERE, "",e);
            throw new InternalErrorFault();
        }
        catch (JAXBException e)
        {
            LOG.log(Level.SEVERE, "",e);
            throw new InternalErrorFault();
        }
    }

    public void pull(Enumeration enuRequest, Enumeration enuResponse)
    {
        try
        {
            EnumerationSupport.pull(enuRequest,enuResponse);
        }
        catch (SOAPException e)
        {
            LOG.log(Level.SEVERE, "",e);
            throw new InternalErrorFault();
        }
        catch (JAXBException e)
        {
            LOG.log(Level.SEVERE, "",e);
            throw new InternalErrorFault();
        }
    }

    private EnumerationModeType getEnumerateModeType( Enumeration enuRequest ) throws JAXBException, SOAPException {
       JAXBElement enumerateModeType = null;
       List<Object> enuModeList = null;

       enuModeList = enuRequest.getEnumerate().getAny();

       if(enuModeList.size() > 0) {
          enumerateModeType = (JAXBElement) enuModeList.get(0);
       }

       return (EnumerationModeType) (enumerateModeType != null ? enumerateModeType.getValue() : null);
    }

    public void enumerateObjects(Enumeration enuRequest, Enumeration enuResponse){
    }

    public void enumerateEprs(Enumeration enuRequest, Enumeration enuResponse) {
       throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.INVALID_VALUES);
    }

    public void enumerateObjectsAndEprs(Enumeration enuRequest, Enumeration enuResponse) {
       throw new UnsupportedFeatureFault(UnsupportedFeatureFault.Detail.INVALID_VALUES);
    }

    public void enumerate(Enumeration enuRequest, Enumeration enuResponse) {
       EnumerationModeType enuMode = null;

       try{
          enuMode = getEnumerateModeType( enuRequest );
       }
       catch( JAXBException e ){
          LOG.log(Level.SEVERE, "",e);
          throw new InternalErrorFault();
       }
       catch( SOAPException e ){
          LOG.log(Level.SEVERE, "",e);
          throw new InternalErrorFault();
       }

       if(enuMode == EnumerationModeType.ENUMERATE_EPR) {
          enumerateEprs(enuRequest, enuResponse);
       }
       else if(enuMode == EnumerationModeType.ENUMERATE_OBJECT_AND_EPR) {
          enumerateObjectsAndEprs(enuRequest, enuResponse);
       }
       else {
          enumerateObjects(enuRequest, enuResponse);
       }

       try
       {
          EnumerationSupport.enumerate(enuRequest,enuResponse, enumIterator,
                                       clientContext, namespaces);
       }
       catch (DatatypeConfigurationException e)
       {
          LOG.log(Level.SEVERE, "",e);
          throw new InternalErrorFault();
       }
       catch (SOAPException e)
       {
          LOG.log(Level.SEVERE, "",e);
          throw new InternalErrorFault();
       }
       catch (JAXBException e)
       {
          LOG.log(Level.SEVERE, "",e);
          throw new InternalErrorFault();
       }
    }


    public void getStatus(Enumeration enuRequest, Enumeration enuResponse)
    {
        throw new ActionNotSupportedFault();
        //GetStatusResponse getStatusResponse = Enumeration.FACTORY.createGetStatusResponse();
        //GetStatus getStatusRequest = getGetStatusRequest(enuRequest);
        //getStatusRequest.getEnumerationContext()

        //com.sun.ws.management.server.EnumerationSupport.
        //getStatusResponse.setExpires();
        //Enumeration.FACTORY.
        //todo need a hook into their BaseSupport...BaseSupport is package scoped and EnumerationSupport is final
        //todo may need to make a class in the same package to access to some of their methods and the context_map
    }

    /*private GetStatus getGetStatusRequest(Enumeration enuRequest)
    {
        try
        {
            HpSOAP soap = new HpSOAP(enuRequest);
            final Object value = soap.unbind(soap.getBody(), GETSTATUS);
            return value == null ? null : (GetStatus) value;    //todo does this work????
        }
        catch (SOAPException e)
        {
            LOG.log(Level.SEVERE, "",e);
            throw new InternalErrorFault();
        }
        catch (JAXBException e)
        {
            LOG.log(Level.SEVERE, "",e);
            throw new InternalErrorFault();
        }
    }*/

    public void renew(Enumeration enuRequest, Enumeration enuResponse)
    {
        throw new ActionNotSupportedFault();
    }
    
    public void setNamespaces(Map<String, String> namespaces)
    {
    	if(namespaces!=null)
    		this.namespaces = new NamespaceMap(namespaces);
    }

    public void setClientContext(Object clientContext)
    {
        this.clientContext = clientContext;
    }
    
 
	
}