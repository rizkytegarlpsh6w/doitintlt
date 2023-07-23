package gu.dtalk.cmd;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.primitives.Ints;

import gu.dtalk.Ack;
import gu.dtalk.DeviceInstruction;
import gu.dtalk.IAckAdapter;
import gu.dtalk.exception.AckTimtoutException;
import gu.simplemq.ISubscriber;
import gu.simplemq.Channel;
import gu.simplemq.IPublisher;
import gu.simplemq.IUnregistedListener;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import static com.google.common.base.Preconditions.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Strings;
/**
 * 
 * 设备命令发送管理模块基类<br>
 * 线程安全<br>
 * 发送设备命令示例:
 * <pre>
 *    String ackChannel = IFaceLogClient.applyAckChannel(myToken); // 向facelog服务申请命令响应通道
 *    long cmdSn = IFaceLogClient.applyCmdSn(myToken); // 向facelog服务申请命令序列号
 *    targetBuilder()
 *        .setCmdSn(cmdSn) // 设置命令序列号
 *        .setDeviceTarget(deviceId) // 指定目标设备ID
 *        .setAckChannel(ackChannel) // 设置命令响应通道
 *        .build()
 *        .reset(null); // 执行reset命令,立即执行
 *    // 如果同时设置命令响应处理对象,请调用 reset(Long ,IAckAdapter)
 * </pre>
 * 带{@code IAckAdapter}类型参数的方法为异步执行方法,需要应用项目提供{@code IAckAdapter}实例,
 * {@code sync}后缀的设备命令方法为同步执行方法,
 * @author guyadong
 *
 */
public abstract class BaseCmdManager {
	protected final ISubscriber subscriber;
	protected final IPublisher publisher;
    private Supplier<Integer> cmdSnSupplier;
    private Supplier<String> ackChannelSupplier = Suppliers.ofInstance(null);
	protected BaseCmdManager(IPublisher publisher,ISubscriber subscriber) {
        this.publisher = checkNotNull(publisher,"publisher is null");
        this.subscriber = checkNotNull(subscriber,"subscriber is null");
	}
    /**
     * 执行数据发送<br>
     * @param cmd 设备命令
     * @return 收到命令的客户端数目
     */
	protected abstract long doSendCmd(DeviceInstruction cmd);
    /**
     * 用于处理超时等待的{@link Ack}对象<br>
     * 向{@link Ack}对象发送超时错误{@link Ack.Status#TIMEOUT}
     * @author guyadong
     *
     * @param <T> 设备命令响应返回数据类型
     */
    private class TimeoutCleaner <T> implements IUnregistedListener<Ack<T>>{
        @Override
        public void apply(Channel<Ack<T>> input) {
            IAckAdapter<T> adapter = (IAckAdapter<T>)input.getAdapter();
            try{
                if(!adapter.isFinished()){
                    // 通知执行器命令超时
                    adapter.onSubscribe(new Ack<T>().setStatus(Ack.Status.TIMEOUT));
                }
            }catch(SmqUnsubscribeException e){
            }catch(RuntimeException e){
                e.printStackTrace();
            }
        }
    }
	/**
	 * 用于同步执行设备命令的{@link IAckAdapter}实现
	 * @author guyadong
	 *
	 * @param <T> 设备命令响应返回数据类型
	 */
	private class AdapterSync<T> extends IAckAdapter.BaseAdapter<T>{
	    final List<Ack<T>> acks = Collections.synchronizedList(new LinkedList<Ack<T>>());
	    final AtomicBoolean timeout = new AtomicBoolean(false);
	    @Override
	    protected void doOnTimeout() {
	        timeout.set(true);
	    }
	
	    @Override
	    protected void doOnSubscribe(Ack<T> t) {
	        acks.add(t);
	    }
	}
	/** 
     * 设备命令参数构建工具类,用于设置命令目标，命令序列号，命令响应频道
     * @author guyadong
     */
    public class CmdBuilder{       
        List<Integer> target;
        boolean group;
        /** 命令发送后是否自动清除TLS变量 */
        boolean autoRemove = true;
		private String ackChannel;
		private Integer cmdSn;
        private CmdBuilder(){
        }
        /**
         * 调用{@link Supplier}实例获取当前设备命令需要的序列号和响应通道,
         * 此方法每次调用获取的命令序列号都不同，所以不可以随意调用
         * @return 当前对象
         */
        CmdBuilder apply(){
        	if(cmdSn == null){
            	checkState(null != cmdSnSupplier,"cmdSnSupplier is uninitialized");
            	// 命令行序列号不可为空
        		cmdSn = checkNotNull(cmdSnSupplier.get(),"cmdSn is null");
        	}
        	if(Strings.isNullOrEmpty(ackChannel)){
        		ackChannel = ackChannelSupplier.get();
        	}
            return this;
        }

        /**
         * 指定目标ID(设备/设备组)列表,参见 {@link DeviceInstruction#setTarget(List, boolean)}
         * @param target 目标列表
         * @param group 目标是否为设备组
         * @return 当前对象
         */
        public CmdBuilder setTarget(List<Integer> target,boolean group){
            this.target = target;
            this.group = group;
            return this;
        }
        /**
         * 指定设备目标为设备ID列表,参见 {@link DeviceInstruction#setTarget(List, boolean)}
         * @param target 目标列表
         * @return 当前对象
         */
        public CmdBuilder setDeviceTarget(List<Integer> target){
            return setTarget(target,false);
        }
        /**
         * 指定设备目标为设备ID列表,参见 {@link DeviceInstruction#setTarget(List, boolean)}
         * @param target 目标列表
         * @return 当前对象
         */
        public CmdBuilder setDeviceTarget(int... target){
            return setDeviceTarget(Ints.asList(target));
        }
        /**
         * 指定设备目标为设备组ID列表,参见 {@link DeviceInstruction#setTarget(List, boolean)}
         * @param target 目标列表
         * @return 当前对象
         */
        public CmdBuilder setDeviceGroupTarget(List<Integer> target){
            return setTarget(target,true);
        }
        /**
         * 指定设备目标为设备组ID列表,参见 {@link DeviceInstruction#setTarget(List, boolean)}
         * @param target 目标列表
         * @return 当前对象
         */
        public CmdBuilder setDeviceGroupTarget(int... target){
            return setDeviceGroupTarget(Ints.asList(target));
        }

        /**
         * @param autoRemove 为{@code true}时,完成设备命令发送后自动清除Thread Local Storage变量{@link CmdManager#TLS_BUILDER},
         *                                    默认值为{@code true}
         * @return 当前对象
         */
        public CmdBuilder autoRemove(boolean autoRemove){
            this.autoRemove = autoRemove;
            return this;
        }
		/**
		 * @param ackChannel 要设置的 ackChannel
		 * @return 当前对象
		 */
		public CmdBuilder setAckChannel(String ackChannel) {
			this.ackChannel = ackChannel;
			return this;
		}
		public CmdBuilder setCmdSn(int cmdSn) {
			this.cmdSn = cmdSn;		
			return this;
		}
    } 

	/** 
	 * TLS变量,用于保存当前线程使用的 {@link CmdBuilder}对象<br>,
	 * TLS变量在多线程高并发环境如果不显式执行{@link ThreadLocal#remove()}有资源泄漏风险,
	 * 如果{@link CmdBuilder#autoRemove}为{@code true}(默认值),则调用设备命令方法发送完命令后会自动清除TLS变量,
	 * 否则需要调用 {@link #removeTlsTarget()}方法手动清除。
	  */
	private static final ThreadLocal<CmdBuilder> TLS_BUILDER = new ThreadLocal<CmdBuilder>();

	public CmdBuilder targetBuilder() {
	    if(null == TLS_BUILDER.get()){
	        TLS_BUILDER.set(new CmdBuilder());
	    }
	    return TLS_BUILDER.get();
	}

	/** 
	 * 清除TLS变量 {@link #TLS_BUILDER}
	 * @return 当前对象
	 * @see ThreadLocal#remove()
	 */
	public BaseCmdManager removeTlsTarget() {
	    TLS_BUILDER.remove();
	    return this;
	}

    /**
     * 指定提供命令序列号的{@link Supplier}实例
     * @param cmdSnSupplier {@link Supplier}实例
     * @return 当前对象
     */
    public BaseCmdManager setCmdSn(Supplier<Integer> cmdSnSupplier) {
        this.cmdSnSupplier = checkNotNull(cmdSnSupplier);
        return this;
    }
    /**
     * 指定命令响应通道,参见 {@link DeviceInstruction#setAckChannel(String)}
     * @param ackChannel 响应频道名
     * @return 当前对象
     */
    public BaseCmdManager setAckChannel(String ackChannel){
        return this.setAckChannel(Suppliers.ofInstance(checkNotNull(Strings.emptyToNull(ackChannel),"ackChannel is null or empty")));
    }
    /**
     * 指定提供命令响应通道的{@link Supplier}实例
     * @param ackChannelSupplier {@link Supplier}实例
     * @return 当前对象
     */
    public BaseCmdManager setAckChannel(Supplier<String> ackChannelSupplier){
        this.ackChannelSupplier = checkNotNull(ackChannelSupplier);
        return this;
    }
	/**
     * 发送设备命令<br>
     * 将设备命令数据封装为{@link DeviceInstruction}对象并执行数据发送，
     * 发送前最后检查数据有效性
	 * @param cmdpath 设备命令名(全路径)
	 * @param params 设备命令参数对象, {@code 参数名(key)->参数值(value)映射},没有参数可为{@code null}
	 * @return 收到命令的客户端数目
	 */
	private long sendCmd(String cmdpath, Map<String, Object> params) {
        checkArgument(!Strings.isNullOrEmpty(cmdpath),"cmdpath must not be null or empty");
	    CmdBuilder builder = targetBuilder();
		try{
	        DeviceInstruction deviceInstruction = new DeviceInstruction()
	                .setCmdpath(cmdpath)
	                .setCmdSn(builder.cmdSn)
	                .setTarget(builder.target, builder.group)
	                .setAckChannel(builder.ackChannel)
	                .setParameters(params);
	        return doSendCmd(deviceInstruction);
	    }finally{
	        if(builder.autoRemove){
	            removeTlsTarget(); 
	        }
	    }
	}	
	/**
     * 发送设备命令<br>
     * 发送前申请命令序列号和响应频道
	 * @param cmdpath 设备命令名(dtalk全路径)
	 * @param params 设备命令参数对象, {@code 参数名(key)->参数值(value)映射},没有参数可为{@code null}
	 * @return 收到命令的客户端数目
	 */
	public int runCmd(String cmdpath, Map<String, Object> params) {
		targetBuilder().apply();
        return (int) sendCmd(cmdpath, params);
	}	
	/**
	 * 设备命令(异步调用)<br>
	 * 该方法会自动将命令响应通道名({@link #setAckChannel(String)})
	 * 关联命令处理对象({@code adapter})注册到REDIS订阅频道,当有收到设备命令响应时自动交由{@code adapter}处理<br>
	 * 该方法要求必须指定命令响应通道,参见{@link #setAckChannel(String)},{@link #setAckChannel(Supplier)}
	 * 
	 * @param cmdpath 设备命令名(dtalk全路径)
	 * @param params 设备命令参数对象, {@code 参数名(key)->参数值(value)映射},没有参数可为{@code null}
	 * @param adapter 命令响应处理对象,不可为{@code null}
	 */
	public void runCmd(String cmdpath, Map<String, Object> params, IAckAdapter<Object> adapter) {
		CmdBuilder builder = targetBuilder().apply();
		checkArgument(!Strings.isNullOrEmpty(builder.ackChannel),"INVALID ackChannel");
		Channel<Ack<Object>> channel = new Channel<Ack<Object>>(builder.ackChannel){}
		.setAdapter(checkNotNull(adapter,"adapter is null"))
		.addUnregistedListener(new TimeoutCleaner<Object>());
		subscriber.register(
				channel,
				adapter.getDuration(),
				TimeUnit.MILLISECONDS
				);
		long clientNum =  sendCmd(cmdpath,params);
		if(0 == clientNum){
			// 如果没有接收端收到命令则立即注销频道 
			subscriber.unregister(channel);
		}else{
			adapter.setClientNum(clientNum);
		}
	}

	/**
	 * 设备命令(同步调用)<br>
	 * 
	 * @param cmdpath 设备命令名(dtalk全路径)
	 * @param params 设备命令参数对象, {@code 参数名(key)->参数值(value)映射},没有参数可为{@code null}
	 * @param throwIfTimeout 当响应超时时，是否抛出{@link AckTimtoutException}异常
	 * @return 设备端返回的所有命令响应对象
	 * @throws InterruptedException 中断异常
	 * @throws AckTimtoutException 命令响应超时
	 * @see #runCmd(String, Map, IAckAdapter)
	 */
	public List<Ack<Object>> runCmdSync(String cmdpath, Map<String, Object> params, boolean throwIfTimeout) throws InterruptedException, AckTimtoutException {
	    AdapterSync<Object> adapter = new AdapterSync<Object>();
	    runCmd(cmdpath,params,adapter);
	    // 等待命令响应结束
	    adapter.waitFinished();
	    if(adapter.timeout.get() && throwIfTimeout){
	        throw new AckTimtoutException();
	    }
	    return adapter.acks;
	}
	/**
	 * 将当前对象转为子类
	 * @param <T> 子类类型
	 * @return 当前对象
	 */
	@SuppressWarnings("unchecked")
	public final <T extends BaseCmdManager> T self(){
		return (T) this;
	}
	/**
	 * 将当前对象转为指定的子类
	 * @param <T> 子类类型
	 * @param clazz 子类类型
	 * @return 当前对象
	 */
	public final <T extends BaseCmdManager> T self(Class<T> clazz){
		return checkNotNull(clazz,"clazz is null").cast(this);
	}
}


