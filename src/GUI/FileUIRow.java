package GUI;

import Client.File;

import javax.swing.*;

/**
 * Wrapper class to hold a reference to each of the swing elements needed for displaying a received file row.
 * <p>
 * Initialises the swing elements and sets up action listeners.
 */
class FileUIRow {
	final JPanel panel;
	final JButton acceptBtn;
	final JButton declineBtn;
	final JButton openBtn;
	final JLabel label;
	
	public FileUIRow(File file, MainMenu mainMenu) {
		acceptBtn = new JButton();
		declineBtn = new JButton();
		openBtn = new JButton();
		panel = new javax.swing.JPanel();
		
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.LINE_AXIS));
		javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
		
		label = new javax.swing.JLabel();
		label.setText(file.name);
		panel.add(label);
		panel.add(filler1);
		
		javax.swing.JButton acceptBtn11 = acceptBtn;
		acceptBtn11.setText("Accept");
		acceptBtn11.addActionListener(evt -> mainMenu.acceptFile(file.ID));
		panel.add(acceptBtn11);
		
		javax.swing.JButton declineBtn11 = declineBtn;
		declineBtn11.setText("Decline");
		declineBtn11.setToolTipText("");
		declineBtn11.addActionListener(evt -> mainMenu.declineFile(file.ID));
		panel.add(declineBtn11);
		
		javax.swing.JButton openBtn11 = openBtn;
		openBtn11.setText("Open");
		openBtn11.addActionListener(evt -> mainMenu.openFile(file.ID));
		panel.add(openBtn11);
		
		openBtn.setEnabled(false);
	}
}
