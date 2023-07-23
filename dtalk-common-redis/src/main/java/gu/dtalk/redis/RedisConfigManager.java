package gu.dtalk.redis;

import gu.simplemq.IMessageQueueConfigManager;
import gu.simplemq.MessageQueueType;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;

import static gu.dtalk.redis.RedisConfigType.*;

public class RedisConfigManager implements IMessageQueueConfigManager {

	public RedisConfigManager() {
	}

	@Override
	public RedisConfigType lookupMessageQueueConnect() throws SmqNotFoundConnectionException{
		return lookupRedisConnect();
	}

	@Override
	public RedisConfigType lookupRedisConnectUnchecked() {
		return lookupRedisConnectUnchecked();
	}
	
	@Override
	public final MessageQueueType getImplType() {
		return MessageQueueType.REDIS;
	}
}
