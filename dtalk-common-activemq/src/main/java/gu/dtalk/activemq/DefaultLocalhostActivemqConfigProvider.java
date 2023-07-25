package gu.dtalk.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import gu.simplemq.activemq.ActivemqProperties;

/**
 * 本机(LOCALHOST)配置默认实现(仅用于测试)
 * @author guyadong
 *
 */
public class DefaultLocalhostActivemqConfigProvider extends BaseActivemqConfigProvider {
	public static final ActivemqProperties INIT_PROPERTIES = new ActivemqProperties();

	static{
		INIT_PROPERTIES.setProperty(ACON_BROKER_URL, ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
	}
	public DefaultLocalhostActivemqConfigProvider() {
	}
	@Override
	protected ActivemqProperties selfProp() {
		return INIT_PROPERTIES;
	}
	@Override
	public final ActivemqConfigType type() {
		return ActivemqConfigType.LOCALHOST;
	}

}
