package com.sun.ws.management.eventing;

import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.soap.SOAPException;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementMessageValues;
import com.sun.ws.management.transfer.Transfer;

/**This class is meant to be a container for constants
 * that are used or applied to an Eventing message.
 * No complicated methods or logic should be present.
 * 
 * See following for original settings as ManagemetMessageConstant
 * values apply.
 * {@link ManagementMessageValues}
 *  
 * @author Nancy Beers
 */
public class EventingMessageValues extends ManagementMessageValues {
	
	//In event of failure and to initialize these values are defined
	public static final EndpointReferenceType DEFAULT_END_TO=null; 
	public static final String DEFAULT_DELIV_MODE=Eventing.PUSH_DELIVERY_MODE;
	public static final EndpointReferenceType DEFAULT_NOTIFY_TO=null; 
	public static final String DEFAULT_EXPIRES="PT30.000S"; 
	public static final String DEFAULT_EVENTING_MESSAGE_ACTION_TYPE = Eventing.SUBSCRIBE_ACTION_URI;
	public static final String DEFAULT_UID_SCHEME=ManagementMessageValues.DEFAULT_UID_SCHEME;
	public static final String DEFAULT_FILTER = "";
	public static final String DEFAULT_FILTER_DIALECT = com.sun.ws.management.xml.XPath.NS_URI;
	public static final Map<String, String> DEFAULT_NS_MAP = null;
	public static final String DEFAULT_STATUS = "";
	public static final String DEFAULT_REASON = "";

	protected EndpointReferenceType endTo = DEFAULT_END_TO;
	protected String deliveryMode = DEFAULT_DELIV_MODE;
	protected EndpointReferenceType notifyTo = DEFAULT_NOTIFY_TO;
	protected String expires = DEFAULT_EXPIRES;
	protected String eventingMessageActionType = DEFAULT_EVENTING_MESSAGE_ACTION_TYPE;
	private String uidScheme = DEFAULT_UID_SCHEME;
	private String filter = DEFAULT_FILTER;
	private String filterDialect = DEFAULT_FILTER_DIALECT;
	private Map<String, String> namespaceMap = DEFAULT_NS_MAP;
	private String status = DEFAULT_STATUS;
	private String reason = DEFAULT_REASON;
	
	public String getUidScheme() {
		return uidScheme;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getFilterDialect() {
		return filterDialect;
	}

	public void setFilterDialect(String filterDialect) {
		this.filterDialect = filterDialect;
	}

	public void setUidScheme(String uidScheme) {
		this.uidScheme = uidScheme;
	}

	/** Instance has all the default values set. 
	 * 
	 * @throws SOAPException
	 */
	public EventingMessageValues() throws SOAPException{
	}
	
	public static EventingMessageValues newInstance() throws SOAPException{
		return new EventingMessageValues();
	}

	public String getDeliveryMode() {
		return deliveryMode;
	}

	public void setDeliveryMode(String deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	public EndpointReferenceType getEndTo() {
		return endTo;
	}

	public void setEndTo(EndpointReferenceType endTo) {
		this.endTo = endTo;
	}

	public String getExpires() {
		return expires;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public EndpointReferenceType getNotifyTo() {
		return notifyTo;
	}

	public void setNotifyTo(EndpointReferenceType notifyTo) {
		this.notifyTo = notifyTo;
	}

	public String getEventingMessageActionType() {
		return eventingMessageActionType;
	}

	public void setEventingMessageActionType(String eventingMessageActionType) {
		this.eventingMessageActionType = eventingMessageActionType;
	}

	public Map<String, String> getNamespaceMap() {
		return namespaceMap;
	}

	public void setNamespaceMap(Map<String, String> namespaceMap) {
		this.namespaceMap = namespaceMap;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	

}
