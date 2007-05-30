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
 * $Id: FileEnumerationHandler.java,v 1.4 2007-05-30 20:30:22 nbeers Exp $
 */
package framework.models;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.server.EnumerationSupport;


/**
 * File deligate is responsible for processing enumeration actions.
 *
 * @author sjc
 *
 */
public class FileEnumerationHandler extends EnumerationHandler {
	
	public static String RESOURCE_URI = "wsman:auth/file";

	static {
		try {
			EnumerationSupport.registerIteratorFactory(RESOURCE_URI,
					new FileIteratorFactory());
		} catch (Exception e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}

	public FileEnumerationHandler() {
		super();
	}
}
