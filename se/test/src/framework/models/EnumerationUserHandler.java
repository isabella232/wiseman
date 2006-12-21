 package framework.models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.dmtf.schemas.wbem.wsman._1.wsman.AnyListType;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableEmpty;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerationContextType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.FilterType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Pull;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Release;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.CannotProcessFilterFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.enumeration.EnumerationExtensions;
import com.sun.ws.management.enumeration.InvalidEnumerationContextFault;
import com.sun.ws.management.framework.enumeration.Enumeratable;
import com.sun.ws.management.framework.handlers.DefaultHandler;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.NamespaceMap;
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
	
	//Fragment transfer stuff
    public static final QName FRAGMENT_TRANSFER =
        new QName(Management.NS_URI, "FragmentTransfer", Management.NS_PREFIX);

    public static final QName DIALECT =
        new QName("Dialect");
	
    javax.xml.xpath.XPath xpath = null;
    
	public EnumerationUserHandler() {
		try {
			
			String[] pkgList ={"com.hp.examples.ws.wsman.user"};
			binding=new XmlBinding(null,pkgList);
			userFactory=new ObjectFactory();
			if(currentEnumerationContexts==null){
				currentEnumerationContexts = new HashMap();
			}
			//load global users.store from jar
			populateGlobalUsersList();
			xpath = XPathFactory.newInstance().newXPath();
			
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		} //catch (SOAPException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
		catch (IOException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}

	//DONE: load the users.store from classpath.
	private void populateGlobalUsersList() throws IOException, JAXBException {
		String userStoreSource="framework/models/users.store";
		InputStream is 
		  =EnumerationUserHandler.class.getClassLoader().getResourceAsStream(userStoreSource);
		//DONE: cycle through input stream and load the UserType instances
		 BufferedReader br = new BufferedReader(new InputStreamReader(is));
		 String line = "";
		 String lineInBuffer = "";
		 ArrayList allUsers = new ArrayList();
		 String pkg ="com.hp.examples.ws.wsman.user";
		 JAXBContext ctxt = JAXBContext.newInstance(pkg);
		 Unmarshaller u = ctxt.createUnmarshaller();
		
		while((line=br.readLine())!=null){
			if((line.trim().length()>0)&&(line.indexOf(div)==-1)){
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
	public void enumerate(HandlerContext context,Enumeration enuRequest, Enumeration enuResponse) {

		//Handle the enumeration request, by retrieving filter if there is any.
			//Instantiate enumerate response object
		EnumerateResponse response = new EnumerateResponse(); 
		
    		//EnumerationContext for response object
    	EnumerationContextType enumCtxtTypeResponseObject = null;
    		//filter params.
    	ArrayList<String> filterParameters = new ArrayList();
    	
    	boolean getItemCount = false;
    	boolean optimized = false;
    	int maxItems = 0;
    	
		try {

			//parse request object to retrieve filter parameters entered.
	    	Enumerate enumerateRequestObject = enuRequest.getEnumerate();
			String xpathExp = null;
			SOAPElement fragmentHeader = null;
        	final EnumerationExtensions enxRequest = new EnumerationExtensions(enuRequest);

        	FilterType enuFilter = enxRequest.getFilter();
        	DialectableMixedDataType enxFilter = enxRequest.getWsmanFilter();
        	if ((enuFilter != null) && (enxFilter != null)) {
        		// Both are not allowed. Throw an exception
        		throw new CannotProcessFilterFault(SOAP.createFaultDetail("Both wsen:Filter and wsman:Filter were specified in the request. Only one is allowed.", null, null, null));
        	}
        	List<Object> cont = null;
        	if (enuFilter != null) {
        		cont = enuFilter.getContent();
        	}
        	if (enxFilter != null) {
        		cont = enxFilter.getContent();
        	}

			if ((cont != null) && (cont.size() > 0)) {
				// filter body is XMLAny
				// Then some value defined for the filter block.
				for (Iterator iter = cont.iterator(); iter.hasNext();) {
					// content is just a string.
					String filterContent = (String) iter.next();
					filterContent = filterContent.trim();
					filterParameters.add(filterContent);
				}
			} else {// Else no filter content supplied
				filterParameters.add("//*"); // return all.
			}
				
			//  Look for getTotalItemsRequest header
			SOAPElement[] reqHeaders = enuRequest.getHeaders();
			
			for (int i = 0; i < reqHeaders.length; i++) {
				if (reqHeaders[i].getElementQName().equals(EnumerationExtensions.REQUEST_TOTAL_ITEMS_COUNT_ESTIMATE)) {
					getItemCount = true;
				}
				// Look for fragment transfer header
				QName elems = reqHeaders[i].getElementQName();
				if(elems!=null){
					if((elems.getLocalPart().equalsIgnoreCase(FRAGMENT_TRANSFER.getLocalPart()))&&
					   (elems.getPrefix().equalsIgnoreCase(FRAGMENT_TRANSFER.getPrefix()))&&
					   (elems.getNamespaceURI().equalsIgnoreCase(FRAGMENT_TRANSFER.getNamespaceURI()))
					   ){
					  fragmentHeader = reqHeaders[i];
					  xpathExp = extractFragmentMessage(fragmentHeader);
					}
				}
				
				// Look for optimized enumeration headers
				if (reqHeaders[i].getElementQName().equals(EnumerationExtensions.OPTIMIZE_ENUMERATION)) {
					optimized = true;
				}
				if (reqHeaders[i].getElementQName().equals(EnumerationExtensions.MAX_ELEMENTS)) {
					maxItems = new Integer(reqHeaders[i].getValue()).intValue();
				}
				
			}
			
			//DONE: after building up filterParameters, create context and store filterParams
			enumCtxtTypeResponseObject = new EnumerationContextType();
			
			 String ctxId = UUID_SCHEME+UUID.randomUUID().toString();
			 enumCtxtTypeResponseObject.getContent().add(ctxId);
			//Now add to EnumeContextInfo container
 
			response.setExpires(enumerateRequestObject.getExpires());
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
				enuResponse.setEnumerateResponse(ctxId,enumerateRequestObject.getExpires());
				//Now add to EnumeContextInfo container
				EnumerationContextType cntxtRef = enuResponse.getEnumerateResponse().getEnumerationContext();
				EnumerationContextContainer container = null;
				try {
					container = new EnumerationContextContainer(ctxId,
							cntxtRef,
							filterParameters);
				} catch (XPathExpressionException e) {
					e.printStackTrace();
					throw new InternalErrorFault(e.getMessage()); 
				}
				if (getItemCount){
					Long itemCount = new Long(container.dataValues.length);
					
					enuResponse.addHeaders(Addressing.createReferencePropertyType(EnumerationExtensions.TOTAL_ITEMS_COUNT_ESTIMATE, itemCount.toString()));
				}
				//add reference to stored enumeration lists
				if(!currentEnumerationContexts.containsKey(ctxId)){
				  currentEnumerationContexts.put(ctxId,container);
				}
				
				
				// If this an optimized enumeration request, get the first set of enumerated items
				if (optimized) {
					enuResponse = currentEnumerationContexts.get(ctxId).processResponse(enuResponse,null,maxItems,
						-1, getItemCount, xpathExp, fragmentHeader, true);
				}
				
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
	public void release(HandlerContext hcontext,Enumeration enuRequest, Enumeration enuResponse) {
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
			e.printStackTrace();
			throw new InternalErrorFault(e.getMessage());
		} catch (SOAPException e) {
			e.printStackTrace();
			throw new InternalErrorFault(e.getMessage());
		} 
	}

	/* (non-Javadoc)
	 * @see com.hp.management.wsman.server.enumeration.HpEnumerationSupport#renew(com.sun.ws.management.enumeration.Enumeration, com.sun.ws.management.enumeration.Enumeration)
	 */
	public void renew(HandlerContext context,Enumeration arg0, Enumeration arg1) {
		// TODO Auto-generated method stub
//			super.renew(arg0, arg1);
	}


	public void pull( HandlerContext hcontext,Enumeration enuRequest, Enumeration enuResponse) {

		Pull pullRequestObject = null;
		boolean getItemCount = false;
		
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
			if(pullRequestObject.getMaxElements()!=null)
			 maxElements = pullRequestObject.getMaxElements().intValue();
			int maxContentLength = -1;
			if(pullRequestObject.getMaxCharacters()!=null)
				maxContentLength = pullRequestObject.getMaxCharacters().intValue();

			 //DONE: locate context information
			 EnumerationContextContainer eCont = 
				 currentEnumerationContexts.get(contentId);
			 
			 if(eCont==null){
				 throw new InvalidEnumerationContextFault();
			 }
			 
			SOAPElement[] reqHeaders = enuRequest.getHeaders();
			SOAPElement fragmentHeader = null;
			String xpathExp = null;
			for (int i = 0; i < reqHeaders.length; i++) {
				if (reqHeaders[i].getElementQName().equals(EnumerationExtensions.REQUEST_TOTAL_ITEMS_COUNT_ESTIMATE)) {
					getItemCount = true;
				}
				// Look for fragment transfer header
				QName elems = reqHeaders[i].getElementQName();
				if(elems!=null){
					if((elems.getLocalPart().equalsIgnoreCase(FRAGMENT_TRANSFER.getLocalPart()))&&
					   (elems.getPrefix().equalsIgnoreCase(FRAGMENT_TRANSFER.getPrefix()))&&
					   (elems.getNamespaceURI().equalsIgnoreCase(FRAGMENT_TRANSFER.getNamespaceURI()))
					   ){
					  fragmentHeader = reqHeaders[i];
					  xpathExp = extractFragmentMessage(fragmentHeader);
					}
				}
			}
			enuResponse = eCont.processResponse(enuResponse,maxTime,maxElements,maxContentLength, getItemCount, xpathExp, fragmentHeader, false);
			 
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new InternalErrorFault(e.getMessage());
		} catch (SOAPException e) {
			e.printStackTrace();
			throw new InternalErrorFault(e.getMessage());
		} 
	}


	public void getStatus(HandlerContext context,Enumeration enuRequest, Enumeration enuResponse) {
		// TODO Auto-generated method stub
		
	}
	
	public static String xmlToString(Node node) {
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}
	

  class EnumerationContextContainer implements java.util.Enumeration<UserType>{
	//ATTRIBUTES
	private UserType[] dataValues = null;
	private int counter = -1;
	
	public EnumerationContextContainer(String enumContextId,
			EnumerationContextType context,
			ArrayList<String> filterValues) throws XPathExpressionException, JAXBException{
		this.enumContextId = enumContextId;
		this.enumerationContext = context;
		this.filterParametersList = filterValues;
		initializeDataSet();
		counter = 0;
	}
	
	public Enumeration processResponse(Enumeration enuResponse, Duration maxTime, int maxElements, 
			int maxContentLength, boolean getItemCount, String xpathExp, SOAPElement fragmentHeader,
			boolean isEnumRequest) throws JAXBException, SOAPException {

		Enumeration endResponse = null;
		
		 //Populate the response object with returned XML data.
		 ArrayList<String> filterList = getFilterParametersList();
		 
		 //if filters were supplied....
		 //if(filterList.size()>0){
		 
		 // Add fragment header if required
			if (xpathExp != null){
				enuResponse.getHeader().addChildElement(fragmentHeader);
			} 
	        List<EnumerationItem> items = new ArrayList<EnumerationItem>();
	        int serializedCount = 0;
	        
			while (this.hasMoreElements()&(serializedCount<maxElements)) {
				// Create an empty dom document and serialize the usertype object to it
		        try {
		        	UserType type = this.nextElement();
		        	JAXBElement<UserType> element = userFactory.createUser(type);
		        	
					// TODO: This check is incorrect. It should check the wsman:filter
		        	//       Enumeration has the filter projection in the wsman:filter
		        	//       and not the fragmentHeader.
		        	//       NOTE: This makes it difficult to decide if it is just a filter
		        	//             or does it also have a projection (fragment transfer case).
					if (fragmentHeader != null) {
						// get the fagments and put them into a JAXBElement
						Document responseDoc = Management.newDocument();
						binding.marshal(element, responseDoc);
						Object resultOb = null;
						// TODO: The filter may be namespace qualified. We need
						//       to add the namespaces
						//       and prefixes in the request to the "xpath" object:
						//       xpath.setNamespaceContext(nsContext);
						resultOb = xpath.evaluate(xpathExp, responseDoc, XPathConstants.NODESET);
						if (resultOb != null) {
							final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
							NodeList nodelist = (NodeList) resultOb;
							for (int i = 0; i < nodelist.getLength(); i++) {
								mixedDataType.getContent().add(nodelist.item(i));
							}
							// create the XmlFragmentElement
							final JAXBElement<MixedDataType> xmlFragment = 
								  Management.FACTORY.createXmlFragment(mixedDataType);
							// add payload to the body
							items.add(new EnumerationItem(xmlFragment, null));
							serializedCount++;
						}
					} else {
						items.add(new EnumerationItem(element, null));
						serializedCount++;
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new InternalErrorFault(e.getMessage());
				}
			}
			if (getItemCount) {
				enuResponse.addHeaders(Addressing.createReferencePropertyType(EnumerationExtensions.TOTAL_ITEMS_COUNT_ESTIMATE, 
						                                                      new Long(dataValues.length).toString()));
			}
			  boolean moreToCome = true;
			  if(cnt ==dataValues.length){
				 moreToCome = false;
			  }
			  //Now stuff every one of the response in?
			  if (isEnumRequest){
			        final AnyListType anyListType = Management.FACTORY.createAnyListType();
			        final List<Object> any = anyListType.getAny();
			        for (final EnumerationItem ee : items) {
			        	any.add(ee.getItem());
			        }

			        JAXBElement anyList = Management.FACTORY.createItems(anyListType);
			        if (!moreToCome) {
			        	JAXBElement<AttributableEmpty> eos = Management.FACTORY.createEndOfSequence(new AttributableEmpty());
			        	enuResponse.setEnumerateResponse(getEnumContextId(), enuResponse.getEnumerateResponse().getExpires(), anyList, eos);
			        } else {
			        	enuResponse.setEnumerateResponse(getEnumContextId(), enuResponse.getEnumerateResponse().getExpires(), anyList);
			        }
			  } else {
				  enuResponse.setPullResponse(items, getEnumContextId(), moreToCome);
				  
			  }
			  
		 //}		
		return endResponse;
	}
	
	private String enumContextId ="";
	private EnumerationContextType enumerationContext = null;
	private ArrayList<String> filterParametersList = null;
	private int cnt = 0;
	private UserType user;
	
	public void initializeDataSet() throws JAXBException, XPathExpressionException{
		ArrayList<UserType> matches = new ArrayList<UserType>();
		String filter = null;
		if(filterParametersList.size()>0)
			filter=filterParametersList.get(0);
		
		//Load the UserType namespaces
		NamespaceMap nsm = null;
		nsm = loadUserTypeNamespaces();
		
		//extract request and process
		if((filter!=null)&&(filter.trim().length()>0)){
			String parameter = "";

			//Process the filtered requests
			for (int i = 0; i < globalUsersList.length; i++) {
			 //DONE:convert to XML doc to run xpath filtering on
			  Document content = Management.newDocument();
				JAXBElement<UserType> userElement = 
				   userFactory.createUser(globalUsersList[i]);
				binding.marshal(userElement, content);
			 List<Node> result = null;
			  result=XPath.filter(content, filter, nsm);
			  if((result!=null)&&(result.size()>0)){//Then add this UserType inst
				  Node nod = result.get(0);
				  matches.add(globalUsersList[i]); 
			  }
			}
		} else {//No filtering. 
			matches = new ArrayList<UserType>(globalUsersList.length);
			for (int i = 0; i < globalUsersList.length; i++) {
				
				matches.add(globalUsersList[i]);	
			}
//			System.arraycopy(globalUsersList, 0, matches, 0, globalUsersList.length);
		}
		//return all filtered/unfiltered data.
		dataValues = new UserType[matches.size()];
		System.arraycopy(matches.toArray(), 0, dataValues, 0, matches.size());
        cnt = 0;
	}

	/**
	 * @return
	 */
	private NamespaceMap loadUserTypeNamespaces() {
		NamespaceMap nsm;
		Map<String,String> ns = new HashMap<String, String>();
		ns.put("ns2","http://schemas.xmlsoap.org/ws/2004/08/addressing");
		ns.put("ns3","http://schemas.xmlsoap.org/ws/2004/08/eventing");
		ns.put("ns4","http://schemas.xmlsoap.org/ws/2004/09/enumeration");
		ns.put("ns5","http://www.w3.org/2003/05/soap-envelope");
		ns.put("ns6","http://schemas.xmlsoap.org/ws/2004/09/transfer");
		ns.put("ns7","http://schemas.xmlsoap.org/ws/2005/06/wsmancat");
		ns.put("ns8","http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd");
		ns.put("ns9","http://examples.hp.com/ws/wsman/user");
		nsm = new NamespaceMap(ns);
		return nsm;
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
  
	private String extractFragmentMessage(SOAPElement element){
		String xpathExp = "";
		//DONE: populate xpathExp
		if(element!=null){
		  NodeList elem = element.getChildNodes();
		  for (int j = 0; j < elem.getLength(); j++) {
			Node node = elem.item(j);
			xpathExp = node.getNodeValue();
		  }
		}
       return xpathExp;
	}  
}