package client;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.util.DOMUtil;

/**
 * Wiseman client utility to class to be used by JAX-WS clients.  This is a helper class to allow users of the JAX-WS
 * SOAP client toolkit to more easily interoperate withe wiseman web services.
 */
public class WsmanJaxwsUtils {

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


	public WsmanJaxwsUtils() {
		
	}
	
	/**
	 * Create an Header representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return Header to be added as a header to the SOAP call
	 */
	public static Header createResourceUriHeader(String uri) {
		return Headers.create(new QName(WSMAN_ADDR, "ResourceURI", WSMAN_PREFIX),uri);
	}

	/**
	 * Create an Header representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return Header to be added as a header to the SOAP call
	 */
	public static Header createToHeader(String to) {
		return Headers.create(new QName(WSA_ADDR, "To", WSA_PREFIX), to);
	}	
	
	/**
	 * Create an Header representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return Header to be added as a header to the SOAP call
	 */
	public static Header createActionHeader(String action) {
		return Headers.create(new QName(WSA_ADDR, "Action", WSA_PREFIX), action);
	}	
	
	/**
	 * Create an Header representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return Header to be added as a header to the SOAP call
	 */
	public static Header createMessageIDHeader(String msgId) {
		return Headers.create(new QName(WSA_ADDR, "MessageID", WSA_PREFIX), msgId);
	}	
	
	/**
	 * Create an Header representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return Header to be added as a header to the SOAP call
	 */
	public static Header createOperationTimeoutHeader(String timeOut) {
		return Headers.create(new QName(WSMAN_ADDR, "OperationTimeout", WSMAN_PREFIX), timeOut);
	}	
	
	/**
	 * Create an Header representing the resource URI header
	 * 
	 * @param Uri value of the resource Uri
	 * @return Header to be added as a header to the SOAP call
	 */
	public static Header createReplyToHeader(String replyTo) {
		
        Document doc = DOMUtil.createDom();
        Element replyElem = doc.createElementNS(WSA_ADDR, "ReplyTo");
        replyElem.appendChild(doc.createElementNS(WSA_ADDR, "Address"));
        replyElem.getFirstChild().setTextContent(replyTo);
		return Headers.create(replyElem);
	}

	/**
	 * Create an Header representing the Selector Set header
	 * 
	 * @param fieldName name of the selector field
	 * @param selector the value of the selector field
	 * @return Header to be added as a header to the SOAP call
	 */
	public static Header createSelectorSetHeader(String[] fieldNames, String[] selectors) {
		
		if (fieldNames.length != selectors.length) {
			// There must be an equal number of field names and selector strings
			return null;
		}
        Document doc = DOMUtil.createDom();
        Element selectorElem = doc.createElementNS(WSMAN_ADDR, "SelectorSet");
		for (int i = 0; i < fieldNames.length; ++i) {
			Element child = doc.createElementNS(WSMAN_ADDR, "Selector");
			child.setAttribute("Name", fieldNames[i]);
			child.setTextContent(selectors[i]);
			selectorElem.appendChild(child);
		}
        return Headers.create(selectorElem); 
	}

	/**
	 * Create an Header representing the Option Set header
	 * 
	 * @param optNames names of the options
	 * @param optValues values of the options
	 * @return Header to be added as a header to the SOAP call
	 */
	public static Header createOptionSetHeader(String[] optNames, String[] optValues) {
		
		if (optNames.length != optValues.length) {
			// There must be an equal number of option names and option values
			return null;
		}
        Document doc = DOMUtil.createDom();
        Element selectorElem = doc.createElementNS(WSMAN_ADDR, "OptionSet");
		for (int i = 0; i < optNames.length; ++i) {
			Element child = doc.createElementNS(WSMAN_ADDR, "Option");
			child.setAttribute("Name", optNames[i]);
			child.setTextContent(optValues[i]);
			selectorElem.appendChild(child);
		}
        return Headers.create(selectorElem); 
	}
	/**
	 * Create the header object for the Create action
	 * @return Create action header
	 */
	public static Header createCreateActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/transfer/Create");
	}
	
	/**
	 * Create the header object for the Get action
	 * @return Get action header
	 */
	public static Header createGetActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/transfer/Get");
	}	
	
	/**
	 * Create the header object for the Put action
	 * @return Put action header
	 */
	public static Header createPutActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/transfer/Put");
	}		

	/**
	 * Create the header object for the Delete action
	 * @return Delete action header
	 */
	public static Header createDeleteActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete");
	}		
	
	/**
	 * Create the header object for the Enumerate action
	 * @return Enumerate action header
	 */
	public static Header createEnumerateActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate");
	}
	
	/**
	 * Create the header object for the Pull action
	 * @return Pull action header
	 */
	public static Header createPullActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull");
	}
	
	/**
	 * Create the header object for the Renew action
	 * @return Renew action header
	 */
	public static Header createRenewActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Renew");
	}
	
	/**
	 * Create the header object for the Get Status action
	 * @return Get Status action header
	 */
	public static Header createGetStatusActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/GetStatus");
	}

	/**
	 * Create the header object for the Release action
	 * @return Release action header
	 */
	public static Header createReleaseActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/09/enumeration/Release");
	}

	/**
	 * Create the header object for the Subscribe action
	 * @return Subscribe action header
	 */
	public static Header createSubscribeActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe");
	}

	/**
	 * Create the Header object for the Renew action
	 * @return Renew action header
	 */
	public static Header createEventingRenewActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/08/eventing/Renew");
	}

	/**
	 * Create the Header object for the eventing Get Status action
	 * @return eventing Get Status action header
	 */
	public static Header createEventingGetStatusActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatus");
	}

	/**
	 * Create the header object for the Unsubscribe action
	 * @return Unsubscribe action header
	 */
	public static Header createUnsubscribeActionHeader() {
		return createActionHeader("http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe");
	}

}
