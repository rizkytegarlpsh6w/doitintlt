package gu.dtalk.engine;

import java.util.Set;

import gu.dtalk.CommonConstant.ReqCmdType;
import gu.dtalk.DeviceInstruction;
import gu.simplemq.Channel;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;
/**
 * 设备任务分发器,实现{@link IMessageAdapter}接口<br>
 * 从任务队列得到设备指令{@link DeviceInstruction},并将交给{@link ItemAdapter}执行<br>
 * 收到的设备命令将按收到命令的顺序在线程池中顺序执行
 * @author guyadong
 *
 */
public class TaskDispatcher extends BaseDispatcher {

	public TaskDispatcher(int deviceId, JedisPoolLazy jedisPoolLazy) {
		super(deviceId, ReqCmdType.TASKQUEUE, jedisPoolLazy);
	}

	public TaskDispatcher(int deviceId) {
		this(deviceId, JedisPoolLazy.getDefaultInstance());
	}

	@Override
	protected void doRegister(Channel<DeviceInstruction> channel) {
		RedisFactory.getConsumer().register(channel);
	}

	@Override
	protected void doUnregister(String channel) {
		RedisFactory.getConsumer().unregister(channel);
	}

	@Override
	protected Set<String> doUnregisterAll() {
		return RedisFactory.getConsumer().unregister(this);
	}


}
