package com.sun.ws.management.mex;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._09.enumeration.ObjectFactory;

import com.sun.ws.management.addressing.Addressing;

public class Metadata extends Addressing{

	    public static final String NS_PREFIX = "mex";
	    public static final String NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/mex";
	    
	    public static final String METADATA_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Request";
	    public static final String METADATA_RESPONSE_URI = "http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Response";
	    
	    public static final ObjectFactory FACTORY = new ObjectFactory();
	    
	    public Metadata() throws SOAPException {
	        super();
	    }
	    
	    public Metadata(final Addressing addr) throws SOAPException {
	        super(addr);
	    }
	    
	    public Metadata(final InputStream is) throws SOAPException, IOException {
	        super(is);
	    }
}
