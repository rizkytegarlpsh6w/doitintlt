package gu.dtalk;

public class CommonConstant {
	public static final String ACK_SUFFIX="_ack";
	public static final String CONNECT_SUFFIX="_connect";
	/**
	 * 默认空闲时间限制(毫秒),超过此时间，自动中断连接
	 */
	public static final long DEFAULT_IDLE_TIME_MILLS =  60*1000;
}
