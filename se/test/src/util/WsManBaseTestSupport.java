package util;
 
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.Message;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transport.HttpClient;
import com.sun.ws.management.xml.XmlBinding;

public class WsManBaseTestSupport extends TestCase {
    protected static final int TEST_TIMEOUT = 30000;
	private XPath xpath;

	public WsManBaseTestSupport() {
		super();
        // Init Jaxb for wiseman
        try {
			Message.initialize();

			if (new Addressing().getXmlBinding() == null)
			{
			    SOAP.setXmlBinding(new XmlBinding(null));
			}
		} catch (Exception e) {
			fail(e.getMessage());
		} 

		final String basicAuth = System.getProperty("wsman.basicauthentication");
        if ("true".equalsIgnoreCase(basicAuth)) {
            HttpClient.setAuthenticator(new transport.BasicAuthenticator());
        }


		xpath = XPathFactory.newInstance().newXPath();
	}
	    protected Management sendCreateRequest(String destination, String resourceUri, Document propertyDocument) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        // Create a transfer document
	        Transfer xf = new Transfer();
	        xf.setAction(Transfer.CREATE_ACTION_URI);
	        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	        xf.setMessageId("uuid:" + UUID.randomUUID().toString());

	        // Add required ws-management values
	        Management mgmt = new Management(xf);
	        mgmt.setTo(destination);
	        mgmt.setResourceURI(resourceUri);
	        Duration timeout = DatatypeFactory.newInstance().newDuration(TEST_TIMEOUT);
	        mgmt.setTimeout(timeout);

	        mgmt.getBody().addDocument(propertyDocument);

	        // Send request
	        Addressing response = HttpClient.sendRequest(mgmt);

	        // Look for returned faults
	        if (response.getBody().hasFault())
	        {
	            SOAPFault fault = response.getBody().getFault();
	            throw new SOAPException(fault.getFaultString());
	        }

	        return new Management(response);
	    }

	    protected Management sendDeleteRequest(String destination, String resourceUri, Set<SelectorType> selectors) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        // Create a transfer document
	        Transfer xf = new Transfer();
	        xf.setAction(Transfer.DELETE_ACTION_URI);
	        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	        xf.setMessageId("uuid:" + UUID.randomUUID().toString());

	        // Add required ws-management values
	        Management mgmt = new Management(xf);
	        mgmt.setTo(destination);
	        mgmt.setResourceURI(resourceUri);
	        Duration timeout = DatatypeFactory.newInstance().newDuration(TEST_TIMEOUT);
	        mgmt.setTimeout(timeout);
	        mgmt.setSelectors(selectors);

	        // Send request
	        Addressing response = HttpClient.sendRequest(mgmt);

	        // Look for returned faults
	        if (response.getBody().hasFault())
	        {
	            SOAPFault fault = response.getBody().getFault();
	            throw new SOAPException(fault.getFaultString());
	        }
	        return new Management(response);


	    }

	    protected Management sendPutRequest(String destination, String resourceUri, Set<SelectorType> selectors, Document propertyDocument) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        // Create a transfer document
	        Transfer xf = new Transfer();
	        xf.setAction(Transfer.PUT_ACTION_URI);
	        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	        xf.setMessageId("uuid:" + UUID.randomUUID().toString());

	        // Add required ws-management values
	        Management mgmt = new Management(xf);
	        mgmt.setTo(destination);
	        mgmt.setResourceURI(resourceUri);
	        Duration timeout = DatatypeFactory.newInstance().newDuration(TEST_TIMEOUT);
	        mgmt.setTimeout(timeout);
	        mgmt.setSelectors(selectors);

	        mgmt.getBody().addDocument(propertyDocument);

	        // Send request
	        Addressing response = HttpClient.sendRequest(mgmt);

	        // Look for returned faults
	        if (response.getBody().hasFault())
	        {
	            SOAPFault fault = response.getBody().getFault();
	            throw new SOAPException(fault.getFaultString());
	        }

	        return new Management(response);
	    }


	    protected Management sendGetRequest(String destination, String resourceUri, Set<SelectorType> selectors) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        // Build a transfer request
	        Transfer xf = new Transfer();
	        xf.setAction(Transfer.GET_ACTION_URI);
	        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	        xf.setMessageId("uuid:" + UUID.randomUUID().toString());

	        // Build Request Document
	        Management mgmt = new Management(xf);
	        mgmt.setTo(destination);
	        mgmt.setResourceURI(resourceUri);
	        Duration timeout = DatatypeFactory.newInstance().newDuration(TEST_TIMEOUT);
	        mgmt.setTimeout(timeout);

	        mgmt.setSelectors(selectors);

	        // Send the get request to the server
	        Addressing response = HttpClient.sendRequest(mgmt);

	        // Look for returned faults
	        if (response.getBody().hasFault())
	        {
	            SOAPFault fault = response.getBody().getFault();
	            throw new SOAPException(fault.getFaultString());
	        }

	        return new Management(response);
	    }

	    protected EnumerateResponse sendEnumerateRequest(String destination, String resourceUri, String filter) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        final Enumeration enu = new Enumeration();
	        enu.setAction(Enumeration.ENUMERATE_ACTION_URI);
	        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	        enu.setMessageId("uuid:" + UUID.randomUUID().toString());
	        final DatatypeFactory factory = DatatypeFactory.newInstance();
	        final FilterType filterType = Enumeration.FACTORY.createFilterType();
	        filterType.setDialect(com.sun.ws.management.xml.XPath.NS_URI);
	        filterType.getContent().add(filter);
	        enu.setEnumerate(null, factory.newDuration(6000).toString(),
	                         filter == null ? null : filterType);

	        final Management mgmt = new Management(enu);
	        mgmt.setTo(destination);
	        mgmt.setResourceURI(resourceUri);

	        final Addressing response = HttpClient.sendRequest(mgmt);

	        if (response.getBody().hasFault())
	        {
	            fail(response.getBody().getFault().getFaultString());
	        }

	        final Enumeration enuResponse = new Enumeration(response);
	        final EnumerateResponse enr = enuResponse.getEnumerateResponse();
	        return enr;
	    }

	    protected PullResponse sendPullRequest(String destination,
	                                                String resourceUri,
	                                                Object ctx) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        final Addressing response = sendEnumRequest(ctx, destination, resourceUri, Enumeration.PULL_ACTION_URI);

	        if (response.getBody().hasFault())
	        {
	            fail(response.getBody().getFault().getFaultString());
	        }

	        final Enumeration enuResponse = new Enumeration(response);
	        final PullResponse enr = enuResponse.getPullResponse();
	        return enr;

	    }

	    public Addressing sendEnumRequest(Object ctx, String destination, String resourceUri, String action)
	            throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        final Enumeration enu = new Enumeration();
	        enu.setAction(action);
	        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	        enu.setMessageId("uuid:" + UUID.randomUUID().toString());
	        final DatatypeFactory factory = DatatypeFactory.newInstance();
	        enu.setPull(ctx,0,100,factory.newDuration(60000));//maxchars not implemented yet

	        final Management mgmt = new Management(enu);
	        mgmt.setTo(destination);
	        mgmt.setResourceURI(resourceUri);

	        final Addressing response = HttpClient.sendRequest(mgmt);
	        return response;
	    }

	    protected EnumerateResponse sendGetStatusRequest() throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        return null;
	    }

	    protected EnumerateResponse sendRenewRequest()  throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        return null;
	    }

	    protected Addressing sendReleaseRequest(String destination,
	                                                String resourceUri, Object ctx) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {
	        final Enumeration enu = new Enumeration();
	        enu.setAction(Enumeration.RELEASE_ACTION_URI);
	        enu.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
	        enu.setMessageId("uuid:" + UUID.randomUUID().toString());
	        //final DatatypeFactory factory = DatatypeFactory.newInstance();
	        enu.setRelease(ctx);

	        final Management mgmt = new Management(enu);
	        mgmt.setTo(destination);
	        mgmt.setResourceURI(resourceUri);

	        Addressing response = HttpClient.sendRequest(mgmt);

	        return response;


	    }
		/**
		 * Returns the element text of the Element pointed to by the provided XPath.
		 * @param xPathExpression
		 * @return A string containg the element text.
		 * @throws XPathExpressionException
		 * @throws NoMatchFoundException 
		 */
		public String getXPathText(String xPathExpression,Object stateDocument) throws XPathExpressionException{
			Object resultOb = xpath.evaluate(xPathExpression, stateDocument, XPathConstants.STRING);
			if(resultOb==null)
				return null;
			return (String)resultOb;
		}
		/** 
		 * Returns a list of nodes that match the provided XPath criteria.
		 * 
		 * @param xPathExpression
		 * @return A list of matching nodes.
		 * @throws XPathExpressionException
		 * @throws NoMatchFoundException 
		 */
		public NodeList getXPathValues(String xPathExpression,Object stateDocument) throws XPathExpressionException{
			Object nodes = xpath.evaluate(xPathExpression, stateDocument, XPathConstants.NODESET);
			if(nodes==null)
				return null;	
			NodeList nodelist = (NodeList)nodes;		
			return nodelist;
		}

}
