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
