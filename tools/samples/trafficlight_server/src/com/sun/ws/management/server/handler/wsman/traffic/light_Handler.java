
package com.sun.ws.management.server.handler.wsman.traffic;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import wsman.traffic.light.LightHandler;

import com.sun.ws.management.Management;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.server.HandlerContext;

/**
 * This Handler deligates to the wsman.traffic.light.LightHandler class.
 * There is typically nothing to implement in this class.
 *
 * @GENERATED
 */
public class light_Handler extends DelegatingHandler
{
    //Log for logging messages
    private Logger m_log = Logger.getLogger(light_Handler.class.getName());

    private static LightHandler m_delegate;
    static
    {
        m_delegate = new LightHandler();
    }

    public light_Handler()
    {
        super(m_delegate);
    } 

   /**
    * Overridden handle operation to support the custom operation name mapping
    * to wsa:Action uri for SPEC Action URIs
    */
    @Override
    public void handle(String action, String resourceURI, HandlerContext context, Management request, Management response) throws Exception
    {
        if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Get".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse");
            m_delegate.Get(resourceURI, request, response);     
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Put".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/PutResponse");
            m_delegate.Put(resourceURI, request, response);     
            return;
        }

        if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/DeleteResponse");
            m_delegate.Delete(resourceURI, request, response);     
            return;
        }

        super.handle(action, resourceURI, context,request, response);//be sure to call to super to ensure all operations are handled.
    }

    /**
     * Overridden method to handle custom operations and custom Action URIs
     */
    public boolean customDispatch(String action, String resourceURI, Management request, Management response) throws Exception
    {
        if ("http://wsman.test/schema/CustomOpResponse".equals(action))
        {
            response.setAction("http://wsman.test/schema/CustomOp");
            m_delegate.TerminateOp(resourceURI, request, response);
            return true;
        }

        return false;
    }
}
