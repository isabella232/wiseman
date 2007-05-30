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
 * $Id: WsManOp.java,v 1.3 2007-05-30 20:30:19 nbeers Exp $
 */
package com.sun.ws.management.tools;

/**
 * An object used to represent an operation for Velocity templating
 * 
 */
public class WsManOp
{
    public String ResponseURI;
    public String OperationName;
    public boolean isEnumMethod = false;
    public boolean isEventMethod = false;
    public String OverriddenOpName;

    public WsManOp(String responseURI, String operationName, boolean enumMethod, boolean eventMethod, String overriddenOpName)
    {
        ResponseURI = responseURI;
        OperationName = operationName;
        isEnumMethod = enumMethod;
        isEventMethod = eventMethod;
        OverriddenOpName = overriddenOpName;
    }

    public WsManOp(String responseURI, String operationName, String overriddenOpName)
    {
        ResponseURI = responseURI;
        OperationName = operationName;
        OverriddenOpName = overriddenOpName;
    }

    public String getResponseURI()
    {
        return ResponseURI;
    }

    public String getOperationName()
    {
        return OperationName;
    }

    public boolean getIsEnumMethod()
    {
        return isEnumMethod;
    }

    public boolean getIsEventMethod()
    {
        return isEventMethod;
    }
    
    public String getOverriddenOpName()
    {
        return OverriddenOpName;
    }

}
