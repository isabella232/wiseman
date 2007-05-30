/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: EventSourceInterface.java,v 1.2 2007-05-30 20:30:31 nbeers Exp $
 */
package com.sun.ws.management.framework.eventing;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import com.sun.ws.management.Management;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementEnumerationAnnotation;
import com.sun.ws.management.server.HandlerContext;

/** This interface defines the required methods for a 
 * EventSource instance.  An Event Source
 * must support SUBSCRIBE.  How an Event Source and it's
 * associated Subscription Manager communicate crucial information
 * when they are VM different, is currently ambiguously defined by 
 * the specifications. 
 * 
 * Provide implementations for:
 * 	 isAlsoTheSubscriptionManager()
 * 	 getSubscriptionManager()
 * 
 * @author Simeon
 */
public interface EventSourceInterface {
	
//	public void subscribe(HandlerContext context, Management eventRequest, 
    public Management subscribe(HandlerContext context, Management eventRequest, 
    		Management eventResponse) throws SOAPException, JAXBException, 
    		DatatypeConfigurationException, IOException;
    
    /* Implementors may optionally support this item to allow other entities 
     * the ability to create events upon request from this entity.
     */
    public void create(HandlerContext context, Management request, 
    		Management response);
    
    //Subscription Manager interaction details.
    /** Flag indicating whether this EventSource is also the
     *  SubscriptionManager instance as well. No soap message
     *  communication with SubscriptionManager instance necessary. 
     * 
     * @return boolean flag indicating the above status.
     */
    boolean isAlsoTheSubscriptionManager();
//    Class getSubscriptionManagerClass();
 
//    public WsManagementDefaultAddressingModelAnnotation getMetadataForEventSource();
    public Management getMetadataForEventSource() throws SOAPException, 
    JAXBException, DatatypeConfigurationException, IOException;
//    public WsManagementEnumerationAnnotation getMetadataForSubscriptionManager();
    public Management getMetadataForSubscriptionManager() throws SOAPException, 
    JAXBException, DatatypeConfigurationException, IOException;
//    Management subscriptionManager = null;
    public void setRemoteSubscriptionManager(Management subscriptionManagerMetaData);
    
//    SubscriptionManagerInterface subscriptionManager = null;
//    /** Either set to this() or
//     * 
//     */ 
//    void setSubscriptionManager(SubscriptionManagerInterface subscriptionManager);
    
    /** Either return this() or a SubscriptionManagerInterface instance that's responsible
     * for handling subscriptionManager duties. 
     * 
     * @return SubscriptionManagerInterface
     */
//    SubscriptionManagerInterface getSubscriptionManager();
    
    
}
