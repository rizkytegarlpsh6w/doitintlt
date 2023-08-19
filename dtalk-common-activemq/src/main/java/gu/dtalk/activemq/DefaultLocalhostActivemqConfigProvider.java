package gu.dtalk.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import gu.simplemq.activemq.PropertiesHelper;
import gu.simplemq.utils.MQProperties;

/**
 * 本机(LOCALHOST)配置默认实现(仅用于测试)
 * @author guyadong
 *
 */
public class DefaultLocalhostActivemqConfigProvider extends BaseActivemqConfigProvider {
	public static final MQProperties INIT_PROPERTIES = PropertiesHelper.AHELPER.makeMQProperties(null);

	static{
		INIT_PROPERTIES.setProperty(ACON_BROKER_URL, ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
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
