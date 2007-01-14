
package framework.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
 * @author denis
 */
public class UserIteratorFactory implements IteratorFactory {

	/* This creates iterators for the "wsman:auth/user" resource.
	 * 
	 * @see com.sun.ws.management.server.IteratorFactory#newIterator(com.sun.ws.management.server.HandlerContext, 
	 * com.sun.ws.management.enumeration.Enumeration, 
	 * javax.xml.parsers.DocumentBuilder, boolean, boolean)
	 */
	public EnumerationIterator newIterator(HandlerContext context,
			Enumeration request, DocumentBuilder db, boolean includeItem,
			boolean includeEPR) throws UnsupportedFeatureFault, FaultException {
		return new UserEnumerationIterator(context, request, db, includeItem, includeEPR);
	}

	private static final ObjectFactory FACTORY = new ObjectFactory();
	
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

		private static final String RESOURCE_URI = "wsman:auth/user";
		private static final String PACKAGE = "com.hp.examples.ws.wsman.user";
		
		private int cursor = 0;
		private boolean isFiltered = false;
		private boolean cancelled = false;
		private JAXBElement[] usersList = null;

		private final XmlBinding binding;
		private final Filter filter;
		private final String address;
		private final DocumentBuilder db;
		private final boolean includeItem;
		private final boolean includeEPR;

		protected UserEnumerationIterator(final HandlerContext context, 
				final Enumeration request, 
				final DocumentBuilder db, 
				final boolean includeItem,
				final boolean includeEPR) {

			try {

				// Make sure the custom filter dialect is registered
				UserEnumerationHandler.registerUserFilterDialect();
				
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
				initUsersArray();
				
			} catch (JAXBException e) {
				e.printStackTrace();
				throw new InternalErrorFault(e.getMessage());
			} catch (SOAPException e) {
				e.printStackTrace();
				throw new InternalErrorFault(e.getMessage());
			}
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
		 * {@link com.sun.ws.management.soap.Fault Fault} is generated) or it can
		 * return {@code null} in which case a
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
		 * @param includeItem
		 *            Indicates whether items are desired, as specified by the
		 *            EnumerationMode in the Enumerate request.
		 * 
		 * @param includeEPR
		 *            Indicates whether EPRs are desired, as specified by the
		 *            EnumerationMode in the Enumerate request.
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
			cancelled = false;
			if (hasNext() == false) {
				throw new NoSuchElementException();
			}
			
			JAXBElement item = null;
			EndpointReferenceType epr = null;
			
			// Always return item if filtering is done by caller
			if ((includeItem) || (isFiltered == false)) {
				item = usersList[cursor];
			}
			if (includeEPR) {
				Map<String, String> selectorMap = new HashMap<String, String>();
				UserType user = (UserType) usersList[cursor].getValue();

				selectorMap.put("firstname", user.getFirstname());
				selectorMap.put("lastname", user.getLastname());

				epr = EnumerationSupport.createEndpointReference(address,
						RESOURCE_URI, selectorMap);
			}
			EnumerationItem ee = new EnumerationItem(item, epr);
			cursor++;
			return ee;
		}

		/**
		 * Indicates if there are more elements remaining in the iteration.
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
		 * @return {@code true} if there are more elements in the iteration,
		 *         {@code false} otherwise.
		 */
		public boolean hasNext() {
			return cursor < usersList.length;
		}

		/**
		 * Indicates if the iterator has already been filtered. This indicates that
		 * further filtering is not required by the framwork.
		 * 
		 * @return {@code true} if the iterator has already been filtered,
		 *         {@code false} otherwise.
		 */
		public boolean isFiltered() {
			return isFiltered;
		}

		/**
		 * Invoked when a {@link #next next} call exceeds the
		 * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
		 * specified in the
		 * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
		 * An implementation is expected to set a flag that causes the
		 * currently-executing {@link #next next} operation to return gracefully.
		 * 
		 * @param context
		 *            The client context that was specified to
		 *            {@link com.sun.ws.management.server.EnumerationSupport#enumerate enumerate}
		 *            is returned.
		 */
		public void cancel() {
			cancelled = true;
		}

		public int estimateTotalItems() {
			return usersList.length;
		}
		
		// Load & filter the users
		private void initUsersArray() {

			try {
				
				final UserType[] allUsers = UserStore.getUsers();
				final ArrayList<JAXBElement> filteredUsers = new ArrayList<JAXBElement>();
				int i = 0;
				
				// The following variable is used to remember if the filter
				// has a projection & requires an XmlFragment. 
				// Done for performance.
				Boolean fragmentCheck = null;
				for (; i < allUsers.length; i++) {
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
								filteredUsers.add(jaxbUser);
							} else {
								// Fragment Transfer. Create the fragment.
								JAXBElement<MixedDataType> fragment = BaseSupport.createXmlFragment(result);
								filteredUsers.add(fragment);
							}
						}
					} else {
						// EnumerationSupport will do the filtering for us
						filteredUsers.add(jaxbUser);
					}
				}
				// Create an empty array and then fill it
				usersList = new JAXBElement[filteredUsers.size()];
				filteredUsers.toArray(usersList);
			} catch (JAXBException e) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			} catch (Exception e) {
				// TODO: Fix the exception for Filter.evaluate()
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}
		}
		
		public void release() {
			usersList = new JAXBElement[0];
		}
	}
}
