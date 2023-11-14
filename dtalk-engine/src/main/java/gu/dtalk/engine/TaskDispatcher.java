package gu.dtalk.engine;

import gu.dtalk.CommonConstant.ReqCmdType;
import gu.dtalk.DeviceInstruction;
import gu.simplemq.Channel;
import gu.simplemq.IConsumer;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.IPublisher;
import static com.google.common.base.Preconditions.*;

/**
 * 设备任务分发器,实现{@link IMessageAdapter}接口<br>
 * 从任务队列得到设备指令{@link DeviceInstruction},并将交给{@link ItemAdapter}执行<br>
 * 收到的设备命令将按收到命令的顺序在线程池中顺序执行
 * @author guyadong
 *
 */
public class TaskDispatcher extends BaseDispatcher {

	private final IConsumer consumer;

	public TaskDispatcher(int deviceId, IPublisher publisher, IConsumer consumer) {
		super(deviceId, ReqCmdType.TASKQUEUE, publisher);
		this.consumer = checkNotNull(consumer,"consumer is null");
	}

	@Override
	protected void doRegister(Channel<DeviceInstruction> channel) {
		consumer.register(channel);
	}

	@Override
	protected void doUnregister(String channel) {
		consumer.unregister(channel);
	}


}
