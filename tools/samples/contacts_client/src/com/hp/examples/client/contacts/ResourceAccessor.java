package com.hp.examples.client.contacts;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import com.sun.ws.management.Management;
import com.sun.ws.management.ManagementUtility;
import com.sun.ws.management.ResourceStateDocument;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.metadata.annotations.AnnotationProcessor;
import com.sun.ws.management.transfer.Transfer;
import com.sun.ws.management.transport.HttpClient;

public class ResourceAccessor extends JFrame {
	
	//Main method
	public static void main(String[] args) {
		new ResourceAccessor();
	}
	//######## GUI Property/Fields List ############
	private JButton load;
	protected JTextField wisemanHost;
	private JMenuBar menuBar;
	private JButton get;
	private JButton put;
	private JTextField metUid;
	private JTextField resFilter = null;
	private JTextField filteredResult = null;
	private JTextArea textArea=null;
	private JTextArea returnedContent = null;
	private static ResourceAccessor guiHandle;
    private static String EDIT = "Edit";
	private static String VIEW = "View";
    private static String BASE_AUTH = "wsman.basicauthentication";
	private static String BASE_USER = "wsman.user";
	private static String BASE_PASS = "wsman.password";
	private static String GET="GET";
	private static String PUT="PUT";
	private static String CREATE="CREATE";
	private static String DELETE="DELETE";


	// Constructor
	public ResourceAccessor(){
		
		//frame settings
	    super("Resource Accessor");
	    setSize(400, 600);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);

	    //Build service address panel and it's listener
	    JPanel servicePanel = new JPanel();
	     JLabel serviceAddress = new JLabel("Service Address:");
	     //populate with good default data.
	     wisemanHost = new JTextField("http://localhost:8080/users/",20);
	     servicePanel.add(serviceAddress); servicePanel.add(wisemanHost);
	     
	    //Build MetaDataUid set fields
	    JPanel metPanel = new JPanel();
	     JLabel metLabel = new JLabel("MetadataResourceUID:");
	     //populate with good default data.
	     metUid = new JTextField("http://localhost:8080/users/Bill.Gates",20);
	     metPanel.add(metLabel); metPanel.add(metUid);
	    
	    //Build the operations panel 
	    JPanel opPanel = new JPanel();
	     JLabel operations = new JLabel("Operations:"); 
	    
	    //Define button listener 
	     ActionListener butLis = new ActionListener(){
	    	 public void actionPerformed(ActionEvent e) {
	    		 initializeAuthenticationData();
	    		 //locate button selected
	    		 final String action = e.getActionCommand();
	    		 //extract the outbound content
	    		 if(action.equals(GET)){//Get action
	    			//
	    			String metaDataUID = metUid.getText();
	    			String serviceAddress = wisemanHost.getText();
    			  Management get;
    			  try {
						get = AnnotationProcessor.findAnnotatedResourceByUID(
									  metaDataUID,serviceAddress);
				    	//set Action to request of the service
				    	get.setAction(Transfer.GET_ACTION_URI);
				    	//run this Management instance through ManagementUtility
				    	get = ManagementUtility.buildMessage(get, null);
				    	//Use the default HttpClient or your own here to send the message off
				    	Addressing response =HttpClient.sendRequest(get);

				    	ResourceStateDocument resState = ManagementUtility.getAsResourceState(response);
				    	String content = response.toString();
				    	
				    	returnedContent.setText(content);
				    	String xpath = resFilter.getText();
				    	if((xpath!=null)&&(xpath.trim().length()>0)){
				    	  String result = resState.getValueText(xpath);
				    	  if((result!=null)&&(result.trim().length()>0)){
				    		 filteredResult.setText(result); 
				    	  }
				    	}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
	    		 }
	    	 }
	     };
	     //GET OPERATION button details.
	     	get = new JButton(GET);
	     get.addActionListener(butLis);
	     	put = new JButton(PUT);	
	     	put.setEnabled(false);
	     put.addActionListener(butLis);
	     
	    opPanel.add(operations);opPanel.add(get);opPanel.add(put); 

	    menuBar = new JMenuBar();
	    JMenu formatMenu = new JMenu("Properties");
	    MenuAction editAction = new MenuAction(EDIT);
	    formatMenu.add(editAction);
	    MenuAction viewAction = new MenuAction(VIEW);
	    formatMenu.add(viewAction);
	    menuBar.add(formatMenu);
	    setJMenuBar(menuBar);
	    
	    //TOP PANEL
	    JPanel topPanel = new JPanel();
	    topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.Y_AXIS));
	    topPanel.add(servicePanel); topPanel.add(metPanel); topPanel.add(opPanel);
	    //BODY PANEL
	    JPanel bodyPanel = new JPanel();
	      JLabel outboundLabel = new JLabel("Outbound Content:");
	      JTabbedPane tabs = new JTabbedPane();
	      //Pure text panel
	        JPanel textOnlyPanel = new JPanel();
	         textArea = new JTextArea("(Insert Payload Content)",10,35);
	          textOnlyPanel.add(textArea,BorderLayout.CENTER);
	      tabs.addTab("XML", textOnlyPanel);
	      //JAXB element serialization
	        JPanel jaxbPanel = new JPanel();
	        jaxbPanel.setLayout(new BoxLayout(jaxbPanel,BoxLayout.Y_AXIS));

	        //First Name
	        JLabel label = new JLabel("FirstName:");
	        JTextField fieldFName = new JTextField("",10);
	        JPanel line = new JPanel();
	          line.add(label); line.add(fieldFName);
	        jaxbPanel.add(line);
	        //Last Name
	        JLabel label1 = new JLabel("LastName:");
	        JTextField fieldLName = new JTextField("",10);
	        JPanel line1 = new JPanel();
	        line1.add(label1); line1.add(fieldLName);
	        jaxbPanel.add(line1);
	        //Address
	        JLabel label2 = new JLabel("Address:");
	        JTextField fieldAddress = new JTextField("",10);
	        JPanel line2 = new JPanel();
	        line2.add(label2); line2.add(fieldAddress);
	        jaxbPanel.add(line2);
	        //City
	        JLabel label3 = new JLabel("City:");
	        JTextField fieldCity = new JTextField("",10);
	        JPanel line3 = new JPanel();
	        line3.add(label3); line3.add(fieldCity);
	        jaxbPanel.add(line3);
	        //State
	        JLabel label4 = new JLabel("State:");
	        JTextField fieldState = new JTextField("",10);
	        JPanel line4 = new JPanel();
	        line4.add(label4); line4.add(fieldState);
	        jaxbPanel.add(line4);
	        //Zip
	        JLabel label5 = new JLabel("Zip:");
	        JTextField fieldZip = new JTextField("",10);
	        JPanel line5 = new JPanel();
	        line5.add(label5); line5.add(fieldZip);
	        jaxbPanel.add(line5);
	        //Age
	        JLabel label6 = new JLabel("Age:");
	        JTextField fieldAge = new JTextField("",10);
	        JPanel line6 = new JPanel();
	          line6.add(label6); line6.add(fieldAge);
	        jaxbPanel.add(line6);
	        
	      tabs.addTab("Custom Model", jaxbPanel);
	      JScrollPane scp = new JScrollPane(tabs);
	      
	      JLabel returnedContentLabel = new JLabel("Message Response:");
	      returnedContent = new JTextArea("(No payload to display)",10,35);
	       JPanel filterPanel = new JPanel();
	        JLabel resourceStateLabel = new JLabel("ResourceState Filter(XPath):");
	        resFilter = new JTextField("//*[local-name()='age']",15);
	         filterPanel.add(resourceStateLabel); filterPanel.add(resFilter);
	      JPanel filterResultPanel = new JPanel();
	        JLabel filtResultLabel = new JLabel("Filter Result:");
	        filteredResult = new JTextField("(No results to display)",15);
	        filteredResult.setEditable(false);
	        filterResultPanel.add(filtResultLabel); filterResultPanel.add(filteredResult);
	     //populate bodyPanel
	     bodyPanel.setLayout(new BoxLayout(bodyPanel,BoxLayout.Y_AXIS));
	     bodyPanel.add(outboundLabel);
	     bodyPanel.add(scp);
	     bodyPanel.add(returnedContentLabel);
	      JScrollPane scContent = new JScrollPane(returnedContent);
	     bodyPanel.add(scContent);
	     bodyPanel.add(filterPanel);
	     bodyPanel.add(filterResultPanel);
	      
	    getContentPane().add(topPanel, BorderLayout.NORTH);
	    getContentPane().add(bodyPanel, BorderLayout.CENTER);
	    setVisible(true);
	    guiHandle = this;
	  }
	  
	//Construct the properties menus
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

}

