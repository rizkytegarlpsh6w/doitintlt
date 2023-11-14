package gu.dtalk.activemq;

import static gu.dtalk.activemq.ActivemqConfigType.lookupConnect;
import static gu.dtalk.activemq.ActivemqConfigType.lookupConnectUnchecked;

import gu.simplemq.IMessageQueueConfigManager;
import gu.simplemq.MessageQueueType;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;

public class ActivemqConfigManager implements IMessageQueueConfigManager {

	public ActivemqConfigManager() {
	}

	@Override
	public ActivemqConfigType lookupMessageQueueConnect(Integer timeoutMills) 
			throws SmqNotFoundConnectionException{
		return lookupConnect(timeoutMills);
	}

	@Override
	public ActivemqConfigType lookupMessageQueueConnectUnchecked(Integer timeoutMills) {
		return lookupConnectUnchecked(timeoutMills);
	}
	
	@Override
	public final MessageQueueType getImplType() {
		return MessageQueueType.ACTIVEMQ;
	}
}
