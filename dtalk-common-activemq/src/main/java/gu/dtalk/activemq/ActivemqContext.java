package gu.dtalk.activemq;

import gu.simplemq.IMQContext;
import gu.simplemq.MQConstProvider;
import gu.simplemq.MQContextLoader;
import gu.simplemq.MQPropertiesHelper;
import gu.simplemq.MessageQueueType;

public class ActivemqContext {

	public static final IMQContext CONTEXT = MQContextLoader.getMQContextChecked(MessageQueueType.ACTIVEMQ);
	public static final MQPropertiesHelper HELPER = CONTEXT.getPropertiesHelper();
	public static final MQConstProvider CONSTP_ROVIDER = HELPER.getConstProvider();

	private ActivemqContext() {
	}

}
