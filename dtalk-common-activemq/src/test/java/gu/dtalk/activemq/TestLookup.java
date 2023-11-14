package gu.dtalk.activemq;

import org.junit.Test;

import gu.simplemq.IMQConnParameterSupplier;
import gu.simplemq.IMessageQueueConfigManager;
import gu.simplemq.MessageQueueConfigManagers;
import gu.simplemq.MessageQueueType;
import gu.simplemq.SimpleLog;
import gu.simplemq.exceptions.SmqNotFoundConnectionException;

public class TestLookup {

	@Test
	public void test() {
		IMessageQueueConfigManager manager = MessageQueueConfigManagers.getManager(MessageQueueType.ACTIVEMQ);
		try {
			IMQConnParameterSupplier supplier = manager.lookupMessageQueueConnect(null);
			SimpleLog.log("connect {}",supplier);
		} catch (SmqNotFoundConnectionException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test2() {
		 DefaultLocalActivemqConfigProvider.initLandtalkhost("192.168.10.226");
		boolean connectable = ActivemqConfigType.LAN.testConnect(1000);
		System.out.print(connectable);
	}
}
