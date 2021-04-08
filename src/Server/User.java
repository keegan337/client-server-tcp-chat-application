package Server;

import Common.Deliverable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The User class is used to represent users registered on the server.
 * It handles starting and stopping the send a receive threads when the user logs in and disconnects.
 */
public class User {
	public final BlockingQueue<Deliverable> queue;
	private final String username;
	
	private Socket socket;
	private final String password;
	private UserSendThread sendThread;
	private UserReceiveThread receiveThread;
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.queue = new LinkedBlockingQueue<>();
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	/**
	 * Starts the sending and receiving threads for the user with the given socket.
	 * Disconnects previously connected clients (logged in to this user) if necessary.
	 * @param socket socket connecting to the client that the user logged in from.
	 * @throws IOException thrown if socket input or output streams do not exist.
	 */
	public void connect(Socket socket) throws IOException {
		if (this.socket != null) disconnect();
		
		this.socket = socket;
		sendThread = new UserSendThread(new DataOutputStream(socket.getOutputStream()), queue);
		receiveThread = new UserReceiveThread(new DataInputStream(socket.getInputStream()), this);
		
		sendThread.start();
		receiveThread.start();
	}
	
	/**
	 * Stops the send and received threads for the user and closes the connection to the client.
	 */
	public void disconnect() {
		sendThread.interrupt();
		receiveThread.interrupt();
		try {
			socket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "User{" +
				"username='" + username + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
