
package com.sun.ws.management.server.handler.wsman.traffic;

import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.Management;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.ws.management.InternalErrorFault;

import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import wsman.traffic.list.ListHandler;

/**
 * This Handler deligates to the wsman.traffic.list.ListHandler class.
 * There is typically nothing to implement in this class.
 *
 * @GENERATED
 */
public class list_Handler extends DelegatingHandler
{
    //Log for logging messages
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(list_Handler.class.getName());

    private static ListHandler delegate;
    static
    {
        delegate = new ListHandler();
    }

    public list_Handler()
    {
        super(delegate = new ListHandler());
    } 

   /**
    * Overridden handle operation to support the custom operation name mapping
    * to wsa:Action uri for SPEC Action URIs
    */
    @Override
    public void handle(String action, String resourceURI, HandlerContext context, Management request, Management response) throws Exception
    {
        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerateResponse");
            delegate.EnumerateOp(context,new Enumeration(request), new Enumeration(response));   
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Release".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/ReleaseResponse");
            delegate.ReleaseOp(context,new Enumeration(request), new Enumeration(response));   
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/PullResponse");
            delegate.PullOp(context,new Enumeration(request), new Enumeration(response));   
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatus".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatusResponse");
            delegate.GetStatusOp(context,new Enumeration(request), new Enumeration(response));   
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Renew".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/RenewResponse");
            delegate.RenewOp(context,new Enumeration(request), new Enumeration(response));   
            return;
        }

        super.handle(action, resourceURI, context, request, response);//be sure to call to super to ensure all operations are handled.
    }

    /**
     * Overridden method to handle custom operations and custom Action URIs
     */
    public boolean customDispatch(String action,HandlerContext context, Management request, Management response) throws Exception
    {
        return false;
    }
}
