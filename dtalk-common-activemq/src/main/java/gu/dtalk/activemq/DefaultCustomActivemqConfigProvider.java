package gu.dtalk.activemq;

import gu.simplemq.utils.MQProperties;
import static gu.dtalk.activemq.ActivemqContext.HELPER;

/**
 * 自定义配置默认实现
 * @author guyadong
 *
 */
public class DefaultCustomActivemqConfigProvider extends BaseActivemqConfigProvider {
	public static final MQProperties INIT_PROPERTIES ;
	static{
		INIT_PROPERTIES = HELPER.makeMQProperties(null);
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
