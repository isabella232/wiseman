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
 * $Id: ReflectiveRequestDispatcher.java,v 1.2 2005-08-10 21:52:56 akhilarora Exp $
 */

package com.sun.ws.management.server;

import com.sun.ws.management.AccessDeniedFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.DestinationUnreachableFault;
import com.sun.ws.management.soap.FaultException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

public final class ReflectiveRequestDispatcher extends RequestDispatcher {
    
    private static final Logger LOG = Logger.getLogger(ReflectiveRequestDispatcher.class.getName());
    private static final Class[] HANDLER_PARAMS = { String.class, String.class, Management.class, Management.class };
    private static final String HANDLER_PREFIX = ReflectiveRequestDispatcher.class.getPackage().getName() + ".handler";
    
    public ReflectiveRequestDispatcher(final Management req) throws JAXBException, SOAPException {
        super(req);
    }
    
    public void dispatch() throws JAXBException, SOAPException,
            InstantiationException, IllegalAccessException,
            FaultException, Throwable {
        
        final String action = request.getAction();
        final String resource = request.getResourceURI();
        if (resource == null) {
            throw new DestinationUnreachableFault(
                    "Missing the " + Management.RESOURCE_URI.getLocalPart(),
                    Management.INVALID_RESOURCE_URI_DETAIL);
        }
        
        final String handlerClassName = createHandlerClassName(resource);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, resource + " . " + action + " -> " + handlerClassName);
        }
        
        final Class handlerClass;
        try {
            handlerClass = Class.forName(handlerClassName);
        } catch (ClassNotFoundException cnfex) {
            throw new DestinationUnreachableFault(
                    "Handler not found for resource " + resource,
                    Management.INVALID_RESOURCE_URI_DETAIL);
        }
        
        // TODO: verify that handlerClass implements the Handler interface
        final Method method;
        try {
            method = handlerClass.getDeclaredMethod("handle", HANDLER_PARAMS);
        } catch (NoSuchMethodException nsmex) {
            throw new DestinationUnreachableFault(
                    "handle method not found in Handler " +
                    handlerClassName + " for resource " + resource,
                    Management.INVALID_RESOURCE_URI_DETAIL);
        }
        
        final Object handler;
        try {
            handler = handlerClass.newInstance();
        } catch (InstantiationException iex) {
            throw new DestinationUnreachableFault(
                    "Could not instantiate handler " +
                    handlerClassName + " for resource " + resource,
                    Management.INVALID_RESOURCE_URI_DETAIL);
        } catch (IllegalAccessException iaex) {
            throw new AccessDeniedFault();
        }
        
        try {
            method.invoke(handler, action, resource, request, response);
        } catch (InvocationTargetException itex) {
            // the cause might be FaultException if a Fault is being indicated by the handler
            throw itex.getCause();
        }
    }
    
    private String createHandlerClassName(final String resource) {
        // map URI schemes to "."
        String className = resource.replaceAll(":/*", ".");
        
        className = className.replaceAll("/", ".");
        
        // map special characters to underscores
        className = className.replaceAll("[,;\\$&+=?#\\[\\]]", "_");
        
        // prefix illegal package names (those that start with a number,
        // for example) and reserved words with an underscore
        final String[] component = className.split("\\.");
        final StringBuilder sb = new StringBuilder();
        for (int i=0; i < component.length; i++) {
            if (component[i].matches("^\\d*") ||
                    // TODO: handle other reserved words
                    component[i].equals("this")) {
                sb.append("_");
            }
            sb.append(component[i]);
            if (i < component.length - 1) {
                sb.append(".");
            }
        }
        
        sb.append("_Handler");
        return HANDLER_PREFIX == null ? sb.toString() : HANDLER_PREFIX + "." + sb.toString();
    }
}
