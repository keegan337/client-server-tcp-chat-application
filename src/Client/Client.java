package Client;

import Common.Deliverable;
import Common.Header;
import Common.ProtocolUtils;
import GUI.MainMenu;

import javax.swing.*;
import java.io.*;
import java.io.File;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static Common.ProtocolUtils.buildHeaderString;
import static Common.ProtocolUtils.parseHeader;

public class Client {
	private static  String MACHINE_NAME = "localhost";
	private static  int SERVER_PORT = 9000;
	private final BlockingDeque<Deliverable> sendQueue = new LinkedBlockingDeque();
	private static Socket clientSocket = null;
	private static DataInputStream input;
	private static DataOutputStream output;
	public static String username;
	private MainMenu mainMenu;
	
	public Client(){}
	public Client(String hostName, String port){
		MACHINE_NAME = hostName;
		SERVER_PORT = Integer.parseInt(port);
	}
	
	/**
	 * Connects the client to the server and initialises the listening and sending threads
	 */
	public void start() {
		System.out.println("Starting client connecting to "+MACHINE_NAME +" | "+SERVER_PORT);
		setupConnection();
	}

	/**
	 * Authenticates a user's credentials
	 *
	 * @param username the clients username
	 * @param password the clients password
	 * @return true if the details are valid and false if they are not.
	 */
	public boolean login(String username, String password) {
		System.out.println("Attempting login: " + username + "|" + password);
		
		Header header = new Header(Header.COMMAND_AUTHENTICATE, null, username, -1);
		String headerString = buildHeaderString(header);
		try {
			output.writeUTF(headerString); // Header
			output.writeUTF(username);
			output.writeUTF(password);
			
			Header rspHeader = ProtocolUtils.parseHeader(input.readUTF());
			if (!rspHeader.command.equals(Header.COMMAND_AUTHENTICATE_RESPONSE)) System.out.println(header);
			else {
				String rspBody = input.readUTF();
				System.out.println(rspBody);
				return rspBody.equals(ProtocolUtils.KEYWORD_SUCCESS);
			}
		}
		catch(SocketException ex){
			JOptionPane.showMessageDialog(mainMenu, "Server unexpectedly disconnected", "Server Connection Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Connects the current user to the server.
	 */
	private static void setupConnection() {
		try {
			clientSocket = new Socket(MACHINE_NAME, SERVER_PORT);
		} catch (IOException e) {
			System.err.println("Error creating client socket.");
			e.printStackTrace();
		}
		
		try {
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Error creating data stream connection");
			e.printStackTrace();
		}
	}
	
	/**
	 * Depopulates the queue of pending messages and gracefully disconnect from the server.
	 */
	public void disconnectConnection() {
		Header header = new Header(Header.COMMAND_DISCONNECT, null, username, -1);
		String headerString = buildHeaderString(header);
		try {
			sendQueue.put(new Deliverable(new String[]{headerString}));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates two threads, one to deal with messages being sent to the server, by the client,
	 * and one to deal with messages being sent to the client, by the server. Must be called after a successful
	 * login has occurred.
	 */
	public void createThreads() {
		Thread readMsgThread = new Thread(() ->
		{
			while (true) {
				try {
					String header = input.readUTF();
					System.out.println("Received header: ");
					String command = decodeMessageCommand(header);
					String senderUserName = decodeMessageSenderUsername(header);
					Header parsedHeader = parseHeader(header);
					System.out.println(parsedHeader);
					// The following switch statement makes sure read is called a command specific time
					// A deliverable is then made and added to the queue
					// The GUI is notified whenever a message is received so it can be updated
					switch (command) {
						case Header.COMMAND_CONNECT_CHAT_RESPONSE: {
							String connectRsp = input.readUTF(); // success or fail
							SwingUtilities.invokeLater(() ->
									mainMenu.handleConnectResponse(connectRsp.equals(ProtocolUtils.KEYWORD_SUCCESS), parsedHeader.source)
							);
							break;
						}
						case Header.COMMAND_MESSAGE: {
							String msgFromOtherClient = input.readUTF(); // The actual message the user will see
							SwingUtilities.invokeLater(() -> mainMenu.handleNewMessage(msgFromOtherClient, senderUserName));
							break;
						}
						case Header.COMMAND_FILE: {
							String fileID = input.readUTF();
							byte[] fileData = new byte[parsedHeader.length];
							input.readFully(fileData);
							SwingUtilities.invokeLater(() -> mainMenu.handleFileReceived(fileID, fileData));
							break;
						}
						case Header.COMMAND_FILE_RESPONSE: {
							String fileRsp = input.readUTF(); // success or fail
							String fileRspID = input.readUTF(); // the file ID
							SwingUtilities.invokeLater(() -> {
								System.out.println(fileRspID + " response: " + fileRsp);
								//LOL do nothing
							});
							break;
						}
						case Header.COMMAND_FILE_REQUEST: {
							String fileIDRqst = input.readUTF();
							String filenameRqst = input.readUTF();
							SwingUtilities.invokeLater(() -> mainMenu.handleFileRequest(parsedHeader.source, fileIDRqst, filenameRqst));
							break;
						}
					}
				}catch (EOFException eofEx){
					System.err.println("Server unexpectedly disconnected connect");
					JOptionPane.showMessageDialog(mainMenu, "Server unexpectedly disconnected", "Server Connection Error", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
					break;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread sendMsgThread = new Thread(() ->
		{
			while (true) {
				try {
					Deliverable deliverable = sendQueue.take(); // take a message out of the queue if there is one available
					for (String s : deliverable.strings)
						output.writeUTF(s);
					
					output.write(deliverable.file);
					
					//Dodgy disconnect thing. Look at this later
					if (parseHeader(deliverable.strings[0]).command.equals(Header.COMMAND_DISCONNECT)){
						clientSocket.close();
						System.exit(0);}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		readMsgThread.start();
		sendMsgThread.start();
	}
	
	/**
	 * Creates a message and add it to a queue of messages that need to be sent to the server.
	 *
	 * @param body        the message that will be sent to the other client
	 * @param Destination the client that the message is intended for
	 */
	public void createMessage(String body, String Destination) {
		Header header = new Header(Header.COMMAND_MESSAGE, Destination, username, -1);
		String[] message = {buildHeaderString(header), body};
		Deliverable d = new Deliverable(message);
		try {
			sendQueue.put(d);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a connection request message and adds it to a queue of messages that need to be sent to the server.
	 *
	 * @param Destination the client that the message is intended for
	 */
	public void createConnectionRequest(String Destination) {
		Header header = new Header(Header.COMMAND_CONNECT_CHAT, Destination, username, -1);
		String[] message = {buildHeaderString(header)};
		Deliverable d = new Deliverable(message);
		try {
			sendQueue.put(d);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a response message indicating whether the client wishes to accept or decline
	 * a pending file
	 *
	 * @param fileID      the file's ID
	 * @param rsp         either accept or reject
	 * @param Destination the original sender of the file
	 */
	public void createFileResponse(String fileID, String rsp, String Destination) {
		Header header = new Header(Header.COMMAND_FILE_RESPONSE, Destination, username, -1);
		String[] message = {buildHeaderString(header), rsp, fileID};
		Deliverable d = new Deliverable(message);
		try {
			sendQueue.put(d);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a message that sends a file to the server
	 *
	 * @param file        the file being sent
	 * @param destination the user that the file is being sent to
	 * @param filename    the file's name
	 * @param fileID      a file ID (combination of timestamp and username) to distinguish different files
	 */
	public void createFileMessage(File file, String destination, String filename, String fileID) {
		try {
			byte[] bytes = Files.readAllBytes(file.toPath());
			Header h = new Header(Header.COMMAND_FILE, destination, username, bytes.length);
			String[] message = {buildHeaderString(h), fileID, filename};
			Deliverable d = new Deliverable(message, bytes);
			sendQueue.put(d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes in a header sent from the server, as a string, and returns the command type
	 *
	 * @param header the string version of the header sent from the server
	 * @return the message's command type
	 */
	private String decodeMessageCommand(String header) {
		String command = null;
		try {
			Header h = parseHeader(header);
			switch (h.command) {
				case Header.COMMAND_CONNECT_CHAT_RESPONSE:
					command = Header.COMMAND_CONNECT_CHAT_RESPONSE;
					break;
				case Header.COMMAND_MESSAGE:
					command = Header.COMMAND_MESSAGE;
					break;
				case Header.COMMAND_FILE:
					command = Header.COMMAND_FILE;
					break;
				case Header.COMMAND_FILE_RESPONSE:
					command = Header.COMMAND_FILE_RESPONSE;
					break;
				case Header.COMMAND_FILE_REQUEST:
					command = Header.COMMAND_FILE_REQUEST;
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return command;
	}
	
	
	/**
	 * Takes in a header sent from the server, as a string, and returns the username where this message
	 * originated
	 *
	 * @param header the string version of the header sent from the server
	 * @return the username where the message originated
	 */
	private String decodeMessageSenderUsername(String header) {
		String senderUsername = null;
		try {
			Header h = parseHeader(header);
			senderUsername = h.source;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return senderUsername;
	}
	
	/**
	 * Links the client to a GUI
	 *
	 * @param mainMenu the chat menu the client is associated with
	 */
	public void setMainViewReference(MainMenu mainMenu) {
		this.mainMenu = mainMenu;
	}
}
