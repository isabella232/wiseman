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
 * $Id: LightPropertiesJPanel.java,v 1.2 2007-05-30 20:30:26 nbeers Exp $
 */
package net.java.dev.wiseman.traffic.light.client;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * This is a simple form that displays the resource properties of a traffic
 * light.
 */
public class LightPropertiesJPanel extends javax.swing.JPanel {
	private JLabel jLabelName;

	private JTextField jTextFieldName;

	private JTextField jTextFieldYPos;

	private JLabel jLabelYPos;

	private JTextField jTextFieldXPos;

	private JLabel jLabelXPos;

	private JComboBox jComboBoxColor;

	private JLabel jLabelColor;

	/**
	 * Auto-generated main method to display this JPanel inside a new JFrame.
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new LightPropertiesJPanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public LightPropertiesJPanel() {
		super();
		initGUI();
	}

	private void initGUI() {
		try {
			this.setLayout(null);
			this.setPreferredSize(new java.awt.Dimension(300, 300));
			{
				jLabelName = new JLabel();
				this.add(jLabelName);
				jLabelName.setText("Name:");
				jLabelName.setBounds(6, 8, 31, 14);
				jLabelName.setPreferredSize(new java.awt.Dimension(100, 14));
				jLabelName.setSize(75, 14);
				jLabelName.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			{
				jTextFieldName = new JTextField();
				this.add(jTextFieldName);
				jTextFieldName.setBounds(91, 7, 147, 21);
				jTextFieldName.setEditable(false);
			}
			{
				jLabelColor = new JLabel();
				this.add(jLabelColor);
				jLabelColor.setText("Color:");
				jLabelColor.setBounds(7, 35, 42, 14);
				jLabelColor.setSize(75, 14);
				jLabelColor.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			{
				ComboBoxModel jComboBoxColorModel = new DefaultComboBoxModel(
						new String[] { "none", "red", "yellow", "green" });
				jComboBoxColor = new JComboBox();
				this.add(jComboBoxColor);
				jComboBoxColor.setModel(jComboBoxColorModel);
				jComboBoxColor.setBounds(91, 35, 49, 21);
				jComboBoxColor.setSize(70, 21);
			}
			{
				jLabelXPos = new JLabel();
				this.add(jLabelXPos);
				jLabelXPos.setText("X Position:");
				jLabelXPos.setBounds(7, 98, 77, 14);
				jLabelXPos.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			{
				jTextFieldXPos = new JTextField();
				this.add(jTextFieldXPos);
				jTextFieldXPos.setBounds(91, 91, 49, 21);
			}
			{
				jLabelYPos = new JLabel();
				this.add(jLabelYPos);
				jLabelYPos.setText("Y Position:");
				jLabelYPos.setBounds(7, 70, 77, 14);
				jLabelYPos.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			{
				jTextFieldYPos = new JTextField();
				this.add(jTextFieldYPos);
				jTextFieldYPos.setBounds(91, 63, 49, 21);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getColor() {
		return (String) jComboBoxColor.getSelectedItem();
	}

	public void setColor(String color) {
		jComboBoxColor.setSelectedItem(color);
	}

	public String getName() {
		return jTextFieldName.getText();
	}

	public void setName(String name) {
		jTextFieldName.setText(name);
	}

	public String getXPos() {
		return jTextFieldXPos.getText();
	}

	public void setXPos(String x) {
		jTextFieldXPos.setText(x);

	}

	public String getYPos() {
		return jTextFieldYPos.getText();
	}

	public void setYPos(String y) {
		jTextFieldYPos.setText(y);
	}

}
