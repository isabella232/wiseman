package com.sun.ws.management.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.java.dev.wiseman.server.handler.config._1.ResourceHandlersType;
import net.java.dev.wiseman.server.handler.config._1.ResourceHandlersType.ResourceHandler;

public class RequestDispatcherConfig {
    
    private static final Logger LOG = Logger
            .getLogger(RequestDispatcherConfig.class.getName());
    
    public static final String RESOURCE_HANDLER_CONFIG = "/WEB-INF/resource-handler-config.xml";
    
    private static final String RESOURCE_HANDLER_PACKAGE_NAME = "net.java.dev.wiseman.server.handler.config._1";
    
    private static final ArrayList<HandlerConfig> handlers = new ArrayList<HandlerConfig>();
    
    private static boolean initDone = false;
    
    private static final class HandlerConfig {
        
        private final Pattern resourcePattern;
        
        private final String handler;
        
        HandlerConfig(final Pattern resourcePattern, final String handler) {
            this.resourcePattern = resourcePattern;
            this.handler = handler;
        }
        
        Pattern getResourcePattern() {
            return resourcePattern;
        }
        
        String getHandler() {
            return handler;
        }
    }
    
    RequestDispatcherConfig(HandlerContext context) {
        // Only load the static 'handlers' once
        synchronized (handlers) {
            if (initDone == false) {
                initDone = true;
                try {
                    // Get the ServletConfig
                    ServletContext servletCtx = 
                            (ServletContext) context.
                            getRequestProperties().
                            get(HandlerContext.SERVLET_CONTEXT);
                    // Create a JAXBContext
                    JAXBContext jbc = JAXBContext
                            .newInstance(RESOURCE_HANDLER_PACKAGE_NAME);
                    
                    // Create an Unmarshaller
                    Unmarshaller unmarshaller = jbc.createUnmarshaller();
                    
                    // Get an InputStream for the config file
                    InputStream is = servletCtx
                            .getResourceAsStream(RESOURCE_HANDLER_CONFIG);
                    
                    // Check if InputStream was successfully opened
                    if (is == null) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE,
                                    "WARNING: Failed to load configuration "
                                    + RESOURCE_HANDLER_CONFIG);
                        }
                        return;
                    }
                    
                    // Read in the config file
                    JAXBElement<?> handlersElem = (JAXBElement<?>) unmarshaller
                            .unmarshal(is);
                    
                    ResourceHandlersType rh = (ResourceHandlersType) handlersElem
                            .getValue();
                    List<ResourceHandler> list = rh.getResourceHandler();
                    
                    // Save the list in our private ArrayList
                    for (int index = 0; index < list.size(); index++) {
                        ResourceHandler handlerEntry = (ResourceHandler) list
                                .get(index);
                        Pattern pattern = Pattern.compile(handlerEntry
                                .getResourcePattern());
                        String className = handlerEntry
                                .getResourceHandlerClass();
                        HandlerConfig handler = new HandlerConfig(pattern,
                                className);
                        handlers.add(handler);
                    }
                } catch (JAXBException je) {
                    je.printStackTrace();
                }
            }
        }
    }
    
    public String getHandlerName(String resource) {
        // Search the list for a match. First match is returned.
        String handlerName = null;
        
        // No synchonization necessary here since we only write the ArrayList
        // once
        for (int index = 0; index < handlers.size(); index++) {
            HandlerConfig handler = (HandlerConfig) handlers.get(index);
            Matcher matcher = handler.getResourcePattern().matcher(resource);
            if (matcher.matches() == true) {
                handlerName = handler.getHandler();
                break;
            }
        }
        return handlerName;
    }
}
