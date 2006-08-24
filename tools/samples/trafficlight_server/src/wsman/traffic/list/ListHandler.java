
package wsman.traffic.list;

import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.Management;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.publicworks.light.model.TrafficLightModel;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;

/**
 * ListHandler deligate is responsible for processing enumeration actions.
 *
 * @GENERATED
 */
public class ListHandler extends EnumerationHandler
{    
    //Log for logging messages
    private Logger log = Logger.getLogger(ListHandler.class.getName());

    //the namespace map should be populated to handle filter requests
    private Map<String, String> namespaces = null;
    
    //private Object m_clientContext;//TODO initialize this

    public ListHandler()
    {
        super(new ListHandlerEnumerationIterator());


        setNamespaces(namespaces);
        /**
         * TODO set the client context object
         * The clientContext is the dataset used in the Enumeration.
         */
        // All you have to do in this class is set the context to some object
        // that the Interator knows how to use.
        setClientContext( TrafficLightModel.getModel().getList());
    }


     public void EnumerateOp( Enumeration enuRequest, Enumeration enuResponse )
     {
         enumerate( enuRequest, enuResponse);
     }
     
     
     // If you don't implement one of these then all enumeration requests
     // will return unsupported. We are enumerating EPRs
     @Override
	public void enumerateEprs(Enumeration enuRequest, Enumeration enuResponse) {
		// Do nothing here. We don't require any setup code for EPR enumeration
    	// Default behavior is to return unsupported.
		// super.enumerateEprs(enuRequest, enuResponse);
	}

// Uncomment these to support these other modes
//	@Override
//	public void enumerateObjects(Enumeration enuRequest, Enumeration enuResponse) {
//		//  Auto-generated method stub
//		super.enumerateObjects(enuRequest, enuResponse);
//	}
//
//
//	@Override
//	public void enumerateObjectsAndEprs(Enumeration enuRequest, Enumeration enuResponse) {
//		//  Auto-generated method stub
//		super.enumerateObjectsAndEprs(enuRequest, enuResponse);
//	}


	public void ReleaseOp( Enumeration enuRequest, Enumeration enuResponse )
     {
         release( enuRequest, enuResponse);
     }

     public void PullOp( Enumeration enuRequest, Enumeration enuResponse )
     {
         pull( enuRequest, enuResponse);
     }

     public void GetStatusOp( Enumeration enuRequest, Enumeration enuResponse )
     {
         getStatus( enuRequest, enuResponse);
     }

     public void RenewOp( Enumeration enuRequest, Enumeration enuResponse )
     {
         renew( enuRequest, enuResponse);
     }




}
