package gu.dtalk.activemq;

import java.util.Properties;

import gu.simplemq.activemq.ActivemqConstants;
import gu.simplemq.activemq.PropertiesHelper;
import gu.simplemq.utils.MQProperties;

public abstract class BaseActivemqConfigProvider implements ActivemqConfigProvider,ActivemqConstants {
	public BaseActivemqConfigProvider() {
	}
	
	/**
	 * 返回当前对象的ActiveMQ连接配置对象(引用)
	 * @return {@link Properties}实例
	 */
	protected abstract MQProperties selfProp();
	
	@Override
	public final MQProperties getProperties(){
		return PropertiesHelper.AHELPER.makeMQProperties(null).init(selfProp());
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

}
