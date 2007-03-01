package com.sun.ws.management.transfer;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;

/** This class is meant to provide general utility functionality for
 *  Transfer instances and all of their related extensions.  Management 
 *  instances populated with data may be returned.
 * 
 * @author Simeon
 */
public class TransferUtility {
	
	private static final Logger LOG = 
		Logger.getLogger(TransferUtility.class.getName());
	
	/** Method populates a transfer instance with the values passed in. The instance
	 *  returned is NOT the response from the server based upon the values passed in. 
	 *  
	 * @param destination
	 * @param resourceUri
	 * @param action
	 * @param selectors
	 * @param contents
	 * @param timeout
	 * @param uidScheme
	 * @return
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws DatatypeConfigurationException
	 */
	public static Management createMessage(
		String destination,
		String resourceUri,
		String action,
		Set<SelectorType> selectors,
		Document contents,
		long timeout,
		String uidScheme) 
	  throws SOAPException, 
		JAXBException, DatatypeConfigurationException{
		
		//Create return element reference
		Management mgmt = null;

		// Build a transfer instance
        Transfer xf = new Transfer();
        //set the action
        if((action!=null)&&(action.trim().length()>0)){
        	xf.setAction(Transfer.GET_ACTION_URI);
        }
        //set replyto to anonymous
        xf.setReplyTo(Addressing.ANONYMOUS_ENDPOINT_URI);
        //use the uidScheme in message id creation
        if((uidScheme!=null)&&(uidScheme.trim().length()>0)){
        	xf.setMessageId(uidScheme + UUID.randomUUID().toString());
        }else{//use the default
        	xf.setMessageId(ManagementUtility.getUidScheme()+
        			UUID.randomUUID().toString());
        }
        
        // Build the Management instance
        mgmt = new Management(xf);
        //populate binding
        mgmt.setXmlBinding(xf.getXmlBinding());
        //populate credentials
        mgmt.setTo(destination);
        mgmt.setResourceURI(resourceUri);
        
        //timeout creation
        Duration timeoutDur = null;
        if(timeout>=ManagementUtility.getDefaultTimeout()){
          timeoutDur= DatatypeFactory.newInstance().newDuration(timeout);
        	mgmt.setTimeout(timeoutDur);
		}
		else{//populate with the default
          timeoutDur=
          	DatatypeFactory.newInstance().newDuration(timeout);
          	mgmt.setTimeout(timeoutDur);
		}
        //proecess the selectors passed in.
        if((selectors!=null)&&(selectors.size()>0)){
        	mgmt.setSelectors(selectors);
        }
        //insert the contents passed in.
        if(contents!=null){
           mgmt.getBody().addDocument(contents);
        }
		
	  return mgmt;
	}
}
