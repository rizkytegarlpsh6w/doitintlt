package gu.dtalk.engine;

import static com.google.common.base.Preconditions.*;
import static gu.dtalk.CommonConstant.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import gu.dtalk.Ack;
import gu.dtalk.BaseItem;
import gu.dtalk.BaseOption;
import gu.dtalk.CmdItem;
import gu.dtalk.CommonConstant.ReqCmdType;
import gu.dtalk.DeviceInstruction;
import gu.simplemq.Channel;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;
import gu.simplemq.redis.RedisPublisher;

/**
 * 设备命令分发器,实现{@link IMessageAdapter}接口,将redis操作与业务逻辑隔离<br>
 * 从设备命令频道或任务队列得到设备指令{@link DeviceInstruction},并将交给{@link ItemAdapter}执行<br>
 * 收到的设备命令将按收到命令的顺序在线程池中顺序执行
 * @author guyadong
 *
 */
public abstract class BaseDispatcher implements IMessageAdapter<DeviceInstruction>{
    private static final Logger logger = LoggerFactory.getLogger(BaseDispatcher.class);

    private static final Supplier<String> NULL_SUPPLIER = Suppliers.ofInstance(null);
	protected final int deviceId;
	/** 命令请求类型 */
	protected final ReqCmdType reqType;

	private final RedisPublisher publisher;
	/**  是否自动注销标志 */
	private final AtomicBoolean autoUnregisterCmdChannel = new AtomicBoolean(false);
	/** 设备命令序列号验证器 */
	protected Predicate<Integer> cmdSnValidator = Predicates.alwaysTrue();
	private ItemAdapter itemAdapter ;
	/**
	 * 当前设备的MAC地址(HEX字符串)
	 */
	private String selfMac;
	private Supplier<String> channelSupplier = NULL_SUPPLIER;
	private boolean enable = true;
	private volatile Channel<DeviceInstruction> channel;
	/**
	 * 构造方法<br>
	 * @param deviceId 当前设备ID,应用项目应确保ID是有效的
	 * @param reqType 设备命令请求类型
	 * @param jedisPoolLazy 
	 */
	protected BaseDispatcher(int deviceId, ReqCmdType reqType, JedisPoolLazy jedisPoolLazy) {	
		checkArgument(reqType == ReqCmdType.MULTI  || reqType == ReqCmdType.TASKQUEUE,
				"INVALID reqType,required %s or %s",ReqCmdType.MULTI,ReqCmdType.TASKQUEUE);
		this.deviceId= deviceId;
		this.reqType = reqType;
		this.publisher = RedisFactory.getPublisher(checkNotNull(jedisPoolLazy,"jedisPoolLazy is null"));
		
	}
	/**
	 * 构造方法<br>
	 * @param deviceId 当前设备ID,应用项目应确保ID是有效的
	 * @param reqType 设备命令请求类型
	 */
	protected BaseDispatcher(int deviceId, ReqCmdType reqType){
		this(deviceId, reqType, JedisPoolLazy.getDefaultInstance());
	}

	/**
	 * @param deviceInstruction
	 * @return
	 */
	private JSONObject makeItemJSON(DeviceInstruction deviceInstruction){		
		String path = deviceInstruction.getCmdpath();
		checkArgument(!Strings.isNullOrEmpty(path ),"path of cmd is null or empty");
		JSONObject json = new JSONObject();

		BaseItem item = getItemAdapterChecked().getRoot().findChecked(path);
		json.fluentPut(ITEM_FIELD_PATH,path)
			.fluentPut(REQ_FIELD_CMDSN,deviceInstruction.getCmdSn())
			.fluentPut(REQ_FIELD_ACKCHANNEL, deviceInstruction.getAckChannel())
			.fluentPut(REQ_FIELD_REQTYPE,  reqType);
		Map<String, ?> parameters = deviceInstruction.getParameters();
		if(parameters != null){
			// 填入设备命令参数根据
			if(item instanceof BaseOption<?>){
				Object value = parameters.get(OPTION_FIELD_VALUE);
				if(value != null){
					json.put(OPTION_FIELD_VALUE, value);
				}
			}else if(item instanceof CmdItem){
				json.put(REQ_FIELD_PARAMETERS, parameters);
			}
		}
		json.put(REQ_FIELD_ACKCHANNEL, deviceInstruction.getAckChannel());
		return json;
	}
	/**
	 * 执行指定的设备命令并向命令响应频道返回命令结果
	 */
	@Override
	public void onSubscribe(DeviceInstruction deviceInstruction) {
		try {
			if(validate(deviceInstruction)){
				JSONObject itemJson = makeItemJSON(deviceInstruction);
				getItemAdapterChecked().onSubscribe(itemJson);
			}
		} catch (Exception e) {
			// 捕获所有异常发到ack响应频道
			logger.error(e.getMessage());
			Ack<Object> ack = new Ack<Object>()
					.setDeviceId(deviceId)
					.setDeviceMac(selfMac)
					.setCmdSn(deviceInstruction.getCmdSn())
					.writeError(e);
			String ackChannel = deviceInstruction.getAckChannel();
			if(!Strings.isNullOrEmpty(ackChannel)){
				try {
					publisher.publish(new Channel<>(ackChannel, Ack.class), ack);
				} catch (Exception e2) {
					logger.error(e2.getMessage());
				}
			}			
		}

	}
	/**
	 * 验证{@link DeviceInstruction}对象的合法性
	 * @param deviceInstruction
	 * @return 验证通过返回{@code true},否则返回{@code false}
	 */
	protected boolean validate(DeviceInstruction deviceInstruction){
		int cmdSn = deviceInstruction.getCmdSn();
		// 设备命令序列号有效才执行设备命令
		if(!cmdSnValidator.apply(cmdSn)){
			logger.warn("INVALID cmd serial number: {}",cmdSn);
			return false;
		}
		return true;
	}
	protected abstract void doRegister(Channel<DeviceInstruction> channel);
	protected abstract void doUnregister(String channel);
	/**
	 * 当前对象注册到指定的频道,重复注册无效
	 * @return 当前对象
	 */
	public BaseDispatcher register(){
		// double check
		if(channel == null){
			synchronized (this) {
				if(channel == null){
					checkState(channelSupplier != NULL_SUPPLIER,"channelSupplier is uninitialized");
					String name = channelSupplier.get();
					checkState(!Strings.isNullOrEmpty(name),"INVALID channel name from channelSupplier");
					channel = new Channel<>(name,DeviceInstruction.class,this);
					doRegister(channel);
				}				
			}
		}
		return this;
	}
	/**
	 * 注销所有使用当前对象作处理器的频道
	 * @return 当前对象
	 */
	public BaseDispatcher unregister(){
		if(channel != null){
			synchronized (this) {
				if(channel != null){
					doUnregister(channel.name);
					logger.debug("unregister cmd channels {}",channel.name);
					channel = null;
				}
			}
		}
		return this;
	}
	/**
	 * 设置命令序列号验证器
	 * @param cmdSnValidator 为{@code null}无效
	 * @return 当前对象
	 */
	public BaseDispatcher setCmdSnValidator(Predicate<Integer> cmdSnValidator) {
		if(null != cmdSnValidator){
			this.cmdSnValidator = cmdSnValidator;
		}
		return this;
	}
	/**
	 * 设置程序退出时自动执行{@link #unregisterAll()}
	 * @return 当前对象
	 */
	public BaseDispatcher autoUnregister(){
		if(this.autoUnregisterCmdChannel.compareAndSet(false, true)){
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					try {
						unregister();	
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}
					
				}
			});
		}
		return this;
	}
	private ItemAdapter getItemAdapterChecked() {
		return checkNotNull(itemAdapter,"itemAdapter is not initialized");
	}
	/**
	 * @return 返回dtalk菜单引擎对象
	 */
	public ItemAdapter getItemAdapter() {
		return itemAdapter;
	}
	/**
	 * 设置dtalk菜单引擎对象
	 * @param itemAdapter 菜单引擎,不可为{@code null} 
	 * @return 当前对象
	 */
	public BaseDispatcher setItemAdapter(ItemAdapter itemAdapter) {
		this.itemAdapter = checkNotNull(itemAdapter,"itemAdapter is null");
		return this;
	}
	/**
	 * @return 返回当前对象的命令请求类型
	 */
	public ReqCmdType getReqType() {
		return reqType;
	}
	/**
	 * @return 返回当前设备的MAC地址
	 */
	public String getSelfMac() {
		return selfMac;
	}
	/**
	 * 设置当前设备的MAC地址
	 * @param selfMac
	 */
	public void setSelfMac(String selfMac) {
		this.selfMac = selfMac;
	}
	/**
	 * 将当前对象转为子类
	 * @return 当前对象
	 */
	@SuppressWarnings("unchecked")
	public final <T extends BaseDispatcher> T self(){
		return (T)this;
	}
	/**
	 * 将当前对象转为指定的子类
	 * @param clazz
	 * @return 当前对象
	 */
	public final <T extends BaseDispatcher> T self(Class<T> clazz){
		return checkNotNull(clazz,"clazz is null").cast(this);
	}
	public Supplier<String> getChannelSupplier() {
		return channelSupplier;
	}
	public BaseDispatcher setChannelSupplier(Supplier<String> channelSupplier) {
		this.channelSupplier = checkNotNull(channelSupplier,"channelSupplier is null");
		return this;
	}
	public boolean isEnable() {
		return enable;
	}
	public BaseDispatcher setEnable(boolean enable) {
		this.enable = enable;
		if(enable){
			register();
		}else{
			unregister();
		}
		return this;
	}
}