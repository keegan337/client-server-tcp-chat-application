package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class Server {

	static Map<String, User> users;
	static ConcurrentMap<String, File> files;
	private static int SERVER_PORT = 9000;

	public static void main(String[] args) throws IOException {
		if(args.length>0){
			SERVER_PORT = Integer.parseInt(args[0]);
		}
		
		users = new ConcurrentHashMap<String, User>(); //Map containing username -> user the users registered on the server
		files = new ConcurrentHashMap<String, File>(); //Map containing fileID -> file for the files sent between users
		
		//Read the list of registered users
		BufferedReader br = new BufferedReader(new InputStreamReader(Server.class.getResourceAsStream("users.txt")));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			User user = new User(parts[0], parts[1]);
			users.put(user.getUsername(), user);
		}
		br.close();
		
		System.out.println("Starting server listening on port: " + SERVER_PORT);
		ServerSocket listenSocket = null;

		try {
			listenSocket = new ServerSocket(SERVER_PORT);
		}
		catch (IOException e) {
			System.err.println("Error whilst opening listening socket.");
			e.printStackTrace();
		}
		
		/*
		 * Main server loop. Listens for connections from clients.
		 * When a client connects, a new thread is spawned to handle the login process for that client.
		 */
		//noinspection InfiniteLoopStatement
		while (true) {
			try {
				assert listenSocket != null;
				Socket clientSocket = listenSocket.accept();
				
				ClientConnectThread clientConnectThread = new ClientConnectThread(clientSocket);
				clientConnectThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
