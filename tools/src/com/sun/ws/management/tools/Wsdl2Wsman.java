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
 * $Id: Wsdl2Wsman.java,v 1.3 2007-05-30 20:30:19 nbeers Exp $
 */
package com.sun.ws.management.tools;

import java.io.File;

/**
 * Commandline Client for {@link Wsdl2WsmanGenerator}
 * 
 */
public class Wsdl2Wsman
{

    Wsdl2WsmanGenerator m_generator = null;

    /**
     * Command-Line Args
     */
    public static final String ARG_OUTPUT_DIR = "-o";
    public static final String ARG_WSDL = "-w";
    public static final String ARG_NO_JAXB = "-noJaxb";
    public static final String ARG_JAXB_AS_SCHEMA = "-asSchema";

    public boolean GEN_JAXB = true;
    public boolean GEN_AS_SCHEMA = false;
    private File m_outputDir = new File("").getAbsoluteFile();
    private File m_wsdl = null;


    public Wsdl2Wsman(File wsdl) throws Exception
    {
        m_wsdl = wsdl;
        m_generator = new Wsdl2WsmanGenerator(m_wsdl);
        m_generator.setGenerateJaxb(GEN_JAXB);
        m_generator.setOutputDir(m_outputDir);
        m_generator.process();
    }

    public Wsdl2Wsman(String[] args) throws Exception
    {
        if (args.length == 0)
        {
            printArgs();
        }
        parseArgs(args);
        m_generator = new Wsdl2WsmanGenerator(m_wsdl);
        m_generator.setGenerateJaxb(GEN_JAXB);
        m_generator.setOutputDir(m_outputDir);
        m_generator.setGenerateAsSchema(GEN_AS_SCHEMA);
        m_generator.process();
    }

    /**
     * Prints the commandline arguments.
     */
    private void printArgs()
    {
        System.out.println("Usage: java " + Wsdl2Wsman.class.getName() + " [ " + ARG_OUTPUT_DIR + " | " + ARG_WSDL + " | " + ARG_NO_JAXB + " | " + ARG_JAXB_AS_SCHEMA);
        System.out.println();
        System.out.println(ARG_OUTPUT_DIR + "\t Output Dir <dir path>");
        System.out.println(ARG_WSDL + "\t Wsdl File <file path> REQUIRED");
        System.out.println(ARG_NO_JAXB + "\t Do Not Generate Types Via JAXB <true> (default is false, generate JAXB)");
        System.out.println(ARG_JAXB_AS_SCHEMA + "\t Generate Types by extract schema sections from wsdl <true> (default is false, will process the wsdl file directly).  This has to do with a currently unsupported feature of JAXB. Set to true if there are issues generating.");
        System.out.println();
        System.out.println("i.e. " + "java " + Wsdl2Wsman.class.getName() + " " + ARG_OUTPUT_DIR + " c:\\temp\\foo " + ARG_WSDL + " c:\\temp\\gen " + ARG_NO_JAXB + " true " + ARG_JAXB_AS_SCHEMA + " false");
        System.out.println();
        System.exit(0);
    }


    /**
     * Parse the commandline arguments
     *
     * @param args
     */
    private void parseArgs(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            String key = args[i];
            if (i + 1 < args.length)
            {
                i++;//increment var i to get the value
                String val = args[i];
                setMemberField(key, val);
            }
            else
            {
                System.err.println("Argument is missing a value! Argument: " + args[i]);
                printArgs();
            }
        }
    }

    /**
     * Sets the internal member fields for generation.
     *
     * @param key
     * @param val
     */
    private void setMemberField(String key, String val)
    {
        if (ARG_OUTPUT_DIR.equalsIgnoreCase(key))
        {
            m_outputDir = new File(val);
            m_outputDir.mkdirs();
        }
        else if (ARG_WSDL.equalsIgnoreCase(key))
        {
            m_wsdl = new File(val);
        }
        else if (ARG_NO_JAXB.equalsIgnoreCase(key))
        {
            GEN_JAXB = Boolean.parseBoolean(val);
        }
        else if (ARG_JAXB_AS_SCHEMA.equalsIgnoreCase(key))
        {
            GEN_AS_SCHEMA = Boolean.parseBoolean(val);
        }
        else
        {
            System.err.println("Invalid Argument! Argument: " + key + " Value: " + val);
            printArgs();
        }
    }

    public static void main(String[] args) throws Exception
    {
        Wsdl2Wsman wsdl2 = new Wsdl2Wsman(args);
    }
}
