package com.sun.ws.management.client.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.xpath.XPathExpressionException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorSetType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.ServerIdentity;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.enumeration.EnumerationExtensions.Mode;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

public class EnumerationResourceImpl extends TransferableResourceImpl implements Resource {
	public ArrayList reqResList = new ArrayList();

	public EnumerationResourceImpl(){}
	
    protected static final String UUID_SCHEME = "uuid:";
    private static Logger log = Logger.getLogger(EnumerationResourceImpl.class.getName());

	public EnumerationResourceImpl(String destination, String resourceURI,long timeout, SelectorSetType selectors) throws SOAPException, JAXBException{
		super(destination, resourceURI,timeout,selectors);
	}
	
	private void initJAXB() {
		//initialize JAXB bindings
        try {
			if(new Addressing().getXmlBinding()==null){
				SOAP.setXmlBinding(new XmlBinding(null));
			}
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	//attributes
	// TODO wire Add support for RequestTotalItemsCountEstimate 
	// ******************* WS Enumeration *************************************
	/**
	 * Starts an enumeration transaction by obtaining an enumeration context.
	 * This is a ticket which must be used in all future calls to access this
	 * enumeration.
	 *
	 * @param filters and array of filter expressions to be applied to the
	 * enumeration.
	 * @param dialect The dialect to be used in filter expressions. XPATH_DIALECT
	 * can be used for XPath. 
	 * @param useEprs  useEprs sets the EnumerateEpr Element causing subsequent pulls to
	 * contain erps only
	 * @return An enumeration context
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public String enumerate(String[] filters, String dialect, boolean useEprs) throws SOAPException,JAXBException, IOException, FaultException, DatatypeConfigurationException {
		String enumerationContextId = "";
		//TODO: add argument for user to supply EPR:ReplyTo, right now assume anonymous.
		
		String filter = "";
		//process filter if available
		if(filters!=null){
			//Filter type is XMLAny so assume String is xml node, and convert filterArray to 
			//		flat filter node list. Server must know how to parse content either way.
			for (int i = 0; i < filters.length; i++) {
				filter+=filters[i]+"\n";
			}
		}
		
		//Now generate the request for an EnumCtxId with parameters passed in
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId("uuid:" + UUID.randomUUID().toString());

        Mode enumerationMode = 
        	useEprs ? 
                    /* EnumerationModeType.valueOf("EnumerateEPR") */
                    EnumerationExtensions.Mode.EnumerateEPR
                : 
                    /* EnumerationModeType.valueOf("EnumerateObjectAndEPR") */
                    EnumerationExtensions.Mode.EnumerateObjectAndEPR;
                

        
        final DatatypeFactory factory = DatatypeFactory.newInstance();
        final FilterType filterType = Enumeration.FACTORY.createFilterType();
        final EndpointReferenceType endTo = Addressing.createEndpointReference("http://host/endTo", null, null, null, null);

        if(filters!=null){
	        filterType.setDialect(XPath.NS_URI);
	        filterType.getContent().add(filter);
	        enu.setEnumerate(endTo, factory.newDuration(getMessageTimeout()).toString(),
                    filter == null ? null : filterType,enumerationMode.toBinding());
        }else{
        	enu.setEnumerate(null, factory.newDuration(getMessageTimeout()).toString(),
                         null,enumerationMode.toBinding());//,
        }
        
        final Management mgmt = new Management(enu);
        mgmt.setTo(getDestination());
        mgmt.setResourceURI(getResourceUri());
        
        //store away request and response for display purposes only
        reqResList = new ArrayList();
        reqResList.add(mgmt.toString());
        
        final Addressing response = HttpClient.sendRequest(mgmt);

        //Check for fault during message generation
        if (response.getBody().hasFault()) {
            SOAPFault fault = response.getBody().getFault();
            throw new FaultException(fault.getFaultString());
        }

        final Enumeration enuResponse = new Enumeration(response);
        reqResList.add(enuResponse.toString());
        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
        
        enumerationContextId = (String) enr.getEnumerationContext().getContent().get(0);
		return enumerationContextId;
	}

	/**
	 * Assumes the return type will contain EPR's. Each EPR that is found in the
	 * returned resource state will be converted into its own Resource implementation.
	 * This is very useful when the response is a collection of EPR's such as when
	 * the UseEpr element is set.
	 * 
	 * @param enumerationContext the context create in your call to enumerate
	 * @param maxTime The maxium timeout you are willing to wait for a response
	 * @param maxElements the maximum number of elements which should be returned
	 * @param maxCharacters the total size of the characters to be contained in
	 * @param endpointUrl
	 * @return
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 * @throws XPathExpressionException
	 * @throws NoMatchFoundException
	 */
	public Resource[] pullResources(String enumerationContext, int maxTime, 
			int maxElements, int maxCharacters,String endpointUrl) throws SOAPException, JAXBException, IOException, FaultException, DatatypeConfigurationException {
		ResourceState resState = pull(enumerationContext, maxTime, 
			maxElements, maxCharacters);
		NodeList eprNodes=null;
		try {
			eprNodes = resState.getValues("//*[namespace-uri()=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" and local-name()=\"EndpointReference\"]");
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoMatchFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Vector ret=new Vector();
		for(int index=0;index<eprNodes.getLength();index++){
			Element eprElement = (Element)eprNodes.item(index);
			ret.add(new ResourceImpl(eprElement,endpointUrl));
		}
		
		return (Resource[]) ret.toArray(new Resource[]{});
	}
	
	// TODO wire Add support for RequestTotalItemsCountEstimate 
	// TODO wire must implement EnumerateObjectAndEpr
	/**
	 * Requests a list of erps or objects. If you request EPRs or some fragment
	 * of the state of an object this version of pull will just return them as
	 * a resource state and you will have to extract the EPRs yourself. Use pullResources
	 * for better access to EPRs.
	 * 
	 * @param enumerationContext The context created from a previous enumerate call.
	 * @param maxTime The maxium timeout you are willing to wait for a response
	 * @param maxElements the maximum number of elements which should be returned
	 * @param maxCharacters the total size of the characters to be contained in
	 * the response
	 * @return A resource state representing the returned complex type of the pull. 
	 * Often this state will contain multiple entries from a number of resources.
	 * 
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public ResourceState pull(String enumerationContext, int maxTime, 
			int maxElements, int maxCharacters) throws SOAPException,JAXBException, 
			IOException, FaultException, DatatypeConfigurationException {
		
		//Now generate the request for an EnumCtxId with parameters passed in
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.PULL_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId("uuid:" + UUID.randomUUID().toString());
        //final DatatypeFactory factory = DatatypeFactory.newInstance();
        Duration timeout = DatatypeFactory.newInstance().newDuration(maxTime);        	
        
        enu.setPull(enumerationContext,maxCharacters,maxElements,timeout);
        	
        final Management mgmt = new Management(enu);
        mgmt.setTo(getDestination());
        mgmt.setResourceURI(getResourceUri());

        reqResList = new ArrayList();
        reqResList.add(mgmt.toString());

        final Addressing response = HttpClient.sendRequest(mgmt);
        
        //Check for fault during message generation
        if (response.getBody().hasFault()) {
            SOAPFault fault = response.getBody().getFault();
            throw new FaultException(fault.getFaultString());
        }
        
        final Enumeration enuResponse = new Enumeration(response);
        reqResList.add(enuResponse.toString());
        
        SOAPBody body = response.getBody();        
		return new ResourceStateImpl(body.extractContentAsDocument());
//        
//        
//        final Enumeration enuResponse = new Enumeration(response);
//        reqResList.add(enuResponse.toString());
//        PullResponse pullResponse = enuResponse.getPullResponse();
//        ItemListType returnedItemsContainer = pullResponse.getItems();
//         List<Object> items = returnedItemsContainer.getAny();
//         enumResultsPart = new Object[items.size()];
//         int arrayIndex = 0;
//         for (Iterator iter = items.iterator(); iter.hasNext();) {
//			Object element = (Object) iter.next();
//			enumResultsPart[arrayIndex++] = element;
//		 }
//		return enumResultsPart;
	}

	/**
	 * Releases a context for enumeration.
	 * 
	 * @param enumerationContext
	 *            Nameof the context to release
	 * @throws DatatypeConfigurationException
	 * @throws FaultException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public void release(String enumerationContext) throws SOAPException,JAXBException, 
	IOException, FaultException, DatatypeConfigurationException  {
		
		//Now generate the request for an EnumCtxId with parameters passed in
		final Enumeration enu = new Enumeration();
		enu.setAction(Enumeration.RELEASE_ACTION_URI);
		enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
		enu.setMessageId("uuid:" + UUID.randomUUID().toString());
		enu.setRelease(enumerationContext);
		
		final Management mgmt = new Management(enu);
		mgmt.setTo(getDestination());
		mgmt.setResourceURI(getResourceUri());
		
		final Addressing response = HttpClient.sendRequest(mgmt);
		
		//Check for fault during message generation
		if (response.getBody().hasFault()) {
			SOAPFault fault = response.getBody().getFault();
			throw new FaultException(fault.getFaultString());
		}
	}

	/**
	 * Releases a context for enumeration.
	 * 
	 * @param enumerationContext
	 *            Nameof the context to release
	 * @throws DatatypeConfigurationException
	 * @throws FaultException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SOAPException
	 */
	public void renew(String enumerationContext) throws SOAPException,JAXBException, 
	IOException, FaultException, DatatypeConfigurationException  {
		
		//Now generate the request for an EnumCtxId with parameters passed in
        final Enumeration enu = new Enumeration();
        enu.setAction(Enumeration.RENEW_ACTION_URI);
        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        enu.setMessageId("uuid:" + UUID.randomUUID().toString());
          enu.setRelease(enumerationContext);

        final Management mgmt = new Management(enu);
        mgmt.setTo(getDestination());
        mgmt.setResourceURI(getResourceUri());

        final Addressing response = HttpClient.sendRequest(mgmt);

        //Check for fault during message generation
        if (response.getBody().hasFault()) {
            SOAPFault fault = response.getBody().getFault();
            throw new FaultException(fault.getFaultString());
        }
	}

	public EndpointReferenceType subscribe(EndpointReferenceType EndToEpr, String deliveryType, Duration expires, String[] filters, String dialect) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return Returns the reqResList.
	 */
	public ArrayList getReqResList() {
		return reqResList;
	}


	/**
	 * @return Returns the destination.
	 */
	public String getDestination() {
		return destination;
	}


	/**
	 * @param destination The destination to set.
	 */
	private void setDestination(String destination) {
		this.destination = destination;
	}


	/**
	 * @return Returns the resourceURI.
	 */
	public String getResourceUri() {
		return resourceURI;
	}


	/**
	 * @param resourceURI The resourceURI to set.
	 */
	private void setResourceUri(String resourceURI) {
		this.resourceURI = resourceURI;
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.ResourceImpl#getMessageTimeout()
	 */
	//@Override
	public long getMessageTimeout() {
		return this.messageTimeout;
	}

	/* (non-Javadoc)
	 * @see com.sun.ws.management.client.impl.ResourceImpl#getSelectorSet()
	 */
	//@Override
	public SelectorSetType getSelectorSet() {
		return this.selectorSet;
	}


}
