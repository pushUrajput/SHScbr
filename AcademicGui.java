/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package examples.universityerp;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
  @author Pushpendra K Rajput
 */
class AcademicGui extends JFrame {	
	private AcademicAgent myAgent;
	
	private JTextField sapIdField, yearField, cgpaField,backlogField;
	
	AcademicGui(AcademicAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(4, 2));
		p.add(new JLabel("Sap ID:"));
		sapIdField = new JTextField(15);
		p.add(sapIdField);
		p.add(new JLabel("year:"));
		yearField = new JTextField(15);
		p.add(yearField);
		p.add(new JLabel("CGPA:"));
		cgpaField = new JTextField(15);
		p.add(cgpaField);
		p.add(new JLabel("BackLog:"));
		backlogField = new JTextField(15);
		p.add(backlogField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String sapid = sapIdField.getText().trim();
					String year = yearField.getText().trim();
					String cgpa = cgpaField.getText().trim();
					String backlog = backlogField.getText().trim();
					//myAgent.updateAcademics(sapid, Integer.parseInt(year), Float.parseFloat(cgpa), Integer.parseInt(backlog));
					sapIdField.setText("");
					yearField.setText("");
					cgpaField.setText("");
					backlogField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(AcademicGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	
}
