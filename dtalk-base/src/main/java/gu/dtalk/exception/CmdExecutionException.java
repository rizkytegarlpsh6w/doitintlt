package gu.dtalk.exception;

/**
 * 发送给命令响应接收端的异常,
 * 如果希望命令响应端收到设备命令执行的异常信息,
 * 就将异常信息封装到此类中抛出.
 * @author guyadong
 *
 */
public class CmdExecutionException extends DtalkException {

	private static final long serialVersionUID = 1L;

	public CmdExecutionException(Throwable cause) {
		super(cause);
	}

	public CmdExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CmdExecutionException(String message) {
		super(message);
	}

}
