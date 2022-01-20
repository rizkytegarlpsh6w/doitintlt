package gu.dtalk.engine.demo;

import net.gdface.cli.CommonCliConstants;

/**
 * 常量定义
 * @author guyadong
 *
 */
public interface DemoConstants extends CommonCliConstants {
	public static final String REDIS_HOST_OPTION_LONG = "host";
	public static final String REDIS_HOST_OPTION_DESC = "redis host name,default: ";
	public static final String REDIS_PORT_OPTION_LONG = "port";
	public static final String REDIS_PORT_OPTION_DESC = "redis port number,default: ";
	public static final String REDIS_PWD_OPTION_LONG = "auth";
	public static final String REDIS_PWD_OPTION_DESC = "redis password,default:empty";
	public static final String REDIS_DB_OPTION_LONG = "db";
	public static final String REDIS_DB_OPTION_DESC = "redis database index,default:0";
	public static final String REDIS_URI_OPTION_LONG = "uri";
	public static final String REDIS_URI_OPTION_DESC = "uri for redis,default:null";
	public static final String REDIS_TIMEOUT_OPTION_LONG = "timout";
	public static final String REDIS_TIMEOUT_OPTION_DESC = "redis timeout,default:";
	public static final String CONNEC_PWD_OPTION_LONG = "pwd";
	public static final String CONNEC_PWD_OPTION_DESC = "password ,default: ";
	public static final String TRACE_OPTION = "X";
	public static final String TRACE_OPTION_LONG = "trace";
	public static final String TRACE_OPTION_DESC = "show stack trace on error ,default: false";
}
