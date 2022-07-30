package gu.dtalk.client;

import com.google.common.base.Supplier;

import gu.dtalk.Ack;
import gu.dtalk.DeviceInstruction;
import gu.dtalk.IAckAdapter;
import gu.dtalk.exception.AckTimtoutException;
import gu.simplemq.Channel;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisConsumer;
import gu.simplemq.redis.RedisFactory;
import gu.simplemq.redis.RedisProducer;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

/**
 * 
 * (任务队列)设备命令发送管理模块<br>
 * @author guyadong
 *
 */
public class TaskManager extends BaseCmdManager {
    private final RedisProducer producer;
	private final String cmdpath;
	private final FreshedChannelSupplier<DeviceInstruction> channelSupplier;
    /**
     * 构造方法
     * @param poolLazy 
     * @param cmdpath 设备(菜单)命令路径
     * @param taskQueueSupplier
     */
    public TaskManager(JedisPoolLazy poolLazy, String cmdpath, Supplier<String> taskQueueSupplier) {
    	super(poolLazy);
        this.producer = RedisFactory.getProducer(poolLazy);
        this.cmdpath = checkNotNull(Strings.emptyToNull(cmdpath),"cmdpath is null or empty");
        this.channelSupplier = new FreshedChannelSupplier<DeviceInstruction>(taskQueueSupplier);
    }
    
    /**
     * 发送设备命令
     * @param cmd
     * @return 收到命令的客户端数目
     */
    @Override
    protected long doSendCmd(DeviceInstruction cmd){
    	Channel<DeviceInstruction> channel = channelSupplier.get();
    	int numSub = RedisConsumer.countOf(channel.name);
    	if(numSub >0){    		
    		producer.produce(channel, cmd);
    		return 1;
    	}
    	return 0;
    }

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map)
	 * @deprecated replaced by {@link #runCmd(Map)}
	 */
	@Override
	public long runCmd(String cmdpath, Map<String, Object> params) {
		checkState(this.cmdpath.equals(cmdpath),"MISMATCH cmdpath,required %s",this.cmdpath);
		return super.runCmd(cmdpath, params);
	}

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map, gu.dtalk.IAckAdapter)
	 * @deprecated replaced by {@link #runCmd(Map, IAckAdapter)}
	 */
	@Override
	public void runCmd(String cmdpath, Map<String, Object> params, IAckAdapter<Object> adapter) {
		checkState(this.cmdpath.equals(cmdpath),"MISMATCH cmdpath,required %s",this.cmdpath);
		super.runCmd(cmdpath, params, adapter);
	}

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmdSync(java.lang.String, java.util.Map, boolean)
	 * @deprecated replaced by {@link #runCmdSync(Map, boolean)}
	 */
	@Override
	public List<Ack<Object>> runCmdSync(String cmdpath, Map<String, Object> params, boolean throwIfTimeout)
			throws InterruptedException, AckTimtoutException {
		checkState(this.cmdpath.equals(cmdpath),"MISMATCH cmdpath,required %s",this.cmdpath);
		return super.runCmdSync(cmdpath, params, throwIfTimeout);
	}
	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map)
	 */
	public long runCmd(Map<String, Object> params) {
		return super.runCmd(cmdpath, params);
	}

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map, gu.dtalk.IAckAdapter)
	 */
	public void runCmd(Map<String, Object> params, IAckAdapter<Object> adapter) {
		super.runCmd(cmdpath, params, adapter);
	}

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmdSync(java.lang.String, java.util.Map, boolean)
	 */
	public List<Ack<Object>> runCmdSync(Map<String, Object> params, boolean throwIfTimeout)
			throws InterruptedException, AckTimtoutException {
		return super.runCmdSync(cmdpath, params, throwIfTimeout);
	}
}
