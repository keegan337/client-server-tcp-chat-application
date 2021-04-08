package GUI;

import Client.Client;
import Client.File;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainMenu extends JFrame {
	
	private final Document doc;
	
	private String currentClientUserName;
	private int currentUserIndex;
	private int chatIndexOfSender;
	
	private final List<String> chats;
	private final ConcurrentMap<String, File> files;
	private final HashMap<String, FileUIRow> fileUIRowHashMap;
	
	private final Client client;
	
	//<editor-fold desc="Swing variable declarations">
	private javax.swing.JList<String> chatList;
	private javax.swing.JFileChooser fileChooser;
	private javax.swing.JTabbedPane jTabbedPane1;
	private javax.swing.JTextField messageInputField;
	private javax.swing.JTextArea messageTextArea;
	private javax.swing.JPanel receivedFilesPanelTab;
	private boolean disconnecting = false;
	//</editor-fold>
	
	
	/**
	 * Initialise the components of the view
	 * <p>
	 * Creates a connection between this MainMenu and the client to enable intercommunication.
	 * Initialises
	 *
	 * @param client this client
	 */
	public MainMenu(Client client) {
		initComponents();
		this.client = client;
		client.setMainViewReference(this);
		chats = Collections.synchronizedList(new ArrayList<>(3));
		files = new ConcurrentHashMap<String, File>();
		
		fileUIRowHashMap = new HashMap<>();
		
		currentClientUserName = "";
		currentUserIndex = 0;
		
		
		chatList.putClientProperty("List.isFileList", Boolean.TRUE);
		doc = this.messageTextArea.getDocument();
	}
	
	
	//<editor-fold desc="Don't touch this, it looks awful but it all works. Just tons of nasty swing code for setting up the view">
	private void initComponents() {
		
		JPanel chatPane = new JPanel();
		JScrollPane jScrollPane1 = new JScrollPane();
		chatList = new javax.swing.JList<>();
		JButton newChatBtn = new JButton();
		jTabbedPane1 = new javax.swing.JTabbedPane();
		JPanel messagesPanelTab = new JPanel();
		JScrollPane jScrollPane3 = new JScrollPane();
		JPanel jPanel1 = new JPanel();
		JPanel jPanel4 = new JPanel();
		messageTextArea = new javax.swing.JTextArea();
		JPanel jPanel2 = new JPanel();
		messageInputField = new javax.swing.JTextField();
		JButton sendbtn = new JButton();
		receivedFilesPanelTab = new javax.swing.JPanel();
		JPanel sendFilePanelTab = new JPanel();
		fileChooser = new javax.swing.JFileChooser();
		
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setBackground(new java.awt.Color(0, 204, 204));
		setResizable(false);
		
		chatPane.setPreferredSize(new java.awt.Dimension(130, 480));
		chatPane.setLayout(new javax.swing.BoxLayout(chatPane, javax.swing.BoxLayout.PAGE_AXIS));
		
		chatList.setModel(new DefaultListModel<>());
		chatList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				chatListMouseClicked(evt);
			}
		});
		jScrollPane1.setViewportView(chatList);
		
		chatPane.add(jScrollPane1);
		
		newChatBtn.setText("New Chat");
		newChatBtn.addActionListener(evt -> newChatBtnActionPerformed(evt));
		chatPane.add(newChatBtn);
		
		getContentPane().add(chatPane, java.awt.BorderLayout.LINE_START);
		
		messagesPanelTab.setLayout(new javax.swing.BoxLayout(messagesPanelTab, javax.swing.BoxLayout.PAGE_AXIS));
		
		jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane3.setAutoscrolls(true);
		jScrollPane3.setPreferredSize(new java.awt.Dimension(420, 265));
		
		jPanel1.setLayout(new java.awt.BorderLayout());
		
		jPanel4.setBackground(new java.awt.Color(255, 255, 255));
		jPanel4.setPreferredSize(new java.awt.Dimension(416, 430));
		
		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(
				jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGap(0, 876, Short.MAX_VALUE)
		                                );
		jPanel4Layout.setVerticalGroup(
				jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGap(0, 430, Short.MAX_VALUE)
		                              );
		
		jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);
		
		messageTextArea.setEditable(false);
		messageTextArea.setColumns(20);
		messageTextArea.setLineWrap(true);
		messageTextArea.setRows(5);
		jPanel1.add(messageTextArea, java.awt.BorderLayout.SOUTH);
		
		jScrollPane3.setViewportView(jPanel1);
		JScrollBar vertical = jScrollPane3.getVerticalScrollBar();
		vertical.setPreferredSize(new Dimension(0, 0));
		
		messagesPanelTab.add(jScrollPane3);
		
		jPanel2.setAlignmentY(0.5F);
		jPanel2.setMaximumSize(new java.awt.Dimension(550, 200));
		jPanel2.setMinimumSize(new java.awt.Dimension(10, 10));
		jPanel2.setPreferredSize(new java.awt.Dimension(420, 27));
		jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
		
		messageInputField.setText("");
		messageInputField.setToolTipText("Message Input");
		messageInputField.addActionListener(evt -> messageInputFieldActionPerformed(evt));
		jPanel2.add(messageInputField);
		
		sendbtn.setText("Send");
		sendbtn.addActionListener(evt -> sendBtnActionPerformed());
		jPanel2.add(sendbtn);
		
		
		messagesPanelTab.add(jPanel2);
		
		jTabbedPane1.addTab("Messages", null, messagesPanelTab, "This tab displays the messages with the current chat.");
		
		receivedFilesPanelTab.setLayout(new javax.swing.BoxLayout(receivedFilesPanelTab, javax.swing.BoxLayout.PAGE_AXIS));
		
		jTabbedPane1.addTab("Received Files", null, receivedFilesPanelTab, "Displays files shared in the current chat");
		
		fileChooser.addActionListener(evt -> fileChooserActionPerformed(evt));
		
		javax.swing.GroupLayout sendFilePanelTabLayout = new javax.swing.GroupLayout(sendFilePanelTab);
		sendFilePanelTab.setLayout(sendFilePanelTabLayout);
		sendFilePanelTabLayout.setHorizontalGroup(
				sendFilePanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(sendFilePanelTabLayout.createSequentialGroup()
								.addContainerGap()
								.addComponent(fileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		                                         );
		sendFilePanelTabLayout.setVerticalGroup(
				sendFilePanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sendFilePanelTabLayout.createSequentialGroup()
								.addGap(0, 34, Short.MAX_VALUE)
								.addComponent(fileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		                                       );
		
		jTabbedPane1.addTab("Send File", sendFilePanelTab);
		
		getContentPane().add(jTabbedPane1, java.awt.BorderLayout.EAST);
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				disconnecting = true;
				client.disconnectConnection();
			}
		});
		
		pack();
	}
	//</editor-fold>
	
	/**
	 * Adds a new file to the received files panel.
	 *
	 * @param file the file (to be) received
	 */
	private void addFileButton(File file) {
		FileUIRow fileUIRow = new FileUIRow(file, this);
		receivedFilesPanelTab.add(fileUIRow.panel);
		fileUIRowHashMap.put(file.ID, fileUIRow);
	}
	
	
	/**
	 * Display an input dialogue and send a connect request for that client
	 */
	private void newChatBtnActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {
		System.out.println("New Chat clicked");
		String inputUsername = JOptionPane.showInputDialog(this, "Please enter the username:");
		if (inputUsername != null) {
			System.out.println(inputUsername);
			client.createConnectionRequest(inputUsername);
		}
	}
	
	
	/**
	 * Add a new chat to the chatList, update the list of saved chats.
	 */
	private void handleNewChatConnected(String otherClientUsername) {
		System.out.println("New Client connected: " + otherClientUsername);
		((DefaultListModel<String>) chatList.getModel()).addElement(otherClientUsername);
		
		chats.add("");
		System.out.println("Added new client in position: " + (chats.size() - 1));
		if (chatList.getModel().getSize() == 1) {
			chatList.setSelectedIndex(0);
			currentClientUserName = otherClientUsername;
			currentUserIndex = 0;
		}
	}
	
	
	/**
	 * Display the select chat
	 * <p>
	 * If a new chat is selected, save the contents of the old chat window and display the new chat window,
	 * first loading any saved content.`
	 */
	@SuppressWarnings("unused")
	private void chatListMouseClicked(java.awt.event.MouseEvent evt) {
		System.out.println("Chat list clicked");
		String selectedUser = this.chatList.getSelectedValue();
		System.out.println("Selected user: " + selectedUser);
		
		// If not clicking on the same chat
		if (selectedUser != null && !selectedUser.equals(currentClientUserName)) {
			System.out.println("previous user index: " + currentUserIndex);
//			chats.set(currentUserIndex, this.messageTextArea.getText());
			
			currentClientUserName = selectedUser;
			currentUserIndex = this.chatList.getSelectedIndex();
			
			System.out.println("chats[currentIndex]: " + chats.get(currentUserIndex));
			this.messageTextArea.setText(chats.get(currentUserIndex));
		}
	}
	
	
	private void sendBtnActionPerformed() {
		sendInputMessage(this.messageInputField.getText());
	}
	
	private void messageInputFieldActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {
		sendInputMessage(this.messageInputField.getText());
	}
	
	/**
	 * Adds the message to the message display and clears the input field
	 * before passing the message data to the client to send to the server.
	 * Prevented if the client is in the process of disconnecting.
	 */
	private void sendInputMessage(String msg) {
		if (!disconnecting && chatList.getSelectedIndex() != -1) {
			System.out.println("Sending this message:\n" + msg);
			handleNewMessage(msg, Client.username);
			this.messageInputField.setText("");
			client.createMessage(msg, currentClientUserName);
		}
	}
	
	/**
	 * React to file being chosen for sending. (nothing if cancelled).
	 * <p>
	 * Gets file path and passes it to client to handle, returns user to messages tab.
	 */
	private void fileChooserActionPerformed(java.awt.event.ActionEvent evt) {
		if (chatList.getSelectedIndex() == -1){
			JOptionPane.showMessageDialog(null, "No user selected in chat list.");
			System.out.println("Returning to messages tab");
			jTabbedPane1.setSelectedIndex(0);
			return;
		}
		
		
		System.out.println("FileChooser interaction");
		System.out.println(evt.getActionCommand());
		if (evt.getActionCommand().equals("ApproveSelection")) {
			System.out.println("Chose file: " + fileChooser.getSelectedFile().getPath());
			client.createFileMessage(fileChooser.getSelectedFile(), currentClientUserName, fileChooser.getSelectedFile().getName(), Client.username + ":" + System.currentTimeMillis());
			
			JOptionPane.showMessageDialog(null, "File sent");
		}
		
		System.out.println("Returning to messages tab");
		jTabbedPane1.setSelectedIndex(0);
	}
	
	/** Handles opening a file using the default program on the pc */
	void openFile(String fileID) {
		System.out.println("Opening file " + fileID);
		try {
			Desktop.getDesktop().open(files.get(fileID).file);
		}
		catch (IOException e) {
			System.err.println("Failed to open file " + fileID);
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles accepting a file
	 * <p>
	 * Displays a file chooser window allowing the user to choose where and under what name to save the file.
	 * Updates the available UI actions and labels.
	 */
	void acceptFile(String fileID) {
		System.out.println("Accepting file " + fileID);
		
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(new java.io.File(files.get(fileID).name));
		int returnVal = chooser.showSaveDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			files.get(fileID).file = new java.io.File(chooser.getSelectedFile().getPath());
			FileUIRow p = fileUIRowHashMap.get(fileID);
			p.openBtn.setEnabled(true);
			p.acceptBtn.setEnabled(false);
			p.declineBtn.setEnabled(false);
			p.label.setText(chooser.getSelectedFile().getName());
		} else {
			return;
		}
		
		
		try {
			client.createFileResponse(fileID, "ACCEPT", null);
		}
		catch (IndexOutOfBoundsException ex) {
			System.err.println("Attempting accept file in index: " + fileID + ", file not found\n\n" + ex);
		}
	}
	
	/** Handles declining a file and removing it from the UI */
	void declineFile(String fileID) {
		System.out.println("Declining file " + fileID);
		
		receivedFilesPanelTab.remove(fileUIRowHashMap.get(fileID).panel);
		receivedFilesPanelTab.repaint();
		
		try {
			client.createFileResponse(fileID, "REJECT", null);
		}
		catch (IndexOutOfBoundsException ex) {
			System.err.println("Attempting decline file in index: " + fileID + ", file not found\n\n" + ex);
		}
	}
	
	
	/**
	 * Handles new messages (both sent and received)
	 * <p>
	 * Handles adding a new chat to the chatlist and UI if the sender is not currently in the chatList.
	 * <p>
	 * Ensures access to the stored chats in memory are synchronized to prevent lost updates.
	 *
	 * @param msg    message to be sent/received
	 * @param sender username of message sender
	 */
	public void handleNewMessage(String msg, String sender) {
		boolean senderInChatList = false;
		ListModel model = chatList.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			if (model.getElementAt(i).equals(sender)) {
				senderInChatList = true;
				chatIndexOfSender = i;        // get the index
				
				break;
			}
		}
		
		if (!sender.equals(Client.username)) {
			// Check if the sender has a chat
			if (!senderInChatList) {
				handleNewChatConnected(sender);
				chatIndexOfSender = chats.size() - 1;
			}
		}
		
		String s = "\n" + "[" + sender + "]:\t" + msg;
		
		synchronized (chats) {
			if (sender.equals(Client.username)) {
				chats.set(currentUserIndex, chats.get(currentUserIndex) + s);
			} else {
				chats.set(chatIndexOfSender, chats.get(chatIndexOfSender) + s);
			}
			
			if (sender.equals(currentClientUserName) || sender.equals(Client.username))
				try {
					doc.insertString(doc.getLength(), s, null);
				}
				catch (BadLocationException ex) {
					Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
				}
		}
		
	}
	
	/** Displays a dialogue window to notify user of response to new chat requests. */
	public void handleConnectResponse(boolean success, String user) {
		JOptionPane.showMessageDialog(this, "Connection to " + user + " " + (success ? "succeeded" : "failed"));
		if (success) {
			handleNewChatConnected(user);
		}
	}
	
	/** Handles notification from the server of files ready to be received.
	 *
	 * Displays a popup for the user.
	 *
	 */
	public void handleFileRequest(String source, String fileID, String fileName) {
		System.out.println("Received a file request: source " + source + " ID " + fileID + " name " + fileName);
		File file = new File(fileID, fileName, source);
		files.put(fileID, file);
		
		JOptionPane.showMessageDialog(null, "New file from "+source, "New File Request", JOptionPane.INFORMATION_MESSAGE);
		
		addFileButton(file);
	}
	
	/** Handles the saving of received files */
	public void handleFileReceived(String fileID, byte[] fileData) {
		System.out.println("Received a file: ID: " + fileID + " bytes: " + fileData.length);
		
		
		try (FileOutputStream fileOutputStream = new FileOutputStream(files.get(fileID).file)) {
			fileOutputStream.write(fileData);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
