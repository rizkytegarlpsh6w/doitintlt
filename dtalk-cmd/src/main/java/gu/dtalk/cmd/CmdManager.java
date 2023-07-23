package gu.dtalk.cmd;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import gu.dtalk.DeviceInstruction;
import gu.simplemq.IPublisher;
import gu.simplemq.ISubscriber;

/**
 * 
 * (多目标)设备命令发送管理模块<br>
 * @author guyadong
 *
 */
public class CmdManager extends BaseCmdManager{
    private final FreshedChannelSupplier channelSupplier;
    /**
     * 构造方法
     * @param publisher 消息发布器
     * @param subscriber 消息订阅(接收)器
     * @param cmdChannelSupplier 提供命令频道名的{@link Supplier}对象
     */
    public CmdManager(IPublisher publisher,ISubscriber subscriber, Supplier<String> cmdChannelSupplier) {
    	super(publisher,subscriber);
    	this.channelSupplier = new FreshedChannelSupplier(cmdChannelSupplier);
    }
    public CmdManager(IPublisher publisher,ISubscriber subscriber, String cmdChannel) {
    	this(publisher,subscriber,
    			Suppliers.ofInstance(checkNotNull(Strings.emptyToNull(cmdChannel),"cmdChannel is null or empty")));
	}
	/**
     * 发送前检查target是否有定义，未定义则抛出异常
     */
    @Override
    protected long doSendCmd(DeviceInstruction cmd){
        checkArgument(null != cmd.getTarget() && !cmd.getTarget().isEmpty(),"DeviceInstruction.target field must not be null");

        return publisher.publish(channelSupplier.get(), cmd);
    }

}
