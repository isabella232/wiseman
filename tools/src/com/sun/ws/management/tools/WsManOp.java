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
