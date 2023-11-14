package gu.dtalk.cmd;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import gu.dtalk.Ack;
import gu.dtalk.DeviceInstruction;
import gu.dtalk.IAckAdapter;
import gu.dtalk.exception.AckTimtoutException;
import gu.simplemq.Channel;
import gu.simplemq.IProducer;
import gu.simplemq.IPublisher;
import gu.simplemq.ISubscriber;
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
    private final IProducer producer;
	private Supplier<Channel<DeviceInstruction>> taskQueueSupplier;
    private String cmdpath;

    /**
     * 构造方法
     * @param publisher 消息发布器
     * @param subscriber 消息订阅(接收)发布器
     * @param producer 生产者
     */
    public TaskManager(IPublisher publisher,ISubscriber subscriber,IProducer producer) {
    	super(publisher,subscriber);
        this.producer = checkNotNull(producer,"producer is null");
    }
    /**
     * 构造方法
     * @param publisher 消息发布器
     * @param subscriber 消息订阅(接收)发布器
     * @param producer 生产者
     * @param taskQueueSupplier
     */
    public TaskManager(IPublisher publisher,ISubscriber subscriber,IProducer producer, Supplier<String> taskQueueSupplier) {
    	this(publisher,subscriber,producer);
        this.taskQueueSupplier = new FreshedChannelSupplier(taskQueueSupplier);
    }

    public TaskManager(IPublisher publisher,ISubscriber subscriber,IProducer producer, String cmdpath, Supplier<String> taskQueueSupplier) {
    	this(publisher,subscriber,producer,taskQueueSupplier);
    	setCmdpath(cmdpath);
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
    	int numSub = producer.getAdvisor().consumerCountOf(checkNotNull(channel,"taskQueue from taskQueueSupplier is null ").name);
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
	 * @see gu.dtalk.cmd.BaseCmdManager#runCmd(java.lang.String, java.util.Map)
	 * @deprecated replaced by {@link #runCmd(Map)}
	 */
	@Override
	public int runCmd(String cmdpath, Map<String, Object> params) {
		checkCmdPath(cmdpath);
		return super.runCmd(cmdpath, params);
	}

	/**
	 * @see gu.dtalk.cmd.BaseCmdManager#runCmd(java.lang.String, java.util.Map, gu.dtalk.IAckAdapter)
	 * @deprecated replaced by {@link #runCmd(Map, IAckAdapter)}
	 */
	@Override
	public void runCmd(String cmdpath, Map<String, Object> params, IAckAdapter<Object> adapter) {
		checkCmdPath(cmdpath);
		super.runCmd(cmdpath, params, adapter);
	}

	/**
	 * @see gu.dtalk.cmd.BaseCmdManager#runCmdSync(java.lang.String, java.util.Map, boolean)
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
	 * @see gu.dtalk.cmd.BaseCmdManager#runCmd(java.lang.String, java.util.Map)
	 */
	public boolean runCmd(Map<String, Object> params) {
		return 1 == super.runCmd(checkCmdpath(), params);
	}

	/**
	 * @param params -
	 * @param adapter -
	 * @see gu.dtalk.cmd.BaseCmdManager#runCmd(java.lang.String, java.util.Map, gu.dtalk.IAckAdapter)
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
	 * @see gu.dtalk.cmd.BaseCmdManager#runCmdSync(java.lang.String, java.util.Map, boolean)
	 */
	public List<Ack<Object>> runCmdSync(Map<String, Object> params, boolean throwIfTimeout)
			throws InterruptedException, AckTimtoutException {
		return super.runCmdSync(checkCmdpath(), params, throwIfTimeout);
	}
}
