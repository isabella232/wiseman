package framework.models;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.MixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.examples.ws.wsman.user.ObjectFactory;
import com.hp.examples.ws.wsman.user.UserType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.framework.transfer.TransferSupport;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.xml.XmlBinding;

/**
 * User deligate is responsible for processing request actions
 * to be performed on users.
 * 
 * TODO
 * Review use of exceptions
 * @author wire
 *
 */
public class UserHandler extends TransferSupport {
	private static HashSet users=new HashSet(10); 
	private XmlBinding binding;
	private ObjectFactory userFactory=new ObjectFactory();
	private org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory wsManFactory= new org.dmtf.schemas.wbem.wsman._1.wsman.ObjectFactory();
	private org.xmlsoap.schemas.ws._2004._09.transfer.ObjectFactory xferFactory= new org.xmlsoap.schemas.ws._2004._09.transfer.ObjectFactory();
	private static String resourceUri= "wsman:auth/user";
	
	//Fragment transfer stuff
    public static final QName FRAGMENT_TRANSFER =
        new QName(Management.NS_URI, "FragmentTransfer", Management.NS_PREFIX);

    public static final QName DIALECT =
        new QName("Dialect");
    XPath xpath = null;
	
	public UserHandler() {
		super();
		try {
			binding=new XmlBinding(null,"com.hp.examples.ws.wsman.user");
			xpath = XPathFactory.newInstance().newXPath();
		} catch (JAXBException e) {
			throw new InternalErrorFault(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
    public void create(Management request, Management response) {
		// create a new user class and add it to the list
		UserModelObject userObject = new UserModelObject();
		
		// Init the user by transfering the fields from
		// the JAXB user to the 
		SOAPBody body = request.getBody();

		//Normal processing to create a new UserObject
		Node userChildNode = body.getFirstChild();
		UserType user=null;
		
		//Determine if this is a fragmentRequest
		  SOAPElement[] allHeaders =null;
		  String xpathExp ="";
		  SOAPElement fragmentHeader = null;
		  Set<SelectorType> selectors = null;
		  
		 try{ 
		  allHeaders= request.getHeaders();

		  //Locate Fragment header
		  fragmentHeader =locateFragmentHeader(allHeaders);
		  //Extract the XPath component
		  xpathExp = extractFragmentMessage(fragmentHeader);

		 }catch(SOAPException sexc){
			 sexc.printStackTrace();
		 }
		 
	  //Processing for FragmentCreate request
	  if(fragmentHeader!=null){
		  //retrieve the userModelObject instance to modify
		 userObject = findInstance(request);
		 //now retrieve the server specific content to replace based on fragmentExp
		 String fragBodyUpdate = xmlToString(userChildNode);
		 //extract content and set that value in the userObject
		 int contentsIndex = fragBodyUpdate.indexOf("</ns9:age>");
		 String newValue = fragBodyUpdate.substring(contentsIndex-2,contentsIndex);
		 userObject.setAge(Integer.valueOf(newValue));

		 //Add the fragment response header
		 try {
			response.getHeader().addChildElement(fragmentHeader);
		} catch (SOAPException e) {
			e.printStackTrace();
		}

	  }
	  //Processing for regular NON-Fragment request
	  else{
		try {
			JAXBElement<UserType> ob = (JAXBElement<UserType>)binding.unmarshal(userChildNode);
			user=(UserType)ob.getValue();
			
		} catch (JAXBException e) {
			throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
		}
		//Null out age as it's already set by default
		
		userObject.setFirstname(user.getFirstname());
		userObject.setLastname(user.getLastname());
		userObject.setAddress(user.getAddress());
		userObject.setCity(user.getCity());
		userObject.setState(user.getState());
		userObject.setZip(user.getZip());
		if(user.getAge()!=null){
		userObject.setAge(user.getAge());
		}
		users.add(userObject);
	  }
		
		HashMap<String, String> selectorMap = new HashMap<String,String>();
//	   selectorMap.put("firstname",user.getFirstname());
//	   selectorMap.put("lastname",user.getLastname());
		selectorMap.put("firstname",userObject.getFirstname());
		selectorMap.put("lastname",userObject.getLastname());
		try{
			appendCreateResponse(response, resourceUri, selectorMap); 
		} catch (JAXBException e) {
			throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
		}

	}


	@Override
	public void get(Management request, Management response) {
		// Find an existing instance

		UserModelObject userOb = findInstance(request);

		// Init the user by transfering the fields from
		// the JAXB user to the 
		UserType user=new UserType();		
		user.setFirstname(userOb.getFirstname());
		user.setLastname(userOb.getLastname());
		user.setAddress(userOb.getAddress());
		user.setCity(userOb.getCity());
		user.setState(userOb.getState());
		user.setZip(userOb.getZip());
		user.setAge(userOb.getAge());

		// Create an empty dom document and serialize the usertype object to it
        Document responseDoc = Management.newDocument();

        try {
			binding.marshal(userFactory.createUser(user), responseDoc );
		} catch (Exception e) {
			throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
		} 	         

		//DONE: figure out if this is a fragment transfer request
		try {
			SOAPElement[] allHeaders = request.getHeaders();
			String xpathExp ="";
			SOAPElement fragmentHeader = null;

			//Locate Fragment header
			fragmentHeader =locateFragmentHeader(allHeaders);
			//Extract the XPath component
			xpathExp = extractFragmentMessage(fragmentHeader);
			
			//DONE: do XPath on body and return results.
			if(fragmentHeader!= null){
				//DONE: extract Body contents
				 Object resultOb = null;
				 Node nod =null;
				resultOb = xpath.evaluate(xpathExp, responseDoc, XPathConstants.NODESET);
				if(resultOb!=null){
					NodeList nodelist = (NodeList)resultOb;
					for (int i = 0; i < nodelist.getLength(); i++) {
						nod = nodelist.item(i);
					}
				}
				
		        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
		        mixedDataType.getContent().add(nod);
		        //create the XmlFragmentElement
		        final JAXBElement<MixedDataType> xmlFragment = 
		                Management.FACTORY.createXmlFragment(mixedDataType);
		        
		        //add the Fragment header passed in to the response
		        fragmentHeader.setTextContent(xpathExp);
		        response.getHeader().addChildElement(fragmentHeader);
		        
		        //add payload to the body
		        new Addressing().getXmlBinding().marshal(xmlFragment, response.getBody());
//		        System.out.println("Resp:"+response);

			}else{
			  response.getBody().addDocument(responseDoc);
			}
		} catch (SOAPException e1) {
			e1.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SOAPElement locateFragmentHeader(SOAPElement[] allHeaders ){
		SOAPElement fragmentHeader = null;
		if(allHeaders!=null){
		 for (int i = 0; ((fragmentHeader==null) &&(i < allHeaders.length)); i++) {
			SOAPElement element = allHeaders[i];
			QName elems = element.getElementQName();
			if(elems!=null){
				if((elems.getLocalPart().equalsIgnoreCase(FRAGMENT_TRANSFER.getLocalPart()))&&
				   (elems.getPrefix().equalsIgnoreCase(FRAGMENT_TRANSFER.getPrefix()))&&
				   (elems.getNamespaceURI().equalsIgnoreCase(FRAGMENT_TRANSFER.getNamespaceURI()))
				   ){
				  fragmentHeader = element;
				}
			}
		 }
		}
		return fragmentHeader;
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

	private UserModelObject findInstance(Management request) {
		UserModelObject searchUser=new UserModelObject();	
		try {
			searchUser.setFirstname(Utilities.getSelectorByName("firstname",request.getSelectors()).getContent().get(0).toString());
			searchUser.setLastname(Utilities.getSelectorByName("lastname",request.getSelectors()).getContent().get(0).toString());
		} catch (Exception e) {
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		} 

		if(!users.contains(searchUser))
			throw new InvalidSelectorsFault(InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		
		UserModelObject userOb=null;
		for (Iterator iter = users.iterator(); iter.hasNext();) {
			UserModelObject userObTest = (UserModelObject) iter.next();
			if(userObTest.hashCode()==searchUser.hashCode()){
				userOb=userObTest;
				break;
			}
		}
		if(userOb==null)
			throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
		return userOb;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void put(Management request, Management response) {
		// Find an existing instance of this object in the model
		UserModelObject userModelObject = findInstance(request);
		
		// Init the user by transfering the fields from
		// the JAXB user to the 
		SOAPBody body = request.getBody();
		Node userChildNode = body.getFirstChild();

		//DONE: figure out if this is a fragment transfer request
		try {
			SOAPElement[] allHeaders = request.getHeaders();
			String xpathExp ="";
			SOAPElement fragmentHeader = null;

			//Locate Fragment header
			fragmentHeader =locateFragmentHeader(allHeaders);
			//Extract the XPath component
			xpathExp = extractFragmentMessage(fragmentHeader);
			
			//DONE: do XPath on body and return results.
			Document responseDoc = Management.newDocument();
			if(fragmentHeader!= null){
				
		        //add the Fragment header passed in to the response
		        fragmentHeader.setTextContent(xpathExp);
		        response.getHeader().addChildElement(fragmentHeader);
				
				//DONE:create an XML representation to run XPath on
				UserType tempUser = new UserType();
				tempUser.setFirstname(userModelObject.getFirstname());
				tempUser.setLastname(userModelObject.getLastname());
				tempUser.setAddress(userModelObject.getAddress());
				tempUser.setCity(userModelObject.getCity());
				tempUser.setState(userModelObject.getState());
				tempUser.setZip(userModelObject.getZip());
				tempUser.setAge(userModelObject.getAge());
//				try {
					binding.marshal(userFactory.createUser(tempUser), responseDoc );
//				} catch (JAXBException e) {
//					throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
//				}
				
				//DONE: extract Body contents
				 Object resultOb = null;
				 Node nod =null;
				resultOb = xpath.evaluate(xpathExp, responseDoc, XPathConstants.NODESET);
				if(resultOb!=null){
					NodeList nodelist = (NodeList)resultOb;
					for (int i = 0; i < nodelist.getLength(); i++) {
						nod = nodelist.item(i);
					}
				}
				//DONE: act on the XPath isolated entity
				String nodeAsString = xmlToString(nod).trim(); 
				String stNode = "<ns9:state";String stEndNode = "</ns9:state>";
				String stAmpNode = "&lt;ns9:state";String stAmpEndNode = "&lt;/ns9:state&gt;";
//				System.out.println("stNode:"+xmlToString(nod)+":");
				if(nodeAsString.indexOf(stNode)>-1){
				   String newValue = xmlToString(userChildNode);
//				   System.out.println("newValue:"+newValue);
				   int end =newValue.indexOf(stAmpEndNode);
//				   System.out.println("end:"+end);
				    int begin =newValue.indexOf(stAmpNode);
//				   System.out.println("beginFirst:"+begin);
				    begin =newValue.indexOf("&gt;",begin)+4;
//				   System.out.println("begin:"+begin);
				   String extracted = newValue.substring(begin,end);
//				   System.out.println("Extracted:"+extracted);
				   userModelObject.setState(extracted);
				   tempUser.setState(extracted);
				   //Now extract the changed value 
				   responseDoc = Management.newDocument();
				   binding.marshal(userFactory.createUser(tempUser), responseDoc );
					resultOb = xpath.evaluate(xpathExp, responseDoc, XPathConstants.NODESET);
					if(resultOb!=null){
						NodeList nodelist = (NodeList)resultOb;
						for (int i = 0; i < nodelist.getLength(); i++) {
							nod = nodelist.item(i);
						}
					}
				}
				
		        final MixedDataType mixedDataType = Management.FACTORY.createMixedDataType();
		        mixedDataType.getContent().add(nod);
		        //create the XmlFragmentElement
		        final JAXBElement<MixedDataType> xmlFragment = 
		                Management.FACTORY.createXmlFragment(mixedDataType);
		        
		        //add payload to the body
		        new Addressing().getXmlBinding().marshal(xmlFragment, response.getBody());
//		        System.out.println("Resp:"+response);

		} else{ //ELSE Process NON-FRAGMENT requests
//			  response.getBody().addDocument(responseDoc);

		UserType user=null;
		
		try {
			JAXBElement<UserType> ob = (JAXBElement<UserType>)binding.unmarshal(userChildNode);
			user=(UserType)ob.getValue();
			
		} catch (JAXBException e) {
			throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
		}
		
		// Set all fields to conform to the provided User document
		userModelObject.setFirstname(user.getFirstname());
		userModelObject.setLastname(user.getLastname());
		userModelObject.setAddress(user.getAddress());
		userModelObject.setCity(user.getCity());
		userModelObject.setState(user.getState());
		userModelObject.setZip(user.getZip());
		userModelObject.setAge(user.getAge());
		}
	  }catch(XPathExpressionException epex){
		  epex.printStackTrace();
	  } catch (JAXBException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  } catch (SOAPException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}
	
	@Override                                         
	public void delete(Management request, Management response) {
		// Find an existing instance
		UserModelObject searchUser=new UserModelObject();			
		try {
			searchUser.setFirstname(Utilities.getSelectorByName("firstname",request.getSelectors()).getContent().get(0).toString());
			searchUser.setLastname(Utilities.getSelectorByName("lastname",request.getSelectors()).getContent().get(0).toString());
		} catch (Exception e) {
			throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
		} 
		
		if(!users.contains(searchUser))
			throw new InvalidRepresentationFault(InvalidRepresentationFault.Detail.INVALID_VALUES);
		else{
//			users.remove(searchUser);
			try {
				SOAPElement[] allHeaders = request.getHeaders();
				String xpathExp ="";
				SOAPElement fragmentHeader = null;

				//Locate Fragment header
				fragmentHeader =locateFragmentHeader(allHeaders);
				//Extract the XPath component
				xpathExp = extractFragmentMessage(fragmentHeader);
				
				//TODO: do delete only the described sub elements.
				if(fragmentHeader!= null){
					//Delete that one optional component from searchUser
//   UserModelObject.class.
			users.remove(searchUser);

					UserModelObject withoutAge = new UserModelObject();
					  withoutAge.setAddress(searchUser.getAddress());
					  withoutAge.setCity(searchUser.getCity());
					  withoutAge.setFirstname(searchUser.getFirstname());
					  withoutAge.setLastname(searchUser.getLastname());
					  withoutAge.setState(searchUser.getState());
					  withoutAge.setZip(searchUser.getZip());
					users.add(withoutAge);
					
			        //add the Fragment header passed in to the response
			        fragmentHeader.setTextContent(xpathExp);
			        response.getHeader().addChildElement(fragmentHeader);
//			         System.out.println("DelResp:"+response);
				}else{//If NO Fragment processing then
					users.remove(searchUser);
				}
			} catch (SOAPException e1) {
				e1.printStackTrace();
			} 
//			catch (XPathExpressionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JAXBException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		}

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
    
    public void setFragmentHeader(final String expression, final String dialect,
    		Management mgmt) throws SOAPException, JAXBException {

        // remove existing, if any
//        removeChildren(mgmt.getHeader(), FRAGMENT_TRANSFER);
        
        final DialectableMixedDataType dialectableMixedDataType = 
                Management.FACTORY.createDialectableMixedDataType();
        if (dialect != null) {
//            if (!XPath.isSupportedDialect(dialect)) {
//                throw new FragmentDialectNotSupportedFault(XPath.SUPPORTED_FILTER_DIALECTS);
//            }
            dialectableMixedDataType.setDialect(dialect);
        }
        dialectableMixedDataType.getOtherAttributes().put(SOAP.MUST_UNDERSTAND, 
                Boolean.TRUE.toString());
        
        //add the query string to the content of the FragmentTransfer Header
        dialectableMixedDataType.getContent().add(expression);
        
        final JAXBElement<DialectableMixedDataType> fragmentTransfer =
                Management.FACTORY.createFragmentTransfer(dialectableMixedDataType);
        
        //set the SOAP Header for Fragment Transfer
        new Addressing().getXmlBinding().marshal(fragmentTransfer, mgmt.getHeader());
    }

}
