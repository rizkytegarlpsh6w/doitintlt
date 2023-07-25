package gu.dtalk.redis;

import gu.simplemq.IMessageQueueConfigManager;
import gu.simplemq.MessageQueueType;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;

import static gu.dtalk.redis.RedisConfigType.*;

public class RedisConfigManager implements IMessageQueueConfigManager {

	public RedisConfigManager() {
	}

	@Override
	public RedisConfigType lookupMessageQueueConnect(Integer timeoutMills) throws SmqNotFoundConnectionException{
		return lookupConnect(timeoutMills);
	}

	@Override
	public RedisConfigType lookupMessageQueueConnectUnchecked(Integer timeoutMills) {
		return lookupConnectUnchecked(timeoutMills);
	}
	
	@Override
	public final MessageQueueType getImplType() {
		return MessageQueueType.REDIS;
	}
}
