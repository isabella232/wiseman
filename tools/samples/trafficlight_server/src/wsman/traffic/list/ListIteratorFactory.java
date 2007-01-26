package wsman.traffic.list;

import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import com.sun.traffic.light.types.ObjectFactory;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.IteratorFactory;
import com.sun.ws.management.soap.FaultException;

public class ListIteratorFactory implements IteratorFactory {

    // Common variables shared by all Iterator instances
    public final String resourceURI;
    public final ObjectFactory factory;
    
    /**
     * Standard constructor.
     *
     * @param the resourceURI for this Iterator Factory.
     */
     ListIteratorFactory(final String resourceURI) {
		this.resourceURI = resourceURI;
		this.factory = new com.sun.traffic.light.types.ObjectFactory();
	}
     
	/**
	 * EnumerationIterator creation.
	 * 
	 * @param context
	 *            the HandlerContext
	 * @param request
	 *            the Enumeration request that this iterator is to fufill
	 * @param db
	 *            the DocumentBuilder to use for items created by this iterator
	 * @param includeItem
	 *            if true the requester wants the item returned, otherwise just
	 *            the EPR if includeEPR is true
	 * @param includeEPR
	 *            if true the requestor wants the EPR for each item returned,
	 *            otherwise just the item if includeItem is true. If EPRs are
	 *            not supported by the iterator, the iterator should throw an
	 *            UnsupportedFeatureFault.
	 * 
	 * @throws com.sun.ws.management.UnsupportedFeatureFault
	 *             If EPRs are not supported.
	 * @throws com.sun.ws.management.soap.FaultException
	 *             If a WS-MAN protocol related exception occurs.
	 * @return An enumeration iterator for the request
	 */
	public EnumerationIterator newIterator(final HandlerContext context,
			final Enumeration request, final DocumentBuilder db,
			final boolean includeItem, final boolean includeEPR)
			throws UnsupportedFeatureFault, FaultException {
		return new ListHandlerEnumerationIterator(context.getURL(), includeEPR);
	}

	/**
	 * The class to be presented by a data source that would like to be
	 * enumerated.
	 * 
	 * Implementations of this class are specific to the data structure being
	 * enumerated.
	 * 
	 * @see EnumerationIterator
	 */
	public class ListHandlerEnumerationIterator implements EnumerationIterator {
		// Log for logging messages
		private final Logger log;

		private final LightEnumerationIteratorImpl impl;

		ListHandlerEnumerationIterator(final String address,
				final boolean includeEPR) {
			this.log = Logger.getLogger(ListIteratorFactory.class.getName());
			this.impl = new LightEnumerationIteratorImpl(address, includeEPR);
		}

		/**
		 * Supply the next element of the iteration. This is invoked to satisfy
		 * a {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
		 * request. The operation must return within the
		 * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
		 * specified in the
		 * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
		 * request, otherwise {@link #release release} will be invoked and the
		 * current thread interrupted. When cancelled, the implementation can
		 * return the result currently accumulated (in which case no
		 * {@link com.sun.ws.management.soap.Fault Fault} is generated) or it
		 * can return {@code null} in which case a
		 * {@link com.sun.ws.management.enumeration.TimedOutFault TimedOutFault}
		 * is returned.
		 * 
		 * @return an {@link EnumerationElement Elements} that is used to
		 *         construct proper responses for a
		 *         {@link org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse PullResponse}.
		 */
		public EnumerationItem next() {
			return impl.next();
		}

		/**
		 * Indicates if there are more elements remaining in the iteration.
		 * 
		 * @return {@code true} if there are more elements in the iteration,
		 *         {@code false} otherwise.
		 */
		public boolean hasNext() {
			return impl.hasNext();
		}

		/**
		 * Indicates if the iterator has already been filtered. This indicates
		 * that further filtering is not required by the framwork.
		 * 
		 * @return {@code true} if the iterator has already been filtered,
		 *         {@code false} otherwise.
		 */
		public boolean isFiltered() {
			return false;
		}

		/**
		 * Estimate the number of elements available.
		 * 
		 * @param context
		 *            The client context that was specified to
		 *            {@link EnumerationSupport#enumerate enumerate} is
		 *            returned.
		 * 
		 * @return an estimate of the number of elements available in the
		 *         enumeration. Return a negative number if an estimate is not
		 *         available.
		 */
		public int estimateTotalItems() {
			return impl.estimateTotalItems();
		}

		/**
		 * Release any resources being used by the iterator. Calls to other
		 * methods of this iterator instance will exhibit undefined behaviour,
		 * after this method completes.
		 */
		public void release() {
			impl.release();
		}
	}
}
