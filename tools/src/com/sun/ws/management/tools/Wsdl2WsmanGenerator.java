package com.sun.ws.management.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ConsoleErrorReporter;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.ModelLoader;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import com.sun.tools.xjc.model.Model;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.transfer.Transfer;
import com.sun.xml.bind.api.impl.NameConverter;

/**
 * Wsdl2Wsman Java Generator Class
 */
public class Wsdl2WsmanGenerator
{
	private String packName;
    private boolean m_hasCustomOpNames = false;
    private boolean m_hasCustomOps = false;
    private boolean isEnumeration = false;

    //Addressing QName
    private static final QName ADDRESSING_ACTION_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "Action");
    //package prefix name for accomodating WISEMAN's handler-lookup
    public static final String WISMAN_DEFAULT_HANDLER_PACKAGE_PREFIX = "com.sun.ws.management.server.handler";
    //package suffix name for accomodating WISEMAN's handler-lookup
    public static final String HANDLER_SUFFIX = "_Handler";


    private File m_wsdl = null;
    private String m_iteratorFactoryName = null;
    private File m_outputDir = new File("").getAbsoluteFile();
    private String m_resourceType = "";
    private String m_resourceName = "";

    private Map<String, WsManOp> m_overriddenMethodMap = new HashMap<String, WsManOp>();
    private Map<String, WsManOp> m_customOperationMap = new HashMap<String, WsManOp>();
    private Map<String, String> m_specActionURIs = new HashMap<String, String>();
    private Map<String, String> m_enumerationActionURIs = new HashMap<String, String>();
    private static final String TEMPLATES_PATH = "templates";
    private HashMap<String, String> m_enumerationResponseActionURIs = new HashMap<String, String>();
    private HashMap<String, String> m_specResponseActionURIs = new HashMap<String, String>();
    
    private static boolean GEN_JAXB = true;
    private static final QName EPR_QNAME = new QName(ADDRESSING_ACTION_QNAME.getNamespaceURI(), "EndpointReference");
    private static boolean GEN_AS_SCHEMA = false;

    public Wsdl2WsmanGenerator(File m_wsdl)
    {
        this.m_wsdl = m_wsdl;
    }

    /**
     * Main processing point for the class.
     *
     * @throws Exception
     */
    public void process() throws Exception
    {
        if (m_wsdl == null)
        {
            System.err.println("The WSDL file must exist!");
            System.exit(0);
        }

        initVelocity();
        initSpecUris();
        parseWsdl(); //schema will probably be external
    }


    /**
     * ADD SPEC URIs HERE
     */
    private void initSpecUris()
    {
        //transfer
        m_specActionURIs.put(Transfer.CREATE_ACTION_URI, "create");
        m_specResponseActionURIs.put(Transfer.CREATE_ACTION_URI, Transfer.CREATE_RESPONSE_URI);
        m_specActionURIs.put(Transfer.DELETE_ACTION_URI, "delete");
        m_specResponseActionURIs.put(Transfer.DELETE_ACTION_URI, Transfer.DELETE_RESPONSE_URI);
        m_specActionURIs.put(Transfer.GET_ACTION_URI, "get");
        m_specResponseActionURIs.put(Transfer.GET_ACTION_URI, Transfer.GET_RESPONSE_URI);
        m_specActionURIs.put(Transfer.PUT_ACTION_URI, "put");
        m_specResponseActionURIs.put(Transfer.PUT_ACTION_URI, Transfer.PUT_RESPONSE_URI);

        //enumeration
        m_enumerationActionURIs.put(Enumeration.ENUMERATE_ACTION_URI, "enumerate");
        m_enumerationResponseActionURIs.put(Enumeration.ENUMERATE_ACTION_URI, Enumeration.ENUMERATE_RESPONSE_URI);
        m_enumerationActionURIs.put(Enumeration.GET_STATUS_ACTION_URI, "getStatus");
        m_enumerationResponseActionURIs.put(Enumeration.GET_STATUS_ACTION_URI, Enumeration.GET_STATUS_RESPONSE_URI);
        m_enumerationActionURIs.put(Enumeration.PULL_ACTION_URI, "pull");
        m_enumerationResponseActionURIs.put(Enumeration.PULL_ACTION_URI, Enumeration.PULL_RESPONSE_URI);
        m_enumerationActionURIs.put(Enumeration.RELEASE_ACTION_URI, "release");
        m_enumerationResponseActionURIs.put(Enumeration.RELEASE_ACTION_URI, Enumeration.RELEASE_RESPONSE_URI);
        m_enumerationActionURIs.put(Enumeration.RENEW_ACTION_URI, "renew");
        m_enumerationResponseActionURIs.put(Enumeration.RENEW_ACTION_URI, Enumeration.RENEW_RESPONSE_URI);

        m_specActionURIs.putAll(m_enumerationActionURIs);
        m_specResponseActionURIs.putAll(m_enumerationResponseActionURIs);
    }


    /**
     * Parses the wsdl and checks for action uris
     *
     * @throws javax.wsdl.WSDLException
     */
    private void parseWsdl() throws Exception
    {
        Definition definition = null;
        if (m_wsdl.exists())
        {
            definition = loadWsdlDefinition();
            m_resourceType = getResourceType();
            m_resourceName = getResourceName();
        }
        else
        {
            System.err.println("The WSDL location: " + m_wsdl.getAbsolutePath() + " does not exist!");
            System.exit(0);
        }

        if (GEN_JAXB)
        {
            generateSchema(definition);
        }
        
        generateHandlers(definition);
    }

    /**
     * Loads the Wsdl file.
     *
     * @return Definition
     * @throws WSDLException
     */
    private Definition loadWsdlDefinition()
            throws WSDLException
    {
        Definition definition = null;
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();

        if (m_wsdl.isDirectory())
        {
            System.err.println("The WSDL location: " + m_wsdl.getAbsolutePath() + " cannot be a directory!");
            System.exit(0);
        }
        else
        {
            definition = wsdlReader.readWSDL(m_wsdl.getParent(), m_wsdl.getAbsolutePath());
            if (definition == null)
            {
                System.err.println("The WSDL location: " + m_wsdl.getAbsolutePath() + " could not be loaded!");
                System.exit(0);
            }
        }
        return definition;
    }

    /**
     * Front-door to the handler-generation process.
     *
     * @param definition
     * @throws Exception
     */
    private void generateHandlers(Definition definition)
            throws Exception
    {
        Collection services = definition.getServices().values();

        if (services == null || services.size() <= 0)
        {
            System.err.println("You must provide a Service block in your wsdl!");
            System.exit(0);
        }

        for (Iterator iterator = services.iterator(); iterator.hasNext();)
        {
            Service service = (Service) iterator.next();
            Collection ports = service.getPorts().values();
            if (ports == null || ports.size() <= 0)
            {
                System.err.println("You must provide a service/port block in your wsdl!");
                System.exit(0);
            }
            for (Iterator iterator1 = ports.iterator(); iterator1.hasNext();)
            {
                Port port = (Port) iterator1.next();
                String resourceUri = null;
                List extensibilityElements = port.getExtensibilityElements();
                for (int i = 0; i < extensibilityElements.size(); i++) //look for resourceuri in epr in wsdl:port
                {
                    Object o = extensibilityElements.get(i);
                    if (o instanceof UnknownExtensibilityElement)
                    {

                        UnknownExtensibilityElement unknownElem = (UnknownExtensibilityElement) o;
                        if (EPR_QNAME.equals(unknownElem.getElementType()))
                        {
                            Element element = unknownElem.getElement();
                            if (element != null)
                            {
                                NodeList elementsByTagNameNS = element.getElementsByTagNameNS("http://schemas.xmlsoap.org/ws/2005/06/management", "ResourceURI");
                                for (int j = 0; j < elementsByTagNameNS.getLength(); j++)
                                {
                                    Node el = elementsByTagNameNS.item(j);
                                    resourceUri = el.getTextContent();
                                }
                            }
                        }
                    }
                }
                if (resourceUri == null)//no resource uri found in wsdl:port
                {
                    //generate resourceuri using the targetnamespace/servicename/portname
                    resourceUri = buildServiceUri(definition.getTargetNamespace(), service.getQName().getLocalPart(), port.getName());
                }

                generateHandlerForPort(port, resourceUri);
            }
        }
    }

    private void generateHandlerForPort(Port port, String resourceUri) throws Exception
    {
        //re-init the maps for each handler
        m_overriddenMethodMap = new HashMap<String, WsManOp>();
        m_customOperationMap = new HashMap<String, WsManOp>();
        isEnumeration = false;
        PortType portType = port.getBinding().getPortType();
        validateIsWsManWsdl(portType);
        processTemplates(resourceUri);
    }

    /**
     * Constructs a serviceURI based on the target namespace of the wsdl, service name and port name
     *
     * @param targetNamespace
     * @param serviceName
     * @param portName
     * @return A serviceURI
     */
    private String buildServiceUri(String targetNamespace, String serviceName, String portName)
    {
        return targetNamespace + "/" + serviceName + "/" + portName;
    }

    /**
     * Invokes XJC on the schema blocks found inside a Wsdl document.
     *
     * @param definition
     */
    private void generateSchema(Definition definition)
    {
    	System.out.println("GENERATING SCHEMA");
        //process each schema block found in wsdl
        if (GEN_AS_SCHEMA)
        {
            Types types = definition.getTypes();
            if (types != null)
            {
                List extensibilityElements = types.getExtensibilityElements();
                for (int i = 0; i < extensibilityElements.size(); i++)
                {
                    Object obj = extensibilityElements.get(i);
                    if (obj instanceof Schema)
                    {
                        Schema schema = (Schema) obj;

                        Element element = schema.getElement();

                        if (element != null)
                        {
                            S2JJAXBModel s2JJAXBModel = getJaxbModel(element);
                            if (s2JJAXBModel != null)
                            {
                                genJaxbCode(s2JJAXBModel);
                            }
                            else
                            {
                                System.err.println("Schema compilation failed!");
                                System.exit(0);
                            }
                        }
                    }
                }
            }
        }
        //process wsdl directly
        else
        {

            // parse additional command line params
            Options options = new Options();
            options.setSchemaLanguage(Language.WSDL);
            options.targetDir = m_outputDir;
            options.addGrammar(m_wsdl);
            //options.entityResolver = new XMLCatalog();

            ErrorReceiver errorReceiver = new ErrorReceiver()
            {
                public void error(SAXParseException exception) throws AbortException
                {
                    exception.printStackTrace();
                }

                public void fatalError(SAXParseException exception) throws AbortException
                {
                    exception.printStackTrace();
                }

                public void warning(SAXParseException exception) throws AbortException
                {
                    exception.printStackTrace();
                }

                public void info(SAXParseException exception)
                {
                    exception.printStackTrace();
                }
            };
            JCodeModel cm = new JCodeModel();
            Model model = ModelLoader.load(options, cm, errorReceiver);
            //packName=getGenPackName(m_outputDir);
            Iterator<JPackage> packit = cm.packages();
            if(packit.hasNext()){
                packName=packit.next().name();
            }
            System.out.println("PACKNAME IS "+packName);

            if (model == null)
            {
                throw new RuntimeException("unable to parse the schema. Error messages should have been provided");
            }

            try
            {

                if (model.generateCode(options, errorReceiver) == null)
                {
                    throw new RuntimeException("failed to compile a schema");
                }

                model.codeModel.build(m_outputDir);

            }
            catch (IOException e)
            {
                throw new RuntimeException("unable to write files: " + e.getMessage(), e);
            }


        }
    }

    private void genJaxbCode(S2JJAXBModel s2JJAXBModel)
    {
        JCodeModel jCodeModel = s2JJAXBModel.generateCode(null, (ErrorListener) new ConsoleErrorReporter(System.out));

        try
        {
            jCodeModel.build(m_outputDir);
            packName=getGenPackName(m_outputDir);
            System.out.println("PACKNAME IS "+packName);
			
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String getGenPackName(File dir) {
    	File currDir = getNextFolder(dir);
    	String packName="";
    	while(currDir!=null){
    		packName=packName+currDir.getName()+".";
    		currDir = getNextFolder(currDir);
    	}
		return packName.substring(packName.length()-1);
	}
    
    private File getNextFolder(File folder){
    	File[] files = folder.listFiles();
    	for (File file : files) {
			if(file.isDirectory())
				return file;
		}
    	return null;
    }

	private S2JJAXBModel getJaxbModel(Element element)
    {
        SchemaCompiler schemaCompiler = XJC.createSchemaCompiler();
        schemaCompiler.setErrorListener((ErrorListener) new ConsoleErrorReporter(System.out));
        schemaCompiler.parseSchema(m_wsdl.toURI().toString(), element);

        S2JJAXBModel s2JJAXBModel = schemaCompiler.bind();
        return s2JJAXBModel;
    }

    /**
     * Builds a SystemId for use when generating with XJC.
     *
     * @param i
     * @return SystemId
     */
    private String getSystemId(int i)
    {
        String name = m_wsdl.getName();
        return name + "#types_" + i;
    }

    /**
     * Verifys the WSDL operations contain action uris
     *
     * @param portType
     */
    private void validateIsWsManWsdl(PortType portType)
    {
        List operations = portType.getOperations();
        for (Object operation1 : operations)
        {
            Operation operation = (Operation) operation1;
            QName extensionAttribute;

            Input input = operation.getInput();
            if (input != null)
            {
                extensionAttribute = (QName) input.getExtensionAttribute(ADDRESSING_ACTION_QNAME);
                checkForActionAttribute(extensionAttribute, operation);
            }
        }
    }

    /**
     * Determines if the action uri is a spec uri or a custom uri and adds to appropriate maps
     *
     * @param extensionAttribute
     * @param operation
     */
    private void checkForActionAttribute(QName extensionAttribute, Operation operation)
    {
        if (extensionAttribute == null)
        {
            System.err.println("All WSDL Operation inputs and outputs must contain the wsa:Action attribute to facilitate request dispatching!");
            System.err.println("The Operation named: " + operation.getName() + " is missing the wsa:Action attribute for its input and/or output.");
            System.exit(0);
        }

        String actionURI = extensionAttribute.getLocalPart();
        if (m_specActionURIs.containsKey(actionURI))
        {
            WsManOp op;
            if (m_enumerationActionURIs.containsKey(actionURI))
            {
                isEnumeration = true;
                op = new WsManOp(m_enumerationResponseActionURIs.get(actionURI), getSpecOperationName(actionURI), true, operation.getName());
            }
            else
            {
                op = new WsManOp(m_specResponseActionURIs.get(actionURI), getSpecOperationName(actionURI), operation.getName());
            }
            m_overriddenMethodMap.put(actionURI, op);
        }
        else
        {
            String responseUri = actionURI;//default to the request uri
            if (operation != null)//attempt to get a response uri
            {
                QName qName = (QName) operation.getOutput().getExtensionAttribute(ADDRESSING_ACTION_QNAME);
                if (qName != null)
                {
                    responseUri = qName.getLocalPart();
                }
                m_customOperationMap.put(actionURI, new WsManOp(responseUri, operation.getName(), null));
            }
        }
    }

    /**
     * Returns the spec operation name for a given ActionUri.
     * This is used when mapping overridden operation names for spec operations
     *
     * @param actionURI
     * @return spec op name
     */
    private String getSpecOperationName(String actionURI)
    {
        return m_specActionURIs.get(actionURI);
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
        //Velocity.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
        //CommonsLogLogSystem.class.getName(  ) );

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
     * Processes all the Velocity templates
     *
     * @param resourceUri
     * @throws Exception
     */
    private void processTemplates(String resourceUri)
            throws Exception
    {

        VelocityContext context = new VelocityContext();

        if ((m_resourceType != null) && (m_resourceType.length() > 0)) {
            context.put("factoryTypeKnown", true);
            context.put("jaxbFactoryType", m_resourceType);
            String methodName = m_resourceName;
            String firstChar = methodName.substring(0, 1).toUpperCase();
            if (methodName.length() > 1)
            	methodName = "create" + firstChar + methodName.substring(1);
            else
            	methodName = "create" + firstChar;
            context.put("jaxbFactoryCreateMethod", methodName);
        } else {
            context.put("factoryTypeKnown", false);
            context.put("jaxbFactoryType", "");
            context.put("jaxbFactoryType", "");
            context.put("jaxbFactoryCreateMethod", "");
        }
        context.put("jaxbFactoryPackage", packName);
        
        // TODO: Add other custom package names
        context.put("jaxbCustomPackages", packName);

        //build directories
        File delegatePackageDir = new File(m_outputDir, getDelegatePackageName(resourceUri).replace('.', File.separatorChar));
        delegatePackageDir.mkdirs();

        String name = resourceUri.substring(resourceUri.lastIndexOf(':') + 1);
        if (name.length() > 1)
            name = name.substring(resourceUri.lastIndexOf('/') + 1);
        String firstChar = name.substring(0, 1).toUpperCase();
        if (name.length() > 1)
            name = firstChar + name.substring(1);
        else
        	name = firstChar;
        String del_package = getDelegatePackageName(resourceUri);
        String wisemanPackage = createHandlerClassName(resourceUri).substring(0, createHandlerClassName(resourceUri).lastIndexOf("."));
        String handlerName = createHandlerClassName(resourceUri).substring(createHandlerClassName(resourceUri).lastIndexOf(".") + 1);
        File wisemanPackageDir = new File(m_outputDir, wisemanPackage.replace('.', File.separatorChar));
        wisemanPackageDir.mkdirs();

        String delegatePackageName = getDelegatePackageName(resourceUri);
        String delegateName = capitalizeFirstLetter(delegatePackageName.substring(delegatePackageName.lastIndexOf(".") + 1)) + "Handler";
        String delegateClassName = delegatePackageName + "." + delegateName;

        if (isEnumeration)
        {
            m_iteratorFactoryName = name + "IteratorFactory";
            context.put("iteratorFactoryName", m_iteratorFactoryName);
        }

        context.put("name", name);
        context.put("resourceURI", resourceUri);
        context.put("package", del_package);
        context.put("wisemanPackage", wisemanPackage);
        context.put("delegateClassName", delegateClassName);
        context.put("delegateName", delegateName);
        context.put("handlerName", handlerName);

        if (m_overriddenMethodMap.size() > 0)
        {
            m_hasCustomOpNames = true;
        }
        if (m_customOperationMap.size() > 0)
        {
            m_hasCustomOps = true;
        }
        context.put("hasCustomOpNames", m_hasCustomOpNames);
        context.put("hasCustomOps", m_hasCustomOps);
        context.put("overriddenMethodMap", m_overriddenMethodMap);
        context.put("customOperationMap", m_customOperationMap);
        
        File outputFile = new File(delegatePackageDir, delegateName + ".java");

        if (isEnumeration)
        {
        	System.out.println("Generating sourcefile: " + outputFile.getName());
            processTemplate(context, TEMPLATES_PATH + "/EnumerationSupport.vm", outputFile);
            outputFile = new File(delegatePackageDir, m_iteratorFactoryName + ".java");
            System.out.println("Generating sourcefile: " + outputFile.getName());
            processTemplate(context, TEMPLATES_PATH + "/IteratorFactory.vm", outputFile);
        }
        else
        {
        	System.out.println("Generating sourcefile: " + outputFile.getName());
            processTemplate(context, TEMPLATES_PATH + "/TransferSupport.vm", outputFile);
        }

        // create ResourceURI handler
        outputFile = new File(wisemanPackageDir, handlerName + ".java");
        System.out.println("Generating sourcefile: " + outputFile.getName());
        processTemplate(context, TEMPLATES_PATH + "/Handler.vm", outputFile);

        // create binding.properties file
        outputFile = new File(m_outputDir, "binding.properties");
        System.out.println("Generating properties file: " + outputFile.getName());
        processTemplate(context, TEMPLATES_PATH + "/binding.properties.vm", outputFile);
    }

    private String capitalizeFirstLetter(String classname)
    {
        String firstPart = classname.substring(0, 1);
        firstPart = firstPart.toUpperCase();
        String lastPart = classname.substring(1);
        return firstPart + lastPart;
    }

    /**
     * Returns the Delegate's Package name given its resource uri.
     *
     * @param resourceURI
     * @return Delegate's Package name
     */
    private String getDelegatePackageName(String resourceURI)
    {
    	return NameConverter.standard.toPackageName(resourceURI);
        //return com.sun.tools.xjc.reader.Util.getPackageNameFromNamespaceURI(resourceURI);
    }

    /**
     * Returns the generated WISEMAN handler class name
     *
     * @param resourceUri
     * @return returns the handler class name
     */
    private String createHandlerClassName(String resourceUri)
    {
        String pkg = getDelegatePackageName(resourceUri);
        StringBuilder sb = new StringBuilder();
        sb.append(WISMAN_DEFAULT_HANDLER_PACKAGE_PREFIX);
        sb.append(".");
        sb.append(pkg);
        sb.append(HANDLER_SUFFIX);
        return sb.toString();
    }

    public void setGenerateJaxb(boolean gen_jaxb)
    {
        GEN_JAXB = gen_jaxb;
    }

    public void setOutputDir(File outputDir)
    {
        m_outputDir = outputDir;
    }

    public void setGenerateAsSchema(boolean asSchema)
    {
        GEN_AS_SCHEMA = asSchema;
    }
    
    // Code follows to get data type from Wiseman generated WSDL
    // Charset and decoder for ISO-8859-15
    private static Charset charset = Charset.forName("ISO-8859-15");
    private static CharsetDecoder decoder = charset.newDecoder();

    // Pattern used to parse lines
    private static Pattern linePattern = Pattern.compile(".*\r?\n");

    // Use the linePattern to break the given CharBuffer into lines, applying
    // the input pattern to each line to see if we have a match
    //
    private static String grep(final CharBuffer cb, final Pattern pattern) {
    	
		Matcher lm = linePattern.matcher(cb); // Line matcher
		Matcher pm = null; // Pattern matcher

		while (lm.find()) {
			CharSequence cs = lm.group(); // The current line
			if (pm == null)
				pm = pattern.matcher(cs);
			else
				pm.reset(cs);
			if (pm.find()) {
				return pm.group();
			}
			if (lm.end() == cb.limit())
				break;
		}
		return "";
	}

    // Search for occurrences of the input pattern in the given file
	//
	private String getResourceType() throws IOException {

		// Open the file and then get a channel from the stream
		final FileInputStream fis = new FileInputStream(m_wsdl);
		final FileChannel fc = fis.getChannel();

		// Get the file's size and then map it into memory
		final int len = (int) fc.size();
		MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, len);

		// Decode the file into a char buffer
		final CharBuffer cb = decoder.decode(bb);

		// Close the channel and the stream
		fc.close();

		final Pattern pattern;

		try {
			pattern = Pattern.compile("^resource_type=.*$");
		} catch (PatternSyntaxException x) {
			System.err.println(x.getMessage());
			return "";
		}

		// Perform the search
		String line = grep(cb, pattern);

		if ((line != null) && (line.length() > 0)) {
			String[] parts = line.split("=");
			if ((parts.length == 2) && (parts[0].equals("resource_type")))
				return parts[1];
		}
		return "";
	}
    
    // Search for occurrences of the input pattern in the given file
    //
    private String getResourceName() throws IOException {

		// Open the file and then get a channel from the stream
		final FileInputStream fis = new FileInputStream(m_wsdl);
		final FileChannel fc = fis.getChannel();

		// Get the file's size and then map it into memory
		final int len = (int) fc.size();
		final MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
				len);

		// Decode the file into a char buffer
		final CharBuffer cb = decoder.decode(bb);

		// Close the channel and the stream
		fc.close();

		final Pattern pattern;

		try {
			pattern = Pattern.compile("^resource_name=.*$");
		} catch (PatternSyntaxException x) {
			System.err.println(x.getMessage());
			return "";
		}

		// Perform the search
		final String line = grep(cb, pattern);

		if ((line != null) && (line.length() > 0)) {
			String[] parts = line.split("=");
			if ((parts.length == 2) && (parts[0].equals("resource_name")))
				return parts[1];
		}
		return "";
	}
}
