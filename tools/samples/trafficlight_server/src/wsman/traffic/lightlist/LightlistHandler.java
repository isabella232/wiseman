
package wsman.traffic.lightlist;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.traffic.light.model.TrafficLightModel;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;

/**
 * LightlistHandler deligate is responsible for processing enumeration actions.
 *
 * @GENERATED
 */
public class LightlistHandler extends EnumerationHandler
{    
    //Log for logging messages
    private Logger m_log = Logger.getLogger(LightlistHandler.class.getName());

    //the namespace map should be populated to handle filter requests
    private Map<String, String> m_namespaces = new HashMap<String, String> ();;
    
    private Object m_clientContext= TrafficLightModel.getModel().getList();

    public LightlistHandler()
    {
        super(new LightlistHandlerEnumerationIterator());


        setNamespaces(m_namespaces);
        /**
         * TODO set the client context object
         * The clientContext is the dataset used in the Enumeration.
         */        
        setClientContext(m_clientContext);
    }


    public void enumerateEprs(Enumeration enuRequest, Enumeration enuResponse) {

    }

    public void EnumerateOp( Enumeration enuRequest, Enumeration enuResponse )
     {
         enumerate( enuRequest, enuResponse);
     }

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
