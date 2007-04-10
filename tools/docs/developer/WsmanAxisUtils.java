package my.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

/**
 * Wiseman client utility to class to be used by Axis clients.  This is a helper class to allow users of the Axis
 * SOAP client toolkit to more easily interoperate withe wiseman web services.
 */
public class WsmanAxisUtils {
	
	// Factory used to produce OMElements
	protected static OMFactory fact = OMAbstractFactory.getOMFactory();

	/**
	 * Address of the wsman addressing namespace
	 */
	public static String WSA_ADDR = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
	/**
	 * Prefix of the wsman addressing namespace
	 */
	public static String WSA_PREFIX = "wsa";
	/**
	 * Address of the wsman namespace
	 */
	public static String WSMAN_ADDR = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd";
	/**
	 * Prefix of the wsman namespace
	 */
	public static String WSMAN_PREFIX = "wsman";

	public WsmanAxisUtils() {
		
	}
	
	/**
	 * Create an OMElement representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return OMElement to be added as a header to the SOAP call
	 */
	public static OMElement createResourceUriHeader(String uri) {
       	OMElement headerElement =
			fact.createOMElement("ResourceURI", WSMAN_ADDR, WSMAN_PREFIX);
		headerElement.setText(uri);
		return headerElement;
	}

	/**
	 * Create an OMElement representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return OMElement to be added as a header to the SOAP call
	 */
	public static OMElement createToHeader(String to) {
       	OMElement headerElement =
			fact.createOMElement("To", WSA_ADDR, WSA_PREFIX);
		headerElement.setText(to);
		return headerElement;
	}	
	
	/**
	 * Create an OMElement representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return OMElement to be added as a header to the SOAP call
	 */
	public static OMElement createActionHeader(String action) {
       	OMElement headerElement =
			fact.createOMElement("Action", WSA_ADDR, WSA_PREFIX);
		headerElement.setText(action);
		return headerElement;
	}	
	
	/**
	 * Create an OMElement representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return OMElement to be added as a header to the SOAP call
	 */
	public static OMElement createMessageIDHeader(String msgId) {
       	OMElement headerElement =
			fact.createOMElement("MessageID", WSA_ADDR, WSA_PREFIX);
		headerElement.setText(msgId);
		return headerElement;
	}	
	
	/**
	 * Create an OMElement representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return OMElement to be added as a header to the SOAP call
	 */
	public static OMElement createOperationTimeoutHeader(String timeOut) {
       	OMElement headerElement =
			fact.createOMElement("OperationTimeout", WSMAN_ADDR, WSMAN_PREFIX);
		headerElement.setText(timeOut);
		return headerElement;
	}	
	
	/**
	 * Create an OMElement representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return OMElement to be added as a header to the SOAP call
	 */
	public static OMElement createReplyToHeader(String replyTo) {
       	OMElement headerElement =
			fact.createOMElement("ReplyTo", WSA_ADDR, WSA_PREFIX);
       	headerElement.addChild(fact.createOMElement("Address", WSA_ADDR, WSA_PREFIX));
    	headerElement.getFirstElement().setText(replyTo);
		return headerElement;
	}
	

	/**
	 * Create an OMElement representing the Selector Set header
	 * 
	 * @param fieldName name of the selector field
	 * @param selector the value of the selector field
	 * @return OMElement to be added as a header to the SOAP call
	 */
	public static OMElement createSelectorSetHeader(String[] fieldNames, String[] selectors) {
		
		if (fieldNames.length != selectors.length) {
			// There must be an equal number of field names and selector strings
			return null;
		}
		OMElement headerElement =
			fact.createOMElement("SelectorSet", WSMAN_ADDR, WSMAN_PREFIX);
		
		for (int i = 0; i < fieldNames.length; ++i) {
			OMElement child = fact.createOMElement("Selector", WSMAN_ADDR, WSMAN_PREFIX);
			child.addAttribute("Name", fieldNames[i], null);
			child.setText(selectors[i]);
			headerElement.addChild(child);
		}
    	return headerElement;
	}
	/**
	 * Create an OMElement representing the Option Set header
	 * 
	 * @param optNames name of the option field
	 * @param optValues the value of the option field
	 * @return OMElement to be added as a header to the SOAP call
	 */
	public static OMElement createOptionSetHeader(String[] optNames, String[] optValues) {
		
		if (optNames.length != optValues.length) {
			// There must be an equal number of option names and option values
			return null;
		}
		OMElement headerElement =
			fact.createOMElement("OptionSet", WSMAN_ADDR, WSMAN_PREFIX);
		
		for (int i = 0; i < optNames.length; ++i) {
			OMElement child = fact.createOMElement("Option", WSMAN_ADDR, WSMAN_PREFIX);
			child.addAttribute("Name", optNames[i], null);
			child.setText(optValues[i]);
			headerElement.addChild(child);
		}
    	return headerElement;
	}	
	
	/**
	 * Create the OMElement object for the Create action
	 * @return Create action OMElement
	 */
	public static OMElement createCreateActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/transfer/Create");
	}
	
	/**
	 * Create the OMElement object for the Get action
	 * @return Get action OMElement
	 */
	public static OMElement createGetActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/transfer/Get");
	}	
	
	/**
	 * Create the OMElement object for the Put action
	 * @return Put action OMElement
	 */
	public static OMElement createPutActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/transfer/Put");
	}		

	/**
	 * Create the OMElement object for the Delete action
	 * @return Delete action OMElement
	 */
	public static OMElement createDeleteActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete");
	}		
	
	/**
	 * Create the OMElement object for the Enumerate action
	 * @return Enumerate action OMElement
	 */
	public static OMElement createEnumerateActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate");
	}
	
	/**
	 * Create the OMElement object for the Pull action
	 * @return Pull action OMElement
	 */
	public static OMElement createPullActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull");
	}
	
	/**
	 * Create the OMElement object for the Renew action
	 * @return Renew action OMElement
	 */
	public static OMElement createRenewActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Renew");
	}
	
	/**
	 * Create the OMElement object for the Get Status action
	 * @return Get Status action OMElement
	 */
	public static OMElement createGetStatusActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatus");
	}

	/**
	 * Create the OMElement object for the Release action
	 * @return Release action OMElement
	 */
	public static OMElement createReleaseActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Release");
	}

	/**
	 * Create the OMElement object for the Subscribe action
	 * @return Subscribe action OMElement
	 */
	public static OMElement createSubscribeActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe");
	}

	/**
	 * Create the OMElement object for the Renew action
	 * @return Renew action OMElement
	 */
	public static OMElement createEventingRenewActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/08/eventing/Renew");
	}

	/**
	 * Create the OMElement object for the eventing Get Status action
	 * @return eventing Get Status action OMElement
	 */
	public static OMElement createEventingGetStatusActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatus");
	}

	/**
	 * Create the OMElement object for the Unsubscribe action
	 * @return Unsubscribe action OMElement
	 */
	public static OMElement createUnsubscribeActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe");
	}

}
