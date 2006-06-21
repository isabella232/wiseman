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
 * $Id: PrettyPrint.java,v 1.1 2006-06-21 00:32:36 akhilarora Exp $
 */

package com.sun.ws.cim.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

public final class PrettyPrint {
    
    public static void main(final String[] args) throws Exception {
        Dom.init();
        for (final String filename : args) {
            final File file = new File(filename);
            if (file.isDirectory()) {
                for (final File f : file.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return (name.startsWith("CIM_") ||
                                name.startsWith("PRS_")) &&
                                name.endsWith(".xml");
                    }
                })) {
                    try {
                        beautify(f);
                    } catch (SAXParseException sxp) {
                        System.out.println("  *** failed at line " +
                                sxp.getLineNumber() + " " +
                                sxp.getMessage());
                    }
                }
            } else {
                beautify(file);
            }
        }
    }
    
    private static void beautify(final File file) throws Exception {
        System.out.println("Beautifying " + file.toString());
        final Document in = Dom.read(new FileInputStream(file));
        Dom.write(in, new FileOutputStream(file));
    }
}
