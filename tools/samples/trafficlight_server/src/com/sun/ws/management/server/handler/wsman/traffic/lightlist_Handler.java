
package com.sun.ws.management.server.handler.wsman.traffic;

import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.Management;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import wsman.traffic.lightlist.LightlistHandler;

/**
 * This Handler deligates to the wsman.traffic.lightlist.LightlistHandler class.
 * There is typically nothing to implement in this class.
 *
 * @GENERATED
 */
public class lightlist_Handler extends DelegatingHandler
{
    //Log for logging messages
    private Logger m_log = Logger.getLogger(lightlist_Handler.class.getName());

    private static LightlistHandler m_delegate;
    static
    {
        m_delegate = new LightlistHandler();
    }

    public lightlist_Handler()
    {
        super(m_delegate);
    } 

   /**
    * Overridden handle operation to support the custom operation name mapping
    * to wsa:Action uri for SPEC Action URIs
    */
    @Override
	public void handle(String action, String resourceURI,HandlerContext context, Management request, Management response) throws Exception {
    
        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerateResponse");
            m_delegate.EnumerateOp(new Enumeration(request), new Enumeration(response));   
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Release".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/ReleaseResponse");
            m_delegate.ReleaseOp(new Enumeration(request), new Enumeration(response));   
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/PullResponse");
            m_delegate.PullOp(new Enumeration(request), new Enumeration(response));   
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatus".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatusResponse");
            m_delegate.GetStatusOp(new Enumeration(request), new Enumeration(response));   
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Renew".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/enumeration/RenewResponse");
            m_delegate.RenewOp(new Enumeration(request), new Enumeration(response));   
            return;
        }

        super.handle(action, resourceURI, context, request, response);//be sure to call to super to ensure all operations are handled.
    }

}
