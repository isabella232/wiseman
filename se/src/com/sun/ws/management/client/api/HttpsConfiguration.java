/*
 * Copyright 2006-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.  Use is subject to license terms.
 */
package com.sun.ws.management.client.api;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.remote.ws.JMXWSDefinitions;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * Stores provided SSL Configuration.
 * Compute default if needed.
 */
public abstract class HttpsConfiguration {
    
    private static class WrappedSSLSocketFactory extends SSLSocketFactory {
        private SSLSocketFactory factory;
        private String[] cyphers;
        private String[] protocols;
        public WrappedSSLSocketFactory(SSLSocketFactory factory,
                String[] cyphers,
                String[] protocols) {
            this.factory = factory;
            this.cyphers = cyphers;
            this.protocols = protocols;
        }
        
        private Socket updateSocket(SSLSocket socket) {
            if(cyphers != null)
                socket.setEnabledCipherSuites(cyphers);
            if(protocols != null)
                socket.setEnabledProtocols(protocols);
            return socket;
        }
        public Socket createSocket(Socket s,
                String host,
                int port,
                boolean autoClose)
                throws IOException {
            return updateSocket((SSLSocket) factory.createSocket(s, host, port, autoClose));
        }
        
        @Override
        public Socket createSocket()
        throws IOException {
            return updateSocket((SSLSocket) factory.createSocket());
        }
        
        public Socket createSocket(String host,
                int port)
                throws IOException,
                UnknownHostException {
            return updateSocket((SSLSocket) factory.createSocket(host, port));
        }
        
        public Socket createSocket(String host,
                int port,
                InetAddress localHost,
                int localPort)
                throws IOException,
                UnknownHostException {
            return updateSocket((SSLSocket) factory.createSocket(host, port, localHost, localPort));
        }
        
        public Socket createSocket(InetAddress host,
                int port)
                throws IOException {
            return updateSocket((SSLSocket) factory.createSocket(host, port));
        }
        
        public Socket createSocket(InetAddress address,
                int port,
                InetAddress localAddress,
                int localPort)
                throws IOException {
            return updateSocket((SSLSocket) factory.createSocket(address, port, localAddress, localPort));
        }
        
        public String[] getDefaultCipherSuites() {
            return factory.getDefaultCipherSuites();
        }
        
        public String[] getSupportedCipherSuites() {
            return factory.getSupportedCipherSuites();
        }
    }
    
    
    private static final Logger logger =
            Logger.getLogger("javax.management.remote.ws");
    
    protected SSLContext sslContext;
    protected SSLContext pushSSLContext;
    protected Boolean want;
    protected Boolean need;
    protected String[] protocols;
    protected String[] cipherSuites;
    protected String[] pushProtocols;
    protected String[] pushCipherSuites;
    private boolean https;
    protected HttpsConfiguration(Map<String, ?> env, 
            URL url) {
        
        https = url.getProtocol().equalsIgnoreCase("https");
        
        sslContext = (SSLContext) WSEnvHelp.failGet(env,
                JMXWSDefinitions.JMX_WS_HTTP_SSL_CONTEXT);
        
        if(!https) {
           if(sslContext != null)
               throw new IllegalArgumentException("SSLContext provided for " +
                        "non https protocol");
           else
               return;
        }

        pushSSLContext = (SSLContext) WSEnvHelp.failGet(env,
                JMXWSDefinitions.JMX_WS_HTTP_SSL_CONTEXT_PUSH_NOTIFICATION);
        
        String protoList = (String) WSEnvHelp.failGet(env,
                JMXWSDefinitions.JMX_WS_TLS_ENABLED_PROTOCOLS);
        
        if(logger.isLoggable(Level.FINER))
            logger.finer("Protocols in env Map: " +  protoList);
        
        protocols = constructArray(protoList);
        
        String cypherList = (String) WSEnvHelp.failGet(env,
                JMXWSDefinitions.JMX_WS_TLS_ENABLED_CIPHER_SUITES);
        
        if(logger.isLoggable(Level.FINER))
            logger.finer("Cypher suite in env Map: " +  cypherList);
        cipherSuites = constructArray(cypherList);
        
        String wantStr = (String) WSEnvHelp.failGet(env,
                JMXWSDefinitions.JMX_WS_TLS_WANT_CLIENT_AUTHENTICATION);
        if(logger.isLoggable(Level.FINER))
            logger.finer("Want in env Map: " +  wantStr);
        
        want = constructBoolean(wantStr);
        
        String needStr = (String) WSEnvHelp.failGet(env,
                JMXWSDefinitions.JMX_WS_TLS_NEED_CLIENT_AUTHENTICATION);
        if(logger.isLoggable(Level.FINER))
            logger.finer("Need in env Map: " +  needStr);
        need = constructBoolean(needStr);
        
        String pushProtoList = (String) WSEnvHelp.failGet(env,
                JMXWSDefinitions.JMX_WS_TLS_ENABLED_PROTOCOLS_PUSH_NOTIFICATION);
        
        if(logger.isLoggable(Level.FINER))
            logger.finer("PUSH Protocols in env Map: " +  pushProtoList);
        
        pushProtocols = constructArray(pushProtoList);
        
        String pushCipherList = (String) WSEnvHelp.failGet(env,
                JMXWSDefinitions.JMX_WS_TLS_ENABLED_CIPHER_SUITES_PUSH_NOTIFICATION);
        
        if(logger.isLoggable(Level.FINER))
            logger.finer("PUSH Cipher suite in env Map: " +  pushCipherList);
        pushCipherSuites = constructArray(pushCipherList);
        
    }
    
    private Boolean constructBoolean(String str) {
        if(str == null) return null;
            if(str.equalsIgnoreCase("true"))
                return Boolean.TRUE;
            if(str.equalsIgnoreCase("false"))
                return Boolean.FALSE;
      
        throw new IllegalArgumentException("Property should be " +
                    "\"true\" or \"false\" but is " + str);
    }
    
    private String[] constructArray(String str) {
        if(str == null) return null;
        
        StringTokenizer t = new StringTokenizer(str);
        String[] array = new String[t.countTokens()];
        int i = 0;
        while(t.hasMoreTokens()) {
            array[i++] = t.nextToken();
        }
        return array;
    }
    
    public boolean isHttps() {
        return https;
    }
    
    public static SSLContext getDefaultSSLContext() throws IOException {
        logger.finer("Default SSLContext.");
        Method method;
        try {
            method = SSLContext.class.getDeclaredMethod("getDefault");
        }catch(NoSuchMethodException ex) {
            /** running on JDK 1.5 **/
            logger.finer("An SSLContext must be provided.");
            // XXX REVISIT PROVIDE A DEFAULT
            // SSLContect.getInstance("TLS");
            // init(null,null,null);
            // TO CHECK WHEN LUIS HAS GOT A REPLY
            IllegalArgumentException iae = 
                    new IllegalArgumentException("No SSLContext " +
                    "provided.");
            iae.initCause(ex);
            throw iae;
        }
        
        logger.finer("Getting default SSLContext.");
        SSLContext sslcontext;
        try {
            sslcontext = (SSLContext) method.invoke(null);
        } catch (Exception ex) {
            IOException io = new IOException(ex.toString());
            io.initCause(ex);
            throw io;
        }
        
        return sslcontext;
    }
    public Boolean getWant() {
        return want;
    }
    
    public Boolean getNeed() {
        return need;
    }
    
    public SSLContext getHttpServerSSLContext() throws IOException {
        if(!https) return null;
        SSLContext ctx;
        if((ctx = doGetHttpServerSSLContext()) == null)
            ctx = getDefaultSSLContext();
        return ctx;
    }
    public SSLSocketFactory getHttpConnectionSSLFactory() throws IOException {
        SSLSocketFactory factory;
        SSLSocketFactory wrapped;
        try {
            SSLContext ctx;
            if((ctx = doGetHttpConnectionSSLContext()) == null)
                ctx = getDefaultSSLContext();
            wrapped = ctx.getSocketFactory();
        }catch(Exception ex) {
            wrapped = (SSLSocketFactory)SSLSocketFactory.getDefault();
        }
        
        factory = new WrappedSSLSocketFactory(wrapped,
                doGetHttpConnectionCipherSuites(),
                doGetHttpConnectionProtocols());
        return factory;
    }
    
    public abstract String[] getHttpServerCipherSuites();
    public abstract String[] getHttpServerProtocols();
    
    protected abstract SSLContext doGetHttpServerSSLContext() throws IOException;
    protected abstract SSLContext doGetHttpConnectionSSLContext() throws IOException;
    protected abstract String[] doGetHttpConnectionCipherSuites() throws IOException;
    protected abstract String[] doGetHttpConnectionProtocols() throws IOException;
}
