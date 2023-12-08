package gu.dtalk.activemq;

import static gu.dtalk.activemq.ActivemqContext.HELPER;

import gu.simplemq.MQProperties;

/**
 * 自定义配置默认实现
 * @author guyadong
 *
 */
public class DefaultCustomActivemqConfigProvider extends BaseActivemqConfigProvider {
	public static final MQProperties INIT_PROPERTIES;

	static{
		INIT_PROPERTIES = HELPER.makeMQProperties(null);
		INIT_PROPERTIES.setProperty(MQ_URI, DEFAULT_AMQP_CONNECTOR);
		initMqttURI(INIT_PROPERTIES,DEFAULT_MQTT_PORT);
	}
	public DefaultCustomActivemqConfigProvider() {
	}
	@Override
	protected MQProperties selfProp() {
		return INIT_PROPERTIES;
	}
	@Override
	public final ActivemqConfigType type() {
		return ActivemqConfigType.CUSTOM;
	}

}
