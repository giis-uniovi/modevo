package giis.modevo.transformations;

/**
 * Exception class for the exceptions that are caught when a document is read. 
 * It it used both for errors that are issued during the read of the file as well as 
 * errors that are detected when opening or creating a file.
 */
public class DocumentReadException extends RuntimeException {

	private static final long serialVersionUID = 7197333030363451352L;

	public DocumentReadException() {
		super();
	}
	public DocumentReadException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	public DocumentReadException(String message, Throwable cause) {
		super(message, cause);
	}
	public DocumentReadException(String message) {
		super(message);
	}
	public DocumentReadException(Throwable cause) {
		super(cause);
	}
	
}
