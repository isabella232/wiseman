package com.sun.ws.management.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.xml.sax.InputSource;

public class Xsd2WsdlTask extends Task {
    private static final String TEMPLATES_PATH = "templates";
    private VelocityContext context = new VelocityContext();
    String filePath=null;
    String wsdlFileName=null;
	private void setResourceName(String resourceName) {
		context.put("resource_name",resourceName);
	}


	private void setTargetNamespace(String targetNamespace) {
		context.put("target_namespace",targetNamespace);
	}

	public void setXsdFile(String xsdFile) {
		filePath=xsdFile;
		
		String xsdFileName = new File(xsdFile).getName();
		String serviceName = xsdFileName.split("\\.")[0];
		wsdlFileName=serviceName+".wsdl";
		context.put("service_name",serviceName);
		context.put("xsd_file",xsdFileName);
	}
	
	private void setElementName(String elementName){
		context.put("element_name",elementName);
		
	}

	public Xsd2WsdlTask() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	   @Override
	public void execute() throws BuildException {
	        super.execute();
	        try {
				initVelocity();
			} catch (Exception e1) {
				throw new BuildException(e1);
			}
			extractXsdInfo(new File(filePath));
			
			File outputFile = new File("wsdl", wsdlFileName);
	        
			try {//wsdlFromXsd
				processTemplate(context, TEMPLATES_PATH + "/WsdlFromXsd.vm", outputFile);
			} catch (Exception e) {
				throw new BuildException(e);
			}		   
	}


	/**
     * Initializes Velocity
     *
     * @throws Exception
     */
    private void initVelocity()
            throws Exception
    {
        // configure to use Commons Logging for logging
        //Velocity.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,CommonsLogLogSystem.class.getName(  ) );

        // don't log warnings for invalid variable references
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_REFERENCE_LOG_INVALID, "false");

        // don't load any global macro libraries (override default of "VM_global_library.vm")
        Velocity.setProperty(RuntimeConstants.VM_LIBRARY, "");

        // configure to use classpath-based resource loader
        Velocity.addProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        String resourceLoaderBaseKey = "classpath." + RuntimeConstants.RESOURCE_LOADER + ".";
        Velocity.setProperty(resourceLoaderBaseKey + "class",
                             "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.setProperty(resourceLoaderBaseKey + "cache", "false");
        Velocity.setProperty(resourceLoaderBaseKey + "modificationCheckInterval", "2");
        Velocity.init();
    }

    /**
     * Processes an individual Velocity Template
     *
     * @param context
     * @param templateLocation
     * @param outputFile
     * @throws Exception
     */
    private void processTemplate(VelocityContext context,
                                 String templateLocation,
                                 File outputFile)
            throws Exception
    {
        /*
        *  get the Template object.  This is the parsed version of your
        *  template input file.  Note that getTemplate() can throw
        *   ResourceNotFoundException : if it doesn't find the template
        *   ParseErrorException : if there is something wrong with the VTL
        *   Exception : if something else goes wrong (this is generally
        *        indicative of a serious problem...)
        */
        try
        {
            Template template = Velocity.getTemplate(templateLocation);
            
            /*
            *  Now have the template engine process your template using the
            *  data placed into the context. Think of it as a  'merge'
            *  of the template and the data to produce the output stream.
            */
            FileWriter writer = new FileWriter(outputFile);
            if (template != null)
            {
                template.merge(context, writer);
            }

            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("Error processing template " + templateLocation);
            e.printStackTrace();
        }
    }
 /**
  * 
  * 	schema
  * 		http://www.w3.org/2001/XMLSchema:element[type="usr:UserType"]
  * 
  *	/schema[targetNamespace="?"]
  *
  * @param pathToFile
  */   
    private void extractXsdInfo(File pathToXsdFile){
    	setXsdFile(pathToXsdFile.getAbsolutePath());
    	XPathFactory  factory=XPathFactory.newInstance();
    	XPath xPath=factory.newXPath();
    	XPathExpression  xPathExpressionNamespace=null;
    	XPathExpression  xPathExpressionType=null;
    	XPathExpression  xPathExpressionTypeName=null;
    	try {
			// /catalog/journal/article[@date='January-2004']/title 
    		xPathExpressionNamespace=
			    xPath.compile("/*[local-name()='schema' and namespace-uri()='http://www.w3.org/2001/XMLSchema']/@targetNamespace");
    		xPathExpressionType=
			    xPath.compile("/*[local-name()='schema' and namespace-uri()='http://www.w3.org/2001/XMLSchema']/*[local-name()='element' and namespace-uri()='http://www.w3.org/2001/XMLSchema']/@type");
    		xPathExpressionTypeName=
			    xPath.compile("/*[local-name()='schema' and namespace-uri()='http://www.w3.org/2001/XMLSchema']/*[local-name()='element' and namespace-uri()='http://www.w3.org/2001/XMLSchema']/@name");
		} catch (XPathExpressionException e) {
			throw new BuildException(e);
		}

		String namespace=getXPathValue(pathToXsdFile, xPathExpressionNamespace);
		setTargetNamespace(namespace);
		
		String type=getXPathValue(pathToXsdFile, xPathExpressionType);
		if(type.split(":").length>=2)
			setResourceName(type.split(":")[1]);
		else
			setResourceName(type);
		String elementName=getXPathValue(pathToXsdFile, xPathExpressionTypeName);
		setElementName(elementName);
    }


private String getXPathValue(File pathToXsdFile, XPathExpression xPathExpressionNamespace) {
	InputSource inputSource;
	try {
		inputSource = 
		    new InputSource(new
		         FileInputStream(pathToXsdFile));
	} catch (FileNotFoundException e) {
		throw new BuildException(e);
	}
	try {
		
			  return xPathExpressionNamespace.evaluate(inputSource);
	} catch (XPathExpressionException e) {
		throw new BuildException(e);
	}
}

}
