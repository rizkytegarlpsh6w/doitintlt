package gu.dtalk;

/**
 * 数据转换异常
 * @author guyadong
 *
 */
public class TransformException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransformException() {
	}

	public TransformException(String message) {
		super(message);
	}

	public TransformException(Throwable cause) {
		super(cause);
	}

	public TransformException(String message, Throwable cause) {
		super(message, cause);
	}


}
