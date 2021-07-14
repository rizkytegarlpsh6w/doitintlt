package gu.dtalk;

import com.google.common.collect.ImmutableSet;

public class CommonConstant {
	public static final String ACK_SUFFIX="_ack";
	public static final String CONNECT_SUFFIX="_connect";
	/**
	 * 默认空闲时间限制(毫秒),超过此时间，自动中断连接
	 */
	public static final long DEFAULT_IDLE_TIME_MILLS =  60*1000;
	public static final String QUIT_NAME="quit";
	public static final String BACK_NAME="back";

	/**
	 * 保留关键字
	 */
	public static final ImmutableSet<String> RESERV_ENAMES =
			ImmutableSet.of(QUIT_NAME,BACK_NAME,"exit");
}
