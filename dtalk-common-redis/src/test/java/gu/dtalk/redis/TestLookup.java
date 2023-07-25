package gu.dtalk.redis;

import org.junit.Test;

import gu.dtalk.redis.RedisConfigType;
import gu.simplemq.IMQConnParameterSupplier;
import gu.simplemq.IMessageQueueConfigManager;
import gu.simplemq.MessageQueueConfigManagers;
import gu.simplemq.MessageQueueType;
import gu.simplemq.SimpleLog;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;

public class TestLookup {

	@Test
	public void test() {
		IMessageQueueConfigManager manager = MessageQueueConfigManagers.getManager(MessageQueueType.REDIS);
		try {
			IMQConnParameterSupplier supplier = manager.lookupMessageQueueConnect(2000);
			SimpleLog.log("connect {}",supplier);
		} catch (SmqNotFoundConnectionException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test2() {
//		 DefaultLocalRedisConfigProvider.initLandtalkhost("192.168.10.226");
		boolean connectable = RedisConfigType.LAN.testConnect(1000);
		System.out.print(connectable);
	}
}
