package com.sun.traffic.light.ui;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * A Panel that displays a traffic light and exposes
 * its color property encapsulating management of
 * which light picture should be shown.
*/
public class TrafficLightJPanel extends javax.swing.JPanel {
	private JLabel traffic;
	private JLabel trafficYellow;
	private JLabel trafficGreen;
	private JLabel trafficRed;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new TrafficLightJPanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public TrafficLightJPanel() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(400, 300));
			this.setBackground(new java.awt.Color(255,255,255));
			{
				traffic = new JLabel();
				this.add(traffic, BorderLayout.CENTER);
				traffic.setIcon(new ImageIcon(getClass().getClassLoader().getResource("com/hp/traffic/light/ui/images/trafic.gif")));
				traffic.setPreferredSize(new java.awt.Dimension(68, 130));
				traffic.setSize(68, 130);
			}
			{
				trafficRed = new JLabel();
				this.add(trafficRed, BorderLayout.CENTER);
				trafficRed.setIcon(new ImageIcon(getClass().getClassLoader().getResource("com/hp/traffic/light/ui/images/trafic_red.gif")));
				trafficRed.setVisible(false);
				trafficRed.setPreferredSize(new java.awt.Dimension(68, 130));
				trafficRed.setSize(68, 130);
			}
			{
				trafficYellow = new JLabel();
				this.add(trafficYellow, BorderLayout.CENTER);
				trafficYellow.setIcon(new ImageIcon(getClass().getClassLoader().getResource("com/hp/traffic/light/ui/images/trafic_yellow.gif")));
				trafficYellow.setVisible(false);
				trafficYellow.setPreferredSize(new java.awt.Dimension(68, 130));
				trafficYellow.setSize(68, 130);
			}
			{
				trafficGreen = new JLabel();
				this.add(trafficGreen, BorderLayout.CENTER);
				trafficGreen.setIcon(new ImageIcon(getClass().getClassLoader().getResource("com/hp/traffic/light/ui/images/trafic_green.gif")));
				trafficGreen.setVisible(false);
				trafficGreen.setPreferredSize(new java.awt.Dimension(68, 130));
				trafficGreen.setSize(68, 130);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getColor() {
		
		if(trafficRed!=null&&trafficRed.isVisible())
			return "red";
		if(trafficGreen!=null&&trafficGreen.isVisible())
			return "green";
		if(trafficYellow!=null&&trafficYellow.isVisible())
			return "yellow";
		return "none";
	}

	public void setColor(final String color) {
		if(traffic==null)
			return;
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				if(trafficRed==null)
					return;
				if(color.equals("none")){
					traffic.setVisible(true);
					trafficRed.setVisible(false);
					trafficGreen.setVisible(false);
					trafficYellow.setVisible(false);
					return;
				}
				if(color.equals("red")){
					traffic.setVisible(false);
					trafficRed.setVisible(true);
					trafficGreen.setVisible(false);
					trafficYellow.setVisible(false);
					return;
				}
				if(color.equals("green")){
					traffic.setVisible(false);
					trafficRed.setVisible(false);
					trafficGreen.setVisible(true);
					trafficYellow.setVisible(false);
					return;
				}
				if(color.equals("yellow")){
					traffic.setVisible(false);
					trafficRed.setVisible(false);
					trafficGreen.setVisible(false);
					trafficYellow.setVisible(true);
					return;
				}				
			}});
	}

}
