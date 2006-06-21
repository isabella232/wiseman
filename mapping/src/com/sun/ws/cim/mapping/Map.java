/*
 * Copyright 2006 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: Map.java,v 1.1 2006-06-21 00:32:36 akhilarora Exp $
 */

package com.sun.ws.cim.mapping;

import com.sun.ws.cim.mapping.loader.JarResourceLoader;
import com.sun.ws.cim.mapping.loader.ResourceLoader;
import java.io.FileOutputStream;
import org.w3c.dom.Document;

public final class Map {
    
    private static final String USAGE = "Usage: classes\n" +
            "  transform one or more classes, generating xsd, wsdl and qualifiers";
    
    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println(USAGE);
            return;
        }
        
        Dom.init();
        final ResourceLoader loader = new JarResourceLoader();
        final Generator gen = new Generator(loader);
        for (final String cimClassName : args) {
            final Document xsd = Dom.newDocument();
            final Document wsdl = Dom.newDocument();
            final Document qualifiers = Dom.newDocument();
            gen.generate(cimClassName, xsd, wsdl, qualifiers);
            Dom.write(xsd, new FileOutputStream(cimClassName + ".xsd"));
            Dom.write(wsdl, new FileOutputStream(cimClassName + ".wsdl"));
            Dom.write(qualifiers, new FileOutputStream(cimClassName + ".xml"));
        }
    }
}
