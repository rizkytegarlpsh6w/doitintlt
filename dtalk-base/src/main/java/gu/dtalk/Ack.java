package gu.dtalk;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * 设备命令响应对象<br>
 * 此类对象将做为设备命令响应经redis服务器发送到命令发送端,
 * 数据发到redis服务器以及设备端从redis服务器收到数据的过程要经过JSON序列化和反序列化<br>
 * @author guyadong
 *
 * @param <T> 设备命令执行返回结果类型
 */
public class Ack<T> {
	private static boolean traceEnable = false;
	private Integer cmdSn;
	private int deviceId;
	private String deviceMac;
	private String item;
	private T value;
	private String valueType;
	private Status status;
	private String statusMessage;
	private String exception;
	private String trace;
	/** 设备命令执行状态 */
	public enum Status{
		/** 设备命令成功执行完成 */
		OK,
		/** 设备端不支持的操作 */
		UNSUPPORTED,
		/** 调用出错 */
		ERROR,
		/** 
		 * 设备命令响应超时
		 */ 
		TIMEOUT,
		/** 设备命令被拒绝执行 */
		REJECTED,
		/** 设备命令开始执行 */
		ACCEPTED,
		/** 返回设备命令完成进度 */
		PROGRESS,
		/** 执行中的设备命令被取消 */
		CANCELED
	}
	public Ack() {
	}
	/**
	 * @return 返回状态信息
	 */
	public String message(){
		Preconditions.checkArgument(null != status,"status field is null");
		StringBuffer buffer = new StringBuffer(String.format("device%d@%d:%s", deviceId,cmdSn,status.name()));
		switch(status){
		case ERROR:
		case REJECTED:
			if(!Strings.isNullOrEmpty(statusMessage)){
				buffer.append(":").append(statusMessage);
			}
			break;
		case PROGRESS:
			/** 此状态下value字段如果为数字类型则被解释为完成进度(0-100) */
			if(value instanceof Number){
				buffer.append(":finished %").append(((Number)value).intValue());
			}
			if(!Strings.isNullOrEmpty(statusMessage)){
				buffer.append(":").append(statusMessage);
			}
			break;
		case TIMEOUT:
		case UNSUPPORTED:
		case OK:
		case ACCEPTED:
		case CANCELED:
		default:
			break;
		}
		return buffer.toString();
	}
	/** 
	 * @return 返回设备命令序列号 
	 */
	public Integer getCmdSn() {
		return cmdSn;
	}
	public Ack<T> setCmdSn(Integer cmdSn) {
		this.cmdSn = cmdSn;
		return this;
	}
	/**
	 * @return  返回执行设备命令的设备ID 
	 */
	public int getDeviceId() {
		return deviceId;
	}

	/**
	 * 设置设备ID
	 * @param deviceId 设备ID
	 * @return 当前{@link Ack}实例
	 */
	public Ack<T> setDeviceId(int deviceId) {
		this.deviceId = deviceId;
		return this;
	}
	/**
	 * @return 返回设备MAC地址16进制(HEX)字符串
	 */
	public String getDeviceMac() {
		return deviceMac;
	}
	/**
	 * @param deviceMac 要设置的 deviceMac
	 * @return 当前对象
	 */
	public Ack<T> setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
		return this;
	}
	/**
	 * @return 返回响应的条目(item)路径
	 */
	public String getItem() {
		return item;
	}
	/**
	 * 设备响应的条目(item)路径
	 * @param item 要设置的 item
	 * @return 当前对象
	 */
	public Ack<T> setItem(String item) {
		this.item = item;
		return this;
	}
	/** 
	 * @return 返回设备命令执行结果对象 
	 */
	public T getValue() {
		return value;
	}
	/**
	 * 设置设备命令执行结果对象
	 * @param value 要设置的value
	 * @return 当前对象
	 */
	public Ack<T> setValue(T value) {
		this.value = value;
		return setValueType(value != null ? value.getClass().getSimpleName() : "NULL");
	}
	
	public String getValueType(){
		return valueType;
	}
	public Ack<T> setValueType(String valueClass){
		this.valueType = valueClass;
		return this;
	}
	/** 
	 * @return 返回设备命令执行状态 
	 */
	public Status getStatus() {
		return status;
	}
	/**
	 * 设置响应状态
	 * @param status 状态
	 * @return 当前{@link Ack}实例
	 */
	public Ack<T> setStatus(Status status) {
		this.status = status;
		return this;
	}
	/** 
	 * @return 返回错误信息
	 */
	public String getStatusMessage() {
		return statusMessage;
	}
	/**
	 * 设置错误信息
	 * @param errorMessage  错误信息
	 * @return 当前{@link Ack}实例
	 */
	public Ack<T> setStatusMessage(String errorMessage) {
		this.statusMessage = errorMessage;
		return this;
	}

	public String getException() {
		return exception;
	}
	public Ack<T> setException(String exception) {
		this.exception = exception;
		return this;
	}
	
	/**
	 * @return trace
	 */
	public String getTrace() {
		return trace;
	}
	/**
	 * @param trace 要设置的 trace
	 * @return 
	 */
	public Ack<T> setTrace(String trace) {
		this.trace = trace;
		return this;
	}

	public Ack<T> writeError(Throwable e){
		
		if(traceEnable){
			StringWriter write = new StringWriter(256);
			PrintWriter pw = new PrintWriter(write);
			e.printStackTrace(pw);
			setTrace(write.toString());
		}
		return setStatus(Status.ERROR)
				.setStatusMessage(e.getMessage())
				.setException(e.getClass().getName());
	}
	/**
	 * @return traceEnable
	 */
	public static boolean isTraceEnable() {
		return traceEnable;
	}
	/**
	 * 设置当调用 {@link #writeError(Throwable)}方法时是否用异常的堆栈信息填充trace字段
	 * @param traceEnable 要设置的 traceEnable
	 */
	public static void setTraceEnable(boolean traceEnable) {
		Ack.traceEnable = traceEnable;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Ack [");
		if (cmdSn != null) {
			builder.append("cmdSn=");
			builder.append(cmdSn);
			builder.append(", ");
		}
		builder.append("deviceId=");
		builder.append(deviceId);
		builder.append(", ");
		if (deviceMac != null) {
			builder.append("deviceMac=");
			builder.append(deviceMac);
			builder.append(", ");
		}
		if (item != null) {
			builder.append("item=");
			builder.append(item);
			builder.append(", ");
		}
		if (value != null) {
			builder.append("value=");
			builder.append(value);
			builder.append(", ");
		}
		if (status != null) {
			builder.append("status=");
			builder.append(status);
			builder.append(", ");
		}
		if (statusMessage != null) {
			builder.append("statusMessage=");
			builder.append(statusMessage);
			builder.append(", ");
		}
		if (exception != null) {
			builder.append("exception=");
			builder.append(exception);
		}
		builder.append("]");
		return builder.toString();
	}
}
