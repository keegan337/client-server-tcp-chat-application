package Client;

/**
 * Wrapper class for files in the client
 * <p>
 * Encapsulates file metadata separate from the filepath.
 */
public class File {
	public final String ID;
	public final String name;
	private final String source;
	public java.io.File file;
	
	public File(String ID, String name, String source) {
		this.ID = ID;
		this.name = name;
		this.source = source;
	}
}
