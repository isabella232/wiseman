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
 * $Id: TraficLightObserver.java,v 1.2 2007-05-30 20:30:27 nbeers Exp $
 */
package org.publicworks.light.model;

import java.util.Observable;

import com.sun.ws.management.framework.eventing.EventingObserver;

public class TraficLightObserver implements EventingObserver {

	public TraficLightObserver() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Use this method to initalize this observer by hooking it into your model.
	 *
	 */
	public void init() {
		

	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

}
