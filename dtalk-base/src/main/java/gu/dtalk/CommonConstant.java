package gu.dtalk;

import redis.clients.jedis.Protocol;

/**
 * dtalk共用常量
 * @author guyadong
 *
 */
public class CommonConstant {
	public static final String ACK_SUFFIX="_dtalk_ack";
	public static final String CONNECT_SUFFIX="_dtalk_connect";
	/**
	 * 默认空闲时间限制(毫秒),超过此时间，自动中断连接
	 */
	public static final long DEFAULT_IDLE_TIME_MILLS =  5*60*1000;
	public static final String QUIT_NAME="quit";
	public static final String BACK_NAME="back";


	public static final String REDIS_HOST=Protocol.DEFAULT_HOST;
	public static final int REDIS_PORT = Protocol.DEFAULT_PORT;
	public static final String REDIS_PASSWORD = "";
	
    /** 用于日期时间戳格式转换格式 */
    public static final String TIMESTAMP_FORMATTER_STR = "yyyy-MM-dd HH:mm:ss";
    /** 用于时间戳格式转换格式 */
    public static final String TIME_FORMATTER_STR = "HH:mm:ss";
    /** 用于日期戳格式转换格式 */
    public static final String DATE_FORMATTER_STR = "yyyy-MM-dd";
	/**
	 * MAC地址匹配表达表达式，'00:00:7f:2a:39:4A','00e8992730FF'都允许
	 */
	public static final String MAC_REG = "([\\da-fA-F]{2}:?){6}";
	
	public static final String ACK_FIELD_STATUS="status";
	public static final String ITEM_FIELD_CATALOG="catalog";
	public static final String ITEM_FIELD_PATH="path";
	public static final String ITEM_FIELD_NAME="name";
	public static final String OPTION_FIELD_TYPE="type";
	public static final String OPTION_FIELD_VALUE="value";
	public static final String OPTION_FIELD_DEFAULT="defaultValue";

}
