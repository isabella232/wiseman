/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: light_Handler.java,v 1.2 2007-05-30 20:30:30 nbeers Exp $
 */

package com.sun.ws.management.server.handler.net.java.dev.wiseman.resources.traffic._1;

import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.Management;

import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import net.java.dev.wiseman.resources.traffic._1.light.LightHandler;

/**
 * This Handler delegates to the net.java.dev.wiseman.resources.traffic._1.light.LightHandler class.
 * There is typically nothing to implement in this class.
 *
 * @GENERATED
 */
public class light_Handler extends DelegatingHandler
{
    // Log for logging messages
    @SuppressWarnings("unused")
    private final Logger log;

    private static final LightHandler delegate = new LightHandler();
    
    /**
     * Handler constructor.
     */
    public light_Handler() {
        super(delegate);    
        log = Logger.getLogger(light_Handler.class.getName());
    } 

    /**
     * Overridden handle operation to support the custom operation name mapping
     * to wsa:Action uri for SPEC Action URIs
     */
    @Override
    public void handle(String action, String resourceURI, HandlerContext context,
                       Management request, Management response) throws Exception {
        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerateResponse");
            delegate.EnumerateOp(context, new Enumeration(request), new Enumeration(response));   
            return;
        }
        if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Create".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse");
            delegate.Create(context, request, response);     
            return;
        }
        if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Get".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse");
            delegate.Get(context, request, response);     
            return;
        }
        if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Put".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/PutResponse");
            delegate.Put(context, request, response);     
            return;
        }
        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Release".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/ReleaseResponse");
            delegate.ReleaseOp(context, new Enumeration(request), new Enumeration(response));   
            return;
        }
        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/PullResponse");
            delegate.PullOp(context, new Enumeration(request), new Enumeration(response));   
            return;
        }
        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatus".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatusResponse");
            delegate.GetStatusOp(context, new Enumeration(request), new Enumeration(response));   
            return;
        }
        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Renew".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/RenewResponse");
            delegate.RenewOp(context, new Enumeration(request), new Enumeration(response));   
            return;
        }
        if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete".equals(action)) {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/DeleteResponse");
            delegate.Delete(context, request, response);     
            return;
        }
        // be sure to call to super to ensure all operations are handled.
        super.handle(action, resourceURI, context, request, response);
    }
}