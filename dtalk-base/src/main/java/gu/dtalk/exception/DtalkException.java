package gu.dtalk.exception;

public class DtalkException extends Exception {

	private static final long serialVersionUID = 1L;

	public DtalkException() {
	}

	public DtalkException(String message) {
		super(message);
	}

	public DtalkException(Throwable cause) {
		super(cause);
	}

	public DtalkException(String message, Throwable cause) {
		super(message, cause);
	}

}
