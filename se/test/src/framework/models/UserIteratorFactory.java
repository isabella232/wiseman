
package framework.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.OptionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidOptionsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.BaseSupport;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.Filter;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.IteratorFactory;
import com.sun.ws.management.soap.FaultException;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.xml.XmlBinding;

/* This enumeration factory is for the "wsman:auth/user" resource.
 * It handles creation of an iterator for use to access the "user" resources.
 * @see com.sun.ws.management.server.IteratorFactory#newIterator(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.enumeration.Enumeration, javax.xml.parsers.DocumentBuilder, boolean, boolean)
 *
 */
public class UserIteratorFactory implements IteratorFactory {
	
	private static final ObjectFactory FACTORY = new ObjectFactory();
	
	private final String resourceURI;

	public UserIteratorFactory(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	
    /**
     * This creates iterators for the "wsman:auth/user" resource.
     * 
     * @param context the HandlerContext
     * @param request the Enumeration request that this iterator is to fufill
     * @param db the DocumentBuilder to use for items created by this iterator
     * @param includeItem if true the requester wants the item returned, otherwise
     * just the EPR if includeEPR is true
     * @param includeEPR if true the requestor wants the EPR for each item returned, otherwise
     * just the item if includeItem is true. If EPRs are not supported by the iterator,
     * the iterator should throw an UnsupportedFeatureFault.
     * 
     * @throws com.sun.ws.management.UnsupportedFeatureFault If EPRs are not supported.
     * @throws com.sun.ws.management.soap.FaultException If a WS-MAN protocol related exception occurs.
     * @returns An enumeration iterator for the request
     */
	public EnumerationIterator newIterator(HandlerContext context,
			Enumeration request, DocumentBuilder db, boolean includeItem,
			boolean includeEPR) throws UnsupportedFeatureFault, FaultException {
		registerUserFilterDialect();
		return new UserEnumerationIterator(context, request, db, includeItem, includeEPR);
	}

	/** Returns the resourceURI associated with this Factory Iterator.
	 * 
	 * @returns string representing the resource URI
	 */
	public String getResourceURI() {
		return resourceURI;
	}
	
    private static void registerUserFilterDialect() {

		if (BaseSupport.isSupportedDialect(UserFilterFactory.DIALECT) == false) {
			try {
				BaseSupport.addSupportedFilterDialect(
						UserFilterFactory.DIALECT, new UserFilterFactory());
			} catch (Exception ex) {
				throw new IllegalArgumentException("Exception " + ex);
			}
		}
	}
	
	// The Iterator implementation follows
	
	/**
	 * The class to be presented by a data source that would like to be enumerated.
	 * 
	 * Implementations of this class are specific to the data structure being
	 * enumerated.
	 * 
	 * @see com.sun.ws.management.server.EnumerationIterator
	 */
	public class UserEnumerationIterator implements EnumerationIterator {

		private static final String PACKAGE = "com.hp.examples.ws.wsman.user";
		
		private boolean isFiltered = false;
		private int length = -1;
		private Iterator<EnumerationItem> usersList;

		private final XmlBinding binding;
		private final Filter filter;
		private final String address;
		private final DocumentBuilder db;
		private final boolean includeItem;
		private final boolean includeEPR;

		/**
		 * Constructor for UserEnumerationIterator
		 * 
		 * @param context HandlerContext for authorizing user
		 * @param request User enumeration request
		 * @param db DocumentBuilder to use when creating items
		 * @param includeItem item required
		 * @param includeEPR epr required
		 */
		protected UserEnumerationIterator(final HandlerContext context, 
				final Enumeration request, 
				final DocumentBuilder db, 
				final boolean includeItem,
				final boolean includeEPR) {

			try {
				
				// parse request object to retrieve filter parameters entered.
				this.filter = EnumerationSupport.createFilter(request);
				this.binding = request.getXmlBinding(); // new XmlBinding(null, new String[] { PACKAGE });
				
				this.db = db;
				this.address = context.getURL();
				this.includeItem = includeItem;
				this.includeEPR = includeEPR;

				// Check our options
				Management mgmt = new Management(request);
				Set<OptionType> options = mgmt.getOptions();
				
				if ((options != null) && (options.size() > 0)) {
				    Iterator iter = options.iterator();
				    while (iter.hasNext()) {
				    	OptionType opt = (OptionType)iter.next();
				    	if (opt.getName().equals("useHandlerFilter")) {
				    		isFiltered = Boolean.valueOf(opt.getValue());
				    	} else if (opt.getName().equals("opt1")) {
				    		// Ignore for now.
				    		// Passed in by testing, but has not affect.
				    	} else if (opt.getName().equals("opt2")) {
				    		// Ignore for now.
				    		// Passed in by testing, but has not affect.
				    	} else if (opt.getName().equals("opt3")) {
				    		// Ignore for now.
				    		// Passed in by testing, but has not affect.
				    	} else {
				    		if (opt.isMustComply() == true) {
				    			// Not a recognized option
				    			throw new InvalidOptionsFault(InvalidOptionsFault.Detail.INVALID_NAME);
				    		}
				    	}
				    }
				}
				
				// load global users.store from jar
				List<EnumerationItem> list = getUsersList();
				this.length = list.size();
				this.usersList = list.iterator();
				
			} catch (JAXBException e) {
				e.printStackTrace();
				throw new InternalErrorFault(e.getMessage());
			} catch (SOAPException e) {
				e.printStackTrace();
				throw new InternalErrorFault(e.getMessage());
			}
		}

	    /**
	     * Supply the next element of the iteration. This is invoked to
	     * satisfy a {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
	     * request. The operation must return within the
	     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
	     * specified in the
	     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request,
	     * otherwise {@link #release release} will
	     * be invoked and the current thread interrupted. When cancelled,
	     * the implementation can return the result currently
	     * accumulated (in which case no
	     * {@link com.sun.ws.management.soap.Fault Fault} is generated) or it can
	     * return {@code null} in which case a
	     * {@link com.sun.ws.management.enumeration.TimedOutFault TimedOutFault}
	     * is returned.
	     *
	     * @return an {@link EnumerationElement Elements} that is used to
	     * construct proper responses for a
	     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse PullResponse}.
	     */
		public EnumerationItem next() {
			if (hasNext() == false) {
				throw new NoSuchElementException();
			}
			EnumerationItem result = usersList.next();
			return result; 
		}

	    /**
	     * Indicates if there are more elements remaining in the iteration.
	     * 
	     * @return {@code true} if there are more elements in the iteration,
	     * {@code false} otherwise.
	     */
		public boolean hasNext() {
			return usersList.hasNext();
		}

	    /**
	     * Indicates if the iterator has already been filtered.
	     * This indicates that further filtering is not required
	     * by the framwork.
	     * 
	     * @return {@code true} if the iterator has already been filtered,
	     * {@code false} otherwise.
	     */
		public boolean isFiltered() {
			return isFiltered;
		}

	    /**
	     * Estimate the total number of elements available.
	     *
	     * @return an estimate of the total number of elements available
	     * in the enumeration.
	     * Return a negative number if an estimate is not available.
	     */
		public int estimateTotalItems() {
			return this.length;
		}
		
	    /**
	     * Release any resources being used by the iterator. Calls
	     * to other methods of this iterator instance will exhibit
	     * undefined behaviour, after this method completes.
	     */
		public void release() {
			final ArrayList<EnumerationItem> users = new ArrayList<EnumerationItem>(0);
			length = 0;
			usersList = users.iterator();
		}
		
		// Load & filter the users
		private List<EnumerationItem> getUsersList() {

			try {
				
				final UserType[] allUsers = UserStore.getUsers();
				final ArrayList<EnumerationItem> filteredUsers = new ArrayList<EnumerationItem>();
				int i = 0;
				
				// The following variable is used to remember if the filter
				// has a projection & requires an XmlFragment. 
				// Done for performance.
				Boolean fragmentCheck = null;
				for (; i < allUsers.length; i++) {
					final JAXBElement item;
					final UserType user = allUsers[i];
					final JAXBElement<UserType> jaxbUser = FACTORY.createUser(user);

					// Check if we should do the filtering
					if ((isFiltered) && (filter != null)) {
						// Convert to XML doc to run filtering on
						Document content = db.newDocument();

						binding.marshal(jaxbUser, content);
						final Element element = content.getDocumentElement();;
						final NodeList result = (NodeList) filter.evaluate(element);
						if ((result != null) && (result.getLength() > 0)) {
							// Then add this UserType instance
							if (fragmentCheck == null) {
								// Only check this one
								// If 'result' is same as the 'item' then this is not a fragment selection
								fragmentCheck = new Boolean(!result.item(0).equals(element));
							}
							if (fragmentCheck == false) {
								// Whole node was selected
								item = jaxbUser;
							} else {
								// Fragment Transfer. Create the fragment.
								JAXBElement<MixedDataType> fragment = BaseSupport.createXmlFragment(result);

                                item = fragment;
							}
						} else {
							item = null;
						}
					} else {
						// EnumerationSupport will do the filtering for us
						item = jaxbUser;
					}
					if (item != null) {
						Map<String, String> selectorMap = new HashMap<String, String>();

						selectorMap.put("firstname", jaxbUser.getValue().getFirstname());
						selectorMap.put("lastname", jaxbUser.getValue().getLastname());

						EndpointReferenceType epr = EnumerationSupport.createEndpointReference(address,
								resourceURI, selectorMap);
						filteredUsers.add(new EnumerationItem(item, epr));
					}
				}
				// Return the filtered list
				return filteredUsers;
			} catch (JAXBException e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			} catch (Exception e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}
		}
	}
}
