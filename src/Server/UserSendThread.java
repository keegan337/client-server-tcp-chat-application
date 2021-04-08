package Server;

import Common.Deliverable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Thread for delivering messages to a logged in user.
 */
public class UserSendThread extends Thread {
	
	private final DataOutputStream outputStream;
	private final BlockingQueue<Deliverable> queue;
	
	/**
	 * Creates a new UserSendThread object for sending messages from the provided queue to the provided outputStream.
	 * @param outputStream The DataOutputStream connected to the user's client.
	 * @param queue The queue of messages to be sent to the user.
	 */
	public UserSendThread(DataOutputStream outputStream, BlockingQueue<Deliverable> queue) {
		this.outputStream = outputStream;
		this.queue = queue;
	}
	
	@Override
	public void run() {
		try {
			//noinspection InfiniteLoopStatement
			while (true) {
				Deliverable deliverable = queue.take(); //When the queue is empty, this blocks until a deliverable is added to it.
				
				for (String s : deliverable.strings)
					outputStream.writeUTF(s);
				
				outputStream.write(deliverable.file);
			}
			
		}
		catch (InterruptedException e) {
			System.out.println("Send thread interrupted");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
