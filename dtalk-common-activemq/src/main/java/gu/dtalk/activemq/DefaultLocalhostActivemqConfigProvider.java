package gu.dtalk.activemq;

import gu.simplemq.Constant;
import gu.simplemq.MQProperties;

import static gu.dtalk.activemq.ActivemqContext.HELPER;
import static gu.dtalk.activemq.ActivemqContext.CONST_PROVIDER;

/**
 * 本机(LOCALHOST)配置默认实现(仅用于测试)
 * @author guyadong
 *
 */
public class DefaultLocalhostActivemqConfigProvider extends BaseActivemqConfigProvider implements Constant {
	public static final MQProperties INIT_PROPERTIES;

	static{
		INIT_PROPERTIES = HELPER.makeMQProperties(null);
		INIT_PROPERTIES.setProperty(MQ_URI, CONST_PROVIDER.getDefaultMQLocation());
		initMqttURI(INIT_PROPERTIES,DEFAULT_MQTT_PORT);
	}
	public DefaultLocalhostActivemqConfigProvider() {
	}
	@Override
	protected MQProperties selfProp() {
		return INIT_PROPERTIES;
	}
	@Override
	public final ActivemqConfigType type() {
		return ActivemqConfigType.LOCALHOST;
	}

}
