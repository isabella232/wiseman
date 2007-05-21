package com.sun.ws.management.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.xml.sax.InputSource;

public class Xsd2WsdlTask extends Task {
    private static final String TEMPLATES_PATH = "templates";
    private VelocityContext context = new VelocityContext();
    private String outputDir = "wsdls";
    private String filePath=null;
    private String wsdlFileName=null;
    private String resourceURI=null;
    private String wsdlTemplate=null;
    private String resourceName=null;
    private String resourceType=null;
    private String targetNamespace=null;
    private boolean singleHandler=true;
    private String serviceAddress=null;
    
    
    public void setResourceType(String resourceType) {
    	this.resourceType = resourceType;
	}

	public void setResourceName(String resourceName) {
		this.resourceName=resourceName;
	}
	
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace=targetNamespace;
	}

	public void setXsdFile(String xsdFile) {
		filePath=xsdFile;
		
		String xsdFileName = new File(xsdFile).getName();
		String serviceName = xsdFileName.split("\\.")[0];
		wsdlFileName=serviceName+".wsdl";
		context.put("service_name",serviceName);
		context.put("xsd_file",new File(xsdFile).toURI().toString());
	}
	
	public void setWsdlTemplate(String template) {
		this.wsdlTemplate = template;
	}
	
    public void setOutputDir(String output)
    {
        this.outputDir = output;
    }
	
    public void setSingleHandler(boolean singleHandler)
    {
        this.singleHandler = singleHandler;
    }
	
    public void setResourceURI(String resourceURI)
    {
        this.resourceURI = resourceURI;
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
			
			// Build the resourceURI if not specified
			if (resourceURI == null) {
				URI uri = URI.create((String)context.get("target_namespace"));
				resourceURI = "urn:" + uri.getHost() + 
				              "/" + uri.getPath() + 
				              "/" + context.get("service_name");
			}
			context.put("resource_uri", resourceURI);
			

			File outputFile = new File(outputDir, wsdlFileName);
	        
			try {//wsdlFromXsd
				if (wsdlTemplate == null) {
					if (this.singleHandler) {
						wsdlTemplate = TEMPLATES_PATH + "/WsdlFromXsd.vm";
					} else {
						wsdlTemplate = TEMPLATES_PATH + "/WsdlFromXsdMultiHandler.vm";
					}
				}
				processTemplate(context, wsdlTemplate, outputFile);
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
        if (wsdlTemplate != null) {
        	Velocity.setProperty( RuntimeConstants.RESOURCE_LOADER, "file" );
        	Velocity.setProperty( "file.resource.loader.path", "./" );

        } else {
            Velocity.addProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            String resourceLoaderBaseKey = "classpath." + RuntimeConstants.RESOURCE_LOADER + ".";
            Velocity.setProperty(resourceLoaderBaseKey + "class",
                             "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.setProperty(resourceLoaderBaseKey + "cache", "false");
            Velocity.setProperty(resourceLoaderBaseKey + "modificationCheckInterval", "2");
        }
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

		if (this.targetNamespace == null) {
			this.targetNamespace=getXPathValue(pathToXsdFile, xPathExpressionNamespace);
		}
		context.put("target_namespace", this.targetNamespace);
		
		String type=getXPathValue(pathToXsdFile, xPathExpressionType);
		if (this.resourceType == null) {
			if (type.split(":").length >= 2)
				this.resourceType = type.split(":")[1];
			else
				this.resourceType = type;
		}
		context.put("resource_type", this.resourceType);
		if (this.resourceName == null) {
		    this.resourceName=getXPathValue(pathToXsdFile, xPathExpressionTypeName);
		}
		context.put("resource_name", this.resourceName);
		
		if (this.serviceAddress == null) {
            // Generate a reasonable default address
			String localhost;
			try {
				localhost = InetAddress.getLocalHost().getCanonicalHostName();
			} catch (UnknownHostException e) {
				localhost = "localhost";
		}
		this.serviceAddress="http://" + localhost + ":8080/" 
           + context.get("service_name") + "/";
		}
		context.put("service_address", this.serviceAddress);
		
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


public void setServiceAddress(String serviceAddress) {
	this.serviceAddress = serviceAddress;
}

}
