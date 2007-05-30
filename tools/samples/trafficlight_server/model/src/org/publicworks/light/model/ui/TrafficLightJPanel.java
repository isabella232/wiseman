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
 * $Id: TrafficLightJPanel.java,v 1.3 2007-05-30 20:30:26 nbeers Exp $
 */
package org.publicworks.light.model.ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;

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
	private static final String GREEN_TRAFICLIGHT_GIF = "org/publicworks/light/model/ui/images/trafic_green.gif";
	private static final String YELLOW_TRAFICLIGHT_GIF = "org/publicworks/light/model/ui/images/trafic_yellow.gif";
	private static final String RED_TRAFICLIGHT_GIF = "org/publicworks/light/model/ui/images/trafic_red.gif";
	private static final String DARK_TRAFICLIGHT_GIF = "org/publicworks/light/model/ui/images/trafic.gif";
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
				traffic.setIcon(new ImageIcon(getClass().getClassLoader().getResource(DARK_TRAFICLIGHT_GIF)));
				traffic.setPreferredSize(new java.awt.Dimension(68, 130));
				traffic.setSize(68, 130);
			}
			{
				trafficRed = new JLabel();
				this.add(trafficRed, BorderLayout.CENTER);
				trafficRed.setIcon(new ImageIcon(getClass().getClassLoader().getResource(RED_TRAFICLIGHT_GIF)));
				trafficRed.setVisible(false);
				trafficRed.setPreferredSize(new java.awt.Dimension(68, 130));
				trafficRed.setSize(68, 130);
			}
			{
				trafficYellow = new JLabel();
				this.add(trafficYellow, BorderLayout.CENTER);
				trafficYellow.setIcon(new ImageIcon(getClass().getClassLoader().getResource(YELLOW_TRAFICLIGHT_GIF)));
				trafficYellow.setVisible(false);
				trafficYellow.setPreferredSize(new java.awt.Dimension(68, 130));
				trafficYellow.setSize(68, 130);
			}
			{
				trafficGreen = new JLabel();
				this.add(trafficGreen, BorderLayout.CENTER);
				trafficGreen.setIcon(new ImageIcon(getClass().getClassLoader().getResource(GREEN_TRAFICLIGHT_GIF)));
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
		try{
		SwingUtilities.invokeAndWait(new Runnable(){
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
		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
