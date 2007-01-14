
package wsman.traffic.list;

import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.xml.XmlBinding;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import java.util.logging.Logger;
import java.util.List;

/**
 * The class to be presented by a data source that would like to be
 * enumerated.
 * 
 * Implementations of this class are specific to the data structure being
 * enumerated.
 *  
 * @see EnumerationIterator
 */
public class ListHandlerEnumerationIterator implements EnumerationIterator
{
    //Log for logging messages
    private Logger log = Logger.getLogger(ListHandlerEnumerationIterator.class.getName());   
    private LightEnumerationIteratorImpl impl;

	ListHandlerEnumerationIterator(final String address,
			final boolean includeEPR) {
		this.impl = new LightEnumerationIteratorImpl(address, includeEPR);
	}
    /**
	 * Supply the next few elements of the iteration. This is invoked to satisfy
	 * a {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
	 * The operation must return within the
	 * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
	 * specified in the
	 * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request,
	 * otherwise {@link #cancel cancel} will be invoked and the current thread
	 * interrupted. When cancelled, the implementation can return the results
	 * currently accumulated (in which case no
	 * {@link com.sun.ws.management.soap.FaultException Fault} is generated) or
	 * it can return {@code null} in which case a
	 * {@link com.sun.ws.management.enumeration.TimedOutFault TimedOutFault} is
	 * returned.
	 * 
	 * @param db
	 *            A document builder that can be used to create documents into
	 *            which the returned items will be placed. Note that each item
	 *            must be placed as the root element of a new Document for XPath
	 *            filtering to work properly.
	 * 
	 * @param context
	 *            The client context that was specified to
	 *            {@link com.sun.ws.management.server.EnumerationSupport#enumerate enumerate}
	 *            is returned.
	 * 
	 * @param startPos
	 *            The starting position (cursor) for this
	 *            {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
	 *            request.
	 * 
	 * @param count
	 *            The number of items desired in this
	 *            {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
	 *            request.
	 * 
	 * @return a List of {@link org.w3c.dom.Element Elements} that will be
	 *         returned in the
	 *         {@link org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse PullResponse}.
	 */    
    public EnumerationItem next() {
        return impl.next();
    }
    /**
     * Indicates if there are more elements remaining in the iteration.
     *
     * @param context The client context that was specified to
     * {@link com.sun.ws.management.server.EnumerationSupport#enumerate enumerate} is returned.
     *
     * @param startPos The starting position (cursor) for this
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @return {@code true} if there are more elements in the iteration,
     * {@code false} otherwise.
     */
    public boolean hasNext()
    {
        return impl.hasNext();
    }
    
    public boolean isFiltered() {
        return false;
    }
    
    /**
     * Estimate the number of elements available.
     *
     * @param context The client context that was specified to
     * {@link EnumerationSupport#enumerate enumerate} is returned.
     *
     * @return an estimate of the number of elements available in the enumeration.
     * Return a negative number if an estimate is not available.
     */
    public int estimateTotalItems(){
    	return impl.estimateTotalItems();
    }
	public void release() {
		impl.release();	
	}       
}
