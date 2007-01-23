package framework.models;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import com.sun.ws.management.framework.enumeration.EnumerationHandler;

/**
 * Example User delegate is responsible for processing CRUD & enumeration
 * actions.
 * 
 * @author dr
 * 
 */
public class UserEnumerationHandler extends EnumerationHandler {
	
    // Log for logging messages
    @SuppressWarnings("unused")
	private static final Logger LOG = 
		Logger.getLogger(EnumerationHandler.class.getName());
	
	public static String RESOURCE_URI= "wsman:auth/userenum";
	public static final String NS_URI = "http://examples.hp.com/ws/wsman/user";
	public static final String NS_PREFIX = "user";
	
    public static final QName USER = new QName(NS_URI, "user", NS_PREFIX);
	 
	public UserEnumerationHandler() {
		super();
	}
    
	/* TODO: Implement CRUD operations
	 * This part is a work in proress.
	 * @see com.sun.ws.management.framework.transfer.TransferSupport#create(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.Management, com.sun.ws.management.Management)

	
	@Override
	public void create(HandlerContext context, Management request,
			Management response) {
		try {
			// Find an existing instance
			// final UserType current = findInstance(request);

			final TransferExtensions transfer = new TransferExtensions(request);
			final TransferExtensions transferResponse = new TransferExtensions(
					response);

			// Get the resource to create
			final JAXBElement resource = (JAXBElement) transfer
					.getResource(UserStore.USER);
			if (resource == null) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}

			final UserType user;
			if (resource.getDeclaredType().equals(UserType.class)) {
				// standard create
				user = (UserType) resource.getValue();
			} else if (resource.getDeclaredType().equals(MixedDataType.class)) {
				// TODO: fragment create
				user = null;
			} else {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}
			UserStore.add(user);

			// TODO: create thre response
			// transferResponse.setFragmentCreateResponse(fragmentHeader,
			// requestContent, expression, nodes, epr)

		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		}
	}

	@Override
	public void get(HandlerContext context, Management request,
			Management response) {

		try {
			// Find an existing instance
			final JAXBElement<UserType> resource = FACTORY
					.createUser(findInstance(request));

			final TransferExtensions transfer = new TransferExtensions(request);
			final TransferExtensions transferResponse = new TransferExtensions(
					response);

			transferResponse.setFragmentGetResponse(transfer
					.getFragmentHeader(), resource);

		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		}
	}

	@Override
	public void put(HandlerContext context, Management request,
			Management response) {

		try {
			// Find an existing instance of this object in the model
			final UserType user = findInstance(request);
			final TransferExtensions transfer = new TransferExtensions(request);

			// Get the resource update transmitted in the body
			final JAXBElement resource = (JAXBElement) transfer
					.getResource(UserStore.USER);
			if (resource == null) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}

			final UserType update;
			if (resource.getDeclaredType().equals(UserType.class)) {
				// standard put
				update = (UserType) resource.getValue();
			} else if (resource.getDeclaredType().equals(MixedDataType.class)) {
				// fragment put
				MixedDataType fragment = (MixedDataType) resource.getValue();

				// ns9:user is flat allowing a simplistic approach here.
				// extract the xml nodes specified in the fragment object.
				// TODO: This does NOT support 'text()' in the xpath expression

				// extract the update fragments
				List<Object> nodes = fragment.getContent();
				if (nodes == null) {
					throw new InvalidRepresentationFault(
							InvalidRepresentationFault.Detail.INVALID_VALUES);
				}
				Iterator iter = nodes.iterator();
				update = new UserType();
				while (iter.hasNext()) {
					Element elem = (Element) iter.next();
					if (elem.getNamespaceURI().equals(UserStore.NS_URI) == false) {
						throw new InvalidRepresentationFault(
								InvalidRepresentationFault.Detail.INVALID_VALUES);
					}
					if (elem.getLocalName().equals("firstname")) {
						update.setFirstname(elem.getNodeValue());
					} else if (elem.getLocalName().equals("lastname")) {
						update.setLastname(elem.getNodeValue());
					} else if (elem.getLocalName().equals("address")) {
						update.setAddress(elem.getNodeValue());
					} else if (elem.getLocalName().equals("city")) {
						update.setCity(elem.getNodeValue());
					} else if (elem.getLocalName().equals("state")) {
						update.setState(elem.getNodeValue());
					} else if (elem.getLocalName().equals("zip")) {
						update.setZip(elem.getNodeValue());
					} else if (elem.getLocalName().equals("age")) {
						update.setAge(new Integer(elem.getNodeValue()));
					} else {
						// Don't recognize this element
						throw new InvalidRepresentationFault(
								InvalidRepresentationFault.Detail.INVALID_VALUES);
					}
				}

				// Now determine 'what' is to be updated
				// from the xpath in the fragment transfer header
				SOAPHeaderElement fragmentHeader = transfer.getFragmentHeader();
			} else {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}
			UserStore.update(user, update);
			
			// TODO: Create thre response
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		}
	}

	@Override                                         
	public void delete(HandlerContext context,Management request, Management response) {
		// TODO: ...
	}

	// private methods follow

	private UserType findInstance(Management request) {
		final String firstname;
		final String lastname;
		try {
			firstname = Utilities.getSelectorByName("firstname",
					request.getSelectors()).getContent().get(0).toString();
			lastname = Utilities.getSelectorByName("lastname",
					request.getSelectors()).getContent().get(0).toString();
		} catch (Exception e) {
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}
		final UserType userOb = UserStore.get(firstname, lastname);
		if (userOb == null)
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		return userOb;
	}
		 */
}
