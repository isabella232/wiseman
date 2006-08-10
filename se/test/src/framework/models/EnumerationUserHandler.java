package framework.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Pull;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Release;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
//import com.hp.management.wsman.handlerimpl.DefaultHandler;
//import com.hp.management.wsman.server.enumeration.Enumeratable;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.framework.enumeration.Enumeratable;
import com.sun.ws.management.framework.enumeration.EnumerationHandler;
import com.sun.ws.management.framework.handlers.DefaultHandler;
import com.sun.ws.management.framework.handlers.DelegatingHandler;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.handler.wsman.auth.user_Handler;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

/**
 * This Handler Deligates to The UserHandler Class
 * @author Simeon
 *
 */
public class EnumerationUserHandler extends DefaultHandler implements Enumeratable {
	
	//attributes.
	private static HashMap<String,EnumerationContextContainer> currentEnumerationContexts = null;
	
	private static HashSet contextids = new HashSet();
	private final String UUID_SCHEME="e-ctxt:";
	public static XmlBinding binding;
	public static ObjectFactory userFactory = null;
	private static UserType[] globalUsersList = null;
	private static final String div = "################# User Type Divider ################";
	
	public EnumerationUserHandler() {
		try {
			if(new Addressing().getXmlBinding()==null){
	    		SOAP.setXmlBinding(new Addressing().getXmlBinding());
			}
			
			String[] pkgList ={"com.hp.examples.ws.wsman.user"};
			binding=new XmlBinding(null,pkgList);
			userFactory=new ObjectFactory();
			if(currentEnumerationContexts==null){
				currentEnumerationContexts = new HashMap();
			}
			//load global users.store from jar
			populateGlobalUsersList();
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}

	//TODO: load the users.store from JAR.
	private void populateGlobalUsersList() throws IOException, JAXBException {
		String userStoreSource="framework/models/users.store";
		InputStream is 
		  =EnumerationUserHandler.class.getClassLoader().getResourceAsStream(userStoreSource);
		//TODO: cycle through input stream and load the UserType instances
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = "";
		String lineInBuffer = "";
		ArrayList allUsers = new ArrayList();
		String pkg ="com.hp.examples.ws.wsman.user";
		JAXBContext ctxt = JAXBContext.newInstance(pkg);
		Unmarshaller u = ctxt.createUnmarshaller();
		
		while((line=br.readLine())!=null){
			if((line.indexOf(div)==-1)&&(line.trim().length()>0)){
				// create a new user class and add it to the list
				UserModelObject userObject = new UserModelObject();
				line = line.trim();

				UserType user=null;
				try {
				  JAXBElement<UserType> obs = (JAXBElement<UserType>)u.unmarshal(new StringBufferInputStream(line));
				  user = (UserType)obs.getValue(); 				
				} catch (JAXBException e) {
					throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
				}
               allUsers.add(user);				
			}
		}
		globalUsersList = new UserType[allUsers.size()];
		System.arraycopy(allUsers.toArray(), 0, globalUsersList, 0, globalUsersList.length);
	}


	/* (non-Javadoc)
	 * @see com.hp.management.wsman.handlerimpl.DefaultHandler#enumerate(java.lang.String, com.sun.ws.management.enumeration.Enumeration, com.sun.ws.management.enumeration.Enumeration)
	 */
	public void enumerate(Enumeration enuRequest, Enumeration enuResponse) {

		//Handle the enumeration request, by retrieving filter if there is any.
			//Instantiate enumerate response object
		EnumerateResponse response = new EnumerateResponse(); 
		
			//Enumerate request object 
    	Enumerate enumerateRequestObject = null;
    		//EnumerationContext for response object
    	EnumerationContextType enumCtxtTypeResponseObject = null;
    		//filter params.
    	ArrayList<String> filterParameters = new ArrayList();
    	
		try {
			//parse request object to retrieve filter parameters entered.
			enumerateRequestObject =enuRequest.getEnumerate();
			FilterType filter = enumerateRequestObject.getFilter();
			//filter body is XMLAny
			List<Object> cont = filter.getContent();
			
			if(cont.size()>0){ //Then some value defined for the filter block.
				for (Iterator iter = cont.iterator(); iter.hasNext();) {
					//content is just a string.
					String filterContent = (String) iter.next();
					filterContent = filterContent.trim();
				    filterParameters.add(filterContent);	
				}
				//TODO: after building up filterParameters, create context and store filterParams
				enumCtxtTypeResponseObject = new EnumerationContextType();
				
				 String ctxId = UUID_SCHEME+UUID.randomUUID().toString();
				 enumCtxtTypeResponseObject.getContent().add(ctxId);
				//Now add to EnumeContextInfo container
				response.setExpires("PT15M");
				response.setEnumerationContext(enumCtxtTypeResponseObject);

				XmlBinding xmlBinding = enuResponse.getXmlBinding(); 
			        Document responseDoc = enuResponse.newDocument();
			        try {
						xmlBinding.marshal(response, responseDoc );
					} catch (JAXBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 		         

					EnumerateResponse enumResp = new EnumerateResponse();
					//TODO: Change to user input. Hardcoded to 15 mins for right now.
					enuResponse.setEnumerateResponse(ctxId,"PT15M");
					//Now add to EnumeContextInfo container
					EnumerationContextType cntxtRef = enuResponse.getEnumerateResponse().getEnumerationContext();
					EnumerationContextContainer container = 
						new EnumerationContextContainer(ctxId,
								cntxtRef,
								filterParameters);
					//add reference to stored enumeration lists
					if(!currentEnumerationContexts.containsKey(ctxId)){
					  currentEnumerationContexts.put(ctxId,container);
					}
			}else{//Else no filter content supplied
				//TODO: select * and prepare to return on pull. 
				//TODO: allow users to define server side maximum pull count/amount.
			}
		//TODO: figure out what to do with each exception.		
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new InternalErrorFault(e.getMessage());
		} catch (SOAPException e) {
			e.printStackTrace();
			throw new InternalErrorFault(e.getMessage());
		} 
	}

	/* (non-Javadoc)
	 * @see com.hp.management.wsman.server.enumeration.HpEnumerationSupport#release(com.sun.ws.management.enumeration.Enumeration, com.sun.ws.management.enumeration.Enumeration)
	 */
	public void release(Enumeration enuRequest, Enumeration enuResponse) {
		Release releaseRequestObject = null;
		
		try {
			//parse request object to retrieve filter parameters entered.
			releaseRequestObject =enuRequest.getRelease();
			//extract relevant info for parsing
			String contentId = "";
			 EnumerationContextType context = releaseRequestObject.getEnumerationContext();
			 contentId = (String) context.getContent().get(0);

			 //DONE: locate context information
			 EnumerationContextContainer eCont = 
				 currentEnumerationContexts.get(contentId);
			 if(eCont==null){
				 //TODO: throw exception to that effect. Throw illegal arg for now.
//					 String msg="No context with id '"+contentId+"' could be found.";
				 throw new InvalidEnumerationContextFault();
			 }else{
				 currentEnumerationContexts.remove(contentId);
			 }
			 
		//TODO: figure out what to do with each exception.		
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/* (non-Javadoc)
	 * @see com.hp.management.wsman.server.enumeration.HpEnumerationSupport#renew(com.sun.ws.management.enumeration.Enumeration, com.sun.ws.management.enumeration.Enumeration)
	 */
	public void renew(Enumeration arg0, Enumeration arg1) {
		// TODO Auto-generated method stub
//			super.renew(arg0, arg1);
	}


	public void pull( Enumeration enuRequest, Enumeration enuResponse) {

		Pull pullRequestObject = null;
		
		try {
			//parse request object to retrieve filter parameters entered.
			pullRequestObject =enuRequest.getPull();
			//extract relevant info for parsing
			String contentId = "";
			 EnumerationContextType context = pullRequestObject.getEnumerationContext();
			 contentId = (String) context.getContent().get(0);
			Duration maxTime = null;
			 maxTime = pullRequestObject.getMaxTime();
			int maxElements = -1;
			 maxElements = pullRequestObject.getMaxElements().intValue();
			int maxContentLength = -1;
			 maxContentLength = pullRequestObject.getMaxCharacters().intValue();

			 //DONE: locate context information
			 EnumerationContextContainer eCont = 
				 currentEnumerationContexts.get(contentId);
			 
			 if(eCont==null){
				 throw new InvalidEnumerationContextFault();
			 }
			 
			 enuResponse = eCont.processResponse(enuResponse,maxTime,maxElements,maxContentLength);
			 
			 
		//TODO: figure out what to do with each exception.		
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new InternalErrorFault(e.getMessage());
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new InternalErrorFault(e.getMessage());
		} 
	}


	public void getStatus(Enumeration enuRequest, Enumeration enuResponse) {
		// TODO Auto-generated method stub
		
	}
	
	

  class EnumerationContextContainer implements java.util.Enumeration<UserType>{
	//ATTRIBUTES
	private UserType[] dataValues = null;
	private int counter = -1;
	
	public EnumerationContextContainer(String enumContextId,
			EnumerationContextType context,
			ArrayList<String> filterValues){
		this.enumContextId = enumContextId;
		this.enumerationContext = context;
		this.filterParametersList = filterValues;
		initializeDataSet();
		counter = 0;
	}
	
	public Enumeration processResponse(Enumeration enuResponse, Duration maxTime, int maxElements, int maxContentLength) throws JAXBException, SOAPException {
		Enumeration endResponse = null;
		
		 //Populate the response object with returned XML data.
		 ArrayList<String> filterList = getFilterParametersList();
		 
		 //if filters were supplied....
		 if(filterList.size()>0){
			 
	        List<EnumerationItem> items = new ArrayList<EnumerationItem>();
	        int serializedCount = 0;
	        
			while (this.hasMoreElements()&(serializedCount<maxElements)) {
				// Create an empty dom document and serialize the usertype object to it
		        Document responseDoc = Management.newDocument();
		        try {
		        	UserType type = this.nextElement();
		        	binding.marshal( new JAXBElement(
		        			  new QName("http://examples.hp.com/ws/wsman/user",
		        					  "user"), UserType.class, type ),responseDoc);
					items.add(new EnumerationItem(responseDoc.getDocumentElement(),null));
					serializedCount++;
				} catch (Exception e) {
					e.printStackTrace();
					throw new InternalErrorFault(e.getMessage());
				}
			}
			  boolean moreToCome = true;
			  if(cnt ==dataValues.length){
				 moreToCome = false;
			  }
			  //Now stuff every one of the response in?
			  enuResponse.setPullResponse(items, getEnumContextId(), null, moreToCome);
		 }		
		return endResponse;
	}
	
	private String enumContextId ="";
	private EnumerationContextType enumerationContext = null;
	private ArrayList<String> filterParametersList = null;
	private int cnt = 0;
	
	public void initializeDataSet(){
		ArrayList<UserType> matches = new ArrayList<UserType>();
		String filter = filterParametersList.get(0);
		//extract request and process
		if((filter!=null)&&(filter.trim().length()>0)){
			String parameter = "";
			if(filter.indexOf("firstname")>-1){
				int start = filter.indexOf("'");
				int end = filter.indexOf("'",start+1);
				parameter = filter.substring(start+1,end);
				if(parameter.trim().length()>-1){
					for (int i = 0; i < globalUsersList.length; i++) {
						if(globalUsersList[i].getFirstname().equalsIgnoreCase(parameter)){
							matches.add(globalUsersList[i]);
						}
					}
				}
			}
			if(filter.indexOf("lastname")>-1){
				int start = filter.indexOf("'");
				int end = filter.indexOf("'",start+1);
				parameter = filter.substring(start+1,end);
				if(parameter.trim().length()>-1){
					for (int i = 0; i < globalUsersList.length; i++) {
						if(globalUsersList[i].getLastname().equalsIgnoreCase(parameter)){
							matches.add(globalUsersList[i]);
						}
					}
				}
			}
		}
		dataValues = new UserType[matches.size()];
		System.arraycopy(matches.toArray(), 0, dataValues, 0, matches.size());
        cnt = 0;
	}
	
	/**
	 * @return Returns the enumContextId.
	 */
	public String getEnumContextId() {
		return enumContextId;
	}
	/**
	 * @param enumContextId The enumContextId to set.
	 */
	public void setEnumContextId(String enumContextId) {
		this.enumContextId = enumContextId;
	}
	/**
	 * @return Returns the enumerationContext.
	 */
	public EnumerationContextType getEnumerationContext() {
		return enumerationContext;
	}
	/**
	 * @param enumerationContext The enumerationContext to set.
	 */
	public void setEnumerationContext(EnumerationContextType enumerationContext) {
		this.enumerationContext = enumerationContext;
	}
	/**
	 * @return Returns the filterParametersList.
	 */
	public ArrayList<String> getFilterParametersList() {
		return filterParametersList;
	}
	/**
	 * @param filterParametersList The filterParametersList to set.
	 */
	public void setFilterParametersList(ArrayList<String> filterParametersList) {
		this.filterParametersList = filterParametersList;
	}

	public boolean hasMoreElements() {
		return (counter<dataValues.length);
	}

	public UserType nextElement() {
		UserType element = null;
		if(hasMoreElements()){
			element = dataValues[counter++];
		}else{
			throw new NoSuchElementException();
		}
		return element;	
	}
  }
}
