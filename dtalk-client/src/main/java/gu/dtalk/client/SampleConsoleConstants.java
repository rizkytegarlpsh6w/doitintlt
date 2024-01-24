package gu.dtalk.client;

import net.gdface.cli.CommonCliConstants;

/**
 * 常量定义
 * @author guyadong
 *
 */
public interface SampleConsoleConstants extends CommonCliConstants {
	public static final String IMPL_TYPE = "IMPL_TYPE";

	public static final String REDIS_HOST_OPTION_LONG = "host";
	public static final String REDIS_HOST_OPTION_DESC = "redis host name,default: ";
	public static final String REDIS_PORT_OPTION_LONG = "port";
	public static final String REDIS_PORT_OPTION_DESC = "redis port number,default: ";
	public static final String REDIS_PWD_OPTION = "a";
	public static final String REDIS_PWD_OPTION_LONG = "auth";
	public static final String REDIS_PWD_OPTION_DESC = "redis password,default:empty";
	public static final String REDIS_DB_OPTION_LONG = "db";
	public static final String REDIS_DB_OPTION_DESC = "redis database index,default:0";
	public static final String REDIS_URI_OPTION_LONG = "uri";
	public static final String REDIS_URI_OPTION_DESC = "uri for redis,default:null";
	public static final String REDIS_TIMEOUT_OPTION_LONG = "timeout";
	public static final String REDIS_TIMEOUT_OPTION_DESC = "redis timeout,default:";
	
	public static final String AMQ_HOST_OPTION_LONG = "host";
	public static final String AMQ_HOST_OPTION_DESC = "activemq host name,default: ";
	public static final String AMQ_PORT_OPTION_LONG = "port";
	public static final String AMQ_PORT_OPTION_DESC = "activemq port number,default: ";
	public static final String AMQ_USERNAME_OPTION = "u";
	public static final String AMQ_USERNAME_OPTION_LONG = "user";
	public static final String AMQ_USERNAME_OPTION_DESC = "activemq username,default:empty";
	public static final String AMQ_PWD_OPTION = "a";
	public static final String AMQ_PWD_OPTION_LONG = "auth";
	public static final String AMQ_PWD_OPTION_DESC = "activemq password,default:empty";
	public static final String AMQ_URI_OPTION_LONG = "uri";
	public static final String AMQ_URI_OPTION_DESC = "uri for redis,default:null";
	public static final String AMQ_TIMEOUT_OPTION_LONG = "timout";
	public static final String AMQ_TIMEOUT_OPTION_DESC = "redis timeout,default:";
	
	public static final String MQTT_OPTION_LONG = "mqtt";
	public static final String MQTT_OPTION_DESC = "use MQTT protocol for publish/subscribe service, "
			+ "and the optional argument define URI or HOST:PORT or HOST or PORT format for activemq mqtt service location,"
			+ "such as '--mqtt mqtt://localhost:1883' OR '--mqtt  127.0.0.1:1883' OR ' --mqtt 127.0.0.1' OR ' --mqtt 1883' ,"
			+ "if no argument follow '--mqtt' ,  use default: same with ${uri} option or ${host}:${port}";
	
	public static final String DEVICE_MAC_OPTION_LONG = "mac";
	public static final String DEVICE_MAC_OPTION_DESC = "MAC addres(hex)for target device,such as d0:17:c2:d0:3f:bf,default: self mac address";
	
}
