package framework.models;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import com.hp.examples.ws.wsman.user.UserType;
import com.hp.examples.ws.wsman.user.UsersType;
import com.sun.ws.management.transfer.InvalidRepresentationFault;

public class UserStore {

	public static final String NS_URI = "http://examples.hp.com/ws/wsman/user";
	public static final String NS_PREFIX = "ns";
	public static final QName USER = new QName(NS_URI, "user", NS_PREFIX);
	
	private static final QName USERS = new QName(NS_URI, "user", NS_PREFIX);
	private static final Logger LOG = Logger.getLogger(UserStore.class.getName());
	private static final String USER_STORE_FILENAME = "framework/models/users.store.xml";
	private static final String PACKAGE = "com.hp.examples.ws.wsman.user";
	
	private static final HashMap<String,UserType> userStoreMap = loadUserStore();
	
	// Load the users.store from classpath.
	private static HashMap<String,UserType> loadUserStore() {
		
		try {
			final JAXBContext ctxt = JAXBContext.newInstance(PACKAGE);
			final Unmarshaller u = ctxt.createUnmarshaller();
			final HashMap<String, UserType> allUsers = new HashMap<String, UserType>();

			final InputStream is = EnumerationUserHandler.class
					.getClassLoader().getResourceAsStream(USER_STORE_FILENAME);

			// Check if InputStream was successfully opened
			if (is == null) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING,
							"WARNING: Failed to load user store: " + USER_STORE_FILENAME);
				}
			} else {
				// Read in the user store
				JAXBElement<UsersType> usersElem = (JAXBElement<UsersType>) u
						.unmarshal(is);
				UsersType users = usersElem.getValue();

				if (users != null) {
					List<UserType> userList = users.getUser();

					if ((userList != null) && (userList.size() > 0)) {

						// Save the list in our private ArrayList
						for (int index = 0; index < userList.size(); index++) {
							UserType user = (UserType) userList.get(index);
							String key = user.getFirstname() + "."
									+ user.getLastname();
							allUsers.put(key, user);
						}
					}
				}
			}
			return allUsers;
		} catch (JAXBException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, 
						"WARNING: JAXBException loading user.store data: " + e.getMessage());
			}
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		}
	}
	
	// Public interfaces follow
	
	public static UserType get(String firstname, String lastname) {
		synchronized (userStoreMap) {
	     return userStoreMap.get(firstname + "." + lastname);
		}
	}
	
	public static UserType[] getUsers() {		
		synchronized (userStoreMap) {
			final UserType[] allUsers = new UserType[userStoreMap.size()];
			userStoreMap.values().toArray(allUsers);
			return allUsers;
		}
	}
	
	public static void add(UserType user) {
		synchronized (userStoreMap) {
			String key = user.getFirstname() + "." + user.getLastname();
			userStoreMap.put(key, user);
		}
	}
	
	
	public static void update(UserType user, UserType update) {
		synchronized (userStoreMap) {
			String oldkey = user.getFirstname() + "." + user.getLastname();
			String key = update.getFirstname() + "." + update.getLastname();
			
			userStoreMap.remove(oldkey);
			userStoreMap.put(key, update);
		}
	}
	
	public static void delete(String firstname, String lastname) {
		synchronized (userStoreMap) {
			String key = firstname + "." + lastname;
			userStoreMap.remove(key);
		}
	}
}
