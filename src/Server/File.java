package Server;

class File {
	public final User sender;
	public final byte[] contents;
	
	public File(User sender, byte[] contents) {
		this.sender = sender;
		this.contents = contents;
	}
}
