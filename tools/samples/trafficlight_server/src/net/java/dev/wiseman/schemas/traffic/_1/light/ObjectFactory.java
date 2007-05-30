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
 * $Id: ObjectFactory.java,v 1.2 2007-05-30 20:30:19 nbeers Exp $
 */

package net.java.dev.wiseman.schemas.traffic._1.light;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.java.dev.wiseman.schemas.traffic._1.light package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Trafficlight_QNAME = new QName("http://schemas.wiseman.dev.java.net/traffic/1/light.xsd", "trafficlight");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.java.dev.wiseman.schemas.traffic._1.light
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TrafficLightType }
     * 
     */
    public TrafficLightType createTrafficLightType() {
        return new TrafficLightType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrafficLightType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.wiseman.dev.java.net/traffic/1/light.xsd", name = "trafficlight")
    public JAXBElement<TrafficLightType> createTrafficlight(TrafficLightType value) {
        return new JAXBElement<TrafficLightType>(_Trafficlight_QNAME, TrafficLightType.class, null, value);
    }

}