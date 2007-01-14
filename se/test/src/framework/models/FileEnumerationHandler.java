package framework.models;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.server.EnumerationSupport;


/**
 * File deligate is responsible for processing enumeration actions.
 *
 * @author sjc
 *
 */
public class FileEnumerationHandler extends EnumerationHandler {
	
	public static String RESOURCE_URI = "wsman:auth/file";

	static {
		try {
			EnumerationSupport.registerIteratorFactory(RESOURCE_URI,
					new FileIteratorFactory());
		} catch (Exception e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}

	public FileEnumerationHandler() {
		super();
	}
}
