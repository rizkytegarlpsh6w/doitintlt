package gu.dtalk.exception;

import gu.dtalk.Ack.Status;
import static com.google.common.base.Preconditions.*;

import com.google.common.base.Strings;
/**
 * 交互命令启动异常
 * @author guyadong
 *
 */
public class InteractiveCmdStartException extends CmdExecutionException {

	private static final long serialVersionUID = 1L;

	private final Status status;
	private final String statusMessage;
	/**
	 * 构造方法
	 * @param status 命令执行状态
	 * @see #InteractiveCmdStartException(Status, String, Throwable)
	 */
	public InteractiveCmdStartException(Status status) {
		this(status, null);
	}
	/**
	 * 构造方法
	 * @param status 命令执行状态
	 * @param statusMessage 附加的状态信息,可为{@code null}
	 * @see #InteractiveCmdStartException(Status, String, Throwable)
	 */
	public InteractiveCmdStartException(Status status, String statusMessage) {
		this(status,statusMessage,null);
	}
	/**
	 * 构造方法
	 * @param status 命令执行状态, 可能的状态：
	 * <ul>
	 * <li>{@link Status#UNSUPPORTED}</li>
	 * <li>{@link Status#REJECTED}</li>
	 * <li>{@link Status#ERROR} 其他错误</li>
	 * </ul>
	 * @param statusMessage 附加的状态信息,可为{@code null}
	 * @param cause 异常对象,可为{@code null}
	 */
	public InteractiveCmdStartException(Status status, String statusMessage, Throwable cause) {
		super(cause);
		checkArgument(status == Status.REJECTED || status == Status.UNSUPPORTED || status == Status.ERROR,
				"INVALID status, available value:%s,%s,%s",Status.REJECTED.name(),Status.UNSUPPORTED.name(),Status.ERROR.name());
		this.status = status;		
		this.statusMessage = statusMessage;
	}

	@Override
	public String getMessage() {
		StringBuffer buffer = new StringBuffer();
		if(!Strings.isNullOrEmpty(statusMessage)){
			buffer.append(statusMessage);
		}
		String expMsg = super.getMessage();
		if(expMsg!=null){
			buffer.append(":").append(expMsg);
		}
		/** 如果前面两项都为空则输出状态名 */
		if(buffer.length() == 0){
			buffer.append(status.name());
		}
		return buffer.toString();
	}
	/**
	 * 可能的状态：
	 * <ul>
	 * <li>{@link Status#UNSUPPORTED}</li>
	 * <li>{@link Status#REJECTED}</li>
	 * <li>{@link Status#ERROR}</li>
	 * </ul>
	 * @return 命令执行状态
	 */
	public Status getStatus() {
		return status;
	}
	/**
	 * @return 附加的状态信息
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

}
