package gu.dtalk.client;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

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
	private Supplier<Channel<DeviceInstruction>> channelSupplier;
    private String cmdpath;
	/**
     * 构造方法
     * @param poolLazy 
     * @param taskQueueSupplier 提供任务队列名的{@link Supplier}对象
     */
    public TaskManager(JedisPoolLazy poolLazy) {
    	super(poolLazy);
        this.producer = RedisFactory.getProducer(poolLazy);
    }
	/**
     * 构造方法
     * @param poolLazy 
     * @param taskQueueSupplier 提供任务队列名的{@link Supplier}对象
     */
    public TaskManager(JedisPoolLazy poolLazy, Supplier<String> taskQueueSupplier) {
    	this(poolLazy);
        this.channelSupplier = new FreshedChannelSupplier(taskQueueSupplier);
    }
    /**
	 * 构造方法
	 * @param taskQueueSupplier 提供任务队列名的{@link Supplier}对象
	 */
	public TaskManager(Supplier<String> taskQueueSupplier) {
		this(JedisPoolLazy.getDefaultInstance(), taskQueueSupplier);
	}
	/**
     * 构造方法
     * @param cmdpath 设备(菜单)命令路径
     * @param taskQueueSupplier 提供任务队列名的{@link Supplier}对象
     */
    public TaskManager(String cmdpath, Supplier<String> taskQueueSupplier) {
    	this(taskQueueSupplier);
        setCmdpath(cmdpath);
    }
    /**
     * 构造方法
     * @param taskQueue 任务队列
     */
    public TaskManager(String taskQueue) {
    	this(Suppliers.ofInstance(checkNotNull(Strings.emptyToNull(taskQueue),"taskQueueSupplier is null or empty")));
    }
    public TaskManager(JedisPoolLazy poolLazy, String cmdpath, Supplier<String> taskQueueSupplier) {
    	this(poolLazy,taskQueueSupplier);
    	setCmdpath(cmdpath);
	}
    public TaskManager(){
    	this(JedisPoolLazy.getDefaultInstance());
    }
	/**
	 * @return cmdpath
	 */
	public String getCmdpath() {
		return cmdpath;
	}
	/**
	 * @param cmdpath 要设置的 cmdpath
	 * @return 
	 */
	public TaskManager setCmdpath(String cmdpath) {
		this.cmdpath = checkNotNull(Strings.emptyToNull(cmdpath),"cmdpath is null or empty");
		return this;
	}
	/**
	 * @return channelSupplier
	 */
	public Supplier<Channel<DeviceInstruction>> getChannelSupplier() {
		return channelSupplier;
	}
	/**
	 * @param channelSupplier 要设置的 channelSupplier
	 * @return 当前对象
	 */
	public TaskManager setChannelSupplier(Supplier<Channel<DeviceInstruction>> channelSupplier) {
		this.channelSupplier = checkNotNull(channelSupplier,"channelSupplier is null");
		return this;
	}
	/**
	 * @param taskQueue 任务队列名
	 * @return 当前对象
	 */
	public TaskManager setChannel(String taskQueue) {
		Channel<DeviceInstruction> channel = 
				new Channel<>(checkNotNull(Strings.emptyToNull(taskQueue),"channel is null or empty"),DeviceInstruction.class);
		return setChannelSupplier(Suppliers.ofInstance(channel));
	}
	/**
     * 发送设备命令
     * @param cmd
     * @return 收到命令的客户端数目
     */
    @Override
    protected long doSendCmd(DeviceInstruction cmd){
    	Channel<DeviceInstruction> channel = checkNotNull(channelSupplier,"channelSupplier is uninitialized").get();
    	int numSub = RedisConsumer.countOf(checkNotNull(channel,"channel from channelSupplier is null ").name);
    	if(numSub >0){    		
    		producer.produce(channel, cmd);
    		return 1;
    	}
    	return 0;
    }

    private String checkCmdpath(){
    	return checkNotNull(cmdpath,"'cmdpath' field is uninitialized");
    }
    
    private void checkCmdPath(String cmdpath){
		checkState(this.checkCmdpath().equals(cmdpath),"MISMATCH argument cmdpath,required %s",this.checkCmdpath());
    }
	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map)
	 * @deprecated replaced by {@link #runCmd(Map)}
	 */
	@Override
	public long runCmd(String cmdpath, Map<String, Object> params) {
		checkCmdPath(cmdpath);
		return super.runCmd(cmdpath, params);
	}

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map, gu.dtalk.IAckAdapter)
	 * @deprecated replaced by {@link #runCmd(Map, IAckAdapter)}
	 */
	@Override
	public void runCmd(String cmdpath, Map<String, Object> params, IAckAdapter<Object> adapter) {
		checkCmdPath(cmdpath);
		super.runCmd(cmdpath, params, adapter);
	}

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmdSync(java.lang.String, java.util.Map, boolean)
	 * @deprecated replaced by {@link #runCmdSync(Map, boolean)}
	 */
	@Override
	public List<Ack<Object>> runCmdSync(String cmdpath, Map<String, Object> params, boolean throwIfTimeout)
			throws InterruptedException, AckTimtoutException {
		checkCmdPath(cmdpath);
		return super.runCmdSync(cmdpath, params, throwIfTimeout);
	}
	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map)
	 */
	public long runCmd(Map<String, Object> params) {
		return super.runCmd(checkCmdpath(), params);
	}

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map, gu.dtalk.IAckAdapter)
	 */
	public void runCmd(Map<String, Object> params, IAckAdapter<Object> adapter) {
		super.runCmd(checkCmdpath(), params, adapter);
	}

	/**
	 * @see gu.dtalk.client.BaseCmdManager#runCmdSync(java.lang.String, java.util.Map, boolean)
	 */
	public List<Ack<Object>> runCmdSync(Map<String, Object> params, boolean throwIfTimeout)
			throws InterruptedException, AckTimtoutException {
		return super.runCmdSync(checkCmdpath(), params, throwIfTimeout);
	}
}
