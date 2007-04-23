package util;
 
import java.io.IOException;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.transfer.TransferMessageValues;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationMessageValues;
import com.sun.ws.management.enumeration.EnumerationUtility;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;

public class WsManBaseTestSupport extends TestCase {
    protected static final int TEST_TIMEOUT = 30000;
	private XPath xpath;

	public WsManBaseTestSupport() {
		super();

		final String basicAuth = System.getProperty("wsman.basicauthentication");
        if ("true".equalsIgnoreCase(basicAuth)) {
            HttpClient.setAuthenticator(new transport.BasicAuthenticator());
        }


		xpath = XPathFactory.newInstance().newXPath();
	}
	    protected Management sendCreateRequest(String destination, String resourceUri, Document propertyDocument) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {

	        // Build Request Document
	    	TransferMessageValues settings = TransferMessageValues.newInstance();
	    	settings.setResourceUri(resourceUri);
	    	settings.setTo(destination);
	    	settings.setTransferMessageActionType(Transfer.CREATE_ACTION_URI);
	    	final Transfer mgmt = TransferUtility.buildMessage(null, settings);

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

	        // Build Request Document
	    	TransferMessageValues settings = TransferMessageValues.newInstance();
	    	settings.setResourceUri(resourceUri);
	    	settings.setTo(destination);
	    	settings.setSelectorSet(selectors);
	    	settings.setTransferMessageActionType(Transfer.DELETE_ACTION_URI);
	    	final Transfer mgmt = TransferUtility.buildMessage(null, settings);

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

	        // Build Request Document
	    	TransferMessageValues settings = TransferMessageValues.newInstance();
	    	settings.setResourceUri(resourceUri);
	    	settings.setTo(destination);
	    	settings.setSelectorSet(selectors);
	    	settings.setTransferMessageActionType(Transfer.PUT_ACTION_URI);
	    	final Transfer mgmt = TransferUtility.buildMessage(null, settings);

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


	    protected Management sendGetRequest(String destination, String resourceUri, Set<SelectorType> selectors, Set<OptionType> options) throws SOAPException, JAXBException, DatatypeConfigurationException, IOException
	    {

    	
	        // Build Request Document
	    	TransferMessageValues settings = TransferMessageValues.newInstance();
	    	settings.setResourceUri(resourceUri);
	    	settings.setTo(destination);
	    	settings.setSelectorSet(selectors);
	    	settings.setTransferMessageActionType(Transfer.GET_ACTION_URI);
	        if (options != null) {
	        	settings.setOptionSet(options);
	        }
	    	final Transfer mgmt = TransferUtility.buildMessage(null, settings);

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

	    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
	    	settings.setFilter(filter);
	    	settings.setDefaultTimeout(6000);
	    	settings.setTo(destination);
	    	settings.setResourceUri(resourceUri);
	    	final Enumeration enu = EnumerationUtility.buildMessage(null, settings);
	    	
	        final Addressing response = HttpClient.sendRequest(enu);

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
//System.out.println("PullResponse:"+response.toString());
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
	    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
	    	settings.setEnumerationMessageActionType(action);
	    	settings.setDefaultTimeout(60000);
	    	settings.setMaxElements(100);
	    	settings.setEnumerationContext(ctx);
	    	settings.setTo(destination);
	    	settings.setResourceUri(resourceUri);
	    	final Enumeration enu = EnumerationUtility.buildMessage(null, settings);
	        
	        final Addressing response = HttpClient.sendRequest(enu);
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
	    	EnumerationMessageValues settings = EnumerationMessageValues.newInstance();
	    	settings.setEnumerationMessageActionType(Enumeration.RELEASE_ACTION_URI);
	    	settings.setDefaultTimeout(60000);
	    	settings.setEnumerationContext(ctx);
	    	settings.setTo(destination);
	    	settings.setResourceUri(resourceUri);
	    	final Enumeration enu = EnumerationUtility.buildMessage(null, settings);
	        
	        Addressing response = HttpClient.sendRequest(enu);
	        
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
