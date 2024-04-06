package giis.modevo.transformations;

/**
 * Exception class that is used for all the exceptions that are related to the ATL transformation process. It covers both the set-up
 * for the transformations as well as the exceptions detected during these transformations.
 */
public class ATLException extends RuntimeException {

	private static final long serialVersionUID = -7455476535919372705L;

	public ATLException() {
		super();
	}
	public ATLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	public ATLException(String message, Throwable cause) {
		super(message, cause);
	}
	public ATLException(String message) {
		super(message);
	}
	public ATLException(Throwable cause) {
		super(cause);
	}
	
}
