package com.sun.ws.management.server.handler.wsman.traffic;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import wsman.traffic.lightfactory.LightfactoryHandler;

import com.sun.ws.management.Management;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.server.HandlerContext;

/**
 * This Handler deligates to the wsman.traffic.lightfactory.LightfactoryHandler
 * class. There is typically nothing to implement in this class.
 * 
 * @GENERATED
 */
public class lightfactory_Handler extends DelegatingHandler {
	// Log for logging messages
	private Logger m_log = Logger.getLogger(lightfactory_Handler.class
			.getName());

	private static LightfactoryHandler m_delegate;
	static {
		m_delegate = new LightfactoryHandler();
	}

	public lightfactory_Handler() {
		super(m_delegate);
	}

	/**
	 * Overridden handle operation to support the custom operation name mapping
	 * to wsa:Action uri for SPEC Action URIs
	 */
	@Override
	public void handle(String action, String resourceURI,HandlerContext context, Management request, Management response) throws Exception {
		if ("http://schemas.xmlsoap.org/ws/2004/09/transfer/Create".equals(action)) {
			response.setAction("http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse");
			m_delegate.Create(resourceURI, request, response);
			return;
		}

		super.handle(action, resourceURI, context, request, response);
		// be sure to call to super to ensure all operations are handled.
	}

}
