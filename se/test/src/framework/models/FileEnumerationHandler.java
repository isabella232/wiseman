package framework.models;

import java.io.File;
import java.util.HashMap;

import com.sun.ws.management.framework.enumeration.EnumerationHandler;


/**
 * File deligate is responsible for processing enumeration actions.
 *
 * TODO
 * Review use of exceptions
 * @author sjc
 *
 */
public class FileEnumerationHandler extends EnumerationHandler
{
    public static final String NS_URI = "http://files.lookup.com";
    public static final String NS_PREFIX = "fl";

    public FileEnumerationHandler()
    {
        super(new FileEnumerationIterator());

        File dirs = new File(System.getProperty("user.dir"));
        if(dirs.exists())
        {
            setClientContext(dirs.listFiles());
        }
        HashMap namespaces = new HashMap();
        namespaces.put(NS_PREFIX, NS_URI);
        setNamespaces(namespaces);
        
    }
}
