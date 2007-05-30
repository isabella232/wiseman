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
 * $Id: Wsdl2WsmanTask.java,v 1.2 2007-05-30 20:30:19 nbeers Exp $
 */
package com.sun.ws.management.tools;

import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.BuildException;

import java.io.File;

/**
 * An Ant Task for Generating Java Artifacts from a Wsdl File.
 */
public class Wsdl2WsmanTask extends MatchingTask
{
    private boolean m_generateJaxb = true;
    private File m_outputDir = new File("").getAbsoluteFile();
    private File m_wsdlFile = null;
    private boolean m_processAsSchema = false;

    public void setGenerateJaxb(boolean flag)
    {
        m_generateJaxb = flag;
    }

    public void setOutputDir(File output)
    {
        m_outputDir = output;
    }

    public void setWsdlFile(File wsdl)
    {
        m_wsdlFile = wsdl;
    }

    public void setProcessAsSchema(boolean flag)
    {
        m_processAsSchema = flag;
    }


    public void execute() throws BuildException
    {
        super.execute();
        Wsdl2WsmanGenerator wsdl2WsmanGenerator = new Wsdl2WsmanGenerator(m_wsdlFile);
        wsdl2WsmanGenerator.setGenerateJaxb(m_generateJaxb);
        wsdl2WsmanGenerator.setOutputDir(m_outputDir);
        wsdl2WsmanGenerator.setGenerateAsSchema(m_processAsSchema);
        try
        {
            wsdl2WsmanGenerator.process();
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
}
