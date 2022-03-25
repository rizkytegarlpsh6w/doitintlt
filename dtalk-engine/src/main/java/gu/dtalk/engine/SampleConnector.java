package gu.dtalk.engine;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;

import gu.dtalk.Ack;
import gu.dtalk.ConnectReq;
import gu.dtalk.DeviceInfoProvider;
import gu.simplemq.Channel;
import gu.simplemq.IMessageAdapter;
import gu.simplemq.exceptions.SmqUnsubscribeException;
import gu.simplemq.json.BaseJsonEncoder;
import gu.simplemq.redis.JedisPoolLazy;
import gu.simplemq.redis.RedisFactory;
import gu.simplemq.redis.RedisPublisher;
import gu.simplemq.redis.RedisSubscriber;
import net.gdface.utils.FaceUtilits;
import static gu.dtalk.CommonConstant.*;
import static com.google.common.base.Preconditions.*;
import static gu.dtalk.CommonUtils.*;

/**
 * 设备端连接控制器简单实现<br>
 * 接收连接请求并验证请求合法性，如果连接请求有效，则将请求频道名封装到{@link Ack}中发送给管理端<br>
 * 管理端有了请求频道名才可以向设备端发送菜单命令(item)请求.
 * @author guyadong
 *
 */
public class SampleConnector implements IMessageAdapter<String>, RequestValidator {
	private static class SingletonTimer{
		private static final Timer instnace = new Timer(true);
	}
	private Timer timer;
	private Timer getTimer(){
		if(timer == null){
			timer = SingletonTimer.instnace;
		}
		return timer;
	}
	
	/**
	 * 当前连接的CLIENT端MAC地址
	 */
	private String connectedMAC;
	private final RedisPublisher ackPublisher;
	/**
	 * 当前连接的CLIENT端的请求频道
	 */
	private String requestChannel;
	private long idleTimeLimit = DEFAULT_IDLE_TIME_MILLS;
	private long timerPeriod = 2000;
	private ItemAdapter itemAdapter;
	private final RedisSubscriber subscriber;
	private RequestValidator requestValidator;
	public final static DeviceInfoProvider DEVINFO_PROVIDER = getDeviceInfoProvider();
	/**
	 * SPI(Service Provider Interface)机制加载 {@link DeviceInfoProvider}实例,没有找到返回默认实例
	 * @return
	 */
	private static DeviceInfoProvider getDeviceInfoProvider() {		
		ServiceLoader<DeviceInfoProvider> providers = ServiceLoader.load(DeviceInfoProvider.class);
		Iterator<DeviceInfoProvider> itor = providers.iterator();
		if(!itor.hasNext()){
			return DefaultDevInfoProvider.INSTANCE;
		}
		return itor.next();
	}
	
	public SampleConnector(JedisPoolLazy pool) {
		ackPublisher = RedisFactory.getPublisher(pool);
		subscriber = RedisFactory.getSubscriber(pool);
		requestValidator = this;
		// 定时检查itemAdapter工作状态，当itemAdapter 空闲超时，则中止频道
		getTimer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				Channel<?> c = subscriber.getChannel(requestChannel);
				if(null != c){
					ItemAdapter adapter = (ItemAdapter) c.getAdapter();
					long lasthit = adapter.lastHitTime();
					if(System.currentTimeMillis() - lasthit > idleTimeLimit){
						subscriber.unregister(requestChannel);
						requestChannel = null;
					}
				}
			}
		}, 0, timerPeriod);

	}
	
	/**
	 * 将连接请求字符串解析为{@link ConnectReq}对象，通过验证密码来确定连接是否有效
	 * @see gu.dtalk.engine.RequestValidator#validate(java.lang.String)
	 */
	@Override
	public String validate(String connstr) throws Exception {
		ConnectReq req = BaseJsonEncoder.getEncoder().fromJson(connstr, ConnectReq.class);
		checkArgument(req != null,"NULL REQUEST");
		
		String admPwd = checkNotNull(DEVINFO_PROVIDER.getPassword(),"admin password for device is null");
		checkArgument(!Strings.isNullOrEmpty(req.mac),"NULL REQUEST MAC ADDRESS");
		checkArgument(!Strings.isNullOrEmpty(req.pwd),"NULL REQUEST PASSWORD");
		checkArgument(!Strings.isNullOrEmpty(admPwd),"NULL ADMIN PASSWORD");
		String pwdmd5 = FaceUtilits.getMD5String(admPwd.getBytes());

		checkState(pwdmd5.equalsIgnoreCase(req.pwd),"INVALID REQUEST PASSWORD");
		return req.mac;
	}
	/** 
	 * 处理来自管理端的连接请求<br>
	 * 如果收到的数据无法解析成{@link ConnectReq}实例则忽略
	 * 如果连接密码不匹配或其他管理端已经连接返回错误信息,否则返回随机生成的管理操作频道名。
	 * @param connstr 连接请求
	 */
	@Override
	public void onSubscribe(String connstr) throws SmqUnsubscribeException {
		Ack<String> ack = new Ack<String>().setStatus(Ack.Status.OK);
		String ackChannel = null;
		try{
			String reqMAC = requestValidator.validate(connstr);
			ackChannel = getAckChannel(reqMAC);
			checkArgument(!Strings.isNullOrEmpty(reqMAC),"the mac address of request client is null");
			checkState(connectedMAC ==null || connectedMAC.equals(reqMAC),"ANOTHER CLIENT LOCKED");
			connectedMAC = reqMAC;
			// 密码匹配则发送请求频道名
			if(requestChannel == null){
				// 生成请求频道名
				requestChannel = String.format("%s_dtalk_%d", 
						FaceUtilits.toHex(DEVINFO_PROVIDER.getMac()),
						System.currentTimeMillis()&0xffff);
			}
			// 请求频道名作为响应消息返回值
			ack.setValue(requestChannel);
			if(null == subscriber.getChannel(requestChannel)){
				checkNotNull(itemAdapter,"Dtalk ENGINE NOT READY").setAckChannel(ackChannel);
				final String ac = ackChannel;
				// 必须在另开线程执行注册，否则会造成onSubscribe调用者JedisPubSub状态异常
				new Thread(){
					@Override
					public void run() {
						// 订阅请求频道用于命令发送
						Channel<JSONObject> c = new Channel<JSONObject> (requestChannel,JSONObject.class,itemAdapter);						
						subscriber.register(c);
						System.out.printf("Connect created(建立连接)for client:%s\n",connectedMAC);
						System.out.printf("request channel %s \n"
								                 + "ack channel       %s:\n", c.name,ac);
					}}.start();

			}
		}catch(JSONException e){
			// 忽略无法解析成ConnectReq请求对象的数据
		}catch(Exception e){
			ack.setStatus(Ack.Status.ERROR).setErrorMessage(e.getMessage());
		}
		if(ackChannel != null){
			// 向响应频道发送响应消息
			Channel<Ack<String>> channel = 
					new Channel<Ack<String>>(ackChannel,new TypeReference<Ack<String>>() {}.getType());
			ackPublisher.publish(channel, ack);
		}
	}

	public SampleConnector setItemAdapter(ItemAdapter adapter) {
		this.itemAdapter = adapter;
		return this;
	}
	/**
	 * 设置执行定时时任的{@link Timer}实例,如果不指定则使用内置的{@link Timer}实例，
	 * 内置实例会在JVM结束自动cancel<br>
	 * @param timer
	 * @return
	 */
	public RequestValidator setTimer(Timer timer) {
		if(timer != null){
			this.timer = timer;
		}
		return this;
	}

	/**
	 * 空闲时间限制(毫秒),超过此时间,自动中断连接
	 * @param idleTimeLimit
	 * @return
	 */
	public RequestValidator setIdleTimeLimit(long idleTimeLimit) {
		if(idleTimeLimit >0){
			this.idleTimeLimit = idleTimeLimit;
		}
		return this;
	}

	/**
	 * 设置定义检查连接的任务时间间隔(毫秒)
	 * @param timerPeriod
	 * @return
	 */
	public RequestValidator setTimerPeriod(long timerPeriod) {
		if(timerPeriod > 0){
		this.timerPeriod = timerPeriod;
		}
		return this;
	}

	public RequestValidator getRequestValidator() {
		return requestValidator;
	}

	/**
	 * 设置连接请求验证接口实例，如果不指定，默认使用基于{@link ConnectReq}格式的请求验证
	 * 参见 {@link #validate(String)}
	 * @param requestValidator 不可为{@code null}
	 * @return
	 */
	public SampleConnector setRequestValidator(RequestValidator requestValidator) {
		this.requestValidator = checkNotNull(requestValidator);
		return this;
	}

}
