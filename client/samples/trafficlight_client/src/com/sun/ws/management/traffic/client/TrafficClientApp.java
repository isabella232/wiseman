package com.sun.ws.management.traffic.client;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathExpressionException;

import com.sun.ws.management.client.EnumerableResource;
import com.sun.ws.management.client.Resource;
import com.sun.ws.management.client.ResourceFactory;
import com.sun.ws.management.client.ResourceState;
import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.exceptions.NoMatchFoundException;
import com.sun.ws.management.transport.HttpClient;


/**
 * A client application for the trafficlight WS-Man resource.
*/
public class TrafficClientApp extends javax.swing.JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel jPanelTop;
	private JPanel jPanelCenter;
	private JLabel jLabelHostname;
	private LightPropertiesJPanel lightPropertiesJPanel;
	private JButton jButtonGet;
	private JButton jButtonPut;
	private JButton jButtonDelete;
	private JButton jButtonCreate;
	private JPanel jPanelBottom;
	private JList jListLights;
	private JScrollPane jScrollPaneList;
	private JSplitPane jSplitPane;
	private JTextField jTextFieldHostname;
	private JButton jButtonEnum;

	
	private static final String TRAFFIC_NS_URI = "http://hp.com/traffic/light/types";
    private static final String TRAFFIC_NS_PREFIX = "tl";

    public static final String PROPERTY_NAME_LOCALNAME = "name";
    public static final String PROPERTY_COLOR_LOCALNAME = "color";
    public static final String PROPERTY_X_LOCALNAME = "x";
    public static final String PROPERTY_Y_LOCALNAME = "y";

    public static final QName PROPERTY_NAME = new QName(TRAFFIC_NS_URI,PROPERTY_NAME_LOCALNAME,TRAFFIC_NS_PREFIX);
    public static final QName PROPERTY_COLOR = new QName(TRAFFIC_NS_URI,PROPERTY_COLOR_LOCALNAME,TRAFFIC_NS_PREFIX);
    public static final QName PROPERTY_X = new QName(TRAFFIC_NS_URI,PROPERTY_X_LOCALNAME,TRAFFIC_NS_PREFIX);
    public static final QName PROPERTY_Y = new QName(TRAFFIC_NS_URI,PROPERTY_Y_LOCALNAME,TRAFFIC_NS_PREFIX);

	public static final String RESOURCE_URI="wsman:traffic/light";
	public static final String RESOURCEFACTORY_URI="wsman:traffic/lightfactory";
	public static final String RESOURCELIST_URI="wsman:traffic/lightlist";

	
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		TrafficClientApp inst = new TrafficClientApp();
		inst.setVisible(true);
	}
	
	public TrafficClientApp() {
		super();		
		initGUI();
		
		// Temporarily set authentication manually
		System.setProperty("wsman.user", "wsman");
        System.setProperty("wsman.password", "secret");
        HttpClient.setAuthenticator(new TrafficAuthenticator());
 
		
		getExistingLights();
	}
	
	/**
	 * Uses Enumeration to find out if the server is aware of any existing
	 * lights.
	 * @throws DatatypeConfigurationException 
	 * @throws FaultException 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws SOAPException 
	 * @throws NoMatchFoundException 
	 * @throws XPathExpressionException 
	 *
	 */
	private void getExistingLights() {
		String endpointUrl="http://"+jTextFieldHostname.getText()+"/traffic/";
		DefaultComboBoxModel model = ((DefaultComboBoxModel)jListLights.getModel());
		model.removeAllElements();
		try {
			Resource[] resources = ResourceFactory.find(endpointUrl,RESOURCELIST_URI,30000,null);
			
			if(resources==null||resources.length==0){
				JOptionPane.showMessageDialog(this,"An error has occured during your enumerate operation: There is no enumeration resource","Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			EnumerableResource enumResource = (EnumerableResource) resources[0];
			String[] filters=null;
			boolean useEprs=true;
			String context = enumResource.enumerate(filters,Resource.XPATH_DIALECT,useEprs);
			int timeout = 30000;
			int numberOfRecords=5;
			
			// Work in batches of 5
			while(true){
				Resource[] lightsObs = null;
				try {
					lightsObs =enumResource.pullResources(context,timeout,numberOfRecords,Resource.IGNORE_MAX_CHARS,endpointUrl);					
				} catch (Exception e) {
					break;
				}
				if(lightsObs==null||lightsObs.length==0){
					break;
				}
					
				for (Object resource : lightsObs) {
					model.addElement((Resource)resource);				
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,"An error has occured during your enumerate operation: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Constructs the UI
	 *
	 */
	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.setTitle("Traffic Light Client");
			{
				jPanelTop = new JPanel();
				FlowLayout jPanelTopLayout = new FlowLayout();
				jPanelTopLayout.setAlignment(FlowLayout.LEFT);
				jPanelTop.setLayout(jPanelTopLayout);
				getContentPane().add(jPanelTop, BorderLayout.NORTH);
				{
					jLabelHostname = new JLabel();
					jPanelTop.add(jLabelHostname);
					jLabelHostname.setText("HostName");
				}
				{
					jTextFieldHostname = new JTextField();
					jPanelTop.add(jTextFieldHostname);
					jTextFieldHostname.setText("localhost:8080");
					jTextFieldHostname.setMinimumSize(new java.awt.Dimension(8, 30));
					jTextFieldHostname.setPreferredSize(new java.awt.Dimension(110, 20));
				}
			}
			{
				jPanelCenter = new JPanel();
				BorderLayout jPanelCenterLayout = new BorderLayout();
				jPanelCenter.setLayout(jPanelCenterLayout);
				getContentPane().add(jPanelCenter, BorderLayout.CENTER);
				{
					jSplitPane = new JSplitPane();
					jPanelCenter.add(jSplitPane, BorderLayout.CENTER);
					jSplitPane.setDividerLocation(80);
					{
						jScrollPaneList = new JScrollPane();
						jSplitPane.add(jScrollPaneList, JSplitPane.LEFT);
						{
							ListModel jListLightsModel = new DefaultComboBoxModel(
								new String[] {  });
							jListLights = new JList();
							jScrollPaneList.setViewportView(jListLights);
							jListLights.setModel(jListLightsModel);
							jListLights
								.addListSelectionListener(new ListSelectionListener() {
								public void valueChanged(ListSelectionEvent evt) {
									jListLightsValueChanged(evt);
								}
								});
						}
					}
					{
						lightPropertiesJPanel = new LightPropertiesJPanel();
						jSplitPane.add(lightPropertiesJPanel, JSplitPane.RIGHT);
					}
				}
			}
			{
				jPanelBottom = new JPanel();
				getContentPane().add(jPanelBottom, BorderLayout.SOUTH);
				{
					jButtonEnum = new JButton();
					jPanelBottom.add(jButtonEnum);
					jButtonEnum.setText("Enumerate");
					jButtonEnum.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							getExistingLights();
							
						}
					});
				}
				{
					jButtonCreate = new JButton();
					jPanelBottom.add(jButtonCreate);
					jButtonCreate.setText("Create");
					jButtonCreate.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							create(e);
							
						}
					});
				}
				{
					jButtonDelete = new JButton();
					jPanelBottom.add(jButtonDelete);
					jButtonDelete.setText("Delete");
					jButtonDelete.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							delete(e);							
						}
					});
				}
				{
					jButtonPut = new JButton();
					jPanelBottom.add(jButtonPut);
					jButtonPut.setText("Put");
					jButtonPut.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							put(e);
							
						}
					});
				}
				{
					jButtonGet = new JButton();
					jPanelBottom.add(jButtonGet);
					jButtonGet.setText("Get");
					jButtonGet.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							get(e);
							
						}
					});
				}
			}
			pack();
			setSize(500, 300);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handler method for then the get button is pressed.
	 * @param e
	 */
	protected void get(ActionEvent e) {
		Resource selectedResource = (Resource)jListLights.getSelectedValue();
		try {
			ResourceState state = selectedResource.get();		
			lightPropertiesJPanel.setName(state.getWrappedValueText(PROPERTY_NAME));
			lightPropertiesJPanel.setColor(state.getWrappedValueText(PROPERTY_COLOR));					
			lightPropertiesJPanel.setXPos(state.getWrappedValueText(PROPERTY_X));	
			lightPropertiesJPanel.setYPos(state.getWrappedValueText(PROPERTY_Y));						
		
		} catch (Exception e1) {
			if(e!=null){
				JOptionPane.showMessageDialog(this,"An error has occured during your get operation: "+e1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} 
		
	}

	/**
	 * Handler method for then the put button is pressed.
	 * @param e
	 */
	protected void put(ActionEvent e) {

		Resource selectedResource = (Resource)jListLights.getSelectedValue();
		try {
			ResourceState state = selectedResource.get();		
			state.setWrappedFieldValue(PROPERTY_COLOR,lightPropertiesJPanel.getColor());
			state.setWrappedFieldValue(PROPERTY_NAME,lightPropertiesJPanel.getName());
			state.setWrappedFieldValue(PROPERTY_X,lightPropertiesJPanel.getXPos());
			state.setWrappedFieldValue(PROPERTY_Y,lightPropertiesJPanel.getYPos());
				selectedResource.put(state);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this,"An error has occured during your put operation: "+e1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		} 
		
	}

	/**
	 * Handler method for then the delete button is pushed
	 * @param e
	 */
	protected void delete(ActionEvent e) {
	
		Resource selectedResource = (Resource)jListLights.getSelectedValue();
		if(selectedResource==null){
			JOptionPane.showMessageDialog(this,"Please select a resource to delete first.","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
			
		try {
			if(jListLights.getSelectedIndex()==-1)
				return;
			selectedResource.delete();
			((DefaultComboBoxModel)jListLights.getModel()).removeElementAt(jListLights.getSelectedIndex());
		}  catch (Exception e1) {
			JOptionPane.showMessageDialog(this,"An error has occured during your delete operation: "+e1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
		
	}

	/**
	 * Handler method for the create button.
	 * @param e
	 */
	protected void create(ActionEvent e) {
		try {
			String endpointUrl="http://"+jTextFieldHostname.getText()+"/traffic/";
			Resource resource = ResourceFactory.create(endpointUrl,RESOURCEFACTORY_URI,30000,null,ResourceFactory.LATEST);
			DefaultComboBoxModel model = (DefaultComboBoxModel)jListLights.getModel();
			model.addElement(resource);
			jListLights.setSelectedIndex(model.getSize()-1);
			get(e);
		}  catch (Exception e1) {
			JOptionPane.showMessageDialog(this,"An error has occured during your create operation: "+e1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}		
	}
	
	
	/**
	 * A handler method for changes in the list of traffic light
	 * names.
	 * @param evt
	 */
	private void jListLightsValueChanged(ListSelectionEvent evt) {
		// Do a get whenever there is a create so that the 
		// property panel populates
		get(null);
	}

}
