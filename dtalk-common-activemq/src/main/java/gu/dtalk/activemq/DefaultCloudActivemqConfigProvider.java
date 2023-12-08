package gu.dtalk.activemq;

import gu.simplemq.Constant;
import gu.simplemq.MQProperties;
import gu.simplemq.MessageQueueType;

import static gu.dtalk.activemq.ActivemqContext.HELPER;
import static gu.dtalk.activemq.ActivemqContext.CONTEXT;;

/**
 * 公有云配置
 * @author guyadong
 *
 */
public class DefaultCloudActivemqConfigProvider extends BaseActivemqConfigProvider implements Constant{
	/** 云端的 ACTIVEMQ 连接用户名和密码 */
	private static final String userinfo = "faceloguser:86a1b907d54bf7010394bf316e183e67@";
	/** 默认的公有云activemq连接(openwire) */
	public static final String DEFAULT_CLOUD_ACTIVEMQ_URI = "tcp://" + userinfo + "dtalk.facelib.net:26417";	
	/** 默认的公有云activemq连接(stomp) */
	public static final String DEFAULT_CLOUD_STOMP_URI = "stomp://" + userinfo + "dtalk.facelib.net:26417";
	/** 默认的公有云activemq连接(stomp) */
	public static final String DEFAULT_CLOUD_AMQP_URI = "amqp://" + userinfo + "dtalk.facelib.net:26417";
	public static final MQProperties INIT_PROPERTIES;
	static{
		INIT_PROPERTIES = HELPER.makeMQProperties(null);
		if(MessageQueueType.ACTIVEMQ.name().equals(CONTEXT.getClientImplType())){
			INIT_PROPERTIES.setProperty(MQ_URI, DEFAULT_CLOUD_ACTIVEMQ_URI);			
		}if(DEFAULT_CLOUD_AMQP_URI.startsWith(CONTEXT.getClientImplType().toLowerCase())){
			INIT_PROPERTIES.setProperty(MQ_URI, DEFAULT_CLOUD_AMQP_URI);
		}else{
			INIT_PROPERTIES.setProperty(MQ_URI, DEFAULT_CLOUD_STOMP_URI);
		}
		initMqttURI(INIT_PROPERTIES,26418);
	}
	@Override
	protected MQProperties selfProp() {
		return INIT_PROPERTIES;
	}
	public DefaultCloudActivemqConfigProvider() {
		super();
	}

	@Override
	public final ActivemqConfigType type(){
		return ActivemqConfigType.CLOUD;
	}

}
