package framework;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import util.WsManBaseTestSupport;

import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;

/**
 * 
 * 
 */
public class EnumerationTestCase extends WsManBaseTestSupport
{
    private static final String RESOURCE_URI = "wsman:auth/file";
    private static final String DESTINATION = "http://localhost:8080/wsman/";

    public EnumerationTestCase()
    {
//        try
//        {
//            new Management();
//        }
//        catch (SOAPException e)
//        {
//            fail("Can't init wiseman");
//        }

    }

    public void testEnumerate() throws JAXBException, IOException, DatatypeConfigurationException, SOAPException
    {
        EnumerateResponse enumerateResponse = sendEnumerateRequest(DESTINATION, RESOURCE_URI, null);
        assertNotNull(enumerateResponse);

        EnumerationContextType enumerationContext = enumerateResponse.getEnumerationContext();
        List<Object> content = enumerationContext.getContent();

        PullResponse pullResponse = sendPullRequest(DESTINATION, RESOURCE_URI, content.get(0));
        assertNotNull(pullResponse);

        Addressing response = sendReleaseRequest(DESTINATION, RESOURCE_URI, content.get(0));
        assertTrue(response.getBody().hasFault());//should have fault since the pull request forced a release


        enumerateResponse = sendEnumerateRequest(DESTINATION, RESOURCE_URI, null);
        assertNotNull(enumerateResponse);
        enumerationContext = enumerateResponse.getEnumerationContext();
        content = enumerationContext.getContent();
        response = sendReleaseRequest(DESTINATION, RESOURCE_URI, content.get(0));
        response = sendEnumRequest(content.get(0),DESTINATION, RESOURCE_URI, Enumeration.PULL_ACTION_URI);
        assertTrue(response.getBody().hasFault());//should have a fault because it should have been released

        assertNull(sendGetStatusRequest());  //todo is null for now..not implemented
        assertNull(sendRenewRequest());  //todo is null for now..not implemented

    }

}
