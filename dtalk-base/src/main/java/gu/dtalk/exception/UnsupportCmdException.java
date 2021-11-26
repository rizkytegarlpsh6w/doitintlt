package gu.dtalk.exception;

/**
 * 当前命令设备端未实现时抛出此异常
 * @author guyadong
 *
 */
public class UnsupportCmdException extends DtalkException {

	private static final long serialVersionUID = 1L;

	public UnsupportCmdException() {
	}

	public UnsupportCmdException(String message) {
		super(message);
	}

	public UnsupportCmdException(Throwable cause) {
		super(cause);
	}

	public UnsupportCmdException(String message, Throwable cause) {
		super(message, cause);
	}

}
