package Server;

import Common.Deliverable;
import Common.Header;
import Common.ProtocolUtils;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Thread for receiving messages from a logged in user.
 */
public class UserReceiveThread extends Thread {
	
	private final DataInputStream inputStream;
	private final User user;
	
	/**
	 * Creates a new UserReceiveThread object receiving messages from the provided inputStream from the provided user.
	 * @param inputStream The DataInputStream connected to the user's client
	 * @param user The user associated with the inputStream
	 */
	public UserReceiveThread(DataInputStream inputStream, User user) {
		this.inputStream = inputStream;
		this.user = user;
	}
	
	
	@Override
	public void run() {
		try {
			//noinspection InfiniteLoopStatement
			while (true) {
				Header header = ProtocolUtils.parseHeader(inputStream.readUTF());
				
				switch (header.command) {
					case Header.COMMAND_CONNECT_CHAT: {
						
						Header responseHeader = new Header(Header.COMMAND_CONNECT_CHAT_RESPONSE, null, header.destination, -1);
						
						if (Server.users.containsKey(header.destination)) {
							
							//Respond that the user exists
							user.queue.put(new Deliverable(new String[]{
									ProtocolUtils.buildHeaderString(responseHeader),
									ProtocolUtils.KEYWORD_SUCCESS,
							}));
						} else {
							
							//Respond that the user doesn't exist
							user.queue.put(new Deliverable(new String[]{
									ProtocolUtils.buildHeaderString(responseHeader),
									ProtocolUtils.KEYWORD_FAILURE,
							}));
						}
						
						break;
					}
					case Header.COMMAND_MESSAGE: {
						String message = inputStream.readUTF();
						
						User destinationUser = Server.users.get(header.destination);
						String source = user.getUsername();
						
						Header responseHeader = new Header(Header.COMMAND_MESSAGE, null, source, -1);
						
						//Send the message to the destination user
						destinationUser.queue.put(new Deliverable(new String[]{
								ProtocolUtils.buildHeaderString(responseHeader),
								message,
						}));
						
						break;
					}
					case Header.COMMAND_FILE: {
						System.out.println("receiving a file");
						String fileID = inputStream.readUTF();
						String fileName = inputStream.readUTF();
						
						byte[] fileContents = new byte[header.length];
						inputStream.readFully(fileContents);
						
						System.out.println("received a file");
						
						Server.files.put(fileID, new File(user, fileContents));
						
						User destinationUser = Server.users.get(header.destination);
						String source = user.getUsername();
						
						Header responseHeader = new Header(Header.COMMAND_FILE_REQUEST, null, source, -1);
						
						//Send file request (ask whether to accept/decline) to destination user
						destinationUser.queue.put(new Deliverable(new String[]{
								ProtocolUtils.buildHeaderString(responseHeader),
								fileID,
								fileName,
						}));
						
						break;
					}
					case Header.COMMAND_FILE_RESPONSE: {
						System.out.println("got a file response");
						String responseString = inputStream.readUTF();
						boolean accepted = false;
						switch (responseString) {
							case ProtocolUtils.KEYWORD_ACCEPTED: {
								accepted = true;
								break;
							}
							case ProtocolUtils.KEYWORD_REJECTED: {
								accepted = false;
								break;
							}
							default: {
								user.queue.put(new Deliverable(new String[]{"🖕"}));
								break;
							}
						}
						
						System.out.println("file accepted: " + accepted);
						
						String fileID = inputStream.readUTF();
						System.out.println("file id: " + fileID);
						
						File file = Server.files.get(fileID);
						
						if (accepted) {
							//Send the file contents to the users that accepted the file
							byte[] fileContents = file.contents;
							Header responseHeader = new Header(Header.COMMAND_FILE, null, null, fileContents.length);
							user.queue.put(new Deliverable(
									new String[]{
											ProtocolUtils.buildHeaderString(responseHeader),
											fileID,
									},
									fileContents
							));
							System.out.println("put file in queue");
							
							//Notify the original sender of the file that it was accepted
							file.sender.queue.put(new Deliverable(new String[]{
									ProtocolUtils.buildHeaderString(Header.COMMAND_FILE_RESPONSE),
									ProtocolUtils.KEYWORD_ACCEPTED,
									fileID
							}));
						} else {
							Server.files.remove(fileID);
							
							//Notify the original sender of the file that it was accepted
							file.sender.queue.put(new Deliverable(new String[]{
									ProtocolUtils.buildHeaderString(Header.COMMAND_FILE_RESPONSE),
									ProtocolUtils.KEYWORD_REJECTED,
									fileID
							}));
						}
						break;
					}
					case Header.COMMAND_DISCONNECT: {
						user.disconnect();
						break;
					}
				}
			}
		}
		catch (IOException e) {
			System.out.println("User disconnected");
		}
		catch (InterruptedException e) {
			System.out.println("Thread interrupted");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		user.disconnect();
	}
}
