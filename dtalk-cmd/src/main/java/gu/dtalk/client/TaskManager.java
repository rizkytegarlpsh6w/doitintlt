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
	private Supplier<Channel<DeviceInstruction>> taskQueueSupplier;
    private String cmdpath;
	/**
     * 构造方法
     * @param poolLazy 连接池对象
     */
    public TaskManager(JedisPoolLazy poolLazy) {
    	super(poolLazy);
        this.producer = RedisFactory.getProducer(poolLazy);
    }
	/**
     * 构造方法
     * @param poolLazy 连接池对象
     * @param taskQueueSupplier 提供任务队列名的{@link Supplier}对象
     */
    public TaskManager(JedisPoolLazy poolLazy, Supplier<String> taskQueueSupplier) {
    	this(poolLazy);
        this.taskQueueSupplier = new FreshedChannelSupplier(taskQueueSupplier);
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
	 * @return 当前对象
	 */
	public TaskManager setCmdpath(String cmdpath) {
		this.cmdpath = checkNotNull(Strings.emptyToNull(cmdpath),"cmdpath is null or empty");
		return this;
	}
	/**
	 * @return channelSupplier
	 */
	public Supplier<Channel<DeviceInstruction>> getTaskQueueSupplier() {
		return taskQueueSupplier;
	}
	/**
	 * @param taskQueueSupplier 要设置的 channelSupplier
	 * @return 当前对象
	 */
	public TaskManager setTaskQueueSupplier(Supplier<Channel<DeviceInstruction>> taskQueueSupplier) {
		this.taskQueueSupplier = checkNotNull(taskQueueSupplier,"taskQueueSupplier is null");
		return this;
	}
	/**
	 * @param taskQueue 任务队列名
	 * @return 当前对象
	 */
	public TaskManager setTaskQueue(String taskQueue) {
		Channel<DeviceInstruction> channel = 
				new Channel<>(checkNotNull(Strings.emptyToNull(taskQueue),"taskQueue is null or empty"),DeviceInstruction.class);
		return setTaskQueueSupplier(Suppliers.ofInstance(channel));
	}
	/**
     * 发送设备命令
     * @param cmd 设备命令
     * @return 1--成功提交任务,0--任务提交失败
     */
    @Override
    protected long doSendCmd(DeviceInstruction cmd){
    	Channel<DeviceInstruction> channel = checkNotNull(taskQueueSupplier,"taskQueueSupplier is uninitialized").get();
    	int numSub = RedisConsumer.countOf(checkNotNull(channel,"taskQueue from taskQueueSupplier is null ").name);
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
	public int runCmd(String cmdpath, Map<String, Object> params) {
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
     * 发送设备命令<br>
	 * @param params 命令参数
	 * @return 任务成功提交返回{@code true},否则返回{@code false}
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map)
	 */
	public boolean runCmd(Map<String, Object> params) {
		return 1 == super.runCmd(checkCmdpath(), params);
	}

	/**
	 * @param params -
	 * @param adapter -
	 * @see gu.dtalk.client.BaseCmdManager#runCmd(java.lang.String, java.util.Map, gu.dtalk.IAckAdapter)
	 */
	public void runCmd(Map<String, Object> params, IAckAdapter<Object> adapter) {
		super.runCmd(checkCmdpath(), params, adapter);
	}

	/**
	 * @param params -
	 * @param throwIfTimeout -
	 * @return -
	 * @throws InterruptedException -
	 * @throws AckTimtoutException -
	 * @see gu.dtalk.client.BaseCmdManager#runCmdSync(java.lang.String, java.util.Map, boolean)
	 */
	public List<Ack<Object>> runCmdSync(Map<String, Object> params, boolean throwIfTimeout)
			throws InterruptedException, AckTimtoutException {
		return super.runCmdSync(checkCmdpath(), params, throwIfTimeout);
	}
}
