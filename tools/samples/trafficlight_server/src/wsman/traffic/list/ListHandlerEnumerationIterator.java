
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
    private boolean m_cancelled = false;
//    public static final org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory addressingFactory = new org.xmlsoap.schemas.ws._2004._08.addressing.ObjectFactory();
//    public static final org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory managementFactory = new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
   
	private static XmlBinding binding;
	private static final String RESOURCE_JAXB_PACKAGE = "com.sun.traffic.light.types";
	{
		try {
			binding = new XmlBinding(null,RESOURCE_JAXB_PACKAGE);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}

    /**
     * Supply the next few elements of the iteration. This is invoked to
     * satisfy a {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
     * request. The operation must return within the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
     * specified in the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request,
     * otherwise {@link #cancel cancel} will
     * be invoked and the current thread interrupted. When cancelled,
     * the implementation can return the results currently
     * accumulated (in which case no
     * {@link com.sun.ws.management.soap.FaultException Fault} is generated) or it can
     * return {@code null} in which case a
     * {@link com.sun.ws.management.enumeration.TimedOutFault TimedOutFault}
     * is returned.
     *
     * @param db A document builder that can be used to create documents into 
     * which the returned items will be placed. Note that each item must be
     * placed as the root element of a new Document for XPath filtering to work
     * properly.
     *
     * @param context The client context that was specified to
     * {@link com.sun.ws.management.server.EnumerationSupport#enumerate enumerate} is returned.
     *
     * @param startPos The starting position (cursor) for this
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @param count The number of items desired in this
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @return a List of {@link org.w3c.dom.Element Elements} that will be
     * returned in the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse PullResponse}.
     */    
    public List<EnumerationItem> next(final DocumentBuilder db, final Object context, final boolean includeItem,
     final boolean includeEPR, final int startPos, int count){
        return LightEnumerationIteratorImpl.next(binding, this, db, context, includeItem, includeEPR, startPos, count);
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
    public boolean hasNext(final Object context, final int startPos)
    {
        return LightEnumerationIteratorImpl.hasNext(context, startPos);
    }
    /**
     * Invoked when a {@link #next next} call exceeds the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
     * specified in the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
     * request. An implementation is expected to set a flag that
     * causes the currently-executing {@link #next next} operation to return
     * gracefully.
     *
     * @param context The client context that was specified to
     * {@link com.sun.ws.management.server.EnumerationSupport#enumerate enumerate} is returned.
     */
    public void cancel(final Object context)
    {
        m_cancelled = true;
    }
    
    public boolean isCancelled(){
    	return m_cancelled;
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
    public int estimateTotalItems(final Object context){
    	return LightEnumerationIteratorImpl.getSize(context);
    }
    
    /**
     * Supply the namespace mappings used by the elements of the iteration. The
     * namespace mapping is primarily used for resolution of namespace prefixes
     * during evaluation of XPath expressions for filtered enumeration.
     *
     * @return a NamespaceMap of all the namespace mappings used by the elements
     * of this iteration. An implementation can choose to return null or
     * an empty map, in which case evaluation of XPath expressions with namespace
     * prefixes may fail.
     */
    public NamespaceMap getNamespaces(){
    	return null;
    }
        
}
