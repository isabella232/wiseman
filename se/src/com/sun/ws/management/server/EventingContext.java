/*
 * Copyright 2006 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com),
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt
 **
 ***
 *** Fudan University
 *** Author: Chuan Xiao (cxiao@fudan.edu.cn)
 ***
 **$Log: not supported by cvs2svn $
 **Revision 1.8.2.1  2008/01/18 07:08:43  denis_rachal
 **Issue number:  150
 **Obtained from:
 **Submitted by:  Chuan Xiao
 **Reviewed by:
 **Eventing with Ack added to branch. (not in main).
 **
 **Revision 1.8  2007/12/20 20:47:52  jfdenise
 **Removal of ACK contribution. The contribution has been commited in the trunk instead of the branch.
 **
 **Revision 1.6  2007/05/30 20:31:04  nbeers
 **Add HP copyright header
 **
 **
 * $Id: EventingContext.java,v 1.8.2.2 2008-01-28 08:00:44 denis_rachal Exp $
 */

package com.sun.ws.management.server;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

class EventingContext extends BaseContext {

    private final EndpointReferenceType notifyTo;
    private byte[] username = null;
    private byte[] password = null;
    private String certificateThumbprint = null;

    EventingContext(final XMLGregorianCalendar expiration,
            final Filter filter,
            final EndpointReferenceType notifyTo,
            final ContextListener listener) {
        super(expiration, filter, listener);
        this.notifyTo = notifyTo;
    }

    EndpointReferenceType getNotifyTo() {
        return notifyTo;
    }
    
    void setCredentials(final byte[] username, final byte[] password) {
    	this.username = username.clone();
    	this.password = password.clone();
    }
    
    byte[] getUsername() {
    	return this.username;
    }
    
    byte[] getPassword() {
    	return this.password;
    }
    
    void setCredentials(final String certificateThumbprint) {
    	this.certificateThumbprint = certificateThumbprint;
    }
    
    String getCertificateThumbprint() {
    	return this.certificateThumbprint;
    }
}
