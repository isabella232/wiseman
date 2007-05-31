/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: contacts_Handler.java,v 1.2 2007-05-31 19:47:48 nbeers Exp $
 */
package com.sun.ws.management.server.handler.wsman;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.hp.examples.ws.wsman.user.UsersType;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.framework.Utilities;
import com.sun.ws.management.metadata.annotations.WsManagementAddressDetailsAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementDefaultAddressingModelAnnotation;
import com.sun.ws.management.metadata.annotations.WsManagementOperationDefinitionAnnotation;
import com.sun.ws.management.server.Handler;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.xml.XmlBinding;
import com.sun.xml.ws.addressing.model.ActionNotSupportedException;

@WsManagementDefaultAddressingModelAnnotation(
	getDefaultAddressDefinition=
	 @WsManagementAddressDetailsAnnotation(
		 //define destination to use for this server	 
		wsaTo=contacts_Handler.DESTINATION, 
		 //define the resourceURI to locate this handler
		wsmanResourceURI=contacts_Handler.RESOURCE_URI,
		 //define the selectors to use to isolate desired contact
		wsmanSelectorSetContents={
			contacts_Handler.selector1,
			contacts_Handler.selector2
		}
	 ),
	 //define a unique and simple metadataResourceId
	resourceMetaDataUID = contacts_Handler.DESTINATION+"Bill.Gates",
	 //define the list of schemas and their location available via HTTP.
	schemaList={
	  "usr="+contacts_Handler.DESTINATION+"schemas/user.xsd"
	},
	  //define the operations available off of this service
	definedOperations={
	 @WsManagementOperationDefinitionAnnotation(
		//operation name	 
	  operationName = "Get",
	    //specify the input details
	  operationInputTypeMap = "EMPTY-BODY=http://schemas.xmlsoap.org/ws/2004/09/transfer/Get",
	    //specify the output details
	  operationOutputTypeMap = "usr:UserType=http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse"
	 )
	}
)
public class contacts_Handler implements Handler {

	/** As per the handler mechanism for Wiseman, this method
	 * is implemented.
	 * 
	 */
	public void handle(String action, String resource, 
			HandlerContext context,
			Management request, 
			Management response) throws Exception {
		
		//Switch on the Action sent in with the request
		if(action.equals(Transfer.GET_ACTION_URI)){
		  get(context, request, response);	
		}
		//Else not supported.
		else{
		  String msg = "Unable to map the action '"+action+
		  "' for specific handling.";
		   throw new ActionNotSupportedException(msg);
		}
	}

   /** Method to handle the Transfer.Get specific details.
    *  This is where the model specific processing needs
    *  to occur on the server side. 
    * @param context
    * @param request
    * @param response
    */	
   private void get(HandlerContext context,Management request, 
		   Management response){
	   
	  //The following is an example of what model specific processing 
	  // may happen on the server side for a TRANSFER.GET request.
		UserType user = findInstance(request);

		// Create an empty dom document and serialize the usertype object to it
        Document responseDoc = Management.newDocument();

        try {
			binding.marshal(userFactory.createUser(user), responseDoc );
		} catch (Exception e) {
			throw new InvalidRepresentationFault(
					InvalidRepresentationFault.Detail.INVALID_VALUES);
		} 	         

		try {
		  response.getBody().addDocument(responseDoc);
		} catch (SOAPException e1) {
			e1.printStackTrace();
		} 
   }

	
	/////////// Helper fields, functions to support WsManagement operations ///
	//FIELD VARIABLES
	private static final Logger LOG = 
		Logger.getLogger(contacts_Handler.class.getName());

	//stores all users loaded in a hashmap
	private static HashMap<String,UserType> userIndex = null;
	private final String USER_STORE_FILENAME="/users.store.xml";
	
	 //JAXB components for model specific processing
	private XmlBinding binding;
	private ObjectFactory userFactory=new ObjectFactory();
	private static String PACKAGE = null;
	
	 //final static references to be exposed via annotation
	public final static String DESTINATION = 
		"http://localhost:8080/users/";
	public final static String RESOURCE_URI = "wsman:contacts";
	public static final String selector1 ="firstname=Bill";
	public static final String selector2 ="lastname=Gates";

	/** Constructor for the contacts handler that exposes simple
	 *  resources via Transfer.GET.  
	 *  initializes XMLBinding for JAXB operations and loads
	 *  static data store of user information.
	 */
	public contacts_Handler(){
	  //package for XMLBinding	
	  PACKAGE = userFactory.getClass().getPackage().getName();
	  try{
		binding=new XmlBinding(null,PACKAGE);
		//lazily instantiate/load the users/contacts data
		if(userIndex ==null){
		  initializeDataStore();
		}//end of
	 }catch(Exception ex){
	   ex.printStackTrace();
	   LOG.severe(ex.getMessage());	
	 }
	}

	/**Loads the contents of an XML file containing User/Contact
	 * information defined by the user.xsd.
	 * @throws JAXBException
	 */
	private void initializeDataStore() throws JAXBException {
		
		//Now populate the master users list from the classpath
		 final InputStream is = contacts_Handler.class
		 .getClassLoader().getResourceAsStream(
				 USER_STORE_FILENAME);
		 // Check if InputStream was successfully opened
		 if (is == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING,
				"WARNING: Failed to load user store: " +
				USER_STORE_FILENAME);
			}
		 } else {
			//instantiate the JAXB components 
			final JAXBContext ctxt = 
				JAXBContext.newInstance(PACKAGE);
			final Unmarshaller u = 
				ctxt.createUnmarshaller();

			 // Read in the user store contents
			JAXBElement<UsersType> usersElem = 
				(JAXBElement<UsersType>) u
					.unmarshal(is);
			UsersType users = usersElem.getValue();
			
			//if successfully retrieved then proceed.
			if (users != null) {
				List<UserType> userList = users.getUser();
				if((userList != null)&&(userList.size()>0)){
				  userIndex = new HashMap<String,UserType>();
				 // Save the list in our private HashMap
				 for(int index=0; index< userList.size(); index++) {
						UserType user = (UserType) userList.get(index);
						//Index the contacts via primary key= FName.LName
						String key = user.getFirstname() + "."
								+ user.getLastname();
						userIndex.put(key,user);
				 }
				}//enf of if list is not empty
			}//end of master users list not null.
		 }
	}
	

    /** Convenience method to locate each user once the FirstName and LastName 
     * is known and passed in as selectors.
     * 
     * @param request
     * @return
     */
    private UserType findInstance(Management request) {
		UserType searchUser= userFactory.createUserType();	
		try {
			searchUser.setFirstname(
					Utilities.getSelectorByName("firstname",
							request.getSelectors()).getContent().get(0).toString());
			searchUser.setLastname(Utilities.getSelectorByName("lastname",
					request.getSelectors()).getContent().get(0).toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		} 
		//Extract the data to create PK index for HashMap.
		String key=searchUser.getFirstname()+"."+searchUser.getLastname();
		UserType userOb=null;
		if(!userIndex.containsKey(key)){
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}else{
			userOb = userIndex.get(key);
		}
		if(userOb==null)
			throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
		
	  return userOb;
	}
}
