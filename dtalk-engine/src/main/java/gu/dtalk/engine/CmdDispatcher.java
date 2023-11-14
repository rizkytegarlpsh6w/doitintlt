package gu.dtalk.engine;

import java.util.List;

import com.google.common.base.Supplier;

import gu.dtalk.CommonConstant.ReqCmdType;
import gu.dtalk.DeviceInstruction;
import gu.simplemq.Channel;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.IPublisher;
import gu.simplemq.ISubscriber;
import static com.google.common.base.Preconditions.*;
/**
 * 多目标设备命令分发器,实现{@link IMessageAdapter}接口<br>
 * 从设备命令频道得到设备指令{@link DeviceInstruction},并将交给{@link ItemAdapter}执行<br>
 * 如果是与当前设备无关的命令则跳过<br>
 * 收到的设备命令将按收到命令的顺序在线程池中顺序执行
 * @author guyadong
 *
 */
public class CmdDispatcher extends BaseDispatcher {
	/** 设备所属的组可能是可以变化的,所以这里需要用{@code Supplier} 接口来动态获取当前设备的设备组 */
	private Supplier<Integer> groupIdSupplier;
	private final ISubscriber subscriber;
	/** 判断target列表是否包括当前设备 */
	private boolean selfIncluded(boolean group,List<Integer> target){
		if(group){
			if(null == groupIdSupplier){
				return false;
			}
			Integer groupId = groupIdSupplier.get();
			return null == groupId ? false : target.contains(groupId); 
		}
		else {
			return target.contains(this.deviceId);
		}
	}
	@Override
	protected boolean validate(DeviceInstruction deviceInstruction){
		return super.validate(deviceInstruction) 
				&& null != deviceInstruction.getTarget() 
				&& selfIncluded(deviceInstruction.isGroup(),deviceInstruction.getTarget());
	}
	public CmdDispatcher(int deviceId, IPublisher publisher, ISubscriber subscriber) {
		super(deviceId, ReqCmdType.MULTI, publisher);
		this.subscriber = checkNotNull(subscriber,"subscriber is null");
	}

	@Override
	protected void doRegister(Channel<DeviceInstruction> channel) {
		subscriber.register(channel);
	}

	@Override
	protected void doUnregister(String channel) {
		subscriber.unregister(channel);
	}

	public Supplier<Integer> getGroupIdSupplier() {
		return groupIdSupplier;
	}

	public CmdDispatcher setGroupIdSupplier(Supplier<Integer> groupIdSupplier) {
		this.groupIdSupplier = groupIdSupplier;
		return this;
	}

}
