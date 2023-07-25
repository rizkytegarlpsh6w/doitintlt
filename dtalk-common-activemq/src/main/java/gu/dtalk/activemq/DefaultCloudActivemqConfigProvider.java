package gu.dtalk.activemq;

import gu.simplemq.activemq.ActivemqProperties;

/**
 * 公有云配置
 * @author guyadong
 *
 */
public class DefaultCloudActivemqConfigProvider extends BaseActivemqConfigProvider{
	public static final ActivemqProperties INIT_PROPERTIES = new ActivemqProperties();

	/** 默认的公有云activemq连接 */
	public static final String DEFAULT_CLOUD_ACTIVEMQ_URI = "tcp://dtalk.facelib.net:26416";	
	static{
		INIT_PROPERTIES.setProperty(ACON_BROKER_URL, DEFAULT_CLOUD_ACTIVEMQ_URI);
	}
	@Override
	protected ActivemqProperties selfProp() {
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
