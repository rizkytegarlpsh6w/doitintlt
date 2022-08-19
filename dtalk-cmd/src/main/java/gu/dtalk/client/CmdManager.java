package gu.dtalk.client;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import gu.dtalk.DeviceInstruction;
import gu.simplemq.redis.JedisPoolLazy;

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
     * @param poolLazy 
     * @param cmdChannelSupplier 提供命令频道名的{@link Supplier}对象
     */
    public CmdManager(JedisPoolLazy poolLazy, Supplier<String> cmdChannelSupplier) {
    	super(poolLazy);
    	this.channelSupplier = new FreshedChannelSupplier(cmdChannelSupplier);
    }
    /**
     * 构造方法
     * @param poolLazy 
     * @param cmdChannelSupplier 命令频道名
     */
    public CmdManager(Supplier<String> cmdChannelSupplier) {
    	this(JedisPoolLazy.getDefaultInstance(), cmdChannelSupplier);
    }
    /**
     * 构造方法
     * @param poolLazy 
     * @param cmdChannel 命令频道名
     */
    public CmdManager(String cmdChannel) {
    	this(Suppliers.ofInstance(checkNotNull(Strings.emptyToNull(cmdChannel),"cmdChannel is null or empty")));
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
