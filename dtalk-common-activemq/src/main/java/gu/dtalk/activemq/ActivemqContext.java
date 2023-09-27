package gu.dtalk.activemq;

import gu.simplemq.MQConstProvider;
import gu.simplemq.MessageQueueType;
import gu.simplemq.utils.IMQContext;
import gu.simplemq.utils.MQContextLoader;
import gu.simplemq.utils.MQPropertiesHelper;

public class ActivemqContext {

	public static final IMQContext CONTEXT = MQContextLoader.getMQContextChecked(MessageQueueType.ACTIVEMQ);
	public static final MQPropertiesHelper HELPER = CONTEXT.getPropertiesHelper();
	public static final MQConstProvider CONSTP_ROVIDER = HELPER.getConstProvider();

	private ActivemqContext() {
	}

}
