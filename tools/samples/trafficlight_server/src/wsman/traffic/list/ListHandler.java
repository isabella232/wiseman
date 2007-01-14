
package wsman.traffic.list;

import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.HandlerContext;

/**
 * ListHandler deligate is responsible for processing enumeration actions.
 *
 * @GENERATED
 */
public class ListHandler extends EnumerationHandler
{    
    //Log for logging messages
    private Logger log = Logger.getLogger(ListHandler.class.getName());

    public ListHandler()
    {
        super();
    }

	public EnumerationIterator createIterator(final HandlerContext context,
			final Enumeration enuRequest, final Enumeration enuResponse) throws SOAPException, JAXBException {
		
		EnumerationExtensions ext = new EnumerationExtensions(enuRequest);
		final boolean includeItem;
        final boolean includeEPR;
		final EnumerationModeType mode = ext.getModeType();
		
		if (mode == null) {
			includeItem = true;
			includeEPR = false;
		} else {
			final String modeString = mode.value();
			if (modeString.equals(EnumerationExtensions.Mode.EnumerateEPR
					.toString())) {
				includeItem = false;
				includeEPR = true;
			} else if (modeString
					.equals(EnumerationExtensions.Mode.EnumerateObjectAndEPR
							.toString())) {
				includeItem = true;
				includeEPR = true;
			} else {
				throw new InternalErrorFault("Unsupported enumeration mode: "
						+ modeString);
			}
		} 
		return new ListHandlerEnumerationIterator(context.getURL(), includeEPR);
	}
	
     public void EnumerateOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse )
     {
         enumerate( context,enuRequest, enuResponse);
     }

	public void ReleaseOp(HandlerContext context, Enumeration enuRequest, Enumeration enuResponse )
     {
         release( context,enuRequest, enuResponse);
     }

     public void PullOp( HandlerContext context,Enumeration enuRequest, Enumeration enuResponse )
     {
         pull( context,enuRequest, enuResponse);
     }

     public void GetStatusOp( HandlerContext context,Enumeration enuRequest, Enumeration enuResponse )
     {
         getStatus(context, enuRequest, enuResponse);
     }

     public void RenewOp( HandlerContext context,Enumeration enuRequest, Enumeration enuResponse )
     {
         renew(context, enuRequest, enuResponse);
     }
}
