package com.sun.ws.management.metadata.viewer;

import com.sun.ws.management.mex.MetadataUtility;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.identify.Identify;
import com.sun.ws.management.identify.IdentifyUtility;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transfer.TransferUtility;
import com.sun.ws.management.transport.HttpClient;

public class MetadataViewer extends JFrame {
	
	  //default fields
	  private static final Logger LOG = Logger.getLogger(MetadataViewer.class.getName());
	  private JScrollPane scrollpane;
	  private JTree defaultTree;
	  private DefaultTreeModel model = null;
	  private static JTextArea messages = null;
	  private DefaultMutableTreeNode rootNode = null;
	  private JButton load = null;
	  private JTextField wisemanHost = null;
	  private static MetadataViewer guiHandle = null;
	  static DefaultMutableTreeNode emptyNode = null;
	  public JMenuBar menuBar;
	  private static String EDIT = "Edit";
	  private static String VIEW = "View";
	  private static String BASE_AUTH = "wsman.basicauthentication";
	  private static String BASE_USER = "wsman.user";
	  private static String BASE_PASS = "wsman.password";
	  
	  public MetadataViewer() {
		//frame settings
	    super("Metadata Viewer");
	    setSize(400, 300);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);

	    //root element settings
	    rootNode = new DefaultMutableTreeNode("Exposed Wiseman Metadata Resources");
	    emptyNode = 
	    	new DefaultMutableTreeNode("(Currently no metadata to display, 'Load' to begin.)");
	    rootNode.add(emptyNode);
	    model = new DefaultTreeModel(rootNode);
	    defaultTree = new JTree(model);
	    defaultTree.setShowsRootHandles(true);
	    defaultTree.setEditable(false);
	    
	    //Build button panel and it's listener
	    JPanel topPanel = new JPanel();
	    load = new JButton("Load");
	    ActionListener butLis = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				initializeAuthenticationData();
				//locate button contents
				final String host = wisemanHost.getText().trim();
				MetadataViewer.loadHostMetaData(host,model);  
			}
	    };
	    load.addActionListener(butLis);
	    //populate with good default data.
	    wisemanHost = new JTextField("http://localhost:8080/wsman/",20);
	    topPanel.add(load); topPanel.add(wisemanHost);
	    scrollpane = new JScrollPane(defaultTree);
	    menuBar = new JMenuBar();
	    JMenu formatMenu = new JMenu("Properties");
	    MenuAction editAction = new MenuAction(EDIT);
	    formatMenu.add(editAction);
	    MenuAction viewAction = new MenuAction(VIEW);
	    formatMenu.add(viewAction);
	    menuBar.add(formatMenu);
	    setJMenuBar(menuBar);
//	    //add a logging text area
//	    messages=new JTextArea("Logging and message exchange may be shown in this area)");
//	    JScrollPane mesPane = new JScrollPane(messages);
//	    JPanel messagePanel = new JPanel();
//	    messagePanel.add(mesPane);
	    
	    getContentPane().add(scrollpane, BorderLayout.CENTER);
	    getContentPane().add(topPanel, BorderLayout.NORTH);
//	    getContentPane().add(messagePanel, BorderLayout.SOUTH);
	    setVisible(true);
	    guiHandle = this;
	  }
	  
	  class MenuAction extends AbstractAction {
		    public MenuAction(String text) {
		      super(text);
		    }
		    public void actionPerformed(ActionEvent e) {
		    	String message="";
		    	if(e.getActionCommand().equals(EDIT)){
		    		message+="Enter new name value properties \n";
		    		message+="to be set for the Viewer. \n";
		    		message+="Ex. "+BASE_AUTH+"=true, \n";
		    		message+=BASE_USER+"=wsman, "+BASE_PASS+"=secret \n";
		    		String response = JOptionPane.showInputDialog(guiHandle, 
		    				message, 
		    				"Add/Edit parameters...", 
		    				JOptionPane.PLAIN_MESSAGE);
		    		//process response to overwrite the values passed in
		    		if((response!=null)&&(response.trim().length()>0)){
		    			StringTokenizer tokens = new StringTokenizer(response," ,");
		    			while(tokens.hasMoreElements()){
		    			   StringTokenizer nvp =
		    				   new StringTokenizer(tokens.nextToken(),"=");
		    			   if(nvp.countTokens()==2){
		    				  String token = nvp.nextToken(); 
		    				  if(token.equals(BASE_AUTH)){
		    					   System.setProperty(BASE_AUTH, nvp.nextToken());
		    				  }
		    				  if(token.equals(BASE_USER)){
		    					   System.setProperty(BASE_USER, nvp.nextToken());
		    				  }
		    				  if(token.equals(BASE_PASS)){
		    					  System.setProperty(BASE_PASS, nvp.nextToken());
		    				  }
		    			   }
		    			}
		    		}
		    	}
		    	if(e.getActionCommand().equals(VIEW)){
		    		message = "";
		    		String authEnabled = BASE_AUTH;
		    		String u = BASE_USER;
		    		String p = BASE_PASS;
		    		message+=authEnabled+"="+System.getProperty(authEnabled)+"\n";
		    		message+=u+"="+System.getProperty(u)+"\n";
		    		message+=p+"="+System.getProperty(p)+"\n";
		        	JOptionPane.showMessageDialog(
		        			guiHandle, 
		        			message, 
		        			"Viewing current property values...", 
		        			JOptionPane.PLAIN_MESSAGE);
		    	}
		    }
	  }
	  
	  private static void initializeAuthenticationData(){
	    //Add authentication mechanism
		final String basicAuth = System.getProperty(BASE_AUTH);
        if ("true".equalsIgnoreCase(basicAuth)) {
            HttpClient.setAuthenticator(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    final String user = System.getProperty(BASE_USER, "");
                    final String password = System.getProperty(BASE_PASS, "");
                    return new PasswordAuthentication(user, password.toCharArray());
                }
            }
            );
        }else{//Display message to the effect that connection parameters were not located
        	String message="Unable to locate the system propety \n"+
        		"'"+BASE_AUTH+"'. Please set the following parameters: \n"+
        		"'"+BASE_AUTH+"', '"+BASE_USER+"' and '"+BASE_PASS+"' \n"	+
        		" in the drop down menu or pass them in as -D parameters.\n" ;
        	   message+="This tool may not perform without those values.\n";
        	JOptionPane.showMessageDialog(guiHandle, message, 
        			"Failure to locate http credentials...", 
        			JOptionPane.WARNING_MESSAGE);
        }
	  }
	  
	  private static void loadHostMetaData(String host, DefaultTreeModel rootTreeModel){
		  
		  DefaultMutableTreeNode rootNodeElement = (DefaultMutableTreeNode) rootTreeModel.getRoot();
		  if((host==null)||(host.trim().length()==0)){
			  return;
		  }
	      //post pend the trailing forward slash if it's not provided
		  if(!host.trim().endsWith("/")){
			 host = host+"/"; 
		  }
		  try {
	    	//############ REQUEST THE METADATA REPOSITORY DATA ######################
	        //Request identify info to get MetaData root information
	        final Identify identify = new Identify();
	        identify.setIdentify();
	        
//	        LOG.fine("Request message:"+identify.getMessage());
	        
	        //Send identify request
	        final Addressing response = HttpClient.sendRequest(identify.getMessage(), host);
	        if (response.getBody().hasFault()) {
	        	LOG.fine(response.getBody().getFault().getFaultString());
	        	return;
	        }
	        
	        //Parse the identify response
	        final Identify id = new Identify(response);
//	        final SOAPElement idr = id.getIdentifyResponse();
	        SOAPElement el =IdentifyUtility.locateElement(id, 
	        		AnnotationProcessor.META_DATA_RESOURCE_URI); 
	         //retrieve the MetaData ResourceURI
	         String resUri=el.getTextContent();
	         el =IdentifyUtility.locateElement(id, 
	        		AnnotationProcessor.META_DATA_TO);
	        //retrieve the MetaData To/Destination
	        String metTo=el.getTextContent();

	        //############ REQUEST THE LIST OF METADATA AVAILABLE ######################
		   //Build the GET request to be submitted for the metadata
	        Management m = null; 
	        m =TransferUtility.createMessage(metTo, resUri,
	        		Transfer.GET_ACTION_URI, null, null, 30000, null);
	        
	         //############ PROCESS THE METADATA RESPONSE ######################
	         //Parse the getResponse for the MetaData
	         final Addressing getResponse = HttpClient.sendRequest(m);
	       Management mResp = new Management(getResponse);

	       //Determine if any Metadata to display
		    //########### TRANSLATE METADATA TO FAMILIAR MANAGEMENT NODES ##### 
		     //Extract the MetaData node returned as Management instances
		     Management[] metaDataList = 
		        	MetadataUtility.extractEmbeddedMetaDataElements(mResp);
		     if((metaDataList!=null)&&(metaDataList.length>0)){
		    	 //remove (no metadata to display node)
		    	 if(rootNodeElement.getIndex(emptyNode)>-1){
		    		 rootNodeElement.remove(emptyNode);
		    	 }
		    	 
		    	 // update the jtree and it's sub components.
		    	 DefaultMutableTreeNode hostRoot = new DefaultMutableTreeNode(host);
		    	 for (int i = 0; i < metaDataList.length; i++) {
		    		 Management man = metaDataList[i];
		    		 DefaultMutableTreeNode node = null;
		    		 SOAPElement value = 
		    			 ManagementUtility.locateHeader(man.getHeaders(), 
		    					 AnnotationProcessor.RESOURCE_META_DATA_UID);
		    		 supportsEnumerations(man);
		    		if((value==null)||(value.getTextContent()==null)||
		    				(value.getTextContent().trim().length()==0)){
		    		  node = new DefaultMutableTreeNode(
		    				  "(No Unique identifier defined for this node. Please add one.)");
		    		}else{
		    		  if(supportsEnumerations(man)){
		    		   node = new DefaultMutableTreeNode(
		    				   value.getTextContent()+" -(suppports Enumeration)");  
		    		  }else{
					   node = new DefaultMutableTreeNode(value.getTextContent());
		    		  }
		    		}
		    		//now put all the sub elements of the metadata as leaves below this node
		    		populateNodeWithMetadata(node,man);

		    		//add the node to the root.
					hostRoot.add(node);
				 }
		    	//prune for duplicates. 
		        Enumeration peers = rootNodeElement.children();
		        while (peers.hasMoreElements()) {
					DefaultMutableTreeNode element = 
						(DefaultMutableTreeNode) peers.nextElement();
					if(element.toString().equals(hostRoot.toString())){
						rootNodeElement.remove(element);
					}
				}

		        //put in the host root node
		    	rootNodeElement.add(hostRoot);
		    	rootTreeModel.reload(rootNodeElement);
		     }
			}catch(Exception e){
			   String message = e.getMessage();
			   for (int i = 0; i < e.getStackTrace().length; i++) {
				   message+="\n"+e.getStackTrace()[i].toString();
			   }
			   JOptionPane.showMessageDialog(guiHandle, 
					   message, 
					   "Error occurred during processing...", 
					   JOptionPane.ERROR_MESSAGE);
				System.out.println("Exception occurred:"+message);
//				messages.append(message);
			}
	  }

	/** Tests whether the management instance passed in contains any enumeration
	 * headers. 
	 * @param man
	 * @throws SOAPException
	 */
	private static boolean supportsEnumerations(Management man) throws SOAPException {
		boolean enumerationEnabled;
		 SOAPElement enumValue = 
			 ManagementUtility.locateHeader(man.getHeaders(), 
					 AnnotationProcessor.ENUMERATION_ACCESS_RECIPE);
		  SOAPElement enumValue2 = 
			ManagementUtility.locateHeader(man.getHeaders(), 
					AnnotationProcessor.ENUMERATION_FILTER_USAGE);
		  enumerationEnabled= false;
		  if(enumValue!=null){
			enumerationEnabled=true; 
		  }
		  if(enumValue2!=null){
			enumerationEnabled=true; 
		  }
		return enumerationEnabled;
	}
	  
	  /** Take a root node and a Management instance and populates a text
	   * representation of the contents of the Management node as child 
	   * elements of the node passed in.
	   * 
	   * @param node DefaultMutableTreeNode instance, should be root of project.
	   * @param man Management instance with Management addressing information.
	   * @throws JAXBException
	   * @throws SOAPException
	   */
	  private static void populateNodeWithMetadata(DefaultMutableTreeNode node, 
			  Management man) throws JAXBException, SOAPException {
		 if((node ==null)||(man==null)){
			 return;
		 }
		 else{
		   SOAPElement[] allHeaders = man.getHeaders();
		   QName selQName = Management.SELECTOR_SET;
		   for (int i = 0; i < allHeaders.length; i++) {
			   SOAPElement header = allHeaders[i];
			   //special parsing for SelectorSet.
			  if(selQName.getLocalPart().equals(header.getElementQName().getLocalPart())&&
				 selQName.getNamespaceURI().equals(header.getElementQName().getNamespaceURI())){
				Map<String, String> selectorsRetrieved = ManagementUtility.extractSelectorsAsMap(null,
					(List)new ArrayList<SelectorType>(man.getSelectors()));
				addNodeForElement(node, selectorsRetrieved); 
			  }
			  else{
				addNodeForElement(node, header);
			  }
		   }
		 }
	  }
	
	/** Adds the selector content as a displayable leaf node of the node passed in.
	 * 
	 * @param node
	 * @param selectorsRetrieved
	 */  
	private static void addNodeForElement(DefaultMutableTreeNode node, Map<String, String> selectorsRetrieved) {
		DefaultMutableTreeNode newNode,sel;
		if(selectorsRetrieved!=null){
			newNode = new DefaultMutableTreeNode(Management.SELECTOR_SET.getLocalPart());
			for (Iterator iter = selectorsRetrieved.keySet().iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				sel = new DefaultMutableTreeNode(element+"="+selectorsRetrieved.get(element));
				newNode.add(sel);
			}
			node.add(newNode);
		}
	}

	/** Adds the SoapElement contents as a displayable leaf node of the node passed in. 
	 * @param node 
	 * @param locatedElement
	 */
	private static void addNodeForElement(DefaultMutableTreeNode node, SOAPElement locatedElement) {
		DefaultMutableTreeNode newNode;
		if(locatedElement!=null){
			newNode = new DefaultMutableTreeNode(
					locatedElement.getElementQName().getLocalPart()+"="+
			locatedElement.getTextContent());
			node.add(newNode);
		}
	}

	/**To enable launch from command line.
	 * @param args 
	 */
	public static void main(String args[]) {
	    new MetadataViewer();
    }

}
