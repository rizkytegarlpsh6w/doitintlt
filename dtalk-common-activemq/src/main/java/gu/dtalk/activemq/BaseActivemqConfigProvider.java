package gu.dtalk.activemq;

import java.net.URI;
import java.util.Properties;

import gu.simplemq.Constant;
import gu.simplemq.MQProperties;
import gu.simplemq.utils.URISupport;

import static gu.dtalk.activemq.ActivemqContext.HELPER;

public abstract class BaseActivemqConfigProvider implements ActivemqConfigProvider,Constant {
	protected static final String DEFAULT_AMQP_URI = "amqp://localhost:5672";
	public BaseActivemqConfigProvider() {
	}
	
	/**
	 * 返回当前对象的ActiveMQ连接配置对象(引用)
	 * @return {@link Properties}实例
	 */
	protected abstract MQProperties selfProp();
	
	@Override
	public final MQProperties getProperties(){
		return HELPER.makeMQProperties(null).init(selfProp());
	}
	@Override
	public final void setProperties(Properties properties) {
		Properties self = selfProp();
		if(properties != null){
			self.clear();
			self.putAll(properties);
		}		
	}
	@Override
	public final void setProperty(String name, String value) {
		selfProp().setProperty(name, value);

	}

	@Override
	public final String getProperty(String name) {
		return selfProp().getProperty(name);
	}

	@Override
	public final String getProperty(String name, String defaultValue) {
		return selfProp().getProperty(name,defaultValue);
	}
	protected static void initMqttURI(MQProperties props,int mqttPort) {
		URI mqttURI = URISupport.changePortUnchecked(props.getLocation(),mqttPort);
		mqttURI = URISupport.changeSchemeUnchecked(mqttURI,MQTT_SCHEMA);
		props.setProperty(MQ_PUBSUB_URI, mqttURI.toString());
		props.setProperty(MQ_PUBSUB_MQTT, Boolean.TRUE.toString());
	}
}
