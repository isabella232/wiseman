
package com.sun.ws.management.server.handler.wsman.traffic;

import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.Management;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.ws.management.InternalErrorFault;

import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import wsman.traffic.factory.FactoryHandler;

/**
 * This Handler deligates to the wsman.traffic.factory.FactoryHandler class.
 * There is typically nothing to implement in this class.
 *
 * @GENERATED
 */
public class factory_Handler extends DelegatingHandler
{
    //Log for logging messages
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(factory_Handler.class.getName());

    private static FactoryHandler delegate;
    static
    {
        delegate = new FactoryHandler();
    }

    public factory_Handler()
    {
        super(delegate);
    } 

   /**
    * Overridden handle operation to support the custom operation name mapping
    * to wsa:Action uri for SPEC Action URIs
    */
    @Override
    public void handle(String action, String resourceURI, HandlerContext context, Management request, Management response) throws Exception
    {
        if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Create".equals(action))
        {
            response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse");
            delegate.Create(context, request, response);     
            return;
        }

        super.handle(action, resourceURI, context, request, response);//be sure to call to super to ensure all operations are handled.
    }

    /**
     * Overridden method to handle custom operations and custom Action URIs
     */
    public boolean customDispatch(String action, HandlerContext context, Management request, Management response) throws Exception
    {
        return false;
    }
}
