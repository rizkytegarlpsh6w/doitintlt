package gu.dtalk.activemq;

import gu.simplemq.activemq.ActivemqProperties;

/**
 * 自定义配置默认实现
 * @author guyadong
 *
 */
public class DefaultCustomActivemqConfigProvider extends BaseActivemqConfigProvider {
	public static final ActivemqProperties INIT_PROPERTIES = new ActivemqProperties();

	public DefaultCustomActivemqConfigProvider() {
	}
	@Override
	protected ActivemqProperties selfProp() {
		return INIT_PROPERTIES;
	}
	@Override
	public final ActivemqConfigType type() {
		return ActivemqConfigType.CUSTOM;
	}

}
