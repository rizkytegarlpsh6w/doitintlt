package gu.dtalk.engine;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;

import gu.dtalk.Ack;
import gu.dtalk.CommonUtils;
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
 * 管理端连接控制器简单实现
 * @author guyadong
 *
 */
public class SampleConnector implements IMessageAdapter<String> {
	private static final Logger logger = LoggerFactory.getLogger(SampleConnector.class);
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
	
	private ConnectReq curconnect;
	private final RedisPublisher ackPublisher;
	private String workChannel;
	private long idleTimeLimit = DEFAULT_IDLE_TIME_MILLS;
	private long timerPeriod = 2000;
	private ItemAdapter itemAdapter;
	private final RedisSubscriber subscriber;
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
		
		// 定时检查itemAdapter工作状态，当itemAdapter 空闲超时，则中止频道
		getTimer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				Channel<?> c = subscriber.getChannel(workChannel);
				if(null != c){
					ItemAdapter adapter = (ItemAdapter) c.getAdapter();
					long lasthit = adapter.lastHitTime();
					if(System.currentTimeMillis() - lasthit > idleTimeLimit){
						subscriber.unregister(workChannel);
						workChannel = null;
					}
				}
			}
		}, 0, timerPeriod);

	}
	/** 
	 * 处理来自管理端的连接请求<br>
	 * 如果收到的数据无法解析成{@link ConnectReq}实例则忽略
	 * 如果连接密码不匹配或其他管理端已经连接返回错误信息,否则返回随机生成的管理操作频道名。
	 * @param connstr 连接请求
	 */
	@Override
	public void onSubscribe(String connstr) throws SmqUnsubscribeException {
		logger.debug("request:"+connstr);
		Ack<String> ack = new Ack<String>().setStatus(Ack.Status.OK);
		ConnectReq req = null;
		try{
			req = BaseJsonEncoder.getEncoder().fromJson(connstr, ConnectReq.class);
			checkArgument(req != null,"NULL REQUEST");
			
			String admPwd = checkNotNull(DEVINFO_PROVIDER.getPassword(),"admin password for device is null");
			checkArgument(!Strings.isNullOrEmpty(req.mac),"NULL REQUEST MAC ADDRESS");
			checkArgument(!Strings.isNullOrEmpty(req.pwd),"NULL REQUEST PASSWORD");
			checkArgument(!Strings.isNullOrEmpty(admPwd),"NULL ADMIN PASSWORD");
			String pwdmd5 = FaceUtilits.getMD5String(admPwd.getBytes());

			checkState(pwdmd5.equalsIgnoreCase(req.pwd),"INVALID REQUEST PASSWORD");
			checkState(curconnect ==null || curconnect.mac.equals(req.mac),"ANOTHER CLIENT LOCKED");
			// 密码匹配则发送工作频道名
			if(workChannel == null){
				workChannel = String.format("%s_%d", 
						FaceUtilits.toHex(DEVINFO_PROVIDER.getMac()),
						System.currentTimeMillis()&0xffff);
			}

			ack.setValue(workChannel);
			if(null == subscriber.getChannel(workChannel)){
				final ConnectReq request = new ConnectReq(req.mac,req.pwd);
				new Thread(){
					@Override
					public void run() {
						Channel<JSONObject> c = new Channel<JSONObject> (workChannel,JSONObject.class,itemAdapter);
						itemAdapter.setAckChannel(getAckChannel(request.mac));
						subscriber.register(c);
						System.out.printf("Connect created(建立连接) %s for client:%s\n", c.name,request.mac);
					}}.start();

			}
		}catch(JSONException e){
			// 忽略无法解析成ConnectReq请求对象的数据
		}catch(Exception e){
			ack.setStatus(Ack.Status.ERROR).setErrorMessage(e.getMessage());
		}
		if(req!=null){
			// 向响应频道发送响应消息
			Channel<Ack<String>> channel = new Channel<Ack<String>>(CommonUtils.getAckChannel(req.mac),new TypeReference<Ack<String>>() {}.getType());
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
	public SampleConnector setTimer(Timer timer) {
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
	public SampleConnector setIdleTimeLimit(long idleTimeLimit) {
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
	public SampleConnector setTimerPeriod(long timerPeriod) {
		if(timerPeriod > 0){
		this.timerPeriod = timerPeriod;
		}
		return this;
	}

}
